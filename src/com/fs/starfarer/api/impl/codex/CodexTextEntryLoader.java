package com.fs.starfarer.api.impl.codex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.awt.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.ListMap;
import com.fs.starfarer.api.util.Misc;

public class CodexTextEntryLoader {

	public static class ParaData {
		public int fontSize = 0;
		public int bulletMode = 0;
		public Color color = Misc.getTextColor();
		public String text;
		public List<String> highlights = new ArrayList<>();
		public List<Color> colors = new ArrayList<>();
		public String image;
		public float imageHeight;
	}
	
	public static class TextEntry extends CodexEntryV2 {
		public List<Object> data = new ArrayList<>();
		public String parentId;
		public TextEntry(String id, String title, String icon) {
			super(id, title, icon);
			setParam(id);
		}
		
		@Override
		public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
			super.createTitleForList(info, width, mode);
		}

		@Override
		public boolean hasCustomDetailPanel() {
			return true;
		}

		@Override
		public void createCustomDetail(CustomPanelAPI panel, UIPanelAPI relatedEntries, CodexDialogAPI codex) {
			Color color = Misc.getBasePlayerColor();
			Color dark = Misc.getDarkPlayerColor();
			Color h = Misc.getHighlightColor();
			Color g = Misc.getGrayColor();
			float opad = 10f;
			float pad = 3f;
			float small = 5f;
			
			float width = panel.getPosition().getWidth();
			
			float initPad = 0f;
			
			float horzBoxPad = 30f;
			
			// the right width for a tooltip wrapped in a box to fit next to relatedEntries
			// 290 is the width of the related entries widget, but it may be null
			float tw = width - 290f - opad - horzBoxPad + 10f;
			
			TooltipMakerAPI text = panel.createUIElement(tw, 0, false);
			text.setParaSmallInsignia();
		
			int prevBulletMode = 0;
			for (Object o : data) {
				if (o instanceof ParaData) {
					ParaData para = (ParaData) o;
					
					TooltipMakerAPI curr = text;
					float outerPad = initPad;
					if (para.image != null) { 
						float height = para.imageHeight;
						if (height <= 0f) height = Global.getSettings().getSprite(para.image).getHeight();
						curr = text.beginImageWithText(para.image, height);
						initPad = 0f;
					}
					
					if (prevBulletMode != 0 && para.bulletMode != 0) { 
						initPad = 0f;
					}
					prevBulletMode = para.bulletMode;
					
					if (para.fontSize == -1) {
						curr.setParaFontDefault();
					} else if (para.fontSize == 0) {
						curr.setParaSmallInsignia();
					} else if (para.fontSize == 1) {
						curr.setParaSmallOrbitron();
					} else if (para.fontSize == 2) {
						curr.setParaOrbitronLarge();
					} else if (para.fontSize == 3) {
						curr.setParaOrbitronVeryLarge();
					}
					
					if (para.bulletMode == 0) {
						curr.setBulletedListMode(null);
					} else if (para.bulletMode == 1) {
						curr.setBulletedListMode(BaseIntelPlugin.INDENT);
					} else if (para.bulletMode == 2) {
						curr.setBulletedListMode(BaseIntelPlugin.BULLET);
					}
					
					LabelAPI label = curr.addPara(para.text, initPad, 
							para.colors.toArray(new Color[0]), para.highlights.toArray(new String[0]));
					label.setColor(para.color);
					label.setHighlight(para.highlights.toArray(new String[0]));
					label.setHighlightColors(para.colors.toArray(new Color[0]));
					initPad = opad;
					
					prevBulletMode = para.bulletMode;
					
					if (para.image != null) { 
						text.addImageWithText(outerPad);
					}
					
					if (para.fontSize > 0) {
						text.addSpacer(-opad + pad);
					}
				}
			}
			panel.updateUIElementSizeAndMakeItProcessInput(text);
			
			UIPanelAPI box = panel.wrapTooltipWithBox(text);
			panel.addComponent(box).inTL(0f, 0f);
			if (relatedEntries != null) {
				panel.addComponent(relatedEntries).inTR(0f, 0f);
			}
			
			float height = box.getPosition().getHeight();
			if (relatedEntries != null) {
				height = Math.max(height, relatedEntries.getPosition().getHeight());
			}
			panel.getPosition().setSize(width, height);
		}
	}
	
	
	public static ListMap<String> LINKS = new ListMap<>();
	public static void linkRelated() {
		for (String key : LINKS.keySet()) {
			CodexEntryPlugin entry = CodexDataV2.getEntry(key);
			for (String key2 : LINKS.getList(key)) {
				CodexEntryPlugin other = CodexDataV2.getEntry(key2);
				if (entry != null && other != null) {
					CodexDataV2.makeRelated(entry, other);
				}
			}
		}
		LINKS.clear();
	}
	
	public static void loadTextEntries() {
		try {
			String csvName = "data/codex/text_codex_entries.csv";
			Global.getLogger(CodexTextEntryLoader.class).info("Loading [" + csvName + "]");
			JSONArray rows = Global.getSettings().loadCSV(csvName, true);
			
			for (int i = 0; i < rows.length(); i++) {
				JSONObject row = rows.getJSONObject(i);
				String filename = row.getString("filename");
				Global.getLogger(CodexTextEntryLoader.class).info("Loading [" + filename + "]");
				String contents = Global.getSettings().loadText(filename);
				Global.getLogger(CodexTextEntryLoader.class).info("Parsing [" + filename + "]");
				parseContents(contents, filename);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public static void parseContents(String contents, String filename) {
		contents = contents.replaceAll("\\r", "").trim();
		String [] lines = contents.split("\\n");

		Color base = Misc.getBasePlayerColor();
		Color dark = Misc.getDarkPlayerColor();
		
		Color currColor = null;
		int currFontSize = 0;
		String currCategory = null;
		String paraImage = null;
		int paraImageHeight = 0;
		
		TextEntry entry = null;
		int lineNum = 0;
		for (String line : lines) {
			lineNum++;
			if (line.startsWith("#")) continue;
			if (line.isBlank()) continue;
			line = line.trim();
			
			if (line.startsWith("CATEGORY ")) {
				line = line.replaceFirst("CATEGORY ", "").trim();
				Global.getLogger(CodexTextEntryLoader.class).info("Parsing category [" + line + "]");
				String [] arr = line.split("\\|");
				TextEntry cat = new TextEntry(arr[0], arr[2], CodexDataV2.getIcon(arr[1]));
				cat.parentId = arr[3];
				cat.setParam(null);
				CodexDataV2.ENTRIES.put(cat.getId(), cat);
				CodexEntryPlugin parent = CodexDataV2.getEntry(cat.parentId);
				if (parent != null) {
					cat.setParent(parent);
					parent.addChild(cat);
				}
				continue;
			}
			
			if (line.startsWith("COLOR ")) {
				line = line.replaceFirst("COLOR ", "").trim();
				currColor = getColor(line);
				continue;
			}
			if (line.startsWith("FONT ")) {
				line = line.replaceFirst("FONT ", "").trim();
				currFontSize = line.toLowerCase().equals("small") ? -1 : 0;
				continue;
			}
			if (line.startsWith("IMAGE ")) {
				line = line.replaceFirst("IMAGE ", "").trim();
				String [] arr = line.split("\\|");
				paraImage = arr[0];
				if (arr.length > 1) {
					try {
						paraImageHeight = Integer.parseInt(arr[1]);
					} catch (Throwable t) {
						paraImage = Global.getSettings().getSpriteName(arr[0], arr[1]);
						if (arr.length > 2) {
							paraImageHeight = Integer.parseInt(arr[2]);
						}
					}
				} else {
					paraImageHeight = 0;
				}
				continue;
			}
			if (line.startsWith("RESET")) {
				currColor = getColor("text");
				currFontSize = 0;
				continue;
			}
			if (line.startsWith("CURRENT_CATEGORY ")) {
				line = line.replaceFirst("CURRENT_CATEGORY ", "").trim();
				currCategory = line;
				continue;
			}
			
			if (line.startsWith("BEGIN ")) {
				line = line.replaceFirst("BEGIN ", "").trim();
				Global.getLogger(CodexTextEntryLoader.class).info("Parsing entry [" + line + "]");
				String [] arr = line.split("\\|");
				entry = new TextEntry(arr[0], arr[1], null);
				entry.parentId = currCategory;
				if (arr.length >= 3) entry.parentId = arr[2];
				continue;
			}
			
			if (entry == null) {
				throw new RuntimeException(String.format("Error parsing [%s] line %s, expected to be inside entry",
						filename, "" + lineNum));
			}
			
			if (line.startsWith("END")) {
				if (entry.getIcon() == null) {
					String icon = CodexDataV2.getIcon("generic");
					entry.setIcon(icon);
				}
				CodexEntryPlugin parent = CodexDataV2.getEntry(entry.parentId);
				if (parent != null) {
					entry.setParent(parent);
					parent.addChild(entry);
				}
				entry = null;
				currColor = null;
				currFontSize = 0;
				continue;
			}
			
			if (line.startsWith("ICON ")) {
				line = line.replaceFirst("ICON ", "").trim();
				String icon = CodexDataV2.getIcon(line);
				entry.setIcon(icon);
				continue;
			}
			
			if (line.startsWith("RELATED ")) {
				line = line.replaceFirst("RELATED ", "").trim();
				String [] ids = line.split("\\|");
				for (String id : ids) {
					//entry.addRelatedEntry(id);
					LINKS.add(entry.getId(), id);
				}
				continue;
			}
			
			
			ParaData para = new ParaData();
			
			Pattern p = Pattern.compile("(?is)^(=+)(.+?)=+$");
		    Matcher m = p.matcher(line);
		    if (m.find()) {
		    	line = m.group(2);
		    	para.fontSize = m.group(1).length();
		    	para.color = base;
		    }
		    
		    if (line.startsWith("_ ")) {
		    	line = line.replaceFirst("_ ", "").trim();
		    	para.bulletMode = 1;
		    } else if (line.startsWith("- ")) {
		    	line = line.replaceFirst("- ", "").trim();
		    	para.bulletMode = 2;
		    }
		    
		    p = Pattern.compile("(?is)\\{\\{(.*?)\\}\\}");
		    m = p.matcher(line);
		    
		    while (m.find()) {
		    	String curr = m.group(1);
		    	String replacement = "";
		    	if (curr.startsWith("color:")) {
		    		curr = curr.replaceFirst("color:", "").trim();
		    		String [] arr = curr.split("\\|");
		    		Color color = getColor(arr[0]);
		    		para.colors.add(color);
		    		para.highlights.add(arr[1]);
		    		replacement = arr[1];
		    	} else if (curr.startsWith("rel:")) {
		    		curr = curr.replaceFirst("rel:", "").trim();
		    		String [] arr = curr.split("\\|");
		    		//entry.addRelatedEntry(arr[0]);
		    		LINKS.add(entry.getId(), arr[0]);
		    		para.colors.add(base);
		    		para.highlights.add(arr[1]);
		    		replacement = arr[1];
		    	} else {
		    		Color color = Misc.getHighlightColor();
		    		para.colors.add(color);
		    		para.highlights.add(curr);
		    		replacement = curr;
		    	}
		    	line = line.replaceFirst(Pattern.quote(m.group(0)), replacement);
		    }
		    
		    para.text = line.replaceAll("%", "%%");
		    if (currColor != null) {
		    	para.color = currColor;
		    }
		    if (para.fontSize == 0) {
		    	para.fontSize = currFontSize;
		    }
		    
		    para.image = paraImage;
		    para.imageHeight = paraImageHeight;
		    paraImage = null;
		    paraImageHeight = 0;
		    
		    entry.data.add(para);
		}
		
		if (entry != null) {
			throw new RuntimeException(String.format("Error parsing [%s] file ended while still parsing entry", filename));
		}
	}
	
	public static Color getColor(String str) {
		if (Global.getSettings().hasDesignTypeColor(str)) {
			return Global.getSettings().getDesignTypeColor(str);
		}
		
		Color dark = Misc.getDarkPlayerColor();
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color good = Misc.getPositiveHighlightColor();
		Color gray = Misc.getGrayColor();
		Color textColor = Misc.getTextColor();
		Color player = Misc.getBasePlayerColor();
		
		Color color = null;
		if (str.equals("h")) color = h;
		else if (str.equals("good")) color = good;
		else if (str.equals("bad")) color = bad;
		else if (str.equals("gray")) color = gray;
		else if (str.equals("grey")) color = gray;
		else if (str.equals("text")) color = textColor;
		else if (str.equals("player")) color = player;
		else if (str.equals("blue")) color = player;
		else if (str.equals("base")) color = player;
		else if (str.equals("dark")) color = dark;
		else color = Global.getSettings().getColor(str);
		if (color == null) {
			throw new RuntimeException(String.format("Parsing error: color [%s] not found", str));
		}
		return color;
	}
	
	
	public static void main(String[] args) {
		String test = "Testing 100% in text";
		test = test.replaceAll("%", "%%");
		System.out.println(String.format(test));
		
//		String line = "Flux generated by taking damage on shields is {{color:h|hard}} flux, meaning that it can not dissipate while shields are up. Flux generated by firing weapons is {{color:h|soft}} flux and can dissipate while shields are up.";
//	    Pattern p = Pattern.compile("(?is)\\{\\{(.*?)\\}\\}");
//	    Matcher m = p.matcher(line);
		
//	    String line = "===A heading===\n";
//	    line = line.trim();
//	    Pattern p = Pattern.compile("(?is)^(=+)(.+?)=+$");
//	    Matcher m = p.matcher(line);
//		
//	    while (m.find()) {
//	    	String curr = m.group(2);
//	    	System.out.println("Found: " + curr);
//	    }
	}
}














