package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.EventProgressBarAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * For capital-E "Events" with a progress bar, Outcomes, contributing factors, and so on.
 * 
 * Large UI; takes up the map area of the intel screen.
 * 
 * @author Alex
 *
 * Copyright 2022 Fractal Softworks, LLC
 */
public class BaseEventIntel extends BaseIntelPlugin implements EconomyTickListener {

	/**
	 * Assigned to EventStageData.rollData when no random event was rolled.
	 */
	public static final String RANDOM_EVENT_NONE = "random_event_none";
	
	public static enum StageIconSize {
		SMALL,
		MEDIUM,
		LARGE,
	}
	public static enum RandomizedStageType {
		GOOD,
		BAD,
		NEUTRAL,
	}
	
	/**
	 * Just a data holder for display purposes in the BaseEventIntel UI.
	 */
	public static class EventStageDisplayData {
		public float size;
		public float downLineLength;
		public String icon;
		public Color color;
		public Color iconColor = Color.white;
		public String label;
		public Color labelColor;
		public int importance = 0;
	}

	public static class EventStageData {
		public Object id;
		public int progress;
		public boolean isOneOffEvent;
		public boolean wasEverReached = false;
		public boolean isRepeatable = true;
		public boolean sendIntelUpdateOnReaching = true;
		public boolean hideIconWhenPastStageUnlessLastActive = false;
		public boolean keepIconBrightWhenLaterStageReached = false;
		public StageIconSize iconSize = StageIconSize.MEDIUM;
		
		public boolean randomized = false;
		public RandomizedStageType randomType = RandomizedStageType.NEUTRAL;
		public int progressToResetAt;
		public int progressToRollAt;
		public Object rollData;
		
		public EventStageData(Object id, int progress, boolean isOneOffEvent) {
			this(id, progress, isOneOffEvent, StageIconSize.MEDIUM);
		}
		public EventStageData(Object id, int progress, boolean isOneOffEvent, StageIconSize iconSize) {
			this.id = id;
			this.progress = progress;
			this.isOneOffEvent = isOneOffEvent;
			this.iconSize = iconSize;
		}
		
		public void addProgressReq(TooltipMakerAPI tooltip, float pad) {
			tooltip.addPara("Requires %s points of event progress.", 
					pad, Misc.getHighlightColor(), "" + progress);
		}
		public void addResetReq(TooltipMakerAPI tooltip, float pad) {
			addResetReq(tooltip, false, pad);
		}
		public void beginResetReqList(TooltipMakerAPI tooltip, boolean withResetInfo, float initPad) {
			float opad = 10f;
			float pad = 3f;
			tooltip.addPara("This outcome will be averted if:", initPad);
			tooltip.setBulletedListMode(BaseIntelPlugin.BULLET);
			if (withResetInfo) {
				tooltip.addPara("Event progress drops to %s points or below",
						pad, Misc.getHighlightColor(), "" + progressToResetAt);
			}
			
		}
		public void endResetReqList(TooltipMakerAPI tooltip, boolean withTriggeredResetInfo) {
			tooltip.setBulletedListMode(null);
			if (withTriggeredResetInfo) {
				float opad = 10f;
				tooltip.addPara("If this outcome is triggered, event progress will be reset to a much lower value afterwards.",
						opad, Misc.getHighlightColor(), "" + progressToResetAt);
			}
		}
		
		public void addResetReq(TooltipMakerAPI tooltip, boolean withResetIfTriggered, float pad) {
			if (withResetIfTriggered) {
				tooltip.addPara("This outcome will be averted if event progress drops to %s points or below. "
						+ "If this outcome is triggered, event progress will be reset to a much lower value afterwards.",
						pad, Misc.getHighlightColor(), "" + progressToResetAt);
			} else {
				tooltip.addPara("This outcome will be averted if event progress drops to %s points or below.", 
						pad, Misc.getHighlightColor(), "" + progressToResetAt);
			}
		}
	}

	protected int progress = 0;
	protected int maxProgress = 1000;
	
	//protected Object startingStage;
	protected List<EventStageData> stages = new ArrayList<EventStageData>();
	protected IntelSortTier sortTier;
	
	protected List<EventFactor> factors = new ArrayList<EventFactor>();
	protected Random random = new Random();
	
	protected float progressDeltaRemainder = 0f;
	protected transient float uiWidth;
	
	public BaseEventIntel() {
		setSortTier(IntelSortTier.TIER_2);
		
		Global.getSector().addScript(this);
		// this needs to be done in sub-classes since it sends out an intel update
		// and that won't have the right data because the event isn't finished
		// being constructed here - it needs stages etc added to it
		//Global.getSector().getIntelManager().addIntel(this);
		Global.getSector().getListenerManager().addListener(this);
	}

	@Override
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);
		
		List<EventFactor> remove = new ArrayList<EventFactor>();
		for (EventFactor curr : factors) {
			if (curr.isExpired()) remove.add(curr);
		}
		factors.removeAll(remove);
	}



	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		boolean large = true;
		if (large) info.setParaSmallInsignia();
		info.addPara(getName(), c, 0f);
		//info.addPara("Hostile Activity", c, 0f);
		if (large) info.setParaFontDefault();
		addBulletPoints(info, mode);
	}
	
	protected boolean addEventFactorBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, 
			   Color tc, float initPad) {
		if (isUpdate && getListInfoParam() instanceof EventFactor) {
			EventFactor factor = (EventFactor) getListInfoParam();
			if (factor.isOneTime()) {
				factor.addBulletPointForOneTimeFactor(this, info, tc, initPad);
			}
			return true;
		}
		return false;
	}


	@Override
	public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
		
		float opad = 10f;
		uiWidth = width;

		// TODO DEBUG
		//setProgress(900);
		//setProgress((int) (0 + (float) Math.random() * 400));
		//setProgress(900);
		//setProgress(499);
		//setProgress(200);
		
		TooltipMakerAPI main = panel.createUIElement(width, height, true);

		main.setTitleOrbitronVeryLarge();
		main.addTitle(getName(), Misc.getBasePlayerColor());
		
		EventProgressBarAPI bar = main.addEventProgressBar(this, 100f);
		TooltipCreator barTC = getBarTooltip();
		if (barTC != null) {
			main.addTooltipToPrevious(barTC, TooltipLocation.BELOW, false);
		}
		
		for (EventStageData curr : stages) {
			if (curr.progress <= 0) continue; // no icon for "starting" stage
			if (curr.rollData == RANDOM_EVENT_NONE) continue;
			if (curr.wasEverReached && curr.isOneOffEvent && !curr.isRepeatable) continue;
			
			if (curr.hideIconWhenPastStageUnlessLastActive && 
					curr.progress <= progress &&
					getLastActiveStage(true) != curr) {
				continue;
			}
			
			EventStageDisplayData data = createDisplayData(curr.id);
			UIComponentAPI marker = main.addEventStageMarker(data);
			float xOff = bar.getXCoordinateForProgress(curr.progress) - bar.getPosition().getX();
			marker.getPosition().aboveLeft(bar, data.downLineLength).setXAlignOffset(xOff - data.size / 2f - 1);
			
			TooltipCreator tc = getStageTooltip(curr.id);
			if (tc != null) {
				main.addTooltipTo(tc, marker, TooltipLocation.LEFT, false); 
			}
		}
		
		// progress indicator
		{
			UIComponentAPI marker = main.addEventProgressMarker(this);
			float xOff = bar.getXCoordinateForProgress(progress) - bar.getPosition().getX();
			marker.getPosition().belowLeft(bar, -getBarProgressIndicatorHeight() * 0.5f - 2)
						.setXAlignOffset(xOff - getBarProgressIndicatorWidth() / 2 - 1);
		}

		main.addSpacer(opad);
		main.addSpacer(opad);
		for (EventStageData curr : stages) {
			if (curr.wasEverReached && curr.isOneOffEvent && !curr.isRepeatable) continue;
			addStageDescriptionWithImage(main, curr.id);
		}
		

		
		float barW = getBarWidth();
		float factorWidth = (barW - opad) / 2f;
		
		if (withMonthlyFactors() != withOneTimeFactors()) {
			//factorWidth = barW;
			factorWidth = (int) (barW * 0.6f);
		}
		
		TooltipMakerAPI mFac = main.beginSubTooltip(factorWidth);
		
		Color c = getFactionForUIColors().getBaseUIColor();
		Color bg = getFactionForUIColors().getDarkUIColor();
		mFac.addSectionHeading("Monthly factors", c, bg, Alignment.MID, opad).getPosition().setXAlignOffset(0);
		
		float strW = 40f;
		float rh = 20f;
		//rh = 15f;
		mFac.beginTable2(getFactionForUIColors(), rh, false, false, 
				"Monthly factors", factorWidth - strW - 3,
				"Progress", strW
				);
		
		for (EventFactor factor : factors) {
			if (factor.isOneTime()) continue;
			if (!factor.shouldShow(this)) continue;
			
			String desc = factor.getDesc(this);
			if (desc != null) {
				mFac.addRowWithGlow(Alignment.LMID, factor.getDescColor(this), desc,
								    Alignment.RMID, factor.getProgressColor(this), factor.getProgressStr(this));
				TooltipCreator t = factor.getMainRowTooltip();
				if (t != null) {
					mFac.addTooltipToAddedRow(t, TooltipLocation.RIGHT, false);
				}
			}
			factor.addExtraRows(mFac, this);
		}
		
		//mFac.addButton("TEST", new String(), factorWidth, 20f, opad);
		mFac.addTable("None", -1, opad);
		mFac.getPrev().getPosition().setXAlignOffset(-5);
		
		main.endSubTooltip();
		
		TooltipMakerAPI oFac = main.beginSubTooltip(factorWidth);
		
		oFac.addSectionHeading("Recent one-time factors", c, bg, Alignment.MID, opad).getPosition().setXAlignOffset(0);
		
		oFac.beginTable2(getFactionForUIColors(), 20f, false, false,
				"One-time factors", factorWidth - strW - 3,
				"Progress", strW
				);
		
		for (EventFactor factor : factors) {
			if (!factor.isOneTime()) continue;
			if (!factor.shouldShow(this)) continue;
			
			String desc = factor.getDesc(this);
			if (desc != null) {
				oFac.addRowWithGlow(Alignment.LMID, factor.getDescColor(this), desc,
								    Alignment.RMID, factor.getProgressColor(this), factor.getProgressStr(this));
				TooltipCreator t = factor.getMainRowTooltip();
				if (t != null) {
					oFac.addTooltipToAddedRow(t, TooltipLocation.LEFT);
				}
			}
			factor.addExtraRows(oFac, this);
		}
		
		oFac.addTable("None", -1, opad);
		oFac.getPrev().getPosition().setXAlignOffset(-5);
		main.endSubTooltip();
		
		
		float factorHeight = Math.max(mFac.getHeightSoFar(), oFac.getHeightSoFar());
		mFac.setHeightSoFar(factorHeight);
		oFac.setHeightSoFar(factorHeight);
		
		
		if (withMonthlyFactors() && withOneTimeFactors()) {
			main.addCustom(mFac, opad * 2f);
			main.addCustomDoNotSetPosition(oFac).getPosition().rightOfTop(mFac, opad);
		} else if (withMonthlyFactors()) {
			main.addCustom(mFac, opad * 2f);
		} else if (withOneTimeFactors()) {
			main.addCustom(oFac, opad * 2f);
		}
		
		//main.addButton("TEST", new String(), factorWidth, 20f, opad);
		
		panel.addUIElement(main).inTL(0, 0);
	}
	
	public TooltipCreator getBarTooltip() {
		return new TooltipCreator() {
			public boolean isTooltipExpandable(Object tooltipParam) {
				return false;
			}
			public float getTooltipWidth(Object tooltipParam) {
				return 450;
			}
			
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				Color h = Misc.getHighlightColor();
				
				tooltip.addPara("Event progress: %s out of %s points.", 0f, h, "" + progress, "" + maxProgress);
				int p = getMonthlyProgress();
				String pStr = "" + p;
				if (p > 0) pStr = "+" + p;
				tooltip.addPara("Projected monthly progress: %s points.", opad, getProgressColor(p), pStr);
				
				tooltip.addPara("Event progress is influenced by various factors. Some of these apply over time, "
						+ "and some only apply once. As the event progresses, "
						+ "different stages and outcomes may unfold.", opad);
			}
		};
	}
	public TooltipCreator getStageTooltip(Object stageId) {
		final EventStageData esd = getDataFor(stageId);
		if (esd == null || (esd.randomized && (esd.rollData == null || RANDOM_EVENT_NONE.equals(esd.rollData)))) {
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
					
					tooltip.addPara("Something might occur when event progress reaches %s points. "
							+ "What that is, if anything, will be determined when event progress reaches "
							+ "%s points.", 0f, 
							h, "" + esd.progress, "" + esd.progressToRollAt);
					
					if (esd.isRepeatable) {
						tooltip.addPara("This event is repeatable.", opad);
					} else {
						tooltip.addPara("This event is not repeatable.", opad);
					}
				}
			};
		}
		return getStageTooltipImpl(stageId);
	}
	public TooltipCreator getStageTooltipImpl(Object stageId) {
		return null;
	}
	
	public void addStageDescriptionWithImage(TooltipMakerAPI main, Object stageId) {
		EventStageDisplayData data = createDisplayData(stageId);
		String icon;
		if (data != null) {
			icon = data.icon;
		} else {
			icon = getIcon();
		}
		float imageSize = 64;
		float opad = 10f;
		float indent = 0;
		indent = 10f;
		float width = getBarWidth() - indent * 2f;
		
		
		TooltipMakerAPI info = main.beginImageWithText(icon, imageSize, width, true);
		//TooltipMakerAPI info = main.beginImageWithText("graphics/icons/missions/ga_intro.png", 64);
		addStageDescriptionText(info, width - imageSize - opad, stageId);
		if (info.getHeightSoFar() > 0) {
			main.addImageWithText(opad).getPosition().setXAlignOffset(indent);
			main.addSpacer(0).getPosition().setXAlignOffset(-indent);
		}
	}
	
	public void addStageDescriptionText(TooltipMakerAPI info, float width, Object stageId) {
		
	}
	
	
	public EventStageDisplayData createDisplayData(Object stageId) {
		EventStageDisplayData data = new EventStageDisplayData();
		data.size = getStageIconSize(stageId);
		data.downLineLength = getStageDownLineLength(stageId);
		data.color = getStageColor(stageId);
		data.icon = getStageIcon(stageId);
		data.iconColor = getStageIconColor(stageId);
		data.importance = getStageImportance(stageId);
		data.label = getStageLabel(stageId);
		data.labelColor = getStageLabelColor(stageId);
		return data;
	}
	
	protected String getStageIcon(Object stageId) {
		EventStageData esd = getDataFor(stageId);
		if (esd == null || (esd.randomized && (esd.rollData == null || RANDOM_EVENT_NONE.equals(esd.rollData)))) {
			if (esd.randomType == RandomizedStageType.GOOD) {
				return Global.getSettings().getSpriteName("events", "stage_unknown_good");
			} else if (esd.randomType == RandomizedStageType.BAD) {
				return Global.getSettings().getSpriteName("events", "stage_unknown_bad");
			}
			return Global.getSettings().getSpriteName("events", "stage_unknown_neutral");
		}
		return getStageIconImpl(stageId);
		
	}
	protected String getStageIconImpl(Object stageId) {
		return Global.getSettings().getSpriteName("events", "stage_unknown");
	}
	
	protected float getStageIconSize(Object stageId) {
		EventStageData esd = getDataFor(stageId);
		if (esd != null && esd.iconSize == StageIconSize.SMALL) return 32;
		if (esd != null && esd.iconSize == StageIconSize.LARGE) return 48;
		return 40;
		//return 32;
	}
	
	protected float getStageDownLineLength(Object stageId) {
		EventStageData esd = getDataFor(stageId);
		//if (esd != null && esd.iconSize == StageIconSize.SMALL) return 24;
		//if (esd != null && esd.iconSize == StageIconSize.SMALL) return 56; // level with top of LARGE
		if (esd != null && esd.iconSize == StageIconSize.SMALL) return 48; // level with middle of LARGE
		if (esd != null && esd.iconSize == StageIconSize.LARGE) return 40;
		return 32;
	}
	

	public float getBarWidth() {
		//return uiWidth - 200f;
		return 750f;
	}
	public float getBarHeight() {
		return 20f;
	}
	
	public boolean putBarProgressIndicatorLabelOnRight() {
		float test = (float)progress / (float)maxProgress * getBarWidth();
		return test < 50;
	}
	
	public float getBarProgressIndicatorHeight() {
		return 20f;
	}
	public float getBarProgressIndicatorWidth() {
		return 20f;
	}
	public Color getBarProgressIndicatorLabelColor() {
		return Misc.getHighlightColor();
	}
	public Color getBarProgressIndicatorColor() {
		return getBarColor();
	}
	public Color getBarBracketColor() {
		if (true) return Misc.getBasePlayerColor();
		return getBarColor();
	}
	
	public Color getBarColor() {
		Color color = Misc.getBasePlayerColor();
		color = Misc.interpolateColor(color, Color.black, 0.25f);
		return color;
	}
	
	protected Color getBaseStageColor(Object stageId) {
		return getBarColor();
	}
	protected Color getDarkStageColor(Object stageId) {
		Color color = getBarColor();
		color = Misc.interpolateColor(color, Color.black, 0.5f);
		return color;
	}
	protected Color getStageColor(Object stageId) {
		int req = getRequiredProgress(stageId);
		
		EventStageData last = getLastActiveStage(false);
		EventStageData esd = getDataFor(stageId);
		boolean grayItOut = false;
		if (last != null && esd != null && last != esd && !esd.isOneOffEvent &&
				!esd.keepIconBrightWhenLaterStageReached &&
				esd.progress < last.progress) {
			grayItOut = true;
		}
		
		if (esd != null && esd.randomized && esd.rollData != null) {
			return getBaseStageColor(stageId);
		}
		
		if (req > progress || grayItOut) {
			return getDarkStageColor(stageId);
		}
		return getBaseStageColor(stageId);
	}
	protected Color getStageIconColor(Object stageId) {
		int req = getRequiredProgress(stageId);
		
		EventStageData last = getLastActiveStage(false);
		EventStageData esd = getDataFor(stageId);
		boolean grayItOut = false;
		if (last != null && esd != null && last != esd && !esd.isOneOffEvent &&
				!esd.keepIconBrightWhenLaterStageReached && 
				esd.progress < last.progress) {
			grayItOut = true;
		}
		
		if (esd != null && esd.randomized && esd.rollData != null) {
			return Color.white;
		}
		
		if (req > progress || grayItOut) {
			return new Color(255,255,255,155);
		}
		return Color.white;
	}
	protected int getStageImportance(Object stageId) {
		return 0;
	}
	protected String getStageLabel(Object stageId) {
		Object least = null;
		int min = Integer.MAX_VALUE;
		for (EventStageData curr : stages) {
			int req = curr.progress;
			if (req > progress && req < min) {
				min = req;
				least = curr.id;
			}
		}
		if (stageId.equals(least)) {
			return "" + getRequiredProgress(least);
		}
		return null;
	}
	protected Color getStageLabelColor(Object stageId) {
		return Misc.getHighlightColor();
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_MAJOR_EVENT);
		return tags;
	}
	
	@Override
	public boolean hasSmallDescription() {
		return false;
	}

	@Override
	public boolean hasLargeDescription() {
		return true;
	}

	public int getMaxProgress() {
		return maxProgress;
	}

	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
	}

	public List<EventStageData> getStages() {
		return stages;
	}
	
	
	public boolean isStageOrOneOffEventReached(Object stageId) {
		return progress >= getRequiredProgress(stageId);
	}
	
	public boolean isStageActiveAndLast(Object stageId) {
		return isStageActiveAndLast(stageId, false);
	}
	public boolean isStageActiveAndLast(Object stageId, boolean includeOneOffEvents) {
		EventStageData data = getLastActiveStage(includeOneOffEvents);
		//if (data == null) return startingStage == stageId;
		if (data == null) return false;
		return data.id == stageId; // assuming stageId will be enums, so == check is ok
	}
	
//	public boolean isStageActive(Object stageId) {
//		EventStageData data = getDataFor(stageId);
//		if (data == null) return false;
//		return data.progress <= getProgress();
//	}
	
	
//	public Object getStartingStage() {
//		return startingStage;
//	}
//	public void setStartingStage(Object startingStage) {
//		this.startingStage = startingStage;
//	}
	
	public void addStage(Object id, int progress) {
		addStage(id, progress, StageIconSize.MEDIUM);
	}
	public void addStage(Object id, int progress, StageIconSize iconSize) {
		addStage(id, progress, false, iconSize);
	}
	public void addStage(Object id, int progress, boolean isOneOffEvent) {
		addStage(id, progress, isOneOffEvent, StageIconSize.MEDIUM);
	}
	public void addStage(Object id, int progress, boolean isOneOffEvent, StageIconSize iconSize) {
		stages.add(new EventStageData(id, progress, isOneOffEvent, iconSize));
	}
	
	public boolean isStageActive(Object stageId) {
		EventStageData data = getDataFor(stageId);
		if (data == null) return false;
		return data.progress <= getProgress();
		
	}
	public EventStageData getLastActiveStage(boolean includeOneOffEvents) {
		EventStageData last = null;
		int max = Integer.MIN_VALUE;
		for (EventStageData curr : stages) {
			if (!includeOneOffEvents && curr.isOneOffEvent) continue;
			
			int req = curr.progress;
			if (progress >= req && req > max) {
				max = req;
				last = curr;
			}
		}
		return last;
	}
	
	public EventStageData getDataFor(Object stageId) {
		for (EventStageData curr : stages) {
			if (stageId.equals(curr.id)) return curr;
		}
		return null;
	}
	
	public int getRequiredProgress(Object stageId) {
		//if (stageId == startingStage) return 0;
		EventStageData data = getDataFor(stageId);
		return data == null ? 0 : data.progress;
	}
	
	public void setSortTier(IntelSortTier sortTier) {
		this.sortTier = sortTier;
	}

	@Override
	public IntelSortTier getSortTier() {
		if (sortTier == null || isEnding() || isEnded()) return super.getSortTier();
		return sortTier;
	}
	
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		Global.getSector().removeScript(this);
		for (EventFactor factor : factors) {
			factor.notifyEventEnded();
		}
	}


	@Override
	protected void notifyEnding() {
		super.notifyEnding();
//		for (MarketAPI curr : getAffectedMarkets()) {
//			if (curr.hasCondition(Conditions.HOSTILE_ACTIVITY)) {
//				curr.removeCondition(Conditions.HOSTILE_ACTIVITY);
//			}
//		}
		Global.getSector().getListenerManager().removeListener(this);
		for (EventFactor factor : factors) {
			factor.notifyEventEnding();
		}
	}

	public void addFactor(EventFactor factor) {
		addFactor(factor, null);
	}
	/**
	 * Adds factor's progress to event progress if it's a one-time factor.
	 * If dialog is passed in, it'll be visible to notifyStageReached() via addingFactorDialog, 
	 * in case that needs to print an update there.
	 * @param factor
	 */
	protected transient InteractionDialogAPI addingFactorDialog = null;
	public void addFactor(EventFactor factor, InteractionDialogAPI dialog) {
		addingFactorDialog = dialog;
		factors.add(factor);
		if (factor.isOneTime()) {
			if (factor.getProgress(this) != 0) {
				TextPanelAPI textPanel = dialog == null ? null : dialog.getTextPanel();
				sendUpdateIfPlayerHasIntel(factor, textPanel);
			}
			setProgress(getProgress() + factor.getProgress(this));
		}
		addingFactorDialog = null;
	}
	public TextPanelAPI getTextPanelForStageChange() {
		if (addingFactorDialog == null) return null;
		return addingFactorDialog.getTextPanel();
	}
	
	public List<EventFactor> getFactors() {
		return factors;
	}
	
	public void removeFactor(EventFactor factor) {
		factors.remove(factor);
	}
	
	public void removeFactorOfClass(Class<EventFactor> c) {
		List<EventFactor> remove = new ArrayList<EventFactor>();
		for (EventFactor curr : factors) {
			if (c.isInstance(curr)) {
				remove.add(curr);
			}
		}
		factors.removeAll(remove);
	}
	
	public boolean isEventProgressANegativeThingForThePlayer() {
		return false;
	}


	public int getMonthlyProgress() {
		int total = 0;
		for (EventFactor factor : factors) {
			if (factor.isOneTime()) continue;
			total += factor.getProgress(this);
		}
		return total;
	}
	
	
	public void reportEconomyTick(int iterIndex) {
		float delta = getMonthlyProgress();
		
		float numIter = Global.getSettings().getFloat("economyIterPerMonth");
		float f = 1f / numIter;
		
		delta *= f;
		delta += progressDeltaRemainder;
		
		int apply = (int) delta;

		progressDeltaRemainder = delta - (float) apply;
		
		setProgress(progress + apply);
	}
	
	public int getProgress() {
		return progress;
	}

	protected transient boolean prevProgressDeltaWasPositive = false;
	public void setProgress(int progress) {
		if (this.progress == progress) return;
		
		if (progress < 0) progress = 0;
		if (progress > maxProgress) progress = maxProgress;
		
		EventStageData prev = getLastActiveStage(true);
		prevProgressDeltaWasPositive = this.progress < progress;
		
		//progress += 30;
		//progress = 40;
		//progress = 40;
		//progress = 499;
		
		this.progress = progress;
		
		
		if (progress < 0) {
			progress = 0;
		}
		if (progress > getMaxProgress()) {
			progress = getMaxProgress();
		}
		
		// Check to see if randomized events need to be rolled/reset
		for (EventStageData esd : getStages()) {
			if (esd.wasEverReached && esd.isOneOffEvent && !esd.isRepeatable) continue;
			
			if (esd.randomized) {
				if (esd.rollData != null && progress <= esd.progressToResetAt) {
					resetRandomizedStage(esd);
				}
				if (esd.rollData == null && progress >= esd.progressToRollAt) {
					rollRandomizedStage(esd);
					if (esd.rollData == null) {
						esd.rollData = RANDOM_EVENT_NONE;
					}
				}
			}
		}
		
		// go through all of the stages made active by the new progress value
		// generally this'd just be one stage, but possible to have multiple for a large
		// progress increase
		for (EventStageData curr : getStages()) {
			if (curr.progress <= prev.progress) continue;
			//if (curr.progress > progress) continue;
			
			// reached
			if (curr.progress <= progress) {
				//EventStageData curr = getLastActiveStage(true);
				if (curr != null && (curr != prev || !prev.wasEverReached)) {
					if (curr.sendIntelUpdateOnReaching && curr.progress > 0 && (prev == null || prev.progress < curr.progress)) {
						sendUpdateIfPlayerHasIntel(curr, getTextPanelForStageChange());
					}
					notifyStageReached(curr);
					curr.rollData = null;
					curr.wasEverReached = true;
					
					progress = getProgress(); // in case it was changed by notifyStageReached()
				}
			}
		}
	}

	protected void notifyStageReached(EventStageData stage) {
		
	}
	
	public void reportEconomyMonthEnd() {
		
	}
	
	public Color getProgressColor(int delta) {
		if (isEventProgressANegativeThingForThePlayer()) {
			if (delta < 0) {
				return Misc.getPositiveHighlightColor();
			}
		} else {
			if (delta < 0) {
				return Misc.getNegativeHighlightColor();
			}
		}
		return Misc.getHighlightColor();
	}
	
	public void setHideStageWhenPastIt(Object stageId) {
		EventStageData esd = getDataFor(stageId);
		if (esd == null) return;
		esd.hideIconWhenPastStageUnlessLastActive = true;
	}
	
	public void setRandomized(Object stageId, RandomizedStageType type, int resetAt, int rollAt, boolean sendUpdateWhenReached) {
		setRandomized(stageId, type, resetAt, rollAt, sendUpdateWhenReached, true);
	}
	public void setRandomized(Object stageId, RandomizedStageType type, int resetAt, int rollAt, boolean sendUpdateWhenReached, boolean repeatable) {
		EventStageData esd = getDataFor(stageId);
		if (esd == null) return;
		esd.sendIntelUpdateOnReaching = sendUpdateWhenReached;
		esd.isRepeatable = repeatable;
		esd.isOneOffEvent = true;
		esd.randomized = true;
		esd.rollData = null;
		esd.progressToResetAt = resetAt;
		esd.progressToRollAt = rollAt;
		esd.randomType = type;
	}

	public Random getRandom() {
		return random;
	}

	public void resetRandomizedStage(EventStageData stage) {
		stage.rollData = null;
	}
	
	public void rollRandomizedStage(EventStageData stage) {
		
	}

	
	public float getProgressFraction() {
		float p = (float) progress / (float) maxProgress;
		if (p < 0) p = 0;
		if (p > 1) p = 1;
		return p;
	}
	
	public boolean withMonthlyFactors() {
		return true;
	}
	public boolean withOneTimeFactors() {
		return true;
	}
}









