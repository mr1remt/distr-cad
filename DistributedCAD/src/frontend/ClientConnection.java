package frontend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

import se.his.drts.message.ClientConnectionRequest;
import se.his.drts.message.DrawObject;
import se.his.drts.message.FetchState;
import se.his.drts.message.MessagePayload;
import se.his.drts.message.RemoveObject;

public class ClientConnection implements Runnable {
	
	private Socket socket;
	private String clientID;
	
	private PrintWriter writer;
	private BufferedReader reader;
	
	public ClientConnection(Socket s) {
		socket = s;
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
		
		// Main message receive loop
		
		while(receiveMessage()) {
			
		}
		
	}
	public boolean receiveMessage() {
		String message = "";
		try {
			message = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] bytes = message.getBytes();
		
		Optional<MessagePayload> mp = MessagePayload.createMessage(bytes);
		
		MessagePayload messagePayload = mp.get();
		
		if (messagePayload instanceof ClientConnectionRequest) {			
			ClientConnectionRequest clientConnectionRequestMessage = (ClientConnectionRequest) messagePayload;
			if (clientID == null) {
				this.clientID = clientConnectionRequestMessage.getID();
			}
			
		}
		else if (messagePayload instanceof DrawObject || messagePayload instanceof RemoveObject || messagePayload instanceof FetchState) {			
			sendMessageRM(messagePayload);
		}
		
		return true;
	}
	public void sendMessageClient(MessagePayload mp) {
		//TODO for sending a reply message from the replica manager to the client
		
		String message = mp.serializeAsString();

		writer.println(message);
		
	}
	
	public void sendMessageRM(MessagePayload mp) {
		
		//TODO send/forward the message to the replica manager
		
		/*TODO if the replica manager is available then send a message to the client confirming 
		 * that the message will be processed and that it can continue sending messages 
		 * */
		
		
		
	}

}
