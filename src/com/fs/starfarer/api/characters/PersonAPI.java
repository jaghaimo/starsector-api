package com.fs.starfarer.api.characters;

import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.HasMemory;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.loading.ContactTagSpec;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface PersonAPI extends HasMemory {
	
	void setPersonality(String personality);
	
	MutableCharacterStatsAPI getStats();

	String getRank();
	String getRankArticle();
	String getRankId();
	void setRankId(String rank);
	
	String getPost();
	String getPostArticle();
	void setPostId(String postId);
	String getPostId();

	FullName getName();
	void setName(FullName name);
	
	String getPortraitSprite();
	void setPortraitSprite(String portraitSprite);

	
	Gender getGender();
	boolean isMale();
	boolean isFemale();

	MemoryAPI getMemory();
	MemoryAPI getMemoryWithoutUpdate();

	boolean wantsToContactPlayer();
	void incrWantsToContactReasons();
	void decrWantsToContactReasons();
	
	float getContactWeight();
	void setContactWeight(float contactWeight);


	FactionAPI getFaction();
	void setFaction(String factionId);

	RelationshipAPI getRelToPlayer();

	String getId();
	boolean hasTag(String tag);
	void addTag(String tag);
	void removeTag(String tag);
	Set<String> getTags();
	void clearTags();

	boolean isPlayer();

	boolean isDefault();

	PersonalityAPI getPersonalityAPI();

	String getAICoreId();
	void setAICoreId(String aiCoreId);
	boolean isAICore();

	String getNameString();

	void setGender(Gender gender);

	/**
	 * For officers, the fleet they're in.
	 * @return
	 */
	CampaignFleetAPI getFleet();
	/**
	 * For officers, the fleet they're in.
	 */
	void setFleet(CampaignFleetAPI fleet);

	MutableCharacterStatsAPI getFleetCommanderStats();

	void setId(String id);

	MarketAPI getMarket();
	void setMarket(MarketAPI market);

	PersonImportance getImportance();
	void setImportance(PersonImportance importance);
	
	/**
	 * The passed in random is used to set the "voice". These are paired since voice is based on importance.
	 * @param importance
	 * @param random
	 */
	void setImportanceAndVoice(PersonImportance importance, Random random);

	String getHisOrHer();

	String getHeOrShe();

	List<ContactTagSpec> getSortedContactTags();

	List<String> getSortedContactTagStrings();

	String getHimOrHer();

	String getManOrWoman();

	String getVoice();
	void setVoice(String voice);

	void setStats(MutableCharacterStatsAPI stats);


}


