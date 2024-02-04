package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.Random;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBCircle;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIRunScript;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIntercept;
import com.fs.starfarer.api.impl.campaign.ghosts.GBStayInPlace;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ShipGhost extends BaseSensorGhost implements Script {

	protected Random random;

	public ShipGhost(SensorGhostManager manager, CampaignFleetAPI fleet) {
		super(manager, 0);
		
		random = Misc.getRandom(manager.getRandom().nextLong(), 5);

		int maxBurn = 9 + random.nextInt(3);;
		initEntity(genMediumSensorProfile(), genSmallRadius());
		
		if (!placeNearPlayer()) {
			setCreationFailed();
			return;
		}
		
		setDespawnRange(-1000f);
		
		float speed = Misc.getSpeedForBurnLevel(maxBurn);
		float accelMult = speed / Misc.getSpeedForBurnLevel(20f);
		if (accelMult < 0.1f) accelMult = 0.1f;
		setAccelMult(1f/ accelMult);
		
		addBehavior(new GBIntercept(fleet, 5f + random.nextFloat() * 2f, maxBurn, 450f, true));
		addBehavior(new GBCircle(fleet, 0.3f + random.nextFloat() * 0.3f, maxBurn / 2, 300f, random.nextBoolean() ? 1f : -1f));
		addBehavior(new GBStayInPlace(0.1f));
		addInterrupt(new GBIRunScript(0f, this, true));
		
	}

	public void run() {
		WeightedRandomPicker<String> factions = SalvageSpecialAssigner.getNearbyFactions(
										random, entity.getLocationInHyperspace(), 15f, 10f, 10f);
		String faction = factions.pick();
		DerelictShipData params = DerelictShipEntityPlugin.createRandom(faction, null, random, DerelictShipEntityPlugin.getDefaultSModProb());
		if (params != null) {
			params.durationDays = 15f + 15f * random.nextFloat();
			
			CustomCampaignEntityAPI ship = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
					random, entity.getContainingLocation(), Entities.WRECK, Factions.NEUTRAL, params);
			SalvageSpecialAssigner.assignSpecials(ship, false, random);
			ship.addTag(Tags.EXPIRES);
			ship.setDiscoverable(false);
			ship.setLocation(entity.getLocation().x, entity.getLocation().y);
			ship.getVelocity().set(entity.getVelocity());
			ship.getMemoryWithoutUpdate().set("$fromGhost", true);
		}		
	}
	
}







