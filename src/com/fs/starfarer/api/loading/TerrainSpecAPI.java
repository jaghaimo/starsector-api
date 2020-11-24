package com.fs.starfarer.api.loading;

import org.json.JSONObject;

public interface TerrainSpecAPI {
	String getId();
	String getPluginClass();
	String getLoopOne();
	String getLoopTwo();
	String getLoopThree();
	String getLoopFour();
	JSONObject getCustom();
	float getMusicSuppression();
}
