package com.fs.starfarer.api.combat;

import java.awt.Color;
import java.util.EnumSet;

import org.json.JSONObject;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;


public interface MissileAPI extends DamagingProjectileAPI {
	boolean isFizzling();
	void flameOut();
	
	ShipEngineControllerAPI getEngineController();
	
	
	/**
	 * Only should be called if the AI needs to be changed dynamically. Otherwise,
	 * use ModPlugin.pickMissileAI() instead.
	 * @param ai
	 */
	void setMissileAI(MissileAIPlugin ai);
	
	/**
	 * Does NOT return the same ai passed in to setShipAI(), but a wrapper around it.
	 * Can be used to save/restore the AI. 
	 * @return
	 */
	MissileAIPlugin getMissileAI();
	
	
	
	/**
	 * Should only be used by a MissileAIPlugin.
	 * @param command type of the command. Only movement-related ShipCommands have any effect.
	 */
	void giveCommand(ShipCommand command);
	
	boolean isFlare();
	
	SpriteAPI getSpriteAPI();
	
	float getAcceleration();
	float getMaxSpeed();
	float getMaxTurnRate();
	float getTurnAcceleration();
	
	float getMaxFlightTime();
	
	
	float getFlightTime();
	void setFlightTime(float flightTime);
	boolean isGuided();
	boolean isArmed();
	float getArmingTime();
	void setArmingTime(float armingTime);
	/**
	 * Setting to false has no effect on missiles whose dudProbabilityOnFlameout is false, as those are
	 * considered conceptually incapable of being duds/disarmed.
	 * @param armedWhileFizzling
	 */
	void setArmedWhileFizzling(boolean armedWhileFizzling);
	boolean isArmedWhileFizzling();
	
	/**
	 * Number of times a missile will ignore being hit by an system EMP *arc* (not emp damage) instead of flaming out.
	 * @param empResistance
	 */
	void setEmpResistance(int empResistance);
	int getEmpResistance();
	void decrEMPResistance();
	
	/**
	 * Useful for missiles that change position using a script. Call twice - once before the missile
	 * is moved, and once when it has moved to its new location.
	 */
	void interruptContrail();
	void fadeOutThenIn(float inDur);
	float getTimeSinceFizzling();
	void setTimeSinceFizzling(float timeSinceFizzling);
	
	/**
	 * Just data flags, WILL NOT actually make the missile a mine/adjust mine properties/etc.
	 * @return
	 */
	boolean isMine();
	
	/**
	 * Just data flags, WILL NOT actually make the missile a mine/adjust mine properties/etc.
	 * @return
	 */
	void setMine(boolean isMine);
	
	/**
	 * Just data flags, WILL NOT actually make the missile a mine/adjust mine properties/etc.
	 * @return
	 */
	void setMineExplosionRange(float mineExplosionRange);
	
	/**
	 * Just data flags, WILL NOT actually make the missile a mine/adjust mine properties/etc.
	 * @return
	 */
	boolean isMinePrimed();
	
	/**
	 * Just data flags, WILL NOT actually make the missile a mine/adjust mine properties/etc.
	 * @return
	 */
	float getMineExplosionRange();
	
	/**
	 * Just data flags, WILL NOT actually make the missile a mine/adjust mine properties/etc.
	 * @return
	 */
	void setMinePrimed(boolean isMinePrimed);
	
	/**
	 * Just data flags, WILL NOT actually make the missile a mine/adjust mine properties/etc.
	 * @return
	 */
	float getUntilMineExplosion();
	
	/**
	 * Just data flags, WILL NOT actually make the missile a mine/adjust mine properties/etc.
	 * @return
	 */
	void setUntilMineExplosion(float untilMineExplosion);
	void setJitter(Object source, Color color, float intensity, int copies, float range);
	void setJitter(Object source, Color color, float intensity, int copies, float minRange, float range);
	float getCurrentBaseAlpha();
	float getGlowRadius();
	void setGlowRadius(float glowRadius);
	boolean isRenderGlowAbove();
	void setRenderGlowAbove(boolean renderGlowAbove);
	void setShineBrightness(float brightness);
	
	boolean isMirv();
	float getMirvWarheadDamage();
	float getMirvWarheadEMPDamage();
	int getMirvNumWarheads();
	DamageType getMirvWarheadDamageType();
	JSONObject getBehaviorSpecParams();
	boolean isDecoyFlare();
	void resetEngineGlowBrightness();
	float getECCMChance();
	WeaponSpecAPI getWeaponSpec();
	void setWeaponSpec(String weaponId);
	
	
	/**
	 * Returns the AI that was passed in to setMissileAI(). getMissileAI() returns an internal wrapper around that.
	 * @return
	 */
	MissileAIPlugin getUnwrappedMissileAI();
	
	
	Object getParamAboutToApplyDamage();
	void setParamAboutToApplyDamage(Object param);
	MissileSpecAPI getSpec();
	EnumSet<CombatEngineLayers> getActiveLayers();
	
	boolean isForceAlwaysArmed();
	void setForceAlwaysArmed(boolean forceAlwaysArmed);
	boolean didDamage();
	boolean isNoMineFFConcerns();
	void setNoMineFFConcerns(boolean noFFConcerns);
	float getEccmChanceOverride();
	void setEccmChanceOverride(float eccmChanceOverride);
	float getEccmChanceBonus();
	void setEccmChanceBonus(float eccmChanceBonus);
	void setSource(ShipAPI source);
	ShipAPI getSourceAPI();
}

