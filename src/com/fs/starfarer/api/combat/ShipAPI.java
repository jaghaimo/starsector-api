package com.fs.starfarer.api.combat;

import java.awt.Color;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.CombatListenerManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface ShipAPI extends CombatEntityAPI {

	
	public static enum HullSize {
		DEFAULT, // also makes FIGHTER.ordinal() = 1, which is convenient
		FIGHTER,
		FRIGATE,
		DESTROYER,
		CRUISER,
		CAPITAL_SHIP;
		
		public HullSize smaller(boolean allowFighter) {
			if (this == FRIGATE && allowFighter) return FIGHTER;			
			if (this == DESTROYER) return FRIGATE;			
			if (this == CRUISER) return DESTROYER;			
			if (this == CAPITAL_SHIP) return CRUISER;
			return this;
		}
		
		public HullSize larger() {
			if (this == FIGHTER) return FRIGATE;			
			if (this == FRIGATE) return DESTROYER;			
			if (this == DESTROYER) return CRUISER;			
			if (this == CRUISER) return CAPITAL_SHIP;			
			return this;
		}
	}
	
	/**
	 * ID of FleetMemberAPI this Ship corresponds to. Can be null if there isn't one.
	 * @return
	 */
	String getFleetMemberId();
	
	
	/**
	 * @return Where the ship is aiming with the mouse pointer, in engine coordinates. Works for AI controlled ships too.
	 */
	Vector2f getMouseTarget();
	
	boolean isShuttlePod();
	boolean isDrone();
	boolean isFighter();
	boolean isFrigate();
	boolean isDestroyer();
	boolean isCruiser();
	boolean isCapital();
	
	HullSize getHullSize();

	
	ShipAPI getShipTarget();
	void setShipTarget(ShipAPI ship);
	
	/**
	 * @return 0 or 1, never 100 (neutral).
	 */
	int getOriginalOwner();
	void setOriginalOwner(int originalOwner);
	void resetOriginalOwner();
	
	MutableShipStatsAPI getMutableStats();
	
	boolean isHulk();
	
	List<WeaponAPI> getAllWeapons();
	
	ShipSystemAPI getPhaseCloak();
	ShipSystemAPI getSystem();
	ShipSystemAPI getTravelDrive();
	void toggleTravelDrive();
	
	
	void setShield(ShieldType type, float shieldUpkeep, float shieldEfficiency, float arc);
	
	ShipHullSpecAPI getHullSpec();
	ShipVariantAPI getVariant();
	
	/**
	 * The ship will try to use its system next frame.
	 * Equivalent to the player pressing the "use system" button while controlling a ship.
	 * So, it may fail for various reasons - out of ammo, not enough flux, overloaded, etc.
	 */
	void useSystem();
	
	FluxTrackerAPI getFluxTracker();
	
	
	/**
	 * Use getWing().getWingMembers() instead.
	 * @return null, or list of fighter wing members.
	 */
	@Deprecated List<ShipAPI> getWingMembers();
	
	/**
	 * Use getWing().getLeader() instead.
	 * @return
	 */
	ShipAPI getWingLeader();
	
	boolean isWingLeader();
	
	FighterWingAPI getWing();
	
	List<ShipAPI> getDeployedDrones();
	ShipAPI getDroneSource();
	
	/**
	 * Useful for determining whether fighters are part of the same wing.
	 * @return
	 */
	Object getWingToken();
	
	
	ArmorGridAPI getArmorGrid();
	
	void setRenderBounds(boolean renderBounds);


	void setCRAtDeployment(float cr);
	float getCRAtDeployment();
	float getCurrentCR();
	void setCurrentCR(float cr);
	
	float getWingCRAtDeployment();

	void setHitpoints(float value);

	/**
	 * @return the time that counts for peak CR reduction purposes, not necessarily the full time.
	 */
	float getTimeDeployedForCRReduction();
	
	float getFullTimeDeployed();
	
	boolean losesCRDuringCombat();
	
	/**
	 * If controls are locked due to crash (or regular) mothballing; only applicable in "ESCAPE" battles.
	 * @return
	 */
	boolean controlsLocked();
	
	void setControlsLocked(boolean controlsLocked);
	
	
	void setShipSystemDisabled(boolean systemDisabled);

	
	
	/**
	 * All weapons every disabled during the last battle.
	 * @return
	 */
	Set<WeaponAPI> getDisabledWeapons();
	
	/**
	 * Number of times a full engine flameout occurred during the last battle.
	 * @return
	 */
	int getNumFlameouts();

	float getHullLevelAtDeployment();

	/**
	 * Note: If also changing bounds, make sure they are still within the armor grid
	 * of the ship, which is determined by the original sprite.
	 * @param category under "graphics" in settings.json
	 * @param key id under category.
	 */
	void setSprite(String category, String key);
	
	/**
	 * A wrapper around the internal implementation of a sprite. Creates a new wrapper with every call, should
	 * store the return value and reuse it when possible instead of calling this method every time.
	 * 
	 * @return
	 */
	SpriteAPI getSpriteAPI();
	
	ShipEngineControllerAPI getEngineController();
	
	
	/**
	 * Should only be used by a ShipAIPlugin.
	 * @param command type of the command.
	 * @param param Generally a Vector2f with a "mouse" location. See ShipCommand.java for details.
	 * @param groupNumber Only used for weapon-group-related commands.
	 */
	void giveCommand(ShipCommand command, Object param, int groupNumber);
	
	/**
	 * Only should be called if the AI needs to be changed dynamically. Otherwise,
	 * use ModPlugin.pickShipAI() instead.
	 * @param ai
	 */
	void setShipAI(ShipAIPlugin ai);
	
	/**
	 * Does NOT return the same ai passed in to setShipAI(), but a wrapper around it.
	 * Can be used to save/restore the AI. 
	 * @return
	 */
	ShipAIPlugin getShipAI();
	
	
	/**
	 * Sets the ship's AI to the core implementation.
	 */
	void resetDefaultAI();
	
	
	void turnOnTravelDrive();
	void turnOnTravelDrive(float dur);
	void turnOffTravelDrive();
	
	boolean isRetreating();
	/**
	 * Should be set to "true" to allow the ship to go outside the map bounds and be picked up as "retreated"
	 * when it gets past the proper map boundary. 
	 * @param retreating
	 */
	//void setRetreating(boolean retreating);
	
	
	/**
	 * Call this if beginLandingAnimation() was already called, but  
	 * the ship being landed on was destroyed before FighterLaunchBayAPI.land() is called.
	 * 
	 * Will cause the fighter to reverse its landing animation and take off.
	 */
	void abortLanding();
	
	/**
	 * The fighter will become invulnerable and gradually get smaller/fade out.
	 * Purely visual. If nothing else is done, it will remain this way,
	 * so FighterLaunchBayAPI.land() should be called to remove it from the
	 * engine when the landing animation is complete.
	 * @param target Used to determine what ship the fighter's shadow is cast on.
	 */
	void beginLandingAnimation(ShipAPI target);
	
	/**
	 * @return whether the landing animation has been kicked off (i.e. beginLandingAnimation() was called, and abortLanding() was not)
	 */
	boolean isLanding();
	
	
	/**
	 * @return whether the landing animation is finished.
	 */
	boolean isFinishedLanding();


	/**
	 * @return true if the ship is still in play, including fighters that are currently landed in a launch bay.
	 */
	boolean isAlive();
	
	
	boolean isInsideNebula();
	void setInsideNebula(boolean isInsideNebula);
	boolean isAffectedByNebula();
	void setAffectedByNebula(boolean affectedByNebula);
	
	/**
	 * CR cost to deploy, range is [0, 1].
	 * For fighters returns cost for entire wing.
	 * @return
	 */
	float getDeployCost();
	
	/**
	 * Removes weapon from any groups it's in. Should be used in conjunction with permanently disabling the weapon.
	 * @param weapon
	 */
	void removeWeaponFromGroups(WeaponAPI weapon);
	
	
	
	/**
	 * @param module WeaponAPI or ShipEngineAPI.
	 */
	void applyCriticalMalfunction(Object module);
	
	float getBaseCriticalMalfunctionDamage();
	float getEngineFractionPermanentlyDisabled();


	/**
	 * Alpha the base ship should be rendered at. Includes alpha modifier from ship systems and
	 * from fighters taking off/landing.
	 * @return
	 */
	float getCombinedAlphaMult();


	float getLowestHullLevelReached();


	/**
	 * Null if the ship is not AI-controlled.
	 * @return
	 */
	ShipwideAIFlags getAIFlags();
	List<WeaponGroupAPI> getWeaponGroupsCopy();


	boolean isHoldFire();
	boolean isHoldFireOneFrame();
	void setHoldFireOneFrame(boolean holdFireOneFrame);
	
	boolean isPhased();


	boolean isAlly();


	void setWeaponGlow(float glow, Color color, EnumSet<WeaponType> types);
	void setVentCoreColor(Color color);
	void setVentFringeColor(Color color);
	Color getVentCoreColor();
	Color getVentFringeColor();


//	void setVentCoreTexture(String textureId);
//	void setVentFringeTexture(String textureId);
//	String getVentFringeTexture();
//	String getVentCoreTexture();
	String getHullStyleId();
	WeaponGroupAPI getWeaponGroupFor(WeaponAPI weapon);

	void setCopyLocation(Vector2f loc, float copyAlpha, float copyFacing);
	Vector2f getCopyLocation();
	

	void setAlly(boolean ally);
	
	void applyCriticalMalfunction(Object module, boolean permanent);


	String getId();


	String getName();


	void setJitter(Object source, Color color, float intensity, int copies, float range);
	void setJitterUnder(Object source, Color color, float intensity, int copies, float range);
	void setJitter(Object source, Color color, float intensity, int copies, float minRange, float range);
	void setJitterUnder(Object source, Color color, float intensity, int copies, float minRange, float range);


	float getTimeDeployedUnderPlayerControl();


	SpriteAPI getSmallTurretCover();
	SpriteAPI getSmallHardpointCover();
	SpriteAPI getMediumTurretCover();
	SpriteAPI getMediumHardpointCover();
	SpriteAPI getLargeTurretCover();
	SpriteAPI getLargeHardpointCover();

	boolean isDefenseDisabled();
	void setDefenseDisabled(boolean defenseDisabled);


	void setPhased(boolean phased);

	void setExtraAlphaMult(float transparency);
	void setApplyExtraAlphaToEngines(boolean applyExtraAlphaToEngines);


	void setOverloadColor(Color color);
	void resetOverloadColor();
	Color getOverloadColor();


	boolean isRecentlyShotByPlayer();


	float getMaxSpeedWithoutBoost();


	float getHardFluxLevel();


	void fadeToColor(Object source, Color color, float durIn, float durOut, float maxShift);


	boolean isShowModuleJitterUnder();
	
	/**
	 * False by default. May need to set to true for ships with large decorative weapons etc.
	 * @param showModuleJitterUnder
	 */
	void setShowModuleJitterUnder(boolean showModuleJitterUnder);


	/**
	 * Location is relative to center of ship.
	 * @param color
	 * @param locX
	 * @param locY
	 * @param velX
	 * @param velY
	 * @param maxJitter
	 * @param in
	 * @param dur
	 * @param out
	 * @param additive
	 * @param combineWithSpriteColor
	 * @param aboveShip
	 */
	void addAfterimage(Color color, float locX, float locY, float velX,
			float velY, float maxJitter, float in, float dur, float out,
			boolean additive, boolean combineWithSpriteColor, boolean aboveShip);


	PersonAPI getCaptain();


	WeaponSlotAPI getStationSlot();
	void setStationSlot(WeaponSlotAPI stationSlot);

	ShipAPI getParentStation();
	void setParentStation(ShipAPI station);


	Vector2f getFixedLocation();
	void setFixedLocation(Vector2f fixedLocation);


	boolean hasRadarRibbonIcon();
	boolean isTargetable();


	void setStation(boolean isStation);


	boolean isSelectableInWarroom();


	//boolean isAITargetable();


	boolean isShipWithModules();
	void setShipWithModules(boolean isShipWithModules);
	List<ShipAPI> getChildModulesCopy();


	boolean isPiece();


	/**
	 * Visual clipping bounds for pieces of ships. Returns null for intact ships.
	 * @return
	 */
	BoundsAPI getVisualBounds();


	/**
	 * Rendering offset for weapons and such, due to ship center changes on a ship piece.
	 * (0, 0) for an intact ship.
	 * @return
	 */
	Vector2f getRenderOffset();


	/**
	 * Should be called on a ship that's already a hulk.
	 * Will return the smaller of the two pieces (the current ship becomes the larger piece), or
	 * null if it did not find a way to split the ship. Calling the method again in this case
	 * may result in a valid split being found. 
	 * @return
	 */
	ShipAPI splitShip();


	int getNumFighterBays();


	boolean isPullBackFighters();
	void setPullBackFighters(boolean pullBackFighters);

	boolean hasLaunchBays();
	List<FighterLaunchBayAPI> getLaunchBaysCopy();
	float getFighterTimeBeforeRefit();
	void setFighterTimeBeforeRefit(float fighterTimeBeforeRefit);

	List<FighterWingAPI> getAllWings();

	float getSharedFighterReplacementRate();
	
	boolean areSignificantEnemiesInRange();


	List<WeaponAPI> getUsableWeapons();


	Vector2f getModuleOffset();


	float getMassWithModules();


	PersonAPI getOriginalCaptain();

	boolean isRenderEngines();
	void setRenderEngines(boolean renderEngines);


	WeaponGroupAPI getSelectedGroupAPI();
	void setHullSize(HullSize hullSize);
	void ensureClonedStationSlotSpec();
	void setMaxHitpoints(float maxArmor);
	void setDHullOverlay(String spriteName);
	boolean isStation();
	boolean isStationModule();
	boolean areAnyEnemiesInRange();

	void blockCommandForOneFrame(ShipCommand command);

	float getMaxTurnRate();
	float getTurnAcceleration();
	float getTurnDeceleration();
	float getDeceleration();
	float getAcceleration();
	float getMaxSpeed();
	float getFluxLevel();
	float getCurrFlux();
	float getMaxFlux();
	float getMinFluxLevel();
	float getMinFlux();

	void setLightDHullOverlay();
	void setMediumDHullOverlay();
	void setHeavyDHullOverlay();


	boolean isJitterShields();
	void setJitterShields(boolean jitterShields);


	boolean isInvalidTransferCommandTarget();
	void setInvalidTransferCommandTarget(boolean invalidTransferCommandTarget);

	void clearDamageDecals();


	void syncWithArmorGridState();
	void syncWeaponDecalsWithArmorDamage();


	boolean isDirectRetreat();

	void setRetreating(boolean retreating, boolean direct);


	boolean isLiftingOff();


	void setVariantForHullmodCheckOnly(ShipVariantAPI variant);


	Vector2f getShieldCenterEvenIfNoShield();
	float getShieldRadiusEvenIfNoShield();


	FleetMemberAPI getFleetMember();


	Vector2f getShieldTarget();
	void setShieldTargetOverride(float x, float y);


	
	/**
	 * Will be null if no listeners added.
	 * @return
	 */
	CombatListenerManagerAPI getListenerManager();
	
	void addListener(Object listener);
	void removeListener(Object listener);
	void removeListenerOfClass(Class<?> c);
	boolean hasListener(Object listener);
	boolean hasListenerOfClass(Class<?> c);
	<T> List<T> getListeners(Class<T> c);


	Object getParamAboutToApplyDamage();
	void setParamAboutToApplyDamage(Object param);


	float getFluxBasedEnergyWeaponDamageMultiplier();


	void setName(String name);


	void setHulk(boolean isHulk);


	void setCaptain(PersonAPI captain);
	float getShipExplosionRadius();


	void setCircularJitter(boolean circular);


	float getExtraAlphaMult();


	void setAlphaMult(float alphaMult);
	float getAlphaMult();


	void setAnimatedLaunch();


	void setLaunchingShip(ShipAPI launchingShip);


	boolean isNonCombat(boolean considerOrders);


	float findBestArmorInArc(float facing, float arc);
	float getAverageArmorInSlice(float direction, float arc);


	void setHoldFire(boolean holdFire);


	void cloneVariant();


	void setTimeDeployed(float timeDeployed);


	void setFluxVentTextureSheet(String textureId);
	String getFluxVentTextureSheet();

	float getAimAccuracy();


	float getForceCarrierTargetTime();
	void setForceCarrierTargetTime(float forceCarrierTargetTime);
	float getForceCarrierPullBackTime();
	void setForceCarrierPullBackTime(float forceCarrierPullBackTime);
	ShipAPI getForceCarrierTarget();
	void setForceCarrierTarget(ShipAPI forceCarrierTarget);


	void setWing(FighterWingAPI wing);
	float getExplosionScale();
	void setExplosionScale(float explosionScale);
	Color getExplosionFlashColorOverride();
	void setExplosionFlashColorOverride(Color explosionFlashColorOverride);
	Vector2f getExplosionVelocityOverride();
	void setExplosionVelocityOverride(Vector2f explosionVelocityOverride);


	void setNextHitHullDamageThresholdMult(float threshold, float multBeyondThreshold);


	boolean isEngineBoostActive();


	void makeLookDisabled();


	void setExtraAlphaMult2(float transparency);


	float getExtraAlphaMult2();

}






