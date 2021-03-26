package com.fs.starfarer.api.impl.campaign.missions;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;

public class DisruptHeavyIndustry extends BaseDisruptIndustry {

	protected void createBarGiver(MarketAPI createdAt) {
		List<String> posts = new ArrayList<String>();
		posts.add(Ranks.POST_AGENT);
		if (createdAt.getSize() >= 6) {
			posts.add(Ranks.POST_ADMINISTRATOR);
		}
		if (Misc.isMilitary(createdAt)) {
			posts.add(Ranks.POST_BASE_COMMANDER);
		}
		if (Misc.hasOrbitalStation(createdAt)) {
			posts.add(Ranks.POST_STATION_COMMANDER);
		}
		String post = pickOne(posts);
		if (post == null) return;
		
		// rank used only when it's an agent, since the other posts mean the person already exists
		// and doesn't need to be created
		setGiverRank(pickOne(Ranks.GROUND_CAPTAIN, Ranks.GROUND_COLONEL, Ranks.GROUND_MAJOR,
					 Ranks.SPACE_COMMANDER, Ranks.SPACE_CAPTAIN, Ranks.SPACE_ADMIRAL));
		setGiverTags(Tags.CONTACT_MILITARY);
		setGiverPost(post);
		setGiverImportance(pickHighImportance());
		findOrCreateGiver(createdAt, false, false);
	}
	
	@Override
	protected boolean availableAtMarket(MarketAPI createdAt) {
		return Misc.isMilitary(createdAt);
	}
	
	protected String [] getTargetIndustries() {
		return new String[] {Industries.HEAVYINDUSTRY, Industries.ORBITALWORKS};
	}
	
	protected CreditReward getRewardTier() {
		return CreditReward.HIGH;
	}

	@Override
	protected void addExtraTriggers(MarketAPI createdAt) {
		if (market.getSize() <= 4) {
			triggerCreateMediumPatrolAroundMarket(market, Stage.DISRUPT, 0f);
		} else if (market.getSize() <= 6) {
			triggerCreateLargePatrolAroundMarket(market, Stage.DISRUPT, 0f);
		} else {
			triggerCreateMediumPatrolAroundMarket(market, Stage.DISRUPT, 0f);
			triggerCreateLargePatrolAroundMarket(market, Stage.DISRUPT, 0f);
		}
	}
	
	
	
}





