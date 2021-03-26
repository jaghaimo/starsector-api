package com.fs.starfarer.api.impl.campaign.missions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
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
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SurplusShipHull extends HubMissionWithBarEvent {

	public static float BASE_PRICE_MULT = 0.5f;
	
	protected FleetMemberAPI member;
	protected int price;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		if (barEvent) {
			List<String> posts = new ArrayList<String>();
			posts.add(Ranks.POST_SUPPLY_OFFICER);
			if (Misc.isMilitary(createdAt)) {
				posts.add(Ranks.POST_BASE_COMMANDER);
			}
			if (Misc.hasOrbitalStation(createdAt)) {
				posts.add(Ranks.POST_STATION_COMMANDER);
			}
			
			String post = pickOne(posts);
			setGiverPost(post);
			if (post.equals(Ranks.POST_SUPPLY_OFFICER)) {
				setGiverRank(Ranks.SPACE_COMMANDER);
				setGiverImportance(pickImportance());
			} else if (post.equals(Ranks.POST_BASE_COMMANDER)) {
				setGiverRank(Ranks.GROUND_COLONEL);
				setGiverImportance(pickImportance());
			} else if (post.equals(Ranks.POST_STATION_COMMANDER)) {
				setGiverRank(Ranks.SPACE_CAPTAIN);
				setGiverImportance(pickHighImportance());
			}
			if (Factions.PIRATES.equals(createdAt.getFaction().getId())) {
				setGiverTags(Tags.CONTACT_UNDERWORLD);
				setGiverFaction(Factions.PIRATES);
			} else {
				setGiverTags(Tags.CONTACT_MILITARY);
			}
			findOrCreateGiver(createdAt, false, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		MarketAPI market = person.getMarket();
		if (market == null) return false;
		
		if (!Misc.isMilitary(market) && market.getSize() < 7) return false;
		
		if (!setPersonMissionRef(person, "$sShip_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		//genRandom = Misc.random;
		
		
		ShipPickParams params = new ShipPickParams(ShipPickMode.PRIORITY_THEN_ALL);
		String role = pickRole(getQuality(), person.getFaction(), person.getImportance(), genRandom);
		
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
		
		float quality = ShipQuality.getShipQuality(market, person.getFaction().getId());
		float averageDmods = DefaultFleetInflater.getAverageDmodsForQuality(quality);
		int addDmods = DefaultFleetInflater.getNumDModsToAdd(variant, averageDmods, genRandom);
		if (addDmods > 0) {
			DModManager.setDHull(variant);
			DModManager.addDMods(member, true, addDmods, genRandom);
		}
		member.getCrewComposition().setCrew(100000);
		member.getRepairTracker().setCR(0.7f);
		
		if (BASE_PRICE_MULT == 1f) {
			price = (int) Math.round(variant.getHullSpec().getBaseValue());
		} else {
			price = getRoundNumber(variant.getHullSpec().getBaseValue() * BASE_PRICE_MULT);
		}
		
		setRepFactionChangesTiny();
		setRepPersonChangesVeryLow();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		// this is weird - in the accept() method, the mission is aborted, which unsets
		// $sShip_ref. So: we use $sShip_ref2 in the ContactPostAccept rule
		// and $sShip_ref2 has an expiration of 0, so it'll get unset on its own later.
		set("$sShip_ref2", this);
		
		set("$sShip_barEvent", isBarEvent());
		set("$sShip_hullSize", member.getHullSpec().getDesignation().toLowerCase());
		set("$sShip_hullClass", member.getHullSpec().getHullNameWithDashClass());
		set("$sShip_price", Misc.getWithDGS(price));
		set("$sShip_manOrWoman", getPerson().getManOrWoman());
		set("$sShip_rank", getPerson().getRank().toLowerCase());
		set("$sShip_rankAOrAn", getPerson().getRankArticle());
		set("$sShip_hisOrHer", getPerson().getHisOrHer());
		set("$sShip_member", member);
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
		return "Surplus Ship Hull"; // not used I don't think
	}
	
	@Override
	public void accept(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		// it's just an transaction immediate transaction handled in rules.csv
		// no intel item etc
		
		currentStage = new Object(); // so that the abort() assumes the mission was successful
		abort();
	}
	
	public static String pickRole(float quality, FactionAPI faction, PersonImportance imp, Random random) {
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		
		float cycles = PirateBaseManager.getInstance().getDaysSinceStart() / 365f;
		
		if (imp == PersonImportance.VERY_HIGH && cycles < 3) imp = PersonImportance.HIGH;
		if (imp == PersonImportance.HIGH && cycles < 1) imp = PersonImportance.MEDIUM;
		
		if (quality < 0.5f && imp.ordinal() > PersonImportance.MEDIUM.ordinal()) {
			imp = PersonImportance.MEDIUM;
		}
		
		float w = faction.getDoctrine().getWarships() - 1f;
		float c = faction.getDoctrine().getCarriers() - 1f;
		float p = faction.getDoctrine().getPhaseShips() - 1f;
		if (w + c + p < 1) w = 1;
		
		switch (imp) {
		case VERY_LOW:
			picker.add(ShipRoles.COMBAT_SMALL, w);
			picker.add(ShipRoles.COMBAT_MEDIUM, w/2f);
			picker.add(ShipRoles.CARRIER_SMALL, c);
			picker.add(ShipRoles.PHASE_SMALL, p);
			break;
		case LOW:
			picker.add(ShipRoles.COMBAT_SMALL, w/2f);
			picker.add(ShipRoles.COMBAT_MEDIUM, w);
			picker.add(ShipRoles.CARRIER_SMALL, c);
			picker.add(ShipRoles.PHASE_SMALL, p);
			break;
		case MEDIUM:
			picker.add(ShipRoles.COMBAT_MEDIUM, w/2f);
			picker.add(ShipRoles.COMBAT_LARGE, w);
			picker.add(ShipRoles.CARRIER_SMALL, c/2f);
			picker.add(ShipRoles.CARRIER_MEDIUM, c);
			picker.add(ShipRoles.PHASE_SMALL, p/2f);
			picker.add(ShipRoles.PHASE_MEDIUM, p);
			break;
		case HIGH:
			picker.add(ShipRoles.COMBAT_MEDIUM, w/4f);
			picker.add(ShipRoles.COMBAT_LARGE, w);
			picker.add(ShipRoles.COMBAT_CAPITAL, w/2f);
			picker.add(ShipRoles.CARRIER_MEDIUM, c);
			picker.add(ShipRoles.CARRIER_LARGE, c/2f);
			picker.add(ShipRoles.PHASE_MEDIUM, p);
			picker.add(ShipRoles.PHASE_LARGE, p/2f);
			break;
		case VERY_HIGH:
			picker.add(ShipRoles.COMBAT_MEDIUM, w/4f);
			picker.add(ShipRoles.COMBAT_LARGE, w/2f);
			picker.add(ShipRoles.COMBAT_CAPITAL, w);
			picker.add(ShipRoles.CARRIER_MEDIUM, c/2f);
			picker.add(ShipRoles.CARRIER_LARGE, c);
			picker.add(ShipRoles.PHASE_MEDIUM, p/2f);
			picker.add(ShipRoles.PHASE_LARGE, p);
			picker.add(ShipRoles.PHASE_CAPITAL, p/2f);
			break;
		}
		return picker.pick();
	}
	
}

