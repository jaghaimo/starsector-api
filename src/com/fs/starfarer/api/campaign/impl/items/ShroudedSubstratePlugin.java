package com.fs.starfarer.api.campaign.impl.items;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.DismissDialogDelegate;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ShroudedSubstratePlugin extends BaseSpecialItemPlugin {
	
	public static String PLAYER_CAN_MAKE_WEAPONS = "$canMakeDwellerWeapons"; // in player memory
	public static String SHROUDED_SUBSTRATE_AVAILABLE = "$shroudedSubstrateAvailable"; // in player memory
	
	public static boolean isPlayerCanMakeWeapons() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(PLAYER_CAN_MAKE_WEAPONS);
	}
	public static void setPlayerCanMakeWeapons() {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(PLAYER_CAN_MAKE_WEAPONS, true);
	}
	

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
			if (isPlayerCanMakeWeapons()) {
				tooltip.addPara("Right-click to manufacture a weapon", b, opad);
			} else {
				tooltip.addPara("Right-click to analyze the " + getName(), b, opad);
			}
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
		return false;
	}

	@Override
	public void performRightClickAction(RightClickActionHelper helper) {
		Global.getSoundPlayer().playUISound(getSpec().getSoundId(), 1f, 1f);
		
		int substrate = (int) helper.getNumItems(CargoItemType.SPECIAL, new SpecialItemData(Items.SHROUDED_SUBSTRATE, null));
		Global.getSector().getPlayerMemoryWithoutUpdate().set(SHROUDED_SUBSTRATE_AVAILABLE, substrate, 0f);
		
		RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl("ShroudedSubstrateRightClick");
		plugin.setCustom1(helper);
		Global.getSector().getCampaignUI().showInteractionDialogFromCargo(plugin, 
				Global.getSector().getPlayerFleet(), new DismissDialogDelegate() {
					@Override
					public void dialogDismissed() {
					}
				});
	}
	
	
}









