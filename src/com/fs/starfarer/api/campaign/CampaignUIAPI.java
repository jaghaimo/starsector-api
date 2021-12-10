package com.fs.starfarer.api.campaign;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.HintPanelAPI;
import com.fs.starfarer.api.util.FaderUtil;

public interface CampaignUIAPI {
	public static enum CoreUITradeMode {
		OPEN,
		SNEAK,
		NONE,
	}
	
	MessageDisplayAPI getMessageDisplay();
	
	void addMessage(String text);
	void addMessage(String text, Color color);
	void addMessage(String text, Color color, String h1, String h2, Color hc1, Color hc2);
	
	void clearMessages();
	
	boolean isShowingDialog();
	void startBattle(BattleCreationContext context);
	
	/**
	 * Returns true if dialog was actually shown, false otherwise (if, for example, UI is already showing another dialog).
	 * @param plugin
	 * @param interactionTarget can be null.
	 */
	boolean showInteractionDialog(InteractionDialogPlugin plugin, SectorEntityToken interactionTarget);
	
	
	/**
 	 * Returns true if dialog was actually shown, false otherwise (if, for example, UI is already showing another dialog).
	 * Picks whatever dialog is appropriate based on the various plugins that provide dialog
	 * implementations.
	 * @param interactionTarget
	 */
	boolean showInteractionDialog(SectorEntityToken interactionTarget);
	
	
	void showCoreUITab(CoreUITabId tab);
	
	/**
	 * @param tab
	 * @param custom CommMessageAPI to select in intel tab, or FleetMemberAPI to select in refit tab
	 */
	void showCoreUITab(CoreUITabId tab, Object custom);
	
	InteractionDialogAPI getCurrentInteractionDialog();

	void setDisallowPlayerInteractionsForOneFrame();

	FaderUtil getSharedFader();

	//void suppressMusic();

	float getZoomFactor();

	void suppressMusic(float maxLevel);

	boolean isShowingMenu();

	void resetViewOffset();

	List<HullModSpecAPI> getAvailableHullModsCopy();

	boolean isHullModAvailable(String id);

	List<String> getAvailableHullModIds();

	HintPanelAPI getHintPanel();

	void quickLoad();

	CoreUITabId getCurrentCoreTab();

	void cmdExitWithoutSaving();
	void cmdSaveAndExit();
	void cmdSettings();
	void cmdSaveCopy();
	void cmdSave();
	void cmdLoad();
	void cmdCodex();

	boolean showConfirmDialog(String message, String ok, String cancel, Script onOk, Script onCancel);
	boolean showConfirmDialog(String message, String ok, String cancel, float width, float height, Script onOk, Script onCancel);

	void addMessage(IntelInfoPlugin intel);
	void addMessage(IntelInfoPlugin intel, MessageClickAction action);
	void addMessage(IntelInfoPlugin intel, MessageClickAction action, Object custom);

	void setFollowingDirectCommand(boolean followingDirectCommand);
	boolean isFollowingDirectCommand();

	void clearLaidInCourse();

	/**
	 * @return true if was shown (may not be if showing another dialog etc)
	 */
	boolean showPlayerFactionConfigDialog();

	void showHelpPopupIfPossible(String id);

	SectorEntityToken getCurrentCourseTarget();
	SectorEntityToken getNextStepForCourse(SectorEntityToken courseTarget);
	void layInCourseForNextStep(SectorEntityToken courseTarget);
	String getNameForCourseTarget(SectorEntityToken entity, boolean isEndpoint);
	float getLastLegDistance(SectorEntityToken courseTarget);

	boolean isFastForward();

	boolean isPlayerFleetFollowingMouse();

	void showMessageDialog(String message);

}




