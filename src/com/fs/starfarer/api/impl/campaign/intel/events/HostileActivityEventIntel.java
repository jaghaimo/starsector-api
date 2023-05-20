package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript.LocationDanger;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.HA_CMD;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.Range;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class HostileActivityEventIntel extends BaseEventIntel implements EconomyUpdateListener, FleetEventListener {

	public static enum Stage {
		START,
		HA_1,
		MINOR_EVENT,
		HA_2,
		INCREASED_DEFENSES,
		HA_3,
		HA_4,
		HA_EVENT,
	}
	public static String KEY = "$hae_ref";
	
	public static float FP_PER_POINT = Global.getSettings().getFloat("HA_fleetPointsPerPoint");
	
	
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
	
	
	protected float accessibilityPenalty;
	protected float stabilityPenalty;
	
	public static HostileActivityEventIntel get() {
		return (HostileActivityEventIntel) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	public HostileActivityEventIntel() {
		super();
		
		Global.getSector().getEconomy().addUpdateListener(this);
		
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		setMaxProgress(500);
		addStage(Stage.START, 0);
		addStage(Stage.HA_1, 50, StageIconSize.MEDIUM);
		addStage(Stage.MINOR_EVENT, 100, StageIconSize.SMALL);
		addStage(Stage.HA_2, 150, StageIconSize.MEDIUM);
		addStage(Stage.INCREASED_DEFENSES, 200, true, StageIconSize.LARGE);
		addStage(Stage.HA_3, 250, StageIconSize.MEDIUM);
		addStage(Stage.HA_4, 350, StageIconSize.MEDIUM);
		addStage(Stage.HA_EVENT, 500, true, StageIconSize.LARGE);
		
		setRandomized(Stage.MINOR_EVENT, RandomizedStageType.BAD, 50, 75, false, false);
		setRandomized(Stage.HA_EVENT, RandomizedStageType.BAD, 400, 450, false);
		
		getDataFor(Stage.INCREASED_DEFENSES).sendIntelUpdateOnReaching = false;

		
		addFactor(new HADefensiveMeasuresFactor());
		addFactor(new HAShipsDestroyedFactorHint());
		
		addActivity(new PirateHostileActivityFactor(this), new KantasProtectionPirateActivityCause2(this));
		addActivity(new PirateHostileActivityFactor(this), new StandardPirateActivityCause2(this));
		addActivity(new PirateHostileActivityFactor(this), new PirateBasePirateActivityCause2(this));
		addActivity(new PirateHostileActivityFactor(this), new KantasWrathPirateActivityCause2(this));
		
		addActivity(new LuddicPathHostileActivityFactor(this), new LuddicPathAgreementHostileActivityCause2(this));
		addActivity(new LuddicPathHostileActivityFactor(this), new StandardLuddicPathActivityCause2(this));
		
		// now that the event is fully constructed, add it and send notification
		Global.getSector().getIntelManager().addIntel(this);
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
	
	public static Pair<String, Color> getImpactDisplayData(Stage id) {
		Pair<String, Color> p = new Pair<String, Color>();
		if (id == Stage.START) {
			p.one = "minimal";
			p.two = Misc.getPositiveHighlightColor();
		} else if (id == Stage.HA_1) {
			p.one = "low";
			p.two = Misc.getPositiveHighlightColor();
		} else if (id == Stage.HA_2) {
			p.one = "moderate";
			p.two = Misc.getHighlightColor();
		} else if (id == Stage.HA_3) {
			p.one = "high";
			p.two = Misc.getNegativeHighlightColor();
		} else if (id == Stage.HA_4) {
			p.one = "extreme";
			p.two = Misc.getNegativeHighlightColor();
		}
		return p;
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
		
		// handled generically in BaseEventIntel class when the factor is added
//		if (isUpdate && getListInfoParam() instanceof HAShipsDestroyedFactor) {
//			HAShipsDestroyedFactor factor = (HAShipsDestroyedFactor) getListInfoParam();
//			info.addPara("Hostile ships destroyed: %s points", initPad, tc, factor.getProgressColor(this), 
//					factor.getProgressStr(this));
//			return;
//		}
		
		if (isUpdate && getListInfoParam() instanceof EventStageData) {
			EventStageData esd = (EventStageData) getListInfoParam();
			if (EnumSet.of(Stage.START, Stage.HA_1, Stage.HA_2, Stage.HA_3, Stage.HA_4).contains(esd.id)) {
				String delta = "increased";
				if (!prevProgressDeltaWasPositive) delta = "reduced";
				
				Pair<String, Color> p = getImpactDisplayData((Stage) esd.id);
				String impact = p.one;
				Color impactColor = p.two;
				info.addPara("Impact " + delta + " to: %s", initPad, tc, impactColor, impact);
				return;
			}
		}
		
		EventStageData esd = getLastActiveStage(false);
		if (esd != null && EnumSet.of(Stage.START, Stage.HA_1, Stage.HA_2, Stage.HA_3, Stage.HA_4).contains(esd.id)) {
			Pair<String, Color> p = getImpactDisplayData((Stage) esd.id);
			String impact = p.one;
			Color impactColor = p.two;
//			if (p.one.equals("extreme")) {
//				System.out.println("wefwefwe");
//			}
			info.addPara("Colony impact: %s", initPad, tc, impactColor, impact);
			return;
		}
		
		//super.addBulletPoints(info, mode, isUpdate, tc, initPad);
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
		//setProgress(499);
		//setProgress(400);
		
		updatePenalties();
		
		List<HAEStarSystemDangerData> systemData = computePlayerSystemDangerData();
		
		EventStageData stage = getDataFor(stageId);
		if (stage == null) return;
		
		if (stage.rollData instanceof HAERandomEventData) {
			HAERandomEventData data = (HAERandomEventData) stage.rollData;
			data.factor.addStageDescriptionForEvent(this, stage, info);
			return;
		}
		
		//if (stageId == Stage.START) {
		if (isStageActiveAndLast(stageId)) {
//			if (stageId == Stage.START) {
//				info.addTitle("Minimal impact");
//			} else if (stage.id == Stage.HA_1) {
//				info.addTitle("Low impact");
//			} else if (stage.id == Stage.HA_2) {
//				info.addTitle("Moderate impact");
//			} else if (stage.id == Stage.HA_3) {
//				info.addTitle("High impact");
//			} else if (stage.id == Stage.HA_4) {
//				info.addTitle("Extreme impact");
//			}
			
			if (stageId == Stage.START) {
				info.addPara("Various hostile forces are threatening your colonies. "
					+ "If left unchecked, your hardest-hit colonies will have "
					+ "to devote more resources to defensive measures, reducing their stability and accessibility.", small);
			} else {
				Range affects = new Range(((Stage)stageId).name() + "_affects");
				int numSystems = (int) Math.max(Math.min(systemData.size(), affects.min), Math.round((float) systemData.size() * affects.max));
				int numColonies = 0;
				int num = 0;
				List<String> systemsList = new ArrayList<String>();
				for (HAEStarSystemDangerData curr : systemData) {
					numColonies += Misc.getMarketsInLocation(curr.system, Factions.PLAYER).size();
					num++;
					if (num <= 3) {
						systemsList.add(curr.system.getNameWithNoType());
					}
					if (num >= numSystems) break;
				}
				int extra = numSystems - systemsList.size();
				if (extra > 0) {
					systemsList.remove(systemsList.size() - 1);
					systemsList.add("" + (extra + 1) + " other");
				}
				String isOrAre = "are";
				String coloniesStr = "colonies";
				if (numColonies == 1) {
					isOrAre = "is";
					coloniesStr = "colony";
				}
				String systemsStr = "systems";
				if (numSystems == 1) systemsStr = "system";
				
				String desc1 = "minor";
				String desc2 = "some";
				if (stageId == Stage.HA_2) {
					desc1 = "moderate";
					desc2 = "considerable";
				} else if (stageId == Stage.HA_3) {
					desc1 = "serious";
					desc2 = "a lot of";
				} else if (stageId == Stage.HA_4) {
					desc1 = "extreme";
					desc2 = "much of their";
				}
				
				String combined = "Your " + coloniesStr + " in the " + Misc.getAndJoined(systemsList) + " " + systemsStr + 
						" " + isOrAre;
				if (numSystems == systemData.size() && numColonies > 1) {
					combined = "All of your colonies are";
				}
				info.addPara(combined + " forced to make " + desc1 + 
						" concessions to security considerations and devote " + 
						desc2 + " resources to defensive measures. " +
						"Stability reduced by %s, accessibility reduced by %s.", small, h,
						"" + (int) stabilityPenalty, 
						"" + (int) Math.round(accessibilityPenalty * 100f) + "%");
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
				
//				LabelAPI systemLabel = info.createLabel(systemName, Misc.getBasePlayerColor(), systemW);
//				systemLabel.setHighlightColors(Misc.getBasePlayerColor());
//				systemLabel.setHighlight(colStr);
				
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
		
		if (esd != null && EnumSet.of(Stage.HA_1, Stage.HA_2, Stage.HA_3, Stage.HA_4).contains(esd.id)) {
			return new BaseFactorTooltip() {
				@Override
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					float opad = 10f;
					Range affects = new Range(((Stage)esd.id).name() + "_affects");
					Range impact = new Range(((Stage)esd.id).name() + "_impact");
					float stability = impact.min;
					float access = impact.max;
					
					if (esd.id == Stage.HA_1) {
						tooltip.addTitle("Low impact");
					} else if (esd.id == Stage.HA_2) {
						tooltip.addTitle("Moderate impact");
					} else if (esd.id == Stage.HA_3) {
						tooltip.addTitle("High impact");
					} else if (esd.id == Stage.HA_4) {
						tooltip.addTitle("Extreme impact");
					}
					
					tooltip.addPara("Colony stability reduced by %s, accessibility reduced by %s.", opad,
							Misc.getHighlightColor(),
							"" + (int) stability, 
							"" + (int) Math.round(access * 100f) + "%");
					
					if (affects.max >= 1f) {
						tooltip.addPara("All of your colonies are affected.", opad);
					} else {
						tooltip.addPara("Affects colonies in %s of your hardest-hit star systems, or "
								+ "in at least %s of your star systems, whichever is higher.", opad,
								Misc.getHighlightColor(),
								"" + (int) affects.min, 
								"" + (int) Math.round(affects.max * 100f) + "%");
					}
					
					esd.addProgressReq(tooltip, opad);
				}
			};
		}
		if (esd != null && esd.id == Stage.INCREASED_DEFENSES) {
			return new BaseFactorTooltip() {
				@Override
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					float opad = 10f;
					tooltip.addTitle("Increased defenses");
					tooltip.addPara("When this stage is reached, your colonies will implement additional "
							+ "defensive measures, substantially reducing the event's rate of progress.", opad);
					esd.addProgressReq(tooltip, opad);
				}
			};
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
					float opad = 10f;
					Color h = Misc.getHighlightColor();
					tooltip.addPara("Hostile activity waxes and wanes with time. When this stage is reached, "
							+ "event progress will be reset to a lower value.",
							opad);
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
	
		if (EnumSet.of(Stage.HA_1, Stage.HA_2, Stage.HA_3, Stage.HA_4, 
				Stage.INCREASED_DEFENSES, Stage.START).contains(esd.id)) {
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
		if (stageId == Stage.HA_EVENT) {
			return 1;
		}
		if (stageId == Stage.MINOR_EVENT) {
			return 1;
		}
		return super.getStageImportance(stageId);
	}



	@Override
	protected String getName() {
		return "Hostile Activity";
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
		mag *= getProgressFraction();
		return mag * 1000f;
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

	public void economyUpdated() {
		syncHostileActivityConditionsWithEventProgress();
	}
	
	
	public void updatePenalties() {
		EventStageData esd = getLastActiveStage(false);
		if (esd == null || !EnumSet.of(Stage.HA_1, Stage.HA_2, Stage.HA_3, Stage.HA_4).contains(esd.id)) {
			stabilityPenalty = 0;
			accessibilityPenalty = 0;
			return;
		}
		
		Range impact = new Range(((Stage)esd.id).name() + "_impact");
		stabilityPenalty = impact.min;
		accessibilityPenalty = impact.max; 
	}
	
	public void syncHostileActivityConditionsWithEventProgress() {
		EventStageData esd = getLastActiveStage(false);
		if (esd == null || !EnumSet.of(Stage.HA_1, Stage.HA_2, Stage.HA_3, Stage.HA_4).contains(esd.id)) {
			cleanUpHostileActivityConditions();
			return;
		}
		
		updatePenalties();
		
		Range affects = new Range(((Stage)esd.id).name() + "_affects");
		List<HAEStarSystemDangerData> sysData = computePlayerSystemDangerData();
		//int numSystems = (int) Math.max(affects.min, Math.round((float) sysData.size() * affects.max));
		int numSystems = (int) Math.max(Math.min(sysData.size(), affects.min), Math.round((float) sysData.size() * affects.max));
		
		
		for (int i = 0; i < sysData.size(); i++) {
			HAEStarSystemDangerData sys = sysData.get(i);
			for (MarketAPI market : Misc.getMarketsInLocation(sys.system, Factions.PLAYER)) {
				if (i < numSystems) {
					if (!market.hasCondition(Conditions.HOSTILE_ACTIVITY)) {
						market.addCondition(Conditions.HOSTILE_ACTIVITY, this);
					}		
				} else {
					if (market.hasCondition(Conditions.HOSTILE_ACTIVITY)) {
						market.removeCondition(Conditions.HOSTILE_ACTIVITY);
					}
				}
			}
		}
	}
	
	public void cleanUpHostileActivityConditions() {
		for (MarketAPI curr : Misc.getPlayerMarkets(false)) {
			if (curr.hasCondition(Conditions.HOSTILE_ACTIVITY)) {
				curr.removeCondition(Conditions.HOSTILE_ACTIVITY);
			}
		}
	}

	public float getAccessibilityPenalty() {
		return accessibilityPenalty;
	}

	public float getStabilityPenalty() {
		return stabilityPenalty;
	}
	
	public boolean isEconomyListenerExpired() {
		return isEnding();
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
				if (otherFleet.isStationMode()) {
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
	
		int points = computerProgressPoints(fpDestroyed);
		if (points > 0) {
			//points = 700;
			HAShipsDestroyedFactor factor = new HAShipsDestroyedFactor(-1 * points);
			//sendUpdateIfPlayerHasIntel(factor, false); // addFactor now sends update
			addFactor(factor);
		}
	}
	
	public static int computerProgressPoints(float fleetPointsDestroyed) {
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
				//int resetProgress = getDataFor(Stage.HA_1).progress + 10 + random.nextInt(100);
				//int resetProgress = getDataFor(Stage.HA_2).progress + 10 + random.nextInt(30);
				int resetProgress = getDataFor(Stage.INCREASED_DEFENSES).progress + 1 + random.nextInt(5);
				if (!fired) {
					resetProgress = getDataFor(Stage.HA_EVENT).progressToRollAt - random.nextInt(100);
				}
				setProgress(resetProgress);
			}
		} else if (stage.id == Stage.HA_EVENT &&
				(stage.rollData == null || RANDOM_EVENT_NONE.equals(stage.rollData))) {
			stage.rollData = null;
			//int resetProgress = getDataFor(Stage.HA_1).progress + 10 + random.nextInt(100);
			//int resetProgress = getDataFor(Stage.HA_2).progress + 10 + random.nextInt(30);
			int resetProgress = getDataFor(Stage.INCREASED_DEFENSES).progress + 1 + random.nextInt(5);
			//resetProgress = 199;
			setProgress(resetProgress);
		}
	}
	
	public void resetHA_EVENT() {
		EventStageData stage = getDataFor(Stage.HA_EVENT);
		int resetProgress = stage.progressToRollAt - getRandom().nextInt(100);
		resetRandomizedStage(stage);
		setProgress(resetProgress);
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
			WeightedRandomPicker<HostileActivityFactor> picker = new WeightedRandomPicker<HostileActivityFactor>(random);
			for (EventFactor factor : factors) {
				if (factor instanceof BaseHostileActivityFactor) {
					BaseHostileActivityFactor curr = (BaseHostileActivityFactor) factor;
					curr.setRandomizedStageSeed(random.nextLong()); // seed will be reused by .rollEvent()
					float f = curr.getEventFrequency(this, stage);
					float w = factor.getProgress(this) / total;
					if (w > 0) {
						w = 0.5f + 0.5f * w;
					}
					picker.add(curr, f * w);
				}
			}
			
			HostileActivityFactor pick = picker.pick();
			if (pick == null) return;
			
			pick.rollEvent(this, stage);
		}
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_COLONIES);
		return tags;
	}
}








