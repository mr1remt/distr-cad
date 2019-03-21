package DCAD;

import java.awt.Graphics;

public abstract class CadDocument implements Iterable<GObject>, Runnable{		
	
	public abstract void addGObject(GObject object);
	
	public abstract void removeLastGObject();
	
	public abstract int size();
		
	public abstract void setGui(GUI gui);
	
	public abstract void draw(Graphics g);
}
