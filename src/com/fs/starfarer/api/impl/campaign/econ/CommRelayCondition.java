package com.fs.starfarer.api.impl.campaign.econ;

import java.util.Iterator;
import java.util.LinkedHashSet;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;



public class CommRelayCondition extends BaseMarketConditionPlugin {

	public static String COMM_RELAY_MOD_ID = "core_comm_relay";
	
	public static float NO_RELAY_PENALTY = -1f;
	public static float COMM_RELAY_BONUS = 2f;
	public static float MAKESHIFT_COMM_RELAY_BONUS = 1f;
	
	public static CommRelayCondition get(MarketAPI market) {
		MarketConditionAPI mc = market.getCondition(Conditions.COMM_RELAY);
		if (mc != null && mc.getPlugin() instanceof CommRelayCondition) {
			return (CommRelayCondition) mc.getPlugin();
		}
		return null;
	}
	
	
	
	protected LinkedHashSet<SectorEntityToken> relays = new LinkedHashSet<SectorEntityToken>();
	
	public LinkedHashSet<SectorEntityToken> getRelays() {
		return relays;
	}

	@Override
	public void advance(float amount) {
		Iterator<SectorEntityToken> iter = relays.iterator();
		while (iter.hasNext()) {
			SectorEntityToken relay = iter.next();
			if (!relay.isAlive() || relay.getContainingLocation() != market.getContainingLocation()) {
				iter.remove();
			}
		}
		if (relays.isEmpty()) {
			market.removeSpecificCondition(condition.getIdForPluginModifications());
		}
	}

	protected boolean isMakeshift(SectorEntityToken relay) {
		return relay.hasTag(Tags.MAKESHIFT);
	}
	
	protected SectorEntityToken getBestRelay() {
		if (market.getContainingLocation() == null) return null;
		
		SectorEntityToken best = null;
		for (SectorEntityToken relay : relays) {
			if (relay.getMemoryWithoutUpdate().getBoolean(MemFlags.OBJECTIVE_NON_FUNCTIONAL)) {
				continue;
			}
			if (relay.getFaction() == market.getFaction()) {
				if (best == null || (isMakeshift(best) && !isMakeshift(relay))) {
					best = relay;
				}
			}
		}
		return best;
	}
	
	
	public void apply(String id) {
		SectorEntityToken relay = getBestRelay();
		if (relay == null) {
			unapply(id);
			return;
		}
		
		if (isMakeshift(relay)) {
			market.getStability().modifyFlat(COMM_RELAY_MOD_ID, MAKESHIFT_COMM_RELAY_BONUS, "Makeshift comm relay");
		} else {
			market.getStability().modifyFlat(COMM_RELAY_MOD_ID, COMM_RELAY_BONUS, "Comm relay");
		}
			
	}
	
	public void unapply(String id) {
		market.getStability().unmodifyFlat(COMM_RELAY_MOD_ID);
	}

	@Override
	public boolean showIcon() {
		return false;
	}
}





