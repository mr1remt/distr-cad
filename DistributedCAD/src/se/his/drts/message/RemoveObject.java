package se.his.drts.message;

import java.util.UUID;

import DCAD.GObject;

public class RemoveObject extends UniqueMessage {
	private static UUID uuid = UUID.fromString("979fde02-de91-4fe1-bd21-4a6253758218");
	
	private GObject gobject;
	
	protected RemoveObject() {
		super(RemoveObject.uuid);
	}
	public RemoveObject(GObject gobject) {
		super(RemoveObject.uuid);
		this.gobject = gobject;
	}
	public GObject getGObject() {
		return gobject;
	}
}
