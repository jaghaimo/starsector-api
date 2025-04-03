package com.fs.starfarer.api.impl.campaign.ids;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.missions.RecoverAPlanetkiller;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseMissionHub;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;

public class People {

	// Hegemony
	public static String DAUD = "daud";
	public static String RIVAS = "rivas";
	public static String HEGEMONY_AGENT = "heg_agent";
	public static String HEGEMONY_GA_OFFICER = "heg_ga_officer";
	public static String RAO = "rao";
	public static String NERIENE_RAO = "neriene_rao";
	public static String CASPIAN = "caspian";
	public static String AUGUSTA_RAO = "augusta_rao";
	public static String MAGNUS = "magnus_cardona";
	public static String SKIRON = "skiron";
	
	// Diktat
	public static String ANDRADA = "andrada";
	public static String SEC_OFFICER = "sec_officer";
	public static String MACARIO = "macario";
	public static String HYDER = "hyder";
	public static String CADEN = "caden";
	public static String RAM = "ram";
	public static String TELL = "tell";
	
	// Pathers
	public static String COTTON = "cotton";
	public static String VIRENS = "virens";
	public static String SEDGE = "sedge";
	public static String ULMUS_POND = "ulmus_pond";
	
	// Luddic Church / CGR
	public static String BORNANEW = "bornanew";
	public static String JASPIS = "jaspis";
	public static String SHRINE_CURATE = "shrine_curate";
	public static String OAK = "oak";
	public static String INITIATE = "initiate";
	public static String STANDFAST = "standfast";
	public static String CEDRA_KEEPFAITH = "cedra_keepfaith";
	public static String LARIX = "larix";
	
	// pirates
	public static String KANTA = "kanta";
	public static String CYDONIA = "cydonia";
	public static String CLONE_LOKE = "clone_loke";
	
	// Tri-Tachyon
	public static String SUN = "sun";
	public static String TRITACH_FIXER = "tt_fixer";
	public static String ARROYO = "arroyo";
	public static String GLAMOR_ROTANEV = "glamor_rotanev";
	public static String LAMECH = "lamech";
	
	// Galatia Academy
	public static String BAIRD = "baird";
	public static String SEBESTYEN = "sebestyen";
	public static String COUREUSE = "coureuse";
	public static String GARGOYLE = "gargoyle";
	public static String ZAL = "zal";
	public static String ELEK = "elek";
	//public static String SIMISOLA = "simisola";
	public static String GA_RECRUITER = "ga_recruiter";
	
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
	public static String MENES_YARIBAY = "menes_yaribay";
	
	public static String FORTUNA_KATO = "fortuna_kato";
	public static String DARDAN_KATO = "dardan_kato";
	public static String IMOINU_KATO = "imoinu_kato";
	
	public static String REYNARD_HANNAN = "reynard_hannan";
	public static String DAMOS_HANNAN = "damos_hannan";
	//public static String IZEL_HANNAN = "izel_hannan";
	
	
	// contacts for LOCR missions
	public static String LOCR_PIRATE = "locr_pirate_contact"; // Pirate
	public static String LOCR_LUDDIC = "locr_luddic_contact"; // Luddic
	public static String LOCR_UTOPIAN = "locr_utopia_contact"; // Utopia
	public static String LOCR_MINER = "locr_miners_contact"; // Miners
	
	// independent & misc
	public static String ROBEDMAN = "robed_man";
	public static String NANOFORGE_ENGINEER = "nanoforge_engineer";
	
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
		
		market =  Global.getSector().getEconomy().getMarket("asharu");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(ROBEDMAN);
			person.setFaction(Factions.INDEPENDENT);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_HERMIT);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Old");
			person.getName().setLast("Man");
			person.addTag(Tags.CONTACT_MILITARY);
			person.setVoice(Voices.SOLDIER);
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "robed_man"));
			person.getStats().setSkillLevel(Skills.OFFICER_TRAINING, 1);
			person.getStats().setSkillLevel(Skills.BEST_OF_THE_BEST, 1);

			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("eochu_bres");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(ARROYO);
			person.setFaction(Factions.TRITACHYON);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.SENIOR_EXECUTIVE);
			person.setPostId(Ranks.POST_ENTREPRENEUR);
			person.setImportance(PersonImportance.HIGH);
			person.getName().setFirst("Rayan");
			person.getName().setLast("Arroyo");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));
			person.getStats().setSkillLevel(Skills.BULK_TRANSPORT, 1);
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
			person.addTag(Tags.CONTACT_TRADE);
			person.addTag(Tags.CONTACT_MILITARY);
			person.setVoice(Voices.BUSINESS);
			
			market.getCommDirectory().addPerson(person, 2); // third, after Zunya 
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("eochu_bres");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(GLAMOR_ROTANEV);
			person.setFaction(Factions.TRITACHYON);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.SENIOR_EXECUTIVE);
			person.setPostId(Ranks.POST_INTELLIGENCE_DIRECTOR);
			person.setImportance(PersonImportance.VERY_HIGH);
			person.getName().setFirst("Zunya");
			person.getName().setLast("Glamor-Rotanev");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters",  person.getId()));
			person.getStats().setSkillLevel(Skills.BULK_TRANSPORT, 1);
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
			person.getStats().setSkillLevel(Skills.OFFICER_MANAGEMENT, 1);
			person.addTag(Tags.CONTACT_TRADE);
			person.addTag(Tags.CONTACT_MILITARY);
			person.setVoice(Voices.BUSINESS);
			
			market.getCommDirectory().addPerson(person, 1); // second after Sun
			market.getCommDirectory().getEntryForPerson(person).setHidden(true); // you'll hear from her people.
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
		
		market =  Global.getSector().getEconomy().getMarket("port_tse");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(LAMECH);
			person.setFaction(Factions.INDEPENDENT);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_SCIENTIST);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Darshan");
			person.getName().setLast("Lamech");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));
			person.getStats().setSkillLevel(Skills.CONTAINMENT_PROCEDURES, 1);
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
			person.setId(FORTUNA_KATO);
			person.setFaction(Factions.PERSEAN);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.HOUSE_LEADER_FEMALE);
			person.setPostId(Ranks.POST_ARISTOCRAT);
			person.setImportance(PersonImportance.HIGH);
			person.setVoice(Voices.ARISTO);
			person.getName().setFirst("Fortuna");
			person.getName().setLast("Kato");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));
			person.addTag(Tags.CONTACT_TRADE);
			person.addTag(Tags.CONTACT_MILITARY);
			person.addTag(Tags.GENS_KATO);
			//market.getCommDirectory().addPerson(person);
			//market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("kazeron");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(HORUS_YARIBAY);
			person.setFaction(Factions.PERSEAN);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.HOUSE_LEADER_MALE);
			person.setPostId(Ranks.POST_ARISTOCRAT);
			person.setImportance(PersonImportance.HIGH);
			person.setVoice(Voices.ARISTO);
			person.getName().setFirst("Horus");
			person.getName().setLast("Yaribay");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));
			person.addTag(Tags.CONTACT_TRADE);
			person.addTag(Tags.CONTACT_MILITARY);
			person.addTag(Tags.GENS_YARIBAY);
			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("olinadu");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(MENES_YARIBAY);
			person.setFaction(Factions.PERSEAN);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.ARISTOCRAT);
			person.setPostId(Ranks.POST_ADMINISTRATOR);
			person.setImportance(PersonImportance.MEDIUM);
			person.setVoice(Voices.ARISTO);
			person.getName().setFirst("Menes");
			person.getName().setLast("Yaribay");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", person.getId()));
			person.addTag(Tags.CONTACT_TRADE);
			person.addTag(Tags.GENS_YARIBAY);
			//market.getCommDirectory().addPerson(person);
			market.getCommDirectory().addPerson(person, 0);
			market.setAdmin(person);
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
		
		market =  Global.getSector().getEconomy().getMarket("gilead");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(BORNANEW);
			person.setFaction(Factions.LUDDIC_CHURCH);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.BROTHER);
			person.setPostId(Ranks.POST_NOVICE); // Paladin, TBH
			person.setImportance(PersonImportance.LOW);
			person.getName().setFirst("Jethro");
			person.getName().setLast("Bornanew");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "jethro_bornanew"));
			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("gilead");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(JASPIS);
			person.setFaction(Factions.LUDDIC_CHURCH);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.MOTHER);
			person.setPostId(Ranks.POST_ARCHCURATE);
			person.setImportance(PersonImportance.HIGH);
			person.getName().setFirst("Sophronia");
			person.getName().setLast("Jaspis");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "jaspis"));

			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("hesperus");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(OAK);
			person.setFaction(Factions.KOL);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.FATHER);
			person.setPostId(Ranks.POST_EXCUBITOR_ORBIS);
			person.setImportance(PersonImportance.HIGH);
			person.getName().setFirst("Gideon");
			person.getName().setLast("Oak");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "gideon_oak"));

			market.getCommDirectory().addPerson(person, 0); // first
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("hesperus");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(INITIATE);
			person.setFaction(Factions.KOL);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.BROTHER);
			person.setPostId(Ranks.POST_INITIATE);
			person.setImportance(PersonImportance.VERY_LOW);
			person.getName().setFirst("Ned");
			person.getName().setLast("Boot");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "initiate"));

			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("volturn");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(STANDFAST);
			person.setFaction(Factions.LUDDIC_CHURCH);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.MOTHER);
			person.setPostId(Ranks.POST_SHRINE_PRIEST);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Moyra");
			person.getName().setLast("Standfast");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "standfast"));

			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("tartessus");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(CEDRA_KEEPFAITH);
			person.setFaction(Factions.LUDDIC_CHURCH);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.SISTER);
			person.setPostId(Ranks.POST_SUBCURATE);
			person.setImportance(PersonImportance.LOW);
			person.getName().setFirst("Cedra");
			person.getName().setLast("Keepfaith");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "cedra_keepfaith"));

			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("tartessus");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(LARIX);
			person.setFaction(Factions.KOL);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.BROTHER);
			person.setPostId(Ranks.KNIGHT_CAPTAIN);
			person.setImportance(PersonImportance.LOW);
			person.getName().setFirst("Dejan");
			person.getName().setLast("Larix");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "larix"));

			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		{
			PersonAPI person = Global.getSector().getFaction(Factions.LUDDIC_CHURCH).createRandomPerson(StarSystemGenerator.random);
			person.setId(SHRINE_CURATE);
			if (person.getGender().equals(Gender.MALE))
			{
				person.setRankId(Ranks.FATHER);
				person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "curate_male"));
			}
			else if (person.getGender().equals(Gender.FEMALE))
			{
				person.setRankId(Ranks.MOTHER);
				person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "curate_female"));
			}
			else
			{
				person.setRankId(Ranks.ELDER);
			}
			
			person.setPostId(Ranks.POST_SHRINE_PRIEST);
			person.setImportance(PersonImportance.MEDIUM);
			person.setVoice(Voices.FAITHFUL);
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
		
		market =  Global.getSector().getEconomy().getMarket("asher");
		if (market != null) {
			// Consulting Nanoforge Engineer
			PersonAPI person = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson(StarSystemGenerator.random);
			person.setId(NANOFORGE_ENGINEER);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_NANOFORGE_ENGINEER);
			person.setImportance(PersonImportance.LOW);
			person.setGender(Gender.FEMALE);
			person.getName().setFirst("Oya");
			person.getName().setLast("Tanaica");
			person.setVoice(Voices.SCIENTIST);
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "nanoforge_engineer"));
			market.getCommDirectory().addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		
		{
			// Luddic splinter group leader
			PersonAPI person = Global.getSector().getFaction(Factions.LUDDIC_CHURCH).createRandomPerson(StarSystemGenerator.random);
			person.setId(LOCR_LUDDIC);
			if( person.getGender() == Gender.FEMALE) person.setRankId(Ranks.SISTER);
			else person.setRankId(Ranks.BROTHER);
			person.setPostId(Ranks.POST_HERETIC);
			person.setImportance(PersonImportance.LOW);
			person.setVoice(Voices.FAITHFUL);
			ip.addPerson(person);
		}
		
		{
			// Lost Miners leader
			PersonAPI person = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson(StarSystemGenerator.random);
			person.setId(LOCR_MINER);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_CREW_BOSS);
			person.setImportance(PersonImportance.LOW);
			person.setVoice(Voices.SPACER);
			ip.addPerson(person);
		}
		
		{
			// Utopian leader
			PersonAPI person = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson(StarSystemGenerator.random);
			person.setId(LOCR_UTOPIAN);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_OUTPOST_COMMANDER);
			person.setImportance(PersonImportance.LOW);
			person.setVoice(Voices.ARISTO);
			ip.addPerson(person);
		}
		
		{
			// Pirate leader
			PersonAPI person = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson(StarSystemGenerator.random);
			person.setId(LOCR_PIRATE);
			person.setRankId(Ranks.SPACE_CAPTAIN);
			person.setPostId(Ranks.POST_CRIMINAL);
			person.setImportance(PersonImportance.LOW);
			person.setVoice(Voices.SPACER);
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
		
		market =  Global.getSector().getEconomy().getMarket("laicaille_habitat");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(DAMOS_HANNAN);
			person.setFaction(Factions.PERSEAN);
			person.setGender(Gender.MALE);
			person.setVoice(Voices.ARISTO);
			person.setRankId(Ranks.GROUND_COLONEL);
			person.setPostId(Ranks.POST_BASE_COMMANDER);
			person.setImportance(PersonImportance.HIGH);
			person.getName().setFirst("Damos");
			person.getName().setLast("Hannan");
			//person.addTag(Tags.CONTACT_TRADE);
			person.addTag(Tags.CONTACT_MILITARY);
			person.addTag(Tags.GENS_HANNAN);
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "damos_hannan"));
			//market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			//market.addPerson(person);
			ip.addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
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
		
		
		PersonAPI person3 = Global.getFactory().createPerson();
		person3.setId(People.ELEK);
		person3.setImportance(PersonImportance.LOW);
		person3.setFaction(Factions.INDEPENDENT);
		person3.setGender(Gender.MALE);
		person3.setRankId(Ranks.CITIZEN);
		person3.setPostId(Ranks.POST_ACADEMICIAN);
		person3.setVoice(Voices.SCIENTIST);
		person3.getName().setFirst("Cornelius");
		person3.getName().setLast("Elek");
		person3.setPortraitSprite(Global.getSettings().getSpriteName("characters", "elek"));
		
		market.getCommDirectory().addPerson(person3);
		market.getCommDirectory().getEntryForPerson(person3).setHidden(true);
		market.addPerson(person3);
		Global.getSector().getImportantPeople().addPerson(person3); // so the person can be retrieved by id
		
		PersonAPI person4 = Global.getFactory().createPerson();
		person4.setId(People.GA_RECRUITER);
		person4.setImportance(PersonImportance.LOW);
		person4.setFaction(Factions.INDEPENDENT);
		person4.setGender(Gender.MALE);
		person4.setRankId(Ranks.CITIZEN);
		person4.setPostId(Ranks.POST_ACADEMICIAN);
		person4.getName().setFirst("Arnaud");
		person4.getName().setLast("Iscare");
		person4.setPortraitSprite(Global.getSettings().getSpriteName("characters", "ga_recruiter"));
		
		market.getCommDirectory().addPerson(person4);
		market.getCommDirectory().getEntryForPerson(person4).setHidden(true);
		market.addPerson(person4);
		Global.getSector().getImportantPeople().addPerson(person4); // so the person can be retrieved by id
		
		/*PersonAPI person5 = Global.getFactory().createPerson();
		person5.setId(People.SIMISOLA);
		person5.setImportance(PersonImportance.LOW);
		person5.setFaction(Factions.INDEPENDENT);
		person5.setGender(Gender.MALE);
		person5.setRankId(Ranks.CITIZEN);
		person5.setPostId(Ranks.POST_ACADEMICIAN);
		person5.setVoice(Voices.SCIENTIST);
		person5.getName().setFirst("Tobe");
		person5.getName().setLast("Simisola");
		person5.setPortraitSprite(Global.getSettings().getSpriteName("characters", "simisola"));
		
		market.getCommDirectory().addPerson(person5);
		market.getCommDirectory().getEntryForPerson(person5).setHidden(true);
		market.addPerson(person5);
		Global.getSector().getImportantPeople().addPerson(person5); // so the person can be retrieved by id*/
	}
	
	
	public static void createFactionLeaders() {
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		MarketAPI market = null;
		
		market =  Global.getSector().getEconomy().getMarket("sindria");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(ANDRADA);
			person.setFaction(Factions.DIKTAT);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.FACTION_LEADER);
			person.setPostId(Ranks.POST_FACTION_LEADER);
			person.setImportance(PersonImportance.VERY_HIGH);
			person.getName().setFirst("Philip");
			person.getName().setLast("Andrada");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "andrada"));
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
//			person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 1);
//			person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 1);
			person.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("sindria");
		if (market != null) {
			// answers the holophone if you call Andrada
			// Spender Balashi
			
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(SEC_OFFICER);
			person.setFaction(Factions.DIKTAT);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.SPACE_LIEUTENANT);
			person.setPostId(Ranks.POST_OFFICER);
			person.setImportance(PersonImportance.VERY_LOW);
			person.setVoice(Voices.SOLDIER);
			person.getName().setFirst("Spender");
			person.getName().setLast("Balashi");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "balashi"));

			market.getCommDirectory().addPerson(person);
			market.addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			ip.addPerson(person);
			
			PersonAPI person2 = Global.getFactory().createPerson();
			person2.setId(MACARIO);
			person2.setFaction(Factions.DIKTAT);
			person2.setGender(Gender.MALE);
			person2.setRankId(Ranks.CHIEF_HIGH_INSPECTOR);
			person2.setPostId(Ranks.POST_INTELLIGENCE_DIRECTOR);
			person2.setImportance(PersonImportance.HIGH);
			person2.setVoice(Voices.VILLAIN);
			person2.getName().setFirst("Dolos");
			person2.getName().setLast("Macario");
			person2.getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 1);
			person2.setPortraitSprite(Global.getSettings().getSpriteName("characters", "dolos_macario"));

			market.getCommDirectory().addPerson(person2);
			market.addPerson(person2);
			market.getCommDirectory().getEntryForPerson(person2).setHidden(true);
			ip.addPerson(person2);
			
			PersonAPI person3 = Global.getFactory().createPerson();
			person3.setId(HYDER);
			person3.setFaction(Factions.DIKTAT);
			person3.setGender(Gender.FEMALE);
			person3.setRankId(Ranks.DEPUTY_STAR_MARSHAL);
			person3.setPostId(Ranks.POST_FLEET_COMMANDER);
			person3.setImportance(PersonImportance.HIGH);
			person3.setVoice(Voices.SOLDIER);
			person3.getName().setFirst("Oxana");
			person3.getName().setLast("Hyder");
			person3.getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
			person3.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
			person3.getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
			person3.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
			person3.getStats().setLevel(4);
			person3.setPortraitSprite(Global.getSettings().getSpriteName("characters", "oxana_hyder"));

			//market.getCommDirectory().addPerson(person3);
			//market.addPerson(person3);
			//market.getCommDirectory().getEntryForPerson(person3).setHidden(true);
			ip.addPerson(person3);
			
			PersonAPI person4 = Global.getFactory().createPerson();
			person4.setId(CADEN);
			person4.setFaction(Factions.DIKTAT);
			person4.setGender(Gender.MALE);
			person4.setRankId(Ranks.GUARD_HIGH_DEPUTY_EXECUTOR);
			person4.setPostId(Ranks.POST_GUARD_LEADER);
			person4.setImportance(PersonImportance.HIGH);
			person4.setVoice(Voices.ARISTO);
			person4.getName().setFirst("Horacio");
			person4.getName().setLast("Caden");
			person4.getStats().setSkillLevel(Skills.CARRIER_GROUP, 1);
			person4.setPortraitSprite(Global.getSettings().getSpriteName("characters", "horacio_caden"));

			//market.getCommDirectory().addPerson(person4);
			//market.addPerson(person4);
			//market.getCommDirectory().getEntryForPerson(person4).setHidden(true);
			ip.addPerson(person4);
			
			PersonAPI person5 = Global.getFactory().createPerson();
			person5.setId(RAM);
			person5.setFaction(Factions.DIKTAT);
			person5.setGender(Gender.MALE);
			person5.setRankId(Ranks.GROUND_CAPTAIN);
			person5.setPostId(Ranks.POST_SPECIAL_AGENT);
			person5.setImportance(PersonImportance.LOW);
			person5.setVoice(Voices.OFFICIAL);
			person5.getName().setFirst("Yannick");
			person5.getName().setLast("Ram");
			person5.setPortraitSprite(Global.getSettings().getSpriteName("characters", "yannick_ram"));

			market.getCommDirectory().addPerson(person5);
			market.addPerson(person5);
			market.getCommDirectory().getEntryForPerson(person5).setHidden(true);
			ip.addPerson(person5);
		}
		
		market =  Global.getSector().getEconomy().getMarket("cruor");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(TELL);
			person.setFaction(Factions.DIKTAT);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.GROUND_GENERAL);
			person.setPostId(Ranks.POST_MILITARY_ADMINISTRATOR);
			person.setImportance(PersonImportance.MEDIUM);
			person.setVoice(Voices.SOLDIER);
			person.getName().setFirst("Laverna");
			person.getName().setLast("Tell");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "laverna_tell"));

			
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			//market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("umbra");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(IMOINU_KATO);
			person.setFaction(Factions.PIRATES);
			person.setGender(Gender.FEMALE);
			person.setPostId(Ranks.POST_SUPPLY_OFFICER);
			person.setRankId(Ranks.FREEDOM_FIGHTER);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Imoinu");
			person.getName().setLast("Kato");
			person.addTag(Tags.GENS_KATO);
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "imoinu_kato"));
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
			market.getCommDirectory().addPerson(person);
			market.addPerson(person);
			ip.addPerson(person);
			assignPost(market, Ranks.POST_SUPPLY_OFFICER , person);
		}
		
		addReynardHannan();
		
		market =  Global.getSector().getEconomy().getMarket("mazalot");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(DARDAN_KATO);
			person.setFaction(Factions.PERSEAN);
			person.setGender(Gender.MALE);
			person.setVoice(Voices.ARISTO);
			person.setPostId(Ranks.POST_ADMINISTRATOR);
			person.setRankId(Ranks.CITIZEN);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Dardan");
			person.getName().setLast("Kato");
			person.addTag(Tags.GENS_KATO);
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "dardan_kato"));
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("mazalot");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(VIRENS);
			person.setFaction(Factions.LUDDIC_PATH);
			person.setGender(Gender.MALE);
			person.setPostId(Ranks.POST_TERRORIST);
			person.setRankId(Ranks.BROTHER);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Nile");
			person.getName().setLast("Virens");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "nile_virens"));
			market.getCommDirectory().addPerson(person);
			market.addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("chalcedon");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(SEDGE);
			person.setFaction(Factions.LUDDIC_PATH);
			person.setGender(Gender.MALE);
			person.setPostId(Ranks.POST_TERRORIST);
			person.setRankId(Ranks.BROTHER);
			person.setImportance(PersonImportance.MEDIUM);
			person.addTag(Tags.CONTACT_MILITARY);
			person.getName().setFirst("Wrestling");
			person.getName().setLast("Sedge");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sedge"));
			person.setVoice(Voices.PATHER);
			market.getCommDirectory().addPerson(person);
			market.addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			ip.addPerson(person);
		}

		market =  Global.getSector().getEconomy().getMarket("olinadu");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(ULMUS_POND);
			person.setFaction(Factions.INDEPENDENT);
			person.setGender(Gender.MALE);
			person.setPostId(Ranks.POST_AGENT);
			person.setRankId(Ranks.CITIZEN); // or brother?
			person.setImportance(PersonImportance.LOW);
			person.addTag(Tags.CONTACT_MILITARY);
			person.getName().setFirst("Ulmus");
			person.getName().setLast("Pond");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "ulmus_pond"));
			person.setVoice(Voices.PATHER);
			market.getCommDirectory().addPerson(person);
			market.addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("kantas_den");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(KANTA);
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
//			person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 1);
//			person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 1);
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
			person.setId(CYDONIA);
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
			person.setId(SUN);
			person.setFaction(Factions.TRITACHYON);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.FACTION_LEADER);
			person.setPostId(Ranks.POST_FACTION_LEADER);
			person.setImportance(PersonImportance.VERY_HIGH);
			person.getName().setFirst("Artemisia");
			person.getName().setLast("Sun");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sun"));
			person.getStats().setSkillLevel(Skills.BULK_TRANSPORT, 1);
//			person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 1);
//			person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 1);
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
			
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("chicomoztoc");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(DAUD);
			person.setFaction(Factions.HEGEMONY);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.FACTION_LEADER);
			person.setPostId(Ranks.POST_FACTION_LEADER);
			person.setImportance(PersonImportance.VERY_HIGH);
			person.getName().setFirst("Baikal");
			person.getName().setLast("Daud");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "baikal"));
//			person.getStats().setSkillLevel(Skills.AUXILIARY_SUPPORT, 1); // if a skill uses his quote, you can bet he's going to get the skill // :( -am
//			person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 1);
//			person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 1);
			person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
			person.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
			
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("chicomoztoc");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(RIVAS);
			person.setFaction(Factions.HEGEMONY);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_SECURITY_CHIEF);
			person.setImportance(PersonImportance.LOW);
			person.getName().setFirst("Alejandro");
			person.getName().setLast("Rivas");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "rivas"));
			//market.getCommDirectory().addPerson(person);
			//market.addPerson(person);
			ip.addPerson(person);
		}
		 
		market =  Global.getSector().getEconomy().getMarket("ragnar_complex");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(RAO);
			person.setFaction(Factions.HEGEMONY);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.SPACE_ADMIRAL);
			person.setPostId(Ranks.POST_MILITARY_ADMINISTRATOR);
			person.setImportance(PersonImportance.VERY_HIGH);
			person.getName().setFirst("Orcus");
			person.getName().setLast("Rao");
			person.addTag(Tags.CONTACT_MILITARY);
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "orcus_rao"));
			person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
			person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
//			person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 1);
//			person.getStats().setSkillLevel(Skills.SPECIAL_MODIFICATIONS, 1);
			
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			ip.addPerson(person);
		}
		
		market =  Global.getSector().getEconomy().getMarket("eventide");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(NERIENE_RAO);
			person.setFaction(Factions.HEGEMONY);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.SPACE_COMMANDER);
			person.setPostId(Ranks.POST_OFFICER);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Neriene");
			person.getName().setLast("Rao");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "neriene_rao"));

			market.getCommDirectory().addPerson(person);
			market.addPerson(person);
			ip.addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
		}
		
		market =  Global.getSector().getEconomy().getMarket("eventide");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(AUGUSTA_RAO);
			person.setFaction(Factions.HEGEMONY);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.CITIZEN);
			person.setPostId(Ranks.POST_ARISTOCRAT);
			person.setImportance(PersonImportance.HIGH);
			person.getName().setFirst("Augusta");
			person.getName().setLast("Rao");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "augusta_rao"));

			market.getCommDirectory().addPerson(person);
			market.addPerson(person);
			ip.addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
		}
		
		market =  Global.getSector().getEconomy().getMarket("eventide");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(CASPIAN);
			person.setFaction(Factions.HEGEMONY);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.SPACE_LIEUTENANT);
			person.setPostId(Ranks.POST_OFFICER);
			person.setImportance(PersonImportance.LOW);
			person.getName().setFirst("Caspian");
			person.getName().setLast("Sang");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "caspian"));

			market.getCommDirectory().addPerson(person);
			market.addPerson(person);
			ip.addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
		}
		
		market =  Global.getSector().getEconomy().getMarket("eventide");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(MAGNUS);
			person.setFaction(Factions.HEGEMONY);
			person.setGender(Gender.MALE);
			person.setRankId(Ranks.SPACE_COMMANDER);
			person.setPostId(Ranks.POST_OFFICER);
			person.setImportance(PersonImportance.LOW);
			person.getName().setFirst("Magnus");
			person.getName().setLast("Cardona");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "magnus_cardona"));

			market.getCommDirectory().addPerson(person);
			market.addPerson(person);
			ip.addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);
		}
		
		market = RecoverAPlanetkiller.getTundraMarket(); //  Global.getSector().getEconomy().getMarket("sentinel");
		if (market != null) {
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(SKIRON);
			person.setFaction(Factions.HEGEMONY);
			person.setGender(Gender.FEMALE);
			person.setRankId(Ranks.SPACE_COMMANDER);
			person.setPostId(Ranks.POST_ADMINISTRATOR);
			person.setImportance(PersonImportance.MEDIUM);
			person.getName().setFirst("Alo");
			person.getName().setLast("Skiron");
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "skiron"));

			
			market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			ip.addPerson(person);
			
			/*market.getCommDirectory().addPerson(person);
			market.addPerson(person);
			ip.addPerson(person);
			market.getCommDirectory().getEntryForPerson(person).setHidden(true);*/
		}
		
		
	}
	
	
	public static void addReynardHannan() {
		MarketAPI market =  Global.getSector().getEconomy().getMarket("kazeron");
		if (market != null) {
			ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
			
			PersonAPI person = Global.getFactory().createPerson();
			person.setId(REYNARD_HANNAN);
			person.setFaction(Factions.PERSEAN);
			person.setGender(Gender.MALE);
			person.setVoice(Voices.ARISTO);
			person.setRankId(Ranks.HOUSE_LEADER_MALE);
			person.setPostId(Ranks.FACTION_LEADER);
			person.setImportance(PersonImportance.VERY_HIGH);
			person.getName().setFirst("Reynard");
			person.getName().setLast("Hannan");
			person.addTag(Tags.CONTACT_TRADE);
			person.addTag(Tags.CONTACT_MILITARY);
			person.addTag(Tags.GENS_HANNAN);
			person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "reynard_hannan"));
			//market.setAdmin(person);
			market.getCommDirectory().addPerson(person, 0);
			market.addPerson(person);
			ip.addPerson(person);
		}		
	}

	/**
	 * Removes any people with this same post from the market.
	 * @param market
	 * @param postId
	 * @param person
	 */
	public static void assignPost(MarketAPI market, String postId, PersonAPI person) {
		for (PersonAPI curr : market.getPeopleCopy()) {
			if (postId.equals(curr.getPostId())) {
				market.removePerson(curr);
				market.getCommDirectory().removePerson(curr);
			}
		}
		person.setPostId(postId);
		market.addPerson(person);
		market.getCommDirectory().addPerson(person);
	}
}
