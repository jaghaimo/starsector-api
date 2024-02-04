package com.fs.starfarer.api.impl.campaign.intel.raid;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.ActionType;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript.MilitaryResponseParams;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI.FleetActionDelegate;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class PirateRaidActionStage extends ActionStage implements FleetActionDelegate {
	
	public static class RaidActionSubStage {
		public List<Pair<SectorEntityToken, Float>> targets = new ArrayList<Pair<SectorEntityToken,Float>>();
		public float duration = 30f;
	}
	
	protected StarSystemAPI system;
	
	protected float untilNextStage = 0f;
	protected List<MilitaryResponseScript> scripts = new ArrayList<MilitaryResponseScript>();
	protected List<RaidActionSubStage> steps = new ArrayList<RaidActionSubStage>();
	protected List<MarketAPI> targets = new ArrayList<MarketAPI>();
	protected boolean playerTargeted = false;
	
	public PirateRaidActionStage(RaidIntel raid, StarSystemAPI system) {
		super(raid);
		
		this.system = system;
		
		for (MarketAPI target : getTargets()) {
			if (target.isPlayerOwned()) {
				playerTargeted = true;
			}
		}
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		float days = Misc.getDays(amount);
		untilNextStage -= days;
		
		if (!steps.isEmpty() && untilNextStage <= 0) {
			removeMilScripts();
			
			RaidActionSubStage step = steps.remove(0);
			untilNextStage = step.duration;

			// scripts get removed anyway so we don't care about when they expire naturally
			// just make sure they're around for long enough
			float duration = 100f;
			
			for (Pair<SectorEntityToken, Float> curr : step.targets) {
				MilitaryResponseParams params = new MilitaryResponseParams(ActionType.HOSTILE, 
						"raid_" + curr.one.getId(), 
						intel.getFaction(),
						curr.one,
						curr.two,
						duration);
				MilitaryResponseScript script = new MilitaryResponseScript(params);
				curr.one.getContainingLocation().addScript(script);
				scripts.add(script);
				
				MilitaryResponseParams defParams = new MilitaryResponseParams(ActionType.HOSTILE, 
						"defRaid_" + curr.one.getId(), 
						curr.one.getFaction(),
						curr.one,
						curr.two,
						duration);
				MilitaryResponseScript defScript = new MilitaryResponseScript(defParams);
				curr.one.getContainingLocation().addScript(defScript);
				scripts.add(defScript);
			}
		}
	}

	protected void removeMilScripts() {
		if (scripts != null) {
			for (MilitaryResponseScript s : scripts) {
				s.forceDone();
			}
		}
	}
	
	@Override
	protected void updateStatus() {
//		if (true) {
//			status = RaidStageStatus.SUCCESS;
//			return;
//		}
		
		abortIfNeededBasedOnFP(true);
		if (status != RaidStageStatus.ONGOING) return;
		
		if (getTargets().isEmpty()) {
			status = RaidStageStatus.FAILURE;
			removeMilScripts();
			giveReturnOrdersToStragglers(getRoutes());
			return;
		}
		
		if (steps.isEmpty()) {
			boolean inSpawnRange = RouteManager.isPlayerInSpawnRange(system.getCenter());
			if (!inSpawnRange && elapsed > maxDays) {
				autoresolve();
				return;
			}
			
			boolean someUnraided = false;
			boolean someRaided = false;
			for (MarketAPI market : targets) {
				if (!market.getFaction().isHostileTo(intel.getFaction())) {
					someUnraided = true;
					continue;
				}
				if (Misc.flagHasReason(market.getMemoryWithoutUpdate(), 
							MemFlags.RECENTLY_RAIDED, intel.getFaction().getId())) {
					someRaided = true;
				} else {
					someUnraided = true;
				}
			}
			if (targets.isEmpty()) {
				someUnraided = true;
			}
			
			if (!someUnraided || (elapsed > maxDays && someRaided)) {
				status = RaidStageStatus.SUCCESS;
				removeMilScripts();
				return;
			}
			
			if (elapsed > maxDays && !someRaided) {
				status = RaidStageStatus.FAILURE;
				giveReturnOrdersToStragglers(getRoutes());
				removeMilScripts();
				return;
			}
		}
	}
	
	
	protected List<MarketAPI> getTargets() {
		List<MarketAPI> targets = new ArrayList<MarketAPI>();
		for (MarketAPI market : Misc.getMarketsInLocation(system)) {
			if (market.getFaction().isHostileTo(intel.getFaction())) {
				targets.add(market);
			}
		}
		return targets;
	}

	protected void updateRoutes() {
		resetRoutes();
		
		if (playerTargeted) {
			intel.sendEnteredSystemUpdate();
		}
		
		
		FactionAPI faction = intel.getFaction();
		
		List<RouteData> routes = RouteManager.getInstance().getRoutesForSource(intel.getRouteSourceId());
		
		for (RouteData route : routes) {
			route.addSegment(new RouteSegment(1000f, system.getCenter()));
		}
		
		List<MarketAPI> targets = getTargets();
		if (targets.isEmpty()) return;
		
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>();
		for (MarketAPI target : targets) {
			picker.add(target, target.getSize() * target.getSize());
		}
		
		float str = WarSimScript.getFactionStrength(faction, system);
		float enemyStr = 0;
		Set<String> seen = new HashSet<String>();
		for (MarketAPI market : targets) {
			if (seen.contains(market.getFactionId())) continue;
			seen.add(market.getFactionId());
			enemyStr += WarSimScript.getFactionStrength(market.getFaction(), system);
		}
		if (str < 1) str = 1;
		if (enemyStr < 1) enemyStr = 1;
		
		boolean concurrent = false;
		int numRaids = 1;
		if (str > enemyStr * 2 && (float) Math.random() > 0.5f) {
			numRaids = 2;
		}
		if (str > enemyStr * 4) {
			concurrent = true;
			numRaids++;
		}
		
		if (!concurrent) {
			for (int i = 0; i < numRaids && !picker.isEmpty(); numRaids++) {
				MarketAPI target = picker.pickAndRemove();
				
				float defensiveStr = enemyStr + WarSimScript.getStationStrength(target.getFaction(), system, target.getPrimaryEntity());
				if (defensiveStr > str) {
					continue;
				}
				
				RaidActionSubStage step = new RaidActionSubStage();
				step.duration = 20f + 10f * (float) Math.random();
				
				float weight = 1f;
				Industry station = Misc.getStationIndustry(target);
				if (station != null && station.getDisruptedDays() < step.duration) {
					step.duration += 10f + (float) Math.random() * 5f;
					weight += 1f;
				}
				
				
				step.targets.add(new Pair<SectorEntityToken, Float>(target.getPrimaryEntity(), weight));
				steps.add(step);
				
				this.targets.add(target);
			}
			
			maxDays = 0f;
			for (RaidActionSubStage step : steps) {
				maxDays += step.duration;
			}
		} else {
			RaidActionSubStage step = new RaidActionSubStage();
			boolean stationPresent = false;
			for (int i = 0; i < numRaids && !picker.isEmpty(); numRaids++) {
				MarketAPI target = picker.pickAndRemove();
				
				float defensiveStr = enemyStr + WarSimScript.getStationStrength(target.getFaction(), system, target.getPrimaryEntity());
				if (defensiveStr > str) {
					continue;
				}
				
				float weight = 1f;
				Industry station = Misc.getStationIndustry(target);
				if (station != null && station.getDisruptedDays() < 20f) {
					stationPresent = true;
					weight += 1f;
				}
				
				
				step.targets.add(new Pair<SectorEntityToken, Float>(target.getPrimaryEntity(), weight));
				
				this.targets.add(target);
			}
			
			steps.add(step);
			
			step.duration = 20f + 10f * (float) Math.random();
			if (stationPresent) {
				step.duration += 10f + (float) Math.random() * 5f;
			}
			
			maxDays = step.duration;
		}
		
		if (this.targets.isEmpty()) {
			steps.clear();
			maxDays = 0f;
			return;
		}
	}
	
	
	public void showStageInfo(TooltipMakerAPI info) {
		int curr = intel.getCurrentStage();
		int index = intel.getStageIndex(this);
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		if (status == RaidStageStatus.FAILURE) {
			info.addPara("The raiding forces have been defeated by the defenders of the " +
					intel.getSystem().getNameWithLowercaseType() + ". The raid is now over.", opad);
		} else if (status == RaidStageStatus.SUCCESS) {
			List<MarketAPI> raided = new ArrayList<MarketAPI>();
			for (MarketAPI market : targets) {
				if (!market.getFaction().isHostileTo(intel.getFaction())) {
					continue;
				}
				if (Misc.flagHasReason(market.getMemoryWithoutUpdate(), 
							MemFlags.RECENTLY_RAIDED, intel.getFaction().getId())) {
					raided.add(market);
				}
			}
			if (!raided.isEmpty()) {
				info.addPara("The raiding forces have been successful in raiding the following colonies:", opad);
				float initPad = opad;
				for (MarketAPI market : raided) {
					BaseIntelPlugin.addMarketToList(info, market, initPad, tc);
					initPad = 0f;
				}
			}
		} else if (curr == index) {
			info.addPara("The raiding forces are currently operating in the " + 
					intel.getSystem().getNameWithLowercaseType() + ".", opad);
			
		}
	}

	@Override
	public boolean isPlayerTargeted() {
		return playerTargeted;
	}

	
	
	protected void autoresolve() {
		float str = WarSimScript.getFactionStrength(intel.getFaction(), system);

		float enemyStr = WarSimScript.getEnemyStrength(intel.getFaction(), system);
		
		status = RaidStageStatus.FAILURE;
		for (MarketAPI target : targets) {
			if (!target.getFaction().isHostileTo(intel.getFaction())) continue;
			
			float defensiveStr = enemyStr + WarSimScript.getStationStrength(target.getFaction(), system, target.getPrimaryEntity());
			if (defensiveStr >= str) {
				continue;
			}

			Industry station = Misc.getStationIndustry(target);
			if (station != null) {
				OrbitalStation.disrupt(station);
			}
			
//			float raidStr = intel.getRaidFP() / intel.getNumFleets() * Misc.FP_TO_GROUND_RAID_STR_APPROX_MULT;
//			new MarketCMD(target.getPrimaryEntity()).doGenericRaid(intel.getFaction(), raidStr);
			performRaid(null, target);
			
			str -= defensiveStr * 0.5f;
			status = RaidStageStatus.SUCCESS;
		}
		
		removeMilScripts();
	}
	
	public String getRaidActionText(CampaignFleetAPI fleet, MarketAPI market) {
		return "raiding " + market.getName();
	}

	public String getRaidApproachText(CampaignFleetAPI fleet, MarketAPI market) {
		return "moving in to raid " + market.getName();
	}

	
	public void performRaid(CampaignFleetAPI fleet, MarketAPI market) {
		float raidStr = intel.getRaidFPAdjusted() / intel.getNumFleets() * Misc.FP_TO_GROUND_RAID_STR_APPROX_MULT;
		if (fleet != null) {
			raidStr = MarketCMD.getRaidStr(fleet);
		}
		
		float maxPenalty = 3f;
//		if (fleet == null) {
//			maxPenalty += (intel.getNumFleets() - 1);
//		}
		
		new MarketCMD(market.getPrimaryEntity()).doGenericRaid(intel.getFaction(), raidStr, maxPenalty);
		
//		float re = MarketCMD.getRaidEffectiveness(market, raidStr);
//		MarketCMD.applyRaidStabiltyPenalty(market, Misc.ucFirst(intel.getFaction().getPersonNamePrefix()) + " raid", re);
//		//RecentUnrest.get(market).add(3, Misc.ucFirst(faction.getPersonNamePrefix()) + " raid");
//		
//		Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_RAIDED, 
//							   faction.getId(), true, 30f);
	}



	public boolean canRaid(CampaignFleetAPI fleet, MarketAPI market) {
		if (Misc.flagHasReason(market.getMemoryWithoutUpdate(), 
				MemFlags.RECENTLY_RAIDED, intel.getFaction().getId())) {
			return false;
		}
		return market.getFaction().isHostileTo(fleet.getFaction());
	}
	
	public String getRaidPrepText(CampaignFleetAPI fleet, SectorEntityToken from) {
		return "preparing for raid";
	}
	
	public String getRaidInSystemText(CampaignFleetAPI fleet) {
		return "raiding";
	}
	
	public String getRaidDefaultText(CampaignFleetAPI fleet) {
		return "raiding";
	}
	
}

















