package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;

public class TriTachMajorLoanBarEvent extends TriTachLoanBarEvent {
	
	public static int REPAYMENT_DAYS_MAJOR = 500;
	
	public TriTachMajorLoanBarEvent() {
		super();
	}
	
	@Override
	protected void regen(MarketAPI market) {
		if (this.market == market) return;
		
		super.regen(market);
		
		
		loanAmount = 1000000 + random.nextInt(6) * 100000;
		repaymentAmount = (int) (loanAmount * 1.5f);
		repaymentDays = REPAYMENT_DAYS_MAJOR;
	}
	
	@Override
	protected void createIntel() {
		TriTachLoanIntel intel = new TriTachLoanIntel(this, market);
		intel.setMajorLoan(true);
		Global.getSector().getIntelManager().addIntel(intel, false, dialog.getTextPanel());
	}

	@Override
	protected String getPrompt() {
		return "A Tri-Tachyon executive sits in a private booth, scrolling through " + getHisOrHer() + " TriPad. An untouched drink sits on the immaculate table.";
	}
	
	@Override
	protected String getOptionText() {
		return "Throw some credits around like a big spender, to see if you can attract the executive's attention";
	}
	
	@Override
	protected String getMainText() {
		return "An impeccable Tri-Tachyon factioneer of completely indeterminate age has you invited to " +
				getHisOrHer() + " booth. " + Misc.ucFirst(getHeOrShe()) + " appraises you coolly as " +
				getHeOrShe() + " fills a glass from a bottle that looks to cost more than many starships.";

	}
	
	@Override
	protected String getMainText2() {
		return 
		"\"The vintage predates the Collapse, you know,\" " + getHeOrShe() + " says. " +
		"\"I enjoy the idea of owning a small part of history.\" " + Misc.ucFirst(getHeOrShe()) + 
		" smiles, \"So let's talk about you.\" As " + getHeOrShe() + " lists detailed specifications " +
		"and statistical trends regarding your career in the Persean Sector you make a mental " +
		"note to have a discussion with your senior officers on the subject of " +
		"information security. \"So,\" " + getHeOrShe() + " concludes, \"I'd like to see " +
		"what you could accomplish with a short-term capital infusion of let us say %s. " +
		"To incentivize the ruthless enterprise I so admire, let us say you would repay %s " +
		"within %s days.\n\n" + 
		
		"You take this in as you sip your drink. " +
		"It's a lot of money. You also consider that this Tri-Tach suit paid too much for the bottle.";
		
	}
	
	@Override
	protected String [] getMainText2Tokens() {
		return new String [] { Misc.getDGSCredits(loanAmount), Misc.getDGSCredits(repaymentAmount), 
							   "" + (int)repaymentDays };
	}
	@Override
	protected Color [] getMainText2Colors() {
		return new Color [] { Misc.getHighlightColor(), Misc.getHighlightColor(), Misc.getHighlightColor() };
	}
	
	@Override
	protected String getConfirmText() {
		return "Accept the deal and toast your joint venture";
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

}



