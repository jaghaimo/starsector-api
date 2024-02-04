package com.fs.starfarer.api.impl.campaign.missions.hub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AsteroidAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.econ.CommodityIconCounts;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 * Hub missions should generally extend this class. The methods/classes defined in this class could easily
 * be in BaseHubMission instead; this class exists purely for organizational purposes.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2019 Fractal Softworks, LLC
 */
public abstract class HubMissionWithSearch extends HubMissionWithTriggers {

//	/**
//	 * "Center of mass".
//	 * @return
//	 */
//	public Vector2f getApproximateLocationOfOtherMissions() {
//		Vector2f com = new Vector2f();
//		
//		PersonAPI person = getPerson();
//		MarketAPI market = null;
//		if (person != null) market = person.getMarket();
//		
//		Vector2f playerLoc = Global.getSector().getPlayerFleet().getLocationInHyperspace();
//		float thresholdDistForBonusAngle = 8000f;
//		
//		float count = 0f;
//		for (BaseHubMission curr : BaseMissionHub.getCreatedMissionsList(person, market)) {
//			if (curr.getStartingStage() == null) continue;
//			SectorEntityToken loc = curr.getMapLocation(null, curr.getStartingStage());
//			if (loc == null) continue;
//			
//			// closer locations are weighed a lot less since they can really swing the angle a lot
//			float f = 1f;
//			float dist = Misc.getDistance(playerLoc, loc.getLocationInHyperspace());
//			if (dist < thresholdDistForBonusAngle) {
//				f = dist / thresholdDistForBonusAngle;
//				f *= f;
//			}
//			
//			// rather, actually, just skip the closer locations
//			if (f < 1f) continue;
//			
//			Vector2f add = new Vector2f(loc.getLocationInHyperspace());
//			add.scale(f);
//			Vector2f.add(com, add, com);
//			count += f;
//		}
//		if (count > 0) {
//			com.scale(1f / count);
//		}
//		return com;
//	}
	
	public boolean matchesSetMissionAngle(Vector2f other, float allowedArc, 
													  float allowedArcBonusIfClose) {
		Vector2f playerLoc = Global.getSector().getPlayerFleet().getLocationInHyperspace();
		
		PersonAPI person = getPerson();
		MarketAPI market = null;
		if (person != null) market = person.getMarket();
		
		float missionAngle = BaseMissionHub.getMissionAngle(person, market);
		
		float angleToOther = Misc.getAngleInDegrees(playerLoc, other); 
		float distToOther = Misc.getDistance(playerLoc, other); 
		
		float angleDiff = Misc.getAngleDiff(missionAngle, angleToOther);
		
		float maxAllowedAngleDiff = allowedArc / 2f;
		float thresholdDistForBonusAngle = 5000f;
		if (distToOther < thresholdDistForBonusAngle) {
			float f = (1f - distToOther / thresholdDistForBonusAngle);
			//f *= f;
			maxAllowedAngleDiff += f * allowedArcBonusIfClose / 2f;
		}
		return angleDiff <= maxAllowedAngleDiff;
	}
	
	public static float DEFAULT_MISSION_ARC = 60f;
	public static float DEFAULT_MISSION_ARC_BONUS = 90f;
	public static float DEFAULT_MISSION_MARKET_ARC = 150f;
	public static float DEFAULT_MISSION_MARKET_ARC_BONUS = 210f;
	
	public static class SystemInDirectionOfOtherMissionsReq implements StarSystemRequirement {
		protected HubMissionWithSearch mission;
		protected float arcMult;
		public SystemInDirectionOfOtherMissionsReq(HubMissionWithSearch mission, float arcMult) {
			this.mission = mission;
			this.arcMult = arcMult;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			return mission.matchesSetMissionAngle(system.getLocation(), 
					DEFAULT_MISSION_ARC * arcMult, 90f);
		}
	}
	
	public static class MarketInDirectionOfOtherMissionsReq implements MarketRequirement {
		protected HubMissionWithSearch mission;
		protected float arcMult;
		public MarketInDirectionOfOtherMissionsReq(HubMissionWithSearch mission, float arcMult) {
			this.mission = mission;
			this.arcMult = arcMult;
		}
		public boolean marketMatchesRequirement(MarketAPI market) {
			return mission.matchesSetMissionAngle(market.getLocationInHyperspace(), 
						DEFAULT_MISSION_MARKET_ARC * arcMult, DEFAULT_MISSION_MARKET_ARC_BONUS);
		}
	}
	
	public static class PlanetInDirectionOfOtherMissionsReq implements PlanetRequirement {
		protected HubMissionWithSearch mission;
		protected float arcMult;
		public PlanetInDirectionOfOtherMissionsReq(HubMissionWithSearch mission, float arcMult) {
			this.mission = mission;
			this.arcMult = arcMult;
		}
		public boolean planetMatchesRequirement(PlanetAPI planet) {
//			if (planet.getStarSystem().getNameWithNoType().toLowerCase().equals("megaron")) {
//				System.out.println("ewfwefwefwe");
//			}
			return mission.matchesSetMissionAngle(planet.getLocationInHyperspace(), 
					DEFAULT_MISSION_ARC * arcMult, DEFAULT_MISSION_ARC_BONUS);
		}
	}
	
	public static class EntityInDirectionOfOtherMissionsReq implements EntityRequirement {
		protected HubMissionWithSearch mission;
		protected float arcMult;
		public EntityInDirectionOfOtherMissionsReq(HubMissionWithSearch mission, float arcMult) {
			this.mission = mission;
			this.arcMult = arcMult;
		}
		public boolean entityMatchesRequirement(SectorEntityToken entity) {
			return mission.matchesSetMissionAngle(entity.getLocationInHyperspace(),
					DEFAULT_MISSION_ARC * arcMult, DEFAULT_MISSION_ARC_BONUS);
		}
	}
	
	public static class TerrainInDirectionOfOtherMissionsReq implements TerrainRequirement {
		protected HubMissionWithSearch mission;
		protected float arcMult;
		public TerrainInDirectionOfOtherMissionsReq(HubMissionWithSearch mission, float arcMult) {
			this.mission = mission;
			this.arcMult = arcMult;
		}
		public boolean terrainMatchesRequirement(CampaignTerrainAPI terrain) {
			return mission.matchesSetMissionAngle(terrain.getLocationInHyperspace(),
					DEFAULT_MISSION_ARC * arcMult, DEFAULT_MISSION_ARC_BONUS);
		}
	}
	

	public static class SystemInDirection implements StarSystemRequirement {
		protected float dir;
		protected float arc;
		protected HubMissionWithSearch mission;
		
		public SystemInDirection(HubMissionWithSearch mission, float dir, float arc) {
			this.mission = mission;
			this.dir = dir;
			this.arc = arc;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			if (mission.getPerson() == null) return false;
			if (mission.getPerson().getMarket() == null) return false;
			Vector2f from = mission.getPerson().getMarket().getLocationInHyperspace();
			return Misc.isInArc(dir, arc, from, system.getLocation());
		}
	}
	
	public static class SystemInDirectionFrom implements StarSystemRequirement {
		protected Vector2f from;
		protected float dir;
		protected float arc;
		
		public SystemInDirectionFrom(Vector2f from, float dir, float arc) {
			this.from = from;
			this.dir = dir;
			this.arc = arc;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			return Misc.isInArc(dir, arc, from, system.getLocation());
		}
	}
	
	public static class MarketIsReq implements MarketRequirement {
		boolean negate;
		MarketAPI param;
		public MarketIsReq(MarketAPI param, boolean negate) {
			this.param = param;
			this.negate = negate;
		}
		public boolean marketMatchesRequirement(MarketAPI market) {
			boolean result = market == param;
			if (negate) result = !result;
			return result;
		}
	}
	public static class MarketFactionReq implements MarketRequirement {
		boolean negate;
		Set<String> factions;
		public MarketFactionReq(boolean negate, String ... factions) {
			this.factions = new LinkedHashSet<String>();
			this.factions.addAll(Arrays.asList(factions));
			this.negate = negate;
		}
		public boolean marketMatchesRequirement(MarketAPI market) {
			boolean result = factions.contains(market.getFactionId());
			if (negate) result = !result;
			return result;
		}
	}
	
	public static class MarketTacticalBombardableReq implements MarketRequirement {
		boolean negate;
		public MarketTacticalBombardableReq(boolean negate) {
			this.negate = negate;
		}
		public boolean marketMatchesRequirement(MarketAPI market) {
			boolean result = !MarketCMD.getTacticalBombardmentTargets(market).isEmpty();
			if (negate) result = !result;
			return result;
		}
	}
	
	public static class MarketFactionHostileReq implements MarketRequirement {
		boolean negate;
		String faction;
		public MarketFactionHostileReq(boolean negate, String faction) {
			this.faction = faction;
			this.negate = negate;
		}
		public boolean marketMatchesRequirement(MarketAPI market) {
			boolean result = market.getFaction().isHostileTo(faction);
			if (negate) result = !result;
			return result;
		}
	}
//	public static class MarketFactionCustomFlagReq implements MarketRequirement {
//		boolean negate;
//		String flag;
//		public MarketFactionCustomFlagReq(boolean negate, String flag) {
//			this.flag = flag;
//			this.negate = negate;
//		}
//		public boolean marketMatchesRequirement(MarketAPI market) {
//			boolean result = market.getFaction().getCustomBoolean(flag);
//			if (negate) result = !result;
//			return result;
//		}
//	}
	
	public static class MarketMilitaryReq implements MarketRequirement {
		public boolean marketMatchesRequirement(MarketAPI market) {
			return Misc.isMilitary(market);
		}
	}
	public static class MarketNotMilitaryReq implements MarketRequirement {
		public boolean marketMatchesRequirement(MarketAPI market) {
			return !Misc.isMilitary(market);
		}
	}
	
	public static class MarketHiddenReq implements MarketRequirement {
		public boolean marketMatchesRequirement(MarketAPI market) {
			return market.isHidden();
		}
	}
	public static class MarketNotHiddenReq implements MarketRequirement {
		public boolean marketMatchesRequirement(MarketAPI market) {
			return !market.isHidden();
			//return !market.isHidden() && !market.isInvalidMissionTarget();
		}
	}
	public static class MarketNotInHyperReq implements MarketRequirement {
		public boolean marketMatchesRequirement(MarketAPI market) {
			return !market.isInHyperspace();
		}
	}
	
	public static class MarketMemoryFlagReq implements MarketRequirement {
		String key;
		Object value;
		public MarketMemoryFlagReq(String key, Object value) {
			this.key = key;
			this.value = value;
		}
		public boolean marketMatchesRequirement(MarketAPI market) {
			Object val = market.getMemoryWithoutUpdate().get(key);
			return val != null && val.equals(value);
		}
	}
	
	public static class MarketLocationReq implements MarketRequirement {
		boolean negate;
		Set<LocationAPI> locations;
		public MarketLocationReq(boolean negate, LocationAPI ... locations) {
			this.locations = new LinkedHashSet<LocationAPI>();
			this.locations.addAll(Arrays.asList(locations));
			this.negate = negate;
		}
		public boolean marketMatchesRequirement(MarketAPI market) {
			boolean result = locations.contains(market.getContainingLocation());
			if (negate) result = !result;
			return result;
		}
	}
	public static class MarketFactionCustomReq extends StringCollectionReqs implements MarketRequirement {
		public MarketFactionCustomReq(ReqMode mode, String ... custom) {
			super(mode, custom);
		}
		public boolean marketMatchesRequirement(MarketAPI market) {
			Set<String> set = new HashSet<String>();
			for (String custom : tags) {
				if (market.getFaction().getCustomBoolean(custom)) {
					set.add(custom);
				}
			}
			return matchesRequirements(set);
		}
	}
	
	public static interface GenericRequirement {
		
	}
	public static interface StarSystemRequirement extends GenericRequirement {
		public boolean systemMatchesRequirement(StarSystemAPI system);
	}
	public static interface PlanetRequirement extends GenericRequirement {
		public boolean planetMatchesRequirement(PlanetAPI planet);
	}
	public static interface EntityRequirement extends GenericRequirement {
		public boolean entityMatchesRequirement(SectorEntityToken entity);
	}
	public static interface MarketRequirement extends GenericRequirement {
		public boolean marketMatchesRequirement(MarketAPI market);
	}
	public static interface CommodityRequirement extends GenericRequirement {
		public boolean commodityMatchesRequirement(CommodityOnMarketAPI com);
	}
	public static interface TerrainRequirement extends GenericRequirement {
		public boolean terrainMatchesRequirement(CampaignTerrainAPI terrain);
	}
	
	public static boolean matchesReq(GenericRequirement req, Object param) {
		if (req instanceof StarSystemRequirement) {
			return ((StarSystemRequirement) req).systemMatchesRequirement((StarSystemAPI) param);
		}
		if (req instanceof PlanetRequirement) {
			return ((PlanetRequirement) req).planetMatchesRequirement((PlanetAPI) param);
		}
		if (req instanceof EntityRequirement) {
			return ((EntityRequirement) req).entityMatchesRequirement((SectorEntityToken) param);
		}
		if (req instanceof MarketRequirement) {
			if (((MarketAPI)param).isInvalidMissionTarget()) return false;
			return ((MarketRequirement) req).marketMatchesRequirement((MarketAPI) param);
		}
		if (req instanceof CommodityRequirement) {
			return ((CommodityRequirement) req).commodityMatchesRequirement((CommodityOnMarketAPI) param);
		}
		if (req instanceof TerrainRequirement) {
			return ((TerrainRequirement) req).terrainMatchesRequirement((CampaignTerrainAPI) param);
		}
		return false;
	}
	
	public static class TerrainTypeReq extends StringCollectionReqs implements TerrainRequirement {
		public TerrainTypeReq(ReqMode mode, String[] types) {
			super(mode, types);
		}
		public boolean terrainMatchesRequirement(CampaignTerrainAPI terrain) {
			Set<String> set = new HashSet<String>();
			set.add(terrain.getType());
			return matchesRequirements(set);
		}
	}
	public static class TerrainHasSpecialNameReq implements TerrainRequirement {
		public TerrainHasSpecialNameReq() {
		}
		public boolean terrainMatchesRequirement(CampaignTerrainAPI terrain) {
			String n1 = terrain.getPlugin().getTerrainName();
			String n2 = terrain.getPlugin().getNameForTooltip();
			if (n1 == null) return false;
			return (!n1.equals(n2));
		}
	}
	public static class EntityUndiscoveredReq implements EntityRequirement {
		boolean negate;
		public EntityUndiscoveredReq(boolean negate) {
			this.negate = negate;
		}
		public boolean entityMatchesRequirement(SectorEntityToken entity) {
			boolean result = entity.isDiscoverable();
			if (negate) result = !result;
			return result;
		}
	}

	public static class EntityTypeReq implements EntityRequirement {
		private Set<String> set;
		public EntityTypeReq(String[] types) {
			set = new LinkedHashSet<String>();
			for (String type : types) {
				set.add(type);
			}
		}
		public boolean entityMatchesRequirement(SectorEntityToken entity) {
			return set.contains(entity.getCustomEntityType());
		}
	}
	
	public static class EntityMemoryReq implements EntityRequirement {
		private List<String> flags = new ArrayList<String>();
		public EntityMemoryReq(String ... flags) {
			for (String flag : flags) {
				this.flags.add(flag);
			}
		}
		public boolean entityMatchesRequirement(SectorEntityToken entity) {
			for (String flag : flags) {
				if (!entity.getMemoryWithoutUpdate().getBoolean(flag)) return false;
			}
			return true;
		}
	}
	
	public static class SystemOnFringeOfSectorReq implements StarSystemRequirement {
		protected float rangeLY = Global.getSettings().getFloat("sectorHeight") / Misc.getUnitsPerLightYear() * 
										NON_FRINGE_PORTION_OF_HEIGHT * 0.5f;
		protected boolean negate = false;
		public SystemOnFringeOfSectorReq() {
			this(false);
		}
		public SystemOnFringeOfSectorReq(boolean negate) {
			this.negate = negate;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			boolean result = Misc.getDistanceLY(new Vector2f(), system.getLocation()) > rangeLY;
			if (negate) result = !result;
			return result;
		}
	}
	public static class SystemInInnerSectorReq implements StarSystemRequirement {
		protected float rangeLY = Global.getSettings().getFloat("sectorHeight") / Misc.getUnitsPerLightYear() * 
										INNER_SECTOR_PORTION_OF_HEIGHT * 0.5f;
		protected boolean negate = false;
		public SystemInInnerSectorReq() {
			this(false);
		}
		public SystemInInnerSectorReq(boolean negate) {
			this.negate = negate;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			boolean result = Misc.getDistanceLY(new Vector2f(), system.getLocation()) < rangeLY;
			if (negate) result = !result;
			return result;
		}
	}
	
	public static class SystemWithinRangeReq implements StarSystemRequirement {
		private Vector2f loc;
		private float minRangeLY;
		private float maxRangeLY;
		public SystemWithinRangeReq(Vector2f loc, float minRangeLY, float maxRangeLY) {
			this.loc = loc;
			this.minRangeLY = minRangeLY;
			this.maxRangeLY = maxRangeLY;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			float dist = Misc.getDistanceLY(loc, system.getLocation());
			return dist >= minRangeLY && dist <= maxRangeLY;
		}
	}
	
	public static class SystemHasBaseReq implements StarSystemRequirement {
		private String factionId;
		public SystemHasBaseReq(String factionId) {
			this.factionId = factionId;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			for (MarketAPI market : Misc.getMarketsInLocation(system)) {
				if (!factionId.equals(market.getFactionId())) continue;
				if (market.getMemoryWithoutUpdate().getBoolean(MemFlags.HIDDEN_BASE_MEM_FLAG)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public static class SystemHasColonyReq implements StarSystemRequirement {
		private String factionId;
		private int minSize;
		public SystemHasColonyReq(String factionId, int minSize) {
			this.factionId = factionId;
			this.minSize = minSize;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			for (MarketAPI market : Misc.getMarketsInLocation(system)) {
				if (!factionId.equals(market.getFactionId())) continue;
				if (market.getSize() < minSize) continue;
				if (market.getMemoryWithoutUpdate().getBoolean(MemFlags.HIDDEN_BASE_MEM_FLAG)) {
					continue;
				}
				return true;
			}
			return false;
		}
	}
	
	public static class MultipleStarSystemRequirements implements StarSystemRequirement {
		protected StarSystemRequirement [] reqs;
		protected ReqMode mode;
		public MultipleStarSystemRequirements(ReqMode mode, StarSystemRequirement ... reqs) {
			this.mode = mode;
			this.reqs = reqs;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			switch (mode) {
			case ALL:
				for (StarSystemRequirement req : reqs) if (!req.systemMatchesRequirement(system)) return false;
				return true;
			case ANY:
				for (StarSystemRequirement req : reqs) if (req.systemMatchesRequirement(system)) return true;
				return false;
			case NOT_ALL:
				for (StarSystemRequirement req : reqs) if (!req.systemMatchesRequirement(system)) return true;
				return false;
			case NOT_ANY:
				for (StarSystemRequirement req : reqs) if (req.systemMatchesRequirement(system)) return false;
				return true;
			}
			return false;
		}
	}
	public static class MultiplePlanetRequirements implements PlanetRequirement {
		protected PlanetRequirement [] reqs;
		protected ReqMode mode;
		public MultiplePlanetRequirements(ReqMode mode, PlanetRequirement ... reqs) {
			this.mode = mode;
			this.reqs = reqs;
		}
		public boolean planetMatchesRequirement(PlanetAPI planet) {
			switch (mode) {
			case ALL:
				for (PlanetRequirement req : reqs) if (!req.planetMatchesRequirement(planet)) return false;
				return true;
			case ANY:
				for (PlanetRequirement req : reqs) if (req.planetMatchesRequirement(planet)) return true;
				return false;
			case NOT_ALL:
				for (PlanetRequirement req : reqs) if (!req.planetMatchesRequirement(planet)) return true;
				return false;
			case NOT_ANY:
				for (PlanetRequirement req : reqs) if (req.planetMatchesRequirement(planet)) return false;
				return true;
			}
			return false;
		}
	}
	public static class MultipleEntityRequirements implements EntityRequirement {
		protected EntityRequirement [] reqs;
		protected ReqMode mode;
		public MultipleEntityRequirements(ReqMode mode, EntityRequirement ... reqs) {
			this.mode = mode;
			this.reqs = reqs;
		}
		public boolean entityMatchesRequirement(SectorEntityToken entity) {
			switch (mode) {
			case ALL:
				for (EntityRequirement req : reqs) if (!req.entityMatchesRequirement(entity)) return false;
				return true;
			case ANY:
				for (EntityRequirement req : reqs) if (req.entityMatchesRequirement(entity)) return true;
				return false;
			case NOT_ALL:
				for (EntityRequirement req : reqs) if (!req.entityMatchesRequirement(entity)) return true;
				return false;
			case NOT_ANY:
				for (EntityRequirement req : reqs) if (req.entityMatchesRequirement(entity)) return false;
				return true;
			}
			return false;
		}
	}
	
	public static class MultipleMarketRequirements implements MarketRequirement {
		protected MarketRequirement [] reqs;
		protected ReqMode mode;
		public MultipleMarketRequirements(ReqMode mode, MarketRequirement ... reqs) {
			this.mode = mode;
			this.reqs = reqs;
		}
		public boolean marketMatchesRequirement(MarketAPI market) {
			switch (mode) {
			case ALL:
				for (MarketRequirement req : reqs) if (!req.marketMatchesRequirement(market)) return false;
				return true;
			case ANY:
				for (MarketRequirement req : reqs) if (req.marketMatchesRequirement(market)) return true;
				return false;
			case NOT_ALL:
				for (MarketRequirement req : reqs) if (!req.marketMatchesRequirement(market)) return true;
				return false;
			case NOT_ANY:
				for (MarketRequirement req : reqs) if (req.marketMatchesRequirement(market)) return false;
				return true;
			}
			return false;
		}
	}
	
	public static class MultipleCommodityRequirements implements CommodityRequirement {
		protected CommodityRequirement [] reqs;
		protected ReqMode mode;
		public MultipleCommodityRequirements(ReqMode mode, CommodityRequirement ... reqs) {
			this.mode = mode;
			this.reqs = reqs;
		}
		public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
			switch (mode) {
			case ALL:
				for (CommodityRequirement req : reqs) if (!req.commodityMatchesRequirement(com)) return false;
				return true;
			case ANY:
				for (CommodityRequirement req : reqs) if (req.commodityMatchesRequirement(com)) return true;
				return false;
			case NOT_ALL:
				for (CommodityRequirement req : reqs) if (!req.commodityMatchesRequirement(com)) return true;
				return false;
			case NOT_ANY:
				for (CommodityRequirement req : reqs) if (req.commodityMatchesRequirement(com)) return false;
				return true;
			}
			return false;
		}
	}
	
	
	public static class PlanetOrbitIsNotNearJumpPoint implements PlanetRequirement {
		protected float checkDist;
		public PlanetOrbitIsNotNearJumpPoint(float checkDist) {
			this.checkDist = checkDist;
		}
		public boolean planetMatchesRequirement(PlanetAPI planet) {
			StarSystemAPI system = planet.getStarSystem();
			if (system == null) return true;
			
			float planetDist = planet.getCircularOrbitRadius();
			if (planet.getOrbit() == null) {
				planetDist = planet.getLocation().length();
			}
			
			for (SectorEntityToken jp : system.getJumpPoints()) {
				float dist = 0;
				if (jp.getOrbitFocus() == planet.getOrbitFocus()) {
					float jpDist = jp.getCircularOrbitRadius();
					if (jp.getOrbitFocus() == null) {
						jpDist = jp.getLocation().length();
					}
					dist = Math.abs(planetDist - jpDist);
					if (jp.getOrbitFocus() == null && planet.getOrbitFocus() == null) {
						dist = Misc.getDistance(planet, jp);
					}
				} else if (jp.getOrbitFocus() == null && planet.getOrbitFocus() != null) {
					float jpDist = Misc.getDistance(jp, planet.getOrbitFocus());
					dist = Math.abs(planetDist - jpDist);
					if (jp.getOrbitFocus() == null && planet.getOrbitFocus() == null) {
						dist = Misc.getDistance(planet, jp);
					}
				} else if (jp.getOrbitFocus() != null && planet.getOrbitFocus() == null) {
					float jpDist = jp.getCircularOrbitRadius();
					float pDist2 = Misc.getDistance(jp.getOrbitFocus(), jp);
					dist = Math.abs(pDist2 - jpDist);
					if (jp.getOrbitFocus() == null && planet.getOrbitFocus() == null) {
						dist = Misc.getDistance(planet, jp);
					}
				} else if (jp.getOrbitFocus() != null && planet.getOrbitFocus() != null) {
					// orbiting different centers
					float jpDist = jp.getCircularOrbitRadius();
					dist = Misc.getDistance(jp.getOrbitFocus(), planet.getOrbitFocus());
					dist -= planetDist + jpDist;
				}
				if (dist < checkDist) return false;
			}
			return true;
		}
	}
	
	public static class PlanetIsPopulatedReq implements PlanetRequirement {
		protected boolean negate = false;
		public PlanetIsPopulatedReq() {
			this(false);
		}
		public PlanetIsPopulatedReq(boolean negate) {
			this.negate = negate;
		}
		public boolean planetMatchesRequirement(PlanetAPI planet) {
			boolean result = planet.getMarket() != null && !planet.getMarket().isPlanetConditionMarketOnly();
			if (negate) result = !result;
			return result;
		}
	}
	public static class PlanetUnexploredRuinsReq implements PlanetRequirement {
		protected boolean negate = false;
		public PlanetUnexploredRuinsReq() {
			this(false);
		}
		public PlanetUnexploredRuinsReq(boolean negate) {
			this.negate = negate;
		}
		public boolean planetMatchesRequirement(PlanetAPI planet) {
			MarketAPI market = planet.getMarket();
			boolean result = market != null && market.isPlanetConditionMarketOnly() && 
						!market.getMemoryWithoutUpdate().getBoolean("$ruinsExplored");
			if (negate) result = !result;
			return result;
		}
	}
	public static class PlanetFullySurveyedReq implements PlanetRequirement {
		protected boolean negate = false;
		public PlanetFullySurveyedReq() {
			this(false);
		}
		public PlanetFullySurveyedReq(boolean negate) {
			this.negate = negate;
		}
		public boolean planetMatchesRequirement(PlanetAPI planet) {
			boolean result = planet.getMarket() != null && planet.getMarket().getSurveyLevel() == SurveyLevel.FULL;
			if (negate) result = !result;
			return result;
		}
	}
	public static class PlanetUnsurveyedReq implements PlanetRequirement {
		protected boolean negate = false;
		public PlanetUnsurveyedReq() {
			this(false);
		}
		public PlanetUnsurveyedReq(boolean negate) {
			this.negate = negate;
		}
		public boolean planetMatchesRequirement(PlanetAPI planet) {
			boolean result = planet.getMarket() != null && planet.getMarket().getSurveyLevel() == SurveyLevel.NONE;
			if (negate) result = !result;
			return result;
		}
	}
	public static class PlanetIsGasGiantReq implements PlanetRequirement {
		protected boolean negate = false;
		public PlanetIsGasGiantReq() {
			this(false);
		}
		public PlanetIsGasGiantReq(boolean negate) {
			this.negate = negate;
		}
		public boolean planetMatchesRequirement(PlanetAPI planet) {
			boolean result = planet.isGasGiant();
			if (negate) result = !result;
			return result;
		}
	}
	public static class SystemHasPulsarReq implements StarSystemRequirement {
		protected boolean negate = false;
		public SystemHasPulsarReq() {
			this(false);
		}
		public SystemHasPulsarReq(boolean negate) {
			this.negate = negate;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			boolean result = Misc.hasPulsar(system);
			if (negate) result = !result;
			return result;
		}
	}
	
	public static class SystemHasAtLeastJumpPointsReq implements StarSystemRequirement {
		protected int min = 0;
		public SystemHasAtLeastJumpPointsReq(int min) {
			this.min = min;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			return system.getJumpPoints().size() >= min;
		}
	}
	
	public static class SystemIsNebulaReq implements StarSystemRequirement {
		protected boolean negate = false;
		public SystemIsNebulaReq() {
			this(false);
		}
		public SystemIsNebulaReq(boolean negate) {
			this.negate = negate;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			boolean result = system.isNebula();
			if (negate) result = !result;
			return result;
		}
	}
	public static class SystemIsBlackHoleReq implements StarSystemRequirement {
		protected boolean negate = false;
		public SystemIsBlackHoleReq() {
			this(false);
		}
		public SystemIsBlackHoleReq(boolean negate) {
			this.negate = negate;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			boolean result = system.getStar() != null && system.getStar().getSpec().isBlackHole();
			if (negate) result = !result;
			return result;
		}
	}
	public static class StarSystemUnexploredReq implements StarSystemRequirement {
		protected boolean negate = false;
		public StarSystemUnexploredReq() {
			this(false);
		}
		public StarSystemUnexploredReq(boolean negate) {
			this.negate = negate;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
//			if (system.getNameWithNoType().equals("Stella Vitae")) {
//				System.out.println("wefwefwef");
//			}
			boolean result = !system.isEnteredByPlayer();
			if (DebugFlags.ALLOW_VIEW_UNEXPLORED_SYSTEM_MAP) {
				result = system.getDaysSinceLastPlayerVisit() >= 100000f;
			}
			if (negate) result = !result;
			return result;
		}
	}
	
	public static class StarSystemDaysSincePlayerVisitReq implements StarSystemRequirement {
		protected float days;
		public StarSystemDaysSincePlayerVisitReq(float days) {
			this.days = days;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			boolean result = system.getDaysSinceLastPlayerVisit() >= days;
			return result;
		}
	}
	public static class StarSystemHasNumPlanetsReq implements StarSystemRequirement {
		protected int num;
		public StarSystemHasNumPlanetsReq(int num) {
			this.num = num;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			int stars = 0;
			if (system.getStar() != null) stars++;
			if (system.getSecondary() != null) stars++;
			if (system.getTertiary() != null) stars++;
			return system.getPlanets().size() - stars >= num;
		}
	}
	
	public static class StarSystemHasNumTerrainReq implements StarSystemRequirement {
		protected int num;
		public StarSystemHasNumTerrainReq(int num) {
			this.num = num;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			return system.getTerrainCopy().size() >= num;
		}
	}
	
	public static class StarSystemHasNumPlanetsAndTerrainReq implements StarSystemRequirement {
		protected int num;
		public StarSystemHasNumPlanetsAndTerrainReq(int num) {
			this.num = num;
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			int stars = 0;
			if (system.getStar() != null) stars++;
			if (system.getSecondary() != null) stars++;
			if (system.getTertiary() != null) stars++;
			return system.getPlanets().size() - stars + system.getTerrainCopy().size() >= num;
		}
	}
	
	public static class StringCollectionReqs {
		public ReqMode mode;
		List<String> tags = new ArrayList<String>();
		public StringCollectionReqs(ReqMode mode, String ... tags) {
			this.mode = mode;
			for (String tag : tags) {
				this.tags.add(tag);
			}
		}
		public boolean matchesRequirements(Collection<String> set) {
			switch (mode) {
			case ALL:
				for (String tag : tags) if (!set.contains(tag)) return false;
				return true;
			case ANY:
				for (String tag : tags) if (set.contains(tag)) return true;
				return false;
			case NOT_ALL:
				for (String tag : tags) if (!set.contains(tag)) return true;
				return false;
			case NOT_ANY:
				for (String tag : tags) if (set.contains(tag)) return false;
				return true;
			}
			return false;
		}
	}
	
	public static class RequiredSystemTags extends StringCollectionReqs implements StarSystemRequirement {
		public RequiredSystemTags(ReqMode mode, String[] tags) {
			super(mode, tags);
		}
		public boolean systemMatchesRequirement(StarSystemAPI system) {
			return matchesRequirements(system.getTags());
		}
	}
	public static class RequiredTerrainTags extends StringCollectionReqs implements TerrainRequirement {
		public RequiredTerrainTags(ReqMode mode, String[] tags) {
			super(mode, tags);
		}
		public boolean terrainMatchesRequirement(CampaignTerrainAPI terrain) {
			return matchesRequirements(terrain.getTags());
		}
	}
	public static class RequiredPlanetTags extends StringCollectionReqs implements PlanetRequirement {
		public RequiredPlanetTags(ReqMode mode, String[] tags) {
			super(mode, tags);
		}
		public boolean planetMatchesRequirement(PlanetAPI planet) {
			return matchesRequirements(planet.getTags());
		}
	}
	public static class RequiredEntityTags extends StringCollectionReqs implements EntityRequirement {
		public RequiredEntityTags(ReqMode mode, String[] tags) {
			super(mode, tags);
		}
		public boolean entityMatchesRequirement(SectorEntityToken entity) {
			return matchesRequirements(entity.getTags());
		}
	}
	public static class RequiredPlanetConditions extends StringCollectionReqs implements PlanetRequirement {
		public RequiredPlanetConditions(ReqMode mode, String[] tags) {
			super(mode, tags);
		}
		public boolean planetMatchesRequirement(PlanetAPI planet) {
			List<String> set = new ArrayList<String>();
			if (planet.getMarket() != null) {
				for (MarketConditionAPI mc : planet.getMarket().getConditions()) {
					set.add(mc.getId());
				}
			}
			return matchesRequirements(set);
		}
	}
	
	public static class RequiredCommodityTags extends StringCollectionReqs implements CommodityRequirement {
		public RequiredCommodityTags(ReqMode mode, String[] tags) {
			super(mode, tags);
		}
		public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
			List<String> set = new ArrayList<String>();
			set.addAll(com.getCommodity().getTags());
			return matchesRequirements(set);
		}
	}
	
	public static class RequiredMarketConditions extends StringCollectionReqs implements MarketRequirement {
		public RequiredMarketConditions(ReqMode mode, String[] tags) {
			super(mode, tags);
		}
		public boolean marketMatchesRequirement(MarketAPI market) {
			List<String> set = new ArrayList<String>();
			for (MarketConditionAPI mc : market.getConditions()) {
				set.add(mc.getId());
			}
			return matchesRequirements(set);
		}
	}
	
	public static class RequiredMarketIndustries extends StringCollectionReqs implements MarketRequirement {
		public RequiredMarketIndustries(ReqMode mode, String[] tags) {
			super(mode, tags);
		}
		public boolean marketMatchesRequirement(MarketAPI market) {
			List<String> set = new ArrayList<String>();
			for (Industry ind : market.getIndustries()) {
				set.add(ind.getId());
			}
			return matchesRequirements(set);
		}
	}
	
	
	public static class SearchData {
		public List<GenericRequirement> systemReqs = new ArrayList<GenericRequirement>();
		public List<GenericRequirement> systemPrefs = new ArrayList<GenericRequirement>();
		public List<PlanetRequirement> planetReqs = new ArrayList<PlanetRequirement>();
		public List<PlanetRequirement> planetPrefs = new ArrayList<PlanetRequirement>();
		public List<EntityRequirement> entityReqs = new ArrayList<EntityRequirement>();
		public List<EntityRequirement> entityPrefs = new ArrayList<EntityRequirement>();
		public List<MarketRequirement> marketReqs = new ArrayList<MarketRequirement>();
		public List<MarketRequirement> marketPrefs = new ArrayList<MarketRequirement>();
		public List<TerrainRequirement> terrainReqs = new ArrayList<TerrainRequirement>();
		public List<TerrainRequirement> terrainPrefs = new ArrayList<TerrainRequirement>();
		public List<CommodityRequirement> commodityReqs = new ArrayList<CommodityRequirement>();
		public List<CommodityRequirement> commodityPrefs = new ArrayList<CommodityRequirement>();
		
		List<StarSystemAPI> matchingSystems = null;
		List<StarSystemAPI> preferredSystems = null;
		
		List<MarketAPI> matchingMarkets = null;
		List<MarketAPI> preferredMarkets = null;
	}
	
	protected transient SearchData search = new SearchData();
	
	public SearchData getSearch() {
		return search;
	}
	public void resetSearch() {
		search = new SearchData();
	}
	
	public void requireSystemInterestingAndNotCore() {
		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_CORE);
		preferSystemTags(ReqMode.ANY, Tags.THEME_INTERESTING, Tags.THEME_INTERESTING_MINOR);
	}
	public void requireSystemInterestingAndNotUnsafeOrCore() {
		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_UNSAFE, Tags.THEME_CORE);
		preferSystemTags(ReqMode.ANY, Tags.THEME_INTERESTING, Tags.THEME_INTERESTING_MINOR);
	}
	public void preferSystemInteresting() {
		preferSystemTags(ReqMode.ANY, Tags.THEME_INTERESTING, Tags.THEME_INTERESTING_MINOR);
	}
	
	public void preferSystemInDirectionOfOtherMissions() {
		search.systemPrefs.add(new SystemInDirectionOfOtherMissionsReq(this, 1f));
	}
	
	public void requireSystemInDirection(float dir, float arc) {
		search.systemReqs.add(new SystemInDirection(this, dir, arc));
	}
	
	public void preferSystemInDirection(float dir, float arc) {
		search.systemPrefs.add(new SystemInDirection(this, dir, arc));
	}
	
	public void requireSystemInDirectionFrom(Vector2f from, float dir, float arc) {
		search.systemReqs.add(new SystemInDirectionFrom(from, dir, arc));
	}
	
	public void preferSystemInDirectionFrom(Vector2f from, float dir, float arc) {
		search.systemPrefs.add(new SystemInDirectionFrom(from, dir, arc));
	}
	
	/**
	 * Shouldn't use "preferSystem" for these because the systems are picked BEFORE planets are checked so 
	 * e.g. we may pick 20 systems that "match", find that none of them have planets that match, and fall
	 * back to the full set of systems. Using the preferPlanets method, we'll look at all-direction systems
	 * and filter them out at the planet level. 
	 */
	public void preferPlanetInDirectionOfOtherMissions() {
		search.planetPrefs.add(new PlanetInDirectionOfOtherMissionsReq(this, 1f));
	}
	
	public void preferEntityInDirectionOfOtherMissions() {
		search.entityPrefs.add(new EntityInDirectionOfOtherMissionsReq(this, 1f));
	}
	
	public void preferTerrainInDirectionOfOtherMissions() {
		search.terrainPrefs.add(new TerrainInDirectionOfOtherMissionsReq(this, 1f));
	}
	
	public void preferMarketInDirectionOfOtherMissions() {
		search.marketPrefs.add(new MarketInDirectionOfOtherMissionsReq(this, 1f));
	}
	
	public void requireSystemTags(ReqMode mode, String ... tags) {
		search.systemReqs.add(new RequiredSystemTags(mode, tags));
	}
	public void preferSystemTags(ReqMode mode, String ... tags) {
		search.systemPrefs.add(new RequiredSystemTags(mode, tags));
	}
	public void requireSystemHasBase(String factionId) {
		search.systemReqs.add(new SystemHasBaseReq(factionId));
	}
	public void preferSystemHasBase(String factionId) {
		search.systemPrefs.add(new SystemHasBaseReq(factionId));
	}
	public void requireSystemHasColony(String factionId, int minSize) {
		search.systemReqs.add(new SystemHasColonyReq(factionId, minSize));
	}
	public void preferSystemHasColony(String factionId, int minSize) {
		search.systemPrefs.add(new SystemHasColonyReq(factionId, minSize));
	}
	
	public void requireSystemHasAtLeastNumJumpPoints(int min) {
		search.systemReqs.add(new SystemHasAtLeastJumpPointsReq(min));
	}
	public void preferSystemHasAtLeastNumJumpPoints(int min) {
		search.systemPrefs.add(new SystemHasAtLeastJumpPointsReq(min));
	}
	
	public void requireSystemUnexplored() {
		search.systemReqs.add(new StarSystemUnexploredReq());
	}
	public void preferSystemUnexplored() {
		search.systemPrefs.add(new StarSystemUnexploredReq());
		preferSystemNotEnteredByPlayerFor(365f); // fallback for when everything is explored
	}
	
	public void requireSystemNotEnteredByPlayerFor(float days) {
		search.systemReqs.add(new StarSystemDaysSincePlayerVisitReq(days));
	}
	public void preferSystemNotEnteredByPlayerFor(float days) {
		search.systemPrefs.add(new StarSystemDaysSincePlayerVisitReq(days));
	}
	
	public void requireSystemExplored() {
		search.systemReqs.add(new StarSystemUnexploredReq(true));
	}
	public void preferSystemExplored() {
		search.systemPrefs.add(new StarSystemUnexploredReq(true));
	}
	public void requireSystemHasNumPlanets(int num) {
		search.systemReqs.add(new StarSystemHasNumPlanetsReq(num));
	}
	public void preferSystemHasNumPlanets(int num) {
		search.systemPrefs.add(new StarSystemHasNumPlanetsReq(num));
	}
	
	public void requireSystemHasNumTerrain(int num) {
		search.systemReqs.add(new StarSystemHasNumTerrainReq(num));
	}
	public void preferSystemHasNumTerrain(int num) {
		search.systemPrefs.add(new StarSystemHasNumTerrainReq(num));
	}
	
	public void requireSystemHasNumPlanetsAndTerrain(int num) {
		search.systemReqs.add(new StarSystemHasNumPlanetsAndTerrainReq(num));
	}
	public void preferSystemHasNumPlanetsAndTerrain(int num) {
		search.systemPrefs.add(new StarSystemHasNumTerrainReq(num));
	}
	
	public void requireSystemIsDense() {
		requireSystemHasNumPlanets(3);
		requireSystemHasNumTerrain(3);
		requireSystemHasNumPlanetsAndTerrain(10);
	}
	public void preferSystemIsDense() {
		preferSystemHasNumPlanets(3);
		preferSystemHasNumTerrain(3);
		preferSystemHasNumPlanetsAndTerrain(10);
	}
	
	public void requireSystemBlackHole() {
		search.systemReqs.add(new SystemIsBlackHoleReq());
	}
	public void requireSystemNebula() {
		search.systemReqs.add(new SystemIsNebulaReq());
	}
	public void requireSystemHasPulsar() {
		search.systemReqs.add(new SystemHasPulsarReq());
	}
	public void preferSystemBlackHole() {
		search.systemPrefs.add(new SystemIsBlackHoleReq());
	}
	public void preferSystemNebula() {
		search.systemPrefs.add(new SystemIsNebulaReq());
	}
	public void preferSystemHasPulsar() {
		search.systemPrefs.add(new SystemHasPulsarReq());
	}
	
	public void requireSystemBlackHoleOrPulsarOrNebula() {
		search.systemReqs.add(new MultipleStarSystemRequirements(ReqMode.ANY, 
				new SystemIsBlackHoleReq(), new SystemHasPulsarReq(), new SystemIsNebulaReq()));
	}
	public void preferSystemBlackHoleOrPulsarOrNebula() {
		search.systemPrefs.add(new MultipleStarSystemRequirements(ReqMode.ANY, 
				new SystemIsBlackHoleReq(), new SystemHasPulsarReq(), new SystemIsNebulaReq()));
	}
	
	public void requireSystemBlackHoleOrNebula() {
		search.systemReqs.add(new MultipleStarSystemRequirements(ReqMode.ANY, 
				new SystemIsBlackHoleReq(), new SystemIsNebulaReq()));
	}
	public void preferSystemBlackHoleOrNebula() {
		search.systemPrefs.add(new MultipleStarSystemRequirements(ReqMode.ANY, 
				new SystemIsBlackHoleReq(), new SystemIsNebulaReq()));
	}
	
	public void requireSystemNotBlackHole() {
		search.systemReqs.add(new SystemIsBlackHoleReq(true));
	}
	public void requireSystemNotNebula() {
		search.systemReqs.add(new SystemIsNebulaReq(true));
	}
	public void requireSystemNotHasPulsar() {
		search.systemReqs.add(new SystemHasPulsarReq(true));
	}
	public void requireSystemNotAlreadyUsedForStory() {
		requireSystemTags(ReqMode.NOT_ANY, Tags.SYSTEM_ALREADY_USED_FOR_STORY);
	}
	
	/**
	 * To avoid re-using the same system for different story things.
	 * @param stage
	 * @param system
	 */
	public void setSystemWasUsedForStory(Object stage, StarSystemAPI system) {
		beginStageTrigger(stage);
		triggerAddTagAfterDelay(0f, system, Tags.SYSTEM_ALREADY_USED_FOR_STORY);
		endTrigger();
	}
	public void preferSystemNotBlackHole() {
		search.systemPrefs.add(new SystemIsBlackHoleReq(true));
	}
	public void preferSystemNotNebula() {
		search.systemPrefs.add(new SystemIsNebulaReq(true));
	}
	public void preferSystemNotPulsar() {
		search.systemPrefs.add(new SystemHasPulsarReq(true));
	}
	
	public void requireSystemHasSafeStars() {
		requireSystemNotBlackHole();
		requireSystemNotHasPulsar();
	}
	
	public static float INNER_SECTOR_PORTION_OF_HEIGHT = 0.7f;
	public static float NON_FRINGE_PORTION_OF_HEIGHT = 0.7f;
	public void requireSystemInInnerSector() {
		search.systemReqs.add(new SystemInInnerSectorReq());
	}
	public void preferSystemInInnerSector() {
		search.systemPrefs.add(new SystemInInnerSectorReq());
	}
	public void requireSystemOnFringeOfSector() {
		search.systemReqs.add(new SystemOnFringeOfSectorReq());
	}
	public void preferSystemOnFringeOfSector() {
		search.systemPrefs.add(new SystemOnFringeOfSectorReq());
	}
	
	public void requireSystemWithinRangeOf(Vector2f location, float rangeLY) {
		search.systemReqs.add(new SystemWithinRangeReq(location, 0, rangeLY));
	}
	public void preferSystemWithinRangeOf(Vector2f location, float rangeLY) {
		search.systemPrefs.add(new SystemWithinRangeReq(location, 0, rangeLY));
	}
	
	public void requireSystemOutsideRangeOf(Vector2f location, float rangeLY) {
		search.systemReqs.add(new SystemWithinRangeReq(location, rangeLY, 1000000f));
	}
	public void preferSystemOutsideRangeOf(Vector2f location, float rangeLY) {
		search.systemPrefs.add(new SystemWithinRangeReq(location, rangeLY, 1000000));
	}
	
	public void requireSystemWithinRangeOf(Vector2f location, float minRangeLY, float maxRangeLY) {
		search.systemReqs.add(new SystemWithinRangeReq(location, minRangeLY, maxRangeLY));
	}
	public void preferSystemWithinRangeOf(Vector2f location, float minRangeLY, float maxRangeLY) {
		search.systemPrefs.add(new SystemWithinRangeReq(location, minRangeLY, maxRangeLY));
	}
	
	public void requirePlanetNotStar() {
		requirePlanetTags(ReqMode.NOT_ANY, Tags.STAR);
	}
	public void requirePlanetIsStar() {
		requirePlanetTags(ReqMode.ALL, Tags.STAR);
	}
	
	public void requirePlanetNotGasGiant() {
		search.planetReqs.add(new PlanetIsGasGiantReq(true));
	}
	public void preferPlanetNonGasGiant() {
		search.planetPrefs.add(new PlanetIsGasGiantReq(true));
	}
	
	public void requirePlanetNotNearJumpPoint(float minDist) {
		search.planetReqs.add(new PlanetOrbitIsNotNearJumpPoint(minDist));
	}
	public void preferPlanetNotNearJumpPoint(float minDist) {
		search.planetPrefs.add(new PlanetOrbitIsNotNearJumpPoint(minDist));
	}
	
	public void requirePlanetIsGasGiant() {
		search.planetReqs.add(new PlanetIsGasGiantReq());
	}
	public void preferPlanetIsGasGiant() {
		search.planetPrefs.add(new PlanetIsGasGiantReq());
	}
	
	public void requirePlanetPopulated() {
		search.planetReqs.add(new PlanetIsPopulatedReq());
	}
	public void preferPlanetPopulated() {
		search.planetPrefs.add(new PlanetIsPopulatedReq());
	}
	public void requirePlanetUnpopulated() {
		search.planetReqs.add(new PlanetIsPopulatedReq(true));
	}
	public void preferPlanetUnpopulated() {
		search.planetPrefs.add(new PlanetIsPopulatedReq(true));
	}
	
	public void requirePlanetTags(ReqMode mode, String ... tags) {
		search.planetReqs.add(new RequiredPlanetTags(mode, tags));
	}
	public void preferPlanetTags(ReqMode mode, String ... tags) {
		search.planetPrefs.add(new RequiredPlanetTags(mode, tags));
	}
	
	public void requirePlanetConditions(ReqMode mode, String ... tags) {
		search.planetReqs.add(new RequiredPlanetConditions(mode, tags));
	}
	public void preferPlanetConditions(ReqMode mode, String ... conditions) {
		search.planetPrefs.add(new RequiredPlanetConditions(mode, conditions));
	}
	
	public void requirePlanetNotFullySurveyed() {
		search.planetReqs.add(new PlanetFullySurveyedReq(true));
	}
	public void preferPlanetNotFullySurveyed() {
		search.planetPrefs.add(new PlanetFullySurveyedReq(true));
	}
	public void requirePlanetFullySurveyed() {
		search.planetReqs.add(new PlanetFullySurveyedReq());
	}
	public void preferPlanetFullySurveyed() {
		search.planetPrefs.add(new PlanetFullySurveyedReq());
	}
	public void preferPlanetUnsurveyed() {
		search.planetPrefs.add(new PlanetUnsurveyedReq());
	}
	public void requirePlanetUnsurveyed() {
		search.planetReqs.add(new PlanetUnsurveyedReq());
	}
	
	public void requirePlanetWithRuins() {
		requirePlanetConditions(ReqMode.ANY, Conditions.RUINS_SCATTERED, Conditions.RUINS_WIDESPREAD,
								Conditions.RUINS_EXTENSIVE, Conditions.RUINS_VAST);
	}
	public void preferPlanetWithRuins() {
		preferPlanetConditions(ReqMode.ANY, Conditions.RUINS_SCATTERED, Conditions.RUINS_WIDESPREAD,
				Conditions.RUINS_EXTENSIVE, Conditions.RUINS_VAST);
	}
	public void requirePlanetWithoutRuins() {
		requirePlanetConditions(ReqMode.NOT_ANY, Conditions.RUINS_SCATTERED, Conditions.RUINS_WIDESPREAD,
				Conditions.RUINS_EXTENSIVE, Conditions.RUINS_VAST);
	}
	public void preferPlanetWithoutRuins() {
		preferPlanetConditions(ReqMode.NOT_ANY, Conditions.RUINS_SCATTERED, Conditions.RUINS_WIDESPREAD,
				Conditions.RUINS_EXTENSIVE, Conditions.RUINS_VAST);
	}
	
	public void requirePlanetUnexploredRuins() {
		search.planetReqs.add(new PlanetUnexploredRuinsReq());
	}
	public void preferPlanetUnexploredRuins() {
		search.planetPrefs.add(new PlanetUnexploredRuinsReq());
	}
	
	
	public void requireEntityTags(ReqMode mode, String ... tags) {
		search.entityReqs.add(new RequiredEntityTags(mode, tags));
	}
	public void preferEntityTags(ReqMode mode, String ... tags) {
		search.entityPrefs.add(new RequiredEntityTags(mode, tags));
	}
	
	public void requireEntityType(String ... types) {
		search.entityReqs.add(new EntityTypeReq(types));
	}
	public void preferEntityType(String ... types) {
		search.entityPrefs.add(new EntityTypeReq(types));
	}
	
	public void requireEntityMemoryFlags(String ... flags) {
		search.entityReqs.add(new EntityMemoryReq(flags));
	}
	public void preferEntityMemoryFlags(String ... flags) {
		search.entityPrefs.add(new EntityMemoryReq(flags));
	}
	
	public void requireEntityUndiscovered() {
		search.entityReqs.add(new EntityUndiscoveredReq(false));
	}
	public void preferEntityUndiscovered() {
		search.entityPrefs.add(new EntityUndiscoveredReq(false));
	}
	
	public void requireEntityNot(final SectorEntityToken entity) {
		search.entityReqs.add(new EntityRequirement() {
			public boolean entityMatchesRequirement(SectorEntityToken param) {
				return entity != param;
			}
		});
	}
	public void requirePlanetNot(final PlanetAPI planet) {
		search.planetReqs.add(new PlanetRequirement() {
			public boolean planetMatchesRequirement(PlanetAPI param) {
				return planet != param;
			}
		});
	}
	public void requireSystemNot(final StarSystemAPI system) {
		if (system == null) return;
		search.systemReqs.add(new StarSystemRequirement() {
			public boolean systemMatchesRequirement(StarSystemAPI param) {
				return system != param;
			}
		});
	}
	
	public void requireSystemIs(final StarSystemAPI system) {
		search.systemReqs.add(new StarSystemRequirement() {
			public boolean systemMatchesRequirement(StarSystemAPI param) {
				return system == param;
			}
		});
	}
	
	public void requireSystem(StarSystemRequirement req) {
		search.systemReqs.add(req);
	}
	public void preferSystem(StarSystemRequirement req) {
		search.systemPrefs.add(req);
	}
	
	
	@SuppressWarnings("unchecked")
	protected void findMatching(List reqs, List prefs, List params,
								List matches, List preferred) {
		if (reqs.isEmpty() && prefs.isEmpty()) {
			matches.addAll(params);
			return;
		}
		
		OUTER: for (Object param : params) {
			for (Object req : reqs) {
				if (!matchesReq((GenericRequirement) req, param)) continue OUTER;
			}
			matches.add(param);
		}
	
		// preferred: check prefs requirements in order, skip any that would produce
		// an empty result so e.g. if A, B, and C are required, then:
		// 1) Retain everything that meets A, say this is not an empty set
		// 2) If A && B is an empty set, skip B
		// 3) Then retain everything that meets C - so, A && C is the final set
		// Note that if the order is [B, A, C], the result would be different - B && C,
		// or just C (if nothing met B)
		List matchingPrefs = new ArrayList(matches);
		boolean foundAny = false;
		for (Object req : prefs) {
			List retain = new ArrayList();
			for (Object curr : matchingPrefs) {
				if (matchesReq((GenericRequirement) req, curr)) {
					retain.add(curr);
				}
			}
			if (retain.isEmpty()) continue;
			foundAny = true;
			matchingPrefs.retainAll(retain);
		}
		if (foundAny) {
			preferred.addAll(matchingPrefs);
		}
	
//		OUTER: for (Object param : params) {
//			for (Object req : prefs) {
//				if (!matchesReq((GenericRequirement) req, param)) continue OUTER;
//			}
//			preferred.add(param);
//		}
	}
	@SuppressWarnings("unchecked")
	public Object pickFromMatching(List matches, List preferred) {
		WeightedRandomPicker pref = new WeightedRandomPicker(genRandom);
		WeightedRandomPicker other = new WeightedRandomPicker(genRandom);
		pref.addAll(preferred);
		other.addAll(matches);
		
		if (!pref.isEmpty()) {
			return pref.pick();
		}
		
		return other.pick();
	}
	
	protected void findMatchingSystems() {
		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_HIDDEN);
		search.matchingSystems = new ArrayList<StarSystemAPI>();
		search.preferredSystems = new ArrayList<StarSystemAPI>();
		findMatching(search.systemReqs, search.systemPrefs, Global.getSector().getStarSystems(),
					 search.matchingSystems, search.preferredSystems);
	}
	
	public StarSystemAPI pickSystem() {
		return pickSystem(true);
	}
	public StarSystemAPI pickSystem(boolean resetSearch) {
		findMatchingSystems();
		StarSystemAPI system = (StarSystemAPI) pickFromMatching(search.matchingSystems, search.preferredSystems);
		if (resetSearch) resetSearch();
		return system;
	}
	
	protected transient boolean makeSystemPreferencesMoreImportant = false;
	public void searchMakeSystemPreferencesMoreImportant(boolean value) {
		makeSystemPreferencesMoreImportant = value;
	}
	
	public PlanetAPI pickPlanet() {
		return pickPlanet(true);
	}
	public PlanetAPI pickPlanet(boolean resetSearch) {
		findMatchingSystems();
		
		List<PlanetAPI> inPreferredSystems = new ArrayList<PlanetAPI>();
		List<PlanetAPI> inMatchingSystems = new ArrayList<PlanetAPI>();
		for (StarSystemAPI system : search.matchingSystems) {
			for (PlanetAPI planet : system.getPlanets()) {
				if (planet.hasTag(Tags.NOT_RANDOM_MISSION_TARGET)) continue;
				inMatchingSystems.add(planet);
			}
			//inMatchingSystems.addAll(system.getPlanets());
		}
		for (StarSystemAPI system : search.preferredSystems) {
			for (PlanetAPI planet : system.getPlanets()) {
				if (planet.hasTag(Tags.NOT_RANDOM_MISSION_TARGET)) continue;
				inPreferredSystems.add(planet);
			}
			//inPreferredSystems.addAll(system.getPlanets());
		}
			
		List<PlanetAPI> matchesInPref = new ArrayList<PlanetAPI>();
		List<PlanetAPI> preferredInPref = new ArrayList<PlanetAPI>();
		findMatching(search.planetReqs, search.planetPrefs, inPreferredSystems, matchesInPref, preferredInPref);
		if (!preferredInPref.isEmpty()) {
			if (resetSearch) resetSearch();
			return (PlanetAPI) pickOneObject(preferredInPref);
		}
		List<PlanetAPI> matchesInMatches = new ArrayList<PlanetAPI>();
		List<PlanetAPI> preferredInMatches = new ArrayList<PlanetAPI>();
		findMatching(search.planetReqs, search.planetPrefs, inMatchingSystems, matchesInMatches, preferredInMatches);
		if (makeSystemPreferencesMoreImportant) {
			if (!matchesInPref.isEmpty()) {
				if (resetSearch) resetSearch();
				return (PlanetAPI) pickOneObject(matchesInPref);
			}
			if (!preferredInMatches.isEmpty()) {
				if (resetSearch) resetSearch();
				return (PlanetAPI) pickOneObject(preferredInMatches);
			}
		} else {
			if (!preferredInMatches.isEmpty()) {
				if (resetSearch) resetSearch();
				return (PlanetAPI) pickOneObject(preferredInMatches);
			}
			if (!matchesInPref.isEmpty()) {
				if (resetSearch) resetSearch();
				return (PlanetAPI) pickOneObject(matchesInPref);
			}
		}
		
		if (resetSearch) resetSearch();
		return (PlanetAPI) pickOneObject(matchesInMatches);
		
//		WeightedRandomPicker<StarSystemAPI> pref = new WeightedRandomPicker<StarSystemAPI>(genRandom);
//		WeightedRandomPicker<StarSystemAPI> other = new WeightedRandomPicker<StarSystemAPI>(genRandom);
//		pref.addAll(search.preferredSystems);
//		other.addAll(search.matchingSystems);
//		
//		WeightedRandomPicker<PlanetAPI> allMatches = new WeightedRandomPicker<PlanetAPI>(genRandom);
//		while (!pref.isEmpty() || !other.isEmpty()) {
//			StarSystemAPI pick = pref.pickAndRemove();
//			if (pick == null) pick = other.pickAndRemove();
//			if (pick == null) break;
//			
//			WeightedRandomPicker<PlanetAPI> matches = new WeightedRandomPicker<PlanetAPI>(genRandom);
//			
//			List<PlanetAPI> planets = new ArrayList<PlanetAPI>(pick.getPlanets());
//			WeightedRandomPicker<PlanetAPI> preferred = new WeightedRandomPicker<PlanetAPI>(genRandom);
//			OUTER: for (PlanetAPI planet : planets) {
//				for (PlanetRequirement req : search.planetReqs) {
//					if (!req.planetMatchesRequirement(planet)) continue OUTER;
//				}
//				allMatches.add(planet);
//				matches.add(planet);
//			}
//			
////			if (planet.getName().equals("Mucalinda")) {
////				System.out.println("32f323223");
////			}
////			if (curr.getName().equals("Megaron")) {
////				System.out.println("32f323223");
////			}
//			
//			List<PlanetAPI> matchingPrefs = new ArrayList<PlanetAPI>(matches.getItems());
//			boolean foundAny = false;
//			for (PlanetRequirement req : search.planetPrefs) {
//				List<PlanetAPI> retain = new ArrayList<PlanetAPI>();
//				for (PlanetAPI curr : matchingPrefs) {
//					if (req.planetMatchesRequirement(curr)) {
//						retain.add(curr);
//					}
//				}
//				if (retain.isEmpty()) continue;
//				foundAny = true;
//				matchingPrefs.retainAll(retain);
//			}
//			if (foundAny) {
//				preferred.addAll(matchingPrefs);
//			}
//			
////			OUTER: for (PlanetAPI planet : matches.getItems()) {
////				for (PlanetRequirement req : search.planetPrefs) {
////					if (!req.planetMatchesRequirement(planet)) continue OUTER;
////				}
////				preferred.add(planet);
////			}
//			
//			if (!preferred.isEmpty()) {
//				if (resetSearch) resetSearch();
//				return preferred.pick();
//			}
//		}
//		
//		if (resetSearch) resetSearch();
//		return allMatches.pick();
	}
	
	public SectorEntityToken pickEntity() {
		return pickEntity(true);
	}
	public SectorEntityToken pickEntity(boolean resetSearch) {
		findMatchingSystems();
		
		List<SectorEntityToken> inPreferredSystems = new ArrayList<SectorEntityToken>();
		List<SectorEntityToken> inMatchingSystems = new ArrayList<SectorEntityToken>();
		for (StarSystemAPI system : search.matchingSystems) {
			List<SectorEntityToken> entities = new ArrayList<SectorEntityToken>(system.getAllEntities());
			for (SectorEntityToken entity : entities) {
				if (entity instanceof AsteroidAPI) continue;
				if (entity.hasTag(Tags.EXPIRES)) continue;
				if (entity.hasTag(Tags.NOT_RANDOM_MISSION_TARGET)) continue;
				//if (!(entity.getCustomPlugin() instanceof DerelictShipEntityPlugin)) continue;
				inMatchingSystems.add(entity);
			}
		}
		for (StarSystemAPI system : search.preferredSystems) {
			List<SectorEntityToken> entities = new ArrayList<SectorEntityToken>(system.getAllEntities());
			for (SectorEntityToken entity : entities) {
				if (entity instanceof AsteroidAPI) continue;
				if (entity.hasTag(Tags.EXPIRES)) continue;
				if (entity.hasTag(Tags.NOT_RANDOM_MISSION_TARGET)) continue;
				inPreferredSystems.add(entity);
			}
		}
			
		List<SectorEntityToken> matchesInPref = new ArrayList<SectorEntityToken>();
		List<SectorEntityToken> preferredInPref = new ArrayList<SectorEntityToken>();
		findMatching(search.entityReqs, search.entityPrefs, inPreferredSystems, matchesInPref, preferredInPref);
		if (!preferredInPref.isEmpty()) {
			if (resetSearch) resetSearch();
			return (SectorEntityToken) pickOneObject(preferredInPref);
		}
		List<SectorEntityToken> matchesInMatches = new ArrayList<SectorEntityToken>();
		List<SectorEntityToken> preferredInMatches = new ArrayList<SectorEntityToken>();
		findMatching(search.entityReqs, search.entityPrefs, inMatchingSystems, matchesInMatches, preferredInMatches);
		if (makeSystemPreferencesMoreImportant) {
			if (!matchesInPref.isEmpty()) {
				if (resetSearch) resetSearch();
				return (SectorEntityToken) pickOneObject(matchesInPref);
			}
			if (!preferredInMatches.isEmpty()) {
				if (resetSearch) resetSearch();
				return (SectorEntityToken) pickOneObject(preferredInMatches);
			}
		} else {
			if (!preferredInMatches.isEmpty()) {
				if (resetSearch) resetSearch();
				return (SectorEntityToken) pickOneObject(preferredInMatches);
			}
			if (!matchesInPref.isEmpty()) {
				if (resetSearch) resetSearch();
				return (SectorEntityToken) pickOneObject(matchesInPref);
			}
		}
		
		if (resetSearch) resetSearch();
		return (SectorEntityToken) pickOneObject(matchesInMatches);
		
		
//		WeightedRandomPicker<StarSystemAPI> pref = new WeightedRandomPicker<StarSystemAPI>(genRandom);
//		WeightedRandomPicker<StarSystemAPI> other = new WeightedRandomPicker<StarSystemAPI>(genRandom);
//		pref.addAll(search.preferredSystems);
//		other.addAll(search.matchingSystems);
//		
//		WeightedRandomPicker<SectorEntityToken> allMatches = new WeightedRandomPicker<SectorEntityToken>(genRandom);
//		while (!pref.isEmpty() || !other.isEmpty()) {
//			StarSystemAPI pick = pref.pickAndRemove();
//			if (pick == null) pick = other.pickAndRemove();
//			if (pick == null) break;
//			
//			WeightedRandomPicker<SectorEntityToken> matches = new WeightedRandomPicker<SectorEntityToken>(genRandom);
//			
//			List<SectorEntityToken> entities = new ArrayList<SectorEntityToken>(pick.getAllEntities());
//			WeightedRandomPicker<SectorEntityToken> preferred = new WeightedRandomPicker<SectorEntityToken>(genRandom);
//			OUTER: for (SectorEntityToken entity : entities) {
//				if (entity instanceof AsteroidAPI) continue;
//				if (entity.hasTag(Tags.EXPIRES)) continue;
//				for (EntityRequirement req : search.entityReqs) {
//					if (!req.entityMatchesRequirement(entity)) continue OUTER;
//				}
//				allMatches.add(entity);
//				matches.add(entity);
//			}
//			
//			
//			List<SectorEntityToken> matchingPrefs = new ArrayList<SectorEntityToken>(matches.getItems());
//			boolean foundAny = false;
//			for (EntityRequirement req : search.entityPrefs) {
//				List<SectorEntityToken> retain = new ArrayList<SectorEntityToken>();
//				for (SectorEntityToken curr : matchingPrefs) {
//					if (req.entityMatchesRequirement(curr)) {
//						retain.add(curr);
//					}
//				}
//				if (retain.isEmpty()) continue;
//				foundAny = true;
//				matchingPrefs.retainAll(retain);
//			}
//			if (foundAny) {
//				preferred.addAll(matchingPrefs);
//			}
//			
////			OUTER: for (SectorEntityToken entity : matches.getItems()) {
////				for (EntityRequirement req : search.entityPrefs) {
////					if (!req.entityMatchesRequirement(entity)) continue OUTER;
////				}
////				preferred.add(entity);
////			}
//			
//			if (!preferred.isEmpty()) {
//				if (resetSearch) resetSearch();
//				return preferred.pick();
//			}
//		}
//		
//		if (resetSearch) resetSearch();
//		return allMatches.pick();
	}
	

	
	protected void findMatchingMarkets() {
		List<MarketAPI> markets = new ArrayList<MarketAPI>();;
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.hasTag(Tags.NOT_RANDOM_MISSION_TARGET)) {
				continue;
			}
			if (market.getPlanetEntity() != null && market.getPlanetEntity().hasTag(Tags.NOT_RANDOM_MISSION_TARGET)) {
				continue;
			}
			markets.add(market);
		}
//		System.out.println("BEGIN");
//		for (MarketAPI curr : markets) {
//			System.out.println(curr.getName());
//		}
//		System.out.println("END");
//		findMatchingSystems();
//		if (!(search.systemPrefs.isEmpty() && search.systemReqs.isEmpty())) {
//			Set<StarSystemAPI> systems = new HashSet<StarSystemAPI>(search.matchingSystems);
//			Iterator<MarketAPI> iter = markets.iterator();
//			while (iter.hasNext()) {
//				MarketAPI curr = iter.next();
//				if (!systems.contains(curr.getStarSystem())) {
//					iter.remove();
//				}
//			}
//		}
		
		search.matchingMarkets = new ArrayList<MarketAPI>();
		search.preferredMarkets = new ArrayList<MarketAPI>();
		findMatching(search.marketReqs, search.marketPrefs, markets,
							search.matchingMarkets, search.preferredMarkets);
	}
	
	public MarketAPI pickMarket() {
		return pickMarket(true);
	}
	public MarketAPI pickMarket(boolean resetSearch) {
		findMatchingMarkets();
		MarketAPI market = (MarketAPI) pickFromMatching(search.matchingMarkets, search.preferredMarkets);
		if (resetSearch) resetSearch();
		
		//if (true) return Global.getSector().getEconomy().getMarket("nomios");
		
		return market;
	}
	
	public CommodityOnMarketAPI pickCommodity() {
		return pickCommodity(true);
	}
	
	public CommodityOnMarketAPI pickCommodity(boolean resetSearch) {
		findMatchingMarkets();
		
		WeightedRandomPicker<CommodityOnMarketAPI> pref = new WeightedRandomPicker<CommodityOnMarketAPI>(genRandom);
		WeightedRandomPicker<CommodityOnMarketAPI> other = new WeightedRandomPicker<CommodityOnMarketAPI>(genRandom);
		
		for (MarketAPI market : search.matchingMarkets) {
			SKIP: for (CommodityOnMarketAPI com : market.getAllCommodities()) {
				if (com.isMeta()) continue;
				if (com.isNonEcon()) continue;
				
				for (CommodityRequirement req : search.commodityReqs) {
					if (!req.commodityMatchesRequirement(com)) continue SKIP;
				}
				other.add(com);
			}
		}
		
		List<CommodityOnMarketAPI> matchingPrefs = new ArrayList<CommodityOnMarketAPI>(other.getItems());
		boolean foundAny = false;
		for (CommodityRequirement req : search.commodityPrefs) {
			List<CommodityOnMarketAPI> retain = new ArrayList<CommodityOnMarketAPI>();
			for (CommodityOnMarketAPI com : matchingPrefs) {
				if (req.commodityMatchesRequirement(com)) {
					retain.add(com);
				}
			}
			if (retain.isEmpty()) continue;
			foundAny = true;
			matchingPrefs.retainAll(retain);
		}
		if (foundAny) {
			pref.addAll(matchingPrefs);
		}

		
		CommodityOnMarketAPI result = pref.pick();
		if (result == null) result = other.pick();
		if (resetSearch) resetSearch();
		
		return result;
	}

	
//	public void requireMarketFactionCustom(String flag) {
//		search.marketReqs.add(new MarketFactionCustomFlagReq(false, flag));
//	}
//	public void preferMarketFactionCustom(String flag) {
//		search.marketPrefs.add(new MarketFactionCustomFlagReq(false, flag));
//	}
//	public void requireMarketFactionNotCustom(String flag) {
//		search.marketReqs.add(new MarketFactionCustomFlagReq(true, flag));
//	}
//	public void preferMarketFactionNotCustom(String flag) {
//		search.marketPrefs.add(new MarketFactionCustomFlagReq(true, flag));
//	}
	
	public void requireMarketTacticallyBombardable() {
		search.marketReqs.add(new MarketTacticalBombardableReq(false));
	}
	public void requireMarketNotTacticallyBombardable() {
		search.marketReqs.add(new MarketTacticalBombardableReq(true));
	}
	public void preferMarketTacticallyBombardable() {
		search.marketPrefs.add(new MarketTacticalBombardableReq(false));
	}
	public void preferMarketNotTacticallyBombardable() {
		search.marketPrefs.add(new MarketTacticalBombardableReq(true));
	}
	public void requireMarketMilitary() {
		search.marketReqs.add(new MarketMilitaryReq());
	}
	public void preferMarketMilitary() {
		search.marketPrefs.add(new MarketMilitaryReq());
	}
	public void requireMarketNotMilitary() {
		search.marketReqs.add(new MarketNotMilitaryReq());
	}
	public void preferMarketNotMilitary() {
		search.marketPrefs.add(new MarketNotMilitaryReq());
	}
	
	public void requireMarketMemoryFlag(String key, Object value) {
		search.marketReqs.add(new MarketMemoryFlagReq(key, value));
	}
	public void preferMarketMemoryFlag(String key, Object value) {
		search.marketPrefs.add(new MarketMemoryFlagReq(key, value));
	}
	
	public void requireMarketHidden() {
		search.marketReqs.add(new MarketHiddenReq());
	}
	public void preferMarketHidden() {
		search.marketPrefs.add(new MarketHiddenReq());
	}
	public void requireMarketNotHidden() {
		search.marketReqs.add(new MarketNotHiddenReq());
	}
	public void preferMarketNotHidden() {
		search.marketPrefs.add(new MarketNotHiddenReq());
	}
	public void requireMarketNotInHyperspace() {
		search.marketReqs.add(new MarketNotInHyperReq());
	}
	public void preferMarketNotInHyperspace() {
		search.marketPrefs.add(new MarketNotInHyperReq());
	}
	
	
	public void requireMarketIs(String id) {
		search.marketReqs.add(new MarketIsReq(Global.getSector().getEconomy().getMarket(id), false));
	}
	public void requireMarketIs(final MarketAPI param) {
		search.marketReqs.add(new MarketIsReq(param, false));
	}
	public void preferMarketIs(final MarketAPI param) {
		search.marketPrefs.add(new MarketIsReq(param, false));
	}
	public void requireMarketIsNot(final MarketAPI param) {
		search.marketReqs.add(new MarketIsReq(param, true));
	}
	public void preferMarketIsNot(final MarketAPI param) {
		search.marketPrefs.add(new MarketIsReq(param, true));
	}
	
	public void requireMarketFaction(String ... factions) {
		search.marketReqs.add(new MarketFactionReq(false, factions));
	}
	public void preferMarketFaction(String ... factions) {
		search.marketPrefs.add(new MarketFactionReq(false, factions));
	}
	public void requireMarketFactionNot(String ... factions) {
		search.marketReqs.add(new MarketFactionReq(true, factions));
	}
	public void preferMarketFactionNot(String ... factions) {
		search.marketPrefs.add(new MarketFactionReq(true, factions));
	}
	
	public void requireMarketFactionNotPlayer() {
		requireMarketFactionNot(Factions.PLAYER);
	}
	
	public void requireMarketFactionHostileTo(String faction) {
		search.marketReqs.add(new MarketFactionHostileReq(false, faction));
	}
	public void preferMarketFactionHostileTo(String faction) {
		search.marketPrefs.add(new MarketFactionHostileReq(false, faction));
	}
	public void requireMarketFactionNotHostileTo(String faction) {
		search.marketReqs.add(new MarketFactionHostileReq(true, faction));
	}
	public void preferMarketFactionNotHostileTo(String faction) {
		search.marketPrefs.add(new MarketFactionHostileReq(true, faction));
	}
	
	protected LocationAPI[] convertLocations(String ... locations) {
		List<LocationAPI> result = new ArrayList<LocationAPI>();
		for (String s : locations) {
			if ("hyperspace".equals(s)) {
				result.add(Global.getSector().getHyperspace());
			} else {
				StarSystemAPI system = Global.getSector().getStarSystem(s);
//				if (Global.getSettings().isDevMode() && system == null) {
//					throw new RuntimeException("Star system named [" + s + "] not found");
//				}
				if (system != null) {
					result.add(system);
				}
			}
		}
		return result.toArray(new LocationAPI[0]);
	}
	public void requireMarketLocation(String ...locations) {
		requireMarketLocation(convertLocations(locations));
	}
	public void preferMarketLocation(String ... locations) {
		preferMarketLocation(convertLocations(locations));
	}
	public void requireMarketLocationNot(String ... locations) {
		requireMarketLocationNot(convertLocations(locations));
	}
	public void preferMarketLocationNot(String ... locations) {
		preferMarketLocationNot(convertLocations(locations));
	}
	public void requireMarketLocation(LocationAPI ... locations) {
		search.marketReqs.add(new MarketLocationReq(false, locations));
	}
	public void preferMarketLocation(LocationAPI ... locations) {
		search.marketPrefs.add(new MarketLocationReq(false, locations));
	}
	public void requireMarketLocationNot(LocationAPI ... locations) {
		search.marketReqs.add(new MarketLocationReq(true, locations));
	}
	public void preferMarketLocationNot(LocationAPI ... locations) {
		search.marketPrefs.add(new MarketLocationReq(true, locations));
	}
	
	public void requireMarketFactionCustom(ReqMode mode, String ... custom) {
		search.marketReqs.add(new MarketFactionCustomReq(mode, custom));
	}
	public void preferMarketFactionCustom(ReqMode mode, String ... custom) {
		search.marketPrefs.add(new MarketFactionCustomReq(mode, custom));
	}
	
	public void requireMarketSizeAtLeast(final int size) {
		search.marketReqs.add(new MarketRequirement() {
			public boolean marketMatchesRequirement(MarketAPI market) {
				return market.getSize() >= size;
			}
		});
	}
	public void preferMarketSizeAtLeast(final int size) {
		search.marketPrefs.add(new MarketRequirement() {
			public boolean marketMatchesRequirement(MarketAPI market) {
				return market.getSize() >= size;
			}
		});
	}
	
	public void requireMarketSizeAtMost(final int size) {
		search.marketReqs.add(new MarketRequirement() {
			public boolean marketMatchesRequirement(MarketAPI market) {
				return market.getSize() <= size;
			}
		});
	}
	public void preferMarketSizeAtMost(final int size) {
		search.marketPrefs.add(new MarketRequirement() {
			public boolean marketMatchesRequirement(MarketAPI market) {
				return market.getSize() <= size;
			}
		});
	}
	
	public void requireMarketStabilityAtLeast(final int stability) {
		search.marketReqs.add(new MarketRequirement() {
			public boolean marketMatchesRequirement(MarketAPI market) {
				return market.getStabilityValue() >= stability;
			}
		});
	}
	public void preferMarketStabilityAtLeast(final int stability) {
		search.marketPrefs.add(new MarketRequirement() {
			public boolean marketMatchesRequirement(MarketAPI market) {
				return market.getStabilityValue() >= stability;
			}
		});
	}
	public void requireMarketStabilityAtMost(final int stability) {
		search.marketReqs.add(new MarketRequirement() {
			public boolean marketMatchesRequirement(MarketAPI market) {
				return market.getStabilityValue() <= stability;
			}
		});
	}
	public void preferMarketStabilityAtMost(final int stability) {
		search.marketPrefs.add(new MarketRequirement() {
			public boolean marketMatchesRequirement(MarketAPI market) {
				return market.getStabilityValue() <= stability;
			}
		});
	}
	
	public void requireMarketConditions(ReqMode mode, String ... conditions) {
		search.marketReqs.add(new RequiredMarketConditions(mode, conditions));
	}
	public void preferMarketConditions(ReqMode mode, String ... conditions) {
		search.marketPrefs.add(new RequiredMarketConditions(mode, conditions));
	}
	
	public void requireMarketIndustries(ReqMode mode, String ... industries) {
		search.marketReqs.add(new RequiredMarketIndustries(mode, industries));
	}
	public void preferMarketIndustries(ReqMode mode, String ... industries) {
		search.marketPrefs.add(new RequiredMarketIndustries(mode, industries));
	}
	
	public void requireMarketIsMilitary() {
		search.marketReqs.add(new MarketRequirement() {
			public boolean marketMatchesRequirement(MarketAPI market) {
				return Misc.isMilitary(market);
			}
		});
	}
	public void preferMarketIsMilitary() {
		search.marketPrefs.add(new MarketRequirement() {
			public boolean marketMatchesRequirement(MarketAPI market) {
				return Misc.isMilitary(market);
			}
		});
	}
	
	public void requireMarketHasSpaceport() {
		search.marketReqs.add(new MarketRequirement() {
			public boolean marketMatchesRequirement(MarketAPI market) {
				return market.hasSpaceport();
			}
		});
	}
	public void preferMarketHasSpaceport() {
		search.marketPrefs.add(new MarketRequirement() {
			public boolean marketMatchesRequirement(MarketAPI market) {
				return market.hasSpaceport();
			}
		});
	}
	
	public void requireMarketNotHasSpaceport() {
		search.marketReqs.add(new MarketRequirement() {
			public boolean marketMatchesRequirement(MarketAPI market) {
				return !market.hasSpaceport();
			}
		});
	}
	public void preferMarketNotHasSpaceport() {
		search.marketPrefs.add(new MarketRequirement() {
			public boolean marketMatchesRequirement(MarketAPI market) {
				return !market.hasSpaceport();
			}
		});
	}
	
	public void requireCommodityIsNotPersonnel() {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return !com.isPersonnel();
			}
		});
	}
	public void preferCommodityIsNotPersonnel() {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return !com.isPersonnel();
			}
		});
	}
	
	public void requireCommodityLegal() {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return !com.isIllegal();
			}
		});
	}
	public void preferCommodityLegal() {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return !com.isIllegal();
			}
		});
	}
	public void requireCommodityIllegal() {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.isIllegal();
			}
		});
	}
	public void preferCommodityIllegal() {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.isIllegal();
			}
		});
	}
	
	public void requireCommodityIs(final String id) {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getId().equals(id);
			}
		});
	}
	public void preferCommodityIs(final String id) {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getId().equals(id);
			}
		});
	}
	
	public void requireCommodityTags(ReqMode mode, String ... tags) {
		search.commodityReqs.add(new RequiredCommodityTags(mode, tags));
	}
	public void preferCommodityTags(ReqMode mode, String ... tags) {
		search.commodityPrefs.add(new RequiredCommodityTags(mode, tags));
	}
	
	public void requireCommodityAvailableAtLeast(final int qty) {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getAvailable() >= qty;
			}
		});
	}
	public void preferCommodityAvailableAtLeast(final int qty) {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getAvailable() >= qty;
			}
		});
	}
	public void requireCommodityAvailableAtMost(final int qty) {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getAvailable() <= qty;
			}
		});
	}
	public void preferCommodityAvailableAtMost(final int qty) {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getAvailable() <= qty;
			}
		});
	}
	
	public void requireCommodityDemandAtLeast(final int qty) {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getMaxDemand() >= qty;
			}
		});
	}
	public void preferCommodityDemandAtLeast(final int qty) {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getMaxDemand() >= qty;
			}
		});
	}
	public void requireCommodityDemandAtMost(final int qty) {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getMaxDemand() <= qty;
			}
		});
	}
	public void preferCommodityDemandAtMost(final int qty) {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getMaxDemand() <= qty;
			}
		});
	}
	
	public void requireCommodityProductionAtLeast(final int qty) {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getMaxSupply() >= qty;
			}
		});
	}
	public void preferCommodityProductionAtLeast(final int qty) {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getMaxSupply() >= qty;
			}
		});
	}
	public void requireCommodityProductionAtMost(final int qty) {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getMaxSupply() <= qty;
			}
		});
	}
	public void preferCommodityProductionAtMost(final int qty) {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getMaxSupply() <= qty;
			}
		});
	}
	
	public void requireCommoditySurplusAtLeast(final int qty) {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				CommodityIconCounts counts = new CommodityIconCounts(com);
				return counts.extra >= qty;
			}
		});
	}
	public void preferCommoditySurplusAtLeast(final int qty) {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				CommodityIconCounts counts = new CommodityIconCounts(com);
				return counts.extra >= qty;
			}
		});
	}
	public void requireCommoditySurplusAtMost(final int qty) {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				CommodityIconCounts counts = new CommodityIconCounts(com);
				return counts.extra <= qty;
			}
		});
	}
	public void preferCommoditySurplusAtMost(final int qty) {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				CommodityIconCounts counts = new CommodityIconCounts(com);
				return counts.extra <= qty;
			}
		});
	}
	
	public void requireCommodityDeficitAtLeast(final int qty) {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				CommodityIconCounts counts = new CommodityIconCounts(com);
				return counts.deficit >= qty;
			}
		});
	}
	public void preferCommodityDeficitAtLeast(final int qty) {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				CommodityIconCounts counts = new CommodityIconCounts(com);
				return counts.deficit >= qty;
			}
		});
	}
	public void requireCommodityDeficitAtMost(final int qty) {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				CommodityIconCounts counts = new CommodityIconCounts(com);
				return counts.deficit <= qty;
			}
		});
	}
	public void preferCommodityDeficitAtMost(final int qty) {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				CommodityIconCounts counts = new CommodityIconCounts(com);
				return counts.deficit <= qty;
			}
		});
	}
	
	public void requireCommodityBasePriceAtLeast(final float price) {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getCommodity().getBasePrice() >= price;
			}
		});
	}
	public void preferCommodityBasePriceAtLeast(final float price) {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getCommodity().getBasePrice() >= price;
			}
		});
	}
	public void requireCommodityBasePriceAtMost(final float price) {
		search.commodityReqs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getCommodity().getBasePrice() <= price;
			}
		});
	}
	public void preferCommodityBasePriceAtMost(final float price) {
		search.commodityPrefs.add(new CommodityRequirement() {
			public boolean commodityMatchesRequirement(CommodityOnMarketAPI com) {
				return com.getCommodity().getBasePrice() <= price;
			}
		});
	}

	public void requireTerrainType(ReqMode mode, String ... types) {
		search.terrainReqs.add(new TerrainTypeReq(mode, types));
	}
	public void preferTerrainType(ReqMode mode, String ... types) {
		search.terrainPrefs.add(new TerrainTypeReq(mode, types));
	}
	
	public void requireTerrainTags(ReqMode mode, String ... tags) {
		search.terrainReqs.add(new RequiredTerrainTags(mode, tags));
	}
	public void preferTerrainTags(ReqMode mode, String ... tags) {
		search.terrainPrefs.add(new RequiredTerrainTags(mode, tags));
	}
	public void requireTerrainHasSpecialName() {
		search.terrainReqs.add(new TerrainHasSpecialNameReq());
	}
	public void preferTerrainHasSpecialName() {
		search.terrainPrefs.add(new TerrainHasSpecialNameReq());
	}
	
	public CampaignTerrainAPI pickTerrain() {
		return pickTerrain(true);
	}
	public CampaignTerrainAPI pickTerrain(boolean resetSearch) {
		findMatchingSystems();
		
		List<CampaignTerrainAPI> inPreferredSystems = new ArrayList<CampaignTerrainAPI>();
		List<CampaignTerrainAPI> inMatchingSystems = new ArrayList<CampaignTerrainAPI>();
		for (StarSystemAPI system : search.matchingSystems) {
			List<CampaignTerrainAPI> terrainList = new ArrayList<CampaignTerrainAPI>(system.getTerrainCopy());
			for (CampaignTerrainAPI terrain : terrainList) {
				if (terrain.hasTag(Tags.EXPIRES)) continue;
				
				// exclude system-wide nebulas
				if (terrain.getPlugin() instanceof BaseTiledTerrain) {
					BaseTiledTerrain btt = (BaseTiledTerrain) terrain.getPlugin();
					if (btt.getTiles() != null && btt.getTiles().length > 50) continue;
				}
				// exclude large rings
				if (terrain.getPlugin() instanceof BaseRingTerrain) {
					BaseRingTerrain rtp = (BaseRingTerrain) terrain.getPlugin();
					if (rtp.getRingParams() != null && rtp.getRingParams().middleRadius > 5000f) continue;
				}
				inMatchingSystems.add(terrain);
			}
		}
		for (StarSystemAPI system : search.preferredSystems) {
			List<CampaignTerrainAPI> terrainList = new ArrayList<CampaignTerrainAPI>(system.getTerrainCopy());
			for (CampaignTerrainAPI terrain : terrainList) {
				if (terrain.hasTag(Tags.EXPIRES)) continue;
				
				// exclude system-wide nebulas
				if (terrain.getPlugin() instanceof BaseTiledTerrain) {
					BaseTiledTerrain btt = (BaseTiledTerrain) terrain.getPlugin();
					if (btt.getTiles() != null && btt.getTiles().length > 50) continue;
				}
				// exclude large rings
				if (terrain.getPlugin() instanceof BaseRingTerrain) {
					BaseRingTerrain rtp = (BaseRingTerrain) terrain.getPlugin();
					if (rtp.getRingParams() != null && rtp.getRingParams().middleRadius > 5000f) continue;
				}
				inMatchingSystems.add(terrain);
			}
		}
			
		List<CampaignTerrainAPI> matchesInPref = new ArrayList<CampaignTerrainAPI>();
		List<CampaignTerrainAPI> preferredInPref = new ArrayList<CampaignTerrainAPI>();
		findMatching(search.terrainReqs, search.terrainPrefs, inPreferredSystems, matchesInPref, preferredInPref);
		if (!preferredInPref.isEmpty()) {
			if (resetSearch) resetSearch();
			return (CampaignTerrainAPI) pickOneObject(preferredInPref);
		}
		List<CampaignTerrainAPI> matchesInMatches = new ArrayList<CampaignTerrainAPI>();
		List<CampaignTerrainAPI> preferredInMatches = new ArrayList<CampaignTerrainAPI>();
		findMatching(search.terrainReqs, search.terrainPrefs, inMatchingSystems, matchesInMatches, preferredInMatches);
		if (makeSystemPreferencesMoreImportant) {
			if (!matchesInPref.isEmpty()) {
				if (resetSearch) resetSearch();
				return (CampaignTerrainAPI) pickOneObject(matchesInPref);
			}
			if (!preferredInMatches.isEmpty()) {
				if (resetSearch) resetSearch();
				return (CampaignTerrainAPI) pickOneObject(preferredInMatches);
			}
		} else {
			if (!preferredInMatches.isEmpty()) {
				if (resetSearch) resetSearch();
				return (CampaignTerrainAPI) pickOneObject(preferredInMatches);
			}
			if (!matchesInPref.isEmpty()) {
				if (resetSearch) resetSearch();
				return (CampaignTerrainAPI) pickOneObject(matchesInPref);
			}
		}
		
		if (resetSearch) resetSearch();
		return (CampaignTerrainAPI) pickOneObject(matchesInMatches);
		
		
		
		
//		WeightedRandomPicker<StarSystemAPI> pref = new WeightedRandomPicker<StarSystemAPI>(genRandom);
//		WeightedRandomPicker<StarSystemAPI> other = new WeightedRandomPicker<StarSystemAPI>(genRandom);
//		pref.addAll(search.preferredSystems);
//		other.addAll(search.matchingSystems);
//		
//		WeightedRandomPicker<CampaignTerrainAPI> allMatches = new WeightedRandomPicker<CampaignTerrainAPI>(genRandom);
//		while (!pref.isEmpty() || !other.isEmpty()) {
//			StarSystemAPI pick = pref.pickAndRemove();
//			if (pick == null) pick = other.pickAndRemove();
//			if (pick == null) break;
//			
//			WeightedRandomPicker<CampaignTerrainAPI> matches = new WeightedRandomPicker<CampaignTerrainAPI>(genRandom);
//			
//			List<CampaignTerrainAPI> terrainList = new ArrayList<CampaignTerrainAPI>(pick.getTerrainCopy());
//			WeightedRandomPicker<CampaignTerrainAPI> preferred = new WeightedRandomPicker<CampaignTerrainAPI>(genRandom);
//			OUTER: for (CampaignTerrainAPI terrain : terrainList) {
//				if (terrain.hasTag(Tags.EXPIRES)) continue;
//				for (TerrainRequirement req : search.terrainReqs) {
//					if (!req.terrainMatchesRequirement(terrain)) continue OUTER;
//				}
//				allMatches.add(terrain);
//				matches.add(terrain);
//			}
//			
//			
//			List<CampaignTerrainAPI> matchingPrefs = new ArrayList<CampaignTerrainAPI>(matches.getItems());
//			boolean foundAny = false;
//			for (TerrainRequirement req : search.terrainPrefs) {
//				List<CampaignTerrainAPI> retain = new ArrayList<CampaignTerrainAPI>();
//				for (CampaignTerrainAPI curr : matchingPrefs) {
//					if (curr.hasTag(Tags.EXPIRES)) continue;
//					if (req.terrainMatchesRequirement(curr)) {
//						retain.add(curr);
//					}
//				}
//				if (retain.isEmpty()) continue;
//				foundAny = true;
//				matchingPrefs.retainAll(retain);
//			}
//			if (foundAny) {
//				preferred.addAll(matchingPrefs);
//			}
//			
//			if (!preferred.isEmpty()) {
//				if (resetSearch) resetSearch();
//				return preferred.pick();
//			}
//		}
//		
//		if (resetSearch) resetSearch();
//		return allMatches.pick();
	}
}





