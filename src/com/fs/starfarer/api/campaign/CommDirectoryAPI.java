package com.fs.starfarer.api.campaign;

import java.util.List;

import com.fs.starfarer.api.characters.PersonAPI;

public interface CommDirectoryAPI {
	String addPerson(PersonAPI person);
	String addPerson(PersonAPI person, int index);
	//String addMissionBoard();
	
	/**
	 * Removes all comm entries associated with this person.
	 * @param person
	 */
	void removePerson(PersonAPI person);
	
	void removeEntry(CommDirectoryEntryAPI entry);
	void removeEntry(String id);
	void clear();
	List<CommDirectoryEntryAPI> getEntriesCopy();
	CommDirectoryEntryAPI getEntryForPerson(PersonAPI person);
	CommDirectoryEntryAPI getEntryForPerson(String personId);

}
