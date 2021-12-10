package com.fs.starfarer.api.impl.campaign.ghosts.types;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBDartAround;
import com.fs.starfarer.api.impl.campaign.ghosts.GBGoAwayFrom;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIDespawn;
import com.fs.starfarer.api.impl.campaign.ghosts.GBITooClose;
import com.fs.starfarer.api.impl.campaign.ghosts.GBITooCloseToOther;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.impl.campaign.ghosts.SharedTrigger;

public class MinnowGhost extends BaseSensorGhost {

	public MinnowGhost(SensorGhostManager manager, SectorEntityToken loc, float minRange, float maxRange, float dur,
					   SharedTrigger trigger) {
		super(manager, 100);
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		
		initEntity(genSmallSensorProfile(), genVerySmallRadius());
		setNumSensorIndicators(3, 5, manager.getRandom());
		placeNearEntity(loc, minRange, maxRange);
//		setCreationFailed();
//		return;
		
		trigger = null; // doesn't look that great, actually; adding GBITooCloseToOther instead 
		
		setAccelMult(0.5f);
		addBehavior(new GBDartAround(loc, dur, 20, minRange, maxRange));
		addInterrupt(new GBITooClose(0, pf, 100f, trigger));
		addInterrupt(new GBITooCloseToOther(0, pf, loc, 300f));
		addInterrupt(new GBIDespawn(dur - 0.1f));
		addBehavior(new GBGoAwayFrom(1f + manager.getRandom().nextFloat() * 1f, pf, 100));
//		addBehavior(new GBGoInDirection(5f + manager.getRandom().nextFloat() * 5f, 
//									    manager.getRandom().nextFloat() * 360f, 100));

	}
}
