package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * AdjustRep <factionId> <RepActions action>
 */
public class AdjustRep extends BaseCommandPlugin {
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String factionId = params.get(0).getString(memoryMap);
		try {
			RepActions action = RepActions.valueOf(params.get(1).getString(memoryMap));
			RepActionEnvelope envelope = new RepActionEnvelope(action, null, dialog.getTextPanel());
			ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(envelope, factionId);
			return result.delta != 0;
		} catch (Throwable t) {
			CustomRepImpact impact = new CustomRepImpact();
			if (params.size() >= 3) {
				impact.limit = RepLevel.valueOf(params.get(1).getString(memoryMap));
				impact.delta = params.get(2).getFloat(memoryMap) * 0.01f;
			} else {
				impact.delta = params.get(1).getFloat(memoryMap) * 0.01f;
			}
			ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.CUSTOM, impact,
										  null, dialog.getTextPanel(), true), 
										  factionId);
			
//			RepActions action = RepActions.valueOf(params.get(1).getString(memoryMap));
//			RepActionEnvelope envelope = new RepActionEnvelope(action, null, dialog.getTextPanel());
//			ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(envelope, factionId);
			return result.delta != 0;
		}
	}
}
