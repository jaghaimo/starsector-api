package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionBarEventWrapper;
import com.fs.starfarer.api.loading.BarEventSpec;

public class SpecBarEventCreator extends BaseBarEventCreator {
	
	protected transient BarEventSpec spec = null;
	protected String specId = null;
	protected boolean wasAutoAdded = false;
	
	public SpecBarEventCreator(String specId) {
		this.specId = specId;
		readResolve();
	}
	
	protected Object readResolve() {
		spec = Global.getSettings().getBarEventSpec(specId);
		return this;
	}
	
	
	public boolean wasAutoAdded() {
		return wasAutoAdded;
	}

	public void setWasAutoAdded(boolean wasAutoAdded) {
		this.wasAutoAdded = wasAutoAdded;
	}

	public BarEventSpec getSpec() {
		return spec;
	}

	public String getBarEventId() {
		return specId;
	}
	
	public PortsideBarEvent createBarEvent() {
		if (spec.isMission()) {
			return new HubMissionBarEventWrapper(specId);
		}
		return spec.createEvent();
	}

	public float getBarEventActiveDuration() {
		return spec.getMinDur() + (spec.getMaxDur() - spec.getMinDur()) * (float) Math.random();
	}

	public float getBarEventFrequencyWeight() {
		return spec.getFreq();
	}

	public float getBarEventTimeoutDuration() {
		return spec.getMinTimeout() + (spec.getMaxTimeout() - spec.getMinTimeout()) * (float) Math.random();
	}

	public float getBarEventAcceptedTimeoutDuration() {
		return spec.getMinAcceptedTimeout() + (spec.getMaxAcceptedTimeout() - spec.getMinAcceptedTimeout()) * (float) Math.random();
	}

	public boolean isPriority() {
		return spec.hasTag(Tags.MISSION_PRIORITY);
	}

}
