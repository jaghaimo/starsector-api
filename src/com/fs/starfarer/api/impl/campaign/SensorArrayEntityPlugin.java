package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SensorArrayEntityPlugin extends BaseCampaignObjectivePlugin {

	public static float SENSOR_BONUS = 700f;
	public static float SENSOR_BONUS_MAKESHIFT = 400f;
	//public static float SENSOR_PENALTY_MULT_FROM_HACK = 0.75f;
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		readResolve();
	}
	
	Object readResolve() {
		return this;
	}
	
	public void advance(float amount) {
		if (entity.getContainingLocation() == null || entity.isInHyperspace()) return;
		
		String id = getModId();
		for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
			if (fleet.isInHyperspaceTransition()) continue;
			
			if (fleet.getFaction() == entity.getFaction() || (isHacked() && fleet.getFaction().isPlayerFaction())) {
				String desc = "Sensor array";
				float bonus = SENSOR_BONUS;
				if (isMakeshift()) {
					desc = "Makeshift sensor array";
					bonus = SENSOR_BONUS_MAKESHIFT;
				}
				
//				if (fleet.getFaction() == entity.getFaction() && isHacked() && !entity.getFaction().isPlayerFaction()) {
//					fleet.getStats().addTemporaryModMult(0.1f, id,
//							desc, SENSOR_PENALTY_MULT_FROM_HACK, 
//							fleet.getStats().getSensorRangeMod());
//				}
				
				StatMod curr = fleet.getStats().getSensorRangeMod().getFlatBonus(id);
				if (curr == null || curr.value <= bonus) {
					fleet.getStats().addTemporaryModFlat(0.1f, id,
							desc, bonus, 
							fleet.getStats().getSensorRangeMod());
				}
			}
		}
		
	}
	
	protected boolean isMakeshift() {
		return entity.hasTag(Tags.MAKESHIFT);
	}
	
	public void printEffect(TooltipMakerAPI text, float pad) {
		int bonus = (int) SENSOR_BONUS;
		if (isMakeshift()) {
			bonus = (int) SENSOR_BONUS_MAKESHIFT;
		}
		text.addPara(BaseIntelPlugin.INDENT + "%s sensor range for all same-faction fleets in system",
				pad, Misc.getHighlightColor(), "+" + bonus);
		
//		text.addPara(BaseIntelPlugin.INDENT + "%s sensor range to same-faction fleets when hacked",
//				0f, Misc.getHighlightColor(), "-" + (int) Math.round((1f - SENSOR_PENALTY_MULT_FROM_HACK) * 100f) + "%");
	}

	public void printNonFunctionalAndHackDescription(TextPanelAPI text) {
		if (entity.getMemoryWithoutUpdate().getBoolean(MemFlags.OBJECTIVE_NON_FUNCTIONAL)) {
			text.addPara("This one, however, does not appear to be transmitting a sensor telemetry broadcast. The cause of its lack of function is unknown.");
		}
		if (isHacked()) {
			text.addPara("You have a hack running on this sensor array.");
		}
	}
	
	
	
	@Override
	public void addHackStatusToTooltip(TooltipMakerAPI text, float pad) {
		int bonus = (int) SENSOR_BONUS;
		if (isMakeshift()) {
			bonus = (int) SENSOR_BONUS_MAKESHIFT;
		}
		text.addPara("%s sensor range for in-system fleets",
				pad, Misc.getHighlightColor(), "+" + bonus);
		
//		text.addPara("%s%% sensor range when hacked",
//				pad, Misc.getHighlightColor(), "-" + (int) Math.round((1f - SENSOR_PENALTY_MULT_FROM_HACK) * 100f));
		
		super.addHackStatusToTooltip(text, pad);
	}

	protected String getModId() {
		return "sensor_array";
	}
	
	
}



