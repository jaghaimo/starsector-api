package com.fs.starfarer.api.util;

import java.awt.Color;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;

public class FleetLog {

	public static CommMessageAPI beginEntry(String subject, SectorEntityToken target) {
		return beginEntry(subject, target, null, (String []) null);
	}
	
	public static CommMessageAPI beginEntry(String subject, SectorEntityToken target, Color highlight, String... highlights) {
		return beginEntry(subject, target, Misc.getTextColor(), highlight, highlights);
	}
	
	public static CommMessageAPI beginEntry(String subject, SectorEntityToken target, Color color, Color highlight, String... highlights) {
		return null;
//		CommMessageAPI message = Global.getFactory().createMessage();
//		message.setSubject(subject);
//		if (color != null) {
//			message.setSubjectColor(color);
//		}
//		if (highlight != null) {
//			Highlights h = new Highlights();
//			for (String curr : highlights) {
//				h.append(curr, highlight);
//			}
//			message.setSubjectHighlights(h);
//		}
//		
//		
//		message.getSection1().setTitle("Summary");
//		
//		//message.setDeliveredBy("Delivered by");
//		
//		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
//		if (target == null) target = player;
//		message.setLocationString(target.getContainingLocation().getNameWithLowercaseType());
//		if (target.getContainingLocation() instanceof StarSystemAPI) {
//			message.setStarSystemId(target.getContainingLocation().getId());
//			if (target != player) message.setCenterMapOnEntity(target);
//		} else {
//			message.setLocInHyper(target.getLocationInHyperspace());
//			if (target != player) message.setCenterMapOnEntity(target);
//		}
//		
//		
//		message.setType("Log Entry");
//		message.setShortType("Fleet log");
//		//message.setSender("Exploration");
//		
//		message.setTimeReceived(Global.getSector().getClock().getTimestamp());
//		message.setTimeSent(Global.getSector().getClock().getTimestamp());
//		
////		message.getSection1().setTitle("Section 1");
////		message.getSection1().addPara("Test paragraph one");
//		message.setSmallIcon(Global.getSettings().getSpriteName("intel_categories", "star_systems"));
//		return message;
	}
	
	
	public static void addToLog(CommMessageAPI message, TextPanelAPI panel) {
//		message.setShowInCampaignList(panel == null);
//		message.setAddToIntelTab(true);
//		message.setAction(MessageClickAction.INTEL_TAB);
//		message.addTag(Tags.FLEET_LOG);
//		
//		Global.getSector().getCampaignUI().addMessage(message);
//		
//		if (panel != null) {
//			panel.setFontSmallInsignia();
//			panel.addParagraph("Fleet log entry added", Misc.getTooltipTitleAndLightHighlightColor());
//			panel.setFontInsignia();
//		}
	}
	
	
}





