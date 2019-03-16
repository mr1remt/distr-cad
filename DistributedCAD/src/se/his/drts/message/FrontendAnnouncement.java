package se.his.drts.message;

import java.util.UUID;

public class FrontendAnnouncement extends MessagePayload {
	
	private static UUID subclassUUID = UUID.fromString("f85e0551-be88-45eb-a53e-36871c09cccc");
	
	public FrontendAnnouncement() {
		super(subclassUUID);
	}
	
	@Override
	public String toString() {
		return "FrontendAnnouncement";
	}
	
}
