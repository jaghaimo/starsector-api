package com.fs.starfarer.api.fleet;

import java.util.Random;


public interface FleetMemberStatusAPI {
	/**
	 * Total hull damage (as a fraction) since resetDamageTaken() was last called.
	 * @return
	 */
	float getHullDamageTaken();
	
	/**
	 * From 0 to 1.
	 * @return
	 */
	float getHullFraction();
	
	/**
	 * After this call, getHullDamageTaken() will return 0 until the ship takes more damage.
	 */
	void resetDamageTaken();
	
	
	/**
	 * Repairs the disabled ship's hull a few percentage points.
	 */
	void repairDisabledABit();
	
	
	void disable();
	void repairFully();
	void repairFullyNoNewFighters();
	
	void repairFraction(float fraction);
	
	/**
	 * Applies damage in a random location on the hull. In the case of a fighter wing, first picks a random wing member.
	 * @param hitStrength
	 */
	void applyDamage(float hitStrength);
	
	
	/**
	 * Applied to a random location on the hull, deals guaranteed amount of hull damage, expressed as a fraction of the maximum hull value.
	 * 
	 * 
	 *  
	 * @param fraction
	 */
	void applyHullFractionDamage(float fraction);
	
	
	/**
	 * Useful for applying damage to specific fighters.
	 * 
	 * @param fraction
	 * @param index
	 */
	void applyHullFractionDamage(float fraction, int index);
	
	/**
	 * @return 1, or number of fighters in the wing, or number of modules including the base.
	 */
	int getNumStatuses();

	void setHullFraction(float fraction);

	void repairArmorAllCells(float fraction);
	void repairHullFraction(float fraction);

	float getArmorDamageTaken();

	void setRandom(Random random);

	Random getRandom();

	void setDetached(int index, Boolean detached);
	void setHullFraction(int index, float hullFraction);
	float getHullFraction(int index);
	boolean isDetached(int index);

	boolean needsRepairs();

	void setPermaDetached(int index, Boolean detached);

	boolean isPermaDetached(int index);

	void resetAmmoState();

	void applyDamage(float hitStrength, float forceHullFractionDamage);

}
