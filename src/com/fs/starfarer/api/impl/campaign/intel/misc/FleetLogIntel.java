package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;

/**
 * Implements EveryFrameScript because of deriving from BaseIntelPlugin, but not actually
 * expected to be added as a script to anything unless that's necessary for the specific subclass.
 *
 * Copyright 2018 Fractal Softworks, LLC
 */
public class FleetLogIntel extends BaseIntelPlugin {

	public static float DEFAULT_DURATION = 365f;

	protected Float duration = null; 
	protected SectorEntityToken removeTrigger = null;
	protected String icon = null;
	protected String sound = null;
	
	public FleetLogIntel() {
	}
	
	public FleetLogIntel(float duration) {
		this.duration = duration;
	}

	public void setDefaultExpiration() {
		duration = DEFAULT_DURATION;
	}
	
	public void setDuration(float days) {
		duration = days;
	}

	@Override
	public boolean shouldRemoveIntel() {
		if (removeTrigger != null && !removeTrigger.isAlive()) return true;
		
		if (isImportant() || duration == null) return false;

		Long ts = getPlayerVisibleTimestamp();
		if (ts == null) return false;
		return Global.getSector().getClock().getElapsedDaysSince(ts) >= duration;
		
//		float dur = DEFAULT_DURATION;
//		if (duration != null) dur = duration;
//		return Global.getSector().getClock().getElapsedDaysSince(ts) >= dur;
	}

	
	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Override
	public String getIcon() {
		if (icon != null) return icon;
		return Global.getSettings().getSpriteName("intel", "fleet_log");
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = new LinkedHashSet<String>();
		tags.add(Tags.INTEL_FLEET_LOG);
		return tags;
	}

	public SectorEntityToken getRemoveTrigger() {
		return removeTrigger;
	}

	public void setRemoveTrigger(SectorEntityToken removeTrigger) {
		this.removeTrigger = removeTrigger;
	}
	
	public String getSound() {
		return sound;
	}

	public void setSound(String sound) {
		this.sound = sound;
	}
	
	@Override
	public String getCommMessageSound() {
		if (sound != null) return sound;
		return getSoundLogUpdate();
	}
	
}





