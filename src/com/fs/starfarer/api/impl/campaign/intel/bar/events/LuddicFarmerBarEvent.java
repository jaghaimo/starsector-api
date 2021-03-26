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

public class LuddicFarmerBarEvent extends BaseGetCommodityBarEvent {
	
	public LuddicFarmerBarEvent() {
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
		return Commodities.FOOD;
	}
	
	@Override
	protected void doExtraConfirmActions() {
		ContactIntel.addPotentialContact(person, market, text);
	}

	@Override
	protected void adjustPerson(PersonAPI person) {
		super.adjustPerson(person);
		person.setImportanceAndVoice(pickLowImportance(), random);
		person.addTag(Tags.CONTACT_TRADE);
	}
	
	@Override
	protected String getPersonPost() {
		return Ranks.POST_COMMUNE_LEADER;
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
		int quantity = 50 + 10 * random.nextInt(6);
		
		CommodityOnMarketAPI com = market.getCommodityData(commodity);
		int size = Math.min(com.getAvailable(), com.getMaxSupply());
		if (size < 1) size = 1;
		//quantity *= BaseIndustry.getSizeMult(size);
		quantity *= Math.max(1, BaseIndustry.getSizeMult(size) - 2);
		return quantity;
	}
	
	@Override
	protected float getPriceMult() {
		return 0.75f;
	}
	
	@Override
	protected String getPrompt() {
		return "A weathered-looking " + getManOrWoman() + " whose worn but well-tailored " +
			   "work suit bears the sigil of a farming commune sits at a table drinking small-beer.";
	}
	
	@Override
	protected String getOptionText() {
		return "Chat up the weathered-looking " + getManOrWoman() + "";
	}
	
	@Override
	protected String getMainText() {
		String heOrShe = getHeOrShe();
		return Misc.ucFirst(heOrShe) + " turns out to be the master of a farming commune " +
			   "and " + heOrShe + " has come \"to town\" to find distributors for their product. " +
			   Misc.ucFirst(heOrShe) + "'s got %s units of food that has to be moved as soon as possible and " +
			   "if you buy it all today, you'll get an under-market price of %s per unit. " +
			   "And, " + heOrShe + " says with a wink, \"As pious folk, we need not bother with the Church’s tariff.\",";
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
		return "Accept with a firm handshake and cryptokey exchange";
	}
	
	@Override
	protected String getCancelText() {
		return "Decline the deal, and thank " + getHimOrHer() + " for the proposal";
	}

	@Override
	protected String getAcceptText() {
		return "You make arrangements with the farm master for pickup of the cargo from a quiet warehouse " +
		"on the outskirts of " + market.getName() + " and transmit the details " +
		"to your quartermaster to handle.";
	}
	
	@Override
	protected String [] getAcceptTextTokens() {
		return new String [] { };
	}
	@Override
	protected Color [] getAcceptTextColors() {
		return new Color [] { };
	}
	
	
}



