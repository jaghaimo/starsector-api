package com.fs.starfarer.api.impl.campaign.intel;

import java.awt.Color;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class TriTachyonDeal extends BaseIntelPlugin implements EconomyUpdateListener {
	
	public static enum AgreementEndingType {
		BROKEN,
	}
	
	// in $player memory
	public static final String HAS_TRI_TACH_DEAL = "$hasTriTachDeal";
	public static final String BROKE_TRI_TACH_DEAL = "$brokeTriTachDeal";
	
	public static float REP_FOR_BREAKING_DEAL = 0.5f;
	
	public static float MARKET_SIZE_TO_ACCESSIBILITY = 0.01f;
	
	public static boolean hasDeal() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(HAS_TRI_TACH_DEAL);
	}
	public static void setHasDeal(boolean deal) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(HAS_TRI_TACH_DEAL, deal);
	}
	public static boolean brokeDeal() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(BROKE_TRI_TACH_DEAL);
	}
	public static void setBrokeDeal(boolean broke) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(BROKE_TRI_TACH_DEAL, broke);
	}
	
	
	public static String KEY = "$triTachDeal_ref";
	public static TriTachyonDeal get() {
		return (TriTachyonDeal) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	public static String BUTTON_END = "End";
	
	public static String UPDATE_PARAM_ACCEPTED = "update_param_accepted";
	
	protected FactionAPI faction = null;
	protected AgreementEndingType endType = null;
	
	public TriTachyonDeal(InteractionDialogAPI dialog) {
		this.faction = Global.getSector().getFaction(Factions.TRITACHYON);
		
		setImportant(true);
		setHasDeal(true);
		
		TextPanelAPI text = null;
		if (dialog != null) text = dialog.getTextPanel();
		
		//Global.getSector().getListenerManager().addListener(this);
		Global.getSector().getEconomy().addUpdateListener(this);
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		Global.getSector().getIntelManager().addIntel(this, true);
		
		economyUpdated(); // apply the modifier right away 
		
		sendUpdate(UPDATE_PARAM_ACCEPTED, text);
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		setHasDeal(false);
		
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
		//Global.getSector().getListenerManager().removeListener(this);
		Global.getSector().getEconomy().removeUpdateListener(this);
		
		unapplyAccessModifier();
	}
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
	}

	protected Object readResolve() {
		return this;
	}
	
	public String getBaseName() {
		return "Tri-Tachyon Strategic Partnership";
	}

	public String getAcceptedPostfix() {
		return "Accepted";
	}
		
	public String getBrokenPostfix() {
		return "Dissolved";

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
	
	protected float computeColonySize(String factionId) {
		float size = 0f;
		for (MarketAPI curr : Global.getSector().getEconomy().getMarketsCopy()) {
			if (factionId.equals(curr.getFactionId())) {
				if (curr.hasCondition(Conditions.DECIVILIZED)) continue;
				size += curr.getSize();
			}
		}
		return size;
	}
	
	protected float computeAccessibilityBonusPlayer() {
		float size = computeColonySize(Factions.TRITACHYON);
		return size * MARKET_SIZE_TO_ACCESSIBILITY;
	}
	protected float computeAccessibilityBonusTriTach() {
		float size = computeColonySize(Factions.PLAYER);
		return size * MARKET_SIZE_TO_ACCESSIBILITY;
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
		
		int accessPlayer = (int)Math.round(computeAccessibilityBonusPlayer() * 100f);
		int accessTriTach = (int)Math.round(computeAccessibilityBonusTriTach() * 100f);

		if (!isEnded() && !isEnding()) {
			String fName = Global.getSector().getPlayerFaction().getDisplayName();
			LabelAPI label;
			label = info.addPara(fName + " colonies receive %s accessibility", initPad, tc, h, "+" + accessPlayer + "%");
			label.setHighlight(fName, "+" + accessPlayer + "%");
			label.setHighlightColors(Misc.getBasePlayerColor(), h);
			initPad = 0f;
			
			fName = faction.getDisplayName();
			label = info.addPara(fName + " colonies receive %s accessibility", initPad, tc, h, "+" + accessTriTach + "%");
			label.setHighlight(fName, "+" + accessTriTach + "%");
			label.setHighlightColors(faction.getBaseUIColor(), h);
		}
	
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
			info.addPara("Your agreement with the Tri-Tachyon Corporation is no longer in force.", opad);
			return;
		}
		
		info.addPara("A partnership with the %s that allows you to leverage your combined market power "
				+ "for mutual benefit.", opad, faction.getBaseUIColor(), faction.getDisplayNameLong());
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
		
		info.addPara("Each partner's accessibility bonus is based "
				+ "on the total size of the colonies of the other partner.", opad);
		
		info.addPara("The contract is carefully worded to survive even in the face of open hostilities, and is, "
				+ "from a legal perspective, perpetually binding.", opad);
		
		info.addPara("You can of course decide to end this partnership unilaterally, but there "
				+ "would be no possibility of re-negotiating a similar deal after displaying such a lack of respect "
				+ "for your contractual obligations.", opad);
	
		ButtonAPI button = info.addButton("End the partnership", BUTTON_END, 
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
				setBrokeDeal(true);
				Misc.incrUntrustwortyCount();
				TextPanelAPI text = dialog == null ? null : dialog.getTextPanel();
				Misc.adjustRep(Factions.TRITACHYON, -REP_FOR_BREAKING_DEAL, text);
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
			prompt.addPara("You can decide to end this partnership unilaterally, but taking this action would "
					+ "hurt your standing with %s, and there "
					+ "would be no possibility of re-negotiating a similar deal after displaying such a lack of respect "
					+ "for your contractual obligations.", 0f,
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
	
	public void commodityUpdated(String commodityId) {
	}
	
	public static String ACCESS_MOD_ID = "ttDeal_access";
	public void economyUpdated() {
		float player = computeAccessibilityBonusPlayer();
		float triTach = computeAccessibilityBonusTriTach();
		String descPlayer = "Strategic partnership with " + faction.getDisplayName(); 
		String descTriTach = "Strategic partnership with " + Global.getSector().getPlayerFaction().getDisplayName(); 
		for (MarketAPI curr : Global.getSector().getEconomy().getMarketsCopy()) {
			float mod = 0f;
			String desc = null;
			if (Factions.TRITACHYON.equals(curr.getFactionId())) {
				mod = triTach;
				desc = descTriTach;
			} else if (Factions.PLAYER.equals(curr.getFactionId())) {
				mod = player;
				desc = descPlayer;
			}
			if (mod != 0) {
				curr.getAccessibilityMod().modifyFlat(ACCESS_MOD_ID, mod, desc);
			} else {
				curr.getAccessibilityMod().unmodifyFlat(ACCESS_MOD_ID);
			}
		}
	}
	
	public void unapplyAccessModifier() {
		for (MarketAPI curr : Global.getSector().getEconomy().getMarketsCopy()) {
			curr.getAccessibilityMod().unmodifyFlat(ACCESS_MOD_ID);
		}
	}
	
	public boolean isEconomyListenerExpired() {
		return isEnding() || isEnded();
	}
	
	
}






