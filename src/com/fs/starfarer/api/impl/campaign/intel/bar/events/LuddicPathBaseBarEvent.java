package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathCellsIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.util.Misc;

public class LuddicPathBaseBarEvent extends BaseBarEventWithPerson {
	
	public static enum OptionId {
		INIT,
		AGREE,
		REJECT,
		LEAVE,
		
	}
	
	public static int COST = 10000;
	
	protected LuddicPathBaseIntel intel;
	
	public LuddicPathBaseBarEvent(LuddicPathBaseIntel intel) {
		this.intel = intel;
	}

	public boolean shouldShowAtMarket(MarketAPI market) {
		for (LuddicPathCellsIntel cell : LuddicPathCellsIntel.getCellsForBase(intel, true)) {
			if (cell.getMarket() == market) return true;
		}
		return false;
	}
	
	@Override
	public boolean shouldRemoveEvent() {
		return intel.isEnding() || intel.isEnded() || intel.isPlayerVisible();
	}

	@Override
	protected void regen(MarketAPI market) {
		if (this.market == market) return;
		super.regen(market);

	}
	
	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog) {
		super.addPromptAndOption(dialog);
		
		regen(dialog.getInteractionTarget().getMarket());
		
		TextPanelAPI text = dialog.getTextPanel();
		text.addPara("A " + getManOrWoman() + " with Pather tattoos is staring at you from across the bar " +
				"with a desperate gleam in " + getHisOrHer() + " eyes.");

		dialog.getOptionPanel().addOption("Make eye contact with the Pather and walk out into the back alley", this, null);
	}
	
	@Override
	public void init(InteractionDialogAPI dialog) {
		super.init(dialog);
		
		done = false;
		dialog.getVisualPanel().showPersonInfo(person, true);
		
		optionSelected(null, OptionId.INIT);
	}
	
	@Override
	public void optionSelected(String optionText, Object optionData) {
		if (!(optionData instanceof OptionId)) {
			return;
		}
		OptionId option = (OptionId) optionData;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		options.clearOptions();
		
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		int credits = (int) cargo.getCredits().get();
		
		Color h = Misc.getHighlightColor();
		Color n = Misc.getNegativeHighlightColor();
		//COST = 10000;
		
		switch (option) {
		case INIT:
			text.addPara("After a minute or two of waiting in the alley, the door opens and the Pather " +
					"walks out.");
			text.addPara("\"I know who you are\", " + getHeOrShe() + " says. \"I need your help. If they find " +
					"out what I've done... well, let's just say my future with the Path is likely to be a short one. " +
					"So, how about this: you arrange a new identity for me, and I tell you where " +
					"the base supplying the Pather cells at this colony is located.\"");
			
			boolean canAccept = COST <= credits;
			LabelAPI label = text.addPara("You estimate that doing as " + getHeOrShe() + 
								" asks will run you about %s. You have %s available.",
					h,
					Misc.getDGSCredits(COST),	
					Misc.getDGSCredits(credits));
			label.setHighlightColors(canAccept ? h : n, h);
			label.setHighlight(Misc.getDGSCredits(COST), Misc.getDGSCredits(credits));
			
			options.addOption("Agree to " + getHisOrHer() + " terms", OptionId.AGREE);
			if (!canAccept) {
				options.setEnabled(OptionId.AGREE, false);
				options.setTooltip(OptionId.AGREE, "Not enough credits.");
			}
			options.addOption("Suggest that " + getHeOrShe() + " handle " + getHisOrHer() + " own problems", OptionId.REJECT);
			break;
		case AGREE:
			text.addPara("You agree to the " + getManOrWoman() + "'s terms and make the necessary arrangements.");
			
			cargo.getCredits().subtract(COST);
			AddRemoveCommodity.addCreditsLossText(COST, dialog.getTextPanel());
			
			done = true;
			intel.makeKnown();
			intel.sendUpdate(PirateBaseIntel.DISCOVERED_PARAM, text);
			
			PortsideBarData.getInstance().removeEvent(this);
			
			options.addOption("Continue", OptionId.LEAVE);
			break;
		case REJECT:
			text.addPara("You leave the back alley and return to the bar.");
			
			options.addOption("Continue", OptionId.LEAVE);
			break;
		case LEAVE:
			noContinue = true;
			done = true;
			break;
		}
	}
	

	@Override
	protected String getPersonFaction() {
		return Factions.LUDDIC_PATH;
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
	protected String getPersonPortrait() {
		return null;
	}
	
	@Override
	protected Gender getPersonGender() {
		return Gender.ANY;
	}

	
}



