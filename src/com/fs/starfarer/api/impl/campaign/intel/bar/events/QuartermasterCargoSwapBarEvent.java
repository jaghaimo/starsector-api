package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class QuartermasterCargoSwapBarEvent extends BaseGetCommodityBarEvent {
	
	public QuartermasterCargoSwapBarEvent() {
		super();
	}
	
	public boolean shouldShowAtMarket(MarketAPI market) {
		if (!super.shouldShowAtMarket(market)) return false;
		regen(market);
		if (commodity == null) return false;
		
		if (!market.getFactionId().equals(Factions.HEGEMONY)) {
			return false;
		}
		boolean hasMilitaryBase = false;
		for (Industry ind : market.getIndustries()) {
			if (ind.getSpec().hasTag(Industries.TAG_MILITARY) || ind.getSpec().hasTag(Industries.TAG_COMMAND)) {
				hasMilitaryBase = true;
				break;
			}
		}
		if (!hasMilitaryBase) return false;
		
		
		return true;
	}

	protected int playerGiveQuantity = 0;
	protected String playerGiveCommodity = null;
	@Override
	protected String getCommodityId() {
		String[] possible = new String[] {
				Commodities.HAND_WEAPONS,
				Commodities.HEAVY_MACHINERY,
				Commodities.SUPPLIES,
				Commodities.FUEL,
				Commodities.RARE_METALS,
				Commodities.METALS,
		};
		
		List <String> playerHas = new ArrayList<String>(); 
		List <String> playerNotHas = new ArrayList<String>(); 
		
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();

		
		float value = getValue();
		for (String c : possible) {
			int q = (int) cargo.getQuantity(CargoItemType.RESOURCES, c);
			CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(c);
			int num = (int) (value / spec.getBasePrice());
			if (q >= num) {
				playerHas.add(c);
			} else if (q < num) {
				playerNotHas.add(c);
			}
		}
		
		if (playerHas.isEmpty() || playerNotHas.isEmpty()) return null;
		
		WeightedRandomPicker<String> take = new WeightedRandomPicker<String>(random);
		take.addAll(playerNotHas);
		
		WeightedRandomPicker<String> give = new WeightedRandomPicker<String>(random);
		give.addAll(playerHas);
		
		String pick = take.pick();
		playerGiveCommodity = give.pick();
		
		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(playerGiveCommodity);
		int num = (int) (value / spec.getBasePrice());
		playerGiveQuantity = (int) (num * 0.33f);
		
		if (playerGiveQuantity <= 0) return null;
		
		return pick;
	}
	
	@Override
	protected PersonAPI createPerson() {
		for (PersonAPI person : market.getPeopleCopy()) {
			if (Ranks.POST_SUPPLY_OFFICER.equals(person.getPostId())) {
				adjustPerson(person);
				return person;
			}
		}

		PersonAPI person = Global.getSector().getFaction(getPersonFaction()).createRandomPerson(random);
		person.setRankId(getPersonRank());
		person.setPostId(getPersonRank());
		adjustPerson(person);
		return person;
	}
	
	@Override
	protected void adjustPerson(PersonAPI person) {
		super.adjustPerson(person);
		person.setImportanceAndVoice(PersonImportance.MEDIUM, random);
		person.addTag(Tags.CONTACT_MILITARY);
	}

	@Override
	protected String getPersonFaction() {
		return Factions.HEGEMONY;
	}
	
	@Override
	protected String getPersonRank() {
		return Ranks.SPACE_COMMANDER;
	}
	
	@Override
	protected String getPersonPost() {
		return Ranks.POST_SUPPLY_OFFICER;
	}
	
	protected float getValue() {
		float value = (1000 + 100 * random.nextInt(6)) * BaseIndustry.getSizeMult(market.getSize());
		return value;
	}
	
	@Override
	protected int computeQuantity() {
		String c = commodity;
		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(c);
		int num = (int) (getValue() / spec.getBasePrice());
		return num;
	}
	
	@Override
	protected float getPriceMult() {
		return 0;
	}
	
	@Override
	protected String getPrompt() {
		return "None other than the station's quartermaster, who looks rather sullen, is sitting at the bar.";
	}
	
	@Override
	protected String getOptionText() {
		return "Approach the quartermaster and offer to buy " + getHimOrHer() + " a drink";
	}
	
	@Override
	protected String getMainText() {
		CommoditySpecAPI take = Global.getSettings().getCommoditySpec(commodity);
		CommoditySpecAPI give = Global.getSettings().getCommoditySpec(playerGiveCommodity);
		
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		int qty = (int) cargo.getQuantity(CargoItemType.RESOURCES, give.getId());
		String units = "units";
		if (qty == 1) units = "unit";
		
		return "In venerated tradition going back thousands of cycles, the quartermaster " +
				"vents to you about how " + getHisOrHer() + " provision request got mixed up and " +
				getHeOrShe() + " was shipped %s units of " + take.getLowerCaseName() + " instead of %s units of " +
				give.getLowerCaseName() + ". \"A mining drone with half its Ludd-damned " +
				"computer fried on rads would do a better job than the idiots in Fleet Supply\" " +
				getHeOrShe() + " growls, knocking back the rest of " + getHisOrHer() + " drink and " +
				"slamming the glass down.\n\n" +
				"You have %s " + units + " of " + give.getLowerCaseName() + " on board.";
	}
	
	@Override
	protected String [] getMainTextTokens() {
		CommoditySpecAPI give = Global.getSettings().getCommoditySpec(playerGiveCommodity);
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		int qty = (int) cargo.getQuantity(CargoItemType.RESOURCES, give.getId());
		return new String [] { Misc.getWithDGS(quantity),  Misc.getWithDGS(playerGiveQuantity) ,
				Misc.getWithDGS(qty)};
	}
	@Override
	protected Color [] getMainTextColors() {
		return new Color [] { Misc.getHighlightColor(), Misc.getHighlightColor(), Misc.getHighlightColor()};
	}
	
	@Override
	protected String getConfirmText() {
		CommoditySpecAPI take = Global.getSettings().getCommoditySpec(commodity);
		CommoditySpecAPI give = Global.getSettings().getCommoditySpec(playerGiveCommodity);
		return "Offer to swap " + playerGiveQuantity + " " + give.getLowerCaseName() + " " +
				"for " + quantity + " " + take.getLowerCaseName() + ", as a favor from you to " + getHimOrHer();
	}
	
	@Override
	protected String getCancelText() {
		return "Do nothing but commiserate with the quartermaster as you finish your drink.";
	}

	@Override
	protected String getAcceptText() {
		CommoditySpecAPI take = Global.getSettings().getCommoditySpec(commodity);
		CommoditySpecAPI give = Global.getSettings().getCommoditySpec(playerGiveCommodity);
		return "You exchange comms with the quartermaster and the very next day " + getHeOrShe() + 
			   " smoothes things along with the port authorities and arranges for the quickest cargo " +
			   "transfer you've ever seen, trading your %s " + give.getLowerCaseName() + " for " +
			   "%s " + take.getLowerCaseName() + ". Afterward, the quartermaster " +
			   "sends you a personal thank-you note.";
	}
	
	@Override
	protected String [] getAcceptTextTokens() {
		return new String [] { Misc.getWithDGS(playerGiveQuantity), Misc.getWithDGS(quantity) };
	}
	@Override
	protected Color [] getAcceptTextColors() {
		return new Color [] { Misc.getHighlightColor(), Misc.getHighlightColor() };
	}

	@Override
	protected void doExtraConfirmActions() {
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		cargo.removeCommodity(playerGiveCommodity, playerGiveQuantity);
		
		TextPanelAPI text = dialog.getTextPanel();
		AddRemoveCommodity.addCommodityLossText(playerGiveCommodity, playerGiveQuantity, text);
		
		CustomRepImpact impact = new CustomRepImpact();
		impact.delta = 0.1f;
		
		Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.CUSTOM, 
						impact, null, text, true, true),
						person);
		
		impact = new CustomRepImpact();
		impact.delta = 0.03f;
		Global.getSector().adjustPlayerReputation(
				new RepActionEnvelope(RepActions.CUSTOM, 
						impact, null, text, true, true),
						person.getFaction().getId());
		
		ContactIntel.addPotentialContact(person, market, text);
	}
	
	
	@Override
	protected String getDeclineText() {
		return "You drink to the bureaucrats in Fleet Supply \"mistaking the airlock for " +
				"the head-hatch\", empty your glass, and make your exit.";
	}


	
}



