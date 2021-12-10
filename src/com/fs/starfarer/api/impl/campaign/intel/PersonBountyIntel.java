package com.fs.starfarer.api.impl.campaign.intel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial;
import com.fs.starfarer.api.impl.campaign.shared.PersonBountyEventData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class PersonBountyIntel extends BaseIntelPlugin implements EveryFrameScript, FleetEventListener {
	public static Logger log = Global.getLogger(PersonBountyIntel.class);
	
	public static enum BountyType {
		PIRATE,
		DESERTER,
	}
	
	public static float MAX_DURATION = 90f;
	
	//public static int FAST_START_LEVEL_INCREASE = 1;
	public static float MAX_TIME_BASED_ADDED_LEVEL = 3;
	
	private float elapsedDays = 0f;
	private float duration = MAX_DURATION;
	private float bountyCredits = 0;
	
	private FactionAPI faction;
	private PersonAPI person;
	private CampaignFleetAPI fleet;
	private FleetMemberAPI flagship;
	
	private BountyType bountyType;
	//private FleetType fleetType;
	
	private SectorEntityToken hideoutLocation = null;
	
	private int level = 0;
	
	public float getElapsedDays() {
		return elapsedDays;
	}

	public void setElapsedDays(float elapsedDays) {
		this.elapsedDays = elapsedDays;
	}

	public static PersonBountyEventData getSharedData() {
		return SharedData.getData().getPersonBountyEventData();
	}
	
	public PersonBountyIntel() {
		pickLevel();
		
		pickFaction();
		if (isDone()) return;
		
		initBountyAmount();
		
		pickHideoutLocation();
		if (isDone()) return;
		
		pickBountyType();
		if (bountyType == BountyType.DESERTER) {
			bountyCredits *= 1.5f;
		}
		
		initPerson();
		if (isDone()) return;
		
		spawnFleet();
		if (isDone()) return;
		
		log.info(String.format("Starting person bounty by faction [%s] for person %s", faction.getDisplayName(), person.getName().getFullName()));
		
		Global.getSector().getIntelManager().queueIntel(this);
	}
	
	public void reportMadeVisibleToPlayer() {
		if (!isEnding() && !isEnded()) {
			duration = Math.max(duration * 0.5f, Math.min(duration * 2f, MAX_DURATION));
		}
	}
	
	protected void pickLevel() {
//		if (true) {
//			level = 10;
//			//level = Misc.random.nextInt(11);
//			return;
//		}
		
		int base = getSharedData().getLevel();

//		if (Misc.isFastStart()) {
			float timeFactor = (PirateBaseManager.getInstance().getDaysSinceStart() - 180f) / (365f * 2f);
			if (timeFactor < 0) timeFactor = 0;
			if (timeFactor > 1) timeFactor = 1;
			
			int add = (int) Math.round(MAX_TIME_BASED_ADDED_LEVEL * timeFactor);
			base += add;
			//base += FAST_START_LEVEL_INCREASE;
//		}
		
//		if (Global.getSector().getPlayerFleet() != null) {
//			int playerLevel = Global.getSector().getPlayerFleet().getCommander().getStats().getLevel();
//			base = Math.max(base, (playerLevel - 20) / 5);
//		}
		
		if (base > 10) base = 10;
		
		boolean hasLow = false;
		boolean hasHigh = false;
		for (EveryFrameScript s : PersonBountyManager.getInstance().getActive()) {
			PersonBountyIntel bounty = (PersonBountyIntel) s;
			
			int curr = bounty.getLevel();
			
			if (curr < base || curr == 0) hasLow = true;
			if (curr > base) hasHigh = true;
		}
		
		level = base;
		if (!hasLow) {
			//level -= new Random().nextInt(2) + 1;
			level = 0;
		} else if (!hasHigh) {
			level += new Random().nextInt(3) + 2;
		}
		
		if (level < 0) level = 0;
	}
	
	public int getLevel() {
		return level;
	}

	protected void pickHideoutLocation() {
		WeightedRandomPicker<StarSystemAPI> systemPicker = new WeightedRandomPicker<StarSystemAPI>();
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float mult = 0f;
			
			if (system.hasPulsar()) continue;
			
			if (system.hasTag(Tags.THEME_MISC_SKIP)) {
				mult = 1f;
			} else if (system.hasTag(Tags.THEME_MISC)) {
				mult = 3f;
			} else if (system.hasTag(Tags.THEME_REMNANT_NO_FLEETS)) {
				mult = 3f;
			} else if (system.hasTag(Tags.THEME_RUINS)) {
				mult = 5f;
			} else if (system.hasTag(Tags.THEME_REMNANT_DESTROYED)) {
				mult = 3f;
			} else if (system.hasTag(Tags.THEME_CORE_UNPOPULATED)) {
				mult = 1f;
			}
			
			for (MarketAPI market : Misc.getMarketsInLocation(system)) {
				if (market.isHidden()) continue;
				mult = 0f;
				break;
			}
			
			float distToPlayer = Misc.getDistanceToPlayerLY(system.getLocation());
			float noSpawnRange = Global.getSettings().getFloat("personBountyNoSpawnRangeAroundPlayerLY");
			if (distToPlayer < noSpawnRange) mult = 0f;
			
			if (mult <= 0) continue;
			
			float weight = system.getPlanets().size();
			for (PlanetAPI planet : system.getPlanets()) {
				if (planet.isStar()) continue;
				if (planet.getMarket() != null) {
					float h = planet.getMarket().getHazardValue();
					if (h <= 0f) weight += 5f;
					else if (h <= 0.25f) weight += 3f;
					else if (h <= 0.5f) weight += 1f;
				}
			}
			
			float dist = system.getLocation().length();
			float distMult = Math.max(0, 50000f - dist);
			
			systemPicker.add(system, weight * mult * distMult);
		}
		
		StarSystemAPI system = systemPicker.pick();
		
		if (system != null) {
			WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>();
			for (SectorEntityToken planet : system.getPlanets()) {
				if (planet.isStar()) continue;
				if (planet.getMarket() != null && 
						!planet.getMarket().isPlanetConditionMarketOnly()) continue;
				
				picker.add(planet);
			}
			hideoutLocation = picker.pick();
		}
		
		
		if (hideoutLocation == null) {
			endImmediately();
		}
	}
	
	

	private void pickFaction() {
		FactionAPI player = Global.getSector().getPlayerFaction();

		String commFacId = Misc.getCommissionFactionId();
		boolean forceCommissionFaction = true;
		if (commFacId != null && getSharedData().isParticipating(commFacId)) {
			for (EveryFrameScript s : PersonBountyManager.getInstance().getActive()) {
				PersonBountyIntel bounty = (PersonBountyIntel) s;
				if (bounty.faction != null && bounty.faction.getId().equals(commFacId)) {
					forceCommissionFaction = false;
				}
			}
		} else {
			forceCommissionFaction = false;
		}
		
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (!getSharedData().isParticipating(market.getFactionId())) continue;
			if (market.getSize() < 3) continue;
			if (market.isHidden()) continue;
			if (market.getFaction().isPlayerFaction()) continue;
			
			float weight = market.getSize();
			if (market.hasIndustry(Industries.PATROLHQ)) weight *= 1.5f;
			if (market.hasIndustry(Industries.MILITARYBASE)) weight *= 3f;
			if (market.hasIndustry(Industries.HIGHCOMMAND)) weight *= 5f;

			if (market.getFaction() != null) {
				if (forceCommissionFaction && !market.getFaction().getId().equals(commFacId)) {
					continue;
				}
					
				if (market.getFaction().isHostileTo(player)) {
					weight *= 0.5f;
				} else {
					// turned off to vary bounties a bit more
					//float rel = market.getFaction().getRelToPlayer().getRel();
					//weight *= 1f + rel; // (0.5 to 2], given that it's not hostile if we're here
				}
			}
			
			if (weight > 0) {
				picker.add(market, weight);
			}
		}
		
		if (picker.isEmpty()) {
			endImmediately();
			return;
		}
		
		MarketAPI market = picker.pick();
		faction = market.getFaction();
	}
	
	private void initBountyAmount() {
		//float highStabilityMult = BaseMarketConditionPlugin.getHighStabilityBonusMult(market);
		float highStabilityMult = 1f;
		float base = Global.getSettings().getFloat("basePersonBounty");
		float perLevel = Global.getSettings().getFloat("personBountyPerLevel");
		
		float random = perLevel * (int)(Math.random() * 15) / 15f;
		
		bountyCredits = (int) ((base + perLevel * level + random) * highStabilityMult);
	}
	
	private void initPerson() {
		String factionId = Factions.PIRATES;
		if (bountyType == BountyType.DESERTER) {
			factionId = faction.getId();
		}
		int personLevel = (int) (5 + level * 1.5f);
		person = OfficerManagerEvent.createOfficer(Global.getSector().getFaction(factionId), 
												   personLevel, false);
	}
	
	private void pickBountyType() {
		WeightedRandomPicker<BountyType> picker = new WeightedRandomPicker<BountyType>();
		picker.add(BountyType.PIRATE, 10f);
		
		//if (getSharedData().getLevel() >= 3) {
		if (level >= 4) {
			picker.add(BountyType.DESERTER, 30f);
		}
		bountyType = picker.pick();
	}
	
//	private String getTargetDesc() {
//		//targetDesc = person.getName().getFullName() + " is known to be a highly capable combat officer in command of a sizeable fleet.";
//		
//		ShipHullSpecAPI spec = flagship.getVariant().getHullSpec();
//		String shipType = spec.getHullNameWithDashClass() + " " + spec.getDesignation().toLowerCase(); 
//
//		String heOrShe = "he";
//		String hisOrHer = "his";
//		if (person.isFemale()) {
//			heOrShe = "she";
//			hisOrHer = "her";
//		}
//		
//		String levelDesc = "";
//		int personLevel = person.getStats().getLevel();
//		if (personLevel <= 5) {
//			levelDesc = "an unremarkable officer";
//		} else if (personLevel <= 10) {
//			levelDesc = "a capable officer";
//		} else if (personLevel <= 15) {
//			levelDesc = "a highly capable officer";
//		} else {
//			levelDesc = "an exceptionally capable officer";
//		}
//		
//		String skillDesc = "";
//		
//		if (person.getStats().getSkillLevel(Skills.OFFICER_MANAGEMENT) > 0) {
//			skillDesc = "having a high number of skilled subordinates";
//		} else if (person.getStats().getSkillLevel(Skills.ELECTRONIC_WARFARE) > 0) {
//			skillDesc = "being proficient in electronic warfare";
//		} else if (person.getStats().getSkillLevel(Skills.CARRIER_GROUP) > 0) {
//			skillDesc = "a noteworthy level of skill in running carrier operations";
//		} else if (person.getStats().getSkillLevel(Skills.COORDINATED_MANEUVERS) > 0) {
//			skillDesc = "a high effectiveness in coordinating the maneuvers of ships during combat";
//		}
//		
//		if (!skillDesc.isEmpty() && levelDesc.contains("unremarkable")) {
//			levelDesc = "an otherwise unremarkable officer";
//		}
//		
//		String fleetDesc = "";
//		if (level < 3) {
//			fleetDesc = "small";
//		} else if (level <= 5) {
//			fleetDesc = "medium-sized";
//		} else if (level <= 8) {
//			fleetDesc = "large";
//		} else {
//			fleetDesc = "very large";
//		}
//		
//		String targetDesc = String.format("%s is in command of a %s fleet and was last seen using a %s as %s flagship.",
//						person.getName().getFullName(), fleetDesc, shipType, hisOrHer);					
//		
//		if (skillDesc.isEmpty()) {
//			targetDesc += String.format(" %s is known to be %s.", Misc.ucFirst(heOrShe), levelDesc);
//		} else {
//			targetDesc += String.format(" %s is %s known for %s.", Misc.ucFirst(heOrShe), levelDesc, skillDesc);
//		}
//		
//		//targetDesc += "\n\nLevel: " + level;
//		
//		return targetDesc;
//	}

	@Override
	protected void advanceImpl(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		//days *= 60f;
		
		elapsedDays += days;

		if (elapsedDays >= duration && !isDone()) {
			boolean canEnd = fleet == null || !fleet.isInCurrentLocation();
			if (canEnd) {
				log.info(String.format("Ending bounty on %s by %s", person.getName().getFullName(), faction.getDisplayName()));
				result = new BountyResult(BountyResultType.END_TIME, 0, null);
				sendUpdateIfPlayerHasIntel(result, true);
				cleanUpFleetAndEndIfNecessary();
				return;
			}
		}
		
		if (fleet == null) return;
		
		if (fleet.isInCurrentLocation() && !fleet.getFaction().getId().equals(Factions.PIRATES)) {
			fleet.setFaction(Factions.PIRATES, true);
		} else if (!fleet.isInCurrentLocation() && !fleet.getFaction().getId().equals(Factions.NEUTRAL)) {
			fleet.setFaction(Factions.NEUTRAL, true);
		}
		
		if (fleet.getFlagship() == null || fleet.getFlagship().getCaptain() != person) {
			result = new BountyResult(BountyResultType.END_OTHER, 0, null);
			boolean current = fleet.isInCurrentLocation();
			sendUpdateIfPlayerHasIntel(result, !current);
			cleanUpFleetAndEndIfNecessary();
			return;
		}
	}
	
	public float getTimeRemainingFraction() {
		float f = 1f - elapsedDays / duration;
		return f;
	}
	
	
	protected BountyResult result = null;

	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		cleanUpFleetAndEndIfNecessary();
	}
	
	protected void cleanUpFleetAndEndIfNecessary() {
		if (fleet != null) {
			Misc.makeUnimportant(fleet, "pbe");
			fleet.clearAssignments();
			if (hideoutLocation != null) {
				fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, hideoutLocation, 1000000f, null);
			} else {
				fleet.despawn();
			}
			fleet = null; //can't null it because description uses it
		}
		if (!isEnding() && !isEnded()) {
			endAfterDelay();
		}
	}

	protected boolean willPay() {
		if (true) return true;
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		RepLevel level = playerFleet.getFaction().getRelationshipLevel(faction);
		return level.isAtWorst(RepLevel.SUSPICIOUS);
	}
	
	protected boolean willRepIncrease() {
		if (true) return true;
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		RepLevel level = playerFleet.getFaction().getRelationshipLevel(faction);
		return level.isAtWorst(RepLevel.HOSTILE);
	}

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (isDone() || result != null) return;
		
		// also credit the player if they're in the same location as the fleet and nearby
		float distToPlayer = Misc.getDistance(fleet, Global.getSector().getPlayerFleet());
		boolean playerInvolved = battle.isPlayerInvolved() || (fleet.isInCurrentLocation() && distToPlayer < 2000f);
		
		if (battle.isInvolved(fleet) && !playerInvolved) {
			if (fleet.getFlagship() == null || fleet.getFlagship().getCaptain() != person) {
				fleet.setCommander(fleet.getFaction().createRandomPerson());
				//Global.getSector().reportEventStage(this, "other_end", market.getPrimaryEntity(), messagePriority);
				result = new BountyResult(BountyResultType.END_OTHER, 0, null);
				sendUpdateIfPlayerHasIntel(result, true);
				cleanUpFleetAndEndIfNecessary();
				return;
			}
		}
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		if (!playerInvolved || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet)) {
			return;
		}
		
		 // didn't destroy the original flagship
		if (fleet.getFlagship() != null && fleet.getFlagship().getCaptain() == person) return;
		
		//int payment = (int) (bountyCredits * battle.getPlayerInvolvementFraction());
		int payment = (int) bountyCredits; // don't bother about reducing the payout if the player didn't do it all themselves
		if (payment <= 0) {
			result = new BountyResult(BountyResultType.END_OTHER, 0, null);
			sendUpdateIfPlayerHasIntel(result, true);
			cleanUpFleetAndEndIfNecessary();
			return;
		}
		
		if (willPay()) {
			log.info(String.format("Paying bounty of %d from faction [%s]", (int) payment, faction.getDisplayName()));
			
			playerFleet.getCargo().getCredits().add(payment);
			ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.PERSON_BOUNTY_REWARD, null, null, null, true, false), 
					faction.getId());
			result = new BountyResult(BountyResultType.END_PLAYER_BOUNTY, payment, rep);
			sendUpdateIfPlayerHasIntel(result, false);
		} else if (willRepIncrease()) {
			log.info(String.format("Not paying bounty, but improving rep with faction [%s]", faction.getDisplayName()));
			ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
								new RepActionEnvelope(RepActions.PERSON_BOUNTY_REWARD, null, null, null, true, false), 
								faction.getId());
			result = new BountyResult(BountyResultType.END_PLAYER_NO_BOUNTY, payment, rep);
			sendUpdateIfPlayerHasIntel(result, false);
		} else {
			log.info(String.format("Not paying bounty or improving rep with faction [%s]", faction.getDisplayName()));
			result = new BountyResult(BountyResultType.END_PLAYER_NO_REWARD, 0, null);
			sendUpdateIfPlayerHasIntel(result, false);
		}
		
		
		getSharedData().reportSuccess();
		//removeBounty();
		
		cleanUpFleetAndEndIfNecessary();
	}

	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (isDone() || result != null) return;
		
		if (this.fleet == fleet) {
			fleet.setCommander(fleet.getFaction().createRandomPerson());
			result = new BountyResult(BountyResultType.END_OTHER, 0, null);
			sendUpdateIfPlayerHasIntel(result, true);
			cleanUpFleetAndEndIfNecessary();
		}
	}
	
	
	private void spawnFleet() {
//		level = 10;
//		bountyType = BountyType.PIRATE;
		
		String fleetFactionId = Factions.PIRATES;
		if (bountyType == BountyType.DESERTER) {
			fleetFactionId = faction.getId();
		}
		
		float qf = (float) level / 10f;
		if (qf > 1) qf = 1;
		
		String fleetName = "";
		
		fleetName = person.getName().getLast() + "'s" + " Fleet";
		
		float fp = (5 + level * 5) * 5f;
		fp *= 0.75f + (float) Math.random() * 0.25f;
		
		if (level >= 7) {
			fp += 20f;
		}
		if (level >= 8) {
			fp += 30f;
		}
		if (level >= 9) {
			fp += 50f;
		}
		if (level >= 10) {
			fp += 50f;
		}
		
		FactionAPI faction = Global.getSector().getFaction(fleetFactionId);
		float maxFp = faction.getApproximateMaxFPPerFleet(ShipPickMode.PRIORITY_THEN_ALL) * 1.1f;
		if (fp > maxFp) fp = maxFp;
		
		FleetParamsV3 params = new FleetParamsV3(
				null, 
				hideoutLocation.getLocationInHyperspace(),
				fleetFactionId, 
				qf + 0.2f, // qualityOverride
				FleetTypes.PERSON_BOUNTY_FLEET,
				fp, // combatPts
				0, // freighterPts 
				0, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
				);
		params.ignoreMarketFleetSizeMult = true;
//		if (route != null) {
//			params.timestamp = route.getTimestamp();
//		}
		//params.random = random;
		fleet = FleetFactoryV3.createFleet(params);
		
//		fleet.getFleetData().addFleetMember("station_small_Standard");
//		fleet.getFleetData().sort();
		
		if (fleet == null || fleet.isEmpty()) {
			endImmediately();
			return;
		}

		fleet.setCommander(person);
		fleet.getFlagship().setCaptain(person);
//		fleet.getFleetData().setSyncNeeded();
//		fleet.getFleetData().syncIfNeeded();
		FleetFactoryV3.addCommanderSkills(person, fleet, null);
		
		//Misc.getSourceMarket(fleet).getStats().getDynamic().getValue(Stats.OFFICER_LEVEL_MULT);
		
		Misc.makeImportant(fleet, "pbe", duration + 20f);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_NO_MILITARY_RESPONSE, true);
		
		fleet.setNoFactionInName(true);
		fleet.setFaction(Factions.NEUTRAL, true);
		fleet.setName(fleetName);

		fleet.addEventListener(this);
		
		LocationAPI location = hideoutLocation.getContainingLocation();
		location.addEntity(fleet);
		fleet.setLocation(hideoutLocation.getLocation().x - 500, hideoutLocation.getLocation().y + 500);
		fleet.getAI().addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, hideoutLocation, 1000000f, null);
		
		flagship = fleet.getFlagship();

	}

	public boolean runWhilePaused() {
		return false;
	}


	
	public static enum BountyResultType {
		END_PLAYER_BOUNTY,
		END_PLAYER_NO_BOUNTY,
		END_PLAYER_NO_REWARD,
		END_OTHER,
		END_TIME,
	}
	
	public static class BountyResult {
		public BountyResultType type;
		public int payment;
		public ReputationAdjustmentResult rep;
		public BountyResult(BountyResultType type, int payment, ReputationAdjustmentResult rep) {
			this.type = type;
			this.payment = payment;
			this.rep = rep;
		}
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;
		
		if (result == null) {
//			RepLevel level = Global.getSector().getPlayerFaction().getRelationshipLevel(faction.getId());
//			Color relColor = faction.getRelColor(Factions.PLAYER);
//			String rel = level.getDisplayName().toLowerCase();
			
			if (mode == ListInfoMode.IN_DESC) {
//				if (!willRepIncrease()) {
//					LabelAPI label = info.addPara("No reward or rep increase (" + rel + ")", bodyColor, initPad);  
//					label.setHighlight("(" + rel + ")");
//					label.setHighlightColors(relColor);
//				} else if (!willPay()) {
//					LabelAPI label = info.addPara("No reward (" + rel + ")", bodyColor, initPad);  
//					label.setHighlight("(" + rel + ")");
//					label.setHighlightColors(relColor);
//				} else {
					info.addPara("%s reward", initPad, tc, h, Misc.getDGSCredits(bountyCredits));
//				}
				int days = (int) (duration - elapsedDays);
				if (days <= 1) {
					days = 1;
				}
				addDays(info, "remaining", days, tc);
			} else {
				info.addPara("Faction: " + faction.getDisplayName(), initPad, tc,
						 faction.getBaseUIColor(), faction.getDisplayName());
				if (!isEnding()) {
					int days = (int) (duration - elapsedDays);
					String daysStr = "days";
					if (days <= 1) {
						days = 1;
						daysStr = "day";
					}
					info.addPara("%s reward, %s " + daysStr + " remaining", 0f, tc,
							h, Misc.getDGSCredits(bountyCredits), "" + days);
				}
			}
			unindent(info);
			return;
		}
		
		switch (result.type) {
		case END_PLAYER_BOUNTY:
			info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(result.payment));
			CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, faction, null, 
					null, null, info, tc, isUpdate, 0f);
			break;
		case END_PLAYER_NO_BOUNTY:
			CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, faction, null, 
					null, null, info, tc, isUpdate, 0f);
			break;
		case END_PLAYER_NO_REWARD:
			CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, faction, null, 
					null, null, info, tc, isUpdate, 0f);
			break;
		case END_TIME:
			break;
		case END_OTHER:
			break;
		
		}
		
		unindent(info);
	}
	
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color c = getTitleColor(mode);
		float pad = 3f;
		float opad = 10f;
		
		info.addPara(getName(), c, 0f);
		
		addBulletPoints(info, mode);
		
	}
	
	
	public String getSortString() {
		return "Personal Bounty";
	}
	
	public String getName() {
		String n = person.getName().getFullName();
		
		if (result != null) {
			switch (result.type) {
			case END_PLAYER_BOUNTY:
			case END_PLAYER_NO_BOUNTY:
			case END_PLAYER_NO_REWARD:
				return "Bounty Completed - " + n;
			case END_OTHER:
			case END_TIME:
				return "Bounty Ended - " + n;
			}
		}
		
		return "Personal Bounty - " + n;
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return faction;
	}

	public String getSmallDescriptionTitle() {
		return getName();
		//return null;
	}
	
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;

		//info.addPara(getName(), c, 0f);
		
		//info.addSectionHeading(getName(), Alignment.MID, 0f);
		
		info.addImage(person.getPortraitSprite(), width, 128, opad);
		
		String type = "a notorious pirate";
		if (bountyType == BountyType.DESERTER) type = "a deserter";
		
		String has = faction.getDisplayNameHasOrHave();
		info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + has + 
				" posted a bounty for bringing " + person.getName().getFullName() + 
				", " + type + ", to justice.",
				opad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
		
		if (result != null) {
			if (result.type == BountyResultType.END_PLAYER_BOUNTY) {
				info.addPara("You have successfully completed this bounty.", opad);
			} else if (result.type == BountyResultType.END_PLAYER_NO_BOUNTY) {
				info.addPara("You have successfully completed this bounty, but received no " +
						"credit reward because of your standing with " + 
						Misc.ucFirst(faction.getDisplayNameWithArticle()) + ".",
						opad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
			} else if (result.type == BountyResultType.END_PLAYER_NO_REWARD) {
				info.addPara("You have successfully completed this bounty, but received no " +
						"reward because of your standing with " + 
						Misc.ucFirst(faction.getDisplayNameWithArticle()) + ".",
						opad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
			} else {
				info.addPara("This bounty is no longer on offer.", opad);
			}
		} else {
//			RepLevel level = Global.getSector().getPlayerFaction().getRelationshipLevel(faction.getId());
//			Color relColor = faction.getRelColor(Factions.PLAYER);
//			String rel = level.getDisplayName().toLowerCase();
//			if (!willRepIncrease()) {
//				LabelAPI label = info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " is " + rel + 
//						" towards you, and you would receive no payment or reputation increase for " +
//						"completing the bounty.", opad);
//				label.setHighlight(faction.getDisplayNameWithArticleWithoutArticle(), rel);
//				label.setHighlightColors(faction.getBaseUIColor(), relColor);
//			} else if (!willPay()) {
//				LabelAPI label = info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " is " + rel + 
//						" towards you, and you would receive no payment for completing the bounty, but " +
//						"it would improve your standing.", opad);
//				label.setHighlight(faction.getDisplayNameWithArticleWithoutArticle(), rel);
//				label.setHighlightColors(faction.getBaseUIColor(), relColor);
//			}
		}
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
		if (result == null) {
//			String targetDesc = getTargetDesc();
//			if (targetDesc != null) {
//				info.addPara(targetDesc, opad);
//			}
			
			if (hideoutLocation != null) {
				SectorEntityToken fake = hideoutLocation.getContainingLocation().createToken(0, 0);
				fake.setOrbit(Global.getFactory().createCircularOrbit(hideoutLocation, 0, 1000, 100));
				
				String loc = BreadcrumbSpecial.getLocatedString(fake);
				loc = loc.replaceAll("orbiting", "hiding out near");
				loc = loc.replaceAll("located in", "hiding out in");
				String sheIs = "She is";
				if (person.getGender() == Gender.MALE) sheIs = "He is";
				info.addPara(sheIs + " rumored to be " + loc + ".", opad);
			}
			
			
			int cols = 7;
			float iconSize = width / cols;
			
			
			if (DebugFlags.PERSON_BOUNTY_DEBUG_INFO) {
				boolean deflate = false;
				if (!fleet.isInflated()) {
					fleet.setFaction(Factions.PIRATES, true);
					fleet.inflateIfNeeded();
					deflate = true;
				}
				
				String her = "her";
				if (person.getGender() == Gender.MALE) her = "his";
				info.addPara("The bounty posting also contains partial intel on the ships under " + her + " command. (DEBUG: full info)", opad);
				info.addShipList(cols, 3, iconSize, getFactionForUIColors().getBaseUIColor(), fleet.getMembersWithFightersCopy(), opad);
				
				info.addPara("level: " + level, 3f);
				info.addPara("type: " + bountyType.name(), 3f);
				
				if (deflate) {
					fleet.deflate();
				}
			} else {
				boolean deflate = false;
				if (!fleet.isInflated()) {
					fleet.setFaction(Factions.PIRATES, true);
					fleet.inflateIfNeeded();
					deflate = true;
				}
				
				List<FleetMemberAPI> list = new ArrayList<FleetMemberAPI>();
				Random random = new Random(person.getNameString().hashCode() * 170000);
				
//				WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<FleetMemberAPI>(random);
//				picker.addAll(fleet.getFleetData().getMembersListCopy());
//				int picks = (int) Math.round(picker.getItems().size() * 0.5f);
				
				List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
				int max = 7;
				for (FleetMemberAPI member : members) {
					if (list.size() >= max) break;
					
					if (member.isFighterWing()) continue;
					
					float prob = (float) member.getFleetPointCost() / 20f;
					prob += (float) max / (float) members.size();
					if (member.isFlagship()) prob = 1f;
					//if (members.size() <= max) prob = 1f;
					
					if (random.nextFloat() > prob) continue;
					
					FleetMemberAPI copy = Global.getFactory().createFleetMember(FleetMemberType.SHIP, member.getVariant());
					if (member.isFlagship()) {
						copy.setCaptain(person);
					}
					list.add(copy);
				}
				
				if (!list.isEmpty()) {
					String her = "her";
					if (person.getGender() == Gender.MALE) her = "his";
					info.addPara("The bounty posting also contains partial intel on some of the ships under " + her + " command.", opad);
					info.addShipList(cols, 1, iconSize, getFactionForUIColors().getBaseUIColor(), list, opad);
					
					int num = members.size() - list.size();
					num = Math.round((float)num * (1f + random.nextFloat() * 0.5f));
					
					if (num < 5) num = 0;
					else if (num < 10) num = 5;
					else if (num < 20) num = 10;
					else num = 20;
					
					if (num > 1) {
						info.addPara("The intel assessment notes the fleet may contain upwards of %s other ships" +
								" of lesser significance.", opad, h, "" + num);
					} else {
						info.addPara("The intel assessment notes the fleet may contain several other ships" +
								" of lesser significance.", opad);
					}
				}
				
				if (deflate) {
					fleet.deflate();
				}
			}
		}
		
		//info.addButton("Accept", "Accept", faction.getBaseUIColor(), faction.getDarkUIColor(), (int)(width/2), 20f, opad);

	}
	
	public String getIcon() {
		return person.getPortraitSprite();
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_BOUNTY);
		tags.add(faction.getId());
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		Constellation c = hideoutLocation.getConstellation();
		SectorEntityToken entity = null;
		if (c != null && map != null) {
			entity = map.getConstellationLabelEntity(c);
		}
		if (entity == null) entity = hideoutLocation;
		return entity;
	}

	public PersonAPI getPerson() {
		return person;
	}

	public CampaignFleetAPI getFleet() {
		return fleet;
	}
	
//	@Override
//	public boolean hasLargeDescription() {
//		return true;
//	}
}


