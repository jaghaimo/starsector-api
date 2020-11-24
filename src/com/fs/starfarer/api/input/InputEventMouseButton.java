package com.fs.starfarer.api.input;

/**
 * The values returned by the LWJGL input library for the mouse buttons.
 * 
 * The value of InputEvent will be set to one of these (0, 1, 2) or a larger number if
 * its a different mouse button.  These are simply provided for convenience, and
 * are not strictly necessary.
 * 
 * @author Alex Mosolov
 *
 */
public interface InputEventMouseButton {
	
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int MIDDLE = 2;
}