package com.fs.starfarer.api.impl.campaign.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.events.CampaignEventManagerAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

public class TestCampaignMission extends BaseCampaignPlugin {
	
	private final SectorEntityToken entity; 
	private PersonAPI contact = null;
	private CampaignEventPlugin event;
	
	public TestCampaignMission(SectorEntityToken entity) {
		this.entity = entity;
		this.contact = entity.getFaction().createRandomPerson();
		
		CampaignEventManagerAPI eventManager = Global.getSector().getEventManager();
		event = eventManager.primeEvent(null, "test_mission", this);
	}
	
	public String getDescription() {
		return null;
	}

	public String getMissionIcon() {
		return Global.getSettings().getSpriteName("campaignMissions", "test");
	}

	public String getName() {
		return "Test mission";
	}

	public String getTypeId() {
		return "test";
	}

	public void playerAccept() {
		CampaignEventManagerAPI eventManager = Global.getSector().getEventManager();
		//eventManager.startEvent(null, "test_mission", this);
		eventManager.startEvent(event);
	}

	public SectorEntityToken getEntity() {
		return entity;
	}

	public PersonAPI getContact() {
		return contact;
	}

	public void advance(float amount) {
		
	}

	public CampaignEventPlugin getPrimedEvent() {
		return event;
	}

	public PersonAPI getImportantPerson() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getCreationTimestamp() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getFactionId() {
		return Factions.INDEPENDENT;
	}
	
}




