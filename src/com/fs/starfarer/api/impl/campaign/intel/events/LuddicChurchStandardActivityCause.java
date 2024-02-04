package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class LuddicChurchStandardActivityCause extends BaseHostileActivityCause2 {

	public static float MAX_MAG = 0.3f;
	
	public LuddicChurchStandardActivityCause(HostileActivityEventIntel intel) {
		super(intel);
	}

	@Override
	public TooltipCreator getTooltip() {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("Negotiating with the Luddic Church about controlling the "
						+ "immigration of the faithful to your colonies "
						+ "is likely to get this harassment to stop. If "
						+ "left unchecked, this low-grade conflict will eventually come to a head and is likely to "
						+ "be resolved one way or another.", 0f, Misc.getHighlightColor(),
						"Negotiating");
				
				FactionAPI f = Global.getSector().getFaction(Factions.LUDDIC_CHURCH);
				tooltip.addPara("Event progress value is based on the number and size of colonies "
						+ "with the \"Luddic Majority\" condition.", opad, f.getBaseUIColor(),
						"Luddic Majority");
			}
		};
	}
	
	public int getProgress() {
		if (LuddicChurchHostileActivityFactor.isDefeatedExpedition()) return 0;
		if (LuddicChurchHostileActivityFactor.isMadeDeal()) return 0;


		int score = 0;
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			if (!market.hasCondition(Conditions.LUDDIC_MAJORITY)) continue;
			
			int size = market.getSize();
			score += size;
//			if (size <= 4) {
//				score += size * 2;
//			} else if (size == 5) {
//				score += size * 2 + 2;
//			} else if (size == 6) {
//				score += size * 2 + 4;
//			} else {
//				score += size * 3;
//			}
		}
		
		int progress = score;
		
		return progress;
	}
	
	
	public String getDesc() {
		return "Colonies with a Luddic Majority";
	}

	
	public float getMagnitudeContribution(StarSystemAPI system) {
		//if (KantaCMD.playerHasProtection()) return 0f;
		if (getProgress() <= 0) return 0f;
		
		float total = 0f;
		for (MarketAPI market : Misc.getMarketsInLocation(system, Factions.PLAYER)) {
			if (!market.hasCondition(Conditions.LUDDIC_MAJORITY)) continue;
			total += market.getSize();
		}
		
		float f = total / 6f;
		if (f > 1f) f = 1f;
		
		return f * MAX_MAG; 
	}

}








