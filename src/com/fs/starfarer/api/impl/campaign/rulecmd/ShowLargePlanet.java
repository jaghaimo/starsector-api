package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

public class ShowLargePlanet extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		SectorEntityToken target = dialog.getInteractionTarget();
		
		if (target.getMarket() != null) {
			target = target.getMarket().getPlanetEntity();
		}
		if (target instanceof PlanetAPI) {
			if (!Global.getSettings().getBoolean("3dPlanetBGInInteractionDialog")) {
				dialog.getVisualPanel().showLargePlanet((PlanetAPI) target);
			}
		}
		return true;
	}
}


