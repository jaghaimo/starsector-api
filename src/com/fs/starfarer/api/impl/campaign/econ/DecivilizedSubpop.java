package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class DecivilizedSubpop extends BaseHazardCondition implements MarketImmigrationModifier {
	
	public static float STABILITY_PENALTY = 2;
	
	public void apply(String id) {
		super.apply(id);
		
		market.getStability().modifyFlat(id, -STABILITY_PENALTY, "Decivilized subpopulation");
		
		market.addTransientImmigrationModifier(this);
	}

	public void unapply(String id) {
		super.unapply(id);
		market.getStability().unmodify(id);
		
		market.removeTransientImmigrationModifier(this);
	}

	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		incoming.add(Factions.POOR, 10f);
		incoming.getWeight().modifyFlat(getModId(), getImmigrationBonus(), Misc.ucFirst(condition.getName().toLowerCase()));
	}
	
	protected float getImmigrationBonus() {
		return market.getSize();
	}
	
	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		
		tooltip.addPara("%s stability", 
				10f, Misc.getHighlightColor(),
				"-" + (int)STABILITY_PENALTY);
		tooltip.addPara("%s population growth (based on colony size)", 
				10f, Misc.getHighlightColor(),
				"+" + (int) getImmigrationBonus());
//		tooltip.addPara("%s stability, %s population growth.", 
//				10f, Misc.getHighlightColor(),
//				"-" + (int)STABILITY_PENALTY, "+" + (int) getImmigrationBonus());
	}
}


