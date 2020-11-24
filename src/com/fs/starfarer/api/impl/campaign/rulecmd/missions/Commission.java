package com.fs.starfarer.api.impl.campaign.rulecmd.missions;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.FactionCommissionIntel;
import com.fs.starfarer.api.impl.campaign.intel.BaseMissionIntel.MissionResult;
import com.fs.starfarer.api.impl.campaign.intel.BaseMissionIntel.MissionState;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * NotifyEvent $eventHandle <params> 
 * 
 */
public class Commission extends BaseCommandPlugin {
	
	public static RepLevel COMMISSION_REQ = RepLevel.FAVORABLE;
	
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected CargoAPI playerCargo;
	protected MemoryAPI memory;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected PersonAPI person;
	protected FactionAPI faction;

	protected boolean offersCommissions;
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		memory = getEntityMemory(memoryMap);
		
		entity = dialog.getInteractionTarget();
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		
		person = dialog.getInteractionTarget().getActivePerson();
		faction = person.getFaction();
		
		offersCommissions = faction.getCustomBoolean("offersCommissions");
		
		
		//printInfo
		//accept
		
		if (command.equals("printRequirements")) {
			printRequirements();
		} else if (command.equals("playerMeetsCriteria")) {
			return playerMeetsCriteria();
		} else if (command.equals("printInfo")) {
			printInfo();
		} else if (command.equals("hasFactionCommission")) {
			return hasFactionCommission();
		} else if (command.equals("hasOtherCommission")) {
			if (hasOtherCommission()) {
				memory.set("$theOtherCommissionFaction", Misc.getCommissionFaction().getDisplayNameWithArticle(), 0);
				return true;
			}
			return false;
		} else if (command.equals("accept")) {
			accept();
		} else if (command.equals("resign")) {
			resign();
		} else if (command.equals("personCanGiveCommission")) {
			return personCanGiveCommission();
		}
		
		return true;
	}

	protected boolean hasFactionCommission() {
		return faction.getId().equals(Misc.getCommissionFactionId());
	}
	protected boolean hasOtherCommission() {
		return Misc.getCommissionFactionId() != null && !hasFactionCommission();
	}
	
	
	protected boolean personCanGiveCommission() {
		if (person == null || !offersCommissions) return false;
		if (person.getFaction().isPlayerFaction()) return false;
		
		//if (Misc.getCommissionFactionId() != null) return false;
		
		return Ranks.POST_BASE_COMMANDER.equals(person.getPostId()) ||
			   Ranks.POST_STATION_COMMANDER.equals(person.getPostId()) ||
			   Ranks.POST_ADMINISTRATOR.equals(person.getPostId()) ||
			   Ranks.POST_OUTPOST_COMMANDER.equals(person.getPostId());
	}
	
	protected void resign() {
		FactionCommissionIntel intel = Misc.getCommissionIntel();
		MissionResult result = intel.createResignedCommissionResult(true, true, dialog);
		intel.setMissionResult(result);
		intel.setMissionState(MissionState.ABANDONED);
		intel.endMission(dialog);
	}
	
	protected void accept() {
		if (Misc.getCommissionFactionId() == null) {
			FactionCommissionIntel intel = new FactionCommissionIntel(faction);
			intel.missionAccepted();
			intel.sendUpdate(FactionCommissionIntel.UPDATE_PARAM_ACCEPTED, dialog.getTextPanel());
			intel.makeRepChanges(dialog);
		}
	}
	
	protected void printInfo() {
		TooltipMakerAPI info = dialog.getTextPanel().beginTooltip();
		
		FactionCommissionIntel temp = new FactionCommissionIntel(faction);
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		info.setParaSmallInsignia();

		int stipend = (int) temp.computeStipend();
		
		info.addPara("At your experience level, you would receive a %s monthly stipend, as well as a modest bounty for destroying enemy ships.",
				0f, h, Misc.getDGSCredits(stipend));
		
		List<FactionAPI> hostile = temp.getHostileFactions();
		if (hostile.isEmpty()) {
			info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " is not currently hostile to any major factions.", 0f);
		} else {
			info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " is currently hostile to:", opad);
			
			info.setParaFontDefault();
			
			info.setBulletedListMode(BaseIntelPlugin.INDENT);
			float initPad = opad;
			for (FactionAPI other : hostile) {
				info.addPara(Misc.ucFirst(other.getDisplayName()), other.getBaseUIColor(), initPad);
				initPad = 3f;
			}
			info.setBulletedListMode(null);
		}
		
		
		dialog.getTextPanel().addTooltip();
	}
	
	
	protected boolean playerMeetsCriteria() {
		return faction.getRelToPlayer().isAtWorst(COMMISSION_REQ);
	}
	protected void printRequirements() {
		CoreReputationPlugin.addRequiredStanding(entityFaction, COMMISSION_REQ, null, dialog.getTextPanel(), null, null, 0f, true);
		CoreReputationPlugin.addCurrentStanding(entityFaction, null, dialog.getTextPanel(), null, null, 0f);
	}
	
	
	
}















