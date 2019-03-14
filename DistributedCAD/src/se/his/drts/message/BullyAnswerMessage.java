package se.his.drts.message;

import java.util.UUID;

public class BullyAnswerMessage extends UniqueMessage{
	
	private static UUID subclassUUID = UUID.fromString("94dc37e3-c157-4c46-838a-9203631dd789");
	
	public BullyAnswerMessage() {
		super(subclassUUID);
	}
	
	@Override
	public String toString() {
		return "PrimaryAnnouncement";
	}
	
}
