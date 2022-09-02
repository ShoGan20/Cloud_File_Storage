package fileSync.model;

import java.io.Serializable;

public class FileBlock implements Serializable {

	private String filename;
	private byte[] bytes;
	private int length;
	private int totalBlocks;
	private int currentBlockNumber;

	public FileBlock(String filename, byte[] bytes, int length) {

		this.filename = filename;
		this.bytes = bytes;
		this.length = length;

	}

	public String getFilename() {

		return filename;

	}

	public byte[] getBytes() {

		return bytes;

	}

	public int getLength() {

		return length;

	}

	public int getTotalBlocks() {

		return totalBlocks;

	}

	public int getCurrentBlockNumber() {

		return currentBlockNumber;

	}

	public void setTotalBlocks(int totalBlocks) {

		this.totalBlocks = totalBlocks;

	}

	public void setCurrentBlockNumber(int currentBlockNumber) {

		this.currentBlockNumber = currentBlockNumber;

	}

}
