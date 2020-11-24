package com.fs.starfarer.api.plugins;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public interface ShipSystemStatsScript {
	public static enum State {
		IN,
		ACTIVE,
		OUT,
		COOLDOWN,
		IDLE;
	}
	
	public static class StatusData {
		public String text;
		public boolean isDebuff;
		public StatusData(String text, boolean isDebuff) {
			this.text = text;
			this.isDebuff = isDebuff;
		}
	}
	
	void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel);
	void unapply(MutableShipStatsAPI stats, String id);
	
	StatusData getStatusData(int index, State state, float effectLevel);
	
	float getActiveOverride(ShipAPI ship);
	float getInOverride(ShipAPI ship);
	float getOutOverride(ShipAPI ship);
	
	int getUsesOverride(ShipAPI ship);
	float getRegenOverride(ShipAPI ship);
}
