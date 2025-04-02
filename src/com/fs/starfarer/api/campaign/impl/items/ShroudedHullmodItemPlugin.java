package com.fs.starfarer.api.campaign.impl.items;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.DismissDialogDelegate;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ShroudedHullmodItemPlugin extends BaseSpecialItemPlugin {
	
	public static String SHROUDED_HULLMOD_ID = "$shroudedHullmodId";
	
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
			if (!playerKnowsHullmod()) {
				tooltip.addPara("Right-click to analyze the " + getName(), b, opad);
			}
		}
	}
	
	protected boolean playerKnowsHullmod() {
		return Global.getSector().getCharacterData().knowsHullMod(getHullmodId());
	}
	
	protected String getHullmodId() {
		return spec.getParams();
	}
	
	protected String getRightClickRuleTrigger() {
		return "ShroudedHullmodItemRC";
	}
	
	@Override
	public boolean hasRightClickAction() {
		return !playerKnowsHullmod();
	}

	@Override
	public boolean shouldRemoveOnRightClickAction() {
		return false;
	}

	@Override
	public void performRightClickAction(RightClickActionHelper helper) {
		Global.getSoundPlayer().playUISound(getSpec().getSoundId(), 1f, 1f);
		
		Global.getSector().getPlayerMemoryWithoutUpdate().set(SHROUDED_HULLMOD_ID, getHullmodId(), 0f);
				
		RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl(getRightClickRuleTrigger());
		plugin.setCustom1(helper);
		Global.getSector().getCampaignUI().showInteractionDialogFromCargo(plugin, 
				Global.getSector().getPlayerFleet(), new DismissDialogDelegate() {
					@Override
					public void dialogDismissed() {
					}
				});
	}
	
	
}









