package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
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
	}
	

	public static final String KEY = "$core_genericBarEventManager";
	
	public static BarEventManager getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (BarEventManager) test; 
	}
	
	protected List<GenericBarEventCreator> creators = new ArrayList<GenericBarEventCreator>();
	protected LinkedHashMap<PortsideBarEvent, GenericBarEventCreator> barEventCreators = new LinkedHashMap<PortsideBarEvent, GenericBarEventCreator>();
	
	protected IntervalUtil tracker = new IntervalUtil(0.4f, 0.6f);
	protected TimeoutTracker<PortsideBarEvent> active = new TimeoutTracker<PortsideBarEvent>();
	protected TimeoutTracker<GenericBarEventCreator> timeout = new TimeoutTracker<GenericBarEventCreator>();
	
	public BarEventManager() {
		super();
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
	}
	
	protected Object readResolve() {
		if (timeout == null) {
			timeout = new TimeoutTracker<GenericBarEventCreator>();
		}
		return this;
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
		
		
		active.advance(days);
		timeout.advance(days);
		
		if (DebugFlags.BAR_DEBUG) days *= 100f;
		tracker.advance(days);
		
		if (tracker.intervalElapsed()) {
			
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
			
			WeightedRandomPicker<GenericBarEventCreator> priority = new WeightedRandomPicker<GenericBarEventCreator>();
			WeightedRandomPicker<GenericBarEventCreator> picker = new WeightedRandomPicker<GenericBarEventCreator>();
			for (GenericBarEventCreator curr : creators) {
				if (barEventCreators.containsValue(curr)) continue;
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

	
	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
	
}







