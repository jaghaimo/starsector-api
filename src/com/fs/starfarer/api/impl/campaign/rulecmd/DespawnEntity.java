package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	DespawnEntity <optional entity id>
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public class DespawnEntity extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof SectorEntityToken)) return false;
		
		SectorEntityToken entity = dialog.getInteractionTarget();
		if (params.size() >= 1) {
			String id = params.get(0).getString(memoryMap);
			entity = Global.getSector().getEntityById(id);
		}
		
		if (entity != null) {
			Misc.fadeAndExpire(entity);
		}
		
		return true;
	}

}





