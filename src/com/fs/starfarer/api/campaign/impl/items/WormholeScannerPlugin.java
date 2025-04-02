package com.fs.starfarer.api.campaign.impl.items;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class WormholeScannerPlugin extends BaseSpecialItemPlugin {
	
	public static String PLAYER_CAN_USE_WORMHOLES = "$playerCanUseWormholes"; // in global memory

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
		float pad = 3f;
		float opad = 10f;
		float small = 5f;
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color b = Misc.getButtonTextColor();
		b = Misc.getPositiveHighlightColor();

		if (!Global.CODEX_TOOLTIP_MODE) {
			tooltip.addTitle(getName());
		} else {
			tooltip.addSpacer(-opad);
		}
		
		String design = getDesignType();
		if (design != null) {
			Misc.addDesignTypePara(tooltip, design, 10f);
		}
		
		if (!spec.getDesc().isEmpty()) {
			if (Global.CODEX_TOOLTIP_MODE) {
				tooltip.setParaSmallInsignia();
			}
			tooltip.addPara(spec.getDesc(), Misc.getTextColor(), opad);
		}
		
		addCostLabel(tooltip, opad, transferHandler, stackSource);
		
		if (!Global.CODEX_TOOLTIP_MODE) {
			tooltip.addPara("Right-click to integrate the " + getName() + " with your fleet", b, opad);
		}
	}

	@Override
	public float getTooltipWidth() {
		return super.getTooltipWidth();
	}
	
	@Override
	public boolean isTooltipExpandable() {
		return false;
	}
	
	@Override
	public boolean hasRightClickAction() {
		return true;
	}

	@Override
	public boolean shouldRemoveOnRightClickAction() {
		return true;
	}

	@Override
	public void performRightClickAction() {
		Global.getSector().getMemoryWithoutUpdate().set(PLAYER_CAN_USE_WORMHOLES, true);
		Global.getSoundPlayer().playUISound(getSpec().getSoundId(), 1f, 1f);
		Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
				getName() + " integrated - can transit wormholes");
	}
	
	
	public static boolean canPlayerUseWormholes() {
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean(PLAYER_CAN_USE_WORMHOLES)) {
			return true;
		}
		
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		String id = Items.WORMHOLE_SCANNER;
		if (cargo.getQuantity(CargoItemType.SPECIAL, new SpecialItemData(id, null)) >= 1) {
			return true;
		}
		
		return false;
	}
}



