package com.fs.starfarer.api.impl.campaign.enc;

import java.awt.Color;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.AbyssalLightEntityPlugin.AbyssalLightParams;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceAbyssPluginImpl.AbyssalEPData;

public class AbyssalLightDwellerEPEC extends AbyssalLightEPEC {
	
	public float getFrequencyForPoint(EncounterManager manager, EncounterPoint point) {
		return AbyssalFrequencies.getAbyssalLightDwellerFrequency(manager, point);
	}
	
	protected LightSpawnType pickSpawnType(EncounterManager manager, EncounterPoint point) {
		return LightSpawnType.NORMAL;
	}
	
	protected void modifySpawnedLight(EncounterManager manager, EncounterPoint point, 
									  AbyssalLightParams params, SectorEntityToken light) {
		params.color = new Color(225,200,255,255);
		light.addTag(Tags.DWELLER_LIGHT);
		
		AbyssalEPData data = (AbyssalEPData) point.custom;
		long seed = data.random.nextLong();
		light.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, seed);
	}
	
}













