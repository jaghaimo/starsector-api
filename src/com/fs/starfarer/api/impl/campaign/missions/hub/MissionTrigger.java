package com.fs.starfarer.api.impl.campaign.missions.hub;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission.ConditionChecker;

public class MissionTrigger {

	public static class TriggerActionContext {
		public HubMission mission;
		public CampaignFleetAPI fleet;
		public SectorEntityToken entity;
		public SectorEntityToken token;
		public PersonAPI person;
		public MarketAPI market;
		public PlanetAPI planet;
		public StarSystemAPI system;
		public int counter;
		
		public LocationAPI containingLocation;
		public Vector2f coordinates;
		public SectorEntityToken jumpPoint;
		
		public Map<String, Object> custom = new LinkedHashMap<String, Object>();
		public Object custom1;
		public Object custom2;
		public Object custom3;
		
		public String patrolText;
		public String travelText;
		
		public boolean makeAllFleetFlagsPermanent = false;
		
		public List<CampaignFleetAPI> allFleets = new ArrayList<CampaignFleetAPI>();

		public TriggerActionContext(HubMission mission) {
			this.mission = mission;
		}
		
	}
	
	public static interface TriggerAction {
		void doAction(TriggerActionContext context);
	}
	
	
	/**
	 * Optional, null by default.
	 * Set by calling
	 * getCurrentTrigger().setId(id)
	 * After beginning a trigger.
	 */
	protected String id = null;
	protected ConditionChecker condition;
	protected LinkedHashSet<Object> stages = new LinkedHashSet<Object>();
	protected List<TriggerAction> actions = new ArrayList<TriggerAction>();
	
	public ConditionChecker getCondition() {
		return condition;
	}
	public void setCondition(ConditionChecker condition) {
		this.condition = condition;
	}
	public LinkedHashSet<Object> getStages() {
		return stages;
	}
	public List<TriggerAction> getActions() {
		return actions;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
