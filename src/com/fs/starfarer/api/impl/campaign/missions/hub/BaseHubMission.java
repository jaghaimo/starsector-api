package com.fs.starfarer.api.impl.campaign.missions.hub;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainPlugin;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.HasMemory;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.MissionCompletionRep;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictType;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.missions.CheapCommodityMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerAction;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerActionContext;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public abstract class BaseHubMission extends BaseIntelPlugin implements HubMission {
	
	public static float DEBRIS_SMALL = 200f;
	public static float DEBRIS_MEDIUM = 350f;
	public static float DEBRIS_LARGE = 500f;
	
	public static float DEBRIS_SPARSE = 0.25f;
	public static float DEBRIS_AVERAGE = 0.5f;
	public static float DEBRIS_DENSE = 1f;
	
	public static float GLOBAL_MISSION_REWARD_MULT = 1f;
	
	public static int EXTRA_REWARD_PER_MARINE = 50;
	
	public static enum CreditReward {
		VERY_LOW(20000, 25000, 2000),
		LOW(30000, 40000, 5000),
		AVERAGE(50000, 60000, 10000),
		HIGH(70000, 80000, 15000),
		VERY_HIGH(90000, 100000, 20000),
//		VERY_LOW(10000, 15000, 2000),
//		LOW(20000, 25000, 5000),
//		AVERAGE(30000, 40000, 10000),
//		HIGH(50000, 60000, 15000),
//		VERY_HIGH(80000, 100000, 20000),
		;
		public int min;
		public int max;
		public int perMarketSize;
		private CreditReward(int min, int max, int perMarketSize) {
			this.min = (int) (min * GLOBAL_MISSION_REWARD_MULT);
			this.max = (int) (max * GLOBAL_MISSION_REWARD_MULT);
			this.perMarketSize = (int) (perMarketSize * GLOBAL_MISSION_REWARD_MULT);
		}
	}
	
	
	
	public static String BUTTON_ABANDON = "Abandon";
	
//	public static int MIN_LEVEL_TO_SCALE_XP_GAIN_AT = 1;
//	public static int MAX_LEVEL_TO_SCALE_XP_GAIN_AT = 5;
//	public static enum XPReward {
//		VERY_LOW(0.05f),
//		LOW(0.1f),
//		MEDIUM(0.15f),
//		HIGH(0.2f),
//		VERY_HIGH(0.3f),
//		;
//		
//		float fractionOfLevel;
//		private XPReward(float fractionOfLevel) {
//			this.fractionOfLevel = fractionOfLevel;
//		}
//		public float getFractionOfLevel() {
//			return fractionOfLevel;
//		}
//	}
	
	public static enum Abandon {
		ABANDON
	}
	
	public static enum EntityLocationType {
		HIDDEN,
		HIDDEN_NOT_NEAR_STAR,
		ORBITING_PLANET,
		ORBITING_PLANET_OR_STAR,
		UNCOMMON, /* similar to HIDDEN, but can also orbit jump-points */
		ANY,
		ORBITING_PARAM,
	}
	
	public static class LocData {
		public CampaignTerrainAPI terrain;
		public EntityLocation loc;
		public EntityLocationType type;
		public SectorEntityToken centerOn;
		public LocationAPI system; // can be hyperspace when type is ORBITING_PARAM or EntityLocation is set
		public boolean removeOnMissionOver;
		
		/**
		 * At provided EntityLocation.
		 */
		public LocData(EntityLocation loc, LocationAPI system, boolean removeOnMissionOver) {
			this.loc = loc;
			this.removeOnMissionOver = removeOnMissionOver;
			this.system = system;
		}
		
		/**
		 * Generate an EntityLocation based on the type. centerOn is used if type is ORBITING_PARAM.
		 */
		public LocData(EntityLocationType type, SectorEntityToken centerOn, LocationAPI system,
								 boolean removeOnMissionOver) {
			this.type = type;
			this.centerOn = centerOn;
			this.system = system;
			this.removeOnMissionOver = removeOnMissionOver;
		}
		
		/**
		 * Set to a zero-radius orbit around centerOn.
		 */
		public LocData(SectorEntityToken centerOn, boolean removeOnMissionOver) {
			if (centerOn instanceof CampaignTerrainAPI) {
				this.centerOn = centerOn;
			} else {
				loc = new EntityLocation();
				loc.type = LocationType.OUTER_SYSTEM;
				loc.orbit = Global.getFactory().createCircularOrbit(centerOn, 0f, 0f, 1000f);
			}
			system = centerOn.getContainingLocation();
			this.removeOnMissionOver = removeOnMissionOver;
		}
		
		/**
		 * At fixed coordinates, no orbit.
		 */
		public LocData(Vector2f loc, LocationAPI system, boolean removeOnMissionOver) {
			this.loc = new EntityLocation();
			this.loc.type = LocationType.OUTER_SYSTEM;
			this.loc.location = loc;
			this.system = system;
			this.removeOnMissionOver = removeOnMissionOver;
		}
		
		/**
		 * At provided EntityLocation.
		 */
		public LocData(EntityLocation loc, LocationAPI system) {
			this(loc, system, true);
		}
		
		/**
		 * Generate an EntityLocation based on the type. centerOn is used if type is ORBITING_PARAM.
		 */
		public LocData(EntityLocationType type, SectorEntityToken centerOn, LocationAPI system) {
			this(type, centerOn, system, true);
		}
		
		/**
		 * Set to a zero-radius orbit around centerOn.
		 */
		public LocData(SectorEntityToken centerOn) {
			this(centerOn, true);
		}
		
		/**
		 * At fixed coordinates, no orbit.
		 */
		public LocData(Vector2f loc, LocationAPI system) {
			this(loc, system, true);
		}
		
		public boolean updateLocIfNeeded(BaseHubMission mission, String entityId) {
			if (centerOn instanceof CampaignTerrainAPI) {
				CampaignTerrainAPI terrain = (CampaignTerrainAPI) centerOn;
				loc = mission.generateLocationInsideTerrain(terrain);
				if (loc == null) return false;
			} else if (type != null) {
				loc = mission.generateLocation(entityId, type, centerOn, system);
				if (loc == null) return false;
			}
			return true;
		}
		
		public void placeEntity(SectorEntityToken entity) {
			//if (!updateLocIfNeeded(mission, entity.getCustomEntityType())) return false;
			
			if (loc.orbit != null) {
				entity.setOrbit(loc.orbit);
				loc.orbit.setEntity(entity);
			} else {
				entity.setOrbit(null);
				entity.getLocation().set(loc.location);
			}
			
			if (removeOnMissionOver) {
				entity.addTag(REMOVE_ON_MISSION_OVER);
			}
		}
	}
	
	
	
	
	public static class HubMissionResult {
		public boolean success;
		public int reward;
		public int xp;
		public ReputationAdjustmentResult repPerson;
		public ReputationAdjustmentResult repFaction;
		public Object custom;
	}
	
	public static enum MapLocationType {
		NORMAL,
		CONSTELLATION;
	}
	public static class ImportanceData {
		public MemoryAPI memory;
		public String flag;
		public MapLocationType locType = null;
		public SectorEntityToken entity;
		public MarketAPI market;
		public PersonAPI person;
		
		public ImportanceData() {
		}
	}
	
	public static class FlagData {
		public MemoryAPI memory;
		public String flag;
		public LinkedHashSet<Object> stages = new LinkedHashSet<Object>();
	}
	
	public static class StageData {
		public Object id;
		public float elapsed = 0f;
		//public LinkedHashMap<MemoryAPI, String> important = new LinkedHashMap<MemoryAPI, String>();
		public List<ImportanceData> important = new ArrayList<ImportanceData>();
	}
	
	public static interface ConditionChecker {
		boolean conditionsMet();
	}
	
	public static class GlobalBooleanChecker implements ConditionChecker {
		public String flag;
		public GlobalBooleanChecker(String flag) {
			this.flag = flag;
		}
		public boolean conditionsMet() {
			return Global.getSector().getMemoryWithoutUpdate().getBoolean(flag);
		}
	}
	
	public static class MemoryBooleanChecker implements ConditionChecker {
		public String flag;
		public MemoryAPI memory;
		public MemoryBooleanChecker(MemoryAPI memory, String flag) {
			this.memory = memory;
			this.flag = flag;
		}
		public boolean conditionsMet() {
			return memory.getBoolean(flag);
		}
	}
	
	public static class EntityNotAliveChecker implements ConditionChecker {
		public SectorEntityToken entity;
		public EntityNotAliveChecker(SectorEntityToken entity) {
			this.entity = entity;
		}
		public boolean conditionsMet() {
			return !entity.isAlive();
		}
	}
	
	public static class MarketDecivChecker implements ConditionChecker {
		public MarketAPI market;
		public MarketDecivChecker(MarketAPI market) {
			this.market = market;
		}
		public boolean conditionsMet() {
			return market.isPlanetConditionMarketOnly() ||
				   (market.getPrimaryEntity() != null && !market.getPrimaryEntity().isAlive());
		}
	}
	
	public static class HostilitiesEndedChecker implements ConditionChecker {
		public PersonAPI person;
		public MarketAPI market;
		public HostilitiesEndedChecker(PersonAPI person, MarketAPI market) {
			this.person = person;
			this.market = market;
		}
		public boolean conditionsMet() {
			return !person.getFaction().isHostileTo(market.getFaction());
		}
	}
	
	public static class HostilitiesStartedChecker implements ConditionChecker {
		public PersonAPI person;
		public MarketAPI market;
		public HostilitiesStartedChecker(PersonAPI person, MarketAPI market) {
			this.person = person;
			this.market = market;
		}
		public boolean conditionsMet() {
			return person.getFaction().isHostileTo(market.getFaction());
		}
	}
	
	public static class DaysElapsedChecker implements ConditionChecker {
		public float days;
		public StageData stage;
		public BaseHubMission mission;
		public DaysElapsedChecker(float days, StageData stage) {
			this.days = days;
			this.stage = stage;
		}
		
		public DaysElapsedChecker(float days, BaseHubMission mission) {
			this.days = days;
			this.mission = mission;
		}

		public boolean conditionsMet() {
			if (mission != null) {
				return mission.elapsed >= days;
			}
			return stage.elapsed >= days;
		}
	}
	
	public static class InCommRelayRangeChecker implements ConditionChecker {
		public boolean conditionsMet() {
			return Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay();
		}
	}
	
	public static class InRangeOfEntityChecker implements ConditionChecker {
		public SectorEntityToken entity;
		public float range;
		public InRangeOfEntityChecker(SectorEntityToken entity, float range) {
			this.entity = entity;
			this.range = range;
		}
		public boolean conditionsMet() {
			return Global.getSector().getCurrentLocation() == entity.getContainingLocation() &&
							Misc.getDistance(Global.getSector().getPlayerFleet(), entity) < range;
		}
	}
	
	public static class InHyperRangeOfEntityChecker implements ConditionChecker {
		public SectorEntityToken entity;
		public float rangeLY;
		public boolean requirePlayerInHyperspace;
		public InHyperRangeOfEntityChecker(SectorEntityToken entity, float rangeLY, boolean requirePlayerInHyperspace) {
			this.entity = entity;
			this.rangeLY = rangeLY;
			this.requirePlayerInHyperspace = requirePlayerInHyperspace;
		}
		public boolean conditionsMet() {
			if (requirePlayerInHyperspace && !Global.getSector().getPlayerFleet().isInHyperspace()) return false;
			return Misc.getDistanceLY(Global.getSector().getPlayerFleet(), entity) < rangeLY;
		}
	}
	
	public static class EnteredLocationChecker implements ConditionChecker {
		public LocationAPI location;
		public EnteredLocationChecker(LocationAPI location) {
			this.location = location;
		}
		public boolean conditionsMet() {
			return Global.getSector().getCurrentLocation() == location;
		}
	}
	
	public static class AlwaysTrueChecker implements ConditionChecker {
		public boolean conditionsMet() {
			return true;
		}
	}
	
	public static class StageConnection {
		public Object from;
		public Object to;
		public ConditionChecker checker;
		public StageConnection(Object from, Object to, ConditionChecker checker) {
			this.from = from;
			this.to = to;
			this.checker = checker;
		}
	}
	
	public static class TimeLimitData {
		public float days;
		public Object failStage;
		public LinkedHashSet<Object> endLimitStages = new LinkedHashSet<Object>();
		public StarSystemAPI noLimitWhileInSystem;
	}
	
	public static interface Abortable {
		void abort(HubMission mission, boolean missionOver);
	}
	
	public static class VariableSet implements Abortable {
		public MemoryAPI memory;
		public String key;
		public boolean removeOnMissionOver;
		public VariableSet(MemoryAPI memory, String key, boolean removeOnMissionOver) {
			this.memory = memory;
			this.key = key;
			this.removeOnMissionOver = removeOnMissionOver;
		}

		public void abort(HubMission mission, boolean missionOver) {
			if (!removeOnMissionOver && missionOver) return;
			memory.unset(key);
		}
	}
	
	public static class MadeImportant implements Abortable {
		public MemoryAPI memory;
		public String reason;
		public MadeImportant(MemoryAPI memory, String reason) {
			this.memory = memory;
			this.reason = reason;
		}
		public void abort(HubMission mission, boolean missionOver) {
			Misc.makeUnimportant(memory, reason);
		}
	}
	
	public static class DefeatTriggerAdded implements Abortable {
		protected CampaignFleetAPI fleet;
		protected String trigger;
		protected boolean permanent;
		public DefeatTriggerAdded(CampaignFleetAPI fleet, String trigger, boolean permanent) {
			this.fleet = fleet;
			this.trigger = trigger;
			this.permanent = permanent;
		}
		public void abort(HubMission mission, boolean missionOver) {
			if (!(permanent && missionOver)) {
				Misc.removeDefeatTrigger(fleet, trigger);
			}
		}
	}
	
	public static String REMOVE_ON_MISSION_OVER = "remove_on_mission_over";
	public static class EntityAdded implements Abortable {
		public SectorEntityToken entity;
		public EntityAdded(SectorEntityToken entity) {
			this.entity = entity;
		}

		public void abort(HubMission mission, boolean missionOver) {
			if (missionOver) {
				if (!entity.hasTag(REMOVE_ON_MISSION_OVER)) return;
				if (entity.hasTag(Tags.FADING_OUT_AND_EXPIRING)) return;
			}
			if (entity.getContainingLocation() != null) {
				entity.getContainingLocation().removeEntity(entity);
			}
		}
	}
	
	public static class PersonAdded implements Abortable {
		public MarketAPI market;
		public PersonAPI person;
		public boolean wasOnlyAddedToCommDirectory;

		public PersonAdded(MarketAPI market, PersonAPI person, boolean wasOnlyAddedToCommDirectory) {
			this.market = market;
			this.person = person;
			this.wasOnlyAddedToCommDirectory = wasOnlyAddedToCommDirectory;
		}

		public void abort(HubMission mission, boolean missionOver) {
			if (missionOver && !person.hasTag(REMOVE_ON_MISSION_OVER)) return;
			
			if (mission instanceof BaseHubMission) {
				BaseHubMission bhm = (BaseHubMission) mission;
				if (Misc.setFlagWithReason(person.getMemoryWithoutUpdate(), 
						"$requiredForMissions", bhm.getReason(), false, -1f)) {
					return;
				}
			}
			
			if (!wasOnlyAddedToCommDirectory) {
				if (market != null) market.removePerson(person);
				Global.getSector().getImportantPeople().removePerson(person);
			}
			if (market != null) market.getCommDirectory().removePerson(person);
		}
	}
	
	public static class PersonMadeRequired implements Abortable {
		public PersonAPI person;
		
		public PersonMadeRequired(PersonAPI person) {
			this.person = person;
		}
		
		public void abort(HubMission mission, boolean missionOver) {
			if (mission instanceof BaseHubMission) {
				BaseHubMission bhm = (BaseHubMission) mission;
				if (Misc.setFlagWithReason(person.getMemoryWithoutUpdate(), 
						"$requiredForMissions", bhm.getReason(), false, -1f)) {
					return;
				}
			}
		}
	}
	
	protected Object currentStage = null;
	protected LinkedHashMap<Object, StageData> stages = new LinkedHashMap<Object, StageData>();
	protected Boolean stageTransitionsRepeatable = null;
	protected List<Object> successStages = new ArrayList<Object>();
	protected List<Object> failStages = new ArrayList<Object>();
	protected List<Object> noPenaltyFailStages = new ArrayList<Object>();
	protected Object abandonStage = Abandon.ABANDON;
	protected List<StageConnection> connections = new ArrayList<StageConnection>();
	protected List<MissionTrigger> triggers = new ArrayList<MissionTrigger>();
	protected List<Abortable> changes = new ArrayList<Abortable>();
	protected List<FlagData> flags = new ArrayList<FlagData>();
	
	protected transient Object startingStage;
	
	protected transient CargoAPI cargoOnAccept = Global.getFactory().createCargo(true);	
	protected CargoAPI cargoOnSuccess = null;	
	
	protected float elapsed = 0f;
	protected TimeLimitData timeLimit;
	protected HubMissionResult result;
	
	
	protected MissionHub hub;
	protected PersonAPI personOverride = null;
	protected HubMissionCreator creator;
	protected Random genRandom = null;
	
	protected IntervalUtil tracker = new IntervalUtil(0.09f, 0.11f);
	
	protected Float repRewardPerson = null;
	protected Float repPenaltyPerson = null;
	protected Float repRewardFaction = null;
	protected Float repPenaltyFaction = null;
	protected Integer creditReward = null;
	protected Integer xpReward = null;
	
	protected String iconName;
	
	protected RepLevel rewardLimitPerson = null; 
	protected RepLevel rewardLimitFaction = null; 
	protected RepLevel penaltyLimitPerson = null; 
	protected RepLevel penaltyLimitFaction = null; 
	
	protected String missionId;
	
	protected float rewardMult = 1f;
	protected float quality = 0f;
	
	protected IntelSortTier sortTier = null;
	
	public static class PotentialContactData {
		public PersonAPI contact;
		public float probability = -1f;
	}
	
	protected List<PotentialContactData> potentialContactsOnMissionSuccess = null;
	protected Boolean doNotAutoAddPotentialContactsOnSuccess = null;
	
	public BaseHubMission() {
	}
	
	public void setStageTransitionsRepeatable() {
		stageTransitionsRepeatable = true;
	}
	
	public void setDoNotAutoAddPotentialContactsOnSuccess() {
		this.doNotAutoAddPotentialContactsOnSuccess = true;
	}

	public void setGiverIsPotentialContactOnSuccess() {
		setPersonIsPotentialContactOnSuccess(getPerson());
	}
	
	public void setGiverIsPotentialContactOnSuccess(float probability) {
		setPersonIsPotentialContactOnSuccess(getPerson(), probability);
	}
	
	public void setPersonIsPotentialContactOnSuccess(PersonAPI person) {
		setPersonIsPotentialContactOnSuccess(person, -1f);
	}
	public void setPersonIsPotentialContactOnSuccess(PersonAPI person, float probability) {
		if (person == null) return;
		if (potentialContactsOnMissionSuccess == null) {
			potentialContactsOnMissionSuccess = new ArrayList<PotentialContactData>();
		}
		for (PotentialContactData data : potentialContactsOnMissionSuccess) {
			if (data.contact == person) return;
		}
		PotentialContactData data = new PotentialContactData();
		data.contact = person;
		data.probability = probability;
		potentialContactsOnMissionSuccess.add(data);
	}
	
	public void setGenRandom(Random random) {
		genRandom = random;
	}
	
	public void setMissionId(String missionId) {
		this.missionId = missionId;
	}
	public String getMissionId() {
		return missionId;
	}
	
	public boolean isBarEvent() {
		return isBarEvent;
	}

	protected boolean isBarEvent = false;
	public void createAndAbortIfFailed(MarketAPI market, boolean barEvent) {
		isBarEvent = barEvent;
		if (getPerson() != null) {
			genMissionRewardMultAndQuality();
		}
		if (!create(market, barEvent)) {
			abort();
		}
	}
	protected abstract boolean create(MarketAPI createdAt, boolean barEvent);
	
	protected String baseName = null;
	
	public String getBaseName() {
		if (baseName != null) {
			return baseName;
		}
		return "Call setName(<name>) to set mission name";
	}
	public void setName(String name) {
		baseName = name;
	}
	
	public String getBlurbText() {
		return null;
	}
	
//	public abstract boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad);
//	public abstract void addDescriptionForCurrentStage(TooltipMakerAPI info, float width, float height);
	
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		String text = getNextStepText();
		if (text != null) {
			info.addPara(text, tc, pad);
			return true;
		}
		return false;
	}
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		
	}
	public void addDescriptionForCurrentStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		String text = getStageDescriptionText();
		if (text != null) {
			info.addPara(text, opad);
		} else {
			String noun = getMissionTypeNoun();
			String verb = getMissionCompletionVerb();
			if (isSucceeded()) {
				info.addPara("You have successfully " + verb + " this " + noun + ".", opad);
			} else if (isFailed()) {
				info.addPara("You have failed this " + noun + ".", opad);
			} else if (isAbandoned()) {
				info.addPara("You have abandoned this " + noun + ".", opad);
			} else {
				addDescriptionForNonEndStage(info, width, height);
			}
		}
	}
	public String getStageDescriptionText() {
		return null;
	}
	public String getNextStepText() {
		return null;
	}
	
	@Override
	protected void advanceImpl(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		StageData stage = getData(currentStage);
		if (stage != null) {
			stage.elapsed += days;
		}
		
		//elapsed += days * 100f;
		elapsed += days;

		//if (timeLimit != null) timeLimit.days = 240;
		if (timeLimit != null && timeLimit.days < elapsed &&
				(timeLimit.noLimitWhileInSystem == null || 
						timeLimit.noLimitWhileInSystem != Global.getSector().getCurrentLocation())) {
			setCurrentStage(timeLimit.failStage, null, null);
			timeLimit = null;
			runTriggers();
		}
		
		tracker.advance(days);
		if (tracker.intervalElapsed()) {
			checkStageChangesAndTriggers(null, null);
		}
	}
	
	public float getElapsedInCurrentStage() {
		StageData stage = getData(currentStage);
		if (stage != null) {
			return stage.elapsed;
		}
		return 0f;
	}
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		Global.getSector().removeScript(this);
	}

	public void accept(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		setImportant(true);
		
		if (startingStage == null) {
			throw new RuntimeException("startingStage can not be null. Use setStartingStage()");
		}
		
		// don't pass in dialog/memoryMap so it doesn't add an update item to the textPanel
		// might not anyway, but that depends on some future decisions...
		setCurrentStage(startingStage, null, null); //dialog, memoryMap);
		
		acceptImpl(dialog, memoryMap);
		
		TextPanelAPI text = dialog != null ? dialog.getTextPanel() : null;
		
		if (cargoOnAccept != null) {
			cargoOnAccept.sort();
			for (CargoStackAPI stack : cargoOnAccept.getStacksCopy()) {
				Global.getSector().getPlayerFleet().getCargo().addItems(stack.getType(), stack.getData(), stack.getSize());
				if (text != null) {
					AddRemoveCommodity.addStackGainText(stack, text);
				}
			}
			cargoOnAccept.clear();
		}
		
		Global.getSector().getIntelManager().addIntel(this, false, text);
		Global.getSector().addScript(this);
		
		startingStage = null;
		
		runTriggers();
	}
	
	public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		
	}

	protected transient boolean aborted = false;
	public void abort() {
		boolean missionWasAccepted = currentStage != null;
		for (Abortable curr : changes) {
			curr.abort(this, missionWasAccepted);
		}
		changes.clear();
		
		//if (genRandom != null) {
		if (!missionWasAccepted) {
			aborted = true;
		}
		
		// is this needed? not if missions are reset every time in BaseMissionHub.updateOfferedMissions()
		// and not saved
//		currentStage = null;
//		stages.clear();
//		successStages.clear();
//		failStages.clear();
//		connections.clear();
//		
//		genRandom = null;
//		startingStage = null;
	}
	
	public boolean isMissionCreationAborted() {
		return aborted;
	}
	
	
	public String getTriggerPrefix() {
		//return getClass().getSimpleName();
		return getMissionId();
	}
	
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		return false;
	}
	
	protected void addPotentialContacts(InteractionDialogAPI dialog) {
		if (potentialContactsOnMissionSuccess != null) {
			for (PotentialContactData curr : potentialContactsOnMissionSuccess) {
				PersonAPI p = curr.contact;
				MarketAPI m = curr.contact.getMarket();
				if (m == null) m = getPerson().getMarket();
				if (m != null) {
					if (curr.probability < 0) {
						ContactIntel.addPotentialContact(p, m, dialog != null ? dialog.getTextPanel() : null);
					} else {
						ContactIntel.addPotentialContact(curr.probability, p, m, dialog != null ? dialog.getTextPanel() : null);
					}
				}
			}
			potentialContactsOnMissionSuccess = null;
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		
		if ("endFailure".equals(action)) {
			Object stage = null;
			if (!failStages.isEmpty()) {
				stage = failStages.get(0);
			}
			if (stage == null && !noPenaltyFailStages.isEmpty()) {
				stage = noPenaltyFailStages.get(0);
			}
			if (stage != null) {
				setCurrentStage(stage, dialog, memoryMap);
				runTriggers();
			}
			return true;
		}
		
		if ("makeUnimportant".equals(action)) {
			SectorEntityToken target = dialog.getInteractionTarget();
			if (target != null) {
				makeUnimportant(target, (Enum) currentStage);
				if (target.getMarket() != null) {
					makeUnimportant(target.getMarket(), (Enum) currentStage);
				}
				if (target.getActivePerson() != null) {
					makeUnimportant(target.getActivePerson(), (Enum) currentStage);
				}
			}
			return true;
		}
		
		if ("showMap".equals(action)) {
			SectorEntityToken mapLoc = getMapLocation(null, startingStage);
			if (mapLoc != null) {
				String title = params.get(1).getStringWithTokenReplacement(ruleId, dialog, memoryMap);
				String text = "";
				Set<String> tags = getIntelTags(null);
				tags.remove(Tags.INTEL_ACCEPTED);
				String icon = getIcon();
				
				Color color = getFactionForUIColors().getBaseUIColor();
				//String factionId = getFactionForUIColors().getId();
				if (mapLoc != null && mapLoc.getFaction() != null && !mapLoc.getFaction().isNeutralFaction()) {
					color = mapLoc.getFaction().getBaseUIColor();
					//factionId = mapLoc.getFaction().getId();
				} else if (mapLoc instanceof PlanetAPI) {
					PlanetAPI planet = (PlanetAPI) mapLoc;
					if (planet.getStarSystem() != null && planet.getFaction().isNeutralFaction()) {
						StarSystemAPI system = planet.getStarSystem();
						if (system.getStar() == planet || system.getCenter() == planet) {
							if (planet.getMarket() != null) {
								color = planet.getMarket().getTextColorForFactionOrPlanet();
							} else {
								color = Misc.setAlpha(planet.getSpec().getIconColor(), 255);
								color = Misc.setBrightness(color, 235);
							}
						} else {
							color = Misc.setAlpha(planet.getSpec().getIconColor(), 255);
							color = Misc.setBrightness(color, 235);
						}
					}
				}
				if (mapMarkerNameColor != null) {
					color = mapMarkerNameColor;
				}
				
				dialog.getVisualPanel().showMapMarker(mapLoc, 
						title, color, 
						true, icon, text, tags);
			}
			return true;
		}
		
		if ("hideMap".equals(action)) {
			dialog.getVisualPanel().removeMapMarkerFromPersonInfo();
			return true;
		}
		
//		if (action.equals("endSuccess")) {
//			doNotEndMission = true;
//			checkStageChangesAndTriggers();
//			doNotEndMission = false;
//			endSuccess(dialog, memoryMap);
//		} else if (action.equals("endFailure")) {
//			doNotEndMission = true;
//			checkStageChangesAndTriggers();
//			doNotEndMission = false;
//			endFailure(dialog, memoryMap);
//		} else
		if (action.equals("updateStage")) {
			checkStageChangesAndTriggers(dialog, memoryMap);
		} else if (action.equals("updateData")) {
			checkStageChangesAndTriggers(dialog, memoryMap);
			updateInteractionData(dialog, memoryMap);
		} else if (action.equals("addContacts")) {
			addPotentialContacts(dialog);
		} else if (action.equals("repSuccess")) {
			adjustRep(dialog.getTextPanel(), null, RepActions.MISSION_SUCCESS);
		} else if (action.equals("repFailure")) {
			adjustRep(dialog.getTextPanel(), null, RepActions.MISSION_FAILURE);
		} else {
			if (!callAction(action, ruleId, dialog, params, memoryMap)) {
				throw new RuntimeException("Unhandled action [" + action + "] in " + getClass().getSimpleName() + 
										   " for rule [" + ruleId + "], params:[" + params + "]");
			}
		}
		return true;
	}
	
	protected void showPersonInfo(PersonAPI person, InteractionDialogAPI dialog, boolean withFaction, boolean withRelBar) {
		dialog.getInteractionTarget().setActivePerson(person);
		dialog.getVisualPanel().showPersonInfo(person, !withFaction, withRelBar);
	}
	
	protected transient MemoryAPI interactionMemory = null;
	public void updateInteractionData(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		interactionMemory = memoryMap.get(MemKeys.LOCAL);
		if (interactionMemory == null) {
			if (dialog.getInteractionTarget().getActivePerson() != null) {
				interactionMemory = dialog.getInteractionTarget().getActivePerson().getMemoryWithoutUpdate();	
			} else {
				interactionMemory = dialog.getInteractionTarget().getMemoryWithoutUpdate();
			}
		}

		// unless we're already talking to a person
		// actually, always do it; this happens in a bar event
		// don't always do it - if updateData is called when talking to *another* person
		// (i.e. not the mission giver) this would override their $HeOrShe etc tokens
		if (isBarEvent() || !memoryMap.containsKey(MemKeys.ENTITY)) {
			setPersonTokens(interactionMemory);
		}

		if (getCurrentStage() != null) {
			set("$" + getMissionId() + "_stage", ((Enum)getCurrentStage()).name());
		}
		
		updateInteractionDataImpl();
	}
	
	
	protected void updateInteractionDataImpl() {
		
	}
	
	protected void setPersonTokens(MemoryAPI mem) {
		PersonAPI person = getPerson();
		if (person == null) return;
		
		if (person.isMale()) {
			mem.set("$hisOrHer", "his", 0);
			mem.set("$HisOrHer", "His", 0);
			mem.set("$himOrHer", "him", 0);
			mem.set("$HimOrHer", "Him", 0);
			mem.set("$heOrShe", "he", 0);
			mem.set("$HeOrShe", "He", 0);
			mem.set("$himOrHerself", "himself", 0);
			mem.set("$HimOrHerself", "Himself", 0);
			mem.set("$manOrWoman", "man", 0);
			mem.set("$ManOrWoman", "Man", 0);
		} else {
			mem.set("$hisOrHer", "her", 0);
			mem.set("$HisOrHer", "Her", 0);
			mem.set("$himOrHer", "her", 0);
			mem.set("$HimOrHer", "Her", 0);
			mem.set("$heOrShe", "she", 0);
			mem.set("$HeOrShe", "She", 0);
			mem.set("$himOrHerself", "herself", 0);
			mem.set("$HimOrHerself", "Herself", 0);
			mem.set("$manOrWoman", "woman", 0);
			mem.set("$ManOrWoman", "Woman", 0);
		}
		
		if (person.getRank() != null) {
			mem.set("$personRankAOrAn", person.getRankArticle(), 0);
			mem.set("$personRank", person.getRank().toLowerCase(), 0);
			mem.set("$PersonRank", Misc.ucFirst(person.getRank()), 0);
		}
		
		if (person.getPost() != null) {
			mem.set("$personPostAOrAn", person.getPostArticle().toLowerCase(), 0);
			mem.set("$personPost", person.getPost().toLowerCase(), 0);
			mem.set("$PersonPost", Misc.ucFirst(person.getPost()), 0);
		}
		
		mem.set("$PersonName", person.getName().getFullName(), 0);
		mem.set("$personName", person.getName().getFullName(), 0);
		mem.set("$personFirstName", person.getName().getFirst(), 0);
		mem.set("$personLastName", person.getName().getLast(), 0);
		
		if (person.getVoice() != null) {
			mem.set("$voice", person.getVoice());
		}
		if (person.getImportance() != null) {
			mem.set("$importance", person.getImportance().name());
		}
		
	}
	
	public void set(String key, Object value) {
		if (value instanceof Enum) value = ((Enum)value).name(); 
		interactionMemory.set(key, value, 0f);
	}
	
	public void unset(String key) {
		interactionMemory.unset(key);
	}
	
	
	public StageData getData(Object id) {
		if (id == null) return null;
		StageData data = stages.get(id);
		if (data == null) {
			data = new StageData();
			data.id = id;
			stages.put(id, data);
		}
		return data;
	}
	
	protected boolean shouldSendUpdateForStage(Object id) {
		return true;
	}
	
	public void checkStageChangesAndTriggers(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		if (isEnding()) return;
		boolean changed = false;
		do {
			changed = false;
			for (StageConnection conn : connections) {
				if (conn.from != currentStage && conn.from != null) continue;
				if (conn.to == currentStage) continue;
				
				if (conn.checker.conditionsMet()) {
					setCurrentStage(conn.to, dialog, memoryMap);
					changed = true;
					
					if (stageTransitionsRepeatable == null || !stageTransitionsRepeatable) {
						connections.remove(conn);
					}
					
					break;
				}
			}
			//runTriggers();
		} while (changed);
		
		runTriggers();
	}
	
	protected void runTriggers() {
		Iterator<MissionTrigger> iter = triggers.iterator();
		while (iter.hasNext()) {
			MissionTrigger trigger = iter.next();
			if (!trigger.getStages().contains(currentStage)) continue;
			if (trigger.getCondition().conditionsMet()) {
				TriggerActionContext context = new TriggerActionContext(this);
				for (TriggerAction curr : trigger.getActions()) {
					curr.doAction(context);
				}
				iter.remove();
			}
		}
	}
	
	public List<CampaignFleetAPI> runStageTriggersReturnFleets(Object stage) {
		List<CampaignFleetAPI> result = new ArrayList<CampaignFleetAPI>();
		Iterator<MissionTrigger> iter = triggers.iterator();
		while (iter.hasNext()) {
			MissionTrigger trigger = iter.next();
			if (!trigger.getStages().contains(stage)) continue;
			if (trigger.getCondition().conditionsMet()) {
				TriggerActionContext context = new TriggerActionContext(this);
				for (TriggerAction curr : trigger.getActions()) {
					curr.doAction(context);
				}
				iter.remove();
				result.addAll(context.allFleets);
			}
		}
		return result;
	}
	
	protected transient boolean doNotEndMission = false;
	public void setCurrentStage(Object next, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		if (currentStage == next) {
			return;
		}
		
		if (currentStage != null) {
			StageData data = getData(currentStage);
			for (ImportanceData curr : data.important) {
				Misc.makeUnimportant(curr.memory, getReason());
				removeImportanceChanges(curr.memory);
				
				if (curr.flag != null) {
					curr.memory.unset(curr.flag);
					removeMemoryFlagChanges(curr.memory, curr.flag);
				}
			}
		}
		
		currentStage = next;
		if (timeLimit != null && timeLimit.endLimitStages != null && 
				timeLimit.endLimitStages.contains(currentStage)) {
			timeLimit = null;
		}
		
		StageData data = getData(currentStage);
		data.elapsed = 0;
		for (ImportanceData curr : data.important) {
			//if (curr.market != null) curr.memory = curr.market.getMemoryWithoutUpdate();
			
			Misc.makeImportant(curr.memory, getReason());
			changes.add(new MadeImportant(curr.memory, getReason()));
			if (curr.flag != null) {
				curr.memory.set(curr.flag, true);
				changes.add(new VariableSet(curr.memory, curr.flag, true));
			}
		}
		
		for (FlagData fd : flags) {
			if (fd.stages.contains(currentStage)) {
				removeMemoryFlagChanges(fd.memory, fd.flag);
				fd.memory.set(fd.flag, true);
				changes.add(new VariableSet(fd.memory, fd.flag, true));
			} else {
				fd.memory.unset(fd.flag);
				removeMemoryFlagChanges(fd.memory, fd.flag);
			}
				
		}

		
		if (!doNotEndMission) {
			if (successStages.contains(currentStage)) {
				endSuccess(dialog, memoryMap);
			} else if (failStages.contains(currentStage)) {
				endFailure(dialog, memoryMap);
			} else if (currentStage != null && currentStage == abandonStage) {
				endAbandon();
			} else if (shouldSendUpdateForStage(currentStage)) {
				sendUpdateForNextStep(NEXT_STEP_UPDATE, dialog == null ? null : dialog.getTextPanel());
			}
		}
		
		runTriggers();
	}
	
	protected void endSuccess(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		setImportant(false);
		
		result = new HubMissionResult();
		result.success = true;
		
		TextPanelAPI textPanel = null;
		if (dialog != null) textPanel = dialog.getTextPanel();
		
		int xp = getXPReward();
		if (xp > 0) { 
			Global.getSector().getPlayerStats().addXP(xp, textPanel);
		}
		
		int reward = getCreditsReward();
		if (reward > 0) {
			Global.getSector().getPlayerFleet().getCargo().getCredits().add(reward);
		}
		
		if (textPanel != null && reward > 0) {
			AddRemoveCommodity.addCreditsGainText(reward, dialog.getTextPanel());
		}
		
		
		result.reward = reward;
		result.xp = xp;
		
		adjustRep(textPanel, result, RepActions.MISSION_SUCCESS);

		
		endSuccessImpl(dialog, memoryMap);
		
		if (cargoOnSuccess != null) {
			cargoOnSuccess.sort();
			for (CargoStackAPI stack : cargoOnSuccess.getStacksCopy()) {
				Global.getSector().getPlayerFleet().getCargo().addItems(stack.getType(), stack.getData(), stack.getSize());
				if (textPanel != null) { 
					AddRemoveCommodity.addStackGainText(stack, textPanel);
				}
			}
			cargoOnSuccess.clear();
		}
		
		endAfterDelay();
		
//		if (textPanel == null) { // if in dialog, already printed stuff to the text panel
//			sendUpdateIfPlayerHasIntel(result, false);
//		}
		sendUpdateForNextStep(END_MISSION_UPDATE, dialog == null ? null : dialog.getTextPanel());
		
		if (creator != null) creator.incrCompleted();
		
		
		if (potentialContactsOnMissionSuccess != null) {
			if (doNotAutoAddPotentialContactsOnSuccess == null || !doNotAutoAddPotentialContactsOnSuccess) {
				for (PotentialContactData curr : potentialContactsOnMissionSuccess) {
					PersonAPI p = curr.contact;
					MarketAPI m = curr.contact.getMarket();
					if (m == null) m = getPerson().getMarket();
					if (curr.probability < 0) {
						ContactIntel.addPotentialContact(p, m, dialog != null ? dialog.getTextPanel() : null);
					} else {
						ContactIntel.addPotentialContact(curr.probability, p, m, dialog != null ? dialog.getTextPanel() : null);
					}
				}
				potentialContactsOnMissionSuccess = null;
				doNotAutoAddPotentialContactsOnSuccess = null;
			}
		}
		
		abort();
		
		if (completedKey != null) {
			Global.getSector().getMemoryWithoutUpdate().set(completedKey, true);
		}
	}
	
	protected void endFailure(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		setImportant(false);
		
		result = new HubMissionResult();
		result.success = false;
		
		TextPanelAPI textPanel = null;
		if (dialog != null) textPanel = dialog.getTextPanel();
		
		if (!noPenaltyFailStages.contains(currentStage)) {
			adjustRep(textPanel, result, RepActions.MISSION_FAILURE);
		}
		
		endFailureImpl(dialog, memoryMap);
		endAfterDelay();
		
//		if (textPanel == null) {
//			sendUpdateIfPlayerHasIntel(result, false);
//		}
		sendUpdateForNextStep(END_MISSION_UPDATE, dialog == null ? null : dialog.getTextPanel());
		
		if (creator != null) creator.incrFailed();
		abort();
	}
	
	protected void endAbandon() {
		result = new HubMissionResult();
		result.success = false;
		
		if (!canAbandonWithoutPenalty()) {
			adjustRep(null, result, RepActions.MISSION_FAILURE);
			if (creator != null) creator.incrFailed();
		}
		
		endAbandonImpl();

		endAfterDelay();
		abort();
	}
	
	
	
	protected void endSuccessImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
	}
	protected void endFailureImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) { 
	}
	protected void endAbandonImpl() { 
	}
	
	
	protected Boolean adjustedRep;
	protected void adjustRep(TextPanelAPI textPanel, HubMissionResult result, RepActions action) {
		if (adjustedRep != null) return;
		adjustedRep = true;
		
		MissionCompletionRep completionRepPerson = new MissionCompletionRep(
				getRepRewardSuccessPerson(), getRewardLimitPerson(),
				-getRepPenaltyFailurePerson(), getPenaltyLimitPerson());
		MissionCompletionRep completionRepFaction = new MissionCompletionRep(
				getRepRewardSuccessFaction(), getRewardLimitFaction(),
				-getRepPenaltyFailureFaction(), getPenaltyLimitFaction());

		boolean withMessage = textPanel != null;
		
		boolean adjustPersonRep = (action == RepActions.MISSION_SUCCESS && completionRepPerson.successDelta != 0) ||
				(action == RepActions.MISSION_FAILURE && completionRepPerson.failureDelta != 0);
		if (adjustPersonRep && getPerson() != null) {
			ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(action, completionRepPerson,
							textPanel, true, withMessage), 
							getPerson());
			if (result != null) result.repPerson = rep;
			completionRepPerson.successDelta = 0;
		}

		boolean adjustFactionRep = (action == RepActions.MISSION_SUCCESS && completionRepFaction.successDelta != 0) ||
				(action == RepActions.MISSION_FAILURE && completionRepFaction.failureDelta != 0);
		if (adjustFactionRep && getPerson() != null) {
			ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(action, completionRepFaction,
							textPanel, true, withMessage), 
							getPerson().getFaction().getId());
			if (result != null) result.repFaction = rep;
			completionRepFaction.successDelta = 0f;
		}
	}
	
	
	public void setSuccessStage(Object id) {
		addSuccessStages(id);
	}
	public void addSuccessStages(Object ... ids) {
		for (Object id : ids) {
			successStages.add(id);
		}
	}
	
	public void setFailureStage(Object id) {
		addFailureStages(id);
	}
	public void addFailureStages(Object ... ids) {
		for (Object id : ids) {
			failStages.add(id);
		}
	}
	
	public void setNoPenaltyFailureStage(Object id) {
		addNoPenaltyFailureStages(id);
	}
	public void addNoPenaltyFailureStages(Object ... ids) {
		addFailureStages(ids);
		for (Object id : ids) {
			noPenaltyFailStages.add(id);
		}
	}
	
	protected void removeImportanceChanges(MemoryAPI memory) {
		Iterator<Abortable> iter = changes.iterator();
		while (iter.hasNext()) {
			Abortable curr = iter.next();
			if (curr instanceof MadeImportant) {
				MadeImportant mi = (MadeImportant) curr;
				if (mi.memory == memory) {
					iter.remove();
				}
			}
		}
	}
	
	protected void removeMemoryFlagChanges(MemoryAPI memory, String flag) {
		Iterator<Abortable> iter = changes.iterator();
		while (iter.hasNext()) {
			Abortable curr = iter.next();
			if (curr instanceof VariableSet) {
				VariableSet vs = (VariableSet) curr;
				if (vs.memory == memory && flag.equals(vs.key)) {
					iter.remove();
				}
			}
		}
	}
	
	public String getReason() {
		return getMissionId(); 
	}

	public int getCreditsReward() {
		if (creditReward != null) return creditReward;
		return 0;
	}
	
	public int getXPReward() {
		if (xpReward != null) return xpReward;
		return 0;
	}
	
	public float getRepRewardSuccessPerson() {
		if (repRewardPerson != null) return repRewardPerson;
		return RepRewards.HIGH;
	}
	
	public float getRepPenaltyFailurePerson() {
		if (repPenaltyPerson != null) return repPenaltyPerson;
		return RepRewards.SMALL;
	}
	
	public float getRepRewardSuccessFaction() {
		if (repRewardFaction != null) return repRewardFaction;
		return RepRewards.MEDIUM;
	}
	
	public float getRepPenaltyFailureFaction() {
		if (repPenaltyFaction != null) return repPenaltyFaction;
		return RepRewards.TINY;
	}
	
	public RepLevel getRewardLimitPerson() {
		return rewardLimitPerson != null ? rewardLimitPerson : RepLevel.COOPERATIVE;
	}

	public RepLevel getRewardLimitFaction() {
		return rewardLimitFaction != null ? rewardLimitFaction : RepLevel.COOPERATIVE;
	}

	public RepLevel getPenaltyLimitPerson() {
		return penaltyLimitPerson != null ? penaltyLimitPerson : RepLevel.VENGEFUL;
	}

	public RepLevel getPenaltyLimitFaction() {
		return penaltyLimitFaction != null ? penaltyLimitFaction : RepLevel.VENGEFUL;
	}

	public MissionHub getHub() {
		return hub;
	}
	
	public void setHub(MissionHub hub) {
		this.hub = hub;
	}
	
	public PersonAPI getPerson() {
		if (personOverride != null) return personOverride;
		if (hub == null) return null;
		return hub.getPerson();
	}

	public HubMissionCreator getCreator() {
		return creator;
	}

	public void setCreator(HubMissionCreator creator) {
		this.creator = creator;
	}

	public void setStartingStage(Object startingStage) {
		this.startingStage = startingStage;
	}
	
	public void setPersonDoGenericPortAuthorityCheck(PersonAPI person) {
		if (person.getMemoryWithoutUpdate().getBoolean("$doGenericPortAuthorityCheck")) return;
		setFlag(person, "$doGenericPortAuthorityCheck", false);
	}

//	public void setMarketExtraSmugglingSuspicionLevel(MarketAPI market, float extra) {
//		setFlag(market.getMemoryWithoutUpdate(), MemFlags.MARKET_EXTRA_SUSPICION, extra, false);
//	}
	
	public void setFlagWithReason(SectorEntityToken entity, String flag, boolean permanent) {
		setFlag(entity.getMemoryWithoutUpdate(), flag, null, permanent, (Object[])null);
		
		String reason = getReason();
		Misc.setFlagWithReason(entity.getMemoryWithoutUpdate(), flag, reason, true, -1f);
		
		if (!permanent) {
			String requiredKey = flag + "_" + reason;
			changes.add(new VariableSet(entity.getMemoryWithoutUpdate(), requiredKey, true));
		}
	}
	
	public void setFlag(SectorEntityToken entity, String flag, boolean permanent) {
		setFlag(entity.getMemoryWithoutUpdate(), flag, null, permanent, (Object[])null);
	}
	public void setFlag(PersonAPI person, String flag, boolean permanent) {
		setFlag(person.getMemoryWithoutUpdate(), flag, null, permanent, (Object[])null);
	}
	
	public void setFlag(SectorEntityToken entity, String flag, boolean permanent, Object ... stages) {
		setFlag(entity.getMemoryWithoutUpdate(), flag, null, permanent, stages);
	}
	public void setFlag(PersonAPI person, String flag, boolean permanent, Object ... stages) {
		setFlag(person.getMemoryWithoutUpdate(), flag, null, permanent, stages);
	}
	
	public void setGlobalFlag(String flag, Object value, Object ... stages) {
		setFlag(getGlobalMemory(), flag, value, false, stages);
	}
	
	public void setFlag(MemoryAPI memory, String flag, Object value, boolean permanent) {
		setFlag(memory, flag, value, permanent, (Object []) null);
	}
	public void setFlag(MemoryAPI memory, String flag, Object value, boolean permanent, Object ... stages) {
		if (stages != null && stages.length > 0) {
			FlagData fd = new FlagData();
			fd.memory = memory;
			fd.flag = flag;
			fd.stages.addAll(Arrays.asList(stages));
			flags.add(fd);
			if (fd.stages.contains(currentStage)) {
				removeMemoryFlagChanges(fd.memory, fd.flag);
				fd.memory.set(fd.flag, true);
				changes.add(new VariableSet(fd.memory, fd.flag, true));
			}
		} else {
			if (value == null) {
				memory.set(flag, true);
			} else {
				memory.set(flag, value);
			}
			changes.add(new VariableSet(memory, flag, !permanent));
		}
	}
	
	public boolean setGlobalReference(String key) {
		if (getGlobalMemory().contains(key)) {
			return false;
		}
		getGlobalMemory().set(key, this);
		changes.add(new VariableSet(getGlobalMemory(), key, true));
		return true;
	}
	
	public boolean setGlobalReference(String refKey, String inProgressFlag) {
		if (getGlobalMemory().contains(refKey)) {
			return false;
		}
		getGlobalMemory().set(refKey, this);
		changes.add(new VariableSet(getGlobalMemory(), refKey, true));
		
		if (inProgressFlag != null) {
			getGlobalMemory().set(inProgressFlag, true);
			changes.add(new VariableSet(getGlobalMemory(), inProgressFlag, true));
		}
		return true;
	}
	
	public boolean setPersonMissionRef(PersonAPI person, String key) {
		if (person == null) return false;
		if (person.getMemoryWithoutUpdate().contains(key)) {
			return false;
		}
		person.getMemoryWithoutUpdate().set(key, this);
		changes.add(new VariableSet(person.getMemoryWithoutUpdate(), key, true));
		return true;
	}
	
	public boolean setFactionMissionRef(FactionAPI faction, String key) {
		if (faction == null) return false;
		if (faction.getMemoryWithoutUpdate().contains(key)) {
			return false;
		}
		faction.getMemoryWithoutUpdate().set(key, this);
		changes.add(new VariableSet(faction.getMemoryWithoutUpdate(), key, true));
		return true;
	}
	
	public boolean setMarketMissionRef(MarketAPI market, String key) {
		if (market == null) return false;
		if (market.getMemoryWithoutUpdate().contains(key)) {
			return false;
		}
		market.getMemoryWithoutUpdate().set(key, this);
		changes.add(new VariableSet(market.getMemoryWithoutUpdate(), key, true));
		return true;
	}
	
	public boolean setEntityMissionRef(SectorEntityToken entity, String key) {
		if (entity == null) return false;
		if (entity.getMemoryWithoutUpdate().contains(key)) {
			return false;
		}
		entity.getMemoryWithoutUpdate().set(key, this);
		changes.add(new VariableSet(entity.getMemoryWithoutUpdate(), key, true));
		return true;
	}
	
	public MemoryAPI getGlobalMemory() {
		return Global.getSector().getMemoryWithoutUpdate();
	}
	
	public void makeImportantDoNotShowAsIntelMapLocation(PersonAPI person, String flag, Enum ... stages) {
		makeImportant(person.getMemoryWithoutUpdate(), flag, null, person, stages);
	}
	public void makeImportantDoNotShowAsIntelMapLocation(SectorEntityToken entity, String flag, Enum ... stages) {
		makeImportant(entity.getMemoryWithoutUpdate(), flag, null, entity, stages);
	}
	public void makeImportantDoNotShowAsIntelMapLocation(MarketAPI market, String flag, Enum ... stages) {
		makeImportant(market.getMemoryWithoutUpdate(), flag, null, market, stages);
	}
	public void makeImportant(PersonAPI person, String flag, Enum ... stages) {
		makeImportant(person.getMemoryWithoutUpdate(), flag, MapLocationType.NORMAL, person, stages);
	}
	public void makeImportant(SectorEntityToken entity, String flag, Enum ... stages) {
		makeImportant(entity.getMemoryWithoutUpdate(), flag, MapLocationType.NORMAL, entity, stages);
	}
	public void makeImportant(MarketAPI market, String flag, Enum ... stages) {
		makeImportant(market.getMemoryWithoutUpdate(), flag, MapLocationType.NORMAL, market, stages);
	}
	public void makeImportant(MemoryAPI memory, String flag, MapLocationType type, Object personOrEntityOrMarket, Enum ... stages) {
		boolean inCurrentStage = false;
		if (stages != null) {
			for (Object id : stages) {
				if (currentStage != null && id == currentStage) inCurrentStage = true;
				ImportanceData data = new ImportanceData();
				data.memory = memory;
				data.flag = flag;
				data.locType = type;
				if (personOrEntityOrMarket instanceof PersonAPI) {
					data.person = (PersonAPI) personOrEntityOrMarket;
				} else if (personOrEntityOrMarket instanceof SectorEntityToken) {
					data.entity = (SectorEntityToken) personOrEntityOrMarket;
				} else if (personOrEntityOrMarket instanceof MarketAPI) {
					data.market = (MarketAPI) personOrEntityOrMarket;
				}
				getData(id).important.add(data);
			}
		} else {
			inCurrentStage = true;
		}
		
		if (inCurrentStage) {
			Misc.makeImportant(memory, getReason());
			if (stages != null) {
				changes.add(new MadeImportant(memory, getReason()));
			}
			if (flag != null) {
				memory.set(flag, true);
				if (stages != null) {
					changes.add(new VariableSet(memory, flag, true));
				}
			}
		}
	}
	
	public void makeUnimportant(PersonAPI person, Enum ... stages) {
		if (person == null) return;
		makeUnimportant(person.getMemoryWithoutUpdate(), person, stages);
	}
	public void makeUnimportant(SectorEntityToken entity, Enum ... stages) {
		if (entity == null) return;
		makeUnimportant(entity.getMemoryWithoutUpdate(), entity, stages);
	}
	public void makeUnimportant(MarketAPI market, Enum ... stages) {
		if (market == null) return;
		makeUnimportant(market.getMemoryWithoutUpdate(), market, stages);
	}
	public void makeUnimportant(PersonAPI person) {
		if (person == null) return;
		makeUnimportant(person.getMemoryWithoutUpdate(), person, (Enum []) null);
	}
	public void makeUnimportant(SectorEntityToken entity) {
		if (entity == null) return;
		makeUnimportant(entity.getMemoryWithoutUpdate(), entity, (Enum []) null);
	}
	public void makeUnimportant(MarketAPI market) {
		if (market == null) return;
		makeUnimportant(market.getMemoryWithoutUpdate(), market, (Enum []) null);
	}
	public void makeUnimportant(MemoryAPI memory, Object personOrEntityOrMarket) {
		makeUnimportant(memory, personOrEntityOrMarket, (Enum []) null);
	}
	public void makeUnimportant(MemoryAPI memory, Object personOrEntityOrMarket, Enum ... stages) {
		List<StageData> list = new ArrayList<BaseHubMission.StageData>();
		if (stages != null) {
			for (Object id : stages) {
				StageData stageData = getData(id);
				list.add(stageData);
			}
		} else {
			list.addAll(this.stages.values());
		}
		
		for (StageData stageData : list) {
			Iterator<ImportanceData> iter = stageData.important.iterator();
			while (iter.hasNext()) {
				ImportanceData data = iter.next();
				if (data.memory == memory || 
						data.person == personOrEntityOrMarket ||
						data.entity == personOrEntityOrMarket ||
						data.market == personOrEntityOrMarket) {
					iter.remove();
				}
			}
		}
		Misc.makeUnimportant(memory, getReason());
	}
	

// too easy to mix up with the other method
//	public void setTimeLimit(Object failStage, float days, Object ... noLimitAfterStages) {
//		setTimeLimit(failStage, days, null, noLimitAfterStages);
//	}
	public void setTimeLimit(Object failStage, float days, StarSystemAPI noLimitWhileInSystem, Object ... noLimitAfterStages) {
		timeLimit = new TimeLimitData();
		timeLimit.days = days;
		timeLimit.failStage = failStage;
		timeLimit.noLimitWhileInSystem = noLimitWhileInSystem;
		if (noLimitAfterStages != null) {
			for (Object stage : noLimitAfterStages) {
				timeLimit.endLimitStages.add(stage);
			}
		}
	}
	
	public HubMissionResult getResult() {
		return result;
	}


	public int genRoundNumber(int min, int max) {
		int result = min + genRandom.nextInt(max - min + 1);
		return getRoundNumber(result);
	}
		
	public static int getRoundNumber(float num) {
		int num2 = (int) num;
		for (int i = 1; i < 10; i++) {
			int threshold = (int) Math.pow(10, i);
			int base = threshold / 10;
			if (num2 > threshold) {
				num2 = num2 / base * base;
			}
		}
		return (int) num2;
	}
	public void setCreditReward(int min, int max) {
		setCreditReward(min, max, true);
	}
	public void setCreditReward(int min, int max, boolean withMult) {
		int reward = min + genRandom.nextInt(max - min + 1);
		if (withMult) {
			reward = getRoundNumber(reward * rewardMult);
		}
		reward = reward / 1000 * 1000;
		if (reward > 100000) {
			reward = reward / 10000 * 10000;
		}
		setCreditReward(reward);
	}
	
	public void setCreditReward(Integer creditReward) {
		this.creditReward = creditReward;
	}
	
	public void setCreditRewardApplyRelMult(Integer creditReward) {
		creditReward = getRoundNumber(creditReward * rewardMult);
		this.creditReward = creditReward;
	}
	
	public void setCreditReward(CreditReward reward) {
		setCreditReward(reward.min, reward.max);
	}
	public void setCreditReward(CreditReward reward, int marketSize) {
		setCreditReward(reward.min / 2 + reward.perMarketSize * Math.max(0, marketSize - 3), 
					    reward.max / 2 + reward.perMarketSize * Math.max(0, marketSize - 3));
	}
	
	public void setCreditRewardWithBonus(CreditReward reward, int bonus) {
		setCreditReward(reward.min + bonus, reward.max + bonus);
	}
	
	public int getRewardBonusForMarines(int marines) {
		return marines * EXTRA_REWARD_PER_MARINE;
	}
	
//	public void setXPReward(XPReward reward) {
//		float f = reward.getFractionOfLevel();
//		LevelupPlugin plugin = Global.getSettings().getLevelupPlugin();
//		
//		int level = Global.getSector().getPlayerStats().getLevel();
//		if (level < MIN_LEVEL_TO_SCALE_XP_GAIN_AT) level = MIN_LEVEL_TO_SCALE_XP_GAIN_AT;
//		if (level > MAX_LEVEL_TO_SCALE_XP_GAIN_AT) level = MAX_LEVEL_TO_SCALE_XP_GAIN_AT;
//		
//		int xp = (int) plugin.getXPForNextLevel(level);
//		xp *= f;
//		
//		setXPReward(xp);
//	}
	
	public void setXPReward(int xpReward) {
		this.xpReward = xpReward;
		if (this.xpReward <= 0) this.xpReward = null;
	}
	

	public void setRepPersonChangesNone() {
		setRepRewardPerson(0f);
		setRepPenaltyPerson(0f);
	}
	
	public void setRepFactionChangesNone() {
		setRepRewardFaction(0f);
		setRepPenaltyFaction(0f);
	}
	
	public void setRepPersonChangesTiny() {
		setRepRewardPerson(RepRewards.TINY);
		setRepPenaltyPerson(0f);
	}
	public void setRepFactionChangesTiny() {
		setRepRewardFaction(RepRewards.TINY);
		setRepPenaltyFaction(0f);
	}
	public void setRepPersonChangesVeryLow() {
		setRepRewardPerson(RepRewards.SMALL);
		setRepPenaltyPerson(RepRewards.TINY);
	}
	public void setRepFactionChangesVeryLow() {
		setRepRewardFaction(RepRewards.SMALL);
		setRepPenaltyFaction(RepRewards.TINY);
	}
	public void setRepPersonChangesLow() {
		setRepRewardPerson(RepRewards.MEDIUM);
		setRepPenaltyPerson(RepRewards.TINY);
	}
	public void setRepFactionChangesLow() {
		setRepRewardFaction(RepRewards.MEDIUM);
		setRepPenaltyFaction(RepRewards.TINY);
	}
	public void setRepPersonChangesMedium() {
		setRepRewardPerson(RepRewards.HIGH);
		setRepPenaltyPerson(RepRewards.SMALL);
	}
	public void setRepFactionChangesMedium() {
		setRepRewardFaction(RepRewards.HIGH);
		setRepPenaltyFaction(RepRewards.SMALL);
	}
	public void setRepPersonChangesHigh() {
		setRepRewardPerson(RepRewards.VERY_HIGH);
		setRepPenaltyPerson(RepRewards.MEDIUM);
	}
	public void setRepFactionChangesHigh() {
		setRepRewardFaction(RepRewards.VERY_HIGH);
		setRepPenaltyFaction(RepRewards.MEDIUM);
	}
	public void setRepPersonChangesVeryHigh() {
		setRepRewardPerson(RepRewards.EXTREME);
		setRepPenaltyPerson(RepRewards.MEDIUM);
	}
	public void setRepFactionChangesVeryHigh() {
		setRepRewardFaction(RepRewards.EXTREME);
		setRepPenaltyFaction(RepRewards.MEDIUM);
	}
	
	public void setRepChanges(float repRewardPerson, float repPenaltyPerson, 
							  float repRewardFaction, float repPenaltyFaction) {
		setRepRewardPerson(repRewardPerson);
		setRepPenaltyPerson(repPenaltyPerson);
		setRepRewardFaction(repRewardFaction);
		setRepPenaltyFaction(repPenaltyFaction);
	}
	public void setNoRepChanges() {
		setRepChanges(0, 0, 0, 0);
	}
	
	public void setRepRewardPerson(Float repRewardPerson) {
		this.repRewardPerson = repRewardPerson;
	}

	public void setRepPenaltyPerson(Float repPenaltyPerson) {
		this.repPenaltyPerson = repPenaltyPerson;
	}

	public void setRepRewardFaction(Float repRewardFaction) {
		this.repRewardFaction = repRewardFaction;
	}

	public void setRepPenaltyFaction(Float repPenaltyFaction) {
		this.repPenaltyFaction = repPenaltyFaction;
	}

	public void setPenaltyLimitPerson(RepLevel penaltyLimitPerson) {
		this.penaltyLimitPerson = penaltyLimitPerson;
	}

	public void setPenaltyLimitFaction(RepLevel penaltyLimitFaction) {
		this.penaltyLimitFaction = penaltyLimitFaction;
	}
	
	public static boolean playerLevelIsAtLeast(int level) {
		return Global.getSector().getPlayerStats().getLevel() >= level;
	}
	
	public static boolean playerLevelIsMaxed() {
		int max = Global.getSettings().getLevelupPlugin().getMaxLevel();
		return Global.getSector().getPlayerStats().getLevel() >= max;
	}
	
	public static int getMaxPlayerLevel() {
		return Global.getSettings().getLevelupPlugin().getMaxLevel();
	}
	
	public static boolean isDevMode() {
		return Global.getSettings().isDevMode();
	}
	
	protected Set<String> addedTags = null;
	public void addTag(String tag) {
		if (addedTags == null) addedTags = new HashSet<String>();
		addedTags.add(tag);
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_MISSIONS);
		tags.add(Tags.INTEL_ACCEPTED);
		tags.add(getFactionForUIColors().getId());
		if (addedTags != null) {
			tags.addAll(addedTags);
		}
		return tags;
	}
	
	public SectorEntityToken getMapLocationFor(SectorEntityToken entity) {
		if ((entity.isDiscoverable() || 
				(entity instanceof CampaignFleetAPI && !((CampaignFleetAPI)entity).isVisibleToPlayerFleet())) && entity.getStarSystem() != null) {
			return entity.getStarSystem().getCenter();
		}
		return entity;
	}
	
	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		if (currentStage == null) return null;

		return getMapLocation(map, currentStage);
	}
	
	public void makePrimaryObjective(Object personOrMarketOrEntity) {
		StageData stage = getData(currentStage);
		ImportanceData data = null;
		for (ImportanceData curr : stage.important) {
			if (curr.locType == null) continue;
			if (curr.entity == personOrMarketOrEntity || 
					curr.market == personOrMarketOrEntity ||
					curr.person == personOrMarketOrEntity) {
				data = curr;
				break;
			}
		}
		if (data != null) {
			stage.important.remove(data);
			stage.important.add(0, data);
		}
	}
	
	public SectorEntityToken getMapLocation(SectorMapAPI map, Object currentStage) {
		if (currentStage == null) {
			currentStage = startingStage;
		}
		StageData stage = getData(currentStage);
		ImportanceData data = null;
		for (ImportanceData curr : stage.important) {
			if (curr.locType == null) continue;
			if (curr.entity != null && !curr.entity.isAlive()) continue;
			data = curr;
			break;
		}
		if (data == null || data.locType == null) return null;
		
		SectorEntityToken entity = data.entity;
		if (entity == null && data.person != null && data.person.getMarket() != null) {
			entity = data.person.getMarket().getPrimaryEntity();
		}
		if (entity == null && data.market!= null) {
			entity = data.market.getPrimaryEntity();
		}
		if (entity == null) return null;
		
		if (data.locType == MapLocationType.NORMAL) {
			if ((entity.isDiscoverable() || 
					(entity instanceof CampaignFleetAPI && !((CampaignFleetAPI)entity).isVisibleToPlayerFleet())) && entity.getStarSystem() != null) {
				return entity.getStarSystem().getCenter();
			}
			return entity;
		} else if (data.locType == MapLocationType.CONSTELLATION) {
			Constellation c = entity.getConstellation();
			SectorEntityToken result = null;
			if (c != null && map != null) {
				result = map.getConstellationLabelEntity(c);
			}
			if (result == null) result = entity;
			return result;
		}
		
		return entity;
	}

	public String getSortString() {
		return getBaseName();
	}
	
	public void setIconName(String iconName) {
		this.iconName = iconName;
	}
	public void setIconName(String category, String id) {
		this.iconName = Global.getSettings().getSpriteName(category, id);
	}
	
	public String getPostfixForState() {
		if (isEnding()) {
			if (isSucceeded()) {
				return " - Completed";	
			} else if (isFailed()) {
				return " - Failed";
			} else if (isAbandoned()) {
				return " - Abandoned";
			}
			return " - Ended";
		}
		if (startingStage != null) {
			return " - Accepted";
		}
		return "";
	}
	
	public String getName() {
		return getBaseName() + (getPostfixForState() == null ? "" : getPostfixForState());
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		if (getPerson() == null) return Global.getSector().getPlayerFaction();
		return getPerson().getFaction();
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}
	
	protected boolean isSucceeded() {
		return successStages.contains(currentStage);
	}
	protected boolean isFailed() {
		return failStages.contains(currentStage);
	}
	protected boolean isAbandoned() {
		return abandonStage == currentStage;
	}
	
	public String getIcon() {
		if (iconName != null) return iconName;
		return getPerson().getPortraitSprite();
	}
	
	@Override
	public String getImportantIcon() {
		if (!isEnding() && !isEnded()) {
			return Global.getSettings().getSpriteName("intel", "important_accepted_mission");
		}
		return super.getImportantIcon();
	}
	
	protected void addResultBulletsAssumingAlreadyIndented(TooltipMakerAPI info, ListInfoMode mode) {
		if (result == null) return;
		if (mode == ListInfoMode.INTEL) return; // don't show result stuff when it's in the intel list or in a textPanel
		
		Color h = Misc.getHighlightColor();
		Color tc = getBulletColorForMode(mode);
		PersonAPI person = getPerson();
		FactionAPI faction = getFactionForUIColors();
		boolean isUpdate = getListInfoParam() != null;
		float initPad = 3f;
		if (mode == ListInfoMode.IN_DESC) initPad = 10f;
		
		if (result.reward > 0) {
			info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(result.reward));
			initPad = 0f;
		}
		
		if (result.repPerson != null) {
			CoreReputationPlugin.addAdjustmentMessage(result.repPerson.delta, null, person, 
												  null, info, tc, isUpdate, initPad);
			initPad = 0f;
		}
		if (result.repFaction != null) {
			CoreReputationPlugin.addAdjustmentMessage(result.repFaction.delta, faction, null, 
												  null, info, tc, isUpdate, initPad);
			initPad = 0f;
		}
	}
	
	public static String NEXT_STEP_UPDATE = "next_step_update";
	public static String END_MISSION_UPDATE = "end_mission_update";
	public void sendUpdateForNextStep(String listInfoParam, TextPanelAPI textPanel) {
		if (textPanel == null) {
			sendUpdateIfPlayerHasIntel(listInfoParam, false);
		} else {
			this.listInfoParam = listInfoParam;
			Global.getSector().getIntelManager().addIntelToTextPanel(this, textPanel);
			this.listInfoParam = null;
		}
	}
	
	public void sendUpdateToTextPanel(String listInfoParam, TextPanelAPI textPanel) {
		this.listInfoParam = listInfoParam;
		Global.getSector().getIntelManager().addIntelToTextPanel(this, textPanel);
		this.listInfoParam = null;
	}
	
	protected void addBulletPointsPre(TooltipMakerAPI info, Color tc, float initPad, ListInfoMode mode) {
		
	}
	protected void addBulletPointsPost(TooltipMakerAPI info, Color tc, float initPad, ListInfoMode mode) {
		
	}
	
	protected String getToCompleteText() {
		return "to complete";
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		
		addBulletPointsPre(info, tc, initPad, mode);
		
		boolean isUpdate = getListInfoParam() != null;
		
		PersonAPI person = getPerson();
		FactionAPI faction = getFactionForUIColors();
		
		if (isUpdate) {
			// Possible updates: failed, completed, next step
			if (getListInfoParam() == NEXT_STEP_UPDATE) {
				if (addNextStepText(info, tc, initPad)) {
					initPad = 0f;
				}
			} else if (isFailed()) {
				addResultBulletsAssumingAlreadyIndented(info, mode);
				addNextStepText(info, tc, initPad);
			} else if (isSucceeded()) {
				addResultBulletsAssumingAlreadyIndented(info, mode);
				addNextStepText(info, tc, initPad);
			} else {
				addNextStepText(info, tc, initPad);
			}
		} else {
			// either in small description, or in tooltip/intel list
			if (result != null) {
				if (mode == ListInfoMode.IN_DESC) {
					addResultBulletsAssumingAlreadyIndented(info, mode);
				}
			} else {
				if (mode == ListInfoMode.IN_DESC) {
//					if (addNextStepText(info, tc, initPad)) {
//						initPad = 0f;
//					}
					
					int reward = getCreditsReward();
					if (reward > 0) {
						info.addPara("%s reward", initPad, tc, h, Misc.getDGSCredits(reward));
						initPad = 0f;
					}
					if (timeLimit != null) {
						addDays(info, getToCompleteText(), timeLimit.days - elapsed, tc, initPad);
						initPad = 0f;
					}	
				} else {
					if (addNextStepText(info, tc, initPad)) {
						initPad = 0f;
					}
//					int reward = getCreditsReward();
//					if (reward > 0) {
//						info.addPara("%s reward", initPad, tc, h, Misc.getDGSCredits(reward));
//						initPad = 0f;
//					}
					if (timeLimit != null) {
						addDays(info, getToCompleteText(), timeLimit.days - elapsed, tc, initPad);
						initPad = 0f;
					}
				}
			}
		}
		
		addBulletPointsPost(info, tc, initPad, mode);
		
		unindent(info);
		
	}
	
	@Override
	public IntelSortTier getSortTier() {
		if (sortTier == null || isEnding() || isEnded()) return super.getSortTier();
		return sortTier;
	}
	
	protected Boolean largeTitleFont;
	public void setUseLargeFontInMissionList() {
		largeTitleFont = true;
		sortTier = IntelSortTier.TIER_2;
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		boolean large = largeTitleFont != null && largeTitleFont;
		if (large) info.setParaSmallInsignia();
		info.addPara(getName(), c, 0f);
		if (large) info.setParaFontDefault();
		
		addBulletPoints(info, mode);
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		FactionAPI faction = getFactionForUIColors();
		PersonAPI person = getPerson();
		
		if (person != null) {
			info.addImages(width, 128, opad, opad, person.getPortraitSprite(), faction.getCrest());
			
			String post = "one";
			if (person.getPost() != null) post = person.getPost().toLowerCase(); 
			if (post == null && person.getRank() != null) post = person.getRank().toLowerCase(); 
			info.addPara(Misc.ucFirst(getMissionTypeNoun()) + " given by " + post + " " + person.getNameString() + ", affiliated with " + 
					faction.getDisplayNameWithArticle() + ".",
					opad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
		}
//		info.addPara("Given by: " + person.getNameString() + ", " + 
//				faction.getDisplayName() + "-affiliated.",
//				opad, faction.getBaseUIColor(), faction.getDisplayName());
		
		addDescriptionForCurrentStage(info, width, height);
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
		
		if (abandonStage != null && !isAbandoned() && !isSucceeded() && !isFailed()) {
			addAbandonButton(info, width);
		}
	}
	
	

	
	
	
	public void setAbandonStage(Object abandonStage) {
		this.abandonStage = abandonStage;
	}
	public void setNoAbandon() {
		this.abandonStage = null;
	}

	@Override
	public boolean doesButtonHaveConfirmDialog(Object buttonId) {
		if (buttonId == BUTTON_ABANDON) {
			return true;
		}
		return super.doesButtonHaveConfirmDialog(buttonId);
	}
	
	protected void addAbandonButton(TooltipMakerAPI info, float width) {
		addAbandonButton(info, width, "Abandon");
	}
	protected void addAbandonButton(TooltipMakerAPI info, float width, String abandon) {
		float opad = 10f;
		ButtonAPI button = info.addButton(abandon, BUTTON_ABANDON, 
				getFactionForUIColors().getBaseUIColor(), getFactionForUIColors().getDarkUIColor(),
				(int)(width), 20f, opad * 2f);
		button.setShortcut(Keyboard.KEY_U, true);
	}
	
	public boolean canAbandonWithoutPenalty() {
		return elapsed < getNoPenaltyAbandonDays();
	}
	
	protected float getNoPenaltyAbandonDays() {
		return 1f;
	}
	
	@Override
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_ABANDON) {
			setImportant(false);
			
			setCurrentStage(abandonStage, null, null);
			//endImmediately();
			runTriggers();
		}
		super.buttonPressConfirmed(buttonId, ui);
	}


	@Override
	public void createConfirmationPrompt(Object buttonId, TooltipMakerAPI prompt) {
		FactionAPI faction = getFactionForUIColors();
		
		if (buttonId == BUTTON_ABANDON) {
			boolean loseRepFaction = getRepPenaltyFailureFaction() > 0; 
			boolean loseRepPerson = getRepPenaltyFailurePerson() > 0;
			if (!loseRepFaction && !loseRepPerson) {
				prompt.addPara("You can abandon this " + getMissionTypeNoun() + " without a penalty.", 0f);
			} else if (canAbandonWithoutPenalty()) {
				prompt.addPara("It's been less than a day, and you can still abandon this " + getMissionTypeNoun() + " without a penalty.", 0f);
			} else {
				if (loseRepFaction && !loseRepPerson) {
					prompt.addPara("You can abandon this " + getMissionTypeNoun() + ", but will suffer " +
							"a reputation penalty with " + faction.getDisplayNameWithArticle() + ".", 0f,
							Misc.getTextColor(), faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
				} else if (!loseRepFaction && loseRepPerson) {
					prompt.addPara("You can abandon this " + getMissionTypeNoun() + ", but will suffer " +
							"a reputation penalty with " + getPerson().getNameString() + ".", 
							Misc.getTextColor(), 0f);
				} else {
					prompt.addPara("You can abandon this " + getMissionTypeNoun() + ", but will suffer " +
							"a reputation penalty with both " + getPerson().getNameString() + " and " + 
							faction.getDisplayNameWithArticle() + ".", 0f,
							Misc.getTextColor(), faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
				}
			}
		} else {
			super.createConfirmationPrompt(buttonId, prompt);
		}
	}
	
	protected String getMissionTypeNoun() {
		return "mission";
	}
	
	protected String getMissionCompletionVerb() {
		return "completed";
	}
	
	public int getDistanceLY(MarketAPI market) {
		return getDistanceLY(market.getPrimaryEntity());
	}
	
	public int getDistanceLY(SectorEntityToken entity) {
		int dist = 0;
		if (getPerson() != null && getPerson().getMarket() != null) {
			dist = (int) Math.round(Misc.getDistanceLY(getPerson().getMarket().getLocationInHyperspace(), entity.getLocationInHyperspace()));
		}
		return dist;
	}
	
	public int getDistanceLY(StarSystemAPI system) {
		int dist = 0;
		if (getPerson() != null && getPerson().getMarket() != null) {
			dist = (int) Math.round(Misc.getDistanceLY(getPerson().getMarket().getLocationInHyperspace(), system.getLocation()));
		}
		return dist;
	}
	
	public int getFuel(SectorEntityToken entity, boolean bothWays) {
		int dist = getDistanceLY(entity);
		
		float fuel = Global.getSector().getPlayerFleet().getLogistics().getFuelCostPerLightYear();
		fuel *= dist;
		if (bothWays) fuel *= 2f;
		return (int) Math.round(fuel);
	}
	
	public Object pickOneObject(List options) {
		WeightedRandomPicker<Object> picker = new WeightedRandomPicker<Object>(genRandom);
		for (Object option : options) {
			picker.add(option);
		}
		return picker.pick();
	}
	
	public String pickOne(List<String> options) {
		return pickOne(options.toArray(new String[0]));
	}
	public String pickOne(String ... options) {
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(genRandom);
		for (String option : options) {
			picker.add(option);
		}
		return picker.pick();
	}
	
	protected String getWithoutArticle(String item) {
		if (item.startsWith("a ")) {
			return item.replaceFirst("a ", "");
		}
		if (item.startsWith("an ")) {
			return item.replaceFirst("an ", "");
		}
		if (item.startsWith("the ")) {
			return item.replaceFirst("the ", "");
		}
		return item;
	}
	
	public void setStageOnGlobalFlag(Object to, String flag) {
		connectWithGlobalFlag(null, to, flag);
	}
	public void setStageOnEntityNotAlive(Object to, SectorEntityToken entity) {
		connectWithEntityNotAlive(null, to, entity);
	}
	public void setStageOnDaysElapsed(Object to, float days) {
		connectWithDaysElapsed(null, to, days);
	}
	public void setStageOnInRangeOfCommRelay(Object to) {
		connectWithInRangeOfCommRelay(null, to);
	}
	public void setStageOnEnteredLocation(Object to, LocationAPI location) {
		connectWithEnteredLocation(null, to, location);
	}
	public void setStageInRangeOfEntity(Object to, SectorEntityToken entity, float range) {
		connectWithInRangeOfEntity(null, to, entity, range);
	}
	public void setStageOnWithinHyperspaceRange(Object to, SectorEntityToken entity, float rangeLY) {
		connectWithWithinHyperspaceRange(null, to, entity, rangeLY);
	}
	public void setStageOnCustomCondition(Object to, ConditionChecker custom) {
		connections.add(new StageConnection(null, to, custom));
	}
	
	public void connectWithGlobalFlag(Object from, Object to, String flag) {
		connections.add(new StageConnection(from, to, new GlobalBooleanChecker(flag)));
		// so it gets auto-unset if it's ever set
		changes.add(new VariableSet(getGlobalMemory(), flag, true));
	}
	
	
	public void setStageOnMemoryFlag(Object to, HasMemory withMemory, String flag) {
		setStageOnMemoryFlag(to, withMemory.getMemoryWithoutUpdate(), flag);
	}
	public void connectWithMemoryFlag(Object from, Object to, HasMemory withMemory, String flag) {
		connectWithMemoryFlag(from, to, withMemory.getMemoryWithoutUpdate(), flag);
	}
	
	public void setStageOnMemoryFlag(Object to, MemoryAPI memory, String flag) {
		connectWithMemoryFlag(null, to, memory, flag);
	}
	public void connectWithMemoryFlag(Object from, Object to, MemoryAPI memory, String flag) {
		connections.add(new StageConnection(from, to, new MemoryBooleanChecker(memory, flag)));
		// so it gets auto-unset if it's ever set
		changes.add(new VariableSet(memory, flag, true));
	}
	
	public void connectWithEntityNotAlive(Object from, Object to, SectorEntityToken entity) {
		connections.add(new StageConnection(from, to, new EntityNotAliveChecker(entity)));
	}
	
	public void connectWithMarketDecivilized(Object from, Object to, MarketAPI market) {
		connections.add(new StageConnection(from, to, new MarketDecivChecker(market)));
	}
	public void setStageOnMarketDecivilized(Object to, MarketAPI market) {
		connections.add(new StageConnection(null, to, new MarketDecivChecker(market)));
	}
	
	public void connectWithHostilitiesEnded(Object from, Object to, PersonAPI person, MarketAPI market) {
		connections.add(new StageConnection(from, to, new HostilitiesEndedChecker(person, market)));
	}
	public void setStageOnHostilitiesEnded(Object to, PersonAPI person, MarketAPI market) {
		connections.add(new StageConnection(null, to, new HostilitiesEndedChecker(person, market)));
	}
	public void connectWithHostilitiesStarted(Object from, Object to, PersonAPI person, MarketAPI market) {
		connections.add(new StageConnection(from, to, new HostilitiesStartedChecker(person, market)));
	}
	public void setStageOnHostilitiesStarted(Object to, PersonAPI person, MarketAPI market) {
		connections.add(new StageConnection(null, to, new HostilitiesStartedChecker(person, market)));
	}
	
	public void connectWithDaysElapsed(Object from, Object to, float days) {
		connections.add(new StageConnection(from, to, new DaysElapsedChecker(days, getData(from))));
	}
	
	public void connectWithInRangeOfCommRelay(Object from, Object to) {
		connections.add(new StageConnection(from, to, new InCommRelayRangeChecker()));
	}
	
	public void connectWithEnteredLocation(Object from, Object to, LocationAPI location) {
		connections.add(new StageConnection(from, to, new EnteredLocationChecker(location)));
	}
	public void connectWithInRangeOfEntity(Object from, Object to, SectorEntityToken entity, float range) {
		connections.add(new StageConnection(from, to, new InRangeOfEntityChecker(entity, range)));
	}
	public void connectWithWithinHyperspaceRange(Object from, Object to, SectorEntityToken entity, float rangeLY) {
		connectWithWithinHyperspaceRange(from, to, entity, rangeLY, false);
	}
	public void connectWithWithinHyperspaceRange(Object from, Object to, SectorEntityToken entity, float rangeLY, 
											boolean requirePlayerInHyperspace) {
		connections.add(new StageConnection(from, to, new InHyperRangeOfEntityChecker(entity, rangeLY, requirePlayerInHyperspace)));
	}
	
	public void connectWithCustomCondition(Object from, Object to, ConditionChecker custom) {
		connections.add(new StageConnection(from, to, custom));
	}
	
	public boolean rollProbability(float p) {
		return genRandom.nextFloat() < p;
	}
	
	
	public SectorEntityToken spawnDebrisField(float radius, float density, LocData data) {
		DebrisFieldParams params = new DebrisFieldParams(
				radius, // field radius - should not go above 1000 for performance reasons
				density, // density, visual - affects number of debris pieces
				10000000f, // duration in days 
				0f); // days the field will keep generating glowing pieces
		params.source = DebrisFieldSource.MIXED;
		params.baseSalvageXP = (long) radius; // base XP for scavenging in field
		
		if (!data.updateLocIfNeeded(this, null)) return null;
		
		SectorEntityToken debris = Misc.addDebrisField(data.system, params, genRandom);
		data.placeEntity(debris);
		changes.add(new EntityAdded(debris));
		
		return debris;
	}
	
	public SectorEntityToken spawnMissionNode(LocData data) {
		return spawnEntity(Entities.MISSION_LOCATION, data);
	}
	
	public void makeMissionNodeDiscoverable(SectorEntityToken node) {
		makeDiscoverable(node, 1000f, 200f);
	}
	public void makeDiscoverable(SectorEntityToken entity, float range, float xp) {
		entity.setDiscoveryXP(xp);
		entity.setSensorProfile(1f);
		entity.setDiscoverable(true);
		entity.getDetectedRangeMod().modifyFlat("gen", range);
	}
	
	public EntityLocation generateLocation(String entityId, EntityLocationType locType, SectorEntityToken param, LocationAPI system) {
		EntityLocation loc = null;
		float gap = 100f;
		if (system instanceof StarSystemAPI) {
			if (locType == EntityLocationType.HIDDEN) {
				loc = BaseThemeGenerator.pickHiddenLocation(genRandom, (StarSystemAPI)system, gap, null);
			} else if (locType == EntityLocationType.HIDDEN_NOT_NEAR_STAR) {
				loc = BaseThemeGenerator.pickHiddenLocationNotNearStar(genRandom, (StarSystemAPI)system, gap, null);
			} else if (locType == EntityLocationType.ORBITING_PLANET) {
				loc = BaseThemeGenerator.pickCommonLocation(genRandom, (StarSystemAPI)system, gap, false, null);
			} else if (locType == EntityLocationType.ORBITING_PLANET_OR_STAR) {
				loc = BaseThemeGenerator.pickCommonLocation(genRandom, (StarSystemAPI)system, gap, true, null);
			} else if (locType == EntityLocationType.UNCOMMON) {
				loc = BaseThemeGenerator.pickUncommonLocation(genRandom, (StarSystemAPI)system, gap, null);
			} else if (locType == EntityLocationType.ANY) {
				loc = BaseThemeGenerator.pickAnyLocation(genRandom, (StarSystemAPI)system, gap, null);
			}
		}
		
		if (locType == EntityLocationType.ORBITING_PARAM) {
			loc = BaseThemeGenerator.createLocationAtRandomGap(genRandom, param, gap);
			if (loc == null) {
				float radius = 75f;
				if (entityId != null) {
					CustomEntitySpecAPI spec = Global.getSettings().getCustomEntitySpec(entityId);
					radius = spec.getDefaultRadius();
				}
				loc = new EntityLocation();
				loc.type = LocationType.PLANET_ORBIT;
				loc.orbit = Global.getFactory().createCircularOrbitWithSpin(param, 
									genRandom.nextFloat() * 360f, param.getRadius() + radius + 100f, 
									20f + 20f * genRandom.nextFloat(), genRandom.nextFloat() * 10f + 1f);
			}
		}
		
		if (loc == null) {
			if (system instanceof StarSystemAPI) {
				loc = new EntityLocation();
				loc.type = LocationType.STAR_ORBIT;
				loc.orbit = Global.getFactory().createCircularOrbitWithSpin(((StarSystemAPI)system).getCenter(), 
									genRandom.nextFloat() * 360f, 5000f, 
									20f + 20f * genRandom.nextFloat(), genRandom.nextFloat() * 10f + 1f);
			} else {
				loc = new EntityLocation();
				loc.type = LocationType.OUTER_SYSTEM;
				loc.location = new Vector2f();
			}
		}
		return loc;
	}
	
	public SectorEntityToken spawnEntity(String entityId, LocData data) {
		
		if (!data.updateLocIfNeeded(this, entityId)) return null;
		
		AddedEntity added = BaseThemeGenerator.addEntityAutoDetermineType(genRandom, data.system, data.loc, entityId, Factions.NEUTRAL);
		if (added == null) return null;
		
		if (data.removeOnMissionOver) {
			added.entity.addTag(REMOVE_ON_MISSION_OVER);
		}
		
		added.entity.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
		
		changes.add(new EntityAdded(added.entity));
		return added.entity;
	}
	
	public SectorEntityToken spawnEntityToken(LocData data) {
		if (!data.updateLocIfNeeded(this, null)) return null;
		
		SectorEntityToken token = data.system.createToken(0, 0);
		data.system.addEntity(token);
		data.placeEntity(token);
		changes.add(new EntityAdded(token));
		
		return token;
	}
	
	
	public SectorEntityToken spawnDerelictHull(String hullId, LocData data) {
		if (hullId == null) {
			return spawnDerelictOfType((DerelictType) null, data);
		}
		DerelictShipData shipData = DerelictShipEntityPlugin.createHull(hullId, genRandom, DerelictShipEntityPlugin.getDefaultSModProb());
		return spawnDerelict(shipData, data);
	}
	
	public SectorEntityToken spawnDerelict(String factionId, DerelictType type, LocData data) {
		if (factionId == null) {
			return spawnDerelictOfType(type, data);
		}
		DerelictShipData shipData = DerelictShipEntityPlugin.createRandom(factionId, type, genRandom, DerelictShipEntityPlugin.getDefaultSModProb());
		return spawnDerelict(shipData, data);
	}
	public SectorEntityToken spawnDerelictOfType(DerelictType type, LocData data) {
		WeightedRandomPicker<String> factions = SalvageSpecialAssigner.getNearbyFactions(genRandom, data.system.getLocation(),
										15f, 10f, 10f);
		DerelictShipData shipData = DerelictShipEntityPlugin.createRandom(factions.pick(), type, genRandom, DerelictShipEntityPlugin.getDefaultSModProb());
		return spawnDerelict(shipData, data);
	}
	
	public SectorEntityToken spawnDerelict(DerelictShipData shipData, LocData data) {
		if (shipData == null) return null;
		
		if (!data.updateLocIfNeeded(this, Entities.WRECK)) return null;
		
		SectorEntityToken entity = BaseThemeGenerator.addSalvageEntity(genRandom, data.system, Entities.WRECK, Factions.NEUTRAL, shipData);
		entity.setDiscoverable(true);
		data.placeEntity(entity);
		
		changes.add(new EntityAdded(entity));
		return entity;
	}
	
	public void spawnShipGraveyard(String factionId, int minShips, int maxShips, LocData data) {
		SectorEntityToken focus = spawnEntityToken(data);
		
		int numShips = minShips + genRandom.nextInt(maxShips - minShips + 1);
		
		WeightedRandomPicker<Float> bands = new WeightedRandomPicker<Float>(genRandom);
		for (int i = 0; i < numShips + 5; i++) {
			bands.add(new Float(120f + i * 20f), (i + 1f) * (i + 1f));
		}
		
		for (int i = 0; i < numShips; i++) {
			float r = bands.pickAndRemove();
			
			EntityLocation loc = new EntityLocation();
			loc.type = LocationType.OUTER_SYSTEM;
			float orbitDays = r / (5f + genRandom.nextFloat() * 10f);
			loc.orbit = Global.getFactory().createCircularOrbit(focus, genRandom.nextFloat() * 360f, r, orbitDays);
			
			LocData curr = new LocData(loc, data.system, data.removeOnMissionOver);
			
			spawnDerelict(factionId, null, curr);
		}
	}
	
	
	protected PersonAPI findOrCreateTrader(String factionId, MarketAPI market, boolean cleanUpOnMissionOverIfWasNewPerson) {
		if (CheapCommodityMission.SAME_CONTACT_DEBUG) {
			return findOrCreatePerson(factionId, market, cleanUpOnMissionOverIfWasNewPerson,
				 	Ranks.CITIZEN, 
					Ranks.POST_MERCHANT);	
		}
		return findOrCreatePerson(factionId, market, cleanUpOnMissionOverIfWasNewPerson,
				 	Ranks.CITIZEN, 
					Ranks.POST_MERCHANT,
					Ranks.POST_COMMODITIES_AGENT,
					Ranks.POST_INVESTOR,
					Ranks.POST_TRADER);
				
	}
	
	protected PersonAPI findOrCreateCriminal(MarketAPI market, boolean cleanUpOnMissionOverIfWasNewPerson) {
		return findOrCreatePerson(Factions.PIRATES, market, cleanUpOnMissionOverIfWasNewPerson,
				Ranks.CITIZEN, 
				Ranks.POST_GANGSTER,
				Ranks.POST_SMUGGLER,
				Ranks.POST_FENCE);
		
	}
	
	protected PersonAPI findOrCreateCriminalTrader(MarketAPI market, boolean cleanUpOnMissionOverIfWasNewPerson) {
		return findOrCreatePerson(Factions.PIRATES, market, cleanUpOnMissionOverIfWasNewPerson,
				Ranks.CITIZEN, 
				Ranks.POST_SMUGGLER,
				Ranks.POST_FENCE);
		
	}
	
	protected PersonAPI findOrCreatePerson(String factionId, MarketAPI market, boolean cleanUpOnMissionOverIfWasNewPerson, String defaultRank, String ... posts) {
		String reason = getReason();
		PersonAPI person = null;
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		
		
		FactionAPI faction = null;
		if (market != null) faction = market.getFaction();
		if (factionId != null) {
			faction = Global.getSector().getFaction(factionId);
		}
		
		person = ip.getPerson(genRandom, faction, market, reason, defaultRank, posts).getPerson();
		
		boolean createdNewPerson = !ip.isLastGetPersonResultWasExistingPerson();
		
		if (person != null && !createdNewPerson &&
				Misc.flagHasReason(person.getMemoryWithoutUpdate(), "$requiredForMissions", getReason())) {
			// this can happen if the person was already created *for this exact type of mission*
			// so, don't use them - they're already the target of the same mission and mission
			// creation would fail later anyway.
			// this also causes - when this mission is aborted - for that person to be removed from
			// their market in this failed mission's abort(), because it's requiredForMissions with the
			// same id as this.
			person = null;
		}
		
		if (person == null) {
			person = faction.createRandomPerson(genRandom);
			WeightedRandomPicker<String> postPicker = new WeightedRandomPicker<String>(genRandom);
			for (String post : posts) {
				postPicker.add(post);
			}
			person.setPostId(postPicker.pick());
			person.setRankId(defaultRank);
			person.setMarket(market);
			if (market != null) market.addPerson(person);
			ip.addPerson(person);
			ip.getData(person).getLocation().setMarket(market);
		}
		
		if (isBarEvent() || createdNewPerson) {
			ip.excludeFromGetPerson(person);
		}
		
		boolean addedToComms = false;
		if (market != null && market.getCommDirectory().getEntryForPerson(person) == null) {
			market.getCommDirectory().addPerson(person);
			addedToComms = true;
		}
		
		boolean willBeRemoved = false;
		if (createdNewPerson || addedToComms) {
			if (cleanUpOnMissionOverIfWasNewPerson) {
				person.addTag(REMOVE_ON_MISSION_OVER);
			}
			PersonAdded added = new PersonAdded(market, person, !createdNewPerson); 
			changes.add(added);
			willBeRemoved = true;
		}
		
		makePersonRequired(person);
		if (!willBeRemoved && person.hasTag(REMOVE_ON_MISSION_OVER)) {
			PersonAdded added = new PersonAdded(market, person, false);
			changes.add(added);
		}
		
		person.setMarket(market);
		
		return person;
	}
	
	public void makePersonRequired(PersonAPI person) {
		PersonMadeRequired req = new PersonMadeRequired(person);
		// always add at the start so the flag is unset and the person can be deleted by the PersonAdded change
		changes.add(0, req);
		Misc.setFlagWithReason(person.getMemoryWithoutUpdate(), "$requiredForMissions", getReason(), true, -1f);
	}
	
	protected void ensurePersonIsInCommDirectory(MarketAPI market, PersonAPI person) {
		boolean addedToComms = false;
		if (market != null && market.getCommDirectory().getEntryForPerson(person) == null) {
			market.getCommDirectory().addPerson(person);
			addedToComms = true;
		}
		
		if (addedToComms) {
			PersonAdded added = new PersonAdded(market, person, true); 
			changes.add(added);
		}
	}
	
	
	protected transient String giverFactionId;
	protected transient String giverRank = Ranks.CITIZEN;
	protected transient String giverPost = Ranks.POST_CITIZEN;
	protected transient String giverVoice = null;
	protected transient String giverPortrait;
	protected transient PersonImportance giverImportance = PersonImportance.MEDIUM;
	protected transient String [] giverTags;
	protected transient Gender giverGender = Gender.ANY;
	
	public void setGiverVoice(String giverVoice) {
		this.giverVoice = giverVoice;
	}

	public void setGiverFaction(String factionId) {
		giverFactionId = factionId;
	}
	
	public Gender getGiverGender() {
		return giverGender;
	}

	public void setGiverRank(String giverRank) {
		this.giverRank = giverRank;
	}

	public void setGiverPost(String giverPost) {
		this.giverPost = giverPost;
	}
	
	public void setGiverPortrait(String giverPortrait) {
		this.giverPortrait = giverPortrait;
	}
	
	public void setGiverImportance(PersonImportance giverImportance) {
		this.giverImportance = giverImportance;
	}
	
	public void setGiverTags(String ... giverTags) {
		this.giverTags = giverTags;
	}


	public void findOrCreateGiver(MarketAPI market, boolean addToCommDirectory, boolean cleanUpOnMissionOverIfWasNewPerson) {
		String factionId = giverFactionId;
		if (factionId == null) factionId = market.getFactionId();
		PersonAPI person = findOrCreatePerson(factionId, market, cleanUpOnMissionOverIfWasNewPerson, giverRank, giverPost);
		
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		boolean createdNewPerson = !ip.isLastGetPersonResultWasExistingPerson();
		
		if (person != null) {
			if (createdNewPerson) {
				person.setRankId(giverRank);
				person.setPostId(giverPost);
				person.setImportanceAndVoice(giverImportance, genRandom);
				if (giverVoice != null) {
					person.setVoice(giverVoice);
				}
				if (giverPortrait != null) {
					person.setPortraitSprite(giverPortrait);
				}
			}
			// add giver tags regardless of whether person was created or already existed
			if (giverTags != null) {
				for (String tag : giverTags){ 
					person.addTag(tag);
				}
			}
			if (createdNewPerson && !addToCommDirectory) {
				market.getCommDirectory().removePerson(person);
			}
			person.setMarket(market);
			personOverride = person;
		}
	}
	
	public PersonAPI getPersonOverride() {
		return personOverride;
	}

	public void setPersonOverride(PersonAPI personOverride) {
		this.personOverride = personOverride;
	}

	public PersonImportance pickImportance() {
		WeightedRandomPicker<PersonImportance> picker = new WeightedRandomPicker<PersonImportance>(genRandom);
		picker.add(PersonImportance.VERY_LOW, 1f);
		picker.add(PersonImportance.LOW, 5f);
		picker.add(PersonImportance.MEDIUM, 10f);
		picker.add(PersonImportance.HIGH, 5f);
		picker.add(PersonImportance.VERY_HIGH, 1f);
		return picker.pick();
	}
	public PersonImportance pickMediumImportance() {
		WeightedRandomPicker<PersonImportance> picker = new WeightedRandomPicker<PersonImportance>(genRandom);
		picker.add(PersonImportance.LOW, 5f);
		picker.add(PersonImportance.MEDIUM, 10f);
		picker.add(PersonImportance.HIGH, 5f);
		return picker.pick();
	}
	public PersonImportance pickHighImportance() {
		WeightedRandomPicker<PersonImportance> picker = new WeightedRandomPicker<PersonImportance>(genRandom);
		picker.add(PersonImportance.MEDIUM, 10f);
		picker.add(PersonImportance.HIGH, 5f);
		picker.add(PersonImportance.VERY_HIGH, 1f);
		return picker.pick();
	}
	public PersonImportance pickLowImportance() {
		WeightedRandomPicker<PersonImportance> picker = new WeightedRandomPicker<PersonImportance>(genRandom);
		picker.add(PersonImportance.VERY_LOW, 10f);
		picker.add(PersonImportance.LOW, 5f);
		picker.add(PersonImportance.MEDIUM, 1f);
		return picker.pick();
	}

	public void createGiver(MarketAPI market, boolean addToCommDirectory, boolean removeOnMissionOver) {
		String factionId = giverFactionId;
		if (factionId == null) factionId = market.getFactionId();
		
		PersonAPI person = Global.getSector().getFaction(factionId).createRandomPerson(giverGender, genRandom);
		person.setRankId(giverRank);
		person.setPostId(giverPost);
		person.setImportanceAndVoice(giverImportance, genRandom);
		if (giverVoice != null) {
			person.setVoice(giverVoice);
		}
		if (giverPortrait != null) {
			person.setPortraitSprite(giverPortrait);
		}
		if (giverTags != null) {
			for (String tag : giverTags){ 
				person.addTag(tag);
			}
		}
		
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		
		market.addPerson(person);
		ip.addPerson(person);
		ip.getData(person).getLocation().setMarket(market);
	
		if (addToCommDirectory) {
			market.getCommDirectory().addPerson(person);
		}
		if (removeOnMissionOver) {
			person.addTag(REMOVE_ON_MISSION_OVER);
		}
		PersonAdded added = new PersonAdded(market, person, false); 
		changes.add(added);
		
		makePersonRequired(person);
		
		personOverride = person;
		
		genMissionRewardMultAndQuality();
	}
	
	
	public void genMissionRewardMultAndQuality() {
		PersonAPI person = getPerson();
		if (person == null) return;
		float rel = person.getRelToPlayer().getRel();
		
		if (rel > 0) {
			rewardMult = 1f + rel * (Global.getSettings().getFloat("missionMaxRewardMultFromRel") - 1f);
		} else if (rel < 0) {
			rewardMult = 1f + rel * (1f - Global.getSettings().getFloat("missionMinRewardMultFromRel"));
		}
		
		float importance = person.getImportance().getValue();

		float min = getMinQuality(); 
		float maxRelBonus = Global.getSettings().getFloat("missionMaxPossibleQualityAboveImportance"); 
		
		quality = Math.min(Math.max(0, rel), importance);
		if (rel > importance && importance < 1f) {
			quality += (rel - importance) / (1f - importance) * maxRelBonus;
		}
		
		if (person.getMemoryWithoutUpdate().contains(BaseMissionHub.MISSION_QUALITY_BONUS)) {
			quality += person.getMemoryWithoutUpdate().getFloat(BaseMissionHub.MISSION_QUALITY_BONUS);
		}
		
		if (quality < min) quality = min;
		if (quality > 1f) quality = 1f;
	}
	
	public float getBaseQuality() {
		PersonAPI person = getPerson();
		if (person == null) return 0.5f;
		float importance = person.getImportance().getValue();
		return importance;
	}
	public float getMaxQuality() {
		PersonAPI person = getPerson();
		if (person == null) return 0f;
		float maxRelBonus = Global.getSettings().getFloat("missionMaxPossibleQualityAboveImportance");
		float importance = person.getImportance().getValue();
		return Math.min(1f, importance + maxRelBonus);
	}
	public float getMinQuality() {
		PersonAPI person = getPerson();
		if (person == null) return 0f;
		float importance = person.getImportance().getValue();
		float min = importance - Global.getSettings().getFloat("missionMinPossibleQualityBelowImportance");
		if (min < 0) min = 0;
		return min;
	}

	public float getQuality() {
		return quality;
	}

	public void setQuality(float quality) {
		this.quality = quality;
	}

	public float getRewardMult() {
		return rewardMult;
	}
	
	public float getRewardMultFraction() {
		float max = Global.getSettings().getFloat("missionMaxRewardMultFromRel");
		return Math.min(1f, Math.max(0f, (rewardMult - 1f) / (max - 1f)));
	}

	public void setRewardMult(float rewardMult) {
		this.rewardMult = rewardMult;
	}

	public Object getCurrentStage() {
		return currentStage;
	}
	
	public void addFleetDefeatTrigger(CampaignFleetAPI fleet, String trigger, boolean permanent) {
		Misc.addDefeatTrigger(fleet, trigger);
		changes.add(new DefeatTriggerAdded(fleet, trigger, permanent));
	}

	public String getLocated(SectorEntityToken entity) {
		return BreadcrumbSpecial.getLocatedString(entity, true);
	}
	public String getLocatedUnclear(SectorEntityToken entity) {
		return BreadcrumbSpecial.getLocatedString(entity, false);
	}
	
	
	public String getGetWithinCommsRangeText() {
		return "Get within range of a functional comm relay to complete the mission and receive " +
	 			"your reward.";
	}
	
	public String getGetWithinCommsRangeTextShort() {
		return "Get within comms range to complete the mission";
	}
	
	public String getGoToSystemTextShort(StarSystemAPI system) {
		return "Go to the " + system.getNameWithLowercaseTypeShort();
	}
	
	public String getGoToPlanetTextShort(PlanetAPI planet) {
		if (planet.getStarSystem() != null) {
			return "Go to " + planet.getName() + " in the " + planet.getStarSystem().getNameWithLowercaseTypeShort();
		} else {
			return "Go to " + planet.getName();
		}
	}
	
	public String getGoToPlanetTextPre(PlanetAPI planet) {
		String a = planet.getSpec().getAOrAn();
		String world = planet.getTypeNameWithWorld().toLowerCase();
		if (planet.getStarSystem() != null) {
			return "Go to " + planet.getName() + ", " + a + " " + world + " in the " + planet.getStarSystem().getNameWithLowercaseType();
		} else {
			return "Go to " + planet.getName() + ", " + a + " " + world + " in hyperspace";
		}
	}
	
	public String getGoToMarketText(MarketAPI market) {
		if (market.getStarSystem() != null) {
			return "Go to " + market.getName() + " in the " + market.getStarSystem().getNameWithLowercaseTypeShort();
		} else {
			return "Go to " + market.getName();
		}
	}
	
	public String getGoTalkToPersonText(PersonAPI person) {
		MarketAPI market = person.getMarket();
		if (market != null) {
			return getGoToMarketText(market) + " and talk to " + person.getNameString();
		} else {
			return "Talk to " + person.getNameString();
		}
	}
	
	
	public String getReturnText(MarketAPI market) {
		return getReturnText(market.getName());
	}
	public String getReturnText(String locationName) {
		return "Return to " + locationName + " and talk to " + 
				 getPerson().getNameString() + " to receive your reward";
	}
	public String getReturnTextShort(MarketAPI market) {
		return getReturnTextShort(market.getName());
	}
	public String getReturnTextShort(String locationName) {
		return "Return to " + locationName + " and talk to " + 
				getPerson().getNameString() + "";
	}
	

	public EntityLocation generateLocationInsideTerrain(CampaignTerrainAPI terrain) {
		//LocationAPI location = terrain.getContainingLocation();
		
		CampaignTerrainPlugin plugin = terrain.getPlugin();
		boolean found = false;
		float orbitAngle = 0f;
		float orbitRadius = 0f;
		float orbitPeriod = 0f;
		SectorEntityToken orbitFocus = terrain;
		Vector2f forceLoc = null;
		
		if (plugin instanceof BaseTiledTerrain) {
			BaseTiledTerrain tiles = (BaseTiledTerrain) plugin;
			
			float maxRadius = plugin.getRenderRange();
			if (maxRadius < 100f) maxRadius = 100f;
			WeightedRandomPicker<Pair<Integer, Integer>> picker = new WeightedRandomPicker<Pair<Integer,Integer>>(genRandom);
			WeightedRandomPicker<Pair<Integer, Integer>> pickerPref = new WeightedRandomPicker<Pair<Integer,Integer>>(genRandom);
			
			for (int i = 0; i < tiles.getTiles().length; i++) {
				for (int j = 0; j < tiles.getTiles()[0].length; j++) {
					if (tiles.getTiles()[i][j] >= 0) {
						float [] f = tiles.getTileCenter(i, j);
						Vector2f loc = new Vector2f(f[0], f[1]);
						float dist = Misc.getDistance(terrain.getLocation(), loc);
						float weight = (float) Math.pow(dist / maxRadius, 3);
						if (dist < 16000) {
							pickerPref.add(new Pair<Integer, Integer>(i, j), weight);
						} else if (pickerPref.isEmpty()) {
							picker.add(new Pair<Integer, Integer>(i, j), weight);
						}
					}
				}
			}
			
			Pair<Integer, Integer> pick = pickerPref.pick();
			if (pick == null) pick = picker.pick();
			
			if (pick != null) {
				float [] f = tiles.getTileCenter(pick.one, pick.two);
				Vector2f loc = new Vector2f(f[0], f[1]);
				
				if (terrain.getOrbit() == null || terrain.getCircularOrbitRadius() <= 0 || terrain.getOrbitFocus() == null) {
					forceLoc = loc;
				} else {
					orbitFocus = terrain.getOrbitFocus();
					orbitAngle = Misc.getAngleInDegrees(orbitFocus.getLocation(), loc);
					orbitRadius = Misc.getDistance(orbitFocus.getLocation(), loc);
					orbitPeriod = terrain.getCircularOrbitPeriod();
				}
				found = true;
			}

		} else if (plugin instanceof BaseRingTerrain) {
			BaseRingTerrain ring = (BaseRingTerrain) plugin;
			SectorEntityToken atCenter = ring.getRingParams().relatedEntity;
			
			float centerRadius = 0f;
			if (atCenter != null) centerRadius = atCenter.getRadius();
			float ringMiddle = ring.getRingParams().middleRadius;
			float ringMin = ring.getRingParams().middleRadius - ring.getRingParams().bandWidthInEngine / 2f;
			float ringMax = ring.getRingParams().middleRadius + ring.getRingParams().bandWidthInEngine / 2f;
			
			float min = Math.max(centerRadius, ringMin);
			orbitRadius = min + (ringMax - min) * (0.1f + 0.8f * genRandom.nextFloat());
			orbitAngle = genRandom.nextFloat() * 360f;
			found = true;
		}
		
		if (!found) {
			orbitRadius = 100f + 100f * genRandom.nextFloat();
			orbitAngle = 360f * genRandom.nextFloat();
		}
		
		EntityLocation eLoc = new EntityLocation();
		eLoc.type = LocationType.OUTER_SYSTEM;
		if (forceLoc != null) {
			eLoc.location = forceLoc;
		} else {
			if (orbitPeriod <= 0f) {
				orbitPeriod = orbitRadius / (5f + 5f * genRandom.nextFloat());
			}
			eLoc.orbit = Global.getFactory().createCircularOrbit(orbitFocus, 
					orbitAngle, orbitRadius, orbitPeriod);
		}

		return eLoc;
	}
	
	public static String getTerrainName(CampaignTerrainAPI terrain) {
		String name = terrain.getPlugin().getTerrainName();
		if (name == null) name = "";
		if (name.contains(" L4") || name.contains(" L5")) {
			name = getTerrainType(terrain);
		}
		return name;
	}
	public static boolean hasSpecialName(CampaignTerrainAPI terrain) {
		return !getTerrainName(terrain).toLowerCase().equals(getTerrainType(terrain).toLowerCase());
	}
	public static String getTerrainNameAOrAn(CampaignTerrainAPI terrain) {
		String name = getTerrainName(terrain);
		if (name != null) {
			return Misc.getAOrAnFor(name);
		} else {
			return terrain.getPlugin().getNameAOrAn();
		}
	}
	public static String getTerrainTypeAOrAn(CampaignTerrainAPI terrain) {
		String type = getTerrainType(terrain);
		if (type != null) {
			return Misc.getAOrAnFor(type);
		} else {
			return terrain.getPlugin().getNameAOrAn();
		}
	}
	public static String getTerrainType(CampaignTerrainAPI terrain) {
		return terrain.getPlugin().getNameForTooltip().toLowerCase();
	}
	
	
//	public void addGetWithinCommsRangeText(TooltipMakerAPI info, float pad) {
//		info.addPara("Get within range of a functional comm relay to complete the mission and receive " +
//				 	"your reward.", pad);
//	}
//	public void addGetWithinCommsRangeTextShort(TooltipMakerAPI info, Color tc, float pad) {
//		info.addPara("Get within comms range to complete the mission", tc, pad);
//	}
//	
//	public void addGoToSystemTextShort(StarSystemAPI system, TooltipMakerAPI info, Color tc, float pad) {
//		info.addPara("Go to the " + system.getNameWithLowercaseTypeShort() + "", tc, pad);
//	}
//	
//	public void addGoToPlanetTextShort(PlanetAPI planet, TooltipMakerAPI info, Color tc, float pad) {
//		if (planet.getStarSystem() != null) {
//			info.addPara("Go to " + planet.getName() + " in the " + planet.getStarSystem().getNameWithLowercaseTypeShort(), tc, pad);
//		} else {
//			info.addPara("Go to " + planet.getName() + " in hyperspace", tc, pad);
//		}
//	}
	
	public static float getUnits(float lightYears) {
		return lightYears * Misc.getUnitsPerLightYear();
	}
	
	public static boolean playerHasEnough(String comId, int quantity) {
		return Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity(comId) >= quantity;
	}
	
	public void assignShipName(FleetMemberAPI member, String factionId) {
		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(factionId, null, true);
		fleet.getFleetData().setShipNameRandom(genRandom);
		fleet.getFleetData().addFleetMember(member);
		fleet.getFleetData().removeFleetMember(member);
	}
	
	public String getDayOrDays(float days) {
		int d = (int) Math.round(days);
		String daysStr = "days";
		if (d == 1) {
			daysStr = "day";
		}
		return daysStr;
	}
	
//	public List<String> getHighRankingMilitaryPosts(MarketAPI market) {
//		List<String> posts = new ArrayList<String>();
//		if (Misc.isMilitary(market)) {
//			posts.add(Ranks.POST_BASE_COMMANDER);
//		}
//		if (Misc.hasOrbitalStation(market)) {
//			posts.add(Ranks.POST_STATION_COMMANDER);
//		}
//		if (posts.isEmpty()) {
//			posts.add(Ranks.POST_GENERIC_MILITARY);
//		}
//		return posts;
//	}

	public List<Abortable> getChanges() {
		return changes;
	}

	public Random getGenRandom() {
		return genRandom;
	}
	
	public void addOnAcceptCommodity(String commodityId, int quantity) {
		cargoOnAccept.addCommodity(commodityId, quantity);
	}
	public void addOnAcceptWeaponDrop(final String weaponId, final int quantity) {
		cargoOnAccept.addWeapons(weaponId, quantity);
	}
	public void addOnAcceptFighterLPCDrop(final String wingId, final int quantity) {
		cargoOnAccept.addFighters(wingId, quantity);
	}
	public void addOnAcceptHullmodDrop(final String hullmodId) {
		cargoOnAccept.addHullmods(hullmodId, 1);
	}
	public void addOnAcceptSpecialItemDrop(final String itemId, final String data) {
		cargoOnAccept.addSpecial(new SpecialItemData(itemId, data), 1);
	}
	
	public void addOnSuccessCommodity(String commodityId, int quantity) {
		if (cargoOnSuccess == null) cargoOnSuccess = Global.getFactory().createCargo(true);
		cargoOnSuccess.addCommodity(commodityId, quantity);
	}
	public void addOnSuccessWeaponDrop(final String weaponId, final int quantity) {
		if (cargoOnSuccess == null) cargoOnSuccess = Global.getFactory().createCargo(true);
		cargoOnSuccess.addWeapons(weaponId, quantity);
	}
	public void addOnSuccessFighterLPCDrop(final String wingId, final int quantity) {
		if (cargoOnSuccess == null) cargoOnSuccess = Global.getFactory().createCargo(true);
		cargoOnSuccess.addFighters(wingId, quantity);
	}
	public void addOnSuccessHullmodDrop(final String hullmodId) {
		if (cargoOnSuccess == null) cargoOnSuccess = Global.getFactory().createCargo(true);
		cargoOnSuccess.addHullmods(hullmodId, 1);
	}
	public void addOnSuccessSpecialItemDrop(final String itemId, final String data) {
		if (cargoOnSuccess == null) cargoOnSuccess = Global.getFactory().createCargo(true);
		cargoOnSuccess.addSpecial(new SpecialItemData(itemId, data), 1);
	}
	
	public int getMarinesRequiredToDisrupt(MarketAPI market, Industry industry, int daysRequired) {
		int daysPerToken = MarketCMD.getDisruptDaysPerToken(market, industry);
		daysRequired -= (int) industry.getDisruptedDays();
		int tokens = (int) Math.ceil((float) daysRequired / (float) daysPerToken);
		if (tokens < 1) tokens = 1;
		
		int marinesRequired = MarketCMD.getMarinesFor(market, tokens);
		marinesRequired = getAdjustedMarinesRequired(marinesRequired);
		return marinesRequired;
	}
	
	public void addDisruptRaidInfo(MarketAPI market, Industry industry, int daysRequired, TooltipMakerAPI info, float pad) {
		int marinesRequired = getMarinesRequiredToDisrupt(market, industry, daysRequired); 
		
		RaidDangerLevel danger = industry.getSpec().getDisruptDanger();
		
		Color h = Misc.getHighlightColor();
		LabelAPI label = info.addPara(industry.getCurrentName() + " must be disrupted for at least %s days. The operation " +
				"will require around %s marines, and the " +
				"danger level is %s.", 
				pad, h,
				"" + daysRequired,
				Misc.getWithDGS(marinesRequired),
				danger.name.toLowerCase());
		label.setHighlightColors(h, h, danger.color);
	}
	
	public int getMarinesRequiredForCustomObjective(MarketAPI market, RaidDangerLevel danger) {
		int marinesRequired = MarketCMD.getMarinesFor(market, Math.max(1, danger.marineTokens));
		marinesRequired = getAdjustedMarinesRequired(marinesRequired);
		return marinesRequired;
	}
	
	public int getMarinesRequiredForCustomDefenderStrength(int defenderStrength, RaidDangerLevel danger) {
		int marinesRequired = MarketCMD.getMarinesFor(defenderStrength, Math.max(1, danger.marineTokens));
		marinesRequired = getAdjustedMarinesRequired(marinesRequired);
		return marinesRequired;
	}
	
	public void addCustomRaidInfo(MarketAPI market, RaidDangerLevel danger, TooltipMakerAPI info, float pad) {
		int marinesRequired = getMarinesRequiredForCustomObjective(market, danger);
		
		Color h = Misc.getHighlightColor();
		LabelAPI label = info.addPara("The operation " +
				"will require around %s marines, and the " +
				"danger level is %s.", 
				pad, h,
				Misc.getWithDGS(marinesRequired),
				danger.name.toLowerCase());
		label.setHighlightColors(h, danger.color);
	}
	
	public void addCustomRaidInfo(int defenderStrength, RaidDangerLevel danger, TooltipMakerAPI info, float pad) {
		int marinesRequired = MarketCMD.getMarinesFor(defenderStrength, Math.max(1, danger.marineTokens));
		marinesRequired = getAdjustedMarinesRequired(marinesRequired);
		
		Color h = Misc.getHighlightColor();
		LabelAPI label = info.addPara("The operation " +
				"will require around %s marines, and the " +
				"danger level is %s.", 
				pad, h,
				Misc.getWithDGS(marinesRequired),
				danger.name.toLowerCase());
		label.setHighlightColors(h, danger.color);
	}
	
	public static int getAdjustedMarinesRequired(int marinesRequired) {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		float support = Misc.getFleetwideTotalMod(playerFleet, Stats.FLEET_GROUND_SUPPORT, 0f);
		StatBonus stat = playerFleet.getStats().getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD);
		
		
		int min = 0;
		int max = 0;
		for (int i = 1; i < marinesRequired * 2; i *= 2) {
			float currSupport = (int) Math.round(Math.min(support, i));
			float strength = i + currSupport;
			strength = stat.computeEffective(strength);
			if (strength >= marinesRequired) {
				min = i / 2;
				max = i;
				break;
			}
		}
		if (max > 0) {
			int iter = Math.max(1, (max - min) / 100);
			
			for (int i = min; i <= max; i += iter) {
				float currSupport = (int) Math.min(support, i);
				float strength = i + currSupport;
				strength = stat.computeEffective(strength);
				if (strength >= marinesRequired) {
					marinesRequired = (int) Math.round(strength);
					break;
				}
			}
		}
		
		//if (true) return marinesRequired;
		
		int base = 10;
		if (marinesRequired > 100) base = 50;
		if (marinesRequired > 500) base = 100;
		if (marinesRequired > 1000) base = 250;
		if (marinesRequired > 2000) base = 500;
		if (marinesRequired > 5000) base = 1000;
		for (int i = 0; i < 10; i++) {
			if (marinesRequired <= (i + 1) * base) {
				marinesRequired = (i + 1) * base;
				break;
			}
		}
		marinesRequired = getRoundNumber(marinesRequired);
		return marinesRequired;
	}
	
	
	public void addStandardMarketDesc(String prefix, MarketAPI market, TooltipMakerAPI info, float pad) {
		Color h = Misc.getHighlightColor();
		FactionAPI f = market.getFaction();

		if (prefix == null || prefix.isEmpty()) {
			if (market.isInHyperspace()) {
				LabelAPI label = info.addPara(market.getName() + "is a size %s " +
						"colony in hyperspace controlled by " + f.getDisplayNameWithArticle() + ".",
						pad, f.getBaseUIColor(),
						"" + market.getSize(), f.getDisplayNameWithArticleWithoutArticle());
				label.setHighlight(market.getName(), "" + market.getSize(), f.getDisplayNameWithArticleWithoutArticle());
				label.setHighlightColors(f.getBaseUIColor(), h, f.getBaseUIColor());
			} else {
				LabelAPI label = info.addPara(market.getName() + "is a size %s " +
						"colony in the " + market.getStarSystem().getNameWithLowercaseTypeShort() + " controlled by " + f.getDisplayNameWithArticle() + ".",
						pad, f.getBaseUIColor(),
						"" + market.getSize(), f.getDisplayNameWithArticleWithoutArticle());
				label.setHighlight(market.getName(), "" + market.getSize(), f.getDisplayNameWithArticleWithoutArticle());
				label.setHighlightColors(f.getBaseUIColor(), h, f.getBaseUIColor());
			}
		} else {
			if (market.isInHyperspace()) {
				LabelAPI label = info.addPara(prefix + " " + market.getName() + ", a size %s " +
						"colony in hyperspace controlled by " + f.getDisplayNameWithArticle() + ".",
						pad, f.getBaseUIColor(),
						"" + market.getSize(), f.getDisplayNameWithArticleWithoutArticle());
				label.setHighlight(market.getName(), "" + market.getSize(), f.getDisplayNameWithArticleWithoutArticle());
				label.setHighlightColors(f.getBaseUIColor(), h, f.getBaseUIColor());
			} else {
				LabelAPI label = info.addPara(prefix + " " + market.getName() + ", a size %s " +
						"colony in the " + market.getStarSystem().getNameWithLowercaseTypeShort() + " controlled by " + f.getDisplayNameWithArticle() + ".",
						pad, f.getBaseUIColor(),
						"" + market.getSize(), f.getDisplayNameWithArticleWithoutArticle());
				label.setHighlight(market.getName(), "" + market.getSize(), f.getDisplayNameWithArticleWithoutArticle());
				label.setHighlightColors(f.getBaseUIColor(), h, f.getBaseUIColor());
			}
		}
//		label.setHighlight("" + market.getSize(), f.getDisplayNameWithArticleWithoutArticle());
//		label.setHighlightColors(h, f.getBaseUIColor());
	}
	
	public int getBombardmentFuel(MarketAPI market) {
		int fuel = MarketCMD.getBombardmentCost(market, Global.getSector().getPlayerFleet());
		fuel = getRoundNumber(fuel);
		return fuel;
	}
	
	public void addBombardmentInfo(MarketAPI market, TooltipMakerAPI info, float pad) {
		int fuel = getBombardmentFuel(market);
		
		Color h = Misc.getHighlightColor();
		info.addPara("Effectively bombarding the target will require approximately %s units of fuel.", 
				pad, h,
				Misc.getWithDGS(fuel));
	}
	
	public void addSpecialItemDropOnlyUseInAcceptImplNotUndoneOnAbort(SectorEntityToken entity, SpecialItemData data) {
		CargoAPI cargo = Global.getFactory().createCargo(true);
		cargo.addSpecial(data, 1);
		BaseSalvageSpecial.addExtraSalvage(entity, cargo);
	}
	
	
	public PersonAPI getImportantPerson(String id) {
		return Global.getSector().getImportantPeople().getData(id).getPerson();
	}
	public PersonDataAPI getImportantPersonData(String id) {
		return Global.getSector().getImportantPeople().getData(id);
	}
	
	public void setMemoryValuePermanent(HasMemory withMemory, String key, Object value) {
		withMemory.getMemoryWithoutUpdate().set(key, value);
	}

	protected String completedKey = null;
	public void setStoryMission() {
		setNoAbandon();
		addTag(Tags.INTEL_STORY);
		setUseLargeFontInMissionList();
		//setCompletedKey();
	}
	
	public void setCompletedKey() {
		completedKey = "$" + getMissionId() + "_missionCompleted";
	}
	
	public boolean isOkToOfferMissionRequiringMarines(int marines) {
		PlaythroughLog log = PlaythroughLog.getInstance();
		long crew = log.getPrevValue("crew");
		long credits = log.getPrevValue("credits");
		return crew > marines * 2 || credits > marines * 400;
	}

	public Object getStartingStage() {
		return startingStage;
	}

	
	public PersonAPI getPersonAtMarketPost(MarketAPI market, String ... postIds) {
		for (String postId : postIds) {
			for (PersonAPI person : market.getPeopleCopy()) {
				if (postId.equals(person.getPostId())) {
					return person;
				}
			}
		}
		return null;
	}
	
	public MarketAPI getMarket(String id) {
		return Global.getSector().getEconomy().getMarket(id);
	}
	
	protected Color mapMarkerNameColor = null;
	public void setMapMarkerNameColor(Color mapMarkerColor) {
		this.mapMarkerNameColor = mapMarkerColor;
	}
	
	public void setMapMarkerNameColorBasedOnStar(StarSystemAPI system) {
		if (system.getCenter() instanceof PlanetAPI) {
			Color color = Misc.setAlpha(((PlanetAPI)system.getCenter()).getSpec().getIconColor(), 255);
			color = Misc.setBrightness(color, 235);
			setMapMarkerNameColor(color);
		}
	}

	public List<MissionTrigger> getTriggers() {
		return triggers;
	}
	

}







