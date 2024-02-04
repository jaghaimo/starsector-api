package com.fs.starfarer.api;

/**
 * WARNING
 * Do not store campaign data in data members of an implementation of this interface.
 * There is only one instance of it per application session and references to campaign
 * data will likely cause memory leaks.
 * 
 * If data storage is required, use SectorAPI.getMemory() or other such.
 * 
 */
public interface MusicPlayerPluginWithVolumeControl extends MusicPlayerPlugin {

	float getMusicSetVolumeForCampaignStateToken(Object token, Object param);



}
