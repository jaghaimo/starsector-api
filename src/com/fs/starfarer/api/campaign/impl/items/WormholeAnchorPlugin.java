package com.fs.starfarer.api.campaign.impl.items;

import java.awt.Color;

import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.impl.campaign.shared.WormholeManager.WormholeItemData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class WormholeAnchorPlugin extends BaseSpecialItemPlugin {
	
	@Override
	public String getName() {
		if (stack == null || stack.getSpecialDataIfSpecial() == null) super.getName();
		
		WormholeItemData itemData = new WormholeItemData(stack.getSpecialDataIfSpecial().getData());
		return super.getName() + " '" + itemData.name + "'";
	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
		//super.createTooltip(tooltip, expanded, transferHandler, stackSource);
		
		float pad = 3f;
		float opad = 10f;
		float small = 5f;
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color b = Misc.getButtonTextColor();
		b = Misc.getPositiveHighlightColor();

		tooltip.addTitle(getName());
		
		String design = getDesignType();
		if (design != null) {
			Misc.addDesignTypePara(tooltip, design, 10f);
		}
		
		if (!spec.getDesc().isEmpty()) {
			tooltip.addPara(spec.getDesc(), Misc.getTextColor(), opad);
		}
		
		addCostLabel(tooltip, opad, transferHandler, stackSource);
		
		//tooltip.addPara("Right-click to integrate the " + getName() + " with your fleet", b, opad);
	}

	@Override
	public float getTooltipWidth() {
		return super.getTooltipWidth();
	}
	
	@Override
	public boolean isTooltipExpandable() {
		return false;
	}
	
}



