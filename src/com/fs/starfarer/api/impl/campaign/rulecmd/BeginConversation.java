package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI.EntryType;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Person must have been added to SectorAPI.getImportantPeople().
 * 
 * BeginConversation <person id> <minimal mode (no faction shown), optional> <show relationship bar>
 * 
 * <person id> can also be POST:<post id> which will find the first person in the comm directory with that post.
 */
public class BeginConversation extends BaseCommandPlugin {
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String id = null;
		PersonAPI person = null;
		
		Object o = params.get(0).getObject(memoryMap);
		if (o instanceof PersonAPI) {
			person = (PersonAPI) o;
		} else {
			id = params.get(0).getStringWithTokenReplacement(ruleId, dialog, memoryMap);
		}
		
		boolean minimal = false;
		boolean showRel = true;
		if (params.size() > 1) {
			minimal = params.get(1).getBoolean(memoryMap);
		}
		if (params.size() > 2) {
			showRel = params.get(2).getBoolean(memoryMap);
		}
		
		if (person == null) {
			PersonDataAPI data = Global.getSector().getImportantPeople().getData(id);
			
			if (data == null) {
				if (dialog.getInteractionTarget() != null && dialog.getInteractionTarget().getMarket() != null) {
					if (id.startsWith("POST:")) {
						String postId = id.substring(id.indexOf(":") + 1);
						for (CommDirectoryEntryAPI entry : dialog.getInteractionTarget().getMarket().getCommDirectory().getEntriesCopy()) {
							if (entry.getType() == EntryType.PERSON && entry.getEntryData() instanceof PersonAPI) {
								PersonAPI curr = (PersonAPI) entry.getEntryData();
								if (postId.equals(curr.getPostId())) {
									person = curr;
									break;
								}
							}
						}
					} else {
						for (PersonAPI curr : dialog.getInteractionTarget().getMarket().getPeopleCopy()) {
							if (curr.getId().equals(id)) {
								person = curr;
								break;
							}
						}
						if (person == null) {
							CommDirectoryEntryAPI entry = dialog.getInteractionTarget().getMarket().getCommDirectory().getEntryForPerson(id);
							if (entry != null) {
								person = (PersonAPI) entry.getEntryData();
							}
						}
					}
				}
			} else {
				person = data.getPerson();
			}
		}
		
		if (person == null) return false;
		
		dialog.getInteractionTarget().setActivePerson(person);
		((RuleBasedDialog) dialog.getPlugin()).notifyActivePersonChanged();
		dialog.getVisualPanel().showPersonInfo(person, minimal, showRel);

		return true;
	}

}










