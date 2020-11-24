package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.util.Misc;

public abstract class BaseGetCommodityBarEvent extends BaseBarEvent {
	public static final String OPTION_CONFIRM = "confirm";
	public static final String OPTION_CANCEL = "cancel";
	public static final String OPTION_CONTINUE = "continue";
	
	protected long seed;
	protected PersonAPI person;
	protected int quantity;
	protected int unitPrice;
	protected String commodity;
	
	public BaseGetCommodityBarEvent() {
		seed = Misc.random.nextLong();
	}
	
	protected transient Random random;
	
	protected MarketAPI market = null;
	
	protected void regen(MarketAPI market) {
		if (this.market == market) return;
		this.market = market;
		
		random = new Random(seed + market.getId().hashCode());
		
		commodity = getCommodityId();
		if (commodity == null) return;
		
		person = createPerson();
		quantity = computeQuantity();
		
		
		float price = market.getSupplyPrice(commodity, 1, true);
		unitPrice = (int) (price * getPriceMult());
		if (unitPrice > 50) {
			unitPrice = unitPrice / 10 * 10;
		}
		if (unitPrice < 1 && unitPrice > 0) {
			unitPrice = 1;
		}
	}
	
	protected PersonAPI createPerson() {
		PersonAPI person = Global.getSector().getFaction(getPersonFaction()).createRandomPerson(random);
		person.setRankId(getPersonRank());
		person.setPostId(getPersonRank());
		return person;
	}
	
	protected float getPriceMult() {
		return 0.75f;
	}
	protected String getCommodityId() {
		return commodity != null ? commodity : Commodities.FOOD;
	}
	protected int computeQuantity() {
		int quantity = 50 + 10 * random.nextInt(6);
		
		int size = market.getSize();
		//quantity *= BaseIndustry.getSizeMult(size);
		quantity *= Math.max(1, size - 2);
		return quantity;
	}
	protected String getPersonFaction() {
		return market.getFactionId();
	}
	protected String getPersonRank() {
		return Ranks.CITIZEN;
	}
	protected String getPersonPost() {
		return Ranks.CITIZEN;
	}
	
	protected String getManOrWoman() {
		String manOrWoman = "man";
		if (person.getGender() == Gender.FEMALE) manOrWoman = "woman";
		return manOrWoman;
	}
	
	protected String getHeOrShe() {
		String heOrShe = "he";
		if (person.getGender() == Gender.FEMALE) {
			heOrShe = "she";
		}
		return heOrShe;
	}
	
	protected String getHimOrHer() {
		String himOrHer = "him";
		if (person.getGender() == Gender.FEMALE) {
			himOrHer = "her";
		}
		return himOrHer;
	}
	
	protected String getHimOrHerself() {
		String himOrHer = "himself";
		if (person.getGender() == Gender.FEMALE) {
			himOrHer = "herself";
		}
		return himOrHer;
	}
	
	protected String getHisOrHer() {
		String hisOrHer = "his";
		if (person.getGender() == Gender.FEMALE) {
			hisOrHer = "her";
		}
		return hisOrHer;
	}

	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog) {
		super.addPromptAndOption(dialog);
		
		regen(dialog.getInteractionTarget().getMarket());
		
		TextPanelAPI text = dialog.getTextPanel();
		text.addPara(getPrompt());
		
		dialog.getOptionPanel().addOption(getOptionText(), this);
	}
	
	protected abstract String getPrompt();
	protected abstract String getOptionText();
	
	protected abstract String getConfirmText();
	protected abstract String getCancelText();
	
	protected abstract String getMainText();
	protected String [] getMainTextTokens() { return null; };
	protected Color [] getMainTextColors() { return null; };
	
	protected String getMainText2() { return null; };
	protected String [] getMainText2Tokens() { return null; };
	protected Color [] getMainText2Colors() { return null; };
	
	protected String getAcceptText() { return null; };
	protected String [] getAcceptTextTokens() { return null; };
	protected Color [] getAcceptTextColors() { return null; };
	
	protected String getDeclineText() { return null; };
	protected String [] getDeclineTextTokens() { return null; };
	protected Color [] getDeclineTextColors() { return null; };

	@Override
	public void init(InteractionDialogAPI dialog) {
		super.init(dialog);
		
		done = false;
		
		dialog.getVisualPanel().showPersonInfo(person, true);
		
		TextPanelAPI text = dialog.getTextPanel();
		if (getMainTextTokens() != null) {
			LabelAPI main = text.addPara(getMainText(), Misc.getHighlightColor(), getMainTextTokens());
			main.setHighlightColors(getMainTextColors());
			main.setHighlight(getMainTextTokens());
		} else {
			text.addPara(getMainText());
		}
		
		if (getMainText2() == null) {
			showTotalAndOptions();
		} else {
			OptionPanelAPI options = dialog.getOptionPanel();
			options.clearOptions();
			options.addOption("Continue", OPTION_CONTINUE);
		}
	}
	
	protected boolean showCargoCap() {
		return true;
	}
	
	protected void showTotalAndOptions() {
		Color h = Misc.getHighlightColor();
		Color n = Misc.getNegativeHighlightColor();

		TextPanelAPI text = dialog.getTextPanel();
		
		boolean canAccept = canAccept();
		
		if (showCargoCap() && commodity != null && quantity > 0) {
			CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(commodity);
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			String str = "";
			int cap = 0;
			if (spec.isFuel()) {
				cap = cargo.getFreeFuelSpace();
				if (cap > 1) {
					str += "Your fleet's fuel tanks can hold an additional %s units of fuel.";
				} else {
					str += "Your fleet's fuel tanks are currently full.";
				}
			} else if (spec.isPersonnel()) {
				cap = cargo.getFreeCrewSpace();
				if (cap > 1) {
					str += "Your fleet's crew quarters can accommodate an additional %s personnel.";
				} else {
					str += "Your fleet's crew berths are currently full.";
				}
			} else {
				cap = (int) cargo.getSpaceLeft();
				if (cap > 1) {
					str += "Your fleet's holds can accommodate an additional %s units of cargo.";
				} else {
					str += "Your fleet's cargo holds are currently full.";
				}
			}
			text.addPara(str, h, Misc.getWithDGS(cap));
		}
		
		float credits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
		int price = unitPrice * quantity;
		if (price > 0) {
			LabelAPI label = text.addPara("The total price is %s. You have %s available.",
							h,
							Misc.getDGSCredits(price),	
							Misc.getDGSCredits(credits));
			label.setHighlightColors(canAccept ? h : n, h);
			label.setHighlight(Misc.getDGSCredits(price), Misc.getDGSCredits(credits));
		}
		
		
		OptionPanelAPI options = dialog.getOptionPanel();
		options.clearOptions();
		options.addOption(getConfirmText(), OPTION_CONFIRM);
		if (!canAccept) {
			options.setEnabled(OPTION_CONFIRM, false);
			String tooltip = getCanNotAcceptTooltip();
			if (tooltip != null) {
				options.setTooltip(OPTION_CONFIRM, tooltip);
			}
		}
		options.addOption(getCancelText(), OPTION_CANCEL);
		//options.setShortcut(OPTION_CANCEL, Keyboard.KEY_ESCAPE, false, false, false, true);
	}
	
	protected boolean canAccept() {
		float credits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
		int price = unitPrice * quantity;
		boolean canAfford = credits >= price;
		return canAfford;
	}
	
	protected String getCanNotAcceptTooltip() {
		return "You don't have enough credits.";
	}
	

	
	protected void doExtraConfirmActions() {
		
	}
	
	
	protected void doConfirmActionsPreAcceptText() {
		
	}
	protected void doStandardConfirmActions() {
		int price = unitPrice * quantity;
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		if (price > 0) cargo.getCredits().subtract(price);
		cargo.addCommodity(commodity, quantity);
		
		TextPanelAPI text = dialog.getTextPanel();
		
		if (price > 0) AddRemoveCommodity.addCreditsLossText(price, text);
		AddRemoveCommodity.addCommodityGainText(commodity, quantity, text);
	}
	
	@Override
	public void optionSelected(String optionText, Object optionData) {
		if (optionData == OPTION_CONTINUE) {
			TextPanelAPI text = dialog.getTextPanel();
			if (getMainText2Tokens() != null) {
				LabelAPI main = text.addPara(getMainText2(), Misc.getHighlightColor(), getMainText2Tokens());
				main.setHighlightColors(getMainText2Colors());
				main.setHighlight(getMainText2Tokens());
			} else {
				text.addPara(getMainText2());
			}
			showTotalAndOptions();
		} else if (optionData == OPTION_CONFIRM) {
			done = true;
			BarEventManager.getInstance().notifyWasInteractedWith(this);
			
			doConfirmActionsPreAcceptText();
			TextPanelAPI text = dialog.getTextPanel();
			String acceptStr = getAcceptText();
			if (acceptStr != null) {
				if (getAcceptTextTokens() != null) {
					LabelAPI accept = text.addPara(acceptStr, Misc.getHighlightColor(), getAcceptTextTokens());
					accept.setHighlightColors(getAcceptTextColors());
					accept.setHighlight(getAcceptTextTokens());
				} else {
					text.addPara(acceptStr);
				}
			}
			
			doStandardConfirmActions();
			doExtraConfirmActions();
			
		} else if (optionData == OPTION_CANCEL) {
			
			TextPanelAPI text = dialog.getTextPanel();
			String declineStr = getDeclineText();
			if (declineStr != null) {
				if (getDeclineTextTokens() != null) {
					LabelAPI decline = text.addPara(declineStr, Misc.getHighlightColor(), getAcceptTextTokens());
					decline.setHighlightColors(getDeclineTextColors());
					decline.setHighlight(getDeclineTextTokens());
				} else {
					text.addPara(declineStr);
				}
			} else {
				noContinue = true;
			}
			done = true;
		}
	}

	@Override
	public boolean isDialogFinished() {
		return done;
	}

	public PersonAPI getPerson() {
		return person;
	}

	public MarketAPI getMarket() {
		return market;
	}

}



