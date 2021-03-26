package com.fs.starfarer.api.fleet;

import java.util.List;

public interface RepairTrackerAPI {
	
	public static class CREvent {
		public float crAmount;
		public String text;
		public float elapsed = 0f;
		public String id;
		public CREvent(float crAmount, String text) {
			this.crAmount = crAmount;
			this.text = text;
		}
		public float getCrAmount() {
			return crAmount;
		}
		public String getText() {
			return text;
		}
		
		public void advance(float days) {
			elapsed += days;
		}
		
		public boolean isExpired() {
			return elapsed > 7;
		}
		public float getElapsed() {
			return elapsed;
		}
	}
	
	/**
	 * @param crChange from -1 to 1
	 * @param description shows up in the CR tooltip
	 */
	void applyCREvent(float crChange, String description);
	
	/**
	 * Returned value is modified by crew fraction.
	 * @return from 0 to 1.
	 */
	float getCR();
	
	/**
	 * getCR() will return this value, modified by the crew fraction
	 * @param cr from 0 to 1.
	 */
	void setCR(float cr);

	float getSuppliesFromScuttling();
	float getFuelFromScuttling();
	
	
	float getRecoveryRate();
	float getDecreaseRate();

	/**
	 * @return 0 to 1
	 */
	float getMaxCR();
	
	
	/**
	 * Current CR without the crew understrength multiplier, if any.
	 * @return
	 */
	float getBaseCR();
	
	
	List<CREvent> getRecentEvents();
	
	/**
	 * The "event" for gradual supply loss over the last week is not included in the return value
	 * of getRecentEvents().
	 * @return
	 */
	CREvent getNoSupplyCRLossEvent();

	
	boolean isSuspendRepairs();
	void setSuspendRepairs(boolean suspendRepairs);
	
	
	//void performRepairsUsingSupplies(float supplies);
	void performRepairsFraction(float fraction);
	
	float getRemainingRepairTime();
	
	/**
	 * Including both hull and armor.
	 * @return 0 to 1
	 */
	float computeRepairednessFraction();

//	boolean isLogisticalPriority();
//	void setLogisticalPriority(boolean priorizeRepairs);

	boolean isMothballed();
	void setMothballed(boolean mothballed);
	
	boolean isCrashMothballed();
	void setCrashMothballed(boolean crashMothballed);

	float getRepairRatePerDay();

	/**
	 * Uses id to apply subsequent CR changes to the same "recent event".
	 * Useful for gradual CR loss, i.e. from star corona.
	 * @param crChange
	 * @param id
	 * @param description
	 */
	void applyCREvent(float crChange, String id, String description);

	float getHeavyMachineryFromScuttling();

	float getCRPriorToMothballing();
	void setCRPriorToMothballing(float crPriorToMothballing);

}
