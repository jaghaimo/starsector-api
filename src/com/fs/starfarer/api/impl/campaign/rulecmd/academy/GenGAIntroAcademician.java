package com.fs.starfarer.api.impl.campaign.rulecmd.academy;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Does not start an actual conversation, but does set them as the active person (so $heOrShe etc tokens work)
 */
public class GenGAIntroAcademician extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		PersonAPI person = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson();
		person.setRankId(Ranks.CITIZEN);
		person.setPostId(Ranks.POST_ACADEMICIAN);
		
		// so that $herOrShe tokens work
		dialog.getInteractionTarget().setActivePerson(person);
		
		dialog.getVisualPanel().showPersonInfo(person, false, true);

		return true;
	}
}










