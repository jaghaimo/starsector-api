package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.econ.CommodityMarketDataAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.impl.campaign.econ.CommRelayCondition;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.StatModValueGetter;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;


public class PopulationAndInfrastructure extends BaseIndustry implements MarketImmigrationModifier {

	//public static int BASE_STABILITY = 5;
	//public static int SMUGGLING_PENALTY = 2;
	//public static int OUT_OF_FACTION_PENALTY = 1;
	//public static int UNMET_DEMAND_PENALTY = 1;
	
	public void apply() {
		modifyStability(this, market, getModId(3));
		
//		if (market.getId().equals("chicomoztoc")) {
//		System.out.println("wefwefwe");
//	}
		
		super.apply(true);
		
		int size = market.getSize();
		
		demand(Commodities.FOOD, size - 1);
		
		if (!market.hasCondition(Conditions.HABITABLE)) {
			demand(Commodities.ORGANICS, size - 1);
		}
		
		int luxuryThreshold = 3;
		
		demand(Commodities.DOMESTIC_GOODS, size - 1);
		demand(Commodities.LUXURY_GOODS, size - luxuryThreshold);
		demand(Commodities.DRUGS, size - 2);
		demand(Commodities.ORGANS, size - 3);
		
		demand(Commodities.SUPPLIES, Math.min(size, 3));

		supply(Commodities.CREW, size - 3);
		supply(Commodities.DRUGS, size - 4);
		supply(Commodities.ORGANS, size - 5);
		
		
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.DOMESTIC_GOODS);
		if (deficit.two <= 0) {
			market.getStability().modifyFlat(getModId(0), 1, "Domestic goods demand met");
		} else {
			market.getStability().unmodifyFlat(getModId(0));
		}
		
		deficit = getMaxDeficit(Commodities.LUXURY_GOODS);
		if (deficit.two <= 0 && size > luxuryThreshold) {
			market.getStability().modifyFlat(getModId(1), 1, "Luxury goods demand met");
		} else {
			market.getStability().unmodifyFlat(getModId(1));
		}
		
		deficit = getMaxDeficit(Commodities.FOOD);
		if (!market.hasCondition(Conditions.HABITABLE)) {
			deficit = getMaxDeficit(Commodities.FOOD, Commodities.ORGANICS);
		}
		if (deficit.two > 0) {
			market.getStability().modifyFlat(getModId(2), -deficit.two, getDeficitText(deficit.one));
		} else {
			market.getStability().unmodifyFlat(getModId(2));
		}
		
		if (!market.hasSpaceport()) {
			float accessibilityNoSpaceport = Global.getSettings().getFloat("accessibilityNoSpaceport");
			market.getAccessibilityMod().modifyFlat(getModId(0), accessibilityNoSpaceport, "No spaceport");
		}
		
		float sizeBonus = getAccessibilityBonus(size);
		if (sizeBonus > 0) {
			market.getAccessibilityMod().modifyFlat(getModId(1), sizeBonus, "Colony size");
		}
		
		
		
		
		float stability = market.getPrevStability();
		float stabilityQualityMod = FleetFactoryV3.getShipQualityModForStability(stability);
		float doctrineQualityMod = market.getFaction().getDoctrine().getShipQualityContribution();
		
		market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlatAlways(getModId(0), stabilityQualityMod,
										      "Stability");
		
		market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlatAlways(getModId(1), doctrineQualityMod,
											  Misc.ucFirst(market.getFaction().getEntityNamePrefix()) + " fleet doctrine");
		
		float stabilityDefenseMult = 0.5f + stability / 10f;
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMultAlways(getModId(),
											  stabilityDefenseMult, "Stability");
		
		float baseDef = getBaseGroundDefenses(market.getSize());
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyFlatAlways(getModId(), 
											  baseDef, "Base value for a size " + market.getSize() + " colony");
		
		
		market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).modifyFlat(getModId(), getMaxIndustries(), null);
		
//		if (market.isPlayerOwned()) {
//			System.out.println("wfwefwef");
//		}
		FactionDoctrineAPI doctrine = market.getFaction().getDoctrine();
		float doctrineShipsMult = FleetFactoryV3.getDoctrineNumShipsMult(doctrine.getNumShips());
		float marketSizeShipsMult = FleetFactoryV3.getNumShipsMultForMarketSize(market.getSize());
		float deficitShipsMult = FleetFactoryV3.getShipDeficitFleetSizeMult(market);
		float stabilityShipsMult = FleetFactoryV3.getNumShipsMultForStability(stability);
		
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlatAlways(getModId(0), marketSizeShipsMult, 
											  "Colony size");

		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMultAlways(getModId(1), doctrineShipsMult,
											  Misc.ucFirst(market.getFaction().getEntityNamePrefix()) + " fleet doctrine");
		
		if (deficitShipsMult != 1f) {
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(getModId(2), deficitShipsMult,
												  getDeficitText(Commodities.SHIPS));
		} else {
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMultAlways(getModId(2), deficitShipsMult,
					  							  getDeficitText(Commodities.SHIPS).replaceAll("shortage", "demand met"));
		}
		
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMultAlways(getModId(3), stabilityShipsMult,
												  "Stability");
		
		
		market.addTransientImmigrationModifier(this);
	}
	
	
	public static float getAccessibilityBonus(int marketSize) {
		if (marketSize <= 4) return 0f;
		if (marketSize == 5) return 0.1f;
		if (marketSize == 6) return 0.15f;
		if (marketSize == 7) return 0.2f;
		if (marketSize == 8) return 0.25f;
		return 0.3f;
	}
	public static float getBaseGroundDefenses(int marketSize) {
		if (marketSize <= 1) return 10;
		if (marketSize <= 2) return 20;
		if (marketSize <= 3) return 50;
		
		return (marketSize - 3) * 100;
	}
	
	@Override
	public void unapply() {
		super.unapply();
		
		market.getStability().unmodify(getModId(0));
		market.getStability().unmodify(getModId(1));
		market.getStability().unmodify(getModId(2));
		
		market.getAccessibilityMod().unmodifyFlat(getModId(0));
		market.getAccessibilityMod().unmodifyFlat(getModId(1));
		
		market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).unmodifyFlat(getModId(0));
		market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).unmodifyFlat(getModId(1));
		
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyFlat(getModId());
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
		
		market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodifyFlat(getModId());
		
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyFlat(getModId(0));
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId(1));
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId(2));
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId(3));
		
		unmodifyStability(market, getModId(3));
		
		market.removeTransientImmigrationModifier(this);
	}
	
	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return true;
	}
	
	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			
			MutableStat stabilityMods = new MutableStat(0);
			
			float total = 0;
			for (StatMod mod : market.getStability().getFlatMods().values()) {
				if (mod.source.startsWith(getModId())) {
					stabilityMods.modifyFlat(mod.source, mod.value, mod.desc);
					total += mod.value;
				}
			}
			
			String totalStr = "+" + (int)Math.round(total);
			Color h = Misc.getHighlightColor();
			if (total < 0) {
				totalStr = "" + (int)Math.round(total);
				h = Misc.getNegativeHighlightColor();
			}
			float opad = 10f;
			float pad = 3f;
			if (total >= 0) {
				tooltip.addPara("Stability bonus: %s", opad, h, totalStr);
			} else {
				tooltip.addPara("Stability penalty: %s", opad, h, totalStr);
			}
			tooltip.addStatModGrid(400, 30, opad, pad, stabilityMods, new StatModValueGetter() {
				public String getPercentValue(StatMod mod) {
					return null;
				}
				public String getMultValue(StatMod mod) {
					return null;
				}
				public Color getModColor(StatMod mod) {
					if (mod.value < 0) return Misc.getNegativeHighlightColor();
					return null;
				}
				public String getFlatValue(StatMod mod) {
					return null;
				}
			});
			
			/*
			MutableStat qualityMods = new MutableStat(0);
			
			total = 0;
			for (StatMod mod : market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).getFlatBonuses().values()) {
				if (mod.source.startsWith(getModId())) {
					qualityMods.modifyFlat(mod.source, mod.value, mod.desc);
					total += mod.value;
				}
			}
			
			totalStr = "+" + (int)Math.round(total * 100f) + "%";
			if (total < 0) {
				totalStr = "" + (int)Math.round(total * 100f) + "%";
				h = Misc.getNegativeHighlightColor();
			}
			if (total >= 0) {
				tooltip.addPara("Ship quality bonus: %s", opad, h, totalStr);
			} else {
				tooltip.addPara("Ship quality penalty: %s", opad, h, totalStr);
			}
			tooltip.addStatModGrid(400, 50, opad, pad, qualityMods, new StatModValueGetter() {
				public String getPercentValue(StatMod mod) {
					return null;
				}
				public String getMultValue(StatMod mod) {
					return null;
				}
				public Color getModColor(StatMod mod) {
					if (mod.value < 0) return Misc.getNegativeHighlightColor();
					return null;
				}
				public String getFlatValue(StatMod mod) {
					String prefix = mod.value >= 0 ? "+" : "";
					return prefix + (int)Math.round(mod.value * 100f) + "%";
				}
			});
			*/
			
		}
	}	
	
	
	
	@Override
	public String getCurrentImage() {
		float size = market.getSize();
		if (size <= 4) {
			return Global.getSettings().getSpriteName("industry", "pop_low");
		}
		if (size >= 7) {
			return Global.getSettings().getSpriteName("industry", "pop_high");
		}
		
		return super.getCurrentImage();
	}

	
	public static float getIncomeStabilityMult(float stability) {
		if (stability <= 5) {
			return Math.max(0, stability / 5f);
		}
		return 1f + (stability - 5f) * .1f;
	}
	
//	public static float getUpkeepHazardMult(float hazard) {
//		float hazardMult = hazard;
//		float min = Global.getSettings().getFloat("minUpkeepMult");
//		if (hazardMult < min) hazardMult = min;
//		return hazardMult;
//	}
	public static float getUpkeepHazardMult(float hazard) {
		//float hazardMult = 1f + hazard;
		float hazardMult = hazard;
		float min = Global.getSettings().getFloat("minUpkeepMult");
		if (hazardMult < min) hazardMult = min;
		return hazardMult;
	}
	
	
	public static int getMismanagementPenalty() {
		int outposts = 0;
		for (MarketAPI curr : Global.getSector().getEconomy().getMarketsCopy()) {
			if (!curr.isPlayerOwned()) continue;
			
			if (curr.getAdmin().isPlayer()) {
				outposts++;
			}
		}
		
		MutableCharacterStatsAPI stats = Global.getSector().getCharacterData().getPerson().getStats();
		
		int maxOutposts = stats.getOutpostNumber().getModifiedInt();
		
		int overOutposts = outposts - maxOutposts;
		
		//if (overOutposts < 0) overOutposts = 0;
		
		int penaltyOrBonus = (int) (overOutposts * Misc.getOutpostPenalty());
		
		return penaltyOrBonus;
	}
	
	public static void modifyStability(Industry industry, MarketAPI market, String modId) {
		market.getIncomeMult().modifyMultAlways(modId, getIncomeStabilityMult(market.getPrevStability()), "Stability");
		market.getUpkeepMult().modifyMultAlways(modId, getUpkeepHazardMult(market.getHazardValue()), "Hazard rating");
		
		float inFactionSupply = 0f;
		float totalDemand = 0f;
		for (CommodityOnMarketAPI com : market.getCommoditiesCopy()) {
			if (com.isNonEcon()) continue;
			
			int d = com.getMaxDemand();
			if (d <= 0) continue;
			
			totalDemand += d;
			CommodityMarketDataAPI cmd = com.getCommodityMarketData();
			int inFaction = Math.max(com.getMaxSupply(), 
							   Math.min(cmd.getMaxShipping(market, true), cmd.getMaxExport(market.getFactionId())));
			if (inFaction > d) inFaction = d;
			if (inFaction < d) inFaction = Math.max(com.getMaxSupply(), 0);
			
			//CommoditySourceType source = cmd.getMarketShareData(market).getSource();;
			//if (source != CommoditySourceType.GLOBAL) {
			//	inFactionSupply += Math.min(d - inFaction, com.getAvailable());
			//}
			inFactionSupply += Math.max(0, Math.min(inFaction, com.getAvailable()));
		}
		
		if (totalDemand > 0) {
			float max = Global.getSettings().getFloat("upkeepReductionFromInFactionImports");
			float f = inFactionSupply / totalDemand;
			if (f < 0) f = 0;
			if (f > 1) f = 1;
			if (f > 0) {
				float mult = Math.round(100f - (f * max * 100f)) / 100f;
				String desc = "Demand supplied in-faction (" + (int)Math.round(f * 100f) + "%)";
				if (f == 1f) desc = "All demand supplied in-faction";
				market.getUpkeepMult().modifyMultAlways(modId + "ifi", mult, desc);
			} else {
				market.getUpkeepMult().modifyMultAlways(modId + "ifi", 1f, "All demand supplied out-of-faction; no upkeep reduction");
			}
		}
		
		
		if (market.isPlayerOwned() && market.getAdmin().isPlayer()) {
			int penalty = getMismanagementPenalty();
			if (penalty > 0) {
				market.getStability().modifyFlat("_" + modId + "_mm", -penalty, "Mismanagement penalty");
			} else if (penalty < 0) {
				market.getStability().modifyFlat("_" + modId + "_mm", -penalty, "Management bonus");
			} else {
				market.getStability().unmodifyFlat("_" + modId + "_mm");
			}
		} else {
			market.getStability().unmodifyFlat(modId + "_mm");
		}
		
		market.getStability().modifyFlat("_" + modId + "_ms", Global.getSettings().getFloat("stabilityBaseValue"), "Base value");

		if (!market.hasCondition(Conditions.COMM_RELAY)) {
			market.getStability().modifyFlat(CommRelayCondition.COMM_RELAY_MOD_ID, CommRelayCondition.NO_RELAY_PENALTY, "No active comm relay in-system");
		}
	}
	
	
	public static void unmodifyStability(MarketAPI market, String modId) {
		market.getIncomeMult().unmodifyMult(modId);
		market.getUpkeepMult().unmodifyMult(modId);
		market.getUpkeepMult().unmodifyMult(modId + "ifi");
		
		market.getStability().unmodifyFlat(modId);
		
//		for (int i = 0; i < 30; i++) {
//			market.getStability().unmodify(modId + i);
//		}
		market.getStability().unmodifyFlat("_" + modId + "_mm");
		market.getStability().unmodifyFlat("_" + modId + "_ms");

		if (!market.hasCondition(Conditions.COMM_RELAY)) {
			market.getStability().unmodifyFlat(CommRelayCondition.COMM_RELAY_MOD_ID);
		}
		
	}
	

	
	@Override
	public boolean showShutDown() {
		return false;
	}

	@Override
	public String getCanNotShutDownReason() {
		//return "Use \"Abandon Colony\" instead.";
		return null;
	}

	@Override
	public boolean canShutDown() {
		return false;
	}

	@Override
	protected String getDescriptionOverride() {
		int size = market.getSize();
		String cid = null;
		if (size >= 1 && size <= 9) {
			cid = "population_" + size;
			MarketConditionSpecAPI mcs = Global.getSettings().getMarketConditionSpec(cid);
			if (mcs != null) {
				return spec.getDesc() + "\n\n" + mcs.getDesc();
			}
		}
		return super.getDescriptionOverride();
	}

	public String getBuildOrUpgradeProgressText() {
//		float f = buildProgress / spec.getBuildTime();
//		return "" + (int) Math.round(f * 100f) + "%";
		if (isUpgrading()) {
			//return "" + (int) Math.round(Misc.getMarketSizeProgress(market) * 100f) + "%";
			return "total growth: " + Misc.getRoundedValue(Misc.getMarketSizeProgress(market) * 100f) + "%";
		}
		
		return super.getBuildOrUpgradeProgressText();
	}

	@Override
	public float getBuildOrUpgradeProgress() {
		if (!super.isBuilding() && market.getSize() < 10) {
			return Misc.getMarketSizeProgress(market);
		}
		return super.getBuildOrUpgradeProgress();
	}

	@Override
	public boolean isBuilding() {
		if (!super.isBuilding() && market.getSize() < 10 && getBuildOrUpgradeProgress() > 0) return true;
		
		return super.isBuilding();
	}

	@Override
	public boolean isUpgrading() {
		if (!super.isBuilding() && market.getSize() < 10) return true;
		
		return super.isUpgrading();
	}

	
	
	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		float patherLevel = 0;
		for (Industry curr : market.getIndustries()) {
			patherLevel += getAICoreImpact(curr.getAICoreId());
		}
		
		String adminCoreId = market.getAdmin().getAICoreId();
		if (adminCoreId != null) {
			patherLevel += 10f * getAICoreImpact(adminCoreId);
		}

		List<String> targeted = new ArrayList<String>();
		targeted.add(Industries.TECHMINING);
		targeted.add(Industries.HEAVYINDUSTRY);
		targeted.add(Industries.FUELPROD);
		targeted.add(Industries.STARFORTRESS);
		
		for (String curr : targeted) {
			if (market.hasIndustry(curr)) {
				patherLevel += 10f;
			}
		}
		
		if (patherLevel > 0) {
			incoming.add(Factions.LUDDIC_PATH, patherLevel * 0.2f);
		}
	}
	
	private float getAICoreImpact(String coreId) {
		if (Commodities.ALPHA_CORE.equals(coreId)) return 10f;
		if (Commodities.BETA_CORE.equals(coreId)) return 4f;
		if (Commodities.GAMMA_CORE.equals(coreId)) return 1f;
		return 0f;
	}
	
	public boolean canBeDisrupted() {
		return false;
	}
	
	public int getMaxIndustries() {
		return getMaxIndustries(market.getSize());
	}
	
	public static int [] MAX_IND = null;
	public static int getMaxIndustries(int size) {
		if (MAX_IND == null) {
			try {
				MAX_IND = new int [10];
				JSONArray a = Global.getSettings().getJSONArray("maxIndustries");
				for (int i = 0; i < MAX_IND.length; i++) {
					MAX_IND[i] = a.getInt(i);
				}
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
		size--;
		if (size < 0) size = 0;
		if (size > 9) size = 9;
		return MAX_IND[size];
//		if (size <= 3) return 1;
//		if (size <= 5) return 2;
//		if (size <= 7) return 3;
//		return 4;
	}
}







