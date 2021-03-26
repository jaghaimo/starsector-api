package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.loading.BarEventSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class BarEventManager implements EveryFrameScript {
	
	public static interface GenericBarEventCreator {
		PortsideBarEvent createBarEvent();
		float getBarEventFrequencyWeight();
		float getBarEventActiveDuration();
		float getBarEventTimeoutDuration();
		float getBarEventAcceptedTimeoutDuration();
		
		
		/**
		 * Priority events get created before non-priority. Should be used sparingly, for gameplay-essential
		 * events. Having too many priority events could crowd out all other events entirely. 
		 * @return
		 */
		boolean isPriority();
		//void updateSeed();
		String getBarEventId();
		boolean wasAutoAdded();
	}
	

	public static final String KEY = "$core_genericBarEventManager";
	
	public static BarEventManager getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (BarEventManager) test; 
	}
	
	protected List<GenericBarEventCreator> creators = new ArrayList<GenericBarEventCreator>();
	protected LinkedHashMap<PortsideBarEvent, GenericBarEventCreator> barEventCreators = new LinkedHashMap<PortsideBarEvent, GenericBarEventCreator>();
	
	protected IntervalUtil tracker = new IntervalUtil(0.4f, 0.6f);
	protected IntervalUtil tracker2 = new IntervalUtil(20f, 40f);
	protected TimeoutTracker<PortsideBarEvent> active = new TimeoutTracker<PortsideBarEvent>();
	protected TimeoutTracker<GenericBarEventCreator> timeout = new TimeoutTracker<GenericBarEventCreator>();
	
	protected long seed = Misc.genRandomSeed();
	
	public BarEventManager() {
		super();
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		readResolve();
	}
	
//	public long getSeed(SectorEntityToken entity) {
//		//updateSeed();
//		if (entity == null) return seed;
//		return seed + (long) entity.getId().hashCode() * 181783497276652981L;
//	}
	
	public long getSeed(SectorEntityToken entity, PersonAPI person, String extra) {
		//updateSeed();
		long mult = 1;
		if (entity != null) mult *= (long) entity.getId().hashCode();
		if (person != null) mult *= (long) person.getId().hashCode();
		if (extra != null) mult *= (long) extra.hashCode();
		
		return seed + mult * 181783497276652981L;
	}

	public void updateSeed() {
		seed = Misc.genRandomSeed();
	}


	protected Object readResolve() {
		if (timeout == null) {
			timeout = new TimeoutTracker<GenericBarEventCreator>();
		}
		if (tracker2 == null) {
			tracker2 = new IntervalUtil(20f, 40f);
		}
		if (seed == 0) {
			updateSeed();
		}
		
		updateBarEventCreatorsFromSpecs();
		
		return this;
	}
	
	
	public void updateBarEventCreatorsFromSpecs() {
		List<BarEventSpec> specs = Global.getSettings().getAllBarEventSpecs();
		
		Set<String> validEvents = new HashSet<String>();
		Set<String> alreadyHaveCreatorsFor = new HashSet<String>();
		for (BarEventSpec spec : specs) {
			validEvents.add(spec.getId());
		}
		
		for (GenericBarEventCreator curr : new ArrayList<GenericBarEventCreator>(creators)) {
			if (!curr.wasAutoAdded()) continue;
			
			if (!validEvents.contains(curr.getBarEventId())) {
				creators.remove(curr);
				timeout.remove(curr);
			} else {
				alreadyHaveCreatorsFor.add(curr.getBarEventId());
			}
		}
	
		for (BarEventSpec spec : specs) {
			if (!alreadyHaveCreatorsFor.contains(spec.getId())) {
				SpecBarEventCreator curr = new SpecBarEventCreator(spec.getId());
				curr.setWasAutoAdded(true);
				creators.add(curr);
			}
		}
	}
	
	
	public void addEventCreator(GenericBarEventCreator creator) {
		creators.add(creator);
	}
	
	public boolean hasEventCreator(Class<?> clazz) {
		for (GenericBarEventCreator script : creators) {
			if (clazz.isInstance(script)) return true;
		}
		return false;
	}
	
	public List<GenericBarEventCreator> getCreators() {
		return creators;
	}
	
	public TimeoutTracker<PortsideBarEvent> getActive() {
		return active;
	}
	
	public TimeoutTracker<GenericBarEventCreator> getTimeout() {
		return timeout;
	}
	
	public void setTimeout(Class creatorClass, float duration) {
		for (GenericBarEventCreator curr : creators) {
			if (curr.getClass().equals(creatorClass)) {
				timeout.set(curr, duration);
				break;
			}
		}
	}
	
	
	public void notifyWasInteractedWith(PortsideBarEvent event) {
		PortsideBarData.getInstance().removeEvent(event);
		GenericBarEventCreator creator = getCreatorFor(event);
		if (creator != null) {
			float dur = creator.getBarEventAcceptedTimeoutDuration();
			dur = Math.max(dur, timeout.getRemaining(creator));
			timeout.set(creator, dur);
			active.remove(event);
		}
	}
	
	public GenericBarEventCreator getCreatorFor(PortsideBarEvent event) {
		return barEventCreators.get(event);
	}

	public void advance(float amount) {
		float days = Misc.getDays(amount);
		
		//timeout.clear();
		
		
		active.advance(days);
		timeout.advance(days);
		
		if (DebugFlags.BAR_DEBUG) days *= 100f;
		
		tracker.advance(days);
		
		tracker2.advance(days);
		if (tracker2.intervalElapsed()) {
			updateSeed();
		}
		
		if (tracker.intervalElapsed()) {
			for (int i = 0; i < 5; i++) {
				List<PortsideBarEvent> orphaned = new ArrayList<PortsideBarEvent>(barEventCreators.keySet());
				for (PortsideBarEvent s : active.getItems()) {
					orphaned.remove(s);
				}
				
				for (PortsideBarEvent event : orphaned) {
					GenericBarEventCreator creator = barEventCreators.remove(event);
					if (creator != null) {
						float dur = creator.getBarEventTimeoutDuration();
						dur = Math.max(dur, timeout.getRemaining(creator));
						timeout.set(creator, dur);
					}
					PortsideBarData.getInstance().removeEvent(event);
				}
				
				float f = Global.getSettings().getFloat("maxTotalBarEventsAsFractionOfEventTypes");
				float max =  Math.round(creators.size() * f);
				
				if (DebugFlags.BAR_DEBUG) {
					max = 10000f;
				}
				
				if (max < 1) max = 1;
				
				if (active.getItems().size() >= max) return;
				if (barEventCreators.size() >= creators.size()) return;
				
				Set<String> activeCreators = new LinkedHashSet<String>();
				for (GenericBarEventCreator curr : barEventCreators.values()) {
					activeCreators.add(curr.getBarEventId());
				}
				
				WeightedRandomPicker<GenericBarEventCreator> priority = new WeightedRandomPicker<GenericBarEventCreator>();
				WeightedRandomPicker<GenericBarEventCreator> picker = new WeightedRandomPicker<GenericBarEventCreator>();
				for (GenericBarEventCreator curr : creators) {
					//if (barEventCreators.containsValue(curr)) continue;
					if (activeCreators.contains(curr.getBarEventId())) continue;
					if (timeout.contains(curr)) continue;
					
					if (curr.isPriority()) {
						priority.add(curr, curr.getBarEventFrequencyWeight());
					} else {
						picker.add(curr, curr.getBarEventFrequencyWeight());
					}
				}
				
				GenericBarEventCreator pick = priority.pick();
				if (pick == null) pick = picker.pick();
				if (pick == null) return;
				
				PortsideBarEvent event = pick.createBarEvent();
				if (event == null) return;
				
				active.add(event, pick.getBarEventActiveDuration());
				barEventCreators.put(event, pick);
				PortsideBarData.getInstance().addEvent(event);
			}
		}
	}

	
	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
	
}







