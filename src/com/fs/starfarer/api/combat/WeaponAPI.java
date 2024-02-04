package com.fs.starfarer.api.combat;

import java.awt.Color;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.MuzzleFlashSpec;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface WeaponAPI {
	
	public static interface DerivedWeaponStatsAPI {
		float getBurstFireDuration();
		float getSustainedDps();
		float getEmpPerSecond();
		float getDamageOver30Sec();
		float getDps();
		float getBurstDamage();
		float getFluxPerDam();
		float getRoF();
		float getFluxPerSecond();
		float getSustainedFluxPerSecond();
		/**
		 * Multiplied by number of barrels for LINKED, by 2 for DUAL_LINKED, and by the number of missiles for MIRVs.
		 * @return
		 */
		float getDamagePerShot();
		float getEmpPerShot();
	}
	
	public static enum WeaponType {
		BALLISTIC("Ballistic"),
		ENERGY("Energy"),
		MISSILE("Missile"),
		LAUNCH_BAY("Launch Bay"),
		UNIVERSAL("Universal"),
		HYBRID("Hybrid"),
		SYNERGY("Synergy"),
		COMPOSITE("Composite"),
		BUILT_IN("Built in"),
		DECORATIVE("Decorative"),
		SYSTEM("System"),
		STATION_MODULE("Station Module");
		
		private String displayName;
		private WeaponType(String displayName) {
			this.displayName = displayName;
		}
		public String getDisplayName() {
			return displayName;
		}
		
	}
	
	public static enum WeaponSize {
		SMALL("Small"),
		MEDIUM("Medium"),
		LARGE("Large");
		
		private String displayName;
		private WeaponSize(String name) {
			this.displayName = name;
		}
		public String getDisplayName() {
			return displayName;
		}
	}
	
	public static enum AIHints {
		PD,
		PD_ONLY,
		PD_ALSO, // PD, but only if there are no other targets
		USE_VS_FRIGATES,
		STRIKE,
		DANGEROUS, /** like STRIKE but only for when the enemy ship is considering it, not for its own weapon use */
		BOMB,
		GUIDED_POOR,
		DO_NOT_AIM,
		ANTI_FTR,
		HEATSEEKER,
		SYSTEM,
		SHOW_IN_CODEX,
		AUTOZOOM,
		DO_NOT_CONSERVE,
		CONSERVE_1,
		CONSERVE_2,
		CONSERVE_3,
		CONSERVE_4,
		CONSERVE_5,
		CONSERVE_ALL,
		CONSERVE_FOR_ANTI_ARMOR,
		FIRE_WHEN_INEFFICIENT,
		EXTRA_RANGE_ON_FIGHTER,
		
		IGNORES_FLARES,
		
		GROUP_LINKED,
		GROUP_ALTERNATING,
		
		MISSILE_SPREAD,
		DIRECT_AIM,
		NO_TURN_RATE_BOOST_WHEN_IDLE,
		RESET_BARREL_INDEX_ON_BURST,
		
		USE_LESS_VS_SHIELDS,
	}
	
	
	String getId();
	WeaponType getType();
	WeaponSize getSize();
	
	void setPD(boolean pd);
	
	/**
	 * Returns 0 if the target is in arc, angular distance to edge of arc otherwise.
	 * @param target
	 * @return
	 */
	float distanceFromArc(Vector2f target);
	boolean isAlwaysFire();
	
	float getCurrSpread();
	float getCurrAngle();
	float getArcFacing();
	float getArc();
	void setCurrAngle(float angle);
	
	float getRange();
	float getDisplayArcRadius();
	float getChargeLevel();
	float getTurnRate();
	float getProjectileSpeed();
	String getDisplayName();
	int getAmmo();
	int getMaxAmmo();
	void setMaxAmmo(int maxAmmo);
	void resetAmmo();
	float getCooldownRemaining();
	float getCooldown();
	void setRemainingCooldownTo(float value);
	
	boolean isBeam();
	boolean isBurstBeam();
	boolean isPulse();
	boolean requiresFullCharge();
	
	/**
	 * @return location, in absolute engine coordinates.
	 */
	Vector2f getLocation();
	
	boolean isFiring();
	
	boolean usesAmmo();
	boolean usesEnergy();
	
	boolean hasAIHint(AIHints hint);
	CollisionClass getProjectileCollisionClass();
	
	void beginSelectionFlash();
	float getFluxCostToFire();
	
	float getMaxHealth();
	float getCurrHealth();
	boolean isDisabled();
	float getDisabledDuration();
	
	boolean isPermanentlyDisabled();
	
	DamageType getDamageType();
	
	ShipAPI getShip();
	
	/**
	 * Base stats, does not include character skill bonuses/hull mods/etc.
	 * @return
	 */
	DerivedWeaponStatsAPI getDerivedStats();
	
	void setAmmo(int ammo);

	/**
	 * @return null for non-animated weapons.
	 */
	AnimationAPI getAnimation();
	
	
	/**
	 * Note:
	 * setAlphaMult() and setAngle() will be called on the sprite returned here just prior to rendering.
	 * Thus, setting these is pointless - the values will be overridden. Uses the alpha channel in SpriteAPI.setColor()
	 * and WeaponAPI.setCurrAngle() instead.
	 * 
	 * @return either the base sprite, or, for animated weapons, the sprite for the current frame.
	 */
	SpriteAPI getSprite();
	
	/**
	 * "Base" sprite for the weapon (see: mjolnir.wpn), or null.
	 * @return
	 */
	SpriteAPI getUnderSpriteAPI();
	
	/**
	 * Sprite with the weapon barrels, or null if the weapon doesn't use recoil/separate barrel graphics.
	 * @return
	 */
	SpriteAPI getBarrelSpriteAPI();
	/**
	 * Renders the barrel. Shouldn't need to do this unless for shaders etc.
	 * @param alphaMult
	 */
	void renderBarrel(SpriteAPI sprite, Vector2f loc, float alphaMult);
	/**
	 * Whether the barrel goes below or above the weapon sprite.
	 * @return
	 */
	boolean isRenderBarrelBelow();
	
	
	void disable();
	
	void disable(boolean permanent);
	
	void repair();
	
	WeaponSpecAPI getSpec();
	WeaponSlotAPI getSlot();
	
	EveryFrameWeaponEffectPlugin getEffectPlugin();
	
	List<MissileRenderDataAPI> getMissileRenderData();
	DamageAPI getDamage();
	float getProjectileFadeRange();
	boolean isDecorative();
	void ensureClonedSpec();
	float getAmmoPerSecond();
	void setPDAlso(boolean pdAlso);
	void setCurrHealth(float currHealth);
	MuzzleFlashSpec getMuzzleFlashSpec();
	List<BeamAPI> getBeams();
	Vector2f getFirePoint(int barrel);
	void setTurnRateOverride(Float turnRateOverride);
	SpriteAPI getGlowSpriteAPI();
	AmmoTrackerAPI getAmmoTracker();
	void setRefireDelay(float delay);
	void setFacing(float facing);
	void updateBeamFromPoints();
	boolean isKeepBeamTargetWhileChargingDown();
	void setKeepBeamTargetWhileChargingDown(boolean keepTargetWhileChargingDown);
	void setScaleBeamGlowBasedOnDamageEffectiveness(boolean scaleGlowBasedOnDamageEffectiveness);
	void setForceFireOneFrame(boolean forceFire);
	void setGlowAmount(float glow, Color glowColor);
	void setForceNoFireOneFrame(boolean forceNoFireOneFrame);
	void setSuspendAutomaticTurning(boolean suspendAutomaticTurning);
	float getBurstFireTimeRemaining();
	Vector2f getRenderOffsetForDecorativeBeamWeaponsOnly();
	void setRenderOffsetForDecorativeBeamWeaponsOnly(Vector2f renderOffsetForDecorativeBeamWeaponsOnly);
	float getRefireDelay();
	void forceShowBeamGlow();
	boolean isInBurst();
	WeaponSpecAPI getOriginalSpec();
	void setWeaponGlowWidthMult(float weaponGlowWidthMult);
	void setWeaponGlowHeightMult(float weaponGlowHeightMult);
	void stopFiring();
}


