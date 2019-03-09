package DCAD;

import java.util.LinkedList;

public abstract class CadDocument {
	
	private LinkedList<GObject> objectList = new LinkedList<GObject>();
	
	
	public abstract void addGObject(GObject object);
	
	public abstract void removeGObject(GObject object);
	
	
	public LinkedList<GObject> GObjectList(){
		//TODO synchronize?
		return objectList;
	}
	
	
}
