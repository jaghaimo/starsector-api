package com.fs.starfarer.api.campaign.events;

import java.util.Collection;

public interface ReportAPI {
	boolean hasTag(String tag);
	Collection<String> getTags();
	String getEventType();
	String getEventStage();
	float getProbability();
	String getChannel();
	String getSubject();
	String getSummary();
	String getAssessment();
	String getImage();
}
