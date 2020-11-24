package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.util.Misc.Token;


/**
 * ActivateAbility <fleet id> <ability id>
 */
public class ActivateAbility extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		String entityId = params.get(0).getString(memoryMap);
		String abilityId = params.get(1).getString(memoryMap);

		SectorEntityToken entity = Global.getSector().getEntityById(entityId);
		if (entity == null) return false;
		
		AbilityPlugin ability = entity.getAbility(abilityId);
		if (ability == null) return false;
		
		ability.activate();

		return ability.isActiveOrInProgress();
	}

}
