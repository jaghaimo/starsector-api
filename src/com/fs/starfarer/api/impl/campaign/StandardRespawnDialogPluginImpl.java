package com.fs.starfarer.api.impl.campaign;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.shared.PersonBountyEventData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;

public class StandardRespawnDialogPluginImpl implements InteractionDialogPlugin {

	public static enum OptionId {
		INIT,
		RESPAWN,
		;
	}
	
	protected InteractionDialogAPI dialog;
	protected TextPanelAPI textPanel;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;
	
	protected CampaignFleetAPI playerFleet;
	
	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		
		visual.showImagePortion("illustrations", "space_wreckage", 640, 400, 0, 0, 480, 300);
		
		optionSelected(null, OptionId.INIT);
	}
	
	public Map<String, MemoryAPI> getMemoryMap() {
		return null;
	}
	
	public void backFromEngagement(EngagementResultAPI result) {
		// no combat here, so this won't get called
	}
	
	public void optionSelected(String text, Object optionData) {
		if (optionData == null) return;
		
		OptionId option = (OptionId) optionData;
		
		if (text != null) {
			textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
		}
		
		switch (option) {
		case INIT:
			textPanel.addParagraph("Your fleet has been defeated!");
			
			textPanel.addParagraph("Normally, if this happens you and your remaining crew will " +
					"spend some time \"behind the scenes\" assembling a smaller fleet of recovered " +
					"ships.");
	
			textPanel.addParagraph("During the course of this tutorial, the only available option is to reload the last save.");
			
			options.clearOptions();
			options.addOption("Continue", OptionId.RESPAWN, null);
			break;
		case RESPAWN:
			
			createPlayerFleet();
			
			dialog.dismiss();
			Global.getSector().getCampaignUI().quickLoad();
			break;
		}
	}
	

	protected void createPlayerFleet() {

		float fp = 10;
		float crew = 50;
		
		float daysSince = Global.getSector().getClock().getElapsedDaysSince(SharedData.getData().getPlayerLosingBattleTimestamp());
		if (daysSince < 2) {
			fp = SharedData.getData().getPlayerPreLosingBattleFP();
			crew = SharedData.getData().getPlayerPreLosingBattleCrew();
		}
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		
		
		for (CampaignFleetAPI other : Global.getSector().getCurrentLocation().getFleets()) {
			MemoryAPI mem = other.getMemoryWithoutUpdate();
			if (mem.getBoolean(MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF)) {
				mem.removeAllRequired(MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF);
				//System.out.println("Hostile: " + mem.getBoolean(MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF));
			}
			mem.unset(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF);
			
			if (!Misc.isPermaKnowsWhoPlayerIs(other)) {
				mem.unset(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
			}
		}
		
		CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.PLAYER, null, null);
		fleet.setName("Fleet");

		Global.getSector().setCurrentLocation(Global.getSector().getHyperspace());
		fleet.setLocation(1000000000f, 1000000000f);
		
		for (OfficerDataAPI officer : playerFleet.getFleetData().getOfficersCopy()) {
			fleet.getFleetData().addOfficer(officer);
		}

		Global.getSector().setPlayerFleet(fleet);
		Global.getSector().setLastPlayerBattleTimestamp(Long.MIN_VALUE);
		Global.getSector().setLastPlayerBattleWon(false);
		
		
		fleet.getFleetData().setSyncNeeded();
		fleet.getFleetData().syncIfNeeded();
		
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			float max = member.getRepairTracker().getMaxCR();
			member.getRepairTracker().setCR(max);
		}

		
		for (String id : Global.getSector().getCharacterData().getAbilities()) {
			fleet.addAbility(id);
		}
		
		if (fleet.getAbility(Abilities.TRANSPONDER) != null) {
			fleet.getAbility(Abilities.TRANSPONDER).activate();
		}
		
		Global.getSector().setPaused(true);
		
//		float minCredits = 2000;
//		float newCredits = Math.max(credits * 0.8f, minCredits);
//		float change = credits - newCredits;
//
//		fleet.getCargo().getCredits().add(newCredits);
//		if (change > 0) {
//			addMessage("Lost " + (int)(change) + " credits");
//		} else if (change < 0) {
//			addMessage("Gained " + (int)(-change) + " credits");
//		}
		
		Global.getSector().getCampaignUI().resetViewOffset();
		
		Misc.clearAreaAroundPlayer(2000f);
		
		PersonBountyEventData data = SharedData.getData().getPersonBountyEventData();
		data.setLevel(Math.max(0, data.getLevel() - 1));
		data.setSuccessesThisLevel(0);
	}
	
	
	
	
	
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public void advance(float amount) {
		
	}
	
	public Object getContext() {
		return null;
	}
}



