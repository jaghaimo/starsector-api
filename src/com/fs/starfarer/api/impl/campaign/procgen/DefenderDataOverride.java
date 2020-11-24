package com.fs.starfarer.api.impl.campaign.procgen;

public class DefenderDataOverride {
	public float probDefenders, minStr, maxStr;
	public int maxDefenderSize = 4;
	public float probStation = 0f;
	public String stationRole;
	public String defFaction;

	
	
	public DefenderDataOverride(String defFaction, float probDefenders, float minStr, float maxStr) {
		this.defFaction = defFaction;
		this.probDefenders = probDefenders;
		this.minStr = minStr;
		this.maxStr = maxStr;
	}

	public DefenderDataOverride(String defFaction, float probDefenders, float minStr, float maxStr, int maxDefenderSize) {
		this.defFaction = defFaction;
		this.probDefenders = probDefenders;
		this.minStr = minStr;
		this.maxStr = maxStr;
		this.maxDefenderSize = maxDefenderSize;
	}

	public DefenderDataOverride(String defFaction, float probDefenders, float minStr, float maxStr, int maxDefenderSize,
								float probStation, String stationRole) {
		this.defFaction = defFaction;
		this.probDefenders = probDefenders;
		this.minStr = minStr;
		this.maxStr = maxStr;
		this.maxDefenderSize = maxDefenderSize;
		this.probStation = probStation;
		this.stationRole = stationRole;
	}
	
	
}
