package com.fs.starfarer.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.impl.items.GenericSpecialItemPlugin;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.Farming;
import com.fs.starfarer.api.impl.campaign.econ.impl.InstallableItemEffect;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.impl.campaign.econ.impl.PopulationAndInfrastructure;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.misc.HypershuntIntel;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

public class PlanetSearchData {

	public static List<PSToggleButtonRowData> GENERAL_FILTERS = new ArrayList<>();
	public static List<ResourceDepositsData> RESOURCE_DEPOSITS = new ArrayList<>();
	public static List<PlanetFilter> COLONY_ITEMS_AND_CONDITIONS = new ArrayList<>();
	public static List<PSToggleButtonRowData> OTHER_FACTORS = new ArrayList<>();
	
	/**
	 * The accept() method needs to be FAST. Otherwise, may slow the game down when
	 * interacting with the planet list and there are lots of known planets.
	 *
	 */
	public static interface PlanetFilter {
		public boolean accept(SectorEntityToken entity, Map<String, String> params);
		public boolean shouldShow();
		default String getOtherFactorId() {
			return null;
		}
		default String getOtherFactorButtonText() {
			return null;
		}
		
		public void createTooltip(TooltipMakerAPI info, float width, String param);
		default float getTooltipWidth() {
			return 350f;
		}
		default boolean isTooltipExpandable() {
			return false;
		}
		default boolean hasTooltip() {
			return true;
		}
	}
	
	public static class MarketConditionData implements PlanetFilter {
		public String id;
		public String conditionId;
		public MarketConditionSpecAPI spec;
		public MarketConditionData(String conditionId) {
			this.id = "ps_mc_" + conditionId;
			this.conditionId = conditionId;
			
			spec = Global.getSettings().getMarketConditionSpec(conditionId);
		}
		
		@Override
		public boolean accept(SectorEntityToken entity, Map<String, String> params) {
			String selected = params.get(id);
			if (selected == null) return true;
			
			if (entity.getMarket() == null) return false;
			
			return entity.getMarket().hasCondition(conditionId);
		}

		@Override
		public void createTooltip(TooltipMakerAPI info, float width, String param) {
			float opad = 10f;
			info.addTitle(spec.getName());
			String name = spec.getName().toLowerCase();
			info.addPara("Require " + Misc.getAOrAnFor(name) + " " + name + " to be present.", opad);
			info.setCodexEntryId(CodexDataV2.getConditionEntryId(conditionId));
		}

		@Override
		public boolean shouldShow() {
			return true;
		}
	}	
	
	public static class ColonyItemData implements PlanetFilter {
		public String id;
		public String itemId;
		public SpecialItemSpecAPI item;
		public ColonyItemData(String itemId) {
			this.id = "ps_item_" + itemId;
			this.itemId = itemId;
			
			item = Global.getSettings().getSpecialItemSpec(itemId);
		}
		
		@Override
		public boolean accept(SectorEntityToken entity, Map<String, String> params) {
			String selected = params.get(id);
			if (selected == null) return true;
			
			if (entity.getMarket() == null) return false;
			
			if (itemId.equals(Items.CORONAL_PORTAL)) {			
				Pair<SectorEntityToken, Float> p = PopulationAndInfrastructure.getNearestCoronalTap(entity.getLocationInHyperspace(), false, true);
				if (p == null || p.two > ItemEffectsRepo.CORONAL_TAP_LIGHT_YEARS) return false;
				return true;
			} else if (itemId.equals(Items.ORBITAL_FUSION_LAMP)) {
				for (String id : ItemEffectsRepo.FUSION_LAMP_CONDITIONS) {
					if (entity.getMarket().hasCondition(id)) return true;
				}
				return false;
			}
			
			InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(itemId);
			
			BaseIndustry fake = new Farming(); // constructor does nothing much
			fake.setMarket(entity.getMarket());
			
			return effect.getUnmetRequirements(fake).isEmpty();
		}

		@Override
		public boolean shouldShow() {
			if (itemId.equals(Items.CORONAL_PORTAL)) {
				for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(HypershuntIntel.class)) {
					HypershuntIntel hypershunt = (HypershuntIntel) intel;
					if (hypershunt.defendersDefeated()) return true;
				}
				return false;
			}
			return SharedUnlockData.get().isPlayerAwareOfSpecialItem(itemId);
		}

		@Override
		public void createTooltip(TooltipMakerAPI info, float width, String param) {
//			float opad = 10f;
//			String name = item.getName().toLowerCase();
//			info.addPara("Require " + Misc.getAOrAnFor(name) + " " + name + " to be usable on the planet.", opad);
			
			SpecialItemPlugin plugin = item.getNewPluginInstance(null);
			if (plugin instanceof GenericSpecialItemPlugin) {
				((GenericSpecialItemPlugin) plugin).setTooltipIsForPlanetSearch(true);
			}
			plugin.createTooltip(info, false, null, null);
			if (plugin instanceof GenericSpecialItemPlugin) {
				((GenericSpecialItemPlugin) plugin).setTooltipIsForPlanetSearch(false);
			}
			
			info.setCodexEntryId(CodexDataV2.getItemEntryId(item.getId()));
//			InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(itemId);
//			effect.addItemDescription(industry, text, data, mode);
		}
		public float getTooltipWidth() {
			return item.getNewPluginInstance(null).getTooltipWidth();
		}

		@Override
		public boolean isTooltipExpandable() {
			return item.getNewPluginInstance(null).isTooltipExpandable();
		}
	}
	
	
	public static class ResourceDepositsData implements PlanetFilter {
		public String id;
		public String idAny;
		public String idNone;
		public String title;
		public String iconNone = Global.getSettings().getSpriteName("intel", "planet_search_none");
		public String iconAny = Global.getSettings().getSpriteName("intel", "planet_search_any");
		public List<String> conditions = new ArrayList<>();
		public ResourceDepositsData(String id, String title) {
			this.id = id;
			this.idAny = id + "_any";
			this.idNone = id + "_none";
			this.title = title;
		}
		
		@Override
		public boolean accept(SectorEntityToken entity, Map<String, String> params) {
			String selected = params.get(id);
			if (selected == null) return true; // invalid UI state
			
			if (idAny.equals(selected)) {
				return true;
			}
			if (entity.getMarket() == null) {
				return false;
			}
			
			MarketAPI market = entity.getMarket();
			if (market.getSurveyLevel() != SurveyLevel.FULL) {
				return false;
			}
			
			
			if (idNone.equals(selected)) {
				for (String cid : conditions) {
					if (market.hasCondition(cid)) {
						return false;
					}
				}
				return true;
			} else {
				boolean foundAtLeastSelected = false;
				for (String cid : conditions) {
					if (cid.equals(selected)) {
						foundAtLeastSelected = true;
					}
					if (foundAtLeastSelected && market.hasCondition(cid)) {
						return true;
					}
				}
				return false;
			}
		}

		@Override
		public boolean shouldShow() {
			return true;
		}
		
		@Override
		public void createTooltip(TooltipMakerAPI info, float width, String param) {
			float opad = 10f;
			if (param == idAny) {
				info.addTitle(title + " not required");
				info.addPara("Disregard the presence or absence of " + title.toLowerCase() + " on the planet, both are acceptable.", opad);
			} else if (param == idNone) {
				info.addTitle("No " + title.toLowerCase());
				info.addPara("Require that no " + title.toLowerCase() + " be present on the planet.", opad);
			} else if (conditions.contains(param)) {
				MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(param);
				if (spec != null) {
					info.addTitle(spec.getName());
					info.addPara("Require " + spec.getName().toLowerCase() + " or better.", opad);
					info.setCodexEntryId(CodexDataV2.getConditionEntryId(param));
				}
			}
		}
	}
	
	public static enum PSToggleButtonRowMode {
		ANY,
		ONE,
	}
	
	public static class PSToggleButtonData {
		public String id;
		public String text;
		public boolean defaultState = false;
		//public float widthOverride;
		public PSToggleButtonData(String id, String text, boolean defaultState) {
			this.id = id;
			this.text = text;
			this.defaultState = defaultState;
		}
	}
	
	public static class PSToggleButtonRowData implements PlanetFilter {
		public String id;
		public String title;
		public PSToggleButtonRowMode mode;
		public List<PSToggleButtonData> buttons = new ArrayList<>();
		public float prevPad = 0f;
		public PSToggleButtonRowData(String id, String title, PSToggleButtonRowMode mode) {
			this.id = id;
			this.title = title;
			this.mode = mode;
		}
		
		public void add(String id, String text, boolean defaultState) {
			buttons.add(new PSToggleButtonData(id, text, defaultState));
		}

		@Override
		public boolean accept(SectorEntityToken entity, Map<String, String> params) {
			return true;
		}
		
		@Override
		public boolean shouldShow() {
			return true;
		}
		
		public void createTooltip(TooltipMakerAPI info, float width, String param) {
			
		}
	}
	
	static {
		PSToggleButtonRowData type = new PSToggleButtonRowData("type", null, PSToggleButtonRowMode.ANY) {
			@Override
			public boolean accept(SectorEntityToken entity, Map<String, String> params) {
				if (params.containsKey("stars") && entity.isStar()) {
					return true;
				}
				if (params.containsKey("gas_giants") && 
						entity instanceof PlanetAPI && ((PlanetAPI)entity).isGasGiant()) { 
					return true;
				}
				if (params.containsKey("planets") && !entity.isStar() && 
						!(entity instanceof PlanetAPI && ((PlanetAPI)entity).isGasGiant())) {
					return true;
				}
				return false;
			}
			@Override
			public void createTooltip(TooltipMakerAPI info, float width, String param) {
				float opad = 10f;
				if (param.equals("stars")) {
					info.addTitle("Stars");
					info.addPara("Show stars and black holes.", opad);
				} else if (param.equals("gas_giants")) {
					info.addTitle("Gas giants");
					info.addPara("Show gas giants.", opad);
				} else if (param.equals("planets")) {
					info.addTitle("Planets");
					info.addPara("Show non-gas-giant planets and stations.", opad);
				}
			}
			
		};
		type.add("stars", "Stars", false);
		type.add("gas_giants", "Gas giants", true);
		type.add("planets", "Planets", true);
		
		PSToggleButtonRowData populated = new PSToggleButtonRowData("populated", null, PSToggleButtonRowMode.ANY) {
			@Override
			public boolean accept(SectorEntityToken entity, Map<String, String> params) {
				boolean conditionMarketOnly = true;
				if (entity.getMarket() != null) {
					conditionMarketOnly = entity.getMarket().isPlanetConditionMarketOnly();
				}
				if (params.containsKey("populated") && !conditionMarketOnly) {
					return true;
				}
				if (params.containsKey("claimed") && conditionMarketOnly) {
					FactionAPI claimedBy = Misc.getClaimingFaction(entity);
					if (claimedBy != null) return true;
				}
				if (params.containsKey("unclaimed") && conditionMarketOnly) {
					FactionAPI claimedBy = Misc.getClaimingFaction(entity);
					if (claimedBy == null) return true;
				}
				return false;
			}
			@Override
			public void createTooltip(TooltipMakerAPI info, float width, String param) {
				float opad = 10f;
				if (param.equals("populated")) {
					info.addTitle("Populated");
					info.addPara("Show populated planets.", opad);
				} else if (param.equals("claimed")) {
					info.addTitle("Claimed");
					info.addPara("Show uninhabited planets that are in a system claimed by a faction.", opad);
					info.addPara("Colonization is inadvisable.", opad);
				} else if (param.equals("unclaimed")) {
					info.addTitle("Unclaimed");
					info.addPara("Show uninhabited and unclaimed planets.", opad);
				}
			}
		};
		populated.add("populated", "Populated", false);
		populated.add("claimed", "Claimed", false);
		populated.add("unclaimed", "Unclaimed", true);
		
		PSToggleButtonRowData surveyed = new PSToggleButtonRowData("surveyed", null, PSToggleButtonRowMode.ANY) {
			@Override
			public boolean accept(SectorEntityToken entity, Map<String, String> params) {
				if (entity.getMarket() == null) {
					if (entity.isStar()) return true;
					return false;
				}
				if (params.containsKey("surveyed") && entity.getMarket().getSurveyLevel() == SurveyLevel.FULL) {
					return true;
				}
				if (params.containsKey("unsurveyed") && entity.getMarket().getSurveyLevel() != SurveyLevel.FULL) {
					return true;
				}
				return false;
			}
			@Override
			public void createTooltip(TooltipMakerAPI info, float width, String param) {
				float opad = 10f;
				if (param.equals("surveyed")) {
					info.addTitle("Surveyed");
					info.addPara("Show fully surveyed planets.", opad);
				} else if (param.equals("unsurveyed")) {
					info.addTitle("Unsurveyed");
					info.addPara("Show planets that have not been fully surveyed.", opad);
				}
			}
		};
		surveyed.add("surveyed", "Surveyed", true);
		surveyed.add("unsurveyed", "Unsurveyed", true);
		
		PSToggleButtonRowData hazard = new PSToggleButtonRowData("hazard", "Hazard rating", PSToggleButtonRowMode.ONE) {
			@Override
			public boolean accept(SectorEntityToken entity, Map<String, String> params) {
				if (entity.getMarket() == null) {
					if (entity.isStar() && params.containsKey("haz_any")) return true;
					return false;
				}
				
				if (entity.getMarket().getSurveyLevel() != SurveyLevel.FULL &&
						!params.containsKey("haz_any")) {
					return false;
				}
				
				float hazard = entity.getMarket().getHazardValue();
				hazard = Math.round(hazard * 100f) / 100f;
				
				float test = 1000000f;
				if (params.containsKey("haz_1")) test = 1f;
				if (params.containsKey("haz_2")) test = 1.5f;
				if (params.containsKey("haz_3")) test = 2f;
				
				return hazard <= test;
			}
			@Override
			public void createTooltip(TooltipMakerAPI info, float width, String param) {
				float opad = 10f;
				Color h = Misc.getHighlightColor();
				info.addTitle("Hazard rating");
				if (param.equals("haz_any")) {
					info.addPara("Do not filter out any planets based on their hazard rating.", opad);
				} else {
					String rating = "";
					if (param.equals("haz_1")) {
						rating = "100%";
					} else if (param.equals("haz_2")) {
						rating = "150%";
					} else if (param.equals("haz_3")) {
						rating = "200%";
					}
					info.addPara("Only show planets with a hazard rating of %s or lower.", opad, h, rating);
				}
			}
		};
		hazard.add("haz_any", "Any", true);
		hazard.add("haz_1", "100%-", false);
		hazard.add("haz_2", "150%-", false);
		hazard.add("haz_3", "200%-", false);
		hazard.prevPad = 7f; // added to default pad of 3f
		
		PSToggleButtonRowData stablePoints = new PSToggleButtonRowData("stable_locs", "Stable locations", PSToggleButtonRowMode.ONE) {
			@Override
			public boolean accept(SectorEntityToken entity, Map<String, String> params) {
				StarSystemAPI system = entity.getStarSystem();
				if (system == null) return false;
				
				String key = "$core_ps_stableLocations";
				int num = 0;
				if (system.getMemoryWithoutUpdate().contains(key)) {
					num = system.getMemoryWithoutUpdate().getInt(key);
				} else {
					num = Misc.getNumStableLocations(entity.getStarSystem());
					system.getMemoryWithoutUpdate().set(key, num, 0f);
				}
				
				int test = 0;
				if (params.containsKey("stable_1")) test = 1;
				if (params.containsKey("stable_2")) test = 2;
				if (params.containsKey("stable_3")) test = 3;
				
				return num >= test;
			}
			@Override
			public void createTooltip(TooltipMakerAPI info, float width, String param) {
				float opad = 10f;
				Color h = Misc.getHighlightColor();
				info.addTitle("Stable locations");
				if (param.equals("stable_0")) {
					info.addPara("Do not filter out any planets based on the number of stable locations in the star system they are in.", opad);
				} else {
					String locs = "";
					if (param.equals("stable_1")) {
						locs = "1";
					} else if (param.equals("stable_2")) {
						locs = "2";
					} else if (param.equals("stable_3")) {
						locs = "3";
					}
					info.addPara("Only show planets with %s or more stable locations in the star system they're in,"
							+ " including locations already in use by objectives or similar.", opad, h, locs);
				}
			}
		};;
		stablePoints.add("stable_0", "Any", true);
		stablePoints.add("stable_1", "1+", false);
		stablePoints.add("stable_2", "2+", false);
		stablePoints.add("stable_3", "3+", false);
		stablePoints.prevPad = 2f;
		
		
		ResourceDepositsData ore = new ResourceDepositsData("ore", "Ore");
		ore.conditions.add(Conditions.ORE_SPARSE);
		ore.conditions.add(Conditions.ORE_MODERATE);
		ore.conditions.add(Conditions.ORE_ABUNDANT);
		ore.conditions.add(Conditions.ORE_RICH);
		ore.conditions.add(Conditions.ORE_ULTRARICH);
		
		ResourceDepositsData rareOre = new ResourceDepositsData("rare_ore", "Transplutonic ore");
		rareOre.conditions.add(Conditions.RARE_ORE_SPARSE);
		rareOre.conditions.add(Conditions.RARE_ORE_MODERATE);
		rareOre.conditions.add(Conditions.RARE_ORE_ABUNDANT);
		rareOre.conditions.add(Conditions.RARE_ORE_RICH);
		rareOre.conditions.add(Conditions.RARE_ORE_ULTRARICH);
		
		ResourceDepositsData volatiles = new ResourceDepositsData("volatiles", "Volatiles");
		volatiles.conditions.add(Conditions.VOLATILES_TRACE);
		volatiles.conditions.add(Conditions.VOLATILES_DIFFUSE);
		volatiles.conditions.add(Conditions.VOLATILES_ABUNDANT);
		volatiles.conditions.add(Conditions.VOLATILES_PLENTIFUL);
		
		ResourceDepositsData organics = new ResourceDepositsData("organics", "Organics");
		organics.conditions.add(Conditions.ORGANICS_TRACE);
		organics.conditions.add(Conditions.ORGANICS_COMMON);
		organics.conditions.add(Conditions.ORGANICS_ABUNDANT);
		organics.conditions.add(Conditions.ORGANICS_PLENTIFUL);
		
		ResourceDepositsData farmland = new ResourceDepositsData("farmland", "Farmland");
		farmland.conditions.add(Conditions.FARMLAND_POOR);
		farmland.conditions.add(Conditions.FARMLAND_ADEQUATE);
		farmland.conditions.add(Conditions.FARMLAND_RICH);
		farmland.conditions.add(Conditions.FARMLAND_BOUNTIFUL);
		
		ResourceDepositsData ruins = new ResourceDepositsData("ruins", "Ruins");
		ruins.conditions.add(Conditions.RUINS_SCATTERED);
		ruins.conditions.add(Conditions.RUINS_WIDESPREAD);
		ruins.conditions.add(Conditions.RUINS_EXTENSIVE);
		ruins.conditions.add(Conditions.RUINS_VAST);
		
		
		ResourceDepositsData hab = new ResourceDepositsData("habitable", "Hab");
		hab.conditions.add(Conditions.HABITABLE);
		
		ResourceDepositsData cold = new ResourceDepositsData("cold", "Cold");
		cold.conditions.add(Conditions.COLD);
		cold.conditions.add(Conditions.VERY_COLD);
		
		ResourceDepositsData heat = new ResourceDepositsData("heat", "Heat");
		heat.conditions.add(Conditions.HOT);
		heat.conditions.add(Conditions.VERY_HOT);
		
		ResourceDepositsData atmo = new ResourceDepositsData("atmo", "Atmo");
		atmo.conditions.add(Conditions.NO_ATMOSPHERE);
		
		ResourceDepositsData weather = new ResourceDepositsData("weather", "Wthr");
		weather.conditions.add(Conditions.EXTREME_WEATHER);
		
		ResourceDepositsData darkness = new ResourceDepositsData("darkness", "Dark");
		darkness.conditions.add(Conditions.POOR_LIGHT);
		darkness.conditions.add(Conditions.DARK);
		
		ResourceDepositsData tectonics = new ResourceDepositsData("tectonics", "Tect");
		tectonics.conditions.add(Conditions.TECTONIC_ACTIVITY);
		tectonics.conditions.add(Conditions.EXTREME_TECTONIC_ACTIVITY);
		
//		PSToggleButtonRowData cryosleeper = new PSToggleButtonRowData("cryosleeper", null, PSToggleButtonRowMode.ANY) {
//			@Override
//			public boolean accept(SectorEntityToken entity, Map<String, String> params) {
//				if (!params.containsKey("cryosleeper")) return true;
//				
//				if (entity.getMarket() == null) return false;
//				
//				Pair<SectorEntityToken, Float> p = Cryorevival.getNearestCryosleeper(entity.getLocationInHyperspace(), false);
//				if (p == null || p.two > Cryorevival.MAX_BONUS_DIST_LY) return false;
//				return true;
//			}
//
//			@Override
//			public boolean shouldShow() {
//				return Global.getSector().getIntelManager().getIntelCount(CryosleeperIntel.class, true) > 0;
//			}
//			@Override
//			public void createTooltip(TooltipMakerAPI info, float width, String param) {
//				float opad = 10f;
//				Color h = Misc.getHighlightColor();
//				IndustrySpecAPI spec = Global.getSettings().getIndustrySpec(Industries.CRYOREVIVAL);
//				info.addTitle("Cryosleeper");
//				info.addPara("Only show planets within %s light-years of a Domain-era Cryosleeper. Colonies "
//						+ "within range can build a %s and benefit from hugely increased population growth.", 
//						opad, h, "" + (int)Math.round(Cryorevival.MAX_BONUS_DIST_LY), spec.getName());
//			}
//		};
//		cryosleeper.add("cryosleeper", "Domain-era Cryosleeper within range", false);
		
		//This is a problem if the static block is triggered earlier (e.g. if a mod adds to it in its ModPlugin)
		// thus: moving this to internal code when the filter is initialized
		// can still add things to OTHER_FACTORS here if it's something that does not depend on the campaign instance.
//		for (ColonyOtherFactorsListener curr : Global.getSector().getListenerManager().
//												getListeners(ColonyOtherFactorsListener.class)) {
//			if (!(curr instanceof PlanetFilter)) continue;
//			final PlanetFilter filter = (PlanetFilter) curr;
//			final String id = filter.getOtherFactorId();
//			if (id == null) continue;
//			
//			PSToggleButtonRowData data = new PSToggleButtonRowData(id, null, PSToggleButtonRowMode.ANY) {
//				@Override
//				public boolean accept(SectorEntityToken entity, Map<String, String> params) {
//					return filter.accept(entity, params);
//				}
//				@Override
//				public boolean shouldShow() {
//					return filter.shouldShow();
//				}
//				@Override
//				public void createTooltip(TooltipMakerAPI info, float width, String param) {
//					filter.createTooltip(info, width, param);
//				}
//			};
//			data.add(id, filter.getOtherFactorButtonText(), false);
//			
//			OTHER_FACTORS.add(data);
//		}

// handling this using the Hypershunt Tap in the items section instead
//		PSToggleButtonRowData hypershunt = new PSToggleButtonRowData("hypershunt", null, PSToggleButtonRowMode.ANY) {
//			@Override
//			public boolean accept(SectorEntityToken entity, Map<String, String> params) {
//				if (!params.containsKey("hypershunt")) return true;
//				
//				if (entity.getMarket() == null) return false;
//				
//				Pair<SectorEntityToken, Float> p = PopulationAndInfrastructure.getNearestCoronalTap(entity.getLocationInHyperspace(), false, true);
//				if (p == null || p.two > ItemEffectsRepo.CORONAL_TAP_LIGHT_YEARS) return false;
//				return true;
//			}
//			
//			@Override
//			public boolean shouldShow() {
//				for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(HypershuntIntel.class)) {
//					HypershuntIntel hypershunt = (HypershuntIntel) intel;
//					if (hypershunt.defendersDefeated()) return true;
//				}
//				return false;
//			}
//			
//		};
//		hypershunt.add("hypershunt", "Coronal Hypershunt within range", false);
		
		
		GENERAL_FILTERS.add(type);
		GENERAL_FILTERS.add(populated);
		GENERAL_FILTERS.add(surveyed);
		GENERAL_FILTERS.add(hazard);
		GENERAL_FILTERS.add(stablePoints);
		
		RESOURCE_DEPOSITS.add(ore);
		RESOURCE_DEPOSITS.add(rareOre);
		RESOURCE_DEPOSITS.add(volatiles);
		RESOURCE_DEPOSITS.add(organics);
		RESOURCE_DEPOSITS.add(farmland);
		RESOURCE_DEPOSITS.add(ruins);
		
		
		List<SpecialItemSpecAPI> items = new ArrayList<>();
		for (SpecialItemSpecAPI spec : Global.getSettings().getAllSpecialItemSpecs()) {
			if (spec.hasTag(Tags.PLANET_SEARCH)) {
				items.add(spec);
			}
		}
		Collections.sort(items, (s1, s2) -> s1.getName().compareTo(s2.getName()));
		items.forEach(item -> COLONY_ITEMS_AND_CONDITIONS.add(new ColonyItemData(item.getId())));
		
		
		List<MarketConditionSpecAPI> conditions = new ArrayList<>();
		for (MarketConditionSpecAPI spec : Global.getSettings().getAllMarketConditionSpecs()) {
			if (spec.hasTag(Tags.PLANET_SEARCH)) {
				conditions.add(spec);
			}
		}
		Collections.sort(conditions, (c1, c2) -> c1.getName().compareTo(c2.getName()));
		conditions.forEach(condition -> COLONY_ITEMS_AND_CONDITIONS.add(new MarketConditionData(condition.getId())));
		
		
//		kinda torn on what I prefer; the below seems more readable and easier to change/put breakpoints in -am
//		Collections.sort(items, new Comparator<SpecialItemSpecAPI>() {
//			@Override
//			public int compare(SpecialItemSpecAPI o1, SpecialItemSpecAPI o2) {
//				return o1.getName().compareTo(o2.getName());
//			}
//		});
//		for (SpecialItemSpecAPI item : items) {
//			COLONY_ITEMS.add(new ColonyItemData(item.getId()));
//		}
		
		//COLONY_FACTORS.add(cryosleeper);
//		COLONY_FACTORS.add(hypershunt);
	}
}










