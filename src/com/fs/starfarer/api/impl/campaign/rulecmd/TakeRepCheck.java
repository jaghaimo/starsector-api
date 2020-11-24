package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.VarAndMemory;


/**
 * TakeRepCheck <factionid> <resultVarName>
 */
public class TakeRepCheck extends BaseCommandPlugin {
//	public static enum RepCheckResult {
//		R0(0.0f),
//		R1(0.1f),
//		R2(0.2f),
//		R3(0.4f),
//		R4(0.6f),
//		R5(0.95f);
//		
//		private final float threshold;
//		private RepCheckResult(float threshold) {
//			this.threshold = threshold;
//		}
//		public float getThreshold() {
//			return threshold;
//		}
//	}
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		String factionId = params.get(0).getString(memoryMap);
		VarAndMemory result = params.get(1).getVarNameAndMemory(memoryMap);
		
		FactionAPI faction = Global.getSector().getFaction(factionId);
		FactionAPI player = Global.getSector().getFaction(Factions.PLAYER);
		RepLevel level = player.getRelationshipLevel(faction);
		if (level.isAtBest(RepLevel.SUSPICIOUS)) {
			//result.memory.set(result.name, RepCheckResult.R0.name(), 0);
			result.memory.set(result.name, 0f, 0);
		}
		
		float rel = player.getRelationship(factionId);
		
		float r = (float) Math.random();
		r *= r;
		float roll = rel * 0.2f + rel * 0.8f * (float) Math.random() + (1f - rel) * r;
		
		result.memory.set(result.name, roll, 0);
		//result.memory.set(result.name, 1f, 0);
//		RepCheckResult max = RepCheckResult.R0;
//		float minDiff = Float.MAX_VALUE;
//		for (RepCheckResult r : EnumSet.allOf(RepCheckResult.class)) {
//			float diff = 2f - 
//		}
		return true;
	}

	
}




