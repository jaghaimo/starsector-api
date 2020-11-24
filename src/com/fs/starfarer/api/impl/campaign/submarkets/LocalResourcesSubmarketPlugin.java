package com.fs.starfarer.api.impl.campaign.submarkets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class LocalResourcesSubmarketPlugin extends BaseSubmarketPlugin implements EconomyUpdateListener, EconomyTickListener {

	public static float STOCKPILE_MULT_PRODUCTION = Global.getSettings().getFloat("stockpileMultProduction");
	public static float STOCKPILE_MULT_EXCESS = Global.getSettings().getFloat("stockpileMultExcess");
	public static float STOCKPILE_MULT_IMPORTS = Global.getSettings().getFloat("stockpileMultImports");
	public static float STOCKPILE_MAX_MONTHS = Global.getSettings().getFloat("stockpileMaxMonths");
	
	public static float STOCKPILE_COST_MULT = Global.getSettings().getFloat("stockpileCostMult");
	public static float STOCKPILE_SHORTAGE_COST_MULT = Global.getSettings().getFloat("stockpileShortageCostMult");
	

	protected CargoAPI taken;
	protected CargoAPI left;
	
	protected Map<String, MutableStat> stockpilingBonus = new HashMap<String, MutableStat>();
	
	public LocalResourcesSubmarketPlugin() {
		taken = Global.getFactory().createCargo(true);
		left = Global.getFactory().createCargo(true);
	}
	
	public void init(SubmarketAPI submarket) {
		super.init(submarket);
		Global.getSector().getEconomy().addUpdateListener(this);
		Global.getSector().getListenerManager().addListener(this);
	}

	public boolean showInFleetScreen() {
		return false;
	}
	
	public boolean showInCargoScreen() {
		return true;
	}
	
	public boolean isEnabled(CoreUIAPI ui) {
		return true;
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
	
		// need to add to stockpiles every frame because the player can see stockpiles
		// in the "market info" screen and updateCargoPrePlayerInteraction() doesn't get called from there
		addAndRemoveStockpiledResources(amount, true, false, true);
		
//		if (!Global.getSector().getListenerManager().hasListener(this)) {
//			Global.getSector().getListenerManager().addListener(this);
//		}
	}
	
	public boolean shouldHaveCommodity(CommodityOnMarketAPI com) {
		if (market.isIllegal(com)) {
			if (com.getCommodityMarketData().getMarketShareData(market).isSourceIsIllegal()) {
				return false;
			}
			return true;
		}
		return true;
	}
	
	@Override
	public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
		if (stack.getCommodityId() == null) return true;
		if (stack.getResourceIfResource().hasTag(Commodities.TAG_NON_ECONOMIC)) return true;
		return false;
	}
	
	
	@Override
	public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
		return "Can only store resources";
	}
	

	@Override
	public int getStockpileLimit(CommodityOnMarketAPI com) {
		
		int demand = com.getMaxDemand();
		
		int shippingGlobal = com.getCommodityMarketData().getMaxShipping(com.getMarket(), false);
		//int shippingInFaction = com.getCommodityMarketData().getMaxShipping(com.getMarket(), true);
		
		int available = com.getAvailable();
		String modId = submarket.getSpecId();
		StatMod mod = com.getAvailableStat().getFlatStatMod(modId);
		if (mod != null) {
			available -= (int) mod.value;
			if (available < 0) available = 0;
		}
		
		int production = com.getMaxSupply();
		production = Math.min(production, available);
			
		int export = 0;
		demand = com.getMaxDemand();
		export = (int) Math.min(production, shippingGlobal);
		
		int extra = available - Math.max(export, demand);
		if (extra < 0) extra = 0;
		
		int deficit = demand - available;
//		int demandMet = Math.min(available, demand);
//		int demandMetWithLocal = Math.min(available, production) - extra;
		int imports = available - production;
		if (imports < 0) imports = 0; 
		
		production -= extra;
		
		float unit = com.getCommodity().getEconUnit();
		
		float limit = 0f;
		limit += STOCKPILE_MULT_EXCESS * BaseIndustry.getSizeMult(extra) * unit;
		limit += STOCKPILE_MULT_PRODUCTION * BaseIndustry.getSizeMult(production) * unit;
		limit += STOCKPILE_MULT_IMPORTS * BaseIndustry.getSizeMult(imports) * unit;
		
		String cid = com.getId();
		if (stockpilingBonus.containsKey(cid)) {
			limit += stockpilingBonus.get(cid).getModifiedValue() * unit;
		}
		
		//limit *= com.getMarket().getStockpileMult().getModifiedValue();
		limit *= STOCKPILE_MAX_MONTHS;
		
		if (deficit > 0) {
			limit = 0;
		}
		
		if (limit < 0) limit = 0;
		
		return (int) limit;
	}
	
	
	
	
	@Override
	public float getStockpilingAddRateMult(CommodityOnMarketAPI com) {
//		float mult = com.getMarket().getStockpileMult().getModifiedValue();
//		if (mult > 0) {
//			return 1f / mult;
//		}
		return 1f / STOCKPILE_MAX_MONTHS;
	}
	
//	public int getApproximateStockpilingCost() {
//		CargoAPI cargo = getCargo();
//		
//		float total = 0f;
//		for (CommodityOnMarketAPI com : market.getCommoditiesCopy()) {
//			if (com.isNonEcon()) continue;
//			if (com.getCommodity().isMeta()) continue;
//			
//			int limit = getStockpileLimit(com);
//			if (limit <= 0) continue;
//			
//			int needed = (int) (limit - cargo.getCommodityQuantity(com.getId()));
//			if (needed <= 0) continue;
//			
//			float price = getStockpilingUnitPrice(com.getCommodity());
//			total += price * needed;
//		}
//		
//		return (int)(Math.ceil(total / 1000f) * 1000f);
//	}


	public void commodityUpdated(String commodityId) {
		if (Global.getSector().isPaused()) {
			CommodityOnMarketAPI com = market.getCommodityData(commodityId);
			addAndRemoveStockpiledResources(com, 0f, true, false, false);
		}
	}

	public void economyUpdated() {
		if (Global.getSector().isPaused()) { // to apply shortage-countering during economy steps in UI operations
			addAndRemoveStockpiledResources(0f, true, false, false);
		}
	}

	public boolean isEconomyListenerExpired() {
//		if (!market.isPlayerOwned()) {
//			market.removeSubmarket(submarket.getSpecId());
//			return true;
//		}
		return !market.hasSubmarket(submarket.getSpecId());
	}
	
	

	
	@Override
	public boolean isParticipatesInEconomy() {
		return false;
	}

	@Override
	public boolean isHidden() {
//		if (true) return false;
		return !market.isPlayerOwned();
	}

	public float getTariff() {
		return 0f;
	}

	@Override
	public boolean isFreeTransfer() {
		return true;
	}

	protected transient CargoAPI preTransactionCargoCopy = null;
	public void updateCargoPrePlayerInteraction() {
		preTransactionCargoCopy = getCargo().createCopy();
		preTransactionCargoCopy.sort();
		getCargo().sort();
		//sinceLastCargoUpdate = 0f;
	}
	
	public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
		//addAndRemoveStockpiledResources(0f, true, false, false); // not needed b/c economyUpdated() gets called
		
		sinceLastCargoUpdate = 0f; // to reset how long until one more unit gets added if something was drawn down to 0
		
		preTransactionCargoCopy = getCargo().createCopy();
		preTransactionCargoCopy.sort();
		
		taken.addAll(transaction.getBought());
		left.addAll(transaction.getSold());
		
		CargoAPI copy = taken.createCopy();
		taken.removeAll(left);
		left.removeAll(copy);
	}
	
	protected Object readResolve() {
		super.readResolve();
		
		if (taken == null) {
			taken = Global.getFactory().createCargo(true);
		}
		if (left == null) {
			left = Global.getFactory().createCargo(true);
		}
		if (stockpilingBonus == null) {
			stockpilingBonus = new HashMap<String, MutableStat>();
		}
		return this;
	}
	
	public MutableStat getStockpilingBonus(String cid) {
		MutableStat stat = stockpilingBonus.get(cid);
		if (stat == null) {
			stat = new MutableStat(0);
			stockpilingBonus.put(cid, stat);
		}
		return stat;
	}

	public CargoAPI getLeft() {
		return left;
	}
	
	public int getEstimatedShortageCounteringCostPerMonth() {
		List<CommodityOnMarketAPI> all = new ArrayList<CommodityOnMarketAPI>(market.getAllCommodities());
		
		float totalCost = 0f;
		
		CargoAPI cargo = getCargo();
		
		for (CommodityOnMarketAPI com : all) {
			int curr = (int) cargo.getCommodityQuantity(com.getId());
			if (curr <= 0) continue;
			
			float units = LocalResourcesSubmarketPlugin.getDeficitMonthlyCommodityUnits(com);
			units = Math.min(units, cargo.getCommodityQuantity(com.getId()));
			units -= taken.getCommodityQuantity(com.getId());
			if (units > 0) {
				float per = LocalResourcesSubmarketPlugin.getStockpilingUnitPrice(com.getCommodity(), true);
				totalCost += units * per;
			}
		}
		return (int) totalCost;
	}

	public static int getStockpilingUnitPrice(CommoditySpecAPI spec, boolean forShortageCountering) {
		float mult = STOCKPILE_COST_MULT;
		if (forShortageCountering) mult = STOCKPILE_SHORTAGE_COST_MULT;
		int result = (int) Math.round((spec.getBasePrice() * mult));
		if (result < 1) result = 1;
		return result;
//		float unitPrice = market.getDemandPrice(com.getId(), 1, true);
//		if (unitPrice < 1) unitPrice = 1;
//		return (int) unitPrice;
	}
	
	public static float getDeficitMonthlyCommodityUnits(CommodityOnMarketAPI com) {
		String modId = Submarkets.LOCAL_RESOURCES;
		
		StatMod mod = com.getAvailableStat().getFlatMods().get(modId);
		float modAlready = 0;
		if (mod != null) modAlready = mod.value;
		
		int demand = com.getMaxDemand();
		int available = (int) Math.round(com.getAvailable() - modAlready);
		
		if (demand > available) {
			float deficitDrawBaseAmount = BaseIndustry.getSizeMult(demand) - BaseIndustry.getSizeMult(available);
			deficitDrawBaseAmount *= com.getCommodity().getEconUnit();
			return deficitDrawBaseAmount;
		}
		return 0;
	}
	
	protected boolean doShortageCountering(CommodityOnMarketAPI com, float amount, boolean withShortageCountering) {
		CargoAPI cargo = getCargo();
		String modId = submarket.getSpecId();
		
		com.getAvailableStat().unmodifyFlat(modId);
		
		int demand = com.getMaxDemand();
		int available = com.getAvailable();

//		if (com.isIllegal() && com.getMarket().isPlayerOwned()) {
//			System.out.println("wefwefew");
//		}
		
		if (withShortageCountering && demand > available) {
			// draw resources and apply bonus
			int deficit = demand - available;
			if (deficit != deficit) return false; // bug report indicates possible NaN here; not sure how
			
			float deficitDrawBaseAmount = BaseIndustry.getSizeMult(demand) - BaseIndustry.getSizeMult(available);
			deficitDrawBaseAmount *= com.getCommodity().getEconUnit();
			
			float days = Global.getSector().getClock().convertToDays(amount);
			
			float drawAmount = deficitDrawBaseAmount * days / 30f;
			float curr = cargo.getCommodityQuantity(com.getId());
			if (curr > 0 && deficitDrawBaseAmount > 0) {
				int daysLeft = (int) (curr / deficitDrawBaseAmount * 30f);
				String daysStr = "days";
				if (daysLeft <= 1) {
					daysLeft = 1;
					daysStr = "day";
				}
				com.getAvailableStat().modifyFlat(modId, deficit, 
							"Local resource stockpiles (" + daysLeft + " " + daysStr + " left)");
				
				float free = left.getCommodityQuantity(com.getId());
				free = Math.min(drawAmount, free);
				left.removeCommodity(com.getId(), free);
				if (drawAmount > 0) {
					cargo.removeCommodity(com.getId(), drawAmount);
				}
				drawAmount -= free;
				
				if (market.isPlayerOwned() && drawAmount > 0) {
					MonthlyReport report = SharedData.getData().getCurrentReport();
					FDNode node = report.getCounterShortageNode(market);
					
					CargoAPI tooltipCargo = (CargoAPI) node.custom2;
					float addToTooltipCargo = drawAmount;
					float q = tooltipCargo.getCommodityQuantity(com.getId()) + addToTooltipCargo;
					if (q < 1) {
						addToTooltipCargo = 1f; // add at least 1 unit or it won't do anything
					}
					tooltipCargo.addCommodity(com.getId(), addToTooltipCargo);
					
					float unitPrice = (int) getStockpilingUnitPrice(com.getCommodity(), true);
					//node.upkeep += unitPrice * addAmount;
					
					FDNode comNode = report.getNode(node, com.getId());
						
					CommoditySpecAPI spec = com.getCommodity();
					comNode.icon = spec.getIconName();
					comNode.upkeep += unitPrice * drawAmount;
					comNode.custom = com;
					
					if (comNode.custom2 == null) {
						comNode.custom2 = 0f;
					}
					comNode.custom2 = (Float)comNode.custom2 + drawAmount;
					
					float qty = Math.max(1, (Float) comNode.custom2);
					qty = (float) Math.ceil(qty);
					comNode.name = spec.getName() + " " + Strings.X + Misc.getWithDGS(qty);
					comNode.tooltipCreator = report.getMonthlyReportTooltip();
				}
			}
			return true;
		}
		return false;
	}
	
	
	public void reportEconomyMonthEnd() {
		if (isEconomyListenerExpired()) {
			Global.getSector().getListenerManager().removeListener(this);
			return;
		}
	}

	public void reportEconomyTick(int iterIndex) {
		if (isEconomyListenerExpired()) {
			Global.getSector().getListenerManager().removeListener(this);
			return;
		}
		
		int lastIterInMonth = (int) Global.getSettings().getFloat("economyIterPerMonth") - 1;
		if (iterIndex != lastIterInMonth) return;
		
		if (market.isPlayerOwned()) {
			CargoAPI copy = taken.createCopy();
			taken.removeAll(left);
			left.removeAll(copy);
			
			MonthlyReport report = SharedData.getData().getCurrentReport();
			
			
			for (CargoStackAPI stack : taken.getStacksCopy()) {
				if (!stack.isCommodityStack()) continue;
				
				FDNode node = report.getRestockingNode(market);
				CargoAPI tooltipCargo = (CargoAPI) node.custom2;
				
				float addToTooltipCargo = stack.getSize();
				String cid = stack.getCommodityId();
				float q = tooltipCargo.getCommodityQuantity(cid) + addToTooltipCargo;
				if (q < 1) {
					addToTooltipCargo = 1f; // add at least 1 unit or it won't do anything
				}
				tooltipCargo.addCommodity(cid, addToTooltipCargo);
				
				float unitPrice = (int) getStockpilingUnitPrice(stack.getResourceIfResource(), false);
				//node.upkeep += unitPrice * addAmount;
				
				FDNode comNode = report.getNode(node, cid);
					
				CommoditySpecAPI spec = stack.getResourceIfResource();
				comNode.icon = spec.getIconName();
				comNode.upkeep += unitPrice * addToTooltipCargo;
				comNode.custom = market.getCommodityData(cid);
				
				if (comNode.custom2 == null) {
					comNode.custom2 = 0f;
				}
				comNode.custom2 = (Float)comNode.custom2 + addToTooltipCargo;
				
				float qty = Math.max(1, (Float) comNode.custom2);
				qty = (float) Math.ceil(qty);
				comNode.name = spec.getName() + " " + Strings.X + Misc.getWithDGS(qty);
				comNode.tooltipCreator = report.getMonthlyReportTooltip();
			}
		}
		taken.clear();
	}
	
	
	@Override
	public String getBuyVerb() {
		return "Take";
	}

	@Override
	public String getSellVerb() {
		return "Leave";
	}

	public String getTariffTextOverride() {
		return "End of month";
	}
	public String getTariffValueOverride() {
		if (preTransactionCargoCopy == null) return null; // could happen when visiting colony from colony list screen
		CargoAPI cargo = getCargo();
		//preTransactionCargoCopy;
		
		float total = 0f;
		Set<String> seen = new HashSet<String>();
		for (CargoStackAPI stack : preTransactionCargoCopy.getStacksCopy()) {
			if (!stack.isCommodityStack()) continue;
			
			String cid = stack.getCommodityId();
			if (seen.contains(cid)) continue;
			seen.add(cid);
			
			CommodityOnMarketAPI com = market.getCommodityData(cid);
			
			int pre = (int) preTransactionCargoCopy.getCommodityQuantity(cid);
			int post = (int) cargo.getCommodityQuantity(cid);
			
			int units = pre - post; // player taking this many units
			
			units -= left.getCommodityQuantity(cid);
			
			if (units > 0) {
				float price = getStockpilingUnitPrice(com.getCommodity(), false);
				total += price * units;
			}
		}
		
		return Misc.getDGSCredits(total);
	}
	
	public String getTotalTextOverride() {
		return "Now";
	}
	public String getTotalValueOverride() {
		return "0" + Strings.C;
		//return "";
	}
	
	
	
	public boolean isTooltipExpandable() {
		return false;
	}
	
	public float getTooltipWidth() {
		return 500f;
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		List<CommodityOnMarketAPI> all = new ArrayList<CommodityOnMarketAPI>(market.getAllCommodities());
		
		Collections.sort(all, new Comparator<CommodityOnMarketAPI>() {
			public int compare(CommodityOnMarketAPI o1, CommodityOnMarketAPI o2) {
				int limit1 = getStockpileLimit(o1);
				int limit2 = getStockpileLimit(o2);
				return limit2 - limit1;
			}
		});

		float opad = 10f;
		
		tooltip.beginGridFlipped(400f, 1, 70f, opad);
		int j = 0;
		for (CommodityOnMarketAPI com : all) {
			if (com.isNonEcon()) continue;
			if (com.getCommodity().isMeta()) continue;
			
			if (!shouldHaveCommodity(com)) continue;
			
			int limit = (int) Math.round(getStockpileLimit(com) * getStockpilingAddRateMult(com));
			if (limit <= 0) continue;
			
			tooltip.addToGrid(0, j++,
					com.getCommodity().getName(),
					Misc.getWithDGS(limit));
						//Misc.getWithDGS(curr) + " / " + Misc.getWithDGS(limit));
		}
		
		tooltip.addPara("A portion of the resources produced by the colony will be made available here. " +
				"These resources can be extracted from the colony's economy for a cost equal to %s of their base value. " +
				"This cost will be deduced at the end of the month.", opad,
				Misc.getHighlightColor(), "" + (int)Math.round(STOCKPILE_COST_MULT * 100f) + "%");
		
		tooltip.addPara("These resources can also be used to counter temporary shortages, for a " +
				"cost equal to %s of their base value. If additional resources are placed here, they " +
				"will be used as well, at no cost.", opad,
				Misc.getHighlightColor(), "" + (int)Math.round(STOCKPILE_SHORTAGE_COST_MULT * 100f) + "%");
		
		
		tooltip.addSectionHeading("Stockpiled per month", market.getFaction().getBaseUIColor(), market.getFaction().getDarkUIColor(), Alignment.MID, opad);
		if (j > 0) {
			tooltip.addGrid(opad);
			
			tooltip.addPara("Stockpiles are limited to %s the monthly rate.", opad,
					Misc.getHighlightColor(), "" + (int)STOCKPILE_MAX_MONTHS + Strings.X);
		} else {
			tooltip.addPara("No stockpiling.", opad);
		}	
	}
	
}




