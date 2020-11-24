package com.fs.starfarer.api.impl.campaign.intel.punitive;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.ActionType;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript.MilitaryResponseParams;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PunitiveExpeditionIntel.PunExOutcome;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PunitiveExpeditionManager.PunExGoal;
import com.fs.starfarer.api.impl.campaign.intel.raid.ActionStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI.FleetActionDelegate;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.BombardType;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class PEActionStage extends ActionStage implements FleetActionDelegate {
	
	protected MarketAPI target;
	protected boolean playerTargeted = false;
	protected List<MilitaryResponseScript> scripts = new ArrayList<MilitaryResponseScript>();
	protected boolean gaveOrders = true; // will be set to false in updateRoutes()
	protected float untilAutoresolve = 30f;
	
	public PEActionStage(PunitiveExpeditionIntel raid, MarketAPI target) {
		super(raid);
		this.target = target;
		playerTargeted = target.isPlayerOwned();
		
		untilAutoresolve = 15f + 5f * (float) Math.random();
	}
	

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		float days = Misc.getDays(amount);
		untilAutoresolve -= days;
		if (DebugFlags.PUNITIVE_EXPEDITION_DEBUG || DebugFlags.FAST_RAIDS) {
			untilAutoresolve -= days * 100f;
		}
		
		if (!gaveOrders) {
			gaveOrders = true;
		
			removeMilScripts();

			// getMaxDays() is always 1 here
			// scripts get removed anyway so we don't care about when they expire naturally
			// just make sure they're around for long enough
			float duration = 100f;
			
			MilitaryResponseParams params = new MilitaryResponseParams(ActionType.HOSTILE, 
					"PE_" + Misc.genUID() + target.getId(), 
					intel.getFaction(),
					target.getPrimaryEntity(),
					1f,
					duration);
			MilitaryResponseScript script = new MilitaryResponseScript(params);
			target.getContainingLocation().addScript(script);
			scripts.add(script);
			
			MilitaryResponseParams defParams = new MilitaryResponseParams(ActionType.HOSTILE, 
					"defPE_" + Misc.genUID() + target.getId(), 
					target.getFaction(),
					target.getPrimaryEntity(),
					1f,
					duration);
			MilitaryResponseScript defScript = new MilitaryResponseScript(defParams);
			target.getContainingLocation().addScript(defScript);
			scripts.add(defScript);
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
		
		boolean inSpawnRange = RouteManager.isPlayerInSpawnRange(target.getPrimaryEntity());
		if (!inSpawnRange && untilAutoresolve <= 0){
			autoresolve();
			return;
		}
		
		if (!target.isInEconomy() || !target.isPlayerOwned()) {
			status = RaidStageStatus.FAILURE;
			removeMilScripts();
			giveReturnOrdersToStragglers(getRoutes());
			return;
		}
		
	}
	
	public String getRaidActionText(CampaignFleetAPI fleet, MarketAPI market) {
		PunitiveExpeditionIntel intel = ((PunitiveExpeditionIntel)this.intel);
		PunExGoal goal = intel.getGoal();
		if (goal == PunExGoal.BOMBARD) {
			return "bombarding " + market.getName();
		}
		return "raiding " + market.getName();
	}

	public String getRaidApproachText(CampaignFleetAPI fleet, MarketAPI market) {
		PunitiveExpeditionIntel intel = ((PunitiveExpeditionIntel)this.intel);
		PunExGoal goal = intel.getGoal();
		if (goal == PunExGoal.BOMBARD) {
			return "moving in to bombard " + market.getName();
		}
		return "moving in to raid " + market.getName();
	}

	public void performRaid(CampaignFleetAPI fleet, MarketAPI market) {
		removeMilScripts();
		
		PunitiveExpeditionIntel intel = ((PunitiveExpeditionIntel)this.intel);
		PunExGoal goal = intel.getGoal();
		
		status = RaidStageStatus.SUCCESS;
		
		if (goal == PunExGoal.BOMBARD) {
			float cost = MarketCMD.getBombardmentCost(market, fleet);
			//float maxCost = intel.getAssembleStage().getOrigSpawnFP() * Misc.FP_TO_BOMBARD_COST_APPROX_MULT;
			float maxCost = intel.getRaidFP() / intel.getNumFleets() * Misc.FP_TO_BOMBARD_COST_APPROX_MULT;
			if (fleet != null) {
				maxCost = fleet.getCargo().getMaxFuel() * 0.25f;
			}
			
			if (cost <= maxCost) {
				new MarketCMD(market.getPrimaryEntity()).doBombardment(intel.getFaction(), BombardType.SATURATION);
				intel.setOutcome(PunExOutcome.SUCCESS);
			} else {
				intel.setOutcome(PunExOutcome.BOMBARD_FAIL);
				status = RaidStageStatus.FAILURE;
				
				Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_BOMBARDED, 
			   			   			   intel.getFaction().getId(), true, 30f);
			}
		} else {
			//float str = intel.getAssembleStage().getOrigSpawnFP() * Misc.FP_TO_GROUND_RAID_STR_APPROX_MULT;
			float str = intel.getRaidFPAdjusted() / intel.getNumFleets() * Misc.FP_TO_GROUND_RAID_STR_APPROX_MULT;
			
			if (fleet != null) str = MarketCMD.getRaidStr(fleet);
			//float re = MarketCMD.getRaidEffectiveness(target, str);
			
			//str = 10f;
			
			float durMult = Global.getSettings().getFloat("punitiveExpeditionDisruptDurationMult");
			boolean raidSuccess = new MarketCMD(market.getPrimaryEntity()).doIndustryRaid(intel.getFaction(), str, intel.targetIndustry, durMult);
			
			if (raidSuccess) {
				intel.setOutcome(PunExOutcome.SUCCESS);
			} else {
				intel.setOutcome(PunExOutcome.RAID_FAIL);
				status = RaidStageStatus.FAILURE;
				
				Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_RAIDED, 
						   			   intel.getFaction().getId(), true, 30f);
			}
		}
		
//		// so it doesn't keep trying to raid/bombard
//		if (fleet != null) {
//			fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_RAIDER);
//		}
		
		// when FAILURE, gets sent by RaidIntel
		if (intel.getOutcome() != null) {
			if (status == RaidStageStatus.SUCCESS) {
				intel.sendOutcomeUpdate();
			} else {
				removeMilScripts();
				giveReturnOrdersToStragglers(getRoutes());
			}
		}
	}

	
	protected void autoresolve() {
		float str = WarSimScript.getFactionStrength(intel.getFaction(), target.getStarSystem());
		float enemyStr = WarSimScript.getFactionStrength(target.getFaction(), target.getStarSystem());
		
		float defensiveStr = enemyStr + WarSimScript.getStationStrength(target.getFaction(), 
							 target.getStarSystem(), target.getPrimaryEntity());
		if (defensiveStr >= str) {
			status = RaidStageStatus.FAILURE;
			removeMilScripts();
			giveReturnOrdersToStragglers(getRoutes());
			
			// not strictly necessary, I think, but shouldn't hurt
			// otherwise would get set in PunitiveExpeditionIntel.notifyRaidEnded()
			PunitiveExpeditionIntel intel = ((PunitiveExpeditionIntel)this.intel);
			intel.setOutcome(PunExOutcome.TASK_FORCE_DEFEATED);
			return;
		}
		
		Industry station = Misc.getStationIndustry(target);
		if (station != null) {
			OrbitalStation.disrupt(station);
		}
		
		performRaid(null, target);
	}
	
	
	protected void updateRoutes() {
		resetRoutes();
		
		gaveOrders = false;
		
		((PunitiveExpeditionIntel)intel).sendEnteredSystemUpdate();
		
		List<RouteData> routes = RouteManager.getInstance().getRoutesForSource(intel.getRouteSourceId());
		for (RouteData route : routes) {
			if (target.getStarSystem() != null) { // so that fleet may spawn NOT at the target
				route.addSegment(new RouteSegment(Math.min(5f, untilAutoresolve), target.getStarSystem().getCenter()));
			}
			route.addSegment(new RouteSegment(1000f, target.getPrimaryEntity()));
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
		
		if (curr < index) return;
		
		if (status == RaidStageStatus.ONGOING && curr == index) {
			info.addPara("The expedition forces are currently in-system.", opad);
			return;
		}
		
		PunitiveExpeditionIntel intel = ((PunitiveExpeditionIntel)this.intel);
		if (intel.getOutcome() != null) {
			switch (intel.getOutcome()) {
			case BOMBARD_FAIL:
				info.addPara("The ground defenses of " + target.getName() + " were sufficient to prevent bombardment.", opad);
				break;
			case RAID_FAIL:
				info.addPara("The raiding forces have been repelled by the ground defenses of " + target.getName() + ".", opad);
				break;
			case SUCCESS:
				if (intel.goal == PunExGoal.BOMBARD) {
					if (!target.isInEconomy()) {
						info.addPara("The expeditionary force has successfully bombarded " + target.getName() + ", destroying the colony outright.", opad);
					} else {
						info.addPara("The expeditionary force has successfully bombarded " + target.getName() + ".", opad);
					}
				} else if (intel.getTargetIndustry() != null) {
					info.addPara("The expeditionary force has disrupted " + 
							intel.getTargetIndustry().getCurrentName() + " operations for %s days.",
							opad, h, "" + (int)Math.round(intel.getTargetIndustry().getDisruptedDays()));
				}
				break;
			case TASK_FORCE_DEFEATED:
				info.addPara("The expeditionary force has been defeated by the defenders of " +
								target.getName() + ".", opad);
				break;
			case COLONY_NO_LONGER_EXISTS:
				info.addPara("The expedition has been aborted.", opad);
				break;
			
			}
		} else if (status == RaidStageStatus.SUCCESS) {			
			info.addPara("The expeditionary force has succeeded.", opad); // shouldn't happen?
		} else {
			info.addPara("The expeditionary force has failed.", opad); // shouldn't happen?
		}
	}

	public boolean canRaid(CampaignFleetAPI fleet, MarketAPI market) {
		PunitiveExpeditionIntel intel = ((PunitiveExpeditionIntel)this.intel);
		if (intel.getOutcome() != null) return false;
		return market == target;
	}
	
	public String getRaidPrepText(CampaignFleetAPI fleet, SectorEntityToken from) {
		return "orbiting " + from.getName();
	}
	
	public String getRaidInSystemText(CampaignFleetAPI fleet) {
		return "traveling";
	}
	
	public String getRaidDefaultText(CampaignFleetAPI fleet) {
		return "traveling";		
	}
	
	@Override
	public boolean isPlayerTargeted() {
		return playerTargeted;
	}
}

















