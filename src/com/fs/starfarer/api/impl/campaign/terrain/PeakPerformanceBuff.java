/**
 * 
 */
package com.fs.starfarer.api.impl.campaign.terrain;

import com.fs.starfarer.api.campaign.BuffManagerAPI.Buff;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public class PeakPerformanceBuff implements Buff {
	private String id;
	private float mult;
	private float dur;
	
	public PeakPerformanceBuff(String id, float mult, float dur) {
		this.id = id;
		this.mult = mult;
		this.dur = dur;
	}
	public void advance(float days) {
		dur -= days;
	}
	public void apply(FleetMemberAPI member) {
		member.getStats().getPeakCRDuration().modifyMult(getId(), mult);
	}
	public String getId() {
		return id;
	}
	public boolean isExpired() {
		return dur <= 0;
	}
	public float getMult() {
		return mult;
	}
	public void setMult(float delta) {
		this.mult = delta;
	}
	public float getDur() {
		return dur;
	}
	public void setDur(float dur) {
		this.dur = dur;
	}
	
	
}