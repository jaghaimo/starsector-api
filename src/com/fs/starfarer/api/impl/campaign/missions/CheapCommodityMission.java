package com.fs.starfarer.api.impl.campaign.missions;

import java.awt.Color;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class CheapCommodityMission extends HubMissionWithBarEvent {

	public static boolean SAME_CONTACT_DEBUG = false;
	
	public static float MISSION_DAYS = 60f;
	
	public static float MIN_BASE_VALUE = 10000;
	public static float MAX_BASE_VALUE = 100000;
	public static float BASE_PRICE_MULT = 0.75f;
	
	public static float PROB_REMOTE = 0.5f;
	
	public static float PROB_BAR_UNDERWORLD = 0.25f;
	public static float PROB_ILLEGAL_IF_UNDERWORLD = 0.75f;
	public static float ILLEGAL_QUANTITY_MULT = 0.5f;
	
	
	public static enum Stage {
		TALK_TO_PERSON,
		COMPLETED,
		FAILED,
	}
	public static enum Variation {
		LOCAL,
		REMOTE,
	}
	
	protected String commodityId;
	protected int quantity;
	protected int pricePerUnit;
	
	protected Variation variation;
	protected MarketAPI remoteMarket;
	protected PersonAPI remoteContact;
	
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		if (barEvent) {
			if (rollProbability(PROB_BAR_UNDERWORLD)) {
				setGiverRank(Ranks.CITIZEN);
				setGiverPost(pickOne(Ranks.POST_SMUGGLER, Ranks.POST_GANGSTER, 
							 		 Ranks.POST_FENCE, Ranks.POST_CRIMINAL));
				setGiverImportance(pickImportance());
				setGiverFaction(Factions.PIRATES);
				setGiverTags(Tags.CONTACT_UNDERWORLD);
			} else {
				setGiverRank(Ranks.CITIZEN);
				setGiverPost(pickOne(Ranks.POST_TRADER, Ranks.POST_COMMODITIES_AGENT, 
							 		 Ranks.POST_MERCHANT, Ranks.POST_INVESTOR, Ranks.POST_PORTMASTER));
				setGiverImportance(pickImportance());
				setGiverTags(Tags.CONTACT_TRADE);
			}
			findOrCreateGiver(createdAt, false, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		MarketAPI market = person.getMarket();
		if (market == null) return false;
		
		if (!setPersonMissionRef(person, "$cheapCom_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		PersonImportance importance = person.getImportance();
		boolean canOfferRemote = importance.ordinal() >= PersonImportance.MEDIUM.ordinal();
		boolean preferExpensive = getQuality() >= PersonImportance.HIGH.getValue();
		variation = Variation.LOCAL;
		if (canOfferRemote && rollProbability(PROB_REMOTE)) {
			variation = Variation.REMOTE;
		}
		if (SAME_CONTACT_DEBUG) variation = Variation.REMOTE;

		CommodityOnMarketAPI com = null;
		if (variation == Variation.LOCAL) {
			requireMarketIs(market);
			requireCommodityIsNotPersonnel();
			requireCommodityDeficitAtMost(0);
			requireCommodityAvailableAtLeast(1);
			requireCommoditySurplusAtLeast(1);
			if (person.hasTag(Tags.CONTACT_UNDERWORLD) && rollProbability(PROB_ILLEGAL_IF_UNDERWORLD)) {
				preferCommodityIllegal();
			} else {
				requireCommodityLegal();
			}
			if (preferExpensive) {
				preferCommodityTags(ReqMode.ALL, Commodities.TAG_EXPENSIVE);
			}
			com = pickCommodity();
		} 
		
		if (com == null && canOfferRemote) {
			variation = Variation.REMOTE;
		}
		
		
		if (variation == Variation.REMOTE) {
			requireMarketIsNot(market);
			requireMarketFaction(market.getFactionId());
			if (SAME_CONTACT_DEBUG) {
				requireMarketIs("jangala");
			}
			requireCommodityIsNotPersonnel();
			requireCommodityDeficitAtMost(0);
			requireCommodityAvailableAtLeast(1);
			requireCommoditySurplusAtLeast(1);
			preferMarketInDirectionOfOtherMissions();
			if (person.hasTag(Tags.CONTACT_UNDERWORLD) && rollProbability(PROB_ILLEGAL_IF_UNDERWORLD)) {
				preferCommodityIllegal();
			} else {
				requireCommodityLegal();
			}
			if (preferExpensive) {
				preferCommodityTags(ReqMode.ALL, Commodities.TAG_EXPENSIVE);
			}
			com = pickCommodity();
			if (com != null) remoteMarket = com.getMarket();
		}
		
		if (SAME_CONTACT_DEBUG) {
			com = Global.getSector().getEconomy().getMarket("jangala").getCommodityData(Commodities.ORGANICS);
			remoteMarket = com.getMarket();
		}
		
		if (com == null) return false;
		
		commodityId = com.getId();
		
		float value = MIN_BASE_VALUE + (MAX_BASE_VALUE - MIN_BASE_VALUE) * getQuality();
		quantity = getRoundNumber(value / com.getCommodity().getBasePrice());
		if (com.isIllegal()) {
			quantity *= ILLEGAL_QUANTITY_MULT;
		}
		
		if (quantity < 10) quantity = 10;
		pricePerUnit = (int) (com.getMarket().getSupplyPrice(com.getId(), quantity, true) / (float) quantity * 
							  BASE_PRICE_MULT / getRewardMult());
		pricePerUnit = getRoundNumber(pricePerUnit);
		if (pricePerUnit < 2) pricePerUnit = 2;
		
		
		if (variation == Variation.REMOTE) {
			remoteContact = findOrCreateTrader(remoteMarket.getFactionId(), remoteMarket, true);
			//person = findOrCreateCriminal(market, true);
			if (remoteContact == null || !setPersonMissionRef(remoteContact, "$cheapCom_ref")) {
				return false;
			}
			setPersonDoGenericPortAuthorityCheck(remoteContact);
			makeImportant(remoteContact, "$cheapCom_hasCommodity", Stage.TALK_TO_PERSON);
			
			setStartingStage(Stage.TALK_TO_PERSON);
			setSuccessStage(Stage.COMPLETED);
			setFailureStage(Stage.FAILED);
			
			setStageOnMemoryFlag(Stage.COMPLETED, remoteContact, "$cheapCom_completed");
			setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
			
		}
		
		if (getQuality() < 0.5f) {
			setRepFactionChangesVeryLow();
		} else {
			setRepFactionChangesLow();
		}
		setRepPersonChangesMedium();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$cheapCom_ref2", this);
		
		set("$cheapCom_barEvent", isBarEvent());
		set("$cheapCom_underworld", getPerson().hasTag(Tags.CONTACT_UNDERWORLD));
		
		set("$cheapCom_commodityId", commodityId);
		set("$cheapCom_commodityName", getSpec().getLowerCaseName());
		set("$cheapCom_quantity", Misc.getWithDGS(quantity));
		set("$cheapCom_pricePerUnit", Misc.getDGSCredits(pricePerUnit));
		set("$cheapCom_totalPrice", Misc.getDGSCredits(pricePerUnit * quantity));
		set("$cheapCom_variation", variation);
		set("$cheapCom_manOrWoman", getPerson().getManOrWoman());
		//set("$cheapCom_heOrShe", getPerson().getHeOrShe());
		//set("$cheapCom_HeOrShe", getPerson().getHeOrShe().substring(0, 1).toUpperCase() + getPerson().getHeOrShe().substring(1));
		
		//set("$cheapCom_manOrWoman", getPerson().getManOrWoman());
		
		if (variation == Variation.REMOTE) {
			set("$cheapCom_personName", remoteContact.getNameString());
			set("$cheapCom_personPost", remoteContact.getPost().toLowerCase());
			set("$cheapCom_marketName", remoteMarket.getName());
			set("$cheapCom_marketOnOrAt", remoteMarket.getOnOrAt());
			set("$cheapCom_dist", getDistanceLY(remoteMarket));
		}
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.TALK_TO_PERSON) {
			TooltipMakerAPI text = info.beginImageWithText(remoteContact.getPortraitSprite(), 48f);
			text.addPara("Go to " + remoteMarket.getName() + " and contact " + remoteContact.getNameString() + " to pick up %s units of " + 
					getSpec().getLowerCaseName() + " " +
					"for %s per unit, or %s total.",
					0f, h,
					Misc.getWithDGS(quantity),
					Misc.getDGSCredits(pricePerUnit), 
					Misc.getDGSCredits(pricePerUnit * quantity));
			info.addImageWithText(opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.TALK_TO_PERSON) {
			info.addPara("Go to " + remoteMarket.getName() + " and contact " + remoteContact.getNameString() + " to arrange pickup", tc, pad);
			return true;
		}
		return false;
	}	
	
//	protected String getMissionTypeNoun() {
//		return "mission";
//	}
	
	@Override
	public String getBaseName() {
		return getSpec().getName() + " Pickup";
	}
	
	@Override
	public void accept(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		if (variation == Variation.REMOTE) {
			super.accept(dialog, memoryMap);
			
			// papering over a bug for a hotfix - apparently it's possible
			// for the remote contact to be removed from the comm directory
			// should be fixed by -RC9, though; possibly save is from RC8
			if (remoteMarket != null && remoteMarket.getCommDirectory() != null && 
					remoteMarket.getCommDirectory().getEntryForPerson(remoteContact) == null) {
				remoteMarket.getCommDirectory().addPerson(remoteContact);
			}
		} else {
			// if it's the local variation, there's no intel item and the commodity/credits etc is handled
			// in the rules csv. Need to abort here, though, so that mission ref is unset from person memory
			
			currentStage = new Object(); // so that the abort() assumes the mission was successful
			abort();
		}
	}
	
	protected transient CommoditySpecAPI spec;
	protected CommoditySpecAPI getSpec() {
		if (spec == null) {
			spec = Global.getSettings().getCommoditySpec(commodityId);
		}
		return spec;
	}

	@Override
	public String getBlurbText() {
		return null;
	}

	
	
//		if (variation == Variation.REMOTE) {
//			return "There's a surplus of " + getSpec().getLowerCaseName() + 
//				   " at " + remoteMarket.getName() + " that I can let you have for a good price.";
//		}
//		return "There's a surplus of " + getSpec().getLowerCaseName() + " I can let you have for a good price.";
}

