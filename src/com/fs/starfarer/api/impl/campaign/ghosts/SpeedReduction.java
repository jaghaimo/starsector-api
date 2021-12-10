package com.fs.starfarer.api.impl.campaign.ghosts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;

public class SpeedReduction implements EveryFrameScript {

	protected float elapsed;
	protected SectorEntityToken target;
	protected float speedReductionRate;
	protected float durationSeconds;

	public SpeedReduction(SectorEntityToken target, float speedReductionFraction) {
		this(target, speedReductionFraction, 0.2f);
	}
	public SpeedReduction(SectorEntityToken target, float speedReductionFraction, float durationSeconds) {
		this.target = target;
		this.durationSeconds = durationSeconds;
		speedReductionRate = target.getVelocity().length() * speedReductionFraction / durationSeconds;
	}

	public void advance(float amount) {
		if (target instanceof CampaignFleetAPI) {
			target.setOrbit(null);
		}
		
		Vector2f v = target.getVelocity();
		
		Vector2f dV = Misc.getUnitVector(new Vector2f(), v);
		dV.scale(-1f * Math.min(v.length(), speedReductionRate * amount));
		
		SensorGhost ghost = SensorGhostManager.getGhostFor(target);
		if (ghost != null) {
			v = ghost.getMovement().getVelocity();
			ghost.getMovement().getVelocity().set(v.x + dV.x, v.y + dV.y);
		} else if (target instanceof CampaignFleetAPI) {
			v = ((CampaignFleetAPI)target).getVelocityFromMovementModule(); 
			((CampaignFleetAPI)target).setVelocity(v.x + dV.x, v.y + dV.y);
		} else {
			target.getVelocity().set(v.x + dV.x, v.y + dV.y);
		}

		elapsed += amount;
	}

	public boolean isDone() {
		return elapsed >= durationSeconds;
	}

	public boolean runWhilePaused() {
		return false;
	}

}
