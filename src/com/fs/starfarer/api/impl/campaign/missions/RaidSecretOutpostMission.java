package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.SmugglingMission.Stage;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class RaidSecretOutpostMission extends HubMissionWithBarEvent {

	//public static float PIRATE_PROB = 0.5f;
	public static float MISSION_DAYS = 120f;
	public static int RAID_DIFFICULTY = 48;
	public static int MARINES_REQUIRED = RAID_DIFFICULTY / 2;
	public static float MIN_VALUE = 80000f;
	public static float MAX_VALUE = 140000f;
	
	public static enum Stage {
		GO_TO_OUTPOST,
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
	protected int goodsAmount;
	protected CommodityOnMarketAPI com;
	protected int quantity;
	//protected Variation variation;

	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission type was already accepted by the player, abort
		if (!setGlobalReference("$rsom_ref")) {
			return false;
		}
		
		resetSearch();
		requireSystemTags(ReqMode.ANY, Tags.THEME_REMNANT_SUPPRESSED, Tags.THEME_DERELICT, Tags.THEME_MISC, Tags.THEME_RUINS);
		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_SPECIAL);
		requireSystemInInnerSector();
		//requireSystemHasPulsar();
		//requireSystemIsDense();
		//requirePlanetIsGasGiant();
		requirePlanetNotGasGiant(); // for the writing.
		requirePlanetConditions(ReqMode.NOT_ANY, Conditions.DECIVILIZED);
		requirePlanetUnpopulated();
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
		
		setStartingStage(Stage.GO_TO_OUTPOST);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		makeImportant(planet, "$rsom_targetPlanet", Stage.GO_TO_OUTPOST);
		//makeImportant(person, "$rsom_contact", Stage.RETURN);
		
		connectWithGlobalFlag(Stage.GO_TO_OUTPOST, Stage.COMPLETED, "$rsom_raidedOutpost");
		//connectWithGlobalFlag(Stage.RETURN, Stage.COMPLETED, "$rsom_returnedData");
		
		setNoAbandon();
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
		
		requireCommodityIllegal();
		requireCommodityDemandAtLeast(1);
		
		//market = com.getMarket();
		//if (market == null) return false;
		
		com = pickCommodity();
		if (com == null) return false;
		
		float value = MIN_VALUE + getQuality() * (MAX_VALUE - MIN_VALUE);
		value *= 0.9f + genRandom.nextFloat() * 0.2f;
		
		quantity = getRoundNumber(value / com.getCommodity().getBasePrice());
		if (quantity < 10) quantity = 10;
		
		
		//setCreditReward(CreditReward.AVERAGE);

//		spawnEntity(Entities.FUSION_LAMP, "$gaData_test", EntityLocationType.ORBITING_PARAM,
//						planet, planet.getStarSystem(), false);
		
//		beginStageTrigger(Stage.GET_IN_COMMS_RANGE);
//		triggerSpawnEntity(Entities.INACTIVE_GATE, "$gaData_test", EntityLocationType.ORBITING_PARAM,
//						   planet, planet.getStarSystem(), false);
//		triggerEntityMakeImportant();
//		endTrigger();
		
		//StarSystemAPI system = planet.getStarSystem();
		
		setRepFactionChangesNone(); // intra-underworld meddling pans out to no change, let's say.

		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$rsom_contactName", person.getNameString());
		set("$rsom_market", market.getName());
		set("$rsom_planetId", planet.getId());
		set("$rsom_planetName", planet.getName());
		set("$rsom_systemName", planet.getStarSystem().getNameWithLowercaseType());
		set("$rsom_dist", getDistanceLY(planet));
		set("$rsom_product", com.getCommodity().getLowerCaseName());
		set("$rsom_productID", com.getCommodity().getId());
		set("$rsom_quantity", quantity);
		set("$rsom_marinesReq", MARINES_REQUIRED);
		set("$rsom_raidDifficulty", RAID_DIFFICULTY);
		
	}
	
	@Override
	protected boolean callAction(String action, String ruleId, final InteractionDialogAPI dialog,
								 List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		if ("giveOutpostPlunder".equals(action)) {

			return true;
		}
		
		return super.callAction(action, ruleId, dialog, params, memoryMap);
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_OUTPOST) {

			info.addPara(getGoToPlanetTextPre(planet) +
						", use the codes to bypass defenses to raid and plunder the " + com.getCommodity().getLowerCaseName() + " in the hidden outpost there. "
						+ "Bring at least %s marines to ensure success.", opad, h, Misc.getWithDGS(MARINES_REQUIRED));
			
		//} else if (currentStage == Stage.RETURN) {
		//	info.addPara(getReturnText(market), opad);
		}
//		else {
//			super.addDescriptionForCurrentStage(info, width, height); // shows the completed/failed/abandoned text, if needed
//		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_OUTPOST) {
			//info.addPara("Go to " + planet.getName() + " in the " + planet.getStarSystem().getNameWithLowercaseTypeShort(), tc, pad);
			info.addPara(getGoToPlanetTextShort(planet), tc, pad);
			return true;
		//} else if (currentStage == Stage.RETURN) {
		//	info.addPara(getReturnTextShort(market), tc, pad);
		//	return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Raid Secret Outpost";
	}
	
	@Override
	public String getBlurbText() {
		return null; // rules.csv
	}

}


