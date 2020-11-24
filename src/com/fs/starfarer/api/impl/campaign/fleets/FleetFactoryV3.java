package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.AbilitySpecAPI;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class FleetFactoryV3 {

	//public static float IMPORTED_QUALITY_PENALTY = Global.getSettings().getFloat("fleetQualityPenaltyForImports");
	public static float BASE_QUALITY_WHEN_NO_MARKET = 0.5f;
	
	public static int FLEET_POINTS_THRESHOLD_FOR_ANNOYING_SHIPS = 50;
	
	public static float MIN_NUM_SHIPS_DEFICIT_MULT = 0.25f;
	
	public static int [][] BASE_COUNTS_WITH_4 = new int [][] 
	       {{9, 4, 2, 0},
			{7, 4, 2, 0},
			{4, 3, 3, 0},
			{1, 1, 1, 0},
			{1, 1, 1, 1},
	       };
	public static int [][] MAX_EXTRA_WITH_4 = new int [][] 
	       {{3, 2, 1, 1},
			{2, 2, 2, 1},
			{2, 2, 2, 1},
			{2, 2, 2, 3},
			{1, 1, 1, 1},
	       };
	
	public static int [][] BASE_COUNTS_WITH_3 = new int [][] 
	   {{6, 2, 1},
		{4, 2, 1},
		{3, 2, 1},
		{1, 1, 1},
		{1, 1, 1},
       };
	public static int [][] MAX_EXTRA_WITH_3 = new int [][] 
       {{2, 0, 0},
		{2, 1, 0},
		{2, 2, 0},
		{2, 2, 0},
		{1, 1, 0},
       };
	
	public static Logger log = Global.getLogger(FleetFactoryV3.class);
	
	
	
	
	public static float getShipQualityModForStability(float stability) {
		return (stability - 5f) * 0.05f;
	}
	public static float getNumShipsMultForStability(float stability) {
		return 1f + (stability - 5f) * 0.05f;
	}
	public static float getNumShipsMultForMarketSize(float marketSize) {
		if (marketSize < 3) marketSize = 3;
		
//		switch ((int)marketSize) {
//		case 3: return 0.5f;
//		case 4: return 0.7f;
//		case 5: return 0.85f;
//		case 6: return 1f;
//		case 7: return 1.15f;
//		case 8: return 1.3f;
//		case 9: return 1.5f;
//		case 10: return 2f;
//		}
//		switch ((int)marketSize) {
//		case 3: return 1f;
//		case 4: return 1.25f;
//		case 5: return 1.5f;
//		case 6: return 1.75f;
//		case 7: return 2.0f;
//		case 8: return 2.25f;
//		case 9: return 2.5f;
//		case 10: return 3f;
//		}
		switch ((int)marketSize) {
		case 3: return 0.5f;
		case 4: return 0.75f;
		case 5: return 1f;
		case 6: return 1.25f;
		case 7: return 1.5f;
		case 8: return 1.75f;
		case 9: return 2f;
		case 10: return 2.5f;
		}		
		
		return marketSize / 6f; 
	}
	public static float getDoctrineNumShipsMult(int doctrineNumShips) {
		float max = Global.getSettings().getFloat("maxDoctrineNumShipsMult");
		
		return 1f + (float) (doctrineNumShips - 1f) * (max - 1f) / 4f; 
	}
	
	public static CampaignFleetAPI createFleet(FleetParamsV3 params) {
		Global.getSettings().profilerBegin("FleetFactoryV3.createFleet()");
		try {
			
		boolean fakeMarket = false;
		MarketAPI market = pickMarket(params);
		if (market == null) {
			market = Global.getFactory().createMarket("fake", "fake", 5);
			market.getStability().modifyFlat("fake", 10000);
			market.setFactionId(params.factionId);
			SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
			market.setPrimaryEntity(token);
			
			market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", BASE_QUALITY_WHEN_NO_MARKET);
			
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
			
//			CommodityOnMarketAPI com = market.getCommodityData(Commodities.SHIPS);
//			com.setMaxSupply(6);
//			com.setMaxDemand(6);
//			com.getAvailableStat().setBaseValue(6);
//			com.setSupplier(new SupplierData(6, com, false));
			fakeMarket = true;
		}
		boolean sourceWasNull = params.source == null;
		params.source = market;
		if (sourceWasNull && params.qualityOverride == null) { // we picked a nearby market based on location
			params.updateQualityAndProducerFromSourceMarket();
		}
		
		//params.timestamp = Global.getSector().getClock().getTimestamp() - (long)(3600 * 24 * 1000);
//		params.timestamp = Global.getSector().getClock().getTimestamp();
//		if (params.forceNoTimestamp != null && params.forceNoTimestamp) {
//			params.timestamp = null;
//		}
		
//		if (market.getName().equals("Jangala")) {
//			System.out.println("wfwdfwef");
//		}
		
//		ShipPickMode mode = ShipPickMode.PRIORITY_THEN_OTHER;
//		if (params.producer != null && params.producer.getFaction() != market.getFaction()) {
//			mode = ShipPickMode.IMPORTED;
//		}
//		if (params.modeOverride != null) mode = params.modeOverride;
		
		String factionId = params.factionId;
		if (factionId == null) factionId = params.source.getFactionId();
		
		ShipPickMode mode = Misc.getShipPickMode(market, factionId);
		if (params.modeOverride != null) mode = params.modeOverride;
		
		CampaignFleetAPI fleet = createEmptyFleet(factionId, params.fleetType, market);
		fleet.getFleetData().setOnlySyncMemberLists(true);
		
//		if (true) {
//			fleet.getFleetData().setOnlySyncMemberLists(false);
//			fleet.getFleetData().addFleetMember("atlas_Standard");
//			return fleet;
//		}
		
		FactionDoctrineAPI doctrine = fleet.getFaction().getDoctrine();
		if (params.doctrineOverride != null) {
			doctrine = params.doctrineOverride;
		}
		
		float numShipsMult = 1f;
		if (params.ignoreMarketFleetSizeMult == null || !params.ignoreMarketFleetSizeMult) {
			numShipsMult = market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).computeEffective(0f);
		}
		
		float quality = params.quality + params.qualityMod;
//		if (mode == ShipPickMode.IMPORTED) { // factored in by FleetParamsV3 calculation of quality
//			quality -= IMPORTED_QUALITY_PENALTY;
//		}
		if (params.qualityOverride != null) {
			quality = params.qualityOverride;
		}
		
		Random random = new Random();
		if (params.random != null) random = params.random;
		
		
		float combatPts = params.combatPts * numShipsMult;
		
		if (params.onlyApplyFleetSizeToCombatShips != null && params.onlyApplyFleetSizeToCombatShips) {
			numShipsMult = 1f;
		}
		
		float freighterPts = params.freighterPts * numShipsMult;
		float tankerPts = params.tankerPts * numShipsMult;
		float transportPts = params.transportPts * numShipsMult;
		float linerPts = params.linerPts * numShipsMult;
		float utilityPts = params.utilityPts * numShipsMult;

		
		
		if (combatPts < 10 && combatPts > 0) {
			combatPts = Math.max(combatPts, 5 + random.nextInt(6));
		}
		
		float dW = (float) doctrine.getWarships() + random.nextInt(3) - 2;
		float dC = (float) doctrine.getCarriers() + random.nextInt(3) - 2;
		float dP = (float) doctrine.getPhaseShips() + random.nextInt(3) - 2;
		
		float r1 = random.nextFloat();
		float r2 = random.nextFloat();
		float min = Math.min(r1, r2);
		float max = Math.max(r1, r2);
		
		float mag = 1f;
		float v1 = min;
		float v2 = max - min;
		float v3 = 1f - max;
		
		v1 *= mag;
		v2 *= mag;
		v3 *= mag;
		
		v1 -= mag/3f;
		v2 -= mag/3f;
		v3 -= mag/3f;
		
		//System.out.println(v1 + "," + v2 + "," + v3);
		dW += v1;
		dC += v2;
		dP += v3;
		
		
//		float dW = (float) doctrine.getWarships() + random.nextInt(2) - 1;
//		float dC = (float) doctrine.getCarriers() + random.nextInt(2) - 1;
//		float dP = (float) doctrine.getPhaseShips() + random.nextInt(2) - 1;
		
		boolean banPhaseShipsEtc = !fleet.getFaction().isPlayerFaction() && 
									combatPts < FLEET_POINTS_THRESHOLD_FOR_ANNOYING_SHIPS;
		if (params.forceAllowPhaseShipsEtc != null && params.forceAllowPhaseShipsEtc) {
			banPhaseShipsEtc = !params.forceAllowPhaseShipsEtc;
		}
		
		params.mode = mode;
		params.banPhaseShipsEtc = banPhaseShipsEtc;
		
		if (banPhaseShipsEtc) {
			dP = 0;
		};
		
		if (dW < 0) dW = 0;
		if (dC < 0) dC = 0;
		if (dP < 0) dP = 0;
		
		float extra = 7 - (dC + dP + dW);
		if (extra < 0) extra = 0f;
		if (doctrine.getWarships() > doctrine.getCarriers() && doctrine.getWarships() > doctrine.getPhaseShips()) {
			dW += extra;
		} else if (doctrine.getCarriers() > doctrine.getWarships() && doctrine.getCarriers() > doctrine.getPhaseShips()) {
			dC += extra;
		} else if (doctrine.getPhaseShips() > doctrine.getWarships() && doctrine.getPhaseShips() > doctrine.getCarriers()) {
			dP += extra;
		}
		
		
		float doctrineTotal = dW + dC + dP;
		
		//System.out.println("DW: " + dW + ", DC: " + dC + " DP: " + dP);
		
		combatPts = (int) combatPts;
		int warships = (int) (combatPts * dW / doctrineTotal);
		int carriers = (int) (combatPts * dC / doctrineTotal);
		int phase = (int) (combatPts * dP / doctrineTotal);
		
		warships += (combatPts - warships - carriers - phase);
		
		
		if (params.treatCombatFreighterSettingAsFraction != null && params.treatCombatFreighterSettingAsFraction) {
			float combatFreighters = (int) Math.min(freighterPts * 1.5f, warships * 1.5f) * doctrine.getCombatFreighterProbability();
			float added = addCombatFreighterFleetPoints(fleet, random, combatFreighters, params);
			freighterPts -= added * 0.5f;
			warships -= added * 0.5f;
		} else if (freighterPts > 0 && random.nextFloat() < doctrine.getCombatFreighterProbability()) {
			float combatFreighters = (int) Math.min(freighterPts * 1.5f, warships * 1.5f);
			float added = addCombatFreighterFleetPoints(fleet, random, combatFreighters, params);
			freighterPts -= added * 0.5f;
			warships -= added * 0.5f;
		}
		
		addCombatFleetPoints(fleet, random, warships, carriers, phase, params);
		
		
		addFreighterFleetPoints(fleet, random, freighterPts, params);
		addTankerFleetPoints(fleet, random, tankerPts, params);
		addTransportFleetPoints(fleet, random, transportPts, params);
		addLinerFleetPoints(fleet, random, linerPts, params);
		addUtilityFleetPoints(fleet, random, utilityPts, params);
		
		
		//System.out.println("FLEET POINTS: " + getFP(fleet));
		
		int extraOfficers = 0;
		int maxShips = Global.getSettings().getInt("maxShipsInAIFleet");
		if (fleet.getFleetData().getNumMembers() > maxShips) {
			if (params.doNotPrune == null || !params.doNotPrune) {
				float targetFP = getFP(fleet);
				sizeOverride = 5;
				addCombatFleetPoints(fleet, random, warships, carriers, phase, params);
				addFreighterFleetPoints(fleet, random, freighterPts, params);
				addTankerFleetPoints(fleet, random, tankerPts, params);
				addTransportFleetPoints(fleet, random, transportPts, params);
				addLinerFleetPoints(fleet, random, linerPts, params);
				addUtilityFleetPoints(fleet, random, utilityPts, params);
				sizeOverride = 0;
			
				int size = doctrine.getShipSize();
				pruneFleet(size, fleet, targetFP, random);
				
				float currFP = getFP(fleet);
				//currFP = getFP(fleet);
				if (currFP < targetFP) {
					extraOfficers = (int) Math.round ((targetFP / Math.max(10f, currFP) - 1f) * 10f);
					if (extraOfficers > 30) extraOfficers = 30;
					if (extraOfficers < 0) extraOfficers = 0;
				}
			}
			
			fleet.getFleetData().sort();
			
			//System.out.println("FLEET POINTS: " + getFP(fleet));
			
			
		} else {
			fleet.getFleetData().sort();
		}
		
		fleet.getFleetData().sort();
		
		if (params.withOfficers) {
			addCommanderAndOfficers(fleet, params, extraOfficers, random);
		}
		
		fleet.forceSync();
		
		//FleetFactoryV2.doctrine = null;
		
		if (fleet.getFleetData().getNumMembers() <= 0 || 
				fleet.getFleetData().getNumMembers() == fleet.getNumFighters()) {
//			if (params.allowEmptyFleet == null || !params.allowEmptyFleet){ 
//				return null;
//			}
		}
		
		if (fakeMarket) {
			params.source = null;
		}
		
		DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
		p.quality = quality;
		p.persistent = true;
		p.seed = random.nextLong();
		p.mode = mode;
		p.timestamp = params.timestamp;
		p.allWeapons = params.allWeapons;
		if (params.factionId != null) {
			p.factionId = params.factionId;
		}
		
		FleetInflater inflater = Misc.getInflater(fleet, p);
		fleet.setInflater(inflater);
		
		fleet.getFleetData().setOnlySyncMemberLists(false);
		fleet.getFleetData().sort();
		
		List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI member : members) {
			member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
		}
		
		return fleet;
		
		} finally {
			Global.getSettings().profilerEnd();
		}
	}
	
	public static void pruneFleet(int doctrineSize, CampaignFleetAPI fleet, float targetFP, Random random) {
		int maxShips = Global.getSettings().getInt("maxShipsInAIFleet");
		
		float combatFP = 0;
		float civFP = 0;
		
		List<FleetMemberAPI> copy = fleet.getFleetData().getMembersListCopy();
		List<FleetMemberAPI> combat = new ArrayList<FleetMemberAPI>();
		//List<FleetMemberAPI> civ = new ArrayList<FleetMemberAPI>();
		List<FleetMemberAPI> tanker = new ArrayList<FleetMemberAPI>();
		List<FleetMemberAPI> freighter = new ArrayList<FleetMemberAPI>();
		List<FleetMemberAPI> liner = new ArrayList<FleetMemberAPI>();
		List<FleetMemberAPI> other = new ArrayList<FleetMemberAPI>();
		
		for (FleetMemberAPI member : copy) {
			if (member.isCivilian()) {
				civFP += member.getFleetPointCost();
				//civ.add(member);
				
				if (member.getHullSpec().getHints().contains(ShipTypeHints.FREIGHTER)) {
					freighter.add(member);
				} else if (member.getHullSpec().getHints().contains(ShipTypeHints.TANKER)) {
					tanker.add(member);
				} else if (member.getHullSpec().getHints().contains(ShipTypeHints.TRANSPORT) ||
						member.getHullSpec().getHints().contains(ShipTypeHints.LINER)) {
					liner.add(member);
				} else {
					other.add(member);
				}
				
			} else {
				combatFP += member.getFleetPointCost();
				combat.add(member);
			}
		}
		if (civFP < 1) civFP = 1;
		if (combatFP < 1) combatFP = 1;
		
		int keepCombat = (int) ((float)maxShips * combatFP / (civFP + combatFP));
		int keepCiv = maxShips - keepCombat;
		if (civFP > 10 && keepCiv < 2) {
			keepCiv = 2;
			if (!freighter.isEmpty()) keepCiv++;
			if (!tanker.isEmpty()) keepCiv++;
			if (!liner.isEmpty()) keepCiv++;
			if (!other.isEmpty()) keepCiv++;
			
			keepCiv = maxShips - keepCiv;
		}
		
		
		float f = 0, t = 0, l = 0, o = 0;
		float total = freighter.size() + tanker.size() + liner.size() + other.size();
		if (total < 1) total = 1;
		
		f = (float) freighter.size() / total;
		t = (float) tanker.size() / total;
		l = (float) liner.size() / total;
		o = (float) other.size() / total;
		
		if (f > 0) f = Math.round(f);
		if (t > 0) t = Math.round(t);
		if (l > 0) l = Math.round(l);
		if (o > 0) o = Math.round(o);
		
		if (freighter.size() > 0 && f < 1) f = 1;
		if (tanker.size() > 0 && t < 1) t = 1;
		if (liner.size() > 0 && l < 1) l = 1;
		if (other.size() > 0 && o < 1) o = 1;
		
		int extra = (int) ((f + t + l + o) - keepCiv);
		if (extra < 0) keepCombat += Math.abs(extra);
		if (extra > 0 && o >= 2) {
			extra--;
			o--;
		}
		if (extra > 0 && l >= 2) {
			extra--;
			l--;
		}
		if (extra > 0 && t >= 2) {
			extra--;
			t--;
		}
		if (extra > 0 && f >= 2) {
			extra--;
			f--;
		}
		
		
		LinkedHashSet<FleetMemberAPI> keep = new LinkedHashSet<FleetMemberAPI>();
		
		Comparator<FleetMemberAPI> c = new Comparator<FleetMemberAPI>() {
			public int compare(FleetMemberAPI o1, FleetMemberAPI o2) {
				return o2.getHullSpec().getHullSize().ordinal() - o1.getHullSpec().getHullSize().ordinal();
			}
		};
		Collections.sort(combat, c);
		Collections.sort(freighter, c);
		Collections.sort(tanker, c);
		Collections.sort(liner, c);
		Collections.sort(other, c);
		
		int [] ratio = new int [] { 4, 2, 1, 1 };
		//doctrineSize = 2;
//		if (doctrineSize == 4) {
//			ratio = new int [] { 3, 3, 1, 1 };
//		} else if (doctrineSize == 3) {
//			ratio = new int [] { 2, 3, 2, 1 };
//		} else if (doctrineSize <= 2) {
//			ratio = new int [] { 2, 3, 2, 1 };
//		}
		//ratio[3] = 0;
		//ratio = new int [] { 4, 0, 0, 0 };
		
		addAll(ratio, combat, keep, keepCombat, random);
		//addAll(ratio, civ, keep, keepCiv, random);
		
		addAll(ratio, freighter, keep, (int)f, random);
		addAll(ratio, tanker, keep, (int)t, random);
		addAll(ratio, liner, keep, (int)l, random);
		addAll(ratio, other, keep, (int)o, random); // adds a Hermes since that's "other" but we don't really care
		
		for (FleetMemberAPI member : copy) {
			if (!keep.contains(member)) {
				fleet.getFleetData().removeFleetMember(member);
			}
		}

		float currFP = getFP(fleet);
		if (currFP > targetFP) {
			fleet.getFleetData().sort();
			copy = fleet.getFleetData().getMembersListCopy();
			//Collections.reverse(copy);
			//Collections.shuffle(copy, random);
			for (int i = 0; i < copy.size()/2; i+=2) {
				FleetMemberAPI f1 = copy.get(i);
				FleetMemberAPI f2 = copy.get(copy.size() - 1 - i);
				copy.set(i, f2);
				copy.set(copy.size() - 1 - i, f1);
			}
			
			float fpGoal = currFP - targetFP;
			float fpDone = 0;
			for (FleetMemberAPI curr : copy) {
				if (curr.isCivilian()) continue;
				for (FleetMemberAPI replace : combat) {
					float fpCurr = curr.getFleetPointCost();
					float fpReplace = replace.getFleetPointCost();
					if (fpCurr > fpReplace) {
						fpDone += fpCurr - fpReplace;
						combat.remove(replace);
						fleet.getFleetData().removeFleetMember(curr);
						fleet.getFleetData().addFleetMember(replace);
						break;
					}
				}
				if (fpDone >= fpGoal) {
					break;
				}
			}	
		}
		
	}
	
	public static void addAll(int [] ratio, List<FleetMemberAPI> from, LinkedHashSet<FleetMemberAPI> to, int num, Random random) {
		int added = 0;
		if (num <= 5) {
			while (added < num && !from.isEmpty()) {
				to.add(from.remove(0));
				added++;
			}
			return;
		}
		
		WeightedRandomPicker<HullSize> picker = makePicker(ratio, random);
		for (int i = 0; i < num; i++) {
			if (picker.isEmpty()) picker = makePicker(ratio, random);
			HullSize size = picker.pickAndRemove();
			
			for (FleetMemberAPI member : from) {
				if (member.getHullSpec().getHullSize() == size) {
					to.add(member);
					from.remove(member);
					added++;
					break;
				}
			}
			
		}
		
		// if we failed to add up to num, add the largest ships until we've got num
		// assumes from list is sorted descending by size
		while (added < num && !from.isEmpty()) {
			to.add(from.remove(0));
			added++;
		}
		
	}
	
	public static WeightedRandomPicker<HullSize> makePicker(int [] ratio, Random random) {
		WeightedRandomPicker<HullSize> picker = new WeightedRandomPicker<HullSize>(random);
		for (int i = 0; i < ratio[0]; i++) {
			picker.add(HullSize.CAPITAL_SHIP);
		}
		for (int i = 0; i < ratio[1]; i++) {
			picker.add(HullSize.CRUISER);
		}
		for (int i = 0; i < ratio[2]; i++) {
			picker.add(HullSize.DESTROYER);
		}
		for (int i = 0; i < ratio[3]; i++) {
			picker.add(HullSize.FRIGATE);
		}
//		picker.add(HullSize.CAPITAL_SHIP, ratio[0]);
//		picker.add(HullSize.CRUISER, ratio[1]);
//		picker.add(HullSize.DESTROYER, ratio[2]);
//		picker.add(HullSize.FRIGATE, ratio[3]);
		return picker;	
	}
	
	
	public static int getFP(CampaignFleetAPI fleet) {
		int fp = 0;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			fp += member.getFleetPointCost();
		}
		return fp;
	}
	
	
	public static List<FleetMemberAPI> getRemoveOrder(CampaignFleetAPI fleet) {
		List<FleetMemberAPI> remove = new ArrayList<FleetMemberAPI>();
		List<FleetMemberAPI> copy = fleet.getFleetData().getMembersListCopy();
		
//		Collections.sort(copy, new Comparator<FleetMemberAPI>() {
//			public int compare(FleetMemberAPI o1, FleetMemberAPI o2) {
//				int f1 = o1.getFleetPointCost();
//				int f2 = o2.getFleetPointCost();
//				
//				if (!o1.isCivilian()) f1 *= 
//				return 0;
//			}
//		});
		
		Collections.reverse(copy);
		
		Iterator<FleetMemberAPI> iter;
		
		iter = copy.iterator();
		while (iter.hasNext()) {
			FleetMemberAPI member = iter.next();
			if (member.isCivilian() && member.getHullSpec().getHullSize().ordinal() <= HullSize.FRIGATE.ordinal()) {
				remove.add(member);
				iter.remove();
			}
		}
		
		iter = copy.iterator();
		while (iter.hasNext()) {
			FleetMemberAPI member = iter.next();
			if (!member.isCivilian() && member.getHullSpec().getHullSize().ordinal() <= HullSize.FRIGATE.ordinal()) {
				remove.add(member);
				iter.remove();
			}
		}
		
		iter = copy.iterator();
		while (iter.hasNext()) {
			FleetMemberAPI member = iter.next();
			if (member.isCivilian() && member.getHullSpec().getHullSize().ordinal() <= HullSize.DESTROYER.ordinal()) {
				remove.add(member);
				iter.remove();
			}
		}
		
		iter = copy.iterator();
		while (iter.hasNext()) {
			FleetMemberAPI member = iter.next();
			if (!member.isCivilian() && member.getHullSpec().getHullSize().ordinal() <= HullSize.DESTROYER.ordinal()) {
				remove.add(member);
				iter.remove();
			}
		}
		
		iter = copy.iterator();
		while (iter.hasNext()) {
			FleetMemberAPI member = iter.next();
			if (member.isCivilian() && member.getHullSpec().getHullSize().ordinal() <= HullSize.CRUISER.ordinal()) {
				remove.add(member);
				iter.remove();
			}
		}
		
		iter = copy.iterator();
		while (iter.hasNext()) {
			FleetMemberAPI member = iter.next();
			if (!member.isCivilian() && member.getHullSpec().getHullSize().ordinal() <= HullSize.CRUISER.ordinal()) {
				remove.add(member);
				iter.remove();
			}
		}
		
		iter = copy.iterator();
		while (iter.hasNext()) {
			FleetMemberAPI member = iter.next();
			if (member.isCivilian()) {
				remove.add(member);
				iter.remove();
			}
		}
		
		iter = copy.iterator();
		while (iter.hasNext()) {
			FleetMemberAPI member = iter.next();
			if (!member.isCivilian()) {
				remove.add(member);
				iter.remove();
			}
		}
		
		return remove;
	}
	
	public static void addCommanderAndOfficers(CampaignFleetAPI fleet, FleetParamsV3 params, Random random) {
		addCommanderAndOfficers(fleet, params, 0, random);
	}
	public static void addCommanderAndOfficers(CampaignFleetAPI fleet, FleetParamsV3 params, int extraOfficers, Random random) {
		OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
		int min = 5;
		int max = plugin.getMaxLevel(null);
		if (max > params.officerLevelLimit) max = params.officerLevelLimit;
		
		FactionAPI faction = fleet.getFaction();
		
		List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
		float combatPoints = 0f;
		for (FleetMemberAPI member : members) {
			if (member.isCivilian()) continue;
			combatPoints += member.getFleetPointCost();
		}
		
		boolean debug = true;
		debug = false;
		
		FactionDoctrineAPI doctrine = faction.getDoctrine();
		if (params.doctrineOverride != null) {
			doctrine = params.doctrineOverride;
		}
		
		float doctrineBonus = ((float) doctrine.getOfficerQuality() - 1f) * 0.25f;
		float fleetSizeBonus = combatPoints / 50f * 0.2f;
		if (fleetSizeBonus > 1f) fleetSizeBonus = 1f;
		
		float officerLevelValue = doctrineBonus * 0.7f + fleetSizeBonus * 0.3f;
		float commanderLevelValue = Math.max(officerLevelValue, doctrineBonus * 0.3f + fleetSizeBonus * 0.7f);
		
		if (debug) System.out.println("officerLevelValue: " + officerLevelValue);
		if (debug) System.out.println("commanderLevelValue: " + commanderLevelValue);
		
		int maxLevel = (int)(min + Math.round((float)(max - min) * officerLevelValue));
		maxLevel += params.officerLevelBonus;
		int minLevel = maxLevel - 4;
		
		if (maxLevel > max) maxLevel = max;
		if (minLevel > max) minLevel = max;
		
		if (minLevel < min) minLevel = min;
		if (maxLevel < min) maxLevel = min;
		
		
		
		WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<FleetMemberAPI>(random);
		WeightedRandomPicker<FleetMemberAPI> flagshipPicker = new WeightedRandomPicker<FleetMemberAPI>(random);
		
		int maxSize = 0;
		for (FleetMemberAPI member : members) {
			if (member.isFighterWing()) continue;
			if (member.isFlagship()) continue;
			if (!member.getCaptain().isDefault()) continue;
			int size = member.getHullSpec().getHullSize().ordinal();
			if (size > maxSize) {
				maxSize = size;
			}
		}
		for (FleetMemberAPI member : members) {
			if (member.isFighterWing()) continue;
			if (member.isFlagship()) continue;
			if (!member.getCaptain().isDefault()) continue;
			
			float q = 1f;
			if (member.isCivilian()) q *= 0.0001f;
			
			float weight = (float) member.getFleetPointCost() * q;
			int size = member.getHullSpec().getHullSize().ordinal();
			if (size >= maxSize) {
				flagshipPicker.add(member, weight);
				weight *= 1000f;
			}
			
			picker.add(member, weight);
		}
		
		
		int baseOfficers = Global.getSettings().getInt("baseNumOfficers");
		int numOfficersIncludingCommander = 1 + random.nextInt(baseOfficers + 1);
		
		
		boolean commander = true;
		for (int i = 0; i < numOfficersIncludingCommander; i++) {
			FleetMemberAPI member = null;
			
			if (commander) {
				member = flagshipPicker.pickAndRemove();
			}
			if (member == null) {
				member = picker.pickAndRemove();
			} else {
				picker.remove(member);
			}
			
			if (member == null) {
				break; // out of ships that need officers
			}
			
			int level = (int) Math.min(max, Math.round(minLevel + random.nextFloat() * (maxLevel - minLevel)));
			if (Misc.isEasy()) {
				 level = (int) Math.ceil((float) level * Global.getSettings().getFloat("easyOfficerLevelMult"));
			}
			
			if (commander && extraOfficers > 0) {
				level += extraOfficers;
				if (level > max) level = max;
				
			}
			
			if (level <= 0) continue;
			
			float weight = getMemberWeight(member);
			float fighters = member.getVariant().getFittedWings().size();
			boolean wantCarrierSkills = weight > 0 && fighters / weight >= 0.5f;
			SkillPickPreference pref = SkillPickPreference.NON_CARRIER;
			if (wantCarrierSkills) pref = SkillPickPreference.CARRIER;
			
			PersonAPI person = OfficerManagerEvent.createOfficer(fleet.getFaction(), level, true, pref, random);
			if (person.getPersonalityAPI().getId().equals(Personalities.TIMID)) {
				person.setPersonality(Personalities.CAUTIOUS);
			}
			
			if (commander) {
				if (params.commander != null) {
					person = params.commander;
				} else {
					addCommanderSkills(person, fleet, params, random);
				}
				person.setRankId(Ranks.SPACE_COMMANDER);
				person.setPostId(Ranks.POST_FLEET_COMMANDER);
				fleet.setCommander(person);
				fleet.getFleetData().setFlagship(member);
				commander = false;
				
				int officerNumLimit = person.getStats().getOfficerNumber().getModifiedInt();
				int aboveBase = officerNumLimit - baseOfficers + params.officerNumberBonus;
				
				aboveBase += extraOfficers;
				
				if (aboveBase < 0) aboveBase = 0;
				numOfficersIncludingCommander += aboveBase;
				
				numOfficersIncludingCommander *= params.officerNumberMult;
				if (numOfficersIncludingCommander < 1) numOfficersIncludingCommander = 1;
				
				
				int maxOfficers = Global.getSettings().getInt("maxOfficersInAIFleet") + 1;
				if (numOfficersIncludingCommander > maxOfficers) {
					maxLevel += (numOfficersIncludingCommander - maxOfficers) * 2;
					numOfficersIncludingCommander = maxOfficers;
					
					minLevel = maxLevel - 4;
					if (maxLevel > max) maxLevel = max;
					if (minLevel > max) minLevel = max;
					
					if (minLevel < min) minLevel = min;
					if (maxLevel < min) maxLevel = min;
				}
				
				if (debug) {
					System.out.println("Adding " + (aboveBase - extraOfficers) + " extra officers due to commander skill");
					if (extraOfficers > 0) {
						System.out.println("Adding " + extraOfficers + " extra officers due to fleet size");
					}
				}
			} else {
				member.setCaptain(person);
			}
		}
	}
	
	
	public static void addCommanderSkills(PersonAPI commander, CampaignFleetAPI fleet, FleetParamsV3 params, Random random) {
		if (random == null) random = new Random();
		
		FactionDoctrineAPI doctrine = fleet.getFaction().getDoctrine();
		if (params != null && params.doctrineOverride != null) {
			doctrine = params.doctrineOverride;
		}
		
		List<String> skills = new ArrayList<String>(doctrine.getCommanderSkills());
		if (skills.isEmpty()) return;
		
		if (random.nextFloat() < doctrine.getCommanderSkillsShuffleProbability()) {
			Collections.shuffle(skills, random);
		}

		OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
		int min = 5;
		int max = plugin.getMaxLevel(null);
		
		int maxPoints = 6;
		
		int points = (int)((float)maxPoints * ((float)commander.getStats().getLevel() - min) / (float)(max - min));
		if (points <= 0) return;
		
		MutableCharacterStatsAPI stats = commander.getStats();
		stats.setSkipRefresh(true);
		
		boolean debug = true;
		debug = false;
		if (debug) System.out.println("Generating commander skills, person level " + stats.getLevel() + ", points: " + points);
		for (String skillId : skills) {
			int spend = Math.min(points, 3);
			if (debug) System.out.println("Adding " + spend + " points to " + skillId);
			stats.setSkillLevel(skillId, spend);
			points -= spend;
			if (points <= 0) {
				break;
			}
		}
		if (debug) System.out.println("Done generating commander skills\n");
		
		stats.setSkipRefresh(false);
		stats.refreshCharacterStatsEffects();
		
	}
	
	
	public static float getMemberWeight(FleetMemberAPI member) {
		boolean nonCombat = member.getVariant().isCivilian();
		float weight = 0;
		switch (member.getVariant().getHullSize()) {
		case CAPITAL_SHIP: weight += 8; break;
		case CRUISER: weight += 4; break;
		case DESTROYER: weight += 2; break;
		case FRIGATE: weight += 1; break;
		case FIGHTER: weight += 1; break;
		}
		if (nonCombat) weight *= 0.1f;
		return weight;
	}

	
	
	
	public static MarketAPI pickMarket(FleetParamsV3 params) {
		if (params.source != null) return params.source;
		if (params.locInHyper == null) return null;
		
		List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
		
		int size = getMinPreferredMarketSize(params);
		float distToClosest = Float.MAX_VALUE;
		MarketAPI closest = null;
		float distToClosestMatchingSize = Float.MAX_VALUE;
		MarketAPI closestMatchingSize = null;
		
		
		for (MarketAPI market : allMarkets) {
			if (market.getPrimaryEntity() == null) continue;
			
			boolean independent = Factions.INDEPENDENT.equals(params.factionId) || Factions.SCAVENGERS.equals(params.factionId);
			if (independent) {
				boolean hostileToIndependent = market.getFaction().isHostileTo(Factions.INDEPENDENT);
				if (hostileToIndependent) continue;
			} else {
				if (!market.getFactionId().equals(params.factionId)) continue;
			}
			
			float currDist = Misc.getDistance(market.getPrimaryEntity().getLocationInHyperspace(),
											  params.locInHyper);
			if (currDist < distToClosest) {
				distToClosest = currDist;
				closest = market;
			}
			
			if (market.getSize() >= size && currDist < distToClosestMatchingSize) {
				distToClosestMatchingSize = currDist;
				closestMatchingSize = market;
			}
		}
		
		if (closestMatchingSize != null) {
			return closestMatchingSize;
		}
		
		if (closest != null) {
			return closest;
		}
		
//		MarketAPI temp = Global.getFactory().createMarket("temp", "Temp", size);
//		temp.setFactionId(params.factionId);
//		return temp;
		return null;
	}
	
	public static int getMinPreferredMarketSize(FleetParamsV3 params) {
		float fp = params.getTotalPts();
		
		if (fp <= 20) return 1;
		if (fp <= 50) return 3;
		if (fp <= 100) return 5;
		if (fp <= 150) return 7;
		
		return 8;
	}
	
	
	
	
	private static List<String> startingAbilities = null;
	public static CampaignFleetAPI createEmptyFleet(String factionId, String fleetType, MarketAPI market) {
		FactionAPI faction = Global.getSector().getFaction(factionId);
		String fleetName = faction.getFleetTypeName(fleetType); 
		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(factionId, fleetName, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_TYPE, fleetType);
		
		if (market != null && !market.getId().equals("fake")) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SOURCE_MARKET, market.getId());
		}
		
		if (startingAbilities == null) {
			startingAbilities = new ArrayList<String>();
			for (String id : Global.getSettings().getSortedAbilityIds()) {
				AbilitySpecAPI spec = Global.getSettings().getAbilitySpec(id);
				if (spec.isAIDefault()) {
					startingAbilities.add(id);
				}
			}
		}
		
		for (String id : startingAbilities) {
			fleet.addAbility(id);
		}
		
		return fleet;
	}

	public static class FPRemaining {
		public int fp;

		public FPRemaining(int fp) {
			this.fp = fp;
		}
		public FPRemaining() {
		}
	}
	
	public static float addToFleet(String role, MarketAPI market, Random random, CampaignFleetAPI fleet, int maxFP, FleetParamsV3 params) {
		float total = 0f;
		List<ShipRolePick> picks = market.pickShipsForRole(role, fleet.getFaction().getId(), 
					new ShipPickParams(params.mode, maxFP, params.timestamp, params.blockFallback), random, null);
		for (ShipRolePick pick : picks) {
			total += addToFleet(pick, fleet, random);
		}
		return total;
	}
	
	protected static float addToFleet(ShipRolePick pick, CampaignFleetAPI fleet, Random random) {
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, pick.variantId);
		String name = fleet.getFleetData().pickShipName(member, random);
		member.setShipName(name);
		fleet.getFleetData().addFleetMember(member);
		return member.getFleetPointCost();
	}
	
//	public static float addCombatFleetPoints(CampaignFleetAPI fleet, Random random, 
//													float fp, FleetParamsV3 params) {
//		FactionDoctrineAPI doctrine = fleet.getFaction().getDoctrine();
//		if (params.doctrineOverride != null) {
//			doctrine = params.doctrineOverride;
//		}
//		
//		int size = doctrine.getShipSize();
//		
//		boolean addedSomething = true;
//		FPRemaining rem = new FPRemaining();
//		rem.fp = (int) fp;
//		
//		String smallRole = ShipRoles.COMBAT_SMALL_FOR_SMALL_FLEET;
//		if (!params.banPhaseShipsEtc) {
//			smallRole = ShipRoles.COMBAT_SMALL;
//		}
//		
//		while (addedSomething && rem.fp > 0) {
//			int small = BASE_COUNTS_WITH_4[size - 1][0] + random.nextInt(MAX_EXTRA_WITH_4[size - 1][0] + 1); 
//			int medium = BASE_COUNTS_WITH_4[size - 1][1] + random.nextInt(MAX_EXTRA_WITH_4[size - 1][1] + 1); 
//			int large = BASE_COUNTS_WITH_4[size - 1][2] + random.nextInt(MAX_EXTRA_WITH_4[size - 1][2] + 1); 
//			int capital = BASE_COUNTS_WITH_4[size - 1][3] + random.nextInt(MAX_EXTRA_WITH_4[size - 1][3] + 1); 
//			
////			System.out.println(String.format("Small: %s Medium: %s Large: %s Capital: %s",
////					"" + small, "" + medium, "" + large, "" + capital));
//			
//			if (params.maxShipSize <= 1) medium = 0;
//			if (params.maxShipSize <= 2) large = 0;
//			if (params.maxShipSize <= 3) capital = 0;
//			
//			int smallPre = small / 2;
//			small -= smallPre;
//			
//			int mediumPre = medium / 2;
//			medium -= mediumPre;
//			
//			addedSomething = false;
//			
//			addedSomething |= addShips(smallRole, smallPre, params.source, random, fleet, rem, params);
//			
//			addedSomething |= addShips(ShipRoles.COMBAT_MEDIUM, mediumPre, params.source, random, fleet, rem, params);
//			addedSomething |= addShips(smallRole, small, params.source, random, fleet, rem, params);
//			
//			addedSomething |= addShips(ShipRoles.COMBAT_LARGE, large, params.source, random, fleet, rem, params);
//			addedSomething |= addShips(ShipRoles.COMBAT_MEDIUM, medium, params.source, random, fleet, rem, params);
//			
//			addedSomething |= addShips(ShipRoles.COMBAT_CAPITAL, capital, params.source, random, fleet, rem, params);
//		}
//		
//		return fp - rem.fp;
//	}
	
	public static boolean addShips(String role, int count, MarketAPI market, Random random, CampaignFleetAPI fleet, FPRemaining rem, FleetParamsV3 params) {
		boolean addedSomething = false;
		for (int i = 0; i < count; i++) {
			if (rem.fp <= 0) break;
			float added = addToFleet(role, market, random, fleet, rem.fp, params);
			if (added > 0) {
				rem.fp -= added;
				addedSomething = true;
			}
		}
		return addedSomething;
	}

	
	public static float addPhaseFleetPoints(CampaignFleetAPI fleet, Random random, float fp, FleetParamsV3 params) {
		return addPriorityOnlyThenAll(fleet, random, fp, params, SizeFilterMode.SMALL_IS_FRIGATE,
							ShipRoles.PHASE_SMALL, ShipRoles.PHASE_MEDIUM, ShipRoles.PHASE_LARGE);
//		FactionDoctrineAPI doctrine = fleet.getFaction().getDoctrine();
//		if (params.doctrineOverride != null) {
//			doctrine = params.doctrineOverride;
//		}
//
//		int size = doctrine.getShipSize();
//
//		boolean addedSomething = true;
//		FPRemaining rem = new FPRemaining();
//		rem.fp = (int) fp;
//
//		while (addedSomething && rem.fp > 0) {
//			int small = BASE_COUNTS_WITH_3[size - 1][0] + random.nextInt(MAX_EXTRA_WITH_3[size - 1][0] + 1); 
//			int medium = BASE_COUNTS_WITH_3[size - 1][1] + random.nextInt(MAX_EXTRA_WITH_3[size - 1][1] + 1); 
//			int large = BASE_COUNTS_WITH_3[size - 1][2] + random.nextInt(MAX_EXTRA_WITH_3[size - 1][2] + 1); 
//
//			//System.out.println(String.format("Small: %s Medium: %s Large: %s Capital: %s",
//			//"" + small, "" + medium, "" + large, "" + capital));
//
//			if (params.maxShipSize <= 1) medium = 0;
//			if (params.maxShipSize <= 2) large = 0;
//
//			int smallPre = small / 2;
//			small -= smallPre;
//
//			int mediumPre = medium / 2;
//			medium -= mediumPre;
//
//			addedSomething = false;
//
//			addedSomething |= addShips(ShipRoles.PHASE_SMALL, smallPre, params.source, random, fleet, rem, params);
//
//			addedSomething |= addShips(ShipRoles.PHASE_MEDIUM, mediumPre, params.source, random, fleet, rem, params);
//			addedSomething |= addShips(ShipRoles.PHASE_SMALL, small, params.source, random, fleet, rem, params);
//
//			addedSomething |= addShips(ShipRoles.PHASE_LARGE, large, params.source, random, fleet, rem, params);
//			addedSomething |= addShips(ShipRoles.PHASE_MEDIUM, medium, params.source, random, fleet, rem, params);
//		}
//
//		return fp - rem.fp;
	}
	
	public static enum SizeFilterMode {
		NONE,
		SMALL_IS_FRIGATE,
		SMALL_IS_DESTROYER,
	}
	public static float addCarrierFleetPoints(CampaignFleetAPI fleet, Random random, float fp, FleetParamsV3 params) {
		return addPriorityOnlyThenAll(fleet, random, fp, params, SizeFilterMode.SMALL_IS_DESTROYER,
						ShipRoles.CARRIER_SMALL, ShipRoles.CARRIER_MEDIUM, ShipRoles.CARRIER_LARGE);
	}
	public static float addPriorityOnlyThenAll(CampaignFleetAPI fleet, Random random, float fp, FleetParamsV3 params,
												SizeFilterMode sizeFilterMode,
												String roleSmall, String roleMedium, String roleLarge) {
		if (fp <= 0) return 0f;
		
		float added = 0f;
		if (params.mode == ShipPickMode.PRIORITY_THEN_ALL) {
			int numPriority = fleet.getFaction().getNumAvailableForRole(roleSmall, ShipPickMode.PRIORITY_ONLY) +
						      fleet.getFaction().getNumAvailableForRole(roleMedium, ShipPickMode.PRIORITY_ONLY) + 
						      fleet.getFaction().getNumAvailableForRole(roleLarge, ShipPickMode.PRIORITY_ONLY);
		
			if (numPriority > 0) {
				params.mode = ShipPickMode.PRIORITY_ONLY;
				added = addFleetPoints(fleet, random, fp, params, sizeFilterMode,
						roleSmall, roleMedium, roleLarge);
				params.mode = ShipPickMode.PRIORITY_THEN_ALL;
			} else {
				params.mode = ShipPickMode.ALL;
				added = addFleetPoints(fleet, random, fp, params, sizeFilterMode,
						roleSmall, roleMedium, roleLarge);
				params.mode = ShipPickMode.PRIORITY_THEN_ALL;
			}
			// if there ARE priority ships for a 3-type category (i.e. carriers/phases/various civs,
			// then ONLY use priority, and use nothing if a priority ship was not added (since that just means not enough FP 
			// for likely a smaller fleet.)
//			if (added <= 0) {
//				added = addFleetPoints(fleet, random, fp, params, sizeFilterMode,
//						roleSmall, roleMedium, roleLarge);
//			}
		} else {		
			added = addFleetPoints(fleet, random, fp, params, sizeFilterMode,
					roleSmall, roleMedium, roleLarge);
		}
		return added;
	}
	
	public static float addTankerFleetPoints(CampaignFleetAPI fleet, Random random, float fp, FleetParamsV3 params) {
		return addPriorityOnlyThenAll(fleet, random, fp, params, SizeFilterMode.SMALL_IS_DESTROYER,
				ShipRoles.TANKER_SMALL, ShipRoles.TANKER_MEDIUM, ShipRoles.TANKER_LARGE);
	}
	
	public static float addFreighterFleetPoints(CampaignFleetAPI fleet, Random random, float fp, FleetParamsV3 params) {
		return addPriorityOnlyThenAll(fleet, random, fp, params, SizeFilterMode.NONE, 
				ShipRoles.FREIGHTER_SMALL, ShipRoles.FREIGHTER_MEDIUM, ShipRoles.FREIGHTER_LARGE);
	}
	
	public static float addLinerFleetPoints(CampaignFleetAPI fleet, Random random, float fp, FleetParamsV3 params) {
		return addPriorityOnlyThenAll(fleet, random, fp, params, SizeFilterMode.NONE, 
				ShipRoles.LINER_SMALL, ShipRoles.LINER_MEDIUM, ShipRoles.LINER_LARGE);
	}
	
	public static float addCombatFreighterFleetPoints(CampaignFleetAPI fleet, Random random, float fp, FleetParamsV3 params) {
		return addPriorityOnlyThenAll(fleet, random, fp, params, SizeFilterMode.SMALL_IS_FRIGATE, 
				ShipRoles.COMBAT_FREIGHTER_SMALL, ShipRoles.COMBAT_FREIGHTER_MEDIUM, ShipRoles.COMBAT_FREIGHTER_LARGE);
	}
	
	public static float addTransportFleetPoints(CampaignFleetAPI fleet, Random random, float fp, FleetParamsV3 params) {
		return addPriorityOnlyThenAll(fleet, random, fp, params, SizeFilterMode.NONE,
				ShipRoles.PERSONNEL_SMALL, ShipRoles.PERSONNEL_MEDIUM, ShipRoles.PERSONNEL_LARGE);
	}
	
	public static float addUtilityFleetPoints(CampaignFleetAPI fleet, Random random, float fp, FleetParamsV3 params) {
		return addPriorityOnlyThenAll(fleet, random, fp, params, SizeFilterMode.NONE,
				ShipRoles.UTILITY, ShipRoles.UTILITY, ShipRoles.UTILITY);
	}

	
	protected static int sizeOverride = 0;
	// tend towards larger ships as fleets get more members, regardless of doctrine
	public static int getAdjustedDoctrineSize(int size, CampaignFleetAPI fleetSoFar) {
		if (sizeOverride > 0) return sizeOverride;
		else return size;
		
//		int num = fleetSoFar.getNumMembersFast();
//		if (num > 8 && size <= 2) {
//			size++;
//		}
//		if (num > 14 && size <= 3) {
//			size++;
//		}
//		if (num > 20 && size <= 4) {
//			size++;
//		}
//		if (size > 5) size = 5;
//		return size;
	}

	
	public static float addFleetPoints(CampaignFleetAPI fleet, Random random, float fp, FleetParamsV3 params,
									   SizeFilterMode sizeFilterMode,
										String ... roles) {
		FactionDoctrineAPI doctrine = fleet.getFaction().getDoctrine();
		if (params.doctrineOverride != null) {
			doctrine = params.doctrineOverride;
		}

		int size = doctrine.getShipSize();
		//size = getAdjustedDoctrineSize(size, fleet);

		boolean addedSomething = true;
		FPRemaining rem = new FPRemaining();
		rem.fp = (int) fp;

		while (addedSomething && rem.fp > 0) {
			size = getAdjustedDoctrineSize(size, fleet);
			
			int small = BASE_COUNTS_WITH_3[size - 1][0] + random.nextInt(MAX_EXTRA_WITH_3[size - 1][0] + 1); 
			int medium = BASE_COUNTS_WITH_3[size - 1][1] + random.nextInt(MAX_EXTRA_WITH_3[size - 1][1] + 1); 
			int large = BASE_COUNTS_WITH_3[size - 1][2] + random.nextInt(MAX_EXTRA_WITH_3[size - 1][2] + 1); 

//			if (sizeOverride > 0) {
//				small = 0;
//				medium = 0;
//			}
			
			if (sizeFilterMode == SizeFilterMode.SMALL_IS_FRIGATE) {
				if (params.maxShipSize <= 1) medium = 0;
				if (params.maxShipSize <= 2) large = 0;
			} else if (sizeFilterMode == SizeFilterMode.SMALL_IS_DESTROYER) {
				if (params.maxShipSize <= 2) medium = 0;
				if (params.maxShipSize <= 3) large = 0;
			}
			
			//System.out.println(String.format("Small: %s Medium: %s Large: %s Capital: %s",
			//"" + small, "" + medium, "" + large, "" + capital));

			int smallPre = small / 2;
			small -= smallPre;

			int mediumPre = medium / 2;
			medium -= mediumPre;

			addedSomething = false;

			addedSomething |= addShips(roles[0], smallPre, params.source, random, fleet, rem, params);

			addedSomething |= addShips(roles[1], mediumPre, params.source, random, fleet, rem, params);
			addedSomething |= addShips(roles[0], small, params.source, random, fleet, rem, params);

			addedSomething |= addShips(roles[2], large, params.source, random, fleet, rem, params);
			addedSomething |= addShips(roles[1], medium, params.source, random, fleet, rem, params);
		}

		return fp - rem.fp;
	}
	
	
	
	
	
	public static void addCombatFleetPoints(CampaignFleetAPI fleet, Random random,
			float warshipFP, float carrierFP, float phaseFP, FleetParamsV3 params) {
		
		FactionAPI faction = fleet.getFaction();
		FactionDoctrineAPI doctrine = faction.getDoctrine();
		if (params.doctrineOverride != null) {
			doctrine = params.doctrineOverride;
		}

		WeightedRandomPicker<String> smallPicker = new WeightedRandomPicker<String>(random);
		WeightedRandomPicker<String> mediumPicker = new WeightedRandomPicker<String>(random);
		WeightedRandomPicker<String> largePicker = new WeightedRandomPicker<String>(random);
		WeightedRandomPicker<String> capitalPicker = new WeightedRandomPicker<String>(random);
		WeightedRandomPicker<String> priorityCapitalPicker = new WeightedRandomPicker<String>(random);
		
		String smallRole = ShipRoles.COMBAT_SMALL_FOR_SMALL_FLEET;
		if (!params.banPhaseShipsEtc) {
			smallRole = ShipRoles.COMBAT_SMALL;
		}
		
//		if (warshipFP > 0) smallPicker.add(smallRole, 1);
//		if (phaseFP > 0) smallPicker.add(ShipRoles.PHASE_SMALL, 1);
//		
//		if (warshipFP > 0) mediumPicker.add(ShipRoles.COMBAT_MEDIUM, 1);
//		if (phaseFP > 0) mediumPicker.add(ShipRoles.PHASE_MEDIUM, 1);
//		if (carrierFP > 0) mediumPicker.add(ShipRoles.CARRIER_SMALL, 1);
//		
//		if (warshipFP > 0) largePicker.add(ShipRoles.COMBAT_LARGE, 1);
//		if (phaseFP > 0) largePicker.add(ShipRoles.PHASE_LARGE, 1);
//		if (carrierFP > 0) largePicker.add(ShipRoles.CARRIER_MEDIUM, 1);
//		
//		if (warshipFP > 0) capitalPicker.add(ShipRoles.COMBAT_CAPITAL, 1);
//		if (phaseFP > 0) capitalPicker.add(ShipRoles.PHASE_CAPITAL, 1);
//		if (carrierFP > 0) capitalPicker.add(ShipRoles.CARRIER_LARGE, 1);
		
		smallPicker.add(smallRole, warshipFP);
		smallPicker.add(ShipRoles.PHASE_SMALL, phaseFP);
		
		mediumPicker.add(ShipRoles.COMBAT_MEDIUM, warshipFP);
		mediumPicker.add(ShipRoles.PHASE_MEDIUM, phaseFP);
		mediumPicker.add(ShipRoles.CARRIER_SMALL, carrierFP);
		
		largePicker.add(ShipRoles.COMBAT_LARGE, warshipFP);
		largePicker.add(ShipRoles.PHASE_LARGE, phaseFP);
		largePicker.add(ShipRoles.CARRIER_MEDIUM, carrierFP);
		
		capitalPicker.add(ShipRoles.COMBAT_CAPITAL, warshipFP);
		capitalPicker.add(ShipRoles.PHASE_CAPITAL, phaseFP);
		capitalPicker.add(ShipRoles.CARRIER_LARGE, carrierFP);
		
		
		Set<String> usePriorityOnly = new HashSet<String>();
		
		if (params.mode == ShipPickMode.PRIORITY_THEN_ALL) {
			if (faction.getNumAvailableForRole(ShipRoles.COMBAT_CAPITAL, ShipPickMode.PRIORITY_ONLY) > 0) {
				priorityCapitalPicker.add(ShipRoles.COMBAT_CAPITAL, doctrine.getWarships());
			}
			if (faction.getNumAvailableForRole(ShipRoles.CARRIER_LARGE, ShipPickMode.PRIORITY_ONLY) > 0) {
				priorityCapitalPicker.add(ShipRoles.CARRIER_LARGE, doctrine.getCarriers());
			}
			if (faction.getNumAvailableForRole(ShipRoles.PHASE_CAPITAL, ShipPickMode.PRIORITY_ONLY) > 0) {
				priorityCapitalPicker.add(ShipRoles.PHASE_CAPITAL, doctrine.getPhaseShips());
			}
			
			if (params.mode == ShipPickMode.PRIORITY_THEN_ALL) {
				addToPriorityOnlySet(fleet, usePriorityOnly, ShipRoles.PHASE_SMALL, ShipRoles.PHASE_MEDIUM, ShipRoles.PHASE_LARGE);
				addToPriorityOnlySet(fleet, usePriorityOnly, ShipRoles.CARRIER_SMALL, ShipRoles.CARRIER_MEDIUM, ShipRoles.CARRIER_LARGE);
			}
		}
		
		Map<String, FPRemaining> remaining = new HashMap<String, FPRemaining>();
		FPRemaining remWarship = new FPRemaining((int)warshipFP);
		FPRemaining remCarrier = new FPRemaining((int)carrierFP);
		FPRemaining remPhase = new FPRemaining((int)phaseFP);
		
		remaining.put(ShipRoles.COMBAT_SMALL_FOR_SMALL_FLEET, remWarship);
		remaining.put(ShipRoles.COMBAT_SMALL, remWarship);
		remaining.put(ShipRoles.COMBAT_MEDIUM, remWarship);
		remaining.put(ShipRoles.COMBAT_LARGE, remWarship);
		remaining.put(ShipRoles.COMBAT_CAPITAL, remWarship);
		
		remaining.put(ShipRoles.CARRIER_SMALL, remCarrier);
		remaining.put(ShipRoles.CARRIER_MEDIUM, remCarrier);
		remaining.put(ShipRoles.CARRIER_LARGE, remCarrier);
		
		remaining.put(ShipRoles.PHASE_SMALL, remPhase);
		remaining.put(ShipRoles.PHASE_MEDIUM, remPhase);
		remaining.put(ShipRoles.PHASE_LARGE, remPhase);
		remaining.put(ShipRoles.PHASE_CAPITAL, remPhase);
		
		
		if (params.maxShipSize <= 1) {
			mediumPicker.clear();
		}
		if (params.maxShipSize <= 2) {
			largePicker.clear();
		}
		if (params.maxShipSize <= 3) {
			capitalPicker.clear();
		}
		
		
		int size = doctrine.getShipSize();
		//size = getAdjustedDoctrineSize(size, fleet);

		int numFails = 0;
		while (numFails < 2) {
			size = getAdjustedDoctrineSize(size, fleet);
			
//			if (size > 5) {
//				System.out.println("wefwefe");
//			}
			
			int small = BASE_COUNTS_WITH_4[size - 1][0] + random.nextInt(MAX_EXTRA_WITH_4[size - 1][0] + 1); 
			int medium = BASE_COUNTS_WITH_4[size - 1][1] + random.nextInt(MAX_EXTRA_WITH_4[size - 1][1] + 1); 
			int large = BASE_COUNTS_WITH_4[size - 1][2] + random.nextInt(MAX_EXTRA_WITH_4[size - 1][2] + 1); 
			int capital = BASE_COUNTS_WITH_4[size - 1][3] + random.nextInt(MAX_EXTRA_WITH_4[size - 1][3] + 1); 

			if (size < 5 && capital > 1) {
				capital = 1;
			}
			
			if (params.maxShipSize <= 1) medium = 0;
			if (params.maxShipSize <= 2) large = 0;
			if (params.maxShipSize <= 3) capital = 0;

			int smallPre = small / 2;
			small -= smallPre;

			int mediumPre = medium / 2;
			medium -= mediumPre;

			boolean addedSomething = false;

			//System.out.println("Rem carrier pre: " + remCarrier.fp);
			addedSomething |= addShips(smallPicker, usePriorityOnly, remaining, null, smallPre, fleet, random, params);
			//System.out.println("Rem carrier after smallPre: " + remCarrier.fp);
			addedSomething |= addShips(mediumPicker, usePriorityOnly, remaining, null, mediumPre, fleet, random, params);
			//System.out.println("Rem carrier after mediumPre: " + remCarrier.fp);
			addedSomething |= addShips(smallPicker, usePriorityOnly, remaining, null, small, fleet, random, params);
			//System.out.println("Rem carrier after small: " + remCarrier.fp);
			addedSomething |= addShips(largePicker, usePriorityOnly, remaining, null, large, fleet, random, params);
			//System.out.println("Rem carrier after large: " + remCarrier.fp);
			addedSomething |= addShips(mediumPicker, usePriorityOnly, remaining, null, medium, fleet, random, params);
			//System.out.println("Rem carrier after medium: " + remCarrier.fp);
			
			
			if (!priorityCapitalPicker.isEmpty()) {
				params.mode = ShipPickMode.PRIORITY_ONLY;
				params.blockFallback = true;
				FPRemaining combined = new FPRemaining(remWarship.fp + remCarrier.fp + remPhase.fp);
				boolean addedCapital = addShips(priorityCapitalPicker, usePriorityOnly, remaining, combined, capital, fleet, random, params);
				addedSomething |= addedCapital;
				if (addedCapital) {
					redistributeFP(remWarship, remCarrier, remPhase, combined.fp);
				}
				params.mode = ShipPickMode.PRIORITY_THEN_ALL;
				params.blockFallback = null;
				//System.out.println("Rem carrier after capitals priority: " + remCarrier.fp);
			} else {
				addedSomething |= addShips(capitalPicker, usePriorityOnly, remaining, null, capital, fleet, random, params);
				//System.out.println("Rem carrier after capitals normal: " + remCarrier.fp);
			}
			
			if (!addedSomething) {
				numFails++;
				
				if (numFails == 2) {
					boolean goAgain = false;
					if (remPhase.fp > 0) {
						remWarship.fp += remPhase.fp;
						remPhase.fp = 0;
						goAgain = true;
					}
					if (remCarrier.fp > 0) {
						remWarship.fp += remCarrier.fp;
						remCarrier.fp = 0;
						goAgain = true;
					}
					
					if (goAgain) {
						numFails = 0;
						smallPicker.add(smallRole, 1);
						mediumPicker.add(ShipRoles.COMBAT_MEDIUM, 1);
						largePicker.add(ShipRoles.COMBAT_LARGE, 1);
						capitalPicker.add(ShipRoles.COMBAT_CAPITAL, 1);	
					}
				}
			}
		}
	}
			
	protected static void addToPriorityOnlySet(CampaignFleetAPI fleet, Set<String> set, String small, String medium, String large) {
		int numPriority = fleet.getFaction().getNumAvailableForRole(small, ShipPickMode.PRIORITY_ONLY) +
	      				  fleet.getFaction().getNumAvailableForRole(medium, ShipPickMode.PRIORITY_ONLY) + 
	      				  fleet.getFaction().getNumAvailableForRole(large, ShipPickMode.PRIORITY_ONLY);
		if (numPriority > 0) {
			set.add(small);
			set.add(medium);
			set.add(large);
		}
	}
	
	protected static void redistributeFP(FPRemaining one, FPRemaining two, FPRemaining three, int newTotal) {
		float total = one.fp + two.fp + three.fp;
		if (total <= 0) return;
		
		int f1 = (int) Math.round((float)one.fp / total * newTotal);
		int f2 = (int) Math.round((float)two.fp / total * newTotal);
		int f3 = (int) Math.round((float)three.fp / total * newTotal);
		
		f1 += newTotal - f1 - f2 - f3;
		
		one.fp = f1;
		two.fp = f2;
		three.fp = f3;
	}
	
	public static boolean addShips(WeightedRandomPicker<String> rolePicker, Set<String> usePriorityOnly, Map<String, FPRemaining> remaining, FPRemaining remOverride, int count,
								   CampaignFleetAPI fleet, Random random, FleetParamsV3 params) {
		if (rolePicker.isEmpty()) return false;
		
		boolean addedSomething = false;
		for (int i = 0; i < count; i++) {
			String role = rolePicker.pick();
			if (role == null) break;
			FPRemaining rem = remaining.get(role);
			FPRemaining remForProperRole = rem;
			if (remOverride != null) rem = remOverride;
			if (usePriorityOnly.contains(role)) {
				params.mode = ShipPickMode.PRIORITY_ONLY;
			}
			int fpPrePick = rem.fp;
			
			boolean added = addShips(role, 1, params.source, random, fleet, rem, params);
			
			if (added && remOverride != null) {
				int fpSpent = fpPrePick - rem.fp;
				int maxToTakeFromProperRole = Math.min(remForProperRole.fp, fpSpent);
				remForProperRole.fp -= maxToTakeFromProperRole;
			}
			
			if (usePriorityOnly.contains(role)) {
				params.mode = ShipPickMode.PRIORITY_THEN_ALL;
			}
			if (!added) {
				rolePicker.remove(role);
				i--;
				if (rolePicker.isEmpty()) {
					break;
				}
			}
			addedSomething |= added;
		}
		return addedSomething;
	}
	
	public static float getShipDeficitFleetSizeMult(MarketAPI market) {
		float mult = 1f;
		CommodityOnMarketAPI com = market.getCommodityData(Commodities.SHIPS);
		float available = com.getAvailable();
		float demand = com.getMaxDemand();
		if (demand > 0) {
			float f = available / demand;
			if (f < MIN_NUM_SHIPS_DEFICIT_MULT) f = MIN_NUM_SHIPS_DEFICIT_MULT;
			mult *= f;
		}
		if (mult < 0) mult = 0;
		if (mult > 1) mult = 1;
		return mult;
	}
	
	
	
	public static void addCommanderSkills(PersonAPI commander, CampaignFleetAPI fleet, Random random) {
		addCommanderSkills(commander, fleet, null, random);
	}
}









