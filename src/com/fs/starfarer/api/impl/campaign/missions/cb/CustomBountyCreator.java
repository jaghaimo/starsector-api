package com.fs.starfarer.api.impl.campaign.missions.cb;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission.Abortable;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch.StarSystemRequirement;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface CustomBountyCreator extends StarSystemRequirement {
	
	public static int MIN_DIFFICULTY = 0;
	public static int MAX_DIFFICULTY = 10;
	
	public class CustomBountyData {
		public Map<Object, Object> customMap = new HashMap<Object, Object>();
		public Object custom1;
		public Object custom2;
		public Object custom3;
		public Object stage;
		
		public int baseReward;
		public float repPerson = RepRewards.MEDIUM;
		public float repFaction = RepRewards.TINY;
		public CampaignFleetAPI fleet;
		public StarSystemAPI system;
		public MarketAPI market;
		public int difficulty;
		
		public List<Abortable> abortWhenOtherVersionAccepted = new ArrayList<Abortable>();
	}
	
	
	String getId();
	float getFrequency(HubMissionWithBarEvent mission, int difficulty);
	
	CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage);
	void notifyAccepted(MarketAPI createdAt, HubMissionWithBarEvent mission, CustomBountyData data);
	
	void addIntelAssessment(TextPanelAPI text, HubMissionWithBarEvent mission, CustomBountyData data);
	void addFleetDescription(TooltipMakerAPI info, float width, float height, HubMissionWithBarEvent mission, CustomBountyData data);
	
	void notifyCompleted(HubMissionWithBarEvent mission, CustomBountyData data);
	void notifyFailed(HubMissionWithBarEvent mission, CustomBountyData data);
	
	int getMinDifficulty();
	int getMaxDifficulty();
	
	StarSystemAPI getSystemWithNoTimeLimit(CustomBountyData data);
	
	String getBaseBountyName(HubMissionWithBarEvent mission, CustomBountyData data);
	String getBountyNamePostfix(HubMissionWithBarEvent mission, CustomBountyData data);
	
	float getBountyDays();
	
	void updateInteractionData(HubMissionWithBarEvent mission, CustomBountyData data);
	void addTargetLocationAndDescription(TooltipMakerAPI info, float width, float height, HubMissionWithBarEvent mission, CustomBountyData data);
	void addTargetLocationAndDescriptionBulletPoint(TooltipMakerAPI info, Color tc, float pad, HubMissionWithBarEvent mission, CustomBountyData data);
	String getIconName();
}
