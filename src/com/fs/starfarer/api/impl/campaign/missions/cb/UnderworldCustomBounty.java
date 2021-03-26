package com.fs.starfarer.api.impl.campaign.missions.cb;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class UnderworldCustomBounty extends BaseCustomBounty {

	public static List<CustomBountyCreator> CREATORS = new ArrayList<CustomBountyCreator>();
	static {
		CREATORS.add(new CBTrader());
		CREATORS.add(new CBPatrol());
		CREATORS.add(new CBMercUW());
	}
	
	@Override
	public List<CustomBountyCreator> getCreators() {
		return CREATORS;
	}

	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		return super.create(createdAt, barEvent);
	}
	
	@Override
	protected void createBarGiver(MarketAPI createdAt) {
		setGiverRank(Ranks.CITIZEN);
		setGiverPost(pickOne(Ranks.POST_SMUGGLER, Ranks.POST_GANGSTER, 
					 		 Ranks.POST_FENCE, Ranks.POST_CRIMINAL));
		setGiverImportance(pickImportance());
		setGiverFaction(Factions.PIRATES);
		setGiverTags(Tags.CONTACT_UNDERWORLD);
		findOrCreateGiver(createdAt, false, false);
		setGiverIsPotentialContactOnSuccess();
	}

	
}













