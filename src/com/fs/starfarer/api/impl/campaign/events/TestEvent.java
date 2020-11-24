package com.fs.starfarer.api.impl.campaign.events;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.MessagePriority;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;

public class TestEvent extends BaseEventPlugin {

	private float elapsedDays = 0f;
	private CampaignEventTarget eventTarget;

	private boolean started = false;
	private int stage = 0;
	private String type;

	public void init(String type, CampaignEventTarget eventTarget) {
		this.type = type;
		this.eventTarget = eventTarget;
	}
	
	public void startEvent() {
		started = true;
	}
	
	public void advance(float amount) {
		if (Global.getSector().isPaused()) return;
		if (!started) return;
		
		//System.out.println("Advance() in TestEvent called");
		float days = Global.getSector().getClock().convertToDays(amount);
		
		elapsedDays += days;
		
		if (elapsedDays > 0 && stage == 0) {
			System.out.println("Test event reporting stage start");
			Global.getSector().reportEventStage(this, "start", MessagePriority.SECTOR);
			stage++;
		}
		
		if (elapsedDays > 1 && stage == 1) {
			System.out.println("Test event reporting stage warning_relief");
			Global.getSector().reportEventStage(this, "warning_relief", MessagePriority.SECTOR);
			stage++;
		}
		
		if (elapsedDays > 2 && stage == 2) {
			System.out.println("Test event reporting stage relief_sent");
			Global.getSector().reportEventStage(this, "relief_sent", MessagePriority.SYSTEM);
			stage++;
		}
		
		if (elapsedDays > 3 && stage == 3) {
			System.out.println("Test event reporting stage relief_arrived");
			Global.getSector().reportEventStage(this, "relief_arrived", MessagePriority.SYSTEM);
			stage++;
		}
		
//		if (elapsedDays > 4 && stage == 4) {
//			System.out.println("Test event reporting stage end");
//			Global.getSector().reportEventStage(this, "end", MessagePriority.SECTOR);
//			stage++;
//		}
	}

	public boolean isDone() {
		return elapsedDays > 15f || stage >= 4;
	}

	public CampaignEventTarget getEventTarget() {
		return eventTarget;
	}

	public String getEventType() {
		return type;
	}

	public Map<String, String> getTokenReplacements() {
		Map<String, String> map = new HashMap<String, String>();

		/*
		 $market
		 $marketSystem
		 $targetFaction
		 $reliefSystem
		 $reliefEntity
		 $playerName
		 */
		
		map.put("$market", eventTarget.getEntity().getName());
		
		if (eventTarget.getLocation() instanceof StarSystemAPI) {
			map.put("$marketSystem", ((StarSystemAPI)eventTarget.getLocation()).getBaseName() + " star system");
		} else {
			map.put("$marketSystem", "hyperspace");
		}
		map.put("$targetFaction", eventTarget.getEntity().getFaction().getDisplayName());
		map.put("$reliefSystem", "Askonia");
		map.put("$reliefEntity", "Volturn");
		
		
		return map;
		
	}
	
	
	public MessagePriority getWarningWhenPossiblePriority() {
		return MessagePriority.SECTOR;
	}
	
	public MessagePriority getWarningWhenLikelyPriority() {
		return MessagePriority.SECTOR;
	}

	public String getStageIdForLikely() {
		return "likely";
	}

	public String getStageIdForPossible() {
		return "possible";
	}

	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	public void setParam(Object param) {
		// TODO Auto-generated method stub
		
	}

	public boolean allowMultipleOngoingForSameTarget() {
		// TODO Auto-generated method stub
		return false;
	}

	public Color[] getHighlightColors(String stageId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getHighlights(String stageId) {
		// TODO Auto-generated method stub
		return null;
	}

}




