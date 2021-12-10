package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.util.Misc;

public abstract class BaseBarEventWithPerson extends BaseBarEvent {
	protected PersonAPI person;
	protected long seed;
	protected MarketAPI market = null;
	protected transient Random random;
	
	public BaseBarEventWithPerson() {
		seed = Misc.random.nextLong();
	}
	
	protected void regen(MarketAPI market) {
		if (this.market == market) return;
		this.market = market;
		
		random = new Random(seed + market.getId().hashCode());
		person = createPerson();
	}
	
	protected PersonAPI createPerson() {
		PersonAPI person = Global.getSector().getFaction(getPersonFaction()).createRandomPerson(getPersonGender(), random);
		String p = getPersonPortrait();
		if (p != null) person.setPortraitSprite(p);
		person.setRankId(getPersonRank());
		person.setPostId(getPersonPost());
		return person;
	}
	
	protected String getPersonPortrait() {
		return null;
	}
	
	protected String getPersonFaction() {
		return Factions.INDEPENDENT;
	}
	protected String getPersonRank() {
		return Ranks.CITIZEN;
	}
	protected Gender getPersonGender() {
		return Gender.ANY;
	}
	protected String getPersonPost() {
		return Ranks.CITIZEN;
	}
	
	protected String getManOrWoman() {
		String manOrWoman = "man";
		if (person.getGender() == Gender.FEMALE) manOrWoman = "woman";
		return manOrWoman;
	}
	
	protected String getHeOrShe() {
		String heOrShe = "he";
		if (person.getGender() == Gender.FEMALE) {
			heOrShe = "she";
		}
		return heOrShe;
	}
	
	protected String getHimOrHer() {
		String himOrHer = "him";
		if (person.getGender() == Gender.FEMALE) {
			himOrHer = "her";
		}
		return himOrHer;
	}
	
	protected String getHimOrHerself() {
		String himOrHer = "himself";
		if (person.getGender() == Gender.FEMALE) {
			himOrHer = "herself";
		}
		return himOrHer;
	}
	
	protected String getHisOrHer() {
		String hisOrHer = "his";
		if (person.getGender() == Gender.FEMALE) {
			hisOrHer = "her";
		}
		return hisOrHer;
	}

	@Override
	public boolean isDialogFinished() {
		return done;
	}

	public PersonAPI getPerson() {
		return person;
	}

	public MarketAPI getMarket() {
		return market;
	}

	public Random getRandom() {
		return random;
	}
	
}



