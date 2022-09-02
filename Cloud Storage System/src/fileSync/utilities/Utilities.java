package fileSync.utilities;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import fileSync.model.FileBlock;
import fileSync.model.Message;

public class Utilities {

	// Updating the tree.
	public static void updateTree(DefaultMutableTreeNode root, File file) {

		if (file != null && file.listFiles() != null) {
			for (File each : file.listFiles()) {

				DefaultMutableTreeNode subRoot = new DefaultMutableTreeNode(each.getName());
				if (each.isDirectory()) {
					updateTree(subRoot, each);
				}
				root.add(subRoot);

			}
		}

	}

	// getting checksums..
	public static Map<String, String> findChecksums(File file) {

		Map<String, String> checksums = new HashMap<>();
		if (file != null && file.listFiles() != null) {
			for (File each : file.listFiles()) {
				if (each.exists() && each.isFile()) {
					checksums.put(checksum(each), each.getName());
				}
			}
		}
		return checksums;

	}

	// Finding the checksum.
	public static String checksum(File file) {

		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			FileInputStream inputStream = new FileInputStream(file);
			byte[] bytes = new byte[1024];
			String output = "";
			int size = 0;
			while ((size = inputStream.read(bytes)) != -1) {
				messageDigest.update(bytes, 0, size);
			}
			inputStream.close();
			bytes = messageDigest.digest();
			for (int i = 0; i < bytes.length; i++) {
				output += (Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			return output;
		} catch (Exception e) {
			return null;
		}

	}

	// Converting to bytes..
	public static byte[] toBytes(Message message) {

		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream outputStream = new ObjectOutputStream(byteStream);
			outputStream.writeObject(message);
			outputStream.close();
			return byteStream.toByteArray();
		} catch (IOException e) {
		}
		return null;

	}

	// Converting from bytes..
	public static Message fromBytes(byte[] data) {

		try {
			ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(data));
			Message message = (Message) iStream.readObject();
			iStream.close();
			return message;
		} catch (Exception e) {
		}
		return null;

	}

	// get file blocks.
	public static List<FileBlock> getFileBlocks(File file, boolean blockNumbers) throws IOException {

		List<FileBlock> blocks = new ArrayList<>();
		int blockNumber = 1, length = 0;
		byte[] block = new byte[50000];

		try (FileInputStream fileStream = new FileInputStream(file);
				BufferedInputStream bufferedStream = new BufferedInputStream(fileStream)) {

			while ((length = bufferedStream.read(block)) > 0) {

				blocks.add(new FileBlock(blockNumbers ? filename(file.getName(), blockNumber++) : file.getName(), block,
						length));
				block = new byte[50000];

			}

		}
		blockNumber = 1;
		for (FileBlock each : blocks) {
			each.setCurrentBlockNumber(blockNumber);
			each.setTotalBlocks(blocks.size());
			blockNumber++;
		}
		return blocks;

	}

	private static String filename(String name, int block) {

		String extension = "";
		if (name.contains(".")) {
			extension = name.substring(name.lastIndexOf('.') + 1, name.length());
			name = name.substring(0, name.lastIndexOf('.'));
		} else {
			extension = "dat";
		}
		return String.format("%s_%03d", name, block) + "." + extension;

	}

}
