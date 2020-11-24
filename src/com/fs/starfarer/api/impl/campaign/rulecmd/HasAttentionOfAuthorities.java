package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Of the local market's faction, if any.
 * 
 * HasAttentionOfAuthorities
 */
public class HasAttentionOfAuthorities extends BaseCommandPlugin {

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		if (dialog.getInteractionTarget() == null) return false;
		
		MarketAPI market = dialog.getInteractionTarget().getMarket();
		if (market == null) return false;;
		
		MemoryAPI entity = memoryMap.get(MemKeys.LOCAL);
		if (memoryMap.containsKey(MemKeys.ENTITY)) {
			entity = memoryMap.get(MemKeys.ENTITY);
		}
		
		if (entity.is("$tradeMode", "NONE")) {
			return true;
		}
		
		// allowed to trade - either openly or snuck in - so we're good
		return false;
		
//		MemoryAPI marketMem = market.getMemoryWithoutUpdate();
//		if (marketMem.is("$playerHostileTimeout", true)) {
//			return true;
//		}
		
//		List<Token> innerParams = new ArrayList<Token>();
//		innerParams.add(new Token(market.getFactionId(), TokenType.LITERAL));
//		boolean hostileAware = new AnyNearbyFleetsHostileAndAware().execute(ruleId, dialog, innerParams, memoryMap);
//		
//		if (hostileAware) return true;
//		
//		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
//		boolean tOn = player.isTransponderOn();
//		
//		FactionAPI faction = market.getFaction();
//		boolean allowsTOffTrade = faction.getCustomBoolean(Factions.CUSTOM_ALLOWS_TRANSPONDER_OFF_TRADE);
//		boolean freePort = market.hasCondition(Conditions.FREE_PORT);
//		
//		RepLevel rep = faction.getRelationshipLevel(player.getFaction());
//		
//		if (rep.isAtBest(RepLevel.INHOSPITABLE) && tOn && !allowsTOffTrade && !freePort) {
//			return true;
//		}
		
	}
}














