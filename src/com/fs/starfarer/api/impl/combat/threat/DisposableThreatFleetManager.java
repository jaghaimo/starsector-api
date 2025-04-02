package com.fs.starfarer.api.impl.combat.threat;

import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.listeners.CurrentLocationChangedListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.enc.AbyssalRogueStellarObjectEPEC;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class DisposableThreatFleetManager extends DisposableFleetManager implements CurrentLocationChangedListener {

	public static float DEPTH_0 = 2f;
	public static float DEPTH_1 = 3f;
	public static float DEPTH_2 = 4f;
	
	public static enum FabricatorEscortStrength {
		NONE,
		LOW,
		MEDIUM,
		HIGH,
		MAXIMUM,
	}
	
	public static class ThreatFleetCreationParams {
		public int numFabricators = 0;
		public int numHives = 0;
		public int numOverseers = 0;
		public int numCapitals = 0;
		public int numCruisers = 0;
		public int numDestroyers = 0;
		public int numFrigates = 0;
		
		public String fleetType = FleetTypes.PATROL_SMALL;
	}
	
	
	/**
	 * In $player memory.
	 */
	public static String SENSOR_MODS_KEY = "$hasThreatDetectionSensorMods";
	
	public static String THREAT_DETECTED_RANGE_MULT_ID = "threat_fleet_stealth";
	
	public static float THREAT_DETECTED_RANGE_MULT = 0.1f;
	public static float ONSLAUGHT_MKI_SENSOR_MODIFICATIONS_RANGE_MULT = 10f;
	
	public static int MIN_FLEETS = 1;
	public static int MAX_FLEETS = 6;
	
	
	public DisposableThreatFleetManager() {
		Global.getSector().getListenerManager().addListener(this);
	}
	
	protected Object readResolve() {
		super.readResolve();
		return this;
	}
	
	@Override
	protected String getSpawnId() {
		return "threat";
	}
	
	
	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		// want Threat fleets to basically "be there" not gradually spawn in
		if (spawnRateMult > 0) {
			spawnRateMult = 1000f;
		}
	}
	

	@Override
	public void reportCurrentLocationChanged(LocationAPI prev, LocationAPI curr) {
		if (tracker2 != null) {
			tracker2.forceIntervalElapsed();
		}
	}

	@Override
	protected float getExpireDaysPerFleet() {
		return 365f; // don't spawn again for a long time after reaching max
	}

	@Override
	protected int getDesiredNumFleetsForSpawnLocation() {
		String id = currSpawnLoc.getOptionalUniqueId();
		if (id == null) id = currSpawnLoc.getId();
		
		Random random = new Random(id.hashCode() * 1343890534L);
		
		float depth = Misc.getAbyssalDepth(currSpawnLoc.getLocation(), true);
		if (depth <= 1f) return 0;
		
		float maxDepth = AbyssalRogueStellarObjectEPEC.MAX_THREAT_PROB_DEPTH;
		
		float f = (depth - 1f) / (maxDepth - 1f);
		if (f > 1f) f = 1f;
		
		int minFleets = 1;
		int maxFleets = MIN_FLEETS +
					Math.round((MAX_FLEETS - MIN_FLEETS) * f);
		
		return minFleets + random.nextInt(maxFleets - minFleets + 1);
	}

	@Override
	protected boolean withReturnToSourceAssignments() {
		return false;
	}

	protected StarSystemAPI pickCurrentSpawnLocation() {
		if (Global.getSector().isInNewGameAdvance()) return null;
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player == null) return null;
		StarSystemAPI nearest = null;
		float minDist = Float.MAX_VALUE;
		
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (!system.hasTag(Tags.SYSTEM_CAN_SPAWN_THREAT)) continue;
			
			float distToPlayerLY = Misc.getDistanceLY(player.getLocationInHyperspace(), system.getLocation());
			if (distToPlayerLY > MAX_RANGE_FROM_PLAYER_LY) continue;
			
			if (distToPlayerLY < minDist) {
				nearest = system;
				minDist = distToPlayerLY;
			}
		}
		
		// stick with current system longer unless something else is closer
		if (nearest == null && currSpawnLoc != null) {
			float distToPlayerLY = Misc.getDistanceLY(player.getLocationInHyperspace(), currSpawnLoc.getLocation());
			if (distToPlayerLY <= DESPAWN_RANGE_LY) {
				nearest = currSpawnLoc;
			}
		}
		
		return nearest;
	}
	

	protected CampaignFleetAPI spawnFleetImpl() {
		StarSystemAPI system = currSpawnLoc;
		if (system == null) return null;
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player == null) return null;
		
		float depth = Misc.getAbyssalDepth(system.getLocation(), true);
		
		int numFirst = 0;
		int numSecond = 0;
		int numThird = 0;
		for (CampaignFleetAPI fleet : currSpawnLoc.getFleets()) {
			if (fleet.getFaction().getId().equals(Factions.THREAT)) {
				String type = Misc.getFleetType(fleet);
				if (type == null) continue;

				if (type.equals(FleetTypes.PATROL_SMALL)) {
					numFirst++;
				} else if (type.equals(FleetTypes.PATROL_MEDIUM)) {
					numSecond++;
				} else if (type.equals(FleetTypes.PATROL_LARGE)) {
					numThird++;
				}
			}
		}

		// this is not entriely accruate because depths don't correspond 100% with first/second/third strike
		// that's fine, though
		int maxSecond = 1;
		if (depth >= DEPTH_2 && (float) Math.random() < 0.5f) maxSecond = 2;
		
		if (numThird > 0) {
			depth = Math.min(depth, DEPTH_2 - 0.1f);
		}
		if (numSecond > maxSecond) {
			if ((float) Math.random() < 0.5f) {
				depth = Math.min(depth, DEPTH_0 - 0.1f);
			} else {
				depth = Math.min(depth, DEPTH_1 - 0.1f);
			}
		}

		
		WeightedRandomPicker<FabricatorEscortStrength> picker = new WeightedRandomPicker<>();
		FabricatorEscortStrength strength = FabricatorEscortStrength.NONE;
		int fabricators = 0;
		if (depth < DEPTH_0) {
			fabricators = 0;
			picker.add(FabricatorEscortStrength.LOW, 3f);
			picker.add(FabricatorEscortStrength.MEDIUM, 10f);
			picker.add(FabricatorEscortStrength.HIGH, 1f);
			strength = picker.pick();
		} else if (depth < DEPTH_1) {
			fabricators = 1;
			picker.add(FabricatorEscortStrength.NONE, 1f);
			picker.add(FabricatorEscortStrength.LOW, 10f);
			picker.add(FabricatorEscortStrength.MEDIUM, 5f);
			strength = picker.pick();
			if (strength == FabricatorEscortStrength.NONE) {
				fabricators = 1;
			}
		} else if (depth < DEPTH_2) {
			fabricators = 2;
			picker.add(FabricatorEscortStrength.LOW, 10f);
			picker.add(FabricatorEscortStrength.MEDIUM, 5f);
			picker.add(FabricatorEscortStrength.HIGH, 5f);
			picker.add(FabricatorEscortStrength.MAXIMUM, 5f);
			strength = picker.pick();
			if (strength == FabricatorEscortStrength.MAXIMUM) {
				fabricators = 1;
			}
		} else {
			fabricators = 2;
			picker.add(FabricatorEscortStrength.LOW, 10f);
			picker.add(FabricatorEscortStrength.MEDIUM, 5f);
			picker.add(FabricatorEscortStrength.HIGH, 5f);
			picker.add(FabricatorEscortStrength.MAXIMUM, 5f);
			strength = picker.pick();
			if (strength == FabricatorEscortStrength.LOW || strength == FabricatorEscortStrength.MEDIUM) {
				fabricators = 3;
			}
		}
		
		CampaignFleetAPI f = createThreatFleet(fabricators, 0, 0, strength, null);
	
		system.addEntity(f);
		
		float radius = 4000f + 2000f * (float) Math.random();
		Vector2f loc = Misc.getPointAtRadius(new Vector2f(), radius);
		f.setLocation(loc.x, loc.y);
		
		f.addScript(new ThreatFleetBehaviorScript(f, system));
		
		return f;
	}
	
	public static CampaignFleetAPI createThreatFleet(int numFabricators, 
				int minOtherCapitals, int maxOtherCapitals, FabricatorEscortStrength escorts, Random random) {
		if (random == null) random = Misc.random;
		
		int minHives = 0;
		int maxHives = 0;
		int minOverseers = 0;
		int maxOverseers = 0;
		int minCruisers = 0;
		int maxCruisers = 0;
		int minDestroyers = 0;
		int maxDestroyers = 0;
		int minFrigates = 0;
		int maxFrigates = 0;
		
		switch (escorts) {
		case NONE:
			break;
		case LOW:
			minOverseers = 0;
			maxOverseers = 1;
			minDestroyers = 0;
			maxDestroyers = 1;
			minFrigates = 2;
			maxFrigates = 4;
			if (numFabricators <= 0) {
				minOverseers = 1;
			}
			break;
		case MEDIUM:
			minHives = 0;
			maxHives = 1;
			minOverseers = 1;
			maxOverseers = 1;
			minCruisers = 0;
			maxCruisers = 1;
			minDestroyers = 1;
			maxDestroyers = 2;
			minFrigates = 2;
			maxFrigates = 4;
			if (numFabricators <= 0) {
				minHives = 1;
			}
			break;
		case HIGH:
			minHives = 2;
			maxHives = 3;
			minOverseers = 2;
			maxOverseers = 2;
			minCruisers = 2;
			maxCruisers = 3;
			minDestroyers = 3;
			maxDestroyers = 6;
			minFrigates = 7;
			maxFrigates = 11;
			
			break;
		case MAXIMUM:
			minHives = 3;
			maxHives = 4;
			minOverseers = 3;
			maxOverseers = 3;
			minCruisers = 4;
			maxCruisers = 5;
			minDestroyers = 5;
			maxDestroyers = 6;
			minFrigates = 5;
			maxFrigates = 6;
			break;
		}
		
		ThreatFleetCreationParams params = new ThreatFleetCreationParams();
		params.numFabricators = numFabricators;
		params.numHives = minHives + random.nextInt(maxHives - minHives + 1);
		params.numOverseers = minOverseers + random.nextInt(maxOverseers - minOverseers + 1);
		params.numCapitals = minOtherCapitals + random.nextInt(maxOtherCapitals - minOtherCapitals + 1);
		params.numCruisers = minCruisers + random.nextInt(maxCruisers - minCruisers + 1);
		params.numDestroyers = minDestroyers + random.nextInt(maxDestroyers - minDestroyers + 1);
		params.numFrigates = minFrigates + random.nextInt(maxFrigates - minFrigates + 1);
		
		params.fleetType = FleetTypes.PATROL_SMALL;
		if (numFabricators >= 3 || 
				(numFabricators >= 2 && escorts.ordinal() >= FabricatorEscortStrength.HIGH.ordinal())) {
			params.fleetType = FleetTypes.PATROL_LARGE;
		} else if (numFabricators >= 2 || 
				(numFabricators >= 1 && escorts.ordinal() >= FabricatorEscortStrength.HIGH.ordinal())) {
			params.fleetType = FleetTypes.PATROL_MEDIUM;
		}
		
		return createThreatFleet(params, random);
	}
	
	public static CampaignFleetAPI createThreatFleet(ThreatFleetCreationParams params, Random random) {
		CampaignFleetAPI f = Global.getFactory().createEmptyFleet(Factions.THREAT, "Host", true);
		f.setInflater(null);
		f.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_TYPE, params.fleetType);
		
		addShips(f, params.numFabricators, ShipRoles.THREAT_FABRICATOR, random);
		addShips(f, params.numHives, ShipRoles.THREAT_HIVE, random);
		addShips(f, params.numOverseers, ShipRoles.THREAT_OVERSEER, random);
		addShips(f, params.numCapitals, ShipRoles.COMBAT_CAPITAL, random);
		addShips(f, params.numCruisers, ShipRoles.COMBAT_LARGE, random);
		addShips(f, params.numDestroyers, ShipRoles.COMBAT_MEDIUM, random);
		addShips(f, params.numFrigates, ShipRoles.COMBAT_SMALL, random);
		
		f.getFleetData().setSyncNeeded();
		f.getFleetData().syncIfNeeded();
		f.getFleetData().sort();
		
		for (FleetMemberAPI curr : f.getFleetData().getMembersListCopy()) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		
		FactionAPI faction = Global.getSector().getFaction(Factions.THREAT);
		f.setName(faction.getFleetTypeName(params.fleetType));
		
		
		f.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, 
				   			new ThreatFIDConfig());
		f.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
		f.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
		f.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);
		f.getMemoryWithoutUpdate().set(MemFlags.MAY_GO_INTO_ABYSS, true);
		f.getDetectedRangeMod().modifyMult(THREAT_DETECTED_RANGE_MULT_ID, THREAT_DETECTED_RANGE_MULT, "Low emission drives");
		
		
		return f;
	}
	
	public static void addShips(CampaignFleetAPI fleet, int num, String role, Random random) {
		FactionAPI faction = Global.getSector().getFaction(Factions.THREAT);
		
		ShipPickParams p = new ShipPickParams(ShipPickMode.ALL);
		p.blockFallback = true;
		p.maxFP = 1000000;
		
		for (int i = 0; i < num; i++) {
			List<ShipRolePick> picks = faction.pickShip(role, p, null, random);
			for (ShipRolePick pick : picks) {
				fleet.getFleetData().addFleetMember(pick.variantId);
			}
		}
	}

}








