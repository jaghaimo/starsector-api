package com.fs.starfarer.api.campaign;

import java.awt.Color;
import java.util.List;


public interface CustomEntitySpecAPI {

	String getNameInText();
	String getAOrAn();
	String getIsOrAre();
	boolean isRenderCircleIndicatorSelectionFlash();
	boolean isRenderCircleIndicator();
	boolean isScaleIconWithZoom();
	boolean isInteractable();
	String getSheetName();
	float getSheetCellSize();
	String getInteractionImage();
	boolean isUseLightColor();
	boolean isRenderShadow();
	boolean isScaleNameWithZoom();
	Color getNameShadowColor();
	boolean isShowInCampaign();
	float getDefaultRadius();
	float getIconWidth();
	float getIconHeight();
	float getSpriteWidth();
	float getSpriteHeight();
	boolean isShowIconOnMap();
	boolean isShowNameOnMap();
	String getNameFont();
	float getNameFontScale();
	float getNameAngle();
	Color getNameColor();
	String getDefaultName();
	String getCustomDescriptionId();
	List<String> getTags();
	List<CampaignEngineLayers> getLayers();
	String getId();
	String getIconName();
	String getSpriteName();
	String getPluginClassName();
	CustomCampaignEntityPlugin getPlugin();
	String getShortName();
	boolean isScaleIconSizeBasedOnDefaultRadius();
	float getDetectionRange();
	void setDetectionRange(float detectionRange);
	void setDiscoverable(boolean discoverable);
	boolean isDiscoverable();
	Color getIconColor();
	void setIconColor(Color iconColor);
	float getDiscoveryXP();
	void setDiscoveryXP(float discoveryXP);

}