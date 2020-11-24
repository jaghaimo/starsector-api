package com.fs.starfarer.api.plugins;

import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.characters.PersonAPI;

public interface OfficerLevelupPlugin {

	long getXPForLevel(int level);
	int getMaxLevel(PersonAPI person);
	
	List<String> pickLevelupSkills(PersonAPI person, Random random);
}
