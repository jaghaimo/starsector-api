package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.PerseanLeagueMembership;
import com.fs.starfarer.api.impl.campaign.rulecmd.HA_CMD;
import com.fs.starfarer.api.ui.MapParams;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;

public class StandardPerseanLeagueActivityCause extends BaseHostileActivityCause2 {

	/** IF CHANGING THESE: ALSO UPDATE THE plReynardHannanJoinSelNo RULE **/
	public static int LARGE_COLONY = 5;
	public static int MEDIUM_COLONY = 4;
	public static int COUNT_IF_MEDIUM = 2;
	
	public static float MAX_MAG = 0.5f;
	
	public StandardPerseanLeagueActivityCause(HostileActivityEventIntel intel) {
		super(intel);
	}

	@Override
	public TooltipCreator getTooltip() {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("Going to Kazeron and negotiating to join the League is likely to get "
						+ "this harassment to stop. If "
						+ "left unchecked, this low-grade conflict will eventually come to a head and is likely to "
						+ "be resolved one way or another.", 0f, Misc.getHighlightColor(),
						"join the League");
				
				tooltip.addPara("Event progress value is based on the number and size of colonies "
						+ "under your control. Requires one size %s colony, or at least %s colonies with one "
						+ "being at least size %s.", opad, Misc.getHighlightColor(),
						"" + LARGE_COLONY, "" + COUNT_IF_MEDIUM, "" + MEDIUM_COLONY);
				
				MarketAPI kazeron = PerseanLeagueHostileActivityFactor.getKazeron(false);
				if (kazeron != null && kazeron.getStarSystem() != null) {
//					tooltip.addPara("%s, size: %s, stability: %s", opad, Misc.getHighlightColor(), 
//							kazeron.getName(),
//							"" + kazeron.getSize(),
//							"" + (int) kazeron.getStabilityValue());
					
					MapParams params = new MapParams();
					params.showSystem(kazeron.getStarSystem());
					float w = tooltip.getWidthSoFar();
					float h = Math.round(w / 1.6f);
					params.positionToShowAllMarkersAndSystems(true, Math.min(w, h));
					UIPanelAPI map = tooltip.createSectorMap(w, h, params, kazeron.getName() + " (" + kazeron.getStarSystem().getNameWithLowercaseTypeShort() + ")");
					tooltip.addCustom(map, opad);
				}
			}
		};
	}
	
	@Override
	public boolean shouldShow() {
		return getProgress() != 0;
	}

	@Override
	public String getProgressStr() {
		//if (KantaCMD.playerHasProtection()) return EventFactor.NEGATED_FACTOR_PROGRESS;
		return super.getProgressStr();
	}

	@Override
	public Color getProgressColor(BaseEventIntel intel) {
		//if (KantaCMD.playerHasProtection()) return Misc.getPositiveHighlightColor();
		// TODO Auto-generated method stub
		return super.getProgressColor(intel);
	}

	public int getProgress() {
		//if (true) return 0;
		if (!HA_CMD.canPlayerJoinTheLeague()) {
			return 0;
		}
		
//		if (Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(
//					PerseanLeagueHostileActivityFactor.DEFEATED_BLOCKADE)) {
		if (PerseanLeagueMembership.isDefeatedBlockadeOrPunEx()) {
			return 0;
		}

		int score = 0;
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			int size = market.getSize();
			if (size <= 4) {
				score += size * 2;
			} else if (size == 5) {
				score += size * 2 + 2;
			} else if (size == 6) {
				score += size * 2 + 4;
			} else {
				score += size * 3;
			}
		}
		
		int progress = score;
		
		return progress;
	}
	
	
	public String getDesc() {
		return "Colony presence and size";
	}

	
	public float getMagnitudeContribution(StarSystemAPI system) {
		//if (KantaCMD.playerHasProtection()) return 0f;
		if (getProgress() <= 0) return 0f;
		
		return (0.4f + 0.6f * intel.getMarketPresenceFactor(system)) * MAX_MAG; 
	}

}






