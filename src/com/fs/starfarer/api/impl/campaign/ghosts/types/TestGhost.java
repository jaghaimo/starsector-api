package com.fs.starfarer.api.impl.campaign.ghosts.types;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBFollow;
import com.fs.starfarer.api.impl.campaign.ghosts.GBGoAwayFrom;
import com.fs.starfarer.api.impl.campaign.ghosts.GBITooClose;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;

public class TestGhost extends BaseSensorGhost {

	public TestGhost(SensorGhostManager manager) {
		super(manager, 30);
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		//initEntity(1000f, 200f);
		initEntity(2000f, pf.getRadius());
		placeNearPlayer(700f, 1200f);
		
		//getMovement().getLocation().set(pf.getLocation().x + 900, pf.getLocationInHyperspace().y);
		
		float delay = 0.05f + manager.getRandom().nextFloat() * 0.05f;
		//addBehavior(new GBStayInPlace(1f));
		//addBehavior(new GBEchoMovement(pf, delay, 5f));
		//addBehavior(new GBIntercept(pf, 10f, 20, true));
		//addBehavior(new GBIntercept(pf, 10f, 20, 500f, true));
		//addBehavior(new GBCircle(pf, 10f, 50, 600f, -1f));
		
		addBehavior(new GBFollow(pf, 1000f, 15, 1200f, 1800f));
		addInterrupt(new GBITooClose(0f, pf, 300f));
		addBehavior(new GBGoAwayFrom(10f, pf, 50));
	}
}
