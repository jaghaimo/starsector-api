package com.fs.starfarer.api.impl.campaign.ghosts.types;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBDartAround;
import com.fs.starfarer.api.impl.campaign.ghosts.GBFollowClosely;
import com.fs.starfarer.api.impl.campaign.ghosts.GBGoAwayFrom;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIDespawn;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIGenerateSlipstream;
import com.fs.starfarer.api.impl.campaign.ghosts.GBITooClose;
import com.fs.starfarer.api.impl.campaign.ghosts.GBLeviathanCalfRun;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class LeviathanCalfGhost extends BaseSensorGhost {

	public LeviathanCalfGhost(SensorGhostManager manager, SectorEntityToken parent) {
		super(manager, 0);
		
		float size = genFloat(200f, 250f);
		initEntity(genLargeSensorProfile(), size);
		setDespawnRange(-100f);
		entity.addTag(Tags.UNAFFECTED_BY_SLIPSTREAM);
		
		int burnLevel = 30;
		setAccelMult(0.3f);
		
		setVel(parent.getVelocity());
		
		placeNearEntity(parent, 100f, 200f);
		
		addBehavior(new GBFollowClosely(parent, 1000f, burnLevel, 0f, 100f));
	}
	
	public LeviathanCalfGhost(SensorGhostManager manager) {
		super(manager, 0);
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		float size = genFloat(200f, 250f);
		initEntity(genLargeSensorProfile(), size);
		setDespawnRange(-100f);
		entity.addTag(Tags.UNAFFECTED_BY_SLIPSTREAM);
		
		int burnLevel = 25;
		
		setAccelMult(0.1f);
		
		if (!placeNearPlayer()) {
			setCreationFailed();
			return;
		}
		
		float travelDur = genFloat(5f, 8f);
		
		//addBehavior(new GBStayInPlace(10f));
		SectorEntityToken loc = Global.getSector().getHyperspace().createToken(entity.getLocation());
		addBehavior(new GBDartAround(loc, 20f, 2, 0f, 100f));
		addInterrupt(new GBITooClose(0f, pf, 100f));
		addInterrupt(new GBIDespawn(genFloat(10f, 15f)));
		addBehavior(new GBLeviathanCalfRun(travelDur, pf,
				0.01f + 0.02f * manager.getRandom().nextFloat(), 
				0f + 15f * manager.getRandom().nextFloat(),
				burnLevel, true));
		addInterrupt(new GBIGenerateSlipstream(size, size * 1.2f, burnLevel - 5, 30f, 20, travelDur));
		addBehavior(new GBGoAwayFrom(10f, pf, 30));
	}
}







