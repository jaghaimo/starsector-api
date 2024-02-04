package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.Random;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBGoInDirection;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class AbyssalDrifterGhost extends BaseSensorGhost {

	public AbyssalDrifterGhost(SensorGhostManager manager, CampaignFleetAPI fleet) {
		super(manager, 0);
		
		Random random = manager.getRandom();
		float r = random.nextFloat();
		if (r < 0.25f) {
			initEntity(genSmallSensorProfile(), genSmallRadius());
			if (random.nextFloat() < 0.5f) {
				setNumSensorIndicators(2, 3, random);
			}
		} else if (r < 0.6f) {
			initEntity(genMediumSensorProfile(), genMediumRadius());
			if (random.nextFloat() < 0.5f) {
				setNumSensorIndicators(2, 3, random);
			}
		} else {
			initEntity(genLargeSensorProfile(), genLargeRadius());
			if (random.nextFloat() < 0.5f) {
				setNumSensorIndicators(5, 7, random);
			}
		}
		
		
		if (!placeNearPlayer(800, 1200)) {
			setCreationFailed();
			return;
		}
		
		setDespawnRange(0f);
		
		WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<Integer>(random);
		picker.add(0, 15f);
		picker.add(1, 50f);
		picker.add(2, 25f);
		picker.add(3, 10f);
		
		int burn = picker.pick();
		float dur = 15f + random.nextFloat() * 20f;
		float dir = random.nextFloat() * 360f;
		
		addBehavior(new GBGoInDirection(dur, dir, burn));
	}
	
}



