package com.fs.starfarer.api.impl.campaign.missions.hub;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.PersonMissionSpec;

public class BaseHubMissionCreator implements HubMissionCreator {

	protected int numCompleted = 0;
	protected int numFailed = 0;
	protected long seed;
	protected transient Random genRandom = null;
	
	//protected Float requiredRep = null;
	
	protected transient PersonMissionSpec spec = null;
	protected String specId = null;
	
	protected boolean wasAutoAdded = false;
	protected boolean isActive = true;
	
	public BaseHubMissionCreator(PersonMissionSpec spec) {
		this.spec = spec;
		if (spec != null) {
			specId = spec.getMissionId();
		}
	}
	
	protected Object readResolve() {
		spec = Global.getSettings().getMissionSpec(specId);
		return this;
	}
	
	public PersonMissionSpec getSpec() {
		return spec;
	}

	public String getSpecId() {
		return specId;
	}

	public HubMission createHubMission(MissionHub hub) {
		return spec.createMission();
	}
	
//	public void updateSeed() {
//		seed = Misc.genRandomSeed();
//	}
	public void setSeed(long seed) {
		this.seed = seed;
	}
	
	public void updateRandom() {
		genRandom = new Random(seed);
	}
	
	public void incrCompleted() {
		numCompleted++;
	}

	public int getNumCompleted() {
		return numCompleted;
	}

	public void setNumCompleted(int numCompleted) {
		this.numCompleted = numCompleted;
	}
	
	public void incrFailed() {
		numFailed++;
	}

	public int getNumFailed() {
		return numFailed;
	}

	public void setNumFailed(int numFailed) {
		this.numFailed = numFailed;
	}
	
	
	public float getFrequencyWeight() {
		return spec.getFreq();
	}

	public float getWasShownTimeoutDuration() {
		return 0f;
	}
	
	public float getAcceptedTimeoutDuration() {
		return spec.getMinTimeout() + (float) Math.random() * (spec.getMaxTimeout() - spec.getMinTimeout());
	}
	
	public float getCompletedTimeoutDuration() {
		return spec.getMinTimeout() + (float) Math.random() * (spec.getMaxTimeout() - spec.getMinTimeout());
	}
	
	public float getFailedTimeoutDuration() {
		return spec.getMinTimeout() + (float) Math.random() * (spec.getMaxTimeout() - spec.getMinTimeout());
	}

//	public float getAcceptedTimeoutDuration() {
//		return 0f;
//	}
//	
//	public float getCompletedTimeoutDuration() {
//		return 30f + (float) Math.random() * 30f;
//	}
//	
//	public float getFailedTimeoutDuration() {
//		return 30f + (float) Math.random() * 30f;
//	}

	public boolean isPriority() {
		return spec.hasTag(Tags.MISSION_PRIORITY);
	}
	
	public boolean matchesRep(float rep) {
		RepLevel level = RepLevel.getLevelFor(rep);
		if (spec.getMinRep() != null) {
			if (!level.isAtWorst(spec.getMinRep())) return false; 
		}
		if (spec.getMaxRep() != null) {
			if (!level.isAtBest(spec.getMaxRep())) return false; 
		}
		return true;
	}
	
//	public float getRequiredRep() {
//		//return 0f;
//		if (requiredRep != null) return requiredRep;
//		return -RepLevel.INHOSPITABLE.getMax() + 0.01f;
//	}

	public Random getGenRandom() {
		return genRandom;
	}

	public boolean wasAutoAdded() {
		return wasAutoAdded;
	}

	public void setWasAutoAdded(boolean wasAutoAdded) {
		this.wasAutoAdded = wasAutoAdded;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

}
