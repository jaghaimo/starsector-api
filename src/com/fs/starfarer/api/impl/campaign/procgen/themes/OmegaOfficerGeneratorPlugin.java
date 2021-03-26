package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.BaseGenerateFleetOfficersPlugin;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class OmegaOfficerGeneratorPlugin extends BaseGenerateFleetOfficersPlugin {
	
	public OmegaOfficerGeneratorPlugin() {
	}

	@Override
	public int getHandlingPriority(Object params) {
		if (!(params instanceof GenerateFleetOfficersPickData)) return -1;
		
		GenerateFleetOfficersPickData data = (GenerateFleetOfficersPickData) params;
		if (data.params != null && !data.params.withOfficers) return -1;
		if (data.fleet == null || !data.fleet.getFaction().getId().equals(Factions.OMEGA)) return -1;
		return GenericPluginManagerAPI.CORE_SUBSET;
	}

	
	@Override
	public void addCommanderAndOfficers(CampaignFleetAPI fleet, FleetParamsV3 params, Random random) {
		if (random == null) random = Misc.random;
		FactionAPI faction = fleet.getFaction();

		List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
		if (members.isEmpty()) return;
		
		WeightedRandomPicker<FleetMemberAPI> withOfficers = new WeightedRandomPicker<FleetMemberAPI>(random);
		AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.OMEGA_CORE);
		for (FleetMemberAPI member : members) {
			if (member.isFighterWing()) continue;
			
			PersonAPI person = plugin.createPerson(Commodities.OMEGA_CORE, faction.getId(), random);
			member.setCaptain(person);
			withOfficers.add(member, (float) Math.pow(member.getFleetPointCost(), 5f));
			// they're all assumed integrated and have the extra skill baked in
			//integrateAndAdaptCoreForAIFleet(member);
		}
		
		FleetMemberAPI flagship = withOfficers.pick();
		if (flagship != null) {
			PersonAPI commander = flagship.getCaptain();
			commander.setRankId(Ranks.SPACE_COMMANDER);
			commander.setPostId(Ranks.POST_FLEET_COMMANDER);
			fleet.setCommander(commander);
			fleet.getFleetData().setFlagship(flagship);
			addCommanderSkills(commander, fleet, params, 2, random);			
		}
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












