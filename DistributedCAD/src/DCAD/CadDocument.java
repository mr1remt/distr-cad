package DCAD;

public abstract class CadDocument implements Iterable<GObject>, Runnable{		
	
	public abstract void addGObject(GObject object);
	
	public abstract void removeLastGObject();
	
	public abstract int size();
	
}
