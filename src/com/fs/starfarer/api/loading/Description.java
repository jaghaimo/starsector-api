package com.fs.starfarer.api.loading;

import java.util.ArrayList;
import java.util.List;

public class Description {

	public static enum Type {
		SHIP,
		WEAPON,
		SHIP_SYSTEM,
		RESOURCE,
		ACTION_TOOLTIP,
		PLANET,
		ASTEROID,
		FACTION,
		TERRAIN,
		CUSTOM,
	}
	
	private Type type;
	private String id;
	
	private String text1 ="No description... yet", text2 ="No description... yet", text3 ="No description... yet";
	private String text4 = null;

	public Description(String id, Type type) {
		this.type = type;
		this.id = id;
	}
	
	public String getUID() {
		return id + "_" + type.name();
	}
	
	public String getText4() {
		return text4;
	}

	public void setText4(String text4) {
		this.text4 = text4;
	}
	public boolean hasText4() {
		return text4 != null && !text4.isEmpty();
	}

	public String getText1() {
		return text1;
	}
	
	public String getText1FirstPara() {
		if (text1 != null) {
			int index = text1.indexOf('\n');
			if (index != -1) {
				return text1.substring(0, index);
			}
		}
		return text1;
	}
	
	public List<String> getText1Paras() {
		List<String> result = new ArrayList<String>();
		if (text1 == null) return result;
		
		String [] temp = text1.split("\\n");
		for (String p : temp) {
			p = p.trim();
			if (p.isEmpty()) continue;
			result.add(p);
		}
		return result;
	}

	
	public void setText1(String text1) {
		if (text1 == null || text1.equals("")) text1 = "No description... yet";
		this.text1 = text1.trim();
	}

	public String getText2() {
		return text2;
	}

	public void setText2(String text2) {
		if (text2 == null || text2.equals("")) text2 = "No description... yet";
		this.text2 = text2.trim();
	}

	public String getText3() {
		return text3;
	}

	public void setText3(String text3) {
		if (text3 == null || text3.equals("")) text3 = "No description... yet";
		this.text3 = text3.trim();
	}
	
	public boolean hasText2() {
		String str = getText2();
		if (str == null || str.isEmpty() || str.equals("No description... yet")) return false;
		return true;
	}
	public boolean hasText1() {
		String str = getText1();
		if (str == null || str.isEmpty() || str.equals("No description... yet")) return false;
		return true;
	}
	public boolean hasText3() {
		String str = getText3();
		if (str == null || str.isEmpty() || str.equals("No description... yet")) return false;
		return true;
	}
	
	
}
