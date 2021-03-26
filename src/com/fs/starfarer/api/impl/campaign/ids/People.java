package com.fs.starfarer.api.impl.campaign.ids;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseMissionHub;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;

public class People {

	// Hegemony
	public static String DAUD = "daud";
	public static String HEGEMONY_AGENT = "heg_agent";
	public static String HEGEMONY_GA_OFFICER = "heg_ga_officer";
	public static String RAO = "rao";
	
	// Diktat
	public static String ANDRADA = "andrada";
	
	// Pathers
	public static String COTTON = "cotton";
	
	// pirates
	public static String KANTA = "kanta";
	public static String CYDONIA = "cydonia";
	public static String CLONE_LOKE = "clone_loke";
	
	// Tri-Tachyon
	public static String SUN = "sun";
	public static String TRITACH_FIXER = "tt_fixer";
	public static String ARROYO = "arroyo";
	
	// Galatia Academy
	public static String BAIRD = "baird";
	public static String SEBESTYEN = "sebestyen";
	public static String COUREUSE = "coureuse";
	public static String GARGOYLE = "gargoyle";
	public static String ZAL = "zal";
	
	// gaFC minor characters
	public static String ADONYA = "adonya_coureuse";
	public static String BIONE = "bione_lata";
	public static String CAVIN = "cavin_pharoh";
	public static String LAICAILLE_ARCHON = "laicaille_archon";
	
	// gaPZ
	public static String IBRAHIM = "ibrahim";
	
	// Persean League
	public static String SIYAVONG = "siyavong";
	public static String HORUS_YARIBAY = "horus_yaribay";
	
	
	public static PersonAPI getPerson(String id) {
		return Global.getSector().getImportantPeople().getPerson(id);
	}
	
	public static void create() {
		createFactionLeaders();
		createMiscCharacters();
	}
	
	public static void createMiscCharacters() {
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		MarketAPI market = null;
		
		market =  Global.getSector().getEconomy().getMarket("agreus");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(IBRAHIM);
			person.setFaction(Factions.INDEPENDENT);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_ENTREPRENEUR);
			person.setImportance(PersonImportance.HIGH);
			person.getName().setFirst("Callisto");
			person.getName().setLast("Ibrahim");
			person.addTag(Tags.CONTACT_TRADE);
			person.setVoice(Voices.SPACER);
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));
			person.getStats().setSkillLevel(Skills.SALVAGING, 1);
			person.getStats().setSkillLevel(Skills.BULK_TRANSPORT, 1);
			person.getStats().setSkillLevel(Skills.NAVIGATION, 1);
			
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("eochu_bres");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(ARROYO);
			person.setFaction(Factions.TRITACHYON);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_SENIOR_EXECUTIVE);
			person.setImportance(PersonImportance.HIGH);
			person.getName().setFirst("Rayan");
			person.getName().setLast("Arroyo");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));
			person.getStats().setSkillLevel(Skills.BULK_TRANSPORT, 1);
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
			person.addTag(Tags.CONTACT_TRADE);
			person.addTag(Tags.CONTACT_MILITARY);
			person.setVoice(Voices.BUSINESS);
			
			market.getCommDirectory().addPerson(person, 1); // second after Sun
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("port_tse");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(GARGOYLE);
			person.setFaction(Factions.INDEPENDENT);
			person.setGender(Gender.ANY);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_HACKER);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Gargoyle");
			person.getName().setLast("");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));
			person.getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 1);
			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("new_maxios");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(CLONE_LOKE);
			person.setFaction(Factions.PIRATES);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.CLONE);
			person.setPostId(Ranks.POST_AGENT);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Loke");
			person.getName().setLast("");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));
			
			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("laicaille_habitat");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(COUREUSE);
			person.setFaction(Factions.INDEPENDENT);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_SCIENTIST);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Scylla");
			person.getName().setLast("Coureuse");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));
			person.setVoice(Voices.SCIENTIST);
			
			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("fikenhild");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(ADONYA);
			person.setFaction(Factions.INDEPENDENT);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_PENSIONER);
			person.setImportance(PersonImportance.VERY_LOW);
			person.getName().setFirst("Adonya");
			person.getName().setLast("Coureuse");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));

			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("fikenhild");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(BIONE);
			person.setFaction(Factions.INDEPENDENT);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_CITIZEN);
			person.setImportance(PersonImportance.VERY_LOW);
			person.getName().setFirst("Bione");
			person.getName().setLast("Lata");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));

			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("fikenhild");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(CAVIN);
			person.setFaction(Factions.INDEPENDENT);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_ACTIVIST);
			person.setImportance(PersonImportance.VERY_LOW);
			person.getName().setFirst("Cavin");
			person.getName().setLast("Pharoh");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));

			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("fikenhild");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(SIYAVONG);
			person.setFaction(Factions.PERSEAN);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.AGENT);
			person.setPostId(Ranks.POST_SPECIAL_AGENT);
			person.setImportance(PersonImportance.HIGH);
			person.getName().setFirst("Finlay");
			person.getName().setLast("Siyavong");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));

			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("kazeron");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(HORUS_YARIBAY);
			person.setFaction(Factions.PERSEAN);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_ARISTOCRAT);
			person.setImportance(PersonImportance.HIGH);
			person.setVoice(Voices.ARISTO);
			person.getName().setFirst("Horus");
			person.getName().setLast("Yaribay");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));
			person.addTag(Tags.CONTACT_TRADE);
			person.addTag(Tags.CONTACT_MILITARY);
			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		//market =  Global.getSector().getEconomy().getMarket("kantas_den");
		market =  Global.getSector().getEconomy().getMarket("fikenhild");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(ZAL);
			person.setFaction(Factions.INDEPENDENT);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_UNKNOWN);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Elissa");
			person.getName().setLast("Zal");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters",  person.getId()));

			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("epiphany");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(COTTON);
			person.setFaction(Factions.LUDDIC_PATH);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.BROTHER);
			person.setPostId(Ranks.POST_TERRORIST);
			person.setImportance(PersonImportance.HIGH);
			person.getName().setFirst("Livewell");
			person.getName().setLast("Cotton");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));

			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}

		{
			// TriTach bar encounter after GAKallichore hack
			PersonAPI person = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson(StarSystemGenerator.random);
			person.setId(TRITACH_FIXER);
			person.setRankId(Ranks.SPECIAL_AGENT);
			person.setPostId(Ranks.POST_SPECIAL_AGENT);
			person.setImportance(PersonImportance.MEDIUM);
			person.setVoice(Voices.BUSINESS);
//			person.setGender(StarSystemGenerator.pickGender());
//			String portraitId = person.getId() + "_" + person.getGender().name();
//			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", portraitId));
			ip.addPerson(person);
			
		}
		
		{
			// Hegemony bar encounter after GAKallichore hack
			PersonAPI person = Global.getSector().getFaction(Factions.HEGEMONY).createRandomPerson(StarSystemGenerator.random);
			person.setId(HEGEMONY_AGENT);
			person.setRankId(Ranks.SPECIAL_AGENT);
			person.setPostId(Ranks.POST_SPECIAL_AGENT);
			person.setImportance(PersonImportance.MEDIUM);
			person.setVoice(Voices.SOLDIER);
			ip.addPerson(person);
			
		}
		
		{
			// Officer doing cleanup at GA during the tutorial
			PersonAPI person = Global.getSector().getFaction(Factions.HEGEMONY).createRandomPerson();
			person.setRankId(Ranks.GROUND_LIEUTENANT);
			person.setPostId(Ranks.POST_OFFICER);
			person.setGender(Gender.MALE);
			person.getName().setFirst("Caliban");
			person.getName().setLast("Tseen Ke");
			person.setVoice(Voices.SOLDIER);
			person.setImportance(PersonImportance.MEDIUM);
			person.addTag(Tags.CONTACT_MILITARY);
			person.setId(HEGEMONY_GA_OFFICER);
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "ga_officer"));
			ip.addPerson(person);
		}
		
		/*
		{
			
			// Pirate low-life during GAFindingCoureuse at Kapteyn
			kapteynAgent = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson(genRandom);
			kapteynAgent.setRankId(Ranks.CITIZEN);
			kapteynAgent.setPostId(Ranks.POST_SHADY);
			kapteyn.getCommDirectory().addPerson(kapteynAgent);
			kapteyn.addPerson(kapteynAgent);
			
			PersonAPI person = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson(StarSystemGenerator.random);
			person.setId(TRITACH_FIXER);
			person.setRankId(Ranks.SPECIAL_AGENT);
			person.setPostId(Ranks.POST_SPECIAL_AGENT);
			person.setImportance(PersonImportance.MEDIUM);
			person.setVoice(Voices.BUSINESS);
//			person.setGender(StarSystemGenerator.pickGender());
//			String portraitId = person.getId() + "_" + person.getGender().name();
//			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", portraitId));
			ip.addPerson(person);
			
		}*/
		
		MarketAPI laicaille = Global.getSector().getEconomy().getMarket("laicaille_habitat");
		if (laicaille != null) {
			for (PersonAPI person : laicaille.getPeopleCopy()) {
				if (Ranks.POST_BASE_COMMANDER.equals(person.getPostId())) {
					person.setId(LAICAILLE_ARCHON);
					ip.addPerson(person);
					break;
				}
			}
			// For consistency, in GAFC, use this to get the archon, instead of laicaille.getAdmin():
			//Global.getSector().getImportantPeople().getPerson(People.LAICAILLE_ARCHON);
		}
	}
	

	/**
	 * Called from Galatia.java
	 * @param market
	 */
	public static void createAcademyPersonnel(MarketAPI market) {

		PersonAPI person = Global.getFactory().createPerson();
		person.setId(People.BAIRD);
		person.setImportance(PersonImportance.VERY_HIGH);
		person.setFaction(Factions.INDEPENDENT);
		person.setGender(Gender.FEMALE);
		person.setRankId(Ranks.CITIZEN);
		person.setPostId(Ranks.POST_PROVOST);
		person.getName().setFirst("Anahita");
		person.getName().setLast("Baird");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "baird"));
		
		market.getCommDirectory().addPerson(person, 0);
		market.getCommDirectory().getEntryForPerson(person).setHidden(true);
		market.addPerson(person);
		Global.getSector().getImportantPeople().addPerson(person); // so the person can be retrieved by id
		
		// baird only offers one mission at a time
//		person.getMemoryWithoutUpdate().set(BaseMissionHub.NUM_BONUS_MISSIONS, -100);
//		BaseMissionHub.set(person, new BaseMissionHub(person));
		
		PersonAPI person2 = Global.getFactory().createPerson();
		person2.setId(People.SEBESTYEN);
		person2.setImportance(PersonImportance.MEDIUM);
		person2.setFaction(Factions.INDEPENDENT);
		person2.setGender(Gender.MALE);
		person2.setRankId(Ranks.CITIZEN);
		person2.setPostId(Ranks.POST_ACADEMICIAN);
		person2.getName().setFirst("Alviss");
		person2.getName().setLast("Sebestyen");
		person2.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sebestyen"));
		
		
		market.getCommDirectory().addPerson(person2, 1);
		market.getCommDirectory().getEntryForPerson(person2).setHidden(true);
		market.addPerson(person2);
		Global.getSector().getImportantPeople().addPerson(person2); // so the person can be retrieved by id
		
		// sebestyen offers a bit more work than a normal non-priority contact
		person2.getMemoryWithoutUpdate().set(BaseMissionHub.NUM_BONUS_MISSIONS, 1);
		BaseMissionHub.set(person2, new BaseMissionHub(person2));
		
	}
	
	
	public static void createFactionLeaders() {
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		MarketAPI market = null;
		
		market =  Global.getSector().getEconomy().getMarket("sindria");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId("andrada");
			person.setFaction(Factions.DIKTAT);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.FACTION_LEADER);
			person.setPostId(Ranks.POST_FACTION_LEADER);
			person.setImportance(PersonImportance.VERY_HIGH);
			person.getName().setFirst("Phillip");
			person.getName().setLast("Andrada");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "andrada"));
			person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 1);
			person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 1);
			person.getStats().setSkillLevel(Skills.WEAPON_DRILLS, 1);
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("kantas_den");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId("kanta");
			person.setFaction(Factions.PIRATES);
			person.setGender(Gender.FEMALE);
			//person.setPostId(Ranks.POST_ADMINISTRATOR);
			person.setPostId(Ranks.POST_WARLORD);
			person.setRankId(Ranks.FACTION_LEADER);
			person.setImportance(PersonImportance.VERY_HIGH);
			person.getName().setFirst("Jorien"); // Jorien, but no one calls her that
			person.getName().setLast("Kanta"); // Kanta
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "kanta"));
			person.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 1);
			person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 1);
			person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 1);
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
			
			/* Maybe she's a little removed from day-to-day operations?
			for (PersonAPI p : market.getPeopleCopy()) {
				if (Ranks.POST_ADMINISTRATOR.equals(p.getPostId())) {
					market.removePerson(p);
					ip.removePerson(p);
					market.getCommDirectory().removePerson(p);
					break;
				}
			}
			market.setAdmin(person);*/
			
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId("cydonia");
			person.setFaction(Factions.PIRATES);
			person.setGender(Gender.MALE);
			person.setPostId(Ranks.POST_DOCTOR);
			person.setRankId(Ranks.CITIZEN);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Wyatt");
			person.getName().setLast("Cydonia");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "doctor"));
			person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 1);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			ip.addPerson(person);
		}
		
		
		market =  Global.getSector().getEconomy().getMarket("eochu_bres");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId("sun");
			person.setFaction(Factions.TRITACHYON);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.FACTION_LEADER);
			person.setPostId(Ranks.POST_FACTION_LEADER);
			person.setImportance(PersonImportance.VERY_HIGH);
			person.getName().setFirst("Artemisia");
			person.getName().setLast("Sun");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sun"));
			person.getStats().setSkillLevel(Skills.BULK_TRANSPORT, 1);
			person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 1);
			person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 1);
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
			
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("chicomoztoc");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId("daud");
			person.setFaction(Factions.HEGEMONY);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.FACTION_LEADER);
			person.setPostId(Ranks.POST_FACTION_LEADER);
			person.setImportance(PersonImportance.VERY_HIGH);
			person.getName().setFirst("Baikal");
			person.getName().setLast("Daud");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "baikal"));
			person.getStats().setSkillLevel(Skills.AUXILIARY_SUPPORT, 1); // if a skill uses his quote, you can bet he's going to get the skill
			person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 1);
			person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 1);
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
			
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			ip.addPerson(person);
		}
		 
		market =  Global.getSector().getEconomy().getMarket("ragnar_complex");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId("rao");
			person.setFaction(Factions.HEGEMONY);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.SPACE_ADMIRAL);
			person.setPostId(Ranks.POST_ADMINISTRATOR);
			person.setImportance(PersonImportance.VERY_HIGH);
			person.getName().setFirst("Orcus");
			person.getName().setLast("Rao");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "orcus_rao"));
			person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
			person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
			person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 1);
			person.getStats().setSkillLevel(Skills.SPECIAL_MODIFICATIONS, 1);
			
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			ip.addPerson(person);
		}
	}
	
}
