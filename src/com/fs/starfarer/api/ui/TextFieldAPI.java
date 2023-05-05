package com.fs.starfarer.api.ui;

import java.awt.Color;

public interface TextFieldAPI extends UIComponentAPI {

	void setPad(float pad);
	LabelAPI getTextLabelAPI();
	void setMidAlignment();
	void setColor(Color color);
	void setBgColor(Color bgColor);
	String getText();
	void setText(String string);
	boolean isValidChar(char c);
	boolean isLimitByStringWidth();
	void setLimitByStringWidth(boolean limitByStringWidth);
	boolean appendCharIfPossible(char c);
	boolean appendCharIfPossible(char c, boolean withSound);
	int getMaxChars();
	void setMaxChars(int maxChars);
	void deleteAll();
	void deleteAll(boolean withSound);
	void deleteLastWord();
	void grabFocus();
	void grabFocus(boolean playSound);
	boolean hasFocus();
	boolean isUndoOnEscape();
	void setUndoOnEscape(boolean undoOnEscape);
	boolean isHandleCtrlV();
	void setHandleCtrlV(boolean handleCtrlV);
	Color getBorderColor();
	void setBorderColor(Color borderColor);
	boolean isVerticalCursor();
	void setVerticalCursor(boolean verticalCursor);
	void hideCursor();
	void showCursor();

}
