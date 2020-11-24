package com.fs.starfarer.api.impl.campaign;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.util.DelayedActionScript;

public class DelayedBlueprintLearnScript extends DelayedActionScript {

	public static float LEARNED_HULL_FREQUENCY = 0.1f;
	
	protected List<String> fighters = new ArrayList<String>();
	protected List<String> weapons = new ArrayList<String>();
	protected List<String> ships = new ArrayList<String>();
	protected List<String> industries = new ArrayList<String>();
	
	protected String factionId;
	
	public DelayedBlueprintLearnScript(String factionId) {
		this(factionId, 30f);
	}

	public DelayedBlueprintLearnScript(String factionId, float daysLeft) {
		super(daysLeft);
		this.factionId = factionId;
	}

	@Override
	public void doAction() {
		FactionAPI faction = Global.getSector().getFaction(factionId);
		if (faction != null) {
			if (!ships.isEmpty()) {
				faction.clearShipRoleCache();
			}
			
			for (String id : ships) {
				if (faction.knowsShip(id)) continue;
				faction.addKnownShip(id, true);
				faction.addUseWhenImportingShip(id);
				faction.getHullFrequency().put(id, LEARNED_HULL_FREQUENCY);
			}
			for (String id : fighters) {
				if (faction.knowsFighter(id)) continue;
				faction.addKnownFighter(id, true);
			}
			for (String id : weapons) {
				if (faction.knowsWeapon(id)) continue;
				faction.addKnownWeapon(id, true);
			}
			for (String id : industries) {
				if (faction.knowsIndustry(id)) continue;
				faction.addKnownIndustry(id);
			}
		}
	}

	public List<String> getFighters() {
		return fighters;
	}

	public List<String> getWeapons() {
		return weapons;
	}

	public List<String> getShips() {
		return ships;
	}

	public List<String> getIndustries() {
		return industries;
	}

}



