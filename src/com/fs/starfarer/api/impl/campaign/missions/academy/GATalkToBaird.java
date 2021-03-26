package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class GATalkToBaird extends GABaseMission {

	public static enum Stage {
		TALK_TO_BAIRD,
		COMPLETED,
	}
	
	protected PersonAPI baird;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$gaTTB_ref")) {
			return false;
		}
		
		baird = getImportantPerson(People.BAIRD);
		if (baird == null) return false;
		
		setStartingStage(Stage.TALK_TO_BAIRD);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();
		
		makeImportant(baird, null, Stage.TALK_TO_BAIRD);
		setStageOnMemoryFlag(Stage.COMPLETED, baird.getMarket(), "$gaTTB_completed");
		
		setRepFactionChangesNone();
		setRepPersonChangesNone();
		
		beginStageTrigger(Stage.TALK_TO_BAIRD);
		triggerSetGlobalMemoryValuePermanent("$bairdWantsToTalk", true);
		endTrigger();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
	
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.TALK_TO_BAIRD) {
			info.addPara("Talk to Provost " + baird.getName().getLast() + " at the Galatia Academy.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.TALK_TO_BAIRD) {
			info.addPara("Talk to Provost " + baird.getName().getLast() + " at the Galatia Academy", tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Talk to Provost " + baird.getName().getLast();
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}

	
}





