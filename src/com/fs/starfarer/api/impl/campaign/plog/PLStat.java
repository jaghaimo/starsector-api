package com.fs.starfarer.api.impl.campaign.plog;

import java.awt.Color;

public interface PLStat {
	
	public static long CREDITS_MAX = 1000000; 
	public static long CARGO_MAX = 5000; 
	public static long FLEET_MAX = 300; 
	public static long COLONY_MAX = 30; 
	
	
	String getId();
	Color getGraphColor();
	String getGraphLabel();
	
	long getValueForAllAccrued();
	void accrueValue();
	
	long getGraphMax();
	
	String getHoverText(long value);
	
	String getSharedCategory();
}
