package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class ANewHope extends HubMissionWithSearch {

	public static enum Stage {
		GO_TO_VOLTURN,
		COMPLETED,
	}
	
	protected PersonAPI robed_man;
	//protected PersonAPI some_kid; 
	//protected PersonAPI robot; 
	
	protected MarketAPI volturn;
	//protected MarketAPI asharu;
	
	public static float MISSION_DAYS = 120f;
	
	protected int payment;
	protected int paymentHigh;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$anh_ref", "$anh_inProgress")) {
			return false;
		}
		
		setPersonOverride(null);
		
		robed_man = getImportantPerson(People.ROBEDMAN);
		if (robed_man == null) return false;
		
		
		volturn = Global.getSector().getEconomy().getMarket("volturn");
		if (volturn == null) return false;
		if (!volturn.getFactionId().equals("sindrian_diktat")) return false;
		
		setStartingStage(Stage.GO_TO_VOLTURN);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();
		
		// yes, these exact numbers.
		payment = 10000;
		paymentHigh = 17000; 
		
		makeImportant(volturn, "$anh_tookTheJob", Stage.GO_TO_VOLTURN);
		//setStageOnMemoryFlag(Stage.COMPLETED, baird.getMarket(), "$gaTTB_completed");
		
		setStageOnGlobalFlag(Stage.COMPLETED, "$anh_completed");
		
		setRepFactionChangesNone();
		setRepPersonChangesNone();
		
		// spawn Diktat Patrol fleet to intercept the player
		beginEnteredLocationTrigger(volturn.getStarSystem(), false, Stage.GO_TO_VOLTURN);
		triggerCreateFleet(FleetSize.SMALL, FleetQuality.DEFAULT, Factions.DIKTAT, FleetTypes.PATROL_SMALL, volturn.getStarSystem());
        triggerAutoAdjustFleetStrengthMajor();
        triggerMakeHostileAndAggressive();
        triggerFleetAllowLongPursuit();
        triggerSetFleetAlwaysPursue();
        triggerPickLocationTowardsPlayer(volturn.getPlanetEntity(), 90f, getUnits(0.25f));
        triggerSpawnFleetAtPickedLocation("$anh_diktatPatrol", null);
        triggerSetFleetMissionRef("$ahn_ref");
        triggerOrderFleetInterceptPlayer();
        triggerFleetMakeImportant(null, Stage.GO_TO_VOLTURN);
        endTrigger();
        
        beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValue("$anh_missionCompleted", true);
		endTrigger();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
			set("$anh_stage", getCurrentStage());
			//set("$anh_robedman", robed_man);
			set("$anh_payment", Misc.getWithDGS(payment));
			set("$anh_paymentHigh", Misc.getWithDGS(paymentHigh));
	}

	@Override
	protected boolean callAction(String action, String ruleId, final InteractionDialogAPI dialog,
								 List<Token> params, final Map<String, MemoryAPI> memoryMap) {
//		if ("THEDUEL".equals(action)) {
//			TextPanelAPI text = dialog.getTextPanel();
//			text.setFontOrbitronUnnecessarilyLarge();
//			Color color = Misc.getBasePlayerColor();
//			color = Global.getSector().getFaction(Factions.HEGEMONY).getBaseUIColor();
//			text.addPara("THE DUEL", color);
//			text.setFontInsignia();
//			text.addImage("misc", "THEDUEL");
//			return true;
//		}
		
		return super.callAction(action, ruleId, dialog, params, memoryMap);
	}

	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		
		info.addImage(robed_man.getPortraitSprite(), width, 128, opad);
		
		if (currentStage == Stage.GO_TO_VOLTURN) {
			info.addPara("Deliver the mysterious old man, the kid, and their robot to Volturn in the Askonia system. Avoid patrols; they may be wanted by the authorities.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_VOLTURN) {
			info.addPara("Go to Volturn in the Askonia system", tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Passage To Volturn";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
}





