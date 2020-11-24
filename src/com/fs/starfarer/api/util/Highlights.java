package com.fs.starfarer.api.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Highlights {

	private String [] text = new String[0];
	private Color [] colors = new Color[0];
	
	
	public void setText(String ... text) {
		this.text = text;
	}
	
	public void setColors(Color ... colors) {
		this.colors = colors;
	}

	public String[] getText() {
		return text;
	}

	public Color[] getColors() {
		return colors;
	}
	
	
	public String [] prependText(String str) {
		List<String> list = new ArrayList<String>(Arrays.asList(text));
		list.add(0, str);
		return (String[]) list.toArray(new String [1]);
	}
	
	public Color [] prependColor(Color color) {
		List<Color> list = new ArrayList<Color>(Arrays.asList(colors));
		list.add(0, color);
		return (Color[]) list.toArray(new Color [1]);
	}
	
	public void append(String str, Color color) {
		List<String> list = new ArrayList<String>(Arrays.asList(text));
		list.add(str);
		text = list.toArray(new String [1]);
		
		List<Color> list2 = new ArrayList<Color>(Arrays.asList(colors));
		list2.add(color);
		colors = list2.toArray(new Color [1]);
	}
}



