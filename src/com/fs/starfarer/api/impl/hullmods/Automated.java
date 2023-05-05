package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class Automated extends BaseHullMod {

	public static float MAX_CR_PENALTY = 1f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMinCrewMod().modifyMult(id, 0);
		stats.getMaxCrewMod().modifyMult(id, 0);
		
		if (isInPlayerFleet(stats) && !isAutomatedNoPenalty(stats)) {
			stats.getMaxCombatReadiness().modifyFlat(id, -MAX_CR_PENALTY, "Automated ship penalty");
		}
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.setInvalidTransferCommandTarget(true);
	}



	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)Math.round(MAX_CR_PENALTY * 100f) + "%";
		return null;
	}
	
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (isInPlayerFleet(ship)) {
			float opad = 10f;
			boolean noPenalty = isAutomatedNoPenalty(ship);
			String usually = "";
			if (noPenalty) usually = "usually ";
			tooltip.addPara("Automated ships " + usually + "require specialized equipment and expertise to maintain. In a " +
					"fleet lacking these, they're virtually useless, with their maximum combat " +
					"readiness being reduced by %s.", opad, Misc.getHighlightColor(),
					"" + (int)Math.round(MAX_CR_PENALTY * 100f) + "%");
			if (noPenalty) {
				tooltip.addPara("However, this ship was automated in a fashion that does not require special expertise "
						+ "to maintain. Some of the techniques used are poorly understood, likely dating to "
						+ "an earlier period.", opad);
			}
		}
	}
	
	public static boolean isAutomatedNoPenalty(MutableShipStatsAPI stats) {
		if (stats == null) return false;
		FleetMemberAPI member = stats.getFleetMember();
		if (member == null) return false;
		return member.getHullSpec().hasTag(Tags.TAG_AUTOMATED_NO_PENALTY) ||
				member.getVariant().hasTag(Tags.TAG_AUTOMATED_NO_PENALTY);
	}
	
	public static boolean isAutomatedNoPenalty(ShipAPI ship) {
		if (ship == null) return false;
		FleetMemberAPI member = ship.getFleetMember();
		if (member == null) return false;
		return member.getHullSpec().hasTag(Tags.TAG_AUTOMATED_NO_PENALTY) ||
				member.getVariant().hasTag(Tags.TAG_AUTOMATED_NO_PENALTY);
	}
	
	public static boolean isAutomatedNoPenalty(FleetMemberAPI member) {
		if (member == null) return false;
		return member.getHullSpec().hasTag(Tags.TAG_AUTOMATED_NO_PENALTY) ||
				member.getVariant().hasTag(Tags.TAG_AUTOMATED_NO_PENALTY);
	}

}
