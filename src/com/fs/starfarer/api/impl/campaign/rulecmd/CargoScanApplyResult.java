package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.rulecmd.CargoScan.CargoScanResult;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class CargoScanApplyResult extends BaseCommandPlugin {

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CampaignFleetAPI other = (CampaignFleetAPI) dialog.getInteractionTarget();
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		
		CargoScanResult result = (CargoScanResult) memory.get(CargoScan.RESULT_KEY);
		
		TextPanelAPI text = dialog.getTextPanel();
		Color red = Misc.getNegativeHighlightColor();
		
		for (CargoStackAPI stack : result.getIllegalFound().getStacksCopy()) {
			playerFleet.getCargo().removeItems(stack.getType(), stack.getData(), stack.getSize());
			if (stack.isCommodityStack()) {
				AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), (int)stack.getSize(), text);
			} else {
				text.setFontSmallInsignia();
				text.addParagraph("Lost " + (int) stack.getSize() + Strings.X + " " + stack.getDisplayName(), red);
				text.highlightLastInLastPara("" + (int) stack.getSize() + Strings.X, Misc.getHighlightColor());
			}
			text.setFontInsignia();
		}
		
		return true;
	}

}







