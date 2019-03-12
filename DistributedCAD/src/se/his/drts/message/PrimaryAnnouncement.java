package se.his.drts.message;

import java.util.UUID;

public class PrimaryAnnouncement extends UniqueMessage{
	
	private static UUID uuid = UUID.fromString("979fde02-de91-4fe1-bd21-4a6253758218");
	
	protected PrimaryAnnouncement() {
		super(PrimaryAnnouncement.uuid);
	}
	
	@Override
	public String toString() {
		return "PrimaryAnnouncement";
	}
	
}
