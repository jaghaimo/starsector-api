package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.util.Misc;

public class DiktatLobsterBarEvent extends BaseGetCommodityBarEvent {
	
	public DiktatLobsterBarEvent() {
		super();
	}
	
	public boolean shouldShowAtMarket(MarketAPI market) {
		if (!super.shouldShowAtMarket(market)) return false;
		regen(market);
		
		if (!market.getFactionId().equals(Factions.DIKTAT)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	protected void doExtraConfirmActions() {
		ContactIntel.addPotentialContact(person, market, text);
	}

	@Override
	protected void adjustPerson(PersonAPI person) {
		super.adjustPerson(person);
		person.setImportanceAndVoice(pickMediumImportance(), random);
		person.addTag(Tags.CONTACT_MILITARY);
	}
	
	@Override
	protected String getPersonPost() {
		return Ranks.POST_AGENT;
	}

	@Override
	protected String getCommodityId() {
		return Commodities.LOBSTER;
	}
	
	@Override
	protected String getPersonFaction() {
		return Factions.DIKTAT;
	}
	
	@Override
	protected String getPersonRank() {
		return Ranks.GROUND_CAPTAIN;
	}
	
	@Override
	protected int computeQuantity() {
		int quantity = 50 + 50 * random.nextInt(4);
		return quantity;
	}
	
	@Override
	protected float getPriceMult() {
		return 0.75f;
	}
	
	@Override
	protected String getPrompt() {
		return "A grinning security officer is gesturing excitedly to a nearby patron.";
	}
	
	@Override
	protected String getOptionText() {
		return "Sidle up to the security officer and see what they're so pleased about";
	}
	
	@Override
	protected String getMainText() {
		String heOrShe = getHeOrShe();
		String himOrHer = getHimOrHer();
		String hisOrHer = getHisOrHer();
		
		return "\"Why, just today an amazing opportunity fell into my lap. " +
				"And yours as well, how wonderful!\" " + heOrShe + " sweeps " + hisOrHer + " arm regally " +
				"to encompass the scale of your shared luck, somehow managing not to spill the drinks of " +
				"several patrons.\n\n"
				+ "You are told the story of tiresome property seizures from " +
				"'disloyal elements'.\n\n"
				+ " \"It's not so much trouble to, you know. Play along? " +
				"But they insist on carrying on, and on... not that I mind the perks of the job, of course.\"";
	}
	
	@Override
	protected String getMainText2() {
		String heOrShe = getHeOrShe();
		String himOrHer = getHimOrHer();
		String hisOrHer = getHisOrHer();
		
		return 
				"What this amounts to, you are told, is that the District 4 Gendarmerie's evidence " +
				"locker - and maintenance bay, and several interrogation rooms - are filled with cases " +
				"of cryo-stabilized Volturnian Lobster, nearly %s units in all. The officers eat like " +
				"royalty, yes, but there's only so much one can take and the coolant cells won't last " +
				"another week without replacement, so.\n\n"
				+ Misc.ucFirst(heOrShe) + " turns to " +
				"you. %s will satisfy the district commandant as well as your new friend here, " +
				"and the entire cargo can be transferred to your holds without the bother of tariffs.\n\n" +
				
				"\"What do you say, will you help maintain a little law and order?\"";
	}
	
	@Override
	protected String [] getMainText2Tokens() {
		return new String [] { Misc.getWithDGS(quantity), Misc.getDGSCredits(unitPrice * quantity) };
	}
	@Override
	protected Color [] getMainText2Colors() {
		return new Color [] { Misc.getHighlightColor(), Misc.getHighlightColor() };
	}
	
	@Override
	protected String getConfirmText() {
		return "Accept the offer and transfer " + Misc.getDGSCredits(unitPrice * quantity) + " to the provided TriAnon account";
	}
	
	@Override
	protected String getCancelText() {
		return "Decline the offer apologetically, citing a terrible shellfish allergy";
	}

	@Override
	protected String getAcceptText() {
		return null;
	}
	
	@Override
	protected String [] getAcceptTextTokens() {
		return new String [] {};
	}
	@Override
	protected Color [] getAcceptTextColors() {
		return new Color [] {};
	}
	
}



