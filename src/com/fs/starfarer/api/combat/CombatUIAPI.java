package com.fs.starfarer.api.combat;

import java.util.List;

import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface CombatUIAPI {
	/**
	 * Params define the message segments.
	 * 
	 * Can be:
	 * Strings - for text
	 * FleetMemberAPI, DeployedFleetMemberAPI, ShipAPI - for ship/wing icons
	 * BattleObjectiveAPI - for objective icons
	 * java.awt.Color - to set the color of subsequent text segments
	 * 
	 * 
	 * 
	 * @param newLineIndentIndex indentation after line wrap, if any occurs, in *message segments*.
	 * @param params
	 */
	void addMessage(int newLineIndentIndex, Object ... params);

	boolean isShowingCommandUI();

	float getCommandUIOpacity();

	CombatEntityAPI getEntityToFollowV2();

	boolean isShowingDeploymentDialog();
	List<FleetMemberAPI> getCurrentlySelectedInFleetDeploymentDialog();

	boolean isAutopilotOn();

	void setDisablePlayerShipControlOneFrame(boolean disablePlayerShipControlOneFrame);

	boolean isDisablePlayerShipControlOneFrame();

	void setShipInfoFanOutBrightness(float b);

	void reFanOutShipInfo();

	void hideShipInfo();

	boolean areWeaponArcsOn();

	List<ShipAPI> getAllTargetReticleTargets();
	ShipAPI getMainTargetReticleTarget();

	boolean isStrafeToggledOn();
	void setStrafeToggledOn(boolean strafeToggledOn);
}
