package com.fs.starfarer.api.impl.campaign.rulecmd.academy;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 * Does not start an actual conversation, but does set them as the active person (so $heOrShe etc tokens work)
 */
public class GAReduceRandomRep extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		WeightedRandomPicker<PersonAPI> picker = new WeightedRandomPicker<PersonAPI>();
		for (PersonDataAPI pd : Global.getSector().getImportantPeople().getPeopleCopy()) {
			PersonAPI person = pd.getPerson();
			if (Ranks.POST_ADMINISTRATOR.equals(person.getPostId()) ||
					Ranks.POST_BASE_COMMANDER.equals(person.getPostId()) ||
					Ranks.POST_BASE_COMMANDER.equals(person.getPostId()) ||
					Ranks.POST_STATION_COMMANDER.equals(person.getPostId())) {
				picker.add(person);
			}
		}
		
		int num = Misc.random.nextInt(2) + 1;
		for (int i = 0; i < num && !picker.isEmpty(); i++) {
			PersonAPI person = picker.pick();
			CustomRepImpact custom = new CustomRepImpact();
			custom.delta = -RepRewards.TINY;
			Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.CUSTOM, custom,
										  null, dialog.getTextPanel(), true, true), 
										  person);
		}
		
		return true;
	}
}










