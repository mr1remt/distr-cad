package se.his.drts.message;

import java.util.UUID;

import DCAD.GObject;

public class DrawObject extends UniqueMessage {
	private static UUID uuid = UUID.fromString("979fde02-de91-4fe1-bd21-4a6253758218");
	
	private GObject gobject;
	
	protected DrawObject() {
		super(DrawObject.uuid);
	}
	public DrawObject(GObject gobject) {
		super(DrawObject.uuid);
		this.gobject = gobject;
	}
	public GObject getGObject() {
		return gobject;
	}
}
