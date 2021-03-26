package com.fs.starfarer.api.impl.campaign.missions;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.econ.impl.ShipQuality;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class HijackingMission extends HubMissionWithBarEvent {

	public static float BASE_PRICE_MULT = 0.33f;


	protected FleetMemberAPI member;
	protected int price;
	protected int marines;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		
		if (barEvent) {
			setGiverRank(Ranks.CITIZEN);
			setGiverPost(pickOne(Ranks.POST_SMUGGLER, Ranks.POST_GANGSTER, 
						 		 Ranks.POST_FENCE, Ranks.POST_CRIMINAL));
			setGiverImportance(pickImportance());
			setGiverFaction(Factions.PIRATES);
			setGiverTags(Tags.CONTACT_UNDERWORLD);
			findOrCreateGiver(createdAt, false, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		MarketAPI market = person.getMarket();
		if (market == null) return false;
		if (market.isPlayerOwned()) return false;
		
		if (!setPersonMissionRef(person, "$hijack_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		//genRandom = Misc.random;
		
		ShipPickParams params = new ShipPickParams(ShipPickMode.PRIORITY_THEN_ALL);
		String role = SurplusShipHull.pickRole(getQuality(), market.getFaction(), person.getImportance(), genRandom);
		
		ShipVariantAPI variant = null;
		for (int i = 0; i < 10; i++) {
			List<ShipRolePick> picks = market.getFaction().pickShip(role, params, null, genRandom);
			if (picks.isEmpty()) return false;
			String variantId = picks.get(0).variantId;
			variant = Global.getSettings().getVariant(variantId);
			variant = Global.getSettings().getVariant(variant.getHullSpec().getHullId() + "_Hull").clone();
			if (variant.getHullSpec().hasTag(Tags.NO_SELL)) {
				variant = null;
				continue;
			}
			break;
		}
		if (variant == null) return false;
			
		member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);
		assignShipName(member, Factions.INDEPENDENT);
		
		float quality = ShipQuality.getShipQuality(market, market.getFaction().getId());
		float averageDmods = DefaultFleetInflater.getAverageDmodsForQuality(quality);
		int addDmods = DefaultFleetInflater.getNumDModsToAdd(variant, averageDmods, genRandom);
		if (addDmods > 0) {
			DModManager.setDHull(variant);
			DModManager.addDMods(member, true, addDmods, genRandom);
		}
		
		price = getRoundNumber(variant.getHullSpec().getBaseValue() * BASE_PRICE_MULT);
		
		
		setRepFactionChangesTiny();
		setRepPersonChangesVeryLow();
		
		switch (member.getHullSpec().getHullSize()) {
		case CAPITAL_SHIP:
			marines = 100;
			setRepFactionChangesVeryLow();
			setRepPersonChangesLow();
			break;
		case CRUISER:
			marines = 50;
			break;
		case DESTROYER:
			marines = 20;
			break;
		case FRIGATE:
			marines = 10;
			break;
		}
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$hijack_barEvent", isBarEvent());
		set("$hijack_hull", member.getHullSpec().getHullNameWithDashClass());
		set("$hijack_designation", member.getHullSpec().getDesignation());
		set("$hijack_price", Misc.getWithDGS(price));
		set("$hijack_marines", Misc.getWithDGS(marines));
		
		set("$hijack_manOrWoman", getPerson().getManOrWoman());
		set("$hijack_hisOrHer", getPerson().getHisOrHer());
		set("$hijack_member", member);
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
		return "Hijacking"; // not used since there's no intel item
	}
	
	@Override
	public void accept(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		// it's just an transaction immediate transaction handled in rules.csv
		// no intel item etc
		
		currentStage = new Object(); // so that the abort() assumes the mission was successful
		abort();
	}
}




