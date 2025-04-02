package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.fleets.BaseGenerateFleetOfficersPlugin;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class RemnantOfficerGeneratorPlugin extends BaseGenerateFleetOfficersPlugin {
	
	protected boolean putCoresOnCivShips = false;
	protected boolean forceIntegrateCores = false;
	protected boolean forceNoCommander = false;
	protected boolean derelictMode = false;
	protected float coreMult = 1f;
	
	public RemnantOfficerGeneratorPlugin() {
	}

	public RemnantOfficerGeneratorPlugin(boolean derelictMode, float coreMult) {
		this.derelictMode = derelictMode;
		this.coreMult = coreMult;
	}
	

	
	public boolean isForceNoCommander() {
		return forceNoCommander;
	}

	public void setForceNoCommander(boolean forceNoCommander) {
		this.forceNoCommander = forceNoCommander;
	}

	public boolean isPutCoresOnCivShips() {
		return putCoresOnCivShips;
	}

	public void setPutCoresOnCivShips(boolean putCoresOnCivShips) {
		this.putCoresOnCivShips = putCoresOnCivShips;
	}

	public boolean isForceIntegrateCores() {
		return forceIntegrateCores;
	}

	public void setForceIntegrateCores(boolean forceIntegrateCores) {
		this.forceIntegrateCores = forceIntegrateCores;
	}

	@Override
	public int getHandlingPriority(Object params) {
		if (!(params instanceof GenerateFleetOfficersPickData)) return -1;
		
		GenerateFleetOfficersPickData data = (GenerateFleetOfficersPickData) params;
		
		if (data.params != null && !data.params.withOfficers) return -1;
		
		if (data.params.aiCores != null) return GenericPluginManagerAPI.CORE_SUBSET;
		
		if (data.fleet == null || !data.fleet.getFaction().getId().equals(Factions.REMNANTS)) return -1;
		
		return GenericPluginManagerAPI.CORE_SUBSET;
	}

	
	@Override
	public void addCommanderAndOfficers(CampaignFleetAPI fleet, FleetParamsV3 params, Random random) {
		if (random == null) random = Misc.random;
		FactionAPI faction = fleet.getFaction();
		FactionDoctrineAPI doctrine = faction.getDoctrine();
		if (!derelictMode && params != null && params.doctrineOverride != null) {
			doctrine = params.doctrineOverride;
		}
		List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
		if (members.isEmpty()) return;
		
		Map<String, AICoreOfficerPlugin> plugins = new HashMap<String, AICoreOfficerPlugin>();
		
		plugins.put(Commodities.OMEGA_CORE, Misc.getAICoreOfficerPlugin(Commodities.OMEGA_CORE));
		plugins.put(Commodities.ALPHA_CORE, Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE));
		plugins.put(Commodities.BETA_CORE, Misc.getAICoreOfficerPlugin(Commodities.BETA_CORE));
		plugins.put(Commodities.GAMMA_CORE, Misc.getAICoreOfficerPlugin(Commodities.GAMMA_CORE));
		String nothing = "nothing";
		
		float fleetFP = 0f; //fleet.getFleetPoints(); <- doesn't work here, requires a call to fleet.forceSync()
		for (FleetMemberAPI member : members) {
			fleetFP += member.getFleetPointCost();
		}
		boolean allowAlphaAnywhere = fleetFP > 150f;
		boolean allowBetaAnywhere = fleetFP > 75f;
		
		//boolean integrate = fleetFP > 200f || params.forceIntegrateAICores;
		boolean integrate = params != null && !params.doNotIntegrateAICores;
		integrate |= forceIntegrateCores;
		
		int numCommanderSkills = 0;
		if (allowBetaAnywhere) numCommanderSkills++;
		if (allowAlphaAnywhere) numCommanderSkills++;
		if (params != null && params.noCommanderSkills != null && params.noCommanderSkills) numCommanderSkills = 0;
		
		
		//float fpPerCore = 20f;
		float fpPerCore = Global.getSettings().getFloat("baseFPPerAICore");
		
		if (derelictMode) {
			fpPerCore = 30 - 20f * coreMult;
		}
		
		int minCores = (int) (fleetFP / fpPerCore * (params != null ? params.officerNumberMult : 1f));
		if (params != null) {
			minCores += params.officerNumberBonus;
		}
		if (minCores < 1) minCores = 1;
		
		boolean debug = true;
		debug = false;
		
		WeightedRandomPicker<FleetMemberAPI> withOfficers = new WeightedRandomPicker<FleetMemberAPI>(random);
		
		int maxSize = 0;
		for (FleetMemberAPI member : members) {
			if (!Misc.isAutomated(member)) continue;
			if (member.isFighterWing()) continue;
			if (member.isCivilian() && !putCoresOnCivShips) continue;
			int size = member.getHullSpec().getHullSize().ordinal();
			if (size > maxSize) {
				maxSize = size;
			}
		}
		
		List<FleetMemberAPI> allWithOfficers = new ArrayList<FleetMemberAPI>();
		int addedCores = 0;
		for (FleetMemberAPI member : members) {
			if (!Misc.isAutomated(member)) continue;
			if (member.isCivilian() && !putCoresOnCivShips) continue;
			if (member.isFighterWing()) continue;
			
			float fp = member.getFleetPointCost();
			
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
			
			if (params != null && params.aiCores == OfficerQuality.AI_GAMMA) {
				picker.add(Commodities.GAMMA_CORE, fp);
			} else if (params != null && params.aiCores == OfficerQuality.AI_BETA) {
				picker.add(Commodities.BETA_CORE, fp);
			} else if (params != null && params.aiCores == OfficerQuality.AI_ALPHA) {
				picker.add(Commodities.ALPHA_CORE, fp);
			} else if (params != null && params.aiCores == OfficerQuality.AI_OMEGA) {
				picker.add(Commodities.OMEGA_CORE, fp);
			} else if (params != null && params.aiCores == OfficerQuality.AI_BETA_OR_GAMMA) {
				if (member.isCapital() || member.isCruiser()) {
					picker.add(Commodities.BETA_CORE, fp);
				} else if (allowAlphaAnywhere) {
					picker.add(Commodities.BETA_CORE, fp);
				} else {
					picker.add(Commodities.BETA_CORE, fp/2f);
				}
				picker.add(Commodities.GAMMA_CORE, fp);
			} else {
				if (derelictMode) {
					picker.add(Commodities.GAMMA_CORE, fp);
				} else {
					if (member.isCapital() || member.isCruiser()) {
						picker.add(Commodities.ALPHA_CORE, fp);
					} else if (allowAlphaAnywhere) {
						picker.add(Commodities.ALPHA_CORE, fp);
					}
					
					if (member.isCruiser() || member.isDestroyer()) {
						picker.add(Commodities.BETA_CORE, fp/2f);
					} else if (allowBetaAnywhere && member.isFrigate()) {
						picker.add(Commodities.BETA_CORE, fp);
					}
					
					if (member.isDestroyer() || member.isFrigate()) {
						picker.add(Commodities.GAMMA_CORE, fp);
					}
				}
			}
			
			if (addedCores >= minCores) {
				picker.add(nothing, 10f * picker.getTotal()/fp);
			}
			
			String pick = picker.pick();
			if (debug) {
				System.out.println("Picked [" + pick + "] for " + member.getHullId());
			}
			AICoreOfficerPlugin plugin = plugins.get(pick);
			if (plugin != null) {
				addedCores++;
				
				PersonAPI person = plugin.createPerson(pick, fleet.getFaction().getId(), random);
				member.setCaptain(person);
				if (integrate && !Commodities.OMEGA_CORE.equals(pick)) {
					integrateAndAdaptCoreForAIFleet(member);
				}
				
				if (!member.isFighterWing() && !member.isCivilian()) {
					withOfficers.add(member, fp);
				}
				
				allWithOfficers.add(member);
			}
			
			if (addedCores > 0 && params != null && params.officerNumberMult <= 0) {
				break; // only want to add the fleet commander
			}
		}
		
		if (withOfficers.isEmpty() && !allWithOfficers.isEmpty()) {
			withOfficers.add(allWithOfficers.get(0), 1f);
		}
		
		
		FleetMemberAPI flagship = withOfficers.pick();
		if (!derelictMode && !forceNoCommander && flagship != null) {
			PersonAPI commander = flagship.getCaptain();
			commander.setRankId(Ranks.SPACE_COMMANDER);
			commander.setPostId(Ranks.POST_FLEET_COMMANDER);
			fleet.setCommander(commander);
			fleet.getFleetData().setFlagship(flagship);
			addCommanderSkills(commander, fleet, params, numCommanderSkills, random);			
		}
	}
	
	public static void integrateAndAdaptCoreForAIFleet(FleetMemberAPI member) {
		PersonAPI person = member.getCaptain();
		if (!person.isAICore()) return;
		
		person.getStats().setLevel(person.getStats().getLevel() + 1);
		
		person.getStats().setSkipRefresh(true);
		
//		if (member.isCarrier()) {
//			person.getStats().setSkillLevel(Skills.STRIKE_COMMANDER, 2);
//			if (person.getStats().getSkillLevel(Skills.POINT_DEFENSE) <= 0) {
//				person.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
//				person.getStats().setSkillLevel(Skills.RELIABILITY_ENGINEERING, 0);
//			}
//		} else {
		if (member.getVariant() != null && member.getVariant().getWeaponGroups() != null) {
			float weight = 0f;
			float pdWeight = 0f;
			float missileWeight = 0f;
			for (String slotId : member.getVariant().getFittedWeaponSlots()) {
				WeaponSpecAPI spec = member.getVariant().getWeaponSpec(slotId);
				if (spec == null) continue;
				float w = 1f;
				if (spec.getSize() == WeaponSize.MEDIUM) w = 2f;
				if (spec.getSize() == WeaponSize.LARGE) w = 4f;
				weight += w;
				if (spec.getAIHints().contains(AIHints.PD)) {
					pdWeight += w;
				}
				if (spec.getType() == WeaponType.MISSILE) {
					missileWeight += w;
				}
				
			}
			
			float decks = member.getNumFlightDecks();
			if (decks > 0) {
				weight += decks * 4f;
				pdWeight += decks * 4f;
			}

			boolean hasUsefulPD = pdWeight > weight * 0.25f;
			boolean hasEnoughMissiles = missileWeight > weight * 0.2f;
			
			
			if (hasUsefulPD && !hasEnoughMissiles) {
				person.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
				person.getStats().setSkipRefresh(false);
				return;
			}
		}
		
		
			if (member.getHullSpec() != null && member.getHullSpec().hasTag(Factions.DERELICT) &&
					person.getStats().getSkillLevel(Skills.BALLISTIC_MASTERY) <= 0) {
				person.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 2);
			} else {
				if (person.getStats().getSkillLevel(Skills.ENERGY_WEAPON_MASTERY) <= 0) {
					person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
				} else {
					person.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
				}
			}
			
			if (member.isCapital() || member.isStation()) {
				if (person.getStats().getSkillLevel(Skills.POLARIZED_ARMOR) <= 0) {
					person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 0);
					person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
				}
			}
		//}
		
		person.getStats().setSkipRefresh(false);
	}
	
	
	public static SkillPickPreference getSkillPrefForShip(FleetMemberAPI member) {
		return FleetFactoryV3.getSkillPrefForShip(member);
//		float weight = FleetFactoryV3.getMemberWeight(member);
//		float fighters = member.getVariant().getFittedWings().size();
//		boolean wantCarrierSkills = weight > 0 && fighters / weight >= 0.5f;
//		SkillPickPreference pref = SkillPickPreference.GENERIC;
//		if (wantCarrierSkills) {
//			pref = SkillPickPreference.CARRIER;
//		} else if (member.isPhaseShip()) {
//			pref = SkillPickPreference.PHASE;
//		}
//		
//		return pref;
	}
	
	
	public static void addCommanderSkills(PersonAPI commander, CampaignFleetAPI fleet, FleetParamsV3 params, int numSkills, Random random) {
		if (random == null) random = new Random();
		if (numSkills <= 0) return;
		
		MutableCharacterStatsAPI stats = commander.getStats();
		
		FactionDoctrineAPI doctrine = fleet.getFaction().getDoctrine();
		if (params != null && params.doctrineOverride != null) {
			doctrine = params.doctrineOverride;
		}
		
		List<String> skills = new ArrayList<String>(doctrine.getCommanderSkills());
		if (skills.isEmpty()) return;
		
		if (random.nextFloat() < doctrine.getCommanderSkillsShuffleProbability()) {
			Collections.shuffle(skills, random);
		}

		stats.setSkipRefresh(true);
		
		boolean debug = true;
		debug = false;
		if (debug) System.out.println("Generating commander skills, person level " + stats.getLevel() + ", skills: " + numSkills);
		int picks = 0;
		for (String skillId : skills) {
			if (debug) System.out.println("Selected skill: [" + skillId + "]");
			stats.setSkillLevel(skillId, 1);
			picks++;
			if (picks >= numSkills) {
				break;
			}
		}
		if (debug) System.out.println("Done generating commander skills\n");
		
		stats.setSkipRefresh(false);
		stats.refreshCharacterStatsEffects();
	}
	
	
	
	
	
}












