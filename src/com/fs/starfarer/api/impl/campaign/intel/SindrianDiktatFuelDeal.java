package com.fs.starfarer.api.impl.campaign.intel;

import java.awt.Color;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.SindrianDiktatHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.rulecmd.HA_CMD;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class SindrianDiktatFuelDeal extends BaseIntelPlugin implements EconomyTickListener {
	
	public static enum AgreementEndingType {
		BROKEN,
	}
	
	public static float REP_FOR_BREAKING_DEAL = 0.5f;
	
	public static String KEY = "$sindrianDiktatDeal_ref";
	public static SindrianDiktatFuelDeal get() {
		return (SindrianDiktatFuelDeal) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	public static String BUTTON_END = "End";
	
	public static String UPDATE_PARAM_ACCEPTED = "update_param_accepted";
	
	protected FactionAPI faction = null;
	protected AgreementEndingType endType = null;
	
	public SindrianDiktatFuelDeal(InteractionDialogAPI dialog) {
		this.faction = Global.getSector().getFaction(Factions.DIKTAT);
		
		setImportant(true);
		SindrianDiktatHostileActivityFactor.setMadeDeal(true);
		
		TextPanelAPI text = null;
		if (dialog != null) text = dialog.getTextPanel();
		
		Global.getSector().getListenerManager().addListener(this);
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		Global.getSector().getIntelManager().addIntel(this, true);
		
		sendUpdate(UPDATE_PARAM_ACCEPTED, text);
		
		HA_CMD.avertOrEndDiktatAttackAsNecessary();
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		SindrianDiktatHostileActivityFactor.setMadeDeal(false);
		Global.getSector().getListenerManager().removeListener(this);
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
		return "Sindrian Diktat Fuel Accord";
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
			info.addPara("You are no longer abiding by your agreement with the Sindrian Diktat.", opad);
			return;
		}
		
		float feeFraction = Global.getSettings().getFloat("diktatFuelFeeFraction");
		
		LabelAPI label = info.addPara("You've made an agreement with the Sindrian Diktat, paying a sizeable "
				+ "fee, %s of your gross fuel exports, in exchange for them not bombarding "
				+ "your fuel production facilities from orbit.", 
				opad, h, "" + (int) Math.round(feeFraction * 100f) + "%");
		label.setHighlight("Sindrian Diktat", "" + (int) Math.round(feeFraction * 100f) + "%");
		label.setHighlightColors(faction.getBaseUIColor(), h);
		
		info.addPara("You can end this agreement, but there "
				+ "would be no possibility of re-negotiating a similar one in the future.", opad);
	
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
			endImmediately();
			
			if (dialog != null) {
				sendUpdate(new Object(), dialog.getTextPanel());
			}

			if (type == AgreementEndingType.BROKEN) {
				SindrianDiktatHostileActivityFactor.setBrokeDeal(true);
				
				//Misc.incrUntrustwortyCount();
				
				TextPanelAPI text = dialog == null ? null : dialog.getTextPanel();
				Misc.adjustRep(Factions.DIKTAT, -REP_FOR_BREAKING_DEAL, text);
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
			prompt.addPara("The terms of the agreement are so onerous that your reputation at large "
					+ "would not suffer for unilaterally breaking it, "
					+ "though your standing with the Sindrian Diktat specifically "
					+ "would of course be affected.", 0f,
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
	
	public void reportEconomyMonthEnd() {
		
	}

	public void reportEconomyTick(int iterIndex) {
		int numIter = Global.getSettings().getInt("economyIterPerMonth");
		if (iterIndex != numIter - 1) return;
		
		float feeFraction = Global.getSettings().getFloat("diktatFuelFeeFraction");
		
		MonthlyReport report = SharedData.getData().getCurrentReport();
		FDNode marketsNode = report.getNode(MonthlyReport.OUTPOSTS);
		if (marketsNode == null) return;
		
		float exportIncome = computeFuelExportIncome(marketsNode);
		int credits = (int) (exportIncome * feeFraction);
		
		if (credits <= 0) return;

		FDNode node = getMonthlyReportNode();
		node.upkeep += credits;
	}
	
	public float computeFuelExportIncome(FDNode curr) {
		float total = 0f;
		if (curr.custom instanceof CommodityOnMarketAPI) {
			CommodityOnMarketAPI com = (CommodityOnMarketAPI) curr.custom;
			if (com.isFuel()) {
				total += curr.income;
			}
		}
		for (FDNode child : curr.getChildren().values()) {
			total += computeFuelExportIncome(child);
		}
		return total;
		
	}
	
	public FDNode getMonthlyReportNode() {
		MonthlyReport report = SharedData.getData().getCurrentReport();
		FDNode marketsNode = report.getNode(MonthlyReport.OUTPOSTS);
		if (marketsNode.name == null) {
			marketsNode.name = "Colonies";
			marketsNode.custom = MonthlyReport.OUTPOSTS;
			marketsNode.tooltipCreator = report.getMonthlyReportTooltip();
		}
		
		FDNode paymentNode = report.getNode(marketsNode, "diktat_fuel_fees"); 
		paymentNode.name = "Sindrian Diktat fuel accord fees";
		paymentNode.icon = faction.getCrest();
		
		if (paymentNode.tooltipCreator == null) {
			paymentNode.tooltipCreator = new TooltipCreator() {
				public boolean isTooltipExpandable(Object tooltipParam) {
					return false;
				}
				public float getTooltipWidth(Object tooltipParam) {
					return 450;
				}
				public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
					tooltip.addPara("Fees paid to the Sindrian Diktat in exchange for their refraining "
							+ "from interfering in your fuel production business. From orbit.", 0f);
				}
			};
		}
		
		return paymentNode;
	}
	
	
}






