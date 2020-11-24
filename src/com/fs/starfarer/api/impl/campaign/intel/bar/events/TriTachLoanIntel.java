package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager.GenericBarEventCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class TriTachLoanIntel extends BaseIntelPlugin {
	public static final String NUM_REPAID_LOANS = "$ttli_numRepaidLoans";
	
	protected TriTachLoanBarEvent event;
	protected MarketAPI market;

	protected ReputationAdjustmentResult repResult;
	protected float daysRemaining = 0f;
	protected boolean wasExtended = false;
	
	protected boolean sentReminder = false;
	protected boolean loanRepaid = false;
	
	protected boolean majorLoan = false;
	
	public TriTachLoanIntel(TriTachLoanBarEvent event, MarketAPI market) {
		this.event = event;
		this.market = market;
		
		daysRemaining = event.getRepaymentDays();
		Global.getSector().addScript(this);
		
		PersonAPI person = getPerson();
		market.getCommDirectory().addPerson(person);
		market.addPerson(person);
		
		person.getMemoryWithoutUpdate().set("$ttli_isPlayerContact", true);
		person.getMemoryWithoutUpdate().set("$ttli_eventRef", this);
		Misc.setFlagWithReason(person.getMemoryWithoutUpdate(),
							  MemFlags.MEMORY_KEY_MISSION_IMPORTANT,
							  "ttli", true, -1);
	}
	
	public TriTachLoanBarEvent getEvent() {
		return event;
	}



	public boolean isMajorLoan() {
		return majorLoan;
	}

	public void setMajorLoan(boolean majorLoan) {
		this.majorLoan = majorLoan;
		
		if (majorLoan) {
			// do not unset this later, needed to make market interaction after failing to pay work
			getPerson().getMemoryWithoutUpdate().set("$ttli_isMajorLoan", true);
		}
	}



	public PersonAPI getPerson() {
		return event.getPerson();
	}
	
	protected float getExtensionDays() {
		return event.getRepaymentDays();
	}

	@Override
	public boolean callEvent(String ruleId, final InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
		//$ttli_repaymentAmount
		//$ttli_loanWasExtended
		//$ttli_extensionDays 
		
		MemoryAPI memory = getPerson().getMemoryWithoutUpdate();
		if (action.equals("putValuesInMemory")) {
			memory.set("$ttli_repaymentAmount", Misc.getDGSCredits(event.getRepaymentAmount()), 0);
			memory.set("$ttli_loanWasExtended", wasExtended, 0);
			memory.set("$ttli_daysRemaining", daysRemaining, 0);
			if (wasExtended) {
				memory.set("$ttli_extensionDays", "" + (int) getExtensionDays(), 0);
			}
		} else if (action.equals("canPay")) {
			return cargo.getCredits().get() >= event.getRepaymentAmount();
		} else if (action.equals("payLoan")) {
			endWithPayment(dialog);
		} else if (action.equals("extendLoan")) {
			extendLoan(dialog);
		} else if (action.equals("applyExtendLoanRepLoss")) {
			applyExtendLoanRepLoss(dialog);
		} else if (action.equals("notPaying")) {
			endNoPayment(dialog);
		} else if (action.equals("noPaymentMessage")) {
			noPaymentMessage(dialog);
		} else if (action.equals("isMajorLoan")) {
			return isMajorLoan();
		}
		
		return true;
	}
	
	protected void noPaymentMessage(InteractionDialogAPI dialog) {
		dialog.getInteractionTarget().setActivePerson(getPerson());
		((RuleBasedDialog) dialog.getPlugin()).notifyActivePersonChanged();
		dialog.getVisualPanel().showPersonInfo(getPerson(), true);
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		PersonAPI person = getPerson();
		market.getCommDirectory().removePerson(person);
		market.removePerson(person);
		person.getMemoryWithoutUpdate().unset("$ttli_isPlayerContact");
		person.getMemoryWithoutUpdate().unset("$ttli_eventRef");
		Misc.setFlagWithReason(person.getMemoryWithoutUpdate(),
							  MemFlags.MEMORY_KEY_MISSION_IMPORTANT,
							  "ttli", false, -1);
	}

	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		Global.getSector().removeScript(this);
	}
	
	protected void extendLoan(InteractionDialogAPI dialog) {
		wasExtended = true;
		
		float extension = event.getRepaymentDays();
		daysRemaining += extension;
		event.setRepaymentAmount((int) (event.getLoanAmount() * 2f));
		
		GenericBarEventCreator creator = null;
		for (GenericBarEventCreator c : BarEventManager.getInstance().getCreators()) {
			if (isMajorLoan() && c instanceof TriTachMajorLoanBarEventCreator) {
				creator = c;
				break;
			} else if (!isMajorLoan() && c instanceof TriTachLoanBarEventCreator) {
				creator = c;
				break;
			}
		}
		
		if (creator != null) {
			BarEventManager.getInstance().getTimeout().add(creator, extension);
		}
	}
	
	protected void applyExtendLoanRepLoss(InteractionDialogAPI dialog) {
		CustomRepImpact impact = new CustomRepImpact();
		impact.delta = -0.05f;
		if (isMajorLoan()) impact.delta = -0.1f;
		impact.limit = RepLevel.SUSPICIOUS;
		repResult = Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.CUSTOM, 
						impact, null, dialog != null ? dialog.getTextPanel() : null, true, true),
						event.getPerson());
		
		impact = new CustomRepImpact();
		impact.delta = -0.02f;
		if (isMajorLoan()) impact.delta = -0.05f;
		impact.limit = RepLevel.SUSPICIOUS;
		repResult = Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.CUSTOM, 
						impact, null, dialog != null ? dialog.getTextPanel() : null, true, true),
						event.getPerson().getFaction().getId());
	}
	
	
	protected void endWithPayment(InteractionDialogAPI dialog) {
		endAfterDelay();
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
		cargo.getCredits().subtract(event.getRepaymentAmount());
		if (dialog != null) {
			TextPanelAPI text = dialog.getTextPanel();
			AddRemoveCommodity.addCreditsLossText(event.getRepaymentAmount(), text);
		}
		
		CustomRepImpact impact = new CustomRepImpact();
		impact.delta = 0.05f;
		if (isMajorLoan()) impact.delta = 0.1f;
		repResult = Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.CUSTOM, 
						impact, null, dialog != null ? dialog.getTextPanel() : null, true, true),
						event.getPerson());
		
		impact = new CustomRepImpact();
		impact.delta = 0.02f;
		if (isMajorLoan()) impact.delta = 0.05f;
		repResult = Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.CUSTOM, 
						impact, null, dialog != null ? dialog.getTextPanel() : null, true, true),
						event.getPerson().getFaction().getId());
		
		
		float repaid = Global.getSector().getMemoryWithoutUpdate().getFloat(NUM_REPAID_LOANS);
		repaid++;
		Global.getSector().getMemoryWithoutUpdate().set(NUM_REPAID_LOANS, repaid);
		
		loanRepaid = true;
	}
	
	
	protected void endNoPayment(InteractionDialogAPI dialog) {
		endAfterDelay();
		
		CustomRepImpact impact = new CustomRepImpact();
		impact.delta = -0f;
		impact.ensureAtBest = RepLevel.HOSTILE;
		if (isMajorLoan()) impact.ensureAtBest = RepLevel.VENGEFUL;
		repResult = Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.CUSTOM, 
						impact, null, dialog != null ? dialog.getTextPanel() : null, dialog != null, dialog != null),
						event.getPerson().getFaction().getId());
		
		if (dialog == null) {
			Global.getSector().getMemoryWithoutUpdate().set("$ttli_unpaidEventRef", this, 60f);
		}
		
		if (majorLoan) {
			Global.getSector().addScript(new TriTachLoanIncentiveScript(this));
		}
		
	}

	@Override
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);
		
		float days = Misc.getDays(amount);
		daysRemaining -= days;
		
		//daysRemaining = 0;
		
		if (daysRemaining <= 0) {
			endNoPayment(null);
			sendUpdateIfPlayerHasIntel(new Object(), false);
			return;
		}
		
		if (!sentReminder) {
			float dist = Misc.getDistance(Global.getSector().getPlayerFleet().getLocationInHyperspace(), market.getLocationInHyperspace());
			float soonDays = dist / 1500 + 10f;
			if (soonDays > daysRemaining) {
				sentReminder = true;
				sendUpdateIfPlayerHasIntel(new Object(), false);
				return;
			}
		}
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
		
		if (!loanRepaid) {
			if (daysRemaining > 0) {
				if (mode == ListInfoMode.IN_DESC) {
					info.addPara("%s original loan amount", initPad, tc, h, Misc.getDGSCredits(event.getLoanAmount()));
					initPad = 0f;
				}
				info.addPara("%s owed", initPad, tc, h, Misc.getDGSCredits(event.getRepaymentAmount()));
				initPad = 0f;
				addDays(info, "left to repay", daysRemaining, tc);
			}
		}
		
		if (repResult != null) {
			CoreReputationPlugin.addAdjustmentMessage(repResult.delta, event.getPerson().getFaction(), null, 
					null, null, info, tc, isUpdate, initPad);
		}
		
		unindent(info);
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		
		addBulletPoints(info, mode);
	}
	
	public String getSortString() {
		return "Loan";
	}
	
	public String getName() {
		if (loanRepaid) {
			return "Loan Repaid";
		} else if (daysRemaining <= 0) {
			return "Loan Repayment - Failed";
		} else if (sentReminder) {
			return "Loan Repayment - Due Soon";
		}
		return "Loan Repayment";
	}
	

	@Override
	public FactionAPI getFactionForUIColors() {
		return event.getPerson().getFaction();
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}
	

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		PersonAPI p = event.getPerson();
		FactionAPI faction = getFactionForUIColors();
		info.addImages(width, 128, opad, opad, p.getPortraitSprite(), faction.getCrest());

		if (loanRepaid) {
			info.addPara("You've repaid the loan from " + p.getNameString() + " on time.", opad);
		} else if (daysRemaining > 0) {
			info.addPara("You've accepted a loan from " + p.getNameString() + " and must repay it on time, " +
					"or your reputation with " + faction.getDisplayNameWithArticle() + " will be ruined.", opad,
					faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
		} else {
			info.addPara("You've failed to repay the loan from " + p.getNameString() + " on time.", opad);
		}
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
		
		if (daysRemaining > 0 && !loanRepaid) {
			info.addPara("You should be able to find " + p.getNameString() + " at " + market.getName() + ".", opad);
		}
	}
	
	public String getIcon() {
		return event.getPerson().getPortraitSprite();
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_ACCEPTED);
		tags.add(getFactionForUIColors().getId());
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return market.getPrimaryEntity();
	}
	
}	
	

