package com.fs.starfarer.api.impl.campaign.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.ActionType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ObjectiveEventListener;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript.MilitaryResponseParams;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.Objectives;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.plugins.BuildObjectiveTypePicker;
import com.fs.starfarer.api.plugins.BuildObjectiveTypePicker.BuildObjectiveParams;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class WarSimScript implements EveryFrameScript, ObjectiveEventListener {

	public static enum LocationDanger {
		NONE(0.01f),
		MINIMAL(0.1f),
		LOW(0.2f),
		MEDIUM(0.3f),
		HIGH(0.5f),
		EXTREME(0.8f),
		;
		
		public static LocationDanger [] vals = values();
		
		public float enemyStrengthFraction;
		private LocationDanger(float enemyStrengthFraction) {
			this.enemyStrengthFraction = enemyStrengthFraction;
		}
		
		public LocationDanger next() {
			int index = this.ordinal() + 1;
			if (index >= vals.length) index = vals.length - 1;
			return vals[index];
		}
		public LocationDanger prev() {
			int index = this.ordinal() - 1;
			if (index < 0) index = 0;
			return vals[index];
		}

	}
	
	
	
	
	
	public static final String KEY = "$core_warSimScript";
	
	public static final float CHECK_DAYS = 10f;
	public static final float CHECK_PROB = 0.5f;
	
	
	public static WarSimScript getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (WarSimScript) test; 
	}
	
	protected TimeoutTracker<String> timeouts = new TimeoutTracker<String>();

	protected List<StarSystemAPI> queue = new ArrayList<StarSystemAPI>();
	
	public WarSimScript() {
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		Global.getSector().getListenerManager().addListener(this);
		
		for (StarSystemAPI system : Global.getSector().getEconomy().getStarSystemsWithMarkets()) {
			String sid = getStarSystemTimeoutId(system);
			timeouts.add(sid, 2f + (float) Math.random() * 3f);
		}
	}
	
	protected Object readResolve() {
		if (timeouts == null) {
			timeouts = new TimeoutTracker<String>();
		}
		return this;
	}
	
	public void advance(float amount) {
		//if (true) return;
		
		if (TutorialMissionIntel.isTutorialInProgress()) {
			return;
		}
		
		float days = Misc.getDays(amount);
		
		timeouts.advance(days);
		
		if (queue.isEmpty()) {
			queue = Global.getSector().getEconomy().getStarSystemsWithMarkets();
		}
		
		if (!queue.isEmpty()) {
			StarSystemAPI curr = queue.remove(0);
			processStarSystem(curr);
		}
	}

	public void processStarSystem(StarSystemAPI system) {
		String sid = getStarSystemTimeoutId(system);
		if (timeouts.contains(sid)) return;
		timeouts.add(sid, 2f + (float) Math.random() * 3f);
		
		CountingMap<FactionAPI> str = getFactionStrengths(system);

		boolean inSpawnRange = RouteManager.isPlayerInSpawnRange(system.getCenter());
		
		List<FactionAPI> factions = new ArrayList<FactionAPI>(str.keySet());
		
//		if (system.getName().toLowerCase().contains("old milix")) {
//			System.out.println("wefwefwe");
//		}
		
//		if (system.isCurrentLocation()) {
//			System.out.println("ff23f23f32");
//		}
		
		for (SectorEntityToken obj : system.getEntitiesWithTag(Tags.OBJECTIVE)) {
			List<FactionAPI> contenders = new ArrayList<FactionAPI>();
			
			// figure out if anyone that doesn't own it thinks they should own it
			for (FactionAPI faction : factions) {
				if (wantsToOwnObjective(faction, str, obj)) {
					contenders.add(faction);
					String id = getControlTimeoutId(obj, faction);
					if (!timeouts.contains(id)) {
						addObjectiveActionResponse(obj, faction, obj.getFaction());
					}
				} else if (faction == obj.getFaction()) {
					contenders.add(faction);
				}
			}
			
			if (!inSpawnRange) {
				String id = getControlSimTimeoutId(obj);
				if (timeouts.contains(id)) continue;
				
				timeouts.add(id, 10f + (float) Math.random() * 30f);
				
				WeightedRandomPicker<FactionAPI> picker = new WeightedRandomPicker<FactionAPI>();
				float max = 0f;
				for (FactionAPI faction : contenders) {
					float curr = str.getCount(faction) + getStationStrength(faction, system, obj);
					if (curr > max) {
						max = curr;
					}
				}
				if (max <= 0) continue;
				
				for (FactionAPI faction : contenders) {
					float curr = str.getCount(faction) + getStationStrength(faction, system, obj);
					float w = (curr / max) - 0.5f;
					picker.add(faction, w);
				}
				
				FactionAPI winner = picker.pick();
				if (winner != null && winner != obj.getFaction()) {
					Objectives o = new Objectives(obj);
					o.control(winner.getId());
				}
			}
		}
		
		
		for (SectorEntityToken sLoc : system.getEntitiesWithTag(Tags.STABLE_LOCATION)) {
			if (sLoc.hasTag(Tags.NON_CLICKABLE)) continue;
			if (sLoc.hasTag(Tags.FADING_OUT_AND_EXPIRING)) continue;
			if (!inSpawnRange) {
				String id = getBuildSimTimeoutId(sLoc);
				if (timeouts.contains(id)) continue;
				
				timeouts.add(id, 20f + (float) Math.random() * 20f);
				
				WeightedRandomPicker<FactionAPI> picker = new WeightedRandomPicker<FactionAPI>();
				float max = 0f;
				for (FactionAPI faction : factions) {
					float curr = str.getCount(faction) + getStationStrength(faction, system, sLoc);
					if (curr > max) {
						max = curr;
					}
				}
				if (max <= 0) continue;
				
				for (FactionAPI faction : factions) {
					float curr = str.getCount(faction) + getStationStrength(faction, system, sLoc);
					float w = (curr / max) - 0.5f;
					picker.add(faction, w);
				}
				
				FactionAPI winner = picker.pick();
				if (winner != null && winner != sLoc.getFaction()) {
					BuildObjectiveParams params = new BuildObjectiveParams();
					params.faction = winner;
					params.fleet = null;
					params.stableLoc = sLoc;
					BuildObjectiveTypePicker pick = Global.getSector().getGenericPlugins().pickPlugin(BuildObjectiveTypePicker.class, params);
					String type = null;
					if (pick != null) {
						type = pick.pickObjectiveToBuild(params);
					}
					if (type != null) {
						Objectives o = new Objectives(sLoc);
						o.build(type, winner.getId());
					}
				}
			}
		}
	}
	
	/**
	 * If it doesn't already own it, it's owned by an enemy, and the faction either
	 * has the closest market to it or is the strongest in-system faction.
	 * 
	 * Or: owned by a non-hostile faction that has no colony presence in the system, and this faction does
	 * @param faction
	 * @param str
	 * @param o
	 * @return
	 */
	protected boolean wantsToOwnObjective(FactionAPI faction, CountingMap<FactionAPI> str, SectorEntityToken o) {
		if (o.getFaction() == faction) return false;
		if (!o.getFaction().isHostileTo(faction) && !o.getFaction().isNeutralFaction()) {
//			for (MarketAPI curr : Misc.getMarketsInLocation(o.getContainingLocation())) {
//				if (curr.getFaction() == o.getFaction() && 
//						!curr.getFaction().isNeutralFaction() &&
//						!curr.getFaction().isPlayerFaction()) {
//					return false;
//				}
//			}
//			return true;
			
			boolean ownerHasColonyInSystem = false;
			for (MarketAPI curr : Misc.getMarketsInLocation(o.getContainingLocation())) {
				if (curr.getFaction() == o.getFaction() && 
						!curr.getFaction().isNeutralFaction()) {
					ownerHasColonyInSystem = true;
					break;
				}
			}
			if (ownerHasColonyInSystem) return false;
			return true;
		}
		
		float minDist = Float.MAX_VALUE;
		MarketAPI closest = null;
		boolean haveInSystemMarkets = false;
		for (MarketAPI market : Misc.getMarketsInLocation(o.getContainingLocation())) {
			float dist = Misc.getDistance(market.getPrimaryEntity(), o);
			if (dist < minDist) {
				minDist = dist;
				closest = market;
			}
			if (faction == market.getFaction()) {
				haveInSystemMarkets = true;
			}
		}
		
		if (closest != null && closest.getFaction() == faction) {
			return true;
		}
		
		// pirate-like factions will try to pick up objectives that are far away from any markets
		if (faction.getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR)) {
			if (minDist > 8000) {
				return true;
			}
		}
		
		if (!haveInSystemMarkets && closest != null && !closest.getFaction().isHostileTo(faction)) {
			return false;
		}
		
		int maxStr = 0;
		FactionAPI strongest = null;
		for (FactionAPI curr : str.keySet()) {
			int s = str.getCount(curr);
			if (s > maxStr) {
				maxStr = s;
				strongest = curr;
			}
		}
		
		return strongest == faction;
	}
	


	public void reportObjectiveChangedHands(SectorEntityToken objective, FactionAPI from, FactionAPI to) {
		addObjectiveActionResponse(objective, from, to);
	}


	public void reportObjectiveDestroyed(SectorEntityToken objective, SectorEntityToken stableLocation, FactionAPI enemy) {
		String id = getBuildSimTimeoutId(stableLocation);
		timeouts.add(id, 40f + (float) Math.random() * 20f, 100f);
		
		addObjectiveActionResponse(objective, objective.getFaction(), null);
	}

	
	protected String getStarSystemTimeoutId(StarSystemAPI system) {
		String id = "starsystem_" + system.getId();
		return id;
	}
	
	protected String getBuildSimTimeoutId(SectorEntityToken objective) {
		String id = "sim_build_" + objective.getId();
		return id;
	}
	
	protected String getControlSimTimeoutId(SectorEntityToken objective) {
		String id = "sim_changedhands_" + objective.getId();
		return id;
	}
	
	protected String getControlTimeoutId(SectorEntityToken objective, FactionAPI faction) {
		String id = faction.getId() + "_" + objective.getId();
		return id;
	}
	
	protected void addObjectiveActionResponse(SectorEntityToken objective, FactionAPI faction, FactionAPI enemy) {
		if (faction.isNeutralFaction()) return;
		if (faction.getCustomBoolean(Factions.CUSTOM_NO_WAR_SIM)) return;
		
		if (enemy != null && enemy.isNeutralFaction()) return;
		if (enemy != null && !faction.isHostileTo(enemy)) return;
		
		String id = getControlTimeoutId(objective, faction);
		if (timeouts.contains(id)) return;
		
		if (isAlreadyFightingFor(objective, faction)) { // an MRS from some other source, such as a raid
			return;
		}
		
		MilitaryResponseParams params = new MilitaryResponseParams(ActionType.HOSTILE, 
				objective.getId(), 
				faction,
				objective,
				0.4f,
				20f + (float) Math.random() * 20f);
		MilitaryResponseScript script = new MilitaryResponseScript(params);
		objective.getContainingLocation().addScript(script);
		
		timeouts.add(id, params.responseDuration * 2f);
	}
	
	
	
	
	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}

	
	
	
	public static CountingMap<FactionAPI> getFactionStrengths(StarSystemAPI system) {
		CountingMap<FactionAPI> result = new CountingMap<FactionAPI>();

		Set<FactionAPI> factions = new LinkedHashSet<FactionAPI>();
//		if (system.getName().startsWith("Askonia")) {
//			System.out.println("wefewfew");
//		}
		
		for (CampaignFleetAPI fleet : system.getFleets()) {
			if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_TRADE_FLEET)) continue;
			if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_SMUGGLER)) continue;
			factions.add(fleet.getFaction());
		}
		
		for (RouteData route : RouteManager.getInstance().getRoutesInLocation(system)) {
			String id = route.getFactionId();
			if (id == null) continue;
			FactionAPI faction = Global.getSector().getFaction(id);
			factions.add(faction);
		}
		
		for (FactionAPI faction : factions) {
			if (faction.getCustomBoolean(Factions.CUSTOM_NO_WAR_SIM)) continue;
			
			int strength = (int) getFactionStrength(faction, system);
			if (strength > 0) {
				result.add(faction, strength);
			}
		}
		return result;
	}
	
	
	
	public static float getRelativeEnemyStrength(String factionId, StarSystemAPI system) {
		float enemyStrength = getEnemyStrength(factionId, system);
		float factionStrength = getFactionStrength(factionId, system);
		float f = enemyStrength / Math.max(1f, factionStrength + enemyStrength);
		return f;
	}
	
	public static float getRelativeFactionStrength(String factionId, StarSystemAPI system) {
		float enemyStrength = getEnemyStrength(factionId, system);
		float factionStrength = getFactionStrength(factionId, system);
		float f = factionStrength / Math.max(1f, factionStrength + enemyStrength);
		return f;
	}
	
	public static float getEnemyStrength(String factionId, StarSystemAPI system) {
		return getEnemyStrength(Global.getSector().getFaction(factionId), system, false);
	}
	public static float getEnemyStrength(FactionAPI faction, StarSystemAPI system) {
		return getEnemyStrength(faction, system, false);
	}
	public static float getEnemyStrength(String factionId, StarSystemAPI system, boolean assumeHostileToPlayer) {
		return getEnemyStrength(Global.getSector().getFaction(factionId), system, assumeHostileToPlayer);
	}
	public static float getEnemyStrength(FactionAPI faction, StarSystemAPI system, boolean assumeHostileToPlayer) {
		float enemyStr = 0;
		Set<String> seen = new HashSet<String>();
		if (EconomyFleetRouteManager.ENEMY_STRENGTH_CHECK_EXCLUDE_PIRATES) {
			seen.add(Factions.PIRATES);
		}
		
		
		for (MarketAPI target : Misc.getMarketsInLocation(system)) {
			if (!(assumeHostileToPlayer && target.getFaction().isPlayerFaction())) {
				if (!target.getFaction().isHostileTo(faction)) continue;
			}
			
			if (seen.contains(target.getFactionId())) continue;
			seen.add(target.getFactionId());
			enemyStr += WarSimScript.getFactionStrength(target.getFaction(), system);
		}
		
		if (faction.isPlayerFaction()) {
			HostileActivityEventIntel intel = HostileActivityEventIntel.get();
			//HostileActivityIntel intel = HostileActivityIntel.get(system);
			if (intel != null) {
				enemyStr += intel.getVeryApproximateFPStrength(system);
			}
		}
		
		return enemyStr;
	}
	
	public static float getFactionStrength(String factionId, StarSystemAPI system) {
		return getFactionStrength(Global.getSector().getFaction(factionId), system);
	}
	public static float getFactionStrength(FactionAPI faction, StarSystemAPI system) {
		float strength = 0f;
		
//		if (system.getName().toLowerCase().contains("naraka") && Factions.PIRATES.equals(faction.getId())) {
//			System.out.println("wefwefwe");
//		}
		
		Set<CampaignFleetAPI> seenFleets = new HashSet<CampaignFleetAPI>();
		for (CampaignFleetAPI fleet : system.getFleets()) {
			if (fleet.getFaction() != faction) continue;
			if (fleet.isStationMode()) continue;
			if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_TRADE_FLEET)) continue;
			if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_SMUGGLER)) continue;
			
			if (fleet.isPlayerFleet()) continue;
			
//			if (EconomyFleetRouteManager.FACTION_STRENGTH_CHECK_EXCLUDE_PIRATES && 
//					fleet.getFaction().getId().equals(Factions.PIRATES)) {
//				continue;
//			}
			
			strength += fleet.getEffectiveStrength();
			
			seenFleets.add(fleet);
		}
		
		for (RouteData route : RouteManager.getInstance().getRoutesInLocation(system)) {
			if (route.getActiveFleet() != null && seenFleets.contains(route.getActiveFleet())) continue;
		
			OptionalFleetData data = route.getExtra();
			if (data == null) continue;
			if (route.getFactionId() == null) continue;
			if (!faction.getId().equals(route.getFactionId())) continue;
			
//			if (EconomyFleetRouteManager.FACTION_STRENGTH_CHECK_EXCLUDE_PIRATES && 
//					route.getFactionId().equals(Factions.PIRATES)) {
//				continue;
//			}
			
			strength += data.getStrengthModifiedByDamage();
		}
		
		return strength;
	}
	
	
	public static float getStationStrength(FactionAPI faction, StarSystemAPI system, SectorEntityToken from) {
		float strength = 0f;
		
		for (CampaignFleetAPI fleet : system.getFleets()) {
			if (!fleet.isStationMode()) continue;
			if (fleet.getFaction() != faction) continue;
			
			float maxDist = Misc.getBattleJoinRange() * 3f;
			
			float dist = Misc.getDistance(from, fleet);
			if (dist < maxDist) {
				strength += fleet.getEffectiveStrength();
			}
		}
		
		return strength;
	}

	public TimeoutTracker<String> getTimeouts() {
		return timeouts;
	}
	
	
	public static void removeFightOrdersFor(SectorEntityToken target, FactionAPI faction) {
		for (EveryFrameScript s : target.getContainingLocation().getScripts()) {
			if (s instanceof MilitaryResponseScript) {
				MilitaryResponseScript script = (MilitaryResponseScript) s;
				if (script.getParams() != null && script.getParams().target == target &&
						script.getParams().faction == faction) {
					script.forceDone();
				}
			}
		}
	}
	
	public static void setNoFightingForObjective(SectorEntityToken objective, FactionAPI faction, float timeout) {
		removeFightOrdersFor(objective, faction);
		if (timeout > 0) {
			WarSimScript wss = getInstance();
			String id = wss.getControlTimeoutId(objective, faction);
			wss.timeouts.add(id, timeout);
		}
	}
	
	public static void removeNoFightingTimeoutForObjective(SectorEntityToken objective, FactionAPI faction) {
		WarSimScript wss = getInstance();
		String id = wss.getControlTimeoutId(objective, faction);
		wss.timeouts.remove(id);
	}
	
	public static boolean isAlreadyFightingFor(SectorEntityToken objective, FactionAPI faction) {
		for (EveryFrameScript s : objective.getContainingLocation().getScripts()) {
			if (s instanceof MilitaryResponseScript) {
				MilitaryResponseScript script = (MilitaryResponseScript) s;
				if (script.getParams() != null && script.getParams().target == objective &&
						script.getParams().faction == faction) {
					return true;
				}
			}
		}
		return false;
		
	}
	
	public static LocationDanger getDangerFor(FactionAPI faction, StarSystemAPI system) {
		if (system == null) return LocationDanger.NONE;
		return getDangerFor(getFactionStrength(faction, system), getEnemyStrength(faction, system));
	}
	public static LocationDanger getDangerFor(String factionId, StarSystemAPI system) {
		if (system == null) return LocationDanger.NONE;
		return getDangerFor(getFactionStrength(factionId, system), getEnemyStrength(factionId, system));
	}
	public static LocationDanger getDangerFor(float factionStrength, float enemyStrength) {
		if (enemyStrength < 100) return LocationDanger.NONE;
		
		float f = enemyStrength / Math.max(1f, factionStrength + enemyStrength);
		for (LocationDanger level : LocationDanger.vals) {
			float test = level.enemyStrengthFraction + (level.next().enemyStrengthFraction - level.enemyStrengthFraction) * 0.5f;
			if (level == LocationDanger.NONE) test = LocationDanger.NONE.enemyStrengthFraction;
			if (test >= f) {
				return level;
			}
		}
		return LocationDanger.EXTREME;
	}
}






