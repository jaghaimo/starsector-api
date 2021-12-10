package com.fs.starfarer.api.input;

public interface InputEventAPI {
	int getEventValue();
	int getX();
	int getY();
	int getDX();
	int getDY();
	InputEventClass getEventClass();
	void logEvent();
	boolean isConsumed();
	void consume();
	boolean isRepeat();
	InputEventType getEventType();
	boolean isMouseEvent();
	boolean isKeyboardEvent();
	boolean isKeyUpEvent();
	boolean isKeyDownEvent();
	boolean isMouseUpEvent();
	boolean isMouseDownEvent();
	boolean isLMBDownEvent();
	boolean isLMBEvent();
	boolean isRMBEvent();
	boolean isLMBUpEvent();
	boolean isRMBDownEvent();
	boolean isRMBUpEvent();
	boolean isMouseMoveEvent();
	boolean isMouseScrollEvent();
	char getEventChar();
	boolean isAltDown();
	boolean isCtrlDown();
	boolean isShiftDown();
	boolean isUnmodified();
	boolean isDoubleClick();
	boolean isModifierKey();
	boolean isControlDownEvent(String controlEnumName);
	boolean isControlUpEvent(String controlEnumName);
	boolean isControlActivated(String enumName);
}
