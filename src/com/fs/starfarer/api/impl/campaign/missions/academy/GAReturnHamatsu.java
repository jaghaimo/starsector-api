package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.missions.DelayedFleetEncounter;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.world.TTBlackSite;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class GAReturnHamatsu extends GABaseMission { //implements ShipRecoveryListener {

	public static enum Stage {
		RETURN_HAMATSU,
		COMPLETED,
		FAILED,
	}
	
	protected PersonAPI callisto;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		if (!setGlobalReference("$gaRH_ref", null)) {
			return false;
		}
		
		callisto = getImportantPerson(People.IBRAHIM);
		if (callisto == null) return false;
		
		setPersonOverride(callisto);
		
		//setStoryMission();
		
		setStartingStage(Stage.RETURN_HAMATSU);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		connectWithGlobalFlag(Stage.RETURN_HAMATSU, Stage.COMPLETED, "$gaRH_completed");
		connectWithGlobalFlag(Stage.RETURN_HAMATSU, Stage.FAILED, "$gaRH_failed");
		makeImportant(callisto, "$gaRH_returnHere", Stage.RETURN_HAMATSU);
		
		setCreditReward(100000);
		setRepRewardPerson(RepRewards.EXTREME);
		setRepRewardFaction(RepRewards.HIGH);
		
		setRepPenaltyPerson(0f); // handled in rules, is in fact very high
		setRepPenaltyFaction(RepRewards.HIGH);
		
		setPersonIsPotentialContactOnSuccess(callisto, 1f);
		
		return true;
	}
	
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
								 Map<String, MemoryAPI> memoryMap) {

		if ("refusedToReturn".equals(action)) {
			if (rollProbability(0.5f)) {
				DelayedFleetEncounter e = new DelayedFleetEncounter(genRandom, getMissionId());
				e.setDelayMedium();
				e.setLocationInnerSector(false, Factions.INDEPENDENT);
				e.beginCreate();
				e.triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.VERY_HIGH, Factions.MERCENARY, FleetTypes.PATROL_LARGE, new Vector2f());
				e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
				e.triggerFleetSetFaction(Factions.INDEPENDENT);
				e.triggerSetFleetFlag("$gaRH_consequences");
				e.triggerMakeNoRepImpact();
				e.triggerSetStandardAggroInterceptFlags();
				e.endCreate();
			}
			return true;
		} else if ("transferHamatsu".equals(action)) {
			FleetMemberAPI hamatsu = null;
			for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
				if (member.getId().equals(TTBlackSite.HAMATSU_ID)) {
					hamatsu = member;
					break;
				}
			}
			if (hamatsu != null) {
				AddRemoveCommodity.addFleetMemberLossText(hamatsu, dialog.getTextPanel());
				Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(hamatsu);
			}
			
			return true;
		}
		return false;
	}
	
	protected void updateInteractionDataImpl() {
		set("$gaRH_stage", getCurrentStage());
		set("$gaRH_reward", Misc.getWithDGS(getCreditsReward()));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.RETURN_HAMATSU) {
			addStandardMarketDesc("Deliver the ISS Hamatsu to " + callisto.getNameString()
					+ " " + callisto.getMarket().getOnOrAt() + "", callisto.getMarket(), info, opad);
			info.addPara("She is sentimental about the ship - her first independent command - and is willing to pay a "
					+ "large sum for its return.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.RETURN_HAMATSU) {
			info.addPara("Deliver the ISS Hamatsu to " + callisto.getNameString(), tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Return the ISS Hamatsu";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
	
	
}





