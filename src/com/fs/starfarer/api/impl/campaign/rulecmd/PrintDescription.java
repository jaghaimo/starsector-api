package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.util.Misc.Token;

public class PrintDescription extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		SectorEntityToken target = dialog.getInteractionTarget();
		
		int index = Integer.parseInt(params.get(0).string);
		
		Description desc = Global.getSettings().getDescription(target.getCustomDescriptionId(), Type.CUSTOM);
		if (desc != null) {
			if (index == 1 && desc.hasText1()) {
				dialog.getTextPanel().addParagraph(desc.getText1());
			} else if (index == 2 && desc.hasText2()) {
				dialog.getTextPanel().addParagraph(desc.getText2());
			} else if (index == 3 && desc.hasText3()) {
				dialog.getTextPanel().addParagraph(desc.getText3());
			}
		}	
		return true;
	}
}





	
	
