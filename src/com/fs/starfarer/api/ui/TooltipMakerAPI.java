package com.fs.starfarer.api.ui;

import java.util.List;
import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageDisplayData;
import com.fs.starfarer.api.impl.codex.CodexEntryPlugin;
import com.fs.starfarer.api.ui.ButtonAPI.UICheckboxSize;



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
	
	public static interface ActionListenerDelegate {
		void actionPerformed(Object data, Object source);
	}
	
	public static class PlanetInfoParams {
		public boolean scaleEvenWhenShowingName = false;
		public boolean showName;
		public boolean withClass;
		public String classStrOverride = null;
		public boolean showConditions;
		public float conditionsYOffset = 0f;
		public float conditionsHeight = 32f;
		public boolean showHazardRating = false;
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
	void addIcons(CommoditySpecAPI com, int num, IconRenderMode mode);
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
	UIPanelAPI beginTable(FactionAPI faction, float itemHeight, Object ... columns);
	UIPanelAPI beginTable2(FactionAPI faction, float itemHeight, boolean withBorder, boolean withHeader, Object ... columns);	
	
	/**
	 * Columns are pairs of <string name> <Float|Integer width>
	 */
	UIPanelAPI beginTable(Color base, Color dark, Color bright, float itemHeight, Object ... columns);
	UIPanelAPI beginTable(Color base, Color dark, Color bright, float itemHeight, boolean withBorder, boolean withHeader, Object ... columns);
	
	/**
	 * Possible sets of data for a column:
	 * string |
	 * color, string |
	 * alignment, color, string |
	 * alignment, color, LabelAPI
	 * 
	 * @param data
	 */
	Object addRow(Object ... data);
	/**
	 * Possible sets of data for a column:
	 * string
	 * color, string
	 * alignment, color, string
	 * alignment, color, LabelAPI
	 * 
	 * @param data
	 */
	Object addRowWithGlow(Object ... data);
	void addTooltipToAddedRow(TooltipCreator tc, TooltipLocation loc);
	void addTooltipToAddedRow(TooltipCreator tc, TooltipLocation loc, boolean recreateEveryFrame);
	
	void addTable(String emptyText, int andMore, float pad);
	void setGridValueColor(Color valueColor);
	
	TooltipMakerAPI beginImageWithText(String spriteName, float imageHeight);
	UIPanelAPI addImageWithText(float pad);
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
	void showCost(String title, boolean withAvailable, float widthOverride, float heightOverride, Color color,
			Color dark, float pad, String[] res, int[] quantities, boolean[] consumed);	
	UIComponentAPI getPrev();
	ButtonAPI addAreaCheckbox(String text, Object data, Color base, Color bg, Color bright, float width, float height,
			float pad, boolean leftAlign);
	UIComponentAPI addSkillPanel(PersonAPI person, float pad);
	UIComponentAPI addSkillPanelOneColumn(PersonAPI person, float pad);
	float computeStringWidth(String in);
	TextFieldAPI addTextField(float width, float pad);
	TextFieldAPI addTextField(float width, String font, float pad);
	TextFieldAPI addTextField(float width, float height, String font, float pad);
	/**
	 * Use the method with the Object data param
	 */
	@Deprecated ButtonAPI addCheckbox(float width, float height, String text, UICheckboxSize size, float pad);
	/**
	 * Use the method with the Object data param
	 */
	@Deprecated ButtonAPI addCheckbox(float width, float height, String text, String font, Color textColor, UICheckboxSize size,
			float pad);
	void setAreaCheckboxFont(String areaCheckboxFont);
	void setAreaCheckboxFontDefault();
	UIComponentAPI addLabelledValue(String label, String value, Color labelColor, Color valueColor, float width, float pad);
	float getHeightSoFar();
	
	/**
	 * Returns the intel UI; only works when creating small intel descriptions.
	 * @return
	 */
	IntelUIAPI getIntelUI();
	EventProgressBarAPI addEventProgressBar(BaseEventIntel intel, float pad);
	
	/**
	 * Add a custom component without appending it to the bottom of the tooltip. Will need to call one of the 
	 * .getPosition().inXXX methods to actually place it somewhere specific within the tooltip.
	 * @param comp
	 * @return
	 */
	UIComponentAPI addCustomDoNotSetPosition(UIComponentAPI comp);
	
	UIComponentAPI addEventStageMarker(EventStageDisplayData data);
	UIComponentAPI addEventProgressMarker(BaseEventIntel intel);
	TooltipMakerAPI beginImageWithText(String spriteName, float imageHeight, float widthWithImage, boolean midAlignImage);
	LabelAPI addSectionHeading(String str, Color textColor, Color bgColor, Alignment align, float width, float pad);
	TooltipMakerAPI beginSubTooltip(float width);
	/**
	 * Tooltip still needs to be added using addCustom() or similar.
	 */
	void endSubTooltip();
	void setHeightSoFar(float height);
	
	/**
	 * TODO: maps don't seem to work right if the tooltip is recreated every frame,
	 * for now make sure to use the addTooltipToPrevious method that takes a boolean to turn that off. 
	 */
	UIPanelAPI createSectorMap(float w, float h, MapParams p, String title);
	
	/**
	 * TODO: maps don't seem to work right if the tooltip is recreated every frame,
	 * for now make sure to use the addTooltipToPrevious method that takes a boolean to turn that off. 
	 */
	UIPanelAPI createSectorMap(float w, float h, MapParams p, String title, Color color, Color dark);
	float getWidthSoFar();
	void addTooltipToPrevious(TooltipCreator tc, TooltipLocation loc, boolean recreateEveryFrame);
	
	
	/**
	 * Create a label without adding it to the tooltip, so it can be added via addCustom() or passed in to 
	 * a table row. Uses the current paragraph font.
	 */
	LabelAPI createLabel(String str, Color color);
	
	/**
	 * Create a label without adding it to the tooltip, so it can be added via addCustom() or passed in to 
	 * a table row. Uses the current paragraph font.
	 */
	LabelAPI createLabel(String str, Color color, float maxTextWidth);
	void addTableHeaderTooltip(int colIndex, TooltipCreator tc);
	void addTableHeaderTooltip(int colIndex, String text);
	UIPanelAPI addSectorMap(float w, float h, StarSystemAPI system, float pad);
	void addTooltipTo(TooltipCreator tc, UIComponentAPI to, TooltipLocation loc);
	void addTooltipTo(TooltipCreator tc, UIComponentAPI to, TooltipLocation loc, boolean recreateEveryFrame);
	UIComponentAPI createRect(Color color, float thickness);
	
	void makeTableItemsClickable();
	/**
	 * To identify which row was clicked on.
	 * @param id
	 */
	void setIdForAddedRow(Object id);
	
	
	void setExternalScroller(ScrollPanelAPI scroller);
	
	/**
	 * Only non-null if this tooltip was added to a CustomPanelAPI using addUIElement().
	 * @return
	 */
	ScrollPanelAPI getExternalScroller();

	/**
	 * Default is 0.85f.
	 */
	void setBgAlpha(float bgAlpha);
	
	void setButtonFontOrbitron20();
	void setButtonFontOrbitron20Bold();
	void setButtonFontOrbitron24();
	void setButtonFontOrbitron24Bold();

	void showPlanetInfo(PlanetAPI planet, float pad);
	void showPlanetInfo(PlanetAPI planet, float w, float h, boolean withName, float pad);
	void showPlanetInfo(PlanetAPI planet, float w, float h, PlanetInfoParams params, float pad);
	
	ButtonAPI addCheckbox(float width, float height, String text, Object data, UICheckboxSize size, float pad);
	ButtonAPI addCheckbox(float width, float height, String text, Object data, String font, Color textColor,
			UICheckboxSize size, float pad);
	UIComponentAPI addSkillPanel(PersonAPI person, boolean admin, float pad);
	UIComponentAPI addSkillPanelOneColumn(PersonAPI person, boolean admin, float pad);
	void addCodexEntries(String title, Set<String> entryIds, boolean sort, float pad);
	
	/**
	 * ID of codex entry to open with F2 when this tooltip is shown.
	 * Setting it to something and then back to null will not remove the "press F2" prompt etc.
	 * See: CodexDataV2.getXXXXEntryId() methods for how to get entry ids. 
	 * @param codexEntryId
	 */
	void setCodexEntryId(String codexEntryId);
	String getCodexEntryId();
	void setCodexEntryFleetMember(FleetMemberAPI member);
	void setCodexTempEntry(CodexEntryPlugin tempCodexEntry);
	//UIPanelAPI beginTable(float itemHeight, Object[] columns);
	LabelAPI addParaWithMarkup(String str, float pad);
	LabelAPI addParaWithMarkup(String str, Color color, float pad);
	LabelAPI addParaWithMarkup(String str, float pad, String... tokens);
	
	/**
	 * Markup:
	 * {{string}} -> highlights it
	 * {{color:<color>|string}} -> highlights it with color
	 * color can be h|good|bad|text|gray|blue or a color in settings.json
	 * If passing in tokens and needing to use a % in the base string: use %% (tokens means a String.format call)
	 * If NOT passing in tokens and needing to use a % in the base string: use % (no String.format call)
	 * 
	 * @param str
	 * @param color
	 * @param pad
	 * @param tokens
	 * @return
	 */
	LabelAPI addParaWithMarkup(String str, Color color, float pad, String... tokens);
	void showCargo(CargoAPI cargo, int max, boolean sort, float pad, float itemHeight, float itemPad);
	
	
	/**
	 * Needs to be called *before* any methods that create UI elements 
	 * that call the action listener (such as addButton) are called.
	 * Warning: If the TooltipMakerAPI already has an action listener, it will be overridden.
	 * @param delegate
	 */
	void setActionListenerDelegate(ActionListenerDelegate delegate);
	
	//LabelAPI addParaWithIndent(String text, Color color, float indent, String format, float pad, Color hl, String... highlights);
}




