package com.fs.starfarer.api.characters;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonDataAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonFilter;

public class MarketPostPersonFilter implements PersonFilter {

	private final String marketId;
	private final String [] postIds;
	private final String checkoutReason;

	public MarketPostPersonFilter(String marketId, String checkoutReason, String ... postIds) {
		this.marketId = marketId;
		this.checkoutReason = checkoutReason;
		this.postIds = postIds;
	}
	
	public boolean accept(PersonDataAPI personData) {
		if (!Global.getSector().getImportantPeople().canCheckOutPerson(personData.getPerson(), checkoutReason)) {
			return false;
		}
		
		if (postIds != null) {
			boolean postMatchFound = false;
			for (String id : postIds) {
				if (id.equals(personData.getPerson().getPostId())) {
					postMatchFound = true;
				}
			}
			if (!postMatchFound) return false;
		}
		
		MarketAPI market = personData.getLocation().getMarket();
		if (market == null || !market.getId().equals(marketId)) return false;
		return true;
	}

}
