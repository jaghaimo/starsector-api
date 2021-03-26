package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.LocationAPI;

public interface CurrentLocationChangedListener {

	void reportCurrentLocationChanged(LocationAPI prev, LocationAPI curr);
}
