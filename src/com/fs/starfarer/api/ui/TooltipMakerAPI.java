package com.fs.starfarer.api.ui;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.FleetMemberAPI;



/**
 * Not just for tooltips; used for normal UI elements as well.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2018 Fractal Softworks, LLC
 */
public interface TooltipMakerAPI extends UIPanelAPI {
	
	public static enum TooltipLocation {
		LEFT,
		RIGHT,
		ABOVE,
		BELOW;
	}
	
	public interface TooltipCreator {
		boolean isTooltipExpandable(Object tooltipParam);
		float getTooltipWidth(Object tooltipParam);
		void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam);
	}
	
	public interface StatModValueGetter {
		String getFlatValue(StatMod mod);
		String getPercentValue(StatMod mod);
		String getMultValue(StatMod mod);
		Color getModColor(StatMod mod);
	}
	
	public interface FleetMemberValueGetter {
		boolean skipMember(FleetMemberAPI member);
		float getValue(FleetMemberAPI member);
		String getDisplayValue(FleetMemberAPI member);
		Color getValueColor(FleetMemberAPI member);
	}
	
	LabelAPI addTitle(String text);
	void setTitleFont(String titleFont);
	void setTitleSmallOrbitron();
	void setTitleFontColor(Color titleFontColor);
	
	void setParaFont(String paraFont);
	void setParaFontColor(Color paraFontColor);
	void setParaSmallInsignia();
	
	LabelAPI addPara(String format, float pad, Color hl, String... highlights);
	LabelAPI addPara(String str, float pad);
	LabelAPI addPara(String str, Color color, float pad);
	LabelAPI addSectionHeading(String str, Alignment align, float pad);
	LabelAPI addSectionHeading(String str, Color textColor, Color bgColor, Alignment align, float pad);
	
	void beginGrid(float itemWidth, int cols);
	void beginGrid(float itemWidth, int cols, Color labelColor);
	Object addToGrid(int x, int y, String label, String value);
	Object addToGrid(int x, int y, String label, String value, Color valueColor);
	void setGridLabelColor(Color nameColor);
	void addGrid(float pad);
	void beginGridFlipped(float itemWidth, int cols, float valueWidth, float valuePad);
	void beginGridFlipped(float itemWidth, int cols, Color labelColor, float valueWidth, float valuePad);
	void addStatModGrid(float width, float valueWidth, float valuePad, float pad, MutableStat stat);
	void addStatModGrid(float width, float valueWidth, float valuePad, float pad, StatBonus stat);
	void addStatGridForShips(float width, float valueWidth, float valuePad, float pad,
							 CampaignFleetAPI fleet, int maxNum, boolean ascending,
							 FleetMemberValueGetter getter);
	void setGridFontDefault();
	void setGridFont(String gridFont);
	void addStatModGrid(float width, float valueWidth, float valuePad,
			float pad, MutableStat stat, StatModValueGetter getter);
	void addStatModGrid(float width, float valueWidth, float valuePad,
			float pad, StatBonus stat, StatModValueGetter getter);
	void setParaSmallOrbitron();
	LabelAPI addTitle(String text, Color color);
	void setParaFontVictor14();
	
	void addImage(String spriteName, float pad);
	void addImage(String spriteName, float width, float pad);
	void setParaFontDefault();
	
	void setParaOrbitronLarge();
	void setParaOrbitronVeryLarge();
	void setParaInsigniaLarge();
	void setParaInsigniaVeryLarge();
	void setTitleOrbitronLarge();
	void setTitleOrbitronVeryLarge();
	
	void beginIconGroup();
	void setIconSpacingMedium();
	void setIconSpacingWide();
	void addIcons(CommodityOnMarketAPI com, int num, IconRenderMode mode);
	void addIconGroup(float pad);
	void addIconGroup(float rowHeight, float pad);
	void cancelGrid();
	
	/**
	 * Assumes a single icon, hacky.
	 * @param pad
	 */
	void addIconGroupAndCenter(float pad);
	void addStatModGrid(float width, float valueWidth, float valuePad, float pad, StatBonus stat, boolean showNonMods, StatModValueGetter getter);
	
	/**
	 * Sort is based on stack size.
	 * @param cargo
	 * @param max
	 * @param sort
	 * @param pad
	 */
	void showCargo(CargoAPI cargo, int max, boolean sort, float pad);
	void showShips(List<FleetMemberAPI> ships, int max, boolean sort, float pad);
	
	/**
	 * Columns are pairs of <string name> <Float|Integer width>
	 * @param faction
	 * @param itemHeight
	 * @param columns
	 */
	void beginTable(FactionAPI faction, float itemHeight, Object ... columns);
	
	/**
	 * Columns are pairs of <string name> <Float|Integer width>
	 */
	void beginTable(Color base, Color dark, Color bright, float itemHeight, Object ... columns);
	
	/**
	 * Possible sets of data for a column:
	 * string
	 * color, string
	 * alignment, color, string
	 * 
	 * @param data
	 */
	Object addRow(Object ... data);
	void addTable(String emptyText, int andMore, float pad);
	void setGridValueColor(Color valueColor);
	
	TooltipMakerAPI beginImageWithText(String spriteName, float imageHeight);
	void addImageWithText(float pad);
	void addIconGroup(float rowHeight, int rows, float pad);
	LabelAPI addPara(String format, float pad, Color color, Color hl, String ... highlights);
	
	void setButtonFontDefault();
	void setButtonFontVictor10();
	void setButtonFontVictor14();
	ButtonAPI addButton(String text, Object data, float width, float height, float pad);
	ButtonAPI addButton(String text, Object data, Color base, Color bg, float width, float height, float pad);
	ButtonAPI addButton(String text, Object data, Color base, Color bg, Alignment align, CutStyle style, float width, float height, float pad);
	void setBulletedListMode(String itemPrefix);
	void setBulletWidth(Float bulletWidth);
	
	UIComponentAPI addCustom(UIComponentAPI comp, float pad);
	String getBulletedListPrefix();
	void addImage(String spriteName, float width, float height, float pad);
	
	float getTextWidthOverride();
	void setTextWidthOverride(float textWidthOverride);
	void addImages(float width, float height, float pad, float imagePad, String ... spriteNames);
	void resetGridRowHeight();
	void setLowGridRowHeight();
	void setGridRowHeight(float gridSize);
	
	String shortenString(String in, float maxWidth);
	void addStatModGrid(float width, float valueWidth, float valuePad,
			float pad, MutableStat stat, boolean showNonMods,
			StatModValueGetter getter);
	void addShipList(int cols, int rows, float iconSize, Color baseColor, List<FleetMemberAPI> ships, float pad);
	void setParaFontOrbitron();
	LabelAPI addPara(String format, float pad, Color[] hl, String ... highlights);
	UIComponentAPI addSpacer(float height);
	void addStoryPointUseInfo(float pad, float bonusXPFraction, boolean withNoSPNotification);
	void addStoryPointUseInfo(float pad, int numPoints, float bonusXPFraction, boolean withNoSPNotification);

	void setForceProcessInput(boolean forceProcessInput);
	
	void addPlaythroughDataPanel(float width, float height);
	void setBulletColor(Color bulletColor);
	void addRelationshipBar(PersonAPI person, float pad);
	void addRelationshipBar(PersonAPI person, float width, float pad);
	void addRelationshipBar(FactionAPI faction, float pad);
	void addRelationshipBar(FactionAPI faction, float width, float pad);
	void addRelationshipBar(float value, float pad);
	void addRelationshipBar(float value, float width, float pad);
	void addImportanceIndicator(PersonImportance importance, float width, float pad);
	void addTooltipToPrevious(TooltipCreator tc, TooltipLocation loc);
	ButtonAPI addAreaCheckbox(String text, Object data, Color base, Color bg,
			Color bright, float width, float height, float pad);
	void showShips(List<FleetMemberAPI> ships, int max, boolean sort, boolean showBaseHullForDHulls, float pad);
	void setGridFontSmallInsignia();
	void showFullSurveyReqs(PlanetAPI planet, boolean withText, float pad);
	void showCost(String title, boolean withAvailable, float widthOverride, Color color, Color dark, float pad,
				  String[] res, int[] quantities, boolean[] consumed);
	void showCost(String title, boolean withAvailable, Color color, Color dark, float pad, String[] res,
				  int[] quantities);
	void showCost(Color color, Color dark, float pad, String[] res, int[] quantities);
	UIComponentAPI getPrev();
	
	
	
	//LabelAPI addParaWithIndent(String text, Color color, float indent, String format, float pad, Color hl, String... highlights);
}




