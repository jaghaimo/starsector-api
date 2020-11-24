package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;



public class FreeMarket extends BaseMarketConditionPlugin implements MarketImmigrationModifier {

	public static final float MIN_STABILITY_PENALTY = 1f;
	public static final float MAX_STABILITY_PENALTY = 3f;
	//public static final float STABILITY_PENALTY = 3f;
	
	public static final float MIN_ACCESS_BONUS = 0.05f;
	public static final float MAX_ACCESS_BONUS = 0.25f;
	
	public static final float MIN_GROWTH = 5f;
	public static final float MAX_GROWTH = 25f;
	public static final float MAX_DAYS = 365f;

	public static FreeMarket get(MarketAPI market) {
		MarketConditionAPI mc = market.getCondition(Conditions.FREE_PORT);
		if (mc != null && mc.getPlugin() instanceof FreeMarket) {
			return (FreeMarket) mc.getPlugin();
		}
		return null;
	}
	
	private float daysActive = 0f;
	@Override
	public void advance(float amount) {
		if (!market.hasSpaceport()) {
			market.removeSpecificCondition(condition.getIdForPluginModifications());
			market.setFreePort(false);
			return;
		}
		if (amount <= 0) return;
		
		super.advance(amount);
		
		float days = Global.getSector().getClock().convertToDays(amount);
		daysActive += days;
	}
	
	public boolean runWhilePaused() {
		return true;
	}
	
	public float getDaysActive() {
		return daysActive;
	}
	
	public void setDaysActive(float daysActive) {
		this.daysActive = daysActive;
	}

	public void apply(String id) {
		market.addTransientImmigrationModifier(this);
		
		market.getAccessibilityMod().modifyFlat(id, getAccessBonus(), "Free port");
		market.getStability().modifyFlat(id, -getStabilityPenalty(), "Free port");
	}
	
	@Override
	public boolean isTransient() {
		return false;
	}

	public void unapply(String id) {
		market.removeTransientImmigrationModifier(this);
		
		market.getStability().unmodifyFlat(id);
		market.getAccessibilityMod().unmodifyFlat(id);
	}

	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		incoming.add(Factions.PIRATES, 5f);
		incoming.add(Factions.POOR, 5f);
		incoming.add(Factions.INDEPENDENT, 5f);
		incoming.getWeight().modifyFlat(getModId(), getImmigrationBonus(), Misc.ucFirst(condition.getName().toLowerCase()));
	}
	
	protected float getImmigrationBonus() {
		//float growth = Math.min(1f, daysActive / 10f) * MAX_GROWTH;
		float growth = MIN_GROWTH + daysActive / MAX_DAYS * (MAX_GROWTH - MIN_GROWTH);
		growth = Math.round(growth);
		if (growth > MAX_GROWTH) growth = MAX_GROWTH;
		if (growth < 1) growth = 1;
		return growth;
	}
	
	protected float getAccessBonus() {
		//float growth = Math.min(1f, daysActive / 10f) * MAX_GROWTH;
		float access = MIN_ACCESS_BONUS + daysActive / MAX_DAYS * (MAX_ACCESS_BONUS - MIN_ACCESS_BONUS);
		access = Math.round(access * 100f) / 100f;
		if (access > MAX_ACCESS_BONUS) access = MAX_ACCESS_BONUS;
		if (access < 0.01f) access = 0.01f;
		return access;
	}
	
	protected float getStabilityPenalty() {
		//float growth = Math.min(1f, daysActive / 10f) * MAX_GROWTH;
		float s = MIN_STABILITY_PENALTY + daysActive / MAX_DAYS * (MAX_STABILITY_PENALTY - MIN_STABILITY_PENALTY);
		s = Math.round(s);
		if (s > MAX_STABILITY_PENALTY) s = MAX_STABILITY_PENALTY;
		if (s < 1f) s = 1f;
		return s;
	}
	

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		
		if (!market.hasSpaceport()) {
			tooltip.addPara("Requires a spaceport to have any effect", Misc.getNegativeHighlightColor(), 10f);
			return;
		}
		
//		tooltip.addPara("%s stability", 
//						10f, Misc.getHighlightColor(),
//						"-" + (int)STABILITY_PENALTY);
		tooltip.addPara("%s stability (maximum of %s, reached after %s days)", 
				10f, Misc.getHighlightColor(),
				"-" + (int)getStabilityPenalty(),
				"-" + (int) (MAX_STABILITY_PENALTY),
				"" + (int) MAX_DAYS);
		tooltip.addPara("%s population growth (maximum of %s, reached after %s days)",
				
				10f, Misc.getHighlightColor(), 
				"+" + (int) getImmigrationBonus(),
				"+" + (int) (MAX_GROWTH),
				"" + (int) MAX_DAYS);
		
		tooltip.addPara("%s accessibility (maximum of %s, reached after %s days)", 
				10f, Misc.getHighlightColor(), 
				"+" + (int) Math.round(getAccessBonus() * 100f) + "%",
				"+" + (int) Math.round(MAX_ACCESS_BONUS * 100f) + "%",
				"" + (int) MAX_DAYS);
		
		tooltip.addPara("Colony does not require the transponder to be turned on for open trade. " +
						"All commodities are legal to trade.", 10f);
		
		tooltip.addPara("Colony gets income from smuggled exports.", 10f);
	}
}





