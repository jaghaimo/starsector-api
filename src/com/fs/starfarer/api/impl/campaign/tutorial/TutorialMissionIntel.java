package com.fs.starfarer.api.impl.campaign.tutorial;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI.EntryType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.JumpPointInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.SystemBountyIntel;
import com.fs.starfarer.api.impl.campaign.intel.SystemBountyManager;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.HintPanelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class TutorialMissionIntel extends BaseIntelPlugin implements EveryFrameScript {
	
	public static final String TUT_STAGE = "$tutStage";
	
	public static final String REASON = "tut";
	
	public static enum TutorialMissionStage {
		INIT,
		GO_GET_DATA,
		GOT_DATA,
		GO_GET_AI_CORE,
		GOT_AI_CORE,
		GO_RECOVER_SHIPS,
		RECOVERED_SHIPS,
		GO_STABILIZE,
		STABILIZED,
		DELIVER_REPORT,
		DONE,
		;
	}
	
	protected float elapsedDays = 0;
	
	//protected TutorialMissionEventData data;
	protected StarSystemAPI system;
	protected PlanetAPI ancyra;
	protected PlanetAPI pontus;
	protected PlanetAPI tetra;
	protected SectorEntityToken derinkuyu;
	protected SectorEntityToken probe;
	protected SectorEntityToken inner;
	protected SectorEntityToken fringe;
	protected SectorEntityToken detachment;
	protected SectorEntityToken relay;
	
	protected PersonAPI mainContact;
	protected PersonAPI dataContact;
	protected PersonAPI jangalaContact;
	protected PlanetAPI jangala;
	
	protected FactionAPI faction;
	
	protected TutorialMissionStage stage = TutorialMissionStage.INIT;
	
	/**
	 * Either this or the tutorial script. Returns false at the "deliver report to Jangala" stage.
	 * @return
	 */
	public static boolean isTutorialInProgress() {
		return Global.getSector().getMemoryWithoutUpdate().contains(CampaignTutorialScript.USE_TUTORIAL_RESPAWN);
	}
	
	public TutorialMissionIntel() {
		faction = Global.getSector().getFaction(Factions.HEGEMONY);
		
		system = Global.getSector().getStarSystem("galatia");
		ancyra = (PlanetAPI) system.getEntityById("ancyra");
		pontus = (PlanetAPI) system.getEntityById("pontus");
		tetra = (PlanetAPI) system.getEntityById("tetra");
		derinkuyu = system.getEntityById("derinkuyu_station");
		probe = system.getEntityById("galatia_probe");
		inner = system.getEntityById("galatia_jump_point_alpha");
		fringe = system.getEntityById("galatia_jump_point_fringe");
		detachment = system.getEntityById("tutorial_security_detachment");
		relay = system.getEntityById("ancyra_relay");
		
		mainContact = createMainContact(ancyra);
		
		dataContact = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson(); 
		dataContact.setRankId(Ranks.AGENT);
		dataContact.setPostId(Ranks.POST_AGENT);
		derinkuyu.getMarket().getCommDirectory().addPerson(dataContact);
		
//		String stageId = "start";
//		Global.getSector().reportEventStage(this, stageId, Global.getSector().getPlayerFleet(), 
//							MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(ancyra)); 

		
		mainContact.getMemoryWithoutUpdate().set("$tut_mainContact", true);
		mainContact.getMemoryWithoutUpdate().set("$tut_eventRef", this);
		Misc.makeImportant(mainContact, REASON);
		
		updateStage(TutorialMissionStage.INIT);
		
		setImportant(true);
		
		Global.getSector().getIntelManager().addIntel(this);
		Global.getSector().addScript(this);
	}
	
	public static PersonAPI createMainContact(PlanetAPI ancyra) {
		PersonAPI mainContact = ancyra.getFaction().createRandomPerson(); 
		mainContact.setRankId(Ranks.CITIZEN);
		mainContact.setPostId(Ranks.POST_STATION_COMMANDER);
		ancyra.getMarket().getCommDirectory().addPerson(mainContact);
		ancyra.getMarket().addPerson(mainContact);
		
		return mainContact;
	}
	
	public static PersonAPI getJangalaContact() {
		StarSystemAPI corvus = Global.getSector().getStarSystem("Corvus");
		PlanetAPI jangala = (PlanetAPI) corvus.getEntityById("jangala");
		
		for (CommDirectoryEntryAPI entry : jangala.getMarket().getCommDirectory().getEntriesCopy()) {
			if (entry.getType() == EntryType.PERSON && entry.getEntryData() instanceof PersonAPI) {
				PersonAPI curr = (PersonAPI) entry.getEntryData();
				if (Ranks.POST_STATION_COMMANDER.equals(curr.getPostId())) {
					return curr;
				}
			}
		}
		return null;
	}
	
	public PersonAPI getMainContact() {
		return mainContact;
	}

	protected void updateStage(TutorialMissionStage stage) {
		this.stage = stage;
		Global.getSector().getMemoryWithoutUpdate().set(TUT_STAGE, stage.name());
		
		if (stage != TutorialMissionStage.INIT) {
			sendUpdateIfPlayerHasIntel(stage, false);
		}
	}
	
	protected void endEvent() {
		endAfterDelay();
		Global.getSector().getMemoryWithoutUpdate().unset(TUT_STAGE);
	}
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		Global.getSector().removeScript(this);
	}

	protected int preRecoverFleetSize = 2;
	
	@Override
	protected void advanceImpl(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player == null) return;
		
		//memory.advance(days);
		elapsedDays += days;
	
		if (probe == null) probe = system.getEntityById("galatia_probe");
		if (tetra == null) tetra = (PlanetAPI) system.getEntityById("tetra");
		if (derinkuyu == null) derinkuyu = system.getEntityById("derinkuyu_station");
		if (inner == null) inner = system.getEntityById("galatia_jump_point_alpha");
		if (fringe == null) fringe = system.getEntityById("galatia_jump_point_fringe");
		if (detachment == null) detachment = system.getEntityById("tutorial_security_detachment");
		
		if (stage == TutorialMissionStage.GO_GET_AI_CORE) {
			int cores = (int) player.getCargo().getCommodityQuantity(Commodities.GAMMA_CORE);
			float distToProbe = Misc.getDistance(player.getLocation(), probe.getLocation());
			if (cores > 0 && (!probe.isAlive() || distToProbe < 300)) {
//				Global.getSector().reportEventStage(this, "salvage_core_end", Global.getSector().getPlayerFleet(),
//						MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(ancyra));
				Misc.makeImportant(mainContact, REASON);
				updateStage(TutorialMissionStage.GOT_AI_CORE);
			}
		}
		
		if (stage == TutorialMissionStage.GO_RECOVER_SHIPS) {
			int count = 0;
			for (FleetMemberAPI member : player.getFleetData().getMembersListCopy()) {
				//if (member.getVariant().getHullSpec().isDHull()) count++;
				count++;
			}
			
			int wrecks = 0;
			for (SectorEntityToken entity : system.getEntitiesWithTag(Tags.SALVAGEABLE)) {
				String id = entity.getCustomEntityType();
				if (id == null) continue;
				if (Entities.WRECK.equals(id)) {
					wrecks ++;
				}
			}
			
			if (count >= preRecoverFleetSize + 2 || wrecks < 3) {
//				Global.getSector().reportEventStage(this, "ship_recovery_end", Global.getSector().getPlayerFleet(),
//							MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(ancyra));
				Misc.makeImportant(mainContact, REASON);
				Misc.makeUnimportant(tetra, REASON);
				updateStage(TutorialMissionStage.RECOVERED_SHIPS);
			}
		}
		
		if (stage == TutorialMissionStage.GO_STABILIZE) {
			boolean innerStable = inner.getMemoryWithoutUpdate().getExpire(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY) > 0;
			boolean fringeStable = fringe.getMemoryWithoutUpdate().getExpire(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY) > 0;
			
			if (innerStable || fringeStable) {
//				Global.getSector().reportEventStage(this, "stabilize_jump_point_done", Global.getSector().getPlayerFleet(),
//						MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(ancyra));
				Misc.makeImportant(mainContact, REASON);
				Misc.makeUnimportant(inner, REASON);
				updateStage(TutorialMissionStage.STABILIZED);
			}
			
		}
	
	}

	@Override
	public boolean callEvent(String ruleId, final InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
		if (action.equals("startGetData")) {
//			Global.getSector().reportEventStage(this, "sneak_start", Global.getSector().getPlayerFleet(),
//					MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(derinkuyu));
			
			dataContact.getMemoryWithoutUpdate().set("$tut_dataContact", true);
			dataContact.getMemoryWithoutUpdate().set("$tut_eventRef", this);
			Misc.makeImportant(dataContact, REASON);
			Misc.makeUnimportant(mainContact, REASON);
			
			detachment.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_ALLOW_TOFF, true);
			
			updateStage(TutorialMissionStage.GO_GET_DATA);
			
			saveNag();
		} else if (action.equals("endGetData")) {
			
//			Global.getSector().reportEventStage(this, "sneak_end", Global.getSector().getPlayerFleet(),
//					MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(ancyra));
			Misc.cleanUpMissionMemory(dataContact.getMemoryWithoutUpdate(), "tut_");
			
			Misc.makeUnimportant(dataContact, REASON);
			Misc.makeImportant(mainContact, REASON);
			
			updateStage(TutorialMissionStage.GOT_DATA);
			
		} else if (action.equals("goSalvage")) {
//			Global.getSector().reportEventStage(this, "salvage_core_start", Global.getSector().getPlayerFleet(),
//					MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(pontus));
			Misc.makeUnimportant(mainContact, REASON);
			Misc.makeImportant(probe, REASON);
			
			updateStage(TutorialMissionStage.GO_GET_AI_CORE);
			
			saveNag();
		} else if (action.equals("goRecover")) {
//			Global.getSector().reportEventStage(this, "ship_recovery_start", Global.getSector().getPlayerFleet(),
//					MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(tetra));
			Misc.makeUnimportant(mainContact, REASON);
			Misc.makeImportant(tetra, REASON);
			
			FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "mudskipper_Standard");
			playerFleet.getFleetData().addFleetMember(member);
			AddRemoveCommodity.addFleetMemberGainText(member, dialog.getTextPanel());
			
			preRecoverFleetSize = playerFleet.getFleetData().getNumMembers();
			
			updateStage(TutorialMissionStage.GO_RECOVER_SHIPS);
		} else if (action.equals("goStabilize")) {
//			Global.getSector().reportEventStage(this, "stabilize_jump_point", Global.getSector().getPlayerFleet(),
//					MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(inner));
			Misc.makeUnimportant(mainContact, REASON);
			Misc.makeImportant(inner, REASON);

			addWeaponsToStorage();
			
			inner.getMemoryWithoutUpdate().set(JumpPointInteractionDialogPluginImpl.CAN_STABILIZE, true);
			fringe.getMemoryWithoutUpdate().set(JumpPointInteractionDialogPluginImpl.CAN_STABILIZE, true);
			
			updateStage(TutorialMissionStage.GO_STABILIZE);
			
			saveNag();
//		} else if (action.equals("addStipend")) {
		} else if (action.equals("pickJangalaContact")) {
			
			StarSystemAPI corvus = Global.getSector().getStarSystem("Corvus");
			jangala = (PlanetAPI) corvus.getEntityById("jangala");
			
			jangalaContact = getJangalaContact();
			
			MemoryAPI mem = mainContact.getMemoryWithoutUpdate();
			mem.set("$jangalaContactPost", jangalaContact.getPost().toLowerCase(), 0);
			mem.set("$jangalaContactLastName", jangalaContact.getName().getLast(), 0);

			float distLY = Misc.getDistanceLY(playerFleet.getLocationInHyperspace(), jangala.getLocationInHyperspace());
			distLY += 4f;

			float fuel = playerFleet.getLogistics().getFuelCostPerLightYear() * distLY;
			fuel = (float) (Math.ceil(fuel / 10) * 10);
			mem.set("$jangalaFuel", (int) fuel);
		} else if (action.equals("deliverReport")) {
//			Global.getSector().reportEventStage(this, "deliver_message", Global.getSector().getPlayerFleet(),
//									MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(jangala));
			
			Misc.makeUnimportant(mainContact, REASON);
			Misc.cleanUpMissionMemory(mainContact.getMemoryWithoutUpdate(), REASON + "_");
			Misc.makeUnimportant(inner, REASON);
			
			jangalaContact.getMemoryWithoutUpdate().set("$tut_jangalaContact", true);
			jangalaContact.getMemoryWithoutUpdate().set("$tut_eventRef", this);
			Misc.makeImportant(jangalaContact, REASON);
			
			updateStage(TutorialMissionStage.DELIVER_REPORT);
			
			endGalatiaPortionOfMission(true);
			
			Global.getSector().getMemoryWithoutUpdate().unset(CampaignTutorialScript.USE_TUTORIAL_RESPAWN);
			
		} else if (action.equals("reportDelivered")) {
//			Global.getSector().reportEventStage(this, "end", Global.getSector().getPlayerFleet(),
//					MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(jangala));
			
			Misc.makeUnimportant(jangalaContact, REASON);
			Misc.cleanUpMissionMemory(jangalaContact.getMemoryWithoutUpdate(), REASON + "_");
			
			updateStage(TutorialMissionStage.DONE);
			
			MarketAPI jangala = Global.getSector().getEconomy().getMarket("jangala");
			
			if (jangala != null) {
				SystemBountyManager.getInstance().addOrResetBounty(jangala);
			}
			
			endEvent();
		} else if (action.equals("printRefitHint")) {
			String refit = Global.getSettings().getControlStringForEnumName("CORE_REFIT");
			String autofit = Global.getSettings().getControlStringForEnumName("REFIT_MANAGE_VARIANTS");
			String transponder = "";
			if (!playerFleet.isTransponderOn()) {
				transponder = "\n\nAlso: you'll need to re-dock with your transponder turned on to take advantage of Ancyra's facilities.";
			}
			dialog.getTextPanel().addPara("(Once this conversation is over, press %s to open the refit screen. " +
					"After selecting a specific ship, you can press %s to %s - pick a desired loadout, " +
					"and the ship will be automatically refitted to match it, using what weapons are available." + 
					transponder + "",
					Misc.getHighlightColor(), refit, autofit, "\"autofit\"");
			
			dialog.getTextPanel().addPara("In addition, you now have access to local storage at Ancyra, " +
					"and some weapons and supplies have been placed there. To access it, click on the " +
					"\"Storage\" button in the trade screen.)",
					Misc.getHighlightColor(), refit, autofit, "\"Storage\"");
		}
		
		return true;
	}
	
	public static void endGalatiaPortionOfMission(boolean withStipend) {
	
		if (withStipend) {
			new GalatianAcademyStipend();
		}
		
		StarSystemAPI system = Global.getSector().getStarSystem("galatia");
		PlanetAPI ancyra = (PlanetAPI) system.getEntityById("ancyra");
		PlanetAPI pontus = (PlanetAPI) system.getEntityById("pontus");
		PlanetAPI tetra = (PlanetAPI) system.getEntityById("tetra");
		SectorEntityToken derinkuyu = system.getEntityById("derinkuyu_station");
		SectorEntityToken probe = system.getEntityById("galatia_probe");
		SectorEntityToken inner = system.getEntityById("galatia_jump_point_alpha");
		SectorEntityToken fringe = system.getEntityById("galatia_jump_point_fringe");
		SectorEntityToken relay = system.getEntityById("ancyra_relay");
		
		relay.getMemoryWithoutUpdate().unset(MemFlags.OBJECTIVE_NON_FUNCTIONAL);
		
		Global.getSector().getCharacterData().addAbility(Abilities.TRANSPONDER);
		Global.getSector().getCharacterData().addAbility(Abilities.GO_DARK);
		Global.getSector().getCharacterData().addAbility(Abilities.SENSOR_BURST);
		Global.getSector().getCharacterData().addAbility(Abilities.EMERGENCY_BURN);
		Global.getSector().getCharacterData().addAbility(Abilities.SUSTAINED_BURN);
		Global.getSector().getCharacterData().addAbility(Abilities.SCAVENGE);
		Global.getSector().getCharacterData().addAbility(Abilities.INTERDICTION_PULSE);
		Global.getSector().getCharacterData().addAbility(Abilities.DISTRESS_CALL);
		
		FactionAPI hegemony = Global.getSector().getFaction(Factions.HEGEMONY);
		if (hegemony.getRelToPlayer().getRel() < 0) {
			hegemony.getRelToPlayer().setRel(0);
		}
		
		// removing this leaves "supplement from local resources" bonuses active
		//ancyra.getMarket().removeSubmarket(Submarkets.LOCAL_RESOURCES);
		
		Global.getSector().getEconomy().addMarket(ancyra.getMarket(), false);
		Global.getSector().getEconomy().addMarket(derinkuyu.getMarket(), false);
		
		HintPanelAPI hints = Global.getSector().getCampaignUI().getHintPanel();
		if (hints != null) {
			hints.clearHints(false);
		}
		
		if (!SystemBountyManager.getInstance().isActive(ancyra.getMarket())) {
			SystemBountyManager.getInstance().addActive(new SystemBountyIntel(ancyra.getMarket()));
		}
//		CampaignEventManagerAPI eventManager = Global.getSector().getEventManager();
//		eventManager.startEvent(new CampaignEventTarget(ancyra.getMarket()), Events.SYSTEM_BOUNTY, null);
		
		RogueMinerMiscFleetManager script = new RogueMinerMiscFleetManager(derinkuyu);
		for (int i = 0; i < 20; i++) {
			script.advance(1f);
		}
		system.addScript(script);
		
		for (CampaignFleetAPI fleet : system.getFleets()) {
			if (Factions.PIRATES.equals(fleet.getFaction().getId())) {
				fleet.removeScriptsOfClass(TutorialLeashAssignmentAI.class);
			}
		}
		
		inner.getMemoryWithoutUpdate().unset(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY);
		inner.getMemoryWithoutUpdate().unset(JumpPointInteractionDialogPluginImpl.CAN_STABILIZE);
		
		fringe.getMemoryWithoutUpdate().unset(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY);
		fringe.getMemoryWithoutUpdate().unset(JumpPointInteractionDialogPluginImpl.CAN_STABILIZE);
		
		system.removeTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER);
		
		MarketAPI market = ancyra.getMarket();
		market.getMemoryWithoutUpdate().unset(MemFlags.MARKET_DO_NOT_INIT_COMM_LISTINGS);
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).unmodifyMult("tut");
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodifyMult("tut");
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodifyMult("tut");
		market.setEconGroup(null);
		
		derinkuyu.getMarket().setEconGroup(null);
	}
	
	
	
	
	protected void saveNag() {
		if (!Global.getSector().hasScript(SaveNagScript.class)) {
			Global.getSector().addScript(new SaveNagScript(10f));
		}
	}
	
	
	public void addWeaponsToStorage() {
		StoragePlugin plugin = ((StoragePlugin)ancyra.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin());
		plugin.setPlayerPaidToUnlock(true);
		
		CargoAPI cargo = plugin.getCargo();
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		for (FleetMemberAPI member : player.getFleetData().getMembersListCopy()) {
			if (!member.getVariant().hasTag(Tags.SHIP_RECOVERABLE)) continue;
			
			//if (member.getVariant().getHullSpec().isDHull()) {
				for (WeaponSlotAPI slot : member.getVariant().getHullSpec().getAllWeaponSlotsCopy()) {
					//if (member.getVariant().getWeaponId(slot.getId()) == null) {
						String weaponId = getWeaponForSlot(slot);
						if (weaponId != null) {
							cargo.addWeapons(weaponId, 1);
						}
					//}
				}
			//}
		}
		
		cargo.addFighters("broadsword_wing", 1);
		cargo.addFighters("piranha_wing", 1);
		
		cargo.addSupplies(50);
		cargo.sort();
	}
	
	public String getWeaponForSlot(WeaponSlotAPI slot) {
		switch (slot.getWeaponType()) {
		case BALLISTIC:
		case COMPOSITE:
		case HYBRID:
		case UNIVERSAL:
			switch (slot.getSlotSize()) {
			case LARGE: return pick("mark9", "hephag", "hellbore");
			case MEDIUM: return pick("arbalest", "heavymortar", "shredder");
			case SMALL: return pick("lightmg", "lightac", "lightmortar");
			}
			break;
		case MISSILE:
		case SYNERGY:
			switch (slot.getSlotSize()) {
			case LARGE: return pick("hammerrack");
			case MEDIUM: return pick("pilum", "annihilatorpod");
			case SMALL: return pick("harpoon", "sabot", "annihilator");
			}
			break;
		case ENERGY:
			switch (slot.getSlotSize()) {
			case LARGE: return pick("autopulse", "hil");
			case MEDIUM: return pick("miningblaster", "gravitonbeam", "pulselaser");
			case SMALL: return pick("mininglaser", "taclaser", "pdlaser", "ioncannon");
			}
			break;
		}
		
	
		return null;
	}
	
	public String pick(String ...strings) {
		return strings[new Random().nextInt(strings.length)];
	}
	
	
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color c = getTitleColor(mode);
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		info.setParaSmallInsignia();
		info.addPara(getName(), c, 0f);
		info.setParaFontDefault();

		addBulletPoints(info, mode);
	}
		
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color c = getTitleColor(mode);
		
		float opad = 10f;
		
		float pad = 3f;
		if (mode == ListInfoMode.IN_DESC) pad = 10f;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		
		switch (stage) {
		case INIT:
			info.addPara("Contact " + getMainContactPostName() + " at Ancyra", tc, pad);
			break;
		case GO_GET_DATA:
			info.addPara("Sneak into " + derinkuyu.getName(), tc, pad);
			info.addPara("Contact " + dataContact.getNameString() + " and retreive data", tc, 0f);
			break;
		case GOT_DATA:
			info.addPara("Deliver data to " + getMainContactPostName() + " at Ancyra", tc, pad);
			break;
		case GO_GET_AI_CORE:
			info.addPara("Retreive AI core from derelict probe beyond the orbit of Pontus", tc, pad);
			break;
		case GOT_AI_CORE:
			info.addPara("Deliver AI core to " + getMainContactPostName() + " at Ancyra", tc, pad);
			break;
		case GO_RECOVER_SHIPS:
			info.addPara("Recover at least %s ships at Tetra", pad, tc, h, "" + 2);
			break;
		case RECOVERED_SHIPS:
			info.addPara("Return to " + getMainContactPostName() + " with the ships", tc, pad);
			break;
		case GO_STABILIZE:
			info.addPara("Stabilize the inner-system jump-point", tc, pad);
			break;
		case STABILIZED:
			info.addPara("Return to Ancyra and report your success", tc, pad);
			break;
		case DELIVER_REPORT:
			info.addPara("Deliver report to " + getJangalaContactPostName() + " at Jangala", tc, pad);
			break;
		case DONE:
			info.addPara("Completed", pad);
			break;
		}
		
		unindent(info);
	}
	
	public String getName() {
		return "Stabilize the Jump-points";
	}
	
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return faction;
	}

	protected String getMainContactPostName() {
		return mainContact.getPost() + " " + mainContact.getNameString();
	}
	protected String getJangalaContactPostName() {
		return jangalaContact.getPost() + " " + jangalaContact.getNameString();
	}
	
	public String getSmallDescriptionTitle() {
		return getName();
	}
	
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		//info.addImage(dataContact.getFaction().getLogo(), width, 256 / 1.6f, opad);

		boolean addedBullets = false;
		switch (stage) {
		case INIT:
			info.addPara("You receive a tight-beam communication from the system's main inhabited world, Ancyra.", opad);
			
			info.addPara("The message is brief and asks you to travel there and contact " +
						 getMainContactPostName() + " as soon as possible.", opad);
			break;
		case GO_GET_DATA:
			info.addPara("Contact " + dataContact.getNameString() + " at Derinkuyu Mining Station " +
					"to acquire the raw jump-point readings.", opad);
			
			info.addPara("Contact must be made with the transponder off as the miners of Derinkuyu have " +
					"turned pirate and your fleet will be attacked otherwise.", opad);

//			addBulletPoints(info, true, tc);
//			addedBullets = true;
			
			info.addPara("Use %s to avoid detection, and %s to get away if you are seen.", opad, h,
					"Go Dark", "Emergency Burn");
			break;
		case GOT_DATA:
			info.addPara("Return to Ancyra and contact " + getMainContactPostName() + 
						 " to deliver the jump-point data.", opad);
			break;
		case GO_GET_AI_CORE:
			info.addPara("Analyzing the jump-point data requires an AI Core.", opad);

			info.addPara("There's a Domain-era survey probe outside the orbit of Pontus. If salvaged, " +
						 "it's likely to yield a gamma AI core, which should be sufficient for the task.", opad);
			
			info.addPara("Go to Pontus, head out towards the asteroid belt, and then use an " +
						 "%s to locate the probe. ", opad, h, "Active Sensor Burst");

			info.addPara("Approach the probe and salvage it to recover the gamma core.", opad);

			info.addPara("It's likely that you will have to overcome some automated defenses first.", opad);
			break;
		case GOT_AI_CORE:
			info.addPara("Return to Ancyra and contact " + getMainContactPostName() + 
						 " to deliver the AI core.", opad);
			break;
		case GO_RECOVER_SHIPS:
			info.addPara("Go to the ship graveyard around Tetra and recover as many ships as possible.", opad);

			info.addPara("Bring extra crew to man the recovered ships, and extra supplies to " +
						 " help restore their combat readiness.", opad);
			break;
		case RECOVERED_SHIPS:
			info.addPara("Return to " + getMainContactPostName() + 
						 " at Ancyra for help with outfitting the recovered ships with weapons.", opad);
			break;
		case GO_STABILIZE:
			info.addPara("Use the hyperwave sequence produced by the AI core " +
					"to stabilize the inner-system jump-point.", opad);

			info.addPara("You will have to defeat the pirates guarding it first.", opad);
			break;
		case STABILIZED:
			info.addPara("Galatia's connection with hyperspace has been restored, " +
					"and trade fleets are once again able to enter and leave the system.", opad);

			info.addPara("The Derinkuyu leadership will surely soon be toppled by rank-and-file " +
						 "miners eager to get on the right side of the law once again.", opad);
			break;
		case DELIVER_REPORT:
			info.addPara(Misc.ucFirst(getMainContactPostName()) + " has tasked you with delivering a report " +
					"detailing the recent events to the " + getJangalaContactPostName() + " at Jangala.", opad);

			info.addPara("Make sure you have enough fuel to make the trip successfully.", opad);
			break;
		case DONE:
			info.addPara("You have delivered the report and " +
						 "your standing with the Hegemony has increased substantially.", opad);
			
			info.addPara("Galatia's connection with hyperspace has been restored, " +
						 "and trade fleets are once again able to enter and leave the system.", opad);
			break;
		}
		
//		if (!addedBullets) {
//			addBulletPoints(info, true, tc);
//		}
		
	}
	
	public String getIcon() {
		return Global.getSettings().getSpriteName("campaignMissions", "tutorial");
	}
	

	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_STORY);
		tags.add(Factions.HEGEMONY);
		return tags;
	}

	@Override
	public String getCommMessageSound() {
		if (isSendingUpdate()) {
			return getSoundStandardUpdate();
		}
		return getSoundMajorPosting();
	}
	

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		switch (stage) {
		case INIT: return ancyra;
		case GO_GET_DATA: return derinkuyu;
		case GOT_DATA: return ancyra;
		case GO_GET_AI_CORE: return pontus;
		case GOT_AI_CORE: return ancyra;
		case GO_RECOVER_SHIPS: return tetra;
		case RECOVERED_SHIPS: return ancyra;
		case GO_STABILIZE: return inner;
		case STABILIZED: return ancyra;
		case DELIVER_REPORT: return jangala;
		case DONE: return ancyra;
		}
		
		return ancyra;
	}

		
	
	@Override
	public boolean canTurnImportantOff() {
		return isEnding();
	}

	@Override
	public IntelSortTier getSortTier() {
		return IntelSortTier.TIER_2;
	}
	
	@Override
	public String getSortString() {
		return getName();
	}
	

	public boolean runWhilePaused() {
		return false;
	}

	
	@Override
	public boolean isHidden() {
		return false;
	}
	
	
}



