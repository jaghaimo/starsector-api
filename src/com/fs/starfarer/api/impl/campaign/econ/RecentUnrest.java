package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

public class RecentUnrest extends BaseMarketConditionPlugin {
	
	public static RecentUnrest get(MarketAPI market) {
		return get(market, true);
	}
	public static RecentUnrest get(MarketAPI market, boolean addIfNeeded) {
		MarketConditionAPI mc = market.getCondition(Conditions.RECENT_UNREST);
		if (mc == null && !addIfNeeded) return null;
		
		if (mc == null) {
			String id = market.addCondition(Conditions.RECENT_UNREST);
			mc = market.getSpecificCondition(id);
		}
		return (RecentUnrest) mc.getPlugin();
	}
	
	public static int getPenalty(MarketAPI market) {
		RecentUnrest ru = get(market, false);
		if (ru == null) return 0;
		return ru.getPenalty();
	}
	
	public static float DECREASE_DAYS = 90f;
	
	protected int penalty;
	protected float untilDecrease = DECREASE_DAYS;
	protected TimeoutTracker<String> reasons = new TimeoutTracker<String>();
	
	public RecentUnrest() {
	}

	public int getPenalty() {
		return penalty;
	}

	public void apply(String id) {
		market.getStability().modifyFlat(id, -1 * penalty, "Recent unrest");
	}

	public void unapply(String id) {
		market.getStability().unmodify(id);
	}
	
	
	public void add(int stability, String reason) {
		penalty += stability;
		float dur = reasons.getRemaining(reason) + stability * DECREASE_DAYS;
		reasons.add(reason, dur);
	}
	
	public void counter(int points, String reason) {
		points = Math.min(points, penalty);
		penalty -= points;
		if (penalty < 0) penalty = 0;
		float dur = reasons.getRemaining(reason) + points * DECREASE_DAYS;
		reasons.add(reason, dur);
	}
	
	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		float days = Misc.getDays(amount);
		//days *= 100000f;
		reasons.advance(days);

		if (penalty > 0) {
			untilDecrease -= days;
			if (untilDecrease <= 0) {
				penalty--;
				if (penalty < 0) penalty = 0;
				untilDecrease = DECREASE_DAYS;
			}
		}
		if (penalty <= 0) {
			market.removeSpecificCondition(condition.getIdForPluginModifications());
		}
	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltip(tooltip, expanded);
	}
	

	@Override
	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		
		float pad = 3f;
		float opad = 10f;
		
		tooltip.addPara("%s stability. Goes down by one point every three months.", 
				opad, Misc.getHighlightColor(),
				"-" + (int)penalty);
		
		if (!reasons.getItems().isEmpty()) {
			tooltip.addPara("Recent contributing factors:", opad);
			
			float initPad = pad;
			for (String reason : reasons.getItems()) {
				tooltip.addPara(BaseIntelPlugin.BULLET + reason, initPad);
				initPad = 0f;
			}
		}
		
	}

	@Override
	public boolean isTransient() {
		return false;
	}
}
