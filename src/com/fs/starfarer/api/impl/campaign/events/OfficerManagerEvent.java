package com.fs.starfarer.api.impl.campaign.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AdminData;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * 
 * @author Alex Mosolov
 *
 * Copyright 2014 Fractal Softworks, LLC
 */
public class OfficerManagerEvent extends BaseEventPlugin {
	
	public static class AvailableOfficer {
		public PersonAPI person;
		public String marketId;
		public int hiringBonus;
		public int salary;
		public AvailableOfficer(PersonAPI person, String marketId, int hiringBonus, int salary) {
			this.person = person;
			this.marketId = marketId;
			this.hiringBonus = hiringBonus;
			this.salary = salary;
		}
		
	}
	
	public static Logger log = Global.getLogger(OfficerManagerEvent.class);
	
	private IntervalUtil addTracker = new IntervalUtil(0.5f, 1.5f);
	private IntervalUtil removeTracker = new IntervalUtil(1f, 3f);
	
	private List<AvailableOfficer> available = new ArrayList<AvailableOfficer>();
	private List<AvailableOfficer> availableAdmins = new ArrayList<AvailableOfficer>();
	
	public void init(String type, CampaignEventTarget eventTarget) {
		super.init(type, eventTarget);
		readResolve();
	}
	
	Object readResolve() {
		if (availableAdmins == null) {
			availableAdmins = new ArrayList<AvailableOfficer>();
		}
		return this;
	}
	
	public void startEvent() {
		super.startEvent();
	}
	
	public void advance(float amount) {
		//if (true) return;
		
		if (!isEventStarted()) return;
		if (isDone()) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		
		addTracker.advance(days);
		if (addTracker.intervalElapsed()) {
			pruneFromRemovedMarkets();
			
			int maxOfficers = (int) Global.getSettings().getFloat("officerMaxHireable");
			if (available.size() < maxOfficers) {
				AvailableOfficer officer = createOfficer();
//				if (Global.getSettings().isDevMode() && (float) Math.random() > 0.75f &&
//						Global.getSector().isInNewGameAdvance() &&
//						Global.getSector().getEconomy().getMarket("jangala") != null) {
//					officer.marketId = "jangala";
//				}
				//officer.marketId = "jangala";
				addAvailable(officer);
				log.info("Added officer at " + officer.marketId + ", " + available.size() + " total available");
			}
			int maxAdmins = (int) Global.getSettings().getFloat("adminMaxHireable");
			if (availableAdmins.size() < maxAdmins) {
				AvailableOfficer officer = createAdmin();
//				if (Global.getSettings().isDevMode() && ((float) Math.random() > 0.75f) &&
//						(Global.getSector().isInNewGameAdvance()) &&
//						Global.getSector().getEconomy().getMarket("jangala") != null) {
//					officer.marketId = "jangala";
//				}
				addAvailableAdmin(officer);
				log.info("Added admin at " + officer.marketId + ", " + availableAdmins.size() + " total available");
			}
		}
		
		removeTracker.advance(days);
		if (removeTracker.intervalElapsed()) {
			pruneFromRemovedMarkets();
			
			WeightedRandomPicker<AvailableOfficer> picker = new WeightedRandomPicker<AvailableOfficer>();
			picker.addAll(available);
			picker.addAll(availableAdmins);
			AvailableOfficer pick = picker.pick();
			if (pick != null) {
				if (available.contains(pick)) {
					log.info("Removed officer from " + pick.marketId + ", " + available.size() + " total available");
				} else {
					log.info("Removed freelance admin from " + pick.marketId + ", " + availableAdmins.size() + " total available");
				}
				removeAvailable(pick);
			}
		}
		
//		for (AvailableOfficer officer : available) {
//			System.out.println("Name: " + officer.person.getName().getFullName() + ", id: " + officer.person.getId());
//		}
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
	}
	
	public static String pickPortrait(FactionAPI faction, Gender gender) {
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
	
	private AvailableOfficer createAdmin() {
		WeightedRandomPicker<MarketAPI> marketPicker = new WeightedRandomPicker<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			marketPicker.add(market, market.getSize());
		}
		MarketAPI market = marketPicker.pick();
		if (market == null) return null;

		WeightedRandomPicker<Integer> tierPicker = new WeightedRandomPicker<Integer>();
		tierPicker.add(0, 50);
		tierPicker.add(1, 45);
		tierPicker.add(2, 5);

		int tier = tierPicker.pick();
		
		PersonAPI person = createAdmin(market.getFaction(), tier, null);
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
//		personalityPicker.add(Personalities.TIMID, 2f);
//		personalityPicker.add(Personalities.CAUTIOUS, 2f);
//		personalityPicker.add(Personalities.STEADY, 2f);
//		personalityPicker.add(Personalities.AGGRESSIVE, 2f);
//		personalityPicker.add(Personalities.RECKLESS, 2f);
		
		String personality = personalityPicker.pick();
		person.setPersonality(personality);
		
		person.getStats().setSkipRefresh(false);
		person.getStats().refreshCharacterStatsEffects();
		
		//person.setPortraitSprite(pickPortrait(person.getFaction(), person.getGender()));
		
		return person;
	}
	
	
	protected AvailableOfficer createOfficer() {
		WeightedRandomPicker<MarketAPI> marketPicker = new WeightedRandomPicker<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			marketPicker.add(market, market.getSize());
		}
		MarketAPI market = marketPicker.pick();
		if (market == null) return null;
		
		//FactionAPI faction = Global.getSector().getFaction(Factions.INDEPENDENT);

		int level = (int)(Math.random() * 5) + 1;
		
		PersonAPI person = createOfficerInternal(market.getFaction(), level, true);
		person.setFaction(Factions.INDEPENDENT);
		
		
		int salary = (int) Misc.getOfficerSalary(person);
		AvailableOfficer result = new AvailableOfficer(person, market.getId(), person.getStats().getLevel() * 2000, salary);
		return result;
	}
	
	public static PersonAPI createOfficerInternal(FactionAPI faction, int level, boolean allowNonDoctrinePersonality) {
		return createOfficer(faction, level, false, allowNonDoctrinePersonality);
	}
	
	
	public static enum SkillPickPreference {
		CARRIER,
		NON_CARRIER,
		EITHER,
	}
	
	public static PersonAPI createOfficer(FactionAPI faction, int level, boolean alwaysPickHigherSkill) {
		return createOfficer(faction, level, alwaysPickHigherSkill, SkillPickPreference.EITHER, false);
	}
	public static PersonAPI createOfficer(FactionAPI faction, int level, boolean alwaysPickHigherSkill, boolean allowNonDoctrinePersonality) {
		return createOfficer(faction, level, alwaysPickHigherSkill, SkillPickPreference.EITHER, allowNonDoctrinePersonality);
	}
	public static PersonAPI createOfficer(FactionAPI faction, int level, boolean alwaysPickHigherSkill, SkillPickPreference pref) {
		return createOfficer(faction, level, alwaysPickHigherSkill, pref, false, null);
	}
	public static PersonAPI createOfficer(FactionAPI faction, int level, boolean alwaysPickHigherSkill, SkillPickPreference pref, boolean allowNonDoctrinePersonality) {
		return createOfficer(faction, level, alwaysPickHigherSkill, pref, allowNonDoctrinePersonality, null);
	}
	public static PersonAPI createOfficer(FactionAPI faction, int level, boolean alwaysPickHigherSkill, 
			SkillPickPreference pref, Random random) {
		return createOfficer(faction, level, alwaysPickHigherSkill, pref, false, random);
	}
	public static PersonAPI createOfficer(FactionAPI faction, int level, boolean alwaysPickHigherSkill, 
										  SkillPickPreference pref, boolean allowNonDoctrinePersonality, Random random) {
		if (random == null) random = new Random();
		
		PersonAPI person = faction.createRandomPerson(random);
		OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
		if (level > plugin.getMaxLevel(person)) level = plugin.getMaxLevel(person);
		
		person.getStats().setSkipRefresh(true);
		
		boolean debug = false;
		
		for (int i = 0; i < 2; i++) {
			List<String> skills = plugin.pickLevelupSkills(person, random);
			String skillId = pickSkill(person, skills, alwaysPickHigherSkill, pref, random);
			if (skillId != null) {
				if (debug) System.out.println("Picking initial skill: " + skillId);
				person.getStats().increaseSkill(skillId);
			}
		}
		
//		level = 20;
//		pref = SkillPickPreference.NON_CARRIER;

		if (debug) System.out.println("Generating officer\n");
		
		long xp = plugin.getXPForLevel(level);
		OfficerDataAPI officerData = Global.getFactory().createOfficerData(person);
		officerData.addXP(xp);
		while (officerData.canLevelUp()) {
			String skillId = pickSkill(officerData.getPerson(), officerData.getSkillPicks(), alwaysPickHigherSkill, pref, random);
			if (skillId != null) {
				if (debug) System.out.println("Leveling up " + skillId);
				officerData.levelUp(skillId);
			} else {
				break;
			}
		}
		
		if (debug) System.out.println("Done\n");
		
		person.setRankId(Ranks.SPACE_LIEUTENANT);
		person.setPostId(Ranks.POST_MERCENARY);
		
		
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
	
	public static String pickSkill(PersonAPI person, List<String> skills, boolean preferHigher, SkillPickPreference pref, Random random) {
		if (random == null) random = new Random();
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		
		if (preferHigher) {
			String highestId = null;
			float highestLevel = -1;
			boolean highestPreferred = false;
			for (String id : skills) {
				float curr = person.getStats().getSkillLevel(id);
				SkillSpecAPI spec = Global.getSettings().getSkillSpec(id);
				boolean carrierSkill = spec.hasTag(Tags.SKILL_CARRIER);
				boolean currPreferred = (pref == SkillPickPreference.CARRIER && carrierSkill) || 
										(pref == SkillPickPreference.NON_CARRIER && !carrierSkill);
				if (curr > highestLevel || (currPreferred && !highestPreferred)) {
					highestId = id;
					highestLevel = curr;
					highestPreferred = currPreferred;
				}
			}
			picker.add(highestId);
		} else {
			for (String id : skills) {
				float curr = person.getStats().getSkillLevel(id);
				SkillSpecAPI spec = Global.getSettings().getSkillSpec(id);
				boolean carrierSkill = spec.hasTag(Tags.SKILL_CARRIER);
				boolean currPreferred = (pref == SkillPickPreference.CARRIER && carrierSkill) || 
										(pref == SkillPickPreference.NON_CARRIER && !carrierSkill);
				if (currPreferred) {
					picker.add(id);
				}
			}
			if (picker.isEmpty()) {
				picker.addAll(skills);
			}
		}
		
		return picker.pick();
	}
	
	
	
	
	@Override
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
				
				text.setFontSmallInsignia();
				
				Color hl = Misc.getHighlightColor();
				Color red = Misc.getNegativeHighlightColor();
				
				text.addParagraph("-----------------------------------------------------------------------------");
				
				if (!admin) {
					text.addParagraph("Level: " + (int) stats.getLevel());
					text.highlightInLastPara(hl, "" + (int) stats.getLevel());
				}
				
				for (String skillId : Global.getSettings().getSortedSkillIds()) {
					int level = (int) stats.getSkillLevel(skillId);
					if (level > 0) {
						SkillSpecAPI spec = Global.getSettings().getSkillSpec(skillId);
						String skillName = spec.getName();
						if (spec.isAptitudeEffect()) {
							skillName += " Aptitude";
						}
						text.addParagraph(skillName + ", level " + level);
						text.highlightInLastPara(hl, "" + level);
					}
				}
				
				if (!admin) {
					String personality = Misc.lcFirst(officer.person.getPersonalityAPI().getDisplayName());
					text.addParagraph("Personality: " + personality);
					text.highlightInLastPara(hl, personality);
					text.addParagraph(officer.person.getPersonalityAPI().getDescription());
				}
				
				text.addParagraph("-----------------------------------------------------------------------------");
				
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
			
			return playerFleet.getFleetData().getOfficersCopy().size() >= max;
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
	
	private AvailableOfficer getOfficer(String personId) {
		for (AvailableOfficer officer: available) {
			if (officer.person.getId().equals(personId)) {
				return officer;
			}
		}
		return null;
	}
	
	private AvailableOfficer getAdmin(String personId) {
		for (AvailableOfficer officer: availableAdmins) {
			if (officer.person.getId().equals(personId)) {
				return officer;
			}
		}
		return null;
	}

	public Map<String, String> getTokenReplacements() {
		Map<String, String> map = super.getTokenReplacements();
		return map;
	}

	@Override
	public String[] getHighlights(String stageId) {
		return null;
	}
	
	@Override
	public Color[] getHighlightColors(String stageId) {
		return super.getHighlightColors(stageId);
	}
	
	
	private CampaignEventTarget tempTarget = null;
	
	@Override
	public CampaignEventTarget getEventTarget() {
		if (tempTarget != null) return tempTarget;
		return super.getEventTarget();
	}

	public boolean isDone() {
		return false;
	}
	
	@Override
	public CampaignEventCategory getEventCategory() {
		return CampaignEventCategory.DO_NOT_SHOW_IN_MESSAGE_FILTER;
	}
	
	public boolean showAllMessagesIfOngoing() {
		return false;
	}
}










