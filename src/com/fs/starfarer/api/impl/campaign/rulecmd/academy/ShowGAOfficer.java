package com.fs.starfarer.api.impl.campaign.rulecmd.academy;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Does not start an actual conversation, but does set them as the active person (so $heOrShe etc tokens work)
 */
public class ShowGAOfficer extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
//		PersonAPI person = Global.getSector().getFaction(Factions.HEGEMONY).createRandomPerson();
//		person.setRankId(Ranks.GROUND_LIEUTENANT);
//		person.setGender(Gender.MALE);
//		person.getName().setFirst("Caliban");
//		person.getName().setLast("Tseen Ke");
//		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "ga_officer"));
		
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(People.HEGEMONY_GA_OFFICER);
		if (person == null) return false;
		
		// so that $herOrShe tokens work
		dialog.getInteractionTarget().setActivePerson(person);
		
		dialog.getVisualPanel().showPersonInfo(person, false, true);
		
		
		// if we did this, it would also change the current memory; don't want that
		//((RuleBasedDialog) dialog.getPlugin()).notifyActivePersonChanged();

		return true;
	}
}










