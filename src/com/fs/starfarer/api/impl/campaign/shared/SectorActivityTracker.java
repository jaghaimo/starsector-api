package com.fs.starfarer.api.impl.campaign.shared;


public class SectorActivityTracker {

	//private Map<String, StarSystemActivityTracker> trackers = new LinkedHashMap<String, StarSystemActivityTracker>();
	private CommodityStatTracker commodityTracker = new CommodityStatTracker();

	Object readResolve() {
		if (commodityTracker == null) {
			commodityTracker = new CommodityStatTracker();
		}
		return this;
	}
	
	Object writeReplace() {
		return this;
	}
	
	public void advance(float days) {
//		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
//			getTracker(system);
//		}
//		for (StarSystemActivityTracker tracker : trackers.values()) {
//			tracker.advance(days);
//		}
		commodityTracker.advance(days);
	}
	
//	public StarSystemActivityTracker getTracker(String starId) {
//		StarSystemAPI system = Global.getSector().getStarSystem(starId);
//		return getTracker(system);
//	}
//	public StarSystemActivityTracker getTracker(StarSystemAPI system) {
//		StarSystemActivityTracker tracker = trackers.get(system.getId());
//		if (tracker == null) {
//			tracker = new StarSystemActivityTracker(system);
//			trackers.put(system.getId(), tracker);
//		}
//		return tracker;
//	}
	
	public CommodityStatTracker getCommodityTracker() {
		return commodityTracker;
	}
	
	
}








