package com.fs.starfarer.api.ui;

public interface UIPanelAPI extends UIComponentAPI {

	PositionAPI addComponent(UIComponentAPI custom);
	void removeComponent(UIComponentAPI component);
	void bringComponentToTop(UIComponentAPI c);
}
