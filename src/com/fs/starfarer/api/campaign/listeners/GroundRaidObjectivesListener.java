package com.fs.starfarer.api.campaign.listeners;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidType;

public interface GroundRaidObjectivesListener {

	public static class RaidResultData {
		public MarketAPI market;
		public SectorEntityToken entity;
		public List<GroundRaidObjectivePlugin> objectives;
		public RaidType type;
		public float raidEffectiveness;
		public long xpGained;
		public int marinesTokens;
		public int marinesTokensInReserve;
		public int marinesLost;
	}
	
	
	/**
	 * Called 10 times with priority from 0 to 9. An implementation should generally only modify objectives in
	 * one of those callse.
	 * @param market
	 * @param objectives
	 * @param priority
	 */
	void modifyRaidObjectives(MarketAPI market, SectorEntityToken entity, List<GroundRaidObjectivePlugin> objectives, RaidType type, int marineTokens, int priority);
	
	
	void reportRaidObjectivesAchieved(RaidResultData data, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap);
	
}
