package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.util.Misc;

public class CorruptPLClerkSuppliesBarEvent extends BaseGetCommodityBarEvent {
	
	public CorruptPLClerkSuppliesBarEvent() {
		super();
	}
	
	public boolean shouldShowAtMarket(MarketAPI market) {
		if (!super.shouldShowAtMarket(market)) return false;
		regen(market);
		
		if (!market.getFactionId().equals(Factions.PERSEAN)) {
			return false;
		}
		if (market.getId().equals("kazeron")) return false;
		if (market.getStabilityValue() > 5) return false;
		
		if (!market.hasSpaceport()) return false;
		
//		boolean hasStation = false;
//		for (Industry ind : market.getIndustries()) {
//			if (ind.getSpec().hasTag(Industries.TAG_STATION)) {
//				hasStation = true;
//				break;
//			}
//		}
//		if (!hasStation) return false;
		
		
		return true;
	}

	@Override
	protected String getCommodityId() {
		return Commodities.SUPPLIES;
	}
	
	@Override
	protected String getPersonFaction() {
		return Factions.PERSEAN;
	}
	
	@Override
	protected String getPersonRank() {
		return Ranks.CITIZEN;
	}
	
	@Override
	protected int computeQuantity() {
		int quantity = 30 + 10 * random.nextInt(4);
		
		CommodityOnMarketAPI com = market.getCommodityData(commodity);
		int size = com.getAvailable();
		if (size < 1) size = 1;
		quantity *= Math.max(1, BaseIndustry.getSizeMult(size) - 2);
		return quantity;
	}
	
	@Override
	protected float getPriceMult() {
		return 0.75f;
	}
	
	@Override
	protected String getPrompt() {
		return "A " + getManOrWoman() + " in the port authority uniform is discreetely trying to catch your eye.";
	}
	
	@Override
	protected String getOptionText() {
		return "Talk to the port authority official";
	}
	
	@Override
	protected String getMainText() {
		return "You relent and sit with the port clerk who smiles and offers to buy you a drink. " +
				"\"I'm familiar with your cargo manifest, of course, and thought you might be interested " +
				"in an opportunity.\" " + Misc.ucFirst(getHeOrShe()) + " looks around, then back at you," +
				" with a sly smile. \"Strictly off-the-books, of course. This job does allow me to, " +
				"shall we say, exercise personal judgment.\"\n\n" +
				
				"The port clerk speaks broadly about how cargo goes missing all the time. " +
				"Even a generous stock of %s supplies might go missing. \"It's earmarked, of course, " +
				"for the rich bastards from one of those Kazeron combines. " +
				"They can afford replacements on the open market, surely.\" " +
				Misc.ucFirst(getHeOrShe()) + " goes on to explain that %s per unit would be necessary for " +
				getHisOrHer() + " trouble, \"Think of it as a commission,\" " + getHeOrShe() + " says.";
	}
	
	@Override
	protected String [] getMainTextTokens() {
		return new String [] { Misc.getWithDGS(quantity),  Misc.getDGSCredits(unitPrice) };
	}
	@Override
	protected Color [] getMainTextColors() {
		return new Color [] { Misc.getHighlightColor(), Misc.getHighlightColor() };
	}
	
	@Override
	protected String getConfirmText() {
		return "Accept and transfer " + Misc.getDGSCredits(unitPrice * quantity) + " to the TriAnon account provided";
	}
	
	@Override
	protected String getCancelText() {
		return "Decline, explaining that you don't wish to meddle in League affairs";
	}

	@Override
	protected String getAcceptText() {
		return "You leave the bar to find a message ping on your TriPad from the local port authority. " +
				"It seems you've left %s of your supplies in cargo bay twelve, " +
				"and the bay needs to be cleared out by the end of the next work-shift.";
	}
	
	@Override
	protected String [] getAcceptTextTokens() {
		return new String [] { Misc.getWithDGS(quantity) };
	}
	@Override
	protected Color [] getAcceptTextColors() {
		return new Color [] { Misc.getHighlightColor() };
	}
	
	
	
}



