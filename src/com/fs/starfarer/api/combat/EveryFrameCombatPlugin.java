package com.fs.starfarer.api.combat;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;

public interface EveryFrameCombatPlugin extends CombatEnginePlugin {
	//boolean hasInputPriority();
	void processInputPreCoreControls(float amount, List<InputEventAPI> events);
	
	void advance(float amount, List<InputEventAPI> events);
	void renderInWorldCoords(ViewportAPI viewport);
	void renderInUICoords(ViewportAPI viewport);
}
