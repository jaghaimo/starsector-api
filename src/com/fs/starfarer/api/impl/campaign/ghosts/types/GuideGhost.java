package com.fs.starfarer.api.impl.campaign.ghosts.types;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBCircle;
import com.fs.starfarer.api.impl.campaign.ghosts.GBITowardsEntity;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIntercept;
import com.fs.starfarer.api.impl.campaign.ghosts.GBLeadPlayerTo;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;

public class GuideGhost extends BaseSensorGhost {

	public GuideGhost(SensorGhostManager manager, SectorEntityToken target) {
		super(manager, 40);
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		float circleRadius = genFloat(300f, 400f);
		
		initEntity(genLargeSensorProfile(), genSmallRadius());
		if (!placeNearPlayer()) {
			setCreationFailed();
			return;
		}
		
		addBehavior(new GBIntercept(pf, 10f, 40, circleRadius + 500f, true));
		addBehavior(new GBCircle(pf, genDelay(2f), 40, circleRadius, -1f));
		addInterrupt(new GBITowardsEntity(genDelay(1.5f), pf, target));
		addBehavior(new GBLeadPlayerTo(20f, target, genFloat(500f, 600f), 25));
	}
}
