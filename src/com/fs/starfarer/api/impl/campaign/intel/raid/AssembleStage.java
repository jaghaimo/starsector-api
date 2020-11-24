package com.fs.starfarer.api.impl.campaign.intel.raid;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.inspection.HIAssembleStage;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PEAssembleStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class AssembleStage extends BaseRaidStage {
	
	public static final String PREP_STAGE = "prep_stage";
	public static final String WAIT_STAGE = "wait_stage";
	
	protected List<MarketAPI> sources = new ArrayList<MarketAPI>();
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);
	
	protected SectorEntityToken gatheringPoint;
	protected float spawnFP = 0;
	protected float origSpawnFP = 0;
	
	protected float minDays = 3f;
	
	public AssembleStage(RaidIntel raid, SectorEntityToken gatheringPoint) {
		super(raid);
		this.gatheringPoint = gatheringPoint;
		interval.forceIntervalElapsed();
	}
	
	public boolean isSourceKnown() {
		return true;
	}
	
	public void setSpawnFP(float spawnFP) {
		this.spawnFP = spawnFP;
		this.origSpawnFP = spawnFP;
	}

	public float getOrigSpawnFP() {
		return origSpawnFP;
	}

	public float getSpawnFP() {
		return spawnFP;
	}

	public void addSource(MarketAPI source) {
		sources.add(source);
	}
	public List<MarketAPI> getSources() {
		return sources;
	}
	
	public void advance(float amount) {
		addRoutesAsNeeded(amount);
		minDays -= Misc.getDays(amount); 
		super.advance(amount);
	}
	
	protected void updateStatus() {
		if (spawnFP > 0) return;
		if (minDays > 0) return;
		
		abortIfNeededBasedOnFP(true);
		updateStatusBasedOnReaching(gatheringPoint, true);
	}
	
	protected int currSource = 0;
	protected String prevType = null;
	
	public static float FP_SMALL = 20;
	public static float FP_MEDIUM = 45;
	public static float FP_LARGE = 85;
	
	protected float getFPSmall() {
		return FP_SMALL;
	}
	protected float getFPMedium() {
		return FP_MEDIUM;
	}
	protected float getFPLarge() {
		return FP_LARGE;
	}
	
	//public static float LARGE_SIZE = Global.getSettings().getFloat("approximateMaxFP");
	protected float getLargeSize(boolean limitToSpawnFP) {
		//if (true) return getFPLarge();
		//float base = LARGE_SIZE;
		float mult = 1f;
		if (!getSources().isEmpty()) {
			MarketAPI source = getSources().get(0);
			ShipPickMode mode = Misc.getShipPickMode(source);
			float base = source.getFaction().getApproximateMaxFPPerFleet(mode);
			
			float numShipsMult = source.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).computeEffective(0f);
			if (numShipsMult < 1f) numShipsMult = 1f;
			mult = 1f / numShipsMult;
			if (limitToSpawnFP) {
				return Math.min(spawnFP, base * mult);
			}
			return base * mult;
		} else {
			return 250f;
		}
		
	}
	
	
	protected String pickNextType() {
		if (spawnFP >= getLargeSize(true) + getFPMedium()) {
			return FleetTypes.PATROL_LARGE;
		}
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>();
		if (!FleetTypes.PATROL_SMALL.equals(prevType)) {
			picker.add(FleetTypes.PATROL_SMALL);
		}
		if (!FleetTypes.PATROL_MEDIUM.equals(prevType) && spawnFP >= FP_MEDIUM) {
			picker.add(FleetTypes.PATROL_MEDIUM);
		}
		if (!FleetTypes.PATROL_LARGE.equals(prevType) && spawnFP >= FP_LARGE) {
			picker.add(FleetTypes.PATROL_LARGE);
		}
		prevType = picker.pick();
		if (prevType == null) prevType = FleetTypes.PATROL_SMALL;
		return prevType;
	}
	
	protected float getFP(String type) {
		if (spawnFP >= getLargeSize(true) + getFPMedium()) {
			float fp = getLargeSize(true);
			spawnFP -= fp;
			return fp;
		}
		
		float base = getFPSmall();
		if (FleetTypes.PATROL_SMALL.equals(type)) {
			base = getFPSmall();
		} else if (FleetTypes.PATROL_MEDIUM.equals(type)) {
			base = getFPMedium();
		} else if (FleetTypes.PATROL_LARGE.equals(type)) {
			base = getFPLarge();
		}
		base *= (1f + ((float) Math.random() - 0.5f) * 0.5f);
		if (base > spawnFP) base = spawnFP;
		spawnFP -= base;
		if (spawnFP < getFPSmall() * 0.5f) {
			base += spawnFP;
			spawnFP = 0f;
		}
		
		return base;
	}
	
	protected void addRoutesAsNeeded(float amount) {
		if (spawnFP <= 0) return;
		
		float days = Misc.getDays(amount);
		
		interval.advance(days);
		if (!interval.intervalElapsed()) return;
			
		if (sources.isEmpty()) {
			status = RaidStageStatus.FAILURE;
			return;
		}
		
		MarketAPI market = sources.get(currSource);
		if (!market.isInEconomy() || !market.getPrimaryEntity().isAlive()) {
			sources.remove(market);
			return;
		}
		
		currSource ++;
		currSource %= sources.size();
		
		
		OptionalFleetData extra = new OptionalFleetData(market);
		
		String sid = intel.getRouteSourceId();
		RouteData route = RouteManager.getInstance().addRoute(sid, market, Misc.genRandomSeed(), extra, intel, null);
		
		extra.fleetType = pickNextType();
		float fp = getFP(extra.fleetType);
		
		//extra.fp = Misc.getAdjustedFP(fp, market);
		extra.fp = fp;
		extra.strength = Misc.getAdjustedStrength(fp, market);

		
		float prepDays = 3f + 3f * (float) Math.random();
		float travelDays = RouteLocationCalculator.getTravelDays(market.getPrimaryEntity(), gatheringPoint);
		
		if (DebugFlags.RAID_DEBUG || DebugFlags.FAST_RAIDS ||
				(this instanceof PEAssembleStage && DebugFlags.PUNITIVE_EXPEDITION_DEBUG) ||
				(this instanceof HIAssembleStage && DebugFlags.HEGEMONY_INSPECTION_DEBUG)) {
			prepDays *= 0.1f;
			travelDays *= 0.1f;
		}
		
		route.addSegment(new RouteSegment(prepDays, market.getPrimaryEntity(), PREP_STAGE));
		route.addSegment(new RouteSegment(travelDays, market.getPrimaryEntity(), gatheringPoint));
		route.addSegment(new RouteSegment(1000f, gatheringPoint, WAIT_STAGE));
		
		maxDays = Math.max(maxDays, prepDays + travelDays);
		//maxDays = 6f;
		
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
			info.addPara("The raiding forces have failed to successfully assemble at the rendezvous point. The raid is now over.", opad);
		} else if (curr == index) {
			if (isSourceKnown()) {
				info.addPara("The raid is currently assembling in the " + gatheringPoint.getContainingLocation().getNameWithLowercaseType() + ".", opad);
			} else {
				info.addPara("The raid is currently assembling at an unknown location.", opad);
			}
		}
	}
	
}



