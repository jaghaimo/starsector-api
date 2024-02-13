package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class RuinsDataSwapMission extends HubMissionWithBarEvent {

	//public static float PIRATE_PROB = 0.5f;
	public static float MISSION_DAYS = 120f;
	public static int RAID_DIFFICULTY = 60;
	public static int MARINES_REQUIRED = RAID_DIFFICULTY / 2;
	
	
	public static enum Stage {
		GO_TO_RUINS,
		RETURN,
		COMPLETED,
		FAILED,
	}
	
	public static enum Variation {
		BASIC,
		DECIV,
	}
	
	protected MarketAPI market;
	protected PersonAPI person;
	protected PlanetAPI planet;
	protected String targetWithArticle;
	protected String target;
	protected String megacorp;
	protected Variation variation;
	//protected int piratePayment;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission type was already accepted by the player, abort
		if (!setGlobalReference("$rdsm_ref")) {
			return false;
		}
		
		targetWithArticle = pickOne("a library", "a datavault", "an archive", "a laboratory");
		target = targetWithArticle.substring(targetWithArticle.indexOf(" ") + 1);
		
		megacorp = pickOne("Fabrique Orbitale", "Bhilai Exospace", "Hastaeus Industries", "Exogen Systems", "Ursa Group");
		
		resetSearch();
		requireSystemTags(ReqMode.ANY, Tags.THEME_REMNANT_SUPPRESSED, Tags.THEME_DERELICT, Tags.THEME_MISC, Tags.THEME_RUINS);
		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_SPECIAL);
		//requireSystemInInnerSector();
		//requireSystemHasPulsar();
		//requireSystemIsDense();
		//requirePlanetIsGasGiant();
		//requirePlanetConditions(ReqMode.ALL, Conditions.DECIVILIZED);
		requirePlanetUnpopulated();
		requirePlanetWithRuins();
		preferPlanetNotFullySurveyed();
		preferPlanetUnexploredRuins();
		preferPlanetInDirectionOfOtherMissions();
		planet = pickPlanet();
		
//		spawnEntity(Entities.INACTIVE_GATE, "$gaData_test", EntityLocationType.ORBITING_PARAM, 
//					planet, planet.getStarSystem(), false);
//		spawnMissionNode("$gaData_test", EntityLocationType.ORBITING_PARAM, planet, planet.getStarSystem());
		
		if (planet == null) return false;

		person = getPerson();
		if (person == null) return false;
		
		market = person.getMarket();
		if (market == null) return false;
		
		setStartingStage(Stage.GO_TO_RUINS);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		makeImportant(planet, "$rdsm_targetPlanet", Stage.GO_TO_RUINS);
		makeImportant(person, "$rdsm_contact", Stage.RETURN);
		
		connectWithGlobalFlag(Stage.GO_TO_RUINS, Stage.RETURN, "$rdsm_gotData");
		connectWithGlobalFlag(Stage.RETURN, Stage.COMPLETED, "$rdsm_returnedData");
		
		setNoAbandon();
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.RETURN);
		
		setCreditReward(CreditReward.AVERAGE);

//		spawnEntity(Entities.FUSION_LAMP, "$gaData_test", EntityLocationType.ORBITING_PARAM,
//						planet, planet.getStarSystem(), false);
		
//		beginStageTrigger(Stage.GET_IN_COMMS_RANGE);
//		triggerSpawnEntity(Entities.INACTIVE_GATE, "$gaData_test", EntityLocationType.ORBITING_PARAM,
//						   planet, planet.getStarSystem(), false);
//		triggerEntityMakeImportant();
//		endTrigger();
		
		//StarSystemAPI system = planet.getStarSystem();
		if (planet.hasCondition(Conditions.DECIVILIZED)) {
			variation = Variation.DECIV;
		} else {
			variation = Variation.BASIC;
		}
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$rdsm_contactName", person.getNameString());
		set("$rdsm_megacorpName", megacorp);
		set("$rdsm_market", market.getName());
		set("$rdsm_target", target);
		set("$rdsm_planetId", planet.getId());
		set("$rdsm_planetName", planet.getName());
		set("$rdsm_systemName", planet.getStarSystem().getNameWithLowercaseType());
		set("$rdsm_dist", getDistanceLY(planet));
		set("$rdsm_reward", Misc.getWithDGS(getCreditsReward()));
		//set("$gaData_piratePayment", Misc.getWithDGS(piratePayment));
		//variation = Variation.BASIC;
		set("$rdsm_variation", variation);
		if (variation == Variation.DECIV) {
			set("$rdsm_marinesReq", MARINES_REQUIRED);
			set("$rdsm_raidDifficulty", RAID_DIFFICULTY);
		}
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_RUINS) {
			if (variation == Variation.DECIV) {
				info.addPara(getGoToPlanetTextPre(planet) +
							", and swap the datacore from the " + megacorp + " " + target + " in the ruins there. There have "
									+ "been reports of decivs so you should bring at least %s " +
							 "marines to ensure the job goes smoothly.", opad, h, Misc.getWithDGS(MARINES_REQUIRED));
			} else {
				String extra = "";
				info.addPara(getGoToPlanetTextPre(planet) + 
							 ", and swap the datacore from the " + megacorp + " " + target + " in the ruins there with the one provided." + extra, opad);
			}
		} else if (currentStage == Stage.RETURN) {
			info.addPara(getReturnText(market), opad);
		}
//		else {
//			super.addDescriptionForCurrentStage(info, width, height); // shows the completed/failed/abandoned text, if needed
//		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_RUINS) {
			//info.addPara("Go to " + planet.getName() + " in the " + planet.getStarSystem().getNameWithLowercaseTypeShort(), tc, pad);
			info.addPara(getGoToPlanetTextShort(planet), tc, pad);
			return true;
		} else if (currentStage == Stage.RETURN) {
			info.addPara(getReturnTextShort(market), tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Ruins Datacore Swap";
	}
	
	@Override
	public String getBlurbText() {
		return null; // rules.csv
	}

}


