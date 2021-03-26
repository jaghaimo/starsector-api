package com.fs.starfarer.api.impl.campaign.plog;

import java.awt.Color;

import com.fs.starfarer.api.util.Misc;

public class PLTextEntry extends BasePLEntry {

	protected Boolean story = null;
	
	public PLTextEntry(String text) {
		super(text);
	}

	public PLTextEntry(String text, boolean story) {
		super(text);
		this.story = story;
	}

	@Override
	public Color getColor() {
		if (story != null && story) {
			return Misc.getStoryOptionColor();
		}
		return super.getColor();
	}

}
