package com.fs.starfarer.api.impl.campaign.enc;

import java.util.List;

import com.fs.starfarer.api.campaign.LocationAPI;

public interface EncounterPointProvider {
	public List<EncounterPoint> generateEncounterPoints(LocationAPI where);
}
