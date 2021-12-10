package com.fs.starfarer.api.impl.campaign.ghosts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public class GBIPlaySound extends BaseGhostBehaviorInterrupt {

	protected String soundId;
	protected float volume;
	protected float pitch;
	protected boolean played = false;

	public GBIPlaySound(float delay, String soundId, float pitch, float volume) {
		super(delay);
		this.soundId = soundId;
		this.pitch = pitch;
		this.volume = volume;
	}

	@Override
	public boolean shouldInterruptBehavior(SensorGhost ghost, GhostBehavior behavior) {
		if (hasDelayRemaining()) return false;

		if (!played) {
			SectorEntityToken entity = ghost.getEntity();
			if (soundId != null && entity.isInCurrentLocation() && entity.isVisibleToPlayerFleet()) {
				Global.getSoundPlayer().playSound(soundId, pitch, volume, entity.getLocation(), entity.getVelocity());
			}
			played = true;
		}
		
		return false;
	}

	
}
