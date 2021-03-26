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

public class HandMeDownFreighter extends HubMissionWithBarEvent {
	public static float BASE_PRICE_MULT = 0.33f;

	public static WeightedRandomPicker<String> HULLS = new WeightedRandomPicker<String>();
	static {
		HULLS.add("buffalo_Hull", 7f);
		HULLS.add("tarsus_Hull", 7f);
		HULLS.add("colossus_Hull", 5f);
		HULLS.add("atlas_Hull", 1f);
		HULLS.add("wayfarer_Hull", 5f);
		HULLS.add("gemini_Hull", 1f);
		HULLS.add("mule_d_pirates_Hull", 1f);
		HULLS.add("mule_Hull", 1f);
	}
	
	
	protected FleetMemberAPI member;
	protected int price;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		
		float probAbort = 0.75f * PirateBaseManager.getInstance().getStandardTimeFactor();
		if (rollProbability(probAbort)) return false;
		
		if (barEvent) {
			setGiverFaction(Factions.INDEPENDENT);
			setGiverPost(Ranks.POST_SPACER);
			setGiverVoice(Voices.SPACER);
			setGiverImportance(pickLowImportance());
			setGiverTags(Tags.CONTACT_UNDERWORLD, Tags.CONTACT_TRADE);
			findOrCreateGiver(createdAt, false, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		MarketAPI market = person.getMarket();
		if (market == null) return false;
		
		if (!setPersonMissionRef(person, "$hmdf_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		//genRandom = Misc.random;
		
//		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(genRandom);
//		picker.add("buffalo_Hull", 7f);
//		picker.add("tarsus_Hull", 7f);
//		picker.add("colossus_Hull", 5f);
//		picker.add("atlas_Hull", 1f);
//		picker.add("wayfarer_Hull", 5f);
//		picker.add("gemini_Hull", 1f);
//		picker.add("mule_d_pirates_Hull", 1f);
//		picker.add("mule_Hull", 1f);
//		
//		String variantId = picker.pick();
		String variantId = HULLS.pick(genRandom);
		ShipVariantAPI variant = Global.getSettings().getVariant(variantId).clone();
		member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);
		assignShipName(member, Factions.INDEPENDENT);
		
		int dMods = 2 + genRandom.nextInt(3);
		DModManager.addDMods(variant, true, dMods, genRandom);
		DModManager.removeDMod(variant, HullMods.COMP_STORAGE);
		
		member.getCrewComposition().setCrew(100000);
		member.getRepairTracker().setCR(0.7f);
		
		price = getRoundNumber(variant.getHullSpec().getBaseValue() * BASE_PRICE_MULT);
		
		setRepFactionChangesTiny();
		setRepPersonChangesVeryLow();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		// this is weird - in the accept() method, the mission is aborted, which unsets
		// $hmdf_ref. So: we use $hmdf_ref2 in the ContactPostAccept rule
		// and $hmdf_ref2 has an expiration of 0, so it'll get unset on its own later.
		set("$hmdf_ref2", this);
		
		set("$hmdf_barEvent", isBarEvent());
		set("$hmdf_hullClass", member.getHullSpec().getHullNameWithDashClass());
		set("$hmdf_price", Misc.getWithDGS(price));
		set("$hmdf_manOrWoman", getPerson().getManOrWoman());
		set("$hmdf_hisOrHer", getPerson().getHisOrHer());
		set("$hmdf_member", member);
	}
	
	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
							     Map<String, MemoryAPI> memoryMap) {
		if ("showShip".equals(action)) {
			dialog.getVisualPanel().showFleetMemberInfo(member, true);
			return true;
		} else if ("showPerson".equals(action)) {
			dialog.getVisualPanel().showPersonInfo(getPerson(), true);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Hand-me-down Freighter"; // not used I don't think
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




