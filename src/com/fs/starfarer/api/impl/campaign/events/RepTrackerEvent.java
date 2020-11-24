package com.fs.starfarer.api.impl.campaign.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.PlayerTradeDataForSubmarket;
import com.fs.starfarer.api.impl.campaign.shared.PlayerTradeProfitabilityData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

/**
 * Trade, time-based rep decay, rep from combat w/ enemies of faction fought against.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2014 Fractal Softworks, LLC
 */
public class RepTrackerEvent extends BaseEventPlugin {
	public static Logger log = Global.getLogger(RepTrackerEvent.class);
	
	public static class FactionTradeRepData {
		public float currVolumePerPoint = Global.getSettings().getFloat("economyPlayerTradeVolumeForRepChangeMin");
		
		public float [] getRepPointsAndVolumeUsedFor(float volume) {
			float max = Global.getSettings().getFloat("economyPlayerTradeVolumeForRepChangeMax");
			float incr = Global.getSettings().getFloat("economyPlayerTradeVolumeForRepChangeIncr");
			float points = 0;
			
			float used = 0;
			
			while (volume >= currVolumePerPoint) {
				points++;
				used += currVolumePerPoint;
				volume -= currVolumePerPoint;
				
				if (currVolumePerPoint < max) currVolumePerPoint += incr;
				if (currVolumePerPoint > max) currVolumePerPoint = max;
			}
			return new float [] {points, used};
		}
	}
	
	
	private IntervalUtil tracker;
	private IntervalUtil repDecayTracker;
	private Map<String, FactionTradeRepData> repData = new HashMap<String, FactionTradeRepData>();
	
	public void init(String type, CampaignEventTarget eventTarget) {
		super.init(type, eventTarget);
		readResolve();
	}
	
	Object readResolve() {
		if (repDecayTracker == null) {
			repDecayTracker = new IntervalUtil(130f, 170f);
		}
		if (tracker == null) {
			tracker = new IntervalUtil(3f, 7f);
		}
		return this;
	}
	
	public void startEvent() {
		super.startEvent();
	}
	
	private float [] getRepPointsAndVolumeUsedFor(float volume, FactionAPI faction) {
		FactionTradeRepData data = repData.get(faction.getId());
		if (data == null) {
			data = new FactionTradeRepData();
			repData.put(faction.getId(), data);
		}
		return data.getRepPointsAndVolumeUsedFor(volume);
	}
	
	public void advance(float amount) {
		if (!isEventStarted()) return;
		if (isDone()) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		
		tracker.advance(days);
		if (tracker.intervalElapsed()) {
			for (FactionAPI faction : Global.getSector().getAllFactions()) {
			//List<FactionAPI> factions = Global.getSector().getAllFactions();
			//FactionAPI faction = factions.get(new Random().nextInt(factions.size()));
				if (!faction.isPlayerFaction() && !faction.isNeutralFaction()) {
					checkForTradeReputationChanges(faction);
				}
			}
			checkForXPGain();
		}
		
//		repDecayTracker.advance(days);
//		if (repDecayTracker.intervalElapsed()) {
//			checkForRepDecay();
//		}
	}
	
	
	private void checkForXPGain() {
		PlayerTradeProfitabilityData data = SharedData.getData().getPlayerActivityTracker().getProfitabilityData();
		final long gain = data.getAccruedXP();
		//final long gain = 1000;
		if (gain > 0) {
			data.setAccruedXP(0);
			Global.getSector().getCampaignUI().addMessage("Gained experience from profitable trades", Misc.getBasePlayerColor());
			Global.getSector().getCharacterData().getPerson().getStats().addXP(gain);
		}
	}
	
	
	public static class MarketTradeInfo {
		public MarketAPI market;
		public float tradeTotal;
		public float smugglingTotal;
	}
	
	private void checkForTradeReputationChanges(final FactionAPI faction) {
		float playerTradeVolume = 0;
		float playerSmugglingVolume = 0;
		
		final List<MarketTradeInfo> info = new ArrayList<MarketTradeInfo>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			//if (market.getFaction() != faction) continue;
			
//			if (faction.getId().equals("hegemony")) {
//				System.out.println("23dsfsdf");
//			}
			
			//boolean isFactionInvolved = market.getFaction() == faction;
			
			MarketTradeInfo curr = new MarketTradeInfo();
			curr.market = market;
			
			for (SubmarketAPI submarket : market.getSubmarketsCopy()) {
				SubmarketPlugin plugin = submarket.getPlugin();
				if (!plugin.isParticipatesInEconomy()) continue;
				
				//isFactionInvolved |= submarket.getFaction() == faction;
				
				PlayerTradeDataForSubmarket tradeData =  SharedData.getData().getPlayerActivityTracker().getPlayerTradeData(submarket);
				
				//if (plugin.isBlackMarket()) {
				if (market.getFaction() == faction && (submarket.getFaction().isHostileTo(faction) || submarket.getPlugin().isBlackMarket())) {
					curr.smugglingTotal += tradeData.getAccumulatedPlayerTradeValueForNegative();
				} else if (submarket.getFaction() == faction) {
					curr.tradeTotal += tradeData.getAccumulatedPlayerTradeValueForPositive();
				}
			}
			
			//if (!isFactionInvolved) continue;
			if (curr.tradeTotal == 0 && curr.smugglingTotal == 0) continue;
			
			info.add(curr);
			
			playerTradeVolume += curr.tradeTotal;
			playerSmugglingVolume += curr.smugglingTotal;
		}
		
//		if (faction.getId().equals("pirates")) {
//			System.out.println("23dsfsdf");
//		}
		
		float [] repPlus = getRepPointsAndVolumeUsedFor(playerTradeVolume, faction);
		float [] repMinus = getRepPointsAndVolumeUsedFor(playerSmugglingVolume, faction);
		final float repChange = repPlus[0] - repMinus[0];
		
		if (Math.abs(repChange) < 1) {
			log.info("Not enough trade/smuggling with " + faction.getDisplayNameWithArticle() + " for a rep change (" + playerTradeVolume + ", " + playerSmugglingVolume + ")");
			return;
		}

		log.info("Sending rep change of " + repChange + " with " + faction.getDisplayNameWithArticle() + " due to trade/smuggling");
		
		float tradeUsed = repPlus[1];
		float smugglingUsed = repMinus[1];
		
		// remove the player trade volume used for the rep change from the accumulated volume
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			//if (market.getFaction() != faction) continue;
			for (SubmarketAPI submarket : market.getSubmarketsCopy()) {
				SubmarketPlugin plugin = submarket.getPlugin();
				if (!plugin.isParticipatesInEconomy()) continue;
				
				if (market.getFaction() != faction && submarket.getFaction() != faction) {
					continue;
				}
				
				PlayerTradeDataForSubmarket tradeData = SharedData.getData().getPlayerActivityTracker().getPlayerTradeData(submarket);
				
				//if (playerTradeVolume > 0 && !plugin.isBlackMarket()) {
				if (playerSmugglingVolume > 0 && market.getFaction() == faction && (submarket.getFaction().isHostileTo(faction) || submarket.getPlugin().isBlackMarket())) {
					float value = tradeData.getAccumulatedPlayerTradeValueForNegative();
					tradeData.setAccumulatedPlayerTradeValueForNegative(value - smugglingUsed * value / playerSmugglingVolume);
				} else if (playerTradeVolume > 0 && submarket.getFaction() == faction) {
					float value = tradeData.getAccumulatedPlayerTradeValueForPositive();
					tradeData.setAccumulatedPlayerTradeValueForPositive(value - tradeUsed * value / playerTradeVolume);
				}
			}
		}
		
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

		if (repChange > 0) {
			Collections.sort(info, new Comparator<MarketTradeInfo>() {
				public int compare(MarketTradeInfo o1, MarketTradeInfo o2) {
					return (int) (o2.tradeTotal - o1.tradeTotal);
				}
			});

			RepActions action = RepActions.TRADE_EFFECT;
			Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(action, new Float(Math.abs(repChange)), null, null, false, true, "Change caused by trade with faction"), 
					faction.getId());

			causeNegativeRepChangeWithEnemies(info);
		} else if (repChange < 0) {
			Collections.sort(info, new Comparator<MarketTradeInfo>() {
				public int compare(MarketTradeInfo o1, MarketTradeInfo o2) {
					return (int) (o2.smugglingTotal - o1.smugglingTotal);
				}
			});
			
			RepActions action = RepActions.SMUGGLING_EFFECT;
			
			Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(action, new Float(Math.abs(repChange)), null, null, false, true, "Change caused by black-market trade"), 
					faction.getId());

			causeNegativeRepChangeWithEnemies(info);
		}
	}
	
	
	public void causeNegativeRepChangeWithEnemies(List<MarketTradeInfo> info) {
		
		int maxToSend = 3;
		int sent = 0;
		for (final MarketTradeInfo curr : info) {
			float actionableTotal = curr.tradeTotal * 2f + curr.smugglingTotal * 0.5f;
			//float repMinus = (float) Math.floor(actionableTotal / volumeForRepChange);
			List<FactionAPI> factions = new ArrayList<FactionAPI>(Global.getSector().getAllFactions());
			Collections.shuffle(factions);
			for (final FactionAPI faction : factions) {
				// pirates don't get mad about you trading with someone else
				//if (faction.getId().equals(Factions.PIRATES)) {
				if (faction.getCustom().optBoolean(Factions.CUSTOM_IGNORE_TRADE_WITH_ENEMIES)) {
					continue;
				}
				if (faction.isPlayerFaction()) continue; // don't report player to themselves for trading with their own enemies
				
				final MarketAPI other = BaseEventPlugin.findNearestMarket(curr.market, new MarketFilter() {
					public boolean acceptMarket(MarketAPI market) {
						if (!market.getFactionId().equals(faction.getId())) {
							return false;
						}
						if (market.getFaction().isAtBest(curr.market.getFaction(), RepLevel.HOSTILE)) {
							return true;
						}
						return false;
					}
				});
				if (other == null) continue;
	
				float dist = Misc.getDistanceLY(curr.market.getLocationInHyperspace(), other.getLocationInHyperspace());
				//if (dist > 2f) continue;
				if (dist > Global.getSettings().getFloat("economyMaxRangeForNegativeTradeRepImpactLY")) continue;
				
				
				float [] repMinus = getRepPointsAndVolumeUsedFor(actionableTotal, faction);
				if (repMinus[0] <= 0) continue;
				
				if (dist <= 0) { // same star system
					repMinus[0] *= 2f;
				}
				
				final float repChange = -repMinus[0];
				
				log.info("Sending rep change of " + repChange + " with " + other.getFaction().getDisplayNameWithArticle() + 
						" due to trade with enemy (" + curr.market.getFaction().getDisplayNameWithArticle() + ")");
				
				RepActions action = RepActions.TRADE_WITH_ENEMY;
				Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(action, new Float(Math.abs(repChange)), null, null, false, true, "Change caused by trade with enemies"), 
						other.getFactionId());

				sent++;
				if (sent >= maxToSend) break;
			}
			if (sent >= maxToSend) break;
		}
	}
	
	public boolean isDone() {
		return false;
	}
}










