package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class GATalkToSebestyen extends GABaseMission {

	public static enum Stage {
		TALK_TO_SEBESTYEN,
		COMPLETED,
	}
	
	protected PersonAPI sebestyen;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$gaTTS_ref")) {
			return false;
		}
		
		sebestyen = getImportantPerson(People.SEBESTYEN);
		if (sebestyen == null) return false;
		
		setStartingStage(Stage.TALK_TO_SEBESTYEN);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();
		
		makeImportant(sebestyen, null, Stage.TALK_TO_SEBESTYEN);
		setStageOnMemoryFlag(Stage.COMPLETED, sebestyen.getMarket(), "$gaTTS_completed");
		
		setRepFactionChangesNone();
		setRepPersonChangesNone();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
	
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.TALK_TO_SEBESTYEN) {
			info.addPara("Talk to Academician " + sebestyen.getName().getLast() + " at the Galatia Academy.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.TALK_TO_SEBESTYEN) {
			info.addPara("Talk to Academician " + sebestyen.getName().getLast() + " at the Galatia Academy", tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Talk to Academician " + sebestyen.getName().getLast();
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}

	
}





