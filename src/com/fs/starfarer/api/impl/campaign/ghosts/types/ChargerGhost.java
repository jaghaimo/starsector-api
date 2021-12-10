package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.abilities.EmergencyBurnAbility;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIPlaySound;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIntercept;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.loading.AbilitySpecAPI;
import com.fs.starfarer.api.util.Misc;

public class ChargerGhost extends BaseSensorGhost {

	public ChargerGhost(SensorGhostManager manager, CampaignFleetAPI fleet) {
		super(manager, 0);
		
		Random random = manager.getRandom();
		float r = random.nextFloat();
		int maxBurn;
		if (r < 0.25f) {
			initEntity(genMediumSensorProfile(), genSmallRadius());
			maxBurn = 9 + random.nextInt(3);
		} else if (r < 0.6f) {
			initEntity(genLargeSensorProfile(), genMediumRadius());
			maxBurn = 8 + random.nextInt(3);
		} else {
			initEntity(genLargeSensorProfile(), genLargeRadius());
			maxBurn = 7 + random.nextInt(3);
		}
		
		
		if (!placeNearPlayer()) {
			setCreationFailed();
			return;
		}
		setDespawnRange(0f);
		
		String soundId = null;
		AbilitySpecAPI spec = Global.getSettings().getAbilitySpec(Abilities.EMERGENCY_BURN);
		if (spec != null) {
			soundId = spec.getWorldOn();
		}

		float speed = Misc.getSpeedForBurnLevel(maxBurn);
		float accelMult = speed / Misc.getSpeedForBurnLevel(20f);
		if (accelMult < 0.1f) accelMult = 0.1f;
		setAccelMult(1f/ accelMult);
		
		float eBurnRange = 800f + random.nextFloat() * 200f;
		addBehavior(new GBIntercept(fleet, 5f + random.nextFloat() * 2f, maxBurn, eBurnRange, true));
		addBehavior(new GBIntercept(fleet, 3f + random.nextFloat() * 2f, 
									maxBurn + (int)Math.round(EmergencyBurnAbility.MAX_BURN_MOD),
									0f, true));
		if (soundId != null) {
			addInterrupt(new GBIPlaySound(0f, soundId, 1f, 1f));
		}
		
	}
	
}



