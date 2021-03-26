package com.fs.starfarer.api.impl.campaign.econ;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.Misc;

public class ShippingDisruption extends BaseMarketConditionPlugin {
	
	public static String COMMODITY_LOSS_PREFIX = "sh_loss";
	public static float ACCESS_LOSS_DURATION = 90f;
	
	public static float ACCESS_PER_UNITS_LOST = 0.1f; // accessibility penalty per marketSize units lost
	
	
	public static float getPenaltyForShippingLost(float marketSize, float unitsLost) {
		float result = Math.round(unitsLost / marketSize * ACCESS_PER_UNITS_LOST  * 100f) / 100f;
		if (result == 0) result = 0.01f;
		return result;
	}
	
	public static ShippingDisruption getDisruption(MarketAPI market) {
		MarketConditionAPI mc = market.getCondition(Conditions.SHIPPING_DISRUPTION);
		if (mc == null) {
			String id = market.addCondition(Conditions.SHIPPING_DISRUPTION);
			mc = market.getSpecificCondition(id);
			//mc.setFromEvent(true);
		}
		return (ShippingDisruption) mc.getPlugin();
	}
	
	protected MutableStatWithTempMods shippingLost = new MutableStatWithTempMods(0f); 
	
	protected float disruptionTimeLeft = 0f;
	public ShippingDisruption() {
	}
	
	/**
	 * "units" is the largest number of econ-units of a commodity being carried by a trade fleet. 
	 * For example, 5 food, 3 organics, 2 fuel = 5 units = approximate "size" of that trade fleet, in economy terms.
	 * @param units
	 */
	public void addShippingLost(float units) {
		shippingLost.addTemporaryModFlat(ACCESS_LOSS_DURATION, getModId() + Misc.genUID(), units);
		updatePenaltyValue();
	}

	protected void updatePenaltyValue() {
		unapply(getModId());
		apply(getModId());
	}

	public float getDisruptionTimeLeft() {
		return disruptionTimeLeft;
	}

	public void setDisruptionTimeLeft(float disruptionTimeLeft) {
		this.disruptionTimeLeft = disruptionTimeLeft;
	}

	public void notifyDisrupted(float duration) {
		disruptionTimeLeft = Math.max(disruptionTimeLeft, duration);
	}
	public void apply(String id) {
		float penalty = getPenaltyForShippingLost(market.getSize(), shippingLost.getModifiedValue());
		String name = "Trade fleets lost";
		market.getAccessibilityMod().modifyFlat(id, -penalty, name);
		
		if (market.isPlayerOwned()) {
			for (CommodityOnMarketAPI com : market.getCommoditiesCopy()) {
				List<String> unmodify = new ArrayList<String>();
				if (com.getMaxSupply() >= com.getAvailable() + 1) {
					for (String key : com.getAvailableStat().getFlatMods().keySet()) {
						StatMod mod = com.getAvailableStat().getFlatStatMod(key);
						int val = (int)Math.round(Math.abs(mod.value));
						if (key.startsWith(COMMODITY_LOSS_PREFIX) && val != 0) {
							unmodify.add(mod.source);
						}
					}
				}
				for (String modId : unmodify) {
					com.getAvailableStat().unmodifyFlat(modId);
				}
			}
		}
	}

	public void unapply(String id) {
		market.getAccessibilityMod().unmodifyFlat(id);
	}
	
	
	@Override
	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		shippingLost.advance(days);
		updatePenaltyValue();
		
		disruptionTimeLeft -= days;
		if (disruptionTimeLeft <= 0) {
			disruptionTimeLeft = 0;
			if (shippingLost.isUnmodified()) {
				market.removeSpecificCondition(condition.getIdForPluginModifications());
			}
		}
	}

	public Map<String, String> getTokenReplacements() {
		return super.getTokenReplacements();
		//return event.getTokenReplacements();
	}

	@Override
	public String[] getHighlights() {
		return super.getHighlights();
		//return event.getHighlights("report_td");
	}
	
	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		tooltip.addTitle("Shipping Disruption");
		
		int loss = shippingLost.getModifiedInt();
		
		int penalty = (int)Math.round(getPenaltyForShippingLost(market.getSize(), shippingLost.getModifiedValue()) * 100f);
		
		Color h = Misc.getHighlightColor();
		Color n = Misc.getNegativeHighlightColor();
		
		float pad = 3f;
		float small = 5f;
		float opad = 10f;
		
		//tooltip.addPara("Loss: %s, Accesibility penalty: %s", pad, h, "" + loss, "" + penalty);
		if (penalty > 0) {
			tooltip.addPara("Trade fleets launched from " + market.getName() + " have suffered losses, " +
					"resulting in a temporary accessibility penalty of %s.", opad, h, "" + penalty + "%");
		}
		
		CountingMap<CommodityOnMarketAPI> losses = new CountingMap<CommodityOnMarketAPI>();
		
		for (CommodityOnMarketAPI com : market.getCommoditiesCopy()) {
			for (String key : com.getAvailableStat().getFlatMods().keySet()) {
				StatMod mod = com.getAvailableStat().getFlatStatMod(key);
				
				int val = (int)Math.round(Math.abs(mod.value));
				if (key.startsWith(COMMODITY_LOSS_PREFIX) && val != 0) {
					losses.add(com, val); 
				}
			}
		}
		
		if (!losses.isEmpty()) {
			tooltip.addPara("The local availability of some commodities has been reduced by trade fleet losses. " +
					"Provided no further losses occur, commodity availability should return to normal levels within at most three months.", opad);
			tooltip.beginGridFlipped(400, 1, 30, opad);
			int j = 0;
			for (CommodityOnMarketAPI com : losses.keySet()) {
				tooltip.addToGrid(0, j++, com.getCommodity().getName(), 
										"-" + losses.getCount(com), h);
			}
			tooltip.addGrid(pad);
		}
		
		//tooltip.addPara(com.getCommodity().getName() + ": %s", pad, h, "" + (int) mod.value);
	}

	@Override
	public float getTooltipWidth() {
		return super.getTooltipWidth();
	}

	@Override
	public boolean hasCustomTooltip() {
		return true;
	}

	@Override
	public boolean isTooltipExpandable() {
		return super.isTooltipExpandable();
	}
	

}





