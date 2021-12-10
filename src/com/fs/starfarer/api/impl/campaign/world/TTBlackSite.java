package com.fs.starfarer.api.impl.campaign.world;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.NascentGravityWellAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.CoreLifecyclePluginImpl;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.WarningBeaconEntityPlugin;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager.RemnantFleetInteractionConfigGen;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;

public class TTBlackSite {

	public static String HAMATSU_ID = "hamatsu";
	
	public static String NASCENT_WELL_KEY = "$ttBlackSite_well";
	public static String DEFEATED_ZIGGURAT_KEY = "$defeatedZiggurat";
	
	public static class ZigFIDConfig implements FIDConfigGen {
		public FIDConfig createConfig() {
			FIDConfig config = new FIDConfig();
			
//			config.alwaysAttackVsAttack = true;
//			config.leaveAlwaysAvailable = true;
//			config.showFleetAttitude = false;
			config.showTransponderStatus = false;
			config.showEngageText = false;
			config.alwaysPursue = true;
			config.dismissOnLeave = false;
			//config.lootCredits = false;
			config.withSalvage = false;
			//config.showVictoryText = false;
			config.printXPToDialog = true;
			
			config.noSalvageLeaveOptionText = "Continue";
//			config.postLootLeaveOptionText = "Continue";
//			config.postLootLeaveHasShortcut = false;
			
			config.delegate = new BaseFIDDelegate() {
				public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
					new RemnantFleetInteractionConfigGen().createConfig().delegate.
								postPlayerSalvageGeneration(dialog, context, salvage);
				}
				public void notifyLeave(InteractionDialogAPI dialog) {
					
					SectorEntityToken other = dialog.getInteractionTarget();
					if (!(other instanceof CampaignFleetAPI)) {
						dialog.dismiss();
						return;
					}
					CampaignFleetAPI fleet = (CampaignFleetAPI) other;
					
					if (!fleet.isEmpty()) {
						dialog.dismiss();
						return;
					}
					
					Global.getSector().getMemoryWithoutUpdate().set(DEFEATED_ZIGGURAT_KEY, true);
					
					PerShipData ship = new PerShipData("ziggurat_Hull", ShipCondition.WRECKED, 0f);
					ship.shipName = "TTS Xenorphica";
					DerelictShipData params = new DerelictShipData(ship, false);
					CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
												fleet.getContainingLocation(),
												Entities.WRECK, Factions.NEUTRAL, params);
					Misc.makeImportant(entity, "ziggurat");
					entity.getMemoryWithoutUpdate().set("$ziggurat", true);
					
					entity.getLocation().x = fleet.getLocation().x + (50f - (float) Math.random() * 100f);
					entity.getLocation().y = fleet.getLocation().y + (50f - (float) Math.random() * 100f);
			
					ShipRecoverySpecialData data = new ShipRecoverySpecialData(null);
					data.notNowOptionExits = true;
					data.noDescriptionText = true;
					DerelictShipEntityPlugin dsep = (DerelictShipEntityPlugin) entity.getCustomPlugin();
					PerShipData copy = (PerShipData) dsep.getData().ship.clone();
					copy.variant = Global.getSettings().getVariant(copy.variantId).clone();
					copy.variantId = null;
					copy.variant.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
					copy.variant.addTag(Tags.SHIP_UNIQUE_SIGNATURE);
					data.addShip(copy);
					
					Misc.setSalvageSpecial(entity, data);
					
					dialog.setInteractionTarget(entity);
					RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl("AfterZigguratDefeat");
					dialog.setPlugin(plugin);
					plugin.init(dialog);
				}
				
				public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
					bcc.aiRetreatAllowed = false;
					bcc.objectivesAllowed = false;
					bcc.fightToTheLast = true;
					bcc.enemyDeployAll = true;
				}
			};
			return config;
		}
	}
	
	
	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Unknown Location");
		//system.setType(StarSystemType.NEBULA);
		system.setName("Unknown Location"); // to get rid of "Star System" at the end of the name
		system.addTag(Tags.THEME_UNSAFE);
		system.addTag(Tags.THEME_HIDDEN);
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "music_campaign_alpha_site");
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
		//system.getLocation().set(2500, 3000);
		system.getLocation().set(4000, 2500);
		
		HyperspaceTerrainPlugin hyperTerrain = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(hyperTerrain);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, 200, 0, 360f);
//		editor.regenNoise();
//		editor.noisePrune(0.8f);
//		editor.regenNoise();

		SectorEntityToken center = system.initNonStarCenter();
		
		system.setLightColor(new Color(225,170,255,255)); // light color in entire system, affects all entities
		
		String type = "barren";
		type = "irradiated";
		PlanetAPI rock = system.addPlanet("site_alpha", center, "Alpha Site", type, 0, 150, 1200, 40);
		//rock.setCustomDescriptionId("???");
		rock.getMemoryWithoutUpdate().set("$ttBlackSite", true);
		
		rock.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		rock.getMarket().addCondition(Conditions.COLD);
		rock.getMarket().addCondition(Conditions.DARK);
		rock.getMarket().addCondition(Conditions.IRRADIATED);
		rock.getMarket().addCondition(Conditions.RUINS_SCATTERED);
		
		rock.getMarket().getMemoryWithoutUpdate().set("$ruinsExplored", true);
		//If this is done, shows up in intel planet list; doesn't matter either way
		//Misc.setFullySurveyed(rock.getMarket(), null, false);
		
		CoreLifecyclePluginImpl.addRuinsJunk(rock);
		
		rock.setOrbit(null);
		rock.setLocation(1200, 300);
		
		SectorEntityToken field = system.addTerrain(Terrain.MAGNETIC_FIELD,
						new MagneticFieldParams(150f, // terrain effect band width 
						500, // terrain effect middle radius
						rock, // entity that it's around
						350f, // visual band start
						650f, // visual band end
						new Color(60, 60, 150, 90), // base color
						1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
						new Color(130, 60, 150, 130),
						new Color(150, 30, 120, 150), 
						new Color(200, 50, 130, 190),
						new Color(250, 70, 150, 240),
						new Color(200, 80, 130, 255),
						new Color(75, 0, 160, 255), 
						new Color(127, 0, 255, 255)
						));
		field.setCircularOrbit(rock, 0, 0, 75);
		
		CustomCampaignEntityAPI beacon = system.addCustomEntity(null, null, Entities.WARNING_BEACON, Factions.NEUTRAL);
		beacon.setCircularOrbitPointingDown(rock, 0, 2500, 60);
		
		beacon.getMemoryWithoutUpdate().set("$ttBlackSite", true);
		beacon.getMemoryWithoutUpdate().set(WarningBeaconEntityPlugin.PING_ID_KEY, Pings.WARNING_BEACON3);
		beacon.getMemoryWithoutUpdate().set(WarningBeaconEntityPlugin.PING_FREQ_KEY, 1.5f);
		beacon.getMemoryWithoutUpdate().set(WarningBeaconEntityPlugin.PING_COLOR_KEY, new Color(250,125,0,255));
		beacon.getMemoryWithoutUpdate().set(WarningBeaconEntityPlugin.GLOW_COLOR_KEY, new Color(250,55,0,255));
		
		
		SectorEntityToken cache = BaseThemeGenerator.addSalvageEntity(system, Entities.ALPHA_SITE_WEAPONS_CACHE, Factions.NEUTRAL);
		cache.getMemoryWithoutUpdate().set("$ttWeaponsCache", true);
		cache.getLocation().set(11111, 11111);
		
//		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("rock_jump_point", "Unstable Jump-point");
//		OrbitAPI orbit = Global.getFactory().createCircularOrbit(rock, 0, 2500, 90);
//		jumpPoint.setOrbit(orbit);
//		//jumpPoint.setRelatedPlanet(magec2);
//		jumpPoint.setStandardWormholeToHyperspaceVisual();
//		system.addEntity(jumpPoint);
		
		system.generateAnchorIfNeeded();
		
		NascentGravityWellAPI well = Global.getSector().createNascentGravityWell(beacon, 50f);
		well.addTag(Tags.NO_ENTITY_TOOLTIP);
		well.setColorOverride(new Color(125, 50, 255));
		hyper.addEntity(well);
		well.autoUpdateHyperLocationBasedOnInSystemEntityAtRadius(beacon, 0);
		
		addFleet(rock);
		
		SectorEntityToken hamatsu = addDerelict(system, beacon, 
							"venture_Outdated", "ISS Hamatsu", HAMATSU_ID, ShipCondition.BATTERED, 200, true);
		hamatsu.getMemoryWithoutUpdate().set("$hamatsu", true);
		
		Global.getSector().getMemoryWithoutUpdate().set(NASCENT_WELL_KEY, well);
	}
	
	public static NascentGravityWellAPI getWell() {
		return (NascentGravityWellAPI) Global.getSector().getMemoryWithoutUpdate().get(NASCENT_WELL_KEY);
	}
	
	
	public static void addFleet(SectorEntityToken rock) {
		CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.NEUTRAL, FleetTypes.PATROL_LARGE, null);
		fleet.setName("Unidentified Vessel");
		fleet.setNoFactionInName(true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
		//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true); // so it keeps transponder on
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
		fleet.getMemoryWithoutUpdate().set("$ziggurat", true);
		
		fleet.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY, true);
		
		
		fleet.getFleetData().addFleetMember("ziggurat_Experimental");
		fleet.getFleetData().ensureHasFlagship();
		
		fleet.clearAbilities();
//		fleet.addAbility(Abilities.TRANSPONDER);
//		fleet.getAbility(Abilities.TRANSPONDER).activate();
		
		// so it never shows as "Unidentified Fleet" but isn't easier to spot due to using the actual transponder ability
		fleet.setTransponderOn(true);
		
		PersonAPI person = createZigguratCaptain();
		fleet.setCommander(person);
		
		FleetMemberAPI flagship = fleet.getFlagship();
		flagship.setCaptain(person);
		flagship.updateStats();
		flagship.getRepairTracker().setCR(flagship.getRepairTracker().getMaxCR());
		flagship.setShipName("TTS Xenorphica");
		
		// to "perm" the variant so it gets saved and not recreated from the "ziggurat_Experimental" id
		flagship.setVariant(flagship.getVariant().clone(), false, false);
		flagship.getVariant().setSource(VariantSource.REFIT);
		flagship.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);

		
		Vector2f loc = new Vector2f(rock.getLocation().x + 300 * ((float) Math.random() - 0.5f),
									rock.getLocation().y + 300 * ((float) Math.random() - 0.5f));
		fleet.setLocation(loc.x, loc.y);
		rock.getContainingLocation().addEntity(fleet);
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, 
				   new ZigFIDConfig());
		
		fleet.addScript(new ZigLeashAssignmentAI(fleet, rock));
		
	}
	
	
	public static PersonAPI createZigguratCaptain() {
		PersonAPI person = Global.getFactory().createPerson();
		person.setName(new FullName("Motes", "", Gender.ANY));
		person.setFaction(Factions.NEUTRAL);
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "ziggurat_captain"));
		person.setPersonality(Personalities.RECKLESS);
		person.setRankId(Ranks.SPACE_CAPTAIN);
		person.setPostId(null);
		
		person.getStats().setSkipRefresh(true);
		
		person.getStats().setLevel(10);
		person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
		person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
		person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
		person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		//person.getStats().setSkillLevel(Skills.RELIABILITY_ENGINEERING, 2);
		//person.getStats().setSkillLevel(Skills.RANGED_SPECIALIZATION, 2);
		person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
		person.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
		//person.getStats().setSkillLevel(Skills.PHASE_MASTERY, 2);
		person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
		
		person.getStats().setSkillLevel(Skills.NAVIGATION, 1);
		
		person.getStats().setSkipRefresh(false);
		
		return person;
	}
	
	public static SectorEntityToken addDerelict(StarSystemAPI system, SectorEntityToken focus, 
							   String variantId, String name, String id, 
							   ShipCondition condition, float orbitRadius, boolean recoverable) {
		DerelictShipData params = new DerelictShipData(new PerShipData(variantId, condition, 0f), false);
		if (name != null) {
			params.ship.shipName = name;
			params.ship.nameAlwaysKnown = true;
			params.ship.fleetMemberId = id;
		}
		SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
		ship.setDiscoverable(true);

		float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
		ship.setCircularOrbit(focus, (float) Math.random() * 360f, orbitRadius, orbitDays);

		if (recoverable) {
			ShipRecoverySpecialCreator creator = new ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
			Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
		}
		return ship;
	}
	
}













