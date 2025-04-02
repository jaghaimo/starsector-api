package com.fs.starfarer.api.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.FactionSpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.ShipSystemSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.misc.SimUpdateIntel;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantOfficerGeneratorPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.plugins.AutofitPlugin.AutofitPluginDelegate;
import com.fs.starfarer.api.plugins.AutofitPlugin.AvailableFighter;
import com.fs.starfarer.api.plugins.AutofitPlugin.AvailableWeapon;
import com.fs.starfarer.api.plugins.SimulatorPlugin;
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SimulatorPluginImpl implements SimulatorPlugin, AutofitPluginDelegate {

	public static boolean INCLUDE_PLAYER_BLUEPRINTS = false;
	public static boolean REQUIRE_AI_CORES = true;
	public static boolean REQUIRE_AI_CORES_IN_CARGO = Global.getSettings().getBoolean("requireAICoresInCargoForSimulator");
	
	
	public static String UNLOCKS_DATA_FILE		= "core_sim_unlocks.json";
	public static String CUSTOM_OPPONENTS_FILE	= "core_sim_custom_opponents.json";
	public static String UI_STATE_DATA_FILE 	= "core_sim_settings.json";
	
	public static String DEFAULT_CAT_ID 		= "cat_default";
	public static String CUSTOM_CAT_ID 			= "cat_custom";
	public static String OTHER_CAT_ID 			= "cat_other";
	public static String CIV_CAT_ID 			= "cat_civ";
	public static String DEV_CAT_ID 			= "cat_dev";
	
	public static String AGGRO_ID 				= "aggro";
	public static String AGGRO_ID_CORES_ONLY	= "aggro_cores";
	public static String AGGRO_DEFAULT 			= "default";
	public static String AGGRO_CAUTIOUS 		= "cautious";
	public static String AGGRO_STEADY 			= "steady";
	public static String AGGRO_AGGRESSIVE 		= "aggressive";
	public static String AGGRO_RECKLESS 		= "reckless";
	public static String AGGRO_NORMAL	 		= "normal";
	public static String AGGRO_DO_NOTHING 		= "do_nothing";
	public static String AGGRO_DEFENSES 		= "defenses_only";
	public static String AGGRO_STATIONARY 		= "stationary";
	
	
	public static String OFFICERS_CUSTOM_ID 	= "officers_custom";
	public static String OFFICERS_CUSTOM_NONE 	= "none";
	public static String OFFICERS_CUSTOM_SOME	= "some";
	public static String OFFICERS_CUSTOM_5 		= "level5";
	public static String OFFICERS_CUSTOM_6	 	= "level6";
	
	public static String OFFICERS_ID 			= "officers";
	public static String OFFICERS_NONE 			= "none";
	public static String OFFICERS_DEFAULT 		= "default";
	public static String OFFICERS_ALL	 		= "all";
	
	public static String QUALITY_ID		 		= "quality";
	public static String QUALITY_MAX_DMODS 		= "max_dmods";
	public static String QUALITY_SOME_DMODS 	= "some_dmods";
	public static String QUALITY_NO_DMODS 		= "no_dmods";
	public static String QUALITY_SOME_SDMODS 	= "some_smods";
	public static String QUALITY_MANY_SMODS 	= "many_smods";
	
	public static String AI_CORES_ID		 	= "ai_cores";
	public static String AI_CORES_DERELICT_ID	= "ai_cores_derelict";
	public static String AI_CORES_OMEGA_ID 		= "ai_cores_omega";
	public static String AI_CORES_DEV_ID 		= "ai_cores_dev";
	public static String AI_CORES_NONE	 		= "none";
	public static String AI_CORES_SOME 			= "some";
	public static String AI_CORES_GAMMA			= "gamma";
	public static String AI_CORES_BETA			= "beta";
	public static String AI_CORES_ALPHA			= "alpha";
	public static String AI_CORES_OMEGA			= "omega";

	public static String RANDOMIZE_VARIANTS_ID	= "randomize_variants";
	public static String INTEGRATE_CORES_ID	= "integrate_cores";
	
		
	
	public static boolean isShowDevCategories() {
		return (Global.getSettings().isDevMode() && !Global.getSettings().getBoolean("playtestingMode")) ||
				!Global.getCombatEngine().isInCampaignSim();
	}
	
	public static boolean isSimFullyUnlocked() {
		return (Global.getSettings().isDevMode() && !Global.getSettings().getBoolean("playtestingMode")) ||
				!Global.getCombatEngine().isInCampaignSim();
	}
	
	public static boolean isAllStandardStuffUnlocked() {
		return Global.getSettings().getBoolean("allStandardShipsAndFactionsUnlockedInSimulator");
	}

	public static class SimUnlocksData {
		public LinkedHashSet<String> factions = new LinkedHashSet<String>();
		public LinkedHashSet<String> variants = new LinkedHashSet<String>();
		
		public void fromJSON(JSONObject json) {
			factions = new LinkedHashSet<String>();
			JSONArray arr = json.optJSONArray("factions");
			if (arr != null) {
				for (int i = 0; i < arr.length(); i++) {
					String value = arr.optString(i);
					if (value != null) {
						factions.add(value);
					}
				}
			}
			variants = new LinkedHashSet<String>();
			arr = json.optJSONArray("variants");
			if (arr != null) {
				for (int i = 0; i < arr.length(); i++) {
					String value = arr.optString(i);
					if (value != null) {
						variants.add(value);
					}
				}
			}
		}
		
		public JSONObject toJSON() throws JSONException {
			JSONObject json = new JSONObject();

			JSONArray arr1 = new JSONArray();
			for (String value : factions) {
				arr1.put(value);
			}
			JSONArray arr2 = new JSONArray();
			for (String value : variants) {
				arr2.put(value);
			}
			
			json.put("factions", arr1);
			json.put("variants", arr2);

			return json;
		}
	}
	
	
	
	
	protected SimUIStateData uiStateData = new SimUIStateData();
	protected Set<String> defaultOpponents = new LinkedHashSet<String>();
	protected Set<String> customOpponents = new LinkedHashSet<String>();
	protected boolean loadedStuff = false;
	protected SimUnlocksData unlocksData = new SimUnlocksData();
	
	public SimulatorPluginImpl() {
		loadUIStateData();
	}
	
	public boolean coreReqsMet(String coreId) {
		if (!REQUIRE_AI_CORES || !Global.getCombatEngine().isInCampaignSim()) return true;
		if (isSimFullyUnlocked()) return true;
		return Global.getSector().getPlayerFleet().getCargo().getQuantity(CargoItemType.RESOURCES, coreId) > 0;
	}
	
	public boolean isRequireAICoresInCargo() {
		if (!REQUIRE_AI_CORES_IN_CARGO) return false;
		if (isSimFullyUnlocked()) return false;
		return Global.getCombatEngine().isInCampaignSim();
	}
	
	
	public void addCustomOpponents(List<String> variants) {
		customOpponents.addAll(variants);
	}
	
	public void removeCustomOpponents(List<String> variants) {
		customOpponents.removeAll(variants);
	}
	
	public void clearCustomOpponents() {
		customOpponents = new LinkedHashSet<String>();
//		for (String variantId : Global.getSettings().getSimOpponents()) {
//			if (Global.getSettings().getVariant(variantId) == null) continue;
//			customOpponents.add(variantId);
//		}
	}
	
	public void loadCustomOpponents() {
		try {
			if (Global.getSettings().fileExistsInCommon(CUSTOM_OPPONENTS_FILE)) {
				customOpponents = new LinkedHashSet<String>();
				JSONObject json = Global.getSettings().readJSONFromCommon(CUSTOM_OPPONENTS_FILE, true);
				JSONArray arr = json.optJSONArray("opponents");
				if (arr != null) {
					for (int i = 0; i < arr.length(); i++) {
						String variantId = arr.getString(i);
						if (Global.getSettings().getVariant(variantId) == null) continue;
						customOpponents.add(variantId);
					}
				}
			} else {
				clearCustomOpponents();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void saveCustomOpponents() {
		try {
			JSONObject json = new JSONObject();
			JSONArray arr = new JSONArray();
			for (String variantId : customOpponents) {
				arr.put(variantId);
			}
			json.put("opponents", arr);
			Global.getSettings().writeJSONToCommon(CUSTOM_OPPONENTS_FILE, json, true);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void resetToDefaults(boolean withSave) {
		uiStateData.settings.clear();
		uiStateData.settings.put(AGGRO_ID, AGGRO_DEFAULT);
		uiStateData.settings.put(OFFICERS_CUSTOM_ID, OFFICERS_NONE);
		uiStateData.settings.put(OFFICERS_ID, OFFICERS_NONE);
		uiStateData.settings.put(QUALITY_ID, QUALITY_NO_DMODS);
		uiStateData.settings.put(AI_CORES_ID, AI_CORES_NONE);
		uiStateData.settings.put(AI_CORES_DERELICT_ID, AI_CORES_NONE);
		uiStateData.settings.put(AI_CORES_OMEGA_ID, AI_CORES_NONE);
		uiStateData.settings.put(RANDOMIZE_VARIANTS_ID, "false");
		uiStateData.settings.put(INTEGRATE_CORES_ID, "false");
		uiStateData.groupSize = 0;
		if (withSave) {
			saveUIStateData();
		}
	}
	
	public void loadUIStateData() {
		try {
			if (Global.getSettings().fileExistsInCommon(UI_STATE_DATA_FILE)) {
				JSONObject json = Global.getSettings().readJSONFromCommon(UI_STATE_DATA_FILE, true);
				uiStateData.fromJSON(json);
			} else {
				uiStateData.selectedCategory = DEFAULT_CAT_ID;
				uiStateData.showAdvanced = false;
				resetToDefaults(false);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void saveUIStateData() {
		try {
			JSONObject json = uiStateData.toJSON();
			Global.getSettings().writeJSONToCommon(UI_STATE_DATA_FILE, json, true);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public SimUIStateData getUIStateData() {
		return uiStateData;
	}

	public void loadUnlocksData() {
		try {
			defaultOpponents.clear();
			defaultOpponents.addAll(Global.getSettings().getSimOpponents());
			
			if (Global.getSettings().fileExistsInCommon(UNLOCKS_DATA_FILE)) {
				JSONObject json = Global.getSettings().readJSONFromCommon(UNLOCKS_DATA_FILE, true);
				unlocksData.fromJSON(json);
				
				//unlocksData.variants.addAll(Global.getSettings().getSimOpponents());
			} else {
			
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void saveUnlocksData() {
		try {
			JSONObject json = unlocksData.toJSON();
			Global.getSettings().writeJSONToCommon(UNLOCKS_DATA_FILE, json, true);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public SimUnlocksData getUnlocksData() {
		return unlocksData;
	}

	public List<AdvancedSimOption> getSimOptions(SimCategoryData category) {
		List<AdvancedSimOption> result = new ArrayList<AdvancedSimOption>();

		boolean custom = CUSTOM_CAT_ID.equals(category.id);
		boolean other = OTHER_CAT_ID.equals(category.id);
		boolean defaultCat = DEFAULT_CAT_ID.equals(category.id);
		boolean civ = CIV_CAT_ID.equals(category.id);
		boolean dev = DEV_CAT_ID.equals(category.id);
		boolean customFaction = custom || civ || dev || defaultCat || other;
		
		JSONObject json = category.faction.getCustom().optJSONObject("simulatorData");
		boolean standardAICores = json != null && json.optBoolean("standardAICores");
		boolean derelictAICores = json != null && json.optBoolean("derelictAICores");
		boolean omegaAICores = json != null && json.optBoolean("omegaAICores");
		boolean noOfficers = json != null && json.optBoolean("noOfficers");
		
		boolean showOfficers = !standardAICores && !derelictAICores && !omegaAICores && !noOfficers;
		boolean integrateCores = standardAICores || derelictAICores || dev || custom || other;
		
		if (custom || defaultCat || other) {
			showOfficers = true;
			standardAICores = true;
			integrateCores = true;
		}
		
		String aggroExtra = "";
		String aiExtra = "";
		if (custom || defaultCat || dev || other) {
			aggroExtra = "\n\nDoes not affect AI cores or automated ships.";
			aiExtra = "\n\nOnly affects automated ships.";
		}
		
		String aggroTitle = "Aggression / behavior";
		String aggroId = AGGRO_ID;
		if (!showOfficers) {
			aggroTitle = "Behavior";
			aggroId = AGGRO_ID_CORES_ONLY;
		}
		
		SimOptionSelectorData aggro = new SimOptionSelectorData(aggroId, aggroTitle, true);
		if (showOfficers) {
			if (custom || defaultCat || civ || dev || other) {
				aggro.options.add(new SimOptionData(AGGRO_DEFAULT, "Default (steady)", 
						Global.getSettings().getPersonaltySpec(Personalities.STEADY).getDescription().replaceAll("officer", "aggression level") + aggroExtra, "behavior_default"));
			} else {
				aggro.options.add(new SimOptionData(AGGRO_DEFAULT, "Faction default", 
						"Default aggression level for the selected faction." + aggroExtra, "behavior_default"));
			}
			aggro.options.add(new SimOptionData(AGGRO_CAUTIOUS, "Cautious",
					Global.getSettings().getPersonaltySpec(Personalities.CAUTIOUS).getDescription().replaceAll("officer", "aggression level") + aggroExtra, "behavior_cautious"));
			aggro.options.add(new SimOptionData(AGGRO_STEADY, "Steady",
					Global.getSettings().getPersonaltySpec(Personalities.STEADY).getDescription().replaceAll("officer", "aggression level") + aggroExtra, "behavior_steady"));
			aggro.options.add(new SimOptionData(AGGRO_AGGRESSIVE, "Aggressive", 
					Global.getSettings().getPersonaltySpec(Personalities.AGGRESSIVE).getDescription().replaceAll("officer", "aggression level") + aggroExtra, "behavior_aggressive"));
			aggro.options.add(new SimOptionData(AGGRO_RECKLESS, "Reckless", 
					Global.getSettings().getPersonaltySpec(Personalities.RECKLESS).getDescription().replaceAll("officer", "aggression level") + aggroExtra, "behavior_reckelss"));
		} else {
			aggro.options.add(new SimOptionData(AGGRO_NORMAL, "Normal", "Opposing ships will behave normally.", "behavior_default"));
		}
		
		aggro.options.add(new SimOptionData(AGGRO_DO_NOTHING, "Do nothing", "Opposing ships will not move, use shields, fire weapons, or take any other actions.", "behavior_passive"));
		if (showOfficers) {
			aggro.options.get(aggro.options.size() - 1).extraPad = 10f;
		}
		aggro.options.add(new SimOptionData(AGGRO_DEFENSES, "Stationary, defenses only", "Opposing ships will not move, but will use shields/phase cloak/other defenses and defensive ship systems, if any.", "behavior_defensive"));
		aggro.options.add(new SimOptionData(AGGRO_STATIONARY, "Stationary", "Opposing ships will not move, but will otherwise behave normally.", "behavior_stationary"));
		aggro.compact = false;
		result.add(aggro);
		
		
		if (showOfficers) {
			if (customFaction) {
				SimOptionSelectorData officers = new SimOptionSelectorData(OFFICERS_CUSTOM_ID, "Officers", true);
				officers.options.add(new SimOptionData(OFFICERS_CUSTOM_NONE, "None", "No officers on any opposing ships.", "officers_none"));
				officers.options.add(new SimOptionData(OFFICERS_CUSTOM_SOME, "Some", "Some officers, up to level 5.", "officers_some"));
				officers.options.add(new SimOptionData(OFFICERS_CUSTOM_5, "All ships, level 5", "Level 5 officers on all opposing ships.", "officers_all"));
				officers.options.add(new SimOptionData(OFFICERS_CUSTOM_6, "All ships, level 6", "Level 6 officers on all opposing ships.", "officers_high"));
				result.add(officers);			
			} else {
				SimOptionSelectorData officers = new SimOptionSelectorData(OFFICERS_ID, "Officers", true);
				officers.options.add(new SimOptionData(OFFICERS_NONE, "None", "No officers on any opposing ships.", "officers_none"));
				officers.options.add(new SimOptionData(OFFICERS_DEFAULT, "Faction default", "Default number and level of officers for the selected faction.", "officers_some"));
				officers.options.add(new SimOptionData(OFFICERS_ALL, "All ships", "Maximum level officers on all opposing ships.", "officers_high"));
				result.add(officers);
			}
		}
		
		if (standardAICores || derelictAICores || omegaAICores || custom || defaultCat || dev || other) {
			String coresId = AI_CORES_ID;
			if (dev) {
				coresId = AI_CORES_DEV_ID;
			} else if (omegaAICores) {
				coresId = AI_CORES_OMEGA_ID;
			} else if (derelictAICores) {
				coresId = AI_CORES_DERELICT_ID;
			}
			SimOptionSelectorData cores = new SimOptionSelectorData(coresId, "AI cores", false);
			cores.options.add(new SimOptionData(AI_CORES_NONE, "None", "No AI cores on any opposing ships.", "cores_none"));

			boolean enableAlpha = coreReqsMet(Commodities.ALPHA_CORE);
			boolean enableBeta = coreReqsMet(Commodities.BETA_CORE) || enableAlpha;
			boolean enableGamma = coreReqsMet(Commodities.GAMMA_CORE) || enableBeta;
			boolean enableMixed = enableAlpha || (derelictAICores && enableGamma);

			String reqGamma = null;
			if (!enableGamma) reqGamma = "Requires a Gamma Core or better in your cargo.";
			String reqBeta = null;
			if (!enableBeta) reqBeta = "Requires a Beta Core or better in your cargo.";
			String reqAlpha = null;
			if (!enableAlpha) reqAlpha = "Requires an Alpha Core in your cargo.";
			String reqMixed = null;
			if (!enableMixed) {
				if (derelictAICores) {
					reqMixed = "Requires a Gamma Core or better in your cargo.";
				} else {
					reqMixed = "Requires an Alpha Core in your cargo.";
				}
			}
			
			String coresCargoNote = "";
			String ifPossible = "";
			if (isRequireAICoresInCargo()) {
				coresCargoNote = " The AI cores used are limited to the total number and type "
						+ "of cores in your cargo (and storage, if docked).";
				ifPossible = ", if possible";
				boolean canUseCores = enableGamma || enableBeta || enableAlpha || enableMixed;
				if (canUseCores) {
					enableGamma = enableBeta = enableAlpha = enableMixed = true;
					reqGamma = reqBeta = reqAlpha = reqMixed = null;
				} else {
					enableGamma = enableBeta = enableAlpha = enableMixed = false;
					reqGamma = reqBeta = reqAlpha = reqMixed = "No AI cores in cargo (or storage, if docked).";
				}
			}
			
			if (standardAICores || derelictAICores || dev) {
				cores.options.add(new SimOptionData(AI_CORES_SOME, "Mixed", "A mix of AI cores on some of the opposing ships, based on the number and size of opponents deployed." + coresCargoNote + aiExtra, enableMixed, reqMixed, "cores_mixed"));
				cores.options.add(new SimOptionData(AI_CORES_GAMMA, "Gamma cores on all ships", "A gamma core on every opposing ship" + ifPossible + "." + coresCargoNote + aiExtra, enableGamma, reqGamma, "cores_gamma"));
				if (standardAICores || dev) {
					cores.options.add(new SimOptionData(AI_CORES_BETA, "Beta cores on all ships", "A beta core on every opposing ship" + ifPossible + "." + coresCargoNote + aiExtra, enableBeta, reqBeta, "cores_beta"));
					cores.options.add(new SimOptionData(AI_CORES_ALPHA, "Alpha cores on all ships", "An alpha core on every opposing ship" + ifPossible + "." + coresCargoNote + aiExtra, enableAlpha, reqAlpha, "cores_alpha"));
				}
			}
			if (dev || omegaAICores) {
				cores.options.add(new SimOptionData(AI_CORES_OMEGA, "Omega cores on all ships", "An omega core on every opposing ship." + aiExtra, "cores_omega"));
			}
			if (!omegaAICores) {
				cores.padAfter = 10f;
			}
			result.add(cores);	
		}
		
		if (integrateCores) {
			String iTooltipExtra = "";
			if (dev) {
				iTooltipExtra = "\n\nDoes not affect omega cores.";
			}
			SimOptionCheckboxData integrate = new SimOptionCheckboxData(INTEGRATE_CORES_ID, "Integrate AI cores",
					"AI cores will be integrated into opposing ships, increasing each core's level by 1." + iTooltipExtra);
			// no longer relevant since aggression/behavior is no longer condensed and is taller
//			if (!custom && !dev && !defaultCat && !other) { 
//				integrate.padAfter += 8f;
//			}
			result.add(integrate);
		}

		
		SimOptionSelectorData quality = new SimOptionSelectorData(QUALITY_ID, "Ship quality", true);
		quality.options.add(new SimOptionData(QUALITY_MAX_DMODS, "Maximum d-mods", "Five d-mods on all opposing ships.", "quality_lowest"));
		quality.options.add(new SimOptionData(QUALITY_SOME_DMODS, "Some d-mods", "Two to four d-mods on all opposing ships.", "quality_low"));
		quality.options.add(new SimOptionData(QUALITY_NO_DMODS, "No d-mods", "No d-mods on any opposing ships.", "quality_no_dmods"));
		quality.options.add(new SimOptionData(QUALITY_SOME_SDMODS, "Some s-mods", "One or two s-mods on all opposing ships.", "quality_high"));
		quality.options.add(new SimOptionData(QUALITY_MANY_SMODS, "Many s-mods", "Two or three s-mods on all opposing ships.", "quality_highest"));
		result.add(quality);
		
		// no longer relevant since aggression/behavior is no longer condensed and is taller
//		if (!custom && !dev && !defaultCat && !other) {
//			quality.padAfter += 11f; // to line it up with the separator line between deployed/reserve
//		}
		
		
		String rTooltipExtra = "";
		if (customFaction) {
			rTooltipExtra = "\n\nFor the selected category, uses a broader set of weapons than is available to most factions.";
		}
		SimOptionCheckboxData loadouts = new SimOptionCheckboxData(RANDOMIZE_VARIANTS_ID, "Randomized loadouts",
				"Opposing ships have randomized loadouts when this setting is enabled." + rTooltipExtra);
		result.add(loadouts);
		
		return result;
	}
	
	public boolean showGroupDeploymentWidget(SimCategoryData category) {
		boolean custom = CUSTOM_CAT_ID.equals(category.id);
		boolean other = OTHER_CAT_ID.equals(category.id);
		boolean defaultCat = DEFAULT_CAT_ID.equals(category.id);
		boolean civ = CIV_CAT_ID.equals(category.id);
		boolean dev = DEV_CAT_ID.equals(category.id);
		boolean customFaction = custom || civ || dev || defaultCat;
		
		if (civ) return false;
		
		return true;
	}
	
	public SimCategoryData getCustomCategory() {
		SimCategoryData custom = new SimCategoryData();
		custom.id = CUSTOM_CAT_ID;
		custom.name = "Custom";
		custom.custom = true;
		custom.nonFactionCategory = true;
		custom.nameColor = Misc.getBasePlayerColor();
		custom.iconName = Global.getSettings().getSpriteName("simulator", "customPlayerCrest");
		custom.data = null;
		custom.faction = createCustomFaction();
		custom.variants = getVariantIDList(sortVariantList(getVariantList(customOpponents)));
		return custom;
	}
	
	
	public List<SimCategoryData> getCategories() {
		if (!loadedStuff) {
			loadCustomOpponents();
			loadUnlocksData();
			loadedStuff = true;
		}
		
		boolean fullUnlock = isSimFullyUnlocked() || isAllStandardStuffUnlocked();
		
		List<SimCategoryData> result = new ArrayList<SimulatorPlugin.SimCategoryData>();
		
		Set<String> civilian = new LinkedHashSet<String>();
		
		Set<String> other = new LinkedHashSet<String>(unlocksData.variants);
//		if (other.contains("doom_Strike")) {
//			System.out.println("23dfefewf");
//		}
//		other.clear();
		if (Global.getCombatEngine().isInCampaignSim() && INCLUDE_PLAYER_BLUEPRINTS) {
			FactionAPI player = Global.getSector().getPlayerFaction();
			for (String roleId : getAllRoles()) {
				Set<String> variants = player.getVariantsForRole(roleId);
				if (variants == null) continue;
				other.addAll(variants);
			}
		}
		
		
		for (FactionSpecAPI spec : Global.getSettings().getAllFactionSpecs()) {
			if (spec == null || spec.getCustom() == null) continue;
			
			JSONObject json = spec.getCustom().optJSONObject("simulatorData");
			if (json == null) continue;
			
			boolean show = json.optBoolean("showInSimulator");
			show |= isShowDevCategories() && json.optBoolean("showInSimulatorDevModeOnly");
			
			show &= fullUnlock || unlocksData.factions.contains(spec.getId());
			
			if (!show) continue;
			
			boolean includeCiv = json.optBoolean("includeCivShipsWithFaction");
			
			SimCategoryData data = new SimCategoryData();
			data.id = spec.getId();
			data.name = Misc.ucFirst(spec.getDisplayName());
			data.nameColor = spec.getBaseUIColor();
			data.iconName = spec.getCrest();
			data.data = spec;
			data.variants = getVariants(spec, includeCiv, false, false);
			data.maxVariants = getVariants(spec, includeCiv, false, true).size();
			data.faction = Global.getSettings().createBaseFaction(spec.getId());
			
			if (!includeCiv) {
				civilian.addAll(getVariants(spec, true, true, false));
			}
			
			if (data.variants == null || data.variants.isEmpty()) continue;
			
			other.removeAll(data.variants);
			
			result.add(data);
		}
		
		other.removeAll(civilian);
		
		Collections.sort(result, new Comparator<SimCategoryData>() {
			public int compare(SimCategoryData o1, SimCategoryData o2) {
				return o1.name.compareTo(o2.name);
			}
		});
		
		SimCategoryData custom = getCustomCategory();
		result.add(0, custom);
		
		SimCategoryData def = new SimCategoryData();
		def.id = DEFAULT_CAT_ID;
		def.name = "Default";
		def.nonFactionCategory = true;
		def.nameColor = Misc.getBasePlayerColor();
		def.iconName = Global.getSettings().getSpriteName("simulator", "defaultPlayerCrest");
		def.data = null;
		def.faction = createCustomFaction();
		def.variants = getVariantIDList(sortVariantList(getVariantList(
				new LinkedHashSet<String>(Global.getSettings().getSimOpponents()))));
		result.add(0, def);
		
		other.removeAll(def.variants);
		
		if (!other.isEmpty()) {
			SimCategoryData otherCat = new SimCategoryData();
			otherCat.id = OTHER_CAT_ID;
			otherCat.name = "Other";
			otherCat.nonFactionCategory = true;
			otherCat.nameColor = Misc.getBasePlayerColor();
			otherCat.iconName = Global.getSettings().getSpriteName("simulator", "otherPlayerCrest");
			otherCat.data = null;
			otherCat.faction = createCustomFaction();
			otherCat.variants = getVariantIDList(sortVariantList(getVariantList(other)));
			otherCat.faction = createCustomFaction();
			if (!otherCat.variants.isEmpty()) {
				result.add(otherCat);
			}
		}
		
		if (!civilian.isEmpty()) {
			FactionSpecAPI neutral = Global.getSettings().getFactionSpec(Factions.NEUTRAL);
			SimCategoryData civ = new SimCategoryData();
			civ.id = CIV_CAT_ID;
			civ.name = "Civilian ships";
			civ.nonFactionCategory = true;
			civ.nameColor = neutral.getBaseUIColor();
			civ.iconName = neutral.getCrest();
			civ.data = null;
			civ.variants = getVariantIDList(sortVariantList(getVariantList(civilian)));
			civ.faction = createCustomFaction();
			if (!civ.variants.isEmpty()) {
				result.add(civ);
			}
		}
		
		if (isShowDevCategories()) {
			SimCategoryData dev = new SimCategoryData();
			dev.id = DEV_CAT_ID;
			dev.name = "DevMode stuff";
			dev.nonFactionCategory = true;
			dev.nameColor = Misc.getBasePlayerColor();
			dev.iconName = Global.getSettings().getSpriteName("simulator", "devModeVariantsIcon");
			dev.data = null;
			dev.faction = createCustomFaction();
			dev.variants = getVariantIDList(sortVariantList(getVariantList(
							new LinkedHashSet<String>(Global.getSettings().getSimOpponentsDev()))));
			result.add(1, dev);
		}
		
		return result;
	}

	public List<String> getVariants(FactionSpecAPI spec, boolean withCiv, boolean onlyCiv, boolean forceFullUnlock) {
		if (spec == null) return new ArrayList<String>();
		
		FactionAPI faction = Global.getSettings().createBaseFaction(spec.getId());
		
		boolean fullUnlock = isSimFullyUnlocked() || forceFullUnlock || isAllStandardStuffUnlocked();
		//fullUnlock = true;
		
		Set<String> seen = new LinkedHashSet<String>();
		List<ShipVariantAPI> variantList = new ArrayList<ShipVariantAPI>();
		for (String roleId : getAllRoles()) {
			Set<String> variants = faction.getVariantsForRole(roleId);
			if (variants == null) continue;
			for (String variantId : variants) {
				if (seen.contains(variantId)) continue;
				seen.add(variantId);
//				if (variantId.equals("onslaught_Standard")) {
//					System.out.println("3f23few");
//				}
				if (!fullUnlock && !unlocksData.variants.contains(variantId) && 
						!defaultOpponents.contains(variantId)) {
					continue;
				}
				
				ShipVariantAPI v = Global.getSettings().getVariant(variantId);
				if (!isAcceptableSimVariant(v, false)) continue;
				if (!v.isStockVariant()) continue;
				
//				if (v == null || !v.isStockVariant()) continue;
//				//if (v.isFighter()) continue;
//				
//				//if (v.getHullSpec().getHints().contains(ShipTypeHints.HIDE_IN_CODEX)) continue;
//				if (v.getHullSpec().hasTag(Tags.NO_SIM) || v.hasTag(Tags.NO_SIM)) continue;
//				if (v.getHullSpec().hasTag(Tags.RESTRICTED)) continue;
				
				boolean civ = v.isCivilian();
				if (civ && !withCiv) continue;
				if (!civ && onlyCiv) continue;
				
				variantList.add(v);
			}
		}
		
		sortVariantList(variantList);
		
		return getVariantIDList(variantList);
	}
	
	
	public static List<ShipVariantAPI> getVariantList(Set<String> variants) {
		List<ShipVariantAPI> variantList = new ArrayList<ShipVariantAPI>();
		for (String id : variants) {
			ShipVariantAPI v = Global.getSettings().getVariant(id);
			if (v == null || !v.isStockVariant()) continue;
			variantList.add(v);
		}
		return variantList;
	}
	public static List<String> getVariantIDList(List<ShipVariantAPI> variantList) {
		List<String> variants = new ArrayList<String>();
		for (ShipVariantAPI v : variantList) {
			variants.add(v.getHullVariantId());
		}
		return variants;
	}
	
	public static List<ShipVariantAPI> sortVariantList(List<ShipVariantAPI> variantList) {
		Collections.sort(variantList, new Comparator<ShipVariantAPI>() {
			public int compare(ShipVariantAPI v1, ShipVariantAPI v2) {
				if (v1.isCivilian() && !v2.isCivilian()) return 1;
				if (v2.isCivilian() && !v1.isCivilian()) return -1;
				
				if (v1.getHullSize().ordinal() < v2.getHullSize().ordinal()) return 1;
				if (v1.getHullSize().ordinal() > v2.getHullSize().ordinal()) return -1;
				
				int diff = (int) (v1.getHullSpec().getSuppliesToRecover() - v2.getHullSpec().getSuppliesToRecover());
				if (diff != 0) return (int) Math.signum(-diff);
				
				return v1.getHullSpec().getHullName().compareTo(v2.getHullSpec().getHullName());
			}
		});
		return variantList;
	}
	
	
	public static List<String> getAllRoles() {
		List<String> result = new ArrayList<String>();
		result.add(ShipRoles.COMBAT_SMALL);
		result.add(ShipRoles.COMBAT_MEDIUM);
		result.add(ShipRoles.COMBAT_LARGE);
		result.add(ShipRoles.COMBAT_CAPITAL);
		result.add(ShipRoles.COMBAT_FREIGHTER_SMALL);
		result.add(ShipRoles.COMBAT_FREIGHTER_MEDIUM);
		result.add(ShipRoles.COMBAT_FREIGHTER_LARGE);
		
		result.add(ShipRoles.CIV_RANDOM);
		
		result.add(ShipRoles.PHASE_SMALL);
		result.add(ShipRoles.PHASE_MEDIUM);
		result.add(ShipRoles.PHASE_LARGE);
			
		result.add(ShipRoles.PHASE_CAPITAL);
			
		result.add(ShipRoles.CARRIER_SMALL);
		result.add(ShipRoles.CARRIER_MEDIUM);
		result.add(ShipRoles.CARRIER_LARGE);
		result.add(ShipRoles.FREIGHTER_SMALL);
		result.add(ShipRoles.FREIGHTER_MEDIUM);
		result.add(ShipRoles.FREIGHTER_LARGE);
		result.add(ShipRoles.TANKER_SMALL);
		result.add(ShipRoles.TANKER_MEDIUM);
		result.add(ShipRoles.TANKER_LARGE);
		result.add(ShipRoles.PERSONNEL_SMALL);
		result.add(ShipRoles.PERSONNEL_MEDIUM);
		result.add(ShipRoles.PERSONNEL_LARGE);
		result.add(ShipRoles.LINER_SMALL);
		result.add(ShipRoles.LINER_MEDIUM);
		result.add(ShipRoles.LINER_LARGE);
		result.add(ShipRoles.TUG);
		result.add(ShipRoles.CRIG);
		result.add(ShipRoles.UTILITY);
		
		return result;
	}



	public void applySettingsToFleetMembers(List<FleetMemberAPI> members,
						SimCategoryData category, Map<String, String> settings) {
	
		boolean custom = CUSTOM_CAT_ID.equals(category.id);
		boolean defaultCat = DEFAULT_CAT_ID.equals(category.id);
		boolean civ = CIV_CAT_ID.equals(category.id);
		boolean dev = DEV_CAT_ID.equals(category.id);
		boolean customFaction = custom || civ || dev || defaultCat; 
		
		FactionAPI faction = category.faction;
		
		String officers = settings.get(OFFICERS_ID);
		if (officers == null) officers = settings.get(OFFICERS_CUSTOM_ID);

		if (officers != null) {
			if (!officers.equals(OFFICERS_NONE) && !officers.equals(OFFICERS_CUSTOM_NONE)) {
				CampaignFleetAPI fleetNonAuto = Global.getFactory().createEmptyFleet(faction, true);
				for (FleetMemberAPI member : members) {
					if (Misc.isAutomated(member)) continue;
					fleetNonAuto.getFleetData().addFleetMember(member);
				}
				FleetParamsV3 params = new FleetParamsV3();
				boolean all = false;
				if (officers.equals(OFFICERS_ALL)) {
					all = true;
					params.officerNumberBonus = 1000;
					params.officerLevelBonus = 10;
				} else if (officers.equals(OFFICERS_CUSTOM_5)) {
					all = true;
					params.officerNumberBonus = 1000;
					params.officerLevelBonus = 10;
					params.officerLevelLimit = 5;
				} else if (officers.equals(OFFICERS_CUSTOM_6)) {
					all = true;
					params.officerNumberBonus = 1000;
					params.officerLevelBonus = 10;
					params.commander = Global.getFactory().createPerson();
					params.commander.getStats().setSkillLevel(Skills.OFFICER_TRAINING, 1);
					params.officerLevelLimit = 6;
				}
				FleetFactoryV3.addCommanderAndOfficersV2(fleetNonAuto, params, new Random(), true, all);
			}
		}
		
		
		String cores = settings.get(AI_CORES_ID);
		boolean derelict = false;
		if (cores == null) {
			cores = settings.get(AI_CORES_DERELICT_ID);
			if (cores != null) derelict = true;
		}
		if (cores == null) cores = settings.get(AI_CORES_OMEGA_ID);
		if (cores == null) cores = settings.get(AI_CORES_DEV_ID);
		
		if (cores != null) {
			if (!cores.equals(AI_CORES_NONE)) {
				CampaignFleetAPI fleetAuto = Global.getFactory().createEmptyFleet(faction, true);
				for (FleetMemberAPI member : members) {
					if (!Misc.isAutomated(member)) continue;
					fleetAuto.getFleetData().addFleetMember(member);
				}
				FleetParamsV3 params = new FleetParamsV3();
				params.doNotIntegrateAICores = true;
				boolean all = false;
				boolean omega = false;
				if (cores.equals(AI_CORES_SOME)) {
					if (derelict) {
						params.aiCores = OfficerQuality.AI_GAMMA;
					} else {
						params.aiCores = OfficerQuality.AI_MIXED;
					}
				} else if (cores.equals(AI_CORES_GAMMA)) {
					params.officerNumberBonus = 1000;
					params.aiCores = OfficerQuality.AI_GAMMA;
					all = true;
				} else if (cores.equals(AI_CORES_BETA)) {
					params.officerNumberBonus = 1000;
					params.aiCores = OfficerQuality.AI_BETA;
					all = true;
				} else if (cores.equals(AI_CORES_ALPHA)) {
					params.officerNumberBonus = 1000;
					params.aiCores = OfficerQuality.AI_ALPHA;
					all = true;
				} else if (cores.equals(AI_CORES_OMEGA)) {
					params.officerNumberBonus = 1000;
					params.aiCores = OfficerQuality.AI_OMEGA;
					all = true;
					omega = true;
				}
				
				// pass in derelictMode = false regardless of derelict or not, since if it's true
				// RemnantOfficerGeneratorPlugin does some stuff potentially undesired here
				// Setting it to AI_GAMMA above does the job, anyway
				RemnantOfficerGeneratorPlugin genPlugin = new RemnantOfficerGeneratorPlugin(false, 1f);
				if (settings.containsKey(INTEGRATE_CORES_ID) && settings.get(INTEGRATE_CORES_ID).toLowerCase().equals("true")) {
					if (!omega) {
						genPlugin.setForceIntegrateCores(true);
					}
				}
				genPlugin.setPutCoresOnCivShips(all);
				genPlugin.setForceNoCommander(true);
				genPlugin.addCommanderAndOfficers(fleetAuto, params, new Random());
				
				pruneAICoresToAvailable(members);
			}
		}
		
		String personality = null;
		if (settings.containsKey(AGGRO_ID) || settings.containsKey(AGGRO_ID_CORES_ONLY)) {
			String aggro = (String) settings.get(AGGRO_ID);
			if (aggro == null) aggro = (String) settings.get(AGGRO_ID_CORES_ONLY);
			if (aggro.equals(AGGRO_CAUTIOUS)) {
				personality = Personalities.CAUTIOUS;
			} else if (aggro.equals(AGGRO_STEADY)) {
				personality = Personalities.STEADY;
			} else if (aggro.equals(AGGRO_AGGRESSIVE)) {
				personality = Personalities.AGGRESSIVE;
			} else if (aggro.equals(AGGRO_RECKLESS)) {
				personality = Personalities.RECKLESS;
			} 
		}
		
		if (personality != null) {
			for (FleetMemberAPI member : members) {
				if (Misc.isAutomated(member)) continue;
				
				PersonAPI captain = member.getCaptain();
				captain.setPersonality(personality);
				member.setPersonalityOverride(personality);
			}
		}
		
		
		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(faction, true);
		for (FleetMemberAPI member : members) {
			fleet.getFleetData().addFleetMember(member);
		}

		if (settings.containsKey(RANDOMIZE_VARIANTS_ID) && settings.get(RANDOMIZE_VARIANTS_ID).toLowerCase().equals("true")) {
			DefaultFleetInflaterParams params = new DefaultFleetInflaterParams();
			params.quality = 2f; // don't add d-mods

			DefaultFleetInflater inflater = new DefaultFleetInflater(params);
			inflater.inflate(fleet);
		}
		
		
		if (settings.containsKey(QUALITY_ID)) {
			for (FleetMemberAPI member : members) {
				if (member.getVariant().isStockVariant()) {
					ShipVariantAPI copy = member.getVariant().clone();
					copy.setSource(VariantSource.REFIT);
					member.setVariant(copy, false, false);
				}
			}
			
			String quality = (String) settings.get(QUALITY_ID);
			if (quality.equals(QUALITY_NO_DMODS)) {
				// nothing to do
			} else if (quality.equals(QUALITY_MAX_DMODS)) {
				for (FleetMemberAPI member : members) {
					DModManager.addDMods(member, true, 5, null);
				}
			} else if (quality.equals(QUALITY_SOME_DMODS)) {
				Random random = new Random();
				for (FleetMemberAPI member : members) {
					int num = 2 + random.nextInt(3);
					DModManager.addDMods(member, true, num, null);
				}
			} else if (quality.equals(QUALITY_SOME_SDMODS)) {
				Random random = new Random();
				CoreAutofitPlugin plugin = new CoreAutofitPlugin(null);
				for (FleetMemberAPI member : members) {
					int num = 1 + random.nextInt(2);
					plugin.addSMods(member, num, this);
				}
			} else if (quality.equals(QUALITY_MANY_SMODS)) {
				Random random = new Random();
				CoreAutofitPlugin plugin = new CoreAutofitPlugin(null);
				for (FleetMemberAPI member : members) {
					int num = 2 + random.nextInt(2);
					plugin.addSMods(member, num, this);
				}
			}
		}
		
//		for (FleetMemberAPI member : members) {
//			member.setFlagship(false, false);
//		}
//		fleet.getFleetData().setFlagship(null);
		
		// not strictly needed anymore after changing FleetFactoryV3.addCommanderAndOfficersV2()
		// to make a "fake" commander
		// but just in case, to make sure fleetwide skills don't apply
		//makeFleetCommanderNormalOfficer(members);		
		fleet.setCommander(Global.getFactory().createPerson());
	}

	@Override
	public void applySettingsToDeployed(List<DeployedFleetMemberAPI> deployed, Map<String, String> settings) {
		if (settings.containsKey(AGGRO_ID) || settings.containsKey(AGGRO_ID_CORES_ONLY)) {
			String aggro = (String) settings.get(AGGRO_ID);
			if (aggro == null) aggro = (String) settings.get(AGGRO_ID_CORES_ONLY);
			final String aggro2 = aggro;
			if (aggro.equals(AGGRO_DO_NOTHING)) {
				for (DeployedFleetMemberAPI member : deployed) {
					final ShipAPI ship = member.getShip();
					if (ship == null) continue;
					
					if (ship.getOwner() == 0) continue;
					
					ship.setShipAI(null);
					ship.setHoldFire(true);
					ship.getLocation().y -= 2000f;
					Global.getCombatEngine().addPlugin(new BaseEveryFrameCombatPlugin() {
						protected float elapsed = 0f;
						@Override
						public void advance(float amount, List<InputEventAPI> events) {
							elapsed += amount;
							if (ship.getTravelDrive() != null) {
								ship.getTravelDrive().deactivate();
							}
							if (elapsed > 0.1f) {
								ship.getVelocity().set(0, 0);
								elapsed = -10000000f;
							}
							
							ship.giveCommand(ShipCommand.DECELERATE, null, 0);
							
							if (ship.isHulk()) {
								Global.getCombatEngine().removePlugin(this);
							}
						}
						
					});
				}
			} else if (aggro.equals(AGGRO_DEFENSES) || aggro.equals(AGGRO_STATIONARY)) {
				final boolean defensesOnly = aggro.equals(AGGRO_DEFENSES);
				for (DeployedFleetMemberAPI member : deployed) {
					final ShipAPI ship = member.getShip();
					if (ship == null) continue;
					
					if (ship.getOwner() == 0) continue;
					
					ship.getLocation().y -= 2000f;
					Global.getCombatEngine().addPlugin(new BaseEveryFrameCombatPlugin() {
						protected float elapsed = 0f;
						@Override
						public void advance(float amount, List<InputEventAPI> events) {
							elapsed += amount;
							if (ship.getTravelDrive() != null && ship.getTravelDrive().isActive()) {
								ship.getTravelDrive().deactivate();
							}
							if (elapsed > 0.1f) {
								ship.getVelocity().set(0, 0);
								elapsed = -10000000f;
							}
							
							List<ShipAPI> all = new ArrayList<>(ship.getChildModulesCopy());
							all.add(ship);
							
							for (ShipAPI curr : all) {
								if (aggro2.equals(AGGRO_DEFENSES)) {
									curr.setHoldFire(true);
								}
								curr.giveCommand(ShipCommand.DECELERATE, null, 0);
								
								curr.blockCommandForOneFrame(ShipCommand.ACCELERATE);
								curr.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
								curr.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT);
								curr.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT);
								
								if (defensesOnly) {
									curr.blockCommandForOneFrame(ShipCommand.FIRE);
									curr.blockCommandForOneFrame(ShipCommand.VENT_FLUX);
									curr.blockCommandForOneFrame(ShipCommand.PULL_BACK_FIGHTERS);
									curr.blockCommandForOneFrame(ShipCommand.TOGGLE_AUTOFIRE);
									curr.blockCommandForOneFrame(ShipCommand.HOLD_FIRE);
									
									if (curr.getSystem() != null && curr.getSystem().getSpecAPI() != null) {
										ShipSystemSpecAPI spec = curr.getSystem().getSpecAPI();
										if (!spec.hasTag(Tags.SHIP_SYSTEM_DEFENSIVE) ||
												spec.hasTag(Tags.SHIP_SYSTEM_OFFENSIVE) || 
												spec.hasTag(Tags.SHIP_SYSTEM_MOVEMENT)) {
											curr.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
										}
									}
								} else {
									if (curr.getSystem() != null && curr.getSystem().getSpecAPI() != null) {
										ShipSystemSpecAPI spec = curr.getSystem().getSpecAPI();
										if (spec.hasTag(Tags.SHIP_SYSTEM_MOVEMENT)) {
											curr.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
										}
									}
								}
							}
							
							if (ship.isHulk()) {
								Global.getCombatEngine().removePlugin(this);
							}
						}
						
					});
				}
			} 
		}
		
	}
	
	
	public static FactionAPI createCustomFaction() {
		FactionAPI faction = Global.getSettings().createBaseFaction(Factions.INDEPENDENT);
		

		FactionDoctrineAPI d = faction.getDoctrine();
		d.setAggression(2);
		d.setCarriers(2);
		d.setPhaseShips(1);
		d.setWarships(4);
		
		d.setAutofitRandomizeProbability(0.25f);
		d.setNumShips(3);
		d.setOfficerQuality(3);
		d.setShipSize(3);
		
		FactionAPI merc = Global.getSettings().createBaseFaction(Factions.MERCENARY);
		
		for (String id : merc.getKnownWeapons()) {
			faction.addKnownWeapon(id, false);
		}
		for (String id : merc.getKnownFighters()) {
			faction.addKnownFighter(id, false);
		}
		for (String id : merc.getKnownHullMods()) {
			faction.addKnownHullMod(id);
		}
		
		faction.getPriorityWeapons().clear();
		faction.getPriorityFighters().clear();
		
		faction.getHullFrequency().clear();
		
		return faction;
	}
	
	
	public static void makeFleetCommanderNormalOfficer(List<FleetMemberAPI> members) {
		int maxLevel = (int) Global.getSettings().getFloat("officerMaxLevel");
		for (FleetMemberAPI member : members) {
			PersonAPI captain = member.getCaptain();
			if (!member.isFlagship() && !captain.isDefault()) {
				maxLevel = Math.max(maxLevel, captain.getStats().getLevel());
			}
		}
		for (FleetMemberAPI member : members) {
			if (member.isFlagship()) {
				PersonAPI captain = member.getCaptain();
				member.setFlagship(false, false);
				captain.getStats().setLevel(Math.min(captain.getStats().getLevel(), maxLevel));
				for (String skillId : Global.getSettings().getSkillIds()) {
					SkillSpecAPI skill = Global.getSettings().getSkillSpec(skillId);
					if (skill.isAdmiralSkill()) {
						captain.getStats().setSkillLevel(skillId, 0);
					}
				}
				break;
			}
		}
	}
	
	
	public List<String> generateSelection(SimCategoryData category, int deploymentPoints) {
		List<String> result = new ArrayList<String>();
		if (category.variants.isEmpty()) return result;
		
		FactionAPI faction = Global.getSettings().createBaseFaction(category.faction.getId());
		
		category.faction.getDoctrine().copyToDoctrine(faction.getDoctrine());
		
		//faction.getDoctrine().setShipSize(5);
		//faction.getDoctrine().setCarriers(10);
		
		faction.clearShipRoleCache();
		faction.getRestrictToVariants().clear();
		faction.getKnownShips().clear();
		
		for (String variantId : category.variants) {
			ShipVariantAPI v = Global.getSettings().getVariant(variantId);
			if (v == null || !v.isStockVariant()) continue;
			faction.addKnownShip(v.getHullSpec().getHullId(), false);
			faction.getRestrictToVariants().add(variantId);
		}
		
		FleetParamsV3 params = new FleetParamsV3(
				null,
				Factions.INDEPENDENT,
				2f, // quality - null to determine from producer/source markets and doctrine
				FleetTypes.PATROL_LARGE,
				deploymentPoints * 1f, // combatPts
				0f, // freighterPts 
				0f * 0.5f, // tankerPts
				0f * 0.5f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				1f // qualityMod
				);
		params.maxNumShips = 100;
		params.factionOverride = faction;
		params.ignoreMarketFleetSizeMult = true;
		params.withOfficers = false;
		params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
		
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
//		CampaignFleetAPI fleet2 = FleetFactoryV3.createFleet(params);
//		for (FleetMemberAPI member : fleet2.getFleetData().getMembersListCopy()) {
//			fleet.getFleetData().addFleetMember(member);
//		}
		pruneFleetDownToDP(fleet, deploymentPoints, new Random());
		
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			ShipVariantAPI v = member.getVariant();
			if (v != null && v.isStockVariant()) { // && v.getHullSize() == HullSize.FRIGATE) {
				result.add(v.getHullVariantId());
			}
		}
		
//		result.add("onslaught_Standard");
//		result.add("onslaught_Standard");
		
		return result;
	}
	
	
	
	

	public void fitFighterInSlot(int index, AvailableFighter fighter, ShipVariantAPI variant) {}
	public void clearFighterSlot(int index, ShipVariantAPI variant) {}
	public void fitWeaponInSlot(WeaponSlotAPI slot, AvailableWeapon weapon, ShipVariantAPI variant) {}
	public void clearWeaponSlot(WeaponSlotAPI slot, ShipVariantAPI variant) {}
	public List<AvailableWeapon> getAvailableWeapons() {return new ArrayList<AvailableWeapon>();}
	public List<AvailableFighter> getAvailableFighters() {return new ArrayList<AvailableFighter>();}
	public boolean isPriority(WeaponSpecAPI weapon) {return false;}
	public boolean isPriority(FighterWingSpecAPI wing) {return false;}
	public void syncUIWithVariant(ShipVariantAPI variant) {}
	public ShipAPI getShip() {return null;}
	public FactionAPI getFaction() {return null;}
	public boolean isAllowSlightRandomization() {return false;}
	public boolean isPlayerCampaignRefit() {return false;}
	public boolean canAddRemoveHullmodInPlayerCampaignRefit(String modId) {return true;}

	public List<String> getAvailableHullmods() {
		List<String> ids = new ArrayList<String>();
		for (HullModSpecAPI mod : Global.getSettings().getAllHullModSpecs()) {
			ids.add(mod.getId());
		}
		return ids;
	}

	
	public static void pruneFleetDownToDP(CampaignFleetAPI fleet, float targetDP, Random random) {
		float currDP = getDP(fleet);
		if (currDP > targetDP) {
			fleet.getFleetData().sort();
			
			float fpRem = currDP - targetDP;
			
			while (fpRem > 0) {
				List<FleetMemberAPI> copy = fleet.getFleetData().getMembersListCopy();
				CountingMap<HullSize> counts = new CountingMap<HullSize>();
				for (FleetMemberAPI curr : copy) {
					counts.add(curr.getHullSpec().getHullSize());
				}
				WeightedRandomPicker<FleetMemberAPI> picker =  new WeightedRandomPicker<FleetMemberAPI>(random);
				for (FleetMemberAPI curr : copy) {
					float dp = curr.getDeploymentPointsCost();
					if (dp <= fpRem) {
						int count = counts.getCount(curr.getHullSpec().getHullSize());
						float mult = 1f;
						if (count <= 1) {
							mult = 0.0001f;
						} else {
							mult = count;
						}
						picker.add(curr, dp * mult);
					}
				}
				FleetMemberAPI pick = picker.pick();
				if (pick == null) break;
				
				float dp = pick.getDeploymentPointsCost();
				fpRem -= dp;
				fleet.getFleetData().removeFleetMember(pick);
			}
		}
	}
	
	public static float getDP(CampaignFleetAPI fleet) {
		float total = 0f;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			total += member.getDeploymentPointsCost();
		}
		return total;
	}

	public void reportPlayerBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (!loadedStuff) {
			loadCustomOpponents();
			loadUnlocksData();
			loadedStuff = true;
		}
		
		//CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
//		unlocksData.factions.clear();
//		unlocksData.variants.clear();
		//unlocksData.variants.addAll(Global.getSettings().getSimOpponents());
		
		LinkedHashSet<String> addedFactions = new LinkedHashSet<String>(); 
		LinkedHashSet<String> addedVariants = new LinkedHashSet<String>(); 
		
		for (CampaignFleetAPI fleet : battle.getNonPlayerSideSnapshot()) {
			if (fleet.getFaction() == null ||
					fleet.getFaction().getFactionSpec() == null ||
					fleet.getFaction().getFactionSpec().getCustom() == null) continue;
			JSONObject json = fleet.getFaction().getFactionSpec().getCustom().optJSONObject("simulatorData");
			if (json == null) continue;
			boolean show = json.optBoolean("showInSimulator");
			if (!show) continue;
			
			List<FleetMemberAPI> members = Misc.getSnapshotMembersLost(fleet);
			String fid = findBestMatchingFaction(fleet.getFaction().getId(), members);
			
			if (!unlocksData.factions.contains(fid)) {
				unlocksData.factions.add(fid);
				addedFactions.add(fid);
			}

			for (FleetMemberAPI member : members) {
//				if ("LGS Dorus".equals(member.getShipName())) {
//					System.out.println("ewfwefewfwefe");
//				}
				String vid = getStockVariantId(member);
				
//				if (vid != null && vid.contains("executor")) {
//					System.out.println("fwfewfe");
//				}
				if (vid != null) {
					if (!unlocksData.variants.contains(vid)) {
						unlocksData.variants.add(vid);
						addedVariants.add(vid);
					}		
				}
			}
		}
		
		if (!addedVariants.isEmpty()) {
			addedVariants = new LinkedHashSet<String>(getVariantIDList(sortVariantList(getVariantList(
					new LinkedHashSet<String>(addedVariants)))));
		}
		
		if (!addedFactions.isEmpty() || !addedVariants.isEmpty()) {
			saveUnlocksData();
			
			new SimUpdateIntel(addedFactions, addedVariants);
		}
	}
	
	public boolean isAcceptableSimVariant(ShipVariantAPI v, boolean forLearning) {
		boolean allowAll = isSimFullyUnlocked() && !forLearning;
		if (v == null) return false;
		if ((v.getHullSpec().hasTag(Tags.NO_SIM) || v.hasTag(Tags.NO_SIM)) && !allowAll) return false;
		if (v.getHullSpec().hasTag(Tags.RESTRICTED) && !allowAll) return false;
		if (v.getHullSpec().getHints().contains(ShipTypeHints.STATION)) return false;
		return true;
	}
	
	public String getStockVariantId(FleetMemberAPI member) {
		ShipVariantAPI v = member.getVariant();
		if (!isAcceptableSimVariant(v, true)) return null;
		
		String vid = null;
		if (v.isStockVariant()) {
			vid = v.getHullVariantId();
		}
		if (vid == null && v.getOriginalVariant() != null) {
			vid = v.getOriginalVariant();
		}
		return vid;
	}
	
	public String findBestMatchingFaction(String fleetFactionId, List<FleetMemberAPI> members) {
		
		List<String> roles = getAllRoles();
		
		FactionAPI best = null;
		float bestScore = 0f;
		
		for (FactionSpecAPI spec : Global.getSettings().getAllFactionSpecs()) {
			if (spec == null || spec.getCustom() == null) continue;
			
			JSONObject json = spec.getCustom().optJSONObject("simulatorData");
			if (json == null) continue;
			
			boolean show = json.optBoolean("showInSimulator");
			if (!show) continue;
			
			FactionAPI faction = Global.getSector().getFaction(spec.getId());
			
			Set<String> allVariants = new LinkedHashSet<String>();
			for (String roleId : roles) {
				allVariants.addAll(faction.getVariantsForRole(roleId));
			}
			
			float matches = 0f;
			float total = 0f;
			for (FleetMemberAPI member : members) {
				String vid = getStockVariantId(member);
				if (vid == null) continue;
				
				if (allVariants.contains(vid)) {
					matches++;
				}
				total++;
			}
			total = Math.max(total, 1f);
			float score = matches / total;
			if (faction.getId().equals(fleetFactionId)) {
				score *= 1.1f;
			}
			if (score > bestScore) {
				bestScore = score;
				best = faction;
			}
		}
		
		if (best == null || bestScore <= 0) return fleetFactionId;
		return best.getId();
	}
	
	public void pruneAICoresToAvailable(List<FleetMemberAPI> members) {
		if (!isRequireAICoresInCargo()) return;
		
		CountingMap<String> availableCores = getAvailableMinusDeployedAICores();
		
		CountingMap<String> current = new CountingMap<String>();
		for (FleetMemberAPI member : members) {
			String coreId = getCoreId(member);
			if (coreId == null) continue;
			
			current.add(coreId, 1);
		}
		
		CountingMap<String> remove = new CountingMap<String>();
		remove.putAll(current);
		for (String id : availableCores.keySet()) {
			remove.sub(id, availableCores.getCount(id));
		}
		
		if (remove.isEmpty()) return;
	
		WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<FleetMemberAPI>();
		for (FleetMemberAPI member : members) {
			String coreId = getCoreId(member);
			if (coreId == null) continue;
			
			if (remove.getCount(coreId) > 0) {
				picker.add(member, member.getDeploymentPointsCost());
			}
		}
		
		while (!remove.isEmpty() && !picker.isEmpty()) {
			FleetMemberAPI member = picker.pickAndRemove();
			
			String coreId = getCoreId(member);
			if (remove.getCount(coreId) > 0) {
				remove.sub(coreId, 1);
				member.setCaptain(Global.getFactory().createPerson());
			}
		}
	}
	
	public String getCoreId(FleetMemberAPI member) {
		if (member == null) return null;
		PersonAPI captain = member.getCaptain();
		if (captain == null || captain.isDefault() || !captain.isAICore()) return null;;
		return captain.getAICoreId();
	}

	public CountingMap<String> getAvailableMinusDeployedAICores() {
		CountingMap<String> cargoCores = getAvailableAICores();
		CountingMap<String> deployedCores = getDeployedAICores();
		CountingMap<String> availableCores = new CountingMap<String>();
		
		availableCores.putAll(cargoCores);
		for (String id : deployedCores.keySet()) {
			availableCores.sub(id, deployedCores.getCount(id));
		}
		return availableCores;
	}
	
	public CountingMap<String> getDeployedAICores() {
		// only checking enemy side: in campaign, these settings don't affect own side
		CountingMap<String> map = new CountingMap<String>();
		for (DeployedFleetMemberAPI member : Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getDeployedCopyDFM()) {
			if (member.getShip() == null) continue;
			PersonAPI captain = member.getShip().getCaptain();
			if (captain == null || captain.isDefault() || !captain.isAICore()) continue;
			String coreId = captain.getAICoreId();
			if (coreId == null) continue;
			
			map.add(coreId, 1);
		}
		return map;
	}
	
	public CountingMap<String> getAvailableAICores() {
		CountingMap<String> map = new CountingMap<String>();
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		
		CargoAPI cargo = player.getCargo(); 
		
		map.add(Commodities.ALPHA_CORE, (int)Math.round(cargo.getCommodityQuantity(Commodities.ALPHA_CORE)));
		map.add(Commodities.BETA_CORE, (int)Math.round(cargo.getCommodityQuantity(Commodities.BETA_CORE)));
		map.add(Commodities.GAMMA_CORE, (int)Math.round(cargo.getCommodityQuantity(Commodities.GAMMA_CORE)));
		
		
		cargo = Misc.getStorageCargo(Global.getSector().getCurrentlyOpenMarket());
		if (cargo != null) {
			map.add(Commodities.ALPHA_CORE, (int)Math.round(cargo.getCommodityQuantity(Commodities.ALPHA_CORE)));
			map.add(Commodities.BETA_CORE, (int)Math.round(cargo.getCommodityQuantity(Commodities.BETA_CORE)));
			map.add(Commodities.GAMMA_CORE, (int)Math.round(cargo.getCommodityQuantity(Commodities.GAMMA_CORE)));
		}
		
		return map;
	}
	
	public void appendToTooltip(TooltipMakerAPI info, float initPad, float width, AdvancedSimOption option, Object extra) {
		if (isRequireAICoresInCargo()) {
			if (option.getId().equals(AI_CORES_ID) && extra != null) {
				if (extra.equals(AI_CORES_SOME) ||
						extra.equals(AI_CORES_GAMMA) ||
						extra.equals(AI_CORES_BETA) ||
						extra.equals(AI_CORES_ALPHA)) {
					CountingMap<String> cores = getAvailableAICores();
					CargoAPI cargo = Global.getFactory().createCargo(true);
					for (String id : cores.keySet()) {
						cargo.addCommodity(id, cores.getCount(id));
					}
					
					float opad = 10f;
					info.addSectionHeading("AI cores in cargo & storage", Alignment.MID, initPad);
					if (cargo.isEmpty()) {
						info.addPara(BaseIntelPlugin.INDENT + "None", opad);
					} else {
						info.showCargo(cargo, 10, true, opad);
					}
					
					cores = getDeployedAICores();
					cargo = Global.getFactory().createCargo(true);
					for (String id : cores.keySet()) {
						cargo.addCommodity(id, cores.getCount(id));
					}
					
					info.addSectionHeading("Cores already in use in simulation", Alignment.MID, initPad);
					if (cargo.isEmpty()) {
						info.addPara(BaseIntelPlugin.INDENT + "None", opad);
					} else {
						info.showCargo(cargo, 10, true, opad);
					}
					
					
					cores = getAvailableMinusDeployedAICores();
					cargo = Global.getFactory().createCargo(true);
					for (String id : cores.keySet()) {
						cargo.addCommodity(id, cores.getCount(id));
					}
					
					info.addSectionHeading("Cores available to deploy", Alignment.MID, initPad);
					if (cargo.isEmpty()) {
						info.addPara(BaseIntelPlugin.INDENT + "None", opad);
					} else {
						info.showCargo(cargo, 10, true, opad);
					}
				}
			}
		}
	}

	@Override
	public MarketAPI getMarket() {
		return null;
	}

	@Override
	public FleetMemberAPI getFleetMember() {
		return null;
	}

}















