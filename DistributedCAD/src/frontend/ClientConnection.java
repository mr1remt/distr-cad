package frontend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

import se.his.drts.message.ClientConnectionRequest;
import se.his.drts.message.ClientResponseMessage;
import se.his.drts.message.DeleteObjectRequest;
import se.his.drts.message.DrawObjectRequest;
import se.his.drts.message.MessagePayload;
import se.his.drts.message.RetrieveObjectsRequest;
import se.his.drts.message.UniqueMessage;

public class ClientConnection implements Runnable {
	
	private Socket socket;
	private String clientID;
	
	private PrintWriter writer;
	private BufferedReader reader;
	
	private RMConnection rmConnection;
	
	public ClientConnection(Socket s, RMConnection rmc) {
		socket = s;
		rmConnection = rmc;
		try {
			writer = new PrintWriter(socket.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		// Wait for connection request
		MessagePayload connMsg;
		while ((connMsg = receiveMessage()) == null);
		
		if (!(connMsg instanceof ClientConnectionRequest)) {
			System.out.println("Client didn't send a ClientConnectionRequest, disconnecting client");
			
			try {
				writer.close();
				reader.close();
				socket.close();
			}catch (IOException e) {
			}
			
			return;
		}
		
		// Save this clients ID and register the client
		clientID = ((ClientConnectionRequest) connMsg).getClientID();
		Frontend.frontend.registerNewClient(clientID, this);
		
		// Main message receive loop
		MessagePayload msg;
		while((msg = receiveMessage()) != null) {
			
			// If the message doesn't contain a return address, reply with a negative result
			
			if (msg instanceof DrawObjectRequest
					|| msg instanceof DeleteObjectRequest
					|| msg instanceof RetrieveObjectsRequest) {
				
				if (!rmConnection.send(msg)) {
					
					// If we couldn't send the message, report a failure to the client
					sendMessageClient(new ClientResponseMessage(
							(UniqueMessage) msg, false));
				}
			}
		}
		
	}
	
	public MessagePayload receiveMessage() {
		String jsonMsg = "";
		try {
			jsonMsg = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		Optional<MessagePayload> optMsg = MessagePayload.createMessage(jsonMsg.getBytes());
		if (!optMsg.isPresent()) {
			System.out.println("Failed to deserialize message");
			return null;
		}
		
		return optMsg.get();
	}
	
	/**
	 * Sends the message 'mp' to this client
	 * @param mp
	 */
	public void sendMessageClient(MessagePayload mp) {
		String message = mp.serializeAsString();
		writer.println(message);
	}

}
