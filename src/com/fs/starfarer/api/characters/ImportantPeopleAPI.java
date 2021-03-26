package com.fs.starfarer.api.characters;

import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

public interface ImportantPeopleAPI {
	
	public interface PersonFilter {
		boolean accept(PersonDataAPI personData);
	}
	
	public interface PersonDataAPI {
		PersonLocationAPI getLocation();
		void setLocation(PersonLocationAPI location);
		PersonAPI getPerson();
		Set<String> getCheckedOutFor();
	}
	
	public interface PersonLocationAPI {
		MarketAPI getMarket();
		void setMarket(MarketAPI market);
		SectorEntityToken getEntity();
		void setEntity(SectorEntityToken entity);
		boolean isInMarket();
		boolean isOnFleet();
	}

	void addPerson(PersonAPI person);
	
	boolean containsPerson(PersonAPI person);
	void removePerson(PersonAPI person);
	void removePerson(String id);
	
	List<PersonDataAPI> getPeopleCopy();
	List<PersonAPI> getPeopleWithRank(String rankId);
	List<PersonAPI> getPeopleWithPost(String postId);
	
	PersonDataAPI getData(PersonAPI person);
	PersonDataAPI getData(String id);
	PersonAPI getPerson(String id);
	
	boolean canCheckOutPerson(PersonAPI person, String reasonId);
	void checkOutPerson(PersonAPI person, String reasonId);
	void returnPerson(PersonAPI person, String reasonId);

	List<PersonDataAPI> getMatching(PersonFilter filter);

	PersonDataAPI getPerson(FactionAPI faction, MarketAPI market,
			String checkoutReason, String defaultRank, String ... postIds);

	PersonDataAPI getPerson(String factionId, MarketAPI market,
			String checkoutReason, String defaultRank, String ... postIds);

	boolean isCheckedOutForAnything(PersonAPI person);

	PersonDataAPI getPerson(Random random, FactionAPI faction,
			MarketAPI market, String checkoutReason, String defaultRank,
			String... postIds);

	boolean isLastGetPersonResultWasExistingPerson();

	void excludeFromGetPerson(PersonAPI person);
	void resetExcludeFromGetPerson();

	
	
}
