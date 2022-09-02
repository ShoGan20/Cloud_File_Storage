package fileSync;

import java.io.IOException;

import fileSync.ui.ClientUI;

public class ClientRunner {

	public static void main(String[] args) throws IOException {

		(new ClientUI()).init();

	}

}
