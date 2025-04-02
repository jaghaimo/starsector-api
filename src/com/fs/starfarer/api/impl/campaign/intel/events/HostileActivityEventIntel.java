package com.fs.starfarer.api.impl.campaign.intel.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.StoryPointActionDelegate;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript.LocationDanger;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.HA_CMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.BaseOptionStoryPointActionDelegate;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.StoryOptionParams;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class HostileActivityEventIntel extends BaseEventIntel implements EconomyUpdateListener, FleetEventListener {

	public static enum Stage {
		START,
		MINOR_EVENT,
		HA_EVENT,
		
		// unused, left in for save compatibility between 0.96a and 0.96.1a, can remove for 0.97a
		@Deprecated HA_1,
		@Deprecated HA_2,
		@Deprecated INCREASED_DEFENSES,
		@Deprecated HA_3,
		@Deprecated HA_4,
	}
	public static String KEY = "$hae_ref";
	
	public static String BUTTON_ESCALATE = "button_escalate";
	
	public static float FP_PER_POINT = Global.getSettings().getFloat("HA_fleetPointsPerPoint");
	
	public static int MAX_PROGRESS = 600;
	public static int ESCALATE_PROGRESS = 550;
	
	public static int RESET_MIN = 0;
	public static int RESET_MAX = 400;
	
	
	public static class HAERandomEventData {
		public HostileActivityFactor factor;
		public EventStageData stage;
		public boolean isReset = false;
		public Object custom;
		public HAERandomEventData(HostileActivityFactor factor, EventStageData stage) {
			this.factor = factor;
			this.stage = stage;
		}
		
	}
	
	public static class HAEFactorDangerData {
		public HostileActivityFactor factor;
		public float mag;
	}
	public static class HAEStarSystemDangerData {
		public StarSystemAPI system;
		public float maxMag;
		public float totalMag;
		public float sortMag;
		public List<HAEFactorDangerData> factorData = new ArrayList<HAEFactorDangerData>();
	}
	
	
	public static HostileActivityEventIntel get() {
		return (HostileActivityEventIntel) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	protected int blowback;
	protected Map<String, MutableStatWithTempMods> systemSpawnMults = new LinkedHashMap<String, MutableStatWithTempMods>();
	
	public HostileActivityEventIntel() {
		super();
		
		//Global.getSector().getEconomy().addUpdateListener(this);
		
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		setup();
		
		// now that the event is fully constructed, add it and send notification
		Global.getSector().getIntelManager().addIntel(this);
	}
	
	protected void setup() {
		
		boolean minorCompleted = false;
		EventStageData minor = getDataFor(Stage.MINOR_EVENT);
		if (minor != null) minorCompleted = minor.wasEverReached;
		
		factors.clear();
		stages.clear();
		
		setMaxProgress(MAX_PROGRESS);
		addStage(Stage.START, 0);
		addStage(Stage.MINOR_EVENT, 300, StageIconSize.MEDIUM);
		addStage(Stage.HA_EVENT, 600, true, StageIconSize.LARGE);
		
		setRandomized(Stage.MINOR_EVENT, RandomizedStageType.BAD, 200, 250, false, false);
		setRandomized(Stage.HA_EVENT, RandomizedStageType.BAD, 425, 500, false);
		
		minor = getDataFor(Stage.MINOR_EVENT);
		if (minor != null) {
			minor.wasEverReached = minorCompleted;
		}
		
		Global.getSector().getListenerManager().removeListenerOfClass(PirateHostileActivityFactor.class);
		Global.getSector().getListenerManager().removeListenerOfClass(LuddicPathHostileActivityFactor.class);
		Global.getSector().getListenerManager().removeListenerOfClass(PerseanLeagueHostileActivityFactor.class);
		Global.getSector().getListenerManager().removeListenerOfClass(TriTachyonHostileActivityFactor.class);
		Global.getSector().getListenerManager().removeListenerOfClass(LuddicChurchHostileActivityFactor.class);
		Global.getSector().getListenerManager().removeListenerOfClass(SindrianDiktatHostileActivityFactor.class);
		Global.getSector().getListenerManager().removeListenerOfClass(HegemonyHostileActivityFactor.class);
		Global.getSector().getListenerManager().removeListenerOfClass(RemnantHostileActivityFactor.class);
		
		
		addFactor(new HAColonyDefensesFactor());
		addFactor(new HAShipsDestroyedFactorHint());
		
		addFactor(new HABlowbackFactor());
		
		PirateHostileActivityFactor pirate = new PirateHostileActivityFactor(this);
		addActivity(pirate, new KantasProtectionPirateActivityCause2(this));
		addActivity(pirate, new StandardPirateActivityCause2(this));
		addActivity(pirate, new PirateBasePirateActivityCause2(this));
		addActivity(pirate, new KantasWrathPirateActivityCause2(this));
		
		LuddicPathHostileActivityFactor path = new LuddicPathHostileActivityFactor(this);
		addActivity(path, new LuddicPathAgreementHostileActivityCause2(this));
		addActivity(path, new StandardLuddicPathActivityCause2(this));
		
		addActivity(new PerseanLeagueHostileActivityFactor(this), new StandardPerseanLeagueActivityCause(this));
		addActivity(new TriTachyonHostileActivityFactor(this), new TriTachyonStandardActivityCause(this));
		addActivity(new LuddicChurchHostileActivityFactor(this), new LuddicChurchStandardActivityCause(this));
		addActivity(new SindrianDiktatHostileActivityFactor(this), new SindrianDiktatStandardActivityCause(this));
		addActivity(new HegemonyHostileActivityFactor(this), new HegemonyAICoresActivityCause(this));
		addActivity(new RemnantHostileActivityFactor(this), new RemnantNexusActivityCause(this));
		
		ListenerUtil.finishedAddingCrisisFactors(this);
	}
	
	
	protected Object readResolve() {
		if (systemSpawnMults == null) {
			systemSpawnMults = new LinkedHashMap<String, MutableStatWithTempMods>();
		}
		return this;
	}
	
	public void redoSetupIfNeeded() {
		if (getDataFor(Stage.INCREASED_DEFENSES) != null || getMaxProgress() == 500) {// || Global.getSettings().isDevMode()) {
			setup();
		}
	}
	
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		Global.getSector().getEconomy().removeUpdateListener(this);
		cleanUpHostileActivityConditions();
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
		
		if (isUpdate && getListInfoParam() instanceof HAERandomEventData) {
			HAERandomEventData data = (HAERandomEventData) getListInfoParam();
			if (data.isReset) {
				data.factor.addBulletPointForEventReset(this, data.stage, info, mode, isUpdate, tc, initPad);
			} else {
				data.factor.addBulletPointForEvent(this, data.stage, info, mode, isUpdate, tc, initPad);
			}
			return;
		}
		
		for (EventStageData stage : stages) {
			if (stage.rollData instanceof HAERandomEventData) {
				HAERandomEventData data = (HAERandomEventData) stage.rollData;
				data.factor.addBulletPointForEvent(this, stage, info, mode, isUpdate, tc, initPad);
				return;
			}
		}
		
//		EventStageData esd = getLastActiveStage(false);
//		if (esd != null && EnumSet.of(Stage.START, Stage.HA_1, Stage.HA_2, Stage.HA_3, Stage.HA_4).contains(esd.id)) {
//			Pair<String, Color> p = getImpactDisplayData((Stage) esd.id);
//			String impact = p.one;
//			Color impactColor = p.two;
////			if (p.one.equals("extreme")) {
////				System.out.println("wefwefwe");
////			}
//			info.addPara("Colony impact: %s", initPad, tc, impactColor, impact);
//			return;
//		}
		
		//super.addBulletPoints(info, mode, isUpdate, tc, initPad);
	}
	
	public HAERandomEventData getRollDataForEvent() {
		EventStageData stage = getDataFor(Stage.HA_EVENT);
		if (stage == null) return null;;
		
		if (stage.rollData instanceof HAERandomEventData) {
			HAERandomEventData data = (HAERandomEventData) stage.rollData;
			return data; 
		}
		return null;
	}

	@Override
	public void addStageDescriptionText(TooltipMakerAPI info, float width, Object stageId) {
		float opad = 10f;
		float small = 0f;
		Color h = Misc.getHighlightColor();
		
		//setProgress(0);
		//setProgress(210);
		//setProgress(600);
		//setProgress(899);
//		setProgress(424);
//		setProgress(480);
//		setProgress(230);
//		random = new Random();
//		setProgress(260);
//		random = new Random();
//		setProgress(499);
		
		List<HAEStarSystemDangerData> systemData = computePlayerSystemDangerData();
		
		EventStageData stage = getDataFor(stageId);
		if (stage == null) return;
		
		if (stage.rollData instanceof HAERandomEventData) {
			HAERandomEventData data = (HAERandomEventData) stage.rollData;
			data.factor.addStageDescriptionForEvent(this, stage, info);
			return;
		}
		
		if (isStageActiveAndLast(stageId)) {
			if (stageId == Stage.START) {
				float delta = getMonthlyProgress();
				
				if (delta <= 0) {
					info.addPara("Low-level pirate activity continues, but aside from that, there are "
							+ "no major crises on the horizon.", small);
				} else {
					info.addPara("A crisis is virtually inevitable at some point, "
							+ "and hostile fleets continually probe your defenses, but where there is "
							+ "danger, there is often opportunity.", small);
				}
//				info.addPara("A crisis is virtually inevitable at some point, "
//						+ "and hostile fleets continually probe your defenses, but where there is "
//						+ "danger, there is often opportunity. A crisis may be averted or delayed by defeating "
//						+ "hostile fleets and taking other actions to address the various contributing factors, "
//						+ "but another crisis will always be just beyond the horizon.", small);
			}
			
			float systemW = 230f;
			float threatW = 300f;
			info.beginTable(getFactionForUIColors(), 20f, 
					"Star system", systemW,
					"Danger", 100f,
					"Primary threats", threatW
					);
			info.makeTableItemsClickable();
			
			int maxSystemsToList = 4;
			int numListed = 0;
			info.addTableHeaderTooltip(0, "Star system with hostile activity, and the name (or number) of your colonies found there.\n\nUp to four of the hardest-hit systems are listed here.");
			info.addTableHeaderTooltip(1, "Danger level of the stronger fleets likely to be found in the system. "
					+ "Approximate, there may be exceptions. Does not include hostile fleets that may be present there for other reasons.");
			info.addTableHeaderTooltip(2, "The most dangerous types of threats likely to be found in the system. "
					+ "Not comprehensive, and does not include hostile fleets that may be present there for other reasons.");
			
			//List<HAEStarSystemDangerData> systemData = computePlayerSystemDangerData();

			for (final HAEStarSystemDangerData sys : systemData) {
				if (sys.sortMag <= 0) continue;
				
				float mag = sys.sortMag;
				String danger = getDangerString(mag);
				
				int maxThreats = 3;
				int count = 0;
				List<String> threats = new ArrayList<String>();
				List<Color> colors = new ArrayList<Color>();
				for (HAEFactorDangerData data : sys.factorData) {
					if (data.mag <= 0) continue;
					threats.add(data.factor.getNameForThreatList(count == 0));
					colors.add(data.factor.getNameColorForThreatList());
					count++;
					if (count >= maxThreats) {
						break;
					}
				}
				String threatStr = Misc.getJoined("", threats);

				LabelAPI label = info.createLabel(threatStr, Misc.getTextColor(), threatW);
				label.setHighlightColors(colors.toArray(new Color[0]));
				label.setHighlight(threats.toArray(new String[0]));
				
				String systemName = sys.system.getNameWithNoType();
				//String systemName = sys.system.getNameWithLowercaseTypeShort();
				List<MarketAPI> colonies = Misc.getMarketsInLocation(sys.system, Factions.PLAYER);
				String colStr = "";
				if (colonies.size() == 1) {
					colStr = colonies.get(0).getName();
				} else {
					colStr = "" + colonies.size() + " colonies";
				}
				systemName += " - " + colStr + "";
				
				info.addRowWithGlow(Alignment.LMID, Misc.getBasePlayerColor(), systemName,
									Alignment.MID, getDangerColor(mag), danger,
									Alignment.MID, null, label);
				info.addTooltipToAddedRow(new BaseFactorTooltip() {
					@Override
					public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
						float w = tooltip.getWidthSoFar();
						float h = Math.round(w / 1.6f);
						tooltip.addSectorMap(w, h, sys.system, 0f);
						tooltip.addPara("Click to open map", Misc.getGrayColor(), 5f);
					}
					
				}, TooltipLocation.LEFT, false);
				
				info.setIdForAddedRow(sys);
				
				numListed++;
				if (numListed >= maxSystemsToList) {
					break;
				}
			}
			info.addTable("None", -1, opad);
			info.addSpacer(3f);
		}
	}
	
	
	
	@Override
	public void afterStageDescriptions(TooltipMakerAPI main) {
		int progress = getProgress();
		if (progress < ESCALATE_PROGRESS) {
			float width = getBarWidth();
			Color color = Misc.getStoryOptionColor();
			Color dark = Misc.getStoryDarkColor();
			float bw = 300f;
			ButtonAPI button = addGenericButton(main, bw, color, dark, "Escalate crisis", BUTTON_ESCALATE);
			float inset = width - bw;
			//inset = 0f;
			button.getPosition().setXAlignOffset(inset);
			main.addSpacer(0f).getPosition().setXAlignOffset(-inset);
			if (progress >= ESCALATE_PROGRESS) {
				button.setEnabled(false);
				main.addTooltipTo(new TooltipCreator() {
					@Override
					public boolean isTooltipExpandable(Object tooltipParam) {
						return false;
					}
					@Override
					public float getTooltipWidth(Object tooltipParam) {
						return 450;
					}
					@Override
					public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
						tooltip.addPara("Only available when event progress is below %s points.", 0f,
								Misc.getHighlightColor(), "" + (int) ESCALATE_PROGRESS);
					}
				}, button, TooltipLocation.BELOW);
			}
		}
	}

	public void tableRowClicked(IntelUIAPI ui, TableRowClickData data) {
		if (data.rowId instanceof HAEStarSystemDangerData) {
			HAEStarSystemDangerData d = (HAEStarSystemDangerData) data.rowId;
			List<MarketAPI> m = Misc.getMarketsInLocation(d.system, Factions.PLAYER);
			if (m.size() == 1) {
				ui.showOnMap(m.get(0).getPrimaryEntity());
			} else {
				ui.showOnMap(d.system.getHyperspaceAnchor());
			}
		}
	}
	
	
	public TooltipCreator getStageTooltipImpl(Object stageId) {
		final EventStageData esd = getDataFor(stageId);
		
		if (esd != null && esd.rollData instanceof HAERandomEventData) {
			HAERandomEventData data = (HAERandomEventData) esd.rollData;
			return data.factor.getStageTooltipImpl(this, esd);
		}

		return null;
	}



	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("events", "hostile_activity");
	}

	

	@Override
	protected String getStageIcon(Object stageId) {
		EventStageData esd = getDataFor(stageId);
		if (esd != null && esd.id == Stage.HA_EVENT && esd.rollData != null && RANDOM_EVENT_NONE.equals(esd.rollData)) {
			return Global.getSettings().getSpriteName("events", "stage_unknown_neutral");
		}
		return super.getStageIcon(stageId);
	}
	
	@Override
	public TooltipCreator getStageTooltip(Object stageId) {
		final EventStageData esd = getDataFor(stageId);
		if (esd != null && esd.id == Stage.HA_EVENT && esd.rollData != null && RANDOM_EVENT_NONE.equals(esd.rollData)) {
			return new TooltipCreator() {
				public boolean isTooltipExpandable(Object tooltipParam) {
					return false;
				}
				public float getTooltipWidth(Object tooltipParam) {
					return BaseEventFactor.TOOLTIP_WIDTH;
				}
				
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					Color h = Misc.getHighlightColor();
					tooltip.addPara("There's no crisis on the horizon right now. When this stage is reached, "
							+ "event progress will be reset to a lower value.",
							0f);
				}
			};
		}
		return super.getStageTooltip(stageId);
	}
	

	protected String getStageIconImpl(Object stageId) {
		EventStageData esd = getDataFor(stageId);
		if (esd == null) return null;
		
//		if (esd.id == Stage.MINOR_EVENT) {
//			System.out.println("ewfwfew");
//		}
		
		//setProgress(48);
		//setProgress(74);
		
		if (esd.rollData instanceof HAERandomEventData) {
			HAERandomEventData data = (HAERandomEventData) esd.rollData;
			return data.factor.getEventStageIcon(this, esd);
		}
//		if (esd.id == Stage.HA_EVENT) {
//			System.out.println("wefwefwe");
//		}
		//if (stageId == Stage.START) return null;
	
		if (esd.id == Stage.START) {
			return Global.getSettings().getSpriteName("events", "hostile_activity_" + ((Stage)esd.id).name());
		}
		// should not happen - the above cases should handle all possibilities - but just in case
		return Global.getSettings().getSpriteName("events", "hostile_activity");
	}
	
	
	@Override
	public Color getBarColor() {
		Color color = Misc.getNegativeHighlightColor();
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
//		if (stageId == Stage.HA_EVENT) {
//			return 1;
//		}
//		if (stageId == Stage.MINOR_EVENT) {
//			return 1;
//		}
		return super.getStageImportance(stageId);
	}



	@Override
	protected String getName() {
		//return "Hostile Activity";
		return "Colony Crises";
	}
	

	public boolean isEventProgressANegativeThingForThePlayer() {
		return true;
	}
	
	
	public void addActivity(BaseHostileActivityFactor factor, HostileActivityCause2 cause) {
		BaseHostileActivityFactor curr = getActivityOfClass(factor.getClass());
		if (curr == null) {
			addFactor(factor);
			curr = factor;
		}
		curr.addCause(cause);
	}
	

	public void removeActivityCause(Class activityClass, Class causeClass) {
		BaseHostileActivityFactor curr = getActivityOfClass(activityClass);
		if (curr == null) return;
		
		HostileActivityCause2 cause = curr.getCauseOfClass(causeClass);
		if (cause == null) return;
		
		curr.getCauses().remove(cause);
		
		if (curr.getCauses().isEmpty()) {
			removeActivity(curr);
		}
	}
	
	public HostileActivityCause2 getActivityCause(Class activityClass, Class causeClass) {
		BaseHostileActivityFactor curr = getActivityOfClass(activityClass);
		if (curr == null) return null;
		
		HostileActivityCause2 cause = curr.getCauseOfClass(causeClass);
		if (cause == null) return null;
		
		return cause;
	}
	
	
	public void removeActivity(BaseHostileActivityFactor plugin) {
		factors.remove(plugin);
	}
	public void removeActivityOfClass(Class c) {
		Iterator<EventFactor> iter = factors.iterator();
		while (iter.hasNext()) {
			EventFactor curr = iter.next();
			if (curr.getClass() == c) {
				iter.remove();
			}
		}
	}
	
	public BaseHostileActivityFactor getActivityOfClass(Class c) {
		Iterator<EventFactor> iter = factors.iterator();
		while (iter.hasNext()) {
			EventFactor curr = iter.next();
			if (curr.getClass() == c) {
				return (BaseHostileActivityFactor) curr;
			}
		}
		return null;
	}
	
	public List<HAEStarSystemDangerData> computePlayerSystemDangerData() {
		List<HAEStarSystemDangerData> systemData = new ArrayList<HAEStarSystemDangerData>();
		for (StarSystemAPI system : Misc.getPlayerSystems(false)) {
			HAEStarSystemDangerData data = computeDangerData(system);
			systemData.add(data);
		}
		Collections.sort(systemData, new Comparator<HAEStarSystemDangerData>() {
			public int compare(HAEStarSystemDangerData o1, HAEStarSystemDangerData o2) {
				int result = (int) Math.signum(o2.sortMag - o1.sortMag);
				if (result == 0) {
					result = (int) Math.signum(o2.totalMag - o1.totalMag);
				}
				if (result == 0) {
					result = (int) Math.signum(o2.system.getId().hashCode() - o1.system.getId().hashCode());
				}
				return result;
			}
		});
		return systemData;
	}
	
	public HAEStarSystemDangerData computeDangerData(StarSystemAPI system) {
		HAEStarSystemDangerData data = new HAEStarSystemDangerData();
		data.system = system;
		
		float maxMag = 0f;
		float total = 0f;
		for (EventFactor factor : factors) {
			if (factor instanceof BaseHostileActivityFactor) {
				HAEFactorDangerData curr = new HAEFactorDangerData();
				curr.factor = (HostileActivityFactor) factor;
				curr.mag = ((BaseHostileActivityFactor) factor).getEffectMagnitude(system);
				data.factorData.add(curr);
				maxMag = Math.max(maxMag, curr.mag);
				total += curr.mag;
			}
		}
		data.maxMag = maxMag;
		data.totalMag = total;
		data.sortMag = data.maxMag * 0.75f + data.totalMag * 0.25f;
		
		Collections.sort(data.factorData, new Comparator<HAEFactorDangerData>() {
			public int compare(HAEFactorDangerData o1, HAEFactorDangerData o2) {
				return (int) Math.signum(o2.mag - o1.mag);
			}
		});
		
		return data;
	}
	
	public LocationDanger getDanger(float mag) {
		if (mag <= 0f) return LocationDanger.NONE;
		if (mag < 0.25f) return LocationDanger.MINIMAL;
		if (mag < 0.5f) return LocationDanger.LOW;
		if (mag < 0.75f) return LocationDanger.MEDIUM;
		if (mag < 1f) return LocationDanger.HIGH;
		return LocationDanger.EXTREME;
	}
	
	public String getDangerString(float mag) {
		return getDangerString(getDanger(mag));
	}
	public String getDangerString(LocationDanger d) {
		switch (d) {
		case EXTREME: return "Extreme";
		case HIGH: return "High";
		case MEDIUM: return "Medium";
		case LOW: return "Low";
		case MINIMAL: return "Minimal";
		case NONE: return "None";
		}
		return "Unknown";
	}
	
	public Color getDangerColor(float mag) {
		LocationDanger d = getDanger(mag);
		if (d == LocationDanger.NONE || d == LocationDanger.MINIMAL) {
			return Misc.getPositiveHighlightColor();
		}
		if (d == LocationDanger.EXTREME || d == LocationDanger.HIGH) {
			return Misc.getNegativeHighlightColor();
		}
		return Misc.getHighlightColor();
	}
	
	public float getVeryApproximateFPStrength(StarSystemAPI system) {
		float mag = getTotalActivityMagnitude(system, true);
		//mag *= getProgressFraction();
		mag *= 0.2f + getMarketPresenceFactor(system) * 0.8f;
		return mag * 1000f;
	}
	
	
	/**
	 * From 0 (at one size-3 market) to 1 (maxSize + count >= 7). Also capped based on largest market.
	 * @param system
	 * @return
	 */
	public float getMarketPresenceFactor(StarSystemAPI system) {
		float maxSize = 0;
		float count = 0;
		for (MarketAPI market : Misc.getMarketsInLocation(system, Factions.PLAYER)) {
			maxSize = Math.max(market.getSize(), maxSize);
			count++;
		}
		
		float f = (maxSize - 3f + count - 1f) / 3f;
		
		float cap = 0.35f;
		if (maxSize <= 4f) cap = 0.55f;
		else if (maxSize <= 5f) cap = 0.75f;
		else cap = 1f;
		
		if (f < 0f) f = 0f;
		if (f > cap) f = cap;
		
		return f;
	}
	
	public float getTotalActivityMagnitude(StarSystemAPI system) {
		return getTotalActivityMagnitude(system, true);
	}
	public float getTotalActivityMagnitude(StarSystemAPI system, boolean capped) {
		//if (true) return 0.1f;
		float total = 0f;
		for (EventFactor factor : factors) {
			if (factor instanceof BaseHostileActivityFactor) {
				total += ((BaseHostileActivityFactor) factor).getEffectMagnitude(system);
			}
		}
		
		if (capped && total > 1f) total = 1f;
		
		total = Math.round(total * 100f) / 100f;
		
		return total;
	}
	
	@Override
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);
		//blowback = 55;
		if (systemSpawnMults != null) {
			float days = Misc.getDays(amount);
			for (MutableStatWithTempMods stat : systemSpawnMults.values()) {
				stat.advance(days);
			}
		}
	}

	public MutableStatWithTempMods getNumFleetsStat(StarSystemAPI system) {
		if (system == null) {
			return new MutableStatWithTempMods(1f);
		}
		if (systemSpawnMults == null) {
			systemSpawnMults = new LinkedHashMap<String, MutableStatWithTempMods>();
		}
		
		String id = system.getId();
		MutableStatWithTempMods stat = systemSpawnMults.get(id);
		if (stat == null) {
			stat = new MutableStatWithTempMods(1f);
			systemSpawnMults.put(id, stat);
		}
		return stat;
	}
	
	public float getNumFleetsMultiplier(StarSystemAPI system) {
		return getNumFleetsStat(system).getModifiedValue();
	}

	public void economyUpdated() {
		cleanUpHostileActivityConditions();
	}
	
	
	public void cleanUpHostileActivityConditions() {
		for (MarketAPI curr : Misc.getPlayerMarkets(false)) {
			if (curr.hasCondition(Conditions.HOSTILE_ACTIVITY)) {
				curr.removeCondition(Conditions.HOSTILE_ACTIVITY);
			}
		}
	}
	
	public boolean isEconomyListenerExpired() {
		return isEnding() || isEnded();
	}
	
	public void commodityUpdated(String commodityId) {
	}

	
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
	}

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (isEnded() || isEnding()) return;
		
		if (!battle.isPlayerInvolved()) return;
		
		if (Global.getSector().getCurrentLocation() instanceof StarSystemAPI &&
				battle.getPlayerSide().contains(primaryWinner)) {
			StarSystemAPI system = (StarSystemAPI) Global.getSector().getCurrentLocation(); 
			for (CampaignFleetAPI otherFleet : battle.getNonPlayerSideSnapshot()) {
				if (otherFleet.isStationMode() && otherFleet.getFleetData().getMembersListCopy().isEmpty()) {
					{
						PirateBaseIntel intel = PirateBaseIntel.getIntelFor(system);
						if (intel != null && Misc.getStationFleet(intel.getMarket()) == otherFleet && 
								HA_CMD.baseInvolved(system, intel)) {
							int tier = intel.getTier().ordinal();
							if (tier < 0) tier = 0;
							if (tier > 4) tier = 4;
							int points = -1 * Global.getSettings().getIntFromArray("HA_pirateBase", tier);
							HAPirateBaseDestroyedFactor factor = new HAPirateBaseDestroyedFactor(points);
							addFactor(factor);
							return;
						}
					}
					{
						LuddicPathBaseIntel intel = LuddicPathBaseIntel.getIntelFor(system);
						if (intel != null && Misc.getStationFleet(intel.getMarket()) == otherFleet) {
							float totalInterest = 0f;
							float activeCells = 0f;
							for (StarSystemAPI curr : Misc.getPlayerSystems(false)) {
								totalInterest += StandardLuddicPathActivityCause2.getPatherInterest(curr, 0f, 0f, 1f);
								activeCells += StandardLuddicPathActivityCause2.getPatherInterest(curr, 0f, 0f, 1f, true);
							}
							
							if (totalInterest > 0) {
								int flat = Global.getSettings().getInt("HA_patherBaseFlat");
								int perCell = Global.getSettings().getInt("HA_patherBasePerActiveCell");
								int max = Global.getSettings().getInt("HA_patherBaseMax");

								int points = -1 * Math.min(max, (flat + perCell * (int) Math.round(activeCells)));
								HAPatherBaseDestroyedFactor factor = new HAPatherBaseDestroyedFactor(points);
								addFactor(factor);
							}
							return;
						}
					}
				}
			}
			
		}
		
		boolean nearAny = false;
		for (StarSystemAPI system : Misc.getPlayerSystems(false)) {
			nearAny |= Misc.isNear(primaryWinner, system.getLocation());
			if (nearAny) break;
		}
		if (!nearAny) return;
		
		float fpDestroyed = 0;
		CampaignFleetAPI first = null;
		for (CampaignFleetAPI otherFleet : battle.getNonPlayerSideSnapshot()) {
			//if (!Global.getSector().getPlayerFaction().isHostileTo(otherFleet.getFaction())) continue;
			for (FleetMemberAPI loss : Misc.getSnapshotMembersLost(otherFleet)) {
				fpDestroyed += loss.getFleetPointCost();
				if (first == null) {
					first = otherFleet;
				}
			}
		}
	
		int points = computeProgressPoints(fpDestroyed);
		if (points > 0) {
			//points = 700;
			HAShipsDestroyedFactor factor = new HAShipsDestroyedFactor(-1 * points);
			//sendUpdateIfPlayerHasIntel(factor, false); // addFactor now sends update
			addFactor(factor);
		}
	}
	
	public static int computeProgressPoints(float fleetPointsDestroyed) {
		if (fleetPointsDestroyed <= 0) return 0;
		
		int points = Math.round(fleetPointsDestroyed / FP_PER_POINT);
		if (points < 1) points = 1;
		return points;
	}

	
	@Override
	protected void notifyStageReached(EventStageData stage) {
		if (stage.rollData instanceof HAERandomEventData) {
			HAERandomEventData data = (HAERandomEventData) stage.rollData;
			boolean fired = data.factor.fireEvent(this, stage);
			stage.rollData = null;
			
			if (stage.id == Stage.HA_EVENT) {
				int resetProgress = getResetProgress(fired);
				setProgress(resetProgress);
			}
		} else if (stage.id == Stage.HA_EVENT &&
				(stage.rollData == null || RANDOM_EVENT_NONE.equals(stage.rollData))) {
			stage.rollData = null;
			int resetProgress = getResetProgress(false);
			setProgress(resetProgress);
		}
	}
	
	protected int getResetProgress(boolean fired) {
		if (!HABlowbackFactor.ENABLED) {
			blowback = 0;
		}
		int min = RESET_MIN;
		if (!fired) min = RESET_MAX - 200;
		
		int resetAdd = random.nextInt(RESET_MAX - min + 1);
		resetAdd = Math.min(resetAdd, random.nextInt(RESET_MAX - min + 1));
		int resetProgress = min + resetAdd;
		
		int add = Math.min(blowback, (int)((RESET_MAX - resetProgress) * 0.5f));
		if (add > 0) {
			resetProgress += add;
			blowback -= add;
		}
		return resetProgress;
	}
	
	public void resetHA_EVENT() {
		EventStageData stage = getDataFor(Stage.HA_EVENT);
		//int resetProgress = stage.progressToRollAt - getRandom().nextInt(100);
		int resetProgress = getResetProgress(false);
		resetRandomizedStage(stage);
		setProgress(resetProgress);
	}
	
	public void resetHA_EVENTIfFromFactor(HostileActivityFactor factor) {
		EventStageData stage = getDataFor(Stage.HA_EVENT);
		if (stage != null && stage.rollData instanceof HAERandomEventData && 
				((HAERandomEventData)stage.rollData).factor == factor) {
			resetHA_EVENT();
		}
	}

	@Override
	public void resetRandomizedStage(EventStageData stage) {
		if (stage.rollData instanceof HAERandomEventData) {
			HAERandomEventData data = (HAERandomEventData) stage.rollData;
			data.isReset = true;
			data.factor.resetEvent(this, stage);
		}
		super.resetRandomizedStage(stage);
	}

	protected HostileActivityFactor prevMajorEventPick = null;
	
	@Override
	public void rollRandomizedStage(EventStageData stage) {
		if (stage.id == Stage.HA_EVENT || stage.id == Stage.MINOR_EVENT) {
			float total = 0f;
			for (EventFactor factor : factors) {
				if (factor instanceof BaseHostileActivityFactor) {
					total += factor.getProgress(this);
				}
			}
			if (total < 1f) total = 1f;
			//System.out.println("Random: " + random.nextLong());
			WeightedRandomPicker<HostileActivityFactor> picker = new WeightedRandomPicker<HostileActivityFactor>(random);
			for (EventFactor factor : factors) {
				if (factor instanceof BaseHostileActivityFactor) {
					BaseHostileActivityFactor curr = (BaseHostileActivityFactor) factor;
					curr.setRandomizedStageSeed(random.nextLong()); // seed will be reused by .rollEvent()
					float f = curr.getEventFrequency(this, stage);
					float w = factor.getProgress(this) / total;
					if (w > 0) {
						w = 0.1f + 0.9f * w;
					}
					picker.add(curr, f * w);
				}
			}
			
			HostileActivityFactor pick = picker.pickAndRemove();
			if (stage.id == Stage.HA_EVENT) {
				if (prevMajorEventPick == pick && !picker.isEmpty()) {
					pick = picker.pickAndRemove();
				}
				prevMajorEventPick = pick;
			}
			
			if (pick == null) return;
			
			stage.rollData = null;
			pick.rollEvent(this, stage);
		}
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_COLONIES);
		return tags;
	}

	
	
	@Override
	public void addFactor(EventFactor factor) {
		if (factor.isOneTime()) {
			int points = factor.getProgress(this);
			if (points < 0) {
				int p = Math.round(-1f * points * HABlowbackFactor.FRACTION);
				p = Math.min(p, getProgress());
				if (p > 0) {
					addBlowback(p);
				}
			}
		}
		super.addFactor(factor);
	}
	
	@Override
	public void reportEconomyMonthEnd() {
		super.reportEconomyMonthEnd();
		
		if (blowback > 0) {
			int amt = Math.round(blowback * HABlowbackFactor.PER_MONTH);
			
			float mult = 1f;
			for (EventFactor factor : factors) {
				if (factor.isOneTime()) continue;
				mult *= factor.getAllProgressMult(this);
			}
			
			amt = Math.round(amt * mult);
			
			if (amt < 1) amt = 1;
			blowback -= amt;
			if (blowback < 0) blowback = 0;
		}
	}

	public void addBlowback(int points) {
		if (!HABlowbackFactor.ENABLED) return;
		blowback += points;
	}
	public int getBlowback() {
		if (!HABlowbackFactor.ENABLED) {
			blowback = 0;
		}
		return blowback;
	}

	public void setBlowback(int blowback) {
		if (!HABlowbackFactor.ENABLED) return;
		this.blowback = blowback;
	}
	
	protected String getSoundForOtherUpdate(Object param) {
		if (param instanceof HAERandomEventData) {
			HAERandomEventData data = (HAERandomEventData) param;
			if (data.isReset) return null;
			if (data.factor == null) return null;
			return data.factor.getEventStageSound(data);
		}
		return null;
	}

	
	@Override
	public int getMaxMonthlyProgress() {
		if (Misc.isEasy()) {
			return Global.getSettings().getInt("ha_maxMonthlyProgressEasy");
		}
		return Global.getSettings().getInt("ha_maxMonthlyProgress");
	}
	
	public void storyActionConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_ESCALATE) {
			ui.recreateIntelUI();
		}
	}
	
	public StoryPointActionDelegate getButtonStoryPointActionDelegate(Object buttonId) {
		if (buttonId == BUTTON_ESCALATE) {
			StoryOptionParams params = new StoryOptionParams(null, 1, "escalateCrisis", 
											Sounds.STORY_POINT_SPEND_INDUSTRY, 
											"Escalated colony crisis");
			return new BaseOptionStoryPointActionDelegate(null, params) {
				@Override
				public void confirm() {
					setProgress(ESCALATE_PROGRESS);
				}
				
				@Override
				public String getTitle() {
					return null;
				}

				@Override
				public void createDescription(TooltipMakerAPI info) {
					info.setParaInsigniaLarge();
					info.addPara("Take certain actions to precipitate a crisis more quickly. Sets event progress "
							+ "to %s points.", -10f, Misc.getHighlightColor(), "" + (int) ESCALATE_PROGRESS);
					info.addSpacer(20f);
					super.createDescription(info);
				}
			};
		}
		return null;
	}
	
}








