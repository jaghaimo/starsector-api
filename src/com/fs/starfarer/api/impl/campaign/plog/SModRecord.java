package com.fs.starfarer.api.impl.campaign.plog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

/**
 * IMPORTANT: only one S-Mod per record is supported.
 * @author Alex
 *
 * Copyright 2021 Fractal Softworks, LLC
 */
public class SModRecord {

	protected WeakReference<FleetMemberAPI> member;
	protected List<String> smods = new ArrayList<String>();
	protected int spSpent;
	protected float bonusXPFractionGained;
	protected long timestamp;
	
	public SModRecord(FleetMemberAPI member) {
		this.member = new WeakReference<FleetMemberAPI>(member);
		this.timestamp = Global.getSector().getClock().getTimestamp();
	}
	
	public FleetMemberAPI getMember() {
		return member == null ? null : member.get();
	}
	public void setMember(FleetMemberAPI member) {
		this.member = new WeakReference<FleetMemberAPI>(member);
	}
	public List<String> getSMods() {
		return smods;
	}
	public void setSmods(List<String> smods) {
		this.smods = smods;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getSPSpent() {
		return spSpent;
	}

	public void setSPSpent(int spSpent) {
		this.spSpent = spSpent;
	}

	public float getBonusXPFractionGained() {
		return bonusXPFractionGained;
	}

	public void setBonusXPFractionGained(float bonusXPFractionGained) {
		this.bonusXPFractionGained = bonusXPFractionGained;
	}
	
	
}
