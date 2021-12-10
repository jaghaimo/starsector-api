package com.fs.starfarer.api.impl.campaign.ghosts.types;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.GBGoInDirectionWithWobble;
import com.fs.starfarer.api.impl.campaign.ghosts.GBIGenerateSlipstream;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;

public class LeviathanGhost extends BaseSensorGhost {

	public LeviathanGhost(SensorGhostManager manager, int burnMod) {
		super(manager, 0);
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		float size = genFloat(400f, 500f);
		initEntity(genHugeSensorProfile(), size);
		setDespawnRange(-200f);
		entity.addTag(Tags.UNAFFECTED_BY_SLIPSTREAM);
		
		float dirToCore = Misc.getAngleInDegrees(pf.getLocation());
		float travelDirMin = dirToCore + 90f - 20f;
		float travelDirMax = dirToCore + 90f + 20f;
		float travelDir = travelDirMin + (travelDirMax - travelDirMin) * getRandom().nextFloat();
		travelDir = Misc.normalizeAngle(travelDir);

		float spawnDist = genFloat(6000, 8000);
		float spawnAngle = (travelDir + 30f - 60f * getRandom().nextFloat()) + 180f;
		
		if (getRandom().nextBoolean()) {
			travelDir += 180f;
			spawnAngle += 180f;
		}
		
		Vector2f loc = Misc.getUnitVectorAtDegreeAngle(spawnAngle);
		loc.scale(spawnDist);
		Vector2f.add(loc, pf.getLocation(), loc);
		setLoc(loc);
		
		int burnLevel = 25 + burnMod;
		
		float speed = Misc.getSpeedForBurnLevel(burnLevel);
		Vector2f vel = Misc.getUnitVectorAtDegreeAngle(travelDir);
		vel.scale(speed);
		setVel(vel);
		
		float travelDur = genFloat(15f, 20f);
		float streamDur = travelDur - (1f + getRandom().nextFloat());
		
		setAccelMult(0.1f);
		//addBehavior(new GBGoInDirection(genDelay(20f), travelDir, burnLevel));
		//addBehavior(new GBGoInDirection(1000f, travelDir, burnLevel));
		addBehavior(new GBGoInDirectionWithWobble(travelDur, travelDir,
							0.01f + 0.04f * manager.getRandom().nextFloat(), 
							10f + 20f * manager.getRandom().nextFloat(), burnLevel));
		addInterrupt(new GBIGenerateSlipstream(size, size * 1.2f, burnLevel - 5, 30f, 20, streamDur));
		
		
		
//		if (!placeNearPlayer()) {
//			setCreationFailed();
//			return;
//		}
//		addBehavior(new GBStayInPlace(10f));
//		addInterrupt(new GBITooClose(0f, pf, 500f));
//		addBehavior(new GBSlipstreamPath(genDelay(5f), pf, burnLevel, true));
//		addInterrupt(new GBIGenerateSlipstream(size, burnLevel - 5, 50f, 20));

	}
}





