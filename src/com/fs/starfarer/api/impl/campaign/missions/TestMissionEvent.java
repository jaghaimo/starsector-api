package com.fs.starfarer.api.impl.campaign.missions;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.comm.MessagePriority;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.events.BaseEventPlugin;
import com.fs.starfarer.api.util.Misc.Token;

public class TestMissionEvent extends BaseEventPlugin {
	
	private TestCampaignMission mission = null;
	private String contactCommEntryId = null;
	
	private float elapsedDays = 0;
	public void init(String type, CampaignEventTarget eventTarget) {
		super.init(type, eventTarget);
	}
	
	@Override
	public void setParam(Object param) {
		mission = (TestCampaignMission) param;
	}

	public void startEvent() {
		super.startEvent();
		
		Global.getSector().reportEventStage(this, "start", Global.getSector().getPlayerFleet(), MessagePriority.DELIVER_IMMEDIATELY);
		mission.getEntity().getMarket().addPerson(mission.getContact());
		contactCommEntryId = mission.getEntity().getMarket().getCommDirectory().addPerson(mission.getContact());
		mission.getContact().getMemoryWithoutUpdate().set("$playerContactForTestMission", true);
		mission.getContact().getMemoryWithoutUpdate().set("$testMissionEventRef", this);
		
		mission.getContact().incrWantsToContactReasons();
	}
	
	public void advance(float amount) {
		if (!isEventStarted()) return;
		if (isDone()) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		elapsedDays += days;
		
//		if (elapsedDays >= 1f) {
//			endEvent();
//		}
	}
	
	private boolean ended = false;
	private void endEvent() {
		ended = true;
		mission.getEntity().getMarket().getCommDirectory().removeEntry(contactCommEntryId);
		mission.getEntity().getMarket().removePerson(mission.getContact());
		
		mission.getContact().getMemoryWithoutUpdate().unset("$playerContactForTestMission");
		mission.getContact().getMemoryWithoutUpdate().unset("$testMissionEventRef");
		
		mission.getContact().decrWantsToContactReasons();
		
		mission = null;
		contactCommEntryId = null;
	}

	public boolean isDone() {
		return ended;
	}

	public String getEventName() {
		//return mission.getName();
		return "Test mission";
	}

	@Override
	public CampaignEventCategory getEventCategory() {
		return CampaignEventCategory.MISSION;
	}

	@Override
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String result = params.get(0).getString(memoryMap);
		
		if (result.equals("success")) {
			Global.getSector().reportEventStage(this, "end_success", Global.getSector().getPlayerFleet(), MessagePriority.DELIVER_IMMEDIATELY);
			endEvent();
		} else if (result.equals("failure")) {
			Global.getSector().reportEventStage(this, "end_failure", Global.getSector().getPlayerFleet(), MessagePriority.DELIVER_IMMEDIATELY);
			endEvent();
		}
		return true;
	}

	@Override
	public void reportPlayerOpenedMarket(MarketAPI market) {
		super.reportPlayerOpenedMarket(market);
	}


	
	
}



