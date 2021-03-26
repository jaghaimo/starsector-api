package com.fs.starfarer.api.campaign;

import java.awt.Color;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Highlights;

public interface TextPanelAPI {
	
	void setFontInsignia();
	void setFontOrbitron();
	void setFontVictor();
	void setFontSmallInsignia();
	
	LabelAPI addPara(String text);
	LabelAPI addPara(String text, Color color);
	LabelAPI addParagraph(String text);
	LabelAPI addParagraph(String text, Color color);
	void replaceLastParagraph(String text);
	void replaceLastParagraph(String text, Color color);
	void appendToLastParagraph(String text);
	void appendToLastParagraph(int charsToCut, String text);
	
	void highlightFirstInLastPara(String text, Color color);
	void highlightLastInLastPara(String text, Color color);
	void highlightInLastPara(Color color, String ...strings);
	
	/**
	 * Must be in order they appear in the paragraph.
	 * @param strings
	 */
	void highlightInLastPara(String ...strings);
	void setHighlightColorsInLastPara(Color ...colors);
	void clear();
	
	InteractionDialogAPI getDialog();
	boolean isOrbitronMode();
	void setOrbitronMode(boolean orbitronMode);
	ResourceCostPanelAPI addCostPanel(String title, float height, Color color, Color dark);
	void setHighlightsInLastPara(Highlights h);
	LabelAPI addPara(String format, Color color, Color hl, String ... highlights);
	LabelAPI addPara(String format, Color hl, String ... highlights);
	
	void advance(float amount);
	
	TooltipMakerAPI beginTooltip();
	void addTooltip();
	void updateSize();
	boolean addCostPanel(String title, Color color, Color dark, Object ... params);
	boolean addCostPanel(String title, Object ... params);
	void addSkillPanel(PersonAPI person, boolean admin);
}
