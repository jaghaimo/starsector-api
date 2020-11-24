package com.fs.starfarer.api.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.PopulationAndInfrastructure;

public class OutpostStats {
	public int outposts;
	public int adminOutposts;
	public int admins;
	public int aiCoreOutposts;
	public int maxOutposts;
	public int maxAdmin;
	public int penalty;
	
	public static OutpostStats get() {
		OutpostStats result = new OutpostStats();
		
		MutableCharacterStatsAPI stats = Global.getSector().getCharacterData().getPerson().getStats();
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (!market.isPlayerOwned()) continue;
			
			if (market.getAdmin().isPlayer() || market.getAdmin().isDefault()) {
				result.outposts++;
			} else {
				result.adminOutposts++;
			}
			if (market.getAdmin().isAICore()) {
				result.aiCoreOutposts++;
			}
		}
		
		result.maxOutposts = stats.getOutpostNumber().getModifiedInt();
		result.maxAdmin = stats.getAdminNumber().getModifiedInt();
		
		result.penalty = PopulationAndInfrastructure.getMismanagementPenalty();
		result.admins = Global.getSector().getCharacterData().getAdmins().size();
		
		return result;
	}
}

