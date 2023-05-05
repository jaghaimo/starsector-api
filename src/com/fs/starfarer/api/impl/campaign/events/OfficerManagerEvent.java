package com.fs.starfarer.api.impl.campaign.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AdminData;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.CallEvent.CallableEvent;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 * 
 * @author Alex Mosolov
 * 
 * extends BaseEventPlugin for in-dev savefile compatibility reasons only
 *
 * Copyright 2019 Fractal Softworks, LLC
 */
public class OfficerManagerEvent extends BaseEventPlugin implements CallableEvent, ColonyInteractionListener, EveryFrameScript {
	
	public static class AvailableOfficer {
		public PersonAPI person;
		public String marketId;
		public int hiringBonus;
		public int salary;
		public float timeRemaining = 0f;
		public AvailableOfficer(PersonAPI person, String marketId, int hiringBonus, int salary) {
			this.person = person;
			this.marketId = marketId;
			this.hiringBonus = hiringBonus;
			this.salary = salary;
		}
		
	}
	
	public static Logger log = Global.getLogger(OfficerManagerEvent.class);
	
	protected IntervalUtil removeTracker = new IntervalUtil(1f, 3f);
	
	protected List<AvailableOfficer> available = new ArrayList<AvailableOfficer>();
	protected List<AvailableOfficer> availableAdmins = new ArrayList<AvailableOfficer>();
	
	protected TimeoutTracker<String> recentlyChecked = new TimeoutTracker<String>();
	
	protected long seed = 0;
	
	public OfficerManagerEvent() {
		readResolve();
		Global.getSector().getListenerManager().addListener(this);
	}
	
	Object readResolve() {
		if (availableAdmins == null) {
			availableAdmins = new ArrayList<AvailableOfficer>();
		}
		if (recentlyChecked == null) {
			recentlyChecked = new TimeoutTracker<String>();
		}
		if (seed == 0) {
			seed = Misc.random.nextLong();
		}
//		Global.getSector().getListenerManager().addListener(this);
		return this;
	}
	
	public void reportPlayerClosedMarket(MarketAPI market) {}

	public void reportPlayerOpenedMarket(MarketAPI market) {
		if (recentlyChecked.contains(market.getId())) return;
		
		if (market.isPlanetConditionMarketOnly()) return;
		if (market.getFaction().isNeutralFaction()) return;
		if (!market.isInEconomy()) return;
		if (market.hasTag(Tags.MARKET_NO_OFFICER_SPAWN)) return;
		
		
		pruneFromRemovedMarkets();
		
		float officerProb = market.getStats().getDynamic().getMod(Stats.OFFICER_PROB_MOD).computeEffective(0f);
		float additionalProb = market.getStats().getDynamic().getMod(Stats.OFFICER_ADDITIONAL_PROB_MULT_MOD).computeEffective(0f);
		float mercProb = market.getStats().getDynamic().getMod(Stats.OFFICER_IS_MERC_PROB_MOD).computeEffective(0f);
		float adminProb = market.getStats().getDynamic().getMod(Stats.ADMIN_PROB_MOD).computeEffective(0f);
		//adminProb = 1f;
		
		log.info("Spawning officers/admins at " + market.getId());
		log.info("    officerProb: " + officerProb);
		log.info("    additionalProb: " + additionalProb);
		log.info("    mercProb: " + mercProb);
		log.info("    adminProb: " + adminProb);
		log.info("");
		
		
		CampaignClockAPI clock = Global.getSector().getClock();
		long mult = clock.getCycle() * 12L + clock.getMonth();
		
		//Random random = new Random(seed + market.getId().hashCode() * mult);
		Random random = Misc.getRandom(seed + market.getId().hashCode() * mult, 11);
		//random = new Random();
		
		float dur = getOfficerDuration(random);
		recentlyChecked.add(market.getId(), dur * 0.5f);
		
		if (random.nextFloat() < officerProb) {
			boolean merc = random.nextFloat() < mercProb;
			AvailableOfficer officer = createOfficer(merc, market, random);
			officer.person.setPortraitSprite(pickPortraitPreferNonDuplicate(officer.person.getFaction(), officer.person.getGender()));
			officer.timeRemaining = dur;
			addAvailable(officer);
			log.info("Added officer at " + officer.marketId + "");
			
			if (random.nextFloat() < officerProb * additionalProb) {
				merc = random.nextFloat() < mercProb;
				officer = createOfficer(merc, market, random);
				officer.person.setPortraitSprite(pickPortraitPreferNonDuplicate(officer.person.getFaction(), officer.person.getGender()));
				officer.timeRemaining = dur;
				addAvailable(officer);
				log.info("Added officer at [" + officer.marketId + "]");
			}
		}
		
		if (random.nextFloat() < adminProb) {
			AvailableOfficer officer = createAdmin(market, random);
			officer.timeRemaining = dur;
			officer.person.setPortraitSprite(pickPortraitPreferNonDuplicate(officer.person.getFaction(), officer.person.getGender()));
			addAvailableAdmin(officer);
			log.info("Added admin at [" + officer.marketId + "]");
		}
	}
	
	protected float getOfficerDuration(Random random) {
		return 60f + 60f * random.nextFloat();
	}
	
	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		
		recentlyChecked.advance(days);
		
//		if (!Global.getSector().getListenerManager().hasListener(this)) {
//			Global.getSector().getListenerManager().addListener(this);
//		}
		
		removeTracker.advance(days);
		if (removeTracker.intervalElapsed()) {
			pruneFromRemovedMarkets();
			
			float interval = removeTracker.getIntervalDuration();
			
			for (AvailableOfficer curr : new ArrayList<AvailableOfficer>(available)) {
				curr.timeRemaining -= interval;
				if (curr.timeRemaining <= 0) {
					removeAvailable(curr);
					log.info("Removed officer from [" + curr.marketId + "]");
				}
			}
			for (AvailableOfficer curr : new ArrayList<AvailableOfficer>(availableAdmins)) {
				curr.timeRemaining -= interval;
				if (curr.timeRemaining <= 0) {
					removeAvailable(curr);
					log.info("Removed freelance admin from [" + curr.marketId + "]");
				}
			}
		}
		
	}
	
	public void pruneFromRemovedMarkets() {
		for (AvailableOfficer curr : new ArrayList<AvailableOfficer>(available)) {
			if (Global.getSector().getEconomy().getMarket(curr.marketId) == null) {
				removeAvailable(curr);
			}
		}
		for (AvailableOfficer curr : new ArrayList<AvailableOfficer>(availableAdmins)) {
			if (Global.getSector().getEconomy().getMarket(curr.marketId) == null) {
				removeAvailable(curr);
			}
		}
	}
	
	public void addAvailable(AvailableOfficer officer) {
		if (officer == null) return;
		
		available.add(officer);
		
		setEventDataAndAddToMarket(officer);
	}
	
	public void addAvailableAdmin(AvailableOfficer officer) {
		if (officer == null) return;
		
		availableAdmins.add(officer);
		
		setEventDataAndAddToMarket(officer);
	}
	
	protected void setEventDataAndAddToMarket(AvailableOfficer officer) {
		MarketAPI market = Global.getSector().getEconomy().getMarket(officer.marketId);
		if (market == null) return;
		market.getCommDirectory().addPerson(officer.person);
		market.addPerson(officer.person);
		
		officer.person.getMemoryWithoutUpdate().set("$ome_hireable", true);
		officer.person.getMemoryWithoutUpdate().set("$ome_eventRef", this);
		officer.person.getMemoryWithoutUpdate().set("$ome_hiringBonus", Misc.getWithDGS(officer.hiringBonus));		
		officer.person.getMemoryWithoutUpdate().set("$ome_salary", Misc.getWithDGS(officer.salary));		
	}
	
	public void removeAvailable(AvailableOfficer officer) {
		if (officer == null) return;
		
		available.remove(officer);
		availableAdmins.remove(officer);
		
		MarketAPI market = Global.getSector().getEconomy().getMarket(officer.marketId);
		if (market != null) {
			market.getCommDirectory().removePerson(officer.person);
			market.removePerson(officer.person);
		}
		
		officer.person.getMemoryWithoutUpdate().unset("$ome_hireable");
		officer.person.getMemoryWithoutUpdate().unset("$ome_eventRef");
		officer.person.getMemoryWithoutUpdate().unset("$ome_hiringBonus");
		officer.person.getMemoryWithoutUpdate().unset("$ome_salary");
	}
	
	public static String pickPortraitPreferNonDuplicate(FactionAPI faction, Gender gender) {
		WeightedRandomPicker<String> all = faction.getPortraits(gender);
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>();
		
		Set<String> exclude = new HashSet<String>();
		exclude.add(Global.getSector().getPlayerPerson().getPortraitSprite());
		if (Global.getSector().getPlayerFleet() != null) {
			for (OfficerDataAPI od : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
				exclude.add(od.getPerson().getPortraitSprite());
			}
		}
		for (AdminData ad : Global.getSector().getCharacterData().getAdmins()) {
			exclude.add(ad.getPerson().getPortraitSprite());
		}
		for (String p : all.getItems()) {
			if (exclude.contains(p)) continue;
			picker.add(p);
		}
		if (picker.isEmpty()) {
			picker = all;
		}
		return picker.pick();
	}
	
	protected AvailableOfficer createAdmin(MarketAPI market, Random random) {
//		WeightedRandomPicker<MarketAPI> marketPicker = new WeightedRandomPicker<MarketAPI>();
//		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//			marketPicker.add(market, market.getSize());
//		}
//		MarketAPI market = marketPicker.pick();
		if (market == null) return null;

		WeightedRandomPicker<Integer> tierPicker = new WeightedRandomPicker<Integer>();
		tierPicker.add(0, 60);
		tierPicker.add(1, 40);

		int tier = tierPicker.pick();
		
		PersonAPI person = createAdmin(market.getFaction(), tier, random);
		person.setFaction(Factions.INDEPENDENT);
		
		String hireKey = "adminHireTier" + tier;
		int hiringBonus = Global.getSettings().getInt(hireKey);
		
		int salary = (int) Misc.getAdminSalary(person);
		
		AvailableOfficer result = new AvailableOfficer(person, market.getId(), hiringBonus, salary);
		return result;
	}
	
	public static PersonAPI createAdmin(FactionAPI faction, int tier, Random random) {
		if (random == null) random = new Random();
		PersonAPI person = faction.createRandomPerson(random);
		
		person.getStats().setSkipRefresh(true);
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		List<String> allSkillIds = Global.getSettings().getSortedSkillIds();
		for (String skillId : allSkillIds) {
			SkillSpecAPI skill = Global.getSettings().getSkillSpec(skillId);
			if (skill.hasTag(Skills.TAG_DEPRECATED)) continue;
			if (skill.hasTag(Skills.TAG_PLAYER_ONLY)) continue;
			if (skill.hasTag(Skills.TAG_AI_CORE_ONLY)) continue;
			if (skill.isAdminSkill()) {
				picker.add(skillId);
			}
		}
		
		for (int i = 0; i < tier && !picker.isEmpty(); i++) {
			String pick = picker.pickAndRemove();
			person.getStats().setSkillLevel(pick, 3);
		}
		
		person.getMemoryWithoutUpdate().set("$ome_isAdmin", true);
		person.getMemoryWithoutUpdate().set("$ome_adminTier", tier);
		
		
		person.setRankId(Ranks.CITIZEN);
		person.setPostId(Ranks.POST_FREELANCE_ADMIN);
		
		
		WeightedRandomPicker<String> personalityPicker = faction.getPersonalityPicker().clone();

		String personality = personalityPicker.pick();
		person.setPersonality(personality);
		
		person.getStats().setSkipRefresh(false);
		person.getStats().refreshCharacterStatsEffects();
		
		//person.setPortraitSprite(pickPortrait(person.getFaction(), person.getGender()));
		
		return person;
	}
	
	
	protected AvailableOfficer createOfficer(boolean isMerc, MarketAPI market, Random random) {
//		WeightedRandomPicker<MarketAPI> marketPicker = new WeightedRandomPicker<MarketAPI>();
//		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//			marketPicker.add(market, market.getSize());
//		}
//		MarketAPI market = marketPicker.pick();
		if (market == null) return null;
		
		//FactionAPI faction = Global.getSector().getFaction(Factions.INDEPENDENT);

		int level = 1;
		if ((float) Math.random() > 0.75f) level = 2;
		
		float payMult = 1f;
		
		PersonAPI person = null;
		if (isMerc) {
			payMult = Global.getSettings().getFloat("officerMercPayMult");
			
			int minLevel = Global.getSettings().getInt("officerMercMinLevel");
			int maxLevel = Global.getSettings().getInt("officerMercMaxLevel");
			level = minLevel + Misc.random.nextInt(maxLevel + 1 - minLevel);
			
			int numElite = 1;
			if (level == maxLevel) numElite = 2;
			person = createMercInternal(market.getFaction(), level, numElite, true, random);
			
			person.setRankId(Ranks.SPACE_CAPTAIN);
			person.setPostId(Ranks.POST_MERCENARY);
			Misc.setMercenary(person, true);
		} else {
			person = createOfficerInternal(market.getFaction(), level, true, random);
			person.setPostId(Ranks.POST_OFFICER_FOR_HIRE);
		}
		
		person.setFaction(Factions.INDEPENDENT);
		
		
		
		int salary = (int) Misc.getOfficerSalary(person);
		AvailableOfficer result = new AvailableOfficer(person, market.getId(), 
							(int) (person.getStats().getLevel() * 2000* payMult), salary);
		return result;
	}
	
	public static PersonAPI createOfficerInternal(FactionAPI faction, int level, boolean allowNonDoctrinePersonality, Random random) {
		return createOfficer(faction, level, SkillPickPreference.ANY, allowNonDoctrinePersonality,
							 null, false, false, -1, random);
	}
	
	public static PersonAPI createMercInternal(FactionAPI faction, int level, int numElite, boolean allowNonDoctrinePersonality, Random random) {
//		SkillPickPreference pref = SkillPickPreference.GENERIC;
//		float f = (float) Math.random();
//		if (f < 0.05f) {
//			pref = SkillPickPreference.ANY;
//		} else if (f < 0.1f) {
//			pref = SkillPickPreference.PHASE;
//		} else if (f < 0.25f) {
//			pref = SkillPickPreference.CARRIER;
//		}
		SkillPickPreference pref = SkillPickPreference.ANY;
		return createOfficer(faction, level, pref, allowNonDoctrinePersonality,
				null, true, true, numElite, random);
	}
	
	
	public static enum SkillPickPreference {
		@Deprecated CARRIER,
		@Deprecated GENERIC,
		@Deprecated PHASE,
		
		/**
		 * Passing essentially three params using this enum to maintain API backwards compability with 0.95a, sigh.
		 * It's 4 now, bigger sigh. 
		 */
		YES_ENERGY_YES_BALLISTIC_YES_MISSILE_YES_DEFENSE,
		YES_ENERGY_YES_BALLISTIC_NO_MISSILE_YES_DEFENSE,
		YES_ENERGY_YES_BALLISTIC_YES_MISSILE_NO_DEFENSE,
		YES_ENERGY_YES_BALLISTIC_NO_MISSILE_NO_DEFENSE,
		YES_ENERGY_NO_BALLISTIC_YES_MISSILE_YES_DEFENSE,
		YES_ENERGY_NO_BALLISTIC_NO_MISSILE_YES_DEFENSE,
		YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE,
		YES_ENERGY_NO_BALLISTIC_NO_MISSILE_NO_DEFENSE,
		NO_ENERGY_YES_BALLISTIC_YES_MISSILE_YES_DEFENSE,
		NO_ENERGY_YES_BALLISTIC_NO_MISSILE_YES_DEFENSE,
		NO_ENERGY_YES_BALLISTIC_YES_MISSILE_NO_DEFENSE,
		NO_ENERGY_YES_BALLISTIC_NO_MISSILE_NO_DEFENSE,
		NO_ENERGY_NO_BALLISTIC_YES_MISSILE_YES_DEFENSE,
		NO_ENERGY_NO_BALLISTIC_NO_MISSILE_YES_DEFENSE,
		NO_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE,
		NO_ENERGY_NO_BALLISTIC_NO_MISSILE_NO_DEFENSE,
		ANY,
	}
	
	public static PersonAPI createOfficer(FactionAPI faction, int level) {
		return createOfficer(faction, level, false);
	}
	public static PersonAPI createOfficer(FactionAPI faction, int level, boolean allowNonDoctrinePersonality) {
		return createOfficer(faction, level, SkillPickPreference.ANY, allowNonDoctrinePersonality,
							null, false, true, -1, null);
	}
	public static PersonAPI createOfficer(FactionAPI faction, int level, SkillPickPreference pref, Random random) {
		return createOfficer(faction, level, pref, false, null, false, true, -1, random);
	}
	
	public static boolean DEBUG = false;
	public static PersonAPI createOfficer(FactionAPI faction, int level, 
										  SkillPickPreference pref, boolean allowNonDoctrinePersonality, 
										  CampaignFleetAPI fleet, boolean allowAnyLevel, 
										  boolean withEliteSkills, int eliteSkillsNumOverride, Random random) {
		if (random == null) random = new Random();
		
		//DEBUG = true;
		
		PersonAPI person = faction.createRandomPerson(random);
		person.setFleet(fleet);
		OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
		
		if (!allowAnyLevel) {
			if (level > plugin.getMaxLevel(person)) level = plugin.getMaxLevel(person);
		}
		
		person.getStats().setSkipRefresh(true);
		
		if (DEBUG) System.out.println("Generating officer\n");
		
		List<String> fixedSkills = new ArrayList<String>(faction.getDoctrine().getOfficerSkills());
		Iterator<String> iter = fixedSkills.iterator();
		while (iter.hasNext()) {
			String id = iter.next();
			SkillSpecAPI spec = Global.getSettings().getSkillSpec(id);
			if (spec != null && spec.hasTag(Skills.TAG_PLAYER_ONLY)) {
				iter.remove();
			}
		}
		
		if (random.nextFloat() < faction.getDoctrine().getOfficerSkillsShuffleProbability()) {
			Collections.shuffle(fixedSkills, random);
		}
		
		int numSpec = 0;
		for (int i = 0; i < 1; i++) {
			List<String> skills = plugin.pickLevelupSkills(person, random);
			String skillId = pickSkill(person, skills, pref, numSpec, random);
			if (!fixedSkills.isEmpty()) {
				skillId = fixedSkills.remove(0);
			}
			if (skillId != null) {
				if (DEBUG) System.out.println("Picking initial skill: " + skillId);
				person.getStats().increaseSkill(skillId);
				SkillSpecAPI spec = Global.getSettings().getSkillSpec(skillId);
				if (spec.hasTag(Skills.TAG_SPEC)) numSpec++;
				
			}
		}
		
//		level = 20;
//		pref = SkillPickPreference.NON_CARRIER;

		long xp = plugin.getXPForLevel(level);
		OfficerDataAPI officerData = Global.getFactory().createOfficerData(person);
		officerData.addXP(xp, null, false);
		
		//DEBUG = true;
		
		officerData.makeSkillPicks(random);
		
		while (officerData.canLevelUp(allowAnyLevel)) {
			String skillId = pickSkill(officerData.getPerson(), officerData.getSkillPicks(), pref, numSpec, random);
			if (!fixedSkills.isEmpty()) {
				skillId = fixedSkills.remove(0);
			}
			if (skillId != null) {
				if (DEBUG) System.out.println("Leveling up " + skillId);
				officerData.levelUp(skillId, random);
				SkillSpecAPI spec = Global.getSettings().getSkillSpec(skillId);
				if (spec.hasTag(Skills.TAG_SPEC)) numSpec++;
				
				if (allowAnyLevel && officerData.getSkillPicks().isEmpty()) {
					officerData.makeSkillPicks(random);
				}
			} else {
				break;
			}
		}
		
		if (withEliteSkills && eliteSkillsNumOverride != 0) {
			int num = eliteSkillsNumOverride;
			if (num < 0) {
				num = plugin.getMaxEliteSkills(person);
			}
			addEliteSkills(person, num, random);
		}
		
		if (DEBUG) System.out.println("Done\n");
		
		person.setRankId(Ranks.SPACE_LIEUTENANT);
		person.setPostId(Ranks.POST_OFFICER);
		
		
		WeightedRandomPicker<String> personalityPicker = faction.getPersonalityPicker().clone();
		if (allowNonDoctrinePersonality) {
			personalityPicker.add(Personalities.TIMID, 4f);
			personalityPicker.add(Personalities.CAUTIOUS, 4f);
			personalityPicker.add(Personalities.STEADY, 4f);
			personalityPicker.add(Personalities.AGGRESSIVE, 4f);
			personalityPicker.add(Personalities.RECKLESS, 4f);
		}
		
		String personality = personalityPicker.pick();
		person.setPersonality(personality);
		
		
		person.getStats().setSkipRefresh(false);
		person.getStats().refreshCharacterStatsEffects();
		
		return person;
	}
	
	public static void addEliteSkills(PersonAPI person, int num, Random random) {
		if (num <= 0) return;
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		for (SkillLevelAPI sl : person.getStats().getSkillsCopy()) {
			if (sl.getSkill().hasTag(Skills.TAG_ELITE_PLAYER_ONLY)) continue;
			if (sl.getSkill().isAptitudeEffect()) continue;
			if (!sl.getSkill().isCombatOfficerSkill()) continue;
			picker.add(sl.getSkill().getId(), 1f);
		}
		
		for (int i = 0; i < num && !picker.isEmpty(); i++) {
			String id = picker.pickAndRemove();
			if (id != null) {
				if (DEBUG) System.out.println("Making skill elite: " + id);
				person.getStats().increaseSkill(id);
			}
		}
	}
	
	public static String pickSkill(PersonAPI person, List<String> skills, SkillPickPreference pref, int numSpec, Random random) {
		if (random == null) random = new Random();
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		List<String> generic = new ArrayList<String>();
		
		boolean energy = pref.name().contains("YES_ENERGY"); // lol
		boolean ballistic = pref.name().contains("YES_BALLISTIC");
		boolean missile = pref.name().contains("YES_MISSILE");
		boolean defense = pref.name().contains("YES_DEFENSE");
		
		
		for (String id : skills) {
			SkillSpecAPI spec = Global.getSettings().getSkillSpec(id);
//			boolean carrierSkill = spec.hasTag(Skills.TAG_CARRIER);
//			boolean phaseSkill = spec.hasTag(Skills.TAG_PHASE);
//			boolean specSkill = spec.hasTag(Skills.TAG_SPEC);
			
			boolean energySkill = spec.hasTag(Skills.TAG_ENERGY_WEAPONS);
			boolean ballisticSkill = spec.hasTag(Skills.TAG_BALLISTIC_WEAPONS);
			boolean missileSkill = spec.hasTag(Skills.TAG_MISSILE_WEAPONS);
			boolean defenseSkill = spec.hasTag(Skills.TAG_ACTIVE_DEFENSES);
			
			boolean preferred = true;
			
			if (pref != SkillPickPreference.ANY) {
				if (!energy && energySkill) preferred = false;
				if (!ballistic && ballisticSkill) preferred = false;
				if (!missile && missileSkill) preferred = false;
				if (!defense && defenseSkill) preferred = false;
			}
			
//			preferred |= pref == SkillPickPreference.ANY;
//			preferred |= pref == SkillPickPreference.CARRIER && carrierSkill;
//			preferred |= pref == SkillPickPreference.PHASE && phaseSkill;
//			preferred |= pref == SkillPickPreference.GENERIC && !phaseSkill && !carrierSkill;
			
//			if (specSkill && !carrierSkill && !phaseSkill && numSpec >= 1) {
//				preferred = false;
//			}
			if (spec.hasTag(Skills.TAG_PLAYER_ONLY)) {
				preferred = false;
			}
			
			if (preferred) {
				picker.add(id);
			} else {
				generic.add(id);
			}
			
			//if ((!specSkill || numSpec < 1) && !carrierSkill && !phaseSkill) {
			//}
		}
		if (picker.isEmpty()) {
			picker.addAll(generic);
			if (picker.isEmpty()) {
				picker.addAll(skills);
			}
		}
		
		return picker.pick();
	}
	
	
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
		if (action.equals("printSkills")) {
			String personId = params.get(1).getString(memoryMap);
			AvailableOfficer officer = getOfficer(personId);
			boolean admin = false;
			int adminTier = 0;
			if (officer == null) {
				officer = getAdmin(personId);
				admin = true;
				if (officer != null) {
					adminTier = (int) officer.person.getMemoryWithoutUpdate().getFloat("$ome_adminTier");
				}
			}
			
			if (officer != null) {
				MutableCharacterStatsAPI stats = officer.person.getStats();
				TextPanelAPI text = dialog.getTextPanel();
				
				Color hl = Misc.getHighlightColor();
				Color red = Misc.getNegativeHighlightColor();
				
				//text.addParagraph("-----------------------------------------------------------------------------");
				
//				if (!admin) {
//					text.addParagraph("Level: " + (int) stats.getLevel());
//					text.highlightInLastPara(hl, "" + (int) stats.getLevel());
//				}
//				for (String skillId : Global.getSettings().getSortedSkillIds()) {
//					int level = (int) stats.getSkillLevel(skillId);
//					if (level > 0) {
//						SkillSpecAPI spec = Global.getSettings().getSkillSpec(skillId);
//						String skillName = spec.getName();
//						if (spec.isAptitudeEffect()) {
//							skillName += " Aptitude";
//						}
//						
//						if (level <= 1) {
//							text.addParagraph(skillName);
//						} else {
//							text.addParagraph(skillName + " (Elite)");
//						}
//						//text.highlightInLastPara(hl, "" + level);
//					}
//				}
				
				text.addSkillPanel(officer.person, admin);
				
				text.setFontSmallInsignia();
				
				if (!admin) {
					String personality = Misc.lcFirst(officer.person.getPersonalityAPI().getDisplayName());
					text.addParagraph("Personality: " + personality + ", level: " + stats.getLevel());
					text.highlightInLastPara(hl, personality, "" + stats.getLevel());
					text.addParagraph(officer.person.getPersonalityAPI().getDescription());
				}
				
				//text.addParagraph("-----------------------------------------------------------------------------");
				
				text.setFontInsignia();
			}
		} else if (action.equals("hireOfficer")) {
			String personId = params.get(1).getString(memoryMap);
			AvailableOfficer officer = getOfficer(personId);
			boolean admin = false;
			if (officer == null) {
				officer = getAdmin(personId);
				if (officer != null) {
					officer.person.setPostId(Ranks.POST_ADMINISTRATOR);
				}
				admin = true;
			}
			if (officer != null) {
				removeAvailable(officer);
				if (admin) {
					Global.getSector().getCharacterData().addAdmin(officer.person);
				} else {
					playerFleet.getFleetData().addOfficer(officer.person);
					if (Misc.isMercenary(officer.person)) {
						Misc.setMercHiredNow(officer.person);
					} else {
						officer.person.setPostId(Ranks.POST_OFFICER);
					}
				}
				AddRemoveCommodity.addCreditsLossText(officer.hiringBonus, dialog.getTextPanel());
				if (admin) {
					AddRemoveCommodity.addAdminGainText(officer.person, dialog.getTextPanel());
				} else {
					AddRemoveCommodity.addOfficerGainText(officer.person, dialog.getTextPanel());
				}
				playerFleet.getCargo().getCredits().subtract(officer.hiringBonus);
				if (playerFleet.getCargo().getCredits().get() <= 0) {
					playerFleet.getCargo().getCredits().set(0);
				}
			}
		} else if (action.equals("atLimit")) {
			//int max = (int) Global.getSettings().getFloat("officerPlayerMax");
			String personId = params.get(1).getString(memoryMap);
			AvailableOfficer officer = getOfficer(personId);
			boolean admin = false;
			if (officer == null) {
				officer = getAdmin(personId);
				admin = true;
			}
			int max = playerFleet.getCommander().getStats().getOfficerNumber().getModifiedInt();
			if (admin) {
				max = playerFleet.getCommander().getStats().getAdminNumber().getModifiedInt();
				return Global.getSector().getCharacterData().getAdmins().size() >= max;
			}
			
			// can hire more than max number of admins, just can't assign to govern w/o penalty
			//if (admin) return false;
			
			return Misc.getNumNonMercOfficers(playerFleet) >= max;
			
			//return playerFleet.getFleetData().getOfficersCopy().size() >= max;
		} else if (action.equals("canAfford")) {
			String personId = params.get(1).getString(memoryMap);
			AvailableOfficer officer = getOfficer(personId);
			if (officer == null) {
				officer = getAdmin(personId);
			}
			if (officer != null) {
				return playerFleet.getCargo().getCredits().get() >= officer.hiringBonus;
			} else {
				return false;
			}
		}
		
		return true;
	}
	
	public AvailableOfficer getOfficer(String personId) {
		for (AvailableOfficer officer: available) {
			if (officer.person.getId().equals(personId)) {
				return officer;
			}
		}
		return null;
	}
	
	public AvailableOfficer getAdmin(String personId) {
		for (AvailableOfficer officer: availableAdmins) {
			if (officer.person.getId().equals(personId)) {
				return officer;
			}
		}
		return null;
	}

	public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
		
	}
	
	public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
		
	}

	public boolean runWhilePaused() {
		return false;
	}
	
	public boolean isDone() {
		return false;
	}
}










