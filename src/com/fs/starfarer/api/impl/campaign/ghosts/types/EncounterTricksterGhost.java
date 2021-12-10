package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBCircle;
import com.fs.starfarer.api.impl.campaign.ghosts.GBITowardsEntity;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIntercept;
import com.fs.starfarer.api.impl.campaign.ghosts.GBLeadPlayerTo;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;

public class EncounterTricksterGhost extends BaseSensorGhost {

	public EncounterTricksterGhost(SensorGhostManager manager, CampaignFleetAPI other, boolean guideToTarget) {
		super(manager, guideToTarget ? 40 : 20);
		
		Random random = manager.getRandom();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		float circleRadius = genFloat(300f, 400f);
		
		initEntity(genLargeSensorProfile(), genSmallRadius());
		placeNearEntity(other, 0f, 0f);
		
		setDespawnRange(-1000f);
		
		if (guideToTarget) {
			SectorEntityToken e = pf.getContainingLocation().createToken(new Vector2f(other.getLocation()));
			other.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE,
								e, 10f + 20f * random.nextFloat(), "investigating anomalous readings");
			
			addBehavior(new GBIntercept(pf, 10f, 40, circleRadius + 500f, true));
			addBehavior(new GBCircle(pf, genDelay(2f), 40, circleRadius, 1f));
			addInterrupt(new GBITowardsEntity(genDelay(1.5f), pf, other));
			addBehavior(new GBLeadPlayerTo(20f, other, genFloat(500f, 600f), 25));
		} else {
			other.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE,
					entity, 10f + 20f * random.nextFloat(), "investigating sensor ghost");
			
			addBehavior(new GBIntercept(pf, 10f, (int)Math.round(other.getFleetData().getBurnLevel()) + 1, 100f, true));
		}
		
	}
	
}






