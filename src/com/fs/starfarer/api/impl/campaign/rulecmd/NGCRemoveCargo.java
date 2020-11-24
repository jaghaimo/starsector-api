package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	NGCRemoveCargo <CargoItemType> <cargo id> <quantity>
 */
public class NGCRemoveCargo extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		CargoItemType type = CargoItemType.valueOf(params.get(0).getString(memoryMap));
		String id = params.get(1).getString(memoryMap);
		float qty = params.get(2).getFloat(memoryMap);
		
		CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");
		
		data.getStartingCargo().removeItems(type, id, qty);
		return true;
	}

}
