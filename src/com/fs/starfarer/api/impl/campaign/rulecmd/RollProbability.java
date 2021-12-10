package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;


/**
 * RollProbability <float probability>
 */
public class RollProbability extends BaseCommandPlugin {
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		float prob = params.get(0).getFloat(memoryMap);
		
		long seed;
		if (dialog.getInteractionTarget() != null) {
			seed = Misc.getSalvageSeed(dialog.getInteractionTarget());
			seed /= 321L;
			seed *= (Global.getSector().getClock().getMonth() + 10 + prob * 10f);
		} else {
			seed = Misc.genRandomSeed();
		}
		
		Random r = Misc.getRandom(seed, 1);
		
		float f = r.nextFloat();
		//System.out.println("Seed: " + seed);
		//System.out.println("rolled: " + f);
		
		return f < prob;  //r.nextFloat() < prob;
	}

	
}




