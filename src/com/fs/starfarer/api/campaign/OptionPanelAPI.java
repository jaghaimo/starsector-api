package com.fs.starfarer.api.campaign;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.ValueDisplayMode;


public interface OptionPanelAPI {
	
	public static interface OptionTooltipCreator {
		void createTooltip(TooltipMakerAPI tooltip, boolean hadOtherText);
	}
	
	
	void setTooltipHighlights(Object data, String ... highlights);
	void setTooltipHighlightColors(Object data, Color ... colors);
	
	void clearOptions();
	void addOption(String text, Object data);
	void addOption(String text, Object data, String tooltip);
	void addOption(String text, Object data, Color color, String tooltip);
	
	/**
	 * Sets an alternate shortcut that works in addition to the number key.
	 * @param data
	 * @param code constant from org.lwjgl.input.Keyboard
	 * @param ctrl whether Control needs to be down to trigger this shortcut.
	 * @param alt whether Alt needs to be down to trigger this shortcut.
	 * @param shift whether Shift needs to be down to trigger this shortcut.
	 * @param putLast ignored
	 */
	void setShortcut(Object data, int code, boolean ctrl, boolean alt, boolean shift, boolean putLast);
	
	/**
	 * Only works for options, not selectors.
	 * @param data
	 * @param enabled
	 */
	void setEnabled(Object data, boolean enabled);
	
	
	void setTooltip(Object data, String tooltipText);
	
	/**
	 * A user-adjustable bar useful for picking a value from a range.
	 * @param text Text to show above the bar.
	 * @param data ID of the bar, used to get/set its state.
	 * @param color Bar color.
	 * @param width Width in pixels, including value label on the right.
	 * @param maxValueWidth Width of the value label on the right.
	 * @param minValue Minimum value (when bar is all the way to the left).
	 * @param maxValue Maximum value (bar all the way to the right).
	 * @param mode How to display the value - as a percentage, X/Y, etc.
	 * @param tooltip Tooltip text. Can be null.
	 */
	void addSelector(String text, Object data, Color color,
					 float width, float maxValueWidth, float minValue, float maxValue,
					 ValueDisplayMode mode, String tooltip);
	
	boolean hasSelector(Object data);
	
	void setSelectorValue(Object data, float value);
	
	float getSelectorValue(Object data);
	float getMinSelectorValue(Object data);
	float getMaxSelectorValue(Object data);
	boolean hasOptions();
	
	List getSavedOptionList();
	void restoreSavedOptions(List list);
	
	void addOptionConfirmation(Object optionId, String text, String yes, String no);
	boolean hasOption(Object data);
	void addOptionConfirmation(Object data, StoryPointActionDelegate confirmDelegate);
	void addOptionTooltipAppender(Object data, OptionTooltipCreator optionTooltipCreator);
	void setOptionText(String text, Object data);
	boolean hasOptionTooltipAppender(Object data);
	boolean optionHasConfirmDelegate(Object data);
	Object getOptionDataBeingConfirmed();
}
