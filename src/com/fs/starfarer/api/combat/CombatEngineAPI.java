package com.fs.starfarer.api.combat;

import java.util.List;
import java.util.Map;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CombatDamageData;
import com.fs.starfarer.api.combat.EmpArcEntityAPI.EmpArcParams;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.CombatListenerManagerAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.mission.FleetSide;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface CombatEngineAPI {
	
	/**
	 * @return true if this battle is inside the campaign, false otherwise (i.e., mission or simulation, including in-campaign simulations).
	 * 
	 */
	boolean isInCampaign();
	
	boolean isInCampaignSim();
	
	CombatUIAPI getCombatUI();
	
	void setHyperspaceMode();
	
	/**
	 * Use getObjectives() instead.
	 */
	@Deprecated
	List<BattleObjectiveAPI> getAllObjectives();
	/**
	 * Use getShips() instead.
	 */
	@Deprecated
	List<ShipAPI> getAllShips();
	
	List<BattleObjectiveAPI> getObjectives();
	List<ShipAPI> getShips();
	List<MissileAPI> getMissiles();
	List<CombatEntityAPI> getAsteroids();
	List<BeamAPI> getBeams();
	/**
	 * Includes missiles.
	 */
	List<DamagingProjectileAPI> getProjectiles();
	
	boolean isEntityInPlay(CombatEntityAPI entity);
	
	FogOfWarAPI getFogOfWar(int owner);
	
	void removeEntity(CombatEntityAPI entity);
	
	CombatFleetManagerAPI getFleetManager(FleetSide side);
	CombatFleetManagerAPI getFleetManager(int owner);
	
	ShipAPI getPlayerShip();
	
	boolean isPaused();
	
	void endCombat(float delay);
	void setDoNotEndCombat(boolean doNotEndCombat);
	void endCombat(float delay, FleetSide winner);
	
	
	ViewportAPI getViewport();
	
	/**
	 * @param entity
	 * @param point Location the damage is dealt at, in absolute engine coordinates (i.e. *not* relative to the ship). MUST fall within the sprite of a ship, given its current location and facing, for armor to properly be taken into account.
	 * @param damageAmount
	 * @param damageType
	 * @param empAmount
	 * @param bypassShields Whether shields are ignored completely.
	 * @param dealsSoftFlux Whether damage dealt to shields results in soft flux.
	 * @param source Should be a ShipAPI if the damage ultimately attributed to it. Can also be null.
	 * @param playSound Whether a sound based on the damage dealt should be played.
	 */
	void applyDamage(CombatEntityAPI entity, Vector2f point, 
					 float damageAmount, DamageType damageType, float empAmount,
					 boolean bypassShields, boolean dealsSoftFlux, 
					 Object source, boolean playSound);
	
	void applyDamage(CombatEntityAPI entity, Vector2f point, 
			float damageAmount, DamageType damageType, float empAmount,
			boolean bypassShields, boolean dealsSoftFlux, 
			Object source);
	
	void applyDamage(Object damageModifierParam, CombatEntityAPI entity, Vector2f point, 
			float damageAmount, DamageType damageType, float empAmount,
			boolean bypassShields, boolean dealsSoftFlux, 
			Object source, boolean playSound);
	
	/**
	 * Particle with a somewhat brighter middle.
	 * @param brightness from 0 to 1
	 * @param duration in seconds
	 */
	
	void addHitParticle(Vector2f loc, Vector2f vel, float size, float brightness, float duration, Color color);
	/**
	 * Standard glowy particle.
	 * @param brightness from 0 to 1
	 * @param duration in seconds
	 */
	public void addSmoothParticle(Vector2f loc, Vector2f vel, float size, float brightness, float duration, Color color);
	
	/**
	 * Opaque smoke particle.
	 * @param brightness from 0 to 1
	 * @param duration in seconds
	 */
	public void addSmokeParticle(Vector2f loc, Vector2f vel, float size, float opacity, float duration, Color color);
	
	
	/**
	 * Purely visual.
	 */
	void spawnExplosion(Vector2f loc, Vector2f vel, Color color, float size, float maxDuration);
	
	/**
	 * @param size 0, 1, 2, or 3, with 3 being the largest.
	 * @param x location x
	 * @param y location y
	 * @param dx velocity x
	 * @param dy velocity y
	 * @return
	 */
	CombatEntityAPI spawnAsteroid(int size, float x, float y, float dx, float dy);
	
	
	void addFloatingText(Vector2f loc, String text, float size, Color color, CombatEntityAPI attachedTo, float flashFrequency, float flashDuration);
	void addFloatingDamageText(Vector2f loc, float damage, Color color, CombatEntityAPI attachedTo, CombatEntityAPI damageSource);
	
	
	
	/**
	 * @param ship The ship launching this projectile. Can be null.
	 * @param weapon Firing weapon. Can be null. If not, used for figuring out range/damage bonuses, etc.
	 * @param weaponId ID of the weapon whose projectile to use. Required.
	 * @param point Location where the projectile will spawn. Required.
	 * @param angle Initial facing, in degrees (0 = 3 o'clock, 90 = 12 o'clock). 
	 * @param shipVelocity Can be null. Otherwise, will be imparted to projectile.
	 * @return Projectile that was created, or null.
	 */
	public CombatEntityAPI spawnProjectile(ShipAPI ship, WeaponAPI weapon, String weaponId,
			  							   Vector2f point, float angle, Vector2f shipVelocity);
	
	
	/**
	 * @param damageSource Ship that's ultimately responsible for dealing the damage of this EMP arc. Can be null.
	 * @param point starting point of the EMP arc, in absolute engine coordinates.
	 * @param pointAnchor The entity the starting point should move together with, if any.
	 * @param empTargetEntity Target of the EMP arc. If it's a ship, it will randomly pick an engine nozzle/weapon to arc to. Can also pass in a custom class implementing CombatEntityAPI to visually target the EMP at a specific location (and not do any damage). 
	 * @param damageType
	 * @param damAmount
	 * @param empDamAmount
	 * @param maxRange Maximum range the arc can reach (useful for confining EMP arc targets to the area near point)
	 * @param impactSoundId Can be null.
	 * @param thickness Thickness of the arc (visual).
	 * @param fringe
	 * @param core
	 * @return
	 */
	public EmpArcEntityAPI spawnEmpArc(ShipAPI damageSource,
										Vector2f point,
										CombatEntityAPI pointAnchor,
										CombatEntityAPI empTargetEntity,
										DamageType damageType,
										float damAmount,
										float empDamAmount,
										float maxRange,
										String impactSoundId,
										float thickness,
										Color fringe, Color core);
	
	public EmpArcEntityAPI spawnEmpArc(ShipAPI damageSource,
			Vector2f point,
			CombatEntityAPI pointAnchor,
			CombatEntityAPI empTargetEntity,
			DamageType damageType,
			float damAmount,
			float empDamAmount,
			float maxRange,
			String impactSoundId,
			float thickness,
			Color fringe, Color core, EmpArcParams params);
	
	/**
	 * Same as spawnEmpArc, but goes through shields if they're blocking the line from the point to the chosen target.
	 */
	public EmpArcEntityAPI spawnEmpArcPierceShields(ShipAPI damageSource,
													Vector2f point, CombatEntityAPI pointAnchor,
													CombatEntityAPI empTargetEntity, DamageType damageType,
													float damAmount, float empDamAmount, float maxRange,
													String impactSoundId, float thickness, Color fringe, Color core);	
	public EmpArcEntityAPI spawnEmpArcPierceShields(ShipAPI damageSource,
			Vector2f point, CombatEntityAPI pointAnchor,
			CombatEntityAPI empTargetEntity, DamageType damageType,
			float damAmount, float empDamAmount, float maxRange,
			String impactSoundId, float thickness, Color fringe, Color core, EmpArcParams params);	
	
	
	float getMapWidth();
	float getMapHeight();
	
	/**
	 * BattleCreationContext used to initialize this battle.
	 * @return
	 */
	BattleCreationContext getContext();
	
	
	float getTotalElapsedTime(boolean includePaused);
	
	/**
	 * Does *not* return 0 if the game is paused; actually the *current* frame.
	 * 
	 * @return
	 */
	float getElapsedInLastFrame();
	
	/**
	 * Plugin has its init method called inside this method.
	 * @param plugin
	 */
	void addPlugin(EveryFrameCombatPlugin plugin);
	
	void removePlugin(EveryFrameCombatPlugin plugin);

	boolean isSimulation();
	boolean isMission();
	String getMissionId();


	void setPlayerShipExternal(ShipAPI ship);
	boolean isUIShowingDialog();
	boolean isUIShowingHUD();
	boolean isUIAutopilotOn();

	
	/**
	 * Time elapsed while both sides can see at least one enemy ship.
	 * @return
	 */
	float getElapsedInContactWithEnemy();

	boolean isFleetsInContact();

	void setSideDeploymentOverrideSide(FleetSide sideDeploymentOverrideSide);

	Map<String, Object> getCustomData();

	/**
	 * In the status list above the left side of the ship info widget in the bottom left.
	 * @param key
	 * @param spriteName
	 * @param title
	 * @param data
	 * @param isDebuff
	 */
	void maintainStatusForPlayerShip(Object key, String spriteName, String title, String data, boolean isDebuff);

	void setPaused(boolean paused);

	boolean playerHasNonAllyReserves();

	boolean playerHasAllyReserves();

	CombatDamageData getDamageData();

	MutableStat getTimeMult();

	void setMaxFleetPoints(FleetSide side, int fleetPoints);

	CombatNebulaAPI getNebula();

	boolean isInFastTimeAdvance();

	/**
	 * Should work now.
	 */
	CombatEntityAPI spawnProjectile(ShipAPI ship, WeaponAPI weapon,
			String weaponId, String projSpecId, Vector2f point, float angle,
			Vector2f shipVelocity);

	void updateStationModuleLocations(ShipAPI station);

	
	/**
	 * All combat entities.
	 * @return
	 */
	CollisionGridAPI getAllObjectGrid();
	/**
	 * Ships only.
	 * @return
	 */
	CollisionGridAPI getShipGrid();
	/**
	 * Missiles only.
	 * @return
	 */
	CollisionGridAPI getMissileGrid();
	/**
	 * Asteroids only.
	 * @return
	 */
	CollisionGridAPI getAsteroidGrid();

	DamagingProjectileAPI spawnDamagingExplosion(DamagingExplosionSpec spec, ShipAPI source, Vector2f location);
	DamagingProjectileAPI spawnDamagingExplosion(DamagingExplosionSpec spec, ShipAPI source, Vector2f location, boolean canDamageSource);

	/**
	 * 0 = player, 1 = enemy, 2 = player allies, no player ships left.
	 * @return
	 */
	int getWinningSideId();
	boolean isCombatOver();

	void removeObject(Object object);

	CombatEntityAPI addLayeredRenderingPlugin(CombatLayeredRenderingPlugin plugin);

	boolean isEnemyInFullRetreat();


	boolean isMissileAlive(MissileAPI missile);

	void spawnMuzzleFlashOrSmoke(ShipAPI ship, WeaponSlotAPI slot, WeaponSpecAPI spec, int barrel, float targetAngle);

	CollisionGridAPI getAiGridMissiles();
	CollisionGridAPI getAiGridShips();
	CollisionGridAPI getAiGridAsteroids();

	/**
	 * Visible (i.e. not under fog) or recently seen.
	 * @param owner
	 * @param other
	 * @return
	 */
	boolean isAwareOf(int owner, CombatEntityAPI other);

	/**
	 * Gives strafe left/right and accelerate forward/backward/decelerate commands to accomplish this.
	 * @param missile
	 * @param desiredHeading
	 * @param desiredSpeed
	 */
	void headInDirectionWithoutTurning(MissileAPI missile, float desiredHeading, float desiredSpeed);

	/**
	 * Gives strafe left/right and accelerate forward/backward/decelerate commands to accomplish this.
	 * @param ship
	 * @param desiredHeading
	 * @param desiredSpeed
	 */
	void headInDirectionWithoutTurning(ShipAPI ship, float desiredHeading, float desiredSpeed);

	/**
	 * accuracyFactor: 1 = best accuracy, 
	 * >1 (up to around 2 at most normally) poor accuracy, <1 = leading target too much, not used.
	 * @param from
	 * @param accuracyFactor
	 * @param to
	 * @param projSpeed
	 * @return
	 */
	Vector2f getAimPointWithLeadForAutofire(CombatEntityAPI from, float accuracyFactor, CombatEntityAPI to, float projSpeed);

	CombatListenerManagerAPI getListenerManager();

	void applyDamageModifiersToSpawnedProjectileWithNullWeapon(ShipAPI source, WeaponType type, boolean isBeam, DamageAPI damage);

	void addHitParticle(Vector2f loc, Vector2f vel, float size, float brightness, float durationIn, float totalDuration, Color color);

	/**
	 * Duration gets auto-computed.
	 * @param loc
	 * @param vel
	 * @param size
	 * @param brightness
	 * @param color
	 */
	void addHitParticle(Vector2f loc, Vector2f vel, float size, float brightness, Color color);

	EmpArcEntityAPI spawnEmpArcVisual(Vector2f from, CombatEntityAPI fromAnchor, Vector2f to, CombatEntityAPI toAnchor,
									  float thickness, Color fringe, Color core);
	EmpArcEntityAPI spawnEmpArcVisual(Vector2f from, CombatEntityAPI fromAnchor, Vector2f to, CombatEntityAPI toAnchor,
			float thickness, Color fringe, Color core, EmpArcParams params);

	void addSmoothParticle(Vector2f loc, Vector2f vel, float size,
			float brightness, float rampUpFraction, float totalDuration,
			Color color);

	void addNegativeParticle(Vector2f loc, Vector2f vel, float size, float rampUpFraction, float totalDuration, Color color);

	void addNebulaParticle(Vector2f loc, Vector2f vel, float size,
			float endSizeMult, float rampUpFraction,
			float fullBrightnessFraction, float totalDuration, Color color);

	void addNegativeNebulaParticle(Vector2f loc, Vector2f vel, float size,
			float endSizeMult, float rampUpFraction,
			float fullBrightnessFraction, float totalDuration, Color color);

	void addNebulaSmokeParticle(Vector2f loc, Vector2f vel, float size,
			float endSizeMult, float rampUpFraction,
			float fullBrightnessFraction, float totalDuration, Color color);

	boolean hasAttachedFloaty(CombatEntityAPI entity);

	void addNebulaParticle(Vector2f loc, Vector2f vel, float size,
			float endSizeMult, float rampUpFraction,
			float fullBrightnessFraction, float totalDuration, Color color,
			boolean expandAsSqrt);

	void addSwirlyNebulaParticle(Vector2f loc, Vector2f vel, float size,
			float endSizeMult, float rampUpFraction,
			float fullBrightnessFraction, float totalDuration, Color color,
			boolean expandAsSqrt);

	void addNegativeSwirlyNebulaParticle(Vector2f loc, Vector2f vel,
			float size, float endSizeMult, float rampUpFraction,
			float fullBrightnessFraction, float totalDuration, Color color);

	boolean isInPlay(Object object);

	void setCombatNotOverForAtLeast(float seconds);
	void setCombatNotOverFor(float seconds);
	float getCombatNotOverFor();

	void setCustomExit(String buttonTitle, String confirmString);
	String getCustomExitButtonTitle();
	String getCustomExitButtonConfirmString();

	void addFloatingTextAlways(Vector2f loc, String text, float size, Color color, CombatEntityAPI attachedTo,
			float flashFrequency, float flashDuration, float durInPlace, float durFloatingUp, float durFadingOut, float baseAlpha);

	WeaponAPI createFakeWeapon(ShipAPI ship, String weaponId);

	ShipAPI getShipPlayerIsTransferringCommandFrom();

	ShipAPI getShipPlayerIsTransferringCommandTo();

	ShipAPI getShipPlayerLastTransferredCommandTo();

	ShipAPI createFXDrone(ShipVariantAPI variant);

	void addEntity(CombatEntityAPI entity);

	void addNebulaSmoothParticle(Vector2f loc, Vector2f vel, float size, float endSizeMult, float rampUpFraction,
			float fullBrightnessFraction, float totalDuration, Color color);

	void addNebulaSmoothParticle(Vector2f loc, Vector2f vel, float size, float endSizeMult, float rampUpFraction,
			float fullBrightnessFraction, float totalDuration, Color color, boolean expandAsSqrt);

	Color getBackgroundColor();
	void setBackgroundColor(Color backgroundColor);
	boolean isBackgroundGlowColorNonAdditive();
	void setBackgroundGlowColorNonAdditive(boolean backgroundGlowColorNonAdditive);
	void setBackgroundGlowColor(Color backgroundGlowColor);
	Color getBackgroundGlowColor();
	boolean isRenderStarfield();
	void setRenderStarfield(boolean renderStarfield);

	void setShipPlayerLastTransferredCommandTo(ShipAPI ship);

	void spawnMuzzleFlashOrSmoke(ShipAPI ship, Vector2f point, WeaponSpecAPI spec, float targetAngle);

	boolean isInMissionSim();

	boolean isShipAlive(ShipAPI ship);

	void spawnDebrisSmall(Vector2f loc, Vector2f vel, int num, float facing, float spread, float minVel, float velRange,
			float maxRotation);
	void spawnDebrisMedium(Vector2f loc, Vector2f vel, int num, float facing, float spread, float minVel,
			float velRange, float maxRotation);
	void spawnDebrisLarge(Vector2f loc, Vector2f vel, int num, float facing, float spread, float minVel, float velRange,
			float maxRotation);

	void addFloatingDamageText(Vector2f loc, float damage, float spread, Color color, CombatEntityAPI to,
									CombatEntityAPI source);

	MissileAIPlugin createProximityFuseAI(MissileAPI missile);
	String getBackgroundSpriteName();

	boolean isInEngine(ShipAPI ship);

	boolean hasPluginOfClass(Class c);

	void applyImpact(Vector2f vel, float impact, CombatEntityAPI target, Vector2f point);

	void playShipExplosionSound(ShipAPI ship);

	CombatEntityAPI spawnAsteroid(String spriteName, float x, float y, float dx, float dy, boolean fromRing);
	
	//float getElapsedInCurrentFrame();

}






