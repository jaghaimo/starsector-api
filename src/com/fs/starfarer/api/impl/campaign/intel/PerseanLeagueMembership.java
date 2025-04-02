package com.fs.starfarer.api.impl.campaign.intel;

import java.util.Set;

import java.awt.Color;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.CommissionEndedListener;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.econ.EstablishedPolity;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.EstablishedPolityScript;
import com.fs.starfarer.api.impl.campaign.rulecmd.HA_CMD;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class PerseanLeagueMembership extends BaseIntelPlugin implements EconomyTickListener, CommissionEndedListener {
	
	public static enum AgreementEndingType {
		EXPIRED,
		BROKEN,
		ENDED,
	}
	
	public static int TIMES_LEFT_LEAGUE_FOR_NO_REJOIN = 2;
	
	// in $player memory
	public static final String PL_MEMBERSHIP_GOOD_DEAL = "$hasGoodPLMembershipDeal";
	public static final String PAYING_HOUSE_HANNAN = "$payingHouseHannan";
	public static final String LEFT_LEAGUE_WHEN_GOOD_DEAL = "$leftLeagueWhenGoodDeal";
	public static final String IS_LEAGUE_MEMBER = "$isLeagueMember";
	public static final String NUM_TIMES_LEFT_LEAGUE = "$numTimesLeftLeague";
	
	public static final String DEFEATED_BLOCKADE = "$defeatedLeagueBlockade";
	public static final String DEFEATED_PUN_EX = "$defeatedLeaguePunEx";
	
	public static int getNumTimesLeftLeague() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getInt(NUM_TIMES_LEFT_LEAGUE);
	}
	public static void incrLeftLeagueCount() {
		int count = getNumTimesLeftLeague();
		Global.getSector().getPlayerMemoryWithoutUpdate().set(NUM_TIMES_LEFT_LEAGUE, count + 1);
	}
	
	public static boolean isDefeatedBlockadeOrPunEx() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(DEFEATED_BLOCKADE) ||
				Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(DEFEATED_PUN_EX);
	}
	public static void setDefeatedBlockade(boolean value) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(DEFEATED_BLOCKADE, value);
		if (!value) {
			Global.getSector().getPlayerMemoryWithoutUpdate().unset(DEFEATED_BLOCKADE);
		}
	}
	public static void setDefeatedPunEx(boolean value) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(DEFEATED_PUN_EX, value);
		if (!value) {
			Global.getSector().getPlayerMemoryWithoutUpdate().unset(DEFEATED_PUN_EX);
		}
	}
	public static boolean isGoodDeal() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(PL_MEMBERSHIP_GOOD_DEAL);
	}

	public static boolean isPayingHouseHannan() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(PAYING_HOUSE_HANNAN);
	}
	public static void setPayingHouseHannan(boolean value) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(PAYING_HOUSE_HANNAN, value);
		if (!value) {
			Global.getSector().getPlayerMemoryWithoutUpdate().unset(PAYING_HOUSE_HANNAN);
		}
	}
	public static boolean isLeftLeagueWhenGoodDeal() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(LEFT_LEAGUE_WHEN_GOOD_DEAL);
	}
	public static void setLeftLeagueWhenGoodDeal(boolean value) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(LEFT_LEAGUE_WHEN_GOOD_DEAL, value);
		if (!value) {
			Global.getSector().getPlayerMemoryWithoutUpdate().unset(LEFT_LEAGUE_WHEN_GOOD_DEAL);
		}
	}
	public static boolean isLeagueMember() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(IS_LEAGUE_MEMBER);
	}
	public static void setLeagueMember(boolean member) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(IS_LEAGUE_MEMBER, member);
	}
	
	
	public static String KEY = "$plMembership_ref";
	public static PerseanLeagueMembership get() {
		return (PerseanLeagueMembership) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	public static String BUTTON_END = "End";
	public static String BUTTON_RENEGE_HANNAN = "Renege Hannan";
	
	public static String UPDATE_PARAM_ACCEPTED = "update_param_accepted";
	
	
	protected FactionAPI faction = null;
	protected AgreementEndingType endType = null;
	
	public PerseanLeagueMembership(InteractionDialogAPI dialog) {
		this.faction = Global.getSector().getFaction(Factions.PERSEAN);
		
		setImportant(true);
		setLeagueMember(true);
		new EstablishedPolityScript();
		
		Global.getSector().getPlayerFaction().setSecondaryColorOverride(getFaction().getBaseUIColor());
		Global.getSector().getPlayerFaction().setSecondaryColorSegmentsOverride(8);
		
		TextPanelAPI text = null;
		if (dialog != null) text = dialog.getTextPanel();
		
		Global.getSector().getListenerManager().addListener(this);
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		Global.getSector().getIntelManager().addIntel(this, true);
		
		RepLevel level = faction.getRelToPlayer().getLevel();
		if (!level.isAtWorst(RepLevel.NEUTRAL)) {
			Misc.adjustRep(Factions.PERSEAN, 2f, RepLevel.NEUTRAL, text);
		}
		
		sendUpdate(UPDATE_PARAM_ACCEPTED, text);
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		setLeagueMember(false);
		setLeftLeagueWhenGoodDeal(isGoodDeal());
		//setPayingHouseHannan(false);
		stopPayingHouseHannan(false, null);
		
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
		
		Global.getSector().getListenerManager().removeListener(this);
	}
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
	}

	protected Object readResolve() {
		return this;
	}
	
	public String getBaseName() {
		return "Persean League Membership";
	}

	public String getAcceptedPostfix() {
		return "Accepted";
	}
		
	public String getBrokenPostfix() {
		return "Annulled";

	}
	public String getEndedPostfix() {
		return "Ended";
	}
	
	public String getExpiredPostfix() {
		return "Expired";
	}
	
	public String getName() {
		String postfix = "";
		if (isEnding() && endType != null) {
			switch (endType) {
			case BROKEN:
				postfix = " - " + getBrokenPostfix();
				break;
			case ENDED:
				postfix = " - " + getEndedPostfix();
				break;
			case EXPIRED:
				postfix = " - " + getExpiredPostfix();
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

		int perMonth = computeCreditsPerMonth();
		if (perMonth != 0 && !isEnded() && !isEnding()) {
			Color c = perMonth > 0 ? h : Misc.getNegativeHighlightColor();
			info.addPara("%s per month", initPad, tc, c, Misc.getDGSCredits(perMonth));
		}
	
		unindent(info);
	}
	
//	@Override
//	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
//		Color h = Misc.getHighlightColor();
//		Color g = Misc.getGrayColor();
//		Color c = getTitleColor(mode);
//		float pad = 3f;
//		float opad = 10f;
//		
//		info.addPara(getName(), c, 0f);
//		
//		addBulletPoints(info, mode);
//	}
	
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		Color c = faction.getBaseUIColor();
		
		info.addImage(getFaction().getLogo(), width, 128, opad);
		
		if (isEnding() || isEnded()) {
			info.addPara("Your agreement with the League is no longer in force.", opad, c, "League");
			return;
		}
		
		if (isGoodDeal()) {
			info.addPara("You've negotiated a good membership deal from a position of strength. "
					+ "The League will support you politically, which "
					+ "for example makes it untenable for the Hegemony to insist on AI inspections "
					+ "in your space.", opad, c, "League");
			
			if (isPayingHouseHannan()) {
				info.addPara("Part of your arrangement involves unofficial payments directly to the accounts "
						+ "of House Hannan, as payment for facilitating this agreement.", opad);
				addBulletPoints(info, ListInfoMode.IN_DESC);
				ButtonAPI button = info.addButton("Renege on the payments", BUTTON_RENEGE_HANNAN, 
						getFactionForUIColors().getBaseUIColor(), getFactionForUIColors().getDarkUIColor(),
						(int)(width), 20f, opad * 1f);
				button.setShortcut(Keyboard.KEY_G, true);
				info.addSpacer(opad);
			}
		} else {
			info.addPara("You've joined the Persean League, though it's whispered that you only did so to avoid "
					+ "harassment, and your membership dues are nothing more than protection payments. "
					+ "However, the League still supports you politically, which "
					+ "for example makes it untenable for the Hegemony to insist on AI inspections "
					+ "in your space.", opad, c, "Persean League");
		}
		
		info.addPara("Due to being new members of the League, your colonies enjoy an "
				+ "increased flow of trade, resulting in a %s accessibility bonus.",
				opad, h,
				"+" + (int)Math.round(EstablishedPolity.ACCESSIBILITY_BONUS * 100f) + "%");
		
		info.addPara("Your League membership is contingent on maintaining an active Persean League commission. If "
				+ "you resign it, or if it is annulled for any reason, your membership will end as well.", opad);
		
		if (computeCreditsPerMonth() != 0 && !isPayingHouseHannan()) {
			info.addPara("You are paying membership dues to the League.", opad);
			addBulletPoints(info, ListInfoMode.IN_DESC);
		}
		
			
		ButtonAPI button = info.addButton("End League membership", BUTTON_END, 
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

	
	public void reportEconomyMonthEnd() {

	}

	public void reportEconomyTick(int iterIndex) {
		
		int credits = computeCreditsPerTick();

		if (credits != 0) {
			FDNode node = getMonthlyReportNode();
			if (credits > 0) {
				node.income += credits;
			} else if (credits < 0) {
				node.upkeep -= credits;
			}
		}
	}
	
	public FDNode getMonthlyReportNode() {
		MonthlyReport report = SharedData.getData().getCurrentReport();
		FDNode marketsNode = report.getNode(MonthlyReport.OUTPOSTS);
		if (marketsNode.name == null) {
			marketsNode.name = "Colonies";
			marketsNode.custom = MonthlyReport.OUTPOSTS;
			marketsNode.tooltipCreator = report.getMonthlyReportTooltip();
		}
		
		FDNode paymentNode = report.getNode(marketsNode, "persean_league_membership"); 
		paymentNode.name = "Persean League membership dues";
		//paymentNode.upkeep += payment;
		//paymentNode.icon = Global.getSettings().getSpriteName("income_report", "generic_expense");
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
					tooltip.addPara("Monthly dues for keeping your Persean League membership in good standing.", 0f);
				}
			};
		}
		
		return paymentNode;
	}

	public int computeCreditsPerMonth() {
		int perTick = computeCreditsPerTick();
		float numIter = Global.getSettings().getFloat("economyIterPerMonth");
		return (int) (perTick * numIter);
	}
	
	public static int computeCreditsPerTick() {
		float numIter = Global.getSettings().getFloat("economyIterPerMonth");
		float f = 1f / numIter;
		
		int payment = 0;
		float feeFraction = Global.getSettings().getFloat("perseanLeagueFeeFraction");
		if (isGoodDeal()) {
			feeFraction = 0f;
		}
		if (isPayingHouseHannan()) {
			feeFraction += Global.getSettings().getFloat("houseHannanFeeFraction");
		}
		
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			payment += (int) (market.getGrossIncome() * f) * feeFraction;
		}
		
		return -payment;
	}

	public FactionAPI getFaction() {
		return faction;
	}
	
	public void endMembership(AgreementEndingType type, InteractionDialogAPI dialog) {
		if (!isEnded() && !isEnding()) {
			boolean baseMembership = !PerseanLeagueMembership.isGoodDeal();
			
			endType = type;
			setImportant(false);
			//endAfterDelay();
			endImmediately();
			
			incrLeftLeagueCount();
			
			Global.getSector().getPlayerFaction().setSecondaryColorOverride(null);
			Global.getSector().getPlayerFaction().setSecondaryColorSegmentsOverride(0);
			
			if (dialog != null) {
				sendUpdate(new Object(), dialog.getTextPanel());
			}
			
			if (baseMembership) {
				HA_CMD.sendPerseanLeaguePunitiveExpedition(dialog);
			}
			
		}
	}
	
	
	public static void stopPayingHouseHannan(boolean trustBroken, InteractionDialogAPI dialog) {
		if (isPayingHouseHannan()) {
			TextPanelAPI text = null;
			if (dialog != null) text = dialog.getTextPanel();
			PersonAPI reynard = People.getPerson(People.REYNARD_HANNAN);
			if (reynard != null) {
				Misc.adjustRep(reynard, -0.5f, text);
				ContactIntel.removeContact(reynard, dialog);
			}
			setPayingHouseHannan(false);
			
			new GensHannanMachinations(dialog);
			if (trustBroken) {
				Misc.incrUntrustwortyCount();
			}
		}
	}
	
	@Override
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_END) {
			endMembership(AgreementEndingType.ENDED, null);
		} else if (buttonId == BUTTON_RENEGE_HANNAN) {
			stopPayingHouseHannan(true, null);
		}
		super.buttonPressConfirmed(buttonId, ui);
	}


	@Override
	public void createConfirmationPrompt(Object buttonId, TooltipMakerAPI prompt) {
		FactionAPI faction = getFactionForUIColors();
		
		if (buttonId == BUTTON_END) {
			if (isGoodDeal()) {
				String extra = "";
				LabelAPI label = prompt.addPara("You've negotiated a good membership deal from a position of strength. If "
						+ "you leave, you will not be allowed to rejoin, but the " +
						faction.getDisplayNameWithArticle() + " will likely refrain "
						+ "from harassing your interests." + extra, 0f,
						Misc.getTextColor(), faction.getBaseUIColor(), 
						faction.getDisplayNameWithArticleWithoutArticle());
					label.setHighlightColors(Misc.getNegativeHighlightColor(), faction.getBaseUIColor());
					label.setHighlight("will not be allowed to rejoin", faction.getDisplayNameWithArticleWithoutArticle());
				if (isPayingHouseHannan()) {
					prompt.addPara("However, Gens Hannan will not be pleased with their payments being stopped.",
							10f, Misc.getNegativeHighlightColor(), "Gens Hannan will not be pleased");
				}
			} else {
				String extra = "";
				boolean canRejoin = getNumTimesLeftLeague() < TIMES_LEFT_LEAGUE_FOR_NO_REJOIN - 1;
				float rejoinPad = 10f;
				if (HA_CMD.canSendPerseanLeaguePunitiveExpedition()) {
					LabelAPI label = prompt.addPara(
							"If you leave, " + faction.getDisplayNameWithArticle() + " will likely take"
							+ " drastic action against your colonies." + extra, 0f);
					label.setHighlightColors(faction.getBaseUIColor(), Misc.getNegativeHighlightColor());
					label.setHighlight(faction.getDisplayNameWithArticleWithoutArticle(), "drastic action");
				} else {
					if (canRejoin) {
						prompt.addPara("If you leave, " + faction.getDisplayNameWithArticle() + " will likely start "
								+ " harassing your interests in the near future.", 0f,
								Misc.getTextColor(), faction.getBaseUIColor(), 
								faction.getDisplayNameWithArticleWithoutArticle());
					} else {
						rejoinPad = 0f;
					}
				}
				if (!canRejoin) {
					prompt.addPara("Given your history, it is likely that you would not be allowed "
							+ "to rejoin the League at any point in the future.",
							rejoinPad, Misc.getNegativeHighlightColor(), 
							"would not be allowed to rejoin");
				}
			}
		} else if (buttonId == BUTTON_RENEGE_HANNAN) {
			prompt.addPara("Gens Hannan will not be pleased. While they are unlikely to be unable to obtain your outright "
					+ "explulsion from the League, their influence is significant and will likely be used "
					+ "to continually undermine your standing. ", 0f, 
					Misc.getNegativeHighlightColor(), 
					"continually undermine your standing");
			
			prompt.addPara("You will also lose Reynard Hannan as a contact.", 10f, 
					Misc.getNegativeHighlightColor(), 
					"lose Reynard Hannan");
		}
	}
	
	@Override
	public boolean doesButtonHaveConfirmDialog(Object buttonId) {
		if (buttonId == BUTTON_END
				|| buttonId == BUTTON_RENEGE_HANNAN) {
			return true;
		}
		return super.doesButtonHaveConfirmDialog(buttonId);
	}
	
	
	public void reportCommissionEnded(FactionCommissionIntel intel) {
		if (intel.getFaction().getId().equals(Factions.PERSEAN)) {
			endMembership(AgreementEndingType.BROKEN, null);
		}
		
	}
}






