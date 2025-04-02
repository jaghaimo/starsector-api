package com.fs.starfarer.api.impl.campaign.missions;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.PlayerFleetPersonnelTracker;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class MercsOnTheRun extends HubMissionWithBarEvent {
	public static float BASE_PRICE_MULT = 0.75f;
	
	protected FleetMemberAPI member;
	protected int price;
	protected int quantity;
	protected MarketAPI market;
	protected String commodity;
	protected int totalPrice;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		
		if (barEvent) {
			setGiverFaction(Factions.INDEPENDENT);
			setGiverPost(Ranks.POST_MARINE_SQUAD_LEADER);
			setGiverVoice(Voices.SPACER);
			setGiverImportance(pickLowImportance());
			//no. setGiverTags(Tags.CONTACT_UNDERWORLD, Tags.CONTACT_TRADE);
			findOrCreateGiver(createdAt, false, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		market = person.getMarket();
		if (market == null) return false;
		
		if (!setPersonMissionRef(person, "$motr_ref")) {
			return false;
		}
	
		commodity = "marines"; // kosher?
	
		quantity = 30 + genRandom.nextInt(21);
		
		float _price = market.getSupplyPrice(commodity, 1, true);
		
		float unitPrice = (int) (_price * BASE_PRICE_MULT);
		if (unitPrice > 50) {
			unitPrice = unitPrice / 10 * 10;
		}
		if (unitPrice < 1 && unitPrice > 0) {
			unitPrice = 1;
		}
		
		price = (int)(unitPrice * quantity);
		
		setRepFactionChangesTiny();
		setRepPersonChangesVeryLow();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		// this is weird - in the accept() method, the mission is aborted, which unsets
		// $hmdf_ref. So: we use $hmdf_ref2 in the ContactPostAccept rule
		// and $hmdf_ref2 has an expiration of 0, so it'll get unset on its own later.
		set("$motr_ref2", this);
		
		set("$motr_barEvent", isBarEvent());
		set("$motr_price", price);
		set("$motr_numberOfMarines", quantity);
		set("$motr_priceText", Misc.getWithDGS(price));
		set("$motr_manOrWoman", getPerson().getManOrWoman());
		set("$motr_hisOrHer", getPerson().getHisOrHer());
		set("$motr_member", member);
	}
	
	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		//if ("showShip".equals(action)) {
		//	dialog.getVisualPanel().showFleetMemberInfo(member, true);
		//	return true;
		if ("buyMarines".equals(action)) {
			//dialog.getVisualPanel().showPersonInfo(getPerson(), true);
			PlayerFleetPersonnelTracker.getInstance().update();
			PlayerFleetPersonnelTracker.getInstance().getMarineData().addXP((float)quantity);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Mercs On The Run"; // not used I don't think
	}
	
	@Override
	public void accept(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		// it's just an transaction immediate transaction handled in rules.csv
		// no intel item etc
		
		currentStage = new Object(); // so that the abort() assumes the mission was successful
		abort();
		
		for (CampaignFleetAPI fleet : getPerson().getMarket().getContainingLocation().getFleets()) {
			if (fleet.getFaction().isPlayerFaction()) continue;
			if (!Misc.isPatrol(fleet)) continue;
			fleet.getMemoryWithoutUpdate().set(MemFlags.PATROL_EXTRA_SUSPICION, 1f);
		}
	}
	
}




