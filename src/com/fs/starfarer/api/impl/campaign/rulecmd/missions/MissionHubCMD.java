package com.fs.starfarer.api.impl.campaign.rulecmd.missions;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseMissionHub;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionHub;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	MissionHubCMD <command>
 */
public class MissionHubCMD extends BaseCommandPlugin { 

	protected SectorEntityToken entity;
	protected InteractionDialogPlugin originalPlugin;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected PersonAPI person;
	protected MissionHub hub;
	protected MarketAPI market;
	protected TextPanelAPI text;

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		if (dialog == null) return false;

		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		entity = dialog.getInteractionTarget();
		originalPlugin = dialog.getPlugin();
		market = entity.getMarket();
		
		text = dialog.getTextPanel();
		person = dialog.getInteractionTarget().getActivePerson();
		
		hub = BaseMissionHub.get(person);
		
		if (command.equals("hasHub")) {
			return hub != null && person != null;
		}
		
		return true;
	}
	
}


















