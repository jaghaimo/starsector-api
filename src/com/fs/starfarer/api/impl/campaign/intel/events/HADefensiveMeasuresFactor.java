package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.Stage;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;
import com.fs.starfarer.api.util.Misc;

public class HADefensiveMeasuresFactor extends BaseEventFactor {
	
	public static int PATROL_HQ_POINTS = 5;
	public static float MILITARY_SIZE_MULT = 2;
	public static float HIGH_COMMAND_SIZE_MULT = 3;
	
	public HADefensiveMeasuresFactor() {
	}

	@Override
	public TooltipCreator getMainRowTooltip() {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				Color h = Misc.getHighlightColor();
				float opad = 10f;
				
				tooltip.addPara("Defensive measures taken by your colonies.", 0f);
			}
		};
	}
	
	
	@Override
	public boolean shouldShow(BaseEventIntel intel) {
		MarketAPI biggest = getBiggestColony();
		MarketAPI mil = getBestMilitaryMarket();
		return biggest != null || mil != null;
	}

	@Override
	public void addExtraRows(TooltipMakerAPI info, BaseEventIntel intel) {
		MarketAPI biggest = getBiggestColony();
		MarketAPI mil = getBestMilitaryMarket();
		Color tc = Misc.getTextColor();
		
		if (biggest != null) {
			int p = getColonyIncreasedDefensesScore(intel, biggest);
			if (p != 0) {
			//info.addRowWithGlow(Alignment.LMID, tc, "    Largest colony",
				info.addRowWithGlow(Alignment.LMID, tc, "    Increased defenses",
					    Alignment.RMID, intel.getProgressColor(p), "" + p);
				
				TooltipCreator t = new BaseFactorTooltip() {
					public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
						float opad = 10f;
						tooltip.addPara("Based on the size and stability of the largest colony "
								+ "under your control. If multiple colonies have the same size, the one with higher "
								+ "stability is used.", 0f);
						MarketAPI biggest = getBiggestColony();
						if (biggest != null && biggest.getStarSystem() != null) {
							tooltip.addPara("Biggest colony: %s, size: %s, stability: %s", opad, Misc.getHighlightColor(), 
									biggest.getName(),
									"" + biggest.getSize(),
									"" + (int) biggest.getStabilityValue());
							float w = tooltip.getWidthSoFar();
							float h2 = Math.round(w / 1.6f);
							tooltip.addSectorMap(w, h2, biggest.getStarSystem(), opad);
						}
					}
				};
				info.addTooltipToAddedRow(t, TooltipLocation.RIGHT, false);
			}
		}
		
		int p = getMilitaryScore(mil);
		if (mil != null && mil.hasFunctionalIndustry(Industries.HIGHCOMMAND)) {
			info.addRowWithGlow(Alignment.LMID, tc, "    High Command",
				    Alignment.RMID, intel.getProgressColor(p), "" + p);
		} else if (Misc.isMilitary(mil)) {
			info.addRowWithGlow(Alignment.LMID, tc, "    Military base",
				    Alignment.RMID, intel.getProgressColor(p), "" + p);
		} else if (mil != null && mil.hasFunctionalIndustry(Industries.PATROLHQ)) {
			info.addRowWithGlow(Alignment.LMID, tc, "    Patrol HQ",
				    Alignment.RMID, intel.getProgressColor(p), "" + p);
		} else {
			info.addRowWithGlow(Alignment.LMID, Misc.getGrayColor(), "    Military infrastructure",
				    Alignment.RMID, intel.getProgressColor(p), "");
		}
		
		TooltipCreator t = new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("Based on the size the largest colony under your control that has a Patrol HQ, "
						+ "a Military Base, or a High Command.", 0f);
				
				tooltip.addPara("A Patrol HQ reduces monthly progress by %s points. A Military Base and a High Command "
						+ "reduce progress by %s and %s the colony's size, respectively. Only the highest-scoring colony is used, "
						+ "building multiple military bases has no effect.", opad, Misc.getHighlightColor(),
						"" + PATROL_HQ_POINTS, 
						"" + (int)MILITARY_SIZE_MULT + Strings.X,
						"" + (int)HIGH_COMMAND_SIZE_MULT + Strings.X);
						
				MarketAPI mil = getBestMilitaryMarket();
				if (mil != null && mil.getStarSystem() != null) {
					int score = -1 * getMilitaryScore(mil);
					if (score > 0) {
						tooltip.addPara("Best military colony: %s, size: %s, score: %s points", opad, Misc.getHighlightColor(), 
								mil.getName(),
								"" + mil.getSize(),
								"" + score);
						float w = tooltip.getWidthSoFar();
						float h2 = Math.round(w / 1.6f);
						tooltip.addSectorMap(w, h2, mil.getStarSystem(), opad);
					}
				}
			}
		};
		info.addTooltipToAddedRow(t, TooltipLocation.RIGHT, false);
		
//		int def = getIncreasedDefensesScore(intel);
//		if (def != 0) {
//			info.addRowWithGlow(Alignment.LMID, tc, "    Increased defenses",
//				    Alignment.RMID, intel.getProgressColor(def), "" + def);
//			
//			t = new BaseFactorTooltip() {
//				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
//					tooltip.addPara("Event progress reduced further by %s the other defense factors.", 0f,
//							Misc.getHighlightColor(), "" + (int)INCREASED_DEFENSES_MULT + Strings.X);
//				}
//			};
//			info.addTooltipToAddedRow(t, TooltipLocation.RIGHT, false);
//		}
	}

//	public int getProgress(BaseEventIntel intel) {
//		return getProgress(intel, true);
//	}
	public int getProgress(BaseEventIntel intel) {
		int p = getColonyIncreasedDefensesScore(intel, getBiggestColony()) + getMilitaryScore(getBestMilitaryMarket());
		return p;
	}
	
//	public int getIncreasedDefensesScore(BaseEventIntel intel) {
//		if (intel.isStageOrOneOffEventReached(Stage.INCREASED_DEFENSES)) {
//			return (int) Math.round(getProgress(intel, false) * INCREASED_DEFENSES_MULT);
//		}
//		return 0;
//	}
	
	public int getColonyIncreasedDefensesScore(BaseEventIntel intel, MarketAPI market) {
		if (market == null) return 0;
		if (intel.isStageOrOneOffEventReached(Stage.INCREASED_DEFENSES)) {
			return -1 * Math.round(market.getSize() + market.getStabilityValue());
		}
		return 0;
	}
	
	@Override
	public String getProgressStr(BaseEventIntel intel) {
		return "";
	}

	public String getDesc(BaseEventIntel intel) {
		return "Defensive measures";
	}	

	@Override
	public Color getDescColor(BaseEventIntel intel) {
		if (getProgress(intel) == 0) {
			return Misc.getGrayColor();
		}
		return super.getDescColor(intel);
	}

	public MarketAPI getBiggestColony() {
		List<MarketAPI> markets = Misc.getPlayerMarkets(false);
		MarketAPI biggest = null;
		float max = 0;
		for (MarketAPI market : markets) {
			float size = market.getSize();
			if (size >= max) {
				if (size == max && biggest != null) {
					if (biggest.getStabilityValue() > market.getStabilityValue()) {
						continue;
					}
				}
				max = size;
				biggest = market;
			}
		}
		return biggest;
	}
	
	public int getMilitaryScore(MarketAPI market) {
		if (market == null) return 0;
		
		if (market.hasFunctionalIndustry(Industries.PATROLHQ)) {
			return -1 * PATROL_HQ_POINTS;
		}
		if (Misc.isMilitary(market)) {
			if (market.hasFunctionalIndustry(Industries.HIGHCOMMAND)) {
				return -1 * Math.round(market.getSize() * HIGH_COMMAND_SIZE_MULT);
			}
			return -1 * Math.round(market.getSize() * MILITARY_SIZE_MULT);
		}
		return 0;
	}
	
	public MarketAPI getBestMilitaryMarket() {
		List<MarketAPI> markets = Misc.getPlayerMarkets(false);
		MarketAPI best = null;
		int max = 0;
		for (MarketAPI market : markets) {
			int curr = -1 * getMilitaryScore(market);
			if (curr > max) {
				max = curr;
				best = market;
			}
		}
		return best;
	}
}
