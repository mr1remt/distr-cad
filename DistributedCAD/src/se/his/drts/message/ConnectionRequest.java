package se.his.drts.message;

import java.util.UUID;

public class ConnectionRequest extends UniqueMessage {
	private static UUID uuid = UUID.fromString("eff23d4d-325d-4f1d-b7b9-b9811e97a510");
	
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
