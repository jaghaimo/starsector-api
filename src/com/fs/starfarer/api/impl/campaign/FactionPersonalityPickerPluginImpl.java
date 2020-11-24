package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.plugins.FactionPersonalityPickerPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class FactionPersonalityPickerPluginImpl implements FactionPersonalityPickerPlugin {

	public WeightedRandomPicker<String> createPersonalityPicker(FactionAPI faction) {
		int aggression = faction.getDoctrine().getAggression();
		
		WeightedRandomPicker<String> result = new WeightedRandomPicker<String>();
		if (aggression == 1) {
			result.add(Personalities.CAUTIOUS, 20);
			//result.add(Personalities.CAUTIOUS, 10);
			//result.add(Personalities.STEADY, 10);
		} else if (aggression == 2) {
			result.add(Personalities.STEADY, 20);
		} else if (aggression == 3) {
			result.add(Personalities.AGGRESSIVE, 20);
		} else if (aggression == 4) {
			result.add(Personalities.AGGRESSIVE, 10);
			result.add(Personalities.RECKLESS, 10);
		} else if (aggression == 5) {
			result.add(Personalities.RECKLESS, 20);
		}
		
		return result;
	}

	public void addDescToTooltip(TooltipMakerAPI tooltip, int level) {
		float opad = 10f;
		
		Color pH = Misc.getHighlightColor();
		if (level == 1) {
			tooltip.addPara("%s officers and ship commanders.", opad,
							pH, "Cautious");
		} else if (level == 2) {
			tooltip.addPara("%s officers and ship commanders.", opad, pH, "Steady");
		} else if (level == 3) {
			tooltip.addPara("%s officers and ship commanders.", opad, pH, "Aggressive");
		} else if (level == 4) {
			tooltip.addPara("A mix of %s and %s officers and ship commanders.", opad, 
					pH, "aggressive", "reckless");
		} else if (level == 5) {
			tooltip.addPara("%s officers and ship commanders.", opad, pH, "Reckless");
		}		
	}

}
