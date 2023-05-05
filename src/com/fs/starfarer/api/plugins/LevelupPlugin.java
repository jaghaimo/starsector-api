package com.fs.starfarer.api.plugins;

public interface LevelupPlugin {

	long getXPForLevel(int level);
	int getPointsAtLevel(int level);
	int getMaxLevel();
	int getStoryPointsPerLevel();
	//long getXPForNextLevel(int level);
	int getBonusXPUseMultAtMaxLevel();
}
