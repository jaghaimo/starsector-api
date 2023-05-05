package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.SectorEntityToken;

/**
 * Called when tooltip info about how far a location is/how much fuel/time it takes to get there is created.
 * At the moment, this happens in two places:
 * 	The star icon tooltip (on the hyperspace map)
 * 	Intel icons
 * 	
 */
public interface NavigationDataSectionListener {
	public void reportNavigationDataSectionAboutToBeCreated(SectorEntityToken target); 
	public void reportNavigationDataSectionWasCreated(SectorEntityToken target); 
}
