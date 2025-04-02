package com.fs.starfarer.api.impl.campaign.missions.academy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Voices;

public class GAFCReplaceArchon implements Script {

	public void run() {
		//ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		
		MarketAPI laicaille = Global.getSector().getEconomy().getMarket("laicaille_habitat");
		if (laicaille == null) return; // if it somehow managed to decivilize or something
		
		PersonAPI laicailleArchon = Global.getSector().getImportantPeople().getPerson(People.LAICAILLE_ARCHON);
		laicailleArchon.setPostId(Ranks.POST_CITIZEN);
		laicailleArchon.addTag(Tags.INVOLUNTARY_RETIREMENT); // so player can talk to them later.
		laicailleArchon.setImportance(laicailleArchon.getImportance().prev());
		laicaille.getCommDirectory().removePerson(laicailleArchon);
		laicaille.getCommDirectory().addPerson(laicailleArchon, 1000); // back of the comm directory for you, buddy
	
	
		PersonAPI newArchon = Global.getSector().getImportantPeople().getPerson(People.DAMOS_HANNAN);
		//PersonAPI newArchon = Global.getSector().getFaction(Factions.PERSEAN).createRandomPerson();
		//newArchon.setRankId(Ranks.GROUND_COLONEL);
		//newArchon.setPostId(Ranks.POST_BASE_COMMANDER);
		
		// gens Hannan; Kazeronian imperialists, basically.
		//newArchon.getName().setLast("Hannan"); 
		//newArchon.setImportance(PersonImportance.HIGH);
		newArchon.addTag(Tags.REPLACEMENT_ARCHON);
		laicaille.getCommDirectory().getEntryForPerson(newArchon).setHidden(false);
		
		// an aristocratic git
		//newArchon.setVoice(Voices.ARISTO); 
		
		laicaille.getCommDirectory().addPerson(newArchon, 0);
		laicaille.addPerson(newArchon);
		
	}
}


