package com.fs.starfarer.api.impl.campaign.intel.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.SystemBountyIntel;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class CommerceBountyManager implements EveryFrameScript {

	protected IntervalUtil tracker = new IntervalUtil(0.5f, 1.5f);
	
	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}

	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		tracker.advance(days);
		if (tracker.intervalElapsed()) {
//			HostileActivityEventIntel ha = HostileActivityEventIntel.get();
//			if (ha == null) return;
			
			//boolean haLevelSufficient = ha.isStageActive(Stage.HA_1);
			boolean haLevelSufficient = true;
			
			for (StarSystemAPI system : Misc.getPlayerSystems(false)) {
				SystemBountyIntel bounty = getCommerceBounty(system);
				if (bounty != null && bounty.isEnding()) continue;
				
				boolean hasFunctionalCommerce = doesStarSystemHavePlayerCommerceIndustry(system, true);
				boolean hasCommerce = doesStarSystemHavePlayerCommerceIndustry(system, false);
			
				if (bounty != null && (!hasCommerce || !haLevelSufficient)) {
					bounty.endAfterDelay();
					continue;
				}
				
				MarketAPI market = getPlayerCommerceMarket(system);
				if (market == null) continue;
				
				if (bounty == null && hasFunctionalCommerce && haLevelSufficient) {
					float reward = Global.getSettings().getFloat("baseCommerceSystemBounty");
					new SystemBountyIntel(market, (int) reward, true);
				} 
			}
		}
	}
	
	public static SystemBountyIntel getCommerceBounty(StarSystemAPI system) {
		for (IntelInfoPlugin curr : Global.getSector().getIntelManager().getIntel(SystemBountyIntel.class)) {
			SystemBountyIntel intel = (SystemBountyIntel) curr;
			if (intel.getLocation() == system && intel.isCommerceMode()) return intel;
		}
		return null;
	}
	public static boolean doesStarSystemHavePlayerCommerceIndustry(StarSystemAPI system, boolean requireFunctional) {
		for (MarketAPI market : Misc.getMarketsInLocation(system, Factions.PLAYER)) {
			if (requireFunctional && market.hasFunctionalIndustry(Industries.COMMERCE)) {
				return true;
			}
			if (!requireFunctional && market.hasIndustry(Industries.COMMERCE)) {
				return true;
			}
		}
		return false;
	}
	public static MarketAPI getPlayerCommerceMarket(StarSystemAPI system) {
		MarketAPI best = null;
		for (MarketAPI market : Misc.getMarketsInLocation(system, Factions.PLAYER)) {
			if (market.hasIndustry(Industries.COMMERCE)) {
				if (best == null || best.getSize() < market.getSize()) {
					best = market;
				}
			}
		}
		return best;
	}

}




