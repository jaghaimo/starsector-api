package com.fs.starfarer.api.impl.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.skills.NeuralLinkScript;
import com.fs.starfarer.api.impl.campaign.skills.SupportDoctrine;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class NeuralInterface extends BaseHullMod {

	public static float SYSTEM_RESET_TIMEOUT_MULT = 2f;
	//public static float SYSTEM_RESET_TIMEOUT = 20f;
	public static final String SYSTEM_RESET_TIMEOUT_KEY = "neural_interface_reset_timeout";
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		String key = SYSTEM_RESET_TIMEOUT_KEY;
//		Float timeout = (Float) Global.getCombatEngine().getCustomData().get(key);
//		if (timeout == null) timeout = 0f;
//		
//		if (ship == Global.getCombatEngine().getPlayerShip()) {
//			timeout -= amount;
//			if (timeout < 0) timeout = 0f;
//			Global.getCombatEngine().getCustomData().put(key, timeout);
//			//System.out.println("NI timeout: " + timeout);
//		}
		
		Float timeout = (Float) ship.getCustomData().get(key);
		if (timeout == null) timeout = 0f;
		timeout -= amount;
		if (timeout < 0) timeout = 0f;
		ship.setCustomData(key, timeout);
		//System.out.println("NI timeout: " + timeout);
		
		if (ship == Global.getCombatEngine().getPlayerShip()) {
			if (ship.getCustomData().containsKey(NeuralLinkScript.TRANSFER_COMPLETE_KEY)) {
				ShipSystemAPI system = ship.getSystem();
				if (system != null && timeout <= 0) {
					boolean didSomething = false;
					//float maxTimeout = 5f;
					float maxTimeout = 0f;
					if (system.getCooldownRemaining() > 0f && system.isCoolingDown()) {
						maxTimeout = Math.max(system.getCooldownRemaining(), maxTimeout);
						system.setCooldownRemaining(0);
						didSomething = true;
					}
					if (system.getAmmo() < system.getMaxAmmo() && system.getAmmoPerSecond() > 0) {
						system.setAmmo(system.getAmmo() + 1);
						didSomething = true;
						maxTimeout = Math.max(1f / system.getAmmoPerSecond() * (1f - system.getAmmoReloadProgress()), maxTimeout);
					}
					//if (didSomething) {
					if (maxTimeout > 0) {
						maxTimeout *= SYSTEM_RESET_TIMEOUT_MULT;
						//Global.getCombatEngine().getCustomData().put(key, SYSTEM_RESET_TIMEOUT);
						ship.setCustomData(key, maxTimeout);
					}
				}
				ship.removeCustomData(NeuralLinkScript.TRANSFER_COMPLETE_KEY);
			}
		} else {
			ship.removeCustomData(NeuralLinkScript.TRANSFER_COMPLETE_KEY);
		}
	}
	

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.HAS_NEURAL_LINK).modifyFlat(id, 1f);
		
		stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).unmodify(SupportDoctrine.SUPPORT_DOCTRINE_DP_REDUCTION_ID);
		
//		if (stats.getFleetMember() != null && stats.getFleetMember().getCaptain() != null) {
//			PersonAPI p = stats.getFleetMember().getCaptain();
//			if (p.isDefault() && Misc.isAutomated(stats.getFleetMember())) {
//				p.getMemoryWithoutUpdate().set(AICoreOfficerPluginImpl.AUTOMATED_POINTS_MULT, 
//											   AICoreOfficerPluginImpl.BETA_MULT);
//			}
//			
//		}
		
		//stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_FLAT).modifyFlat(id, (Float) mag.get(hullSize));
		//stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_FLAT).modifyFlat(id, (Float) mag.get(hullSize));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)NeuralLinkScript.INSTANT_TRANSFER_DP;
		return null;
	}
	
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color g = Misc.getGrayColor();
		
		tooltip.addSectionHeading("Neural system reset", Alignment.MID, opad);
		tooltip.addPara("After the transfer is complete, the target ship's system cooldown (if any) will be reset, "
				+ "and if the ship system uses charges, it will gain an additonal charge. This effect operates on "
				+ "a cooldown equal to %s the cooldown/charge regeneration time saved by it.", opad, h,
				"" + (int)SYSTEM_RESET_TIMEOUT_MULT + Strings.X);
		
//		tooltip.addPara("If installed on an automated ship, increases its " 
//				+ "\"automated ship points\" value as if a Beta Core was installed on the ship.", opad);
		
		if (Global.getCurrentState() == GameState.CAMPAIGN) {
			if (Global.getSector().getPlayerStats().getDynamic().getMod(Stats.HAS_NEURAL_LINK).computeEffective(0f) <= 0f) {
				tooltip.addPara("Requires the Neural Link skill to function", Misc.getNegativeHighlightColor(), opad);
			}
		}
		
		if (isForModSpec || ship == null) return;
		
		String control = Global.getSettings().getControlStringForEnumName(NeuralLinkScript.TRANSFER_CONTROL);
		String desc = Global.getSettings().getControlDescriptionForEnumName(NeuralLinkScript.TRANSFER_CONTROL);
		tooltip.addPara("Use the \"" + desc + "\" control [" + control + "] to switch between ships.", opad,
				g, h, control);
		
		
	}
	
	public boolean isApplicableToShip(ShipAPI ship) {
		if (Misc.isAutomated(ship)) {
			return false;
		}
		if (ship.getHullSpec().getHints().contains(ShipTypeHints.NO_NEURAL_LINK)) {
			return false;
		}
		return true;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (Misc.isAutomated(ship)) {
			return "Can not be installed on automated ships, install Neural Integrator instead";
		}
		if (ship.getHullSpec().getHints().contains(ShipTypeHints.NO_NEURAL_LINK)) {
			return "Can not be installed on this ship";
		}
		return null;
	}
}




