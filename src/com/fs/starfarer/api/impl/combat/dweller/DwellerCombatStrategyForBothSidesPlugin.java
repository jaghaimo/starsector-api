package com.fs.starfarer.api.impl.combat.dweller;

import java.util.List;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.input.InputEventAPI;


public class DwellerCombatStrategyForBothSidesPlugin extends BaseEveryFrameCombatPlugin {

	protected DwellerCombatStrategyAI playerSide;
	protected DwellerCombatStrategyAI enemySide;

	public DwellerCombatStrategyForBothSidesPlugin() {
		playerSide = new DwellerCombatStrategyAI(0);
		enemySide = new DwellerCombatStrategyAI(1);
	}
	
	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		if (Global.getCurrentState() != GameState.COMBAT) return;
		
		playerSide.advance(amount);
		enemySide.advance(amount);
	}

}













