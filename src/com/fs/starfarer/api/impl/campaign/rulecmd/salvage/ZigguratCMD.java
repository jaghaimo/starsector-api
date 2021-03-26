package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.missions.DelayedFleetEncounter;
import com.fs.starfarer.api.impl.campaign.missions.academy.GAPZPostEncounters;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * 
 */
public class ZigguratCMD extends BaseCommandPlugin {

	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected MemoryAPI memory;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;

	
	public ZigguratCMD() {
	}
	
	public ZigguratCMD(SectorEntityToken entity) {
		init(entity);
	}
	
	protected void init(SectorEntityToken entity) {
		memory = entity.getMemoryWithoutUpdate();
		this.entity = entity;
		playerFleet = Global.getSector().getPlayerFleet();
		
		
	}

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		entity = dialog.getInteractionTarget();
		init(entity);
		
		memory = getEntityMemory(memoryMap);
		
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		
		if (command.equals("initEncounters")) {
			initEncounters();	
		} else if (command.equals("createSecondDiktatEncounter")) {
			createSecondDiktatEncounter();	
		} else if (command.equals("abortSecondDiktatEncounter")) {
			abortSecondDiktatEncounter();	
		} else if (command.equals("updateData")) {
			updateData();
		}
		
		return true;
	}
	
	protected void updateData() {
		boolean hasZiggurat = false;
		boolean hasNonZiggurat = false;
		for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
			if (member.getHullSpec().getBaseHullId().equals("ziggurat")) {
				memory.set("$zigguratShipName", member.getShipName(), 0f);
				memory.set("$zigguratMember", member, 0f);
				hasZiggurat = true;
			} else {
				hasNonZiggurat = true;
			}
		}
		memory.set("$hasZiggurat", hasZiggurat, 0f);
		memory.set("$hasNonZiggurat", hasNonZiggurat, 0f);
		memory.set("$hasOnlyZiggurat", hasZiggurat && !hasNonZiggurat, 0f);
		
		memory.set("$ttZigBuyPrice", Misc.getWithDGS(2000000), 0f);
		memory.set("$ttZigLowBuyPrice", Misc.getWithDGS(100000), 0f);
	}

	protected void initEncounters() {
		GAPZPostEncounters.init();
	}
	
	protected void createSecondDiktatEncounter() {
		DelayedFleetEncounter e = GAPZPostEncounters.createSecondDiktatEncounter();
		memory.set("$gaPZ_secondDiktatEncounter", e, 0);
	}
	
	protected void abortSecondDiktatEncounter() {
		DelayedFleetEncounter e = (DelayedFleetEncounter)memory.get("$gaPZ_secondDiktatEncounter");
		if (e != null) {
			e.setAlwaysAbort();
		}
	}
	
}




















