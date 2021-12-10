package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class GAIntro2 extends GABaseMission {

	public static enum Stage {
		GO_TO_ACADEMY,
		COMPLETED,
	}
	
	protected int gaIntro2_credits;
	protected PersonAPI baird;
	protected PersonAPI sebestyen;
	protected boolean pointAtSebestyen = false;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		
		//System.out.print("attempting to start gaIntro2");
		
		// if already accepted by the player, abort
		if (!setGlobalReference("$gaIntro2_ref")) {
			System.out.print("aborting because missions is already accepted");
			return false;
		}
		
		baird = getImportantPerson(People.BAIRD);
		if (baird == null) return false;
		
		sebestyen = getImportantPerson(People.SEBESTYEN);
		if (sebestyen == null) return false;
		
		setStartingStage(Stage.GO_TO_ACADEMY);
		//setStartingStage(Stage.GO_TO_ACADEMY_SEB);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();
		
		//System.out.print("galatia found");

		gaIntro2_credits = 18000; // so its a fixed number, sue me.

		setStartingStage(Stage.GO_TO_ACADEMY);
		addSuccessStages(Stage.COMPLETED);
		
		
		setStoryMission();
		makeImportant(baird.getMarket(), "$gaIntro2_returnHere", Stage.GO_TO_ACADEMY);
		setStageOnGlobalFlag(Stage.GO_TO_ACADEMY, "$gaIntro2_started"); // so it isn't offered again
		
		//setStageOnGlobalFlag(Stage.GO_TO_ACADEMY_SEB, "$gaIntro2_started"); // so it isn't offered again
		setStageOnGlobalFlag(Stage.COMPLETED, "$gaIntro2_completed");
		
		setRepFactionChangesNone();
		setRepPersonChangesNone();
		
		//System.out.print("starting gaIntro2 for real");
		
		return true;
	}
	
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		if ("makeSebestyenImportant".equals(action)) {
			if (!pointAtSebestyen) {
				makeImportant(sebestyen, null, Stage.GO_TO_ACADEMY);
				makePrimaryObjective(sebestyen);
				makeUnimportant(baird.getMarket());
				pointAtSebestyen = true;
			}
			return true;
		} 
		return false;
	}
	
	protected void updateInteractionDataImpl() {
		set("$gaIntro2_stage", getCurrentStage());
		set("$gaIntro2_credits", Misc.getWithDGS(gaIntro2_credits));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		//Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_ACADEMY) {
			if (pointAtSebestyen) {
				info.addPara("Return the data core to Academician Sebestyen. He'll get you your reward.", opad);
				addStandardMarketDesc("Sebestyen is located " + sebestyen.getMarket().getOnOrAt(), sebestyen.getMarket(), info, opad);
			}
			else
			{
				info.addPara("Return the data core to the Galatia Academy for a reward.", opad);
			}
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		//Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_ACADEMY) {
			
			if (pointAtSebestyen) {
				info.addPara("Return the data core to Academician Sebestyen at the Galatia Academy in the Galatia system", tc, pad);
			}
			else
			{
				info.addPara("Go to the Galatia Academy in the Galatia system", tc, pad);
			}
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Return the Data Core";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
}





