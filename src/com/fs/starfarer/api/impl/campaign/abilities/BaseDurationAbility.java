package com.fs.starfarer.api.impl.campaign.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * Ability that's turned on and then plays out until it's finished.
 * 
 * (Why use methods to pass in sound ids etc instead of passing them in to a constructor?
 * Mainly so they don't need to go into the save file as they would if they were
 * stored in member variables.)
 * 
 * @author Alex Mosolov
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public abstract class BaseDurationAbility extends BaseAbilityPlugin {

	public static final float LOOP_FADE_TIME_DAYS = 0.1f;
	
	public float getLoopSoundUIVolume() {
		if (loopFadeLeft > 0) {
			return loopFadeLeft / LOOP_FADE_TIME_DAYS;
		}
		if (level > 0) {
			if (fadingOut) {
				// keep loop volume at 1 when ability is winding down, fade it out after
				return 1;
			} else {
				// ramp loop volume up as ability winds up
				return level;
			}
		}
		return 0;
	}
	public float getLoopSoundUIPitch() { return 1f; }
	
	public float getLoopSoundWorldVolume() { return getLoopSoundUIVolume(); }
	public float getLoopSoundWorldPitch() { return 1f; }
	

	
	public float getCooldownDays() { return spec.getDeactivationCooldown(); }
	public float getDurationDays() { return spec.getDurationDays(); }
	public float getTotalDurationDays() { return spec.getDurationDays() + spec.getActivationDays() + spec.getDeactivationDays(); }
	public float getActivationDays() { return spec.getActivationDays(); }
	public float getDeactivationDays() { return spec.getDeactivationDays(); }
	
	
	protected abstract void activateImpl();
	
	/**
	 * Will be called once when level is 0 and consistently when level >0.
	 * @param level
	 */
	protected abstract void applyEffect(float amount, float level);
	protected abstract void deactivateImpl();
	protected abstract void cleanupImpl();
	

	protected boolean turnedOn = false;
	protected float activeDaysLeft = 0;
	protected float cooldownLeft = 0;
	protected float level = 0;
	
	protected float loopFadeLeft = 0;
	protected boolean fadingOut = false;
	
	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (entity.isInCurrentLocation() && entity.isVisibleToPlayerFleet() &&
				getLoopSoundWorldVolume() > 0 && !Global.getSector().isPaused()) {
			String soundId = getLoopSoundWorld();
			if (soundId != null) {
				Global.getSector().getCampaignUI().suppressMusic(spec.getMusicSuppression() * getLoopSoundWorldVolume());
				Global.getSoundPlayer().playLoop(soundId, entity, 
							getLoopSoundWorldPitch(), getLoopSoundWorldVolume(),
							entity.getLocation(), entity.getVelocity());
			}
		}
		
		if (entity.isPlayerFleet() && getLoopSoundUIVolume() > 0 && !Global.getSector().isPaused()) {
			String soundId = getLoopSoundUI();
			if (soundId != null) {
				Global.getSector().getCampaignUI().suppressMusic(spec.getMusicSuppression() * getLoopSoundUIVolume());
				Global.getSoundPlayer().playLoop(soundId, entity, 
							getLoopSoundUIPitch(), getLoopSoundUIVolume(),
							entity.getLocation(), entity.getVelocity());
			}
		}
		
		if (activeDaysLeft > 0) {
			float days = Global.getSector().getClock().convertToDays(amount);
			activeDaysLeft -= days;
			
			if (activeDaysLeft <= 0) {
				level = 1f;
				applyEffect(amount, level);
				activeDaysLeft = 0;
				
				deactivate();
			}
		} else if (cooldownLeft > 0) {
			float days = Global.getSector().getClock().convertToDays(amount);
			cooldownLeft -= days;
			if (cooldownLeft < 0) cooldownLeft = 0;
		}
		
		if (loopFadeLeft > 0) {
			float days = Global.getSector().getClock().convertToDays(amount);
			loopFadeLeft -= days;
			if (loopFadeLeft < 0) {
				loopFadeLeft = 0;
			}
		}
		
		
		float prevLevel = level;
		if (activeDaysLeft > 0) {
			float a = getActivationDays();
			float d = getDeactivationDays();
			float t = getTotalDurationDays();
			if (activeDaysLeft > t - a) {
				if (a <= 0) {
					level = 1;
				} else {
					level = 1f - (activeDaysLeft - (t - a)) / a;
				}
			} else if (activeDaysLeft < d) {
				fadingOut = true;
				if (d <= 0) {
					level = 0;
				} else {
					level = activeDaysLeft / d;
				}
			} else {
				level = 1;
			}
		} else {
			level = 0;
		}
		
		if (prevLevel != level || level > 0) {
			applyEffect(amount, level);
			disableIncompatible();
		}
	}

	
	protected void addIncompatibleToTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		addIncompatibleToTooltip(tooltip, "Disables the following abilities while active:",
										  "Expand tooltip to view conflicting abilities",
										  expanded);
	}
	
	@Override
	public float getCooldownFraction() {
		if (cooldownLeft <= 0) return 1f;
		return 1f - cooldownLeft / getCooldownDays();
	}
	
	@Override
	public float getProgressFraction() {
		if (getTotalDurationDays() <= 0 || !turnedOn) return 0f;
		return 1f - (activeDaysLeft / getTotalDurationDays());
	}
	
	@Override
	public boolean showProgressIndicator() {
		return turnedOn;
	}
	
	@Override
	public boolean showActiveIndicator() {
		return false;
	}
	
	@Override
	public boolean isUsable() {
		return !isOnCooldown() && !isInProgress() && !turnedOn && disableFrames <= 0;
	}
	public void pressButton() {
		if (isUsable() && !turnedOn) {
			activate();
			if (entity.isPlayerFleet()) {
				String soundId = getOnSoundUI();
				if (soundId != null) {
					Global.getSoundPlayer().playUISound(soundId, 1f, 1f);
				}
			}
		}
	}
	
	public void activate() {
		if (isUsable() && !turnedOn) {
			turnedOn = true;
			loopFadeLeft = 0f;
			fadingOut = false;
			activeDaysLeft = getTotalDurationDays();
			
			if (entity.isInCurrentLocation() && entity.isVisibleToPlayerFleet() && !entity.isPlayerFleet()) {
				String soundId = getOnSoundWorld();
				if (soundId != null) {
					Global.getSoundPlayer().playSound(soundId, 1f, 1f, entity.getLocation(), entity.getVelocity());
				}
			}
			if (getActivationDays() <= 0) {
				level = 1;
			}
			
			if (entity.isInCurrentLocation()) {
				if (getActivationText() != null && entity.isVisibleToPlayerFleet()) {
					entity.addFloatingText(getActivationText(), Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
				}
			}
			
			activateImpl();
			applyEffect(0f, level);
			interruptIncompatible();
			disableIncompatible();
			
			if (getTotalDurationDays() <= 0) {
				deactivate();
			}
			
			super.activate();
		}
	}

	public void deactivate() {
		if (turnedOn) {
			turnedOn = false;
			activeDaysLeft = 0f;
			loopFadeLeft = LOOP_FADE_TIME_DAYS;
			if (entity.isInCurrentLocation() && entity.isVisibleToPlayerFleet() && !entity.isPlayerFleet()) {
				String soundId = getOffSoundWorld();
				if (soundId != null) {
					Global.getSoundPlayer().playSound(soundId, 1f, 1f, entity.getLocation(), entity.getVelocity());
				}
			}
			if (entity.isPlayerFleet()) {
				String soundId = getOffSoundUI();
				if (soundId != null) {
					Global.getSoundPlayer().playUISound(soundId, 1f, 1f);
				}
			}
			cooldownLeft = getCooldownDays();
			
			level = 0;
			applyEffect(0f, level);
			
			
			if (entity.isInCurrentLocation()) {
				if (getDeactivationText() != null && entity.isVisibleToPlayerFleet()) {
					entity.addFloatingText(getDeactivationText(), Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
				}
			}
			deactivateImpl();
			
			super.deactivate();
		}
	}
	
	
	@Override
	public void cleanup() {
		super.cleanup();
		
		applyEffect(0f, 0f);
		
		cleanupImpl();
	}
	
	public boolean isActive() {
		return false;
	}
	
	@Override
	public boolean isInProgress() {
		return super.isInProgress();
	}
	
	
	public boolean hasCustomButtonPressSounds() {
		return getOnSoundUI() != null;
	}
	
	@Override
	public boolean runWhilePaused() {
		return false;
	}
	public float getActiveDaysLeft() {
		return activeDaysLeft;
	}
	public void setActiveDaysLeft(float activeDaysLeft) {
		this.activeDaysLeft = activeDaysLeft;
	}
	public float getCooldownLeft() {
		return cooldownLeft;
	}
	public void setCooldownLeft(float cooldownLeft) {
		this.cooldownLeft = cooldownLeft;
	}
	public boolean isFadingOut() {
		return fadingOut;
	}
	public float getLevel() {
		return level;
	}
	
}





