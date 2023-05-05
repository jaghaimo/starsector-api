package com.fs.starfarer.api.impl.campaign.missions.cb;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class CBRemnantStation extends BaseCustomBountyCreator {

	@Override
	public float getBountyDays() {
		return CBStats.REMNANT_STATION_DAYS;
	}

	@Override
	public float getFrequency(HubMissionWithBarEvent mission, int difficulty) {
		String faction = mission.getPerson().getFaction().getId();
		if (!Factions.HEGEMONY.equals(faction) &&
				!Factions.LUDDIC_PATH.equals(faction) && 
				!Factions.LUDDIC_CHURCH.equals(faction)) {
			return 0f;
		}
		if (getStations(mission, difficulty).isEmpty()) return 0f;
		return super.getFrequency(mission, difficulty) * CBStats.REMNANT_STATION_FREQ;
	}
	
	public void addIntelAssessment(TextPanelAPI text, HubMissionWithBarEvent mission, CustomBountyData data) {
		float opad = 10f;
		List<FleetMemberAPI> list = new ArrayList<FleetMemberAPI>();
		List<FleetMemberAPI> members = data.fleet.getFleetData().getMembersListCopy();
		int max = 7;
		int cols = 7;
		float iconSize = 440 / cols;
		Color h = Misc.getHighlightColor();
		
		for (FleetMemberAPI member : members) {
			if (list.size() >= max) break;
			
			if (member.isFighterWing()) continue;
			
			FleetMemberAPI copy = Global.getFactory().createFleetMember(FleetMemberType.SHIP, member.getVariant());
			if (member.isFlagship()) {
				copy.setCaptain(data.fleet.getCommander());
			}
			list.add(copy);
		}
		
		if (!list.isEmpty()) {
			TooltipMakerAPI info = text.beginTooltip();
			info.setParaSmallInsignia();
			info.addPara(Misc.ucFirst(mission.getPerson().getHeOrShe()) + " taps a data pad, and " +
						 "an intel assessment shows up on your tripad.", 0f);
			info.addShipList(cols, 1, iconSize, data.fleet.getFaction().getBaseUIColor(), list, opad);
			
			if (data.difficulty >= 9) {
				info.addPara("The station is assessed to be fully functional and extremely dangerous.", opad);
			} else {
				info.addPara("The station is assessed to be damaged, but still highly dangerous.", opad);
			}
			text.addTooltip();
		}
		return;
	}
	
	
	public void addFleetDescription(TooltipMakerAPI info, float width, float height, HubMissionWithBarEvent mission, CustomBountyData data) {
		PersonAPI person = data.fleet.getCommander();
		FactionAPI faction = person.getFaction();
		int cols = 7;
		float iconSize = width / cols;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		
		if (DebugFlags.PERSON_BOUNTY_DEBUG_INFO) {

		}
		boolean deflate = false;
		if (!data.fleet.isInflated()) {
			data.fleet.inflateIfNeeded();
			deflate = true;
		}
		
		List<FleetMemberAPI> list = new ArrayList<FleetMemberAPI>();
		Random random = new Random(person.getNameString().hashCode() * 170000);
		
		List<FleetMemberAPI> members = data.fleet.getFleetData().getMembersListCopy();
		int max = 7;
		for (FleetMemberAPI member : members) {
			if (list.size() >= max) break;
			
			if (member.isFighterWing()) continue;
			
			float prob = (float) member.getFleetPointCost() / 20f;
			prob += (float) max / (float) members.size();
			if (member.isFlagship()) prob = 1f;
			//if (members.size() <= max) prob = 1f;
			
			if (random.nextFloat() > prob) continue;
			
			FleetMemberAPI copy = Global.getFactory().createFleetMember(FleetMemberType.SHIP, member.getVariant());
			if (member.isFlagship()) {
				copy.setCaptain(person);
			}
			list.add(copy);
		}
		
		if (!list.isEmpty()) {
			info.addPara("The bounty posting contains partial intel about the station.", opad);
			info.addShipList(cols, 1, iconSize, faction.getBaseUIColor(), list, opad);
			
			int num = members.size() - list.size();
			//num = Math.round((float)num * (1f + random.nextFloat() * 0.5f));
			
			if (num < 5) num = 0;
			else if (num < 10) num = 5;
			else if (num < 20) num = 10;
			else num = 20;
			
			if (data.difficulty >= 9) {
				info.addPara("The station is assessed to be fully functional and extremely dangerous.", opad);
			} else {
				info.addPara("The station is assessed to be damaged, but still highly dangerous.", opad);
			}
		}
		
		if (deflate) {
			data.fleet.deflate();
		}
	}
	
	
	public List<CampaignFleetAPI> getStations(HubMissionWithBarEvent mission, int difficulty) {
		List<CampaignFleetAPI> stations = new ArrayList<CampaignFleetAPI>();
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (!system.hasTag(Tags.THEME_REMNANT_MAIN)) continue;
			if (system.hasTag(Tags.THEME_REMNANT_DESTROYED)) continue;
			
			for (CampaignFleetAPI fleet : system.getFleets()) {
				if (!fleet.isStationMode()) continue;
				if (!Factions.REMNANTS.equals(fleet.getFaction().getId())) continue;
				
				boolean damaged = fleet.getMemoryWithoutUpdate().getBoolean("$damagedStation");
				if ((difficulty == 7 || difficulty == 8) && damaged) {
					stations.add(fleet);
				} else if (!damaged && difficulty > 8) {
					stations.add(fleet);
				}
			}
		}
		return stations;
	}
	
	public String getBountyNamePostfix(HubMissionWithBarEvent mission, CustomBountyData data) {
		return " - Remnant Nexus";
	}
	
	@Override
	public String getIconName() {
		return Global.getSettings().getSpriteName("campaignMissions", "remnant_bounty");
	}
	
	@Override
	public CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
		CustomBountyData data = new CustomBountyData();
		data.difficulty = difficulty;
		
		
		WeightedRandomPicker<CampaignFleetAPI> picker = new WeightedRandomPicker<CampaignFleetAPI>(mission.getGenRandom());
		picker.addAll(getStations(mission, difficulty));
		
		data.fleet = picker.pick();
		if (data.fleet == null) return null;
		
		data.system = data.fleet.getStarSystem();
		if (data.system == null) return null;
		
		setRepChangesBasedOnDifficulty(data, difficulty);
		data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.REMNANT_STATION_MULT, mission);
		
		return data;
	}
	

	@Override
	public int getMaxDifficulty() {
		return super.getMaxDifficulty();
	}

	@Override
	public int getMinDifficulty() {
		return 7;
	}

}






