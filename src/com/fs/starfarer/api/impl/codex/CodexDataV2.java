package com.fs.starfarer.api.impl.codex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import java.awt.Color;

import org.json.JSONException;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModPlugin;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.impl.items.MultiBlueprintItemPlugin;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.ShipSystemSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.SharedUnlockData;
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.econ.impl.InstallableItemEffect;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Planets;
import com.fs.starfarer.api.impl.campaign.ids.ShipSystems;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.threat.EnergyLashActivatedSystem;
import com.fs.starfarer.api.loading.AbilitySpecAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.loading.WingRole;
import com.fs.starfarer.api.loading.WithSourceMod;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TagDisplayAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.ListMap;
import com.fs.starfarer.api.util.Misc;

public class CodexDataV2 {

	public static boolean WITH_GAME_MECHANICS_CAT = false;
	public static boolean WITH_CUSTOM_EXAMPLE_CAT = false;
	
	public static boolean USE_KEY_NAMES_FOR_GALLERY = false;
	
	public static CodexEntryPlugin ROOT;
	public static Map<String, CodexEntryPlugin> ENTRIES = new LinkedHashMap<>();
	public static Map<String, CodexEntryPlugin> SEEN_STATION_MODULES = new LinkedHashMap<>(); 
	
	public static String TAG_EMPTY_MODULE = "empty_module";
	
	public static String CAT_ROOT = "root";
	public static String CAT_SHIPS = "ships";
	public static String CAT_STATIONS = "stations";
	public static String CAT_FIGHTERS = "fighters";
	public static String CAT_WEAPONS = "weapons";
	public static String CAT_HULLMODS = "hullmods";
	public static String CAT_SHIP_SYSTEMS = "ship_systems";
	public static String CAT_SPECIAL_ITEMS = "special_items";
	public static String CAT_INDUSTRIES = "industries";
	public static String CAT_STARS_AND_PLANETS = "stars_and_planets";
	public static String CAT_PLANETARY_CONDITIONS = "conditions";
	public static String CAT_COMMODITIES = "commodities";
	public static String CAT_GALLERY = "gallery";
	public static String CAT_SKILLS = "skills";
	public static String CAT_ABILITIES = "abilities";
	//public static String CAT_TERRAIN = "terrain";
	//public static String CAT_FACTIONS = "factions";
	public static String CAT_GAME_MECHANICS = "game_mechanics";
	public static String CAT_CUSTOM_EXAMPLE = "custom_example";
	
	public static String UNKNOWN_ENTRY_ID = "unknown_entry";
	
	
	public static Map<String, Float> CAT_SORT_RELATED_ENTRIES = new HashMap<>();
	static {
		//CAT_SORT_RELATED_ENTRIES.put(CAT_FACTIONS, -10f);
		//CAT_SORT_RELATED_ENTRIES.put(CAT_FACTIONS, 500f);
		CAT_SORT_RELATED_ENTRIES.put(CAT_SHIP_SYSTEMS, 0f);
		CAT_SORT_RELATED_ENTRIES.put(CAT_HULLMODS, 10f);
		CAT_SORT_RELATED_ENTRIES.put(CAT_WEAPONS, 20f);
		CAT_SORT_RELATED_ENTRIES.put(CAT_FIGHTERS, 30f);
		CAT_SORT_RELATED_ENTRIES.put(CAT_SHIPS, 40f);
		CAT_SORT_RELATED_ENTRIES.put(CAT_STATIONS, 45f);
		
		CAT_SORT_RELATED_ENTRIES.put(CAT_SKILLS, 50f);
		CAT_SORT_RELATED_ENTRIES.put(CAT_ABILITIES, 60f);
		
		// TODO: rearrange these as needed depending on where they show up as related
		CAT_SORT_RELATED_ENTRIES.put(CAT_INDUSTRIES, 100f);
		CAT_SORT_RELATED_ENTRIES.put(CAT_SPECIAL_ITEMS, 110f);
		CAT_SORT_RELATED_ENTRIES.put(CAT_STARS_AND_PLANETS, 120f);
		CAT_SORT_RELATED_ENTRIES.put(CAT_COMMODITIES, 130f);
		CAT_SORT_RELATED_ENTRIES.put(CAT_PLANETARY_CONDITIONS, 140f);
		//CAT_SORT_RELATED_ENTRIES.put(CAT_TERRAIN, 145f);
		CAT_SORT_RELATED_ENTRIES.put(CAT_GAME_MECHANICS, 150f);
		CAT_SORT_RELATED_ENTRIES.put(CAT_GALLERY, 160f);
		
	}
	
	
	public static String ALL_TECHS = "All designs";
	public static String ALL_APTITUDES = "All aptitudes";
	public static String ALL_SIZES = "All sizes";
	public static String ALL_TYPES = "All types";
	public static String ALL_DAMAGE_TYPES = "All damage";
	public static String HIGH_EXPLOSIVE = "High explosive";
	public static String KINETIC = "Kinetic";
	public static String DAM_ENERGY = "_Energy"; // leadiner _ gets removed from tag name because HACKS
	public static String FRAGMENTATION = "Fragmentation";
	//public static String OTHER = "Other";
	
	public static String FRIGATES = "Frigates";
	public static String DESTROYERS = "Destroyers";
	public static String CRUISERS = "Cruisers";
	public static String CAPITALS = "Capitals";
	
	public static String COMBAT_SHIPS = "Warships";
	public static String PHASE_SHIPS = "Phase ships";
	public static String CARRIERS = "Carriers";
	public static String CIVILIAN = "Civilian";
	
	public static String SMALL = "Small";
	public static String MEDIUM = "Medium";
	public static String LARGE = "Large";
	public static String FIGHTER_WEAPON = "Fighter";
	
	public static String BALLISTIC = "Ballistic";
	public static String MISSILE = "Missile";
	public static String ENERGY = "Energy";
	public static String HYBRID = "Hybrid";
	public static String COMPOSITE = "Composite";
	public static String SYNERGY = "Synergy";
	public static String UNIVERSAL = "Universal";
	public static String BEAM = "Beam";
	
	public static String FIGHTER = "Fighters";
	public static String BOMBER = "Bombers";
	public static String INTERCEPTOR = "Interceptors";
	public static String OTHER = "Other";
	
	public static String DMODS = "D-mods";
	public static String INTRINSIC = "Intrinsic";
	
	public static String PLANETS = "Planets";
	public static String STARS = "Stars";
	public static String GAS_GIANTS = "Gas giants";
	public static String HABITABLE = "Habitable";
	
	public static String COLONY = "Colony items";
	public static String AI_CORE = "AI cores";
	public static String BLUEPRINTS  = "Blueprint packages";
	
	public static String INDUSTRIES = "Industries";
	public static String STRUCTURES = "Structures";
	public static String STATIONS = "Stations";
	
	public static String RESOURCES = "Resources";
	
	public static String PILOTED_SHIP = "Piloted ship";
	
	
	public static void init() {
		ROOT = new CodexEntryV2("root", "Codex categories", null);
		ROOT.setRetainOrderOfChildren(true);
		
		for (ModPlugin plugin : Global.getSettings().getModManager().getEnabledModPlugins()) {
			plugin.onAboutToStartGeneratingCodex();
		}
		
		CodexEntryPlugin ships = createShipsCategory();
		CodexEntryPlugin stations = createStationsCategory();
		CodexEntryPlugin fighters = createFightersCategory();
		CodexEntryPlugin weapons = createWeaponsCategory();
		CodexEntryPlugin hullmods = createHullModsCategory();
		CodexEntryPlugin shipSystems = createShipSystemsCategory();
		
		CodexEntryPlugin items = createSpecialItemsCategory();
		CodexEntryPlugin industries = createIndustriesCategory();
		CodexEntryPlugin planets = createStarsAndPlanetsCategory();
		CodexEntryPlugin conditions = createPlanetaryConditionsCategory();
		CodexEntryPlugin commodities = createCommoditiesCategory();
		//CodexEntryPlugin terrain = createTerrainCategory();
		//CodexEntryPlugin factions = createFactionsCategory();
		CodexEntryPlugin gallery = createGalleryCategory();
		
		CodexEntryPlugin skills = createSkillsCategory();
		CodexEntryPlugin abilities = createAbilitiesCategory();
		
		ROOT.addChild(ships);
		//ships.addChild(stations);
		ROOT.addChild(stations);
		ROOT.addChild(fighters);
		ROOT.addChild(weapons);
		ROOT.addChild(hullmods);
		ROOT.addChild(shipSystems);
		ROOT.addChild(items);
		ROOT.addChild(industries);
		ROOT.addChild(commodities);
		ROOT.addChild(planets);
		ROOT.addChild(conditions);
		//ROOT.addChild(terrain);
		ROOT.addChild(skills);
		ROOT.addChild(abilities);
		//ROOT.addChild(factions);
		ROOT.addChild(gallery);
		
		if (WITH_GAME_MECHANICS_CAT) {
			CodexEntryPlugin mechanics = createGameMechanicsCategory();
			ROOT.addChild(mechanics);
		}
		
		rebuildIdToEntryMap();
		
		setCatSort(ROOT, null);
		
		populateShipsAndStations(ships, stations);
		//populateStations(ships);
		populateShipSystems(shipSystems, ships);
		populateHullMods(hullmods);
		populateWeapons(weapons);
		populateFighters(fighters);
		populateStarsAndPlanets(planets);
		populatePlanetaryConditions(conditions);
		populateSpecialItems(items);
		populateCommodities(commodities, items);
		populateIndustries(industries);
		populateSkills(skills);
		populateAbilities(abilities);
		//populateTerrain(terrain);
		//populateFactions(factions);
		populateGallery(gallery);
		addUnknownEntry();
		
		if (WITH_GAME_MECHANICS_CAT) {
			CodexTextEntryLoader.loadTextEntries();
		}
		
		rebuildIdToEntryMap();
		
		for (ModPlugin plugin : Global.getSettings().getModManager().getEnabledModPlugins()) {
			plugin.onAboutToLinkCodexEntries();
		}
		
		sortSkillsCategory();
		
		linkRelatedEntries();
		if (WITH_GAME_MECHANICS_CAT) {
			CodexTextEntryLoader.linkRelated();
		}
		
		if (WITH_CUSTOM_EXAMPLE_CAT) {
			addCustomCodexEntryDetailPanelExample();
		}
		
		for (ModPlugin plugin : Global.getSettings().getModManager().getEnabledModPlugins()) {
			plugin.onCodexDataGenerated();
		}
	}
	
	public static void addUnknownEntry() {
		ROOT.addChild(new CodexEntryV2(UNKNOWN_ENTRY_ID, 
				"UNKNOWN ENTRY", null, UNKNOWN_ENTRY_ID) {
			@Override
			public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
				info.addPara("UNKNOWN ENTRY", Misc.getBasePlayerColor(), 0f);
			}
			@Override
			public boolean isVisible() {
				return false;
			}
			@Override
			public boolean isLocked() {
				return false;
			}
			@Override
			public boolean hasCustomDetailPanel() {
				return true;
			}
			@Override
			public void createCustomDetail(CustomPanelAPI panel, UIPanelAPI relatedEntries, CodexDialogAPI codex) {
				float opad = 10f;
				float width = panel.getPosition().getWidth();
				float horzBoxPad = 30f;
				// the right width for a tooltip wrapped in a box to fit next to relatedEntries
				float tw = width - 290f - opad - horzBoxPad + 10f;
				
				TooltipMakerAPI text = panel.createUIElement(tw, 0, false);
				text.setParaSmallInsignia();
				text.addPara("UNKNOWN ENTRY\n\nPlease register your Tri-Tachyon datapad to receive the latest news and updates.", 0f);
				panel.updateUIElementSizeAndMakeItProcessInput(text);
				UIPanelAPI box = panel.wrapTooltipWithBox(text);
				panel.addComponent(box).inTL(0f, 0f);
				if (relatedEntries != null) {
					panel.addComponent(relatedEntries).inTR(0f, 0f);
				}
				
				float height = box.getPosition().getHeight();
				if (relatedEntries != null) {
					height = Math.max(height, relatedEntries.getPosition().getHeight());
				}
			}			
		});		
	}
	
	public static void addCustomCodexEntryDetailPanelExample() {
		if (Global.getSettings().isDevMode() && !Global.getSettings().getBoolean("playtestingMode")) {
			CodexEntryPlugin custom = createCustomExampleCategory();
			ROOT.addChild(custom);
			
			CodexCustomEntryExample entry = new CodexCustomEntryExample("custom_example_1", 
										"Custom detail example", getIcon(CAT_CUSTOM_EXAMPLE));
			entry.setParam("Has to be set to something for detail to show; "
					+ "usually this is the data for what the entry is about");
			// or, can set it to a fighter wing/hull/weapon spec to get a custom icon
			entry.setParam(Global.getSettings().getHullSpec("paragon"));
			custom.addChild(entry);
			entry.addRelatedEntry(getEntry(getHullmodEntryId(HullMods.ADVANCED_TARGETING_CORE)));
			entry.addRelatedEntry(getEntry(getShipEntryId("onslaught")));
			entry.addRelatedEntry(getEntry(getWeaponEntryId("autopulse")));
			entry.addRelatedEntry(getEntry(getFighterEntryId("wasp_wing")));
		}
	}

	public static void setCatSort(CodexEntryPlugin root, Set<CodexEntryPlugin> seen) {
		if (seen == null) seen = new LinkedHashSet<>();
		if (seen.contains(root)) return;
		seen.add(root);
		for (CodexEntryPlugin cat : root.getChildren()) {
			if (!cat.isCategory()) continue;
			Float sort = CAT_SORT_RELATED_ENTRIES.get(cat.getId());
			if (sort == null) sort = 1000f;
			cat.setCategorySortTierForRelatedEntries(sort);
			setCatSort(cat, seen);
		}
	}
	
	public static CodexEntryV2 createHullModsCategory() {
		CodexEntryV2 hullmods = new CodexEntryV2(CAT_HULLMODS, "Hullmods", getIcon(CAT_HULLMODS)) {
			@Override
			public boolean hasTagDisplay() {
				return true;
			}
			@Override
			public void configureTagDisplay(TagDisplayAPI tags) {
				CountingMap<String> techs = new CountingMap<String>();
				CountingMap<String> types = new CountingMap<String>();
				int dmods = 0;
				int intrinsic = 0;
				int total = 0;
				
				for (CodexEntryPlugin curr : getChildren()) {
					if (!(curr.getParam() instanceof HullModSpecAPI)) continue;
					if (!curr.isVisible() || curr.isLocked() || curr.skipForTags()) continue;
					HullModSpecAPI spec = (HullModSpecAPI) curr.getParam();
					
					if (spec.hasTag(Tags.HULLMOD_DMOD)) {
						dmods++;
					} else if (spec.isHidden()) {
						intrinsic++;
					}
				
					techs.add(spec.getManufacturer());
					for (String type : spec.getUITags()) {
						types.add(type);
					}
					total++;
				}
				
				if (dmods > 0) types.add(DMODS, dmods);
				if (intrinsic > 0) types.add(INTRINSIC, intrinsic);

				float opad = 10f;
				float pad = 3f;
				
				tags.beginGroup(false, ALL_TECHS);
				List<String> keys = new ArrayList<String>(techs.keySet());
				Collections.sort(keys);
				for (String tech : keys) {
					tags.addTag(tech, tech, techs.getCount(tech));
				}
				tags.setTotalOverrideForCurrentGroup(total);
				tags.addGroup(0f);
				
				
				tags.beginGroup(false, ALL_TYPES);
				keys = new ArrayList<String>(types.keySet());
				Collections.sort(keys);
				for (String type : keys) {
					tags.addTag(type, type, types.getCount(type));
				}
				tags.setTotalOverrideForCurrentGroup(total);
				tags.addGroup(opad * 1f);
				
				tags.checkAll();
			}
			
		};
		return hullmods;
	}
	
	public static CodexEntryV2 createGameMechanicsCategory() {
		CodexEntryV2 cat = new CodexEntryV2(CAT_GAME_MECHANICS, "Spacer's Manual", getIcon(CAT_GAME_MECHANICS)) {
			@Override
			public boolean hasCustomDetailPanel() {
				return true;
			}
			
			@Override
			public boolean hasDetail() {
				return true;
			}

			@Override
			public void createCustomDetail(CustomPanelAPI panel, UIPanelAPI relatedEntries, CodexDialogAPI codex) {
				Color color = Misc.getBasePlayerColor();
				Color dark = Misc.getDarkPlayerColor();
				Color h = Misc.getHighlightColor();
				Color g = Misc.getGrayColor();
				float opad = 10f;
				float pad = 3f;
				float small = 5f;
				
				float width = panel.getPosition().getWidth();
				
				float initPad = 0f;
				
				float horzBoxPad = 30f;
				
				// the right width for a tooltip wrapped in a box to fit next to relatedEntries
				// 290 is the width of the related entries widget, but it may be null
				float tw = width - 290f - opad - horzBoxPad + 10f;
				
				TooltipMakerAPI text = panel.createUIElement(tw, 0, false);
				text.setParaSmallInsignia();
			
				text.addPara("Spacer's manual description test.", 0f);
				
				panel.updateUIElementSizeAndMakeItProcessInput(text);
				
				UIPanelAPI box = panel.wrapTooltipWithBox(text);
				panel.addComponent(box).inTL(0f, 0f);
				if (relatedEntries != null) {
					panel.addComponent(relatedEntries).inTR(0f, 0f);
				}
				
				float height = box.getPosition().getHeight();
				if (relatedEntries != null) {
					height = Math.max(height, relatedEntries.getPosition().getHeight());
				}
				panel.getPosition().setSize(width, height);
			}			
		};
		
		return cat;
	}
	public static CodexEntryV2 createGalleryCategory() {
		CodexEntryV2 cat = new CodexEntryV2(CAT_GALLERY, "Gallery", getIcon(CAT_GALLERY));
		return cat;
	}
	public static CodexEntryV2 createCommoditiesCategory() {
		CodexEntryV2 cat = new CodexEntryV2(CAT_COMMODITIES, "Commodities", getIcon(CAT_COMMODITIES)) {
			
		};
		return cat;
	}
	
//	public static CodexEntryV2 createTerrainCategory() {
//		CodexEntryV2 cat = new CodexEntryV2(CAT_TERRAIN, "Terrain", getIcon(CAT_TERRAIN)) {
//			
//		};
//		return cat;
//	}
	
	public static CodexEntryV2 createIndustriesCategory() {
		CodexEntryV2 cat = new CodexEntryV2(CAT_INDUSTRIES, "Industries", getIcon(CAT_INDUSTRIES)) {
			@Override
			public boolean hasTagDisplay() {
				return true;
			}
			@Override
			public void configureTagDisplay(TagDisplayAPI tags) {
				int industry = 0;
				int structure = 0;
				int station = 0;
				int other = 0;
				int total = 0;
				for (CodexEntryPlugin curr : getChildren()) {
					if (!curr.isVisible() || curr.isLocked() || curr.skipForTags()) continue;
					if (!(curr.getParam() instanceof IndustrySpecAPI)) continue;
					
					IndustrySpecAPI spec = (IndustrySpecAPI) curr.getParam();
					if (spec.hasTag(Industries.TAG_INDUSTRY)) industry++;
					else if (spec.hasTag(Industries.TAG_STATION)) station++;
					else if (spec.hasTag(Industries.TAG_STRUCTURE)) structure++;
					else other++;
					
					total++;
				}
				tags.beginGroup(false, ALL_TYPES);
				tags.addTag(INDUSTRIES, industry);
				tags.addTag(STRUCTURES, structure);
				if (station > 0) tags.addTag(STATIONS, station);
				if (other > 0) tags.addTag(OTHER, other);
				tags.setTotalOverrideForCurrentGroup(total);
				tags.addGroup(0f);
				
				tags.checkAll();
			}
		};
		return cat;
	}
	public static CodexEntryV2 createSpecialItemsCategory() {
		CodexEntryV2 cat = new CodexEntryV2(CAT_SPECIAL_ITEMS, "Special items", getIcon(CAT_SPECIAL_ITEMS)) {
			@Override
			public boolean hasTagDisplay() {
				return true;
			}
			@Override
			public void configureTagDisplay(TagDisplayAPI tags) {
				int colony = 0;
				int core = 0;
				int bp = 0;
				int other = 0;
				int total = 0;
				for (CodexEntryPlugin curr : getChildren()) {
					if (!curr.isVisible() || curr.isLocked() || curr.skipForTags()) continue;
					if (curr.getParam() instanceof SpecialItemSpecAPI) {
						SpecialItemSpecAPI spec = (SpecialItemSpecAPI) curr.getParam();
						if (spec.hasTag(Items.TAG_COLONY_ITEM)) colony++;
						else if (spec.hasTag(Items.TAG_BLUEPRINT_PACKAGE)) bp++;
						else other++;
					} else if (curr.getParam() instanceof CommoditySpecAPI) {
						CommoditySpecAPI spec = (CommoditySpecAPI) curr.getParam();
						if (spec.hasTag(Commodities.TAG_AI_CORE)) core++;
						else other++;
					}
					total++;
				}
				tags.beginGroup(false, ALL_TYPES);
				if (colony > 0) tags.addTag(COLONY, colony);
				if (bp > 0) tags.addTag(BLUEPRINTS, bp);
				if (core > 0) tags.addTag(AI_CORE, core);
				if (other > 0) tags.addTag(OTHER, other);
				tags.setTotalOverrideForCurrentGroup(total);
				tags.addGroup(0f);
				
				tags.checkAll();
			}
		};
		return cat;
	}
	public static CodexEntryV2 createPlanetaryConditionsCategory() {
		CodexEntryV2 cat = new CodexEntryV2(CAT_PLANETARY_CONDITIONS, "Planetary conditions", getIcon(CAT_PLANETARY_CONDITIONS)) {
			@Override
			public boolean hasTagDisplay() {
				return true;
			}
			@Override
			public void configureTagDisplay(TagDisplayAPI tags) {
				int resource = 0;
				int other = 0;
				int total = 0;
				for (CodexEntryPlugin curr : getChildren()) {
					if (!curr.isVisible() || curr.isLocked() || curr.skipForTags()) continue;
					if (!(curr.getParam() instanceof MarketConditionSpecAPI)) continue;
					MarketConditionSpecAPI spec = (MarketConditionSpecAPI) curr.getParam();
					if (ResourceDepositsCondition.COMMODITY.containsKey(spec.getId())) resource++;
					else other++;
					
					total++;
				}
				tags.beginGroup(false, ALL_TYPES);
				tags.addTag(RESOURCES, resource);
				tags.addTag(OTHER, other);
				tags.setTotalOverrideForCurrentGroup(total);
				tags.addGroup(0f);
				
				tags.checkAll();
			}
		};
		return cat;
	}
	
	public static CodexEntryV2 createStarsAndPlanetsCategory() {
		CodexEntryV2 cat = new CodexEntryV2(CAT_STARS_AND_PLANETS, "Stars & planets", getIcon(CAT_STARS_AND_PLANETS)) {
			@Override
			public boolean hasTagDisplay() {
				return true;
			}
			@Override
			public void configureTagDisplay(TagDisplayAPI tags) {
				int gasGiants = 0;
				int planets = 0;
				int stars = 0;
				int habitable = 0;
				int total = 0;
				
				for (CodexEntryPlugin curr : getChildren()) {
					if (!(curr.getParam() instanceof PlanetSpecAPI)) continue;
					if (!curr.isVisible() || curr.isLocked() || curr.skipForTags()) continue;
					
					PlanetSpecAPI spec = (PlanetSpecAPI) curr.getParam();
					if (Misc.canPlanetTypeRollHabitable(spec)) {
						habitable++;
					}
					if (spec.isGasGiant()) {
						gasGiants++;
					} else if (spec.isStar()) {
						stars++;
					} else {
						planets++;
					}
					
					total++;
				}
				
				float opad = 10f;
				float pad = 3f;
				
				tags.beginGroup(false, ALL_TYPES, 120f);
				tags.addTag(STARS, stars);
				tags.addTag(GAS_GIANTS, gasGiants);
				tags.addTag(PLANETS, planets);
				tags.addTag(HABITABLE, habitable);
				tags.setTotalOverrideForCurrentGroup(total);
				tags.addGroup(opad);
				
				tags.checkAll();
			}
		};
		return cat;
	}
	
	public static CodexEntryV2 createShipSystemsCategory() {
		CodexEntryV2 shipSystems = new CodexEntryV2(CAT_SHIP_SYSTEMS, "Ship systems", getIcon(CAT_SHIP_SYSTEMS)) {
			@Override
			public boolean hasTagDisplay() {
				return true;
			}
			@Override
			public void configureTagDisplay(TagDisplayAPI tags) {
				int total = 0;
				CountingMap<String> types = new CountingMap<>();
				for (CodexEntryPlugin curr : getChildren()) {
					if (!(curr.getParam() instanceof ShipSystemSpecAPI)) continue;
					if (!curr.isVisible() || curr.isLocked() || curr.skipForTags()) continue;
					
					ShipSystemSpecAPI spec = (ShipSystemSpecAPI) curr.getParam();
					
					Description desc = Global.getSettings().getDescription(spec.getId(), Type.SHIP_SYSTEM);
					if (desc.hasText2()) {
						String typeStr = desc.getText2();
						types.add(typeStr);
					} else {
						types.add("Special");
					}
					
					total++;
				}
				
				tags.beginGroup(false, ALL_TYPES);
				List<String> keys = new ArrayList<String>(types.keySet());
				Collections.sort(keys);
				for (String type : keys) {
					tags.addTag(type, type, types.getCount(type));
				}
				tags.setTotalOverrideForCurrentGroup(total);
				tags.addGroup(0f);
				
				tags.checkAll();
			}
		};
		return shipSystems;
	}
	
	public static CodexEntryV2 createFightersCategory() {
		CodexEntryV2 fighters = new CodexEntryV2(CAT_FIGHTERS, "Fighters", getIcon(CAT_FIGHTERS)) {
			@Override
			public boolean hasTagDisplay() {
				return true;
			}
			@Override
			public void configureTagDisplay(TagDisplayAPI tags) {
				int fighter = 0;
				int bomber = 0;
				int interceptor = 0;
				int other = 0;
				int total = 0;
				
				CountingMap<String> techs = new CountingMap<String>();
				for (CodexEntryPlugin curr : getChildren()) {
					if (!(curr.getParam() instanceof FighterWingSpecAPI)) continue;
					if (!curr.isVisible() || curr.isLocked() || curr.skipForTags()) continue;
					
					FighterWingSpecAPI spec = (FighterWingSpecAPI) curr.getParam();
					techs.add(spec.getVariant().getHullSpec().getManufacturer());
					if (spec.getRole() == WingRole.FIGHTER) fighter++;
					else if (spec.getRole() == WingRole.BOMBER) bomber++;
					else if (spec.getRole() == WingRole.INTERCEPTOR) interceptor++;
					else other++;
					
					total++;
				}
				
				float opad = 10f;
				float pad = 3f;
				
				if (!techs.isEmpty()) {
					tags.beginGroup(false, ALL_TECHS);
					List<String> keys = new ArrayList<String>(techs.keySet());
					Collections.sort(keys);
					for (String tech : keys) {
						tags.addTag(tech, tech, techs.getCount(tech));
					}
					tags.setTotalOverrideForCurrentGroup(total);
					tags.addGroup(0f);
				}
				
				tags.beginGroup(false, ALL_TYPES, 120f);
				tags.addTag(FIGHTER, fighter);
				tags.addTag(BOMBER, bomber);
				tags.addTag(INTERCEPTOR, interceptor);
				tags.addTag(OTHER, other);
				tags.setTotalOverrideForCurrentGroup(total);
				tags.addGroup(opad);
				
				tags.checkAll();
			}
		};
		return fighters;
	}
	
	public static CodexEntryV2 createCustomExampleCategory() {
		CodexEntryV2 custom = new CodexEntryV2(CAT_CUSTOM_EXAMPLE, "Custom example", getIcon(CAT_CUSTOM_EXAMPLE));
		return custom;
	}
	
	public static CodexEntryV2 createWeaponsCategory() {
		CodexEntryV2 weapons = new CodexEntryV2(CAT_WEAPONS, "Weapons", getIcon(CAT_WEAPONS)) {
			@Override
			public boolean hasTagDisplay() {
				return true;
			}
			@Override
			public void configureTagDisplay(TagDisplayAPI tags) {
				int small = 0;
				int medium = 0;
				int large = 0;
				int fighter = 0;
				int ballistic = 0;
				int missile = 0;
				int energy = 0;
				int hybrid = 0;
				int composite = 0;
				int synergy = 0;
				int universal = 0;
				int beam = 0;
				int damHE = 0;
				int damKinetic = 0;
				int damEnergy = 0;
				int damFrag = 0;
				int damOther = 0;
				int total = 0;
				
				CountingMap<String> techs = new CountingMap<String>();
				for (CodexEntryPlugin curr : getChildren()) {
					if (!(curr.getParam() instanceof WeaponSpecAPI)) continue;
					if (!curr.isVisible() || curr.isLocked() || curr.skipForTags()) continue;
					
					WeaponSpecAPI spec = (WeaponSpecAPI) curr.getParam();
					techs.add(spec.getManufacturer());
					
					if (spec.getAIHints().contains(AIHints.SYSTEM) &&
							spec.getPrimaryRoleStr() != null &&
							spec.getPrimaryRoleStr().endsWith("(Fighter)")) {
						fighter++;
					} else {
						if (spec.getSize() == WeaponSize.SMALL) small++;
						else if (spec.getSize() == WeaponSize.MEDIUM) medium++;
						else if (spec.getSize() == WeaponSize.LARGE) large++;
					}
					
					if (spec.getType() == WeaponType.BALLISTIC) ballistic++;
					else if (spec.getType() == WeaponType.MISSILE) missile++;
					else if (spec.getType() == WeaponType.ENERGY) energy++;
					
					if (spec.getMountType() == WeaponType.HYBRID) hybrid++;
					else if (spec.getMountType() == WeaponType.COMPOSITE) composite++;
					else if (spec.getMountType() == WeaponType.SYNERGY) synergy++;
					else if (spec.getMountType() == WeaponType.UNIVERSAL) universal++;
					
					if (spec.getDamageType() == DamageType.HIGH_EXPLOSIVE) damHE++;
					else if (spec.getDamageType() == DamageType.KINETIC) damKinetic++;
					else if (spec.getDamageType() == DamageType.ENERGY) damEnergy++;
					else if (spec.getDamageType() == DamageType.FRAGMENTATION) damFrag++;
					else if (spec.getDamageType() == DamageType.OTHER) damOther++;
					
					if (spec.isBeam()) beam++;
					
					total++;
				}
				
				float opad = 10f;
				float pad = 3f;
				
				if (!techs.isEmpty()) {
					tags.beginGroup(false, ALL_TECHS);
					List<String> keys = new ArrayList<String>(techs.keySet());
					Collections.sort(keys);
					for (String tech : keys) {
						tags.addTag(tech, tech, techs.getCount(tech));
					}
					tags.setTotalOverrideForCurrentGroup(total);
					tags.addGroup(0f);
				}
				
				tags.beginGroup(false, ALL_SIZES, 120f);
				tags.addTag(SMALL, small);
				tags.addTag(MEDIUM, medium);
				tags.addTag(LARGE, large);
				if (fighter > 0) tags.addTag(FIGHTER_WEAPON, fighter);
				tags.setTotalOverrideForCurrentGroup(total);
				tags.addGroup(opad * 1f);
				
				tags.beginGroup(false, ALL_TYPES, 120f);
				tags.addTag(BALLISTIC, ballistic);
				tags.addTag(MISSILE, missile);
				tags.addTag(ENERGY, energy);
				if (hybrid > 0) tags.addTag(HYBRID, hybrid);
				if (composite > 0) tags.addTag(COMPOSITE, composite);
				if (synergy > 0) tags.addTag(SYNERGY, synergy);
				if (universal > 0) tags.addTag(UNIVERSAL, universal);
				if (beam > 0) tags.addTag(BEAM, beam);
				tags.setTotalOverrideForCurrentGroup(total);
				tags.addGroup(pad);
				
				tags.beginGroup(false, ALL_DAMAGE_TYPES, 140f);
				tags.addTag(HIGH_EXPLOSIVE, damHE);
				tags.addTag(KINETIC, damKinetic);
				tags.addTag(FRAGMENTATION, damFrag);
				tags.addTag(DAM_ENERGY, damEnergy);
				if (damOther > 0) tags.addTag(OTHER, damOther);
				tags.setTotalOverrideForCurrentGroup(total);
				tags.addGroup(pad);
				
				tags.checkAll();
			}
			
		};
		return weapons;
	}
	
	public static CodexEntryV2 createSkillsCategory() {
		CodexEntryV2 skills = new CodexEntryV2(CAT_SKILLS, "Skills", getIcon(CAT_SKILLS)) {
			@Override
			public boolean hasTagDisplay() {
				return true;
			}
			@Override
			public void configureTagDisplay(TagDisplayAPI tags) {
				int personal = 0;
				int other = 0;
				int total = 0;
				
				CountingMap<String> apts = new CountingMap<String>();
				for (CodexEntryPlugin curr : getChildren()) {
					if (!(curr.getParam() instanceof SkillSpecAPI)) continue;
					if (!curr.isVisible() || curr.isLocked() || curr.skipForTags()) continue;
					
					SkillSpecAPI spec = (SkillSpecAPI) curr.getParam();
					String apt = getAptitudeName(spec);
					if (!apt.isEmpty()) apts.add(apt);
					
					if (spec.isElite()) personal++;
					else other++;
					
					total++;
				}
				
				float opad = 10f;
				float pad = 3f;
				
				if (!apts.isEmpty()) {
					tags.beginGroup(false, ALL_APTITUDES);
					List<String> keys = new ArrayList<String>(apts.keySet());
					Collections.sort(keys);
					for (String tech : keys) {
						tags.addTag(tech, tech, apts.getCount(tech));
					}
					tags.addGroup(0f);
				}
				
				tags.beginGroup(false, ALL_TYPES, 120f);
				tags.addTag(PILOTED_SHIP, personal);
				tags.addTag(OTHER, other);
				tags.setTotalOverrideForCurrentGroup(total);
				tags.addGroup(opad);
				
				tags.checkAll();
			}			
		};
		skills.setRetainOrderOfChildren(true);
		return skills;
	}
	
	public static CodexEntryV2 createAbilitiesCategory() {
		CodexEntryV2 abilities = new CodexEntryV2(CAT_ABILITIES, "Abilities", getIcon(CAT_ABILITIES));
		return abilities;
	}
	
	public static CodexEntryV2 createShipsCategory() {
		CodexEntryV2 ships = new CodexEntryV2(CAT_SHIPS, "Ships", getIcon(CAT_SHIPS)) {
			@Override
			public boolean hasTagDisplay() {
				return true;
			}
			@Override
			public void configureTagDisplay(TagDisplayAPI tags) {
				int carrier = 0;
				int civilian = 0;
				int phase = 0;
				int combat = 0;
				int frigate = 0;
				int destroyer = 0;
				int cruiser = 0;
				int capital = 0;
				int stations = 0;
				
				int total = 0;
				CountingMap<String> counts = new CountingMap<String>();
				//for (UITableRow o : shipTable.getRows()) {
				for (CodexEntryPlugin curr : getChildren()) {
					if (!(curr.getParam() instanceof ShipHullSpecAPI)) continue;
					if (!curr.isVisible() || curr.isLocked() || curr.skipForTags()) continue;

					boolean station = false;
					//boolean stationModule = false;
					if (curr.getParam2() instanceof FleetMemberAPI) {
						FleetMemberAPI fm = (FleetMemberAPI) curr.getParam2();
						station = fm.isStation();
					}
					
					ShipHullSpecAPI spec = (ShipHullSpecAPI) curr.getParam();
					boolean isPhase = spec.isPhase() || spec.getHints().contains(ShipTypeHints.PHASE);
					
					counts.add(spec.getManufacturer());
					if (spec.isCarrier()) carrier++;
					else if (spec.isCivilianNonCarrier()) civilian++;
					else if (isPhase) phase++;
					else if (!station) combat++;
					
					if (spec.isCivilianNonCarrier() && isPhase) phase++;
					
					if (station) stations++;
					else if (spec.getHullSize() == HullSize.FRIGATE) frigate++;
					else if (spec.getHullSize() == HullSize.DESTROYER) destroyer++;
					else if (spec.getHullSize() == HullSize.CRUISER) cruiser++;
					else if (spec.getHullSize() == HullSize.CAPITAL_SHIP) capital++;
					
					total++;
				}
				
				float opad = 10f;
				float pad = 3f;
				
				if (!counts.isEmpty()) {
					tags.beginGroup(false, ALL_TECHS);
					List<String> keys = new ArrayList<String>(counts.keySet());
					Collections.sort(keys);
					for (String tech : keys) {
						tags.addTag(tech, tech, counts.getCount(tech));
					}
					tags.addGroup(0f);
				}
				
				tags.beginGroup(false, ALL_SIZES, 120f);
				//tags.beginGroup(true, null, 110f);
				tags.addTag(FRIGATES, frigate);
				tags.addTag(DESTROYERS, destroyer);
				tags.addTag(CRUISERS, cruiser);
				tags.addTag(CAPITALS, capital);
				if (stations > 0) tags.addTag(STATIONS, stations);
				tags.setTotalOverrideForCurrentGroup(total);
				tags.addGroup(opad * 1f);
				
				tags.beginGroup(false, ALL_TYPES, 120f);
				//tags.beginGroup(true, null, 110f);
				tags.addTag(COMBAT_SHIPS, combat);
				tags.addTag(PHASE_SHIPS , phase);
				tags.addTag(CARRIERS, carrier);
				tags.addTag(CIVILIAN, civilian);
				tags.setTotalOverrideForCurrentGroup(total);
				tags.addGroup(pad);
				
				tags.setGroupChecked(0, true);
				tags.setGroupChecked(1, true);
				tags.setGroupChecked(2, true);
			}
			
		};
		return ships;
	}
	
	public static CodexEntryV2 createStationsCategory() {
		CodexEntryV2 stations = new CodexEntryV2(CAT_STATIONS, "Stations", getIcon(CAT_STATIONS)) {
			@Override
			public boolean hasTagDisplay() {
				return true;
			}
			@Override
			public void configureTagDisplay(TagDisplayAPI tags) {
				int total = 0;
				CountingMap<String> counts = new CountingMap<String>();
				//for (UITableRow o : shipTable.getRows()) {
				for (CodexEntryPlugin curr : getChildren()) {
					if (!(curr.getParam() instanceof ShipHullSpecAPI)) continue;
					if (!curr.isVisible() || curr.isLocked() || curr.skipForTags()) continue;
					
					ShipHullSpecAPI spec = (ShipHullSpecAPI) curr.getParam();
					counts.add(spec.getManufacturer());
					total++;
				}
				
				if (!counts.isEmpty()) {
					tags.beginGroup(false, ALL_TECHS);
					List<String> keys = new ArrayList<String>(counts.keySet());
					Collections.sort(keys);
					for (String tech : keys) {
						tags.addTag(tech, tech, counts.getCount(tech));
					}
					tags.setTotalOverrideForCurrentGroup(total);
					tags.addGroup(0f);
				}
				tags.checkAll();
			}
			
		};
		return stations;
	}
	
	public static void populateShipsAndStations(CodexEntryPlugin ships, CodexEntryPlugin stations) {
		List<ShipHullSpecAPI> shipSpecs = Global.getSettings().getAllShipHullSpecs();
		for (final ShipHullSpecAPI spec : shipSpecs) {
			if (spec.getHullSize() == ShipAPI.HullSize.FIGHTER && !spec.hasTag(Tags.SHOW_IN_CODEX_AS_SHIP)) continue;
			if (spec.getHints().contains(ShipTypeHints.HIDE_IN_CODEX)) continue;
			if (spec.hasTag(Tags.HIDE_IN_CODEX)) continue;
			if (spec.isDefaultDHull()) continue;
			if (spec.getHullId().equals("shuttlepod")) continue;
			
			boolean station = spec.getHints().contains(ShipTypeHints.STATION);
			String name = spec.getHullNameWithDashClass();
			String designation = "";
			if (spec.hasDesignation() && !spec.getDesignation().equals(spec.getHullName())) {
				designation = " " + spec.getDesignation().toLowerCase();
			}
			if (station) {
				designation = "";
				name = spec.getHullName();
			}
			CodexEntryV2 curr = new CodexEntryV2(getShipEntryId(spec.getHullId()), 
				spec.getHullNameWithDashClass() + designation, null, spec) {
					@Override
					public String getSortTitle() {
						return spec.getHullName();
					}
					@Override
					public String getSearchString() {
						if (station) {
							return super.getSearchString() + " Station";
						}
						return super.getSearchString();
					}
					@Override
					public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
						info.addPara(spec.getHullName(), Misc.getBasePlayerColor(), 0f);
						if (station) {
							if (mode == ListMode.RELATED_ENTRIES) {
								info.addPara("Station", Misc.getGrayColor(), 0f);
							}
						} else if (spec.hasDesignation() && !spec.getDesignation().equals(spec.getHullName())) {
							info.addPara(Misc.ucFirst(spec.getDesignation().toLowerCase()), Misc.getGrayColor(), 0f);
						}
					}

					@Override
					public boolean matchesTags(Set<String> tags) {
						HullSize size = spec.getHullSize();
						
						if (station) {
//							String sizeTag = STATIONS;
//							if (sizeTag != null && !tags.contains(sizeTag)) return false;
	
							String m = spec.getManufacturer();
							if (m != null && !tags.contains(m) && !tags.contains(ALL_TECHS)) return false;
							
//							if (!tags.contains(ALL_TYPES)) return false;
							
							return true;
						}
						
						String sizeTag = null;
						if (size == HullSize.FRIGATE) sizeTag = FRIGATES;
						if (size == HullSize.DESTROYER) sizeTag = DESTROYERS;
						if (size == HullSize.CRUISER) sizeTag = CRUISERS;
						if (size == HullSize.CAPITAL_SHIP) sizeTag = CAPITALS;
						
						if (sizeTag != null && !tags.contains(sizeTag)) return false;

						boolean isPhase = spec.isPhase() || spec.getHints().contains(ShipTypeHints.PHASE);
						boolean phaseCiv = isPhase && spec.isCivilianNonCarrier();
						if (!phaseCiv && isPhase && !tags.contains(PHASE_SHIPS)) return false;
						if (!phaseCiv && spec.isCivilianNonCarrier() && !tags.contains(CIVILIAN)) return false;
						if (phaseCiv && !tags.contains(PHASE_SHIPS) && !tags.contains(CIVILIAN)) return false;
						
						if (spec.isCarrier() && !tags.contains(CARRIERS)) return false;
						
						boolean combat = !isPhase && !spec.isCarrier() && !spec.isCivilianNonCarrier();
						if (combat && !tags.contains(COMBAT_SHIPS)) return false;
						
						String m = spec.getManufacturer();
						if (m != null && !tags.contains(m) && !tags.contains(ALL_TECHS)) return false;
						
						return true;
					}
					
					@Override
					public Set<String> getUnlockRelatedTags() {
						return spec.getTags();
					}
					
					@Override
					public boolean isUnlockedIfRequiresUnlock() {
						return SharedUnlockData.get().isPlayerAwareOfShip(spec.getHullId());
					}
			};
			if (station) {
				stations.addChild(curr);
			} else {
				ships.addChild(curr);
			}
		
			String variantId = spec.getHullId() + "_Hull";
			if (spec.getCodexVariantId() != null && !spec.getCodexVariantId().isBlank()) {
				variantId = spec.getCodexVariantId();
			}
			ShipVariantAPI variant = Global.getSettings().getVariant(variantId);
			if (variant != null) {
				CodexEntryPlugin parent = ships;
				if (station) parent = stations;
				
				int relBefore = curr.getRelatedEntries().size();
				List<CodexEntryPlugin> added = addModulesForVariant(variant, true, curr, parent);
				// created entries, or they were already created but just added them as related
				if (!added.isEmpty() || relBefore < curr.getRelatedEntryIds().size()) {
					FleetMemberAPI member = Global.getSettings().createFleetMember(FleetMemberType.SHIP, variant);
					curr.setParam2(member);
				}
			}
		}
	}
	
//  handled by addShipsAndStations now	
//	public static void populateStations(CodexEntryPlugin parent) {
//		List<IndustrySpecAPI> specs = Global.getSettings().getAllIndustrySpecs();
//		for (final IndustrySpecAPI spec : specs) {
//			if (spec.hasTag(Industries.TAG_PARENT)) continue;
//			if (spec.hasTag(Tags.HIDE_IN_CODEX)) continue;
//		
//			if (spec.hasTag(Tags.STATION)) {
//				String variantId = null;
//				try {
//					JSONObject json = new JSONObject(spec.getData());
//					variantId = json.getString("variant");
//				} catch (JSONException e) {
//					continue;
//				}
//				
//				if (variantId == null || variantId.isBlank()) continue;
//				
//				FleetMemberAPI member = createStationFleetMember(variantId);
//				addStationEntry(parent, member, true, false);
//			}
//		}
//		
//		addStationEntry(parent, "remnant_station2_Standard", true, false);
//		
//		SalvageEntityGenDataSpec mothership = SalvageEntityGeneratorOld.getSalvageSpec(Entities.DERELICT_MOTHERSHIP);
//		if (mothership != null) {
//			String role = mothership.getStationRole();
//			String factionId = mothership.getDefFaction();
//			FactionSpecAPI faction = Global.getSettings().getFactionSpec(factionId);
//			if (faction != null) {
//				for (String variantId : faction.getAllVariantsForRole(role)) {
//					addStationEntry(parent, variantId, true, false);
//				}
//			}
//		}
//	}
//	
//	public static void addStationEntry(CodexEntryPlugin parent, String variantId, boolean convertToHull, boolean forceAdd) {
//		FleetMemberAPI member = createStationFleetMember(variantId);
//		addStationEntry(parent, member, convertToHull, forceAdd);
//	}
//	
//	public static FleetMemberAPI createStationFleetMember(String variantId) {
//		FleetMemberAPI member = Global.getSettings().createFleetMember(FleetMemberType.SHIP, variantId);
//		return member;
//	}
//	
//	public static void addStationEntry(CodexEntryPlugin parent, FleetMemberAPI member, boolean convertToHull, boolean forceAdd) {
//		ShipHullSpecAPI spec = member.getHullSpec();
//		
//		if (!forceAdd && spec.hasTag(Tags.HIDE_STATION_IN_CODEX)) return;
//		
//		if (convertToHull) {
//			String variantId = getFleetMemberBaseHullId(member) + "_Hull";
//			ShipVariantAPI variant = Global.getSettings().getVariant(variantId).clone();
//			// the variant already has empty _Hull modules, or should
//			if (variant.getStationModules() == null || variant.getStationModules().isEmpty()) {
//	//			for (String slotId : member.getVariant().getModuleSlots()) {
//	//				ShipVariantAPI module = member.getVariant().getModuleVariant(slotId);
//	//				if (module != null) {
//	//					String moduleBaseHullVariantId = getBaseHullId(module.getHullSpec()) + "_Hull";
//	//					ShipVariantAPI emptyModuleVariant = Global.getSettings().getVariant(moduleBaseHullVariantId).clone();
//	//					variant.setModuleVariant(slotId, emptyModuleVariant);
//	//				}
//	//			}
//			}
//			member.setVariant(variant, false, true);
//		}
//		
//			
//		CodexEntryV2 curr = new CodexEntryV2(getShipEntryId(spec.getHullId()), 
//			spec.getHullName(), null, spec) {
//				@Override
//				public String getSortTitle() {
//					return spec.getHullName();
//				}
//				@Override
//				public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
//					info.addPara(spec.getHullName(), Misc.getBasePlayerColor(), 0f);
//					info.addPara("Station", Misc.getGrayColor(), 0f);
//				}
//
//				@Override
//				public boolean matchesTags(Set<String> tags) {
//					String sizeTag = STATIONS;
//					if (sizeTag != null && !tags.contains(sizeTag)) return false;
//
//					String m = spec.getManufacturer();
//					if (m != null && !tags.contains(m) && !tags.contains(ALL_TECHS)) return false;
//					
//					if (!tags.contains(ALL_TYPES)) return false;
//					
//					return true;
//				}
//				
//				@Override
//				public String getSearchString() {
//					return super.getSearchString() + " Station";
//				}
//				@Override
//				public Set<String> getUnlockRelatedTags() {
//					return spec.getTags();
//				}
//				
//				@Override
//				public boolean isUnlockedIfRequiresUnlock() {
//					return SharedUnlockData.get().isPlayerAwareOfShip(spec.getHullId());
//				}
//		};
//		curr.setParam2(member);
//		parent.addChild(curr);
//		
//		if (convertToHull) {
//			curr.addTag(TAG_EMPTY_HULL_WITH_MODULES);
//		}
//		
//		addModulesForVariant(member.getVariant(), convertToHull, curr, parent);
//	}
	
	public static List<CodexEntryPlugin> addModulesForVariant(ShipVariantAPI variant, boolean isEmptyHull,
											CodexEntryPlugin entry, CodexEntryPlugin parent) {
		List<CodexEntryPlugin> result = new ArrayList<>();		
		Set<String> seenVariants = new HashSet<>();
		for (String slotId : variant.getStationModules().keySet()) {
			ShipVariantAPI moduleVariant = variant.getModuleVariant(slotId);
			if (moduleVariant == null) continue;
			if (moduleVariant.hasHullMod(HullMods.VASTBULK)) continue;

			String moduleEntryId = getShipEntryId(moduleVariant.getHullSpec().getHullId());
			if (isEmptyHull) {
				if (SEEN_STATION_MODULES.containsKey(moduleEntryId) || ENTRIES.containsKey(moduleEntryId)) {
					makeRelated(entry, SEEN_STATION_MODULES.get(moduleEntryId));
					continue;
				}
			} else {
				// we're adding an entry for a specific fleet member's variant of the station
				// so, want to add all the modules as fleetmember entries
				// except for ones with duplicate variants
				try {
					String test = moduleVariant.toJSONObject().toString(4);
					if (seenVariants.contains(test)) continue;
					seenVariants.add(test);
				} catch (JSONException e) {
				}
			}
			
			FleetMemberAPI moduleMember = Global.getSettings().createFleetMember(FleetMemberType.SHIP, moduleVariant);
			//if (moduleMember.getVariant().getFittedWeaponSlots().isEmpty()) continue;
			CodexEntryPlugin module = addModuleEntry(parent, entry, moduleMember, isEmptyHull);
			SEEN_STATION_MODULES.put(moduleEntryId, module);
			
			makeRelated(entry, module);
			result.add(module);
			
			if (!isEmptyHull) {
				linkFleetMemberEntryToRelated(module, moduleMember, false);
			}
		}
		return result;
	}
	
	public static CodexEntryPlugin addModuleEntry(CodexEntryPlugin parent, CodexEntryPlugin entryForParentShip,
												  FleetMemberAPI member, boolean isEmptyHull) {
		ShipHullSpecAPI spec = member.getHullSpec();
			
		String moduleEntryId = getShipEntryId(spec.getHullId());
		if (!isEmptyHull) { // adding a temp entry
			moduleEntryId = UUID.randomUUID().toString();
		}
		
		CodexEntryV2 curr = new CodexEntryV2(moduleEntryId, 
			spec.getHullName(), null, spec) {
				@Override
				public String getSortTitle() {
					return spec.getHullName();
				}
				@Override
				public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
					info.addPara(spec.getHullName(), Misc.getBasePlayerColor(), 0f);
					info.addPara("Module", Misc.getGrayColor(), 0f);
				}
				@Override
				public boolean matchesTags(Set<String> tags) {
					return false;
				}
				@Override
				public boolean checkTagsWhenLocked() {
					// so that the module does not show up in the main list when it's locked
					// despite matchesTags() always being false
					return true;
				}
				@Override
				public boolean isVisible() {
					return entryForParentShip.isVisible();
				}
				@Override
				public boolean isLocked() {
					return entryForParentShip.isLocked();
				}
				@Override
				public String getSearchString() {
					return "";
				}
				@Override
				public boolean skipForTags() {
					// actually, not adding it to the parent's children is easier
					// actually: not easier, it messes up the sort order in the related entries list
					return true;
				}
		};
		curr.setParam2(member);
		if (isEmptyHull) {
			curr.addTag(TAG_EMPTY_MODULE);
		}
		if (parent != null) parent.addChild(curr);
		return curr;
	}
	
	public static void populateShipSystems(CodexEntryPlugin parent, CodexEntryPlugin ships) {
		List<ShipSystemSpecAPI> specs = Global.getSettings().getAllShipSystemSpecs();
		for (final ShipSystemSpecAPI spec : specs) {
			//if (spec.getTags().contains(Tags.RESTRICTED)) continue;
			if (spec.getTags().contains(Tags.HIDE_IN_CODEX)) continue;
			
			Description desc = Global.getSettings().getDescription(spec.getId(), Type.SHIP_SYSTEM);
			String typeStr = desc.getText2();
			CodexEntryV2 curr = new CodexEntryV2(getShipSystemEntryId(spec.getId()), 
				spec.getName(), spec.getIconSpriteName(), spec) {
					@Override
					public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
						info.addPara(spec.getName(), Misc.getBasePlayerColor(), 0f);
						if (mode == ListMode.RELATED_ENTRIES) {
							info.addPara("Ship system", Misc.getGrayColor(), 0f);
						} else if (desc.hasText2()) {
							info.addPara(typeStr, Misc.getGrayColor(), 0f);
						} else {
							info.addPara("Special", Misc.getGrayColor(), 0f);
						}
					}
					@Override
					public boolean matchesTags(Set<String> tags) {
						if (tags.contains(ALL_TYPES)) return true;
						Description desc = Global.getSettings().getDescription(spec.getId(), Type.SHIP_SYSTEM);
						String typeStr = desc.getText2();
						if (!desc.hasText2()) typeStr = "Special";
						return tags.contains(typeStr);
					}
					
					@Override
					public boolean isVignetteIcon() {
						return true;
					}
					
					@Override
					public Color getIconColor() {
						return Misc.getBasePlayerColor();
					}

					@Override
					public Set<String> getUnlockRelatedTags() {
						LinkedHashSet<String> tags = new LinkedHashSet<>(spec.getTags());
						tags.add(Tags.CODEX_REQUIRE_RELATED);
						return tags;
					}
					
					@Override
					public boolean isUnlockedIfRequiresUnlock() {
						return SharedUnlockData.get().isPlayerAwareOfShipSystem(spec.getId());
					}
					
					@Override
					public String getSearchString() {
						return super.getSearchString();// + " " + typeStr;
					}
			};
			
			parent.addChild(curr);
		}
	}
	
	
	public static void populateSkills(CodexEntryPlugin parent) {
		List<String> skillIds = Global.getSettings().getSkillIds();
		for (String skillId : skillIds) {
			SkillSpecAPI spec = Global.getSettings().getSkillSpec(skillId);
			String aptitude = getAptitudeName(spec);
			String aptStr = "";
			if (aptitude != null && !aptitude.isBlank()) {
				aptStr = " - " + aptitude + " skill";
			}

			if (spec.hasTag(Tags.HIDE_IN_CODEX)) continue;
			if (spec.isAptitudeEffect()) continue;
			
			if (!spec.hasTag(Tags.SHOW_IN_CODEX) && !spec.hasTag(Tags.CODEX_UNLOCKABLE)) {
				if (spec.hasTag(Skills.TAG_NPC_ONLY)) continue;
				if (spec.hasTag(Skills.TAG_AI_CORE_ONLY)) continue;
			};
			if (spec.hasTag(Skills.TAG_DEPRECATED)) continue;
			
			CodexEntryV2 curr = new CodexEntryV2(getSkillEntryId(spec.getId()), 
				spec.getName() + aptStr, spec.getSpriteName(), spec) {
					@Override
					public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
						info.addPara(spec.getName(), Misc.getBasePlayerColor(), 0f);
						if (mode == ListMode.RELATED_ENTRIES || true) {
							String aptitude = getAptitudeName(spec);
							if (aptitude != null && !aptitude.isBlank()) {
								info.addPara(aptitude + " skill", Misc.getGrayColor(), 0f);
							} else {
								info.addPara("Skill", Misc.getGrayColor(), 0f);
							}
						}
					}
					
					@Override
					public boolean matchesTags(Set<String> tags) {
						if (spec.isElite() && !tags.contains(PILOTED_SHIP)) return false;
						if (!spec.isElite() && !tags.contains(OTHER)) return false;
						
						String aptitude = getAptitudeName(spec);
						if ((aptitude == null || aptitude.isBlank()) && !tags.contains(ALL_APTITUDES)) {
							return false;
						}
						return tags.contains(aptitude);
					}
					
					@Override
					public boolean isVignetteIcon() {
						return true;
					}
					
					@Override
					public Color getIconColor() {
						return Color.white;
					}

					@Override
					public Set<String> getUnlockRelatedTags() {
						return spec.getTags();
					}
					
					@Override
					public boolean isUnlockedIfRequiresUnlock() {
						return SharedUnlockData.get().isPlayerAwareOfSkill(spec.getId());
					}
					
					@Override
					public String getSearchString() {
						String aptitude = getAptitudeName(spec);
						return super.getSearchString() + " " + aptitude;
					}
			};
			
			parent.addChild(curr);
		}
	}
	
	public static void populateAbilities(CodexEntryPlugin parent) {
		List<String> ids = Global.getSettings().getSortedAbilityIds();
		for (String id : ids) {
			AbilitySpecAPI spec = Global.getSettings().getAbilitySpec(id);

			if (spec.hasTag(Tags.HIDE_IN_CODEX)) continue;
			
			CodexEntryV2 curr = new CodexEntryV2(getAbilityEntryId(spec.getId()), 
				spec.getName(), spec.getIconName(), spec) {
					@Override
					public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
						info.addPara(spec.getName(), Misc.getBasePlayerColor(), 0f);
						if (mode == ListMode.RELATED_ENTRIES) {
							info.addPara("Ability", Misc.getGrayColor(), 0f);
						}
					}
					@Override
					public boolean matchesTags(Set<String> tags) {
						return super.matchesTags(tags);
					}
					
					@Override
					public boolean isVignetteIcon() {
						return true;
					}
					
					@Override
					public Color getIconColor() {
						return Color.white;
					}

					@Override
					public Set<String> getUnlockRelatedTags() {
						return spec.getTags();
					}
					
					@Override
					public boolean isUnlockedIfRequiresUnlock() {
						return SharedUnlockData.get().isPlayerAwareOfAbility(spec.getId());
					}
					
					@Override
					public String getSearchString() {
						return super.getSearchString();
					}
			};
			
			parent.addChild(curr);
		}
	}
	
	public static String getAptitudeName(SkillSpecAPI spec) {
		String aptitude = spec.getGoverningAptitudeName();
		if (spec.hasTag(Skills.TAG_AI_CORE_ONLY)) {
			aptitude = "AI core";
		}
		return aptitude;
	}
	
	
	public static void sortSkillsCategory() {
		CodexEntryPlugin skills = getEntry(CAT_SKILLS);
		Collections.sort(skills.getChildren(), new Comparator<CodexEntryPlugin>() {
			@Override
			public int compare(CodexEntryPlugin o1, CodexEntryPlugin o2) {
				SkillSpecAPI s1 = null;
				SkillSpecAPI s2 = null;
				if (o1.getParam() instanceof SkillSpecAPI) {
					s1 = (SkillSpecAPI) o1.getParam();
				}
				if (o2.getParam() instanceof SkillSpecAPI) {
					s2 = (SkillSpecAPI) o2.getParam();
				}
				if (s1 != null && s2 == null) return Integer.MIN_VALUE;
				if (s2 != null && s1 == null) return Integer.MAX_VALUE;
				if (s1 == null && s2 == null) return o1.getTitle().compareTo(o2.getTitle());
				
				int tier1 = 0;
				int tier2 = 0;
				if (s1.hasTag(Skills.TAG_AI_CORE_ONLY)) tier1 = 5;
				if (s2.hasTag(Skills.TAG_AI_CORE_ONLY)) tier2 = 5;
				
				if (tier1 != tier2) return tier1 - tier2;
				
				int diff = s1.getGoverningAptitudeOrder() - s2.getGoverningAptitudeOrder();
				if (diff != 0) return diff;
				
				return (int) Math.signum(s1.getOrder() - s2.getOrder());
			}
		});
	}
	
	
	public static void populateWeapons(CodexEntryPlugin parent) {
		
		for (WeaponSpecAPI spec : Global.getSettings().getActuallyAllWeaponSpecs()) {
			//if (spec.getTags().contains(Tags.RESTRICTED)) continue;
			if (spec.hasTag(Tags.HIDE_IN_CODEX)) continue;
			if ((spec.getAIHints().contains(AIHints.SYSTEM) || spec.getType() == WeaponType.SYSTEM) &&
					!(spec.getAIHints().contains(AIHints.SHOW_IN_CODEX) ||
							spec.hasTag(Tags.SHOW_IN_CODEX))) {
				continue;
			}
			
			CodexEntryV2 curr = new CodexEntryV2(getWeaponEntryId(spec.getWeaponId()), 
				spec.getWeaponName(), null, spec) {
					@Override
					public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
						info.addPara(spec.getWeaponName(), Misc.getBasePlayerColor(), 0f);
						if (mode == ListMode.RELATED_ENTRIES || true) {
							String size = spec.getSize().getDisplayName();
							WeaponType type = spec.getMountType();
							if (type == null) type = spec.getType();
							
							info.addPara(size + " " + type.getDisplayName().toLowerCase() + " weapon", Misc.getGrayColor(), 0f);
							//info.addPara("Weapon", Misc.getGrayColor(), 0f);
						}
					}

					@Override
					public boolean matchesTags(Set<String> tags) {
						WeaponSize size = spec.getSize();
						
						String sizeTag = null;
						if (size == WeaponSize.SMALL) sizeTag = SMALL;
						if (size == WeaponSize.MEDIUM) sizeTag = MEDIUM;
						if (size == WeaponSize.LARGE) sizeTag = LARGE;
						if (spec.getAIHints().contains(AIHints.SYSTEM) && 
								spec.getPrimaryRoleStr() != null &&
								spec.getPrimaryRoleStr() != null && 
								spec.getPrimaryRoleStr().endsWith("(Fighter)")) {
							sizeTag = FIGHTER_WEAPON;
						}
						
						if (sizeTag != null && !tags.contains(sizeTag)) return false;
						
						String damTypeTag = null;
						DamageType type = spec.getDamageType();
						if (type == DamageType.HIGH_EXPLOSIVE) damTypeTag = HIGH_EXPLOSIVE;
						if (type == DamageType.KINETIC) damTypeTag = KINETIC;
						if (type == DamageType.ENERGY) damTypeTag = DAM_ENERGY;
						if (type == DamageType.FRAGMENTATION) damTypeTag = FRAGMENTATION;
						if (type == DamageType.OTHER) damTypeTag = OTHER;
						if (damTypeTag != null && !tags.contains(damTypeTag)) return false;
						
						String m = spec.getManufacturer();
						if (m != null && !tags.contains(m) && !tags.contains(ALL_TECHS)) return false;
						
						
						if (spec.getMountType() == WeaponType.HYBRID && tags.contains(HYBRID)) return true;
						if (spec.getMountType() == WeaponType.COMPOSITE && tags.contains(COMPOSITE)) return true;
						if (spec.getMountType() == WeaponType.SYNERGY && tags.contains(SYNERGY)) return true;
						if (spec.getMountType() == WeaponType.UNIVERSAL && tags.contains(UNIVERSAL)) return true;
						if (spec.isBeam() && tags.contains(BEAM)) return true;
						
						if (spec.getType() == WeaponType.BALLISTIC && !tags.contains(BALLISTIC)) return false;
						if (spec.getType() == WeaponType.MISSILE && !tags.contains(MISSILE)) return false;
						if (spec.getType() == WeaponType.ENERGY && !tags.contains(ENERGY)) return false;
						
						return true;
					}
					
					@Override
					public Set<String> getUnlockRelatedTags() {
						return spec.getTags();
					}
					
					@Override
					public boolean isUnlockedIfRequiresUnlock() {
						return SharedUnlockData.get().isPlayerAwareOfWeapon(spec.getWeaponId());
					}
					
			};
			parent.addChild(curr);
		}
	}
	
	public static void populateFighters(CodexEntryPlugin parent) {
		
		for (final FighterWingSpecAPI spec : Global.getSettings().getAllFighterWingSpecs()) {
			if (spec.getVariant().getHints().contains(ShipTypeHints.HIDE_IN_CODEX)) continue;
			if (spec.getTags().contains(Tags.HIDE_IN_CODEX)) continue;			
			//if (spec.getTags().contains(Tags.RESTRICTED)) continue;			
			
			ShipVariantAPI variant = spec.getVariant();
			String nameStr = variant.getHullSpec().getHullName() + " " + variant.getDisplayName();
			if (spec.hasTag(Tags.SWARM_FIGHTER)) {
				nameStr = variant.getDisplayName() + " " + variant.getHullSpec().getHullName();
			}
			if (variant.getDisplayName().isEmpty()) {
				nameStr = variant.getHullSpec().getHullName();
			}
			String nameStr2 = nameStr;
			CodexEntryV2 curr = new CodexEntryV2(getFighterEntryId(spec.getId()), 
				nameStr2, null, spec) {
					@Override
					public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
						info.addPara(nameStr2, Misc.getBasePlayerColor(), 0f);
						if (mode == ListMode.RELATED_ENTRIES || true) {
							String role = spec.getRole().name().toLowerCase();
							role = Misc.ucFirst(role);
							if (spec.getRoleDesc() != null && !spec.getRoleDesc().isEmpty()) {
								role = spec.getRoleDesc();
							}
							info.addPara(role, Misc.getGrayColor(), 0f);
						}
					}

					@Override
					public boolean matchesTags(Set<String> tags) {
						if (spec.getRole() == WingRole.FIGHTER) {
							if (!tags.contains(FIGHTER)) return false;
						} else if (spec.getRole() == WingRole.BOMBER) {
							if (!tags.contains(BOMBER)) return false;
						} else if (spec.getRole() == WingRole.INTERCEPTOR) {
							if (!tags.contains(INTERCEPTOR)) return false;
						} else {
							if (!tags.contains(OTHER)) return false;
						}
						
						String m = spec.getVariant().getHullSpec().getManufacturer();
						if (m != null && !tags.contains(m) && !tags.contains(ALL_TECHS)) return false;
						
						return true;
					}
					
					@Override
					public Set<String> getUnlockRelatedTags() {
						return spec.getTags();
					}
					
					@Override
					public boolean isUnlockedIfRequiresUnlock() {
						return SharedUnlockData.get().isPlayerAwareOfFighter(spec.getId());
					}
					
			};
			parent.addChild(curr);
		}
	}
	
	public static class GalleryEntryData implements WithSourceMod {
		public String sprite;
		public Description desc;
		public ModSpecAPI sourceMod;
		@Override
		public ModSpecAPI getSourceMod() {
			return sourceMod;
		}
	}
	public static void populateGallery(CodexEntryPlugin parent) {
		String cat = "illustrations";
		for (String key : Global.getSettings().getSpriteKeys(cat)) {
			String sprite = Global.getSettings().getSpriteName(cat, key);
			
			Description desc = Global.getSettings().getDescription(key, Type.GALLERY);
			
			if (desc.getText3().toLowerCase().contains(Tags.HIDE_IN_CODEX)) continue;
			
			if (!desc.hasText1()) continue;
			
			if (USE_KEY_NAMES_FOR_GALLERY) {
				String text = key.replaceAll("_", " ");
				text = Misc.ucFirst(text);
				desc.setText1(text);
				desc.setText2(text);
			}
			
			GalleryEntryData data = new GalleryEntryData();
			data.sprite = sprite;
			data.desc = desc;
			data.sourceMod = desc.getSourceMod();
			
			CodexEntryV2 curr = new CodexEntryV2(getGalleryEntryId(key), 
					desc.getText1(), sprite, data) {
				@Override
				public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
					info.addPara(desc.getText1(), Misc.getBasePlayerColor(), 0f);
					if (mode == ListMode.RELATED_ENTRIES) {
						info.addPara("Illustration", Misc.getGrayColor(), 0f);
					}
				}
				@Override
				public boolean isVignetteIcon() {
					return true;
				}
				@Override
				public boolean isVisible() {
					return super.isVisible();
				}
				@Override
				public boolean isLocked() {
					if (codexFullyUnlocked()) return false;
					if (SharedUnlockData.get().isPlayerAwareOfIllustration(key)) {
						return false;
					}
					return !desc.getText3().toLowerCase().contains("unlocked");
				}
			};
			parent.addChild(curr);
		}
	}
	
	public static void populateSpecialItems(CodexEntryPlugin parent) {
		List<SpecialItemSpecAPI> specs = Global.getSettings().getAllSpecialItemSpecs();
		for (final SpecialItemSpecAPI spec : specs) {
			//if (spec.hasTag(Items.TAG_BLUEPRINT_PACKAGE)) continue;
			if (spec.hasTag(Items.TAG_SINGLE_BP)) continue;
			if (spec.hasTag(Items.TAG_MODSPEC)) continue;
			if (spec.getId().equals(Items.INDUSTRY_BP)) continue;
			if (spec.hasTag(Tags.HIDE_IN_CODEX)) continue;
			
			CodexEntryV2 curr = new CodexEntryV2(getItemEntryId(spec.getId()), 
				spec.getName(), spec.getIconName(), spec) {
					@Override
					public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
						info.addPara(spec.getName(), Misc.getBasePlayerColor(), 0f);
						if (mode == ListMode.RELATED_ENTRIES) {
							info.addPara("Special item", Misc.getGrayColor(), 0f);
						}
					}

					@Override
					public boolean matchesTags(Set<String> tags) {
						boolean colony = spec.hasTag(Items.TAG_COLONY_ITEM);
						boolean bp = spec.hasTag(Items.TAG_BLUEPRINT_PACKAGE);
						if (tags.contains(OTHER) && !colony && !bp) return true;
						if (tags.contains(COLONY) && colony) return true;
						if (tags.contains(BLUEPRINTS) && bp) return true;
						return false;
					}
					
					@Override
					public Set<String> getUnlockRelatedTags() {
						return spec.getTags();
					}
					
					@Override
					public boolean isUnlockedIfRequiresUnlock() {
						return SharedUnlockData.get().isPlayerAwareOfSpecialItem(spec.getId());
					}

			};
			parent.addChild(curr);
		}
	}
	
	public static void populateCommodities(CodexEntryPlugin commodities, CodexEntryPlugin items) {
		List<CommoditySpecAPI> specs = Global.getSettings().getAllCommoditySpecs();
		for (final CommoditySpecAPI spec : specs) {
			if (spec.hasTag(Tags.HIDE_IN_CODEX)) continue;

			boolean special = spec.hasTag(Commodities.TAG_AI_CORE) || spec.hasTag(Commodities.TAG_NON_ECONOMIC);
			CodexEntryV2 curr = new CodexEntryV2(getCommodityEntryId(spec.getId()), 
				spec.getName(), spec.getIconName(), spec) {
					@Override
					public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
						info.addPara(spec.getName(), Misc.getBasePlayerColor(), 0f);
						if (mode == ListMode.RELATED_ENTRIES) {
							if (special) {
								info.addPara("Special item", Misc.getGrayColor(), 0f);
							} else {
								info.addPara("Commodity", Misc.getGrayColor(), 0f);
							}
						}
						
					}
					@Override
					public boolean matchesTags(Set<String> tags) {
						if (special) {
							boolean aiCore = spec.hasTag(Commodities.TAG_AI_CORE); 
							if (tags.contains(OTHER) && !aiCore) return true;
							if (tags.contains(AI_CORE) && aiCore) return true;
							return false;
						} else {
							return super.matchesTags(tags);
						}
					}
					
					@Override
					public Set<String> getUnlockRelatedTags() {
						return spec.getTags();
					}
					
					@Override
					public boolean isUnlockedIfRequiresUnlock() {
						return SharedUnlockData.get().isPlayerAwareOfCommodity(spec.getId());
					}

			};
			if (special) {
				items.addChild(curr);
			} else {
				commodities.addChild(curr);
			}
		}
	}

//	public static CodexEntryV2 createFactionsCategory() {
//		CodexEntryV2 cat = new CodexEntryV2(CAT_FACTIONS, "Factions", getIcon(CAT_FACTIONS)) {
//
//		};
//		return cat;
//	}
//	public static void populateFactions(CodexEntryPlugin factions) {
//		List<FactionSpecAPI> specs = Global.getSettings().getAllFactionSpecs();
//		for (final FactionSpecAPI spec : specs) {
//			if (!spec.isShowInIntelTab()) continue;
//
//			String name = Misc.ucFirst(spec.getDisplayName());
//			CodexEntryV2 curr = new CodexEntryV2(getFactionEntryId(spec.getId()), 
//					name, spec.getCrest(), spec) {
//					@Override
//					public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
//						info.addPara(name, Misc.getBasePlayerColor(), 0f);
//						if (mode == ListMode.RELATED_ENTRIES) {
//							info.addPara("Faction", Misc.getGrayColor(), 0f);
//						}
//						
//					}
//					@Override
//					public boolean isVignetteIcon() {
//						return true;
//					}
//					@Override
//					public Set<String> getUnlockRelatedTags() {
//						return super.getUnlockRelatedTags();
//					}
//					
//					@Override
//					public boolean isUnlockedIfRequiresUnlock() {
//						return super.isUnlockedIfRequiresUnlock();
//						//return SharedUnlockData.get().isPlayerAwareOfCommodity(spec.getId());
//					}
//			};
//			factions.addChild(curr);
//		}
//	}		

// doesn't work for various reasons - need icons, and terrain REALLY depends on being inside a campaign
// would need to create a parallel set of codex entries, not use the terrain plugins directly, which has its own problems 
//	public static void populateTerrain(CodexEntryPlugin terrain) {
//		List<TerrainSpecAPI> specs = Global.getSettings().getAllTerrainSpecs();
//		for (final TerrainSpecAPI spec : specs) {
//			if (spec.hasTag(Tags.HIDE_IN_CODEX)) continue;
//
//			SectorEntityToken temp = Global.getSettings().createLocationToken(0, 0);
//			CampaignTerrainPlugin plugin = spec.getNewPluginInstance(temp, null);
//			String name = Misc.ucFirst(plugin.getNameForTooltip());
//			CodexEntryV2 curr = new CodexEntryV2(getFactionEntryId(spec.getId()), 
//					name, null, spec) {
//				@Override
//				public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
//					info.addPara(name, Misc.getBasePlayerColor(), 0f);
//					if (mode == ListMode.RELATED_ENTRIES) {
//						info.addPara("Terrain", Misc.getGrayColor(), 0f);
//					}
//
//				}
//				@Override
//				public boolean isVignetteIcon() {
//					return true;
//				}
//				@Override
//				public Set<String> getUnlockRelatedTags() {
//					return super.getUnlockRelatedTags();
//				}
//
//				@Override
//				public boolean isUnlockedIfRequiresUnlock() {
//					return super.isUnlockedIfRequiresUnlock();
//					//return SharedUnlockData.get().isPlayerAwareOfCommodity(spec.getId());
//				}
//			};
//			terrain.addChild(curr);
//		}
//	}		
	
	public static void populateHullMods(CodexEntryPlugin parent) {
		List<HullModSpecAPI> specs = Global.getSettings().getAllHullModSpecs();
		for (final HullModSpecAPI spec : specs) {
			if (spec.isHiddenEverywhere()) continue;
			if (spec.hasTag(Tags.HIDE_IN_CODEX)) continue;

			CodexEntryV2 curr = new CodexEntryV2(getHullmodEntryId(spec.getId()), 
				spec.getDisplayName(), spec.getSpriteName(), spec) {
					@Override
					public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
						info.addPara(spec.getDisplayName(), Misc.getBasePlayerColor(), 0f);
						if (mode == ListMode.RELATED_ENTRIES) {
							info.addPara("Hullmod", Misc.getGrayColor(), 0f);
						}
					}
					@Override
					public boolean isVignetteIcon() {
						return true;
					}
					
					@Override
					public boolean matchesTags(Set<String> tags) {
						if (!tags.contains(spec.getManufacturer())) {
							return false;
						}
						
						if (tags.contains(ALL_TYPES)) return true;
						
						boolean hasATag = false;
						for (String tag : spec.getUITags()) {
							if (tags.contains(tag)) hasATag = true;
						}
						if (spec.hasTag(Tags.HULLMOD_DMOD) && tags.contains(DMODS)) {
							hasATag = true;
						}
						if (spec.isHidden() && !spec.hasTag(Tags.HULLMOD_DMOD) && tags.contains(INTRINSIC)) {
							hasATag = true;
						}
						if (!hasATag) return false;
						
						return true;
					}
					
					@Override
					public Set<String> getUnlockRelatedTags() {
						return spec.getTags();
					}
					
					@Override
					public boolean isUnlockedIfRequiresUnlock() {
						return SharedUnlockData.get().isPlayerAwareOfHullmod(spec.getId());
					}
			};
			
			parent.addChild(curr);
		}
	}
	
	public static void populateIndustries(CodexEntryPlugin parent) {
		List<IndustrySpecAPI> specs = Global.getSettings().getAllIndustrySpecs();
		for (final IndustrySpecAPI spec : specs) {
			if (spec.hasTag(Industries.TAG_PARENT)) continue;
			if (spec.hasTag(Tags.HIDE_IN_CODEX)) continue;
			
			CodexEntryV2 curr = new CodexEntryV2(getIndustryEntryId(spec.getId()), 
					spec.getName(), spec.getImageName(), spec) {
				@Override
				public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
					info.addPara(spec.getName(), Misc.getBasePlayerColor(), 0f);
					if (mode == ListMode.RELATED_ENTRIES || true) {
						//info.addPara("Industry", Misc.getGrayColor(), 0f);
						boolean structure = spec.hasTag(Industries.TAG_STRUCTURE);
						String type = "Industry";
						if (structure) type = "Structure";
						info.addPara(type, Misc.getGrayColor(), 0f);
					}
				}
				@Override
				public boolean isVignetteIcon() {
					return true;
				}
				@Override
				public boolean matchesTags(Set<String> tags) {
					boolean industry = spec.hasTag(Industries.TAG_INDUSTRY);
					boolean structure = spec.hasTag(Industries.TAG_STRUCTURE);
					boolean station = spec.hasTag(Industries.TAG_STATION);
					if (tags.contains(OTHER) && !industry && !structure && !station) return true;
					if (tags.contains(INDUSTRIES) && industry) return true;
					if (tags.contains(STRUCTURES) && structure && !station) return true;
					if (tags.contains(STATIONS) && station) return true;
					return false;
				}
				@Override
				public Set<String> getUnlockRelatedTags() {
					return spec.getTags();
				}
				@Override
				public boolean isUnlockedIfRequiresUnlock() {
					return SharedUnlockData.get().isPlayerAwareOfIndustry(spec.getId());
				}
			};
			parent.addChild(curr);
		}
	}
	
	public static void populateStarsAndPlanets(CodexEntryPlugin parent) {
		List<PlanetSpecAPI> specs = Global.getSettings().getAllPlanetSpecs();
		for (final PlanetSpecAPI spec : specs) {
			//if (spec.isNebulaCenter()) continue;
			if (spec.hasTag(Tags.HIDE_IN_CODEX)) continue;

			CodexEntryV2 curr = new CodexEntryV2(getPlanetEntryId(spec.getPlanetType()), 
				spec.getName(), spec.getIconTexture(), spec) {
					@Override
					public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
						info.addPara(spec.getName(), Misc.getBasePlayerColor(), 0f);
						String type = "Planet";
						if (spec.isGasGiant()) type = "Gas giant";
						else if (spec.isNebulaCenter()) type = "Nebula";
						else if (spec.isStar()) type = "Star";
						if (mode == ListMode.RELATED_ENTRIES) {
							info.addPara(type, Misc.getGrayColor(), 0f);
						}
					}
					@Override
					public boolean matchesTags(Set<String> tags) {
						if (tags.contains(ALL_TYPES)) return true;
						
						if (tags.contains(HABITABLE) && Misc.canPlanetTypeRollHabitable(spec)) {
							return true;
						}
						if (spec.isGasGiant() && !tags.contains(GAS_GIANTS)) {
							return false;
						} else if (spec.isStar() && !tags.contains(STARS)) {
							return false;
						} else if (!spec.isGasGiant() && !spec.isStar() && !tags.contains(PLANETS)) {
							return false;
						}
						
						return true;
					}
					@Override
					public Set<String> getUnlockRelatedTags() {
						return spec.getTags();
					}
					@Override
					public boolean isUnlockedIfRequiresUnlock() {
						return SharedUnlockData.get().isPlayerAwareOfPlanet(spec.getPlanetType());
					}
			};
			
			parent.addChild(curr);
		}
	}
	
	
	public static void populatePlanetaryConditions(CodexEntryPlugin parent) {
		List<MarketConditionSpecAPI> specs = Global.getSettings().getAllMarketConditionSpecs();
		for (final MarketConditionSpecAPI spec : specs) {

			if (spec.hasTag(Tags.HIDE_IN_CODEX)) continue;
			
			if (spec.getGenSpec() == null &&
					!spec.hasTag(Tags.SHOW_IN_PLANET_LIST) &&
					!spec.hasTag(Tags.SHOW_IN_CODEX)) {
				continue;
			}
			
			CodexEntryV2 curr = new CodexEntryV2(getConditionEntryId(spec.getId()), 
				spec.getName(), spec.getIcon(), spec) {
					@Override
					public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
						info.addPara(spec.getName(), Misc.getBasePlayerColor(), 0f);
						if (mode == ListMode.RELATED_ENTRIES) {
							info.addPara("Planetary condition", Misc.getGrayColor(), 0f);
						}
					}
					@Override
					public boolean isVignetteIcon() {
						return true;
					}
					
					@Override
					public boolean matchesTags(Set<String> tags) {
						boolean res = ResourceDepositsCondition.COMMODITY.containsKey(spec.getId());
						if (tags.contains(OTHER) && !res) return true;
						if (tags.contains(RESOURCES) && res) return true;
						return false;
					}
					@Override
					public Set<String> getUnlockRelatedTags() {
						return spec.getTags();
					}
					@Override
					public boolean isUnlockedIfRequiresUnlock() {
						return SharedUnlockData.get().isPlayerAwareOfCondition(spec.getId());
					}
			};
			
			parent.addChild(curr);
		}
	}
	
	
	public static void linkRelatedEntries() {
		CodexEntryPlugin ships = getEntry(CAT_SHIPS);
		CodexEntryPlugin stations = getEntry(CAT_STATIONS);
		CodexEntryPlugin fighters = getEntry(CAT_FIGHTERS);
		CodexEntryPlugin weapons = getEntry(CAT_WEAPONS);
		CodexEntryPlugin hullmods = getEntry(CAT_HULLMODS);
		CodexEntryPlugin shipSystems = getEntry(CAT_SHIP_SYSTEMS);
		CodexEntryPlugin planets = getEntry(CAT_STARS_AND_PLANETS);
		CodexEntryPlugin conditions = getEntry(CAT_PLANETARY_CONDITIONS);
		CodexEntryPlugin items = getEntry(CAT_SPECIAL_ITEMS);
		CodexEntryPlugin commodities = getEntry(CAT_COMMODITIES);
		CodexEntryPlugin industries = getEntry(CAT_INDUSTRIES);
		CodexEntryPlugin skills = getEntry(CAT_SKILLS);
		CodexEntryPlugin abilities = getEntry(CAT_ABILITIES);
		//CodexEntryPlugin factions = getEntry(CAT_FACTIONS);
		
		List<CodexEntryPlugin> shipsAndStations = new ArrayList<>();
		shipsAndStations.addAll(ships.getChildren());
		shipsAndStations.addAll(stations.getChildren());
		
		// link all ships that are based on the same hull - skins etc
		ListMap<CodexEntryPlugin> relatedHulls = new ListMap<>();
		for (CodexEntryPlugin ship : shipsAndStations) {
			if (ship.getParam() instanceof ShipHullSpecAPI) {
				ShipHullSpecAPI spec = (ShipHullSpecAPI) ship.getParam();
				String baseId = getBaseHullIdEvenIfNotRestorableTo(spec);
				relatedHulls.add(baseId, ship);
				if (spec.hasTag(Tags.DWELLER)) {
					relatedHulls.add(Tags.DWELLER + "_allParts", ship);
				}
			}
		}
		for (List<CodexEntryPlugin> list : relatedHulls.values()) {
			makeRelated(list);
		}
		
		for (CodexEntryPlugin ship : shipsAndStations) {
			if (ship.getParam() instanceof ShipHullSpecAPI) {
				ShipHullSpecAPI spec = (ShipHullSpecAPI) ship.getParam();
				
				// link ships and their systems
				String sysId = spec.getShipSystemId();
				if (sysId != null && !sysId.isBlank()) {
					String key = getShipSystemEntryId(sysId);
					CodexEntryPlugin sys = getEntry(key);
					if (sys != null) {
						sys.addRelatedEntry(ship);
						ship.addRelatedEntry(sys);
					
						// link ship systems and ships with their drones, if any
						if (sys.getParam() instanceof ShipSystemSpecAPI) {
							ShipSystemSpecAPI sysSpec = (ShipSystemSpecAPI) sys.getParam();
							if (sysSpec.getDroneVariant() != null) {
								ShipVariantAPI drone = Global.getSettings().getVariant(sysSpec.getDroneVariant());
								if (drone != null) {
									String droneHullId = getBaseHullId(drone.getHullSpec());
									String droneEntryId = getShipEntryId(droneHullId);
									makeRelated(ship.getId(), droneEntryId);
									makeRelated(sys.getId(), droneEntryId);
								}
							}
						}
					}
				}
				
				if (!spec.isPhase()) {
					sysId = spec.getShipDefenseId();
					String key = getShipSystemEntryId(sysId);
					CodexEntryPlugin sys = getEntry(key);
					if (sys != null) {
						sys.addRelatedEntry(ship);
						ship.addRelatedEntry(sys);
					}
				}
				
				String variantId = spec.getHullId() + "_Hull";
				if (spec.getCodexVariantId() != null && !spec.getCodexVariantId().isBlank()) {
					variantId = spec.getCodexVariantId();
				}
				ShipVariantAPI variant = Global.getSettings().getVariant(variantId);
				
				// link ships and their hullmods
				//for (String id : spec.getBuiltInMods()) {
				for (String id : variant.getHullMods()) {
					String key = getHullmodEntryId(id);
					CodexEntryPlugin mod = getEntry(key);
					if (mod != null) {
						mod.addRelatedEntry(ship);
						ship.addRelatedEntry(mod);
						
						if (id.equals(HullMods.VAST_HANGAR)) {
							ship.addRelatedEntry(getEntry(getHullmodEntryId(HullMods.CONVERTED_HANGAR)));
						}
					}
				}
				
				// link ships and their built-in weapons
				//for (String slotId : spec.getBuiltInWeapons().keySet()) {
				for (String slotId : variant.getFittedWeaponSlots()) {
					WeaponSlotAPI slot = spec.getWeaponSlot(slotId);
					if (slot == null || slot.isDecorative() || slot.isSystemSlot()) {
						continue;
					}
					//String id = spec.getBuiltInWeapons().get(slotId);
					String id = variant.getWeaponId(slotId);
					
					String key = getWeaponEntryId(id);
					CodexEntryPlugin weapon = getEntry(key);
					if (weapon != null) {
						weapon.addRelatedEntry(ship);
						ship.addRelatedEntry(weapon);
					}
				}

				// link ships and their built-in fighters
				//for (String id : spec.getBuiltInWings()) {
				for (String id : variant.getWings()) {
					String key = getFighterEntryId(id);
					CodexEntryPlugin fighter = getEntry(key);
					if (fighter != null) {
						fighter.addRelatedEntry(ship);
						ship.addRelatedEntry(fighter);
					}
				}
			}
		}
		
		
		
		// link fighters to their hullmods, systems, and weapons
		for (CodexEntryPlugin fighter : fighters.getChildren()) {
			if (fighter.getParam() instanceof FighterWingSpecAPI) {
				FighterWingSpecAPI wing = (FighterWingSpecAPI) fighter.getParam();
				ShipVariantAPI variant = wing.getVariant();
				ShipHullSpecAPI spec = variant.getHullSpec();
				
				// link fighters and their systems
				String sysId = spec.getShipSystemId();
				if (sysId != null) {
					String key = getShipSystemEntryId(sysId);
					CodexEntryPlugin sys = getEntry(key);
					if (sys != null) {
						sys.addRelatedEntry(fighter);
						fighter.addRelatedEntry(sys);
					}
				}
				
				if (!spec.isPhase()) {
					sysId = spec.getShipDefenseId();
					String key = getShipSystemEntryId(sysId);
					CodexEntryPlugin sys = getEntry(key);
					if (sys != null) {
						sys.addRelatedEntry(fighter);
						fighter.addRelatedEntry(sys);
					}
				}
				
				// link fighters and their hullmods
				for (String id : variant.getHullMods()) {
					String key = getHullmodEntryId(id);
					CodexEntryPlugin mod = getEntry(key);
					if (mod != null) {
						mod.addRelatedEntry(fighter);
						fighter.addRelatedEntry(mod);
					}
				}
				
				// link fighters and their weapons
				for (String slotId : variant.getFittedWeaponSlots()) {
					String id = variant.getWeaponId(slotId);
					String key = getWeaponEntryId(id);
					CodexEntryPlugin weapon = getEntry(key);
					if (weapon != null) {
						weapon.addRelatedEntry(fighter);
						fighter.addRelatedEntry(weapon);
					}
				}
			}
		}
		
		List<CodexEntryPlugin> dem = new ArrayList<>();
		// link related weapons to seach other
		for (CodexEntryPlugin weapon : weapons.getChildren()) {
			if (weapon.getParam() instanceof WeaponSpecAPI) {
				WeaponSpecAPI spec = (WeaponSpecAPI) weapon.getParam();
				// DEM missiles
				if (spec.hasTag(Tags.DAMAGE_SOFT_FLUX) && spec.hasTag(Tags.DAMAGE_SPECIAL)) {
					dem.add(weapon);
				}
			}
		}
		makeRelated(dem);
		
		
		
		ListMap<CodexEntryPlugin> relatedPlanets = new ListMap<>();
		String gasGiantListId = "gas_giant_related_id";
		
		List<CodexEntryPlugin> habitablePlanets = new ArrayList<>();
		
		// link planets variations with the same description
		for (CodexEntryPlugin planet : planets.getChildren()) {
			if (planet.getParam() instanceof PlanetSpecAPI) {
				PlanetSpecAPI spec = (PlanetSpecAPI) planet.getParam();
				String id = spec.getDescriptionId();
				if (id == null) id = spec.getPlanetType();
				
				relatedPlanets.add(id, planet);
				
				if (spec.isNebulaCenter()) {
					relatedPlanets.add("nebula_related_id", planet);
				}
				if (spec.isGasGiant()) {
					relatedPlanets.add(gasGiantListId, planet);
				}
				if (spec.isPulsar()) {
					relatedPlanets.add("pulsar_related_id", planet);
				}
				if (spec.isBlackHole()) {
					relatedPlanets.add("black_hole_related_id", planet);
				}
				if (Misc.canPlanetTypeRollHabitable(spec)) {
					habitablePlanets.add(planet);
				}
			}
		}
		for (List<CodexEntryPlugin> list : relatedPlanets.values()) {
			makeRelated(list);
		}
		makeRelated(getPlanetEntryId(Planets.PLANET_LAVA), getPlanetEntryId(Planets.PLANET_LAVA_MINOR));
		
		// link related conditions to each other
		ListMap<CodexEntryPlugin> relatedDeposits = new ListMap<>();
		for (String cid : ResourceDepositsCondition.COMMODITY.keySet()) {
			String commodityId = ResourceDepositsCondition.COMMODITY.get(cid);
			relatedDeposits.add(commodityId, getEntry(getConditionEntryId(cid)));
		}
		for (List<CodexEntryPlugin> list : relatedDeposits.values()) {
			makeRelated(list);
		}
		
		makeRelated(getConditionEntryId(Conditions.RUINS_SCATTERED),
					getConditionEntryId(Conditions.RUINS_WIDESPREAD),
					getConditionEntryId(Conditions.RUINS_EXTENSIVE),
					getConditionEntryId(Conditions.RUINS_VAST));
		makeRelated(getConditionEntryId(Conditions.NO_ATMOSPHERE),
					getConditionEntryId(Conditions.THIN_ATMOSPHERE),
					getConditionEntryId(Conditions.TOXIC_ATMOSPHERE),
					getConditionEntryId(Conditions.DENSE_ATMOSPHERE));
		makeRelated(getConditionEntryId(Conditions.DECIVILIZED),
					getConditionEntryId(Conditions.DECIVILIZED_SUBPOP));
		makeRelated(getConditionEntryId(Conditions.COLD),
					getConditionEntryId(Conditions.VERY_COLD));
		makeRelated(getConditionEntryId(Conditions.HOT),
					getConditionEntryId(Conditions.VERY_HOT));
		makeRelated(getConditionEntryId(Conditions.LOW_GRAVITY),
					getConditionEntryId(Conditions.HIGH_GRAVITY));
		makeRelated(getConditionEntryId(Conditions.POOR_LIGHT),
					getConditionEntryId(Conditions.DARK));		
		makeRelated(getConditionEntryId(Conditions.HABITABLE),
					getConditionEntryId(Conditions.MILD_CLIMATE));		
		makeRelated(getConditionEntryId(Conditions.TECTONIC_ACTIVITY),
					getConditionEntryId(Conditions.EXTREME_TECTONIC_ACTIVITY));		
		
		// link Habitable to all planets that can roll it
		CodexEntryPlugin habitable = getEntry(getConditionEntryId(Conditions.HABITABLE));
		for (CodexEntryPlugin planet : habitablePlanets) {
			makeRelated(habitable, planet);
		}
		
		
//		for (CodexEntryPlugin curr : planets.getChildren()) {
//			if (curr.getParam() instanceof MarketConditionSpecAPI) {
//				MarketConditionSpecAPI spec = (MarketConditionSpecAPI) curr.getParam();
//			}
//		}
		
		
		// link all commmodities sharing a demand class - AI cores, survey data, luxury goods/lobster
		ListMap<CodexEntryPlugin> commoditiesByDemandClass = new ListMap<>(); 
		for (CodexEntryPlugin item : items.getChildren()) {
			if (item.getParam() instanceof CommoditySpecAPI) {
				CommoditySpecAPI spec = (CommoditySpecAPI) item.getParam();
				commoditiesByDemandClass.add(spec.getDemandClass(), item);
			}
		}
		for (CodexEntryPlugin item : commodities.getChildren()) {
			if (item.getParam() instanceof CommoditySpecAPI) {
				CommoditySpecAPI spec = (CommoditySpecAPI) item.getParam();
				commoditiesByDemandClass.add(spec.getDemandClass(), item);
			}
		}
		for (List<CodexEntryPlugin> list : commoditiesByDemandClass.values()) {
			makeRelated(list);
		}
		
		
		// link colony items to related conditions (and gas giants)
		// link blueprint packages to contents
		List<CodexEntryPlugin> gasGiants = relatedPlanets.getList(gasGiantListId);
		for (CodexEntryPlugin item : items.getChildren()) {
			if (item.getParam() instanceof SpecialItemSpecAPI) {
				SpecialItemSpecAPI spec = (SpecialItemSpecAPI) item.getParam();
				if (spec.hasTag(Items.TAG_COLONY_ITEM)) {
					InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(spec.getId());
					Set<String> relatedConditions = effect.getConditionsRelatedToRequirements(null);
					for (String conditionId : relatedConditions) {
						CodexEntryPlugin condition = getEntry(getConditionEntryId(conditionId));
						if (condition != null) {
							item.addRelatedEntry(condition);
							condition.addRelatedEntry(item);
						}
					}
					List<String> req = effect.getRequirements(null);
					if (req != null && (req.contains(ItemEffectsRepo.GAS_GIANT) ||
							req.contains(ItemEffectsRepo.NOT_A_GAS_GIANT))) {
						for (CodexEntryPlugin gasGiant : gasGiants) {
							item.addRelatedEntry(gasGiant);
							gasGiant.addRelatedEntry(item);
						}
					}
				} else if (spec.hasTag(Items.TAG_BLUEPRINT_PACKAGE)) {
					SpecialItemPlugin plugin = spec.getNewPluginInstance(null);
					plugin.init(null);
					if (plugin instanceof MultiBlueprintItemPlugin) {
						MultiBlueprintItemPlugin multi = (MultiBlueprintItemPlugin) plugin;
						for (String shipId : multi.getProvidedShips()) {
							makeRelated(item.getId(), getShipEntryId(shipId));
						}
						for (String fighterId : multi.getProvidedFighters()) {
							makeRelated(item.getId(), getFighterEntryId(fighterId));
						}
						for (String weaponId : multi.getProvidedWeapons()) {
							makeRelated(item.getId(), getWeaponEntryId(weaponId));
						}
					}
				}
			} else if (item.getParam() instanceof CommoditySpecAPI) {
				CommoditySpecAPI spec = (CommoditySpecAPI) item.getParam();
				// TODO - anything ???
			}
		}
		
		
		// link colony items to industries
		for (CodexEntryPlugin curr : items.getChildren()) {
			if (curr.getParam() instanceof SpecialItemSpecAPI) {
				SpecialItemSpecAPI spec = (SpecialItemSpecAPI) curr.getParam();
				if (spec.hasTag(Items.TAG_COLONY_ITEM)) {
					for (String industryId : spec.getParams().split(",")) {
						makeRelated(curr.getId(), getIndustryEntryId(industryId.trim()));
					}
				}
			}
		}
		
		
		ListMap<String> commodityToResourceConditions = new ListMap<>();
		for (String condId : ResourceDepositsCondition.COMMODITY.keySet()) {
			String commodityId = ResourceDepositsCondition.COMMODITY.get(condId);
			commodityToResourceConditions.add(commodityId, condId);
		}
		
		for (CodexEntryPlugin curr : industries.getChildren()) {
			if (curr.getParam() instanceof IndustrySpecAPI) {
				IndustrySpecAPI spec = (IndustrySpecAPI) curr.getParam();
				
				// link industries to commodities they produce/demand
				for (String comId : Global.getSettings().getIndustryDemand(spec.getId())) {
					makeRelated(curr.getId(), getCommodityEntryId(comId));
				}
				for (String comId : Global.getSettings().getIndustrySupply(spec.getId())) {
					makeRelated(curr.getId(), getCommodityEntryId(comId));
					if (!spec.getId().equals(Industries.AQUACULTURE)) {
						for (String condId : commodityToResourceConditions.get(comId)) {
							makeRelated(curr.getId(), getConditionEntryId(condId));
						}
					}
				}
				
				// link industries to their entire upgrade chain
				String otherId = spec.getUpgrade();
				Set<String> seen = new HashSet<>(); // handle circular upgrade chain
				while (otherId != null && !seen.contains(otherId)) {
					seen.add(otherId);
					makeRelated(curr.getId(), getIndustryEntryId(otherId));
					IndustrySpecAPI other = Global.getSettings().getIndustrySpec(otherId);
					otherId = other.getUpgrade();
				}
				otherId = spec.getDowngrade();
				while (otherId != null && !seen.contains(otherId)) {
					seen.add(otherId);
					makeRelated(curr.getId(), getIndustryEntryId(otherId));
					IndustrySpecAPI other = Global.getSettings().getIndustrySpec(otherId);
					otherId = other.getDowngrade();
				}
			}
		}
		
		makeRelated(getIndustryEntryId(Industries.AQUACULTURE), getConditionEntryId(Conditions.WATER_SURFACE));
		makeRelated(getIndustryEntryId(Industries.AQUACULTURE), getPlanetEntryId(Planets.PLANET_WATER));
		makeRelated(getIndustryEntryId(Industries.AQUACULTURE), getConditionEntryId(Conditions.VOLTURNIAN_LOBSTER_PENS));
		makeRelated(getIndustryEntryId(Industries.AQUACULTURE), getIndustryEntryId(Industries.FARMING));
		makeUnrelated(getIndustryEntryId(Industries.FARMING), getConditionEntryId(Conditions.VOLTURNIAN_LOBSTER_PENS));
		makeUnrelated(getIndustryEntryId(Industries.FARMING), getConditionEntryId(Conditions.WATER_SURFACE));
		makeUnrelated(getIndustryEntryId(Industries.FARMING), getCommodityEntryId(Commodities.LOBSTER));
		makeRelated(getConditionEntryId(Conditions.WATER_SURFACE), getPlanetEntryId(Planets.PLANET_WATER));
		
		// link commodities with their resource conditions
		for (String comId : commodityToResourceConditions.keySet()) {
			for (String condId : commodityToResourceConditions.get(comId)) {
				makeRelated(getCommodityEntryId(comId), getConditionEntryId(condId));
			}
		}
		
		// link weapons and related hullmods; fairly manual process, may comment this out later
		// "look here are all the missile weapons in the game in related entries" is not very helpful
		// so, try to limit it to smaller subsets that are more difficult to obtain using just tags
		// and/or just less obvious
		for (CodexEntryPlugin weapon : weapons.getChildren()) {
			if (weapon.getParam() instanceof WeaponSpecAPI) {
				WeaponSpecAPI spec = (WeaponSpecAPI) weapon.getParam();
				if (spec.getAIHints().contains(AIHints.SYSTEM) &&
						spec.getPrimaryRoleStr() != null &&
						spec.getPrimaryRoleStr().endsWith("(Fighter)")) {
					continue;
				}
				String id = weapon.getId();
				
				if (spec.usesAmmo() &&
						(spec.getType() == WeaponType.BALLISTIC || spec.getType() == WeaponType.ENERGY)) {
					makeRelated(id, getHullmodEntryId(HullMods.MAGAZINES));
				}
				if (spec.usesAmmo() && spec.getAmmoPerSecond() <= 0 && spec.getType() == WeaponType.MISSILE) {
					//makeRelated(id, getHullModEntryId(HullMods.MISSLERACKS));
					if (spec.getSize() == WeaponSize.SMALL) {
						makeRelated(id, getHullmodEntryId(HullMods.MISSILE_AUTOLOADER));
					}
				}
//				if (spec.isBeam()) {
//					makeRelated(id, getHullModEntryId(HullMods.ADVANCEDOPTICS));
//					makeRelated(id, getHullModEntryId(HullMods.HIGH_SCATTER_AMP));
//				}
				//if (spec.getMountType() == WeaponType.BALLISTIC || spec.getMountType() == WeaponType.HYBRID) {
				if (spec.getMountType() == WeaponType.HYBRID && spec.getSize() != WeaponSize.LARGE && 
						!spec.getAIHints().contains(AIHints.PD)) {
					makeRelated(id, getHullmodEntryId(HullMods.BALLISTIC_RANGEFINDER));
				}
				
//				if (!spec.isBeam() && 
//						(spec.getType() == WeaponType.ENERGY || spec.getType() == WeaponType.HYBRID)) {
//					makeRelated(id, getHullModEntryId(HullMods.COHERER));
//				}
			}
		}
		
		for (CodexEntryPlugin skill : skills.getChildren()) {
			if (skill.getParam() instanceof SkillSpecAPI) {
				String id = skill.getId();
				SkillSpecAPI spec = (SkillSpecAPI) skill.getParam();
				
				// link skill to unlocked hullmods
				for (String hullmodId : spec.getAllHullmodUnlocks()) {
					makeRelated(id, getHullmodEntryId(hullmodId));
				}
				
				// link skill to unlocked abilities
				for (String abilityId : spec.getAllAbilityUnlocks()) {
					makeRelated(id, getAbilityEntryId(abilityId));
				}
				
				// link AI-only skills to AI cores
				if (spec.hasTag(Skills.TAG_AI_CORE_ONLY)) {
					if (spec.isAdminSkill()) {
						makeRelated(id, getCommodityEntryId(Commodities.ALPHA_CORE));
					} else {
						makeRelated(id, getCommodityEntryId(Commodities.ALPHA_CORE));
						makeRelated(id, getCommodityEntryId(Commodities.BETA_CORE));
						makeRelated(id, getCommodityEntryId(Commodities.GAMMA_CORE));
					}
				}
			}
		}
		
		
		for (CodexEntryPlugin hullmod : hullmods.getChildren()) {
			if (hullmod.getParam() instanceof HullModSpecAPI) {
				HullModSpecAPI spec = (HullModSpecAPI) hullmod.getParam();
				
				if (spec.hasTag(Tags.HULLMOD_DMOD)) {
					makeRelated(getSkillEntryId(Skills.DERELICT_CONTINGENT), getHullmodEntryId(spec.getId()));
					if (spec.hasTag(Tags.HULLMOD_DAMAGE)) {
						makeRelated(getSkillEntryId(Skills.HULL_RESTORATION), getHullmodEntryId(spec.getId()));
					}
				}
				
				CargoStackAPI req = spec.getEffect().getRequiredItem();
				if (req != null) {
					if (req.getType() == CargoItemType.RESOURCES) {
						makeRelated(getHullmodEntryId(spec.getId()), getCommodityEntryId(req.getCommodityId()));
					} else if (req.getType() == CargoItemType.SPECIAL) {
						makeRelated(getHullmodEntryId(spec.getId()), getItemEntryId(req.getSpecialItemSpecIfSpecial().getId()));
					}
				}
			}
		}
		
		for (CodexEntryPlugin curr : shipSystems.getChildren()) {
			if (curr.getParam() instanceof ShipSystemSpecAPI) {
				ShipSystemSpecAPI spec = (ShipSystemSpecAPI) curr.getParam();
				if (spec.getStatsScript() instanceof EnergyLashActivatedSystem) {
					makeRelated(getShipSystemEntryId(ShipSystems.ENERGY_LASH), getShipSystemEntryId(spec.getId()));
				}
			}
		}
		
		
		// add some custom links between skills/abilities/hullmods/etc
		makeRelated(getSkillEntryId(Skills.AUTOMATED_SHIPS), getHullmodEntryId(HullMods.AUTOMATED));
		makeRelated(getSkillEntryId(Skills.PHASE_CORPS), getHullmodEntryId(HullMods.PHASE_FIELD));
		makeRelated(getSkillEntryId(Skills.CONTAINMENT_PROCEDURES), getAbilityEntryId(Abilities.EMERGENCY_BURN));
		makeRelated(getSkillEntryId(Skills.NAVIGATION), getAbilityEntryId(Abilities.SUSTAINED_BURN));
		makeRelated(getSkillEntryId(Skills.SENSORS), getAbilityEntryId(Abilities.GO_DARK));
		makeRelated(getSkillEntryId(Skills.SENSORS), getAbilityEntryId(Abilities.SENSOR_BURST));
		makeRelated(getAbilityEntryId(Abilities.INTERDICTION_PULSE), getAbilityEntryId(Abilities.SENSOR_BURST));
		makeRelated(getAbilityEntryId(Abilities.INTERDICTION_PULSE), getAbilityEntryId(Abilities.SUSTAINED_BURN));
		makeRelated(getAbilityEntryId(Abilities.INTERDICTION_PULSE), getAbilityEntryId(Abilities.EMERGENCY_BURN));
		makeRelated(getAbilityEntryId(Abilities.INTERDICTION_PULSE), getAbilityEntryId(Abilities.TRANSVERSE_JUMP));
		
		makeRelated(getSkillEntryId(Skills.TACTICAL_DRILLS), getCommodityEntryId(Commodities.MARINES));
		makeRelated(getHullmodEntryId(HullMods.GROUND_SUPPORT), getCommodityEntryId(Commodities.MARINES));
		makeRelated(getHullmodEntryId(HullMods.ADVANCED_GROUND_SUPPORT), getCommodityEntryId(Commodities.MARINES));
		
		makeRelated(getHullmodEntryId(HullMods.NEURAL_INTEGRATOR), getHullmodEntryId(HullMods.AUTOMATED));
		makeRelated(getSkillEntryId(Skills.NEURAL_LINK), getHullmodEntryId(HullMods.AUTOMATED));
		
		makeRelated(getSkillEntryId(Skills.NEURAL_LINK), getHullmodEntryId(HullMods.AUTOMATED));
		
		// phase skimmer and degraded phase skimmer
		makeRelated(getShipSystemEntryId("displacer"), getShipSystemEntryId("displacer_degraded"));
		
		// relate terminator drone and termination sequence
		makeRelated(getFighterEntryId("terminator_wing"), getShipSystemEntryId("drone_strike"));
				
		makeRelated(getHullmodEntryId(HullMods.VAST_HANGAR), getHullmodEntryId(HullMods.CONVERTED_HANGAR));
		makeRelated(getHullmodEntryId(HullMods.DESIGN_COMPROMISES), getHullmodEntryId(HullMods.CONVERTED_HANGAR));

		// tech mining and all ruins
		makeRelated(getIndustryEntryId(Industries.TECHMINING), getConditionEntryId(Conditions.RUINS_SCATTERED));
		makeRelated(getIndustryEntryId(Industries.TECHMINING), getConditionEntryId(Conditions.RUINS_WIDESPREAD));
		makeRelated(getIndustryEntryId(Industries.TECHMINING), getConditionEntryId(Conditions.RUINS_EXTENSIVE));
		makeRelated(getIndustryEntryId(Industries.TECHMINING), getConditionEntryId(Conditions.RUINS_VAST));
		
		
		String substrateEntryId = getItemEntryId(Items.SHROUDED_SUBSTRATE);
		CodexEntryPlugin substrateEntry = getEntry(substrateEntryId);
		if (substrateEntry != null) {
			for (CodexEntryPlugin dwellerPart : relatedHulls.get(Tags.DWELLER + "_allParts")) {
				makeRelated(substrateEntry, dwellerPart);
			}
		}
		makeRelated(getWeaponEntryId("vortex_launcher"), getShipEntryId("shrouded_vortex"));
		
//		// link factions to things they sell
//		for (CodexEntryPlugin curr : factions.getChildren()) {
//			if (curr.getParam() instanceof FactionSpecAPI) {
//				FactionSpecAPI spec = (FactionSpecAPI) curr.getParam();
//				String id = curr.getId();
//				
//				// weapons
//				for (String key : spec.getWeaponSellFrequency().keySet()) {
//					Float val = spec.getWeaponSellFrequency().get(key);
//					if (val > 1f) {
//						makeRelated(id, getWeaponEntryId(key));
//					}
//				}
//				
//				// fighters
//				for (String key : spec.getFighterSellFrequency().keySet()) {
//					Float val = spec.getFighterSellFrequency().get(key);
//					if (val > 1f) {
//						makeRelated(id, getFighterEntryId(key));
//					}
//				}
//				
//				// hullmods
//				for (String key : spec.getHullmodSellFrequency().keySet()) {
//					Float val = spec.getHullmodSellFrequency().get(key);
//					if (val > 1f) {
//						makeRelated(id, getHullmodEntryId(key));
//					}
//				}
//			}
//		}
	}
	
	
	public static void makeRelated(CodexEntryPlugin ... plugins) {
		for (CodexEntryPlugin one : plugins) {
			for (CodexEntryPlugin two : plugins) {
				if (one == two) continue;
				one.addRelatedEntry(two);
				two.addRelatedEntry(one);
			}
		}
	}
	public static void makeRelated(List<CodexEntryPlugin> plugins) {
		for (CodexEntryPlugin one : plugins) {
			for (CodexEntryPlugin two : plugins) {
				if (one == two) continue;
				one.addRelatedEntry(two);
				two.addRelatedEntry(one);
			}
		}
	}
	public static void makeRelated(String ... ids) {
		for (String id1 : ids) {
			CodexEntryPlugin one = getEntry(id1);
			if (one == null) continue;
			for (String id2 : ids) {
				if (id1 == id2) continue;
				CodexEntryPlugin two = getEntry(id2);
				if (two == null) continue;
				one.addRelatedEntry(two);
				two.addRelatedEntry(one);
			}
		}
	}
	public static void makeUnrelated(String ... ids) {
		for (String id1 : ids) {
			CodexEntryPlugin one = getEntry(id1);
			if (one == null) continue;
			for (String id2 : ids) {
				if (id1 == id2) continue;
				CodexEntryPlugin two = getEntry(id2);
				if (two == null) continue;
				one.removeRelatedEntry(two);
				two.removeRelatedEntry(one);
			}
		}
	}
	
	public static String getBaseHullIdEvenIfNotRestorableTo(ShipHullSpecAPI spec) {
		String baseId = spec.getBaseHullId();
		return baseId;		
	}
	
	public static String getBaseHullId(ShipHullSpecAPI spec) {
		ShipHullSpecAPI base = spec.getDParentHull();
		
		if (!spec.isDefaultDHull() && !spec.isRestoreToBase()) {
			base = spec;
		}
		
		if (base == null && spec.isRestoreToBase()) {
			base = spec.getBaseHull();
		}
		if (base == null) {
			base = spec;
		}
		
		return base.getHullId();
	}
	
	public static String getFleetMemberEntryId(FleetMemberAPI member) {
		return getShipEntryId(getFleetMemberBaseHullId(member));
	}
	
	public static String getFleetMemberBaseHullId(FleetMemberAPI member) {
		ShipHullSpecAPI spec = member.getHullSpec();
		ShipHullSpecAPI base = spec.getDParentHull();
		
		if (!spec.isDefaultDHull() && !spec.isRestoreToBase()) {
			base = spec;
		}
		
		if (base == null && spec.isRestoreToBase()) {
			base = spec.getBaseHull();
		}
		if (base == null) {
			base = spec;
		}
		
		return base.getHullId();
	}
	
	
	public static String getShipEntryId(String shipId) {
		return "codex_hull_" + shipId;
	}
	public static String getWeaponEntryId(String weaponId) {
		return "codex_weapon_" + weaponId;
	}
	public static String getFighterEntryId(String wingId) {
		return "codex_fighter_" + wingId;
	}
	public static String getShipSystemEntryId(String shipSystemId) {
		return "codex_system_" + shipSystemId;
	}
	public static String getHullmodEntryId(String hullModId) {
		return "codex_hullmod_" + hullModId;
	}
	public static String getPlanetEntryId(String planetId) {
		return "codex_planet_" + planetId;
	}
	public static String getConditionEntryId(String conditionId) {
		return "codex_condition_" + conditionId;
	}
	public static String getItemEntryId(String itemId) {
		return "codex_item_" + itemId;
	}
	public static String getIndustryEntryId(String industryId) {
		return "codex_industry_" + industryId;
	}
	public static String getCommodityEntryId(String commodityId) {
		return "codex_commodity_" + commodityId;
	}
	public static String getFactionEntryId(String factionId) {
		return "codex_faction_" + factionId;
	}
	public static String getMechanicEntryId(String mechanicId) {
		return "codex_mechanic_" + mechanicId;
	}
	public static String getGalleryEntryId(String galleryId) {
		return "codex_gallery_" + galleryId;
	}
	public static String getSkillEntryId(String skillId) {
		return "codex_skill_" + skillId;
	}
	public static String getAbilityEntryId(String abilityId) {
		return "codex_ability_" + abilityId;
	}
	
	public static String getIcon(String key) {
		return Global.getSettings().getSpriteName("codex", key);
	}
	
	public static CodexEntryPlugin getEntry(String id) {
		return ENTRIES.get(id);
	}
	
	public static void rebuildIdToEntryMap() {
		ENTRIES.clear();
		if (ROOT == null) return;
		
		ENTRIES.put(ROOT.getId(), ROOT);
		rebuildIdToEntryMap(ROOT);
	}
	
	public static void rebuildIdToEntryMap(CodexEntryPlugin curr) {
		for (CodexEntryPlugin child : curr.getChildren()) {
			ENTRIES.put(child.getId(), child);
			if (!child.getChildren().isEmpty()) {
				rebuildIdToEntryMap(child);
			}
		}
	}
	
	public static boolean codexFullyUnlocked() {
//		if (Global.getSettings().isDevMode() && !Global.getSettings().getBoolean("playtestingMode")) {
//			return true;
//		}
//		if (true) return true;
		return Global.getSettings().getBoolean("allCodexEntriesUnlocked");
	}
	
	
	
	/**
	 * Returns a list because it could be a station or a ship with modules.
	 * @param member
	 * @return
	 */
	public static List<CodexEntryPlugin> createTempFleetMemberEntry(FleetMemberAPI member) {
		ShipHullSpecAPI spec = member.getHullSpec();
		ShipVariantAPI variant = member.getVariant();
		String entryId = UUID.randomUUID().toString();
		
		boolean station = spec.getHints().contains(ShipTypeHints.STATION);
		boolean limited = variant.hasTag(Tags.SHIP_LIMITED_TOOLTIP);
		if (!SharedUnlockData.get().isPlayerAwareOfShip(variant.getHullSpec().getRestoredToHullId())) {
			limited |= variant.hasTag(Tags.LIMITED_TOOLTIP_IF_LOCKED);
			limited |= member.getHullSpec().hasTag(Tags.LIMITED_TOOLTIP_IF_LOCKED);
		}
		boolean limited2 = limited;
		
		
		Description desc = Global.getSettings().getDescription(spec.getDescriptionId(), Type.SHIP);
		String entryName = variant.getFullDesignationWithHullNameForShip();
		if (!station && member.getShipName() != null && !member.getShipName().isBlank() && !member.isFighterWing()) {
			entryName = member.getShipName() + ", " + entryName;
		}
		if (limited) entryName = desc.getText2();
		
		CodexEntryV2 entry = new CodexEntryV2(entryId, entryName, null, member) {
				@Override
				public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
					//info.addPara(spec.getHullName(), Misc.getBasePlayerColor(), 0f);
					//info.addPara(member.getShipName() + " (" + spec.getHullName() + ")", Misc.getBasePlayerColor(), 0f);
					String name = member.getShipName();
					if (name == null || name.isBlank()) {
						name = spec.getHullName();
					}
					info.addPara(name, Misc.getBasePlayerColor(), 0f);
					if (limited2) {
						info.addPara(Misc.ucFirst(desc.getText2().toLowerCase()), Misc.getGrayColor(), 0f);
					} else if (spec.hasDesignation() && !spec.getDesignation().equals(spec.getHullName())) {
						//info.addPara(Misc.ucFirst(variant.getFullDesignationForShip().toLowerCase()), Misc.getGrayColor(), 0f);
						//info.addPara(Misc.ucFirst(spec.getHullNameWithDashClass().toLowerCase()), Misc.getGrayColor(), 0f);
						info.addPara(Misc.ucFirst(variant.getFullDesignationForShip().toLowerCase()), Misc.getGrayColor(), 0f);
					} else if (spec.getHints().contains(ShipTypeHints.STATION)) {
						info.addPara("Station", Misc.getGrayColor(), 0f);
					} else if (spec.getHints().contains(ShipTypeHints.MODULE)) {
						info.addPara("Module", Misc.getGrayColor(), 0f);
					}
				}
				@Override
				public boolean matchesTags(Set<String> tags) {
					return false;
				}
				@Override
				public boolean isVisible() {
					// only shows up in related lists because not added to its own parent category
					return true;
				}
				@Override
				public boolean isLocked() {
					return false;
				}
		};
		
		linkFleetMemberEntryToRelated(entry, member, true);
		
		CodexEntryPlugin parent = getEntry(CAT_SHIPS);
		if (station) parent = getEntry(CAT_STATIONS);
		entry.setParent(parent);
		
		List<CodexEntryPlugin> result = new ArrayList<>();
		result.add(entry);
		
		List<CodexEntryPlugin> modules = addModulesForVariant(member.getVariant(), false, entry, parent);
		result.addAll(modules);
		
		return result;
		
	}
	
	/**
	 * This method assumes the entry is a specific fleet member with a loadout, and NOT an empty hull.
	 * Meaning, this entry was added on the fly such as e.g. from a tooltip of a fleet member.
	 * The linking for empty hull entries is done in the linkRelatedEntries() method.
	 * 
	 * @param entry
	 * @param member
	 * @param parentOfModuleEntry
	 * @param parentOfModule
	 */
	public static void linkFleetMemberEntryToRelated(CodexEntryPlugin entry, 
				FleetMemberAPI member, boolean linkCaptainSkills) {
		ShipHullSpecAPI spec = member.getHullSpec();
		ShipVariantAPI variant = member.getVariant();
		CodexEntryPlugin hullEntry = getEntry(getFleetMemberEntryId(member));
		if (hullEntry != null) {
			for (CodexEntryPlugin rel : hullEntry.getRelatedEntries()) {
				if (rel.hasTag(TAG_EMPTY_MODULE)) continue;
//				if (rel.getId().equals("codex_hull_station3")) {
//					System.out.println("Relid: " + rel.getId());
//				}
				// skip all entries linking modules back to their empty hull
				// since we'll be linking to a non-empty hull
				// or, if we're viewing a module directly, don't link to parent hull at all
				// if getParam2() is a fleet member, we don't want to link to it
				if (rel.getParam2() instanceof FleetMemberAPI) {
					continue;
//					FleetMemberAPI otherMember = (FleetMemberAPI) rel.getParam2();
//					String h1 = getFleetMemberBaseHullId(otherMember);
//					String h2 = getFleetMemberBaseHullId(parentOfModule);
//					if (h1.equals(h2)) continue;
				}
				entry.addRelatedEntry(rel);
				rel.addRelatedEntry(entry);
			}
		}
		
		String sysId = spec.getShipSystemId();
		if (sysId != null && !sysId.isBlank()) {
			String key = getShipSystemEntryId(sysId);
			CodexEntryPlugin sys = getEntry(key);
			if (sys != null) {
				sys.addRelatedEntry(entry);
				entry.addRelatedEntry(sys);
			
				// link ship systems and ships with their drones, if any
				if (sys.getParam() instanceof ShipSystemSpecAPI) {
					ShipSystemSpecAPI sysSpec = (ShipSystemSpecAPI) sys.getParam();
					if (sysSpec.getDroneVariant() != null) {
						ShipVariantAPI drone = Global.getSettings().getVariant(sysSpec.getDroneVariant());
						if (drone != null) {
							String droneHullId = getBaseHullId(drone.getHullSpec());
							String droneEntryId = getShipEntryId(droneHullId);
							makeRelated(entry.getId(), droneEntryId);
							makeRelated(sys.getId(), droneEntryId);
						}
					}
				}
			}
		}
		
		
		for (String slotId : variant.getFittedWeaponSlots()) {
			String wid = variant.getWeaponId(slotId);
			CodexEntryPlugin other = getEntry(getWeaponEntryId(wid));
			if (other != null) {
				entry.addRelatedEntry(other);
				other.addRelatedEntry(entry);
			}
		}
		for (String wingId : variant.getFittedWings()) {
			CodexEntryPlugin other = getEntry(getFighterEntryId(wingId));
			if (other != null) {
				entry.addRelatedEntry(other);
				other.addRelatedEntry(entry);
			}
		}
		for (String hullmodId : variant.getHullMods()) {
			CodexEntryPlugin other = getEntry(getHullmodEntryId(hullmodId));
			if (other != null) {
				entry.addRelatedEntry(other);
				other.addRelatedEntry(entry);
			}
		}
		
		if (linkCaptainSkills && member.getCaptain() != null && !member.getCaptain().isDefault()) {
			for (SkillLevelAPI sl : member.getCaptain().getStats().getSkillsCopy()) {
				if (sl.getLevel() > 0 && sl.getSkill().isCombatOfficerSkill()) {
					CodexEntryPlugin other = getEntry(getSkillEntryId(sl.getSkill().getId()));
					if (other != null) {
						entry.addRelatedEntry(other);
						other.addRelatedEntry(entry);
					}
				}
			}
		}
	}
	
	public static boolean hasUnlockedEntry(String entryId) {
		CodexEntryPlugin entry = getEntry(entryId);
		return entry != null && !entry.isLocked();
	}
	
	public static boolean hasUnlockedEntryForShip(String hullId) {
		CodexEntryPlugin entry = getEntry(getShipEntryId(hullId));
		return entry != null && !entry.isLocked();
	}
	
	public static void unlinkAndRemoveTempEntry(CodexEntryPlugin entry) {
		if (entry == null) return;
		for (CodexEntryPlugin rel : entry.getRelatedEntries()) {
			rel.removeRelatedEntry(entry.getId());
			entry.removeRelatedEntry(rel.getId());
		}
		ENTRIES.remove(entry.getId());
		
		Set<String> remove = new LinkedHashSet<>();
		for (String id : SEEN_STATION_MODULES.keySet()) {
			if (SEEN_STATION_MODULES.get(id) == entry) {
				remove.add(id);
			}
		}
		for (String id : remove) {
			SEEN_STATION_MODULES.remove(id);
		}
		
		if (entry.getParent() != null) {
			entry.getParent().getChildren().remove(entry);
		}
	}
}










