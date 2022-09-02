package fileSync.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fileSync.model.Address;
import fileSync.model.FileBlock;
import fileSync.model.Message;
import fileSync.model.MessageType;
import fileSync.model.Updator;
import fileSync.utilities.Utilities;

public class Server extends Thread {

	// Attributes..
	private List<Address> clients;
	private DatagramSocket socket;
	private final int PORT = 1996;
	private Updator updator;
	private String root;
	private int clientNumber;
	private List<Address> suspended;

	// Constructor..
	public Server(Updator updator, String root) throws IOException {

		this.updator = updator;
		this.root = root;
		clients = new ArrayList<>();
		socket = new DatagramSocket(PORT);
		this.clientNumber = 1;
		suspended = Collections.synchronizedList(new ArrayList<>());

	}

	@Override
	public void run() {

		// 2-MB Block.
		byte[] bytes;
		DatagramPacket packet;

		// Keep on running always..
		while (true) {

			bytes = new byte[2000000];
			packet = new DatagramPacket(bytes, bytes.length);

			try {
				socket.receive(packet);
			} catch (IOException e) {
				break;
			}

			processPacket(bytes, new Address(packet.getAddress(), packet.getPort()));

		}

	}

	private void processPacket(byte[] bytes, Address address) {

		new Thread(() -> {

			Message message;
			message = Utilities.fromBytes(bytes);

			updator.append("\n\nNew Request is accepted." + "\n******** actionType ******** " + message.getType());

			if (!clients.contains(address)) {
				this.send(address, new Message(MessageType.INIT_CLIENT, String.valueOf(clientNumber)));
				clientNumber++;
				this.clients.add(address);
			}

			if (message.getType() == MessageType.DELTA_SYNC) {

				processDeltaSync(message, address);

			} else if (message.getType() == MessageType.FILE_BLOCK) {

				takeBlock(message);

			} else if (message.getType() == MessageType.REQUEST_TO_DOWNLOAD) {

				File file = new File(root + "/" + message.getMessage());
				uploadFile(file, address);

			} else if (message.getType() == MessageType.UPDATE) {

				for (int i = 0; i < 5; i++) {
					broadcast(new Message(MessageType.DELETE, message.getMessage()));
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
				}
				takeBlock(message);

			} else if (message.getType() == MessageType.SUSPEND) {

				updator.append("***********************************" + "\nSUSPENDED THE OPERATIONS FOR CLIENT.\n");
				suspended.add(address);

			} else if (message.getType() == MessageType.RESUME) {

				updator.append("***********************************" + "\nRESUMED THE OPERATIONS FOR CLIENT.\n");
				suspended.remove(address);

			}

		}).start();

	}

	// uploading the file to server..
	private void uploadFile(File file, Address address) {

		new Thread(() -> {

			try {
				for (FileBlock block : Utilities.getFileBlocks(file, false)) {

					while (suspended.contains(address)) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
					}

					updator.append("\n*************************************\n\n" + ">> Send file block: "
							+ block.getFilename());

					send(address, new Message(MessageType.FILE_BLOCK, file.getName(), block));

					updator.append("\n>> server signals the client that data transfer ends."
							+ "\n>> Finish sending file block: " + block.getFilename() + "\n");

				}
			} catch (IOException e) {
				updator.append("ERROR IN UPLOADING: " + e.toString());
			}

		}).start();

	}

	private void takeBlock(Message message) {

		File file = new File(root + "/" + message.getBlock().getFilename());

		updator.append("\n****** files to be recieved ******\n" + ">> (*)Prepare to write the file : "
				+ message.getBlock().getFilename());

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

	private void processDeltaSync(Message message, Address address) {

		updator.append(" :: Request content\n");
		Map<String, String> checksum = message.getChecksums();
		if (checksum.isEmpty()) {
			updator.append("Request content is empty.");
		} else {
			updator.append("format -> : filename : checksum");
			for (Entry<String, String> each : checksum.entrySet()) {
				updator.append("\n" + each.getValue() + " : " + each.getKey());
			}
		}

		Map<String, String> serverFiles = Utilities.findChecksums(new File(root));
		for (Entry<String, String> each : checksum.entrySet()) {

			// Checksum not found..
			if (!serverFiles.containsKey(each.getKey())) {

				if (serverFiles.containsValue(each.getValue())) {
					updator.append("\nUpdating file: " + each.getValue() + "\n");
					send(address, new Message(MessageType.UPDATE, each.getValue()));
				} else {
					// requesting again.
					send(address, new Message(MessageType.REQUEST_TO_UPLOAD, each.getValue()));
				}

			}

		}

		for (Entry<String, String> each : serverFiles.entrySet()) {

			if (!message.getChecksums().containsValue(each.getValue())) {

				uploadFile(new File(root + "/" + each.getValue()), address);

			}

		}

	}

	// send to client.
	public synchronized void send(Address address, Message message) {

		try {
			byte[] bytes = Utilities.toBytes(message);
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address.getHost(), address.getPort());
			socket.send(packet);
			Thread.sleep(50);
		} catch (Exception e) {
			updator.append("ERROR IN SENDING: " + e.toString());
		}

	}

	public synchronized void broadcast(Message message) {

		try {
			byte[] bytes = Utilities.toBytes(message);
			for (Address address : clients) {
				DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address.getHost(), address.getPort());
				socket.send(packet);
			}
		} catch (Exception e) {
			updator.append("ERROR IN BROADCAST: " + e.toString());
		}

	}

}
