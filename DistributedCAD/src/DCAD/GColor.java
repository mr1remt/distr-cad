package DCAD;

import java.awt.Color;
import java.io.Serializable;

public class GColor implements Serializable {
	private static final long serialVersionUID = -3468370885524679389L;
	
	public static final GColor RED		= new GColor(255, 0, 0);
	public static final GColor GREEN	= new GColor(0, 255, 0);
	public static final GColor BLUE		= new GColor(0, 0, 255);
	public static final GColor BLACK	= new GColor(0, 0, 0);
	public static final GColor WHITE	= new GColor(255, 255, 255);
	public static final GColor PINK		= new GColor(255, 175, 175);
	
	private int red, green, blue;

	public GColor() {
	}

	public GColor(int r, int g, int b) {
		red = r;
		green = g;
		blue = b;
	}

	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}
	
	public Color toColor() {
		return new Color(red, green, blue);
	}

}
