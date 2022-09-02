package fileSync.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import fileSync.model.FileBlock;
import fileSync.model.Message;
import fileSync.model.MessageType;
import fileSync.model.Updator;
import fileSync.utilities.Utilities;

public class Client extends Thread {

	// Attributes..
	private DatagramSocket socket;
	private InetAddress host;
	private final int SERVER_PORT = 1996;
	private String root;
	private Updator updator;
	private volatile boolean suspended;
	private Set<String> sync;
	private int client;

	// Constructor..
	public Client(Updator updator, String root) throws IOException {

		this.updator = updator;
		this.root = root;
		host = InetAddress.getLocalHost();
		socket = new DatagramSocket();
		suspended = false;
		sync = Collections.synchronizedSet(new TreeSet<String>());

	}

	@Override
	public void run() {

		// 2-MB Block.
		byte[] bytes;
		DatagramPacket packet;

		// Keep on running always..
		while (true) {

			bytes = new byte[2 * (1024 * 1024)];
			packet = new DatagramPacket(bytes, bytes.length);

			try {
				socket.receive(packet);
			} catch (IOException e) {
				break;
			}

			processPacket(bytes);

		}

	}

	private void processPacket(byte[] bytes) {

		new Thread(() -> {

			Message message = Utilities.fromBytes(bytes);

			if (message.getType() == MessageType.FILE_BLOCK) {

				takeBlock(message);

			} else if (message.getType() == MessageType.INIT_CLIENT) {

				int id = Integer.parseInt(message.getMessage());
				root += id;
				updator.updateTitle(id);
				client = id;
				deltaSync();

			} else if (message.getType() == MessageType.REQUEST_TO_UPLOAD) {

				File file = new File(root + "/" + message.getMessage());
				uploadFile(MessageType.FILE_BLOCK, file, true, true);

			} else if (message.getType() == MessageType.UPDATE) {

				File file = new File(root + "/" + message.getMessage());
				uploadFile(MessageType.UPDATE, file, false, false);

			} else if (message.getType() == MessageType.DELETE) {

				File file = new File(root + "/" + message.getMessage());
				file.delete();

			}

		}).start();

	}

	public void printSync() {

		String out = "*********************************\nSync\n" + "format -> : filename : status";
		for (String each : Utilities.findChecksums(new File(root)).values()) {
			if (sync.contains(each)) {
				out += "\n" + each + " : sync";
			} else {
				out += "\n" + each + " : completed";
			}
		}
		updator.append(out);

	}

	private void takeBlock(Message message) {

		if (message.getBlock().getCurrentBlockNumber() != message.getBlock().getTotalBlocks()) {
			this.sync.add(message.getMessage());
		} else {
			this.sync.remove(message.getMessage());
		}

		updator.append("\n****** files to be recieved ******\n" + ">> (*)Prepare to write the file : "
				+ message.getBlock().getFilename());
		File file = new File(root + "/" + message.getBlock().getFilename());

		try {

			FileOutputStream stream = new FileOutputStream(file);
			stream.write(message.getBlock().getBytes(), 0, message.getBlock().getLength());
			stream.close();

			updator.append("\n>> Recieve the signal of ending data transfer.");
			updator.append("\n>> Packet indices for file block: " + file.getAbsolutePath());
			updator.append("\n>> Finsh saving the file block: " + file.getName());

		} catch (IOException e) {
			updator.append("ERROR IN TAKING BLOCK: " + e.toString());
		}
		updator.updateTree();

	}

	// send to server.
	public synchronized void send(Message message) {

		if (message.getType() == MessageType.SUSPEND) {
			suspended = true;
			updator.append("***********************************" + "\nSUSPENDED THE OPERATIONS.\n");
		} else if (message.getType() == MessageType.RESUME) {
			suspended = false;
			updator.append("***********************************" + "\nRESUMED THE OPERATIONS.\n");
		}
		try {
			byte[] bytes = Utilities.toBytes(message);
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, host, SERVER_PORT);
			socket.send(packet);
			Thread.sleep(50);
		} catch (Exception e) {
			updator.append("ERROR IN SENDING PACKET: " + e.toString());
		}

	}

	// uploading the file to server..
	private void uploadFile(MessageType type, File file, boolean blockNumber, boolean delete) {

		new Thread(() -> {

			try {
				List<FileBlock> blocks = Utilities.getFileBlocks(file, blockNumber);
				for (FileBlock block : blocks) {

					while (suspended) {
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
						}
					}

					updator.append("\n*************************************\n\n" + ">> Send file block: "
							+ block.getFilename());

					send(new Message(type, file.getName(), block));

					updator.append("\n>> client_" + client + " signals the server that data transfer ends."
							+ "\n>> Finish sending file block: " + block.getFilename() + "\n");

				}
				if (delete) {
					file.delete();
				}
				updator.updateTree();
			} catch (IOException e) {
				updator.append("ERROR IN UPLOADING: " + e.toString());
			}

		}).start();

	}

	/*
	 * 
	 * 
	 * DELTA_SYNC
	 * 
	 * 
	 */
	private void deltaSync() {

		new Thread() {

			@Override
			public void run() {

				updator.append(
						"*********************************\n" + ">> client_" + client + " starts delta sync\n\n");

				// run always..
				while (true) {

					// Delta-Sync after every 5 seconds.
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					while (suspended) {
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
						}
					}

					updator.append("\n\n>> Send request: DELTASYNC\n" + "*********************************\n"
							+ " :: Request content\n");
					Map<String, String> checksum = Utilities.findChecksums(new File(root));
					if (checksum.isEmpty()) {
						updator.append("Request content is empty.");
					} else {
						updator.append("format -> : filename : checksum");
						for (Entry<String, String> each : checksum.entrySet()) {
							updator.append("\n" + each.getValue() + " : " + each.getKey());
						}
					}
					// Doing delta sync.
					send(new Message(MessageType.DELTA_SYNC, checksum));
					updator.updateTree();

					updator.append(
							" :: Response for: DELTASYNC\n" + "All file blocks are in sync or there is no file.");

				}

			}

		}.start();

	}

}
