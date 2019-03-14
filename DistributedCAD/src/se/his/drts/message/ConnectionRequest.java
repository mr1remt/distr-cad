package se.his.drts.message;

import java.util.UUID;

public class ConnectionRequest extends UniqueMessage {
	private static UUID uuid = UUID.fromString("979fde02-de91-4fe1-bd21-4a6253758218");
	
	private String ID;
	
	// TODO not used by the client ! ?
	
	protected ConnectionRequest() {
		super(ConnectionRequest.uuid);
	}
	
	public ConnectionRequest(String ID) {
		super(ConnectionRequest.uuid);
		this.ID = ID;
	}
	public String getID() {
		return ID;
	}
}
