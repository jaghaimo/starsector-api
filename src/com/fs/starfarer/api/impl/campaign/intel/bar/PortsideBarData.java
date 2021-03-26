package com.fs.starfarer.api.impl.campaign.intel.bar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;

public class PortsideBarData implements EveryFrameScript {
	
	public static final String KEY = "$core_PortsideBarData";
	
//	public static final float CHECK_DAYS = 10f;
//	public static final float CHECK_PROB = 0.5f;
	
	
	public static PortsideBarData getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (PortsideBarData) test; 
	}
	
	protected List<PortsideBarEvent> active = new ArrayList<PortsideBarEvent>();
	
	public PortsideBarData() {
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
	}

	
	public void addEvent(PortsideBarEvent event) {
		active.add(event);
	}
	
	public void removeEvent(PortsideBarEvent event) {
		active.remove(event);
		// may or may not be there, but try to remove in any case
		BarEventManager.getInstance().getActive().remove(event);
	}
	
	public List<PortsideBarEvent> getEvents() {
//		boolean exists = false;
//		for (PortsideBarEvent curr : active) {
//			if (curr instanceof HubMissionBarEventWrapper) {
//				exists = true;
//				break;
//			}
//		}
//		if (!exists) {
//			active.add(new HubMissionBarEventWrapper("cheapCom"));
//		}
		return active;
	}

	public void advance(float amount) {
		
		Iterator<PortsideBarEvent> iter = active.iterator();
		while (iter.hasNext()) {
			PortsideBarEvent curr = iter.next();
			if (curr.shouldRemoveEvent()) {
				iter.remove();
			} else {
				curr.advance(amount);
				if (curr.shouldRemoveEvent()) {
					iter.remove();
				}
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


