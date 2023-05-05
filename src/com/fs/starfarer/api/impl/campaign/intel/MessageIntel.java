package com.fs.starfarer.api.impl.campaign.intel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * For campaign messages that show up in the lower-left corner, but are not tied to an actual
 * piece of intel. E.G. "fleet member finished repairs" or "got a monthly income report".
 * 
 * 
 * 
 * @author Alex Mosolov
 *
 * Copyright 2018 Fractal Softworks, LLC
 */
public class MessageIntel extends BaseIntelPlugin {

	public static class MessageLineData {
		protected String text;
		protected Color color;
		protected String [] highlights;
		protected Color [] colors;
		public MessageLineData(String text) {
			this(text, null, null, (Color [])null);
		}
		public MessageLineData(String text, Color color) {
			this(text, color, null, (Color [])null);
		}
		public MessageLineData(String text, Color color, String [] highlights, Color ... colors) {
			this.text = text;
			this.color = color;
			this.highlights = highlights;
			this.colors = colors;
		}
	}
	
//	protected String text;
//	protected Color color;
//	protected String [] highlights;
//	protected Color [] colors;
//	
//	protected String text2;
//	protected Color color2;
//	protected String [] highlights2;
//	protected Color [] colors2;
	
	protected List<MessageLineData> lines = new ArrayList<MessageLineData>();
	protected String icon;
	protected String sound;
	protected String extra;
	
	public MessageIntel() {
		
	}
	
	public MessageIntel(String text) {
		this(text, null, null, (Color [])null);
	}
	public MessageIntel(String text, Color color) {
		this(text, color, null, (Color [])null);
	}
	public MessageIntel(String text, Color color, String [] highlights, Color ... colors) {
		addLine(text, color, highlights, colors);
	}
	
	public void addLine(String text) {
		addLine(text, null, null, (Color []) null);
	}
	public void addLine(String text, Color color) {
		addLine(text, color, null, (Color []) null);
	}
	public void addLine(String text, Color color, String [] highlights, Color ... colors) {
		MessageLineData line = new MessageLineData(text, color, highlights, colors);
		lines.add(line);
	}
	
	public void clearLines() {
		lines.clear();
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		float pad = 3f;
		boolean first = true;
		for (MessageLineData line : lines) {
			Color c = line.color;
			if (c == null) {
				c = Misc.getTextColor();
//				if (first) {
//					c = Misc.getTextColor();
//				} else {
//					c = Misc.getGrayColor();
//				}
			}
			
			float currPad = pad;
			if (first) {
				currPad = 0f;
			}
			
			if (line.highlights != null) {
				LabelAPI label = info.addPara(line.text, currPad, c, c, line.highlights);
				label.setHighlight(line.highlights);
				label.setHighlightColors(line.colors);
			} else {
				info.addPara(line.text, c, currPad);
			}
			
			
			if (!first) pad = 0f;
			first = false;
		}
		
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getSound() {
		return sound;
	}

	public void setSound(String sound) {
		this.sound = sound;
	}

	@Override
	public String getCommMessageSound() {
		if (sound != null) {
			return sound;
		}
		return getSoundMinorMessage();
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}
	
}








