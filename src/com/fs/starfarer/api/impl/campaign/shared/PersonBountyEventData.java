package com.fs.starfarer.api.impl.campaign.shared;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.impl.campaign.ids.Factions;

public class PersonBountyEventData {

	private List<String> participatingFactions = new ArrayList<String>();
	
	private int level = 0;
	private int successesThisLevel = 0;

	public PersonBountyEventData() {
		addParticipatingFaction(Factions.HEGEMONY);
		addParticipatingFaction(Factions.DIKTAT);
		addParticipatingFaction(Factions.LUDDIC_CHURCH);
		addParticipatingFaction(Factions.TRITACHYON);
		addParticipatingFaction(Factions.INDEPENDENT);
		addParticipatingFaction(Factions.PERSEAN);
	}

	public void reportSuccess() {
		successesThisLevel++;
		
		int threshold = getThresholdForLevel(level);
		if (successesThisLevel >= threshold) {
			level++;
			successesThisLevel = 0;
		}
		if (level > 10) level = 10;
	}
	
	public int getThresholdForLevel(int level) {
		if (level == 0) return 2;
		if (level == 1) return 2;
		if (level == 2) return 2;
		
		if (level == 3) return 3;
		if (level == 4) return 3;
		if (level == 5) return 3;
		if (level == 6) return 3;
		
		if (level == 7) return 4;
		if (level == 8) return 5;
		if (level == 9) return 6;
		
		return 6;
	}
	
	public int getLevel() {
		//if (true) return 10;
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}

	public void setSuccessesThisLevel(int successesThisLevel) {
		this.successesThisLevel = successesThisLevel;
	}

	public List<String> getParticipatingFactions() {
		return participatingFactions;
	}
	
	public void addParticipatingFaction(String factionId) {
		participatingFactions.add(factionId);
	}
	
	public void removeParticipatingFaction(String factionId) {
		participatingFactions.remove(factionId);
	}
	
	public boolean isParticipating(String factionId) {
		return participatingFactions.contains(factionId);
	}
}

