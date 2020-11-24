package com.fs.starfarer.api.impl.combat;

import java.util.List;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;

public class EscapeRevealPlugin extends BaseEveryFrameCombatPlugin {
	
	private CombatEngineAPI engine;
	private BattleCreationContext context;

	public EscapeRevealPlugin(BattleCreationContext context) {
		this.context = context;
	}
	public void init(CombatEngineAPI engine) {
		this.engine = engine;
	}
	
	private float elapsed = 0f;
	public void advance(float amount, List<InputEventAPI> events) {
		if (elapsed > 10f) return;
		if (!engine.isPaused()) elapsed += amount;
	
		float width = engine.getMapWidth();
		float height = engine.getMapHeight();
		
		float minX = -width/2;
		float minY = -height/2;
		engine.getFogOfWar(0).revealAroundPoint(this, minX + width/2f, minY,
												context.getInitialEscapeRange() + 1000f);
		
		engine.getFogOfWar(1).revealAroundPoint(this, minX + width/2f, minY,
												context.getInitialEscapeRange() + 1000f);		
	}
}
