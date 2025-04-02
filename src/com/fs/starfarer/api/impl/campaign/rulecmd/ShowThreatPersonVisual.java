package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc.Token;

public class ShowThreatPersonVisual extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		SectorEntityToken target = dialog.getInteractionTarget();
		if (target == null) return false;
		
		target.getMemoryWithoutUpdate().set(FleetInteractionDialogPluginImpl.DO_NOT_AUTO_SHOW_FC_PORTRAIT, true, 0f);
		PersonAPI person = Global.getSector().getFaction(Factions.THREAT).createRandomPerson();
		dialog.getVisualPanel().showPersonInfo(person, false, false);
		dialog.getVisualPanel().hideRankNamePost();
	
		return true;
	}

}
