package com.fs.starfarer.api.impl.campaign.missions;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class DisruptSpaceport extends BaseDisruptIndustry {

	protected void createBarGiver(MarketAPI createdAt) {
		setGiverRank(Ranks.CITIZEN);
		setGiverPost(pickOne(Ranks.POST_SMUGGLER, Ranks.POST_GANGSTER, 
					 		 Ranks.POST_FENCE, Ranks.POST_CRIMINAL));
		setGiverImportance(pickHighImportance());
		setGiverFaction(Factions.PIRATES);
		setGiverTags(Tags.CONTACT_UNDERWORLD);
		findOrCreateGiver(createdAt, false, false);
	}
	
	protected String [] getTargetIndustries() {
		return new String[] {Industries.SPACEPORT, Industries.MEGAPORT};
	}
	
	protected CreditReward getRewardTier() {
		return CreditReward.HIGH;
	}

	@Override
	protected boolean requireFactionHostile() {
		return false;
	}
	
}





