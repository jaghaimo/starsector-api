package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.fleets.AutoDespawnScript;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBCircle;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIRunScript;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIntercept;
import com.fs.starfarer.api.impl.campaign.ghosts.GBStayInPlace;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.util.Misc;

public class RemnantGhost extends BaseSensorGhost implements Script {

	protected Random random;

	public RemnantGhost(SensorGhostManager manager, CampaignFleetAPI fleet) {
		super(manager, 0);
		
		random = Misc.getRandom(manager.getRandom().nextLong(), 5);

		int maxBurn = 9 + random.nextInt(3);;
		initEntity(genLargeSensorProfile(), genLargeRadius());
		
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
		addBehavior(new GBCircle(fleet, 0.7f + random.nextFloat() * 0.5f, maxBurn / 2, 300f, random.nextBoolean() ? 1f : -1f));
		addBehavior(new GBStayInPlace(0.1f));
		addInterrupt(new GBIRunScript(0f, this, true));
		
	}

	public void run() {
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = entity.getLocationInHyperspace();
		
		FleetSize size = FleetSize.MEDIUM;
		FleetQuality quality = FleetQuality.VERY_LOW;
		OfficerQuality oQuality = OfficerQuality.AI_MIXED;
		OfficerNum oNum = OfficerNum.DEFAULT;
		String type = FleetTypes.PATROL_MEDIUM;
		float r = random.nextFloat();
		if (r < 0.25f) {
			size = FleetSize.LARGE;
		} else if (r < 0.5f) {
			size = FleetSize.LARGER;
		}
		
		m.triggerCreateFleet(size, quality, Factions.REMNANTS, type, loc);
		m.getPreviousCreateFleetAction().fQualityMod = -10f;
		m.triggerSetFleetOfficers(oNum, oQuality);
		m.triggerSetRemnantConfigDormant();
		
		CampaignFleetAPI fleet = m.createFleet();
		if (fleet != null) {
			setVel(new Vector2f(0, 0));
			entity.getContainingLocation().addEntity(fleet);
			fleet.setLocation(entity.getLocation().x, entity.getLocation().y);
			fleet.addScript(new AutoDespawnScript(fleet));
			fleet.getMemoryWithoutUpdate().set("$fromGhost", true);
		}
	}
	
}







