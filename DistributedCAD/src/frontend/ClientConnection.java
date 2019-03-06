package frontend;

import java.net.Socket;

public class ClientConnection implements Runnable {
	
	private Socket socket;

	public ClientConnection(Socket s) {
		socket = s;
	}

	@Override
	public void run() {
		// Wait for connection request
		
		// Main message receive loop
	}

}
