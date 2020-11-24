package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;

public class PirateBaseRumorBarEvent extends BaseBarEvent {
	protected PirateBaseIntel intel;
	
	public PirateBaseRumorBarEvent(PirateBaseIntel intel) {
		this.intel = intel;
	}

	public boolean shouldShowAtMarket(MarketAPI market) {
		return intel.getTarget() == market.getContainingLocation();
	}
	
	@Override
	public boolean shouldRemoveEvent() {
		return intel.isEnding() || intel.isEnded() || intel.isPlayerVisible();
	}



	transient protected boolean done = false;
	transient protected Gender gender;
	
	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog) {
		super.addPromptAndOption(dialog);
		
		gender = Gender.MALE;
		if ((float) Math.random() > 0.5f) {
			gender = Gender.FEMALE;
		}
		
		String himOrHer = "him";
		if (gender == Gender.FEMALE) himOrHer = "her";
		
		TextPanelAPI text = dialog.getTextPanel();
		text.addPara("A grizzled spacer sits at the bar, downing shots " +
									  "of what looks like the cheapest liquor available.");
		
		dialog.getOptionPanel().addOption(
				"Approach the spacer and offer to buy " + himOrHer + " something more palatable",
				this);
	}

	@Override
	public void init(InteractionDialogAPI dialog) {
		super.init(dialog);
		
		String himOrHerSelf = "himself";
		if (gender == Gender.FEMALE) himOrHerSelf = "herself";
		
		TextPanelAPI text = dialog.getTextPanel();
		text.addPara("You keep the drinks going and mostly just listen, " +
					 "letting the spacer unburden " + himOrHerSelf + "."); 
		
		PersonAPI person = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson(gender);
		dialog.getVisualPanel().showPersonInfo(person, true);
		
		done = true;
		intel.makeKnown();
		intel.sendUpdate(PirateBaseIntel.DISCOVERED_PARAM, text);
		
		PortsideBarData.getInstance().removeEvent(this);
	}

	
	@Override
	public void optionSelected(String optionText, Object optionData) {
	}

	@Override
	public boolean isDialogFinished() {
		return done;
	}
	
	
	protected boolean showCargoCap() {
		return false;
	}
}



