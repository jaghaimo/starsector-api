package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.util.Misc;

public class LuddicCraftBarEvent extends BaseGetCommodityBarEvent {
	
	public LuddicCraftBarEvent() {
		super();
	}
	
	public boolean shouldShowAtMarket(MarketAPI market) {
		if (!super.shouldShowAtMarket(market)) return false;
		regen(market);
		
		if (!market.getFactionId().equals(Factions.LUDDIC_CHURCH) &&
				!market.getFactionId().equals(Factions.LUDDIC_PATH)) {
			return false;
		}
		
		if (market.getStabilityValue() < 4) return false;
		
		CommodityOnMarketAPI com = market.getCommodityData(commodity);
		if (com.getMaxSupply() <= 0) return false;
		if (com.getAvailable() < com.getMaxDemand()) return false;
		
		return true;
	}

	@Override
	protected String getCommodityId() {
		return Commodities.DOMESTIC_GOODS;
	}
	
	@Override
	protected void doExtraConfirmActions() {
		ContactIntel.addPotentialContact(person, market, text);
	}

	@Override
	protected void adjustPerson(PersonAPI person) {
		super.adjustPerson(person);
		person.setImportanceAndVoice(pickMediumImportance(), random);
		person.addTag(Tags.CONTACT_TRADE);
	}
	
	@Override
	protected String getPersonPost() {
		return Ranks.POST_GUILDMASTER;
	}
	
	@Override
	protected String getPersonFaction() {
		return Factions.LUDDIC_CHURCH;
	}
	
	@Override
	protected String getPersonRank() {
		return Ranks.CITIZEN;
	}
	
	@Override
	protected int computeQuantity() {
		int quantity = 30 + 10 * random.nextInt(4);
		
		CommodityOnMarketAPI com = market.getCommodityData(commodity);
		int size = Math.min(com.getAvailable(), com.getMaxSupply());
		if (size < 1) size = 1;
		quantity *= Math.max(1, BaseIndustry.getSizeMult(size) - 2);
		return quantity;
	}
	
	@Override
	protected float getPriceMult() {
		return 0.75f;
	}
	
	@Override
	protected String getPrompt() {
		return "A tough looking " + getManOrWoman() + " whose worn but well-tailored work suit bears the " +
				"sigil of a crafts guild sits at a corner table bearing the remnants of a spare meal.";

	}
	
	@Override
	protected String getOptionText() {
		return "Strike up a conversation with the tough-looking " + getManOrWoman() + "";
	}
	
	@Override
	protected String getMainText() {
		String heOrShe = getHeOrShe();
		String himOrHer = getHimOrHer();
		
		return "You buy " + himOrHer + " a drink and strike up a conversation. " + 
			   Misc.ucFirst(heOrShe) + " turns out to be one of the masters of a local craft guild " + 
			   " with a bit of a problem. \"His holiness, the Curate Astropolitan, turns his mind to spiritual " +
			   "rather than practical matters.\" " + 
			   Misc.ucFirst(heOrShe) + " explains that the commercial bureaucracy of " + market.getName() + 
			   " is understaffed and overworked. \"Ludd forgive me, but bills aren't paid by prayer.\"\n\n" + 
			   Misc.ucFirst(heOrShe) + " explains that there are %s units of domestic goods gathering dust " +
			   	"in a guild warehouse. If they could be quietly sold at even the under-market price " +
			   	"of %s each, don't mind the tariffs, it'd be nothing more than an act of charity.";
	}
	
	@Override
	protected String [] getMainTextTokens() {
		return new String [] { Misc.getWithDGS(quantity), Misc.getDGSCredits(unitPrice) };
	}
	@Override
	protected Color [] getMainTextColors() {
		return new Color [] { Misc.getHighlightColor(), Misc.getHighlightColor() };
	}
	
	@Override
	protected String getConfirmText() {
		return "Accept the deal and transfer " + Misc.getDGSCredits(unitPrice * quantity) + " to the guild";
	}
	
	@Override
	protected String getCancelText() {
		return "Decline the deal; \"Patience is a virtue, after all\"";
	}

	@Override
	protected String getAcceptText() {
		return "\"Praise be to Providence for bringing you to me!\" exclaims the guild-master " + 
			   "with a broad smile and a clap on the back. Soon enough, your cargo manifest " +
			   "counts %s additional units of domestic goods, \"recovered from deep space salvage\".";
	}
	
	@Override
	protected String [] getAcceptTextTokens() {
		return new String [] { Misc.getWithDGS(quantity)};
	}
	@Override
	protected Color [] getAcceptTextColors() {
		return new Color [] {Misc.getHighlightColor() };
	}
	
}



