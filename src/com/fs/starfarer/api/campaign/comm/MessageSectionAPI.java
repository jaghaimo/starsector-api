package com.fs.starfarer.api.campaign.comm;

import java.awt.Color;
import java.util.List;


public interface MessageSectionAPI {
	String getTitle();
	List<MessageParaAPI> getBody();
	boolean isEmpty();
	void setTitle(String title);
	void setHighlights(String ... highlights);
	String[] getHighlights();
	Color[] getHighlightColors();
	void setHighlightColors(Color ... highlightColors);
	void addFirstPara(String heading, Color headingColor, String body, Color bodyColor);
	void addPara(String heading, Color headingColor, String body, Color bodyColor);
	void addPara(String heading, Color headingColor, String body);
	void addPara(String heading, String body);
	void addHeading(String heading, Color headingColor);
	void addFirstHeading(String heading, Color headingColor);
	void addPara(String body);
}
