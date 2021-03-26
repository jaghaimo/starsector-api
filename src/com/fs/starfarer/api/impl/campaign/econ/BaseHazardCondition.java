package com.fs.starfarer.api.impl.campaign.econ;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


/**
 * Requires corresponding entry in condition_gen_data.csv.
 *  
 * @author Alex Mosolov
 *
 * Copyright 2020 Fractal Softworks, LLC
 */
public class BaseHazardCondition extends BaseMarketConditionPlugin {
	
	public void apply(String id) {
		Object test = Global.getSettings().getSpec(ConditionGenDataSpec.class, condition.getId(), true);
		if (test instanceof ConditionGenDataSpec) {
			ConditionGenDataSpec spec = (ConditionGenDataSpec) test;
			float hazard = spec.getHazard();
			if (hazard != 0) {
				market.getHazard().modifyFlat(id, hazard, condition.getName());
			}
		}
	}

	public void unapply(String id) {
		market.getHazard().unmodifyFlat(id);
	}

	@Override
	public Map<String, String> getTokenReplacements() {
		return super.getTokenReplacements();
	}

	
	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		
		Object test = Global.getSettings().getSpec(ConditionGenDataSpec.class, condition.getId(), true);
		if (test instanceof ConditionGenDataSpec) {
			ConditionGenDataSpec spec = (ConditionGenDataSpec) test;
			float hazard = spec.getHazard();
			//hazard = 0.25f;
			if (hazard != 0) {
				String pct = "" + (int)(hazard * 100f) + "%";
				if (hazard > 0) pct = "+" + pct;
				tooltip.addPara("%s hazard rating", 10f, Misc.getHighlightColor(), pct);
			}
		}
	}
}




