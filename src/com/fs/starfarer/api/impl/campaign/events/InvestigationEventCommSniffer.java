package com.fs.starfarer.api.impl.campaign.events;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseOnMessageDeliveryScript;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.comm.MessagePriority;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;


public class InvestigationEventCommSniffer extends InvestigationEvent {

	public InvestigationEventCommSniffer() {
		super();
	}

	@Override
	public void startEvent() {
		if (faction == null || faction.isNeutralFaction()) {
			log.info("Aborting comm sniffer invesitgation on " + entity.getName() + ", null or neutral faction");
			endEvent();
		}
		
		Global.getSector().getIntel().getCommSnifferLocations().remove(entity);
		
		InvestigationEventParams params = 
			new InvestigationEventParams("Comm sniffer investigation - " + entity.getName(), "start_comm_sniffer");
		
		params.warningPriority = MessagePriority.ENSURE_DELIVERY;
		params.minInitialDelay = 0;
		params.maxInitialDelay = 0f;
		
		float guiltMult = getPlayerRepGuiltMult(faction);
		
		InvestigationResult clear = new InvestigationResult(null, null);
		InvestigationResult guilty = new InvestigationResult("player_guilty_comm_sniffer", MessagePriority.ENSURE_DELIVERY);
		params.results.add(clear);
		params.results.add(guilty);
		
		clear.weight = 100f;
		guilty.weight = 25f * guiltMult;
		
		guilty.onDelivery = new BaseOnMessageDeliveryScript() {
			public void beforeDelivery(CommMessageAPI message) {
				Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(RepActions.COMM_SNIFFER_INVESTIGATION_GUILTY, null, message, true), 
						faction.getId());
			}
		};
		
		setParam(params);
		
		super.startEvent();
	}

	@Override
	public Map<String, String> getTokenReplacements() {
		return super.getTokenReplacements();
	}
	
}









