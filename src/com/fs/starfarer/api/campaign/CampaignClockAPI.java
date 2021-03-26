package com.fs.starfarer.api.campaign;

import java.util.GregorianCalendar;


/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface CampaignClockAPI {
	public int getCycle();
	public int getMonth();
	public int getDay();
	public int getHour();
	public float convertToDays(float realSeconds);
	public float convertToMonths(float realSeconds);
	public long getTimestamp();
	public float getElapsedDaysSince(long timestamp);
	
	public String getMonthString();
	public String getShortMonthString();
	public float getSecondsPerDay();
	
	/**
	 * New clock based on the timestamp.
	 * @param timestamp
	 * @return
	 */
	CampaignClockAPI createClock(long timestamp);
	String getDateString();
	float convertToSeconds(float days);
	String getShortDate();
	String getCycleString();
	GregorianCalendar getCal();
}
