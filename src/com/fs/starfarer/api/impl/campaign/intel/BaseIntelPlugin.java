package com.fs.starfarer.api.impl.campaign.intel;

import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CommRelayEntityPlugin.CommSnifferReadableIntel;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.CallEvent.CallableEvent;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * @author Alex Mosolov
 *
 * Copyright 2018 Fractal Softworks, LLC
 */
public class BaseIntelPlugin implements IntelInfoPlugin, CallableEvent, EveryFrameScript, CommSnifferReadableIntel {
	
	public static final String BULLET = "    - ";
	public static final String INDENT = "      ";
	
	protected Boolean important;
	protected Long timestamp;
	protected Boolean neverClicked = true;
	
	transient protected Object listInfoParam = null;
	
	protected Boolean ended = null; 
	protected Boolean ending = null; 
	protected Float endingTimeRemaining = null;
	
	protected SectorEntityToken postingLocation = null; 
	protected Float postingRangeLY = null; 
	
	public BaseIntelPlugin() {
	}
	
	
	public void advance(float amount) {
		if (isEnded()) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		if (isEnding()) {
			endingTimeRemaining -= days;
			if (endingTimeRemaining <= 0) {
				ended = true;
				notifyEnded();
			}
			return;
		}
		
		advanceImpl(amount);
	}
	
	protected void advanceImpl(float amount) {}
	
	protected void notifyEnding() {
		
	}
	
	protected void notifyEnded() {
		//Global.getSector().getIntelManager().removeIntel(this);
	}
	
	public void endImmediately() {
		endAfterDelay(0f);
	}
	public void endAfterDelay() {
		endAfterDelay(getBaseDaysAfterEnd());
	}
	public void endAfterDelay(float days) {
		endingTimeRemaining = days;
		boolean wasEnding = isEnding();
		ending = true;
		if (!wasEnding) {
			notifyEnding();
		}
		if (endingTimeRemaining <= 0) {
			ended = true;
			notifyEnded();
		}
	}
	
	protected float getBaseDaysAfterEnd() {
		return 3f;
	}

	public boolean isDone() {
		return isEnded();
	}

	public boolean runWhilePaused() {
		return false;
	}
	
	public boolean isEnding() {
		return ending != null && ending;
	}
	
	public boolean isEnded() {
		return ended != null && ended;
	}
	
	
	/**
	 * Call from createMessageListInfo() to figure out what type of update to create.
	 * Call from getCommMesageSound() to pick sound to play.
	 * @return
	 */
	public Object getListInfoParam() {
		return listInfoParam;
	}
	
	public void setListInfoParam(Object listInfoParam) {
		this.listInfoParam = listInfoParam;
	}


	public boolean isSendingUpdate() {
		return listInfoParam != null;
	}
	
	public void sendUpdate(Object listInfoParam, TextPanelAPI textPanel) {
		this.listInfoParam = listInfoParam;
		Global.getSector().getIntelManager().addIntelToTextPanel(this, textPanel);
		this.listInfoParam = null;
	}
	
	public void sendUpdateIfPlayerHasIntel(Object listInfoParam, boolean onlyIfImportant) {
		sendUpdateIfPlayerHasIntel(listInfoParam, onlyIfImportant, false);
	}
	public void sendUpdateIfPlayerHasIntel(Object listInfoParam, boolean onlyIfImportant, boolean sendIfHidden) {
		if (timestamp == null) return;
		
		if (onlyIfImportant && !isImportant()) return;
		
		if (!sendIfHidden && isHidden()) return;
		
		this.listInfoParam = listInfoParam;
		Global.getSector().getCampaignUI().addMessage(this, MessageClickAction.INTEL_TAB, this);
		this.listInfoParam = null;
	}


	public boolean canTurnImportantOff() {
		return true;
	}

	public String getImportantIcon() {
		return null;
	}

	public boolean hasImportantButton() {
		return true;
	}

	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		info.addPara("Override .createIntelListInfo()", Misc.getNegativeHighlightColor(), 0f);
	}
	
	public boolean hasSmallDescription() {
		return true;
	}
	
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		info.addPara("Override .createSmallDescription()", Misc.getNegativeHighlightColor(), 0f);
	}
	
	public boolean hasLargeDescription() {
		return false;
	}
	
	public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
		TooltipMakerAPI desc = panel.createUIElement(width, height, true);
		desc.addPara("Override .createLargeDescription()", Misc.getNegativeHighlightColor(), 0f);
		panel.addUIElement(desc).inTL(0, 0);
	}

	public String getIcon() {
		return null;
	}
	
//	public float getIconBrightness() {
//		if (isEnding() || isEnded()) return 0.5f;
//		return 1f;
//	}
	
	public Color getBackgroundGlowColor() {
		return null;
	}

	public boolean shouldRemoveIntel() {
//		if (this instanceof CommSnifferIntel) {
//			System.out.println("wefwefe");
//		}
		if (isImportant()) return false;
		
		if (timestamp == null && isEnding()) {
			return true; // already ending, and not yet player-visible; remove
		}
		return isEnded();
	}

	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = new LinkedHashSet<String>();
		if (isImportant()) {
			tags.add(Tags.INTEL_IMPORTANT);
		}
		if (isNew()) {
			tags.add(Tags.INTEL_NEW);
		}
		if (map != null) {
			SectorEntityToken loc = getMapLocation(map);
			if (loc != null) {
				float max = Global.getSettings().getFloat("maxRelayRangeInHyperspace");
				float dist = Misc.getDistanceLY(loc.getLocationInHyperspace(), Global.getSector().getPlayerFleet().getLocationInHyperspace());
				if (dist <= max) {
					tags.add(Tags.INTEL_LOCAL);
				}
			}
		}
		
//		FactionAPI faction = getFactionForUIColors();
//		if (faction != null && !faction.isPlayerFaction()) {
//			if (faction.isHostileTo(Factions.PLAYER)) {
//				tags.add(Tags.INTEL_HOSTILE);
//			} else {
//				tags.add(Tags.INTEL_NOT_HOSTILE);
//			}
//		}
		
		return tags;
	}

	
	public boolean isImportant() {
		return important != null && important;
	}

	public void setImportant(Boolean important) {
		this.important = important;
		if (this.important != null && !this.important) {
			this.important = null;
		}
	}
	
	public FactionAPI getFactionForUIColors() {
		return Global.getSector().getPlayerFaction();
	}
	
	public boolean doesButtonHaveConfirmDialog(Object buttonId) {
		return false;
	}
	public float getConfirmationPromptWidth(Object buttonId) {
		return 550f;
	}
	
	public void createConfirmationPrompt(Object buttonId, TooltipMakerAPI prompt) {

	}
	
	public String getConfirmText(Object buttonId) {
		return "Confirm";
	}

	public String getCancelText(Object buttonId) {
		return "Cancel";
	}
	
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		ui.updateUIForItem(this);
	}
	
	public void buttonPressCancelled(Object buttonId, IntelUIAPI ui) {
	}
	
	public boolean isPlayerVisible() {
		if (isHidden()) return false;
		if (isEnded()) return false;
		return timestamp != null;
	}

	public Long getPlayerVisibleTimestamp() {
		return timestamp;
	}

	public void setPlayerVisibleTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public float getDaysSincePlayerVisible() {
		long ts = Global.getSector().getClock().getTimestamp();
		if (timestamp != null) ts = timestamp;
		return Global.getSector().getClock().getElapsedDaysSince(ts);
	}
	
	public void reportPlayerClickedOn() {
		neverClicked = null; // makes it lose "new" status
	}
	
	public boolean isNew() {
		Long ts = getPlayerVisibleTimestamp();
		if (ts == null) ts = 0L;
		float days = Global.getSector().getClock().getElapsedDaysSince(ts);
		return neverClicked != null && neverClicked && days < NEW_DAYS;
	}
	
	public void setNew(boolean isNew) {
		if (isNew) {
			neverClicked = true;
		} else {
			neverClicked = null;
		}
	}
	
	public IntelSortTier getSortTier() {
		if (isEnding()) {
			return IntelSortTier.TIER_COMPLETED;
		}
		return IntelSortTier.TIER_3;
	}
	
	public String getSortString() {
		return null;
	}

	public boolean autoAddCampaignMessage() {
		return !isHidden();
	}
	
	public String getCommMessageSound() {
		if (isSendingUpdate()) {
			return getSoundStandardUpdate();
		}
		return getSoundStandardPosting();
	}
	
	
	protected Boolean hidden = null;
	public boolean isHidden() {
		// never mind; handled by making comm relay in Galatia non-functional until the jump-point is stabilized
		// but procurement missions etc generated inside Galatia still show up, so: need to do this
		return TutorialMissionIntel.isTutorialInProgress();  
		//return hidden != null;
	}
	
	
	public void setHidden(boolean hidden) {
		if (hidden) {
			this.hidden = hidden;
		} else {
			this.hidden = null;
		}
	}


	public void reportMadeVisibleToPlayer() {
		
	}
	
	protected float getCommRelayRange() {
		return Global.getSettings().getFloat("maxRelayRangeInHyperspace");
	}

	public boolean canMakeVisibleToPlayer(boolean playerInRelayRange) {
		return canMakeVisible(playerInRelayRange, 
									  Global.getSector().getPlayerFleet().getContainingLocation(),
									  Global.getSector().getPlayerFleet().getLocationInHyperspace(), false);
	}
	
	public boolean canMakeVisibleToCommSniffer(boolean playerInRelayRange, SectorEntityToken relay) {
		return canMakeVisible(playerInRelayRange, relay.getContainingLocation(), 
							  relay.getLocationInHyperspace(), true);
	}
	
	public boolean canMakeVisible(boolean playerInRelayRange, LocationAPI conLoc, Vector2f hyperLoc,
										  boolean commSniffer) {
		if (isEnding()) return false;
		
		if (postingLocation != null) {
			float rangeLY = 0f;
			if (postingRangeLY != null) {
				rangeLY = postingRangeLY;
			} else { // unless a range of 0 is specified, get local messages when nearing system
				rangeLY = getCommRelayRange();
			}
			
			float commRange = 0f;
			if (postingLocation.isInHyperspace()) {
				commRange = getCommRelayRange();
				rangeLY = Math.max(rangeLY, commRange);
			}

			boolean sameLoc = postingLocation.getContainingLocation() != null &&
			   				  postingLocation.getContainingLocation() == 
			   					conLoc;
			if (rangeLY <= 0 && !sameLoc) {
				return false;
			}
			
			if (playerInRelayRange) {
				float dist = Misc.getDistanceLY(postingLocation.getLocationInHyperspace(), 
												hyperLoc);
				return dist <= rangeLY;
			} else {
				if (postingLocation.isInHyperspace()) {
					float dist = Misc.getDistanceLY(postingLocation.getLocationInHyperspace(), 
													hyperLoc);
					return dist < commRange && (!commSniffer || playerInRelayRange);
				} else {
					return sameLoc && (!commSniffer || playerInRelayRange);
				}
			}
		}
		
		return playerInRelayRange;
	}

	public SectorEntityToken getPostingLocation() {
		return postingLocation;
	}

	public void setPostingLocation(SectorEntityToken postingLocation) {
		this.postingLocation = postingLocation;
	}

	public Float getPostingRangeLY() {
		return postingRangeLY;
	}

	public void setPostingRangeLY(Float postingRangeLY) {
		setPostingRangeLY(postingRangeLY, false);
	}
	public void setPostingRangeLY(Float postingRangeLY, boolean ensureVisibleOutsideSystem) {
		if (ensureVisibleOutsideSystem) {
			if (postingRangeLY == null) postingRangeLY = 0f;
			postingRangeLY = Math.max(postingRangeLY, getCommRelayRange());
		}
		this.postingRangeLY = postingRangeLY;
	}

	
	public void reportRemovedIntel() {
		
	}
	
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return null;
	}

	
	protected void bullet(TooltipMakerAPI info) {
		info.setBulletedListMode("    - ");
		if (listInfoParam != null && false) {
			info.setTextWidthOverride(0);
		} else {
			info.setTextWidthOverride(LIST_ITEM_TEXT_WIDTH);
		}
	}
	protected void indent(TooltipMakerAPI info) {
		info.setBulletedListMode("      ");
		if (listInfoParam != null && false) {
			info.setTextWidthOverride(0);
		} else {
			info.setTextWidthOverride(LIST_ITEM_TEXT_WIDTH);
		}
	}
	protected void unindent(TooltipMakerAPI info) {
		info.setBulletedListMode(null);
		info.setTextWidthOverride(0);
	}
	
	protected void addDays(TooltipMakerAPI info, String after, float days) {
		addDays(info, after, days, null);
	}
	protected void addDays(TooltipMakerAPI info, String after, float days, Color c) {
		addDays(info, after, days, c, 0f);
	}
	protected void addDays(TooltipMakerAPI info, String after, float days, Color c, float pad) {
		String pre = "";
		if (info.getBulletedListPrefix() != null) {
			pre = "";
		}
		
		int d = (int) Math.round(days);
		String daysStr = "days";
		if (d <= 1) {
			d = 1;
			daysStr = "day";
		}
		if (c == null) c = Misc.getGrayColor();
		info.addPara(pre + "%s " + daysStr + " " + after, pad, c,
				Misc.getHighlightColor(), "" + d);
	}

	protected String getDays(float days) {
		return "" + (int) Math.round(days);
	}
	public static String getDaysString(float days) {
		int d = (int) Math.round(days);
		String daysStr = "days";
		if (d <= 1) {
			d = 1;
			daysStr = "day";
		}
		return daysStr;
	}
	
	public String getSmallDescriptionTitle() {
		return null;
	}
	
	public Color getTitleColor(ListInfoMode mode) {
		boolean isUpdate = getListInfoParam() != null;
		if (isEnding() && !isUpdate && mode != ListInfoMode.IN_DESC) {
			return Misc.getGrayColor();
		}
		return Global.getSector().getPlayerFaction().getBaseUIColor();
	}
	
//	public static Color getListMessageBulletColor() {
//		//return Misc.getGrayColor();
//		return Misc.getTextColor();
//	}
//	
//	public static Color getIntelListBulletColor() {
//		return Misc.getGrayColor();
//		//return Misc.getTextColor();
//	}
	
	protected Color getBulletColorForMode(ListInfoMode mode) {
		//boolean isUpdate = getListInfoParam() != null;
		Color tc = Misc.getTextColor();
		//if (true) return tc;
		Color g = Misc.getGrayColor();
		switch (mode) {
		case INTEL: return g;
		case MAP_TOOLTIP: return tc;
		case IN_DESC: return tc;
		case MESSAGES: return tc;
		}

		return g;
	}
	

	public boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		return false;
	}
	
	
	public static String getSoundStandardPosting() {
		return "ui_intel_something_posted";
	}
	
	public static String getSoundStandardUpdate() {
		return "ui_intel_update";
	}
	
	public static String getSoundMajorPosting() {
		return "ui_intel_major_posted";
	}
	
	public static String getSoundColonyThreat() {
		return "colony_threat";
	}
	
	public static String getSoundLogUpdate() {
		return "ui_intel_log_update";
	}
	
	public static String getSoundMinorMessage() {
		return "ui_intel_minor_message";
	}

	public List<ArrowData> getArrowData(SectorMapAPI map) {
		return null;
	}


	public float getTimeRemainingFraction() {
		return 0;
	}
	
	
	protected ButtonAPI addGenericButton(TooltipMakerAPI info, float width, String text, Object data) {
		float opad = 10f;
		ButtonAPI button = info.addButton(text, data, 
				  	getFactionForUIColors().getBaseUIColor(), getFactionForUIColors().getDarkUIColor(),
				  (int)(width), 20f, opad * 2f);
		return button;
	}


	protected Boolean forceAdd = null;
	public boolean forceAddNextFrame() {
		return forceAdd != null;
	}

	public void setForceAddNextFrame(boolean add) {
		if (add) {
			forceAdd = true;
		} else {
			forceAdd = null;
		}
	}
	
	public static void addMarketToList(TooltipMakerAPI info, MarketAPI market, float pad) {
		String indent = BaseIntelPlugin.INDENT;
		if (info.getBulletedListPrefix() != null) indent = "";
		LabelAPI label = info.addPara(indent + market.getName() + " (size %s, %s)",
				//faction.getPersonNamePrefixAOrAn() + " %s colony.", 
				pad, market.getFaction().getBaseUIColor(),
				"" + (int) market.getSize(),
				market.getFaction().getDisplayName());
		
		label.setHighlight("" + (int) market.getSize(), market.getFaction().getDisplayName());
		label.setHighlightColors(Misc.getHighlightColor(), market.getFaction().getBaseUIColor());
	}
	
}






