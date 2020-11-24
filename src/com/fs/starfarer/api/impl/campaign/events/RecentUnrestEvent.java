package com.fs.starfarer.api.impl.campaign.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;

public class RecentUnrestEvent extends BaseEventPlugin {

	public static final float DAYS_PER_STAGE = 10f;
	private float elapsedDays = 0f;
	private int stabilityPenalty = 0;
	private String conditionToken = null;
	
	public void init(String type, CampaignEventTarget eventTarget) {
		super.init(type, eventTarget, false);
	}
	
	public void startEvent() {
		super.startEvent(true);
		if (market == null) {
			endEvent();
			return;
		}
		
		conditionToken = market.addCondition(Conditions.RECENT_UNREST, this);
	}
	
	public void advance(float amount) {
		if (!isEventStarted()) return;
		if (isDone()) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		elapsedDays += days;
		
		if (elapsedDays >= DAYS_PER_STAGE) {
			elapsedDays -= DAYS_PER_STAGE;
			stabilityPenalty--;
			market.reapplyCondition(conditionToken);
		}
		
		if (stabilityPenalty <= 0) {
			endEvent();
		}
	}
	
	private boolean ended = false;
	private void endEvent() {
		if (market != null && conditionToken != null) {
			market.removeSpecificCondition(conditionToken);
		}
		ended = true;
	}

	public boolean isDone() {
		return ended;
	}

	public int getStabilityPenalty() {
		return stabilityPenalty;
	}

	public void setStabilityPenalty(int stabilityPenalty) {
		if (isDone()) return;
		
		this.stabilityPenalty = stabilityPenalty;
		if (stabilityPenalty <= 0) {
			endEvent();
		} else {
			market.reapplyCondition(conditionToken);
		}
	}
	
	public void increaseStabilityPenalty(int penalty) {
		if (isDone()) return;
		
		this.stabilityPenalty += penalty;
		if (stabilityPenalty <= 0) {
			endEvent();
		} else {
			market.reapplyCondition(conditionToken);
		}
	}
	
	public void reduceStabilityPenalty(int penalty) {
		if (isDone()) return;
		
		this.stabilityPenalty -= penalty;
		if (stabilityPenalty <= 0) {
			endEvent();
		} else {
			market.reapplyCondition(conditionToken);
		}
	}
	
	public String getEventName() {
		if (isDone()) return "Recent unrest at null market";
		return "Recent unrest at " + market.getName();
	}
}




