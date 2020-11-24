package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetStubAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.RollingAverageTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class PatrolFleetManagerV2 extends BaseCampaignEventListener implements 
						EveryFrameScript, FleetEventListener {

	public static Logger log = Global.getLogger(PatrolFleetManagerV2.class);
	
	public static class PatrolFleetData {
		public float startingFleetPoints = 0;
		public FleetStubAPI stub;
		public PatrolType type;
		public MarketAPI sourceMarket;
		public PatrolFleetData(FleetStubAPI fleet, PatrolType type) {
			this.stub = fleet;
			this.type = type;
		}
	}
	
	//private float econInterval = Global.getSettings().getFloat("economyIntervalnGameDays");
	
	private MarketAPI market;
	private List<PatrolFleetData> activePatrols = new ArrayList<PatrolFleetData>();
	private IntervalUtil tracker;
	private int maxPatrols;

	private RollingAverageTracker patrolBattlesLost;
	public PatrolFleetManagerV2(MarketAPI market) {
		super(true);
		this.market = market;
		
		float interval = Global.getSettings().getFloat("averagePatrolSpawnInterval");
		tracker = new IntervalUtil(interval * 0.75f, interval * 1.25f);
		
		readResolve();
	}
	
	protected Object readResolve() {
		if (patrolBattlesLost == null) {
			float patrolStrengthCheckInterval = Global.getSettings().getFloat("economyIntervalnGameDays");
			float min = patrolStrengthCheckInterval - Math.min(patrolStrengthCheckInterval * 0.5f, 2f);
			float max = patrolStrengthCheckInterval + Math.min(patrolStrengthCheckInterval * 0.5f, 2f);
			patrolBattlesLost = new RollingAverageTracker(min, max, Misc.getGenericRollingAverageFactor());
		}
		return this;
	}
	
	public void advance(float amount) {
		//if (true) return;
		float days = Global.getSector().getClock().convertToDays(amount);
		
		patrolBattlesLost.advance(days);
		
		float losses = patrolBattlesLost.getAverage();
		
		//tracker.advance(days);
		tracker.advance(days * Math.max(1f, losses));
		if (!tracker.intervalElapsed()) return;
		
		if (market.hasCondition(Conditions.DECIVILIZED)) return;
		
//		if (market.getId().equals("jangala")) {
//			System.out.println("fwefwefe");
//		}
		
		List<PatrolFleetData> remove = new ArrayList<PatrolFleetData>();
		for (PatrolFleetData data : activePatrols) {
			if (data.stub.getContainingLocation() == null ||
				(!data.stub.getContainingLocation().getFleets().contains(data.stub.getFleet()) &&
				!data.stub.getContainingLocation().getFleetStubs().contains(data.stub))) {
				remove.add(data);
				log.info("Cleaning up orphaned patrol [" + data.stub.getId() + "] for market [" + market.getName() + "]");
			}
		}
		activePatrols.removeAll(remove);
		
//		if (market.getId().equals("jangala")) {
//			System.out.println("23rwefwe");
//		}
		//maxPatrols = Math.max(1, market.getSize() - 3) + (int) (market.getStabilityValue() * 0.5f);
		//float losses = patrolBattlesLost.getAverage();
		
		maxPatrols = (int) (Math.max(1, market.getSize() - 3) * (market.getStabilityValue() / 10f)) + 
					(int) Math.max(0, Math.min(losses, 5));
		if (maxPatrols < 1) maxPatrols = 1;
		
		boolean hasStationOrSpaceport = market.hasIndustry(Industries.ORBITALSTATION) ||
										market.hasSpaceport() ||
										market.hasIndustry(Industries.BATTLESTATION);
		if (market.hasIndustry(Industries.MILITARYBASE)) {
			maxPatrols += 1;
			if (hasStationOrSpaceport) maxPatrols++;
		}
		if (hasStationOrSpaceport) maxPatrols++;
//		if (market.getId().equals("jangala")) {
//			System.out.println("fwefewew");
//		}
		
		log.debug("");
		log.debug("Checking whether to spawn patrol for market [" + market.getName() + "]");
		if (activePatrols.size() < maxPatrols) {
			log.info(activePatrols.size() + " out of a maximum " + maxPatrols + " patrols in play for market [" + market.getName() + "]");

			WeightedRandomPicker<PatrolType> picker = new WeightedRandomPicker<PatrolType>();
			picker.add(PatrolType.FAST, 
					Math.max(1, maxPatrols - getCount(PatrolType.COMBAT, PatrolType.HEAVY)));
			picker.add(PatrolType.COMBAT, 
					Math.max(1, maxPatrols - getCount(PatrolType.FAST, PatrolType.HEAVY) + market.getSize()) + losses * 0.5f);
			
			if (market.getSize() >= 5) {
				picker.add(PatrolType.HEAVY, 
						Math.max(1, maxPatrols - getCount(PatrolType.FAST, PatrolType.COMBAT) + market.getSize() - 5) + losses);
			}
			
			
			PatrolType type = picker.pick();
			
			float combat = 0f;
			float tanker = 0f;
			float freighter = 0f;
			String fleetType = FleetTypes.PATROL_SMALL;
			switch (type) {
			case FAST:
				fleetType = FleetTypes.PATROL_SMALL;
				combat = Math.round(3f + (float) Math.random() * 2f);
				combat += Math.min(5f, losses * 2f);
				break;
			case COMBAT:
				fleetType = FleetTypes.PATROL_MEDIUM;
				combat = Math.round(6f + (float) Math.random() * 3f);
				combat += Math.min(15f, losses * 4f);
				
				tanker = Math.round((float) Math.random());
				break;
			case HEAVY:
				fleetType = FleetTypes.PATROL_LARGE;
				combat = Math.round(10f + (float) Math.random() * 5f);
				combat += Math.min(25f, losses * 6f);
				
				tanker = 2f;
				freighter = 2f;
				break;
			}
			combat *= 1f + (market.getStabilityValue() / 20f);
			//combat += Math.min(30f, losses * 3f);

			//CampaignFleetAPI fleet = FleetFactoryV2.createFleet(new FleetParams(
					
			SectorEntityToken entity = market.getPrimaryEntity();
			//FleetOrStubAPI fleet = FleetFactoryV2.createFleetOrStub(new FleetParams(
			FleetStubAPI stub = null;
//				FleetFactoryV2.createStub(new FleetParams(
//					null,
//					market, 
//					market.getFactionId(),
//					null, // fleet's faction, if different from above, which is also used for source market picking
//					fleetType,
//					combat, // combatPts
//					freighter, // freighterPts 
//					tanker, // tankerPts
//					0f, // transportPts
//					0f, // linerPts
//					0f, // civilianPts 
//					0f, // utilityPts
//					0f, // qualityBonus
//					-1f, // qualityOverride
//					1f + Math.min(1f, losses / 10f), // officer num mult
//					0 + (int) losses// officer level bonus
//					), entity.getContainingLocation(),
//					new Vector2f(entity.getLocation().x, entity.getLocation().y));
//			
//			stub.addEventListener(this);
			
			
			PatrolFleetData data = new PatrolFleetData(stub, type);
			data.startingFleetPoints = combat * 3;
			data.sourceMarket = market;
			activePatrols.add(data);
			
			PatrolAssignmentAIV2 ai = new PatrolAssignmentAIV2(stub, data);
			stub.addScript(ai);
			
			stub.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);

//			if ((type == PatrolType.FAST && (float) Math.random() > 0.25f) ||
//					(type == PatrolType.COMBAT && (float) Math.random() > 0.5f)) {
			
			if (type == PatrolType.FAST || type == PatrolType.COMBAT) {
				stub.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_CUSTOMS_INSPECTOR, true);
			}
			
			stub.setAdmiralPost(Ranks.POST_PATROL_COMMANDER);
			
			switch (type) {
			case FAST:
				stub.setAdmiralRank(Ranks.SPACE_LIEUTENANT);
				break;
			case COMBAT:
				stub.setAdmiralRank(Ranks.SPACE_COMMANDER);
				break;
			case HEAVY:
				stub.setAdmiralRank(Ranks.SPACE_CAPTAIN);
				break;
			}
			
//			FleetStubConverterPlugin converter = stub.getConverter();
//			if (stub.isStub() && converter.shouldConvertFromStub(stub)) {
//				converter.convertToFleet(stub);
//			}
			
			
			log.info("Spawned patrol fleet [" + stub.getId() + "] from market " + market.getName());
		} else {
			log.debug("Maximum number of " + maxPatrols + " patrols already in play for market [" + market.getName() + "]");
		}
	}
	
	private int getCount(PatrolType ... types) {
		int count = 0;
		for (PatrolType type : types) {
			for (PatrolFleetData data : activePatrols) {
				if (data.type == type) count++;
			}
		}
		return count;
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}

	
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		for (PatrolFleetData data : activePatrols) {
			if (data.stub.getId() == fleet.getId()) {
				activePatrols.remove(data);
				break;
			}
		}
	}

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		
	}
	
	
	/*
	@Override
	public void reportFleetDespawned(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		super.reportFleetDespawned(fleet, reason, param);
		
		for (PatrolFleetData data : activePatrols) {
			if (data.fleet == fleet) {
				activePatrols.remove(data);
				break;
			}
		}
	}

	@Override
	public void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle) {
		super.reportBattleOccurred(primaryWinner, battle);
		
		boolean playerWon = battle.isPlayerSide(battle.getSideFor(primaryWinner));
		boolean playerLost = battle.isPlayerSide(battle.getOtherSideFor(primaryWinner));
		if (primaryWinner.isInOrNearSystem(market.getStarSystem())) {
			// losing to pirates doesn't trigger patrol strength going up; don't want pirates wiped out
			if (primaryWinner.getFaction().getId().equals(Factions.PIRATES)) return;
			if (primaryWinner.getFaction().getId().equals(Factions.LUDDIC_PATH)) return;
			
			for (CampaignFleetAPI loser : battle.getOtherSideSnapshotFor(primaryWinner)) {
				if (loser.getFaction() == market.getFaction()) {
					if (playerWon) {
						patrolBattlesLost.add(1);
					} else {
						//patrolBattlesLost.add(1);
					}
				} else if (primaryWinner.getFaction() == market.getFaction()) {
					// winning vs pirates doesn't trigger strength getting smaller, might happen too easily				
					if (loser.getFaction().getId().equals(Factions.PIRATES)) return;
					if (loser.getFaction().getId().equals(Factions.LUDDIC_PATH)) return;
					if (playerLost) {
						patrolBattlesLost.sub(1);
					} else {
						//patrolBattlesLost.sub(1);
					}
				}
			}
		}
	}

	*/

}














