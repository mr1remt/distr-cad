package frontend;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

import se.his.drts.message.ClientResponseMessage;

public class Frontend {
	
	public static Frontend frontend;
	
	private RMConnection rmConn;
	
	private ServerSocket serverSocket;
	
	private HashMap<String, ClientConnection> clientList;
	
	public Frontend() {
		clientList = new HashMap<String, ClientConnection>();
	}

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
				new Thread(
					new ClientConnection(serverSocket.accept(), rmConn)
				).start();
				
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Register a new client by it's clientID
	 * @param clientID
	 * @param client
	 */
	public void registerNewClient(String clientID, ClientConnection client) {
		clientList.put(clientID, client);
	}
	
	public void unregisterClient(ClientConnection client) {
		clientList.remove(client.getClientID(), client)
	}

	/**
	 * Forward the message 'msg' to the correct client if we know who they are
	 * @param msg
	 */
	public void forwardResponse(ClientResponseMessage msg) {
		ClientConnection targetClient = clientList.get(msg.getClientID());
		if (targetClient == null) return;
		
		targetClient.sendMessageClient(msg);
	}

	public static void main(String[] args) {
		frontend = new Frontend();
		frontend.connect();
		frontend.listenForClientConnections();
	}

}
