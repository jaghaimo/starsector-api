package com.fs.starfarer.api.impl.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.BattleObjectiveEffect;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.FogOfWarAPI;

public abstract class BaseBattleObjectiveEffect implements BattleObjectiveEffect {
	
	protected CombatEngineAPI engine;
	protected List<ShipStatusItem> itemsNAFrigates = new ArrayList<ShipStatusItem>();
	protected List<ShipStatusItem> itemsNAFighters= new ArrayList<ShipStatusItem>();

	protected BattleObjectiveAPI objective;
	
	private Map<BattleObjectiveAPI, Integer> prevOwners = new HashMap<BattleObjectiveAPI, Integer>();
	
	public void init(CombatEngineAPI engine, BattleObjectiveAPI objective) {
		this.engine = engine;
		this.objective = objective;
		
		ShipStatusItem item = new ShipStatusItem(objective.getDisplayName(), "n / a to fighters", false);
		itemsNAFighters.add(item);
		item = new ShipStatusItem(objective.getDisplayName(), "n / a to frigates", false);
		itemsNAFrigates.add(item);
	}
	
	public void giveCommandPointsForCapturing(int points) {
		int owner = objective.getOwner();
		CombatFleetManagerAPI fleetManager = engine.getFleetManager(owner);
		if (fleetManager != null) {
			Integer prevOwner = (Integer) prevOwners.get(objective);
			if (prevOwner != null && prevOwner.intValue() != owner) {
				// objective just switched hands, give bonus
				String bonusKey = objective.getDisplayName() + "_bonus_ " + "" + (float) Math.random();
				fleetManager.getTaskManager(false).getCommandPointsStat().modifyFlat(bonusKey, points);
				fleetManager.getTaskManager(true).getCommandPointsStat().modifyFlat(bonusKey, points);
			}
		}
		prevOwners.put(objective,  owner);
	}
	
	
	public void revealArea(float radius) {
		FogOfWarAPI fog = engine.getFogOfWar(0);
		if (objective.getOwner() == 0) {
			fog.revealAroundPoint(objective, objective.getLocation().x, objective.getLocation().y, radius);
		}
		
		fog = engine.getFogOfWar(1);
		if (objective.getOwner() == 1) {
			fog.revealAroundPoint(objective, objective.getLocation().x, objective.getLocation().y, radius);
		}
	}

}
