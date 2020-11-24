package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * Usage: AbortWait $waitHandle
 * 
 * @author Alex Mosolov
 *
 * Copyright 2014 Fractal Softworks, LLC
 */
public class DumpMemory extends BaseCommandPlugin {

	public static final String OPTION_ID = "DumpMemory.option_dump_memory";
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		//if (dialog == null) return false;
		
		//System.out.println("Transponder: " + dialog.getInteractionTarget().isTransponderOn());
		
		List<String> memKeys = new ArrayList<String>(memoryMap.keySet());
		Collections.sort(memKeys);
		memKeys.remove(MemKeys.LOCAL);
		memKeys.add(MemKeys.LOCAL);
		
		Color HIGHLIGHT_COLOR = Global.getSettings().getColor("buttonShortcut");
		Color GRAY_COLOR = new Color(100,100,100);
		
		
		for (String memKey : memKeys) {
			String text = "";
			MemoryAPI memory = memoryMap.get(memKey);
			//text += memKey.toUpperCase() + "\n";
			List<String> keys = new ArrayList<String>(memory.getKeys());
			Collections.sort(keys);
			
			List<Color> highlightColors = new ArrayList<Color>();
			List<String> highlightList = new ArrayList<String>();
			for (String key : keys) {
				Object value = memory.get(key);
				
				String varName = "$" + memKey + ".";
				if (memKey.equals(MemKeys.LOCAL)) {
					varName = "$";
				}
				if (key.startsWith("$")) {
					varName += key.substring(1);
					//highlightList.add(key.substring(1));
				} else {
					varName += key;
					//highlightList.add(key);
				}
				
				if (varName.length() > 35) {
					varName = varName.substring(0, 35) + "...";
				}
				
				highlightColors.add(HIGHLIGHT_COLOR);
				highlightList.add(varName);

				text += varName;
				if (value instanceof Boolean || value instanceof String || value instanceof Float || value instanceof Integer || value instanceof Long) {
					text += " = " + value.toString();
				} else if (value != null) {
					text += " = " + value.getClass().getSimpleName() + "@" + value.hashCode();
				} else {
					text += " = " + "null";
				}
				float expire = memory.getExpire(key);
				if (expire >= 0) {
					String eText = "(e=" + (float)((int)(expire * 10)/10f) + ")";
					if (expire == 0) {
						eText = "(e=0)";
					}
					highlightColors.add(GRAY_COLOR);
					highlightList.add(eText);
					text += " " + eText;
				}
				text += "\n";
			}
			if (dialog != null) {
				//dialog.getTextPanel().setFontSmallInsignia();
				dialog.getTextPanel().addParagraph(text);
				dialog.getTextPanel().setHighlightColorsInLastPara(highlightColors.toArray(new Color[0]));
				dialog.getTextPanel().highlightInLastPara(highlightList.toArray(new String [0]));
				//dialog.getTextPanel().setFontInsignia();
			} else {
				if (DebugFlags.PRINT_RULES_DEBUG_INFO) {
					System.out.println(text);	
				}
			}
			//dialog.getTextPanel().highlightInLastPara(HIGHLIGHT_COLOR, highlightList.toArray(new String [0]));
		}
		
		return true;
	}
	
	public static void addOption(InteractionDialogAPI dialog) {
		dialog.getOptionPanel().addOption(">> (dev) dump memory", DumpMemory.OPTION_ID, Misc.getGrayColor(), null);
	}
}





