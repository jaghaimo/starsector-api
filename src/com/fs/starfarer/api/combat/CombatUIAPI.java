package com.fs.starfarer.api.combat;


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
}
