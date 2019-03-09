package se.his.drts.message;

import java.util.UUID;

public class PrimaryAnnouncement extends UniqueMessage{
	private static UUID uuid = UUID.fromString("979fde02-de91-4fe1-bd21-4a6253758218");
	private String name;
	protected PrimaryAnnouncement() {
		super(PrimaryAnnouncement.uuid);
	}
	public PrimaryAnnouncement(String name) {
		super(PrimaryAnnouncement.uuid);
		this.name = name;
	}
	public final String getName() {
		return name;
	}
	@Override
	public String toString() {
		return "TestUniqueMessage [name=" + name + ", toString()=" + super.toString() + "]";
	}
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && this.name.equals(((PrimaryAnnouncement)obj).name);
	}
	@Override
	public int compareTo(MessagePayload arg0) {
		final int n = super.compareTo(arg0);
		if (n!=0) {
			return n;
		}
		return this.name.compareTo(((PrimaryAnnouncement)arg0).name);
	}
	@Override
	public int hashCode() {
		return super.hashCode()+name.hashCode();
	}
	
}
