package fileSync.model;

import java.io.Serializable;

public enum MessageType implements Serializable {

	SIMPLE_MESSAGE, INIT_CLIENT, FILE_BLOCK, DELTA_SYNC, REQUEST_TO_UPLOAD, REQUEST_TO_DOWNLOAD, CONFIRMED, SUSPEND,
	RESUME, DELETE, UPDATE;

}
