package com.fs.starfarer.api.impl.campaign.intel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class GenericMissionManager extends BaseEventManager {
	
	public static interface GenericMissionCreator {
		float getMissionFrequencyWeight();
		EveryFrameScript createMissionIntel();
	}
	

	public static final String KEY = "$core_genericMissionManager";
	
	public static GenericMissionManager getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (GenericMissionManager) test; 
	}
	
	protected List<GenericMissionCreator> creators = new ArrayList<GenericMissionCreator>();
	protected Set<GenericMissionCreator> failed = new LinkedHashSet<GenericMissionCreator>();
	
	protected Map<EveryFrameScript, GenericMissionCreator> missionCreators = new LinkedHashMap<EveryFrameScript, GenericMissionCreator>();
	
	public GenericMissionManager() {
		super();
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
	}
	
	protected Object readResolve() {
		super.readResolve();
		if (failed == null) {
			failed = new LinkedHashSet<GenericMissionCreator>();
		}
		return this;
	}
	
	public void addMissionCreator(GenericMissionCreator creator) {
		creators.add(creator);
	}
	
	public boolean hasMissionCreator(Class<?> clazz) {
		for (GenericMissionCreator script : creators) {
			if (clazz.isInstance(script)) return true;
		}
		return false;
	}
	
	public List<GenericMissionCreator> getCreators() {
		return creators;
	}

	@Override
	protected int getMinConcurrent() {
		return Global.getSettings().getInt("minGenericMissions");
	}
	@Override
	protected int getMaxConcurrent() {
		return Global.getSettings().getInt("maxGenericMissions");
	}
	
	@Override
	protected float getIntervalRateMult() {
		float approximateMissionPostingDurationDays = 10f;
		return (float) getCurrMax() / approximateMissionPostingDurationDays;
	}

	@Override
	protected EveryFrameScript createEvent() {
		//if ((float) Math.random() < 0.75f) return null;
		
//		if (getActive().size() >= 3) {
//			return null;
//		}
		
		
		List<EveryFrameScript> orphaned = new ArrayList<EveryFrameScript>(missionCreators.keySet());
		CountingMap<GenericMissionCreator> current = new CountingMap<GenericMissionCreator>();
		for (EveryFrameScript s : getActive()) {
			orphaned.remove(s);
			GenericMissionCreator c = missionCreators.get(s);
			if (c != null) { 
				current.add(c);
			}
		}
		
		for (EveryFrameScript s : orphaned) {
			missionCreators.remove(s);
		}
		
		
		
		float totalWeight = 0f;
		for (GenericMissionCreator c : creators) {
			totalWeight += c.getMissionFrequencyWeight();
		}
		
		WeightedRandomPicker<GenericMissionCreator> picker = new WeightedRandomPicker<GenericMissionCreator>();
		for (GenericMissionCreator c : creators) {
			if (failed.contains(c)) continue;
			
			float currNum = current.getCount(c);
			if (currNum < 1) currNum = 1;
			
			float desiredNum = getCurrMax() * c.getMissionFrequencyWeight() / totalWeight;
			if (desiredNum < 0.1f) desiredNum = 0.1f;
		
			float deviation = desiredNum * 0.25f;
			float exponent = (desiredNum - currNum) / deviation;
			if (exponent > 4) exponent = 4;
			
			float probMult = (float) Math.pow(10f, exponent);
			picker.add(c, c.getMissionFrequencyWeight() * probMult);
		}
		
		GenericMissionCreator creator = picker.pick();
		if (creator == null) {
			failed.clear();
			return null;
		}
		
		EveryFrameScript intel = creator.createMissionIntel();
		if (intel instanceof BaseIntelPlugin && ((BaseIntelPlugin)intel).isDone()) {
			intel = null;
		}

		if (intel != null) {
// 			debug stuff: make mission intel visible to player and print some stats when adding a new mission
//			((BaseIntelPlugin) intel).setPostingLocation(null);
//			String detail = "";
//			for (GenericMissionCreator c : creators) {
//				detail += c.getClass().getSimpleName() + ": " + current.getCount(c) + " | ";
//			}
//			detail = "[" + detail + "]";
//			System.out.println("Added mission by " + creator.getClass().getSimpleName() + ", weight: " + (int)picker.getWeight(creator) + " / " + (int)picker.getTotal() + " " + detail);
			missionCreators.put(intel, creator);
			failed.clear();
		} else {
			failed.add(creator);
		}
		
		return intel;
	}
	
}







