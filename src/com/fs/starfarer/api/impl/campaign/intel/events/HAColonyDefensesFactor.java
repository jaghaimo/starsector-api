package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class HAColonyDefensesFactor extends BaseEventFactor {
	
	public static float PATROL_HQ_MULT = 0.9f;
	public static float MILITARY_BASE_MULT = 0.7f;
	public static float HIGH_COMMAND_MULT = 0.5f;
	
	public static class HAColonyDefenseData {
		public MarketAPI market;
		public Industry industry;
		public float mult = 1f;
	}
	
	public HAColonyDefensesFactor() {
	}

	@Override
	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				Color h = Misc.getHighlightColor();
				float opad = 10f;
				
				
				tooltip.addPara("The presence of military infrastructure slows down event progress, but does not actually stop or reverse it. "
						+ "The highest level military structure present on any of your colonies determines the multiplier.", 0f);
				
				HAColonyDefenseData data = getDefenseData(null);
				
				tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 
									20f, "Infrastructure", 200f, "Multiplier", 100f);
				
				Color c = Misc.getGrayColor();
				Color c2 = Misc.getGrayColor();
				if (data.industry != null && data.industry.getId().equals(Industries.PATROLHQ)) {
					c = Misc.getHighlightColor();
					c2 = Misc.getPositiveHighlightColor();
				}
				tooltip.addRow(c, "Patrol HQ", c2, Strings.X + Misc.getRoundedValueMaxOneAfterDecimal(PATROL_HQ_MULT));
				
				c = Misc.getGrayColor();
				c2 = Misc.getGrayColor();
				if (data.industry != null && data.industry.getId().equals(Industries.MILITARYBASE)) {
					c = Misc.getHighlightColor();
					c2 = Misc.getPositiveHighlightColor();
				}
				tooltip.addRow(c, "Military Base", c2, Strings.X + Misc.getRoundedValueMaxOneAfterDecimal(MILITARY_BASE_MULT));
				
				c = Misc.getGrayColor();
				c2 = Misc.getGrayColor();
				if (data.industry != null && data.industry.getId().equals(Industries.HIGHCOMMAND)) {
					c = Misc.getHighlightColor();
					c2 = Misc.getPositiveHighlightColor();
				}
				tooltip.addRow(c, "High Command", c2, Strings.X + Misc.getRoundedValueMaxOneAfterDecimal(HIGH_COMMAND_MULT));
				
				tooltip.addTable("None", 0, opad);
				tooltip.addSpacer(5f);
				
				if (data.industry != null && data.market != null) {
					tooltip.addPara("You have a %s at %s.", opad, h, 
							data.industry.getCurrentName(), data.market.getName());
				}
			}
		};
	}
	
	
	@Override
	public boolean shouldShow(BaseEventIntel intel) {
		//HAColonyDefenseData data = getDefenseData(intel);
		return true;
	}


	public float getAllProgressMult(BaseEventIntel intel) {
		HAColonyDefenseData data = getDefenseData(intel);
		return data.mult;
	}
	
	
	@Override
	public Color getProgressColor(BaseEventIntel intel) {
		HAColonyDefenseData data = getDefenseData(intel);
		if (data.mult < 1) {
			return Misc.getPositiveHighlightColor();
		} else if (data.mult > 1) {
			return Misc.getNegativeHighlightColor();
		}
		return Misc.getHighlightColor(); // no text anyway
	}

	@Override
	public String getProgressStr(BaseEventIntel intel) {
		HAColonyDefenseData data = getDefenseData(intel);
		if (data.mult != 1) {
			return Strings.X + Misc.getRoundedValueMaxOneAfterDecimal(data.mult);
		}
		return "";
	}

	public String getDesc(BaseEventIntel intel) {
		HAColonyDefenseData data = getDefenseData(intel);
		if (data.industry == null) {
			return "Military infrastructure";
		}
		return data.industry.getCurrentName();
	}	

	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getDefenseData(intel).market == null) {
			return Misc.getGrayColor();
		}
		return super.getDescColor(intel);
	}

	public HAColonyDefenseData getDefenseData(BaseEventIntel intel) {
		HAColonyDefenseData best = new HAColonyDefenseData();
		
		List<MarketAPI> markets = Misc.getPlayerMarkets(false);
		for (MarketAPI market : markets) {
			float mult = 1f;
			Industry industry = null;
			if (market.hasFunctionalIndustry(Industries.PATROLHQ)) {
				mult = PATROL_HQ_MULT;
				industry = market.getIndustry(Industries.PATROLHQ);
			}
			if (Misc.isMilitary(market)) {
				if (market.hasFunctionalIndustry(Industries.HIGHCOMMAND)) {
					mult = HIGH_COMMAND_MULT;
					industry = market.getIndustry(Industries.HIGHCOMMAND);
				} else {
					mult = MILITARY_BASE_MULT;
					industry = market.getIndustry(Industries.MILITARYBASE);
				}
			}
			
			if (industry != null && mult < best.mult) {
				best.market = market;
				best.industry = industry;
				best.mult = mult;
			}
		}
		
		return best;
	}
	
}
