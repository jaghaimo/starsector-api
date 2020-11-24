package com.fs.starfarer.api.impl.campaign.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.OnMessageDeliveryScript;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.comm.MessagePriority;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 * Should only be used if started directly (not via probability), as that's the only
 * way the investigation parameters can be set.
 * 
 * If using event probability, should subclass this for a new event instead,
 * and set the parameters in startEvent() of the subclass. See SmugglingInvestigationEvent as example.
 *  
 * @author Alex Mosolov
 *
 * Copyright 2014 Fractal Softworks, LLC
 */
public class InvestigationEvent extends BaseEventPlugin {

	public static Logger log = Global.getLogger(InvestigationEvent.class);
	
//	public static final String START = "start";
//	public static final String CLEAR = "clear";
//	public static final String PLAYER_GUILTY = "player_guilty";
//	public static final String PLAYER_GUILTY_FALSE = "player_false";
//	public static final String OTHER_GUILTY = "other_guilty";
	
	public static class InvestigationResult {
		public String stageName;
		public float weight = 0;
		public MessagePriority priority = MessagePriority.SECTOR;
		public OnMessageDeliveryScript onDelivery = null;
		public InvestigationResult(String stage, MessagePriority priority) {
			this.stageName = stage;
			this.priority = priority;
		}
	}
	
	public static class InvestigationEventParams {
		public String name;
		public String startStage;
		public String warningSender = null;
		public MessagePriority warningPriority = MessagePriority.SECTOR;
		public List<InvestigationResult> results = new ArrayList<InvestigationResult>();
		public float minInitialDelay = 1f; 
		public float maxInitialDelay = 6f;
		public float minDuration = 10f; 
		public float maxDuration = 15f;
		public InvestigationEventParams(String name, String startStage) {
			this.name = name;
			this.startStage = startStage;
		}
		
	}
	
	private float elapsedDays = 0f;
	private float initialDelay = 0f;
	private float duration = 0f;
	
	private InvestigationEventParams params;
	
	public void init(String type, CampaignEventTarget eventTarget) {
		super.init(type, eventTarget);
	}
	
	@Override
	public void setParam(Object param) {
		params = (InvestigationEventParams) param;
	}

	public void startEvent() {
		super.startEvent();
		
		if (market != null && market.hasCondition(Conditions.DECIVILIZED)) {
			endEvent();
			return;
		}
		
		initialDelay = params.minInitialDelay + (params.maxInitialDelay - params.minInitialDelay) * (float) Math.random();
		duration = params.minDuration + (params.maxDuration - params.minDuration) * (float) Math.random();
		
		log.info(String.format("Starting investigation with suffix \"%s\" at %s. Delay: %f, dur: %f", 
								params.startStage, getTargetName(), initialDelay, duration));
	}
	
	private int stage = 0;
	public void advance(float amount) {
		if (!isEventStarted()) return;
		if (isDone()) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		elapsedDays += days;
		
		if (elapsedDays >= initialDelay && stage == 0) {
			stage++;
			log.info("Reporting investigation stage " + params.startStage + " at priority " + params.warningPriority.name());
			Global.getSector().reportEventStage(this, params.startStage, params.warningPriority);
		}
		
		if (elapsedDays >= initialDelay + duration && stage == 1) {
			WeightedRandomPicker<InvestigationResult> picker = new WeightedRandomPicker<InvestigationResult>();
			
			for (InvestigationResult result : params.results) {
				picker.add(result, result.weight);
			}
			InvestigationResult result = picker.pick();
			if (result.stageName != null) {
				Global.getSector().reportEventStage(this, result.stageName, null, result.priority, result.onDelivery);
				log.info("Investigation outcome: " + result.stageName + " at priority " + result.priority);
			}
			
			endEvent();
		}
	}
	
	
	@Override
	public String getEventName() {
		return params.name;
	}
	
	

	@Override
	public Map<String, String> getTokenReplacements() {
		Map<String, String> map = super.getTokenReplacements();
		if (params.warningSender != null) {
			map.put("$sender", params.warningSender);
		}
		return map;
	}

	private boolean ended = false;
	protected void endEvent() {
		ended = true;
	}

	public boolean isDone() {
		return ended;
	}

	public static float getPlayerRepGuiltMult(FactionAPI faction) {
		FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
		//RepLevel level = market.getFaction().getRelationshipLevel(player);
		RepLevel level = faction.getRelationshipLevel(player);
		switch (level) {
		case COOPERATIVE:
			return 0.1f;
		case FRIENDLY:
			return 0.2f;
		case WELCOMING:
			return 0.3f;
		case FAVORABLE:
			return 0.5f;
		case NEUTRAL:
			return 1f;
		case SUSPICIOUS:
			return 1.5f;
		case INHOSPITABLE:
			return 2f;
		case HOSTILE:
			return 5f;
		case VENGEFUL:
			return 10f;
		}
		return 1f;
	}
	
}




