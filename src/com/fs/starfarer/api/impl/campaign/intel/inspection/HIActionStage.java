package com.fs.starfarer.api.impl.campaign.intel.inspection;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.ActionType;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript.MilitaryResponseParams;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.inspection.HegemonyInspectionIntel.AntiInspectionOrders;
import com.fs.starfarer.api.impl.campaign.intel.inspection.HegemonyInspectionIntel.HegemonyInspectionOutcome;
import com.fs.starfarer.api.impl.campaign.intel.raid.ActionStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI.FleetActionDelegate;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class HIActionStage extends ActionStage implements FleetActionDelegate {
	
	public static float REP_PENALTY_HID_STUFF = -0.2f;
	public static float REP_PENALTY_NORMAL = -0.1f;
	
	protected MarketAPI target;
	protected boolean playerTargeted = false;
	protected List<MilitaryResponseScript> scripts = new ArrayList<MilitaryResponseScript>();
	protected boolean gaveOrders = true; // will be set to false in updateRoutes()
	protected float untilAutoresolve = 0f;
	
	public HIActionStage(HegemonyInspectionIntel raid, MarketAPI target) {
		super(raid);
		this.target = target;
		playerTargeted = target.isPlayerOwned(); // I mean, it's player-targeted by nature, but still
		untilAutoresolve = 5f;
		HegemonyInspectionIntel intel = ((HegemonyInspectionIntel)this.intel);
		if (intel.getOrders() == AntiInspectionOrders.RESIST) {
			//untilAutoresolve = 30f;
			untilAutoresolve = 15f + 5f * (float) Math.random();
		}
	}
	
	

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		float days = Misc.getDays(amount);
		untilAutoresolve -= days;
		
		if (!gaveOrders) {
			gaveOrders = true;
		
			removeMilScripts();
			
			// getMaxDays() is always 1 here
			// scripts get removed anyway so we don't care about when they expire naturally
			// just make sure they're around for long enough
			float duration = 100f;
			
			MilitaryResponseParams params = new MilitaryResponseParams(ActionType.HOSTILE, 
					"HI_" + target.getId(), 
					intel.getFaction(),
					target.getPrimaryEntity(),
					1f,
					duration);
			MilitaryResponseScript script = new MilitaryResponseScript(params);
			target.getContainingLocation().addScript(script);
			scripts.add(script);
			
			MilitaryResponseParams defParams = new MilitaryResponseParams(ActionType.HOSTILE, 
					"defHI_" + target.getId(), 
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
		return "performing inspection of " + market.getName();
	}

	public String getRaidApproachText(CampaignFleetAPI fleet, MarketAPI market) {
		return "moving to inspect " + market.getName();
	}

	public void performRaid(CampaignFleetAPI fleet, MarketAPI market) {
		removeMilScripts();
		
		if (market == null) {
			market = target;
		}
		
		HegemonyInspectionIntel intel = ((HegemonyInspectionIntel)this.intel);
		
		status = RaidStageStatus.SUCCESS;
		boolean hostile = market.getFaction().isHostileTo(intel.getFaction());
		AntiInspectionOrders orders = intel.getOrders();
		
		if (hostile || orders == AntiInspectionOrders.RESIST) {
			//RecentUnrest.get(target).add(3, Misc.ucFirst(intel.getFaction().getPersonNamePrefix()) + " inspection");
			//float str = HegemonyInspectionIntel.DEFAULT_INSPECTION_GROUND_STRENGTH;
			float str = intel.getAssembleStage().getOrigSpawnFP() * 3f;
			if (fleet != null) str = MarketCMD.getRaidStr(fleet);
			
			float re = MarketCMD.getRaidEffectiveness(market, str);
			MarketCMD.applyRaidStabiltyPenalty(market, 
					Misc.ucFirst(intel.getFaction().getPersonNamePrefix()) + " inspection", re);
			Misc.setFlagWithReason(market.getMemoryWithoutUpdate(), MemFlags.RECENTLY_RAIDED, 
								   intel.getFaction().getId(), true, 30f);
			Misc.setRaidedTimestamp(market);
			removeCoresAndApplyResult(fleet);
		} else if (orders == AntiInspectionOrders.BRIBE) {
			intel.setOutcome(HegemonyInspectionOutcome.BRIBED);
		} else if (orders == AntiInspectionOrders.COMPLY) {
			removeCoresAndApplyResult(fleet);
		}
		
//		if (fleet != null) {
//			fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_RAIDER);
//		}
		
		//if (intel.getOutcome() != null) {
//		if (intel.getOutcome() != null && status == RaidStageStatus.SUCCESS) {			
//			intel.sendOutcomeUpdate();
//		}
		if (intel.getOutcome() != null) {
			if (status == RaidStageStatus.SUCCESS) {
				intel.sendOutcomeUpdate();
			} else {
				removeMilScripts();
				giveReturnOrdersToStragglers(getRoutes());
			}
		}
	}
	
	protected List<String> coresRemoved = new ArrayList<String>();
	protected void removeCoresAndApplyResult(CampaignFleetAPI fleet) {
		HegemonyInspectionIntel intel = ((HegemonyInspectionIntel)this.intel);
		AntiInspectionOrders orders = intel.getOrders();
		
		boolean resist = orders == AntiInspectionOrders.RESIST;
		List<String> found = removeCores(fleet, resist);
		
		if (coresRemoved == null) coresRemoved = new ArrayList<String>();
		coresRemoved.clear();
		coresRemoved.addAll(found);
		List<String> expected = intel.getExpectedCores();
		
		int valFound = 0;
		int valExpected = 0;
		
		for (String id : found) {
			CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(id);
			valFound += spec.getBasePrice();
			
			if (fleet != null) {
				fleet.getCargo().addCommodity(id, 1);
			}
		}
		for (String id : expected) {
			CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(id);
			valExpected += spec.getBasePrice();
		}
		
		if (valExpected < 30000) {
			valExpected = 30000;
		}
		
		//resist = false;
		if (!resist && valExpected > valFound * 1.25f) {
			intel.setOutcome(HegemonyInspectionOutcome.FOUND_EVIDENCE_NO_CORES);
			for (Industry curr : target.getIndustries()) {
				curr.setDisrupted((intel.getRandom().nextFloat() * 45f) + 15f);
			}
			intel.applyRepPenalty(REP_PENALTY_HID_STUFF);
		} else {
			intel.setOutcome(HegemonyInspectionOutcome.CONFISCATE_CORES);
			intel.applyRepPenalty(REP_PENALTY_NORMAL);
		}
	}
	
	public List<String> getCoresRemoved() {
		return coresRemoved;
	}

	protected List<String> removeCores(CampaignFleetAPI inspector, boolean resist) {
		
		HegemonyInspectionIntel intel = ((HegemonyInspectionIntel)this.intel);
		//float str = HegemonyInspectionIntel.DEFAULT_INSPECTION_GROUND_STRENGTH;
		//float str = intel.getAssembleStage().getOrigSpawnFP() * Misc.FP_TO_GROUND_RAID_STR_APPROX_MULT;
		float str = intel.getRaidFPAdjusted() / intel.getNumFleets() * Misc.FP_TO_GROUND_RAID_STR_APPROX_MULT;
		if (inspector != null) str = MarketCMD.getRaidStr(inspector);
		
		//str = 100000f;
		
		float re = MarketCMD.getRaidEffectiveness(target, str);
		
		List<String> result = new ArrayList<String>();
		for (Industry curr : target.getIndustries()) {
			String id = curr.getAICoreId();
			if (id != null) {
				if (resist && intel.getRandom().nextFloat() > re) continue;
				result.add(id);
				curr.setAICoreId(null);
			}
		}
		PersonAPI admin = target.getAdmin();
		if (admin.isAICore()) {
			if (!resist || intel.getRandom().nextFloat() < re) {
				result.add(admin.getAICoreId());
				target.setAdmin(null);
			}
		}
		target.reapplyIndustries();
		
		List<String> missing = new ArrayList<String>(intel.getExpectedCores());
		for (String id : result) {
			missing.remove(id);
		}
		
		CargoAPI cargo = Misc.getStorageCargo(target);
		if (cargo != null) {
			for (String id : new ArrayList<String>(missing)) {
				float qty = cargo.getCommodityQuantity(id);
				if (qty >= 1) {
					if (resist && intel.getRandom().nextFloat() > re) continue;
					cargo.removeCommodity(id, 1);
					missing.remove(id);
					result.add(id);
				}
			}
		}
		
		cargo = Misc.getLocalResourcesCargo(target);
		if (cargo != null) {
			for (String id : new ArrayList<String>(missing)) {
				float qty = cargo.getCommodityQuantity(id);
				if (qty >= 1) {
					if (resist && intel.getRandom().nextFloat() > re) continue;
					cargo.removeCommodity(id, 1);
					missing.remove(id);
					result.add(id);
				}
			}
		}
		
		
		return result;
	}
	
	protected void autoresolve() {
		float str = WarSimScript.getFactionStrength(intel.getFaction(), target.getStarSystem());

		float enemyStr = WarSimScript.getEnemyStrength(intel.getFaction(), target.getStarSystem());
		
		
		boolean hostile = target.getFaction().isHostileTo(intel.getFaction());
		
		//AntiInspectionOrders orders = ((HegemonyInspectionIntel) intel).getOrders(); 
		
		//if (hostile || )
		float defensiveStr = enemyStr + WarSimScript.getStationStrength(target.getFaction(), 
							 target.getStarSystem(), target.getPrimaryEntity());
		if (hostile && defensiveStr >= str) {
			status = RaidStageStatus.FAILURE;
			removeMilScripts();
			giveReturnOrdersToStragglers(getRoutes());
			return;
		}
		
		//status = RaidStageStatus.FAILURE;
		if (hostile) {
			Industry station = Misc.getStationIndustry(target);
			if (station != null) {
				OrbitalStation.disrupt(station);
			}
		}
		
		performRaid(null, target);

		//removeMilScripts();
	}
	
	protected void updateRoutes() {
		resetRoutes();
		
		boolean hostile = target.getFaction().isHostileTo(intel.getFaction());

		AntiInspectionOrders orders = ((HegemonyInspectionIntel)intel).getOrders(); 
		
		if (!hostile && orders == AntiInspectionOrders.RESIST) {
			((HegemonyInspectionIntel)intel).makeHostileAndSendUpdate();
		} else {
			((HegemonyInspectionIntel)intel).sendInSystemUpdate();
		}
		
		gaveOrders = false;
		((HegemonyInspectionIntel)intel).setEnteredSystem(true);
		
		//FactionAPI faction = intel.getFaction();
		
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
		
		HegemonyInspectionIntel intel = ((HegemonyInspectionIntel)this.intel);
		AntiInspectionOrders orders = intel.getOrders();
		boolean resist = orders == AntiInspectionOrders.RESIST;
		
		if (status == RaidStageStatus.FAILURE) {
			if (intel.getOutcome() == HegemonyInspectionOutcome.COLONY_NO_LONGER_EXISTS) {
				info.addPara("The inspection has been aborted.", opad);	
			} else {
				info.addPara("The inspection task force has been defeated by the defenders of " +
					target.getName() + ". The inspection is now over.", opad);
			}
		} else if (status == RaidStageStatus.SUCCESS) {
			CargoAPI cores = Global.getFactory().createCargo(true);
			for (String id : coresRemoved) {
				cores.addCommodity(id, 1);
			}
			cores.sort();
			
			switch (intel.getOutcome()) {
			case BRIBED:
				info.addPara("The funds you've allocated have been used to resolve the inspection to the " +
						"satisfaction of all parties.", opad);
				break;
			case CONFISCATE_CORES:
				if (!cores.isEmpty()) {
					info.addPara("The inspectors have confiscated the following AI cores:", opad);
					info.showCargo(cores, 10, true, opad);
				} else {
					if (resist) {
						info.addPara("The inspectors have not been able to confiscate any AI cores.", opad);
					} else {
						info.addPara("The inspectors have not found any AI cores.", opad);
					}
				}
				break;
			case FOUND_EVIDENCE_NO_CORES:
				if (!cores.isEmpty()) {
					info.addPara("The inspectors have confiscated the following AI cores:", opad);
					info.showCargo(cores, 10, true, opad);
				} else {
					info.addPara("The inspectors have not found any AI cores.", opad);
				}
				info.addPara("There was ample evidence of AI core use, spurring the inspectors to great zeal " +
						"in trying to find them. Local operations have been significantly disrupted.", opad);
				break;
			}
		} else if (curr == index) {
			info.addPara("The inspection of " + target.getName() + " is currently under way.", opad);
			
		}
	}

	public boolean canRaid(CampaignFleetAPI fleet, MarketAPI market) {
		HegemonyInspectionIntel intel = ((HegemonyInspectionIntel)this.intel);
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

















