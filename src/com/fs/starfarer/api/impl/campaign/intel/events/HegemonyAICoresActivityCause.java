package com.fs.starfarer.api.impl.campaign.intel.events;

import java.util.List;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.PerseanLeagueMembership;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class HegemonyAICoresActivityCause extends BaseHostileActivityCause2 {

	public static int IGNORE_COLONY_THRESHOLD = 3;
	
	
	public HegemonyAICoresActivityCause(HostileActivityEventIntel intel) {
		super(intel);
	}
	
	@Override
	public TooltipCreator getTooltip() {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				tooltip.addPara("The Hegemony considers the use of %s illegal, though it is unlikely "
						+ "to take much notice of what goes on at colonies of size %s or smaller.", 0f,
						Misc.getHighlightColor(), "AI cores", "" + IGNORE_COLONY_THRESHOLD);
				
				if (isNegatedByPLMembership()) {
					Color c = Global.getSector().getFaction(Factions.PERSEAN).getBaseUIColor();
					LabelAPI label = tooltip.addPara("However, your membership in the Persean League makes it politically "
							+ "difficult for the Hegemony to pursue the matter.", opad);
					label.setHighlight("Persean League", "politically difficult");
					label.setHighlightColors(c, Misc.getPositiveHighlightColor());
				}
			}
		};
	}
	
	public boolean isNegatedByPLMembership() {
		//if (true) return true;
		return PerseanLeagueMembership.isLeagueMember() && getProgress(false) > 0;
	}
	
	@Override
	public String getProgressStr() {
		if (isNegatedByPLMembership()) return EventFactor.NEGATED_FACTOR_PROGRESS;
		return super.getProgressStr();
	}

	@Override
	public Color getProgressColor(BaseEventIntel intel) {
		if (isNegatedByPLMembership()) return Misc.getPositiveHighlightColor();
		return super.getProgressColor(intel);
	}
	
	@Override
	public boolean shouldShow() {
		return getProgress() != 0 || isNegatedByPLMembership();
	}

	public int getProgress() {
		return getProgress(true);
	}
	public int getProgress(boolean checkNegated) {
		if (HegemonyHostileActivityFactor.isPlayerDefeatedHegemony()) return 0;
		if (checkNegated && isNegatedByPLMembership()) return 0;
		
		int progress = (int) Math.round(getTotalAICorePoints());
		
		float unit = Global.getSettings().getFloat("hegemonyProgressUnit");
		float mult = Global.getSettings().getFloat("hegemonyProgressMult");
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
		return "AI core use";
	}
	
	public float getTotalAICorePoints() {
		float total = 0f;
		for (StarSystemAPI system : Misc.getPlayerSystems(false)) {
			total += getAICorePoints(system);
		}
		return total;
	}
	
	public static float getAICorePoints(StarSystemAPI system) {
		float total = 0f;
		List<MarketAPI> markets = Misc.getMarketsInLocation(system, Factions.PLAYER);
		for (MarketAPI market : markets) {
			if (market.getSize() <= IGNORE_COLONY_THRESHOLD) continue;
			float interest = getAICorePoints(market);
			total += interest;
		}
		return total;
	}
	
	public static float getAICorePoints(MarketAPI market) {
		float total = 0f;
		
		float admin = Global.getSettings().getFloat("hegemonyPointsAdmin");
		float alpha = Global.getSettings().getFloat("hegemonyPointsAlpha");
		float beta = Global.getSettings().getFloat("hegemonyPointsBeta");
		float gamma = Global.getSettings().getFloat("hegemonyPointsGamma");
		
		String aiCoreId = market.getAdmin().getAICoreId();
		if (aiCoreId != null) {
			total += admin;
		}
		
		for (Industry ind : market.getIndustries()) {
			String core = ind.getAICoreId();
			if (Commodities.ALPHA_CORE.equals(core)) {
				total += alpha;
			} else if (Commodities.BETA_CORE.equals(core)) {
				total += beta;
			} else if (Commodities.GAMMA_CORE.equals(core)) {
				total += gamma;
			}
		}
		
		return total;
	}
	
	public float getMagnitudeContribution(StarSystemAPI system) {
		if (HegemonyHostileActivityFactor.isPlayerDefeatedHegemony()) return 0f;
		if (isNegatedByPLMembership()) return 0f;
		
		if (getProgress() <= 0) return 0f;
		
		List<MarketAPI> markets = Misc.getMarketsInLocation(system, Factions.PLAYER);
		
		float total = 0f;
		for (MarketAPI market : markets) {
			float points = getAICorePoints(market);
			total += points;
		}
		
		total = Math.round(total * 100f) / 100f;
		
		return total;
	}

}
