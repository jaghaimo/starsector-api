package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.PerseanLeagueMembership;
import com.fs.starfarer.api.impl.campaign.intel.events.EstablishedPolityScript;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class EstablishedPolity extends BaseMarketConditionPlugin {

	public static float ACCESSIBILITY_BONUS = 0.1f;
	
	public static String NAME_BASE = "Established Polity";
	public static String NAME_LEAGUE = "League development programs";
	
	public EstablishedPolity() {
	}

	public void apply(String id) {
		String text = Misc.ucFirst(getName().toLowerCase());
//		if (PerseanLeagueMembership.isLeagueMember()) {
//			text = "New Persean League member";
//		}
		market.getAccessibilityMod().modifyFlat(id, ACCESSIBILITY_BONUS, text);
	}

	public void unapply(String id) {
		market.getAccessibilityMod().unmodifyFlat(id);
	}
	
	@Override
	public void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		EstablishedPolityScript script = EstablishedPolityScript.get();
		if (script == null) return;
		
		Color h = Misc.getHighlightColor();
		
		float opad = 10f;
		
		if (PerseanLeagueMembership.isLeagueMember()) {
			tooltip.addPara("Your colonies are recently joined members of the Persean League, resulting in an increased "
					+ "flow of commerce.", opad);
		} else {
			tooltip.addPara("You've proven the independence and strength of your colonies by defeating a "
					+ "Persean League force sent against you, and are viewed as a stable trading partner.", opad);
		}
		
		tooltip.addPara("%s accessibility.", 
				opad, h,
				"+" + (int)Math.round(ACCESSIBILITY_BONUS * 100f) + "%");
	}

	@Override
	public boolean hasCustomTooltip() {
		return true;
	}


	@Override
	public String getName() {
		if (PerseanLeagueMembership.isLeagueMember()) {
			return NAME_LEAGUE;
		}
		return NAME_BASE;
	}


	@Override
	public String getIconName() {
		if (PerseanLeagueMembership.isLeagueMember()) {
			return Global.getSettings().getSpriteName("events", "new_league_member");
		}
		return super.getIconName();
	}
	
	

}





