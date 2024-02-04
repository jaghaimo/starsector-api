package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI.GenericPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.BaseGenericPlugin;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantOfficerGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageEntityGeneratorOld;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Actually just generates automated defenses, not salvage. SalvageEntity does that.
 * 
 * SalvageGenFromSeed
 */
public class SalvageGenFromSeed extends BaseCommandPlugin {

	public static class SDMParams {
		public SectorEntityToken entity;
		public String factionId;
		public SDMParams() {
		}
	}
	
	
	public static interface SalvageDefenderModificationPlugin extends GenericPlugin {
		float getStrength(SDMParams p, float strength, Random random, boolean withOverride);
		float getProbability(SDMParams p, float probability, Random random, boolean withOverride);
		float getQuality(SDMParams p, float quality, Random random, boolean withOverride);
		float getMaxSize(SDMParams p, float maxSize, Random random, boolean withOverride);
		float getMinSize(SDMParams p, float minSize, Random random, boolean withOverride);
		
		void modifyFleet(SDMParams p, CampaignFleetAPI fleet, Random random, boolean withOverride);
		void reportDefeated(SDMParams p, SectorEntityToken entity, CampaignFleetAPI fleet);
	}
	
	public static final String DEFEATED_DERELICT_STR = "$defeatedDerelictStr";
	public static final float DEFEATED_TO_ADDED_FACTOR = 0.2f;
	public static final float DEFEATED_TO_QUALITY_FACTOR = 0.005f;
	
	public static class SalvageDefenderModificationPluginImpl extends BaseGenericPlugin implements SalvageDefenderModificationPlugin {
		public float getStrength(SDMParams p, float strength, Random random, boolean withOverride) {
			if (withOverride) return strength;
			if (Factions.DERELICT.equals(p.factionId)) {
				// Limbo stuff is not affected
				if (p.entity.getMemoryWithoutUpdate().getBoolean("$limboWormholeCache") ||
						p.entity.getMemoryWithoutUpdate().getBoolean("$limboMiningStation")) {
					return strength;
				}
				
				
				float bonus = Global.getSector().getMemoryWithoutUpdate().getFloat(DEFEATED_DERELICT_STR) * DEFEATED_TO_ADDED_FACTOR;
				
				String type = p.entity.getCustomEntityType();
				float limit = 300f;
				if (Entities.DERELICT_SURVEY_PROBE.equals(type)) {
					limit = 60;
				} else if (Entities.DERELICT_SURVEY_SHIP.equals(type)) {
					limit = 90;
				} else if (Entities.DERELICT_MOTHERSHIP.equals(type) || Entities.DERELICT_CRYOSLEEPER.equals(type)) {
					limit = 150;
				}
				
	//			if (Global.getSettings().isDevMode()) {
	//				bonus = limit;
	//			}
				
				if (bonus > limit) bonus = limit;
				return strength + (int) bonus;
			}
			return strength;
		}
		public float getMinSize(SDMParams p, float minSize, Random random, boolean withOverride) {
			if (withOverride) return minSize;
			return minSize;
		}
		public float getMaxSize(SDMParams p, float maxSize, Random random, boolean withOverride) {
			if (withOverride) return maxSize;
			if (Factions.DERELICT.equals(p.factionId)) {
				float bonus = Global.getSector().getMemoryWithoutUpdate().getFloat(DEFEATED_DERELICT_STR) * DEFEATED_TO_ADDED_FACTOR;
				String type = p.entity.getCustomEntityType();
				float bonusSize = 1;
				if (Entities.DERELICT_SURVEY_PROBE.equals(type)) {
					if (bonus >= 5) bonusSize = 2;
				}
				
				return Math.max(maxSize, bonusSize);
			}
			return maxSize; 
		}
		public float getProbability(SDMParams p, float probability, Random random, boolean withOverride) {
			if (withOverride) return probability;
			if (Factions.DERELICT.equals(p.factionId)) {
				String type = p.entity.getCustomEntityType();
				float limit = 0f;
				if (Entities.DERELICT_SURVEY_PROBE.equals(type)) {
					limit = 60;
				} else if (Entities.DERELICT_SURVEY_SHIP.equals(type)) {
					limit = 90;
				}
				if (limit <= 0 || probability >= 1f) return probability;
				
				float bonus = Global.getSector().getMemoryWithoutUpdate().getFloat(DEFEATED_DERELICT_STR) * DEFEATED_TO_ADDED_FACTOR;
				if (bonus > limit) bonus = limit;
				
				return probability * (1f - 0.5f * bonus / limit);
			}
			return probability;
		}
		public void reportDefeated(SDMParams p, SectorEntityToken entity, CampaignFleetAPI fleet) {
			if (Factions.DERELICT.equals(p.factionId)) {
				float total = Global.getSector().getMemoryWithoutUpdate().getFloat(DEFEATED_DERELICT_STR);
				for (FleetMemberAPI member : Misc.getSnapshotMembersLost(fleet)) {
					//total += FleetFactoryV2.getMemberWeight(member);
					total += member.getFleetPointCost();
				}
				Global.getSector().getMemoryWithoutUpdate().set(DEFEATED_DERELICT_STR, total);
			}
		}
		public void modifyFleet(SDMParams p, CampaignFleetAPI fleet, Random random, boolean withOverride) {
			if (Factions.OMEGA.equals(fleet.getFaction().getId())) {
				for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
					ShipVariantAPI copy = member.getVariant().clone();
					member.setVariant(copy, false, false);
					copy.addTag(Tags.SHIP_LIMITED_TOOLTIP);
				}
				
				DropData d;
				d = new DropData();
				d.chances = 5;
				d.maxChances = 7;
				d.group = "omega_weapons_small";
				fleet.addDropRandom(d);
				
				d = new DropData();
				d.chances = 3;
				d.maxChances = 4;
				d.group = "omega_weapons_medium";
				fleet.addDropRandom(d);
				
				long seed = Misc.getSalvageSeed(p.entity);
				fleet.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, Misc.getRandom(seed, 11).nextLong());
			} else
			if (Entities.ALPHA_SITE_WEAPONS_CACHE.equals(p.entity.getCustomEntityType())) {
				AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
				for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
					PersonAPI person = plugin.createPerson(Commodities.ALPHA_CORE, fleet.getFaction().getId(), random);
					member.setCaptain(person);
					RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(member);
					member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
				}
				if (fleet.getInflater() instanceof DefaultFleetInflater) {
					DefaultFleetInflater dfi = (DefaultFleetInflater) fleet.getInflater();
					((DefaultFleetInflaterParams)dfi.getParams()).allWeapons = true;
				}
			} else
			if (p.entity != null && p.entity.getMemoryWithoutUpdate().contains(MiscellaneousThemeGenerator.PLANETARY_SHIELD_PLANET)) {
				FleetMemberAPI flagship = null;
				for (ShipRolePick pick : fleet.getFaction().pickShip(ShipRoles.COMBAT_CAPITAL, ShipPickParams.all(), null, random)) {
					FleetMemberAPI member = fleet.getFleetData().addFleetMember(pick.variantId);
					flagship = member;
					// the name is used as part of the random seed for autofit, so, want it to be consistent
					member.setShipName(fleet.getFaction().pickRandomShipName(random));
				}
				
				AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
				
				for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
					member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
					member.setFlagship(member == flagship);

					//PersonAPI person = OfficerManagerEvent.createOfficer(fleet.getFaction(), 20, SkillPickPreference.NON_CARRIER, random);
					PersonAPI person = plugin.createPerson(Commodities.ALPHA_CORE, fleet.getFaction().getId(), random);
					member.setCaptain(person);
					RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(member);
				}
				
				//PersonAPI person = OfficerManagerEvent.createOfficer(fleet.getFaction(), 20, SkillPickPreference.NON_CARRIER, random);
				PersonAPI person = plugin.createPerson(Commodities.ALPHA_CORE, fleet.getFaction().getId(), random);
				fleet.setCommander(person);
				fleet.getFlagship().setCaptain(person);
				RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(fleet.getFlagship());
				
				if (fleet.getInflater() instanceof DefaultFleetInflater) {
					DefaultFleetInflater dfi = (DefaultFleetInflater) fleet.getInflater();
					((DefaultFleetInflaterParams)dfi.getParams()).allWeapons = true;
					//dfi.setSeed(Misc.random.nextLong());
				}
			} else
			if (Entities.DERELICT_CRYOSLEEPER.equals(p.entity.getCustomEntityType())) {
				fleet.getFleetData().clear();
				for (ShipRolePick pick : fleet.getFaction().pickShip(ShipRoles.COMBAT_CAPITAL, ShipPickParams.all(), null, random)) {
					fleet.getFleetData().addFleetMember(pick.variantId);
				}
				AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
				//PersonAPI person = OfficerManagerEvent.createOfficer(fleet.getFaction(), 20, true, SkillPickPreference.NON_CARRIER, random);
				PersonAPI person = plugin.createPerson(Commodities.ALPHA_CORE, fleet.getFaction().getId(), random);
				// so it's not the standard alpha core portrait but an older-looking one
				person.setPortraitSprite(fleet.getFaction().createRandomPerson().getPortraitSprite());
				fleet.setCommander(person);
				fleet.getFlagship().setCaptain(person);
				RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(fleet.getFlagship());
				
				for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
					member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
				}
			} else
			if (Entities.SPEC_LIMBO_WORMHOLE_CACHE.equals(p.entity.getCustomDescriptionId())) {
				//fleet.getFleetData().clear();
				for (ShipRolePick pick : fleet.getFaction().pickShip(ShipRoles.COMBAT_CAPITAL, ShipPickParams.all(), null, random)) {
					fleet.getFleetData().addFleetMember(pick.variantId);
				}
				AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.GAMMA_CORE);
				//PersonAPI person = OfficerManagerEvent.createOfficer(fleet.getFaction(), 20, true, SkillPickPreference.NON_CARRIER, random);
				PersonAPI person = plugin.createPerson(Commodities.GAMMA_CORE, fleet.getFaction().getId(), random);
				// so it's not the standard alpha core portrait but an older-looking one
				//person.setPortraitSprite(fleet.getFaction().createRandomPerson().getPortraitSprite());
				for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
					if (member.isCapital()) {
						fleet.getFleetData().setFlagship(member);
						break;
					}
				}
				fleet.setCommander(person);
				fleet.getFlagship().setCaptain(person);
				RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(fleet.getFlagship());
				
				for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
					member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
				}
			} else
			if (Entities.DERELICT_MOTHERSHIP.equals(p.entity.getCustomEntityType()) ||
					Entities.DERELICT_SURVEY_SHIP.equals(p.entity.getCustomEntityType()) ||
					Entities.DERELICT_SURVEY_PROBE.equals(p.entity.getCustomEntityType())
					) {
				
				float bonus = Global.getSector().getMemoryWithoutUpdate().getFloat(DEFEATED_DERELICT_STR) * DEFEATED_TO_ADDED_FACTOR;
				float coreMult = Math.max(0f, (bonus / 100f) - 0.1f);
				if (coreMult > 1f) coreMult = 1f;
				
				if (coreMult > 0) {
					RemnantOfficerGeneratorPlugin gen = new RemnantOfficerGeneratorPlugin(true, coreMult);
					if (coreMult >= 1f) {
						gen.setForceIntegrateCores(true);
					}
					gen.addCommanderAndOfficers(fleet, null, random);
				}
				for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
					if (member.isStation()) {
						AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
						PersonAPI person = plugin.createPerson(Commodities.ALPHA_CORE, fleet.getFaction().getId(), random);
						// so it's not the standard alpha core portrait but an older-looking one
						person.setPortraitSprite(fleet.getFaction().createRandomPerson().getPortraitSprite());
						member.setCaptain(person);
						RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(member);
					}
					member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
				}
				
			}
		}
		@Override
		public int getHandlingPriority(Object params) {
			if (!(params instanceof SDMParams)) return 0;
			SDMParams p = (SDMParams) params;
			
			if (p.entity != null && p.entity.getMemoryWithoutUpdate().contains(MiscellaneousThemeGenerator.PLANETARY_SHIELD_PLANET)) {
				return 1;
			}
			if (Factions.DERELICT.equals(p.factionId)) {
				return 1;
			}
			if (Factions.OMEGA.equals(p.factionId)) {
				return 1;
			}
			if (Factions.REMNANTS.equals(p.factionId)) {
				return 1;
			}
			
			return -1;
		}
		public float getQuality(SDMParams p, float quality, Random random, boolean withOverride) {
			if (withOverride) return quality;
			if (Factions.DERELICT.equals(p.factionId)) {
				float bonus = Global.getSector().getMemoryWithoutUpdate().getFloat(DEFEATED_DERELICT_STR) * DEFEATED_TO_QUALITY_FACTOR;
				return quality + bonus;
			}
			return quality;
		}
	}
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		SectorEntityToken entity = dialog.getInteractionTarget();
		String specId = entity.getCustomEntityType();
		if (specId == null || entity.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPEC_ID_OVERRIDE)) {
			specId = entity.getMemoryWithoutUpdate().getString(MemFlags.SALVAGE_SPEC_ID_OVERRIDE);
		}
		SalvageEntityGenDataSpec spec = SalvageEntityGeneratorOld.getSalvageSpec(specId);
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memoryMap.containsKey(MemKeys.ENTITY)) {
			memory = memoryMap.get(MemKeys.ENTITY);
		}
		
		long seed = memory.getLong(MemFlags.SALVAGE_SEED);
		//seed = new Random().nextLong();
//		if (Global.getSettings().isDevMode()) {
//			seed = new Random().nextLong();
//		}
		
		// don't use seed directly so that results from different uses of it aren't tied together
		Random random = Misc.getRandom(seed, 0);
		
		DefenderDataOverride override = null;
		if (memory.contains(MemFlags.SALVAGE_DEFENDER_OVERRIDE)) {
			override = (DefenderDataOverride) memory.get(MemFlags.SALVAGE_DEFENDER_OVERRIDE);
		}
		
		Random fleetRandom = Misc.getRandom(seed, 1);
//		if (Global.getSettings().isDevMode()) {
//			fleetRandom = Misc.random;
//		}
		
		//fleetRandom = new Random();
		float strength = spec.getMinStr() + Math.round((spec.getMaxStr() - spec.getMinStr()) * fleetRandom.nextFloat());
		float prob = spec.getProbDefenders();
		
		if (override != null) {
			strength = override.minStr + Math.round((override.maxStr - override.minStr) * fleetRandom.nextFloat());
			prob = override.probDefenders;
		}
		
		String factionId = entity.getFaction().getId();
		if (spec.getDefFaction() != null) {
			factionId = spec.getDefFaction();
		}
		if (override != null && override.defFaction != null) {
			factionId = override.defFaction;
		}
		
		SDMParams p = new SDMParams();
		p.entity = entity;
		p.factionId = factionId;
		
		SalvageDefenderModificationPlugin plugin = Global.getSector().getGenericPlugins().pickPlugin(
								SalvageDefenderModificationPlugin.class, p);
		
		if (plugin != null) {
			strength = plugin.getStrength(p, strength, random, override != null);
			prob = plugin.getProbability(p, prob, random, override != null);
		}
		
		float probStation = spec.getProbStation();
		if (override != null) {
			probStation = override.probStation;
		}
		String stationRole = null;
		if (fleetRandom.nextFloat() < probStation) {
			stationRole = spec.getStationRole();
			if (override != null && override.stationRole != null) {
				stationRole = override.stationRole;
			}
		}
		
		//prob = 1f;
		//strength = 0;
		
		if (((int) strength > 0 || stationRole != null) &&  
				random.nextFloat() < prob && 
				!memory.getBoolean("$defenderFleetDefeated")) {
			memory.set("$hasDefenders", true, 0);
			
			if (!memory.contains("$defenderFleet") || DebugFlags.FORCE_REGEN_AUTOMATED_DEFENSES) {
				float quality = spec.getDefQuality();
				if (plugin != null) {
					quality = plugin.getQuality(p, quality, fleetRandom, override != null);
				}
				
				FleetParamsV3 fParams = new FleetParamsV3(null, null,
								factionId,
								quality,
								FleetTypes.PATROL_SMALL,
								(int) strength,
								0, 0, 0, 0, 0, 0);
				fParams.random = fleetRandom;
				//fParams.withOfficers = false;
				FactionAPI faction = Global.getSector().getFaction(factionId);
				fParams.withOfficers = faction.getCustomBoolean(Factions.CUSTOM_OFFICERS_ON_AUTOMATED_DEFENSES);
				
				fParams.maxShipSize = (int) spec.getMaxDefenderSize();
				if (override != null) {
					fParams.maxShipSize = override.maxDefenderSize;
				}
				
				if (plugin != null) {
					fParams.maxShipSize = (int) (plugin.getMaxSize(p, fParams.maxShipSize, random, override != null));
				}
				
				fParams.minShipSize = (int) spec.getMinDefenderSize();
				if (override != null) {
					fParams.minShipSize = override.minDefenderSize;
				}
				if (plugin != null) {
					fParams.minShipSize = (int) (plugin.getMinSize(p, fParams.minShipSize, random, override != null));
				}
				
				//fParams.allowEmptyFleet = true;
				
				CampaignFleetAPI defenders = FleetFactoryV3.createFleet(fParams);
				
				if (!defenders.isEmpty()) {
					defenders.getInflater().setRemoveAfterInflating(false);
					
					//defenders.setName(entity.getName() + ": " + "Automated Defenses");
					defenders.setName("Automated Defenses");
	
					if (stationRole != null) {
						defenders.getFaction().pickShipAndAddToFleet(stationRole, ShipPickParams.all(), defenders, fleetRandom);
						defenders.getFleetData().sort();
					}
					
					defenders.clearAbilities();
					
					if (plugin != null) {
						//System.out.println("NEXT: " + fleetRandom.nextLong());
						plugin.modifyFleet(p, defenders, fleetRandom, override != null);
					}
					
					defenders.getFleetData().sort();
					
					memory.set("$defenderFleet", defenders, 0);
					
				} else {
					memory.set("$hasDefenders", false, 0);
				}
			}
			
			
			CampaignFleetAPI defenders = memory.getFleet("$defenderFleet");
			if (defenders != null) {
				boolean hasStation = false;
				boolean hasNonStation = false;
				for (FleetMemberAPI member : defenders.getFleetData().getMembersListCopy()) {
					if (member.isStation()) {
						hasStation = true;
					} else {
						hasNonStation = true;
					}
				}
				memory.set("$hasStation", hasStation, 0);
				memory.set("$hasNonStation", hasNonStation, 0);
				
				defenders.setLocation(entity.getLocation().x, entity.getLocation().y);
			}
		} else {
			memory.set("$hasDefenders", false, 0);
			memory.set("$hasStation", false, 0);
		}
		
		//memory.set("$hasSalvageSpecial", false, 0);
		//memory.set("$salvageSpecialData", null, 0);
		//memory.set("$salvageSpecialData", new DomainSurveyDerelictSpecialData(SpecialType.SCRAMBLED), 0);
		
	
		return true;
	}

	
}




