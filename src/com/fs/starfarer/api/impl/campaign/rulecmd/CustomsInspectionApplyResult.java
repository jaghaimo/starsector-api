package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.CustomsInspectionGenerateResult.CargoInspectionResult;
import com.fs.starfarer.api.util.Misc.Token;

public class CustomsInspectionApplyResult extends BaseCommandPlugin {

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CampaignFleetAPI other = (CampaignFleetAPI) dialog.getInteractionTarget();
		
		//FactionAPI faction = other.getFaction();
		MemoryAPI memory = memoryMap.get(MemKeys.ENTITY);
		
		CargoInspectionResult result = (CargoInspectionResult) memory.get("$cargoInspectionResult");
		
		for (CargoStackAPI stack : result.getIllegalFound().getStacksCopy()) {
			playerFleet.getCargo().removeItems(stack.getType(), stack.getData(), stack.getSize());
		}
		
		if (playerFleet.getCargo().getCredits().get() >= result.getTollAmount()) {
			playerFleet.getCargo().getCredits().subtract(result.getTollAmount());
		}
		
		return true;
	}

}







