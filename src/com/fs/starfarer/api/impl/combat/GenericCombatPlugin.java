package com.fs.starfarer.api.impl.combat;

import java.util.List;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;

public class GenericCombatPlugin extends BaseEveryFrameCombatPlugin {
	
	private CombatEngineAPI engine;
	public void init(CombatEngineAPI engine) {
		this.engine = engine;
		
//		engine.getListenerManager().addListener(new RangedSpecialization.TestDamageModifier());
//		engine.getListenerManager().addListener(new RangedSpecialization.TestDamageModifierTaken());
		
		engine.removePlugin(this);
	}
	
	public void advance(float amount, List<InputEventAPI> events) {
		
	}
	

	public void renderInUICoords(ViewportAPI viewport) {
	}

	public void renderInWorldCoords(ViewportAPI viewport) {
	}


}
