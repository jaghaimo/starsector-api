package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.econ.AbandonMarketPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.ShowDefaultVisual;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class AbandonMarketPluginImpl extends BaseGenericPlugin implements AbandonMarketPlugin {

	public static int MAX_SIZE = 4;
	public static int COST_PER_SIZE_MINUS_2 = 50000;
	
	public boolean canAbandon(MarketAPI market) {
		return market.getSize() <= MAX_SIZE;// && market.getPlanetEntity() != null;
	}

	public int getAbandonCost(MarketAPI market) {
		return Math.max(1, market.getSize() - 2) * COST_PER_SIZE_MINUS_2;
	}
	
	public void createAbandonButtonTooltip(TooltipMakerAPI info, float width, boolean expanded, MarketAPI market) {
		info.addPara("Can only abandon colonies of size %s or smaller.", 0f,
				Misc.getNegativeHighlightColor(), Misc.getHighlightColor(),
				"" + MAX_SIZE);
	}

	public boolean isAbandonButtonTooltipExpandable(MarketAPI market) {
		return false;
	}

	public boolean abandonButtonHasTooltip(MarketAPI market) {
		return !canAbandon(market);
	}
	

	public void createConfirmationPrompt(MarketAPI market, TooltipMakerAPI prompt) {
		float opad = 10f;
		
		int cost = getAbandonCost(market);
		int refund = Misc.computeTotalShutdownRefund(market);
		int credits = (int) Global.getSector().getPlayerFleet().getCargo().getCredits().get();
		
		Color h = Misc.getHighlightColor();
		Color cc = h;
		
		String costStr = "cost";
		if (refund > cost) {
			costStr = "payout";
		}
		
		if (cost - refund > credits) {
			cc = Misc.getNegativeHighlightColor();
		}
		
//		prompt.setParaFontDefault();
//		prompt.setParaSmallInsignia();
		
		FactionAPI faction = market.getFaction();
//		LabelAPI label = prompt.addPara("You can abandon %s, a size %s colony, for a cost of %s in evacuation expenses.", 0f,
//				h, market.getName(), "" + market.getSize(), Misc.getDGSCredits(cost));
		LabelAPI label = prompt.addPara("Abandoning %s, a size %s colony, requires %s in evacuation expenses. " +
				"Shutting down all operations will generate %s credits, for a net " + costStr + " of %s.",
				0f,
				h,
				market.getName(), 
				"" + market.getSize(),
				Misc.getDGSCredits(cost),
				Misc.getDGSCredits(refund),
				Misc.getDGSCredits(Math.abs(cost - refund))
				);
		label.setHighlightColors(faction.getBaseUIColor(), h, h, h, cc);
		
		
		prompt.addPara("You have %s credits.", opad, h, Misc.getDGSCredits(credits));
		
		prompt.addPara("This action can not be undone. All items in storage and in use by industries will be lost.", opad);
	}
	
	public boolean isConfirmEnabled(MarketAPI market) {
		int cost = getAbandonCost(market);
		int refund = Misc.computeTotalShutdownRefund(market);
		
		return Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= (cost - refund);
	}

	public float getConfirmationPromptWidth(MarketAPI market) {
		return 550f;
	}

	public void abandonConfirmed(MarketAPI market) {
		
		int cost = getAbandonCost(market);
		int refund = Misc.computeTotalShutdownRefund(market);
		
		int diff = cost - refund;
		
		Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(diff);
		
		if (diff > 0) {
			Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
					String.format("Spent %s", Misc.getDGSCredits(diff)), 
					Misc.getTooltipTitleAndLightHighlightColor(), Misc.getDGSCredits(diff), Misc.getHighlightColor());
		} else if (diff < 0) {
			Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
					String.format("Received %s", Misc.getDGSCredits(-diff)), 
					Misc.getTooltipTitleAndLightHighlightColor(), Misc.getDGSCredits(-diff), Misc.getHighlightColor());
		}
		
		DecivTracker.removeColony(market, false);
		
		InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
		if (dialog != null && dialog.getPlugin() instanceof RuleBasedDialog) {
			//dialog.dismiss();
			//dialog.getVisualPanel().closeCoreUI();
			RuleBasedDialog rbd = ((RuleBasedDialog) dialog.getPlugin());
			rbd.updateMemory();
			market.getMemoryWithoutUpdate().unset("$tradeMode");
			if (market.getPrimaryEntity() != null) {
				market.getPrimaryEntity().getMemoryWithoutUpdate().unset("$tradeMode");
			}
			//FireBest.fire(null, dialog, rbd.getMemoryMap(), "OpenInteractionDialog");
			
			new ShowDefaultVisual().execute(null, dialog, Misc.tokenize(""), rbd.getMemoryMap());
			
			//FireAll.fire(null, dialog, memoryMap, "MarketPostOpen");
			dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$menuState", "main", 0);
			dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$tradeMode", "NONE", 0);
			dialog.getInteractionTarget().getMemoryWithoutUpdate().unset("$hasMarket");
			//FireAll.fire(null, dialog, rbd.getMemoryMap(), "PopulateOptions");
			
			dialog.getVisualPanel().closeCoreUI();
			
			rbd.updateMemory();
			FireAll.fire(null, dialog, rbd.getMemoryMap(), "OpenInteractionDialog");
		} else {
			// if abandoned from command tab (rather than by interacting with colony), go back to colony list
			Global.getSector().getCampaignUI().showCoreUITab(CoreUITabId.OUTPOSTS);
		}
	}

}






