package com.fs.starfarer.api.campaign;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;

public interface CustomUIPanelPlugin {
	
	/**
	 * Called whenever the location or size of this UI panel changes.
	 * @param position
	 */
	void positionChanged(PositionAPI position);
	
	/**
	 * Below any UI elements in the panel.
	 * @param alphaMult
	 */
	void renderBelow(float alphaMult);
	/**
	 * alphaMult is the transparency the panel should be rendered at.
	 * @param alphaMult
	 */
	void render(float alphaMult);
	
	/**
	 * @param amount in seconds.
	 */
	void advance(float amount);
	
	/**
	 * List of input events that occurred this frame. (Almost) always includes one mouse move event.
	 * 
	 * Events should be consume()d if they are acted on.
	 * Mouse-move events should generally not be consumed.
	 * The loop processing events should check to see if an event has already been consumed, and if so, skip it.
	 * Accessing the data of a consumed event will throw an exception.
	 * 
	 * @param events
	 */
	void processInput(List<InputEventAPI> events);
}
