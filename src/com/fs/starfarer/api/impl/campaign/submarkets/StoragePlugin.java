package com.fs.starfarer.api.impl.campaign.submarkets;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;

public class StoragePlugin extends BaseSubmarketPlugin {
	
	private boolean playerPaidToUnlock = false;
	
	public void init(SubmarketAPI submarket) {
		super.init(submarket);
	}

	public void updateCargoPrePlayerInteraction() {

	}
	
	@Override
	public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
		if (market.isPlayerOwned()) return false;
		return super.isIllegalOnSubmarket(stack, action);
	}

	@Override
	public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
		if (market.isPlayerOwned()) return false;
		return super.isIllegalOnSubmarket(commodityId, action);
	}
	
	@Override
	public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
		if (market.isPlayerOwned() || 
				(market.getFaction() != null && market.getFaction().isNeutralFaction())) return false;
		return super.isIllegalOnSubmarket(member, action);
	}
	
	public boolean isParticipatesInEconomy() {
		return false;
	}
	
	public float getTariff() {
		return 0f;
	}

	@Override
	public boolean isFreeTransfer() {
		return true;
	}

	@Override
	public String getBuyVerb() {
		return "Take";
	}

	@Override
	public String getSellVerb() {
		return "Leave";
	}

	public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
		return "Illegal to put into storage here";
	}

//	@Override
//	public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
//		// do nothing; don't want to adjust market stockpiles since player storage
//		// doesn't affect those
//		// don't need to do this since isParticipatesInEconomy() returns false
//	}
	
	
	
	public boolean isEnabled(CoreUIAPI ui) {
		//if (mode == CoreUITradeMode.SNEAK) return false;
		//return playerPaidToUnlock;
		return true;
	}
	
	public void setPlayerPaidToUnlock(boolean playerPaidToUnlock) {
		this.playerPaidToUnlock = playerPaidToUnlock;
	}

	public OnClickAction getOnClickAction(CoreUIAPI ui) {
		if (playerPaidToUnlock) return OnClickAction.OPEN_SUBMARKET;
		return OnClickAction.SHOW_TEXT_DIALOG;
	}
	
	private int getUnlockCost() {
		return 5000;
	}
	
	private boolean canPlayerAffordUnlock() {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		int credits = (int) playerFleet.getCargo().getCredits().get();
		return credits >= getUnlockCost();
	}
	
	public String getDialogText(CoreUIAPI ui) {
		if (canPlayerAffordUnlock()) {
			return "Gaining access to storage at this colony requires a one-time fee of " + getUnlockCost() + " credits.";
		} else {
			return "Gaining access to storage at this colony requires a one-time fee of " + getUnlockCost() + " credits, which you can't afford.";
		}
	}
	public Highlights getDialogTextHighlights(CoreUIAPI ui) {
		Highlights h = new Highlights();
		h.setText("" + getUnlockCost());
		if (canPlayerAffordUnlock()) {
			h.setColors(Misc.getHighlightColor());
		} else {
			h.setColors(Misc.getNegativeHighlightColor());
		}
		return h;
	}
	public DialogOption [] getDialogOptions(CoreUIAPI ui) {
		if (canPlayerAffordUnlock()) {
			return new DialogOption [] {
				new DialogOption("Pay", new Script() {
					public void run() {
						CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
						playerFleet.getCargo().getCredits().subtract(getUnlockCost());
						playerPaidToUnlock = true;
					}
				}),
				new DialogOption("Never mind", null)
			};
		} else {
			return new DialogOption [] {
					new DialogOption("Never mind", null)
			};
		}
	}
	public String getTooltipAppendix(CoreUIAPI ui) {
//		if (!playerPaidToUnlock) {
//			return "Requires a one-time access fee of " + getUnlockCost() + " credits.";
//		}
		return null;
	}
	public Highlights getTooltipAppendixHighlights(CoreUIAPI ui) {
//		String appendix = getTooltipAppendix(ui);
//		if (appendix == null) return null;
//		
//		Highlights h = new Highlights();
//		h.setText("" + getUnlockCost());
//		if (canPlayerAffordUnlock()) {
//			h.setColors(Misc.getHighlightColor());
//		} else {
//			h.setColors(Misc.getNegativeHighlightColor());
//		}
//		return h;
		return null;
	}
	
	@Override
	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		
		if (!market.isInEconomy()) return;
		
		float opad = 10f;
		float pad = 3f;
		if (market.isPlayerOwned()) {
			tooltip.addPara(Misc.getTokenReplaced("$marketName is under your control, and there " +
					"are no storage fees or expenses.", market.getPrimaryEntity()), opad); 
			return;
		}
		
		float f = Misc.getStorageFeeFraction();
		int percent = (int) (f * 100f);
		
		Color h = Misc.getHighlightColor();
		
		if (!playerPaidToUnlock) {
			tooltip.addPara("Requires a one-time access fee of %s, and a monthly fee equal to %s of the " +
					"base value of the items in storage.", opad, h, 
					"" + getUnlockCost() + Strings.C, "" + percent + "%");
			return;
		}
		
		int cargoCost = (int) (Misc.getStorageCargoValue(market) * f);
		int shipCost = (int) (Misc.getStorageShipValue(market) * f);
		
		if (cargoCost + shipCost > 0) {
			//tooltip.beginGrid(150, 1);
			tooltip.addPara("Monthly fees and expenses (%s of base value of stored items):", opad, h, "" + percent + "%");
			tooltip.beginGridFlipped(300, 1, 80, 10);
			int j = 0;
			tooltip.addToGrid(0, j++, "Ships in storage", Misc.getDGSCredits(shipCost));
			tooltip.addToGrid(0, j++, "Cargo in storage", Misc.getDGSCredits(cargoCost));
			tooltip.addGrid(pad);
		} else {
			tooltip.addPara("Monthly fees and expenses are equal to %s of base value of the stored items.", opad, h, "" + percent + "%");
		}
	}
	
	
}





