package com.fs.starfarer.api.impl.campaign.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * Ability that can be toggled on and off.
 * 
 * (Why use methods to pass in sound ids etc instead of passing them in to a constructor?
 * Mainly so they don't need to go into the save file as they would if they were
 * stored in member variables.)
 * 
 * @author Alex Mosolov
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public abstract class BaseToggleAbility extends BaseAbilityPlugin {

	public float getLoopSoundUIVolume() { return level; }
	public float getLoopSoundUIPitch() { return 1f; }
	
	public float getLoopSoundWorldVolume() { return level; }
	public float getLoopSoundWorldPitch() { return 1f; }
	
	
	public float getActivateCooldownDays() { return spec.getActivationCooldown(); }
	public float getDeactivateCooldownDays() { return spec.getDeactivationCooldown(); }
	
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
	protected float cooldownLeft = 0;
	protected boolean isActivateCooldown = false;
	
	protected float level = 0;
	
	public float getCooldownLeft() {
		return cooldownLeft;
	}
	public void setCooldownLeft(float cooldownLeft) {
		this.cooldownLeft = cooldownLeft;
	}
	
	
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
		
		
		if (cooldownLeft > 0) {
			float days = Global.getSector().getClock().convertToDays(amount);
			cooldownLeft -= days;
			if (cooldownLeft < 0) cooldownLeft = 0;
		}
		
		float prevLevel = level;
		if (turnedOn && level < 1) {
			float days = Global.getSector().getClock().convertToDays(amount);
			level += days / getActivationDays();
			if (level > 1) level = 1;
		} else if (!turnedOn && level > 0) {
			float days = Global.getSector().getClock().convertToDays(amount);
			level -= days / getDeactivationDays();
			if (level < 0) level = 0;
		}
		if (prevLevel != level || level > 0) {
			applyEffect(amount, level);
			//disableIncompatible();
		}
	}
	
	protected void addIncompatibleToTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		addIncompatibleToTooltip(tooltip, "Interrupts the following abilities when activated:",
										  "Expand tooltip to view conflicting abilities",
										  expanded);
	}
	
	@Override
	public boolean isUsable() {
		if (!isActivateCooldown && 
				getProgressFraction() > 0 && getProgressFraction() < 1 && 
				getDeactivationDays() > 0) return false;
		return super.isUsable();
	}
	
	@Override
	public float getCooldownFraction() {
		if (cooldownLeft <= 0) return 1f;
		if (isActivateCooldown) {
			float max = getActivateCooldownDays();
			if (max <= 0f) {
				return 1f - cooldownLeft / 1f;
			}
			return 1f - cooldownLeft / max;
		} else {
			float max = getDeactivateCooldownDays();
			if (max <= 0f) {
				return 1f - cooldownLeft / 1f;
			}
			return 1f - cooldownLeft / max;
		}
	}
	
	@Override
	public float getProgressFraction() {
		return level;
	}
	
	@Override
	public boolean showProgressIndicator() {
		return level > 0 && (!turnedOn || level < 1);
	}
	
	@Override
	public boolean showActiveIndicator() {
		return isActive() && level >= 1;
	}
	
	public void pressButton() {
		if (isActive()) {
			deactivate();
		} else {
			activate();
		}
		if (entity.isPlayerFleet()) {
			if (isActive()) {
				String soundId = getOnSoundUI();
				if (soundId != null) {
					Global.getSoundPlayer().playUISound(soundId, 1f, 1f);
				}
			} else {
				String soundId = getOffSoundUI();
				if (soundId != null) {
					Global.getSoundPlayer().playUISound(soundId, 1f, 1f);
				}
			}
		}
	}
	
	public void activate() {
		if (!isActive() && isUsable()) {
			turnedOn = true;
			if (entity.isInCurrentLocation() && entity.isVisibleToPlayerFleet() && !entity.isPlayerFleet()) {
				String soundId = getOnSoundWorld();
				if (soundId != null) {
					Global.getSoundPlayer().playSound(soundId, 1f, 1f, entity.getLocation(), entity.getVelocity());
				}
			}
			if (getActivationDays() <= 0) {
				level = 1;
			}
			
			cooldownLeft = getActivateCooldownDays();
			isActivateCooldown = true;
			
			if (entity.isInCurrentLocation()) {
				if (getActivationText() != null && entity.isVisibleToPlayerFleet()) {
					entity.addFloatingText(getActivationText(), Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
				}
			}
			activateImpl();
			applyEffect(0f, level);
			interruptIncompatible();
			
			super.activate();
		}
	}

	public void deactivate() {
		if (isActive()) {// && isUsable()) {
			turnedOn = false;
			if (entity.isInCurrentLocation() && entity.isVisibleToPlayerFleet() && !entity.isPlayerFleet()) {
				String soundId = getOffSoundWorld();
				if (soundId != null) {
					Global.getSoundPlayer().playSound(soundId, 1f, 1f, entity.getLocation(), entity.getVelocity());
				}
			}
			if (getDeactivationDays() <= 0) {
				level = 0;
			}
			cooldownLeft = getDeactivateCooldownDays();
			isActivateCooldown = false;
			
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
		
		applyEffect(0, 0);
		
		cleanupImpl();
	}
	
	public boolean isActive() {
		return turnedOn;
	}
	
	public boolean hasCustomButtonPressSounds() {
		return getOnSoundUI() != null;
	}
	
	@Override
	public boolean runWhilePaused() {
		return false;
	}
	public float getLevel() {
		return level;
	}
	
}





