package com.fs.starfarer.api.impl.campaign.fleets;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.loading.VariantSource;

public class PersonalFleetHoracioCaden extends PersonalFleetScript {
	
	public PersonalFleetHoracioCaden() {
		super(People.CADEN);
		setMinRespawnDelayDays(10f);
		setMaxRespawnDelayDays(20f);
	}

	@Override
	public CampaignFleetAPI spawnFleet() {
		
		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = sindria.getLocationInHyperspace();
		
		m.triggerCreateFleet(FleetSize.HUGE, FleetQuality.VERY_HIGH, Factions.LIONS_GUARD, FleetTypes.PATROL_LARGE, loc);
		m.triggerSetFleetOfficers( OfficerNum.MORE, OfficerQuality.DEFAULT);
		m.triggerSetFleetCommander(getPerson());
		m.triggerSetFleetFaction(Factions.DIKTAT);
		m.triggerSetPatrol();
		m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, sindria);
		m.triggerFleetSetNoFactionInName();
		m.triggerFleetSetName("Lion's Guard Grand Armada");
		m.triggerPatrolAllowTransponderOff();
		m.triggerFleetSetPatrolActionText("parading");
		m.triggerOrderFleetPatrol(sindria.getStarSystem());
		
		CampaignFleetAPI fleet = m.createFleet();
		fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
		sindria.getContainingLocation().addEntity(fleet);
		fleet.setLocation(sindria.getPlanetEntity().getLocation().x, sindria.getPlanetEntity().getLocation().y);
		fleet.setFacing((float) random.nextFloat() * 360f);
		
		// for the Lion's Guard only; make the Executor use the default with-special-weapons variant
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			if (member.isCapital()) {
				member.setVariant(member.getVariant().clone(), false, false);
				member.getVariant().setSource(VariantSource.REFIT);
				member.getVariant().addTag(Tags.TAG_NO_AUTOFIT);
				member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
			}
		}
		
		return fleet;
	}

	@Override
	public boolean canSpawnFleetNow() {
		MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
		if (sindria == null || sindria.hasCondition(Conditions.DECIVILIZED)) return false;
		if (!sindria.hasIndustry(Industries.LIONS_GUARD)) return false;
		if (!sindria.getFactionId().equals(Factions.DIKTAT)) return false;
		return true;
	}

	@Override
	public boolean shouldScriptBeRemoved() {
		return false;
	}

}




