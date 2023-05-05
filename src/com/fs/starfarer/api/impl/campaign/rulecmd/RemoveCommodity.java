package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.MutableValue;

/**
 *	AddRemoveCommodity <commodity id> <quantity> <withText>
 */
public class RemoveCommodity extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String commodityId = params.get(0).getString(memoryMap);
		float quantity = 0;
		int next = 2;
		if (params.get(1).isOperator()) {
			quantity = -1 * params.get(2).getFloat(memoryMap);
			next = 3;
		} else {
			quantity = params.get(1).getFloat(memoryMap);
		}
		
		quantity = -quantity;
		
		boolean withText = Math.abs(quantity) >= 1;
		if (dialog != null && params.size() >= next + 1) {
			withText = params.get(next).getBoolean(memoryMap) && withText;
		}
		
		if (commodityId.equals("credits")) {
			MutableValue credits = Global.getSector().getPlayerFleet().getCargo().getCredits();
			if (quantity > 0) {
				credits.add(quantity);
				if (withText) {
					AddRemoveCommodity.addCreditsGainText((int) quantity, dialog.getTextPanel());
				}
			} else {
				credits.subtract(Math.abs(quantity));
				if (credits.get() < 0) credits.set(0);
				if (withText) {
					AddRemoveCommodity.addCreditsLossText((int) Math.abs(quantity), dialog.getTextPanel());
				}
			}
		} else {
//			if (quantity < 0) {
//				quantity = -1f * Math.max(Math.abs(quantity), Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity(commodityId));
//			}
			if (quantity > 0) {
				Global.getSector().getPlayerFleet().getCargo().addCommodity(commodityId, quantity);
				if (withText) {
					AddRemoveCommodity.addCommodityGainText(commodityId, (int) quantity, dialog.getTextPanel());
				}
			} else {
				Global.getSector().getPlayerFleet().getCargo().removeCommodity(commodityId, Math.abs(quantity));
				if (withText) {
					AddRemoveCommodity.addCommodityLossText(commodityId, (int) Math.abs(quantity), dialog.getTextPanel());
				}
			}
		}
		
		if (!"credits".equals(commodityId)) {
			// update $supplies, $fuel, etc if relevant
			AddRemoveCommodity.updatePlayerMemoryQuantity(commodityId);
		}
		
		return true;
	}
	

}



