package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.rulecmd.KantaCMD;
import com.fs.starfarer.api.ui.MapParams;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;

public class StandardPirateActivityCause2 extends BaseHostileActivityCause2 {

	public static float MAX_MAG = 0.5f;
	
	public StandardPirateActivityCause2(HostileActivityEventIntel intel) {
		super(intel);
	}

	@Override
	public TooltipCreator getTooltip() {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("Any colony, especially one outside the core, attracts some degree of piracy."
						+ " %s and %s colonies attract more pirates.", 0f,
						Misc.getHighlightColor(), "Larger", "less stable");
				tooltip.addPara("Event progress value is based on the size and stability of the largest colony "
						+ "under your control. If multiple colonies have the same size, the one with higher "
						+ "stability is used.", opad);
				
				MarketAPI biggest = getBiggestColony();
				if (biggest != null && biggest.getStarSystem() != null) {
					tooltip.addPara("Biggest colony: %s, size: %s, stability: %s", opad, Misc.getHighlightColor(), 
							biggest.getName(),
							"" + biggest.getSize(),
							"" + (int) biggest.getStabilityValue());
					
					MapParams params = new MapParams();
					params.showSystem(biggest.getStarSystem());
					float w = tooltip.getWidthSoFar();
					float h = Math.round(w / 1.6f);
					params.positionToShowAllMarkersAndSystems(true, Math.min(w, h));
					UIPanelAPI map = tooltip.createSectorMap(w, h, params, biggest.getStarSystem().getNameWithLowercaseTypeShort());
					tooltip.addCustom(map, opad);
				}
			}
		};
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
	
	@Override
	public boolean shouldShow() {
		return getProgress() != 0 || KantaCMD.playerHasProtection();
	}

	@Override
	public String getProgressStr() {
		if (KantaCMD.playerHasProtection()) return EventFactor.NEGATED_FACTOR_PROGRESS;
		return super.getProgressStr();
	}

	@Override
	public Color getProgressColor(BaseEventIntel intel) {
		if (KantaCMD.playerHasProtection()) return Misc.getPositiveHighlightColor();
		// TODO Auto-generated method stub
		return super.getProgressColor(intel);
	}

	public int getProgress() {
		if (KantaCMD.playerHasProtection()) return 0;
		
		MarketAPI biggest = getBiggestColony();
		if (biggest == null) return 0;
		int progress = (int) (biggest.getSize() + (10 - biggest.getStabilityValue()));
		return progress;
	}
	
	public String getDesc() {
		return "Colony presence and instability";
	}

	public float getMagForMarket(MarketAPI market) {
		float val = market.getSize() * (0.33f + 0.67f * (1f - market.getStabilityValue() / 10f));
		val *= 0.1f;
		if (val > MAX_MAG) val = MAX_MAG;
		return val;
	}
	
	public float getMagnitudeContribution(StarSystemAPI system) {
		if (KantaCMD.playerHasProtection()) return 0f;
		
		List<MarketAPI> markets = Misc.getMarketsInLocation(system, Factions.PLAYER);
		
		float max = 0.1f;
		for (MarketAPI market : markets) {
			float val = getMagForMarket(market);
			//float val = market.getSize() * 0.01f * 5f;
			max = Math.max(val, max);
		}
		
		if (max > MAX_MAG) max = MAX_MAG;
		
		max = Math.round(max * 100f) / 100f;
		
		//if (true) return 0.79f;
		return max;
	}

}




