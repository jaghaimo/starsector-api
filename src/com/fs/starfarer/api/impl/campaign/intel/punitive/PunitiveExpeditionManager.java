package com.fs.starfarer.api.impl.campaign.intel.punitive;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.JSONObject;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.CommodityMarketDataAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class PunitiveExpeditionManager implements EveryFrameScript {

	public static final String KEY = "$core_punitiveExpeditionManager";
	public static PunitiveExpeditionManager getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (PunitiveExpeditionManager) test; 
	}
	
	public static int MAX_CONCURRENT = Global.getSettings().getInt("punExMaxConcurrent");
	public static float PROB_TIMEOUT_PER_SENT = Global.getSettings().getFloat("punExProbTimeoutPerExpedition");
	public static float MIN_TIMEOUT = Global.getSettings().getFloatFromArray("punExTimeoutDays", 0);
	public static float MAX_TIMEOUT = Global.getSettings().getFloatFromArray("punExTimeoutDays", 1);
	
	public static int MIN_COLONY_SIZE_FOR_NON_TERRITORIAL = Global.getSettings().getInt("punExMinColonySizeForNonTerritorial");
	
	
	// if more factions send non-territorial expeditions, longer timeout
	public static float TARGET_NUMBER_FOR_FREQUENCY = 5f;
	
	public static float ANGER_BUILDUP_MULT = 0.5f;
	
	public static int FACTION_MUST_BE_IN_TOP_X_PRODUCERS = 3;
	//public static float PLAYER_FRACTION_TO_NOTICE = 0.33f;
	public static float PLAYER_FRACTION_TO_NOTICE = 0.5f;
	//public static final float MAX_THRESHOLD = 1000f;
	public static float MAX_THRESHOLD = 600f;
	
	public static enum PunExType {
		ANTI_COMPETITION,
		ANTI_FREE_PORT,
		TERRITORIAL,
	}
	
	public static enum PunExGoal {
		RAID_PRODUCTION,
		RAID_SPACEPORT,
		BOMBARD,
		//EVACUATE,
	}
	
	public static class PunExReason {
		public PunExType type;
		public String commodityId;
		public String marketId;
		public float weight;
		public PunExReason(PunExType type) {
			this.type = type;
		}
	}
	
	public static class PunExData {
		public FactionAPI faction;
		public IntervalUtil tracker = new IntervalUtil(20f, 40f);
		public float anger = 0f;
		public float threshold = 100f;
		public float timeout = 0f;;
		public BaseIntelPlugin intel;
		public Random random = new Random();
		
		public int numSuccesses = 0;
		public int numAttempts = 0;
	}
	
	protected float timeout = 0f;
	protected int numSentSinceTimeout = 0;
	protected LinkedHashMap<FactionAPI, PunExData> data = new LinkedHashMap<FactionAPI, PunExData>();
	
	public PunitiveExpeditionManager() {
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
	}
	
	protected Object readResolve() {
		return this;
	}
	
	public PunExData getDataFor(FactionAPI faction) {
		return data.get(faction);
	}
	
	
	public LinkedHashMap<FactionAPI, PunExData> getData() {
		return data;
	}

	public void advance(float amount) {
		
		float days = Misc.getDays(amount);
		
		Set<FactionAPI> seen = new HashSet<FactionAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsInGroup(null)) {
			if (market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_MILITARY)) {
				FactionAPI faction = market.getFaction();
				if (Misc.getCommissionFaction() == faction) continue;
				
				if (seen.contains(faction) || data.containsKey(faction)) {
					seen.add(faction);
					continue;
				}
				JSONObject json = faction.getCustom().optJSONObject(Factions.CUSTOM_PUNITIVE_EXPEDITION_DATA);
				if (json != null) {
					PunExData curr = new PunExData();
					curr.faction = faction;
					data.put(faction, curr);
					seen.add(faction);
				}
			}
		}
		data.keySet().retainAll(seen);

		if (timeout > 0) {
			timeout -= days * (DebugFlags.PUNITIVE_EXPEDITION_DEBUG ? 1000f : 1f);
			if (timeout <= 0) {
				timeout = 0;
				numSentSinceTimeout = 0;
			}
			return;
		}
		
		boolean first = true;
		for (PunExData curr : data.values()) {
			if (first && DebugFlags.PUNITIVE_EXPEDITION_DEBUG) {
				days *= 1000f;
				curr.timeout = 0f;
				curr.anger = 1000f;
			}
			first = false;
			
			if (curr.intel != null) {
				if (curr.intel.isEnded()) {
					curr.timeout = 100f + 100f * curr.random.nextFloat();
					
					if (curr.intel instanceof PunitiveExpeditionIntel) {
						PunitiveExpeditionIntel intel = (PunitiveExpeditionIntel) curr.intel;
						if (!intel.isTerritorial()) {
							curr.timeout += getExtraTimeout(curr);
						}
					}
					
					curr.intel = null;
				}
			} else {
				curr.timeout -= days;
				if (curr.timeout <= 0) curr.timeout = 0;
			}
			
			
			curr.tracker.advance(days);
			//System.out.println(curr.tracker.getElapsed());
			if (curr.tracker.intervalElapsed() && 
					curr.intel == null && 
					curr.timeout <= 0) {
				checkExpedition(curr);
			}
		}
	}
	
	public float getExtraTimeout(PunExData d) {
		float total = 0f;
		for (PunExData curr : data.values()) {
			JSONObject json = curr.faction.getCustom().optJSONObject(Factions.CUSTOM_PUNITIVE_EXPEDITION_DATA);
			if (json == null) continue;
			
			List<MarketAPI> markets = Misc.getFactionMarkets(curr.faction, null);
			if (markets.isEmpty()) continue;
			
			boolean vsCompetitors = json.optBoolean("vsCompetitors", false);
			boolean vsFreePort = json.optBoolean("vsFreePort", false);
			boolean territorial = json.optBoolean("territorial", false);
			
			if (vsCompetitors || vsFreePort) {
				total++;
			}
		}
		
		return Math.min(10f, Math.max(0, total - TARGET_NUMBER_FOR_FREQUENCY)) * 
					(MIN_TIMEOUT * 0.9f + MIN_TIMEOUT * 0.9f * d.random.nextFloat());
	}
	
	
	public int getOngoing() {
		int ongoing = 0;
		for (PunExData d : data.values()) {
			if (d.intel != null) {
				ongoing++;
			}
		}
		//ongoing = 0;
		return ongoing;
	}

	protected void checkExpedition(PunExData curr) {
		JSONObject json = curr.faction.getCustom().optJSONObject(Factions.CUSTOM_PUNITIVE_EXPEDITION_DATA);
		if (json == null) return;
		
//		if (curr.faction.getId().equals(Factions.TRITACHYON)) {
//			System.out.println("wefwefwe");
//		}
		List<PunExReason> reasons = getExpeditionReasons(curr);
//		if (!reasons.isEmpty()) {
//			System.out.println("HERE");
//		}
		float total = 0f;
		for (PunExReason reason : reasons) {
			total += reason.weight;
		}
		
		total *= ANGER_BUILDUP_MULT;
		
		curr.anger += total * (0.25f + curr.random.nextFloat() * 0.75f);
		if (curr.anger >= curr.threshold) {
			if (getOngoing() >= MAX_CONCURRENT) {
				curr.anger = 0;
			} else {
				createExpedition(curr);
			}
		}
	}
	
	public static float COMPETITION_PRODUCTION_MULT = 20f;
	public static float ILLEGAL_GOODS_MULT = 3f;
	public static float FREE_PORT_SIZE_MULT = 5f;
	public static float TERRITORIAL_ANGER = 500f;
	
	public List<PunExReason> getExpeditionReasons(PunExData curr) {
		List<PunExReason> result = new ArrayList<PunExReason>();

		JSONObject json = curr.faction.getCustom().optJSONObject(Factions.CUSTOM_PUNITIVE_EXPEDITION_DATA);
		if (json == null) return result;
		
		List<MarketAPI> markets = Misc.getFactionMarkets(curr.faction, null);
		if (markets.isEmpty()) return result;
		
		boolean vsCompetitors = json.optBoolean("vsCompetitors", false);
		boolean vsFreePort = json.optBoolean("vsFreePort", false);
		boolean territorial = json.optBoolean("territorial", false);
		
		MarketAPI test = markets.get(0);
		FactionAPI player = Global.getSector().getPlayerFaction();
		
		if (vsCompetitors) {
			for (CommodityOnMarketAPI com : test.getAllCommodities()) {
				if (com.isNonEcon()) continue;
				if (curr.faction.isIllegal(com.getId())) continue;
				
				CommodityMarketDataAPI cmd = com.getCommodityMarketData();
				if (cmd.getMarketValue() <= 0) continue;
				
				Map<FactionAPI, Integer> shares = cmd.getMarketSharePercentPerFaction();
				int numHigher = 0;
				int factionShare = shares.get(curr.faction);
				if (factionShare <= 0) continue;
				
				for (FactionAPI faction : shares.keySet()) {
					if (curr.faction == faction) continue;
					if (shares.get(faction) > factionShare) {
						numHigher++;
					}
				}
				
				if (numHigher >= FACTION_MUST_BE_IN_TOP_X_PRODUCERS) continue;
				
				int playerShare = cmd.getMarketSharePercent(player);
				float threshold = PLAYER_FRACTION_TO_NOTICE;
				if (DebugFlags.PUNITIVE_EXPEDITION_DEBUG) {
					threshold = 0.1f;
				}
				if (playerShare < factionShare * threshold || playerShare <= 0) continue;
				
				PunExReason reason = new PunExReason(PunExType.ANTI_COMPETITION);
				reason.weight = (float)playerShare / (float)factionShare * COMPETITION_PRODUCTION_MULT;
				reason.commodityId = com.getId();
				result.add(reason);
			}
		}
		
		if (vsFreePort) {
			for (MarketAPI market : Global.getSector().getEconomy().getMarketsInGroup(null)) {
				if (!market.isPlayerOwned()) continue;
				if (!market.isFreePort()) continue;
				if (market.isInHyperspace()) continue;
				
				for (CommodityOnMarketAPI com : test.getAllCommodities()) {
					if (com.isNonEcon()) continue;
					if (!curr.faction.isIllegal(com.getId())) continue;
					
					CommodityMarketDataAPI cmd = com.getCommodityMarketData();
					if (cmd.getMarketValue() <= 0) continue;
					
					int playerShare = cmd.getMarketSharePercent(player);
					if (playerShare <= 0) continue;
					
					PunExReason reason = new PunExReason(PunExType.ANTI_FREE_PORT);
					reason.weight = playerShare * ILLEGAL_GOODS_MULT;
					reason.commodityId = com.getId();
					reason.marketId = market.getId();
					result.add(reason);
				}
				
				if (market.isFreePort()) {
					PunExReason reason = new PunExReason(PunExType.ANTI_FREE_PORT);
					reason.weight = Math.max(1, market.getSize() - 2) * FREE_PORT_SIZE_MULT;
					reason.marketId = market.getId();
					result.add(reason);
				}
			}
		}
		
		if (territorial) {
			int maxSize = MarketCMD.getBombardDestroyThreshold();
			for (MarketAPI market : Global.getSector().getEconomy().getMarketsInGroup(null)) {
				if (!market.isPlayerOwned()) continue;
				if (market.isInHyperspace()) continue;
				
				boolean destroy = market.getSize() <= maxSize;
				if (!destroy) continue;
				
				FactionAPI claimedBy = Misc.getClaimingFaction(market.getPrimaryEntity());
				if (claimedBy != curr.faction) continue;
				
				PunExReason reason = new PunExReason(PunExType.TERRITORIAL);
				reason.weight = TERRITORIAL_ANGER;
				reason.marketId = market.getId();
				result.add(reason);
			}
		}
		
		return result;
	}
	

	public void createExpedition(PunExData curr) {
		createExpedition(curr, null);
	}
	public void createExpedition(PunExData curr, Integer fpOverride) {
		
		JSONObject json = curr.faction.getCustom().optJSONObject(Factions.CUSTOM_PUNITIVE_EXPEDITION_DATA);
		if (json == null) return;
		
//		boolean vsCompetitors = json.optBoolean("vsCompetitors", false);
//		boolean vsFreePort = json.optBoolean("vsFreePort", false);
		boolean canBombard = json.optBoolean("canBombard", false);
//		boolean territorial = json.optBoolean("territorial", false);
		
		List<PunExReason> reasons = getExpeditionReasons(curr);
		WeightedRandomPicker<PunExReason> reasonPicker = new WeightedRandomPicker<PunExReason>(curr.random);
		for (PunExReason r : reasons) {
			//if (r.type == PunExType.ANTI_COMPETITION) continue;
			reasonPicker.add(r, r.weight);
		}
		PunExReason reason = reasonPicker.pick();
		if (reason == null) return;
		
		
		WeightedRandomPicker<MarketAPI> targetPicker = new WeightedRandomPicker<MarketAPI>(curr.random);
		//for (PunExReason reason : reasons) {
		
		//WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(curr.random);
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (!market.isPlayerOwned()) continue;
			if (market.isInHyperspace()) continue;
			
			float weight = 0f;
			if (reason.type == PunExType.ANTI_COMPETITION && reason.commodityId != null) {
				if (market.getSize() < MIN_COLONY_SIZE_FOR_NON_TERRITORIAL) continue;
				
				CommodityOnMarketAPI com = market.getCommodityData(reason.commodityId);
				int share = com.getCommodityMarketData().getExportMarketSharePercent(market);
//				if (share <= 0 && com.getAvailable() > 0) {
//					share = 1;
//				}
				weight += share * share;
			} else if (reason.type == PunExType.ANTI_FREE_PORT && market.getId().equals(reason.marketId)) {
				if (market.getSize() < MIN_COLONY_SIZE_FOR_NON_TERRITORIAL) continue;
				
				weight = 1f;
			} else if (reason.type == PunExType.TERRITORIAL && market.getId().equals(reason.marketId)) {
				weight = 1f;
			}
			
			targetPicker.add(market, weight);
		}
		
		MarketAPI target = targetPicker.pick();
		if (target == null) return;
		
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(curr.random);
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsInGroup(null)) {
			if (market.getFaction() == curr.faction && 
					market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_MILITARY)) {
				picker.add(market, market.getSize());
			}
		}
		
		MarketAPI from = picker.pick();
		if (from == null) return;
		
		PunExGoal goal = null;
		Industry industry = null;
		if (reason.type == PunExType.ANTI_FREE_PORT) {
			goal = PunExGoal.RAID_SPACEPORT;
			if (canBombard && curr.numSuccesses >= 2) {
				goal = PunExGoal.BOMBARD;
			}
		} else if (reason.type == PunExType.TERRITORIAL) {
			if (canBombard || true) {
				goal = PunExGoal.BOMBARD;
			} else {
				//goal = PunExGoal.EVACUATE;
			}
		} else {
			goal = PunExGoal.RAID_PRODUCTION;
			if (reason.commodityId == null || curr.numSuccesses >= 1) {
				goal = PunExGoal.RAID_SPACEPORT;
			}
			if (canBombard && curr.numSuccesses >= 2) {
				goal = PunExGoal.BOMBARD;
			}
		}
		
		//goal = PunExGoal.BOMBARD;
		
		if (goal == PunExGoal.RAID_SPACEPORT) {
			for (Industry temp : target.getIndustries()) {
				if (temp.getSpec().hasTag(Industries.TAG_UNRAIDABLE)) continue;
				if (temp.getSpec().hasTag(Industries.TAG_SPACEPORT)) {
					industry = temp;
					break;
				}
			}
			if (industry == null) return;
		} else if (goal == PunExGoal.RAID_PRODUCTION && reason.commodityId != null) {
			int max = 0;
			for (Industry temp : target.getIndustries()) {
				if (temp.getSpec().hasTag(Industries.TAG_UNRAIDABLE)) continue;
				
				int prod = temp.getSupply(reason.commodityId).getQuantity().getModifiedInt();
				if (prod > max) {
					max = prod;
					industry = temp;
				}
			}
			if (industry == null) return;
		}
		
		//float fp = from.getSize() * 20 + threshold * 0.5f;
		float fp = 50 + curr.threshold * 0.5f;
		fp = Math.max(50, fp - 50);
		//fp = 500;
//		if (from.getFaction().isHostileTo(target.getFaction())) {
//			fp *= 1.25f;
//		}
		
		if (fpOverride != null) {
			fp = fpOverride;
		}
		

		float totalAttempts = 0f;
		for (PunExData d : data.values()) {
			totalAttempts += d.numAttempts;
		}
		//if (totalAttempts > 10) totalAttempts = 10;
		
		float extraMult = 0f;
		if (totalAttempts <= 2) {
			extraMult = 0f;
		} else if (totalAttempts <= 4) {
			extraMult = 1f;
		} else if (totalAttempts <= 7) {
			extraMult = 2f;
		} else if (totalAttempts <= 10) {
			extraMult = 3f;
		} else {
			extraMult = 4f;
		}
		
		float orgDur = 20f + extraMult * 10f + (10f + extraMult * 5f) * (float) Math.random();
		
		
		curr.intel = new PunitiveExpeditionIntel(from.getFaction(), from, target, fp, orgDur,
												 goal, industry, reason);
		if (curr.intel.isDone()) {
			curr.intel = null;
			timeout = orgDur + MIN_TIMEOUT + curr.random.nextFloat() * (MAX_TIMEOUT - MIN_TIMEOUT);
			return;
		}
		
		if (curr.random.nextFloat() < numSentSinceTimeout * PROB_TIMEOUT_PER_SENT) {
			timeout = orgDur + MIN_TIMEOUT + curr.random.nextFloat() * (MAX_TIMEOUT - MIN_TIMEOUT);
		}
		numSentSinceTimeout++;
		
		curr.numAttempts++;
		curr.anger = 0f;
		curr.threshold *= 2f;
		if (curr.threshold > MAX_THRESHOLD) {
			curr.threshold = MAX_THRESHOLD;
		}
	}
	
	

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
	
}















