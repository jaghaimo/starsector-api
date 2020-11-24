package com.fs.starfarer.api.campaign.comm;

import java.awt.Color;

public interface MessageParaAPI {
	String getHeading();
	void setHeading(String heading);
	String getBody();
	void setBody(String body);
	Color getHeadingColor();
	void setHeadingColor(Color headingColor);
	Color getBodyColor();
	void setBodyColor(Color bodyColor);
}
