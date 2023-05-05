package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Highlights;

public interface SubmarketPlugin {
	
	public static enum PlayerEconomyImpactMode {
		PLAYER_SELL_ONLY,
		PLAYER_BUY_ONLY,
		BOTH,
		NONE,
	}
	
	public static enum TransferAction {
		PLAYER_BUY,
		PLAYER_SELL,
	}
	
	public static enum OnClickAction {
		OPEN_SUBMARKET,
		SHOW_TEXT_DIALOG,
	}
	
	public static class DialogOption {
		private String text;
		private Script action;
		public DialogOption(String text, Script action) {
			this.text = text;
			this.action = action;
		}
		public String getText() {
			return text;
		}
		public Script getAction() {
			return action;
		}
	}
	
	
	void init(SubmarketAPI submarket);
	String getName();
	
	void updateCargoPrePlayerInteraction();
	
	CargoAPI getCargo();
	void addAllCargo(CargoAPI otherCargo);
	
	boolean isIllegalOnSubmarket(String commodityId, TransferAction action);
	boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action);
	String getIllegalTransferText(CargoStackAPI stack, TransferAction action);
	Highlights getIllegalTransferTextHighlights(CargoStackAPI stack, TransferAction action);
	
	boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action);
	String getIllegalTransferText(FleetMemberAPI member, TransferAction action);
	Highlights getIllegalTransferTextHighlights(FleetMemberAPI member, TransferAction action);
	
	
	void advance(float amount);
	
	/**
	 * Fraction of value that gets used as tariff.
	 * @return
	 */
	float getTariff();
	
	
	boolean isFreeTransfer();
	String getSellVerb();
	String getBuyVerb();
	
	void reportPlayerMarketTransaction(PlayerMarketTransaction transaction);
	
	boolean isBlackMarket();
	boolean isOpenMarket();
	
	/**
	 * Whether transactions with this submarket affect the market's supply/demand and
	 * result in reputation changes for the player.
	 * @return
	 */
	boolean isParticipatesInEconomy();
	
	//boolean isEnabled();
	boolean isEnabled(CoreUIAPI ui);
	
	OnClickAction getOnClickAction(CoreUIAPI ui);
	String getDialogText(CoreUIAPI ui);
	Highlights getDialogTextHighlights(CoreUIAPI ui);
	DialogOption [] getDialogOptions(CoreUIAPI ui);
	
	String getTooltipAppendix(CoreUIAPI ui);
	Highlights getTooltipAppendixHighlights(CoreUIAPI ui);
	CargoAPI getCargoNullOk();
	
	boolean isTooltipExpandable();
	float getTooltipWidth();
	void createTooltip(CoreUIAPI ui, TooltipMakerAPI tooltip, boolean expanded);
	boolean hasCustomTooltip();

	boolean isHidden();
	boolean showInFleetScreen();
	boolean showInCargoScreen();
	float getPlayerTradeImpactMult();
	PlayerEconomyImpactMode getPlayerEconomyImpactMode();
	
	public String getTariffTextOverride();
	public String getTariffValueOverride();
	public String getTotalTextOverride();
	public String getTotalValueOverride();
	
	public SubmarketAPI getSubmarket();
	boolean okToUpdateShipsAndWeapons();
	
}






