package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	$ngcAddOfficer
 *	$ngcSkipTutorial
 *
 */
public class NGCAddDevStartingScript extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");
		final MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		data.addScript(new Script() {
			public void run() {
				
				Global.getSector().getPlayerStats().addStoryPoints(100);
				
				CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
				fleet.getFleetData().addFleetMember("hammerhead_Balanced");
				fleet.getFleetData().addFleetMember("tarsus_Standard");
				fleet.getFleetData().addFleetMember("dram_Light");
				// add crew, supplies, and fuel
				int crew = 0;
				int supplies = 0;
				for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
					crew += Math.ceil(member.getMinCrew() + (member.getMaxCrew() - member.getMinCrew()) * 0.5f);
					supplies += member.getDeploymentCostSupplies() * 4f;
				}
				
				CargoAPI cargo = fleet.getCargo();
				cargo.removeCrew(cargo.getCrew());
				cargo.addCrew(crew);
				cargo.addFuel(cargo.getMaxFuel() * 0.5f);
				
				cargo.addCommodity(Commodities.ALPHA_CORE, 10);
				cargo.addCommodity(Commodities.BETA_CORE, 10);
				cargo.addCommodity(Commodities.GAMMA_CORE, 10);
				
				cargo.addSupplies(cargo.getSpaceLeft());
				
				fleet.getFleetData().ensureHasFlagship();
				
				for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
					float max = member.getRepairTracker().getMaxCR();
					member.getRepairTracker().setCR(max);
				}
				fleet.getFleetData().setSyncNeeded();
				
				StarSystemAPI system = Global.getSector().getStarSystem("galatia");
				PlanetAPI ancyra = (PlanetAPI) system.getEntityById("ancyra");
				PersonAPI mainContact = TutorialMissionIntel.createMainContact(ancyra);
				PersonAPI jangalaContact = TutorialMissionIntel.getJangalaContact();
				
				TutorialMissionIntel.endGalatiaPortionOfMission(true, false);
					
				mainContact.getRelToPlayer().setRel(0.2f);
				jangalaContact.getRelToPlayer().setRel(0.1f);
				Global.getSector().getFaction(Factions.HEGEMONY).getRelToPlayer().setRel(0.15f);
			}
			
		});
		return true;
	}
	
	

}



