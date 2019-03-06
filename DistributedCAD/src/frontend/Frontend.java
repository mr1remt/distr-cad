package frontend;

import java.io.IOException;
import java.net.ServerSocket;

public class Frontend {
	
	private ServerSocket serverSocket;

	public Frontend() {
		// Open TCP server socket
		try {
			serverSocket = new ServerSocket(50000);
		}catch (IOException e) {
			System.exit(-1);
		}
	}
	
	public void listenForClientConnections() {
		while (true) {
			try {
				new Thread(new ClientConnection(
					serverSocket.accept()
				));
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		Frontend f = new Frontend();
		f.listenForClientConnections();
	}

}
