package com.fs.starfarer.api.impl.campaign.intel.group;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.ActionType;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript.MilitaryResponseParams;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI.GenericPayloadAction;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI.FleetActionDelegate;
import com.fs.starfarer.api.impl.campaign.procgen.themes.WarfleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.BombardType;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class FGRaidAction extends FGDurationAction implements FleetActionDelegate, GenericPayloadAction {

	public static enum FGRaidType {
		CONCURRENT,
		SEQUENTIAL,
	}
	
	
	public static class FGRaidParams {
		public StarSystemAPI where;
		public FGRaidType type = FGRaidType.CONCURRENT;
		public boolean doNotGetSidetracked = true;
		public boolean tryToCaptureObjectives = true;
		public boolean allowAnyHostileMarket = false;
		public float maxDurationIfSpawnedFleetsConcurrent = 90f;
		public float maxDurationIfSpawnedFleetsPerSequentialStage = 45f;
		public int maxStabilityLostPerRaid = 3;
		public int raidsPerColony = 2;
		
		public String raidApproachText = null;
		public String raidActionText = null;
		public String targetTravelText = null;
		public boolean appendTargetNameToTravelText = true;
		public String inSystemActionText = null;
		public BombardType bombardment = null;
		public List<String> disrupt = new ArrayList<String>();
		
		public boolean allowNonHostileTargets = false;
		public List<MarketAPI> allowedTargets = new ArrayList<MarketAPI>();
		
		public void setBombardment(BombardType type) {
			this.bombardment = type;
			raidApproachText = "moving to bombard";
			raidActionText = "bombarding";
			raidsPerColony = 1;
		}
		
		public void setDisrupt(String ... industries) {
			for (String id : industries) {
				disrupt.add(id);
			}
			raidsPerColony = Math.min(disrupt.size(), 3);
			if (raidsPerColony < 1) raidsPerColony = 1;
		}
	}
	
	public static class RaidSubstage {
		protected boolean started = false;
		public float maxDuration;
		//public int raidsPerformed = 0;
		public List<SectorEntityToken> objectives = new ArrayList<SectorEntityToken>();
		public List<SectorEntityToken> markets = new ArrayList<SectorEntityToken>();
		public List<SectorEntityToken> finishedRaiding = new ArrayList<SectorEntityToken>();
		
		protected Object readResolve() {
			if (objectives == null) {
				objectives = new ArrayList<SectorEntityToken>();
			}
			if (markets == null) {
				markets = new ArrayList<SectorEntityToken>();
			}
			if (finishedRaiding == null) {
				finishedRaiding = new ArrayList<SectorEntityToken>();
			}
			return this;
		}
		
		public boolean allGoalsAchieved(FGRaidAction action) {
			if (markets.isEmpty()) {
				for (SectorEntityToken curr : objectives) {
					if (curr.getFaction() != action.intel.getFaction()) {
						return false;
					}
				}
			}
			for (SectorEntityToken curr : markets) {
				if (action.raidCount.getCount(curr.getMarket()) < action.params.raidsPerColony) {
					return false;
				}
						
			}
			return true;
		}
	}
	
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);
	
	
	protected boolean computedSubstages = false;
	
	protected FGRaidParams params;
	protected CountingMap<MarketAPI> raidCount = new CountingMap<MarketAPI>();
	protected int bombardCount = 0;
	protected List<RaidSubstage> stages = new ArrayList<FGRaidAction.RaidSubstage>();
	protected List<MilitaryResponseScript> scripts = new ArrayList<MilitaryResponseScript>();
	protected float originalDuration = 0f;

	public FGRaidAction(FGRaidParams params, float raidDays) {
		super(raidDays);
		originalDuration = raidDays;
		this.params = params;
		
		interval.forceIntervalElapsed();
	}
	
	public Object readResolve() {
		if (raidCount == null) {
			raidCount = new CountingMap<MarketAPI>();
		}
		return this;
	}

	@Override
	public void addRouteSegment(RouteData route) {
		RouteSegment segment = new RouteSegment(getDurDays(), params.where.getCenter());
		route.addSegment(segment);
	}

	
	@Override
	public void notifyFleetsSpawnedMidSegment(RouteSegment segment) {
		super.notifyFleetsSpawnedMidSegment(segment);
	}

	@Override
	public void notifySegmentFinished(RouteSegment segment) {
		super.notifySegmentFinished(segment);
		
		autoresolve();
	}
	
	protected void computeSubstages() {
		List<CampaignFleetAPI> fleets = intel.getFleets();
		if (fleets.isEmpty()) return;
		
//		params.maxDurationIfSpawnedFleetsConcurrent = 90f;
//		params.maxDurationIfSpawnedFleetsPerSequentialStage = 45f;
//		params.maxStabilityLostPerRaid = 3;
//		params.raidsPerColony = 2;
//		params.doNotGetSidetracked = true;
//		params.appendTargetNameToTravelText = true;
//		params.type = FGRaidType.SEQUENTIAL;
//		intel.setApproximateNumberOfFleets(5);
		
		
		for (CampaignFleetAPI fleet : fleets) {
			if (!fleet.hasScriptOfClass(WarfleetAssignmentAI.class)) {
				WarfleetAssignmentAI script = new WarfleetAssignmentAI(fleet, true, true);
				script.setDelegate(this);
				fleet.addScript(script);
			}
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_WAR_FLEET, true);
			fleet.getMemoryWithoutUpdate().unset(MemFlags.FLEET_BUSY);
			//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
		}
		
		List<MarketAPI> sortedTargetMarkets = new ArrayList<MarketAPI>();
		for (MarketAPI market : Misc.getMarketsInLocation(params.where)) {
			if (!params.allowAnyHostileMarket && !params.allowedTargets.contains(market)) continue;
			if (!params.allowNonHostileTargets && !intel.getFaction().isHostileTo(market.getFaction())) continue;
		
			sortedTargetMarkets.add(market);
		}
		
		final Vector2f sortLoc = new Vector2f(fleets.get(0).getLocation());
		Collections.sort(sortedTargetMarkets, new Comparator<MarketAPI>() {
			public int compare(MarketAPI o1, MarketAPI o2) {
				float d1 = Misc.getDistance(sortLoc, o1.getPrimaryEntity().getLocation());
				float d2 = Misc.getDistance(sortLoc, o2.getPrimaryEntity().getLocation());
				return (int) Math.signum(d1 - d2);
			}
		});
		
		// otherwise, WasSimScript adds extra MilitaryResponseScripts for objectives and
		// attacking fleets go there almost to the exclusion of other targets
		for (SectorEntityToken objective : params.where.getEntitiesWithTag(Tags.OBJECTIVE)) {
			WarSimScript.setNoFightingForObjective(objective, intel.getFaction(), 1000f);
		}
		
		List<SectorEntityToken> objectives = new ArrayList<SectorEntityToken>();
		float minDist = Float.MAX_VALUE;
		SectorEntityToken closest = null;
		for (SectorEntityToken objective : params.where.getEntitiesWithTag(Tags.NAV_BUOY)) {
			float dist = Misc.getDistance(sortLoc, objective.getLocation());
			if (dist < minDist) {
				closest = objective;
				minDist = dist;
			}
		}
		if (closest != null) {
			objectives.add(closest);
		}
		
		
		minDist = Float.MAX_VALUE;
		closest = null;
		for (SectorEntityToken objective : params.where.getEntitiesWithTag(Tags.SENSOR_ARRAY)) {
			float dist = Misc.getDistance(sortLoc, objective.getLocation());
			if (dist < minDist) {
				closest = objective;
				minDist = dist;
			}
		}
		if (closest != null) {
			objectives.add(closest);
		}
		
		if (!params.tryToCaptureObjectives) {
			objectives.clear();
		}
		
		if (params.type == FGRaidType.CONCURRENT) {
			RaidSubstage stage = new RaidSubstage();
			stage.maxDuration = params.maxDurationIfSpawnedFleetsConcurrent;
			stage.objectives.addAll(objectives);
			for (MarketAPI market : sortedTargetMarkets) {
				stage.markets.add(market.getPrimaryEntity());
			}
			stages.add(stage);
		} else {
			if (!objectives.isEmpty()) {
				RaidSubstage stage = new RaidSubstage();
				stage.maxDuration = params.maxDurationIfSpawnedFleetsPerSequentialStage;
				stage.objectives.addAll(objectives);
				stages.add(stage);
			}
			
			for (MarketAPI market : sortedTargetMarkets) {
				RaidSubstage stage = new RaidSubstage();
				stage.maxDuration = params.maxDurationIfSpawnedFleetsConcurrent;
				stage.markets.add(market.getPrimaryEntity());
				stages.add(stage);
			}
		}
		
		float totalDur = 0f;
		for (RaidSubstage stage : stages) {
			totalDur += stage.maxDuration;
		}
		setDurDays(totalDur + 10f);
		

		// system defenders protect targeted markets
		float responseFraction = 1f / Math.max(1f, sortedTargetMarkets.size());
		for (MarketAPI market : sortedTargetMarkets) {
			MilitaryResponseParams defParams = new MilitaryResponseParams(ActionType.HOSTILE, 
					"defRaid_" + market.getId(), 
					market.getFaction(),
					market.getPrimaryEntity(),
					responseFraction,
					getDurDays());
			MilitaryResponseScript defScript = new MilitaryResponseScript(defParams);
			params.where.addScript(defScript);
			scripts.add(defScript);
		}
		
		computedSubstages = true;
	}
	
	public void removeAggroMilitaryScripts(boolean clearAssignments) {
		if (clearAssignments) {
			for (CampaignFleetAPI fleet : intel.getFleets()) {
				fleet.clearAssignments();
			}
		}
		if (scripts != null) {
			List<MilitaryResponseScript> remove = new ArrayList<MilitaryResponseScript>();
			for (MilitaryResponseScript s : scripts) {
				if (s.getParams() != null && 
						s.getParams().responseReason != null && s.getParams().responseReason.startsWith("raid_")) {
					s.forceDone();
					remove.add(s);
				}
			}
			scripts.removeAll(remove);
		}
	}
	
	@Override
	public void setActionFinished(boolean finished) {
		if (finished && !this.finished) {
			List<CampaignFleetAPI> fleets = intel.getFleets();
			for (CampaignFleetAPI fleet : fleets) {
				fleet.removeScriptsOfClass(WarfleetAssignmentAI.class);
				Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.FLEET_BUSY, fleet.getId(), true, -1f);
			}
			
			if (scripts != null) {
				for (MilitaryResponseScript s : scripts) {
					s.forceDone();
				}
			}
			
			for (SectorEntityToken objective : params.where.getEntitiesWithTag(Tags.OBJECTIVE)) {
				WarSimScript.removeNoFightingTimeoutForObjective(objective, intel.getFaction());
			}
		}
		super.setActionFinished(finished);
	}
	

	@Override
	public void directFleets(float amount) {
		super.directFleets(amount);
		if (isActionFinished()) return;
		
		if (intel.isSpawning()) return; // could happen if source is in same system as target
		
		List<CampaignFleetAPI> fleets = intel.getFleets();
		if (fleets.isEmpty()) {
			setActionFinished(true);
			return;
		}
		
		if (!computedSubstages) {
			computeSubstages();
		}
		
		if (stages.isEmpty()) {
			setActionFinished(true);
			return;
		}
		
		float days = Global.getSector().getClock().convertToDays(amount);
		
		RaidSubstage stage = stages.get(0);
		if (!stage.started) {
			stage.started = true;
			
			removeAggroMilitaryScripts(true);

			List<SectorEntityToken> targets = new ArrayList<SectorEntityToken>(stage.objectives);
			targets.addAll(stage.markets);
			
			orderFleetMovements(targets);
		}
		
		
		stage.maxDuration -= days;
		if (stage.maxDuration <= 0 || stage.allGoalsAchieved(this)) {
			stages.remove(stage);
		}
		
		
		interval.advance(days);
		if (!interval.intervalElapsed()) return;

		boolean inSpawnRange = RouteManager.isPlayerInSpawnRange(params.where.getCenter());
		if (!inSpawnRange && elapsed > originalDuration) {
			autoresolve();
			return;
		}
		
		
		for (SectorEntityToken obj : stage.objectives) {
			if (obj.getFaction() == intel.getFaction()) {
				WarSimScript.removeFightOrdersFor(obj, intel.getFaction());
			}
		}
		
		boolean someRaidsFinished = false;
		for (SectorEntityToken e : stage.markets) {
			if (stage.finishedRaiding.contains(e)) continue;
			if (!canRaid(null, e.getMarket())) {
				someRaidsFinished = true;
				stage.finishedRaiding.add(e);
			}
		}
		
		if (someRaidsFinished) {
			for (SectorEntityToken e : stage.markets) {
				WarSimScript.removeFightOrdersFor(e, intel.getFaction());
			}
			List<SectorEntityToken> remaining = new ArrayList<SectorEntityToken>();
			remaining.addAll(stage.markets);
			remaining.removeAll(stage.finishedRaiding);
			
			// if wanted to re-fight for objectives that were lost, would add those here, but don't
			
			if (!remaining.isEmpty()) {
				orderFleetMovements(remaining);
			}
		}
		
		
		
		for (CampaignFleetAPI fleet : fleets) {		
			if (params.doNotGetSidetracked) {
				boolean battleNear = false;
				for (CampaignFleetAPI other : fleets) {
					if (other == fleet || other.getBattle() == null) continue;
					if (other.getContainingLocation() != fleet.getContainingLocation());
					float dist = Misc.getDistance(fleet, other);
					if (dist < 1000) {
						CampaignFleetAIAPI ai = fleet.getAI();
						if (ai != null && ai.wantsToJoin(other.getBattle(), other.getBattle().isPlayerInvolved())) {
							battleNear = true;
							break;
						}
					}
				}
				if (!battleNear) {
					fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED, true, 0.4f);
				}
			}
		}
	}

	
	protected void orderFleetMovements(List<SectorEntityToken> targets) {
		float responseFraction = 1f / Math.max(1f, targets.size());
		
		for (SectorEntityToken target : targets) {
			if (!target.hasTag(Tags.OBJECTIVE) &&!canRaid(null, target.getMarket())) {
				continue;
			}
			
			float rf = responseFraction;
			MilitaryResponseParams aggroParams = new MilitaryResponseParams(ActionType.HOSTILE, 
					"raid_" + target.getId(), 
					intel.getFaction(),
					target,
					rf,
					getDurDays());
			if (params.appendTargetNameToTravelText) {
				aggroParams.travelText = 
						(params.targetTravelText != null ? params.targetTravelText + " " : "traveling to ") + target.getName();
			} else if (params.targetTravelText != null) {
				aggroParams.travelText = params.targetTravelText;
			}

			if (params.inSystemActionText != null) {
				aggroParams.actionText = params.inSystemActionText;
			} else {
				aggroParams.actionText = "raiding system";
			}
			
			MilitaryResponseScript script = new MilitaryResponseScript(aggroParams);
			params.where.addScript(script);
			scripts.add(script);
		}
	}
	
	public FGRaidParams getParams() {
		return params;
	}
	
	
	public boolean canRaid(CampaignFleetAPI fleet, MarketAPI market) {
		if (market == null) return false;
		if (!market.isInEconomy()) {
			return false;
		}
		if (!params.allowedTargets.contains(market) && !params.allowedTargets.isEmpty() && !params.allowAnyHostileMarket) {
			return false;
		}
		if (raidCount.getCount(market) >= params.raidsPerColony) {
			return false;
		}
		
		if (fleet != null && !intel.getFleets().contains(fleet)) {
			return false;
		}
		
		if (params.bombardment != null && fleet != null) {
			float fp = fleet.getFleetPoints();
			for (CampaignFleetAPI other : intel.getFleets()) {
				if (other == fleet) continue;
				if (other.getContainingLocation() != fleet.getContainingLocation()) continue;
				float dist = Misc.getDistance(fleet, other);
				if (dist > 1000) continue;
				float otherFP = other.getFleetPoints();
				if (otherFP > fp * 1.2f) {
					return false;
				}
			}
		}
		
		FactionAPI faction = intel.getFaction();
		if (fleet != null) {
			faction = fleet.getFaction();
		}
		boolean hostile = market.getFaction().isHostileTo(faction);
		if (fleet != null) {
			hostile |= Misc.isFleetMadeHostileToFaction(fleet, market.getFaction());
		}
		return (params.allowNonHostileTargets || hostile) && !isActionFinished();
	}
	

	public void performRaid(CampaignFleetAPI fleet, MarketAPI market) {
		raidCount.add(market);
		
		FactionAPI faction = intel.getFaction();
		if (fleet != null) {
			faction = fleet.getFaction();
		}
		
		if (params.bombardment != null) {
			float cost = MarketCMD.getBombardmentCost(market, fleet);
			float bombardStr = intel.getRoute().getExtra().getStrengthModifiedByDamage() / intel.getApproximateNumberOfFleets() * 
									Misc.FP_TO_BOMBARD_COST_APPROX_MULT;
			if (fleet != null) {
				bombardStr = fleet.getCargo().getMaxFuel() * 0.5f;
			}
			
			if (cost <= bombardStr) {
				new MarketCMD(market.getPrimaryEntity()).doBombardment(intel.getFaction(), params.bombardment);
				bombardCount++;
			} else {
				Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_BOMBARDED, 
			   			   			   intel.getFaction().getId(), true, 30f);
			}
		} else {
			float raidStr = intel.getRoute().getExtra().getStrengthModifiedByDamage() / intel.getApproximateNumberOfFleets() * 
							Misc.FP_TO_GROUND_RAID_STR_APPROX_MULT;
			if (fleet != null) {
				raidStr = MarketCMD.getRaidStr(fleet);
			}
			
			Industry industry = null;
			int index = raidCount.getCount(market) - 1;
			if (index < 0) index = 0;
			if (params.disrupt != null && index < params.disrupt.size()) {
				int count = 0;
				for (String industryId : params.disrupt) {
					if (market.hasIndustry(industryId)) {
						if (count >= index) {
							industry = market.getIndustry(industryId);
							break;
						}
						count++;
					}
				}
//				String industryId = params.disrupt.get(index);
//				industry = market.getIndustry(industryId);
			}
		
			if (intel instanceof GenericRaidFGI && ((GenericRaidFGI)intel).hasCustomRaidAction()) {
				((GenericRaidFGI)intel).doCustomRaidAction(fleet, market, raidStr);
				Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_RAIDED, 
						   faction.getId(), true, 30f);
				Misc.setRaidedTimestamp(market);
			} else if (industry != null) {
				new MarketCMD(market.getPrimaryEntity()).doIndustryRaid(faction, raidStr, industry, 1f);
			} else {
				new MarketCMD(market.getPrimaryEntity()).doGenericRaid(faction,
						   		raidStr, params.maxStabilityLostPerRaid, params.raidsPerColony > 1);
			}
		}
	}

	
	
	public void autoresolve() {
		if (isActionFinished()) return;
		
		float str = WarSimScript.getFactionStrength(intel.getFaction(), params.where);
		if (!intel.isSpawnedFleets() && intel.getRoute().isExpired()) {
			// the above doesn't pick it up
			OptionalFleetData data = intel.getRoute().getExtra();
			if (data != null) str += data.getStrengthModifiedByDamage();
		}

		float enemyStr = WarSimScript.getEnemyStrength(intel.getFaction(), params.where);
		float origStr = str;
		float strMult = 1f;
		for (MarketAPI target : Misc.getMarketsInLocation(params.where)) {
			if (!params.allowAnyHostileMarket && !params.allowedTargets.contains(target)) continue;
			if (!params.allowNonHostileTargets && !intel.getFaction().isHostileTo(target.getFaction())) continue;
			
			float defensiveStr = enemyStr + WarSimScript.getStationStrength(target.getFaction(), params.where, target.getPrimaryEntity());
			
			float damage = 0.5f * defensiveStr / Math.max(str, 1f);
			if (damage > 0.75f) damage = 0.75f;
			strMult *= (1f - damage);
			
			if (defensiveStr >= str) {
				continue;
			}

			Industry station = Misc.getStationIndustry(target);
			if (station != null) {
				OrbitalStation.disrupt(station);
				station.reapply();
			}
			
			for (int i = 0; i < params.raidsPerColony; i++) {
				performRaid(null, target);
			}
			
			//str -= defensiveStr * 0.5f;
			str = origStr * strMult;
		}
		
		if (intel.isSpawnedFleets() && strMult < 1f) {
			for (CampaignFleetAPI fleet : intel.getFleets()) {
				FleetFactoryV3.applyDamageToFleet(fleet, 1f - strMult, false, intel.getRandom());
			}
		}
		
		if (!intel.isSpawnedFleets() && strMult < 1) {
			OptionalFleetData extra = intel.getRoute().getExtra();
			if (extra != null) {
				if (extra.damage == null) {
					extra.damage = 0f;
				}
				extra.damage = 1f - (1f - extra.damage) * strMult;
				//extra.damage = 1f;
				if (extra.damage > 1f) extra.damage = 1f;
			}
		} else if (intel.isSpawnedFleets() && getSuccessFraction() <= 0f) {
			intel.abort();
		} else {
			// if fleets were not spawned and it needs to abort due to the damage taken,
			// that's handled in FleetGroupIntel
		}
		
		setActionFinished(true);
	}
	
	
	
	public String getRaidApproachText(CampaignFleetAPI fleet, MarketAPI market) {
		if (params.raidApproachText != null) {
			return params.raidApproachText + " " + market.getName();
		}
		return null;
	}

	public String getRaidActionText(CampaignFleetAPI fleet, MarketAPI market) {
		if (params.raidActionText != null) {
			return params.raidActionText + " " + market.getName();
		}
		return null;
	}
	
	
	// not needed and not used by the WarfleetAssignmentAI
	public String getRaidPrepText(CampaignFleetAPI fleet, SectorEntityToken from) {
		return null;
	}

	public String getRaidInSystemText(CampaignFleetAPI fleet) {
		return null;
	}

	public String getRaidDefaultText(CampaignFleetAPI fleet) {
		return null;
	}

	public CountingMap<MarketAPI> getRaidCount() {
		return raidCount;
	}
	

	public float getSuccessFraction() {
		int totalGoal = params.raidsPerColony * params.allowedTargets.size();
		if (totalGoal < 1) totalGoal = 1;
		int achieved = raidCount.getTotal();
		
		if (params.bombardment != null) {
			achieved = bombardCount;
		}
		
		return Math.max(0f, (float)achieved / (float)totalGoal);
	}
	
	public Color getSystemNameHighlightColor() {
		MarketAPI largest = null;
		int max = 0;
		boolean player = false;
		for (MarketAPI target : Misc.getMarketsInLocation(params.where)) {
			if (!params.allowAnyHostileMarket && !params.allowedTargets.contains(target)) continue;
			if (!params.allowNonHostileTargets && !intel.getFaction().isHostileTo(target.getFaction())) continue;
			
			int size = target.getSize();
			if (size > max) {
				largest = target;
				max = size;
			}
			if (target.isPlayerOwned()) {
				player = true;
			}
		}
		if (player) return Misc.getBasePlayerColor();
		if (largest != null) {
			return largest.getFaction().getBaseUIColor();
		}
		return Misc.getTextColor();
	}

	public StarSystemAPI getWhere() {
		return params.where;
	}
	
}








