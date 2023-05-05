package com.fs.starfarer.api.campaign.comm;

import java.awt.Color;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StoryPointActionDelegate;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

public interface IntelInfoPlugin {
	
	public static class TableRowClickData {
		public Object rowId;
		public UIPanelAPI table;
		public TableRowClickData(Object rowId, UIPanelAPI table) {
			this.rowId = rowId;
			this.table = table;
		}
		
	}
	
	public static enum ListInfoMode {
		MESSAGES,
		INTEL,
		MAP_TOOLTIP,
		IN_DESC, // not used from core but useful for some implementation details
	}
	
	public static class ArrowData {
		public float alphaMult = 0.33f;
		public float width = 10f;
		public SectorEntityToken from;
		public SectorEntityToken to;
		public Color color;
		
		public ArrowData(SectorEntityToken from, SectorEntityToken to) {
			this.from = from;
			this.to = to;
		}

		public ArrowData(float width, SectorEntityToken from, SectorEntityToken to) {
			this.width = width;
			this.from = from;
			this.to = to;
		}

		public ArrowData(float width, SectorEntityToken from, SectorEntityToken to, Color color) {
			this.width = width;
			this.from = from;
			this.to = to;
			this.color = color;
		}
	}
	
	public static final float LIST_ITEM_TEXT_WIDTH = 261f;
	public static final float NEW_DAYS = 5f;
	
	/**
	 * Lower-tier shown first.
	 */
	public static enum IntelSortTier {
		TIER_0,
		TIER_1,
		TIER_2,
		TIER_3, // default
		TIER_4,
		TIER_5,
		TIER_6,
		TIER_COMPLETED,
	}
	
	
	/**
	 * 40x40, no icon if null.
	 * @return
	 */
	String getIcon();
	
	/**
	 * 20x20, if null will use default.
	 * @return
	 */
	String getImportantIcon();
	
	boolean hasImportantButton();
	boolean canTurnImportantOff();
	
	
	Color getBackgroundGlowColor();
	
//	void createIntelListInfo(TooltipMakerAPI info);
//	void createMessageListInfo(TooltipMakerAPI info);
	
	void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode);

	boolean hasSmallDescription();
	String getSmallDescriptionTitle();
	void createSmallDescription(TooltipMakerAPI info, float width, float height);
	
	boolean hasLargeDescription();
	void createLargeDescription(CustomPanelAPI panel, float width, float height);
	
	void notifyPlayerAboutToOpenIntelScreen();
	boolean shouldRemoveIntel();
	
	/**
	 * Method NEEDS to handle map being null gracefully.
	 * @param map
	 * @return
	 */
	Set<String> getIntelTags(SectorMapAPI map);
	boolean isImportant();
	void setImportant(Boolean important);
	SectorEntityToken getMapLocation(SectorMapAPI map);
	
	List<ArrowData> getArrowData(SectorMapAPI map);
	
	
	boolean doesButtonHaveConfirmDialog(Object buttonId);
	StoryPointActionDelegate getButtonStoryPointActionDelegate(Object buttonId);
	float getConfirmationPromptWidth(Object buttonId);
	void createConfirmationPrompt(Object buttonId, TooltipMakerAPI prompt);
	String getConfirmText(Object buttonId);
	String getCancelText(Object buttonId);
	FactionAPI getFactionForUIColors();
	
	void buttonPressConfirmed(Object buttonId, IntelUIAPI ui);
	void buttonPressCancelled(Object buttonId, IntelUIAPI ui);
	void storyActionConfirmed(Object buttonId, IntelUIAPI ui);
	
	void setPlayerVisibleTimestamp(Long timestamp);
	Long getPlayerVisibleTimestamp();
	boolean autoAddCampaignMessage();
	String getCommMessageSound();
	
	/**
	 * Only checked if adding using IntelManager.queueIntel(). addIntel() bypasses this and all other checks.
	 * @param playerInRelayRange
	 * @return
	 */
	boolean canMakeVisibleToPlayer(boolean playerInRelayRange);
	void reportMadeVisibleToPlayer();
	void reportPlayerClickedOn();
	void reportRemovedIntel();
	
	boolean isNew();
	void setNew(boolean isNew);
	
	IntelSortTier getSortTier();
	String getSortString();

	
	/**
	 * Whether to actually show this piece of intel in the intel screen/show messages or updates for it,
	 * despite it being technically known to the player. 
	 * 
	 * Something can have "reportMadeVisibleToPlayer" called on it but still be hidden.
	 * 
	 * @return
	 */
	boolean isHidden();
	void setHidden(boolean hidden);	
	
	/**
	 * Should return 0 if the concept doesn't apply.
	 * @return
	 */
	float getTimeRemainingFraction();

	
	Color getCircleBorderColorOverride();
	
	boolean forceAddNextFrame();
	void setForceAddNextFrame(boolean add);

	boolean isEnded();

	boolean isEnding();


	void tableRowClicked(IntelUIAPI ui, TableRowClickData data);
}





