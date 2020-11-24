package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.rulecmd.CustomsInspectionGenerateResult.CargoInspectionResult;
import com.fs.starfarer.api.util.Misc.Token;

public class CustomsInspectionApplyRepLoss extends BaseCommandPlugin {

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CampaignFleetAPI other = (CampaignFleetAPI) dialog.getInteractionTarget();
		FactionAPI faction = other.getFaction();
		
		MemoryAPI memory = memoryMap.get(MemKeys.ENTITY);
		CargoInspectionResult result = (CargoInspectionResult) memory.get("$cargoInspectionResult");

		float totalIllegalFound = 0f;
		for (CargoStackAPI stack : result.getIllegalFound().getStacksCopy()) {
			totalIllegalFound += stack.getSize();
		}
		float capacity = playerFleet.getCargo().getMaxCapacity();
		
		float repLoss = totalIllegalFound / 10f * totalIllegalFound / capacity;
		repLoss = Math.round(repLoss);
		if (repLoss > 5) repLoss = 5f;
		if (repLoss == 0 && totalIllegalFound > 0) repLoss = 1f;
		if (repLoss > 0) {
			RepActionEnvelope envelope = new RepActionEnvelope(RepActions.CUSTOMS_CAUGHT_SMUGGLING, repLoss, dialog.getTextPanel());
			Global.getSector().adjustPlayerReputation(envelope, faction.getId());
		}
		return true;
	}

}







