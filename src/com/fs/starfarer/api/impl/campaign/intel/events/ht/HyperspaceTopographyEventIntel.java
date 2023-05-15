package com.fs.starfarer.api.impl.campaign.intel.events.ht;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.CharacterStatsRefreshListener;
import com.fs.starfarer.api.campaign.listeners.CurrentLocationChangedListener;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

public class HyperspaceTopographyEventIntel extends BaseEventIntel implements FleetEventListener,
																			  CharacterStatsRefreshListener,
																			  CurrentLocationChangedListener {
	
	public static Color BAR_COLOR = Global.getSettings().getColor("progressBarFleetPointsColor");

	
//	public static int PROGRESS_MAX = 1000;
//	public static int PROGRESS_1 = 100;
//	public static int PROGRESS_2 = 400;
//	public static int PROGRESS_3 = 700;
	public static int PROGRESS_MAX = 500;
	public static int PROGRESS_1 = 100;
	public static int PROGRESS_2 = 250;
	public static int PROGRESS_3 = 400;
	
	public static float BASE_DETECTION_RANGE_LY = 3f;
	public static float RANGE_WITHIN_WHICH_SENSOR_ARRAYS_HELP_LY = 5f;
	public static float RANGE_PER_DOMAIN_SENSOR_ARRAY = 2f;
	public static float RANGE_PER_MAKESHIFT_SENSOR_ARRAY = 1f;
	public static int MAX_SENSOR_ARRAYS = 3;
	public static float WAYSTATION_BONUS = 2f;
	
	
	public static float SLIPSTREAM_FUEL_MULT = 0.5f;
	public static float HYPER_BURN_BONUS = 3f;
	
	public static String KEY = "$hte_ref";
	
	public static enum Stage {
		START,
		SLIPSTREAM_DETECTION,
		SLIPSTREAM_NAVIGATION,
		HYPERFIELD_OPTIMIZATION,
		TOPOGRAPHIC_DATA,
	}
	

	public static float RECENT_READINGS_TIMEOUT = 30f;
	public static float RECENT_READINGS_RANGE_LY = 10f;
	
	public static class RecentTopographyReadings {
		public Vector2f loc;
		public RecentTopographyReadings(Vector2f loc) {
			this.loc = loc;
		}
	}
	
	public static void addFactorCreateIfNecessary(EventFactor factor, InteractionDialogAPI dialog) {
		if (get() == null) {
			//TextPanelAPI text = dialog == null ? null : dialog.getTextPanel();
			//new HyperspaceTopographyEventIntel(text);
			// adding a factor anyway, so it'll show a message - don't need to double up
			new HyperspaceTopographyEventIntel(null, false);
		}
		if (get() != null) {
			get().addFactor(factor, dialog);
		}
	}
	
	public static HyperspaceTopographyEventIntel get() {
		return (HyperspaceTopographyEventIntel) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	
	protected TimeoutTracker<RecentTopographyReadings> recent = new TimeoutTracker<RecentTopographyReadings>();
	
//	public static float CHECK_DAYS = 0.1f;
//	protected IntervalUtil interval = new IntervalUtil(CHECK_DAYS * 0.8f, CHECK_DAYS * 1.2f);
//	protected float burnBasedPoints = 0f;
	
	public HyperspaceTopographyEventIntel(TextPanelAPI text, boolean withIntelNotification) {
		super();
		
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		setMaxProgress(PROGRESS_MAX);
		
		addStage(Stage.START, 0);
		addStage(Stage.SLIPSTREAM_NAVIGATION, PROGRESS_1, StageIconSize.MEDIUM);
		addStage(Stage.SLIPSTREAM_DETECTION, PROGRESS_2, StageIconSize.MEDIUM);
		addStage(Stage.HYPERFIELD_OPTIMIZATION, PROGRESS_3, false, StageIconSize.LARGE);
		addStage(Stage.TOPOGRAPHIC_DATA, PROGRESS_MAX, true, StageIconSize.SMALL);
		
		//setRandomized(Stage.TOPOGRAPHIC_DATA, RandomizedStageType.BAD, 400, 450, false);
		getDataFor(Stage.SLIPSTREAM_NAVIGATION).keepIconBrightWhenLaterStageReached = true;
		getDataFor(Stage.SLIPSTREAM_DETECTION).keepIconBrightWhenLaterStageReached = true;
		getDataFor(Stage.HYPERFIELD_OPTIMIZATION).keepIconBrightWhenLaterStageReached = true;

		
//		addFactor(new HADefensiveMeasuresFactor());
//		addFactor(new HAShipsDestroyedFactorHint());
		
		// now that the event is fully constructed, add it and send notification
		Global.getSector().getIntelManager().addIntel(this, !withIntelNotification, text);
	}
	
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
	}

	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, 
			   						Color tc, float initPad) {
		
		if (addEventFactorBulletPoints(info, mode, isUpdate, tc, initPad)) {
			return;
		}
		
		Color h = Misc.getHighlightColor();
		if (isUpdate && getListInfoParam() instanceof EventStageData) {
			EventStageData esd = (EventStageData) getListInfoParam();
			if (esd.id == Stage.SLIPSTREAM_DETECTION) {
				info.addPara("Able to detect slipstreams near your spaceports", tc, initPad);
			}
			if (esd.id == Stage.SLIPSTREAM_NAVIGATION) {
//				info.addPara("Fuel use while traversing slipstreams multiplied by %s", initPad, tc,
//						h, "" + SLIPSTREAM_FUEL_MULT + Strings.X);
				info.addPara("Fuel use while traversing slipstreams reduced by %s", initPad, tc,
						h, "" + (int)Math.round((1f - SLIPSTREAM_FUEL_MULT) * 100f) + "%");
			}
			if (esd.id == Stage.HYPERFIELD_OPTIMIZATION) {
				info.addPara("Maximum burn increased by %s while in hyperspace", initPad, tc, 
						h, "" + (int) HYPER_BURN_BONUS);
			}
			if (esd.id == Stage.TOPOGRAPHIC_DATA) {
				info.addPara("Topographic data gained", tc, initPad);
			}
			return;
		}
		
//		EventStageData esd = getLastActiveStage(false);
//		if (esd != null && EnumSet.of(Stage.START, Stage.HA_1, Stage.HA_2, Stage.HA_3, Stage.HA_4).contains(esd.id)) {
//			
//		}
	}

	@Override
	public void addStageDescriptionText(TooltipMakerAPI info, float width, Object stageId) {
		float opad = 10f;
		float small = 0f;
		Color h = Misc.getHighlightColor();
		
		//setProgress(0);
		//setProgress(199);
		//setProgress(600);
		//setProgress(899);
		//setProgress(1000);
		//setProgress(499);
		//setProgress(600);
		
		EventStageData stage = getDataFor(stageId);
		if (stage == null) return;
		
//		if (isStageActiveAndLast(stageId) &&  stageId == Stage.START) {
//			addStageDesc(info, stageId, small, false);
//		} else if (isStageActive(stageId) && stageId != Stage.START) {
//			addStageDesc(info, stageId, small, false);
//		}
		
		if (isStageActive(stageId)) {
			addStageDesc(info, stageId, small, false);
		}
	}
	
	
	public void addStageDesc(TooltipMakerAPI info, Object stageId, float initPad, boolean forTooltip) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (stageId == Stage.START) {
			info.addPara("Detailed sensor readings greatly aid hyperspace navigation. "
					+ "There are many ways of acquiring this data, including using in-system sensor arrays,"
					+ " using an Active Sensor Burst near interesting phenomena, "
					+ "traveling through hyperspace at a very high burn level, or simply buying the data from scavengers.",
					initPad);
		} else if (stageId == Stage.SLIPSTREAM_DETECTION) {
			info.addPara("The facilities and staff at a Spaceport are able to interpret data from various sources "
					+ "to discover the presence of nearby slipstreams. The detection range is increased "
					+ "for larger colonies. Claimed sensor arrays within %s light-years provide an additional "
					+ "bonus - %s ly for Domain-era arrays, and %s ly for makeshift ones. "
					+ "Up to %s sensor arrays can be of use.", initPad, 
					Misc.getHighlightColor(),
					"" + (int) RANGE_WITHIN_WHICH_SENSOR_ARRAYS_HELP_LY,
					"+" + (int) RANGE_PER_DOMAIN_SENSOR_ARRAY,
					"+" + (int) RANGE_PER_MAKESHIFT_SENSOR_ARRAY,
					"" + (int) MAX_SENSOR_ARRAYS
					);
		} else if (stageId == Stage.SLIPSTREAM_NAVIGATION) {
			info.addPara("Fuel use while traveling inside slipstreams reduced by %s. This reduction is multiplicative " +
						 "with the baseline fuel use reduction for traveling inside a slipstream.",
					initPad, h,
					"" + (int)Math.round((1f - SLIPSTREAM_FUEL_MULT) * 100f) + "%");
		} else if (stageId == Stage.HYPERFIELD_OPTIMIZATION) {
			info.addPara("Maximum burn increased by %s while in hyperspace.", initPad, h,
					"" + (int) HYPER_BURN_BONUS);
		} else if (stageId == Stage.TOPOGRAPHIC_DATA) {
			int min = getTopoResetMin();
			int max = getTopoResetMax();
			info.addPara("A batch of topographic data that can be sold for a"
					+ " considerable number of credits.", initPad);
			info.addPara("Event progress will be reset to between %s and %s points when this outcome is reached.",
					opad, h, "" + min, "" + max);
		}
	}
	
	public TooltipCreator getStageTooltipImpl(Object stageId) {
		final EventStageData esd = getDataFor(stageId);
		
		if (esd != null && EnumSet.of(Stage.SLIPSTREAM_DETECTION, Stage.SLIPSTREAM_NAVIGATION,
				Stage.HYPERFIELD_OPTIMIZATION, Stage.TOPOGRAPHIC_DATA).contains(esd.id)) {
			return new BaseFactorTooltip() {
				@Override
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					float opad = 10f;
					
					if (esd.id == Stage.SLIPSTREAM_DETECTION) {
						tooltip.addTitle("Slipstream detection");
					} else if (esd.id == Stage.SLIPSTREAM_NAVIGATION) {
						tooltip.addTitle("Slipstream navigation");
					} else if (esd.id == Stage.HYPERFIELD_OPTIMIZATION) {
						tooltip.addTitle("Hyperfield optimization");
					} else if (esd.id == Stage.TOPOGRAPHIC_DATA) {
						tooltip.addTitle("Topographic data");
					}

					addStageDesc(tooltip, esd.id, opad, true);
					
					esd.addProgressReq(tooltip, opad);
				}
			};
		}
		
		return null;
	}



	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("events", "hyperspace_topography");
	}

	protected String getStageIconImpl(Object stageId) {
		EventStageData esd = getDataFor(stageId);
		if (esd == null) return null;
		
	
		if (EnumSet.of(Stage.SLIPSTREAM_DETECTION, Stage.SLIPSTREAM_NAVIGATION, Stage.HYPERFIELD_OPTIMIZATION, 
				Stage.TOPOGRAPHIC_DATA, Stage.START).contains(esd.id)) {
			return Global.getSettings().getSpriteName("events", "hyperspace_topography_" + ((Stage)esd.id).name());
		}
		// should not happen - the above cases should handle all possibilities - but just in case
		return Global.getSettings().getSpriteName("events", "hyperspace_topography");
	}
	
	
	@Override
	public Color getBarColor() {
		Color color = BAR_COLOR;
		//color = Misc.getBasePlayerColor();
		color = Misc.interpolateColor(color, Color.black, 0.25f);
		return color;
	}
	
	@Override
	public Color getBarProgressIndicatorColor() {
		return super.getBarProgressIndicatorColor();
	}

	@Override
	protected int getStageImportance(Object stageId) {
		return super.getStageImportance(stageId);
	}


	@Override
	protected String getName() {
		return "Hyperspace Topography";
	}
	

	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		
	}
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
//		if (isEnded() || isEnding()) return;
//		
//		if (!battle.isPlayerInvolved()) return;
		
//		HAShipsDestroyedFactor factor = new HAShipsDestroyedFactor(-1 * points);
//		sendUpdateIfPlayerHasIntel(factor, false);
//		addFactor(factor);
	}
	

	public int getTopoResetMin() {
		EventStageData stage = getDataFor(Stage.HYPERFIELD_OPTIMIZATION);
		return stage.progress;
	}
	public int getTopoResetMax() {
		return getTopoResetMin() + 50;
	}

	public void resetTopographicData() {
		int resetProgress = getTopoResetMin() + getRandom().nextInt(getTopoResetMax() - getTopoResetMin() + 1);
		setProgress(resetProgress);
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_EXPLORATION);
		//tags.remove(Tags.INTEL_MAJOR_EVENT);
		return tags;
	}

	@Override
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);
		applyFleetEffects();
		
		float days = Global.getSector().getClock().convertToDays(amount);
		recent.advance(days);
		
		//setProgress(getProgress() + 10);
	}
	
	@Override
	protected void notifyStageReached(EventStageData stage) {
		//applyFleetEffects();
		
		if (stage.id == Stage.TOPOGRAPHIC_DATA) {
			resetTopographicData();
			
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			cargo.addSpecial(new SpecialItemData(Items.TOPOGRAPHIC_DATA, null), 1);
			//sendUpdateIfPlayerHasIntel(stage, getTextPanelForStageChange());
		}
	}
	
	public void reportCurrentLocationChanged(LocationAPI prev, LocationAPI curr) {
		//applyFleetEffects();
	}
	
	public void reportAboutToRefreshCharacterStatEffects() {
		
	}

	public void reportRefreshedCharacterStatEffects() {
		// called when opening colony screen, so the Spaceport tooltip gets the right values
		updateMarketDetectionRanges();
		applyFleetEffects();
	}
	
	public void applyFleetEffects() {
		String id1 = "hypertopology1";
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		pf.getStats().getFleetwideMaxBurnMod().unmodifyFlat(id1);
		
		MutableStat stat = pf.getStats().getDynamic().getStat(Stats.FUEL_USE_NOT_SHOWN_ON_MAP_MULT);
		stat.unmodifyMult(id1);
		
		//if (pf.isInHyperspace()) { // doesn't work; after reportCurrentLocationChanged()
		// the current location is right but the player fleet hasn't been added to it yet
		if (Global.getSector().getCurrentLocation().isHyperspace()) {
			if (isStageActive(Stage.SLIPSTREAM_NAVIGATION)) {
				for (StatMod mod : stat.getMultMods().values()) {
					if (SlipstreamTerrainPlugin2.FUEL_USE_MODIFIER_DESC.equals(mod.desc)) {
						stat.modifyMult(id1, SLIPSTREAM_FUEL_MULT, 
								SlipstreamTerrainPlugin2.FUEL_USE_MODIFIER_DESC + " (hyperspace topography)");
						break;
					}
				}
			}
			
			if (isStageActive(Stage.HYPERFIELD_OPTIMIZATION)) {
				pf.getStats().getFleetwideMaxBurnMod().modifyFlat(id1, HYPER_BURN_BONUS, "Hyperspace topography");
			}
			
		}
	}
	
	public void updateMarketDetectionRanges() {
		if (isStageActive(Stage.SLIPSTREAM_DETECTION)) {
			String id1 = "hypertopology1";
			String id2 = "hypertopology2";
			String id3 = "hypertopology3";
			String id4 = "hypertopology4";
			for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
				if (market.isHidden()) continue;

				boolean unapplicable = false;
				Industry spaceport = market.getIndustry(Industries.SPACEPORT);
				if (spaceport == null) {
					spaceport = market.getIndustry(Industries.MEGAPORT);
				}
				if (spaceport == null || !spaceport.isFunctional()) {
					unapplicable = true;
				}
				
				StatBonus mod = market.getStats().getDynamic().getMod(Stats.SLIPSTREAM_REVEAL_RANGE_LY_MOD);
				if (!market.isPlayerOwned() || unapplicable) {
					mod.unmodify(id1);
					mod.unmodify(id2);
					mod.unmodify(id3);
					mod.unmodify(id4);
					continue;
				}
				
				mod.modifyFlat(id1, BASE_DETECTION_RANGE_LY, "Base detection range");
				mod.modifyFlat(id2, market.getSize(), "Colony size");
				
				float arraysBonus = gerSensorArrayBonusFor(market, RANGE_WITHIN_WHICH_SENSOR_ARRAYS_HELP_LY);
				
				mod.modifyFlatAlways(id3, arraysBonus, 
						"Claimed sensor arrays within " + (int) RANGE_WITHIN_WHICH_SENSOR_ARRAYS_HELP_LY + 
						" ly (max: " + (int) MAX_SENSOR_ARRAYS + " arrays)");
			}
		}
	}
	
	public float gerSensorArrayBonusFor(MarketAPI market, float range) {
		int countDomain = 0;
		int countMakeshift= 0;
		Vector2f locInHyper = market.getLocationInHyperspace();
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float dist = Misc.getDistanceLY(locInHyper, system.getLocation());
			if (dist > range && Math.round(dist * 10f) <= range * 10f) {
				dist = range;
			}
			if (dist <= range) {
				for (SectorEntityToken entity : system.getEntitiesWithTag(Tags.SENSOR_ARRAY)) {
					if (entity.getFaction() != null && entity.getFaction().isPlayerFaction()) {
						if (entity.hasTag(Tags.MAKESHIFT)) {
							countMakeshift++;
						} else {
							countDomain++;
						}
					}
				}
			}
		}
		
		float bonus = Math.min(countDomain, MAX_SENSOR_ARRAYS) * RANGE_PER_DOMAIN_SENSOR_ARRAY;
		bonus += Math.min(Math.max(0, countMakeshift - countDomain), MAX_SENSOR_ARRAYS) * RANGE_PER_MAKESHIFT_SENSOR_ARRAY;

		return bonus;
	}

	public boolean withMonthlyFactors() {
		return false;
	}
	
	
	public void addRecentReadings(Vector2f loc) {
		recent.add(new RecentTopographyReadings(loc), RECENT_READINGS_TIMEOUT);
	}
	
	public static boolean hasRecentReadingsNearPlayer() {
		return get() != null && get().hasRecentReadingsNear(Global.getSector().getPlayerFleet().getLocationInHyperspace());
	}
	
	public boolean hasRecentReadingsNear(Vector2f loc) {
		for (RecentTopographyReadings curr : recent.getItems()) {
			float distLY = Misc.getDistanceLY(loc, curr.loc);
			if (distLY <= RECENT_READINGS_RANGE_LY) {
				return true;
			}
		}
		return false;
	}
}








