package com.fs.starfarer.api.impl.campaign.ghosts.types;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBFollow;
import com.fs.starfarer.api.impl.campaign.ghosts.GBGoAwayFrom;
import com.fs.starfarer.api.impl.campaign.ghosts.GBITooClose;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.world.MoteParticleScript;

public class ZigguratGhost extends BaseSensorGhost {

	public ZigguratGhost(SensorGhostManager manager) {
		super(manager, 30);
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		//initEntity(1000f, 200f);
		initEntity(2000f, pf.getRadius());
		if (!placeNearPlayer(700f, 1200f)) {
			setCreationFailed();
			return;
		}
		
		entity.addScript(new MoteParticleScript(entity, 0.1f));
		entity.addTag(Tags.ZIG_GHOST);
		
		addBehavior(new GBFollow(pf, 1000f, 15, 800f, 1500f));
		//addBehavior(new GBIRunEveryFrame(0f, this));
		addInterrupt(new GBITooClose(0f, pf, 300f));
		addBehavior(new GBGoAwayFrom(10f, pf, 50));
	}

}
