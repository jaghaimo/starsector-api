package com.fs.starfarer.api;

import com.fs.starfarer.api.combat.CombatEngineAPI;

/**
 * WARNING
 * Do not store campaign data in data members of an implementation of this interface.
 * There is only one instance of it per application session and references to campaign
 * data will likely cause memory leaks.
 * 
 * If data storage is required, use SectorAPI.getMemory() or other such.
 * 
 */
public interface MusicPlayerPlugin {

	/**
	 * Called when free-flying in the campaign only, i.e. not in 
	 * any sort of interaction dialog.
	 * @return
	 */
	Object getStateTokenForCampaignLocation();
	
	
	/**
	 * Called for the following states:
	 * 
	 * MusicPlayerPluginImpl.MARKET
	 * MusicPlayerPluginImpl.ENCOUNTER
	 * MusicPlayerPluginImpl.PLANET_SURVEY
	 * MusicPlayerPluginImpl.CAMPAIGN_SYSTEM
	 * MusicPlayerPluginImpl.CAMPAIGN_HYPERSPACE
	 * 
	 * ... and any other states returned by getStateTokenForCampaignLocation()
	 * 
	 * 
	 * @param token
	 * @return
	 */
	String getMusicSetIdForCampaignStateToken(Object token, Object param);


	String getMusicSetIdForCombat(CombatEngineAPI engine);
	String getMusicSetIdForTitle();

}
