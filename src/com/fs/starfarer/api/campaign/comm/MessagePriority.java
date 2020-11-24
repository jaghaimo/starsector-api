/**
 * 
 */
package com.fs.starfarer.api.campaign.comm;


public enum MessagePriority {
	DELIVER_IMMEDIATELY(1000, 10000),
	ENSURE_DELIVERY(1000, 10000),
	SECTOR(1000, 28),
	CLUSTER(10, 14),
	SYSTEM(0, 7),
	;
	
	private float maxRangeLightYears;
	private float broadcastDurationDays;
	
	private MessagePriority(float maxRangeLightYears, float broadcastDurationDays) {
		this.maxRangeLightYears = maxRangeLightYears;
		this.broadcastDurationDays = broadcastDurationDays;
	}
	
	public float getMaxRangeLightYears() {
		return maxRangeLightYears;
	}
	public float getBroadcastDurationDays() {
		return broadcastDurationDays;
	}
	
}