package com.fs.starfarer.api.characters;

import java.util.Collection;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface PersonAPI {
	
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
	Collection<String> getTags();
	void clearTags();

	boolean isPlayer();

	boolean isDefault();

	PersonalityAPI getPersonalityAPI();

	String getAICoreId();
	void setAICoreId(String aiCoreId);
	boolean isAICore();

	String getNameString();

	void setGender(Gender gender);

}


