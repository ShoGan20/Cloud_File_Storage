package fileSync.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {

	private MessageType type;
	private String message;
	private Map<String, String> checksums;
	private FileBlock block;

	public Message(String message) {

		this(MessageType.SIMPLE_MESSAGE, message, null);

	}

	public Message(MessageType type, String message) {

		this(type, message, null);

	}

	public Message(MessageType type, Map<String, String> checksums) {

		this(type, null, null);
		this.checksums = checksums;

	}

	public Message(MessageType type, String message, FileBlock block) {

		this.type = type;
		this.message = message;
		this.block = block;
		this.checksums = new HashMap<>();

	}

	public MessageType getType() {

		return type;

	}

	public String getMessage() {

		return message;

	}

	public FileBlock getBlock() {

		return block;

	}

	public Map<String, String> getChecksums() {

		return checksums;

	}

}
