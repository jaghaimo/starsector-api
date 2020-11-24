package com.fs.starfarer.api.fleet;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.util.DynamicStatsAPI;

public interface MutableFleetStatsAPI {
	
	void addTemporaryModFlat(float durInDays, String source, float value, StatBonus stat);
	void addTemporaryModFlat(float durInDays, String source, String desc, float value, StatBonus stat);
	void addTemporaryModMult(float durInDays, String source, String desc, float value, StatBonus stat);
	void addTemporaryModPercent(float durInDays, String source, float value, StatBonus stat);
	void addTemporaryModPercent(float durInDays, String source, String desc, float value, StatBonus stat);
	
	void addTemporaryModFlat(float durInDays, String source, String desc, float value, MutableStat stat);
	void addTemporaryModMult(float durInDays, String source, String desc, float value, MutableStat stat);
	void addTemporaryModFlat(float durInDays, String source, float value, MutableStat stat);
	void addTemporaryModPercent(float durInDays, String source, String desc, float value, MutableStat stat);
	void addTemporaryModPercent(float durInDays, String source, float value, MutableStat stat);
	
	boolean hasMod(String source);
	
	StatBonus getMovementSpeedMod();
	StatBonus getFleetwideMaxBurnMod();
	StatBonus getSensorStrengthMod();
	StatBonus getSensorProfileMod();
	StatBonus getSensorRangeMod();
	StatBonus getDetectedRangeMod();
	MutableStat getAccelerationMult();
	MutableStat getFuelUseHyperMult();
	MutableStat getFuelUseNormalMult();
	
	void removeTemporaryMod(String source);
	
	
	DynamicStatsAPI getDynamic();
	
}
