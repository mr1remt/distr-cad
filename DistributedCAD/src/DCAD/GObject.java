package DCAD;

import java.awt.Graphics;
import java.io.Serializable;
import java.util.Random;

public class GObject implements Serializable {

	private static final long serialVersionUID = -4000363049117064546L;

	private long id;
	
	private Shape s;
	private GColor c;
	private int x, y, width, height;
	// Note that the x and y coordinates are relative to the top left corner of the
	// graphics context in which the object is to be drawn - NOT the top left corner
	// of the GUI window.
	
	/**
	 * Whether this object should still be displayed or has been removed
	 */
	private boolean active;
	
	public GObject() {
	}

	public GObject(Shape s, GColor c, int x, int y, int width, int height) {
		this.s = s;
		this.c = c;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.id = new Random().nextLong();
		this.setActive(true);
	}

    public void setShape(Shape s) {this.s = s;}
    public void setColor(GColor c) {this.c = c;}
    public void setCoordinates(int x, int y) {this.x = x; this.y = y;}
    public void setDimensions(int width, int height) {this.width = width; this.height = height;}
    public Shape getShape() {return s;}
    public GColor getColor() {return c;}
    public int getX() {return x;}
    public int getY() {return y;}

	public int getWidth() {return width;}
	public int getHeight() {return height;}
	
    public void setID(long id) {this.id = id;}
    public long getID() { return id;}
    
    public boolean isActive() {return active;}
	public void setActive(boolean active) {this.active = active;}
    
	public void draw(Graphics g) {
		g.setColor(c.toColor());
		int drawX = x, drawY = y, drawWidth = width, drawHeight = height;

		// Convert coordinates and dimensions if objects are not drawn from top left
		// corner to
		// bottom right.
		if (width < 0) {
			drawX = x + width;
			drawWidth = -width;
		}

		if (height < 0) {
			drawY = y + height;
			drawHeight = -height;
		}

		// Use string comparison to allow comparison of shapes even if the objects
		// have different nodes of origin

		if (s.toString().compareTo(Shape.OVAL.toString()) == 0) {
			g.drawOval(drawX, drawY, drawWidth, drawHeight);
		}else if (s.toString().compareTo(Shape.RECTANGLE.toString()) == 0) {
			g.drawRect(drawX, drawY, drawWidth, drawHeight);
		}else if (s.toString().compareTo(Shape.LINE.toString()) == 0) {
			g.drawLine(x, y, x + width, y + height);
		}else if (s.toString().compareTo(Shape.FILLED_RECTANGLE.toString()) == 0) {
			g.fillRect(drawX, drawY, drawWidth, drawHeight);
		}else if (s.toString().compareTo(Shape.FILLED_OVAL.toString()) == 0) {
			g.fillOval(drawX, drawY, drawWidth, drawHeight);
		}
	}
	@Override
	public String toString() {
		return "GObject w:" + width + " h:" + height;
	}
}
