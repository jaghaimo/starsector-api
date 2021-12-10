package com.fs.starfarer.api.impl.campaign.ghosts.types;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBEchoMovement;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class EchoGhost extends BaseSensorGhost {

	public EchoGhost(SensorGhostManager manager, CampaignFleetAPI fleet) {
		super(manager, 0);
		
		initEntity(2000f, fleet.getRadius());
		if (!placeNearPlayer(700f, 1200f)) {
			setCreationFailed();
			return;
		}
		
		entity.addTag(Tags.UNAFFECTED_BY_SLIPSTREAM);
		
		float delay = 0.05f + manager.getRandom().nextFloat() * 0.05f;
		float duration = 5f + manager.getRandom().nextFloat() * 5f;
		addBehavior(new GBEchoMovement(fleet, delay, duration));
	}
	
}
