package com.fs.starfarer.api.impl.campaign.intel;

import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public abstract class BaseMissionIntel extends BaseIntelPlugin {
	
	public static String BUTTON_ACCEPT = "Accept";
	public static String BUTTON_ABANDON = "Abandon";
	
	public static enum MissionState {
		POSTED,
		CANCELLED,
		FAILED,
		ACCEPTED,
		ABANDONED,
		COMPLETED,
	}
	
	public static class MissionResult {
		public int payment;
		public ReputationAdjustmentResult rep1;
		public ReputationAdjustmentResult rep2;
		
		public Object custom;

		
		public MissionResult() {
			super();
		}

		public MissionResult(int payment, ReputationAdjustmentResult rep1) {
			this(payment, rep1, null);
		}

		public MissionResult(int payment, ReputationAdjustmentResult rep1, ReputationAdjustmentResult rep2) {
			this.payment = payment;
			this.rep1 = rep1;
			this.rep2 = rep2;
		}
		
	}

	protected IntervalUtil randomCancel = null;
	protected Float randomCancelProb = null;
	protected MissionResult missionResult;
	protected MissionState missionState = MissionState.POSTED;
	
	protected Float duration = null;
	protected float elapsedDays = 0f;
	
	protected void initRandomCancel() {
		initRandomCancel(0.5f);
	}
	protected void initRandomCancel(float prob) {
		randomCancel = new IntervalUtil(4, 6);
		randomCancelProb = prob;
	}
	
	@Override
	public void advanceImpl(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		
		if (isPosted()) {
			if (randomCancel != null) {
				randomCancel.advance(days);
				if (randomCancel.intervalElapsed()) {
					if ((float) Math.random() < randomCancelProb) {
						setMissionState(MissionState.CANCELLED);
						missionResult = createCancelledResult();
						endMission();
						sendUpdateIfPlayerHasIntel(missionResult, true);
					}
				}
			}
			return;
		}
		//getFactionForUIColors().getDisplayName()
		elapsedDays += days;
		
		if (duration != null && duration - elapsedDays <= 0) {
			setMissionState(MissionState.FAILED);
			missionResult = createTimeRanOutFailedResult();
			endMission();
			sendUpdateIfPlayerHasIntel(missionResult, false);
			return;
		}
		
		advanceMission(amount);
	}
	
	public float getTimeRemainingFraction() {
		if (!isAccepted() || duration == null) return super.getTimeRemainingFraction();
		
		float f = 1f - elapsedDays / duration;
		return f;
	}
	
	public boolean isPosted() {
		return missionState == MissionState.POSTED;
	}
	public boolean isCancelled() {
		return missionState == MissionState.CANCELLED;
	}
	
	public boolean isFailed() {
		return missionState == MissionState.FAILED;
	}
	
	public boolean isCompleted() {
		return missionState == MissionState.COMPLETED;
	}
	
	public boolean isAccepted() {
		return missionState == MissionState.ACCEPTED;
	}
	
	public boolean isAbandoned() {
		return missionState == MissionState.ABANDONED;
	}
	
	public boolean canAbandonWithoutPenalty() {
		return elapsedDays < getNoPenaltyAbandonDays();
	}
	
	protected float getNoPenaltyAbandonDays() {
		return 1f;
	}
	
	@Override
	public String getImportantIcon() {
		if (isAccepted()) {
			return Global.getSettings().getSpriteName("intel", "important_accepted_mission");
		}
		return super.getImportantIcon();
	}
	
	
	abstract public void endMission();
	abstract public void advanceMission(float amount);
	abstract public void missionAccepted();
	abstract protected MissionResult createTimeRanOutFailedResult();
	abstract protected MissionResult createAbandonedResult(boolean withPenalty);
	
	protected MissionResult createCancelledResult() {
		return new MissionResult();
	}
	
	
	protected String getMissionTypeNoun() {
		return "mission";
	}
	
	@Override
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_ACCEPT) {
			setImportant(true);
			setMissionState(MissionState.ACCEPTED);
			missionAccepted();
		} else if (buttonId == BUTTON_ABANDON) {
			setImportant(false);
			
			if (!canAbandonWithoutPenalty()) {
				setMissionState(MissionState.ABANDONED);
				missionResult = createAbandonedResult(true);
				endMission();
			} else {
				setMissionState(MissionState.ABANDONED);
				missionResult = createAbandonedResult(false);
				endMission();
			}
		}
		super.buttonPressConfirmed(buttonId, ui);
	}


	@Override
	public void createConfirmationPrompt(Object buttonId, TooltipMakerAPI prompt) {
		FactionAPI faction = getFactionForUIColors();
		
		if (buttonId == BUTTON_ACCEPT) {
			prompt.addPara("Accepting this " + getMissionTypeNoun() + " will commit you to completing it. " +
					"Failing to complete it within the required timeframe will result in a reputation penalty " +
					"with " + faction.getDisplayNameWithArticle() + ".", 0f,
						Misc.getTextColor(), faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
		} else if (buttonId == BUTTON_ABANDON) {
			if (canAbandonWithoutPenalty()) {
				prompt.addPara("It's been less than a day, and you can still abandon this " + getMissionTypeNoun() + " without a penalty.", 0f);
			} else {
				prompt.addPara("You can abandon this " + getMissionTypeNoun() + ", but will suffer " +
						"a reputation penalty with " + faction.getDisplayNameWithArticle() + ".", 0f,
						Misc.getTextColor(), faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
			}
		}
	}
	
	@Override
	public boolean doesButtonHaveConfirmDialog(Object buttonId) {
		if (buttonId == BUTTON_ACCEPT) {
			return true;
		}
		return true;
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_MISSIONS);
		if (isAccepted() || isAbandoned() || isFailed() || isCompleted()) {
			tags.add(Tags.INTEL_ACCEPTED);
		}
		return tags;
	}
	
	protected void addGenericMissionState(TooltipMakerAPI info) {
		float opad = 10f;
		String noun = getMissionTypeNoun();
		if (isAccepted()) {
			info.addPara("You have accepted this " + noun + ".", opad);
		} else if (isFailed()) {
			info.addPara("You have failed this " + noun + ".", opad);
		} else if (isCompleted()) {
			info.addPara("You have completed this " + noun + ".", opad);
		} else if (isCancelled()) {
			info.addPara("This " + noun + " is no longer being offered.", opad);
		} else if (isAbandoned()) {
			info.addPara("You have abandoned this " + noun + ".", opad);
		} else if (isPosted()) {
			info.addPara("This " + noun + " posting may be withdrawn at any time unless it's accepted.", 
					Misc.getHighlightColor(), opad);
					//Misc.getGrayColor(), opad);
		}
	}
	
	public String getPostfixForState() {
		if (isEnding()) {
			if (isCompleted()) {
				return " - Completed";	
			} else if (isFailed()) {
				return " - Failed";
			} else if (isAbandoned()) {
				return " - Abandoned";
			} else if (isCancelled()) {
				return " - Withdrawn";
			}
			return " - Terminated";
		}
		return "";
	}
	
//	public String getNameBasedOnState(String prefix, String name) {
//		prefix += " ";
//		if (isEnding()) {
//			if (isCompleted()) {
//				return prefix + name + " - Completed";	
//			} else if (isFailed()) {
//				return prefix + name + " - Failed";
//			} else if (isAbandoned()) {
//				return prefix + name + " - Abandoned";
//			} else if (isCancelled()) {
//				return prefix + name + " - Withdrawn";
//			}
//			return prefix + name + " - Terminated";
//		}
//		return prefix + name;
//	}

	protected void addAcceptOrAbandonButton(TooltipMakerAPI info, float width) {
		addAcceptOrAbandonButton(info, width, "Accept", "Abandon");
	}
	protected void addAcceptOrAbandonButton(TooltipMakerAPI info, float width, String accept, String abandon) {
		if (isPosted()) {
			addAcceptButton(info, width, accept);
		} else if (isAccepted()) {
			addAbandonButton(info, width, abandon);
		}
	}
	
	protected void addAcceptButton(TooltipMakerAPI info, float width) {
		addAcceptButton(info, width, "Accept");
	}
	protected void addAcceptButton(TooltipMakerAPI info, float width, String accept) {
		float opad = 10f;
		ButtonAPI button = info.addButton(accept, BUTTON_ACCEPT, 
				  	getFactionForUIColors().getBaseUIColor(), getFactionForUIColors().getDarkUIColor(),
				  (int)(width), 20f, opad * 2f);
		button.setShortcut(Keyboard.KEY_T, true);
	}
	
	protected void addAbandonButton(TooltipMakerAPI info, float width) {
		addAbandonButton(info, width, "Abandon");
	}
	protected void addAbandonButton(TooltipMakerAPI info, float width, String abandon) {
		float opad = 10f;
		ButtonAPI button = info.addButton(abandon, BUTTON_ABANDON, 
				getFactionForUIColors().getBaseUIColor(), getFactionForUIColors().getDarkUIColor(),
				(int)(width), 20f, opad * 2f);
		button.setShortcut(Keyboard.KEY_U, true);
	}

	public MissionResult getMissionResult() {
		return missionResult;
	}
	
	public void setMissionResult(MissionResult missionResult) {
		this.missionResult = missionResult;
	}
	
	public MissionState getMissionState() {
		return missionState;
	}
	
	public void setMissionState(MissionState missionState) {
		this.missionState = missionState;
	}
	
	public Float getDuration() {
		return duration;
	}
	
	public void setDuration(Float duration) {
		this.duration = duration;
	}
	
	public float getElapsedDays() {
		return elapsedDays;
	}
	
	public void setElapsedDays(float elapsedDays) {
		this.elapsedDays = elapsedDays;
	}
	
	
	public boolean shouldRemoveIntel() {
		if (timestamp == null && isEnding()) {
			return true; // already ending, and not yet player-visible; remove
		}
		return isEnded();
	}
	
	
}
