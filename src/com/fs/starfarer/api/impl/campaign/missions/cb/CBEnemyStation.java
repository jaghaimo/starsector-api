package com.fs.starfarer.api.impl.campaign.missions.cb;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.missions.cb.BaseCustomBounty.Stage;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class CBEnemyStation extends BaseCustomBountyCreator {

	@Override
	public float getBountyDays() {
		return CBStats.ENEMY_STATION_DAYS;
	}

	@Override
	public float getFrequency(HubMissionWithBarEvent mission, int difficulty) {
		if (getStations(mission, difficulty).isEmpty()) return 0f;
		return super.getFrequency(mission, difficulty) * CBStats.ENEMY_STATION_FREQ;
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
			
			FactionAPI f = data.fleet.getFaction();
			info.addPara("The station is controlled by " + f.getDisplayNameWithArticle() + " and is likely to " +
					"be supported by a number of patrols.",
					opad, f.getBaseUIColor(),
					f.getDisplayNameWithArticleWithoutArticle());
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
		
		List<FleetMemberAPI> members = data.fleet.getFleetData().getMembersListCopy();
		int max = 7;
		for (FleetMemberAPI member : members) {
			if (list.size() >= max) break;
			
			if (member.isFighterWing()) continue;
			
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
			
			FactionAPI f = data.fleet.getFaction();
			info.addPara("The station is controlled by " + f.getDisplayNameWithArticle() + " and is likely to " +
					"be supported by a number of patrols.",
					opad, f.getBaseUIColor(),
					f.getDisplayNameWithArticleWithoutArticle());
		}
		
		if (deflate) {
			data.fleet.deflate();
		}
	}
	
	
	public List<CampaignFleetAPI> getStations(HubMissionWithBarEvent mission, int difficulty) {
		List<CampaignFleetAPI> stations = new ArrayList<CampaignFleetAPI>();
		String faction = mission.getPerson().getFaction().getId();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isHidden()) continue;
			if (market.isPlayerOwned()) continue;
			if (market.hasCondition(Conditions.DECIVILIZED)) continue;
			if (!market.getFaction().isHostileTo(faction)) continue;
			
			Industry ind = Misc.getStationIndustry(market);
			
			if (!(ind instanceof OrbitalStation)) continue;
			OrbitalStation os = (OrbitalStation) ind;
			if (os.getDisruptedDays() > 0) continue;
			
			boolean battlestation = os.getSpec().hasTag(Industries.TAG_BATTLESTATION);
			boolean starfortress = os.getSpec().hasTag(Industries.TAG_STARFORTRESS);
			
			if (difficulty == 10 && !starfortress) continue;
			if ((difficulty == 8 || difficulty == 9) && !battlestation) continue;
			if (!battlestation && !starfortress && difficulty >= 8) continue;
			
			CampaignFleetAPI fleet = Misc.getStationFleet(market);
			if (fleet != null) {
				stations.add(fleet);
			}
		}
		return stations;
	}
	
	public String getBountyNamePostfix(HubMissionWithBarEvent mission, CustomBountyData data) {
		return " - " + data.fleet.getName();
	}
	
	
	@Override
	public CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
		CustomBountyData data = new CustomBountyData();
		data.difficulty = difficulty;
		
		
		WeightedRandomPicker<CampaignFleetAPI> picker = new WeightedRandomPicker<CampaignFleetAPI>(mission.getGenRandom());
		picker.addAll(getStations(mission, difficulty));
		
		
		data.fleet = picker.pick();
		if (data.fleet == null) return null;
		
		MarketAPI market = Misc.getStationMarket(data.fleet);
		if (market == null) return null;
		
		data.market = market;
		data.custom2 = bountyStage;
		
		data.system = data.fleet.getStarSystem();
		if (data.system == null) return null;
		
		setRepChangesBasedOnDifficulty(data, difficulty);
		data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.ENEMY_STATION_MULT, mission);
		
		return data;
	}
	
	@Override
	public void notifyAccepted(MarketAPI createdAt, HubMissionWithBarEvent mission, CustomBountyData data) {
		MarketAPI market = data.market;
		Object stage = data.custom2;
		
		if (data.difficulty <= 7) {
			mission.triggerCreateLargePatrolAroundMarket(market, stage, 0f);
			mission.triggerCreateSmallPatrolAroundMarket(market, stage, 0f);
		} else if (data.difficulty <= 9) {
			mission.triggerCreateLargePatrolAroundMarket(market, stage, 0f);
			mission.triggerCreateMediumPatrolAroundMarket(market, stage, 0f);
			mission.triggerCreateSmallPatrolAroundMarket(market, stage, 0f);
		} else {
			mission.triggerCreateLargePatrolAroundMarket(market, stage, 0f);
			mission.triggerCreateLargePatrolAroundMarket(market, stage, 0f);
			mission.triggerCreateMediumPatrolAroundMarket(market, stage, 0f);
			mission.triggerCreateSmallPatrolAroundMarket(market, stage, 0f);
		}
		
		mission.connectWithHostilitiesEnded(Stage.BOUNTY, Stage.FAILED_NO_PENALTY, mission.getPerson(), market);
		mission.setStageOnHostilitiesEnded(Stage.FAILED_NO_PENALTY, mission.getPerson(), market);
	}
	
	@Override
	public String getIconName() {
		return Global.getSettings().getSpriteName("campaignMissions", "station_bounty");
	}
	

	@Override
	public int getMaxDifficulty() {
		return super.getMaxDifficulty();
	}

	@Override
	public int getMinDifficulty() {
		return 6;
	}

}






