package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

/**
 * 
 * "Event" in the sense of something noteworthy happening, not just 
 * actual "events" from events.json.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2014 Fractal Softworks, LLC
 */
public interface CampaignEventListener {

//	public static enum ListenerCategory {
//		PLAYER,
//		FLEET,
//	}
	
	public static enum FleetDespawnReason {
		/**
		 * param is a SectorEntityToken
		 */
		REACHED_DESTINATION,
		
		/**
		 * param is a BattleAPI, with a snapshot taken before the battle.
		 */
		DESTROYED_BY_BATTLE, 
		NO_MEMBERS, // ??
		OTHER,
		NO_REASON_PROVIDED,
		PLAYER_FAR_AWAY,
	}
	
	void reportPlayerOpenedMarket(MarketAPI market);
	void reportPlayerClosedMarket(MarketAPI market);
	
	void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market);
	
	void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot);
	
	void reportPlayerMarketTransaction(PlayerMarketTransaction transaction);
	
	
	/**
	 * Once for each autoresolve round for AI vs AI. Only once per player battle encounter.
	 * @param primaryWinner
	 * @param battle
	 */
	void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle);
	void reportBattleFinished(CampaignFleetAPI primaryWinner, BattleAPI battle);
	
	void reportPlayerEngagement(EngagementResultAPI result);
	
	/**
	 * Could be destroyed or simply reached a despawn location. Or had too many accidents
	 * and lost all of its ships. Or told by other code to despawn itself.
	 * @param fleet
	 * @param reason
	 * @param param
	 */
	void reportFleetDespawned(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param);
	
	
	/**
	 * @param fleet
	 */
	void reportFleetSpawned(CampaignFleetAPI fleet);
	
	/**
	 * Planets/stations/etc that are the target of a fleet's assignment.
	 * @param fleet
	 * @param entity
	 */
	void reportFleetReachedEntity(CampaignFleetAPI fleet, SectorEntityToken entity);
	
	/**
	 * from is generally a JumpPointAPI, but doesn't *have* to be.
	 * @param fleet
	 * @param from
	 * @param to
	 */
	void reportFleetJumped(CampaignFleetAPI fleet, SectorEntityToken from, JumpDestination to);
	
	
	void reportShownInteractionDialog(InteractionDialogAPI dialog);
	
	
	void reportPlayerReputationChange(String faction, float delta);
	void reportPlayerReputationChange(PersonAPI person, float delta);
	
	void reportPlayerActivatedAbility(AbilityPlugin ability, Object param);
	void reportPlayerDeactivatedAbility(AbilityPlugin ability, Object param);

	
	void reportPlayerDumpedCargo(CargoAPI cargo);
	void reportPlayerDidNotTakeCargo(CargoAPI cargo);
	
	void reportEconomyTick(int iterIndex);
	void reportEconomyMonthEnd();
}




