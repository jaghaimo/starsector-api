package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.util.Misc;

public class TriTachLoanBarEvent extends BaseGetCommodityBarEvent {
	
	public static int REPAYMENT_DAYS = 400;
	
	public TriTachLoanBarEvent() {
		super();
	}
	
	public boolean shouldShowAtMarket(MarketAPI market) {
		if (!super.shouldShowAtMarket(market)) return false;
		
		if (!market.getFactionId().equals(Factions.TRITACHYON)) {
			return false;
		}
		
		if (market.getStabilityValue() < 4) return false;
		
		if (Global.getSector().getFaction(Factions.TRITACHYON).getRelToPlayer().isAtBest(RepLevel.HOSTILE)) {
			return false;
		}
		
		return true;
	}
	
	protected int loanAmount;
	protected int repaymentAmount;
	protected int repaymentDays;
	
	@Override
	protected void regen(MarketAPI market) {
		if (this.market == market) return;
		
		super.regen(market);
		
		loanAmount = 200000 + random.nextInt(6) * 10000;
		repaymentAmount = (int) (loanAmount * 1.5f);
		//repaymentDays = 365;
		repaymentDays = REPAYMENT_DAYS;
	}

	@Override
	protected void doStandardConfirmActions() {
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		cargo.getCredits().add(loanAmount);
		
		TextPanelAPI text = dialog.getTextPanel();
		AddRemoveCommodity.addCreditsGainText(loanAmount, text);
		
		createIntel();
	}
	
	protected void createIntel() {
		TriTachLoanIntel intel = new TriTachLoanIntel(this, market);
		Global.getSector().getIntelManager().addIntel(intel, false, dialog.getTextPanel());
	}

	@Override
	protected String getPersonFaction() {
		return Factions.TRITACHYON;
	}
	
	@Override
	protected String getPersonRank() {
		return Ranks.CITIZEN;
	}
	
	@Override
	protected String getPersonPost() {
		return Ranks.CITIZEN;
	}
	
	@Override
	protected float getPriceMult() {
		return 0;
	}
	
	@Override
	protected String getPrompt() {
		return "A Tri-Tachyon careerist sits at a spotless table, sipping something expensive-looking.";
	}
	
	@Override
	protected String getOptionText() {
		return "Introduce yourself to the Tri-Tachyon " + getManOrWoman() + " and try to land some investment credits";
	}
	
	@Override
	protected String getMainText() {
		return "A young Tri-Tachyon factioneer in a perfect suit sips from some insubstantial drink " +
				"as sophisticated as it is expensive. After introductions " + getHeOrShe() + 
				" quickly cuts to the point, and says \"I'm simply in the business of statistics, and " +
				"I like your growth potential. I think that you can take my %s now and pay " +
				"%s back to me in, oh, %s days. If not,\" " + getHeOrShe() + 
				" mimics a stricken look, \"I'll ruin your reputation across Tri-Tachyon space. What do you say?\"";
	}
	
	@Override
	protected String [] getMainTextTokens() {
		return new String [] { Misc.getDGSCredits(loanAmount), Misc.getDGSCredits(repaymentAmount), 
							   "" + (int)repaymentDays };
	}
	@Override
	protected Color [] getMainTextColors() {
		return new Color [] { Misc.getHighlightColor(), Misc.getHighlightColor(), Misc.getHighlightColor() };
	}
	
	@Override
	protected String getConfirmText() {
		return "Accept the deal and order another round to celebrate";
	}
	
	@Override
	protected String getCancelText() {
		return "Decline the deal, explaining that you're \"just here to network\"";
	}

	@Override
	protected String getAcceptText() {
		return "You leave the lounge rich in credits, having exchanged secure comm keys with the " +
				"Tri-Tachyon shark and receiving the transfer immediately. Your head spins with plans " +
				"for how to leverage your new assets - and a bit from the drink, you admit to yourself.";
	}

	
	
	public int getLoanAmount() {
		return loanAmount;
	}

	public void setLoanAmount(int loanAmount) {
		this.loanAmount = loanAmount;
	}

	public int getRepaymentAmount() {
		return repaymentAmount;
	}

	public void setRepaymentAmount(int repaymentAmount) {
		this.repaymentAmount = repaymentAmount;
	}

	public int getRepaymentDays() {
		return repaymentDays;
	}

	public void setRepaymentDays(int repaymentDays) {
		this.repaymentDays = repaymentDays;
	}
	
	protected boolean showCargoCap() {
		return false;
	}
	
}



