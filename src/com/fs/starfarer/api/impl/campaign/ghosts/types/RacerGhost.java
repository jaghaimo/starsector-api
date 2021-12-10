package com.fs.starfarer.api.impl.campaign.ghosts.types;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBFollowStream;
import com.fs.starfarer.api.impl.campaign.ghosts.GBGoInDirection;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamSegment;
import com.fs.starfarer.api.util.Misc;

public class RacerGhost extends BaseSensorGhost {

	public RacerGhost(SensorGhostManager manager, SlipstreamSegment segment, SlipstreamTerrainPlugin2 plugin) {
		super(manager, 20);
		
		initEntity(genLargeSensorProfile(), genSmallRadius());
		setDespawnRange(-entity.getRadius() * 0.5f);
		
		Random random = manager.getRandom();
		float offset = (random.nextBoolean() ? 1f : -1f) * random.nextFloat() * 0.5f;
		
		Vector2f loc = plugin.getPointAt(segment.totalLength, offset);
		if (loc == null) {
			setCreationFailed();
			return;
		}
		setLoc(loc);
		
		float intensity = plugin.getIntensity(offset);
		float wMult = plugin.getWidthBasedSpeedMult(segment.totalLength);
		float b = plugin.getFaderBrightness(segment.totalLength);
		float speedMult = intensity * wMult * b;
		
		float speed = Misc.getSpeedForBurnLevel(plugin.getParams().burnLevel);
		Vector2f vel = new Vector2f(segment.dir);
		vel.scale(speed * speedMult);
		setVel(vel);
		
		int burn = 17 + random.nextInt(15);
		
		float dur = 5f + 3f * random.nextFloat();
		addBehavior(new GBFollowStream(dur, burn, plugin));
		addBehavior(new GBGoInDirection(dur * 0.5f, random.nextFloat() * 360f, burn));
	}
}





