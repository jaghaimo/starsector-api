package com.fs.starfarer.api.impl.campaign.ghosts.types;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBCircle;
import com.fs.starfarer.api.impl.campaign.ghosts.GBFollowClosely;
import com.fs.starfarer.api.impl.campaign.ghosts.GBGoAwayFrom;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIRemoraDrain;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIntercept;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class RemoraGhost extends BaseSensorGhost {

	public RemoraGhost(SensorGhostManager manager, SectorEntityToken target, float duration) {
		this(manager, target, duration, null);
	}
	public RemoraGhost(SensorGhostManager manager, SectorEntityToken target, float duration, Vector2f loc) {
		super(manager, 50);
		
		float circleRadius = genFloat(0f, 50f);
		if (target instanceof CampaignFleetAPI) {
			circleRadius = genFloat(300f, 500f);
		}
		
		initEntity(genMediumSensorProfile(), genSmallRadius());
		setDespawnRange(0f);
		entity.addTag(Tags.IMMUNE_TO_REMORA_PULSE);
		
		if (loc != null) {
			setLoc(loc);
		} else if (target.isPlayerFleet()) {
			if (!placeNearPlayer()) {
				setCreationFailed();
				return;
			}
		} else {
			placeNearEntity(target, 200f, 300f);
		}
		
		addBehavior(new GBIntercept(target, 10f, 25, circleRadius + 500f, true));
		if (target instanceof CampaignFleetAPI) {
			setAccelMult(5f);
			addBehavior(new GBCircle(target, duration, 25 + manager.getRandom().nextInt(6), circleRadius, 1f));
		} else {
			setAccelMult(0.1f); // GBFollowClosely doesn't work well otherwise
			addBehavior(new GBFollowClosely(target, duration, 50, 200f, 300f));
		}
		addInterrupt(new GBIRemoraDrain(target, 750f));
		addBehavior(new GBGoAwayFrom(5f, target, 20));
	}
}











