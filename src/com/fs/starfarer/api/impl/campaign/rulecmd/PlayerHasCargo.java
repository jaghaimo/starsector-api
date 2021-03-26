package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	PlayerHasCargo <cargo id - commodity, weapon, fighter, special item> <optional: quantity, default 1>
 */
public class PlayerHasCargo extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String id = params.get(0).getString(memoryMap);
		float quantity = 1;
		if (params.size() > 1) {
			quantity = params.get(1).getFloat(memoryMap);
		}
		
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		
		if (cargo.getCommodityQuantity(id) >= quantity) return true;
		if (cargo.getNumFighters(id) >= quantity) return true;
		if (cargo.getNumWeapons(id) >= quantity) return true;
		if (cargo.getQuantity(CargoItemType.SPECIAL, new SpecialItemData(id, null)) >= quantity) return true;
		
		return false;
	}

}
