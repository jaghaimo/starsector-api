package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI.OptionTooltipCreator;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	DoCanAffordCheck <price> <option id> <with total>
 */
public class DoCanAffordCheck extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		float price = params.get(0).getFloat(memoryMap);
		String option = params.get(1).getString(memoryMap);
		boolean showCredits = params.size() >= 3;
		boolean showTotal = params.size() >= 3 && params.get(2).getBoolean(memoryMap);
		
		TextPanelAPI text = dialog.getTextPanel();
		OptionPanelAPI options = dialog.getOptionPanel();
		
		Color h = Misc.getHighlightColor();
		Color n = Misc.getNegativeHighlightColor();
		n = h; // don't do red highlights they don't seem to come across well here, I think
		final float credits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
		//credits = 100;
		final boolean canAccept = (int) price <= (int) credits;
		
		LabelAPI label = null;
		
		if (showCredits) {
			if (showTotal) {
				label = text.addPara("The total price is %s. You have %s available.",
									h,
									Misc.getDGSCredits(price),	
									Misc.getDGSCredits(credits));
				label.setHighlightColors(canAccept ? h : n, h);
				label.setHighlight(Misc.getDGSCredits(price), Misc.getDGSCredits(credits));
			} else {
				label = text.addPara("You have %s available.",
						h,
						Misc.getDGSCredits(credits));
				label.setHighlightColors(canAccept ? h : n);
				label.setHighlight(Misc.getDGSCredits(credits));
			}
		}
		
		if (!canAccept) {
			options.setEnabled(option, false);
			//options.setTooltip(option, "Not enough credits.");
		}
		options.addOptionTooltipAppender(option, new OptionTooltipCreator() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean hadOtherText) {
				if (canAccept) {
					tooltip.addPara("You have %s credits available.", 10f, 
							Misc.getHighlightColor(), Misc.getWithDGS(credits));
				} else {
					tooltip.addPara("You only have %s credits available.", 10f, 
							Misc.getHighlightColor(), Misc.getWithDGS(credits));
				}
			}
		});
		
		return true;
	}
}


