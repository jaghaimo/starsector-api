package com.fs.starfarer.api.impl.campaign.tutorial;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseOnMessageDeliveryScript;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OnMessageDeliveryScript;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI.EntryType;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.comm.MessagePriority;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.JumpPointInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.events.BaseEventPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.SystemBountyIntel;
import com.fs.starfarer.api.impl.campaign.intel.SystemBountyManager;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.HintPanelAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class TutorialMissionEvent extends BaseEventPlugin {
	
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
	protected boolean ended = false;
	
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
	
	protected TutorialMissionStage stage = TutorialMissionStage.INIT;
	
	public void init(String type, CampaignEventTarget eventTarget) {
		super.init(type, eventTarget, false);
	}
	
	@Override
	public void setParam(Object param) {
		//data = (TutorialMissionEventData) param;
	}

	public void startEvent() {
		super.startEvent();
		
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
		
//		mainContact = ancyra.getFaction().createRandomPerson(); 
//		mainContact.setRankId(Ranks.CITIZEN);
//		mainContact.setPostId(Ranks.POST_STATION_COMMANDER);
//		ancyra.getMarket().getCommDirectory().addPerson(mainContact);
		mainContact = createMainContact(ancyra);
		
		dataContact = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson(); 
		dataContact.setRankId(Ranks.AGENT);
		dataContact.setPostId(Ranks.POST_AGENT);
		derinkuyu.getMarket().getCommDirectory().addPerson(dataContact);
		
		String stageId = "start";
		Global.getSector().reportEventStage(this, stageId, Global.getSector().getPlayerFleet(), 
							MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(ancyra)); 

		
		mainContact.getMemoryWithoutUpdate().set("$tut_mainContact", true);
		mainContact.getMemoryWithoutUpdate().set("$tut_eventRef", this);
		Misc.makeImportant(mainContact, REASON);
		
		updateStage(TutorialMissionStage.INIT);
	}
	
	public static PersonAPI createMainContact(PlanetAPI ancyra) {
		PersonAPI mainContact = ancyra.getFaction().createRandomPerson(); 
		mainContact.setRankId(Ranks.CITIZEN);
		mainContact.setPostId(Ranks.POST_STATION_COMMANDER);
		ancyra.getMarket().getCommDirectory().addPerson(mainContact);
		
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
	}
	
	protected void endEvent() {
		ended = true;
		Global.getSector().getMemoryWithoutUpdate().unset(TUT_STAGE);
	}
	
	public void advance(float amount) {
		if (!isEventStarted()) return;
		if (isDone()) return;
		
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
				Global.getSector().reportEventStage(this, "salvage_core_end", Global.getSector().getPlayerFleet(),
						MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(ancyra));
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
			
			if (count >= 5 || wrecks < 3) {
				Global.getSector().reportEventStage(this, "ship_recovery_end", Global.getSector().getPlayerFleet(),
							MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(ancyra));
				Misc.makeImportant(mainContact, REASON);
				Misc.makeUnimportant(tetra, REASON);
				updateStage(TutorialMissionStage.RECOVERED_SHIPS);
			}
		}
		
		if (stage == TutorialMissionStage.GO_STABILIZE) {
			boolean innerStable = inner.getMemoryWithoutUpdate().getExpire(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY) > 0;
			boolean fringeStable = fringe.getMemoryWithoutUpdate().getExpire(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY) > 0;
			
			if (innerStable || fringeStable) {
				Global.getSector().reportEventStage(this, "stabilize_jump_point_done", Global.getSector().getPlayerFleet(),
						MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(ancyra));
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
			Global.getSector().reportEventStage(this, "sneak_start", Global.getSector().getPlayerFleet(),
					MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(derinkuyu));
			
			dataContact.getMemoryWithoutUpdate().set("$tut_dataContact", true);
			dataContact.getMemoryWithoutUpdate().set("$tut_eventRef", this);
			Misc.makeImportant(dataContact, REASON);
			Misc.makeUnimportant(mainContact, REASON);
			
			detachment.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_ALLOW_TOFF, true);
			
			updateStage(TutorialMissionStage.GO_GET_DATA);
			
			saveNag();
		} else if (action.equals("endGetData")) {
			
			Global.getSector().reportEventStage(this, "sneak_end", Global.getSector().getPlayerFleet(),
					MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(ancyra));
			Misc.cleanUpMissionMemory(dataContact.getMemoryWithoutUpdate(), "tut_");
			
			Misc.makeUnimportant(dataContact, REASON);
			Misc.makeImportant(mainContact, REASON);
			
			updateStage(TutorialMissionStage.GOT_DATA);
			
		} else if (action.equals("goSalvage")) {
			Global.getSector().reportEventStage(this, "salvage_core_start", Global.getSector().getPlayerFleet(),
					MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(pontus));
			Misc.makeUnimportant(mainContact, REASON);
			Misc.makeImportant(probe, REASON);
			
			updateStage(TutorialMissionStage.GO_GET_AI_CORE);
			
			saveNag();
		} else if (action.equals("goRecover")) {
			Global.getSector().reportEventStage(this, "ship_recovery_start", Global.getSector().getPlayerFleet(),
					MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(tetra));
			Misc.makeUnimportant(mainContact, REASON);
			Misc.makeImportant(tetra, REASON);
			
			FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "mudskipper_Standard");
			playerFleet.getFleetData().addFleetMember(member);
			AddRemoveCommodity.addFleetMemberGainText(member, dialog.getTextPanel());
			
			updateStage(TutorialMissionStage.GO_RECOVER_SHIPS);
		} else if (action.equals("goStabilize")) {
			Global.getSector().reportEventStage(this, "stabilize_jump_point", Global.getSector().getPlayerFleet(),
					MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(inner));
			Misc.makeUnimportant(mainContact, REASON);
			Misc.makeImportant(inner, REASON);

			addWeaponsToStorage();
			
			inner.getMemoryWithoutUpdate().set(JumpPointInteractionDialogPluginImpl.CAN_STABILIZE, true);
			fringe.getMemoryWithoutUpdate().set(JumpPointInteractionDialogPluginImpl.CAN_STABILIZE, true);
			
			updateStage(TutorialMissionStage.GO_STABILIZE);
			
			saveNag();
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
			Global.getSector().reportEventStage(this, "deliver_message", Global.getSector().getPlayerFleet(),
									MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(jangala));
			
			Misc.makeUnimportant(mainContact, REASON);
			Misc.cleanUpMissionMemory(mainContact.getMemoryWithoutUpdate(), REASON + "_");
			Misc.makeUnimportant(inner, REASON);
			
			jangalaContact.getMemoryWithoutUpdate().set("$tut_jangalaContact", true);
			jangalaContact.getMemoryWithoutUpdate().set("$tut_eventRef", this);
			Misc.makeImportant(jangalaContact, REASON);
			
			updateStage(TutorialMissionStage.DELIVER_REPORT);
			
			endGalatiaPortionOfMission();
			
			Global.getSector().getMemoryWithoutUpdate().unset(CampaignTutorialScript.USE_TUTORIAL_RESPAWN);
			
		} else if (action.equals("reportDelivered")) {
			Global.getSector().reportEventStage(this, "end", Global.getSector().getPlayerFleet(),
					MessagePriority.DELIVER_IMMEDIATELY, createSetMessageLocationScript(jangala));
			
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
	
	public static void endGalatiaPortionOfMission() {
	
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
	
	
	public OnMessageDeliveryScript createSetMessageLocationScript(final SectorEntityToken entity) {
		return new BaseOnMessageDeliveryScript() {
			public void beforeDelivery(CommMessageAPI message) {
				if (entity != null && entity.getContainingLocation() instanceof StarSystemAPI) {
					message.setStarSystemId(entity.getContainingLocation().getId());
				} else {
					message.setStarSystemId(system.getId());
				}
				message.setCenterMapOnEntity(entity);
			}
		};
	}
	

	public Map<String, String> getTokenReplacements() {
		
		Map<String, String> map = super.getTokenReplacements();
		
		addPersonTokens(map, "mainContact", mainContact);
		
		if (dataContact != null) {
			addPersonTokens(map, "dataContact", dataContact);
		}
		
		if (jangalaContact != null) {
			addPersonTokens(map, "jangalaContact", jangalaContact);
		}
		
		//map.put("$sender", "Ancyra Research Facility");
		
		map.put("$systemName", system.getNameWithLowercaseType());

		
		return map;
	}

	@Override
	public String[] getHighlights(String stageId) {
		List<String> result = new ArrayList<String>();
		
		if ("posting".equals(stageId)) {
		} else if ("success".equals(stageId)) {
		} else {
			//addTokensToList(result, "$rewardCredits");
		}
				
		return result.toArray(new String[0]);
	}
	
	@Override
	public Color[] getHighlightColors(String stageId) {
		return super.getHighlightColors(stageId);
	}

	public boolean isDone() {
		return ended;
	}

	public String getEventName() {
		if (stage == TutorialMissionStage.INIT) {
			return "Contact " + mainContact.getPost() + " " + mainContact.getName().getLast();
		}
		if (stage == TutorialMissionStage.DELIVER_REPORT) {
			return "Deliver Report to Jangala";
		}
		if (stage == TutorialMissionStage.DONE) {
			return "Deliver Report to Jangala - completed";
		}
		return "Stabilize the Jump-points";
	}

	
	
	@Override
	public CampaignEventCategory getEventCategory() {
		return CampaignEventCategory.MISSION;
	}

	@Override
	public String getEventIcon() {
		return Global.getSettings().getSpriteName("campaignMissions", "tutorial");
	}

	@Override
	public String getCurrentImage() {
		return ancyra.getFaction().getLogo();
	}

	
	
}



