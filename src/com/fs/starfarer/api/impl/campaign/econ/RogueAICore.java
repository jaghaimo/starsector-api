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



public class RogueAICore extends BaseMarketConditionPlugin implements MarketImmigrationModifier {

	public static final float STABILITY_PENALTY = 1f;
	
	public static RogueAICore get(MarketAPI market) {
		MarketConditionAPI mc = market.getCondition(Conditions.ROGUE_AI_CORE);
		if (mc != null && mc.getPlugin() instanceof RogueAICore) {
			return (RogueAICore) mc.getPlugin();
		}
		return null;
	}
	
	private float daysActive = 0f;
	@Override
	public void advance(float amount) {
		super.advance(amount);
		float days = Global.getSector().getClock().convertToDays(amount);
		daysActive += days;
	}
	
	public float getDaysActive() {
		return daysActive;
	}
	
	public void setDaysActive(float daysActive) {
		this.daysActive = daysActive;
	}

	public void apply(String id) {
		market.addTransientImmigrationModifier(this);
		
		market.getStability().modifyFlat(id, -STABILITY_PENALTY, "Rogue AI core");
	}
	
	@Override
	public boolean isTransient() {
		return false;
	}

	public void unapply(String id) {
		market.removeTransientImmigrationModifier(this);
		
		market.getStability().unmodifyFlat(id);
	}

	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		incoming.add(Factions.LUDDIC_PATH, 10f);
	}
	
	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		
		tooltip.addPara("%s stability.", 
						10f, Misc.getHighlightColor(),
						"-" + (int)STABILITY_PENALTY);
	}
}





