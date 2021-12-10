package com.fs.starfarer.api.impl.campaign.ghosts.types;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.GBGoTo;
import com.fs.starfarer.api.impl.campaign.ghosts.GBStayInPlace;
import com.fs.starfarer.api.impl.campaign.ghosts.GBStormArea;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class StormcallerGhost extends BaseSensorGhost {

	public StormcallerGhost(SensorGhostManager manager) {
		super(manager, 40);
		
		float size = genFloat(600f, 700f);
		initEntity(genHugeSensorProfile(), size);
		//setDespawnRange(-size * 0.7f);
		setDespawnRange(-500f);
		entity.addTag(Tags.UNAFFECTED_BY_SLIPSTREAM);
		if (!placeNearPlayer()) {
			setCreationFailed();
			return;
		}
		
		int numStops = 2 + manager.getRandom().nextInt(4);
		
		int maxBurn = 10;
		Vector2f curr = entity.getLocation();
		for (int i = 0; i < numStops; i++) {
			Vector2f loc = BaseSensorGhostCreator.findDeepHyperspaceArea(curr, 1000f, 3000f, 1000f, manager.getRandom());
			curr = loc;
			if (loc == null) {
				setCreationFailed();
				return;
			}
			SectorEntityToken target = Global.getSector().getHyperspace().createToken(loc);
			addBehavior(new GBGoTo(20f, target, maxBurn));
			addBehavior(new GBStayInPlace(0.4f + manager.getRandom().nextFloat() * 0.3f));
			addBehavior(new GBStormArea(1500f + manager.getRandom().nextFloat() * 500f));
			addBehavior(new GBStayInPlace(0.4f + manager.getRandom().nextFloat() * 0.3f));
		}
	}
	
}






