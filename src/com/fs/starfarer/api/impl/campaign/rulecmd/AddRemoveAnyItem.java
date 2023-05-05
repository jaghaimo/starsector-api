package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * 	First param is one of:
 * 		RESOURCES
 *		WEAPONS
 *		FIGHTER_CHIP
 *		SPECIAL
 *		
 *	SPECIAL catalytic_core 1
 *	SPECIAL ship_bp paragon 1
 *  WEAPONS ionpulser 1
 *
 *	AddRemoveAnyItem <type> <id> <optional other id for blueprints etc> <quantity>
 *
 */
public class AddRemoveAnyItem extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String typeStr = params.get(0).getString(memoryMap);
		CargoItemType type = Enum.valueOf(CargoItemType.class, typeStr);
		
		String id = params.get(1).getString(memoryMap);
		String param = null;
		int q = 1;
		if (type != CargoItemType.SPECIAL) {
			q = params.get(2).getInt(memoryMap);
		} else {
			if (params.size() <= 3) {
				q = params.get(2).getInt(memoryMap);
			} else if (params.size() >= 4) {
				param = params.get(2).getString(memoryMap);
				q = params.get(3).getInt(memoryMap);
			}
		}
		
		TextPanelAPI text = dialog.getTextPanel();
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		
		if (type == CargoItemType.RESOURCES) {
			if (q > 0) {
				cargo.addCommodity(id, q);
				AddRemoveCommodity.addCommodityGainText(id, q, text);
			} else
			if (q < 0) {
				cargo.removeCommodity(id, -q);
				AddRemoveCommodity.addCommodityLossText(id, -q, text);
			}
			return true;
		}
		if (type == CargoItemType.FIGHTER_CHIP) {
			if (q > 0) {
				cargo.addFighters(id, q);
				AddRemoveCommodity.addFighterGainText(id, q, text);
			} else
			if (q < 0) {
				cargo.removeFighters(id, -q);
				AddRemoveCommodity.addFighterLossText(id, -q, text);
			}
			return true;
		}
		if (type == CargoItemType.WEAPONS) {
			if (q > 0) {
				cargo.addWeapons(id, q);
				AddRemoveCommodity.addWeaponGainText(id, q, text);
			} else
			if (q < 0) {
				cargo.removeWeapons(id, -q);
				AddRemoveCommodity.addWeaponLossText(id, -q, text);
			}
			return true;
		}
		if (type == CargoItemType.SPECIAL) {
			SpecialItemData data = new SpecialItemData(id, param);
			if (q > 0) {
				cargo.addSpecial(data, q);
				AddRemoveCommodity.addItemGainText(data, q, text);
			} else
			if (q < 0) {
				cargo.removeItems(CargoItemType.SPECIAL, data, -q);
				AddRemoveCommodity.addItemLossText(data, -q, text);
			}
			return true;
		}
		
		return false;
	}

}
