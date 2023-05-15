package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathCellsIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.HA_CMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class StandardLuddicPathActivityCause2 extends BaseHostileActivityCause2 {

	public StandardLuddicPathActivityCause2(HostileActivityEventIntel intel) {
		super(intel);
	}
	
	@Override
	public TooltipCreator getTooltip() {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("Use of advanced technology and AI cores attracts the attention of Pathers.", 0f,
						Misc.getHighlightColor(), "advanced technology", "AI cores");
				
				Set<LuddicPathBaseIntel> seen = new LinkedHashSet<LuddicPathBaseIntel>();
				for (MarketAPI market : Misc.getPlayerMarkets(false)) {
					LuddicPathBaseIntel base = LuddicPathCellsIntel.getClosestBase(market);
					if (base == null) continue;
					
					if (seen.contains(base)) continue;
					
					LuddicPathCellsIntel cells = LuddicPathCellsIntel.getCellsForMarket(market);
					if (cells == null || cells.isSleeper()) continue;
					
					seen.add(base);
					
					Color h = Misc.getHighlightColor();
					String system = "";
					if (base.isPlayerVisible()) {
						system = " located in the " + base.getSystem().getNameWithLowercaseTypeShort() + "";
					}
					tooltip.addPara("Active Pather cells on some of your colonies are being supported "
							+ "by a Pather base" + system + ". %s "
							+ "will reduce the level of Pather fleet actvity.", opad, h,
							"Dealing with this base");
					break;
				}
				
				for (MarketAPI market : Misc.getPlayerMarkets(false)) {
					LuddicPathCellsIntel cells = LuddicPathCellsIntel.getCellsForMarket(market);
					if (cells == null) continue;
					if (cells.getSleeperTimeout() > 0) {
						tooltip.addPara("Pather cells on some of your colonies have been disrupted, reducing "
								+ "the Pather interest contribution from these colonies to event progress.", opad, 
								Misc.getPositiveHighlightColor(), "disrupted");
						break;
					}
					
				}
			}
		};
	}
	
	@Override
	public boolean shouldShow() {
		return getProgress() != 0 || HA_CMD.playerHasPatherAgreement();
	}

	@Override
	public String getProgressStr() {
		if (HA_CMD.playerHasPatherAgreement()) return EventFactor.NEGATED_FACTOR_PROGRESS;
		return super.getProgressStr();
	}

	@Override
	public Color getProgressColor(BaseEventIntel intel) {
		if (HA_CMD.playerHasPatherAgreement()) return Misc.getPositiveHighlightColor();
		return super.getProgressColor(intel);
	}

	public int getProgress() {
		if (HA_CMD.playerHasPatherAgreement()) return 0;
		
		int progress = (int) Math.round(getTotalPatherInterest());
		
		float unit = Global.getSettings().getFloat("patherProgressUnit");
		float mult = Global.getSettings().getFloat("patherProgressMult");
		//progress = 200;
		int rem = progress;
		float adjusted = 0;
		while (rem > unit) {
			adjusted += unit;
			rem -= unit;
			rem *= mult;
		}
		adjusted += rem;
		
		int reduced = Math.round(adjusted);
		if (progress > 0 && reduced < 1) reduced = 1;
		progress = reduced;
		
//		int reduced = (int) Math.round(Math.pow(progress, 0.8f));
//		if (progress > 0 && reduced <= 0) reduced = 1;
//		progress = reduced;
		
		return progress;
	}
	
	public String getDesc() {
		return "Technology and AI core use";
	}
	
	public float getTotalPatherInterest() {
		float total = 0f;
		for (StarSystemAPI system : Misc.getPlayerSystems(false)) {
			float noCells = Global.getSettings().getFloat("patherProgressMultNoCells");
			float sleeperCells = Global.getSettings().getFloat("patherProgressMultSleeperCells");
			float activeCells = Global.getSettings().getFloat("patherProgressMultActiveCells");
			
//			noCells = 0f;
//			sleeperCells = 0.25f;
//			activeCells = 1f;
			total += getPatherInterest(system, noCells, sleeperCells, activeCells);
		}
		return total;
	}
	
	public static float getPatherInterest(StarSystemAPI system, float multIfNoCells, float multIfSleeper, float multIfActive) {
		return getPatherInterest(system, multIfNoCells, multIfSleeper, multIfActive, false);
	}
	public static float getPatherInterest(StarSystemAPI system, float multIfNoCells, float multIfSleeper, float multIfActive, boolean countCellsOnly) {
		float total = 0f;
		List<MarketAPI> markets = Misc.getMarketsInLocation(system, Factions.PLAYER);
		for (MarketAPI market : markets) {
			float mult = 1f;
			LuddicPathCellsIntel intel = LuddicPathCellsIntel.getCellsForMarket(market);
			if (intel == null) {
				mult = multIfNoCells; 
			} else if (intel.isSleeper()) {
				mult = multIfSleeper;
			} else {
				mult = multIfActive;
			}

			float interest = LuddicPathBaseManager.getLuddicPathMarketInterest(market);
			if (countCellsOnly) interest = 1f;
			total += (interest * mult);
		}
		return total;
	}
	
	
	public float getMagnitudeContribution(StarSystemAPI system) {
		if (HA_CMD.playerHasPatherAgreement()) return 0f;
		
		List<MarketAPI> markets = Misc.getMarketsInLocation(system, Factions.PLAYER);
		
//		float noCells = Global.getSettings().getFloat("luddicPathNoCellsInterestMult");
//		float sleeperCells = Global.getSettings().getFloat("luddicPathSleeperInterestMult");
//		float activeCells = Global.getSettings().getFloat("luddicPathActiveInterestMult");
		
		float perSleeperBase = Global.getSettings().getFloat("luddicPathSleeperCellsBase");
		float perSleeperSize = Global.getSettings().getFloat("luddicPathSleeperCellsPerSize");
		float perActiveBase = Global.getSettings().getFloat("luddicPathActiveCellsBase");
		float perActiveSize = Global.getSettings().getFloat("luddicPathActiveCellsPerSize");
		float perPointOfInterest = Global.getSettings().getFloat("luddicPathPerPointOfInterest");

		float max = 0f;
		for (MarketAPI market : markets) {
			LuddicPathCellsIntel intel = LuddicPathCellsIntel.getCellsForMarket(market);
			if (intel == null) continue;
			
			float curr = 0f;
			if (intel.isSleeper()) {
				curr += perSleeperBase + market.getSize() * perSleeperSize;
			} else {
				curr += perActiveBase + market.getSize() * perActiveSize;
			}

			float interest = LuddicPathBaseManager.getLuddicPathMarketInterest(market);
			curr += interest * perPointOfInterest;
			
			if (curr >= max) {
				max = curr;
			}
		}
		
		max = Math.round(max * 100f) / 100f;
		
		return max;
	}

}
