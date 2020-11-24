package com.fs.starfarer.api.impl.campaign.rulecmd.missions;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.DeliveryMissionIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * 
 */
public class DeliveryMission extends BaseCommandPlugin {

	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected CargoAPI playerCargo;
	protected MemoryAPI memory;
	protected MarketAPI market;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected FactionAPI faction;

	
	public DeliveryMission() {
		
	}
	
	protected void init(SectorEntityToken entity) {
		memory = entity.getMemoryWithoutUpdate();
		this.entity = entity;
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		
		faction = entity.getFaction();
		
		market = entity.getMarket();
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
		
		if (command.equals("completeMissions")) {
			completeMissions();
		} else if (command.equals("checkCompletion")) {
			return checkCompletion();
		}
		
		
		
		return true;
	}

	protected boolean checkCompletion() {
		for (IntelInfoPlugin temp : Global.getSector().getIntelManager().getIntel(DeliveryMissionIntel.class)) {
			DeliveryMissionIntel intel = (DeliveryMissionIntel) temp;
			if (!intel.isAccepted()) continue;
			if (intel.isEnding()) continue;
			
			MarketAPI dest = intel.getDestination();
			if (dest != market) continue;
			
			if (intel.hasEnough()) return true;
		}
		return false;
	}

	protected void completeMissions() {
		dialog.getTextPanel().addPara("You contact the relevant parties and drop off the cargo at the agreed-upon dockside locations.");
		
		for (IntelInfoPlugin temp : Global.getSector().getIntelManager().getIntel(DeliveryMissionIntel.class)) {
			DeliveryMissionIntel intel = (DeliveryMissionIntel) temp;
			if (!intel.isAccepted()) continue;
			if (intel.isEnding()) continue;
			
			MarketAPI dest = intel.getDestination();
			if (dest != market) continue;
			
			if (intel.hasEnough()) {
				intel.performDelivery(dialog);
			}
		}
		
		options.clearOptions();
		options.addOption("Continue", MarketCMD.DEBT_RESULT_CONTINUE);
		
	}


}




















