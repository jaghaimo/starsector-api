package com.fs.starfarer.api.impl.campaign.enc;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.AbyssalLightEntityPlugin;
import com.fs.starfarer.api.impl.campaign.AbyssalLightEntityPlugin.AbyssalLightParams;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceAbyssPluginImpl.AbyssalEPData;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class AbyssalLightEPEC extends BaseEPEncounterCreator {
	
	public static enum LightSpawnType {
		NORMAL,
		PAIR,
		CLUSTER,
		LARGE,
	}
	
	public float getFrequencyForPoint(EncounterManager manager, EncounterPoint point) {
		return AbyssalFrequencies.getAbyssalLightFrequency(manager, point);
	}
	
	protected LightSpawnType pickSpawnType(EncounterManager manager, EncounterPoint point) {
		AbyssalEPData data = (AbyssalEPData) point.custom;
		
		WeightedRandomPicker<LightSpawnType> picker = new WeightedRandomPicker<LightSpawnType>(data.random);
		picker.add(LightSpawnType.NORMAL, 100f);
		picker.add(LightSpawnType.PAIR, 7f);
		picker.add(LightSpawnType.CLUSTER, 3f);
		picker.add(LightSpawnType.LARGE, 1f);
		
		LightSpawnType type = picker.pick();
		return type;
	}
	
	protected void modifyLightParams(EncounterManager manager, EncounterPoint point, LightSpawnType type, AbyssalLightParams params) {
	}
	protected void modifySpawnedLight(EncounterManager manager, EncounterPoint point, AbyssalLightParams params, SectorEntityToken light) {
	}

	@Override
	public void createEncounter(EncounterManager manager, EncounterPoint point) {
		
		AbyssalEPData data = (AbyssalEPData) point.custom;
		LightSpawnType type = pickSpawnType(manager, point);
		
		float minSize = AbyssalLightEntityPlugin.MIN_SIZE;
		float maxSize = AbyssalLightEntityPlugin.MAX_SIZE;
		if (type == LightSpawnType.NORMAL) {
			AbyssalLightParams params = new AbyssalLightParams();
			modifyLightParams(manager, point, type, params);
			SectorEntityToken e = Global.getSector().getHyperspace().addCustomEntity(
									null, null, Entities.ABYSSAL_LIGHT, Factions.NEUTRAL, params);
			e.setLocation(point.loc.x, point.loc.y);
			modifySpawnedLight(manager, point, params, e);
		} else if (type == LightSpawnType.LARGE) {
			AbyssalLightParams params = new AbyssalLightParams(maxSize + 800f, maxSize + 1200f);
			params.durationDays = 1000f + data.random.nextFloat() * 500f;
			params.frequencyChangeMult = 0.25f;
			modifyLightParams(manager, point, type, params);
			
			SectorEntityToken e = Global.getSector().getHyperspace().addCustomEntity(
									null, null, Entities.ABYSSAL_LIGHT, Factions.NEUTRAL, params);
			e.setLocation(point.loc.x, point.loc.y);
			modifySpawnedLight(manager, point, params, e);
		} else if (type == LightSpawnType.PAIR) {
			AbyssalLightParams larger = new AbyssalLightParams(maxSize - 300f, maxSize + 300f);
			larger.durationDays = 90f + data.random.nextFloat() * 30f;
			larger.frequencyChangeMult = 0.75f;
			modifyLightParams(manager, point, type, larger);
			
			SectorEntityToken e = Global.getSector().getHyperspace().addCustomEntity(
					null, null, Entities.ABYSSAL_LIGHT, Factions.NEUTRAL, larger);
			e.setLocation(point.loc.x, point.loc.y);
			modifySpawnedLight(manager, point, larger, e);
			
			AbyssalLightParams smaller = new AbyssalLightParams(minSize * 0.2f, minSize * 0.5f);
			smaller.durationDays = larger.durationDays;
			smaller.frequencyChangeMult = larger.frequencyChangeMult;
			modifyLightParams(manager, point, type, smaller);
			
			Vector2f loc = Misc.getPointAtRadius(point.loc, 100f + data.random.nextFloat() * 300f);
			e = Global.getSector().getHyperspace().addCustomEntity(
					null, null, Entities.ABYSSAL_LIGHT, Factions.NEUTRAL, smaller);
			e.setLocation(loc.x, loc.y);
			modifySpawnedLight(manager, point, smaller, e);
		} else if (type == LightSpawnType.CLUSTER) {
			int num = 3 + data.random.nextInt(7);
			float spread = 50f + num * 10f;
			spread *= 0.5f;
			for (int i = 0; i < num; i++) {
				AbyssalLightParams params = new AbyssalLightParams(minSize * 0.1f, minSize * 0.2f);
				params.durationDays += data.random.nextFloat() * 50f;
				params.frequencyChangeMult = 2f + data.random.nextFloat() * 2f;;
				params.frequencyMultMin *= params.frequencyChangeMult;
				params.frequencyMultMax *= params.frequencyChangeMult;
				modifyLightParams(manager, point, type, params);
				
				//Vector2f loc = Misc.getPointWithinRadiusUniform(point.loc, spread, data.random);
				Vector2f loc = Misc.getPointWithinRadius(point.loc, spread);
				SectorEntityToken e = Global.getSector().getHyperspace().addCustomEntity(
										null, null, Entities.ABYSSAL_LIGHT, Factions.NEUTRAL, params);
				e.setLocation(loc.x, loc.y);
				modifySpawnedLight(manager, point, params, e);
			}
		}
		
	}
	
}













