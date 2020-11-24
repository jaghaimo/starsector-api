package com.fs.starfarer.api.impl.campaign.events;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * 
 * NOT ACTUALLY USED - see FleetLog.java instead.
 * @author Alex Mosolov
 *
 * Copyright 2014 Fractal Softworks, LLC
 */
public class FleetLogEvent extends BaseEventPlugin {
	
	public static Logger log = Global.getLogger(FleetLogEvent.class);
	
	public void init(String type, CampaignEventTarget eventTarget) {
		super.init(type, eventTarget);
		readResolve();
	}
	
	Object readResolve() {
		return this;
	}
	
	public void startEvent() {
		super.startEvent();
	}
	
	public void advance(float amount) {
		if (!isEventStarted()) return;
		if (isDone()) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		
//		if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
//			CommMessageAPI message = Global.getFactory().createMessage();
//			message.setSubject("Test MESSAGE");
//			message.getSection1().setTitle("Section 1");
//			message.getSection1().addPara("Test paragraph one");
//			message.setAction(MessageClickAction.INTEL_TAB);
//			message.setCustomData(this);
//			message.setAddToIntelTab(true);
//			message.setSmallIcon(Global.getSettings().getSpriteName("intel_categories", "star_systems"));
//			Global.getSector().getCampaignUI().addMessage(message);
//		}
	}
	
	
	
	@Override
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
//		if (action.equals("printSkills")) {
//			String personId = params.get(1).getString(memoryMap);
//		}
		
		return true;
	}

	
	public Map<String, String> getTokenReplacements() {
		Map<String, String> map = super.getTokenReplacements();
		return map;
	}

	@Override
	public String[] getHighlights(String stageId) {
		return null;
	}
	
	@Override
	public Color[] getHighlightColors(String stageId) {
		return super.getHighlightColors(stageId);
	}
	
	
	private CampaignEventTarget tempTarget = null;
	
	@Override
	public CampaignEventTarget getEventTarget() {
		if (tempTarget != null) return tempTarget;
		return super.getEventTarget();
	}

	public boolean isDone() {
		return false;
	}
	
	@Override
	public CampaignEventCategory getEventCategory() {
		return CampaignEventCategory.DO_NOT_SHOW_IN_MESSAGE_FILTER;
	}
	
	public boolean showAllMessagesIfOngoing() {
		return false;
	}
}










