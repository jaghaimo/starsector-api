package com.fs.starfarer.api.impl.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.NeuralLinkScript;
import com.fs.starfarer.api.impl.campaign.skills.SupportDoctrine;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class NeuralInterface extends BaseHullMod {

	
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




