package com.fs.starfarer.api.impl.campaign.graid;

import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class BlueprintGroundRaidObjectivePluginImpl extends BaseGroundRaidObjectivePluginImpl {
	
	public static float PROBABILITY_TO_DROP_BP_NOT_DROPPED_BEFORE = 0.8f;
	
	protected int bpUseScale = 0;
	public BlueprintGroundRaidObjectivePluginImpl(MarketAPI market) {
		super(market, Commodities.BLUEPRINTS);
		setSource(computeSource());
	}
	
	@Override
	public void setSource(Industry source) {
		super.setSource(source);
		RaidDangerLevel level = getDangerLevel();
		int marines = level.marineTokens;
		if (source != null) {
			marines = source.adjustMarineTokensToRaidItem(id, null, marines); 
		}
		setMarinesRequired(marines);
	}

	public int getProjectedCreditsValue() {
		return 0;
	}
	
	public CommoditySpecAPI getCommoditySpec() {
		return Global.getSettings().getCommoditySpec(id);
	}
	
	public RaidDangerLevel getDangerLevel() {
		RaidDangerLevel level = getCommoditySpec().getBaseDanger();
		if (source != null) {
			level = source.adjustItemDangerLevel(id, null, level);
		}
		return level;
	}

	public float getQuantitySortValue() {
		CommoditySpecAPI spec = getCommoditySpec();
		float add = 0;
		if (spec != null) {
			add = spec.getOrder();
		}
		return QUANTITY_SORT_TIER_2 + add; 
	}
	
	public String getQuantityString(int marines) {
		Pair<Integer, Integer> q = getQuantityRange();
		return "" + q.one + "-" + q.two;
	}
	
	@Override
	public String getValueString(int marines) {
		return "";
	}

	public int getValue(int marines) {
		return 0;
	}

	public Pair<Integer, Integer> getQuantityRange() {
		Pair<Integer, Integer> q = new Pair<Integer, Integer>();
		int scale = bpUseScale - 995;
		//scale = bpUseScale;
		if (scale <= 0) {
			q.one = 0;
			q.two = 0;
		} else if (scale <= 5) {
			q.one = 1;
			q.two = 2;
		} else if (scale <= 7) {
			q.one = 2;
			q.two = 3;
		} else {
			q.one = 3;
			q.two = 4;
		}
		
		return q;
	}
	
	public float getQuantity(int marines) {
		return getQuantityRange().one;
	}
	
	public Industry computeSource() {
		Industry best = null;
		int score = 0;
		RaidDangerLevel base = getCommoditySpec().getBaseDanger();
		for (Industry ind : market.getIndustries()) {
			if (!ind.getSpec().hasTag(Industries.TAG_USES_BLUEPRINTS)) continue;
			
			int scale = 0;
			for (MutableCommodityQuantity q : ind.getAllSupply()) {
				scale = Math.max(scale, q.getQuantity().getModifiedInt());
			}
			for (MutableCommodityQuantity q : ind.getAllDemand()) {
				scale = Math.max(scale, q.getQuantity().getModifiedInt());
			}
			int currScore = scale;
			RaidDangerLevel danger = ind.adjustItemDangerLevel(Commodities.BLUEPRINTS, null, base);
			currScore += 1000 - danger.ordinal();
			if (currScore > score) {
				score = currScore;
				best = ind;
			}
		}
		bpUseScale = score;
		return best;
	}

	public String getName() {
//		if (bpTypeId != null && data != null) {
//			CargoStackAPI stack = Global.getFactory().createCargoStack(CargoItemType.SPECIAL, 
//														new SpecialItemData(bpTypeId, data), null);
//			return stack.getDisplayName();
//		}
		return "Blueprints";
	}

	public CargoStackAPI getStackForIcon() {
		CargoStackAPI stack = Global.getFactory().createCargoStack(CargoItemType.RESOURCES, Commodities.BLUEPRINTS, null);
//		if (bpTypeId != null && data != null) {
//			stack = Global.getFactory().createCargoStack(CargoItemType.SPECIAL, 
//														new SpecialItemData(bpTypeId, data), null);
//		}
		return stack;
	}
	
	@SuppressWarnings("unchecked")
	public Set<String> getDropped() {
		String key = "BlueprintGroundRaidObjectivePluginImpl_dropped";
		Object data = Global.getSector().getPersistentData().get(key);
		if (data == null) {
			data = new LinkedHashSet<String>();
			Global.getSector().getPersistentData().put(key, data);
		}
		return (Set<String>) data;
	}
	
	protected CargoAPI looted = Global.getFactory().createCargo(true);
	
	public int performRaid(CargoAPI loot, Random random, float lootMult, TextPanelAPI text) {
		if (marinesAssigned <= 0) return 0;
		
		//random = new Random();
		
		String ship    = "MarketCMD_ship____";
		String weapon  = "MarketCMD_weapon__";
		String fighter = "MarketCMD_fighter_";
		
		FactionAPI playerFaction = Global.getSector().getPlayerFaction();
		
		Set<String> droppedBefore = getDropped();
		
		WeightedRandomPicker<String> notDroppedBefore = new WeightedRandomPicker<String>(random);
		WeightedRandomPicker<String> other = new WeightedRandomPicker<String>(random);
		for (String id : market.getFaction().getKnownShips()) {
			if (Global.getSettings().getHullSpec(id).hasTag(Tags.NO_BP_DROP)) continue;
			
			String id2 = ship + id;
			if (!playerFaction.knowsShip(id) && !droppedBefore.contains(id2)) {
				notDroppedBefore.add(id2, 1f);
			} else {
				other.add(id2, 1f);
			}
		}
		for (String id : market.getFaction().getKnownWeapons()) {
			if (Global.getSettings().getWeaponSpec(id).hasTag(Tags.NO_BP_DROP)) continue;
			
			String id2 = weapon + id;
			if (!playerFaction.knowsWeapon(id) && !droppedBefore.contains(id2)) {
				notDroppedBefore.add(weapon + id, 1f);
			} else {
				other.add(weapon + id, 1f);
			}
		}
		for (String id : market.getFaction().getKnownFighters()) {
			if (Global.getSettings().getFighterWingSpec(id).hasTag(Tags.NO_BP_DROP)) continue;
			
			String id2 = fighter + id;
			if (!playerFaction.knowsFighter(id) && !droppedBefore.contains(id2)) {
				notDroppedBefore.add(fighter + id, 1f);
			} else {
				other.add(fighter + id, 1f);
			}
		}
		
		looted.clear();
		
		Pair<Integer, Integer> q = getQuantityRange();
		int num = q.one + random.nextInt(q.two - q.one + 1);
		for (int i = 0; i < num && (!notDroppedBefore.isEmpty() || !other.isEmpty()); i++) {
			String id = null;
			if (random.nextFloat() < PROBABILITY_TO_DROP_BP_NOT_DROPPED_BEFORE) {
				id = notDroppedBefore.pickAndRemove();
			}
			 
			if (id == null) {
				id = other.pickAndRemove();
			}
			if (id == null) continue;
			
			droppedBefore.add(id);
			
			if (id.startsWith(ship)) {
				String specId = id.substring(ship.length());
				//if (Global.getSettings().getHullSpec(specId).hasTag(Tags.NO_BP_DROP)) continue;
				looted.addSpecial(new SpecialItemData(Items.SHIP_BP, specId), 1);
			} else if (id.startsWith(weapon)) {
				String specId = id.substring(weapon.length());
				//if (Global.getSettings().getWeaponSpec(specId).hasTag(Tags.NO_BP_DROP)) continue;
				looted.addSpecial(new SpecialItemData(Items.WEAPON_BP, specId), 1);
			} else if (id.startsWith(fighter)) {
				String specId = id.substring(fighter.length());
				//if (Global.getSettings().getFighterWingSpec(specId).hasTag(Tags.NO_BP_DROP)) continue;
				looted.addSpecial(new SpecialItemData(Items.FIGHTER_BP, specId), 1);
			}
		}
		
		int totalValue = 0;
		for (CargoStackAPI stack : looted.getStacksCopy()) {
			totalValue += stack.getBaseValuePerUnit() * stack.getSize();
		}
		
		loot.addAll(looted);
		
		xpGained = (int) (totalValue * XP_GAIN_VALUE_MULT);
		return xpGained;
	}

	
	
	public CargoAPI getLooted() {
		return looted;
	}
	
	
	@Override
	public boolean hasTooltip() {
		return true;
	}

	@Override
	public void createTooltip(TooltipMakerAPI t, boolean expanded) {
		float opad = 10f;
		float pad = 3f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color good = Misc.getPositiveHighlightColor();

		// scale
		// value not being predictable
		// unknown blueprints being targeted
		
		t.addPara("Blueprints that enable heavy industry to construct ships, ship weapons, and fighter LPCs. " +
				"Availability based on the scale of the biggest blueprint-using industry at the colony.", 0f);
		
//		t.addPara("The value of the recovered blueprints can vary wildly, but your marines will focus on " +
//				"acquiring unknown blueprints first.", opad);
		t.addPara("The value of the recovered blueprints can vary wildly.", opad);
	}

}








