package se.his.drts.message;

import java.util.UUID;

public class BullyElectionMessage extends UniqueMessage{
	
	private static UUID subclassUUID = UUID.fromString("45219dc5-415b-4136-aefa-5f06064aa7a2");
	
	public BullyElectionMessage() {
		super(subclassUUID);
	}
	
	@Override
	public String toString() {
		return "PrimaryAnnouncement";
	}
	
}
