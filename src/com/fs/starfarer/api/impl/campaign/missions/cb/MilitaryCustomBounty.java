package com.fs.starfarer.api.impl.campaign.missions.cb;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;

public class MilitaryCustomBounty extends BaseCustomBounty {

	public static List<CustomBountyCreator> CREATORS = new ArrayList<CustomBountyCreator>();
	static {
		CREATORS.add(new CBPirate());
		CREATORS.add(new CBDeserter());
		CREATORS.add(new CBDerelict());
		CREATORS.add(new CBMerc());
		CREATORS.add(new CBPather());
		CREATORS.add(new CBRemnant());
		CREATORS.add(new CBRemnantPlus());
		CREATORS.add(new CBRemnantStation());
		CREATORS.add(new CBEnemyStation());
	}
	
	@Override
	public List<CustomBountyCreator> getCreators() {
		return CREATORS;
	}

	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		if (Factions.PIRATES.equals(createdAt.getFaction().getId())) {
			return false;
		}
		return super.create(createdAt, barEvent);
	}
	
	@Override
	protected void createBarGiver(MarketAPI createdAt) {
		List<String> posts = new ArrayList<String>();
		if (Misc.isMilitary(createdAt)) {
			posts.add(Ranks.POST_BASE_COMMANDER);
		}
		if (Misc.hasOrbitalStation(createdAt)) {
			posts.add(Ranks.POST_STATION_COMMANDER);
		}
		if (posts.isEmpty()) {
			posts.add(Ranks.POST_GENERIC_MILITARY);
		}
		String post = pickOne(posts);
		setGiverPost(post);
		if (post.equals(Ranks.POST_GENERIC_MILITARY)) {
			setGiverRank(Ranks.SPACE_COMMANDER);
			setGiverImportance(pickImportance());
		} else if (post.equals(Ranks.POST_BASE_COMMANDER)) {
			setGiverRank(Ranks.GROUND_COLONEL);
			setGiverImportance(pickImportance());
		} else if (post.equals(Ranks.POST_STATION_COMMANDER)) {
			setGiverRank(Ranks.SPACE_CAPTAIN);
			setGiverImportance(pickHighImportance());
		}
		setGiverTags(Tags.CONTACT_MILITARY);
		findOrCreateGiver(createdAt, false, false);
		setGiverIsPotentialContactOnSuccess();
	}

	
}











