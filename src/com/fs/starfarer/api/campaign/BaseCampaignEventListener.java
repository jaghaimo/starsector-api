package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

public class BaseCampaignEventListener implements CampaignEventListener {

	public BaseCampaignEventListener(boolean permaRegister) {
		if (permaRegister) {
			Global.getSector().addListener(this);
		}
	}

	
	public void reRegister(float daysToKeep) {
		Global.getSector().addListenerWithTimeout(this, daysToKeep);
	}

	public void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle) {
		// TODO Auto-generated method stub
		
	}
	
	public void reportBattleFinished(CampaignFleetAPI primaryWinner, BattleAPI battle) {
		// TODO Auto-generated method stub
		
	}

	public void reportFleetDespawned(CampaignFleetAPI fleet,
			FleetDespawnReason reason, Object param) {
		// TODO Auto-generated method stub
		
	}

	public void reportFleetJumped(CampaignFleetAPI fleet,
			SectorEntityToken from, JumpDestination to) {
		// TODO Auto-generated method stub
		
	}

	public void reportFleetReachedEntity(CampaignFleetAPI fleet,
			SectorEntityToken entity) {
		// TODO Auto-generated method stub
		
	}

	public void reportPlayerMarketTransaction(
			PlayerMarketTransaction transaction) {
		// TODO Auto-generated method stub
		
	}

	public void reportShownInteractionDialog(InteractionDialogAPI dialog) {
		// TODO Auto-generated method stub
		
	}

	public void reportPlayerOpenedMarket(MarketAPI market) {
		
	}


	public void reportPlayerReputationChange(String faction, float delta) {
		
	}

	public void reportPlayerEngagement(EngagementResultAPI result) {
		
	}

	public void reportFleetSpawned(CampaignFleetAPI fleet) {
		
	}

	public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
		
	}


	public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
		
	}

	public void reportPlayerClosedMarket(MarketAPI market) {
		
	}

	public void reportPlayerReputationChange(PersonAPI person, float delta) {
		
	}

	public void reportPlayerActivatedAbility(AbilityPlugin ability, Object param) {
		
	}

	public void reportPlayerDeactivatedAbility(AbilityPlugin ability, Object param) {
		
	}

	public void reportPlayerDumpedCargo(CargoAPI cargo) {
		
	}

	public void reportPlayerDidNotTakeCargo(CargoAPI cargo) {
		
	}


	public void reportEconomyMonthEnd() {
		// TODO Auto-generated method stub
		
	}


	public void reportEconomyTick(int iterIndex) {
		// TODO Auto-generated method stub
		
	}

}
