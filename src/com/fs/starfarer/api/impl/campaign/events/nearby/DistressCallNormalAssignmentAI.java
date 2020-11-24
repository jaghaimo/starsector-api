package com.fs.starfarer.api.impl.campaign.events.nearby;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CargoPodsEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.CargoPods;
import com.fs.starfarer.api.util.Misc;

public class DistressCallNormalAssignmentAI extends BaseAssignmentAI {

	protected StarSystemAPI system;
	protected SectorEntityToken jumpPoint;
	
	protected float elapsed = 0f;
	protected float dur = 30f + (float) Math.random() * 20f;
	protected boolean contactedPlayer = false;
	
	public DistressCallNormalAssignmentAI(CampaignFleetAPI fleet, StarSystemAPI system, SectorEntityToken jumpPoint) {
		super();
		this.fleet = fleet;
		this.system = system;
		this.jumpPoint = jumpPoint;
		
		giveInitialAssignments();
	}

	@Override
	protected void giveInitialAssignments() {
		fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, jumpPoint, 30f);
	}

	@Override
	protected void pickNext() {
		fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, jumpPoint, 30f);
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (!fleet.getMemoryWithoutUpdate().contains("$distress")) {
			//fleet.getMemoryWithoutUpdate().unset(MemFlags.ENTITY_MISSION_IMPORTANT);
			
			Misc.giveStandardReturnToSourceAssignments(fleet);
			fleet.removeScript(this);
			return;
		}
		
		float days = Global.getSector().getClock().convertToDays(amount);
		boolean playerNearby = Misc.getDistanceToPlayerLY(fleet) <= 3f;
		
		elapsed += days;
		
//		if (!playerNearby && elapsed >= dur && fleet.getBattle() == null) {
//			fleet.despawn(FleetDespawnReason.OTHER, null);
//		}
//		if (fleet.isDespawning()) return;
		
		
		if (elapsed >= dur - 10f) {
			boolean seesPlayer = false;
			if (playerNearby) {
				VisibilityLevel level = fleet.getVisibilityLevelOfPlayerFleet();
				seesPlayer |= level == VisibilityLevel.COMPOSITION_DETAILS;
				seesPlayer |= level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS;
			}
			if (!seesPlayer) {
				undistress(fleet);
				int crewToLeave = (int) (fleet.getFleetData().getMinCrew() * 0.5f);
				scuttleShips(fleet, crewToLeave);
				leaveCrewInPods(fleet, crewToLeave);
			}
		}
		
		if (fleet.isInCurrentLocation() && !contactedPlayer) {
			VisibilityLevel level = fleet.getVisibilityLevelOfPlayerFleet();
			if (level != VisibilityLevel.NONE &&
					level != VisibilityLevel.SENSOR_CONTACT) {
				contactedPlayer = true;
				fleet.clearAssignments();
				fleet.addAssignment(FleetAssignment.INTERCEPT, Global.getSector().getPlayerFleet(), 30f, "approaching your fleet");
			}
		}
	}
	
	public static void leaveCrewInPods(CampaignFleetAPI fleet, int crew) {
		CustomCampaignEntityAPI pods = Misc.addCargoPods(fleet.getContainingLocation(), fleet.getLocation());
		CargoPodsEntityPlugin plugin = (CargoPodsEntityPlugin)pods.getCustomPlugin();
		plugin.setExtraDays(100f);
		pods.getCargo().addCrew(crew);
		CargoPods.stabilizeOrbit(pods, false);
		
		//int machinery = crew / 10;
		//pods.setDiscoverable(true);
	}
	

	public static void undistress(SectorEntityToken fleet) {
		MemoryAPI memory = fleet.getMemoryWithoutUpdate();
		memory.unset("$distress");
		memory.unset(MemFlags.MEMORY_KEY_NO_JUMP);
		
		//memory.unset(MemFlags.ENTITY_MISSION_IMPORTANT);
		Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.ENTITY_MISSION_IMPORTANT,
    			   			   "distress", false, 1000f);
	}
	
	public static void scuttleShips(CampaignFleetAPI fleet, int crewFreed) {
		List<FleetMemberAPI> members = new ArrayList<FleetMemberAPI>(fleet.getFleetData().getMembersListCopy());
		float scuttledCrewRoom = 0;
		for (int i = members.size() - 1; i >= 0 && scuttledCrewRoom < crewFreed; i--) {
			FleetMemberAPI member = members.get(i);
			fleet.removeFleetMemberWithDestructionFlash(member);
			scuttledCrewRoom += member.getMinCrew();
		}
	}

}












