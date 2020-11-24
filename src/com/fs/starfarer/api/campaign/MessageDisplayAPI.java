package com.fs.starfarer.api.campaign;

import java.awt.Color;

public interface MessageDisplayAPI {
	void addMessage(String text);
	void addMessage(String text, Color color);
	void addMessage(String text, String highlight, Color highlightColor);
	void addMessage(String text, Color color, String highlight, Color highlightColor);
	void removeMessage(String text);
}
