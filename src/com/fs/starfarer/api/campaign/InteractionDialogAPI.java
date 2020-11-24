package com.fs.starfarer.api.campaign;

import java.util.List;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;


public interface InteractionDialogAPI {

	void setTextWidth(float width);
	void setTextHeight(float height);
	void setXOffset(float xOffset);
	void setYOffset(float yOffset);
	void setPromptText(String promptText);
	
	void hideTextPanel();
	void showTextPanel();
	
	float getTextWidth();
	float getTextHeight();
	float getXOffset();
	float getYOffset();
	String getPromptText();
	
	void flickerStatic(float in, float out);

	OptionPanelAPI getOptionPanel();
	TextPanelAPI getTextPanel();
	VisualPanelAPI getVisualPanel();
	
	SectorEntityToken getInteractionTarget();
	
	InteractionDialogPlugin getPlugin();


	/**
	 * optionSelected() with these parameters will be called when the player presses
	 * "escape" while in the dialog, as the dialog is being dismissed.
	 * 
	 * Can be set to null to have the escape key do nothing.
	 * @param text
	 * @param optionId
	 */
	void setOptionOnEscape(String text, Object optionId);

	void startBattle(BattleCreationContext context);
	void dismiss();
	void dismissAsCancel();
	
	void showFleetMemberPickerDialog(String title, String okText, String cancelText,
									 int rows, int cols, float iconSize, boolean canPickNotReady, boolean canPickMultiple,
									 List<FleetMemberAPI> pool, FleetMemberPickerListener listener);
	
	
	
//	/**
//	 * Temporary hack, to be used until proper dialog mechanics are in.
//	 * Doesn't belong in this interface conceptually (hence "hack").
//	 * @param context
//	 * @param playerFleet
//	 * @param otherFleet
//	 * @return
//	 */
//	String getNPCText(FleetEncounterContextPlugin context, CampaignFleetAPI playerFleet, CampaignFleetAPI otherFleet);
	
	void hideVisualPanel();
	
	void showCommDirectoryDialog(CommDirectoryAPI dir);
	void setOptionOnConfirm(String text, Object optionId);
	void setOpacity(float opacity);
	void setBackgroundDimAmount(float backgroundDimAmount);
	
	void setPlugin(InteractionDialogPlugin plugin);
	void setInteractionTarget(SectorEntityToken interactionTarget);
	void showFleetMemberRecoveryDialog(String title, List<FleetMemberAPI> pool, FleetMemberPickerListener listener);
	void showCargoPickerDialog(String title, String okText, String cancelText, 
			boolean small, float textPanelWidth, CargoAPI cargo, CargoPickerListener listener);
	void makeOptionOpenCore(String optionId, CoreUITabId tabId, CoreUITradeMode mode);
	void showIndustryPicker(String title, String okText, MarketAPI market,
						List<Industry> industries, IndustryPickerListener listener);
}



