package com.fs.starfarer.api.impl.campaign.intel;

import java.awt.Color;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.LuddicChurchHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.rulecmd.HA_CMD;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class LuddicChurchImmigrationDeal extends BaseIntelPlugin {
	
	public static enum AgreementEndingType {
		BROKEN,
	}
	
	public static float REP_FOR_BREAKING_DEAL = 0.25f;
	
	public static String KEY = "$luddicChurchDeal_ref";
	public static LuddicChurchImmigrationDeal get() {
		return (LuddicChurchImmigrationDeal) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	public static String BUTTON_END = "End";
	
	public static String UPDATE_PARAM_ACCEPTED = "update_param_accepted";
	
	protected FactionAPI faction = null;
	protected AgreementEndingType endType = null;
	
	public LuddicChurchImmigrationDeal(InteractionDialogAPI dialog) {
		this.faction = Global.getSector().getFaction(Factions.LUDDIC_CHURCH);
		
		setImportant(true);
		LuddicChurchHostileActivityFactor.setMadeDeal(true);
		
		TextPanelAPI text = null;
		if (dialog != null) text = dialog.getTextPanel();
		
		//Global.getSector().getListenerManager().addListener(this);
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		Global.getSector().getIntelManager().addIntel(this, true);
		
		sendUpdate(UPDATE_PARAM_ACCEPTED, text);
		
		HA_CMD.avertOrEndKOLTakeoverAsNecessary();
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		LuddicChurchHostileActivityFactor.setMadeDeal(false);
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
	}
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
	}

	protected Object readResolve() {
		return this;
	}
	
	public String getBaseName() {
		return "Luddic Church Immigration Controls";
	}

	public String getAcceptedPostfix() {
		return "Accepted";
	}
		
	public String getBrokenPostfix() {
		return "Ended";

	}
	
	public String getName() {
		String postfix = "";
		if (isEnding() && endType != null) {
			switch (endType) {
			case BROKEN:
				postfix = " - " + getBrokenPostfix();
				break;
			}
		}
		if (isSendingUpdate() && getListInfoParam() == UPDATE_PARAM_ACCEPTED) {
			postfix =  " - " + getAcceptedPostfix();
		}
		return getBaseName() + postfix;
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return faction;
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;
		
//		if (getListInfoParam() == UPDATE_PARAM_ACCEPTED) {
//			return;
//		}
		
		
	
		unindent(info);
	}
	
	
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		info.addImage(getFaction().getLogo(), width, 128, opad);
		
		if (isEnding() || isEnded()) {
			info.addPara("You are no longer abiding by your agreement with the Luddic Church.", opad);
			return;
		}
		
		info.addPara("You've made an agreement with the Luddic Church, curtailing "
				+ "excessive immigration from their worlds. In exchange, the Knights of Ludd \"protector\" "
				+ "fleets no longer operate in your volume.", 
				opad, faction.getBaseUIColor(), "Luddic Church", "Knights of Ludd");
		
		info.addPara("You can end this agreement, but there "
				+ "would be no possibility of re-negotiating a similar agreement after "
				+ "demonstrating such faithlessness.", opad);
	
		ButtonAPI button = info.addButton("End the agreement", BUTTON_END, 
				getFactionForUIColors().getBaseUIColor(), getFactionForUIColors().getDarkUIColor(),
				(int)(width), 20f, opad * 1f);
		button.setShortcut(Keyboard.KEY_U, true);
		
	}
	
	
	public String getIcon() {
		return faction.getCrest();
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_AGREEMENTS);
		tags.add(faction.getId());
		return tags;
	}
	
	@Override
	public String getImportantIcon() {
		return Global.getSettings().getSpriteName("intel", "important_accepted_mission");
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return null;
	}

	
	public FactionAPI getFaction() {
		return faction;
	}
	
	public void endAgreement(AgreementEndingType type, InteractionDialogAPI dialog) {
		if (!isEnded() && !isEnding()) {
			endType = type;
			setImportant(false);
			//endAfterDelay();
			endImmediately();
			
			if (dialog != null) {
				sendUpdate(new Object(), dialog.getTextPanel());
			}

			if (type == AgreementEndingType.BROKEN) {
				LuddicChurchHostileActivityFactor.setBrokeDeal(true);
				Misc.incrUntrustwortyCount();
				TextPanelAPI text = dialog == null ? null : dialog.getTextPanel();
				Misc.adjustRep(Factions.LUDDIC_CHURCH, -REP_FOR_BREAKING_DEAL, text);
			}
		}
	}
	
	
	@Override
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_END) {
			endAgreement(AgreementEndingType.BROKEN, null);
		}
		super.buttonPressConfirmed(buttonId, ui);
	}


	@Override
	public void createConfirmationPrompt(Object buttonId, TooltipMakerAPI prompt) {
		if (buttonId == BUTTON_END) {
			prompt.addPara("You can end this agreement, but taking this action would "
					+ "hurt your standing with the %s, and there "
					+ "would be no possibility of re-negotiating a similar agreement after "
					+ "demonstrating such faithlessness.", 0f,
					faction.getBaseUIColor(), faction.getDisplayName());				
		}
			
	}
	
	@Override
	public boolean doesButtonHaveConfirmDialog(Object buttonId) {
		if (buttonId == BUTTON_END) {
			return true;
		}
		return super.doesButtonHaveConfirmDialog(buttonId);
	}
	
	
}






