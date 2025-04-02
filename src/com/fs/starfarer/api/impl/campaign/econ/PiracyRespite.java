package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;

import com.fs.starfarer.api.impl.campaign.intel.events.PiracyRespiteScript;
import com.fs.starfarer.api.impl.campaign.rulecmd.KantaCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class PiracyRespite extends BaseMarketConditionPlugin {

	public static boolean NEW_MODE = true;
	//public static float ACCESSIBILITY_BONUS = 0.3f;
	public static float ACCESSIBILITY_BONUS = 0.1f;
	public static float ACCESSIBILITY_BONUS_KANTA = 0.1f;
	
	public PiracyRespite() {
	}

	
	public static float getBonus() {
		float bonus = ACCESSIBILITY_BONUS;
		if (KantaCMD.playerHasProtection()) {
			bonus += ACCESSIBILITY_BONUS_KANTA;
		}
		return bonus;
	}

	public void apply(String id) {
		if (NEW_MODE) return;
		String text = Misc.ucFirst(getName().toLowerCase());
		if (KantaCMD.playerHasProtection()) {
			text += " (with Kanta's Protection)";
		}
		market.getAccessibilityMod().modifyFlat(id, getBonus(), text);
	}

	public void unapply(String id) {
		if (NEW_MODE) return;
		market.getAccessibilityMod().unmodifyFlat(id);
	}
	
	@Override
	public void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		PiracyRespiteScript script = PiracyRespiteScript.get();
		if (script == null) return;
		
		Color h = Misc.getHighlightColor();
		
		float opad = 10f;
		
		if (NEW_MODE) {
			if (KantaCMD.playerHasProtection()) {
				tooltip.addPara("Your colonies have %s, and pirates are wary of "
						+ "attacking trade fleets serving them lest they attract her wrath. "
						+ "Shipping disruptions from piracy are virtually eliminated.", opad, Misc.getPositiveHighlightColor(),
						"Kanta's Protection");
			} else {
				tooltip.addPara("You've defeated a large armada sent against your colonies, and pirates are wary of "
						+ "attacking trade fleets serving them, "
						+ "resulting in a greatly reduced number of shipping disruptions.", opad);
			}
		} else {
			int rem = Math.round(script.getDaysRemaining());
			String days = rem == 1 ? "day" : "days";
	
			if (KantaCMD.playerHasProtection()) {
				tooltip.addPara("Your colonies have %s, resulting in an "
						+ "increased accessibility bonus.", opad, Misc.getPositiveHighlightColor(),
						"Kanta's Protection");
			}
			
			if (rem >= 0) {
				tooltip.addPara("%s accessibility (%s " + days + " remaining).", 
						opad, h,
						"+" + (int)Math.round(getBonus() * 100f) + "%", "" + rem);
			} else {
				tooltip.addPara("%s accessibility.", 
						opad, h,
						"+" + (int)Math.round(getBonus() * 100f) + "%");
			}
		}
	}

	@Override
	public boolean hasCustomTooltip() {
		return true;
	}

}





