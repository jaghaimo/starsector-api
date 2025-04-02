package com.fs.starfarer.api.impl.campaign.intel.events.ht;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PersistentUIDataAPI.AbilitySlotAPI;
import com.fs.starfarer.api.campaign.PersistentUIDataAPI.AbilitySlotsAPI;
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
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddAbility;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.TokenType;
import com.fs.starfarer.api.util.TimeoutTracker;

public class HyperspaceTopographyEventIntel extends BaseEventIntel implements FleetEventListener,
																			  CharacterStatsRefreshListener,
																			  CurrentLocationChangedListener {
	
	public static Color BAR_COLOR = Global.getSettings().getColor("progressBarFleetPointsColor");

	
//	public static int PROGRESS_MAX = 1000;
//	public static int PROGRESS_1 = 100;
//	public static int PROGRESS_2 = 400;
//	public static int PROGRESS_3 = 700;
	public static int PROGRESS_MAX = 1000;
	public static int PROGRESS_1 = 100;
	public static int PROGRESS_2 = 250;
	public static int PROGRESS_3 = 400;
	public static int PROGRESS_4 = 550;
	public static int PROGRESS_5 = 700;
	
	public static float BASE_DETECTION_RANGE_LY = 3f;
	public static float RANGE_WITHIN_WHICH_SENSOR_ARRAYS_HELP_LY = 5f;
	public static float RANGE_PER_DOMAIN_SENSOR_ARRAY = 2f;
	public static float RANGE_PER_MAKESHIFT_SENSOR_ARRAY = 1f;
	public static int MAX_SENSOR_ARRAYS = 3;
	public static float WAYSTATION_BONUS = 2f;
	
	
	public static float SLIPSTREAM_FUEL_MULT = 0.25f;
	public static float HYPER_BURN_BONUS = 3f;
	
	public static String KEY = "$hte_ref";
	
	public static enum Stage {
		START,
		SLIPSTREAM_DETECTION,
		SLIPSTREAM_NAVIGATION,
		REVERSE_POLARITY,
		HYPERFIELD_OPTIMIZATION,
		GENERATE_SLIPSURGE,
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
		
		
		setup();
		
		// now that the event is fully constructed, add it and send notification
		Global.getSector().getIntelManager().addIntel(this, !withIntelNotification, text);
	}
	
	protected void setup() {
		factors.clear();
		stages.clear();
		
		setMaxProgress(PROGRESS_MAX);
		
		addStage(Stage.START, 0);
		addStage(Stage.SLIPSTREAM_NAVIGATION, PROGRESS_1, StageIconSize.MEDIUM);
		addStage(Stage.REVERSE_POLARITY, PROGRESS_2, StageIconSize.LARGE);
		addStage(Stage.SLIPSTREAM_DETECTION, PROGRESS_3, StageIconSize.MEDIUM);
		addStage(Stage.HYPERFIELD_OPTIMIZATION, PROGRESS_4, StageIconSize.MEDIUM);
		addStage(Stage.GENERATE_SLIPSURGE, PROGRESS_5, StageIconSize.LARGE);
		addStage(Stage.TOPOGRAPHIC_DATA, PROGRESS_MAX, true, StageIconSize.SMALL);
		
		getDataFor(Stage.SLIPSTREAM_NAVIGATION).keepIconBrightWhenLaterStageReached = true;
		getDataFor(Stage.SLIPSTREAM_DETECTION).keepIconBrightWhenLaterStageReached = true;
		getDataFor(Stage.REVERSE_POLARITY).keepIconBrightWhenLaterStageReached = true;
		getDataFor(Stage.HYPERFIELD_OPTIMIZATION).keepIconBrightWhenLaterStageReached = true;
		getDataFor(Stage.GENERATE_SLIPSURGE).keepIconBrightWhenLaterStageReached = true;
		
	}
	
	protected Object readResolve() {
		if (getDataFor(Stage.GENERATE_SLIPSURGE) == null) {
			setup();
		}
		return this;
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
			if (esd.id == Stage.REVERSE_POLARITY) {
				info.addPara("%s ability unlocked", initPad, tc, h, "Reverse Polarity");
			}
			if (esd.id == Stage.GENERATE_SLIPSURGE) {
				info.addPara("%s ability unlocked", initPad, tc, h, "Generate Slipsurge");
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
	
	public float getImageSizeForStageDesc(Object stageId) {
//		if (stageId == Stage.REVERSE_POLARITY || stageId == Stage.GENERATE_SLIPSURGE) {
//			return 48f;
//		}
		if (stageId == Stage.START) {
			return 64f;
		}
		return 48f;
	}
	public float getImageIndentForStageDesc(Object stageId) {
//		if (stageId == Stage.REVERSE_POLARITY || stageId == Stage.GENERATE_SLIPSURGE) {
//			return 16f;
//		}
		if (stageId == Stage.START) {
			return 0f;
		}
		return 16f;
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
//			info.addPara("The facilities and staff at a Spaceport are able to interpret data from various sources "
//					+ "to discover the presence of nearby slipstreams. The detection range is increased "
//					+ "for larger colonies. Claimed sensor arrays within %s light-years provide an additional "
//					+ "bonus - %s ly for Domain-era arrays, and %s ly for makeshift ones. "
//					+ "Up to %s sensor arrays can be of use.", initPad, 
//					Misc.getHighlightColor(),
//					"" + (int) RANGE_WITHIN_WHICH_SENSOR_ARRAYS_HELP_LY,
//					"+" + (int) RANGE_PER_DOMAIN_SENSOR_ARRAY,
//					"+" + (int) RANGE_PER_MAKESHIFT_SENSOR_ARRAY,
//					"" + (int) MAX_SENSOR_ARRAYS
//					);
			info.addPara("Allows a Spaceport "
					+ "to detect nearby slipstreams. Detection range increased "
					+ "for %s. Claimed sensor arrays within %s light-years provide extra detection range: "
					+ "%s ly for Domain-era arrays, and %s ly for makeshift ones. "
					+ "Up to %s sensor arrays can be used.", initPad, 
					Misc.getHighlightColor(),
					"larger colonies",
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
		} else if (stageId == Stage.REVERSE_POLARITY) {
			info.addPara("Unlocks the %s ability, which allows your fleet to "
					+ "travel against the current of slipstreams.", initPad, h,
					"Reverse Polarity");
		} else if (stageId == Stage.GENERATE_SLIPSURGE) {
			info.addPara("Unlocks the %s ability, which allows your fleet to "
					+ "create powerful, short-lived slipstreams useful for rapid travel.", initPad, h,
					"Generate Slipsurge");
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
				Stage.GENERATE_SLIPSURGE, Stage.REVERSE_POLARITY,
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
					} else if (esd.id == Stage.REVERSE_POLARITY) {
						tooltip.addTitle("Reverse Polarity");
					} else if (esd.id == Stage.GENERATE_SLIPSURGE) {
						tooltip.addTitle("Generate Slipsurge");
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
		if (stageId == Stage.REVERSE_POLARITY) {
			return Global.getSettings().getAbilitySpec(Abilities.REVERSE_POLARITY).getIconName();
		}
		if (stageId == Stage.GENERATE_SLIPSURGE) {
			return Global.getSettings().getAbilitySpec(Abilities.GENERATE_SLIPSURGE).getIconName();
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
		EventStageData stage = getDataFor(Stage.GENERATE_SLIPSURGE);
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
	
	public void addAbility(String id) {
		if (Global.getSector().getPlayerFleet().hasAbility(id)) {
			return;
		}
		List<Token> params = new ArrayList<Token>();
		Token t = new Token(id, TokenType.LITERAL);
		params.add(t);
		t = new Token("-1", TokenType.LITERAL);
		params.add(t); // don't want to assign it to a slot - will assign as hyper-only alternate later here
		new AddAbility().execute(null, null, params, null);
		
		
		AbilitySlotsAPI slots = Global.getSector().getUIData().getAbilitySlotsAPI();
		int curr = slots.getCurrBarIndex();
		OUTER: for (int i = 0; i < 5; i++) {
			slots.setCurrBarIndex(i);
			for (AbilitySlotAPI slot : slots.getCurrSlotsCopy()) {
				if (Abilities.REVERSE_POLARITY.equals(id) && Abilities.SCAVENGE.equals(slot.getAbilityId())) {
					slot.setInHyperAbilityId(Abilities.REVERSE_POLARITY);
					break OUTER;
				}
				if (Abilities.GENERATE_SLIPSURGE.equals(id) && Abilities.DISTRESS_CALL.equals(slot.getAbilityId())) {
					slot.setInHyperAbilityId(Abilities.GENERATE_SLIPSURGE);
					break OUTER;
				}
			}
		}
		slots.setCurrBarIndex(curr);
	}
	
	
	@Override
	protected void notifyStageReached(EventStageData stage) {
		//applyFleetEffects();
		
		if (stage.id == Stage.REVERSE_POLARITY) {
			addAbility(Abilities.REVERSE_POLARITY);
		}
		if (stage.id == Stage.GENERATE_SLIPSURGE) {
			addAbility(Abilities.GENERATE_SLIPSURGE);
		}
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
		float useMakeshift = Math.min(MAX_SENSOR_ARRAYS - countDomain, countMakeshift);
		if (useMakeshift < 0) useMakeshift = 0;
		bonus += useMakeshift * RANGE_PER_MAKESHIFT_SENSOR_ARRAY;
		//bonus += Math.min(Math.max(0, countMakeshift - countDomain), MAX_SENSOR_ARRAYS) * RANGE_PER_MAKESHIFT_SENSOR_ARRAY;

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
	
	protected String getSoundForStageReachedUpdate(Object stageId) {
		if (stageId == Stage.REVERSE_POLARITY || stageId == Stage.GENERATE_SLIPSURGE) {
			return "ui_learned_ability";
		}
		return super.getSoundForStageReachedUpdate(stageId);
	}

	@Override
	protected String getSoundForOneTimeFactorUpdate(EventFactor factor) {
//		if (factor instanceof HTAbyssalLightFactor) {
//			return "sound_none";
//		}
		return null;
	}
	
	
	
}








