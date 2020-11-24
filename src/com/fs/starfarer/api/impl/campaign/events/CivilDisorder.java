package com.fs.starfarer.api.impl.campaign.events;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.MessagePriority;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class CivilDisorder extends BaseEventPlugin {

	private float elapsedDays = 0f;
	private CampaignEventTarget eventTarget;

	private boolean started = false;
	private int stage = 0;
	private String type;

	// called when there's the possibility of the event happening
	// doesn't mean it'll actually happen, needs to pass the internal probability check first
	public void init(String type, CampaignEventTarget eventTarget) {
		this.type = type;
		this.eventTarget = eventTarget;
	}
	
	// event passed the probability check and is happening
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
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>();
			picker.add("start1");
			picker.add("start2");
			picker.add("start3");
			Global.getSector().reportEventStage(this, picker.pick(), MessagePriority.SECTOR);
			stage++;
		}
		
		if (elapsedDays > 1 && stage == 1) {
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>();
			picker.add("ending1");
			picker.add("ending2");
			picker.add("ending3");
			picker.add("ending4");
			Global.getSector().reportEventStage(this, picker.pick(), MessagePriority.SECTOR);
			stage++;
		}
		
		if (elapsedDays > 2 && stage == 2) {
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>();
			picker.add("end1");
			picker.add("end2");
			Global.getSector().reportEventStage(this, picker.pick(), MessagePriority.SECTOR);
			stage++;
		}
	}

	public boolean isDone() {
		return elapsedDays > 5f || stage >= 3;
	}

	public CampaignEventTarget getEventTarget() {
		return eventTarget;
	}

	public String getEventType() {
		return type;
	}

	// tokens that are used in reports.csv for the stages of this event
	public Map<String, String> getTokenReplacements() {
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("$market", eventTarget.getEntity().getName());
		map.put("$rulingFaction", eventTarget.getEntity().getFaction().getDisplayName());
		map.put("$rulingParty", eventTarget.getEntity().getFaction().getDisplayName());
		
		map.put("$theRulingFaction", eventTarget.getEntity().getFaction().getDisplayNameWithArticle());
		map.put("$TheRulingFaction", Misc.ucFirst(eventTarget.getEntity().getFaction().getDisplayNameWithArticle()));
		
		return map;
		
	}
	
	// how far to send an "event is possible" type report
	public MessagePriority getWarningWhenPossiblePriority() {
		return MessagePriority.SECTOR;
	}
	
	// how far to send an "event is likely" type report
	public MessagePriority getWarningWhenLikelyPriority() {
		return MessagePriority.SECTOR;
	}

	// pick an event_stage for an "event is likely" report.
	// called every so often while an event is likely.
	public String getStageIdForLikely() {
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>();
		picker.add("possible1");
		picker.add("possible2");
		picker.add("possible3");
		picker.add("possible4");
		return picker.pick();
		//return "likely";
	}

	// pick an event_stage for an "event is possible" report.
	// called every so often while an event is possible.
	public String getStageIdForPossible() {
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>();
		picker.add("possible1");
		picker.add("possible2");
		picker.add("possible3");
		picker.add("possible4");
		return picker.pick();
	}

	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	public void setParam(Object param) {
		
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




