package com.fs.starfarer.api.combat;

import java.util.List;

import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.FormationType;
import com.fs.starfarer.api.loading.WingRole;

public interface FighterWingAPI {

	public static class ReturningFighter {
		public ShipAPI fighter;
		public FighterLaunchBayAPI bay;
		public ReturningFighter(ShipAPI fighter, FighterLaunchBayAPI bay) {
			this.fighter = fighter;
			this.bay = bay;
		}
	}
	
	/**
	 * Which launch bay the fighter should be returning to, if it's returning.
	 * @param fighter
	 * @return
	 */
	FighterWingAPI.ReturningFighter getReturnData(ShipAPI fighter);
	
	
	int getWingOwner();
	void setWingOwner(int owner);
	
	List<ShipAPI> getWingMembers();
	
	WingRole getRole();
	FormationType getFormation();

	boolean isDestroyed();

	String getWingId();

	boolean isAlly();

	ShipAPI getLeader();

	FighterLaunchBayAPI getSource();

	ShipAPI getSourceShip();

	float getRange();

	FighterWingSpecAPI getSpec();

	
	boolean isReturning(ShipAPI fighter);
	void orderReturn(ShipAPI fighter);
	void stopReturning(ShipAPI fighter);


	List<ReturningFighter> getReturning();
}




