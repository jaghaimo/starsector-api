package com.fs.starfarer.api.impl.campaign.terrain;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.TerrainAIFlags;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class EventHorizonPlugin extends StarCoronaTerrainPlugin {

	@Override
	public String getTerrainName() {
		return "Event Horizon";
	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		float pad = 10f;
		float small = 5f;
		tooltip.addTitle("Event Horizon");
		float nextPad = pad;
		
		tooltip.addPara(Global.getSettings().getDescription(getTerrainId(), Type.TERRAIN).getText1(), pad);
		
		if (expanded) {
			tooltip.addSectionHeading("Travel", Alignment.MID, pad);
			nextPad = small;
		}
		tooltip.addPara("Reduces the combat readiness of " +
				"all ships near the event horizon at a steady pace.", nextPad);
		tooltip.addPara("The drive field is also distrupted, making getting away from the event horizon more difficult.", pad);
		
		if (expanded) {
			tooltip.addSectionHeading("Combat", Alignment.MID, pad);
			tooltip.addPara("Reduces the peak performance time of ships and increases the rate of combat readiness degradation in protracted engagements.", small);
		}
	}

	@Override
	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		float alpha = viewport.getAlphaMult();
		viewport.setAlphaMult(alpha * 0.33f);
		super.render(layer, viewport);
		viewport.setAlphaMult(alpha);
	}

	
	public boolean hasAIFlag(Object flag) {
		return super.hasAIFlag(flag) || flag == TerrainAIFlags.AVOID_VERY_CAREFULLY;
	}
	
	
	
}
