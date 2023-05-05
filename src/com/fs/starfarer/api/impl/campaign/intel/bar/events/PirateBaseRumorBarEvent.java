package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.PirateBasePirateActivityCause2;
import com.fs.starfarer.api.util.Misc;

public class PirateBaseRumorBarEvent extends BaseBarEvent {
	protected PirateBaseIntel intel;
	protected long seed;
	
	public boolean isAlwaysShow() {
		return true;
	}
	
	public PirateBaseRumorBarEvent(PirateBaseIntel intel) {
		this.intel = intel;
		seed = Misc.random.nextLong();
	}

	public boolean shouldShowAtMarket(MarketAPI market) {
		if (intel.getTarget() == market.getContainingLocation()) {
			return true;
		}
		if (market.isPlayerOwned()) {
			HostileActivityEventIntel ha = HostileActivityEventIntel.get();
			if (ha != null) {
				return PirateBasePirateActivityCause2.getBaseIntel(market.getStarSystem()) == intel;
			}
//			HostileActivityIntel hai = HostileActivityIntel.get(market.getStarSystem());
//			if (hai != null) {
//				PirateBasePirateActivityCause cause = (PirateBasePirateActivityCause)hai.getActivityCause(
//								PirateHostileActivityPluginImpl.class, PirateBasePirateActivityCause.class);
//				if (cause != null) {
//					return cause.getBaseIntel() == intel;
//				}
//			}
		}
		return false;
	}
	
	@Override
	public boolean shouldRemoveEvent() {
		return intel.isEnding() || intel.isEnded() || intel.isPlayerVisible();
	}


	transient protected boolean done = false;
	transient protected Gender gender;
	transient protected PersonAPI person;
	
	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.addPromptAndOption(dialog, memoryMap);
		
		Random random = new Random(seed + dialog.getInteractionTarget().getMarket().getId().hashCode());
		
		gender = Gender.MALE;
		if (random.nextFloat() > 0.5f) {
			gender = Gender.FEMALE;
		}
		person = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson(gender, random);
		person.setPostId(Ranks.POST_MINORCRIMINAL);
		person.setImportanceAndVoice(PersonImportance.VERY_LOW, random);
		person.addTag(Tags.CONTACT_UNDERWORLD);
		
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
	public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.init(dialog, memoryMap);
		
		String himOrHerSelf = "himself";
		if (gender == Gender.FEMALE) himOrHerSelf = "herself";
		
		TextPanelAPI text = dialog.getTextPanel();
		text.addPara("You keep the drinks flowing and mostly just listen, " +
					 "letting the spacer unburden " + himOrHerSelf + "."); 
		
		//PersonAPI person = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson(gender);
		dialog.getVisualPanel().showPersonInfo(person, true);
		
		done = true;
		intel.makeKnown();
		intel.sendUpdate(PirateBaseIntel.DISCOVERED_PARAM, text);
		
		PortsideBarData.getInstance().removeEvent(this);
		
		ContactIntel.addPotentialContact(person, dialog.getInteractionTarget().getMarket(), text);
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



