package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.PlayerFleetPersonnelTracker;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.util.Misc;

public class MercsOnTheRunBarEvent extends BaseGetCommodityBarEvent {
	
	public MercsOnTheRunBarEvent() {
		super();
	}
	
	public boolean shouldShowAtMarket(MarketAPI market) {
		//if (true) return true;
		
		if (!super.shouldShowAtMarket(market)) return false;
		regen(market);
		
		if (market.getFactionId().equals(Factions.PIRATES)) {
			return false;
		}
		
		//if (market.getStabilityValue() >= 7) return false;
		
		return true;
	}
	

	@Override
	protected void doExtraConfirmActions() {
		PlayerFleetPersonnelTracker.getInstance().update();
		PlayerFleetPersonnelTracker.getInstance().getMarineData().addXP((float)quantity);
	}

	@Override
	protected String getCommodityId() {
		return Commodities.MARINES;
	}
	
	@Override
	protected String getPersonFaction() {
		return Factions.INDEPENDENT;
	}
	
	@Override
	protected String getPersonRank() {
		return Ranks.GROUND_SERGEANT;
	}
	
	@Override
	protected int computeQuantity() {
		//int quantity = 10;
		int quantity = 30 + random.nextInt(21);
		return quantity;
	}
	
	@Override
	protected float getPriceMult() {
		return 0.75f;
	}
	
	@Override
	protected String getPrompt() {
		return "A table of tattooed roughs who bear - to your discerning eye, mil-grade cybermods - are drinking alarming amounts of liquor.";
	}
	
	@Override
	protected String getOptionText() {
		return "Join the table of tattooed roughs and see if their contract is up";
	}
	
	@Override
	protected String getMainText() {
		return "You prove yourself worth speaking to by partaking in a round of \"Atmo Drops\", " +
				"a drink based largely on consuming eye-watering liquor as quickly as possible, " +
				"with the mercenary band. They're drunk for sure, but are composed enough to speak " +
				"around the precise details of why they're being investigated by local authorities.\n\n" + 
				"After a couple of the mercs head up to order a round of \"Orbital Bombardments\"," +
				" their leader pulls you aside and sketches out a proposal. \"We've got %s suits, " +
				"we fight in vac or black. I'll give you our contract for %s, " +
				"and that's giving it away. Put us to work, or sell it off elsewhere, doesn't matter to " +
				"me if you can get us outta this fix. What'dya say?\"";
	}
	
	@Override
	protected String [] getMainTextTokens() {
		return new String [] { Misc.getWithDGS(quantity), Misc.getDGSCredits(unitPrice * quantity) };
	}
	@Override
	protected Color [] getMainTextColors() {
		return new Color [] { Misc.getHighlightColor(), Misc.getHighlightColor() };
	}
	
	@Override
	protected String getConfirmText() {
		return "Buy the mercenary marine contract and sneak them off " + market.getName();
	}
	
	@Override
	protected String getCancelText() {
		return "Decline the proposal, but stay for an \"Orbital Bombardment\" to soften the blow";
	}

	@Override
	protected String getAcceptText() {
		return "Some clever device provided by the mercs convinces the automated scanners that two " +
				"particular cargo containers are half-filled with mildly radioactive silicate gravel " +
				"and nothing else. Your logistics officer sneaks them aboard with no problems.";
	}
	
	@Override
	protected String [] getAcceptTextTokens() {
		return new String [] { };
	}
	@Override
	protected Color [] getAcceptTextColors() {
		return new Color [] { };
	}
	
	@Override
	protected String getDeclineText() {
		return "You wake up the next day in a small room above the bar with no particular memory of how " +
				"you got there. How many more rounds was it with the mercs, one? Three?";
	}
	
//	@Override
//	protected String [] getAcceptTextTokens() {
//		return new String [] { };
//	}
//	@Override
//	protected Color [] getAcceptTextColors() {
//		return new Color [] { };
//	}
	
	
}



