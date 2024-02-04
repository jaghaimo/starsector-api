package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.util.Misc;

public class DonationOffer extends BaseHistorianOffer {

	public static enum OptionId {
		DONATE,
		NEVER_MIND,
	}
	
	protected int credits;
	
	public DonationOffer() {
		HistorianData hd = HistorianData.getInstance();
		int tier = hd.getTier();
		if (tier == 0) {
			credits = 20000;
		} else if (tier == 1) {
			credits = 50000;
		} else if (tier >= 2) {
			credits = 250000;
		}
	}
	
	

	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog) {
		HistorianData hd = HistorianData.getInstance();
		
		dialog.getOptionPanel().addOption("... some promising leads that a " + Misc.getDGSCredits(credits) + 
				" donation would let " + hd.getHimOrHer() + " pursue",
										this);
//		dialog.getOptionPanel().addOption("... that a donation would let " + hd.getHimOrHer() + " pursue some promising leads",
//				this);
	}
	
	@Override
	public void init(InteractionDialogAPI dialog) {
		super.init(dialog);
		
		setEndConversationOnReturning(false);
		
		options.clearOptions();
		//options.addOption("Make a donation of " + Misc.getDGSCredits(credits) + "", OptionId.DONATE);
		options.addOption("Make the donation", OptionId.DONATE);
		options.addOption("Steer the conversation to other topics", OptionId.NEVER_MIND);
		
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		String c = "" + Misc.getDGSCredits(cargo.getCredits().get());
		String d = "" + Misc.getDGSCredits(credits);
		if (cargo.getCredits().get() < credits) {
			text.addPara("The asked-for donation amount is %s. You only have %s available.", Misc.getHighlightColor(), d, c);
			options.setEnabled(OptionId.DONATE, false);
			options.setTooltip(OptionId.DONATE, "You don't have enough credits.");
		} else {
			text.addPara("The asked-for donation amount is %s. You have %s available.", Misc.getHighlightColor(), d, c);
		}
	}

	@Override
	public void optionSelected(String optionText, Object optionData) {
		
		HistorianData hd = HistorianData.getInstance();
		
		if (optionData == OptionId.DONATE) {
			setDone(true);
			setRemove(true);
			
			hd.incrTier();
			hd.setRecentlyDonated();
			
			Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(credits);
			AddRemoveCommodity.addCreditsLossText(credits, text);
			
			CustomRepImpact impact = new CustomRepImpact();
			impact.limit = RepLevel.COOPERATIVE;
			impact.delta = 0.1f;
			Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.CUSTOM, impact,
										  null, dialog.getTextPanel(), true), hd.getPerson());
			
//			CustomRepImpact impact = new CustomRepImpact();
//			impact.delta = 0.1f;
//			
//			Global.getSector().adjustPlayerReputation(
//					new RepActionEnvelope(RepActions.CUSTOM, 
//							impact, null, text, true, true),
//							hd.getPerson());
			
			dialog.getTextPanel().addPara("The historian thanks you for your help.");
			return;
		}
		
		
		if (optionData == OptionId.NEVER_MIND) {
			setDone(true);
			
			dialog.getTextPanel().addPara("The historian cooperates in changing topics.");
			return;
		}
	}

	
	@Override
	public int getSortOrder() {
		return 1000;
	}
	
	
	
	
}










