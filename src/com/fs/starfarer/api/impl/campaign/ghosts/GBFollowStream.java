package com.fs.starfarer.api.impl.campaign.ghosts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2;
import com.fs.starfarer.api.util.Misc;


public class GBFollowStream extends BaseGhostBehavior {
	
	protected int maxBurn;
	protected SlipstreamTerrainPlugin2 plugin;
	protected float phase = 0f;
	protected float preferredYOff = -100f;
	protected boolean courseCorrecting = false;
	
	public GBFollowStream(float duration, int maxBurn, SlipstreamTerrainPlugin2 plugin) {
		super(duration);
		this.maxBurn = maxBurn;
		this.plugin = plugin;
		
		phase = Misc.random.nextFloat();
	}

	@Override
	public void advance(float amount, SensorGhost ghost) {
		if (!ghost.getEntity().isInCurrentLocation()) {
			end();
			return;
		}
		super.advance(amount, ghost);
		
		float [] coords = plugin.getLengthAndWidthFractionWithinStream(ghost.getEntity().getLocation());
		if (coords == null) {
			end();
			return;
		}
		float distAlong = coords[0];
		float yOff = coords[1];
		if (preferredYOff < -10f) preferredYOff = yOff;
		float yOffDest = preferredYOff;
		boolean closeToPreferred = Math.abs(yOff - preferredYOff) < 0.25f;
		boolean veryCloseToPreferred = Math.abs(yOff - preferredYOff) < 0.05f;
		if (!closeToPreferred) {
			courseCorrecting = true;
		}
		if (veryCloseToPreferred) {
			courseCorrecting = false;
		}
		if (!courseCorrecting) {
			yOffDest = yOff;
		}
		Vector2f p1 = plugin.getPointAt(distAlong, yOff);
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		//pf = null;
		float dist = Float.MAX_VALUE;
		if (pf != null) {
			dist = Misc.getDistance(ghost.getEntity(), pf);
		}
		if (pf != null && dist < 500f) {
			float angleDiff = Misc.getAngleDiff(Misc.getAngleInDegrees(ghost.getEntity().getVelocity()), 
											  Misc.getAngleInDegrees(ghost.getEntity().getLocation(), pf.getLocation()));
			coords = plugin.getLengthAndWidthFractionWithinStream(pf.getLocation());
			if (coords != null && angleDiff < 90f) {
				float yOffPlayer = coords[1];
				float test1 = yOffPlayer - 0.4f;
				float test2 = yOffPlayer + 0.4f;
				if (Math.abs(test1) > 0.67f) test1 = Math.signum(test1) * 0.67f;
				if (Math.abs(test2) > 0.67f) test2 = Math.signum(test2) * 0.67f;
				float diff1 = Math.abs(test1 - yOff);
				float diff2 = Math.abs(test2 - yOff);
//				float diff1 = Math.abs(test1);
//				float diff2 = Math.abs(test2);
				float diff3 = Math.abs(yOff - yOffPlayer);
				if (diff3 < 0.4f) {
					if (diff1 < diff2) {
						yOffDest = test1;
					} else {
						yOffDest = test2;
					}
				}
			}
		}
		
		Vector2f p2 = plugin.getPointAt(distAlong + 200f, yOffDest);
		if (p1 == null || p2 == null) {
			end();
			return;
		}
		Vector2f dest = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(p1, p2));
		
		dest.scale(1000f);
		Vector2f.add(dest, ghost.getEntity().getLocation(), dest);

		ghost.moveTo(dest, new Vector2f(), maxBurn);
	}
	
	
	
}













