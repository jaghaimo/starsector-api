package com.fs.starfarer.api.impl.campaign.plog;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;

public class BasePLEntry implements PLEntry {

	// persists across saves but that doesn't matter
	// point is to maintain order of events that happen at the same point in time
	public static long offset = 0;
	
	protected String text;
	protected long timestamp;
	
	public BasePLEntry(String text) {
		this.text = text;
		timestamp = Global.getSector().getClock().getTimestamp() + offset;
		offset++;
	}

	public Color getColor() {
		return Misc.getTextColor();
	}

	public String getText() {
		return text;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

}
