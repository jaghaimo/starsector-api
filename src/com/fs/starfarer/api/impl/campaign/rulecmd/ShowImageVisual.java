package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.SharedUnlockData;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * ShowImageVisual <category> <key>
 */
public class ShowImageVisual extends BaseCommandPlugin {

	public ShowImageVisual() {
		
	}
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		
		String category = "illustrations";
		String key = null;
		
		if (params.size() <= 1) {
			key = params.get(0).string;
		} else {
			category = params.get(0).string;
			key = params.get(1).string;
		}
		
		SharedUnlockData.get().reportPlayerAwareOfIllustration(key, true);
		SharedUnlockData.get().saveIfNeeded();
		
		SpriteAPI sprite = Global.getSettings().getSprite(category, key);
		dialog.getVisualPanel().showImagePortion(category, key, sprite.getWidth(), sprite.getHeight(), 0, 0, 480, 300);
		
		return true;
	}

}


