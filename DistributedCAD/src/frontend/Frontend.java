package frontend;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;

public class Frontend {
	
	private RMConnection rmConn;
	
	private ServerSocket serverSocket;
	
	private LinkedList<ClientConnection> clientConnections = new LinkedList<>();

	public void connect() {
		// Open TCP server socket
		try {
			serverSocket = new ServerSocket(50000);
		}catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		// Connect to the replica managers
		try {
			rmConn = new RMConnection();
			rmConn.connect();
		}catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void listenForClientConnections() {
		while (true) {
			try {
				// Create a new thread for every connected client
				ClientConnection client = new ClientConnection(serverSocket.accept());
				clientConnections.add(client);
				new Thread(client).start();
				
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		Frontend f = new Frontend();
		f.connect();
		f.listenForClientConnections();
	}

}
