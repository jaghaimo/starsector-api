package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidDelegate;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;

/**
 * A base event factor with added code for managing multiple hostile activity causes.
 * 
 * @author Alex
 *
 * Copyright 2022 Fractal Softworks, LLC
 */
@SuppressWarnings("unused")
public class BaseHostileActivityFactor extends BaseEventFactor implements HostileActivityFactor, RaidDelegate {

	protected HostileActivityEventIntel intel;
	protected List<HostileActivityCause2> causes = new ArrayList<HostileActivityCause2>();
	
	protected transient long seed;
	

	public BaseHostileActivityFactor(HostileActivityEventIntel intel) {
		this.intel = intel;
	}
	
	public int getProgress(BaseEventIntel intel) {
		int total = 0;
		for (HostileActivityCause2 cause : getCauses()) {
			total += cause.getProgress();
		}
		return total;
	}
	
	
	public void addExtraRows(TooltipMakerAPI info, BaseEventIntel intel) {
		for (HostileActivityCause2 cause : getCauses()) {
			if (!cause.shouldShow()) continue;
			
			String desc = cause.getDesc();
			if (desc != null) {
				info.addRowWithGlow(Alignment.LMID, cause.getDescColor(intel), "    " + desc,
								    Alignment.RMID, cause.getProgressColor(intel), cause.getProgressStr());
				TooltipCreator t = cause.getTooltip();
				if (t != null) {
					info.addTooltipToAddedRow(t, TooltipLocation.RIGHT, false);
				}
			}
			cause.addExtraRows(info, intel);
		}
	}
	
	public String getId() {
		return getClass().getSimpleName();
	}
	
	public float getSpawnFrequency(StarSystemAPI system) {
		return getEffectMagnitude(system);
	}

	public float getSpawnInHyperProbability(StarSystemAPI system) {
		return 0.25f;
	}

	
	public float getStayInHyperProbability(StarSystemAPI system) {
		return 0.25f;
	}

	public int getMaxNumFleets(StarSystemAPI system) {
		return 1000;
	}

//	public float getEffectMagnitudeAdjustedBySuppression(StarSystemAPI system) {
//		float mag = getEffectMagnitude(system);
//		//float s = intel.computeSuppressionAmount();
//		// currently, keep fleets the same size when suppressed, too
//		// otherwise, the fights just get less fun as you're trying to suppress more
//		return mag;
//	}
	
	public float getEffectMagnitude(StarSystemAPI system) {//, boolean adjustByEventProgress) {
		float mag = 0f;
		for (HostileActivityCause2 cause : causes) {
			mag += cause.getMagnitudeContribution(system);
		}
//		if (adjustByEventProgress) {
//			float f = intel.getProgressFraction();
//			float add = f * 0.5f;
//			add = Math.min(add, 1f - mag); wefwefwefewfwe
//			if (mag < f) {
//				mag = Misc.interpolate(mag, f, 0.5f);
//			}
//		}
		return mag;
	}

	public void addCause(HostileActivityCause2 cause) {
		causes.add(cause);
	}

	public List<HostileActivityCause2> getCauses() {
		return causes;
	}
	
	@SuppressWarnings("rawtypes")
	public void removeCauseOfClass(Class c) {
		Iterator<HostileActivityCause2> iter = causes.iterator();
		while (iter.hasNext()) {
			HostileActivityCause2 curr = iter.next();
			if (curr.getClass() == c) {
				iter.remove();
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public HostileActivityCause2 getCauseOfClass(Class c) {
		Iterator<HostileActivityCause2> iter = causes.iterator();
		while (iter.hasNext()) {
			HostileActivityCause2 curr = iter.next();
			if (curr.getClass() == c) {
				return curr;
			}
		}
		return null;
	}

	public CampaignFleetAPI createFleet(StarSystemAPI system, Random random) {
		return null;
	}

	public String getNameForThreatList(boolean first) {
		return getDesc(intel);
	}

	public Color getNameColorForThreatList() {
		return getDescColor(intel);
	}

	public float getEventFrequency(HostileActivityEventIntel intel, EventStageData stage) {
		return 0;
	}

	public void rollEvent(HostileActivityEventIntel intel, EventStageData stage) {
		
	}

	public void addBulletPointForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
										 ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		
	}

	public void addStageDescriptionForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info) {
		
	}

	public String getEventStageIcon(HostileActivityEventIntel intel, EventStageData stage) {
		return null;
	}

	public TooltipCreator getStageTooltipImpl(HostileActivityEventIntel intel, EventStageData stage) {
		// TODO Auto-generated method stub
		return null;
	}

	public void resetEvent(HostileActivityEventIntel intel, EventStageData stage) {
		HAERandomEventData data = (HAERandomEventData) stage.rollData;
		intel.sendUpdateIfPlayerHasIntel(data, false);
		stage.rollData = null;		
	}

	public void addBulletPointForEventReset(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
			ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		
	}

	public boolean fireEvent(HostileActivityEventIntel intel, EventStageData stage) {
		return false;
	}

	public void notifyRaidEnded(RaidIntel raid, RaidStageStatus status) {
		
	}
	
	public void setRandomizedStageSeed(long seed) {
		this.seed = seed;
	}
	public long getRandomizedStageSeed() {
		return seed;
	}
	
	public Random getRandomizedStageRandom() {
		return new Random(seed);
	}

}
