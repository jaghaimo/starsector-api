package com.fs.starfarer.api.impl.campaign.submarkets;

import java.awt.Color;
import java.util.Random;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;

public class MilitarySubmarketPlugin extends BaseSubmarketPlugin {
	
	public static Logger log = Global.getLogger(MilitarySubmarketPlugin.class);
	
	public void init(SubmarketAPI submarket) {
		super.init(submarket);
	}

	public void updateCargoPrePlayerInteraction() {
		float seconds = Global.getSector().getClock().convertToSeconds(sinceLastCargoUpdate);
		addAndRemoveStockpiledResources(seconds, false, true, true);
		sinceLastCargoUpdate = 0f;
		
		if (okToUpdateShipsAndWeapons()) {
			sinceSWUpdate = 0f;
			
			pruneWeapons(0f);
			
			int weapons = 7 + Math.max(0, market.getSize() - 1) * 2;
			int fighters = 2 + Math.max(0, market.getSize() - 3);
			
			addWeapons(weapons, weapons + 2, 3, submarket.getFaction().getId());
			addFighters(fighters, fighters + 2, 3, market.getFactionId());

			float stability = market.getStabilityValue();
			float sMult = Math.max(0.1f, stability / 10f);
			getCargo().getMothballedShips().clear();
			addShips(submarket.getFaction().getId(),
					200f * sMult, // combat
					15f, // freighter 
					10f, // tanker
					20f, // transport
					10f, // liner
					10f, // utilityPts
					null, // qualityOverride
					0f, // qualityMod
					null,
					null);
				
			addHullMods(4, 2 + itemGenRandom.nextInt(4));
		}
		
		getCargo().sort();
	}
	
	protected Object writeReplace() {
		if (okToUpdateShipsAndWeapons()) {
			pruneWeapons(0f);
			getCargo().getMothballedShips().clear();
		}
		return this;
	}
	
	
	@Override
	public String getName() {
		if (submarket.getFaction().getId().equals(Factions.LUDDIC_CHURCH)) {
			return "Knights of Ludd";
		}
		return Misc.ucFirst(submarket.getFaction().getPersonNamePrefix()) + "\n" + "Military";
	}

	protected boolean requiresCommission(RepLevel req) {
		if (!submarket.getFaction().getCustomBoolean(Factions.CUSTOM_OFFERS_COMMISSIONS)) return false;
		
		if (req.isAtWorst(RepLevel.WELCOMING)) return true;
		return false;
	}
	
	protected boolean hasCommission() {
		return submarket.getFaction().getId().equals(Misc.getCommissionFactionId());
	}
	
	
	public boolean shouldHaveCommodity(CommodityOnMarketAPI com) {
		if (Commodities.CREW.equals(com.getId())) return true;
		return com.getCommodity().hasTag(Commodities.TAG_MILITARY);
	}
	
	@Override
	public int getStockpileLimit(CommodityOnMarketAPI com) {
//		int demand = com.getMaxDemand();
//		int available = com.getAvailable();
//		
//		float limit = BaseIndustry.getSizeMult(available) - BaseIndustry.getSizeMult(Math.max(0, demand - 2));
//		limit *= com.getCommodity().getEconUnit();
		
		float limit = OpenMarketPlugin.getBaseStockpileLimit(com);
		
		//limit *= com.getMarket().getStockpileMult().getModifiedValue();
		
		Random random = new Random(market.getId().hashCode() + submarket.getSpecId().hashCode() + Global.getSector().getClock().getMonth() * 170000);
		limit *= 0.9f + 0.2f * random.nextFloat();
		
		float sm = market.getStabilityValue() / 10f;
		limit *= (0.25f + 0.75f * sm);
		
		if (limit < 0) limit = 0;
		
		return (int) limit;
	}
	
	public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
		//boolean illegal = submarket.getFaction().isIllegal(commodityId);
		boolean illegal = market.isIllegal(commodityId);
		RepLevel req = getRequiredLevelAssumingLegal(commodityId, action);
		
		if (req == null) return illegal;
		
		RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
		boolean legal = level.isAtWorst(req);
		if (requiresCommission(req)) {
			legal &= hasCommission();
		}
		return !legal;
	}

	public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
		if (stack.isCommodityStack()) {
			return isIllegalOnSubmarket((String) stack.getData(), action);
		}
		
		RepLevel req = getRequiredLevelAssumingLegal(stack, action);
		if (req == null) return false;
		
		RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
		
		boolean legal = level.isAtWorst(req);
		if (requiresCommission(req)) {
			legal &= hasCommission();
		}
		
		return !legal;
	}
	
	public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
		RepLevel req = getRequiredLevelAssumingLegal(stack, action);

		if (req != null) {
			if (requiresCommission(req)) {
				return "Req: " +
						submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase() + ", " +
						" commission";
			}
			return "Req: " + 
					submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase();
		}
		
		return "Illegal to trade in " + stack.getDisplayName() + " here";
	}
	

	public Highlights getIllegalTransferTextHighlights(CargoStackAPI stack, TransferAction action) {
		RepLevel req = getRequiredLevelAssumingLegal(stack, action);
		if (req != null) {
			Color c = Misc.getNegativeHighlightColor();
			Highlights h = new Highlights();
			RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
			if (!level.isAtWorst(req)) {
				h.append(submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase(), c);
			}
			if (requiresCommission(req) && !hasCommission()) {
				h.append("commission", c);
			}
			return h;
		}
		return null;
	}
	
	private RepLevel getRequiredLevelAssumingLegal(CargoStackAPI stack, TransferAction action) {
		int tier = -1;
		if (stack.isWeaponStack()) {
			WeaponSpecAPI spec = stack.getWeaponSpecIfWeapon();
			tier = spec.getTier();
		} else if (stack.isModSpecStack()) {
			HullModSpecAPI spec = stack.getHullModSpecIfHullMod();
			tier = spec.getTier();
		} else if (stack.isFighterWingStack()) {
			FighterWingSpecAPI spec = stack.getFighterWingSpecIfWing();
			tier = spec.getTier();
		}
		
		if (tier >= 0) {
			if (action == TransferAction.PLAYER_BUY) {
				switch (tier) {
				case 0: return RepLevel.FAVORABLE;
				case 1: return RepLevel.WELCOMING;
				case 2: return RepLevel.FRIENDLY;
				case 3: return RepLevel.COOPERATIVE;
				}
			}
			return RepLevel.VENGEFUL;
		}
		
		if (!stack.isCommodityStack()) return null;
		return getRequiredLevelAssumingLegal((String) stack.getData(), action);
	}
	
	private RepLevel getRequiredLevelAssumingLegal(String commodityId, TransferAction action) {
		if (action == TransferAction.PLAYER_SELL) {
			//return null;
			return RepLevel.VENGEFUL;
		}
		
		CommodityOnMarketAPI com = market.getCommodityData(commodityId);
		boolean isMilitary = com.getCommodity().getTags().contains(Commodities.TAG_MILITARY);
		if (isMilitary) {
			if (com.isPersonnel()) {
				return RepLevel.COOPERATIVE;
			}
			return RepLevel.FAVORABLE;
		}
		return null;
	}
	
	public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
		if (action == TransferAction.PLAYER_SELL && Misc.isAutomated(member)) {
			return true;
		}
		
		RepLevel req = getRequiredLevelAssumingLegal(member, action);
		if (req == null) return false;
		
		RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
		
		boolean legal = level.isAtWorst(req);
		if (requiresCommission(req)) {
			legal &= hasCommission();
		}
		
		return !legal;
	}
	
	public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
		RepLevel req = getRequiredLevelAssumingLegal(member, action);
		if (req != null) {
			String str = "";
			RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
			if (!level.isAtWorst(req)) {
				str += "Req: " + submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase();				
			}
			if (requiresCommission(req) && !hasCommission()) {
				if (!str.isEmpty()) str += "\n";
				str += "Req: " + submarket.getFaction().getDisplayName() + " - " + "commission";
			}
			return str;
//			if (requiresCommission(req)) {
//				return //"Requires:\n" +
//						"Req: " + submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase() + "\n" +
//						"Req: " + submarket.getFaction().getDisplayName() + " - " + "commission";
//			}
//			return "Requires: " + 
//					submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase();
		}
		
		if (action == TransferAction.PLAYER_BUY) {
			return "Illegal to buy"; // this shouldn't happen
		} else {
			return "Illegal to sell";
		}
	}

	public Highlights getIllegalTransferTextHighlights(FleetMemberAPI member, TransferAction action) {
		if (isIllegalOnSubmarket(member, action)) return null;
		
		RepLevel req = getRequiredLevelAssumingLegal(member, action);
		if (req != null) {
			Color c = Misc.getNegativeHighlightColor();
			Highlights h = new Highlights();
			RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
			if (!level.isAtWorst(req)) {
				h.append("Req: " + submarket.getFaction().getDisplayName() + " - " + req.getDisplayName().toLowerCase(), c);
			}
			if (requiresCommission(req) && !hasCommission()) {
				h.append("Req: " + submarket.getFaction().getDisplayName() + " - commission", c);
			}
			return h;
		}
		return null;
	}
	
	private RepLevel getRequiredLevelAssumingLegal(FleetMemberAPI member, TransferAction action) {
		if (action == TransferAction.PLAYER_BUY) {
			int fp = member.getFleetPointCost();
			HullSize size = member.getHullSpec().getHullSize();
			
			if (size == HullSize.CAPITAL_SHIP || fp > 15) return RepLevel.COOPERATIVE;
			if (size == HullSize.CRUISER || fp > 10) return RepLevel.FRIENDLY;
			if (size == HullSize.DESTROYER || fp > 5) return RepLevel.WELCOMING;
			return RepLevel.FAVORABLE;
		}
		return null;
	}
	
	
	
	private RepLevel minStanding = RepLevel.FAVORABLE;
	public boolean isEnabled(CoreUIAPI ui) {
		//if (mode == CoreUITradeMode.OPEN) return false;
		if (ui.getTradeMode() == CoreUITradeMode.SNEAK) return false;
		
		RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
		return level.isAtWorst(minStanding);
	}
	
	public OnClickAction getOnClickAction(CoreUIAPI ui) {
		return OnClickAction.OPEN_SUBMARKET;
	}
	
	public String getTooltipAppendix(CoreUIAPI ui) {
		if (!isEnabled(ui)) {
			return "Requires: " + submarket.getFaction().getDisplayName() + " - " + minStanding.getDisplayName().toLowerCase();
		}
		if (ui.getTradeMode() == CoreUITradeMode.SNEAK) {
			return "Requires: proper docking authorization";
		}
		return null;
	}
	
	public Highlights getTooltipAppendixHighlights(CoreUIAPI ui) {
		String appendix = getTooltipAppendix(ui);
		if (appendix == null) return null;
		
		Highlights h = new Highlights();
		h.setText(appendix);
		h.setColors(Misc.getNegativeHighlightColor());
		return h;
	}
	
	@Override
	public PlayerEconomyImpactMode getPlayerEconomyImpactMode() {
		return PlayerEconomyImpactMode.PLAYER_SELL_ONLY;
	}
	
	public boolean isMilitaryMarket() {
		return true;
	}
}



