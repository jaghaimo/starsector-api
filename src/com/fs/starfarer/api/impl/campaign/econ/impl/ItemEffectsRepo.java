package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin.InstallableItemDescriptionMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.FusionLampEntityPlugin;
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

@SuppressWarnings("serial")
public class ItemEffectsRepo {
	
	public static String NOT_A_GAS_GIANT = "not a gas giant";
	public static String NOT_HABITABLE = "not habitable";
	public static String HABITABLE = "habitable";
	public static String GAS_GIANT = "gas giant";
	public static String NO_ATMOSPHERE = "no atmosphere";
	public static String NOT_EXTREME_WEATHER = "not extreme weather";
	public static String NOT_EXTREME_TECTONIC_ACTIVITY = "not extreme tectonic activity";
	public static String NO_TRANSPLUTONIC_ORE_DEPOSITS = "no transplutonic ore deposits";
	public static String NO_VOLATILES_DEPOSITS = "no volatiles deposits";
	public static String HOT_OR_EXTREME_HEAT = "hot or extreme heat";
	public static String COLD_OR_EXTREME_COLD = "cold or extreme cold";
	
	public static int CORONAL_TAP_LIGHT_YEARS = 10;
	public static String CORONAL_TAP_RANGE = "coronal tap within " + CORONAL_TAP_LIGHT_YEARS + " light-years";
	
	public static int CORONAL_TAP_TRANSPLUTONICS = 10;
	public static int CORONAL_TAP_INDUSTRIES = 1;
	
	public static int FUSION_LAMP_FARMING_BONUS = 0;
	public static int FUSION_LAMP_VOLATILES = 10;
	public static float FUSION_LAMP_SHORTAGE_HAZARD = 0.5f;
	public static List<String> FUSION_LAMP_CONDITIONS = new ArrayList<String>();
	static {
		FUSION_LAMP_CONDITIONS.add(Conditions.COLD);
		FUSION_LAMP_CONDITIONS.add(Conditions.VERY_COLD);
		FUSION_LAMP_CONDITIONS.add(Conditions.POOR_LIGHT);
		FUSION_LAMP_CONDITIONS.add(Conditions.DARK);
	}
	
	public static float CORRUPTED_NANOFORGE_QUALITY_BONUS = 0.2f;
	public static float PRISTINE_NANOFORGE_QUALITY_BONUS = 0.5f;
	
	public static int CORRUPTED_NANOFORGE_PROD = 1;
	public static int PRISTINE_NANOFORGE_PROD = 3;
	
	public static int SYNCHROTRON_FUEL_BONUS = 3;
	
	public static int MANTLE_BORE_MINING_BONUS = 3;
	public static List<String> MANTLE_BORE_COMMODITIES = new ArrayList<String>();
	static {
		MANTLE_BORE_COMMODITIES.add(Commodities.ORE);
		MANTLE_BORE_COMMODITIES.add(Commodities.RARE_ORE);
		MANTLE_BORE_COMMODITIES.add(Commodities.ORGANICS);
	}
	
	public static int PLASMA_DYNAMO_MINING_BONUS = 3;
	public static List<String> PLASMA_DYNAMO_COMMODITIES = new ArrayList<String>();
	static {
		PLASMA_DYNAMO_COMMODITIES.add(Commodities.VOLATILES);
	}
	
	public static int BIOFACTORY_PROD_BONUS = 2;
	
	public static int CATALYTIC_CORE_BONUS = 3;
	
	public static float FULLERENE_SPOOL_ACCESS_BONUS = 0.3f;
	
	public static int SOIL_NANITES_BONUS = 2;
	
	public static float CRYOARITHMETIC_FLEET_SIZE_BONUS_HOT = 0.25f;
	public static float CRYOARITHMETIC_FLEET_SIZE_BONUS_VERY_HOT = 1f;
	
	public static float DRONE_REPLICATOR_BONUS_MULT = 1.5f;
	
	public static int DEALMAKER_INCOME_PERCENT_BONUS = 50;
	

	public static Map<String, InstallableItemEffect> ITEM_EFFECTS = new HashMap<String, InstallableItemEffect>() {{
		put(Items.ORBITAL_FUSION_LAMP, new BaseInstallableItemEffect(Items.ORBITAL_FUSION_LAMP) {
			public void apply(Industry industry) {
				Industry farming = getFarming(industry);
				if (farming != null && FUSION_LAMP_FARMING_BONUS > 0) {
					if (farming.isFunctional()) {
						farming.getSupplyBonusFromOther().modifyFlat(spec.getId(), FUSION_LAMP_FARMING_BONUS,
								Misc.ucFirst(spec.getName().toLowerCase()) + " (" + industry.getCurrentName() + ")");
					} else {
						farming.getSupplyBonusFromOther().unmodifyFlat(spec.getId());
					}
				}
				if (industry instanceof BaseIndustry) {
					BaseIndustry b = (BaseIndustry) industry;
					b.demand(9, Commodities.VOLATILES, FUSION_LAMP_VOLATILES, Misc.ucFirst(spec.getName().toLowerCase()));
					
					MemoryAPI memory = getLampMemory(industry);
					float h = getShortageHazard(industry);
					if (h > 0) {
						industry.getMarket().getHazard().modifyFlat(spec.getId(), h, 
								Misc.ucFirst(spec.getName().toLowerCase()) + " volatiles shortage");
						if (memory != null) {
							// so that FusionLampEntityPlugin knows to flicker during a shortage
							memory.set(FusionLampEntityPlugin.VOLATILES_SHORTAGE_KEY, h / FUSION_LAMP_SHORTAGE_HAZARD);
						}
					}
				}
				for (String id : FUSION_LAMP_CONDITIONS) {
					industry.getMarket().suppressCondition(id);
				}
			}
			public void unapply(Industry industry) {
				Industry farming = getFarming(industry);
				if (farming != null && FUSION_LAMP_FARMING_BONUS > 0) {
					farming.getSupplyBonusFromOther().unmodifyFlat(spec.getId());
				}
				if (industry instanceof BaseIndustry) {
					BaseIndustry b = (BaseIndustry) industry;
					b.demand(9, Commodities.VOLATILES, 0, null);
					industry.getMarket().getHazard().unmodifyFlat(spec.getId());
					MemoryAPI memory = getLampMemory(industry);
					if (memory != null) {
						memory.unset(FusionLampEntityPlugin.VOLATILES_SHORTAGE_KEY);
					}
				}
				for (String id : FUSION_LAMP_CONDITIONS) {
					industry.getMarket().unsuppressCondition(id);
				}
			}
			protected MemoryAPI getLampMemory(Industry industry) {
				if (industry instanceof PopulationAndInfrastructure) {
					PopulationAndInfrastructure b = (PopulationAndInfrastructure) industry;
					if (b.lamp != null) {
						return b.lamp.getMemoryWithoutUpdate();
					}
				}
				return null;
			}
			protected float getShortageHazard(Industry industry) {
				int volatilesDemand = industry.getDemand(Commodities.VOLATILES).getQuantity().getModifiedInt();
				if (volatilesDemand <= 0 || FUSION_LAMP_SHORTAGE_HAZARD <= 0) return 0f;
				float v = industry.getMarket().getCommodityData(Commodities.VOLATILES).getAvailable();
				float f = 1f - v / (float) volatilesDemand;
				float h = Math.round(f * FUSION_LAMP_SHORTAGE_HAZARD * 100f) / 100f;
				if (h > 0) {
					industry.getMarket().getHazard().modifyFlat(spec.getId(), h, 
							Misc.ucFirst(spec.getName().toLowerCase()) + " volatiles shortage");
				}
				return h;
			}
			protected Industry getFarming(Industry industry) {
				String farmingId = Industries.FARMING;
				Industry farming = industry.getMarket().getIndustry(farmingId);
				if (farming == null) {
					farmingId = Industries.AQUACULTURE;
					farming = industry.getMarket().getIndustry(farmingId);
				}
				return farming;
			}
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
				   	  							  InstallableItemDescriptionMode mode, String pre, float pad) {
				List<String> conds = new ArrayList<String>();
				for (String id : FUSION_LAMP_CONDITIONS) {
					MarketConditionSpecAPI mc = Global.getSettings().getMarketConditionSpec(id);
					conds.add(mc.getName());
				}
				text.addPara(pre + "Counters the effects of " + Misc.getAndJoined(conds) + ". Increases heat on non-cold planets. " +  
						"Adds demand for %s units of volatiles.",
						pad, Misc.getHighlightColor(), 
						"" + FUSION_LAMP_VOLATILES);
			}
		});
		put(Items.CORRUPTED_NANOFORGE, new BoostIndustryInstallableItemEffect(
									Items.CORRUPTED_NANOFORGE, CORRUPTED_NANOFORGE_PROD, 0) {
			public void apply(Industry industry) {
				super.apply(industry);
				industry.getMarket().getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD)
						.modifyFlat("nanoforge", CORRUPTED_NANOFORGE_QUALITY_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
			}
			public void unapply(Industry industry) {
				super.unapply(industry);
				industry.getMarket().getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat("nanoforge");
			}
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
					  						   	  InstallableItemDescriptionMode mode, String pre, float pad) {
				String heavyIndustry = "heavy industry ";
				if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_LIST) {
					heavyIndustry = "";
				}
				text.addPara(pre + "Increases ship and weapon production quality by %s. " +
						"Increases " + heavyIndustry + "production by %s unit." +
						" On habitable worlds, causes pollution which becomes permanent.",
						pad, Misc.getHighlightColor(), 
						"" + (int) Math.round(CORRUPTED_NANOFORGE_QUALITY_BONUS * 100f) + "%",
						"" + (int) CORRUPTED_NANOFORGE_PROD);
			}
		});
		put(Items.PRISTINE_NANOFORGE, new BoostIndustryInstallableItemEffect(
										Items.PRISTINE_NANOFORGE, PRISTINE_NANOFORGE_PROD, 0) {
			public void apply(Industry industry) {
				super.apply(industry);
				industry.getMarket().getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD)
						.modifyFlat("nanoforge", PRISTINE_NANOFORGE_QUALITY_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
			}
			public void unapply(Industry industry) {
				super.unapply(industry);
				industry.getMarket().getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodifyFlat("nanoforge");
			}
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
					  							  InstallableItemDescriptionMode mode, String pre, float pad) {
				String heavyIndustry = "heavy industry ";
				if (mode == InstallableItemDescriptionMode.MANAGE_ITEM_DIALOG_LIST) {
					heavyIndustry = "";
				}
				text.addPara(pre + "Increases ship and weapon production quality by %s. " +
						"Increases " + heavyIndustry + "production by %s units." +
						" On habitable worlds, causes pollution which becomes permanent.",
						pad, Misc.getHighlightColor(), 
						"" + (int) Math.round(PRISTINE_NANOFORGE_QUALITY_BONUS * 100f) + "%",
						"" + (int) PRISTINE_NANOFORGE_PROD);
			}
		});
		put(Items.SYNCHROTRON, new BoostIndustryInstallableItemEffect(
								Items.SYNCHROTRON, SYNCHROTRON_FUEL_BONUS, 0) {
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
					  							  InstallableItemDescriptionMode mode, String pre, float pad) {
				//text.addPara(pre + "Increases fuel production and demand for volatiles by %s.",
				text.addPara(pre + "Increases fuel production output by %s units.",
						pad, Misc.getHighlightColor(), "" + SYNCHROTRON_FUEL_BONUS);
			}
			@Override
			public String[] getSimpleReqs(Industry industry) {
				return new String [] {NO_ATMOSPHERE};
			}
		});
		put(Items.MANTLE_BORE, new BaseInstallableItemEffect(Items.MANTLE_BORE) {
			protected Set<String> getAffectedCommodities(Industry industry) {
				MarketAPI market = industry.getMarket();
				
				Set<String> result = new LinkedHashSet<String>();
				for (MarketConditionAPI mc : market.getConditions()) {
					String cid = mc.getId();
					String commodity = ResourceDepositsCondition.COMMODITY.get(cid);
					for (String curr : MANTLE_BORE_COMMODITIES) {
						if (curr.equals(commodity)) {
							result.add(curr);
						}
					}
				}
				return result;
			}
			public void apply(Industry industry) {
				if (industry instanceof BaseIndustry) {
					BaseIndustry b = (BaseIndustry) industry;
					Set<String> list = getAffectedCommodities(industry);
					
					if (!list.isEmpty()) {
						//b.demand(spec.getId(), Commodities.HEAVY_MACHINERY, MANTLE_BORE_MINING_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
						for (String curr : list) {
							b.supply(spec.getId(), curr, MANTLE_BORE_MINING_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
						}
					}
				}
			}
			public void unapply(Industry industry) {
				if (industry instanceof BaseIndustry) {
					BaseIndustry b = (BaseIndustry) industry;
					
					//b.demand(spec.getId(), Commodities.HEAVY_MACHINERY, 0, null);
					for (String curr : MANTLE_BORE_COMMODITIES) {
						b.supply(spec.getId(), curr, 0, null);
					}
				}
			}
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
					  							  InstallableItemDescriptionMode mode, String pre, float pad) {
				List<String> commodities = new ArrayList<String>();
				for (String curr : MANTLE_BORE_COMMODITIES) {
					CommoditySpecAPI c = Global.getSettings().getCommoditySpec(curr);
					commodities.add(c.getName().toLowerCase());
				}
				text.addPara(pre + "Increases " + Misc.getAndJoined(commodities) + " production by %s units.",
						pad, Misc.getHighlightColor(),
						"" + MANTLE_BORE_MINING_BONUS);
//				text.addPara(pre + "Increases " + Misc.getAndJoined(commodities) + " production by %s units. " +
//						"Increases demand for heavy machinery by %s units.",
//						pad, Misc.getHighlightColor(),
//						"" + MANTLE_BORE_MINING_BONUS,
//						"" + MANTLE_BORE_MINING_BONUS);
			}
			@Override
			public String[] getSimpleReqs(Industry industry) {
				return new String [] {NOT_A_GAS_GIANT, NOT_HABITABLE};
			}
		});	
		put(Items.PLASMA_DYNAMO, new BaseInstallableItemEffect(Items.PLASMA_DYNAMO) {
			protected Set<String> getAffectedCommodities(Industry industry) {
				MarketAPI market = industry.getMarket();
				
				Set<String> result = new LinkedHashSet<String>();
				for (MarketConditionAPI mc : market.getConditions()) {
					String cid = mc.getId();
					String commodity = ResourceDepositsCondition.COMMODITY.get(cid);
					for (String curr : PLASMA_DYNAMO_COMMODITIES) {
						if (curr.equals(commodity)) {
							result.add(curr);
						}
					}
				}
				return result;
			}
			public void apply(Industry industry) {
				if (industry instanceof BaseIndustry) {
					BaseIndustry b = (BaseIndustry) industry;
					Set<String> list = getAffectedCommodities(industry);
					
					if (!list.isEmpty()) {
						//b.demand(spec.getId(), Commodities.HEAVY_MACHINERY, PLASMA_DYNAMO_MINING_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
						for (String curr : list) {
							b.supply(spec.getId(), curr, PLASMA_DYNAMO_MINING_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
						}
					}
				}
			}
			public void unapply(Industry industry) {
				if (industry instanceof BaseIndustry) {
					BaseIndustry b = (BaseIndustry) industry;
					
					//b.demand(spec.getId(), Commodities.HEAVY_MACHINERY, 0, null);
					for (String curr : PLASMA_DYNAMO_COMMODITIES) {
						b.supply(spec.getId(), curr, 0, null);
					}
				}
			}
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
					  							  InstallableItemDescriptionMode mode, String pre, float pad) {
				List<String> commodities = new ArrayList<String>();
				for (String curr : PLASMA_DYNAMO_COMMODITIES) {
					CommoditySpecAPI c = Global.getSettings().getCommoditySpec(curr);
					commodities.add(c.getName().toLowerCase());
				}
				text.addPara(pre + "Increases " + Misc.getAndJoined(commodities) + " production by %s units.",
						pad, Misc.getHighlightColor(),
						"" + PLASMA_DYNAMO_MINING_BONUS);
//				text.addPara(pre + "Increases " + Misc.getAndJoined(commodities) + " production by %s units. " +
//						"Increases demand for heavy machinery by %s units.",
//						pad, Misc.getHighlightColor(),
//						"" + PLASMA_DYNAMO_MINING_BONUS,
//						"" + PLASMA_DYNAMO_MINING_BONUS);
			}
			@Override
			public String[] getSimpleReqs(Industry industry) {
				return new String [] {GAS_GIANT};
			}
		});
		put(Items.BIOFACTORY_EMBRYO, new BoostIndustryInstallableItemEffect(
								Items.BIOFACTORY_EMBRYO, BIOFACTORY_PROD_BONUS, 0) {
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
												  InstallableItemDescriptionMode mode, String pre, float pad) {
				text.addPara(pre + "Increases light industry production by %s units.",
						pad, Misc.getHighlightColor(), 
						"" + (int) BIOFACTORY_PROD_BONUS);
			}
			@Override
			public String[] getSimpleReqs(Industry industry) {
				return new String [] {HABITABLE};
			}
		});
		put(Items.CATALYTIC_CORE, new BoostIndustryInstallableItemEffect(
									Items.CATALYTIC_CORE, CATALYTIC_CORE_BONUS, 0) {
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data, 
												  InstallableItemDescriptionMode mode, String pre, float pad) {
				text.addPara(pre + "Increases refining production by %s units.",
						pad, Misc.getHighlightColor(), 
						"" + (int) CATALYTIC_CORE_BONUS);
			}
			@Override
			public String[] getSimpleReqs(Industry industry) {
				return new String [] {NO_ATMOSPHERE};
			}
		});
		put(Items.FULLERENE_SPOOL, new BaseInstallableItemEffect(Items.FULLERENE_SPOOL) {
			public void apply(Industry industry) {
				industry.getMarket().getAccessibilityMod().modifyFlat(spec.getId(), 
										FULLERENE_SPOOL_ACCESS_BONUS, Misc.ucFirst(spec.getName().toLowerCase()));
			}
			public void unapply(Industry industry) {
				industry.getMarket().getAccessibilityMod().unmodifyFlat(spec.getId());
			}
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
					InstallableItemDescriptionMode mode, String pre, float pad) {
				text.addPara(pre + "Increases colony accessibility by %s.",
						pad, Misc.getHighlightColor(), 
						"" + (int) Math.round(FULLERENE_SPOOL_ACCESS_BONUS * 100f) + "%");
			}
			@Override
			public String[] getSimpleReqs(Industry industry) {
				return new String [] {NOT_A_GAS_GIANT, NOT_EXTREME_WEATHER, NOT_EXTREME_TECTONIC_ACTIVITY};
			}
		});
		put(Items.SOIL_NANITES, new BoostIndustryInstallableItemEffect(
									Items.SOIL_NANITES, SOIL_NANITES_BONUS, 0) {
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data, 
					InstallableItemDescriptionMode mode, String pre, float pad) {
				text.addPara(pre + "Increases farming production by %s units.",
						pad, Misc.getHighlightColor(), 
						"" + (int) SOIL_NANITES_BONUS);
			}
			@Override
			public String[] getSimpleReqs(Industry industry) {
				return new String [] {NO_TRANSPLUTONIC_ORE_DEPOSITS, NO_VOLATILES_DEPOSITS};
			}
		});
		put(Items.CRYOARITHMETIC_ENGINE, new BaseInstallableItemEffect(Items.CRYOARITHMETIC_ENGINE) {
			public void apply(Industry industry) {
				float bonus = 0f;
				if (industry.getMarket().hasCondition(Conditions.HOT)) {
					bonus = CRYOARITHMETIC_FLEET_SIZE_BONUS_HOT;
				} else if (industry.getMarket().hasCondition(Conditions.VERY_HOT)) {
					bonus = CRYOARITHMETIC_FLEET_SIZE_BONUS_VERY_HOT;
				}
				industry.getMarket().getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT)
							.modifyFlat(spec.getId(), bonus, Misc.ucFirst(spec.getName().toLowerCase()));
			}
			public void unapply(Industry industry) {
				industry.getMarket().getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyFlat(spec.getId());
			}
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
					InstallableItemDescriptionMode mode, String pre, float pad) {
				text.addPara(pre + "Increases size of fleets launched by colony by %s for hot worlds, and " +
						"by %s for worlds with extreme heat.",
						pad, Misc.getHighlightColor(), 
						"" + (int) Math.round(CRYOARITHMETIC_FLEET_SIZE_BONUS_HOT * 100f) + "%",
						"" + (int) Math.round(CRYOARITHMETIC_FLEET_SIZE_BONUS_VERY_HOT * 100f) + "%");
			}
			@Override
			public String[] getSimpleReqs(Industry industry) {
				return new String [] {HOT_OR_EXTREME_HEAT};
			}
		});
		put(Items.DRONE_REPLICATOR, new BaseInstallableItemEffect(Items.DRONE_REPLICATOR) {
			public void apply(Industry industry) {
				industry.getMarket().getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
					.modifyMult(spec.getId(), DRONE_REPLICATOR_BONUS_MULT, Misc.ucFirst(spec.getName().toLowerCase()));
			}
			public void unapply(Industry industry) {
				industry.getMarket().getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(spec.getId());
			}
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
					InstallableItemDescriptionMode mode, String pre, float pad) {
				text.addPara(pre + "Colony's ground defenses increased by %s.",
						pad, Misc.getHighlightColor(), 
						Strings.X + DRONE_REPLICATOR_BONUS_MULT);
			}
		});
		put(Items.DEALMAKER_HOLOSUITE, new BaseInstallableItemEffect(Items.DEALMAKER_HOLOSUITE) {
			public void apply(Industry industry) {
				industry.getMarket().getIncomeMult().modifyPercent(spec.getId(), DEALMAKER_INCOME_PERCENT_BONUS,
														Misc.ucFirst(spec.getName().toLowerCase()));
			}
			public void unapply(Industry industry) {
				industry.getMarket().getIncomeMult().unmodifyPercent(spec.getId());
			}
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
					InstallableItemDescriptionMode mode, String pre, float pad) {
				text.addPara(pre + "Colony's income increased by %s.",
						pad, Misc.getHighlightColor(), 
						"" + (int)DEALMAKER_INCOME_PERCENT_BONUS + "%");
			}
		});
		put(Items.CORONAL_PORTAL, new BaseInstallableItemEffect(Items.CORONAL_PORTAL) {
			public void apply(Industry industry) {
				if (industry instanceof BaseIndustry) {
					BaseIndustry b = (BaseIndustry) industry;
					b.demand(8, Commodities.RARE_METALS, CORONAL_TAP_TRANSPLUTONICS, Misc.ucFirst(spec.getName().toLowerCase()));
					
					if (!hasShortage(industry)) {
						industry.getMarket().getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).modifyFlat(
												spec.getId(), CORONAL_TAP_INDUSTRIES);
					} else {
						industry.getMarket().getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodifyFlat(spec.getId());
					}
				}
			}
			public void unapply(Industry industry) {
				if (industry instanceof BaseIndustry) {
					BaseIndustry b = (BaseIndustry) industry;
					b.demand(8, Commodities.RARE_METALS, 0, null);
					industry.getMarket().getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodifyFlat(spec.getId());
				}
			}
			protected boolean hasShortage(Industry industry) {
				//if (true) return false;
				int transplutonicsDemand = industry.getDemand(Commodities.RARE_METALS).getQuantity().getModifiedInt();
				float v = industry.getMarket().getCommodityData(Commodities.RARE_METALS).getAvailable();
				float f = 1f - v / (float) transplutonicsDemand;
				return f > 0;
			}
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
				   	  							  InstallableItemDescriptionMode mode, String pre, float pad) {
				text.addPara(pre + "Increases the maximum number of industries at a colony by %s when demand for " +
						"%s units of transplutonics is fully met.",
						pad, Misc.getHighlightColor(), 
						"" + (int)CORONAL_TAP_INDUSTRIES,
						"" + (int)CORONAL_TAP_TRANSPLUTONICS);
			}
			@Override
			public String[] getSimpleReqs(Industry industry) {
				return new String [] {CORONAL_TAP_RANGE};
			}
		});		
	}};
	
	
	
	
	
}







