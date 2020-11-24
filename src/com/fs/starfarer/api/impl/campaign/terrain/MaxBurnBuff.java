/**
 * 
 */
package com.fs.starfarer.api.impl.campaign.terrain;

import com.fs.starfarer.api.campaign.BuffManagerAPI.Buff;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public class MaxBurnBuff implements Buff {
	private String id;
	private float delta;
	private float dur;
	
	public MaxBurnBuff(String id, float delta, float dur) {
		this.id = id;
		this.delta = delta;
		this.dur = dur;
	}
	public void advance(float days) {
		dur -= days;
	}
	public void apply(FleetMemberAPI member) {
		member.getStats().getMaxBurnLevel().modifyFlat(getId(), delta);
	}
	public String getId() {
		return id;
	}
	public boolean isExpired() {
		return dur <= 0;
	}
	public float getDelta() {
		return delta;
	}
	public void setDelta(float delta) {
		this.delta = delta;
	}
	public float getDur() {
		return dur;
	}
	public void setDur(float dur) {
		this.dur = dur;
	}
	
	
}