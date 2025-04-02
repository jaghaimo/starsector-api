package com.fs.starfarer.api.impl.combat.threat;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BoundsAPI.SegmentAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ConstructionSwarmSystemScript extends BaseShipSystemScript {
	

//	public static float MIN_COOLDOWN = 2f;
//	public static float MAX_COOLDOWN = 10f;
//	public static float COOLDOWN_DP_MULT = 0.25f;
	public static int BASE_FRAGMENTS = 50;
	
	public static float CONSTRUCTION_SWARM_SPEED_MULT = 0.33f;
//	public static float BASE_CONSTRUCTION_TIME = 2f;
//	public static float CONSTRUCTION_TIME_DP_MULT = 1f;
	public static float BASE_CONSTRUCTION_TIME = 5f;
	public static float CONSTRUCTION_TIME_DP_MULT = 1.5f;
	public static float CONSTRUCTION_TIME_OVERSEER_EXTRA = 13f; // makes it 30 seconds
	
	public static float NUM_LARGE_AS_FRACTION_OF_DESTROYERS = 0.5f;
	public static float NUM_DESTROYERS_AS_FRACTION_OF_FRIGATES = 0.6f;
	
	public static int FAST_CONSTRUCTION_FRIGATES_MAX = 2;
	
	public static float MIN_CR;
	public static float MIN_DP;
	public static int MIN_FRAGMENTS;
	public static int MAX_FRAGMENTS;
	
	
	
	public static enum SwarmConstructableType {
		COMBAT_UNIT,
		OVERSEER,
		HIVE,
	}
	
	public static class SwarmConstructableVariant {
		public SwarmConstructableType type;
		public String variantId;
		public float cr; // 0 to 1 (so, 0.02 or similar)
		public float dp;
		public int fragments;
		public HullSize size;
		
		public SwarmConstructableVariant(SwarmConstructableType type, String variantId) {
			this.type = type;
			this.variantId = variantId;
			
			ShipVariantAPI v = Global.getSettings().getVariant(variantId);
			dp = v.getHullSpec().getSuppliesToRecover();
			size = v.getHullSize();
			
			cr = 0.01f;
			if (v.getHullSize() == HullSize.FRIGATE) {
				cr = 0.02f;
			} else if (v.getHullSize() == HullSize.DESTROYER) {
				cr = 0.04f;
			} else if (v.getHullSize() == HullSize.CRUISER) {
				cr = 0.06f;
			} else if (v.getHullSize() == HullSize.CAPITAL_SHIP) {
				cr = 0.1f;
			}
			
			if (type == SwarmConstructableType.HIVE) {
				cr += 0.02f;
			}
			if (type == SwarmConstructableType.OVERSEER) {
				cr += 0.01f;
			}
			fragments = getFragmentCost(dp, size);
		}
	}
	
	public static List<SwarmConstructableVariant> CONSTRUCTABLE = new ArrayList<>();
	
	protected static boolean inited = false;
	/**
	 * Can't do this in a static block because the AI script is loaded and references this
	 * and would run the static block which in turns triggers some stuff that makes the game crash on startup.
	 */
	public static void init() {
		if (inited) return;
		inited = true;
		CONSTRUCTABLE.add(new SwarmConstructableVariant(SwarmConstructableType.COMBAT_UNIT, "skirmish_unit_Type100"));
		CONSTRUCTABLE.add(new SwarmConstructableVariant(SwarmConstructableType.COMBAT_UNIT, "skirmish_unit_Type101"));
		CONSTRUCTABLE.add(new SwarmConstructableVariant(SwarmConstructableType.COMBAT_UNIT, "assault_unit_Type200"));
		CONSTRUCTABLE.add(new SwarmConstructableVariant(SwarmConstructableType.COMBAT_UNIT, "assault_unit_Type201"));
		CONSTRUCTABLE.add(new SwarmConstructableVariant(SwarmConstructableType.COMBAT_UNIT, "standoff_unit_Type300"));
		CONSTRUCTABLE.add(new SwarmConstructableVariant(SwarmConstructableType.COMBAT_UNIT, "standoff_unit_Type301"));
		CONSTRUCTABLE.add(new SwarmConstructableVariant(SwarmConstructableType.COMBAT_UNIT, "standoff_unit_Type302"));
		
		CONSTRUCTABLE.add(new SwarmConstructableVariant(SwarmConstructableType.OVERSEER, "overseer_unit_Type250"));
		CONSTRUCTABLE.add(new SwarmConstructableVariant(SwarmConstructableType.HIVE, "hive_unit_Type350"));
		
		MIN_CR = 1f;
		MIN_DP = 100f;
		MIN_FRAGMENTS = 500;
		
		MAX_FRAGMENTS = 0;
		
		for (SwarmConstructableVariant v : CONSTRUCTABLE) {
			MIN_CR = Math.min(v.cr, MIN_CR);
			MIN_DP = Math.min(v.dp, MIN_DP);
			MIN_FRAGMENTS = Math.min(v.fragments, MIN_FRAGMENTS);
			MAX_FRAGMENTS = Math.max(v.fragments, MAX_FRAGMENTS);
		}
	}
	
	
	public static class SwarmConstructionData {
		public String variantId;
		public float constructionTime = 10f;
		public float preConstructionTravelTime = 3f;
	}
	
	
	protected WeightedRandomPicker<WeaponSlotAPI> slots;
	protected boolean readyToFire = true;
	protected int fastConstructionLeft = FAST_CONSTRUCTION_FRIGATES_MAX;
	
	protected void findSlots(ShipAPI ship) {
		if (slots != null) return;
		slots = new WeightedRandomPicker<>();
		for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			if (slot.isSystemSlot()) {
				slots.add(slot);
			}
		}
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		//boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			//player = ship == Global.getCombatEngine().getPlayerShip();
		} else {
			return;
		}
		
		init();
		
		if (state == State.IDLE || state == State.COOLDOWN || effectLevel <= 0f) {
			readyToFire = true;
		}
		
		if (effectLevel == 1 && readyToFire) {
			readyToFire = false;
			launchSwarm(ship);
		}
	}
	
	
	protected void launchSwarm(ShipAPI ship) {
		findSlots(ship);
		
		String wingId = SwarmLauncherEffect.CONSTRUCTION_SWARM_WING;

		CombatEngineAPI engine = Global.getCombatEngine();
		CombatFleetManagerAPI manager = engine.getFleetManager(ship.getOwner());
		manager.setSuppressDeploymentMessages(true);
		
		WeaponSlotAPI slot = slots.pick();
		
		Vector2f loc = slot.computePosition(ship);
		float facing = slot.computeMidArcAngle(ship);
		
		ShipAPI fighter = manager.spawnShipOrWing(wingId, loc, facing, 0f, null);
		fighter.getWing().setSourceShip(ship);
		
		manager.setSuppressDeploymentMessages(false);
		
		fighter.getMutableStats().getMaxSpeed().modifyMult("construction_swarm", CONSTRUCTION_SWARM_SPEED_MULT);
		
		Vector2f takeoffVel = Misc.getUnitVectorAtDegreeAngle(facing);
		takeoffVel.scale(fighter.getMaxSpeed() * 1f);
		
		fighter.setDoNotRender(true);
		fighter.setExplosionScale(0f);
		fighter.setHulkChanceOverride(0f);
		fighter.setImpactVolumeMult(SwarmLauncherEffect.IMPACT_VOLUME_MULT);
		fighter.getArmorGrid().clearComponentMap(); // no damage to weapons/engines
		Vector2f.add(fighter.getVelocity(), takeoffVel, fighter.getVelocity());
		
		RoilingSwarmEffect sourceSwarm = RoilingSwarmEffect.getSwarmFor(ship);
		if (sourceSwarm == null) return;
		
		RoilingSwarmEffect swarm = FragmentSwarmHullmod.createSwarmFor(fighter);
		swarm.params.flashFringeColor = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR;
		RoilingSwarmEffect.getFlockingMap().remove(swarm.params.flockingClass, swarm);
		swarm.params.flockingClass = FragmentSwarmHullmod.CONSTRUCTION_SWARM_FLOCKING_CLASS;
		RoilingSwarmEffect.getFlockingMap().add(swarm.params.flockingClass, swarm);

		
		SwarmConstructableVariant pick = pickVariant(ship);
		if (pick == null) return;
		
		String variantId = pick.variantId;
//		variantId = "standoff_unit_Type300";
//		variantId = "overseer_unit_Type250";
//		variantId = "skirmish_unit_Type100";
//		variantId = "assault_unit_Type200";
//		variantId = "assault_unit_Type201"; // no swarm
//		variantId = "hive_unit_Type350";
		
		ShipVariantAPI variant = Global.getSettings().getVariant(variantId);
		if (variant == null) return;
		
		ship.setCurrentCR(ship.getCurrentCR() - pick.cr);
		
		float dp = variant.getHullSpec().getSuppliesToRecover();
		
		int numFragments = pick.fragments;
		float radiusMult = 1f;
		float collisionMult = 2f;
		float hpMult = 1f;
		float travelTime = 3f;
		
		if (variant.getHullSize() == HullSize.DESTROYER) {
			radiusMult = 2f;
			collisionMult = 4f;
			hpMult = radiusMult;
			travelTime = 4f;
		} else if (variant.getHullSize() == HullSize.CRUISER) {
			radiusMult = 3.5f;
			collisionMult = 6f;
			hpMult = radiusMult;
			travelTime = 5f;
		} else if (variant.getHullSize() == HullSize.CAPITAL_SHIP) {
			radiusMult = 4;
			collisionMult = 8f;
			hpMult = radiusMult;
			travelTime = 6f;
		}
		
		for (SegmentAPI s : fighter.getExactBounds().getOrigSegments()) {
			s.getP1().scale(collisionMult);
			s.getP2().scale(collisionMult);
			s.set(s.getP1().x, s.getP1().y, s.getP2().x, s.getP2().y);
		}
		fighter.setCollisionRadius(fighter.getCollisionRadius() * collisionMult);
		
		fighter.setMaxHitpoints(fighter.getMaxHitpoints() * hpMult);
		fighter.setHitpoints(fighter.getHitpoints() * hpMult);
		
		swarm.params.maxOffset *= radiusMult;
//		swarm.params.initialMembers *= numMult;
//		swarm.params.baseMembersToMaintain = swarm.params.initialMembers;
//		requiredFragments = swarm.params.initialMembers;
		swarm.params.initialMembers = numFragments;
		swarm.params.baseMembersToMaintain = numFragments;
		
		boolean overseer = variant.getHullSpec().hasTag(Tags.THREAT_OVERSEER);
		
		SwarmConstructionData data = new SwarmConstructionData();
		data.variantId = variantId;
		data.constructionTime = BASE_CONSTRUCTION_TIME + dp * CONSTRUCTION_TIME_DP_MULT;
		if (overseer) {
			data.constructionTime += CONSTRUCTION_TIME_OVERSEER_EXTRA;
		}
		data.preConstructionTravelTime = travelTime;
		
		if (fastConstructionLeft > 0) {
			if (pick.size == HullSize.FRIGATE) {
				fastConstructionLeft--;
				data.constructionTime = 2f;
			} else {
				fastConstructionLeft = 0;
			}
		}
		
		swarm.custom1 = data;
		
		int transfer = Math.min(numFragments, sourceSwarm.getNumActiveMembers());
		if (transfer > 0) {
			loc = new Vector2f(takeoffVel);
			loc.scale(0.5f);
			Vector2f.add(loc, fighter.getLocation(), loc);
			sourceSwarm.transferMembersTo(swarm, transfer, loc, 100f);
		}
		
		int add = numFragments - transfer;
		if (add > 0) {
			swarm.addMembers(add);
		}
	}
	
	
	public SwarmConstructableVariant pickVariant(ShipAPI ship) {
		init();
		
//		if (true) {
//			return new SwarmConstructableVariant(SwarmConstructableType.COMBAT_UNIT, "standoff_unit_Type302");
//		}
		
		CombatEngineAPI engine = Global.getCombatEngine();
		CombatFleetManagerAPI manager = engine.getFleetManager(ship.getOwner());
		if (manager == null) return null;
		
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
		int fragments = swarm == null ? 0 : swarm.getNumActiveMembers();
		
		int dpLeft = manager.getMaxStrength() - manager.getCurrStrength();
		float cr = ship.getCurrentCR();
		
		int overseers = getNumOverseersDeployed(manager);
		int hives = getNumHivesDeployed(manager);
		int fabricators = getNumFabricatorsDeployed(manager);
		float combatWeight = getCombatWeightDeployed(manager);
		
		
		int wantOverseers = (int) (combatWeight / 8f);
		if (wantOverseers < 1) wantOverseers = 1;
		
		combatWeight += Math.max(0, fabricators - 1f) * 16f;
		int wantHives = (int) (combatWeight / 16f);
		
		if (wantHives < 1) wantHives = 1; 
		if (wantHives > 2) wantHives = 2;
		
		wantOverseers -= overseers;
		wantHives -= hives;
		
		float frigates = getCombatDeployed(manager, HullSize.FRIGATE);
		float destroyers = getCombatDeployed(manager, HullSize.DESTROYER);
		float cruisers = getCombatDeployed(manager, HullSize.CRUISER);
		float capitals = getCombatDeployed(manager, HullSize.CAPITAL_SHIP);
		float large = cruisers + capitals;
		
		if (frigates >= 2) {
			fastConstructionLeft = 0;
		}
		
		CountingMap<HullSize> numCombatVariants = new CountingMap<>();
		for (SwarmConstructableVariant curr : CONSTRUCTABLE) {
			if (curr.type == SwarmConstructableType.COMBAT_UNIT) {
				numCombatVariants.add(curr.size);
			}
		}
		
		WeightedRandomPicker<SwarmConstructableVariant> hivePicker = new WeightedRandomPicker<>();
		WeightedRandomPicker<SwarmConstructableVariant> overseerPicker = new WeightedRandomPicker<>();
		WeightedRandomPicker<SwarmConstructableVariant> smallPicker = new WeightedRandomPicker<>();
		WeightedRandomPicker<SwarmConstructableVariant> mediumPicker = new WeightedRandomPicker<>();
		WeightedRandomPicker<SwarmConstructableVariant> largePicker = new WeightedRandomPicker<>();
		
		for (SwarmConstructableVariant curr : CONSTRUCTABLE) {
			if (curr.dp > dpLeft) continue;
			if (curr.cr > cr) continue;
			if (curr.fragments > fragments) continue;
			
			if (curr.type == SwarmConstructableType.HIVE) {
				hivePicker.add(curr, 1f / curr.dp);
			} else if (curr.type == SwarmConstructableType.OVERSEER) {
				overseerPicker.add(curr, 1f / curr.dp);
			} else {
				float wMult = 1f / Math.max(1f, numCombatVariants.getCount(curr.size));
				if (curr.size == HullSize.FRIGATE) {
					smallPicker.add(curr, 1f / curr.dp * wMult);
				} else if (curr.size == HullSize.DESTROYER) {
					mediumPicker.add(curr, 1f / curr.dp * wMult);
				} else {
					largePicker.add(curr, 1f / curr.dp * wMult);
				}
			}
		}
		
		if (frigates <= 1 && !smallPicker.isEmpty()) {
			return smallPicker.pick();
		}
		
		if (wantOverseers > 0 || wantHives > 0) {
			if (wantOverseers >= wantHives && !overseerPicker.isEmpty()) {
				return overseerPicker.pick();
			} else if (!hivePicker.isEmpty()) {
				return hivePicker.pick();
			}
		}
		
		if (large <= destroyers * NUM_LARGE_AS_FRACTION_OF_DESTROYERS && !largePicker.isEmpty()) {
			return largePicker.pick();
		}
		
		if (destroyers <= frigates * NUM_DESTROYERS_AS_FRACTION_OF_FRIGATES && !mediumPicker.isEmpty()) {
			return mediumPicker.pick();
		}
		
		return smallPicker.pick();
	}
	
	public static boolean constructionSwarmWillBuild(ShipAPI ship, String tag, HullSize size) {
		if (!ship.isFighter() || ship.hasTag(ThreatShipConstructionScript.SWARM_CONSTRUCTING_SHIP)) {
			return false;
		}
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
		if (swarm == null) {
			return false;
		}
		
		if (swarm.custom1 instanceof SwarmConstructionData) {
			SwarmConstructionData data = (SwarmConstructionData) swarm.custom1;
			ShipVariantAPI v = Global.getSettings().getVariant(data.variantId);
			if (v.getHullSpec().hasTag(tag)) {
				return size == null || v.getHullSize() == size;
			}
		}
		return false;
	}
	
	public static int getNumFabricatorsDeployed(CombatFleetManagerAPI manager) {
		init();
		int count = 0;
		for (DeployedFleetMemberAPI dfm : manager.getDeployedCopyDFM()) {
			ShipAPI ship = dfm.getShip();
			if (ship == null) continue;
			
			if (ship.getHullSpec().hasTag(Tags.THREAT_FABRICATOR)) {
				count++;
			}
		}
		return count;
	}
	
	public static int getNumOverseersDeployed(CombatFleetManagerAPI manager) {
		init();
		int count = 0;
		for (DeployedFleetMemberAPI dfm : manager.getDeployedCopyDFM()) {
			ShipAPI ship = dfm.getShip();
			if (ship == null) continue;
			
			if (constructionSwarmWillBuild(ship, Tags.THREAT_OVERSEER, null)) {
				count++;
				continue;
			}
			if (ship.isFighter()) continue;
			
			
			if (ship.getHullSpec().hasTag(Tags.THREAT_OVERSEER)) {
				count++;
			}
		}
		return count;
	}
	
	public static int getNumHivesDeployed(CombatFleetManagerAPI manager) {
		init();
		int count = 0;
		for (DeployedFleetMemberAPI dfm : manager.getDeployedCopyDFM()) {
			ShipAPI ship = dfm.getShip();
			if (ship == null) continue;
			
			if (constructionSwarmWillBuild(ship, Tags.THREAT_HIVE, null)) {
				count++;
				continue;
			}
			if (ship.isFighter()) continue;
			
			if (ship.getHullSpec().hasTag(Tags.THREAT_HIVE)) {
				count++;
			}
		}
		return count;
	}
	
	public static float getCombatWeightDeployed(CombatFleetManagerAPI manager) {
		init();
		float weight = 0;
		for (DeployedFleetMemberAPI dfm : manager.getDeployedCopyDFM()) {
			ShipAPI ship = dfm.getShip();
			if (ship == null) continue;
			
			if (ship.isFighter() && !ship.hasTag(ThreatShipConstructionScript.SWARM_CONSTRUCTING_SHIP)) {
				RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
				if (swarm != null && swarm.custom1 instanceof SwarmConstructionData) {
					SwarmConstructionData data = (SwarmConstructionData) swarm.custom1;
					ShipVariantAPI v = Global.getSettings().getVariant(data.variantId);
					if (v.getHullSpec().hasTag(Tags.THREAT_COMBAT)) {
						switch (v.getHullSize()) {
						case CAPITAL_SHIP: weight += 8; break;
						case CRUISER: weight += 4; break;
						case DESTROYER: weight += 2; break;
						case FRIGATE: weight += 1; break;
						case FIGHTER: weight += 1; break;
						}
					}
				}
				continue;
			}
			
			if (ship.getHullSpec().hasTag(Tags.THREAT_COMBAT)) {
				weight += Misc.getShipWeight(ship, false);
			}
		}
		return weight;
	}
	
	public static int getCombatDeployed(CombatFleetManagerAPI manager, HullSize size) {
		init();
		int count = 0;
		for (DeployedFleetMemberAPI dfm : manager.getDeployedCopyDFM()) {
			ShipAPI ship = dfm.getShip();
			if (ship == null) continue;
			
			if (constructionSwarmWillBuild(ship, Tags.THREAT_COMBAT, size)) {
				count++;
				continue;
			}
			if (ship.isFighter() || ship.getHullSize() != size) continue;
			
			if (ship.getHullSpec().hasTag(Tags.THREAT_COMBAT)) {
				count++;
			}
		}
		return count;
	}
	
	
	public static int getFragmentCost(float dp, HullSize size) {
		float numMult = 1f * dp / 5f;
		if (size == HullSize.DESTROYER) {
			numMult = 2f * dp / 20f;
		} else if (size == HullSize.CRUISER) {
			numMult = 3f * dp / 20f;
		} else if (size == HullSize.CAPITAL_SHIP) {
			numMult = 5f * dp / 40f;
		}
		return (int) Math.round(BASE_FRAGMENTS * numMult);
	}

	
	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		init();
		if (system.isOutOfAmmo()) return null;
		if (system.getState() != SystemState.IDLE) return null;
		
		if (!enoughFragments(system, ship)) {
			return "LOW FRAGMENTS";
		}
		if (!enoughDP(system, ship)) {
			return "LOW DP";
		}
		if (!enoughCR(system, ship)) {
			return "LOW CR";
		}
		return "READY";
	}
	
	public boolean enoughCR(ShipSystemAPI system, ShipAPI ship) {
		return ship.getCurrentCR() >= MIN_CR;
	}
	public boolean enoughDP(ShipSystemAPI system, ShipAPI ship) {
		CombatEngineAPI engine = Global.getCombatEngine();
		CombatFleetManagerAPI manager = engine.getFleetManager(ship.getOwner());
		if (manager == null) return true;
		
		int dpLeft = manager.getMaxStrength() - manager.getCurrStrength();
		
		for (DeployedFleetMemberAPI dfm : manager.getDeployedCopyDFM()) {
			ShipAPI ship2 = dfm.getShip();
			if (ship2 == null) continue;
			
			if (!ship2.isFighter() || ship2.hasTag(ThreatShipConstructionScript.SWARM_CONSTRUCTING_SHIP)) {
				continue;
			}
			RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship2);
			if (swarm == null) {
				continue;
			}
			if (swarm.custom1 instanceof SwarmConstructionData) {
				SwarmConstructionData data = (SwarmConstructionData) swarm.custom1;
				ShipVariantAPI v = Global.getSettings().getVariant(data.variantId);
				dpLeft -= v.getHullSpec().getSuppliesToRecover();
			}
		}
		
		return dpLeft >= MIN_DP;
	}
	public boolean enoughFragments(ShipSystemAPI system, ShipAPI ship) {
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
		int active = swarm == null ? 0 : swarm.getNumActiveMembers();
		int required = MIN_FRAGMENTS;
		return active >= required;
	}

	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		init();
		return enoughFragments(system, ship) && enoughDP(system, ship) && enoughCR(system, ship);
	}
	
}








