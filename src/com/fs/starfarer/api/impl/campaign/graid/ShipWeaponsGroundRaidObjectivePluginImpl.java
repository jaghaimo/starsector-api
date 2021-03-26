package com.fs.starfarer.api.impl.campaign.graid;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.impl.campaign.econ.CommodityIconCounts;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.IconGroupAPI;
import com.fs.starfarer.api.ui.IconRenderMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ShipWeaponsGroundRaidObjectivePluginImpl extends BaseGroundRaidObjectivePluginImpl {
	
	public static float CARGO_SPACE_PER_LARGE = 8f;
	public static float CARGO_SPACE_PER_MEDIUM = 4f;
	public static float CARGO_SPACE_PER_SMALL = 2f;
	
	public static float SELL_MULT = Global.getSettings().getFloat("shipWeaponSellPriceMult");
	
	
	public static float VALUE_NORMAL = 1f;
	public static float VALUE_EXCESS = 2f;
	public static float VALUE_DEFICIT = -1f;
	public static float VALUE_OVERALL = 1000f;
	
	protected CommodityOnMarketAPI com;
	
	public ShipWeaponsGroundRaidObjectivePluginImpl(MarketAPI market) {
		super(market, Commodities.SHIPS);
		com = market.getCommodityData(id);
		setSource(CommodityGroundRaidObjectivePluginImpl.computeCommoditySource(market, com));
	}
	

	public void addIcons(IconGroupAPI iconGroup) {
		CommodityIconCounts counts = new CommodityIconCounts(com);
		
		int deficit = counts.deficit;
		int available = Math.max(0, counts.available - counts.extra);
		int extra = counts.extra;
		
		if (available > 0) {
			iconGroup.addIconGroup(Commodities.SHIP_WEAPONS, IconRenderMode.NORMAL, available, null);
		}
		if (deficit > 0) {
			iconGroup.addIconGroup(Commodities.SHIP_WEAPONS, IconRenderMode.RED, deficit, null);
		}
		if (extra > 0) {
			iconGroup.addIconGroup(Commodities.SHIP_WEAPONS, IconRenderMode.GREEN, extra, null);
		}
	}

//	public int getCargoSpaceNeeded() {
//		return (int) getQuantity(getMarinesAssigned());
//	}
	
	public int getProjectedCreditsValue() {
		return (int) getQuantity(getMarinesAssigned());
	}
	
	public CommoditySpecAPI getWeaponsCommoditySpec() {
		return Global.getSettings().getCommoditySpec(Commodities.SHIP_WEAPONS);
	}
	
	public RaidDangerLevel getDangerLevel() {
		RaidDangerLevel danger = getWeaponsCommoditySpec().getBaseDanger();
		
		CommodityIconCounts counts = new CommodityIconCounts(com);
		if (counts.production >= counts.available) {
			danger = danger.prev();
		}
		if (counts.deficit > 0) {
			return danger.next();
		}
		if (counts.extra > 0) {
			return danger.prev();
		}
		
		if (source != null) {
			danger = source.adjustCommodityDangerLevel(id, danger);
		}
		
		return danger;
	}

	public float getQuantitySortValue() {
		CommoditySpecAPI spec = getWeaponsCommoditySpec();
		float add = 0;
		if (spec != null) {
			add = spec.getOrder();
		}
		return QUANTITY_SORT_TIER_1 + add; 
	}
	
	public String getQuantityString(int marines) {
//		int value = (int) getQuantity(Math.max(1, marines));
//		return Misc.getDGSCredits(value);
		return "";
	}
	
	@Override
	public String getValueString(int marines) {
		int value = (int) getQuantity(Math.max(1, marines));
		return Misc.getDGSCredits(value);
	}


	public int getValue(int marines) {
		return (int) getQuantity(marines);
	}

	public float getQuantity(int marines) {
		float base = Math.round(getBaseRaidValue());
		return base * marines;
	}
	
	public float getBaseRaidValue() {
		CommodityOnMarketAPI com = market.getCommodityData(id);
		float unit = 1f;
		
		CommodityIconCounts counts = new CommodityIconCounts(com);
		
		float result = 0f;
		
		result += Math.max(0, counts.available - counts.extra) * unit * VALUE_NORMAL;
		result += counts.extra * unit * VALUE_EXCESS;
		result += counts.deficit * unit * VALUE_DEFICIT;
		
		result *= VALUE_OVERALL;
		
		if (result < 0) result = 0;
		
		return result;
	}

	public String getName() {
		return getWeaponsCommoditySpec().getName();
	}

	public CargoStackAPI getStackForIcon() {
		CargoStackAPI stack = Global.getFactory().createCargoStack(CargoItemType.RESOURCES, Commodities.SHIP_WEAPONS, null);
		return stack;
	}
	
	public String getCommodityIdForDeficitIcons() {
		return com.getId();
	}

	protected CargoAPI looted = Global.getFactory().createCargo(true);
	
	protected float getQMult(int tier) {
		if (tier <= 0) return 0f;
		if (tier <= 1) return 0.25f;
		if (tier <= 2) return 0.5f;
		return 0.75f;
	}
	
	public int performRaid(CargoAPI loot, Random random, float lootMult, TextPanelAPI text) {
		if (marinesAssigned <= 0) return 0;
		
		//random = new Random();
		
		WeightedRandomPicker<WeaponSpecAPI> pickerW = new WeightedRandomPicker<WeaponSpecAPI>(random);
		WeightedRandomPicker<FighterWingSpecAPI> pickerF = new WeightedRandomPicker<FighterWingSpecAPI>(random);
		WeightedRandomPicker<HullModSpecAPI> pickerH = new WeightedRandomPicker<HullModSpecAPI>(random);
		
		WeightedRandomPicker<WeaponSpecAPI> weaponSubset = new WeightedRandomPicker<WeaponSpecAPI>(random);
		WeightedRandomPicker<FighterWingSpecAPI> fighterSubset = new WeightedRandomPicker<FighterWingSpecAPI>(random);

		String factionId = market.getFactionId();
		float quality = Misc.getShipQuality(market, factionId);
		FactionAPI faction = Global.getSector().getFaction(factionId);
		
		int maxTier = 0;
		if (market.getSize() >= 6) {
			maxTier = 1;
		}
		if (Misc.hasHeavyIndustry(market) || Misc.isMilitary(market)) {
			maxTier = 1000;
		}
		
		float numSmall = 0;
		float numMedium = 0;
		float numLarge = 0;
		for (String id : faction.getKnownWeapons()) {
			WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(id);
			switch (spec.getSize()) {
			case LARGE:
				numLarge++;
				break;
			case MEDIUM:
				numMedium++;
				break;
			case SMALL:
				numSmall++;
				break;
			}
		}
		float numTotal = numSmall + numMedium + numLarge + 1f;
			
		for (String id : faction.getKnownWeapons()) {
			WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(id);
			if (spec.getAIHints().contains(AIHints.SYSTEM)) continue;
			if (spec.getTier() > maxTier) continue;
			
			float p = 1f * spec.getRarity() + quality * getQMult(spec.getTier());
			switch (spec.getSize()) {
			case LARGE:
				p *= 1f - numLarge / numTotal;
				p *= 2f;
				break;
			case MEDIUM:
				p *= 1f - numMedium / numTotal;
				p *= 3f;
				break;
			case SMALL:
				p *= 1f - numSmall / numTotal;
				p *= 4f;
				break;
			}
			pickerW.add(spec, p);
		}
		for (int i = 0; i < 4 + marinesAssigned; i++) {
			WeaponSpecAPI spec = pickerW.pick();
			if (spec != null) {
				float w = pickerW.getWeight(spec);
				weaponSubset.add(spec, w);
				pickerW.remove(spec);
			}
		}
		
		for (String id : faction.getKnownFighters()) {
			FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(id);
			if (spec.getTier() > maxTier) continue;
			
			float p = 1f * spec.getRarity() + quality * getQMult(spec.getTier());
			pickerF.add(spec, p);
		}
		for (int i = 0; i < 2 + marinesAssigned/2; i++) {
			FighterWingSpecAPI spec = pickerF.pick();
			if (spec != null) {
				float w = pickerF.getWeight(spec);
				fighterSubset.add(spec, w);
				pickerF.remove(spec);
			}
		}
		
		for (String id : faction.getKnownHullMods()) {
			HullModSpecAPI spec = Global.getSettings().getHullModSpec(id);
			if (spec.isHidden()) continue;
			if (spec.isAlwaysUnlocked()) continue;
			if (spec.hasTag(Tags.NO_DROP)) continue;
			if (spec.getTier() > maxTier) continue;
			
			float p = 1f * spec.getRarity();
			if (Global.getSector().getPlayerFaction().knowsHullMod(id)) {
				p *= 0.2f;
			}
			pickerH.add(spec, p);
		}
		
		
		float targetValue = getQuantity(marinesAssigned);
		targetValue *= lootMult;
		float mult = 0.9f + random.nextFloat() * 0.2f;
		targetValue *= mult;
		
		quantityLooted = (int) targetValue;
		
		float weaponWeight = faction.getDoctrine().getWarships() + faction.getDoctrine().getPhaseShips();
		float fighterWeight = 1f + faction.getDoctrine().getCarriers();
		float hullmodWeight = 1f + quality * 1f;
		float totalWeight = weaponWeight + fighterWeight + hullmodWeight;
		
		float weaponValue = targetValue * weaponWeight / totalWeight;
		float fighterValue = targetValue * fighterWeight / totalWeight;
		float hullmodValue = targetValue * hullmodWeight / totalWeight;

		float totalValue = 0;
		
		looted.clear();
		
		while (weaponValue > 0) {
			WeaponSpecAPI weapon = weaponSubset.pick();
			if (weapon != null) {
				int min = 1, max = 2;
				// don't do this since the odds of rolling large are smaller than medium smaller than small etc
//				switch (weapon.getSize()) {
//				case LARGE: min = 1; max = 2; break;
//				case MEDIUM: min = 1; max = 4; break;
//				case SMALL: min = 2; max = 6; break;
//				}
				float val = weapon.getBaseValue() * SELL_MULT;
				int num = min + random.nextInt(max - min + 1);
				num = (int) Math.min(num, weaponValue / val);
				if (num == 0) {
					if (random.nextFloat() < weaponValue / val) num = 1;
				}
				if (num > 0) {
					looted.addWeapons(weapon.getWeaponId(), num);
					weaponValue -= val * num;
					totalValue += val * num;
				} else {
					break;
				}
			}
		}
		
		fighterValue += Math.max(0, weaponValue);
		

		while (fighterValue > 0) {
			FighterWingSpecAPI fighter = fighterSubset.pick();
			if (fighter != null) {
				int min = 1, max = 2;
				switch (fighter.getRole()) {
				case ASSAULT:
				case BOMBER:
				case SUPPORT:
					min = 1; max = 2;
					break;
				case FIGHTER:
					min = 1; max = 3;
					break;
				case INTERCEPTOR:
					min = 1; max = 4;
					break;
				}
				float val = fighter.getBaseValue() * SELL_MULT;
				int num = min + random.nextInt(max - min + 1);
				num = (int) Math.min(num, fighterValue / val);
				if (num == 0) {
					if (random.nextFloat() < fighterValue / val) num = 1;
				}
				if (num > 0) {
					looted.addFighters(fighter.getId(), num);
					fighterValue -= val * num;
					totalValue += val * num;
				} else {
					break;
				}
			}
		}
		
		hullmodValue += Math.max(0, fighterValue);

		
		while (hullmodValue > 0) {
			HullModSpecAPI mod = pickerH.pickAndRemove();
			if (mod != null) {
				float val = mod.getBaseValue();
				int num = 0;
				if (random.nextFloat() < hullmodValue / val) num = 1;
				if (num > 0) {
					looted.addHullmods(mod.getId(), num);
					hullmodValue -= val * num;
					totalValue += val * num;
				} else {
					break;
				}
			}
		}
		
		loot.addAll(looted);
		
		xpGained = (int) (totalValue * XP_GAIN_VALUE_MULT);
		return xpGained;
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
		
		CommodityOnMarketAPI com = market.getCommodityData(Commodities.SHIPS);
		
		t.addPara("Ship weapons, fighter LPCs, and hullmod specs. Availability is based on the " + 
				"\"" + com.getCommodity().getName() + "\" commodity.", 0f);
		t.addPara("The colony faction's doctrine affects the number of weapons vs fighter LPCs acquired. Higher ship quality " +
				"increases the probability of finding modspecs..", opad);
		//t.addPara("The \"projected value\" is the typical sell value of the equipment acquired.", opad);
		
		if (Misc.hasHeavyIndustry(market) || Misc.isMilitary(market)) {
			if (Misc.hasHeavyIndustry(market)) {
				t.addPara("This colony has heavy industry and high-tier equipment may be found.", good, opad);
			} else if (Misc.isMilitary(market)) {
				t.addPara("This colony has a military presence and high-tier equipment may be found.", good, opad);
			}
		} else {
			t.addPara("This colony does not have heavy industry or a military presence and has no access to high-tier ship equipment.", bad, opad);
		}
		
		// weapons fighters and hullmods
		// based on S&W
		// impact of doctrine
		// impact of ship quality
		// how hullmods get rolled
		// impact of assigning more marines, explain that more marines = more stuff despite "variable"
		// whether it has military or production, or size >= 6
	}
	




	public CargoAPI getLooted() {
		return looted;
	}
	
	

}








