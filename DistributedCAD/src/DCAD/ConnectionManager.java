package DCAD;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import se.his.drts.message.MessagePayload;

public class ConnectionManager extends CadDocument{
	
	private Cad cad;
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	
	public ConnectionManager(Cad cad, String serverAddress, int serverPort) {
		this.cad = cad;
		
		//set up socket
		try {
			socket = new Socket(serverAddress, serverPort);
			writer = new PrintWriter(socket.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			//TODO retry setup?
		}
		handshake();
		
	}
	
	public void handshake() {
		//TODO send name or id to the front end 
		
	}
	
	public void sendMessage(MessagePayload messagePayload) {
		
		//TODO keep a list of messages sent 
		
		//TODO send the message 
		
	}
	
	public void sendObject(GObject object) {
		//TODO send a message with the newly added object 
		
		sendMessage(null);
	}
	
	public void receive() {
		// TODO receive an object that should be added to the list, or removed
		cad.addObjectToGUI(null);
	}
	
	
}
