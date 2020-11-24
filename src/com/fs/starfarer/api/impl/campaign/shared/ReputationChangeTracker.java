package com.fs.starfarer.api.impl.campaign.shared;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

public class ReputationChangeTracker {
	public static class ReputationChangeData {
		private long lastPositiveChange;
		private long lastNegativeChange;
		private float lastValue;
		
		public long getLastPositiveChange() {
			return lastPositiveChange;
		}
		public void setLastPositiveChange(long lastChange) {
			this.lastPositiveChange = lastChange;
		}
		public float getLastValue() {
			return lastValue;
		}
		public void setLastValue(float lastValue) {
			this.lastValue = lastValue;
		}
		public long getLastNegativeChange() {
			return lastNegativeChange;
		}
		public void setLastNegativeChange(long lastNegativeChange) {
			this.lastNegativeChange = lastNegativeChange;
		}
	}
	
	private Map<String, ReputationChangeData> repData = new HashMap<String, ReputationChangeData>();
	
	public void advance(float days) {
		
		for (FactionAPI faction : Global.getSector().getAllFactions()) {
			if (faction.isPlayerFaction()) continue;
		
			ReputationChangeData data = getDataFor(faction.getId());
			float rep = faction.getRelationship(Factions.PLAYER);
			if (data.getLastValue() != rep) {
				if (data.getLastValue() > rep) {
					data.setLastNegativeChange(Global.getSector().getClock().getTimestamp());
				} else {
					data.setLastPositiveChange(Global.getSector().getClock().getTimestamp());
				}
				data.setLastValue(rep);
			}
		}
	}
	
	public float getDaysSinceLastPositiveChange(String factionId) {
		ReputationChangeData data = getDataFor(factionId);
		return Global.getSector().getClock().getElapsedDaysSince(data.getLastPositiveChange());
	}
	public float getDaysSinceLastNegativeChange(String factionId) {
		ReputationChangeData data = getDataFor(factionId);
		return Global.getSector().getClock().getElapsedDaysSince(data.getLastPositiveChange());
	}
	
	public ReputationChangeData getDataFor(String factionId) {
		ReputationChangeData data = repData.get(factionId);
		if (data == null) {
			data = new ReputationChangeData();
			FactionAPI faction = Global.getSector().getFaction(factionId);
			float rep = faction.getRelationship(Factions.PLAYER);
			data.setLastValue(rep);
			data.setLastPositiveChange(Global.getSector().getClock().getTimestamp());
			data.setLastNegativeChange(Global.getSector().getClock().getTimestamp());
			repData.put(factionId, data);
		}
		return data;
	}
}











