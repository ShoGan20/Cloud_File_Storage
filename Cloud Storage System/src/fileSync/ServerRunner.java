package fileSync;

import java.io.IOException;

import fileSync.ui.ServerUI;

public class ServerRunner {

	public static void main(String[] args) throws IOException {

		(new ServerUI()).init();

	}

}
