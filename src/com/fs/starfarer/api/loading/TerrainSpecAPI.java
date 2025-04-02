package com.fs.starfarer.api.loading;

import java.util.Set;

import org.json.JSONObject;

import com.fs.starfarer.api.campaign.CampaignTerrainPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public interface TerrainSpecAPI extends WithSourceMod {
	String getId();
	String getPluginClass();
	String getLoopOne();
	String getLoopTwo();
	String getLoopThree();
	String getLoopFour();
	JSONObject getCustom();
	float getMusicSuppression();
	
	void addTag(String tag);
	boolean hasTag(String tag);
	Set<String> getTags();
	CampaignTerrainPlugin getNewPluginInstance(SectorEntityToken entity, Object param);
}
