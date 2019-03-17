package frontend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;
import java.util.UUID;

import se.his.drts.message.ClientConnectionRequest;
import se.his.drts.message.DeleteObjectRequest;
import se.his.drts.message.DrawObjectRequest;
import se.his.drts.message.MessageConfirmed;
import se.his.drts.message.MessagePayload;
import se.his.drts.message.RetrieveObjectsRequest;
import se.his.drts.message.UniqueMessage;

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
		
		// Main message receive loop
		while(receiveMessage()) { }

System.out.println("client crashed");
		// client has crashed
		
	}
	public boolean receiveMessage() {
		String message = "";
		try {
			message = reader.readLine();
		} catch (IOException e) {
			try {
				socket.close();
			} catch (IOException e1) {
			}
			return false;
		}

		byte[] bytes = message.getBytes();
		
		Optional<MessagePayload> mp = MessagePayload.createMessage(bytes);
		
		UniqueMessage uniqueMessage = (UniqueMessage) mp.get();
		
System.out.println("message recieved " + uniqueMessage);
		
		if (uniqueMessage instanceof ClientConnectionRequest) {			
			ClientConnectionRequest clientConnectionRequestMessage = (ClientConnectionRequest) uniqueMessage;
			if (clientID == null) {
				this.clientID = clientConnectionRequestMessage.getClientID();
			}
		}
		else if (uniqueMessage instanceof DrawObjectRequest
				|| uniqueMessage instanceof DeleteObjectRequest
				|| uniqueMessage instanceof RetrieveObjectsRequest) {			
			sendMessageRM(uniqueMessage);
		}
		
		return true;
	}
	public void sendMessageConfirmed() {
		MessageConfirmed messageConfirmed = new MessageConfirmed(UUID.randomUUID());
		sendMessageClient(messageConfirmed);
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
