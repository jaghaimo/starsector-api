package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.VarAndMemory;

/**
 * AdjustRepActivePerson <RepActions action>
 */
public class AdjustRepPerson extends BaseCommandPlugin {
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		VarAndMemory var = params.get(0).getVarNameAndMemory(memoryMap);
		PersonAPI person = null;
		
		if (var.memory.get(var.name) instanceof PersonAPI) {
			person = (PersonAPI) var.memory.get(var.name);
		} else {
			String id = params.get(0).getString(memoryMap);
			person = Global.getSector().getImportantPeople().getPerson(id);
		}
		
//		PersonAPI person = (PersonAPI) var.memory.get(var.name);
//		
//		if (person == null) {
//			// well, let's try this another way, then. This is probably sloppy, eh? -dgb
//			String id = params.get(0).getString(memoryMap);
//			person = Global.getSector().getImportantPeople().getPerson(id);
//		}
		
		try {
			RepActions action = RepActions.valueOf(params.get(1).getString(memoryMap));
			RepActionEnvelope envelope = new RepActionEnvelope(action, null, dialog.getTextPanel());
			ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(envelope, person);
			return result.delta != 0;
		} catch (Throwable t) {
			CustomRepImpact impact = new CustomRepImpact();
			impact.limit = RepLevel.valueOf(params.get(1).getString(memoryMap));
			impact.delta = params.get(2).getFloat(memoryMap) * 0.01f;
			ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.CUSTOM, impact,
										  null, dialog.getTextPanel(), true), person);
			return result.delta != 0;
		}
	}
}
