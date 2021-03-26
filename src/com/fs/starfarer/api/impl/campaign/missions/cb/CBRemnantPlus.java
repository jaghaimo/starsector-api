package com.fs.starfarer.api.impl.campaign.missions.cb;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class CBRemnantPlus extends BaseCustomBountyCreator {

	public static float PROB_IN_SYSTEM_WITH_BASE = 0.5f;
	
	@Override
	public float getBountyDays() {
		return CBStats.REMNANT_PLUS_DAYS;
	}
	
	@Override
	public float getFrequency(HubMissionWithBarEvent mission, int difficulty) {
		return super.getFrequency(mission, difficulty) * CBStats.REMNANT_PLUS_FREQ;
	}
	
	@Override
	protected boolean isRepeatableGlobally() {
		return false;
	}

	public String getBountyNamePostfix(HubMissionWithBarEvent mission, CustomBountyData data) {
		return " - Unusual Remnant Fleet";
	}
	
	@Override
	public CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
		CustomBountyData data = new CustomBountyData();
		data.difficulty = difficulty;
		
		mission.setIconName("campaignMissions", "remnant_bounty");
		//mission.requireSystem(this);
		mission.requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_CORE);
		mission.preferSystemTags(ReqMode.ANY, Tags.HAS_CORONAL_TAP);
		mission.preferSystemUnexplored();
		mission.preferSystemInteresting();
//		mission.requireSystemTags(ReqMode.ANY, Tags.THEME_RUINS, Tags.THEME_MISC, Tags.THEME_REMNANT,
//				  Tags.THEME_DERELICT, Tags.THEME_REMNANT_DESTROYED);
		mission.requireSystemNotHasPulsar();		
		mission.preferSystemBlackHoleOrNebula();
		mission.preferSystemOnFringeOfSector();
		
		StarSystemAPI system = mission.pickSystem();
		data.system = system;
	
		FleetSize size = FleetSize.HUGE;
		FleetQuality quality = FleetQuality.SMOD_3;
		OfficerQuality oQuality = OfficerQuality.AI_ALPHA;
		OfficerNum oNum = OfficerNum.ALL_SHIPS;
		String type = FleetTypes.PATROL_LARGE;
		
		beginFleet(mission, data);
		mission.triggerCreateFleet(size, quality, Factions.REMNANTS, type, data.system);
		mission.triggerSetFleetOfficers(oNum, oQuality);
		mission.triggerAutoAdjustFleetSize(size, size.next());
		mission.triggerSetRemnantConfigActive();
		mission.triggerSetFleetNoCommanderSkills();
		mission.triggerFleetAddCommanderSkill(Skills.FLUX_REGULATION, 1);
		mission.triggerFleetAddCommanderSkill(Skills.ELECTRONIC_WARFARE, 1);
		mission.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
		mission.triggerFleetAddCommanderSkill(Skills.NAVIGATION, 1);
		mission.triggerFleetSetAllWeapons();
		mission.triggerMakeHostileAndAggressive();
		mission.triggerFleetAllowLongPursuit();
		mission.triggerPickLocationAtInSystemJumpPoint(data.system);
		mission.triggerSpawnFleetAtPickedLocation(null, null);
		mission.triggerFleetSetPatrolActionText("sending hyperwave signals");
		mission.triggerOrderFleetPatrol(data.system, true, Tags.JUMP_POINT, Tags.NEUTRINO, Tags.NEUTRINO_HIGH, Tags.STATION,
									    Tags.SALVAGEABLE, Tags.GAS_GIANT);
		
		data.fleet = createFleet(mission, data);
		if (data.fleet == null) return null;
		
		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(Factions.OMEGA, "", true);
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(mission.getGenRandom());
		picker.add("tesseract_Attack");
		picker.add("tesseract_Attack2");
		picker.add("tesseract_Strike");
		picker.add("tesseract_Disruptor");
		fleet.getFleetData().addFleetMember(picker.pick());
		FleetMemberAPI member = fleet.getFlagship();
		
		AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.OMEGA_CORE);
		PersonAPI person = plugin.createPerson(Commodities.OMEGA_CORE, Factions.OMEGA, mission.getGenRandom());
		member.setCaptain(person);
		
		int i = data.fleet.getFleetData().getMembersListCopy().size() - 1;
		FleetMemberAPI last = data.fleet.getFleetData().getMembersListCopy().get(i);
		data.fleet.getFleetData().removeFleetMember(last);
		
		data.fleet.setCommander(person);
		data.fleet.getFleetData().addFleetMember(member);
		data.fleet.getFleetData().sort();
		List<FleetMemberAPI> members = data.fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		
		member.setVariant(member.getVariant().clone(), false, false);
		member.getVariant().setSource(VariantSource.REFIT);
		member.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
		member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
		
		// otherwise, remnant dialog which isn't appropriate with an Omega in charge
		data.fleet.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
		
		setRepChangesBasedOnDifficulty(data, difficulty);
		data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.REMNANT_PLUS_MULT, mission);
		
		return data;
	}
	

	@Override
	public int getMaxDifficulty() {
		return super.getMaxDifficulty();
	}

	@Override
	public int getMinDifficulty() {
		return super.getMaxDifficulty();
	}

}






