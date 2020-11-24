package com.fs.starfarer.api.impl.campaign.intel;

import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.ListMap;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SystemBountyManager extends BaseEventManager {

	public static final String KEY = "$core_systemBountyManager";
	
	public static SystemBountyManager getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (SystemBountyManager) test; 
	}
	
	public SystemBountyManager() {
		super();
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
	}
	
	@Override
	protected int getMinConcurrent() {
		return Global.getSettings().getInt("minSystemBounties");
	}
	@Override
	protected int getMaxConcurrent() {
		return Global.getSettings().getInt("maxSystemBounties");
	}

	@Override
	protected EveryFrameScript createEvent() {
		//if ((float) Math.random() < 0.1f) return null;
		
		MarketAPI market = pickMarket();
		if (market == null) return null;
		
		SystemBountyIntel intel = new SystemBountyIntel(market);
		return intel;
	}
	
	
	public boolean isActive(MarketAPI market) {
		for (EveryFrameScript s : getActive()) {
			if (market == ((SystemBountyIntel)s).getMarket()) return true;
		}
		return false;
	}
	
	public SystemBountyIntel getActive(MarketAPI market) {
		for (EveryFrameScript s : getActive()) {
			SystemBountyIntel intel = (SystemBountyIntel) s;
			if (intel.isDone()) continue;
			
			if (market == intel.getMarket()) return intel;
		}
		return null;
	}
	
	public void addOrResetBounty(MarketAPI market) {
		if (market != null) {
			SystemBountyIntel active = getActive(market);
			if (active != null) {
				active.reset();
			} else {
				addActive(new SystemBountyIntel(market));
			}
		}
	}
	
	protected MarketAPI pickMarket() {
		Set<MarketAPI> already = new HashSet<MarketAPI>();
		for (EveryFrameScript s : getActive()) {
			already.add(((SystemBountyIntel)s).getMarket());
		}
		
		ListMap<MarketAPI> locationIdToMarket = new ListMap<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isHidden()) continue;
			locationIdToMarket.add(market.getContainingLocation().getId(), market);
		}
		
		WeightedRandomPicker<MarketAPI> pickerWithPirateActivity = new WeightedRandomPicker<MarketAPI>();
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>();
		OUTER: for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isHidden()) continue;
			if (market.getSize() <= 4) continue;
			if (market.isPlayerOwned()) continue;
			if (already.contains(market)) continue;
			
			if (market.getFaction().getCustom().optBoolean(Factions.CUSTOM_POSTS_NO_BOUNTIES)) {
				continue;
			}
			
			if (Global.getSector().isInNewGameAdvance() && market.getId().equals("jangala")) {
				continue; // want a fresh one to be auto-started there on game start
			}

			for (MarketAPI other : locationIdToMarket.getList(market.getContainingLocation().getId())) {
				if (market.getFaction() == other.getFaction() && already.contains(other)) {
					continue OUTER;
				}
			}
			
			float w = market.getSize() * 0.25f;
			for (MarketAPI other : locationIdToMarket.getList(market.getContainingLocation().getId())) {
				if (market.getFaction().isHostileTo(other.getFaction())) {
//					w += other.getSize() * 25f;
//					if (other.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_MILITARY) ||
//							other.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_PATROL)) {
//						w += other.getSize() * 25f;
//					}
					w += 10f;
					if (other.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_MILITARY) ||
							other.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_PATROL)) {
						w += 10f;
					}
					if (market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_MILITARY)) {
						w += 10f;
					} else if (market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_PATROL)) {
						w += 5f;
					}
				}
			}
			
			if (market.hasCondition(Conditions.PIRATE_ACTIVITY)) {
				w += 10f;
				pickerWithPirateActivity.add(market, w);
			} else {
				picker.add(market, w);
			}
		}
		
		//picker.print("Bounty weights: ");
		
		MarketAPI market = pickerWithPirateActivity.pick();
		float w = pickerWithPirateActivity.getWeight(market);
		if (market == null) {
			market = picker.pick();
			w = picker.getWeight(market);
		}
		
		float probMult = 1f / (getOngoing() + 1f);
		
		if ((float) Math.random() > w * 0.01f * probMult) {
			market = null;
		}
		
		return market;
		
	}
	
}








