package com.fs.starfarer.api.impl.campaign.missions.academy;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.missions.DelayedFleetEncounter;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

@SuppressWarnings("unchecked")
public class GADeliverVIP extends GABaseMission {


	public static float MISSION_DAYS = 60f;
	public static float PROB_KANTA = 0.1f;
	public static float PROB_MERC_KIDNAPPER = 0.5f;
	public static float PROB_PIRATE_KIDNAPPER = 0.5f;
	
	public static float SPECIFIC_FACTION_SUBJECT_EVENT_LIKELIHOOD = 2f;
	

	
	public static class FactionData {
		//public Pair<String, String>[] subjects; 
		public String[] subjects; 
		public String[] events; 
	}
	
	public static FactionData ALL_FACTIONS;
	public static Map<String, FactionData> FACTION_DATA = new HashMap<String, FactionData>();
	public static List<String> ALLOWED_FACTIONS = new ArrayList<String>();
	public static List<String> MERC_FACTIONS = new ArrayList<String>();
	static {
		ALLOWED_FACTIONS.add(Factions.INDEPENDENT);
		ALLOWED_FACTIONS.add(Factions.PERSEAN);
		ALLOWED_FACTIONS.add(Factions.TRITACHYON);
		ALLOWED_FACTIONS.add(Factions.HEGEMONY);
		ALLOWED_FACTIONS.add(Factions.LUDDIC_CHURCH);
		
		MERC_FACTIONS.add(Factions.TRITACHYON);
		MERC_FACTIONS.add(Factions.HEGEMONY);
		MERC_FACTIONS.add(Factions.DIKTAT);
		MERC_FACTIONS.add(Factions.LUDDIC_CHURCH);
		
		ALL_FACTIONS = new FactionData();
		ALL_FACTIONS.subjects = new String[] {
			"child",	
			"son",
			"daughter",
			"niece",	
			"nephew",	
			"ward",
		};
		ALL_FACTIONS.events = new String [] {
				"a wedding", "a funeral", "a reunion", "a judicial procedure", "an operation", "a family gathering", "their legitimation"  
		};
		
		FactionData luddic = new FactionData();
		FACTION_DATA.put(Factions.LUDDIC_CHURCH, luddic);
		luddic.subjects = new String[] {
			"godchild",	
			"goddaughter",	
			"godson",	
			"apprentice",	
		};
		luddic.events = new String [] {
				"a spiritual retreat", "a penitent seclusion", "their Path-stepping", "their godnaming", "their blessing", "their contemplation", 
				"their consecration", "their invocation", "their witnessing", "their ministration", "an ordination ceremony", "an Exodus Fast"
		};
		
		FactionData hegemony = new FactionData();
		FACTION_DATA.put(Factions.HEGEMONY, hegemony);
		hegemony.subjects = new String[] {
				"unit-ward",	
		};
		hegemony.events = new String [] {
				"their service induction", "cadet training", "survival school", "a warship launching", "a COMSEC interview",
				"a unit commemoration", "Legion remembrance day"
		};
		
		FactionData tritach = new FactionData();
		FACTION_DATA.put(Factions.TRITACHYON, tritach);
		tritach.subjects = new String[] {
				"contract-ward",	
		};
		tritach.events = new String [] {
				"a cybernetic enhancement", "gene therapy", "an enrichment retreat", "a cryo-sabbatical ceremony",
				"a promotion celebration"
		};
		
		FactionData league = new FactionData();
		FACTION_DATA.put(Factions.PERSEAN, league);
		league.subjects = new String[] {
				"gensheir",	
		};
		league.events = new String [] {
				"a coronation", "a premiere", "their coming-of-majority", "a landing commemoration", "a titanium jubilee",
				"a grand fete", "a carnival"
		};
		
		FactionData pirates = new FactionData();
		FACTION_DATA.put(Factions.PIRATES, pirates);
		pirates.subjects = new String[] {
				"clanward", "clone-variant",
		};
		pirates.events = new String [] {
				"a \"blooding\"", "their induction", "their \"making\"", "a grand castigation", "a Grand Division", "an \"unmaking\""
		};
	}
	
	public String pickSubject(String factionId) {
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(genRandom);
		for (String p : ALL_FACTIONS.subjects) {
			picker.add(p, 1f);
		}
		FactionData data = FACTION_DATA.get(factionId);
		if (data != null) {
			float w = Math.max(1f, data.subjects.length) / Math.max(1f, picker.getTotal());
			w *= SPECIFIC_FACTION_SUBJECT_EVENT_LIKELIHOOD; // faction-specific is more likely than generic, overall
			for (String p : data.subjects) {
				picker.add(p, w);
			}
		}
		return picker.pick();
	}
	
	public String pickEvent(String factionId) {
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(genRandom);
		for (String event : ALL_FACTIONS.events) {
			picker.add(event, 1f);
		}
		FactionData data = FACTION_DATA.get(factionId);
		if (data != null) {
			float w = Math.max(1f, data.events.length) / Math.max(1f, picker.getTotal());
			w *= SPECIFIC_FACTION_SUBJECT_EVENT_LIKELIHOOD; // faction-specific is more likely than generic, overall
			for (String event : data.events) {
				picker.add(event, w);
			}
		}
		return picker.pick();
	}
	
	
	public static enum Stage {
		DELIVER_VIP,
		COMPLETED,
		FAILED,
		FAILED_DECIV,
	}
	
	public static enum Variation {
		BASIC,
		KANTA,
	}
	
	protected StarSystemAPI system;
	protected MarketAPI destination;
	protected Variation variation;
	protected FactionAPI faction;
	protected String theMercFaction;
	protected String mercFactionId;
	
	protected String subjectRelation;
	protected String event;
	protected PersonAPI target;
	protected String kantaRelationFirstName;

	protected int piratePayment;
	protected int mercPayment;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission type was already accepted by the player, abort
		if (!setGlobalReference("$gaVIP_ref")) {
			return false;
		}

		requireMarketFaction(ALLOWED_FACTIONS.toArray(new String [0]));
		requireMarketLocationNot("galatia");
		requireMarketNotHidden();
		requireMarketNotInHyperspace();
		preferMarketInDirectionOfOtherMissions();
		
//		PROB_KANTA = 1f;
//		PROB_MERC_KIDNAPPER = 1f;
		
		destination = pickMarket();
		variation = Variation.BASIC;
		if (rollProbability(PROB_KANTA)) {
			MarketAPI kantasDen = Global.getSector().getEconomy().getMarket("kantas_den");
			if (kantasDen != null) {
				destination = kantasDen;
				variation = Variation.KANTA;
				kantaRelationFirstName = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson(genRandom).getName().getFirst();
			}
		}
		if (destination == null) return false;
		
		faction = destination.getFaction();
		subjectRelation = pickSubject(faction.getId());
		if (subjectRelation == null) return false;
		
		event = pickEvent(faction.getId());
		if (event == null) return false;
		
		if (variation == Variation.BASIC) {
			target = findOrCreatePerson(faction.getId(), destination, true, Ranks.CITIZEN, 
							Ranks.POST_ADMINISTRATOR, Ranks.POST_BASE_COMMANDER, Ranks.POST_STATION_COMMANDER,
							Ranks.POST_OUTPOST_COMMANDER, Ranks.POST_PORTMASTER, Ranks.POST_FACTION_LEADER
							);
//			target.addTag(Tags.CONTACT_TRADE);
//			setPersonIsPotentialContactOnSuccess(target);
		} else if (variation == Variation.KANTA) {
			// set the VIP to Kanta, for rep purposes, since the sub-boss that's the actual relation doesn't exist
			PersonDataAPI pd = Global.getSector().getImportantPeople().getData("kanta");
			if (pd != null) target = pd.getPerson();
		}
		if (target == null) return false;
		
		
		system = destination.getStarSystem();
		
		setStartingStage(Stage.DELIVER_VIP);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		addNoPenaltyFailureStages(Stage.FAILED_DECIV);
		
		// used for generic pirate reaction
		if (variation == Variation.KANTA) {
			setGlobalFlag("$gaVIP_workingForKanta", true, Stage.DELIVER_VIP);
		}
		
		makeImportant(destination, "$gaVIP_target", Stage.DELIVER_VIP);
		
		setStageOnGlobalFlag(Stage.COMPLETED, "$gaVIP_delivered");
		setStageOnGlobalFlag(Stage.FAILED, "$gaVIP_failed");
		connectWithMarketDecivilized(Stage.DELIVER_VIP, Stage.FAILED_DECIV, destination);
		
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
		if (variation == Variation.BASIC) {
			//setCreditReward(30000, 40000);
			setCreditReward(CreditReward.AVERAGE);
			setRepPenaltyPerson(RepRewards.VERY_HIGH);
			setRepPenaltyFaction(RepRewards.HIGH);
		} else {
			//setCreditReward(50000, 70000);
			setCreditReward(CreditReward.HIGH);
			setRepPenaltyPerson(RepRewards.EXTREME);
			setRepPenaltyFaction(RepRewards.HIGH);
		}
		
		setDefaultGARepRewards();
		
//		beginStageTrigger(Stage.DELIVER_VIP);
//		LocData data = new LocData(EntityLocationType.HIDDEN_NOT_NEAR_STAR, null, system);
//		triggerSpawnShipGraveyard(Factions.REMNANTS, 10, 10, data);
//		endTrigger();
		
		piratePayment = genRoundNumber(40000, 60000);
		mercPayment = getCreditsReward() / 2;
		
		if (variation == Variation.BASIC && rollProbability(PROB_PIRATE_KIDNAPPER)) {
			beginWithinHyperspaceRangeTrigger(destination, 3f, false, Stage.DELIVER_VIP);
			triggerCreateFleet(FleetSize.LARGE, FleetQuality.HIGHER, Factions.PIRATES, FleetTypes.PATROL_MEDIUM, system);
			triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
			triggerAutoAdjustFleetStrengthMajor();
			triggerSetStandardAggroPirateFlags();
			triggerFleetAllowLongPursuit();
			triggerSetFleetAlwaysPursue();
			triggerPickLocationTowardsPlayer(system.getHyperspaceAnchor(), 90f, getUnits(1.5f));
			triggerSpawnFleetAtPickedLocation("$gaVIP_pirate", null);
			triggerOrderFleetInterceptPlayer();
			triggerFleetMakeImportant(null, Stage.DELIVER_VIP);
			endTrigger();
		}
		
		if (variation == Variation.KANTA && rollProbability(PROB_MERC_KIDNAPPER)) {
			mercFactionId = pickOne(MERC_FACTIONS.toArray(new String[0]));
			FactionAPI mercFaction = Global.getSector().getFaction(mercFactionId);
			theMercFaction = mercFaction.getDisplayNameWithArticle();
			
			beginWithinHyperspaceRangeTrigger(destination, 3f, true, Stage.DELIVER_VIP);
			triggerCreateFleet(FleetSize.VERY_LARGE, FleetQuality.VERY_HIGH, Factions.MERCENARY, FleetTypes.PATROL_LARGE, system);
			triggerSetFleetFaction(Factions.INDEPENDENT);
			triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
			triggerAutoAdjustFleetStrengthMajor();
			triggerMakeHostileAndAggressive();
			triggerFleetMakeFaster(true, 2, true);
			//triggerMakeNoRepImpact(); // this happens in dialog instead
			triggerFleetAllowLongPursuit();
			triggerSetFleetAlwaysPursue();
			triggerPickLocationTowardsPlayer(system.getHyperspaceAnchor(), 90f, getUnits(1.5f));
			triggerSpawnFleetAtPickedLocation("$gaVIP_merc", null);
			triggerOrderFleetInterceptPlayer();
			triggerFleetMakeImportant(null, Stage.DELIVER_VIP);
			endTrigger();
		}
		
		return true;
	}

	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
								 Map<String, MemoryAPI> memoryMap) {
		if (action.equals("betrayal")) {
			DelayedFleetEncounter e = new DelayedFleetEncounter(genRandom, getMissionId());
			e.setDelayMedium();
			e.setLocationInnerSector(false, Factions.INDEPENDENT);
			e.beginCreate();
			e.triggerCreateFleet(FleetSize.LARGE, FleetQuality.SMOD_3, Factions.MERCENARY, FleetTypes.PATROL_LARGE, new Vector2f());
			e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.UNUSUALLY_HIGH);
			e.triggerFleetSetFaction(Factions.INDEPENDENT);
			e.triggerFleetMakeFaster(true, 2, true);
			e.triggerSetFleetFlag("$gaVIP_kantaConsequences");
			e.triggerMakeNoRepImpact();
			e.triggerSetStandardAggroInterceptFlags();
			e.endCreate();
			return true;
		}
		return false;
	}

	protected void updateInteractionDataImpl() {
		if (getCurrentStage() != null) {
			set("$gaVIP_stage", ((Enum)getCurrentStage()).name());
		}
		set("$gaVIP_starName", system.getNameWithNoType());
		set("$gaVIP_marketName", destination.getName());
		set("$gaVIP_systemName", system.getNameWithLowercaseTypeShort());
		
		set("$gaVIP_subjectRelation", subjectRelation);
		set("$gaVIP_kantaRelationFirstName", kantaRelationFirstName);
		set("$gaVIP_VIP", target);
		set("$gaVIP_VIP_faction", target.getFaction().getId());
		set("$gaVIP_VIPName", target.getNameString());
		set("$gaVIP_VIPhisOrHer", target.getHisOrHer());
		set("$gaVIP_VIPPost", target.getPost().toLowerCase());
		set("$gaVIP_event", event);
		
		set("$gaVIP_reward", Misc.getWithDGS(getCreditsReward()));
		set("$gaVIP_piratePayment", Misc.getWithDGS(piratePayment));
		set("$gaVIP_mercPayment", Misc.getWithDGS(mercPayment));
		set("$gaVIP_theMercFaction", theMercFaction);
		set("$gaVIP_mercFactionId", mercFactionId);
		set("$gaVIP_timeLimit", (int)MISSION_DAYS);
		set("$gaVIP_variation", variation);
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.DELIVER_VIP) {
			if (variation == Variation.BASIC) {
				info.addPara("Deliver the " + subjectRelation + " of " + target.getNameString() + " to " + 
						destination.getName() + " in the " + system.getNameWithLowercaseTypeShort() + ", for " +
						event + ". " +
						target.getNameString() + " is the " + target.getPost().toLowerCase() + ".", opad);
			} else if (variation == Variation.KANTA) {
				info.addPara("Deliver the " + subjectRelation + " of " + kantaRelationFirstName + " Kanta to " + 
						destination.getName() + " in the " + system.getNameWithLowercaseTypeShort() + ", for " +
						event + ". " +
						kantaRelationFirstName + " is kin to Kanta herself.", opad);
			}
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.DELIVER_VIP) {
			info.addPara("Deliver student to " + destination.getName() + " in the " + system.getNameWithLowercaseTypeShort(), tc, pad);
			return true;
		}
		return false;
	}
	
	
	@Override
	public String getBaseName() {
		return "Deliver VIP";
	}
	
}


