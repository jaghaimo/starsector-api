package com.fs.starfarer.api.impl.campaign.missions.hub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.HasMemory;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictType;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerAction;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerActionContext;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.TransmitterTrapSpecial;
import com.fs.starfarer.api.impl.campaign.skills.OfficerTraining;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 * The methods/classes defined in this class could easily
 * be in BaseHubMission instead; this class exists purely for organizational purposes.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2019 Fractal Softworks, LLC
 */
public abstract class HubMissionWithTriggers extends BaseHubMission {
	
	public static enum FleetSize {
		TINY(0.06f),
		VERY_SMALL(0.1f),
		SMALL(0.2f),
		MEDIUM(0.35f),
		LARGE(0.5f),
		LARGER(0.6f),
		VERY_LARGE(0.7f),
		HUGE(0.9f),
		MAXIMUM(1.1f);
		
		public float maxFPFraction;
		private FleetSize(float maxFPFraction) {
			this.maxFPFraction = maxFPFraction;
		}
		
		private static FleetSize [] vals = values();
		public FleetSize next() {
			int index = this.ordinal() + 1;
			if (index >= vals.length) index = vals.length - 1;
			return vals[index];
		}
		public FleetSize prev() {
			int index = this.ordinal() - 1;
			if (index < 0) index = 0;
			return vals[index];
		}
	}
	
	public static enum FleetQuality {
		VERY_LOW(-1f, 0),
		LOWER(-0.5f, 0),
		DEFAULT(0f, 0),
		HIGHER(0.5f, 0),
		VERY_HIGH(1f, 0),
		SMOD_1(1.5f, 1),
		SMOD_2(1.5f, 2),
		SMOD_3(1.5f, 3);
		
		
		public float qualityMod;
		public int numSMods;
		private FleetQuality(float qualityMod, int numSMods) {
			this.qualityMod = qualityMod;
			this.numSMods = numSMods;
		}
		
		private static FleetQuality [] vals = values();
		public FleetQuality next() {
			int index = this.ordinal() + 1;
			if (index >= vals.length) index = vals.length - 1;
			return vals[index];
		}
		public FleetQuality prev() {
			int index = this.ordinal() - 1;
			if (index < 0) index = 0;
			return vals[index];
		}
	}
	
	public static enum OfficerNum {
		NONE,
		FC_ONLY,
		FEWER,
		DEFAULT,
		MORE,
		ALL_SHIPS;
		
		private static OfficerNum [] vals = values();
		public OfficerNum next() {
			int index = this.ordinal() + 1;
			if (index >= vals.length) index = vals.length - 1;
			return vals[index];
		}
		public OfficerNum prev() {
			int index = this.ordinal() - 1;
			if (index < 0) index = 0;
			return vals[index];
		}
	}
	
	public static enum OfficerQuality {
		LOWER,
		DEFAULT,
		HIGHER,
		UNUSUALLY_HIGH, /** officers like this can only be found in cryopods by the player */
		AI_GAMMA,
		AI_BETA,
		AI_BETA_OR_GAMMA,
		AI_ALPHA,
		AI_OMEGA, /** made you look */
		AI_MIXED;
		
		private static OfficerQuality [] vals = values();
		public OfficerQuality next() {
			int index = this.ordinal() + 1;
			if (index >= vals.length) index = vals.length - 1;
			return vals[index];
		}
		public OfficerQuality prev() {
			int index = this.ordinal() - 1;
			if (index < 0) index = 0;
			return vals[index];
		}
	}
	
	public static class FleetAddTugs implements TriggerAction {
		int numTugs = 0;
		public FleetAddTugs(int numTugs) {
			this.numTugs = numTugs;
		}
		public void doAction(TriggerActionContext context) {
			if (context.fleet != null) {
				addTugsToFleet(context.fleet, numTugs, ((BaseHubMission)context.mission).genRandom);
			}
		}
	}
	public static class UnhideCommListing implements TriggerAction {
		protected PersonAPI person;
		public UnhideCommListing(PersonAPI person) {
			this.person = person;
		}

		public void doAction(TriggerActionContext context) {
			if (person.getMarket() != null) {
				MarketAPI market = person.getMarket();
				if (market.getCommDirectory().getEntryForPerson(person) == null) {
					market.getCommDirectory().addPerson(person);
				}
				if (market.getCommDirectory().getEntryForPerson(person) != null) {
					market.getCommDirectory().getEntryForPerson(person).setHidden(false);
				}
			}
		}
	}
	
	/**
	 * Person must be "important", i.e. in the ImportantPeople directory.
	 */
	public static class HideCommListing implements TriggerAction {
		protected PersonAPI person;
		public HideCommListing(PersonAPI person) {
			this.person = person;
		}

		public void doAction(TriggerActionContext context) {
			if (person.getMarket() != null) {
				MarketAPI market = person.getMarket();
				if (market.getCommDirectory().getEntryForPerson(person) == null) {
					market.getCommDirectory().addPerson(person);
				}
				if (market.getCommDirectory().getEntryForPerson(person) != null) {
					market.getCommDirectory().getEntryForPerson(person).setHidden(true);
				}
			}
		}
	}
	
	public static class MovePersonToMarket implements TriggerAction {
		protected PersonAPI person;
		protected MarketAPI market;
		protected boolean alwaysAddToComms;
		
		public MovePersonToMarket(PersonAPI person, MarketAPI market, boolean alwaysAddToComms) {
			super();
			this.person = person;
			this.market = market;
			this.alwaysAddToComms = alwaysAddToComms;
		}


		public void doAction(TriggerActionContext context) {
			Misc.moveToMarket(person, market, alwaysAddToComms);
		}
	}
	
	
	public static class DespawnEntityAction implements TriggerAction {
		protected SectorEntityToken entity;
		public DespawnEntityAction(SectorEntityToken entity) {
			this.entity = entity;
		}

		public void doAction(TriggerActionContext context) {
			Misc.fadeAndExpire(entity);
		}
	}
	
	public static class MakeDiscoverableAction implements TriggerAction {
		protected float range;
		protected float xp;
		public MakeDiscoverableAction(float range, float xp) {
			this.range = range;
			this.xp = xp;
		}
		
		public void doAction(TriggerActionContext context) {
			((BaseHubMission)context.mission).makeDiscoverable(context.entity, range, xp);
			
		}
	}
	
	//public void spawnShipGraveyard(String factionId, int minShips, int maxShips, LocData data) {
	public static class SpawnShipGraveyardAction implements TriggerAction {
		protected String factionId;
		protected int minShips;
		protected int maxShips;
		protected LocData data;
		public SpawnShipGraveyardAction(String factionId, int minShips, int maxShips, LocData data) {
			this.factionId = factionId;
			this.minShips = minShips;
			this.maxShips = maxShips;
			this.data = data;
		}
		
		public void doAction(TriggerActionContext context) {
			((BaseHubMission)context.mission).spawnShipGraveyard(factionId, minShips, maxShips, data); 
		}
		
	}
	public static class SpawnDebrisFieldAction implements TriggerAction {
		protected float radius;
		protected float density;
		protected LocData data;
		
		public SpawnDebrisFieldAction(float radius, float density, LocData data) {
			this.radius = radius;
			this.density = density;
			this.data = data;
		}

		public void doAction(TriggerActionContext context) {
			SectorEntityToken entity = ((BaseHubMission)context.mission).spawnDebrisField(radius, density, data); 
			context.entity = entity;
		}
	}
	
	
	public static class SpawnEntityAction implements TriggerAction {
		protected String entityId;
		protected LocData data;

		public SpawnEntityAction(String entityId, LocData data) {
			this.entityId = entityId;
			this.data = data;
		}

		public void doAction(TriggerActionContext context) {
			SectorEntityToken entity = ((BaseHubMission)context.mission).spawnEntity(entityId, data);
			context.entity = entity;
		}
	}
	
	public static class SpawnDerelictAction implements TriggerAction {
		protected String hullId;
		protected String factionId;
		protected DerelictType type;
		protected DerelictShipData shipData;
		protected LocData data;
		
		/**
		 * Specify hullId OR factionId, not both.
		 * @param data
		 */
		public SpawnDerelictAction(String hullId, String factionId, DerelictType type, LocData data) {
			this.hullId = hullId;
			this.factionId = factionId;
			this.data = data;
			this.type = type;
		}
		
		public SpawnDerelictAction(DerelictType type, LocData data) {
			this.data = data;
			this.type = type;
		}
		public SpawnDerelictAction(DerelictShipData shipData, LocData data) {
			this.shipData = shipData;
			this.data = data;
		}
		
		public void doAction(TriggerActionContext context) {
			SectorEntityToken entity = null;
			if (hullId != null) {
				entity = ((BaseHubMission)context.mission).spawnDerelictHull(hullId, data);
			} else if (factionId != null) {
				entity = ((BaseHubMission)context.mission).spawnDerelict(factionId, type, data);
			} else if (shipData != null) {
				entity = ((BaseHubMission)context.mission).spawnDerelict(shipData, data);
			} else {
				entity = ((BaseHubMission)context.mission).spawnDerelictOfType(type, data);
			}
			context.entity = entity;
		}
	}

	

	public static class SpawnFleetNearAction implements TriggerAction {
		protected SectorEntityToken entity;
		protected float range;

		public SpawnFleetNearAction(SectorEntityToken entity, float range) {
			this.entity = entity;
			this.range = range;
		}

		public void doAction(TriggerActionContext context) {
			entity.getContainingLocation().addEntity(context.fleet);
			Vector2f loc = Misc.getPointWithinRadius(entity.getLocation(), range);
			context.fleet.setLocation(loc.x, loc.y);
		}
	}
	public static class SetFleetFactionAction implements TriggerAction {
		protected String factionId;

		public SetFleetFactionAction(String factionId) {
			this.factionId = factionId;
		}

		public void doAction(TriggerActionContext context) {
			context.fleet.setFaction(factionId, true);
		}
	}
	
	public static class SetEntityToPickedJumpPoint implements TriggerAction {
		public SetEntityToPickedJumpPoint() {
		}
		
		public void doAction(TriggerActionContext context) {
			context.entity = context.jumpPoint;
		}
	}

	public static class FleetMakeImportantAction implements TriggerAction {
		protected String flag;
		protected Enum[] stages;
		public FleetMakeImportantAction(String flag, Enum ... stages) {
			this.flag = flag;
			this.stages = stages;
		}
		public void doAction(TriggerActionContext context) {
			BaseHubMission bhm = (BaseHubMission) context.mission;
			bhm.makeImportant(context.fleet, flag, stages);
			
			if (stages != null && Arrays.asList(stages).contains(bhm.getCurrentStage())) {
				Misc.makeImportant(context.fleet.getMemoryWithoutUpdate(), bhm.getReason());
				bhm.changes.add(new MadeImportant(context.fleet.getMemoryWithoutUpdate(), bhm.getReason()));
				
				if (flag != null) {
					context.fleet.getMemoryWithoutUpdate().set(flag, true);
					bhm.changes.add(new VariableSet(context.fleet.getMemoryWithoutUpdate(), flag, true));
				}
			}
//			else if (stages == null) {
//				Misc.makeImportant(context.fleet.getMemoryWithoutUpdate(), bhm.getReason());
//			}
			
//			if (context.mission instanceof BaseHubMission) {
//				Misc.makeImportant(context.fleet.getMemoryWithoutUpdate(), reason);
//				BaseHubMission bhm = (BaseHubMission) context.mission;
//				bhm.changes.add(new MadeImportant(context.fleet.getMemoryWithoutUpdate(), reason));
//			}
		}
	}
	
	public static class EntityMakeImportantAction implements TriggerAction {
		protected String flag;
		protected Enum[] stages;
		public EntityMakeImportantAction(String flag, Enum ... stages) {
			this.flag = flag;
			this.stages = stages;
		}
		public void doAction(TriggerActionContext context) {
			BaseHubMission bhm = (BaseHubMission) context.mission;
			bhm.makeImportant(context.entity, flag, stages);
			
			if (Arrays.asList(stages).contains(bhm.getCurrentStage())) {
				Misc.makeImportant(context.entity.getMemoryWithoutUpdate(), bhm.getReason());
				bhm.changes.add(new MadeImportant(context.entity.getMemoryWithoutUpdate(), bhm.getReason()));
				
				if (flag != null) {
					context.entity.getMemoryWithoutUpdate().set(flag, true);
					bhm.changes.add(new VariableSet(context.entity.getMemoryWithoutUpdate(), flag, true));
				}
			}
			
//			if (context.mission instanceof BaseHubMission) {
//				Misc.makeImportant(context.entity.getMemoryWithoutUpdate(), reason);
//				BaseHubMission bhm = (BaseHubMission) context.mission;
//				bhm.changes.add(new MadeImportant(context.entity.getMemoryWithoutUpdate(), reason));
//			}
		}
	}

	public static class SetFleetFlagsWithReasonAction implements TriggerAction {
		protected String[] flags;
		protected String reason;
		protected boolean permanent;

		public SetFleetFlagsWithReasonAction(String reason, boolean permanent, String ... flags) {
			this.permanent = permanent;
			this.flags = flags;
			this.reason = reason;
		}

		public void doAction(TriggerActionContext context) {
			BaseHubMission bhm = (BaseHubMission) context.mission;
			for (String flag : flags) {
				Misc.setFlagWithReason(context.fleet.getMemoryWithoutUpdate(),
									   flag, reason, true, -1f);
				
				if (context.makeAllFleetFlagsPermanent) {
					permanent = true;
				}
				
				if (!permanent && bhm != null) {
					String requiredKey = flag + "_" + reason;
					bhm.changes.add(new VariableSet(context.fleet.getMemoryWithoutUpdate(), requiredKey, true));
				}
			}
		}
	}

	public static class UnsetFleetFlagsWithReasonAction implements
			TriggerAction {
		protected String[] flags;
		protected String reason;

		public UnsetFleetFlagsWithReasonAction(String reason, String ... flags) {
			this.reason = reason;
			this.flags = flags;
		}

		public void doAction(TriggerActionContext context) {
			for (String flag : flags) {
				Misc.setFlagWithReason(context.fleet.getMemoryWithoutUpdate(),
						flag, reason, false, -1f);
			}
		}
	}


	public static class SetPersonMissionRefAction implements TriggerAction {
		protected String key;

		public SetPersonMissionRefAction(String key) {
			this.key = key;
		}

		public void doAction(TriggerActionContext context) {
			context.person.getMemoryWithoutUpdate().set(key, context.mission);
		}
	}


	public static class SetFleetMissionRefAction implements TriggerAction {
		protected String key;

		public SetFleetMissionRefAction(String key) {
			this.key = key;
		}

		public void doAction(TriggerActionContext context) {
			context.fleet.getMemoryWithoutUpdate().set(key, context.mission);
		}
	}


	public static class SetMemoryValueAction implements TriggerAction {
		protected String key;
		protected Object value;
		protected MemoryAPI memory;
		protected boolean removeOnMissionOver;
		
		public SetMemoryValueAction(MemoryAPI memory, String key, Object value, boolean removeOnMissionOver) {
			this.memory = memory;
			this.key = key;
			this.value = value;
			this.removeOnMissionOver = removeOnMissionOver;
		}
		
		public void doAction(TriggerActionContext context) {
			memory.set(key, value);
			BaseHubMission bhm = (BaseHubMission) context.mission;
			bhm.changes.add(new VariableSet(memory, key, removeOnMissionOver));
		}
	}
	
	public static class SetMemoryValueAfterDelay implements TriggerAction, EveryFrameScript {
		protected String key;
		protected Object value;
		protected MemoryAPI memory;
		protected float delay;
		
		public SetMemoryValueAfterDelay(float delay, MemoryAPI memory, String key, Object value) {
			this.memory = memory;
			this.key = key;
			this.value = value;
			this.delay = delay;
		}
		
		public void doAction(TriggerActionContext context) {
			if (delay <= 0) {
				memory.set(key, value);
			} else {
				Global.getSector().addScript(this);
			}
		}

		public boolean isDone() {
			return delay < 0;
		}

		public boolean runWhilePaused() {
			return false;
		}

		public void advance(float amount) {
			if (delay < 0) return; 
			float days = Global.getSector().getClock().convertToDays(amount);
			delay -= days;
			if (delay < 0) {
				memory.set(key, value);
			}
		}
	}
	
	public static class AddTagAfterDelay implements TriggerAction, EveryFrameScript {
		protected String tag;
		protected float delay;
		protected StarSystemAPI system;
		
		public AddTagAfterDelay(float delay, StarSystemAPI system, String tag) {
			this.delay = delay;
			this.system = system;
			this.tag = tag;
		}
		
		public void doAction(TriggerActionContext context) {
			Global.getSector().addScript(this);
		}
		
		public boolean isDone() {
			return delay < 0;
		}
		
		public boolean runWhilePaused() {
			return false;
		}
		
		public void advance(float amount) {
			if (delay < 0) return; 
			float days = Global.getSector().getClock().convertToDays(amount);
			delay -= days;
			if (delay < 0) {
				system.addTag(tag);
			}
		}
	}
	
	public static class RunScriptAfterDelay implements TriggerAction, EveryFrameScript {
		protected float delay;
		protected Script script;
		
		public RunScriptAfterDelay(float delay, Script script) {
			this.script = script;
			this.delay = delay;
		}
		
		public void doAction(TriggerActionContext context) {
			Global.getSector().addScript(this);
		}
		
		public boolean isDone() {
			return delay < 0;
		}
		
		public boolean runWhilePaused() {
			return false;
		}
		
		public void advance(float amount) {
			if (delay < 0) return; 
			float days = Global.getSector().getClock().convertToDays(amount);
			delay -= days;
			if (delay < 0 && script != null) {
				script.run();
				script = null;
			}
		}
	}
	
	public static class IncreaseMarketHostileTimeout implements TriggerAction {
		protected MarketAPI market;
		protected float days;
		
		public IncreaseMarketHostileTimeout(MarketAPI market, float days) {
			this.market = market;
			this.days = days;
		}

		public void doAction(TriggerActionContext context) {
			Misc.increaseMarketHostileTimeout(market, days);
		}
	}
	
	public static class SetFleetMemoryValueAction implements TriggerAction {
		protected String key;
		protected Object value;

		public SetFleetMemoryValueAction(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		public void doAction(TriggerActionContext context) {
			context.fleet.getMemoryWithoutUpdate().set(key, value);
		}
	}
	
	public static class AddFleetDefeatTriggerAction implements TriggerAction {
		protected String trigger;
		protected boolean permanent;
		public AddFleetDefeatTriggerAction(String trigger, boolean permanent) {
			this.trigger = trigger;
			this.permanent = permanent;
		}
		public void doAction(TriggerActionContext context) {
			((BaseHubMission)context.mission).addFleetDefeatTrigger(context.fleet, trigger, permanent);
		}
	}
	
	public static class MakeFleetFlagsPermanentAction implements TriggerAction {
		protected boolean permanent;
		public MakeFleetFlagsPermanentAction(boolean permanent) {
			this.permanent = permanent;
		}
		public void doAction(TriggerActionContext context) {
			context.makeAllFleetFlagsPermanent = permanent;
		}
	}


	public static class AddTagsAction implements TriggerAction {
		protected String [] tags;
		
		public AddTagsAction(String ... tags) {
			this.tags = tags;
		}
		
		public void doAction(TriggerActionContext context) {
			for (String tag : tags) {
				context.fleet.addTag(tag);
			}
		}
	}
	
	public static class AddCommanderSkillAction implements TriggerAction {
		protected String skill;
		protected int level;
		
		public AddCommanderSkillAction(String skill, int level) {
			this.skill = skill;
			this.level = level;
		}
		
		public void doAction(TriggerActionContext context) {
			context.fleet.getCommanderStats().setSkillLevel(skill, level);
		}
	}
	
	public static class SetFleetFlagAction implements TriggerAction {
		protected String flag;
		protected Object[] stages;
		protected boolean permanent;

		public SetFleetFlagAction(String flag, boolean permanent, Object ... stages) {
			this.flag = flag;
			this.permanent = permanent;
			this.stages = stages;
		}

		public void doAction(TriggerActionContext context) {
			if (context.makeAllFleetFlagsPermanent) {
				permanent = true;
			}
			((BaseHubMission)context.mission).setFlag(context.fleet, flag, permanent, stages);
		}
	}
	
	public static class SetEntityFlagAction implements TriggerAction {
		protected String flag;
		protected Object[] stages;
		protected boolean permanent;

		public SetEntityFlagAction(String flag, boolean permanent, Object ... stages) {
			this.flag = flag;
			this.permanent = permanent;
			this.stages = stages;
		}

		public void doAction(TriggerActionContext context) {
			((BaseHubMission)context.mission).setFlag(context.entity, flag, permanent, stages);
		}
	}


	public static class UnsetFleetFlagsAction implements TriggerAction {
		protected String[] flags;

		public UnsetFleetFlagsAction(String ... flags) {
			this.flags = flags;
		}

		public void doAction(TriggerActionContext context) {
			for (String flag : flags) {
				context.fleet.getMemoryWithoutUpdate().unset(flag);
			}
		}
	}
	
	public static class UnsetEntityFlagsAction implements TriggerAction {
		protected String[] flags;
		
		public UnsetEntityFlagsAction(String ... flags) {
			this.flags = flags;
		}
		
		public void doAction(TriggerActionContext context) {
			for (String flag : flags) {
				context.entity.getMemoryWithoutUpdate().unset(flag);
			}
		}
	}


	public static class SaveEntityReferenceAction implements TriggerAction {
		protected MemoryAPI memory;
		protected String key;
		public SaveEntityReferenceAction(MemoryAPI memory, String key) {
			this.memory = memory;
			this.key = key;
		}
		
		public void doAction(TriggerActionContext context) {
			memory.set(key, context.entity);
			((BaseHubMission) context.mission).changes.add(new VariableSet(memory, key, true));
		}
	}
	
	public static class SaveFleetReferenceAction implements TriggerAction {
		protected MemoryAPI memory;
		protected String key;
		public SaveFleetReferenceAction(MemoryAPI memory, String key) {
			this.memory = memory;
			this.key = key;
		}
		
		public void doAction(TriggerActionContext context) {
			memory.set(key, context.fleet);
			((BaseHubMission) context.mission).changes.add(new VariableSet(memory, key, true));
		}
	}
	
	public static class RemoveAbilitiesAction implements TriggerAction {
		protected String[] abilities;

		public RemoveAbilitiesAction(String ... abilities) {
			this.abilities = abilities;
		}

		public void doAction(TriggerActionContext context) {
			for (String ability : abilities) {
				context.fleet.removeAbility(ability);
			}
		}
	}


	public static class AddAbilitiesAction implements TriggerAction {
		protected String[] abilities;

		public AddAbilitiesAction(String ... abilities) {
			this.abilities = abilities;
		}

		public void doAction(TriggerActionContext context) {
			for (String ability : abilities) {
				context.fleet.addAbility(ability);
			}
		}
	}
	
	public static class GenericAddTagsAction implements TriggerAction {
		protected String [] tags;
		protected SectorEntityToken entity;
		
		public GenericAddTagsAction(SectorEntityToken entity, String ... tags) {
			this.tags = tags;
			this.entity = entity;
		}
		
		public void doAction(TriggerActionContext context) {
			for (String tag : tags) {
				entity.addTag(tag);
			}
		}
	}
	
	public static class GenericRemoveTagsAction implements TriggerAction {
		protected String [] tags;
		protected SectorEntityToken entity;
		
		public GenericRemoveTagsAction(SectorEntityToken entity, String ... tags) {
			this.tags = tags;
			this.entity = entity;
		}
		
		public void doAction(TriggerActionContext context) {
			for (String tag : tags) {
				entity.removeTag(tag);
			}
		}
	}
	
	public static class MakeNonStoryCriticalAction implements TriggerAction {
		protected MemoryAPI [] memoryArray;

		public MakeNonStoryCriticalAction(MemoryAPI ... memoryArray) {
			this.memoryArray = memoryArray;
		}
		public void doAction(TriggerActionContext context) {
			BaseHubMission bhm = (BaseHubMission) context.mission;
			for (MemoryAPI memory : memoryArray) {
				Misc.makeNonStoryCritical(memory, bhm.getReason());
			}
		}
	}


	public static class SetInflaterAction implements TriggerAction {
		protected FleetInflater inflater;

		public SetInflaterAction(FleetInflater inflater) {
			this.inflater = inflater;
		}

		public void doAction(TriggerActionContext context) {
			context.fleet.setInflater(inflater);
		}
	}


	public static class SetRemnantConfigAction implements TriggerAction {
		protected boolean dormant;
		protected long seed;

		public SetRemnantConfigAction(boolean dormant, long seed) {
			this.dormant = dormant;
			this.seed = seed;
		}

		public void doAction(TriggerActionContext context) {
			Random random = new Random(seed);
			RemnantSeededFleetManager.initRemnantFleetProperties(random, context.fleet, dormant);
		}
	}


	public static class AddCustomDropAction implements TriggerAction {
		protected CargoAPI cargo;

		public AddCustomDropAction(CargoAPI cargo) {
			this.cargo = cargo;
		}

		public void doAction(TriggerActionContext context) {
			BaseSalvageSpecial.addExtraSalvage(context.fleet, cargo);
		}
	}


	public static class AddCommodityDropAction implements TriggerAction {
		protected int quantity;
		protected String commodityId;
		protected boolean dropQuantityBasedOnShipsDestroyed;

		public AddCommodityDropAction(int quantity, String commodityId, boolean dropQuantityBasedOnShipsDestroyed) {
			this.quantity = quantity;
			this.commodityId = commodityId;
			this.dropQuantityBasedOnShipsDestroyed = dropQuantityBasedOnShipsDestroyed;
		}

		public void doAction(TriggerActionContext context) {
			if (dropQuantityBasedOnShipsDestroyed) {
				context.fleet.getCargo().addCommodity(commodityId, quantity);
			} else {
				CargoAPI cargo = Global.getFactory().createCargo(true);
				cargo.addCommodity(commodityId, quantity);
				BaseSalvageSpecial.addExtraSalvage(context.fleet, cargo);
			}
		}
	}
	
	public static class AddCommodityFractionDropAction implements TriggerAction {
		protected float fraction;
		protected String commodityId;
		protected boolean dropQuantityBasedOnShipsDestroyed;
		
		public AddCommodityFractionDropAction(float fraction, String commodityId) {
			this.fraction = fraction;
			this.commodityId = commodityId;
		}
		
		public void doAction(TriggerActionContext context) {
			CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(commodityId);
			float capacity = context.fleet.getCargo().getMaxCapacity();
			if (spec.isFuel()) {
				capacity = context.fleet.getCargo().getMaxFuel();
			} else if (spec.isPersonnel()) {
				capacity = context.fleet.getCargo().getMaxPersonnel();
			}
			int quantity = (int) Math.round(fraction * capacity);
			if (quantity > 0) {
				context.fleet.getCargo().addCommodity(commodityId, quantity);
			}
		}
	}


	public static class AddWeaponDropAction implements TriggerAction {
		protected int quantity;
		protected String weaponId;

		public AddWeaponDropAction(int quantity, String weaponId) {
			this.quantity = quantity;
			this.weaponId = weaponId;
		}

		public void doAction(TriggerActionContext context) {
			CargoAPI cargo = Global.getFactory().createCargo(true);
			cargo.addWeapons(weaponId, quantity);
			BaseSalvageSpecial.addExtraSalvage(context.fleet, cargo);
		}
	}


	public static class AddFighterLPCDropAction implements TriggerAction {
		protected String wingId;
		protected int quantity;

		public AddFighterLPCDropAction(String wingId, int quantity) {
			this.wingId = wingId;
			this.quantity = quantity;
		}

		public void doAction(TriggerActionContext context) {
			CargoAPI cargo = Global.getFactory().createCargo(true);
			cargo.addFighters(wingId, quantity);
			BaseSalvageSpecial.addExtraSalvage(context.fleet, cargo);
		}
	}


	public static class AddHullmodDropAction implements TriggerAction {
		protected String hullmodId;

		public AddHullmodDropAction(String hullmodId) {
			this.hullmodId = hullmodId;
		}

		public void doAction(TriggerActionContext context) {
			CargoAPI cargo = Global.getFactory().createCargo(true);
			cargo.addHullmods(hullmodId, 1);
			BaseSalvageSpecial.addExtraSalvage(context.fleet, cargo);
		}
	}


	public static class AddSpecialItemDropAction implements TriggerAction {
		protected String data;
		protected String itemId;

		public AddSpecialItemDropAction(String data, String itemId) {
			this.data = data;
			this.itemId = itemId;
		}

		public void doAction(TriggerActionContext context) {
			CargoAPI cargo = Global.getFactory().createCargo(true);
			cargo.addSpecial(new SpecialItemData(itemId, data), 1);
			BaseSalvageSpecial.addExtraSalvage(context.fleet, cargo);
		}
	}


	public static class SpawnFleetAtPickedLocationAction implements
			TriggerAction {
		protected float range;

		public SpawnFleetAtPickedLocationAction(float range) {
			this.range = range;
		}

		public void doAction(TriggerActionContext context) {
			context.containingLocation.addEntity(context.fleet);
			Vector2f loc = Misc.getPointWithinRadius(context.coordinates, range);
			context.fleet.setLocation(loc.x, loc.y);
		}
	}


	public static class PickSetLocationAction implements TriggerAction {
		protected Vector2f coordinates;
		protected LocationAPI location;

		public PickSetLocationAction(Vector2f coordinates, LocationAPI location) {
			this.coordinates = coordinates;
			this.location = location;
		}

		public void doAction(TriggerActionContext context) {
			context.coordinates = coordinates;
			context.containingLocation = location;
		}
	}


	public static class PickLocationInHyperspaceAction implements TriggerAction {
		protected StarSystemAPI system;

		public PickLocationInHyperspaceAction(StarSystemAPI system) {
			this.system = system;
		}

		public void doAction(TriggerActionContext context) {
			//CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
			Vector2f pick = pickLocationWithinArc(((BaseHubMission)context.mission).genRandom, system.getHyperspaceAnchor(), 0, 360f, 2000f, 0f, 1000f);
			
			context.coordinates = pick;
			//context.containingLocation = playerFleet.getContainingLocation();
			context.containingLocation = Global.getSector().getHyperspace();
		}
	}


	public static class PickLocationTowardsPlayerAction implements TriggerAction {
		protected SectorEntityToken entity;
		protected float arc;
		protected float minDist;
		protected float maxDist;
		protected float minDistFromPlayer;

		public PickLocationTowardsPlayerAction(SectorEntityToken entity,
				float arc, float minDist, float maxDist, float minDistFromPlayer) {
			this.entity = entity;
			this.arc = arc;
			this.minDist = minDist;
			this.maxDist = maxDist;
			this.minDistFromPlayer = minDistFromPlayer;
		}

		public void doAction(TriggerActionContext context) {
			if (entity == null) entity = context.entity;
			if (entity == null) entity = context.jumpPoint;
			
			CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
			float dir = Misc.getAngleInDegrees(playerFleet.getLocation(), entity.getLocation());
			dir += 180f;
			if (playerFleet.getContainingLocation() != entity.getContainingLocation()) {
				dir = ((BaseHubMission)context.mission).genRandom.nextFloat() * 360f;
			}
			
			Vector2f pick = pickLocationWithinArc(((BaseHubMission)context.mission).genRandom, entity, dir, arc, minDistFromPlayer, minDist, maxDist);
			
			context.coordinates = pick;
			//context.containingLocation = playerFleet.getContainingLocation();
			context.containingLocation = entity.getContainingLocation();
		}
	}
	public static class PickLocationTowardsEntityAction implements TriggerAction {
		protected SectorEntityToken entity;
		protected float arc;
		protected float minDist;
		protected float maxDist;
		protected float minDistFromPlayer;
		
		public PickLocationTowardsEntityAction(SectorEntityToken entity,
				float arc, float minDist, float maxDist, float minDistFromPlayer) {
			this.entity = entity;
			this.arc = arc;
			this.minDist = minDist;
			this.maxDist = maxDist;
			this.minDistFromPlayer = minDistFromPlayer;
		}
		
		public void doAction(TriggerActionContext context) {
			if (entity == null) entity = context.entity;
			if (entity == null) entity = context.jumpPoint;
			
			CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
			float dir = Misc.getAngleInDegrees(playerFleet.getLocation(), entity.getLocation());
			//dir += 180f;
			if (playerFleet.getContainingLocation() != entity.getContainingLocation()) {
				dir = ((BaseHubMission)context.mission).genRandom.nextFloat() * 360f;
			}
			
			Vector2f pick = pickLocationWithinArc(((BaseHubMission)context.mission).genRandom, playerFleet, dir, arc, minDistFromPlayer, minDist, maxDist);
			
			context.coordinates = pick;
			//context.containingLocation = playerFleet.getContainingLocation();
			context.containingLocation = entity.getContainingLocation();
		}
	}


	public static class PickLocationAwayFromPlayerAction implements
			TriggerAction {
		protected float minDist;
		protected SectorEntityToken entity;
		protected float maxDist;
		protected float arc;
		protected float minDistFromPlayer;

		public PickLocationAwayFromPlayerAction(float minDist,
				SectorEntityToken entity, float maxDist, float arc,
				float minDistFromPlayer) {
			this.minDist = minDist;
			this.entity = entity;
			this.maxDist = maxDist;
			this.arc = arc;
			this.minDistFromPlayer = minDistFromPlayer;
		}

		public void doAction(TriggerActionContext context) {
			if (entity == null) entity = context.entity;
			if (entity == null) entity = context.jumpPoint;
			
			CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
			float dir = Misc.getAngleInDegrees(playerFleet.getLocation(), entity.getLocation());
			if (playerFleet.getContainingLocation() != entity.getContainingLocation()) {
				dir = ((BaseHubMission)context.mission).genRandom.nextFloat() * 360f;
			}
			
			Vector2f pick = pickLocationWithinArc(((BaseHubMission)context.mission).genRandom, entity, dir, arc, minDistFromPlayer, minDist, maxDist);
			
			context.coordinates = pick;
			//context.containingLocation = playerFleet.getContainingLocation();
			context.containingLocation = entity.getContainingLocation();
		}
	}


	public static class PickLocationAroundPlayerAction implements TriggerAction {
		protected float maxDist;
		protected float minDist;

		public PickLocationAroundPlayerAction(float maxDist, float minDist) {
			this.maxDist = maxDist;
			this.minDist = minDist;
		}

		public void doAction(TriggerActionContext context) {
			CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
			Vector2f pick = pickLocationWithinArc(((BaseHubMission)context.mission).genRandom, playerFleet, 0, 360f, minDist, minDist, maxDist);
			
			context.coordinates = pick;
			context.containingLocation = playerFleet.getContainingLocation();
		}
	}


	public static class PickLocationAroundEntityAction implements TriggerAction {
		protected float minDist;
		protected SectorEntityToken entity;
		protected float maxDist;
		protected float minDistFromPlayer;

		public PickLocationAroundEntityAction(float minDist,
				SectorEntityToken entity, float maxDist, float minDistFromPlayer) {
			this.minDist = minDist;
			this.entity = entity;
			this.maxDist = maxDist;
			this.minDistFromPlayer = minDistFromPlayer;
		}

		public void doAction(TriggerActionContext context) {
			if (entity == null) entity = context.entity;
			if (entity == null) entity = context.jumpPoint;
			Vector2f pick = pickLocationWithinArc(((BaseHubMission)context.mission).genRandom, entity, 0, 360f, minDistFromPlayer, minDist, maxDist);
			
			context.coordinates = pick;
			context.containingLocation = entity.getContainingLocation();
		}
	}


	public static class PickLocationWithinArcAction implements TriggerAction {
		protected float arc;
		protected SectorEntityToken entity;
		protected float maxDist;
		protected float minDist;
		protected float minDistFromPlayer;
		protected float dir;

		public PickLocationWithinArcAction(float arc, SectorEntityToken entity,
				float maxDist, float minDist, float minDistFromPlayer, float dir) {
			this.arc = arc;
			this.entity = entity;
			this.maxDist = maxDist;
			this.minDist = minDist;
			this.minDistFromPlayer = minDistFromPlayer;
			this.dir = dir;
		}

		public void doAction(TriggerActionContext context) {
			if (entity == null) entity = context.entity;
			if (entity == null) entity = context.jumpPoint;
			
			Vector2f pick = pickLocationWithinArc(((BaseHubMission)context.mission).genRandom, entity, dir, arc, minDistFromPlayer, minDist, maxDist);
			
			context.coordinates = pick;
			context.containingLocation = entity.getContainingLocation();
		}
	}


	public static class FleetSetPatrolActionText implements TriggerAction {
		protected String text;
		public FleetSetPatrolActionText(String text) {
			this.text = text;
		}
		
		public void doAction(TriggerActionContext context) {
			context.patrolText = text;
		}
	}
	
	public static class FleetSetTravelActionText implements TriggerAction {
		protected String text;
		public FleetSetTravelActionText(String text) {
			this.text = text;
		}
		
		public void doAction(TriggerActionContext context) {
			context.travelText = text;
		}
	}
	
	public static class OrderFleetPatrolSystemAction implements TriggerAction {
		protected StarSystemAPI system;

		public OrderFleetPatrolSystemAction(StarSystemAPI system) {
			this.system = system;
		}

		public void doAction(TriggerActionContext context) {
			context.fleet.addScript(new TriggerFleetAssignmentAI(context.travelText, context.patrolText, context.mission, system, false, 
																 context.fleet, (SectorEntityToken[])null));
		}
	}


	public static class OrderFleetPatrolPointsAction implements TriggerAction {
		protected List<SectorEntityToken> patrolPoints;
		protected boolean randomizeLocation;
		protected StarSystemAPI system;

		public OrderFleetPatrolPointsAction(SectorEntityToken[] patrolPoints,
				boolean randomizeLocation, StarSystemAPI system) {
			this.patrolPoints = new ArrayList<SectorEntityToken>();
			for (SectorEntityToken curr : patrolPoints) {
				this.patrolPoints.add(curr);
			}
			this.randomizeLocation = randomizeLocation;
			this.system = system;
		}

		public void doAction(TriggerActionContext context) {
			context.fleet.addScript(new TriggerFleetAssignmentAI(context.travelText, context.patrolText, 
					context.mission, system, randomizeLocation, context.fleet, patrolPoints.toArray(new SectorEntityToken[0])));
		}
	}
	
	public static class OrderFleetPatrolSpawnedEntity implements TriggerAction {
		protected boolean moveToNearEntity;

		public OrderFleetPatrolSpawnedEntity(boolean moveToNearEntity) {
			this.moveToNearEntity = moveToNearEntity;
		}
		
		public void doAction(TriggerActionContext context) {
			SectorEntityToken entity = context.entity;
			if (entity == null) entity = context.jumpPoint;
			if (entity == null) entity = context.token;
			if (entity == null) entity = context.planet;
			context.fleet.addScript(
					new TriggerFleetAssignmentAI(context.travelText, context.patrolText, context.mission,
							context.entity.getContainingLocation(), moveToNearEntity, context.fleet, entity));
		}
	}


	public static class OrderFleetPatrolTagsAction implements TriggerAction {
		protected List<SectorEntityToken> added;
		protected StarSystemAPI system;
		protected boolean randomizeLocation;
		protected String[] tags;

		public OrderFleetPatrolTagsAction(StarSystemAPI system,
				boolean randomizeLocation, String ... tags) {
			this.system = system;
			this.randomizeLocation = randomizeLocation;
			this.tags = tags;
		}

		public void doAction(TriggerActionContext context) {
			List<SectorEntityToken> points = new ArrayList<SectorEntityToken>();
			for (SectorEntityToken entity : system.getAllEntities()) {
				for (String tag : tags) {
					if (entity.hasTag(tag)) {
						points.add(entity);
						break;
					}
				}
			}
			if (added != null) points.addAll(added);
			context.fleet.addScript(new TriggerFleetAssignmentAI(context.travelText, context.patrolText, context.mission, system, randomizeLocation, context.fleet,
									points.toArray(new SectorEntityToken[0])));
		}
	}

	public static class OrderFleetStopPursuingPlayerUnlessInStage implements TriggerAction {
		protected List<Object> stages;
		protected BaseHubMission mission;
		public OrderFleetStopPursuingPlayerUnlessInStage(BaseHubMission mission, Object...stages) {
			this.mission = mission;
			this.stages = Arrays.asList(stages);
		}

		public void doAction(TriggerActionContext context) {
			context.fleet.addScript(new MissionFleetStopPursuingPlayer(context.fleet, mission, stages));
		}
	}
	
	public static class OrderFleetInterceptNearbyPlayerInStage implements TriggerAction {
		protected List<Object> stages;
		protected BaseHubMission mission;
		protected float maxRange;
		protected boolean repeatable;
		protected boolean mustBeStrongEnoughToFight;
		protected float repeatDelay;
		public OrderFleetInterceptNearbyPlayerInStage(BaseHubMission mission, 
				boolean mustBeStrongEnoughToFight,
				float maxRange, 
				boolean repeatable, float repeatDelay, Object...stages) {
			this.mission = mission;
			this.mustBeStrongEnoughToFight = mustBeStrongEnoughToFight;
			this.maxRange = maxRange;
			this.repeatable = repeatable;
			this.repeatDelay = repeatDelay;
			this.stages = Arrays.asList(stages);
		}
		
		public void doAction(TriggerActionContext context) {
			context.fleet.addScript(new MissionFleetInterceptPlayerIfNearby(context.fleet, mission, 
					mustBeStrongEnoughToFight, maxRange, repeatable, repeatDelay, stages));
		}
	}
	

	public static class OrderFleetInterceptPlayerAction implements TriggerAction {
		protected boolean makeHostile;
		public OrderFleetInterceptPlayerAction(boolean makeHostile) {
			this.makeHostile = makeHostile;
		}

		public void doAction(TriggerActionContext context) {
			TransmitterTrapSpecial.makeFleetInterceptPlayer(context.fleet, false, false, makeHostile, 1000f);
			if (!context.fleet.hasScriptOfClass(MissionFleetAutoDespawn.class)) {
				context.fleet.addScript(new MissionFleetAutoDespawn(context.mission, context.fleet));
			}
		}
	}
	
	public static class OrderFleetEBurn implements TriggerAction {
		public OrderFleetEBurn() {
		}
		
		public void doAction(TriggerActionContext context) {
			AbilityPlugin eb = context.fleet.getAbility(Abilities.EMERGENCY_BURN);
			if (eb != null && eb.isUsable()) eb.activate();
		}
	}


	public static class FleetNoAutoDespawnAction implements TriggerAction {
		public void doAction(TriggerActionContext context) {
			context.fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
		}
	}


	public static class PickLocationAtInSystemJumpPointAction implements TriggerAction {
		protected StarSystemAPI system;
		protected float minDistFromPlayer;

		public PickLocationAtInSystemJumpPointAction(StarSystemAPI system,
				float minDistFromPlayer) {
			this.system = system;
			this.minDistFromPlayer = minDistFromPlayer;
		}

		public void doAction(TriggerActionContext context) {
			WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>(((BaseHubMission)context.mission).genRandom);
			picker.addAll(system.getJumpPoints());
			
			SectorEntityToken pick = picker.pick();
			Vector2f loc = pickLocationWithinArc(((BaseHubMission)context.mission).genRandom, pick, 0, 360f, minDistFromPlayer, 0f, 0f);
			
			context.jumpPoint = pick;
			context.coordinates = loc;
			context.containingLocation = system;
		}
	}
	
	public static class PickLocationAtClosestToPlayerJumpPointAction implements TriggerAction {
		protected StarSystemAPI system;
		protected float minDistFromPlayer;
		
		public PickLocationAtClosestToPlayerJumpPointAction(StarSystemAPI system, float minDistFromPlayer) {
			this.system = system;
			this.minDistFromPlayer = minDistFromPlayer;
		}
		
		public void doAction(TriggerActionContext context) {
			WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>(((BaseHubMission)context.mission).genRandom);
			
			SectorEntityToken closest = null;
			float minDist = Float.MAX_VALUE;
			for (SectorEntityToken jp : system.getJumpPoints()) {
				if (system.isCurrentLocation()) {
					float dist = Misc.getDistance(jp, Global.getSector().getPlayerFleet());
					if (dist < minDist) {
						closest = jp;
						minDist = dist;
					}
				} else {
					picker.add(jp);
				}
			}
			if (closest != null) {
				picker.add(closest);
			}
			
			SectorEntityToken pick = picker.pick();
			Vector2f loc = pickLocationWithinArc(((BaseHubMission)context.mission).genRandom, pick, 0, 360f, minDistFromPlayer, 0f, 0f);
			
			context.jumpPoint = pick;
			context.coordinates = loc;
			context.containingLocation = system;
		}
	}
	
	/**
	 * Should not use this for player fleet since that entity can change if it respawns.
	 * @author Alex Mosolov
	 *
	 * Copyright 2020 Fractal Softworks, LLC
	 */
	public static class PickLocationAtClosestToEntityJumpPointAction implements TriggerAction {
		protected StarSystemAPI system;
		protected float minDistFromEntity;
		protected SectorEntityToken entity;
		
		public PickLocationAtClosestToEntityJumpPointAction(StarSystemAPI system, SectorEntityToken entity, float minDistFromEntity) {
			this.system = system;
			this.entity = entity;
			this.minDistFromEntity = minDistFromEntity;
		}
		
		public void doAction(TriggerActionContext context) {
			WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>(((BaseHubMission)context.mission).genRandom);
			
			SectorEntityToken closest = null;
			float minDist = Float.MAX_VALUE;
			for (SectorEntityToken jp : system.getJumpPoints()) {
				if (system.isCurrentLocation()) {
					float dist = Misc.getDistance(jp, entity);
					if (dist < minDist) {
						closest = jp;
						minDist = dist;
					}
				} else {
					picker.add(jp);
				}
			}
			if (closest != null) {
				picker.add(closest);
			}
			
			SectorEntityToken pick = picker.pick();
			Vector2f loc = pickLocationWithinArc(((BaseHubMission)context.mission).genRandom, pick, 0, 360f, minDistFromEntity, 0f, 0f);
			
			context.jumpPoint = pick;
			context.coordinates = loc;
			context.containingLocation = system;
		}
	}
	
	public static class CreateFleetAction implements TriggerAction {
		public long seed;
		
		public FleetParamsV3 params;
		public FleetSize fSize;
		public Float fSizeOverride;
		public Float combatFleetPointsOverride;
		public FleetQuality fQuality;
		public Float fQualityMod;
		public Integer fQualitySMods;
		public OfficerNum oNum;
		public OfficerQuality oQuality;
		public Boolean doNotIntegrateAICores;
		public String faction = null;
		
		public Float freighterMult = null;
		public Float tankerMult = null;
		public Float linerMult = null;
		public Float transportMult = null;
		public Float utilityMult = null;
		public Float qualityMod = null;
		
		public Boolean allWeapons;
		public ShipPickMode shipPickMode;
		public Boolean removeInflater;
		
		public String nameOverride = null;
		public Boolean noFactionInName = null;

		public CreateFleetAction(String type, Vector2f locInHyper,
				FleetSize fSize, FleetQuality fQuality, String factionId) {
			seed = Misc.genRandomSeed();
			params = new FleetParamsV3(locInHyper, factionId, null, type, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
			params.ignoreMarketFleetSizeMult = true;
			
			this.fSize = fSize;
			this.fQuality = fQuality;
			
			freighterMult = 0.1f;
			tankerMult = 0.1f;
		}

		public void doAction(TriggerActionContext context) {
			//Random random = new Random(seed);
			Random random = null;
			if (context.mission != null) {
				random = ((BaseHubMission)context.mission).genRandom; 
			} else {
				random = Misc.random;
			}
			FactionAPI faction = Global.getSector().getFaction(params.factionId);
			float maxPoints = faction.getApproximateMaxFPPerFleet(ShipPickMode.PRIORITY_THEN_ALL);
			
			//strength = FleetStrength.MAXIMUM;
			
			//float fraction = fSize.maxFPFraction * (0.9f + random.nextFloat() * 0.2f);
			float min = fSize.maxFPFraction - (fSize.maxFPFraction - fSize.prev().maxFPFraction) / 2f; 
			float max = fSize.maxFPFraction + (fSize.next().maxFPFraction - fSize.maxFPFraction) / 2f;
			float fraction = min + (max - min) * random.nextFloat();
			
			if (fSizeOverride != null) {
				fraction = fSizeOverride * (0.95f + random.nextFloat() * 0.1f);
			}
			
			int numShipsDoctrine = 1;
			if (params.doctrineOverride != null) numShipsDoctrine = params.doctrineOverride.getNumShips();
			else if (faction != null) numShipsDoctrine = faction.getDoctrine().getNumShips();
			float doctrineMult = FleetFactoryV3.getDoctrineNumShipsMult(numShipsDoctrine);
			fraction *= 0.75f * doctrineMult;
			float excess = 0;
			if (fraction > FleetSize.MAXIMUM.maxFPFraction) {
				excess = fraction - FleetSize.MAXIMUM.maxFPFraction;
				fraction = FleetSize.MAXIMUM.maxFPFraction;
			}
			
			//fraction = 1f;
			
			float combatPoints = fraction * maxPoints;
			if (combatFleetPointsOverride != null) {
				combatPoints = combatFleetPointsOverride;
			}

			FactionDoctrineAPI doctrine = params.doctrineOverride;
			if (excess > 0) {
				if (doctrine == null) {
					doctrine = faction.getDoctrine().clone();
				}
				int added = (int)Math.round(excess / 0.1f);
				if (added > 0) {
					doctrine.setOfficerQuality(Math.min(5, doctrine.getOfficerQuality() + added));
					doctrine.setShipQuality(doctrine.getShipQuality() + added);
				}
			}
//			if (fraction > 0.5f && false) {
//				if (doctrine == null) {
//					doctrine = faction.getDoctrine().clone();
//				}
//				int added = (int)Math.round((fraction - 0.5f) / 0.1f);
//				if (added > 0) {
//					doctrine.setNumShips(Math.max(1, doctrine.getNumShips() - added));
//					doctrine.setOfficerQuality(Math.min(5, doctrine.getOfficerQuality() + added));
//					doctrine.setShipQuality(doctrine.getShipQuality() + added);
//				}
//			}
			
			if (freighterMult == null) freighterMult = 0f;
			if (tankerMult == null) tankerMult = 0f;
			if (linerMult == null) linerMult = 0f;
			if (transportMult == null) transportMult = 0f;
			if (utilityMult == null) utilityMult = 0f;
			if (qualityMod == null) qualityMod = 0f;
			
			params.combatPts = combatPoints;
			params.freighterPts = combatPoints * freighterMult; 
			params.tankerPts = combatPoints * tankerMult;
			params.transportPts = combatPoints * transportMult;
			params.linerPts = combatPoints * linerMult;
			params.utilityPts = combatPoints * utilityMult;
			params.qualityMod = qualityMod;
			
			//params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL; 
			params.doctrineOverride = doctrine;
			params.random = random;
			

			if (fQuality != null) {
				switch (fQuality) {
				case VERY_LOW:
					if (fQualityMod != null) {
						params.qualityMod += fQuality.qualityMod;
					} else {
						params.qualityOverride = 0f;
					}
					break;
				case LOWER:
					params.qualityMod += fQuality.qualityMod;
					break;
				case DEFAULT:
					params.qualityMod += fQuality.qualityMod;
					break;
				case HIGHER:
					params.qualityMod += fQuality.qualityMod;
					break;
				case VERY_HIGH:
					if (fQualityMod != null) {
						params.qualityMod += fQuality.qualityMod;
					} else {
						params.qualityMod += fQuality.qualityMod;
						//params.qualityOverride = 1f;
					}
					break;
				case SMOD_1:
					params.qualityMod += fQuality.qualityMod;
					params.averageSMods = fQuality.numSMods;
					break;
				case SMOD_2:
					params.qualityMod += fQuality.qualityMod;
					params.averageSMods = fQuality.numSMods;
					break;
				case SMOD_3:
					params.qualityMod += fQuality.qualityMod;
					params.averageSMods = fQuality.numSMods;
					break;
				}
			}
			if (fQualityMod != null) {
				params.qualityMod += fQualityMod;
			}
			if (fQualitySMods != null) {
				params.averageSMods = fQualitySMods;
			}
			
			if (oNum != null) {
				switch (oNum) {
				case NONE:
					params.withOfficers = false;
					break;
				case FC_ONLY:
					params.officerNumberMult = 0f;
					break;
				case FEWER:
					params.officerNumberMult = 0.5f;
					break;
				case DEFAULT:
					break;
				case MORE:
					params.officerNumberMult = 1.5f;
					break;
				case ALL_SHIPS:
					params.officerNumberBonus = Global.getSettings().getInt("maxShipsInAIFleet");
					break;
				}
			}
			
			if (oQuality != null) {
				switch (oQuality) {
				case LOWER:
					params.officerLevelBonus = -3;
					params.officerLevelLimit = Global.getSettings().getInt("officerMaxLevel") - 1;
					params.commanderLevelLimit = Global.getSettings().getInt("maxAIFleetCommanderLevel") - 2;
					if (params.commanderLevelLimit < params.officerLevelLimit) {
						params.commanderLevelLimit = params.officerLevelLimit;
					}
					break;
				case DEFAULT:
					break;
				case HIGHER:
					params.officerLevelBonus = 2;
					params.officerLevelLimit = Global.getSettings().getInt("officerMaxLevel") + (int) OfficerTraining.MAX_LEVEL_BONUS;
					break;
				case UNUSUALLY_HIGH:
					params.officerLevelBonus = 4;
					params.officerLevelLimit = SalvageSpecialAssigner.EXCEPTIONAL_PODS_OFFICER_LEVEL;
					break;
				case AI_GAMMA:
				case AI_BETA:
				case AI_BETA_OR_GAMMA:
				case AI_ALPHA:
				case AI_MIXED:
				case AI_OMEGA:
					params.aiCores = oQuality;
					break;
				}
				if (doNotIntegrateAICores != null) {
					params.doNotIntegrateAICores = doNotIntegrateAICores;
				}
			}
			
			if (shipPickMode != null) {
				params.modeOverride = shipPickMode;
			}
			
			params.updateQualityAndProducerFromSourceMarket();
			context.fleet = FleetFactoryV3.createFleet(params);
			context.fleet.setFacing(random.nextFloat() * 360f);
			
			if (this.faction != null) {
				context.fleet.setFaction(this.faction, true);
			}
			
			if (this.nameOverride != null) {
				context.fleet.setName(this.nameOverride);
			}
			if (this.noFactionInName != null && this.noFactionInName) {
				context.fleet.setNoFactionInName(noFactionInName);
			}
			
			if (removeInflater != null && removeInflater) {
				context.fleet.setInflater(null);
			} else {
				if (context.fleet.getInflater() instanceof DefaultFleetInflater) {
					DefaultFleetInflater inflater = (DefaultFleetInflater) context.fleet.getInflater();
					if (inflater.getParams() instanceof DefaultFleetInflaterParams) {
						DefaultFleetInflaterParams p = (DefaultFleetInflaterParams) inflater.getParams();
						if (allWeapons != null) {
							p.allWeapons = allWeapons;
						}
						if (shipPickMode != null) {
							p.mode = shipPickMode;
						}
					}
				}
			}
			
			context.fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_BUSY, true);
			//context.fleet.getMemoryWithoutUpdate().set("$LP_titheAskedFor", true);
			
			context.allFleets.add(context.fleet);
			
			if (!context.fleet.hasScriptOfClass(MissionFleetAutoDespawn.class)) {
				context.fleet.addScript(new MissionFleetAutoDespawn(context.mission, context.fleet));
			}
			
//			if (Factions.PIRATES.equals(params.factionId)) {
//				
//			}
		}
	}
	
	
//	public TriggerAction getPreviousAction() {
//		if (currTrigger.getActions().isEmpty()) return null;
//		return currTrigger.getActions().get(currTrigger.getActions().size() - 1);
//	}
	public CreateFleetAction getPreviousCreateFleetAction() {
		for (int i = currTrigger.getActions().size() - 1; i >= 0; i--) {
			TriggerAction action = currTrigger.getActions().get(i);
			if (action instanceof CreateFleetAction) {
				return (CreateFleetAction) action;
			}
		}
		return null;
	}

	public void triggerMovePersonToMarket(PersonAPI person, MarketAPI market, boolean alwaysAddToComms) {
		triggerCustomAction(new MovePersonToMarket(person, market, alwaysAddToComms));
	}
	public void triggerIncreaseMarketHostileTimeout(MarketAPI market, float days) {
		triggerCustomAction(new IncreaseMarketHostileTimeout(market, days));
	}
	public void triggerRunScriptAfterDelay(float delay, Script script) {
		triggerCustomAction(new RunScriptAfterDelay(delay, script));
	}
	public void triggerAddTagAfterDelay(float delay, StarSystemAPI system, String tag) {
		triggerCustomAction(new AddTagAfterDelay(delay, system, tag));
	}
	public void triggerSetMemoryValueAfterDelay(float delay, HasMemory hasMemory, String key, Object value) {
		triggerSetMemoryValueAfterDelay(delay, hasMemory.getMemory(), key, value);
	}
	public void triggerSetMemoryValueAfterDelay(float delay, MemoryAPI memory, String key, Object value) {
		triggerCustomAction(new SetMemoryValueAfterDelay(delay, memory, key, value));
	}
	public void triggerSetGlobalMemoryValueAfterDelay(float delay, String key, Object value) {
		triggerCustomAction(new SetMemoryValueAfterDelay(delay, Global.getSector().getMemory(), key, value));
	}
	public float genDelay(float base) {
		return base * StarSystemGenerator.getNormalRandom(genRandom, 0.75f, 1.25f);
	}
	
	public void triggerUnhideCommListing(PersonAPI person) {
		triggerCustomAction(new UnhideCommListing(person));
	}
	public void triggerHideCommListing(PersonAPI person) {
		triggerCustomAction(new HideCommListing(person));
	}
	public void triggerSaveGlobalEntityRef(String key) {
		triggerSaveEntityRef(Global.getSector().getMemoryWithoutUpdate(), key);
	}
	public void triggerSaveEntityRef(MemoryAPI memory, String key) {
		triggerCustomAction(new SaveEntityReferenceAction(memory, key));
	}
	public void triggerSaveGlobalFleetRef(String key) {
		triggerSaveFleetRef(Global.getSector().getMemoryWithoutUpdate(), key);
	}
	public void triggerSaveFleetRef(MemoryAPI memory, String key) {
		triggerCustomAction(new SaveFleetReferenceAction(memory, key));
	}
	
	public SectorEntityToken getEntityFromGlobal(String key) {
		return Global.getSector().getMemoryWithoutUpdate().getEntity(key);
	}
	
	public void triggerCreateFleet(FleetSize size, FleetQuality quality, String factionId, String type, StarSystemAPI roughlyWhere) {
		triggerCustomAction(new CreateFleetAction(type, roughlyWhere.getLocation(), size, quality, factionId));
	}
	public void triggerCreateFleet(FleetSize size, FleetQuality quality, String factionId, String type, SectorEntityToken roughlyWhere) {
		triggerCustomAction(new CreateFleetAction(type, roughlyWhere.getLocationInHyperspace(), size, quality, factionId));
	}
	public void triggerCreateFleet(FleetSize size, FleetQuality quality, String factionId, String type, Vector2f locInHyper) {
		triggerCustomAction(new CreateFleetAction(type, locInHyper, size, quality, factionId));
	}
	
	public void triggerAutoAdjustFleetSize(FleetSize min, FleetSize max) {
		float f = getQualityFraction();
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.fSizeOverride = min.maxFPFraction + (max.maxFPFraction - min.maxFPFraction) * f;
		
		autoAdjustFleetTypeName();
	}
	
	public void triggerSetFleetSizeFraction(float fractionOfMax) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.fSizeOverride = fractionOfMax;
	}
	public void triggerSetFleetCombatFleetPoints(float combatFleetPointsOverride) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.combatFleetPointsOverride = combatFleetPointsOverride;
	}
	public void triggerAutoAdjustFleetQuality(FleetQuality min, FleetQuality max) {
		float f = getQualityFraction();
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.fQualityMod = min.qualityMod + (max.qualityMod - min.qualityMod) * f;
		cfa.fQualitySMods = (int) Math.round(min.numSMods + (max.numSMods - min.numSMods) * f);
		if (cfa.fQualitySMods <= 0) {
			cfa.fQualitySMods = null;
		}
	}
	
	public void triggerAutoAdjustOfficerNum(OfficerNum min, OfficerNum max) {
		float f = getQualityFraction();
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.oNum = (OfficerNum) pickEnum(f, getEnums(min, max));
	}
	public void triggerAutoAdjustOfficerQuality(OfficerQuality min, OfficerQuality max) {
		float f = getQualityFraction();
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.oQuality = (OfficerQuality) pickEnum(f, getEnums(min, max));
	}
	
	public void triggerRandomizeFleetStrengthMinor() {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		FleetSize size = cfa.fSize;
		if (size == null) size = FleetSize.MEDIUM;
		
		float min = size.maxFPFraction - (size.maxFPFraction - size.prev().maxFPFraction) / 2f; 
		float max = size.maxFPFraction + (size.next().maxFPFraction - size.maxFPFraction) / 2f;
		cfa.fSizeOverride = min + (max - min) * genRandom.nextFloat();
		//triggerAutoAdjustFleetSize(size.prev(), size.next());
		
		FleetQuality fq = cfa.fQuality;
		if (fq == null) fq = FleetQuality.DEFAULT;
		min = fq.qualityMod - (fq.qualityMod - fq.prev().qualityMod) / 2f; 
		max = fq.qualityMod + (fq.next().qualityMod - fq.qualityMod) / 2f;
		cfa.fQualityMod = min + (max - min) * genRandom.nextFloat();
		//triggerAutoAdjustFleetQuality(fq.prev(), fq.next());
	}
	
	public void triggerAutoAdjustFleetStrengthModerate() {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		FleetSize size = cfa.fSize;
		if (size == null) size = FleetSize.MEDIUM;
		
		triggerAutoAdjustFleetSize(size.prev(), size.next());
		
		FleetQuality fq = cfa.fQuality;
		if (fq == null) fq = FleetQuality.DEFAULT;
		FleetQuality limit = FleetQuality.VERY_HIGH;
		FleetQuality next = fq;
		int steps = 1;
		while (next.next().ordinal() <= limit.ordinal() && steps > 0) {
			next = next.next();
			steps--;
		}
		limit = FleetQuality.LOWER;
		FleetQuality prev = fq;
		steps = 1;
		while (prev.prev().ordinal() >= limit.ordinal() && steps > 0) {
			prev = prev.prev();
			steps--;
		}
		triggerAutoAdjustFleetQuality(prev, next);
		
		
		OfficerNum oNum = cfa.oNum;
		if (oNum == null) oNum = OfficerNum.DEFAULT;
		if (oNum == OfficerNum.FEWER || oNum == OfficerNum.DEFAULT) {
			switch (oNum) {
			case FEWER:
				triggerAutoAdjustOfficerNum(OfficerNum.FEWER, OfficerNum.DEFAULT);
				break;
			case DEFAULT:
				triggerAutoAdjustOfficerNum(OfficerNum.DEFAULT, OfficerNum.MORE);
				break;
			}
		}
		
		OfficerQuality oQuality = cfa.oQuality;
		if (oQuality == null) oQuality = OfficerQuality.DEFAULT;
		if (oQuality == OfficerQuality.LOWER || oQuality == OfficerQuality.DEFAULT) {
			switch (oQuality) {
			case LOWER:
				triggerAutoAdjustOfficerQuality(OfficerQuality.LOWER, OfficerQuality.DEFAULT);
				break;
			case DEFAULT:
				triggerAutoAdjustOfficerQuality(OfficerQuality.DEFAULT, OfficerQuality.HIGHER);
				break;
			}
		}
	}
	
	public void triggerAutoAdjustFleetStrengthMajor() {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		FleetSize size = cfa.fSize;
		if (size == null) size = FleetSize.MEDIUM;
		
		triggerAutoAdjustFleetSize(size.prev().prev(), size.next().next());
		
		FleetQuality fq = cfa.fQuality;
		if (fq == null) fq = FleetQuality.DEFAULT;
		FleetQuality limit = FleetQuality.VERY_HIGH;
		FleetQuality next = fq;
		int steps = 2;
		while (next.next().ordinal() <= limit.ordinal() && steps > 0) {
			next = next.next();
			steps--;
		}
		limit = FleetQuality.LOWER;
		FleetQuality prev = fq;
		steps = 2;
		while (prev.prev().ordinal() >= limit.ordinal() && steps > 0) {
			prev = prev.prev();
			steps--;
		}
		triggerAutoAdjustFleetQuality(prev, next);
		
		
		OfficerNum oNum = cfa.oNum;
		if (oNum == null) oNum = OfficerNum.DEFAULT;
		if (oNum == OfficerNum.FEWER || oNum == OfficerNum.DEFAULT) {
			switch (oNum) {
			case FEWER:
				triggerAutoAdjustOfficerNum(OfficerNum.FEWER, OfficerNum.DEFAULT);
				break;
			case DEFAULT:
				triggerAutoAdjustOfficerNum(OfficerNum.DEFAULT, OfficerNum.MORE);
				break;
			}
		}
		
		OfficerQuality oQuality = cfa.oQuality;
		if (oQuality == null) oQuality = OfficerQuality.DEFAULT;
		if (oQuality == OfficerQuality.LOWER || oQuality == OfficerQuality.DEFAULT) {
			switch (oQuality) {
			case LOWER:
				triggerAutoAdjustOfficerQuality(OfficerQuality.LOWER, OfficerQuality.DEFAULT);
				break;
			case DEFAULT:
				triggerAutoAdjustOfficerQuality(OfficerQuality.DEFAULT, OfficerQuality.HIGHER);
				break;
			}
		}
	}
	
	public void triggerAutoAdjustFleetStrengthExtreme() {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		FleetSize size = cfa.fSize;
		if (size == null) size = FleetSize.MEDIUM;
		
		triggerAutoAdjustFleetSize(size.prev().prev(), size.next().next().next());
		
		FleetQuality fq = cfa.fQuality;
		if (fq == null) fq = FleetQuality.DEFAULT;
		FleetQuality limit = FleetQuality.SMOD_1;
		FleetQuality next = fq;
		int steps = 3;
		while (next.next().ordinal() <= limit.ordinal() && steps > 0) {
			next = next.next();
			steps--;
		}
		limit = FleetQuality.LOWER;
		FleetQuality prev = fq;
		steps = 2;
		while (prev.prev().ordinal() >= limit.ordinal() && steps > 0) {
			prev = prev.prev();
			steps--;
		}
		triggerAutoAdjustFleetQuality(prev, next);
		
		
		OfficerNum oNum = cfa.oNum;
		if (oNum == null) oNum = OfficerNum.DEFAULT;
		if (oNum == OfficerNum.FEWER || oNum == OfficerNum.DEFAULT || oNum == OfficerNum.MORE) {
			switch (oNum) {
			case FEWER:
				triggerAutoAdjustOfficerNum(OfficerNum.FEWER, OfficerNum.MORE);
				break;
			case DEFAULT:
				triggerAutoAdjustOfficerNum(OfficerNum.FEWER, OfficerNum.ALL_SHIPS);
				break;
			case MORE:
				triggerAutoAdjustOfficerNum(OfficerNum.DEFAULT, OfficerNum.ALL_SHIPS);
				break;
			}
		}
		
		OfficerQuality oQuality = cfa.oQuality;
		if (oQuality == null) oQuality = OfficerQuality.DEFAULT;
		if (oQuality == OfficerQuality.LOWER || oQuality == OfficerQuality.DEFAULT) {
			switch (oQuality) {
			case LOWER:
				triggerAutoAdjustOfficerQuality(OfficerQuality.LOWER, OfficerQuality.DEFAULT);
				break;
			case DEFAULT:
				triggerAutoAdjustOfficerQuality(OfficerQuality.DEFAULT, OfficerQuality.HIGHER);
				break;
			}
		}
	}
	
	
	protected transient boolean useQualityInsteadOfQualityFraction = false;
	/**
	 * Set to true when methods that auto-adjust fleet strength should do so based on the mission quality
	 * rather than the mission qualityFactor - i.e. absolute mission quality rather than where it is within
	 * the range of possible qualities given the giver's importance and your relationship level with them.
	 * @param temporarilyUseQualityInsteadOfQualityFraction
	 */
	public void setUseQualityInsteadOfQualityFraction(boolean temporarilyUseQualityInsteadOfQualityFraction) {
		this.useQualityInsteadOfQualityFraction = temporarilyUseQualityInsteadOfQualityFraction;
	}

	/**
	 * Where the current quality is relative to min and max quality for this mission giver.
	 * @return
	 */
	protected float getQualityFraction() {
		float quality = getQuality();
		if (useQualityInsteadOfQualityFraction) {
			return quality;
		}
		
		float minQuality = getMinQuality();
		float maxQuality = getMaxQuality();
		float base = getBaseQuality();
		
		float f;
		if (quality < base) {
			float range = base - minQuality;
			f = 0f;
			if (range > 0) {
				f = (quality - minQuality) / range;
			}
			if (f < 0) f = 0;
			if (f > 1) f = 1;
			f = f * 0.5f;
		} else {
			float range = maxQuality - base;
			f = 1f;
			if (range > 0) {
				f = (quality - base) / range;
			}
			if (f < 0) f = 0;
			if (f > 1) f = 1;
			f = 0.5f + f * 0.5f;
		}
		return f;
	}
	
	@SuppressWarnings("unchecked")
	protected Object [] getEnums(Enum from, Enum to) {
		return EnumSet.range(from, to).toArray();
	}
	
	protected Object pickEnum(float f, Object ... enums) {
		float num = enums.length;
		f *= (num - 1f);
		
		float rem = (float)(f - (int) f);
		if (rem < 0.2f) rem = 0f;
		if (rem > 0.8f) rem = 1f;
		
		int index = (int) f;
		if (genRandom.nextFloat() < rem) {
			index++;
		} 
		if (index > enums.length - 1) index = enums.length - 1;
		if (index < 0) index = 0;
		return enums[index];
	}

//	defaults to this
//	public void triggerSetFleetCompositionStandardSupportShips() {
//		triggerSetFleetComposition(0.1f, 0.1f, 0f, 0f, 0f, 0f);
//	}
	
	public void triggerSetFleetCompositionNoSupportShips() {
		triggerSetFleetComposition(0f, 0f, 0f, 0f, 0f);
	}
			
	public void triggerSetFleetComposition(float freighterMult, float tankerMult,
										   float transportMult, float linerMult,
										   float utilityMult) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		if (freighterMult > 0) cfa.freighterMult = freighterMult;
		else cfa.freighterMult = null;
		
		if (tankerMult > 0) cfa.tankerMult = tankerMult;
		else cfa.tankerMult = null;
		
		if (transportMult > 0) cfa.transportMult = transportMult;
		else cfa.transportMult = null;
		
		if (linerMult > 0) cfa.linerMult = linerMult;
		else cfa.linerMult = null;
		
		if (utilityMult > 0) cfa.utilityMult = utilityMult;
		else cfa.utilityMult = null;
	}
	
	public void triggerSetFleetDoctrineComp(int warships, int carriers, int phaseShips) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		
		if (cfa.params.doctrineOverride == null) {
			FactionAPI faction = Global.getSector().getFaction(cfa.params.factionId);
			cfa.params.doctrineOverride = faction.getDoctrine().clone();
		}
		
		cfa.params.doctrineOverride.setWarships(warships);
		cfa.params.doctrineOverride.setCarriers(carriers);
		cfa.params.doctrineOverride.setPhaseShips(phaseShips);
	}
	
	public void triggerSetFleetProbabilityCombatFreighters(float prob) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		if (cfa.params.doctrineOverride == null) {
			FactionAPI faction = Global.getSector().getFaction(cfa.params.factionId);
			cfa.params.doctrineOverride = faction.getDoctrine().clone();
		}
		
		cfa.params.doctrineOverride.setCombatFreighterProbability(prob);
	}
	
	public void triggerSetFleetDoctrineQuality(int officerQuality, int shipQuality, int numShips) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		
		if (cfa.params.doctrineOverride == null) {
			FactionAPI faction = Global.getSector().getFaction(cfa.params.factionId);
			cfa.params.doctrineOverride = faction.getDoctrine().clone();
		}
		
		if (officerQuality >= 0) {
			cfa.params.doctrineOverride.setOfficerQuality(officerQuality);
		}
		
		if (shipQuality >= 0) {
			cfa.params.doctrineOverride.setShipQuality(shipQuality);
		}
		
		if (numShips >= 0) {
			cfa.params.doctrineOverride.setNumShips(numShips);
		}
	}
	
	public void triggerSetFleetDoctrineOther(int shipSize, int aggression) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		
		if (cfa.params.doctrineOverride == null) {
			FactionAPI faction = Global.getSector().getFaction(cfa.params.factionId);
			cfa.params.doctrineOverride = faction.getDoctrine().clone();
		}
		
		if (shipSize >= 0) {
			cfa.params.doctrineOverride.setShipSize(shipSize);
		}
		if (aggression >= 0) {
			cfa.params.doctrineOverride.setAggression(aggression);
		}
	}
	
	public void triggerSetFleetDoctrineRandomize(float randomizeProb) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		
		if (cfa.params.doctrineOverride == null) {
			FactionAPI faction = Global.getSector().getFaction(cfa.params.factionId);
			cfa.params.doctrineOverride = faction.getDoctrine().clone();
		}
		
		cfa.params.doctrineOverride.setAutofitRandomizeProbability(randomizeProb);
	}
	
	public void triggerSetFleetSizeAndQuality(FleetSize size, FleetQuality quality, String fleetType) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.fSize = size;
		cfa.fQuality = quality;
		cfa.params.fleetType = fleetType;
		cfa.fSizeOverride = null;
	}
	public void triggerSetFleetOfficers(OfficerNum num, OfficerQuality quality) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.oNum = num;
		cfa.oQuality = quality;
	}
	
	public void triggerFleetSetCommander(PersonAPI commander) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.params.commander = commander;
	}
	
	public void triggerSetFleetNoCommanderSkills() {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.params.noCommanderSkills = true;
	}
	
	public void triggerSetFleetMaxShipSize(int max) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.params.maxShipSize = max;
	}
	public void triggerSetFleetMinShipSize(int min) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.params.minShipSize = min;
	}
	public void triggerSetFleetMaxNumShips(int num) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.params.maxNumShips = num;
	}
	
	public void triggerFleetSetSingleShipOnly() {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.params.onlyRetainFlagship = true;
	}
	public void triggerFleetSetFlagship(String variantId) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.params.flagshipVariantId = variantId;
	}
	
	public void triggerFleetSetFlagship(ShipVariantAPI variant) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.params.flagshipVariant = variant;
	}
	
	public void triggerFleetRemoveInflater() {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.removeInflater = true;
	}
	
	public void triggerFleetSetShipPickMode(ShipPickMode mode) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.shipPickMode = mode;
	}
	
	public void triggerFleetSetAllWeapons() {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.allWeapons = true;
	}
	
//	public void triggerFleetSetFaction(String factionId) {
//		CreateFleetAction cfa = getPreviousCreateFleetAction();
//		cfa.faction = factionId;
//	}
	public void triggerSetFleetFaction(final String factionId) {
//		triggerCustomAction(new SetFleetFactionAction(factionId));
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.faction = factionId;
	}
	
	public void triggerFleetSetName(String name) {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.nameOverride = name;
	}
	public void triggerFleetSetNoFactionInName() {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.noFactionInName = true;
	}
	public void triggerFleetDoNotIntegrateAICores() {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		cfa.doNotIntegrateAICores = true;
	}
	
	public FleetParamsV3 triggerGetFleetParams() {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		return cfa.params;
	}
	
	
	public void triggerSetFleetCommander(final PersonAPI commander) {
		triggerCustomAction(new TriggerAction() {
			public void doAction(TriggerActionContext context) {
				context.fleet.setCommander(commander);
				context.fleet.getFleetData().ensureHasFlagship();
			}
		});
	}
// commander will get overriden by default inflater so uh
// will it? doesn't seem like it would, looking at DFI
//	public void triggerSetFleetCommanderFlags(final String ... flags) {
//		triggerCustomAction(new TriggerAction() {
//			public void doAction(TriggerActionContext context) {
//				for (String flag : flags) {
//					context.fleet.getCommander().getMemoryWithoutUpdate().set(flag, true);
//				}
//			}
//		});
//	}
	
	public void triggerFleetMakeImportantPermanent(String flag) {
		triggerCustomAction(new FleetMakeImportantAction(flag, (Enum[]) null));
	}
	public void triggerFleetMakeImportant(String flag, Enum ... stages) {
		triggerCustomAction(new FleetMakeImportantAction(flag, stages));
	}
	public void triggerEntityMakeImportant(String flag, Enum ... stages) {
		triggerCustomAction(new EntityMakeImportantAction(flag, stages));
	}
	
	public void triggerSetFleetFlagsWithReasonPermanent(final String ... flags) {
		triggerCustomAction(new SetFleetFlagsWithReasonAction(getReason(), true, flags));
	}
	public void triggerSetFleetFlagsWithReason(final String ... flags) {
		triggerCustomAction(new SetFleetFlagsWithReasonAction(getReason(), false, flags));
	}
	
//	public void setFleetFlagsWithReason(final String ... flags) {
//		new SetFleetFlagsWithReasonAction(getReason(), false, flags).doAction(null);
//	}
//	
//	public void setFleetFlag(String flag) {
//		new SetFleetFlagAction(flag, false, (Object[])null).doAction(null);;
//	}
	
	public void triggerUnsetFleetFlagsWithReason(final String ... flags) {
		triggerCustomAction(new UnsetFleetFlagsWithReasonAction(getReason(), flags));
	}
	public void triggerSetPersonMissionRef(final String key) {
		triggerCustomAction(new SetPersonMissionRefAction(key));
	}
	public void triggerSetFleetMissionRef(final String key) {
		triggerCustomAction(new SetFleetMissionRefAction(key));
	}
	
	public void triggerSetFleetMemoryValue(final String key, final Object value) {
		triggerCustomAction(new SetFleetMemoryValueAction(key, value));
	}
	
	public void triggerSetMemoryValue(HasMemory withMemory, String key, Object value) {
		triggerCustomAction(new SetMemoryValueAction(withMemory.getMemoryWithoutUpdate(), key, value, true));
	}
	public void triggerSetMemoryValuePermanent(HasMemory withMemory, String key, Object value) {
		triggerCustomAction(new SetMemoryValueAction(withMemory.getMemoryWithoutUpdate(), key, value, false));
	}
	
	public void triggerSetGlobalMemoryValue(final String key, final Object value) {
		triggerCustomAction(new SetMemoryValueAction(Global.getSector().getMemoryWithoutUpdate(), key, value, true));
	}
	public void triggerSetGlobalMemoryValuePermanent(final String key, final Object value) {
		triggerCustomAction(new SetMemoryValueAction(Global.getSector().getMemoryWithoutUpdate(), key, value, false));
	}
	
	public void triggerSetFleetFlagPermanent(String flag) {
		triggerCustomAction(new SetFleetFlagAction(flag, true, (Object[])null));
	}
	public void triggerSetFleetGenericHailPermanent(String commsTrigger) {
		triggerSetFleetGenericHail(commsTrigger, (Object[])null);
	}
	public void triggerSetFleetGenericHail(String commsTrigger, Object ...stages) {
		if (stages == null || stages.length <= 0) {
			triggerSetFleetFlagPermanent("$genericHail");
		} else {
			triggerSetFleetFlag("$genericHail", stages);
		}
		triggerSetFleetMemoryValue("$genericHail_openComms", commsTrigger);
	}
	public void triggerSetFleetGenericHailIfNonHostilePermanent(String commsTrigger) {
		triggerSetFleetGenericHail(commsTrigger, (Object[])null);
	}
	public void triggerSetFleetGenericHailIfNonHostile(String commsTrigger, Object ...stages) {
		if (stages == null || stages.length <= 0) {
			triggerSetFleetFlagPermanent("$genericHail_nonHostile");
		} else {
			triggerSetFleetFlag("$genericHail_nonHostile", stages);
		}
		triggerSetFleetMemoryValue("$genericHail_openComms", commsTrigger);
	}
	public void triggerSetFleetFlag(String flag) {
		triggerCustomAction(new SetFleetFlagAction(flag, false, (Object[])null));
	}
	public void triggerSetEntityFlagPermanent(String flag) {
		triggerCustomAction(new SetEntityFlagAction(flag, true, (Object[])null));
	}
	public void triggerSetEntityFlag(String flag) {
		triggerCustomAction(new SetEntityFlagAction(flag, false, (Object[])null));
	}
	public void triggerSetFleetFlagPermanent(String flag, Object ... stages) {
		triggerCustomAction(new SetFleetFlagAction(flag, true, stages));
	}
	public void triggerSetFleetFlag(String flag, Object ... stages) {
		triggerCustomAction(new SetFleetFlagAction(flag, false, stages));
	}
	public void triggerUnsetFleetFlag(String flag) {
		triggerCustomAction(new UnsetFleetFlagsAction(flag));
	}
	public void triggerSetEntityFlagPermanent(String flag, Object ... stages) {
		triggerCustomAction(new SetEntityFlagAction(flag, true, stages));
	}
	public void triggerSetEntityFlag(String flag, Object ... stages) {
		triggerCustomAction(new SetEntityFlagAction(flag, false, stages));
	}
	public void triggerUnsetEntityFlag(String flag) {
		triggerCustomAction(new UnsetEntityFlagsAction(flag));
	}
//	public void triggerMakeHostileAndAggressiveNotPermanent(Object ... stages) {
//		triggerSetFleetFlagsWithReason(MemFlags.MEMORY_KEY_MAKE_HOSTILE, 
//				 					   MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE);
//		triggerSetFleetFlag(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE_ONE_BATTLE_ONLY);
//	}
	public void triggerMakeHostileAndAggressive() {
		triggerSetFleetFlagsWithReason(MemFlags.MEMORY_KEY_MAKE_HOSTILE, 
							 //MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF,
							 MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE);
		triggerSetFleetFlag(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE_ONE_BATTLE_ONLY);
	}
	
	public void triggerMakeFleetIgnoreOtherFleetsExceptPlayer() {
		triggerMakeFleetIgnoreOtherFleets();
		triggerMakeFleetNotIgnorePlayer();
	}
	public void triggerMakeFleetNotIgnorePlayer() {
		triggerSetFleetFlag(MemFlags.FLEET_DO_NOT_IGNORE_PLAYER);
	}
	public void triggerMakeFleetIgnoreOtherFleets() {
		triggerSetFleetFlag(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
	}
	public void triggerMakeFleetIgnoredByOtherFleets() {
		triggerSetFleetFlag(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
	}
	public void triggerMakeFleetAllowDisengage() {
		triggerSetFleetFlag(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE);
	}
	
	public void makeHostileAndAggressive(CampaignFleetAPI fleet, boolean permanent) {
		setFlagWithReason(fleet, MemFlags.MEMORY_KEY_MAKE_HOSTILE, permanent);
		setFlagWithReason(fleet, MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, permanent);
		setFlag(fleet, MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE_ONE_BATTLE_ONLY, permanent);
	}
	
	public void triggerMakeNonHostile() {
		triggerSetFleetFlagsWithReason(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE);
	}
	public void triggerMakeHostile() {
		triggerSetFleetFlagsWithReason(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
	}
	public void triggerMakeHostileWhileTransponderOff() {
//		triggerSetFleetFlagsWithReason(MemFlags.MEMORY_KEY_MAKE_HOSTILE,
//							 MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF);
		triggerSetFleetFlagsWithReason(MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF);
	}
	public void triggerMakeLowRepImpact() {
		triggerSetFleetFlagsWithReason(MemFlags.MEMORY_KEY_LOW_REP_IMPACT);
	}
	public void triggerMakeNoRepImpact() {
		triggerSetFleetFlagsWithReason(MemFlags.MEMORY_KEY_LOW_REP_IMPACT,
									   MemFlags.MEMORY_KEY_NO_REP_IMPACT);
	}
	public void triggerPatrolAllowTransponderOff() {
		triggerSetFleetFlag(MemFlags.MEMORY_KEY_PATROL_ALLOW_TOFF);
	}
	public void triggerDoNotShowFleetDesc() {
		triggerSetFleetFlagPermanent(MemFlags.MEMORY_KEY_DO_NOT_SHOW_FLEET_DESC);
	}
	
	public void triggerFleetForceAutofitOnAllShips() {
		triggerSetFleetFlag(MemFlags.MEMORY_KEY_FORCE_AUTOFIT_ON_NO_AUTOFIT_SHIPS);
	}
	
	public void triggerFleetOnlyEngageableWhenVisibleToPlayer() {
		triggerSetFleetFlag(MemFlags.CAN_ONLY_BE_ENGAGED_WHEN_VISIBLE_TO_PLAYER);
	}
	
	public void triggerFleetNoJump() {
		triggerSetFleetFlagPermanent(MemFlags.MEMORY_KEY_NO_JUMP);
	}
	public void triggerFleetAllowJump() {
		triggerUnsetFleetFlag(MemFlags.MEMORY_KEY_NO_JUMP);
	}
	
	/**
	 * Fleet will respond to WarSimScript orders and get distracted by false sensor readings from a sensor array, etc. 
	 */
	public void triggerSetFleetNotBusy() {
		triggerUnsetFleetFlag(MemFlags.FLEET_BUSY);
	}
	
	public void triggerSetPatrol() {
		triggerSetFleetFlagPermanent(MemFlags.MEMORY_KEY_PATROL_FLEET);
	}
	public void triggerSetFleetExtraSmugglingSuspicion(float extraSuspicion) {
		triggerSetFleetMemoryValue(MemFlags.PATROL_EXTRA_SUSPICION, extraSuspicion);
	}
	public void triggerSetPirateFleet() {
		triggerSetFleetFlagPermanent(MemFlags.MEMORY_KEY_PIRATE);
	}
	public void triggerSetTraderFleet() {
		triggerSetFleetFlagPermanent(MemFlags.MEMORY_KEY_TRADE_FLEET);
	}
	public void triggerSetWarFleet() {
		triggerSetFleetFlagPermanent(MemFlags.MEMORY_KEY_WAR_FLEET);
	}
	public void triggerSetSmugglerFleet() {
		triggerSetFleetFlagPermanent(MemFlags.MEMORY_KEY_SMUGGLER);
	}
	public void triggerFleetAllowLongPursuit() {
		triggerSetFleetFlagPermanent(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT);
	}
	public void triggerFleetPatherAllowTithe() {
		triggerUnsetFleetFlag("$LP_titheAskedFor");
	}
	public void triggerFleetPatherNoDefaultTithe() {
		triggerSetFleetFlagPermanent("$LP_titheAskedFor");
	}
//	public void triggerFleetAllowLongPursuitNotPermanent() {
//		triggerSetFleetFlag(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT);
//	}
	public void triggerFleetUnsetAllowLongPursuit() {
		triggerUnsetFleetFlag(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT);
	}
	public void triggerFleetSetAvoidPlayerSlowly() {
		triggerSetFleetFlag(MemFlags.MEMORY_KEY_AVOID_PLAYER_SLOWLY);
	}
	public void triggerUnsetAvoidPlayerSlowly() {
		triggerUnsetFleetFlag(MemFlags.MEMORY_KEY_AVOID_PLAYER_SLOWLY);
	}
	
	public void triggerSetFleetAlwaysPursue() {
		triggerSetFleetFlag(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE);
	}
//	public void triggerSetFleetAlwaysPursueNotPermanent() {
//		triggerSetFleetFlag(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE);
//	}
	public void triggerUnsetFleetAlwaysPursue() {
		triggerUnsetFleetFlag(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE);
	}
	
	public void triggerSetStandardHostilePirateFlags() {
		triggerSetPirateFleet();
		triggerMakeHostile();
		triggerMakeLowRepImpact();
	}
	public void triggerSetStandardHostileNonPirateFlags() {
		triggerMakeHostile();
		triggerMakeLowRepImpact();
	}
	public void triggerSetStandardAggroPirateFlags() {
		triggerSetPirateFleet();
		triggerMakeHostileAndAggressive();
		triggerMakeLowRepImpact();
	}
	public void triggerSetStandardAggroNonPirateFlags() {
		triggerMakeHostileAndAggressive();
		triggerMakeLowRepImpact();
	}
	public void triggerRemoveAbilities(final String ... abilities) {
		triggerCustomAction(new RemoveAbilitiesAction(abilities));
	}	
	public void triggerAddAbilities(final String ... abilities) {
		triggerCustomAction(new AddAbilitiesAction(abilities));
	}
	public void triggerSetInflater(final FleetInflater inflater) {
		triggerCustomAction(new SetInflaterAction(inflater));
	}
	public void triggerSetRemnantConfig() {
		triggerSetRemnantConfig(false);
	}
	public void triggerSetRemnantConfigDormant() {
		triggerSetRemnantConfig(true);
	}
	public void triggerSetRemnantConfig(boolean dormant) {
		//final long seed = Misc.genRandomSeed();
		long seed = Misc.seedUniquifier() ^ genRandom.nextLong();
		triggerCustomAction(new SetRemnantConfigAction(dormant, seed));
	}
	
	public void triggerSetRemnantConfigActive() {
		triggerSetRemnantConfig();
		triggerAddAbilities(Abilities.EMERGENCY_BURN);
		triggerAddAbilities(Abilities.SENSOR_BURST);
		triggerAddAbilities(Abilities.GO_DARK);
		triggerFleetAllowJump();
	}
	
	public void triggerAddCustomDrop(final CargoAPI cargo) {
		triggerCustomAction(new AddCustomDropAction(cargo));
	}
	public void triggerAddCommodityDrop(String commodityId, int quantity, boolean dropQuantityBasedOnShipsDestroyed) {
		triggerCustomAction(new AddCommodityDropAction(quantity, commodityId, dropQuantityBasedOnShipsDestroyed));
	}
	public void triggerAddCommodityFractionDrop(String commodityId, float fraction) {
		triggerCustomAction(new AddCommodityFractionDropAction(fraction, commodityId));
	}
	public void triggerAddWeaponDrop(final String weaponId, final int quantity) {
		triggerCustomAction(new AddWeaponDropAction(quantity, weaponId));
	}
	public void triggerAddFighterLPCDrop(final String wingId, final int quantity) {
		triggerCustomAction(new AddFighterLPCDropAction(wingId, quantity));
	}
	public void triggerAddHullmodDrop(final String hullmodId) {
		triggerCustomAction(new AddHullmodDropAction(hullmodId));
	}
	public void triggerAddSpecialItemDrop(final String itemId, final String data) {
		triggerCustomAction(new AddSpecialItemDropAction(data, itemId));
	}
	
	
	public void triggerSpawnFleetAtPickedLocation() {
		triggerSpawnFleetAtPickedLocation(null, null);
	}
	/**
	 * refKey could be needed if there's no global mission reference set.
	 * @param flag
	 * @param refKey
	 */
	public void triggerSpawnFleetAtPickedLocation(final String flag, final String refKey) {
		triggerSpawnFleetAtPickedLocation(200f, flag, refKey);
	}
	public void triggerSpawnFleetAtPickedLocation(final float range, final String flag, final String refKey) {
		triggerCustomAction(new SpawnFleetAtPickedLocationAction(range));
		if (flag != null) {
			triggerSetFleetFlag(flag);
		}
		if (refKey != null) {
			triggerSetFleetMissionRef(refKey);
		}
	}
	public void triggerSpawnFleetNear(final SectorEntityToken entity, final String flag, final String refKey) {
		triggerSpawnFleetNear(entity, 200f, flag, refKey);
	}
	public void triggerSpawnFleetNear(final SectorEntityToken entity, final float range, final String flag, final String refKey) {
		triggerCustomAction(new SpawnFleetNearAction(entity, range));
		if (flag != null) {
			triggerSetFleetFlag(flag);
		}
		if (refKey != null) {
			triggerSetFleetMissionRef(refKey);
		}
	}
	
	public void triggerPickSetLocation(final LocationAPI location, final Vector2f coordinates) {
		triggerCustomAction(new PickSetLocationAction(coordinates, location));
	}
	public void triggerPickLocationInHyperspace(final StarSystemAPI system) {
		triggerCustomAction(new PickLocationInHyperspaceAction(system));
	}
	
	public void triggerPickLocationFromEntityTowardsPlayer(final float arc, final float dist) {
		triggerPickLocationTowardsPlayer(null, arc, DEFAULT_MIN_DIST_FROM_PLAYER, dist, dist);
	}
	public void triggerPickLocationTowardsPlayer(final SectorEntityToken entity, final float arc, final float dist) {
		triggerPickLocationTowardsPlayer(entity, arc, DEFAULT_MIN_DIST_FROM_PLAYER, dist, dist);
	}
	public void triggerPickLocationFromEntityTowardsPlayer(final float arc, 
			final float minDist, final float maxDist) {
		triggerPickLocationAwayFromPlayer(null, arc, DEFAULT_MIN_DIST_FROM_PLAYER, minDist, maxDist);
	}
	public void triggerPickLocationTowardsPlayer(final SectorEntityToken entity, final float arc, 
			  									  final float minDist, final float maxDist) {
		triggerPickLocationAwayFromPlayer(entity, arc, DEFAULT_MIN_DIST_FROM_PLAYER, minDist, maxDist);
	}
	public void triggerPickLocationFromEntityTowardsPlayer(final float arc, 
			final float minDistFromPlayer, final float minDist, final float maxDist) {
		triggerCustomAction(new PickLocationTowardsPlayerAction(null, arc, minDist, maxDist, minDistFromPlayer));
	}
	public void triggerPickLocationTowardsPlayer(final SectorEntityToken entity, final float arc, 
												 final float minDistFromPlayer, final float minDist, final float maxDist) {
		triggerCustomAction(new PickLocationTowardsPlayerAction(entity, arc, minDist, maxDist, minDistFromPlayer));
	}
	
	public void triggerPickLocationTowardsEntity(SectorEntityToken entity, float arc, float dist) {
		triggerPickLocationTowardsEntity(entity, arc, DEFAULT_MIN_DIST_FROM_PLAYER, dist, dist);
	}
	public void triggerPickLocationTowardsEntity(final SectorEntityToken entity, final float arc, 
			final float minDistFromPlayer, final float minDist, final float maxDist) {
		triggerCustomAction(new PickLocationTowardsEntityAction(entity, arc, minDist, maxDist, minDistFromPlayer));
	}
	
	
	public void triggerPickLocationFromEntityAwayFromPlayer(final float arc, final float dist) {
		triggerPickLocationAwayFromPlayer(null, arc, DEFAULT_MIN_DIST_FROM_PLAYER, dist, dist);
	}
	public void triggerPickLocationFromEntityAwayFromPlayer(final float arc, 
			final float minDist, final float maxDist) {
		triggerPickLocationAwayFromPlayer(null, arc, DEFAULT_MIN_DIST_FROM_PLAYER, minDist, maxDist);
	}
	public void triggerPickLocationFromEntityAwayFromPlayer(final float arc, 
			final float minDistFromPlayer, final float minDist, final float maxDist) {
		triggerCustomAction(new PickLocationAwayFromPlayerAction(minDist, null, maxDist, arc, minDistFromPlayer));
	}
	public void triggerPickLocationAwayFromPlayer(final SectorEntityToken entity, final float arc, final float dist) {
		triggerPickLocationAwayFromPlayer(entity, arc, DEFAULT_MIN_DIST_FROM_PLAYER, dist, dist);
	}
	public void triggerPickLocationAwayFromPlayer(final SectorEntityToken entity, final float arc, 
												  final float minDist, final float maxDist) {
		triggerPickLocationAwayFromPlayer(entity, arc, DEFAULT_MIN_DIST_FROM_PLAYER, minDist, maxDist);
	}
	public void triggerPickLocationAwayFromPlayer(final SectorEntityToken entity, final float arc, 
												  final float minDistFromPlayer, final float minDist, final float maxDist) {
		triggerCustomAction(new PickLocationAwayFromPlayerAction(minDist, entity, maxDist, arc, minDistFromPlayer));
	}
	
	public void triggerPickLocationAroundPlayer(final float dist) {
		triggerPickLocationAroundPlayer(dist, dist);
	}
	public void triggerPickLocationAroundPlayer(final float minDist, final float maxDist) {
		triggerCustomAction(new PickLocationAroundPlayerAction(maxDist, minDist));
	}
	
	public static float DEFAULT_MIN_DIST_FROM_PLAYER = 3000f;
	public void triggerPickLocationAroundEntity(final float dist) {
		triggerPickLocationAroundEntity(null, DEFAULT_MIN_DIST_FROM_PLAYER, dist, dist);
	}
	public void triggerPickLocationAroundEntity(final SectorEntityToken entity, final float dist) {
		triggerPickLocationAroundEntity(entity, DEFAULT_MIN_DIST_FROM_PLAYER, dist, dist);
	}
	public void triggerPickLocationAroundEntity(final SectorEntityToken entity, final float minDist, final float maxDist) {
		triggerPickLocationAroundEntity(entity, DEFAULT_MIN_DIST_FROM_PLAYER, minDist, maxDist);
	}
	public void triggerPickLocationAroundEntity(final SectorEntityToken entity, final float minDistFromPlayer, final float minDist, final float maxDist) {
		triggerCustomAction(new PickLocationAroundEntityAction(minDist, entity, maxDist, minDistFromPlayer));
	}
	
	public void triggerPickLocationAtInSystemJumpPoint(final StarSystemAPI system) {
		triggerPickLocationAtInSystemJumpPoint(system, DEFAULT_MIN_DIST_FROM_PLAYER);
	}
	public void triggerPickLocationAtInSystemJumpPoint(final StarSystemAPI system, final float minDistFromPlayer) {
		triggerCustomAction(new PickLocationAtInSystemJumpPointAction(system, minDistFromPlayer));
	}
	
	public void triggerPickLocationAtClosestToPlayerJumpPoint(final StarSystemAPI system) {
		triggerPickLocationAtClosestToPlayerJumpPoint(system, DEFAULT_MIN_DIST_FROM_PLAYER);
	}
	public void triggerPickLocationAtClosestToPlayerJumpPoint(final StarSystemAPI system, final float minDistFromPlayer) {
		triggerCustomAction(new PickLocationAtClosestToPlayerJumpPointAction(system, minDistFromPlayer));
	}
	
	public void triggerPickLocationAtClosestToEntityJumpPoint(StarSystemAPI system, SectorEntityToken entity) {
		triggerCustomAction(new PickLocationAtClosestToEntityJumpPointAction(system, entity, 0f));
	}
	public void triggerPickLocationAtClosestToEntityJumpPoint(StarSystemAPI system, SectorEntityToken entity, float minDistFromEntity) {
		triggerCustomAction(new PickLocationAtClosestToEntityJumpPointAction(system, entity, minDistFromEntity));
	}
	
	public void triggerPickLocationWithinArc(final float dir, final float arc,
							final float minDistFromPlayer, final float minDist, final float maxDist) {
		triggerPickLocationWithinArc(null, dir, arc, minDistFromPlayer, minDist, maxDist);
	}
	public void triggerPickLocationWithinArc(final SectorEntityToken entity, final float dir, final float arc,
											 final float minDistFromPlayer, final float minDist, final float maxDist) {
		triggerCustomAction(new PickLocationWithinArcAction(arc, entity, maxDist, minDist, minDistFromPlayer, dir));
	}
	
	public void triggerSetEntityToPickedJumpPoint() {
		triggerCustomAction(new SetEntityToPickedJumpPoint());
	}
	public void triggerFleetSetPatrolActionText(String patrolText) {
		triggerCustomAction(new FleetSetPatrolActionText(patrolText));
	}
	
	public void triggerFleetSetPatrolLeashRange(float dist) {
		triggerSetFleetMemoryValue(MemFlags.FLEET_PATROL_DISTANCE, dist);
	}
	public void triggerFleetSetTravelActionText(String travelText) {
		triggerCustomAction(new FleetSetTravelActionText(travelText));
	}
	
	public void triggerOrderFleetPatrol(final StarSystemAPI system) {
		triggerCustomAction(new OrderFleetPatrolSystemAction(system));
	}
	public void triggerOrderFleetPatrol(final SectorEntityToken ... patrolPoints) {
		triggerOrderFleetPatrol(null, false, patrolPoints);
	}
	public void triggerOrderFleetPatrol(final boolean randomizeLocation, final SectorEntityToken ... patrolPoints) {
		triggerOrderFleetPatrol(null, randomizeLocation, patrolPoints);
	}
	public void triggerOrderFleetPatrol(final StarSystemAPI system, final boolean randomizeLocation, 
										final SectorEntityToken ... patrolPoints) {
		triggerCustomAction(new OrderFleetPatrolPointsAction(patrolPoints, randomizeLocation, system));
	}
	public void triggerOrderFleetPatrol(final StarSystemAPI system, final boolean randomizeLocation, final String ... tags) {
		triggerCustomAction(new OrderFleetPatrolTagsAction(system, randomizeLocation, tags));
	}
	public void triggerOrderExtraPatrolPoints(SectorEntityToken ... points) {
		for (int i = currTrigger.getActions().size() - 1; i >= 0; i--) {
			TriggerAction action = currTrigger.getActions().get(i);
			if (action instanceof OrderFleetPatrolTagsAction) {
				OrderFleetPatrolTagsAction a = (OrderFleetPatrolTagsAction) action;
				if (a.added == null) a.added = new ArrayList<SectorEntityToken>();
				for (SectorEntityToken curr : points) {
					if (curr != null) {
						a.added.add(curr);
					}
				}
				return;
			}
			if (action instanceof OrderFleetPatrolPointsAction) {
				OrderFleetPatrolPointsAction a = (OrderFleetPatrolPointsAction) action;
				for (SectorEntityToken curr : points) {
					if (curr != null) {
						a.patrolPoints.add(curr);
					}
				}
				return;
			}
		}
	}
	
	public void triggerOrderFleetPatrolEntity(boolean moveToNearEntity) {
		triggerCustomAction(new OrderFleetPatrolSpawnedEntity(moveToNearEntity));
	}
	
	public void triggerOrderFleetPatrolHyper(final StarSystemAPI system) {
		triggerOrderFleetPatrol(system, false, system.getHyperspaceAnchor());
	}

	public void triggerFleetAddDefeatTrigger(String trigger) {
		triggerCustomAction(new AddFleetDefeatTriggerAction(trigger, false));
	}
	
	public void triggerFleetAddDefeatTriggerPermanent(String trigger) {
		triggerCustomAction(new AddFleetDefeatTriggerAction(trigger, true));
	}
	
	public void triggerMakeFleetGoAwayAfterDefeat() {
		triggerCustomAction(new AddFleetDefeatTriggerAction("GoAwayAfterDefeatTrigger", true));
	}
	
	public void triggerOrderFleetInterceptPlayer() {
		triggerOrderFleetInterceptPlayer(false, true);
	}
	public void triggerOrderFleetInterceptPlayer(boolean makeHostile, boolean allowLongPursuit) {
		triggerCustomAction(new OrderFleetInterceptPlayerAction(makeHostile));
		if (allowLongPursuit) {
			triggerFleetAllowLongPursuit();
		}
	}
	
	public void triggerOrderFleetMaybeEBurn() {
		triggerOrderFleetEBurn(0.5f);
	}
	public void triggerOrderFleetEBurn(float probabilityToEBurn) {
		if (genRandom.nextFloat() < probabilityToEBurn) {
			triggerCustomAction(new OrderFleetEBurn());
		}
	}
	
	public void triggerOrderFleetAttackLocation(final SectorEntityToken entity) {
		triggerOrderFleetPatrol(null, false, entity);
	}
	
	public void triggerFleetNoAutoDespawn() {
		triggerCustomAction(new FleetNoAutoDespawnAction());
	}
	
	public void triggerFleetStopPursuingPlayerUnlessInStage(Object ... stages) {
		triggerCustomAction(new OrderFleetStopPursuingPlayerUnlessInStage(this, stages));
	}
	
	public void triggerFleetInterceptPlayerWithinRange(boolean mustBeStrongEnoughToFight, float maxRange, 
											boolean repeatable, float repeatDelay, Object ... stages) {
		triggerCustomAction(
				new OrderFleetInterceptNearbyPlayerInStage(this, mustBeStrongEnoughToFight, maxRange, repeatable, repeatDelay, stages));
		triggerFleetStopPursuingPlayerUnlessInStage(stages);
	}
	
	public void triggerFleetInterceptPlayerNearby(boolean mustBeStrongEnoughToFight, Object ... stages) {
		triggerFleetInterceptPlayerWithinRange(mustBeStrongEnoughToFight, 500f, true, 5f, stages);
	}
	
	public void triggerFleetInterceptPlayerOnSight(boolean mustBeStrongEnoughToFight,Object ... stages) {
		triggerFleetInterceptPlayerWithinRange(mustBeStrongEnoughToFight, 10000f, true, 5f, stages);
	}
	
	
	public static Vector2f pickLocationWithinArc(Random random, final SectorEntityToken entity, final float dir, final float arc,
			final float minDistToPlayer, final float minDist, final float maxDist) {
		float angleIncr = 10f;
		float distIncr = (maxDist - minDist) / 5f;
		if (distIncr < 1000f) {
			distIncr = (maxDist - minDist) / 2f;
		}
		if (distIncr < 1) distIncr = 1;


		WeightedRandomPicker<Vector2f> picker = new WeightedRandomPicker<Vector2f>(random);
		for (float currAngle = dir - arc / 2f; currAngle < dir + arc / 2f; currAngle += angleIncr) {
			for (float dist = minDist; dist <= maxDist; dist += distIncr) {
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(currAngle);
				loc.scale(dist);
				Vector2f.add(entity.getLocation(), loc, loc);
				picker.add(loc);
			}
		}

		WeightedRandomPicker<Vector2f> copy = new WeightedRandomPicker<Vector2f>(random);
		copy.addAll(picker);
		
		
		StarSystemAPI system = entity.getStarSystem();
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		Vector2f pick = null;
		LocationAPI containingLocation = entity.getContainingLocation();

		while (!picker.isEmpty()) {
			Vector2f loc = picker.pickAndRemove();
			if (isNearCorona(system, loc)) continue;

			float distToPlayer = Float.MAX_VALUE;
			if (playerFleet != null && playerFleet.getContainingLocation() == containingLocation) {
				distToPlayer = Misc.getDistance(playerFleet.getLocation(), loc);
				if (distToPlayer < minDistToPlayer) {
					continue;
				}
			}
			pick = loc;
		}

		if (pick == null) {
			pick = copy.pick();
		}

		float distToPlayer = Float.MAX_VALUE;
		if (playerFleet != null && playerFleet.getContainingLocation() == containingLocation) {
			distToPlayer = Misc.getDistance(playerFleet.getLocation(), pick);
			if (distToPlayer < minDistToPlayer) {
				Vector2f away = Misc.getUnitVectorAtDegreeAngle(
						Misc.getAngleInDegrees(playerFleet.getLocation(), pick));
				away.scale(minDistToPlayer);
				Vector2f.add(playerFleet.getLocation(), away, away);
				pick = away;
			}
		}
		
		return pick;
	}
	
	public static boolean isNearCorona(StarSystemAPI system, Vector2f loc) {
		if (system == null) return false;
		for (PlanetAPI planet : system.getPlanets()) {
			if (!planet.isStar()) continue;
			StarCoronaTerrainPlugin corona = Misc.getCoronaFor(planet);
			if (corona == null) continue;
			float dist = Misc.getDistance(planet.getLocation(), loc);
			float radius = corona.getParams().middleRadius + corona.getParams().bandWidthInEngine * 0.5f;
			if (dist < radius + 500f) {
				return true;
			}
		}
		return false;
	}
	
	
	public void triggerCustomAction(TriggerAction action) {
		currTrigger.getActions().add(action);
	}
	
	
	
	
	

	protected transient MissionTrigger currTrigger = null;
	public void beginGlobalFlagTrigger(String flag, Object ... stages) {
		beginCustomTrigger(new GlobalBooleanChecker(flag), stages); 
	}
	public void beginDaysElapsedTrigger(float days, Object ... stages) {
		beginCustomTrigger(new DaysElapsedChecker(days, this), stages); 
	}
	public void beginDaysElapsedTrigger(float days, Object stage, Object ... stages) {
		beginCustomTrigger(new DaysElapsedChecker(days, getData(stage)), stages); 
	}
	public void beginInCommRelayRangeTrigger(Object ... stages) {
		beginCustomTrigger(new InCommRelayRangeChecker(), stages);
	}
	public void beginEnteredLocationTrigger(LocationAPI location, Object ... stages) {
		beginCustomTrigger(new EnteredLocationChecker(location), stages);
	}
	public void beginInRangeOfEntityTrigger(SectorEntityToken entity, float range, Object ... stages) {
		beginCustomTrigger(new InRangeOfEntityChecker(entity, range), stages);
	}
//	public void beginWithinHyperspaceRangeTrigger(SectorEntityToken entity, float rangeLY, Object ... stages) {
//		beginWithinHyperspaceRangeTrigger(entity, rangeLY, false, stages);
//	}
	public void beginWithinHyperspaceRangeTrigger(SectorEntityToken entity, float rangeLY, boolean requirePlayerInHyperspace, 
									Object ... stages) {
		beginCustomTrigger(new InHyperRangeOfEntityChecker(entity, rangeLY, requirePlayerInHyperspace), stages);
	}
	
	public void beginWithinHyperspaceRangeTrigger(StarSystemAPI system, float rangeLY, boolean requirePlayerInHyperspace, 
												  Object ... stages) {
		beginWithinHyperspaceRangeTrigger(system.getCenter(), rangeLY, requirePlayerInHyperspace, stages);
	}
	
//	public void beginWithinHyperspaceRangeTrigger(MarketAPI market, float rangeLY, Object ... stages) {
//		beginWithinHyperspaceRangeTrigger(market, rangeLY, false, stages);
//	}
	public void beginWithinHyperspaceRangeTrigger(MarketAPI market, float rangeLY, boolean requirePlayerInHyperspace,
								Object ... stages) {
		beginCustomTrigger(new InHyperRangeOfEntityChecker(market.getPrimaryEntity(), rangeLY, requirePlayerInHyperspace), stages);
	}
	
	public void beginStageTrigger(Object ... stages) {
		beginCustomTrigger(new AlwaysTrueChecker(), stages);
	}
	public void beginCustomTrigger(ConditionChecker condition, Object ... stages) {
		checkExistingTrigger();
		
		currTrigger = new MissionTrigger();
		currTrigger.setCondition(condition);
		if (stages != null) {
			for (Object stage : stages) {
				currTrigger.getStages().add(stage);
			}
		}
	}
	
	
	public void endTrigger() {
		if (currTrigger == null) {
			throw new RuntimeException("endTrigger() called without a corresponding beginTrigger()");
		}
		triggers.add(currTrigger);
		currTrigger = null;
	}
	protected void checkExistingTrigger() {
		if (currTrigger != null) throw new RuntimeException("Already began a trigger, call endTrigger() to finish it");
	}
	
	public MissionTrigger getCurrTrigger() {
		return currTrigger;
	}
	public void setCurrTrigger(MissionTrigger currTrigger) {
		this.currTrigger = currTrigger;
	}
	
	
	public void triggerSpawnEntity(final String entityId, LocData data) {
		triggerCustomAction(new SpawnEntityAction(entityId, data));
	}
	
	public void triggerSpawnDebrisField(float radius, float density, LocData data) {
		triggerCustomAction(new SpawnDebrisFieldAction(radius, density, data));
	}
	
	public void triggerDespawnEntity(SectorEntityToken entity) {
		triggerCustomAction(new DespawnEntityAction(entity));
	}
	

	public void triggerSpawnDerelictHull(String hullId, LocData data) {
		triggerCustomAction(new SpawnDerelictAction(hullId, null, null, data));
	}
	
	public void triggerSpawnDerelict(String factionId, DerelictType type, LocData data) {
		triggerCustomAction(new SpawnDerelictAction(null, factionId, type, data));
	}
	public void triggerSpawnDerelict(DerelictType type, LocData data) {
		triggerCustomAction(new SpawnDerelictAction(type, data));
	}
	
	public void triggerSpawnDerelict(DerelictShipData shipData, LocData data) {
		triggerCustomAction(new SpawnDerelictAction(shipData, data));
	}
	
	public void triggerSpawnShipGraveyard(String factionId, int minShips, int maxShips, LocData data) {
		triggerCustomAction(new SpawnShipGraveyardAction(factionId, minShips, maxShips, data));
	}
	
	public void triggerMakeMissionNodeDiscoverable() {
		triggerCustomAction(new MakeDiscoverableAction(1000f, 200f));
	}
	public void triggerMakeDiscoverable(float range, float xp) {
		triggerCustomAction(new MakeDiscoverableAction(range, xp));
	}
	
	public void triggerFleetAddTags(String ... tags) {
		triggerCustomAction(new AddTagsAction(tags));
	}
	
	public void triggerAddTags(SectorEntityToken entity, String ... tags) {
		triggerCustomAction(new GenericAddTagsAction(entity, tags));
	}
	public void triggerRemoveTags(SectorEntityToken entity, String ... tags) {
		triggerCustomAction(new GenericRemoveTagsAction(entity, tags));
	}
	public void triggerMakeNonStoryCritical(MemoryAPI ... memoryArray) {
		triggerCustomAction(new MakeNonStoryCriticalAction(memoryArray));
	}
	public void triggerMakeNonStoryCritical(String ... markets) {
		for (String id : markets) {
			MarketAPI market = Global.getSector().getEconomy().getMarket(id);
			if (market != null) {
				triggerCustomAction(new MakeNonStoryCriticalAction(market.getMemory()));
			}
		}
	}
	public void triggerMakeNonStoryCritical(MarketAPI ... markets) {
		for (MarketAPI market : markets) {
			triggerCustomAction(new MakeNonStoryCriticalAction(market.getMemory()));
		}
	}
	
	public void triggerFleetAddCommanderSkill(String skill, int level) {
		triggerCustomAction(new AddCommanderSkillAction(skill, level));
	}
	
	/**
	 * Used if a fleet being aggressive/allowed to long-pursue the player/etc needs to persist after
	 * the mission has ended. Some flags - such as whether a fleet is a pirate/patrol/trader/smuggler -
	 * are always permanent regardless of this setting.
	 */
	public void triggerMakeAllFleetFlagsPermanent() {
		triggerCustomAction(new MakeFleetFlagsPermanentAction(true));
	}
	public void triggerUndoMakeAllFleetFlagsPermanent() {
		triggerCustomAction(new MakeFleetFlagsPermanentAction(false));
	}
	
	
	public static CampaignFleetAPI createFleet(FleetSize size, FleetQuality quality, 
			OfficerNum oNum, OfficerQuality oQuality, String factionId, String fleetFactionId, String type, Vector2f locInHyper) {
		CreateFleetAction action = new CreateFleetAction(type, locInHyper, size, quality, factionId);
		action.oNum = oNum;
		action.oQuality = oQuality;
		action.faction = fleetFactionId;
		TriggerActionContext context = new TriggerActionContext(null);
		action.doAction(context);
		return context.fleet;
	}
	
	
	public void triggerCreateSmallPatrolAroundMarket(MarketAPI market, Object stage, float extraSuspicion) {
		triggerCreatePatrolAroundMarket(market, null, stage, FleetSize.VERY_SMALL, FleetTypes.PATROL_MEDIUM, extraSuspicion);
	}
	public void triggerCreateMediumPatrolAroundMarket(MarketAPI market, Object stage, float extraSuspicion) {
		triggerCreatePatrolAroundMarket(market, null, stage, FleetSize.MEDIUM, FleetTypes.PATROL_MEDIUM, extraSuspicion);
	}
	public void triggerCreateLargePatrolAroundMarket(MarketAPI market, Object stage, float extraSuspicion) {
		triggerCreatePatrolAroundMarket(market, null, stage, FleetSize.LARGE, FleetTypes.PATROL_LARGE, extraSuspicion);
	}
	public void triggerCreateSmallPatrol(MarketAPI from, String factionId, SectorEntityToken entityToPatrol, Object stage, float extraSuspicion) {
		triggerCreatePatrolAroundMarket(from, factionId, entityToPatrol, stage, FleetSize.VERY_SMALL, FleetTypes.PATROL_MEDIUM, extraSuspicion);
	}
	public void triggerCreateMediumPatrol(MarketAPI from, String factionId, SectorEntityToken entityToPatrol, Object stage, float extraSuspicion) {
		triggerCreatePatrolAroundMarket(from, factionId, entityToPatrol, stage, FleetSize.MEDIUM, FleetTypes.PATROL_MEDIUM, extraSuspicion);
	}
	public void triggerCreateLargePatrol(MarketAPI from, String factionId, SectorEntityToken entityToPatrol, Object stage, float extraSuspicion) {
		triggerCreatePatrolAroundMarket(from, factionId, entityToPatrol, stage, FleetSize.LARGE, FleetTypes.PATROL_LARGE, extraSuspicion);
	}
	public void triggerCreatePatrolAroundMarket(MarketAPI market, SectorEntityToken entityToPatrol, 
												Object stage, FleetSize size, String fleetType,
												float extraSuspicion) {
		triggerCreatePatrolAroundMarket(market, null, entityToPatrol, stage, size, fleetType, extraSuspicion);
	}
	public void triggerCreatePatrolAroundMarket(MarketAPI market, String factionId, SectorEntityToken entityToPatrol, 
								Object stage, FleetSize size, String fleetType,
								float extraSuspicion) {
		if (entityToPatrol == null) entityToPatrol = market.getPrimaryEntity();
		if (factionId == null) factionId = market.getFactionId();
		
		beginWithinHyperspaceRangeTrigger(entityToPatrol, 1f, false, stage);
		triggerCreateFleet(size, FleetQuality.DEFAULT, factionId, fleetType, entityToPatrol);
		triggerAutoAdjustFleetStrengthModerate();
		triggerMakeAllFleetFlagsPermanent();
		FactionAPI faction = Global.getSector().getFaction(factionId);
		if (faction.getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR)) {
			triggerSetPirateFleet();
		} else {
			triggerSetPatrol();
		}
		triggerPickLocationAroundEntity(entityToPatrol, 100f);
		triggerSpawnFleetAtPickedLocation(null, null);
		triggerOrderFleetPatrol(entityToPatrol);
		triggerSetFleetExtraSmugglingSuspicion(extraSuspicion);
		triggerSetFleetNotBusy(); // so that it can be distracted and in general acts like a normal patrol
		if (market != null) {
			triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, market.getId());
		}
		endTrigger();
	}
	
	
	public static enum ComplicationSpawn {
		APPROACHING_OR_ENTERING,
		APPROACHING_SYSTEM,
		ENTERING_SYSTEM,
		EXITING_SYSTEM,
	}
	public static enum ComplicationRepImpact{
		NONE,
		LOW,
		FULL,
	}
	
	public ComplicationSpawn pickComplicationSpawnType() {
		WeightedRandomPicker<ComplicationSpawn> picker = new WeightedRandomPicker<ComplicationSpawn>(genRandom);
		picker.add(ComplicationSpawn.APPROACHING_SYSTEM);
		picker.add(ComplicationSpawn.ENTERING_SYSTEM);
		picker.add(ComplicationSpawn.EXITING_SYSTEM);
		return picker.pick();
	}
	
	public void triggerRandomizeFleetProperties() {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		
		if (genRandom.nextFloat() < 0.33f && cfa.fSize != FleetSize.TINY && getQuality() > 0.25f) {
			// less ships, better quality and more officers
			cfa.fSize = cfa.fSize.prev();
			if (cfa.fQuality == null) cfa.fQuality = FleetQuality.DEFAULT;
			cfa.fQuality = cfa.fQuality.next();
			
			if (cfa.oNum == null) cfa.oNum = OfficerNum.DEFAULT;
			cfa.oNum = cfa.oNum.next();
		} else if (genRandom.nextFloat() < 0.5f && cfa.fSize != FleetSize.MAXIMUM) {
			// more ships, lower quality, same officers
			cfa.fSize = cfa.fSize.next();
			if (cfa.fQuality == null) cfa.fQuality = FleetQuality.DEFAULT;
			cfa.fQuality = cfa.fQuality.prev();
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	public void triggerComplicationBegin(Object stage, ComplicationSpawn spawnType, StarSystemAPI system,
						String factionId,
						String thing,
						String thingItOrThey,
						String thingDesc,
						int paymentOffered,
						boolean aggressiveIfDeclined,
						ComplicationRepImpact repImpact,
						String failTrigger) {
		if (spawnType == ComplicationSpawn.APPROACHING_OR_ENTERING) spawnType = pickComplicationSpawnType();
		
		if ("them".equals(thingItOrThey)) thingItOrThey = "they";
		
		if (spawnType == ComplicationSpawn.APPROACHING_SYSTEM) {
			beginWithinHyperspaceRangeTrigger(system.getCenter(), 3f, true, stage);
		} else if (spawnType == ComplicationSpawn.ENTERING_SYSTEM) {
			beginEnteredLocationTrigger(system, stage);
		} else if (spawnType == ComplicationSpawn.EXITING_SYSTEM) {
			//beginEnteredLocationTrigger(Global.getSector().getHyperspace(), stage);
			// so that it doesn't trigger if the player exits the system through alternate means and is far away
			beginWithinHyperspaceRangeTrigger(system, 1f, true, stage);
		}
		
		triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, factionId, FleetTypes.PATROL_MEDIUM, system);
		
		triggerSetFleetMissionRef("$" + getMissionId() + "_ref");
		triggerSetFleetMissionRef("$fwt_ref");
		
		FactionAPI faction = Global.getSector().getFaction(factionId);
		if (aggressiveIfDeclined) {
			triggerSetPirateFleet();
			triggerMakeHostileAndAggressive();
		}
		
		if (repImpact == ComplicationRepImpact.LOW) {
			triggerMakeLowRepImpact();
		} else if (repImpact == ComplicationRepImpact.NONE) {
			triggerMakeNoRepImpact();
		}
		
		triggerFleetAllowLongPursuit();
		triggerSetFleetAlwaysPursue();
		
		if (faction.getCustomBoolean(Factions.CUSTOM_SPAWNS_AS_INDEPENDENT)) {
			triggerSetFleetFaction(Factions.INDEPENDENT);
			triggerSetFleetMemoryValue("$fwt_originalFaction", factionId);
		}
		
		
		if (spawnType == ComplicationSpawn.APPROACHING_SYSTEM) {
			triggerPickLocationTowardsPlayer(system.getHyperspaceAnchor(), 90f, getUnits(1.5f));
		} else if (spawnType == ComplicationSpawn.ENTERING_SYSTEM) {
			triggerPickLocationTowardsPlayer(system.getCenter(), 90, 2000);
		} else if (spawnType == ComplicationSpawn.EXITING_SYSTEM) {
			triggerPickLocationAroundPlayer(2000);
		}
		
		triggerOrderFleetInterceptPlayer();
		triggerFleetStopPursuingPlayerUnlessInStage(stage);
		
		triggerSpawnFleetAtPickedLocation("$fwt_wantsThing", null);
		triggerSetFleetMemoryValue("$fwt_aggressive", aggressiveIfDeclined);
		triggerSetFleetMemoryValue("$fwt_thing", getWithoutArticle(thing));
		triggerSetFleetMemoryValue("$fwt_Thing", Misc.ucFirst(getWithoutArticle(thing)));
		triggerSetFleetMemoryValue("$fwt_theThing", thing);
		triggerSetFleetMemoryValue("$fwt_TheThing", Misc.ucFirst(thing));
		triggerSetFleetMemoryValue("$fwt_payment", Misc.getWithDGS(paymentOffered));
		triggerSetFleetMemoryValue("$fwt_itOrThey", thingItOrThey);
		triggerSetFleetMemoryValue("$fwt_ItOrThey", Misc.ucFirst(thingItOrThey));
		
		String thingItOrThem = "them";
		if ("it".equals(thingItOrThey)) thingItOrThem = "it";
		triggerSetFleetMemoryValue("$fwt_itOrThem", thingItOrThem);
		triggerSetFleetMemoryValue("$fwt_ItOrThem", Misc.ucFirst(thingItOrThem));
		
		triggerSetFleetMemoryValue("$fwt_thingDesc", thingDesc);
		triggerSetFleetMemoryValue("$fwt_ThingDesc", Misc.ucFirst(thingDesc));
		
		if (failTrigger == null) {
			failTrigger = "FWTDefaultFailTrigger";
		}
		triggerSetFleetMemoryValue("$fwt_missionFailTrigger", failTrigger);
		
		triggerFleetMakeImportant(null, (Enum) stage);
		
		//endTrigger();
	}
	
	public void triggerComplicationEnd(boolean randomizeAndAdjustFleetSize) {
		if (randomizeAndAdjustFleetSize) {
			triggerRandomizeFleetProperties();
			
			setUseQualityInsteadOfQualityFraction(true);
			triggerAutoAdjustFleetStrengthMajor();
			setUseQualityInsteadOfQualityFraction(false);
		}
		autoAdjustFleetTypeName();
		
		endTrigger();
	}
	
	public void autoAdjustFleetTypeName() {
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		if (cfa.params.fleetType != null && cfa.fSizeOverride != null &&
				(cfa.params.fleetType.equals(FleetTypes.PATROL_SMALL) ||
				 cfa.params.fleetType.equals(FleetTypes.PATROL_MEDIUM) ||
				 cfa.params.fleetType.equals(FleetTypes.PATROL_LARGE))) {
			if (cfa.fSizeOverride <= 0.2f) {
				cfa.params.fleetType = FleetTypes.PATROL_SMALL;	
			} else if (cfa.fSizeOverride < 0.7f) {
				cfa.params.fleetType = FleetTypes.PATROL_MEDIUM;	
			} else {
				cfa.params.fleetType = FleetTypes.PATROL_LARGE;	
			}
		} else if (cfa.params.fleetType != null && cfa.fSizeOverride != null &&
				(cfa.params.fleetType.equals(FleetTypes.SCAVENGER_SMALL) ||
						cfa.params.fleetType.equals(FleetTypes.SCAVENGER_MEDIUM) ||
						cfa.params.fleetType.equals(FleetTypes.SCAVENGER_LARGE))) {
			if (cfa.fSizeOverride <= 0.2f) {
				cfa.params.fleetType = FleetTypes.SCAVENGER_SMALL;	
			} else if (cfa.fSizeOverride < 0.7f) {
				cfa.params.fleetType = FleetTypes.SCAVENGER_MEDIUM;	
			} else {
				cfa.params.fleetType = FleetTypes.SCAVENGER_LARGE;	
			}
		}
	}
	
	
	public void triggerFleetSetWarnAttack(String warnCommsTrigger, String attackCommsTrigger, Object ... stages) {
		triggerSetFleetFlag("$warnAttack", stages);
		triggerSetFleetMemoryValue("$warnAttack_warningComms", warnCommsTrigger);
		triggerSetFleetMemoryValue("$warnAttack_attackComms", attackCommsTrigger);
		triggerFleetInterceptPlayerOnSight(true, stages);
		
		CreateFleetAction cfa = getPreviousCreateFleetAction();
		if (cfa != null && cfa.params != null && cfa.params.factionId != null) {
			triggerSetFleetMemoryValue("$warnAttack_factionId", cfa.params.factionId);
		}
	}
	
	public void triggerFleetAddTugsFlag(int tugs) {
		triggerCustomAction(new FleetAddTugs(tugs));
	}

	public void triggerFleetMakeFaster(boolean navigationSkill, int numTugs, boolean allowLongPursuit) {
		if (navigationSkill) {
			triggerFleetAddCommanderSkill(Skills.NAVIGATION, 1);
		}
		if (numTugs > 0) {
			triggerFleetAddTugsFlag(numTugs);
		}
		if (allowLongPursuit) {
			triggerFleetAllowLongPursuit();
		}
	}
	
	public static void addTugsToFleet(CampaignFleetAPI fleet, int tugs, Random random) {
		//if (true) return;
		
		int max = Global.getSettings().getInt("maxShipsInAIFleet");
		if (fleet.getNumMembersFast() + tugs > max) {
			FleetFactoryV3.pruneFleet(max - tugs, 0, fleet, 100000, random);
		}
		
		FactionAPI faction = fleet.getFaction();
		for (int i = 0; i < tugs; i++) {
			ShipPickParams params = new ShipPickParams(ShipPickMode.ALL);
			List<ShipRolePick> picks = faction.pickShip(ShipRoles.TUG, params, null, random);
			for (ShipRolePick pick : picks) {
				FleetMemberAPI member = fleet.getFleetData().addFleetMember(pick.variantId);
				member.updateStats();
				member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
				break;
			}
		}
	}
}






