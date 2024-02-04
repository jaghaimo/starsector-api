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
		//if (dialog == null) return false;
		
		// if the player already had the ability, calling this command ensures they don't lose it
		// if they spec out of the skill that granted it, but this command will be "quiet"
		
		String abilityId = params.get(0).getString(memoryMap);
		if (abilityId == null) return false;
		
		boolean hadAbilityAlready = Global.getSector().getPlayerFleet().hasAbility(abilityId);
		
		boolean found = false;
		AbilitySlotsAPI slots = Global.getSector().getUIData().getAbilitySlotsAPI();
		int curr = slots.getCurrBarIndex();
		OUTER: for (int i = 0; i < 5; i++) {
			slots.setCurrBarIndex(i);
			for (AbilitySlotAPI slot : slots.getCurrSlotsCopy()) {
				if (abilityId.equals(slot.getAbilityId())) {
					found = true;
					break OUTER;
				}
			}
			slots.setCurrBarIndex(curr);
		}
		if (!found) {
			hadAbilityAlready = false;
		}
		
		Global.getSector().getCharacterData().addAbility(abilityId);
		//Global.getSector().getUIData().getAbilitySlotsAPI().getCurrSlotsCopy().get(8).getAbilityId()
		
		if (!hadAbilityAlready) {
			boolean assignedToSlot = false;
			boolean doNotAssign = false;
			if (params.size() >= 2) {
				int slotIndex = (int) params.get(1).getFloat(memoryMap);
				if (slotIndex < 0) {
					doNotAssign = true;
				} else {
					slots.setCurrBarIndex(0);
					AbilitySlotAPI slot = slots.getCurrSlotsCopy().get(slotIndex);
					if (slot.getAbilityId() == null) {
						slot.setAbilityId(abilityId);
						assignedToSlot = true;
					}
				}
			}
			
			if (!assignedToSlot && !doNotAssign) {
				int currBarIndex = slots.getCurrBarIndex();
				OUTER: for (int i = 0; i < 5; i++) {
					slots.setCurrBarIndex(i);
					for (int j = 0; j < 10; j++) {
						AbilitySlotAPI slot = slots.getCurrSlotsCopy().get(j);
						if (slot.getAbilityId() == null) {
							slot.setAbilityId(abilityId);
							assignedToSlot = true;
							break OUTER;
						}		
					}
				}
				slots.setCurrBarIndex(currBarIndex);
			}
			
			Global.getSector().getCharacterData().getMemoryWithoutUpdate().set("$ability:" + abilityId, true, 0);
			if (dialog != null) {
				AddRemoveCommodity.addAbilityGainText(abilityId, dialog.getTextPanel());
			}
		}
		
		return true;
	}
	
	
}



