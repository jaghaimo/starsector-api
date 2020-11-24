package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersistentUIDataAPI.AbilitySlotAPI;
import com.fs.starfarer.api.campaign.PersistentUIDataAPI.AbilitySlotsAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	AddAbility <ability id> <optional default slot>
 */
public class AddAbility extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String abilityId = params.get(0).getString(memoryMap);
		Global.getSector().getCharacterData().addAbility(abilityId);

		if (params.size() >= 2) {
			int slotIndex = (int) params.get(1).getFloat(memoryMap);
			AbilitySlotsAPI slots = Global.getSector().getUIData().getAbilitySlotsAPI();
			slots.setCurrBarIndex(0);
			AbilitySlotAPI slot = slots.getCurrSlotsCopy().get(slotIndex);
			if (slot.getAbilityId() == null) {
				slot.setAbilityId(abilityId);
			}
		}
		
		AddRemoveCommodity.addAbilityGainText(abilityId, dialog.getTextPanel());
		
		return true;
	}
	
	
}



