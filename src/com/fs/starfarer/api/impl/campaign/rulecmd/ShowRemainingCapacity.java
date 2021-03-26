package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	ShowRemainingCapacity <commodity id>
 */
public class ShowRemainingCapacity extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String cid = Commodities.SUPPLIES; // show cargo capacity by default
		if (params.size() >= 1) {
			cid = params.get(0).getString(memoryMap);
		}
		
		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(cid);
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		String str = "";
		int cap = 0;
		if (spec.isFuel()) {
			cap = cargo.getFreeFuelSpace();
			if (cap > 1) {
				str += "Your fleet's fuel tanks can hold an additional %s units of fuel.";
			} else {
				str += "Your fleet's fuel tanks are currently full.";
			}
		} else if (spec.isPersonnel()) {
			cap = cargo.getFreeCrewSpace();
			if (cap > 1) {
				str += "Your fleet's crew quarters can accommodate an additional %s personnel.";
			} else {
				str += "Your fleet's crew berths are currently full.";
			}
		} else {
			cap = (int) cargo.getSpaceLeft();
			if (cap > 1) {
				str += "Your fleet's holds can accommodate an additional %s units of cargo.";
			} else {
				str += "Your fleet's cargo holds are currently full.";
			}
		}
		dialog.getTextPanel().addPara(str, Misc.getHighlightColor(), Misc.getWithDGS(cap));
		
		return true;
	}
}


