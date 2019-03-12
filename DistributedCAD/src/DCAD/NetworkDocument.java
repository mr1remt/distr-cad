package DCAD;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

import se.his.drts.message.MessagePayload;

public class NetworkDocument extends CadDocument{
	
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	
	private LinkedList<GObject> objectList = new LinkedList<GObject>();

	public NetworkDocument(String serverAddress, int serverPort) {		
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
		//cad.addObjectToGUI(null);
	}

	@Override
	public void addGObject(GObject object) {
		// TODO Auto-generated method stub
		
		objectList.addLast(object);
		
	}

	@Override
	public void removeLastGObject() {
		// TODO Auto-generated method stub
		objectList.removeLast();
	}

	@Override
	public int size() {
		return objectList.size();
	}
	
	public Iterator<GObject> iterator() {
		return objectList.iterator(); 
	}
	
	
}
