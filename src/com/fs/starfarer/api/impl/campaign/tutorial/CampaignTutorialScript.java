package com.fs.starfarer.api.impl.campaign.tutorial;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.TransmitterTrapSpecial;
import com.fs.starfarer.api.ui.HintPanelAPI;
import com.fs.starfarer.api.util.Misc;

public class CampaignTutorialScript implements EveryFrameScript {

	public static final String USE_TUTORIAL_RESPAWN = "$tutorialRespawn";
	
	public static enum CampaignTutorialStage {
		SHOW_WELCOME_DIALOG,
		SHOW_DEBRIS_HINT,
		HEADING_TO_DEBRIS,
		REACHED_DEBRIS,
		SAVE_NAG_1,
		SHOW_PIRATE_DIALOG,
		SHOW_PIRATE_HINT,
		PIRATE_APPROACHES,
		SAVE_NAG_2,
		SHOW_LEVELUP_DIALOG,
		SHOW_LEVELUP_HINT,
		WAIT_CHAR_TAB,
		SHOW_LAY_IN_COURSE_DIALOG,
		SHOW_LAY_IN_COURSE_HINT,
		WAITING_TO_LAY_IN_COURSE,
		SHOW_GO_SLOW_DIALOG,
		SHOW_GO_SLOW_HINT,
		WAITING_TO_GO_SLOW,
		SHOW_SUSTAINED_BURN_DIALOG,
		SHOW_SUSTAINED_BURN_HINT,
		WAIT_SUSTAINED_BURN_USE,
		SHOW_TRANSPONDER_DIALOG,
		SHOW_TRANSPONDER_HINT,
		WAIT_TRANSPONDER_USE,
		DONE,
		
		WAITING_TO_QUICKSAVE,
	}
	
	
	protected boolean askedPlayerToSave = false;
	protected boolean playerSaved = false;
	protected float elapsed = 0f;
	protected float lastCheckDistToAncyra = -1f;
	
	protected StarSystemAPI system;
	protected PlanetAPI ancyra;
	protected SectorEntityToken derinkuyu;
	protected CampaignTutorialStage stage = CampaignTutorialStage.SHOW_WELCOME_DIALOG;
	
	protected boolean orbitalResetDone = false;
	
	protected CampaignTerrainAPI debrisField;
	protected CampaignFleetAPI pirateFleet;
	protected CampaignFleetAPI detachment;
	protected TutorialMissionIntel intel;
	
	public CampaignTutorialScript(StarSystemAPI system) {
		this.system = system;
		debrisField = (CampaignTerrainAPI) system.getEntityById("debris_tutorial");
		ancyra = (PlanetAPI) system.getEntityById("ancyra");
		derinkuyu = system.getEntityById("derinkuyu_station");
		
		Global.getSector().getMemoryWithoutUpdate().set(USE_TUTORIAL_RESPAWN, true);
	}

	protected Object readResolve() {
		return this;
	}
	
	protected Object writeReplace() {
		if (askedPlayerToSave) {
			playerSaved = true;
			HintPanelAPI hints = Global.getSector().getCampaignUI().getHintPanel();
			if (hints != null) {
				hints.clearHints(false);
			}
		}
		return this;
	}
	
	protected CampaignTutorialStage quickSaveFrom = null;
	protected boolean quickSaveNag(CampaignTutorialStage nagStage, CampaignTutorialStage next, float timeout) {
		HintPanelAPI hints = Global.getSector().getCampaignUI().getHintPanel();
		
		if (stage == nagStage) {
			quickSaveFrom = nagStage;
			hints.clearHints();
			
			String control = Global.getSettings().getControlStringForEnumName("QUICK_SAVE");
			if (timeout > 0) {
				hints.setHint(0, "- Press %s to quick-save, if you like", true, Misc.getHighlightColor(), control);
			} else {
				hints.setHint(0, "- Press %s to quick-save and advance the tutorial", true, Misc.getHighlightColor(), control);
			}
			
			stage = CampaignTutorialStage.WAITING_TO_QUICKSAVE;
			elapsed = 0f;
			askedPlayerToSave = true;
			playerSaved = false;
			
			return true;
		}
		
		if (quickSaveFrom == nagStage && stage == CampaignTutorialStage.WAITING_TO_QUICKSAVE && 
				(playerSaved || (timeout > 0 && elapsed > timeout))) {
			hints.clearHints();
			stage = next;
			elapsed = 0f;
			playerSaved = false;
			askedPlayerToSave = false;
			quickSaveFrom = null;
			return true;
		}
		
		return false;
	}
	
	protected boolean charTabWasOpen = false;
	public void advance(float amount) {
		if (Global.getSector().isInFastAdvance()) return;
		
		if (!orbitalResetDone) {
			system.getEntityById("ancyra").setCircularOrbitAngle(55f);
			system.getEntityById("ancyra_relay").setCircularOrbitAngle(55 - 60);;
			
			system.getEntityById("pontus").setCircularOrbitAngle(230);
			system.getEntityById("pontus_L4").setCircularOrbitAngle(230 + 60);
			system.getEntityById("pontus_L5").setCircularOrbitAngle(230 - 60);
			system.getEntityById("galatia_probe").setCircularOrbitAngle(230);
			system.getEntityById("galatia_jump_point_alpha").setCircularOrbitAngle(230 + 180f);
			
			system.getEntityById("tetra").setCircularOrbitAngle(340);
			system.getEntityById("derinkuyu_station").setCircularOrbitAngle(135);
			system.getEntityById("galatia_jump_point_fringe").setCircularOrbitAngle(160);;
			
			orbitalResetDone = true;
		}
		
		
		if (amount == 0) return;
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		if (playerFleet == null) return;
		
		HintPanelAPI hints = Global.getSector().getCampaignUI().getHintPanel();
		if (hints == null) return;
		
		//playerFleet.addAbility(Abilities.SENSOR_BURST);
		
		if (lastCheckDistToAncyra < 0) {
			lastCheckDistToAncyra = Misc.getDistance(playerFleet.getLocation(), ancyra.getLocation());
		}
		
		elapsed += amount;
		
		if (stage == CampaignTutorialStage.SHOW_WELCOME_DIALOG && elapsed > 1f) {
			if (Global.getSector().getCampaignUI().showInteractionDialog(new TutorialWelcomeDialogPluginImpl(), null)) {
				addFleets();
				stage = CampaignTutorialStage.SHOW_DEBRIS_HINT;
			}
			return;
		}
		
		if (stage == CampaignTutorialStage.SHOW_DEBRIS_HINT) {
			String control = Global.getSettings().getControlStringForAbilitySlot(5);
			hints.setHint(1, "- Move up into the debris field");
			hints.setHint(0, "- Press %s to start scavenging", false, Misc.getHighlightColor(), control);
			hints.makeDim(0);
			stage = CampaignTutorialStage.HEADING_TO_DEBRIS;
			return;
		}
		
		if (stage == CampaignTutorialStage.HEADING_TO_DEBRIS) {
			if (debrisField.getPlugin().containsEntity(playerFleet)) {
				stage = CampaignTutorialStage.REACHED_DEBRIS;
				hints.fadeOutHint(1);
				//hints.makeNormal(0);
				String control = Global.getSettings().getControlStringForAbilitySlot(5);
				hints.setHint(0, "- Press %s to start scavenging", true, Misc.getHighlightColor(), control);
			}
			return;
		}
		
		if (stage == CampaignTutorialStage.REACHED_DEBRIS) {
			AbilityPlugin scavenge = playerFleet.getAbility(Abilities.SCAVENGE);
			if (scavenge != null && scavenge.isOnCooldown()) {
				stage = CampaignTutorialStage.SAVE_NAG_1;
			}
			return;
		}
		
		if (quickSaveNag(CampaignTutorialStage.SAVE_NAG_1, CampaignTutorialStage.SHOW_PIRATE_DIALOG, 0)) {
			return;
		}

		if (stage == CampaignTutorialStage.SHOW_PIRATE_DIALOG && elapsed >= 1f) {
			if (Global.getSector().getCampaignUI().showInteractionDialog(new TutorialPirateApproachesDialogPluginImpl(), null)) {
				stage = CampaignTutorialStage.SHOW_PIRATE_HINT;
			}
			return;
		}
		
		if (stage == CampaignTutorialStage.SHOW_PIRATE_HINT) {
			addPirateFleet();
			
			hints.setHint(0, "- Wait for the pirates to approach, then engage and defeat them!");
			stage = CampaignTutorialStage.PIRATE_APPROACHES;
			return;
		}
		
		if (stage == CampaignTutorialStage.PIRATE_APPROACHES) {
			if (pirateFleet == null || !pirateFleet.isAlive()) {
				hints.clearHints();
				stage = CampaignTutorialStage.SAVE_NAG_2;
				elapsed = 0f;
				
				long xp = Global.getSector().getPlayerPerson().getStats().getXP();
				long add = Global.getSettings().getLevelupPlugin().getXPForLevel(2) - xp;
				Global.getSector().getPlayerPerson().getStats().addPoints(1);
				Global.getSector().getPlayerPerson().getStats().addXP(add);
			}
			return;
		}
		
		if (quickSaveNag(CampaignTutorialStage.SAVE_NAG_2, CampaignTutorialStage.SHOW_LEVELUP_DIALOG, 0)) {
			return;
		}
		
		if (stage == CampaignTutorialStage.SHOW_LEVELUP_DIALOG && elapsed >= 1f) {
			if (Global.getSector().getCampaignUI().showInteractionDialog(new TutorialLevelUpDialogPluginImpl(), null)) {
				stage = CampaignTutorialStage.SHOW_LEVELUP_HINT;
			}
			return;
		}
		
		if (stage == CampaignTutorialStage.SHOW_LEVELUP_HINT) {
			String character = Global.getSettings().getControlStringForEnumName("CORE_CHARACTER");
			hints.setHint(0, "- Press %s to open the character tab and consider your options", true, Misc.getHighlightColor(), character);
			stage = CampaignTutorialStage.WAIT_CHAR_TAB;
			return;
		}
		
		if (stage == CampaignTutorialStage.WAIT_CHAR_TAB) {
			CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
			if (tab == CoreUITabId.CHARACTER) {
				charTabWasOpen = true;
			}
			if (charTabWasOpen && !Global.getSector().getCampaignUI().isShowingDialog()) {
				stage = CampaignTutorialStage.SHOW_LAY_IN_COURSE_DIALOG;
				elapsed = 0f;
				hints.clearHints();
			}
		}

		if (stage == CampaignTutorialStage.SHOW_LAY_IN_COURSE_DIALOG && elapsed >= 1f) {
			startTutorialMissionEvent();
			if (Global.getSector().getCampaignUI().showInteractionDialog(
					new TutorialLayInCourseDialogPluginImpl(ancyra.getMarket(), intel.getMainContact()), null)) {
				stage = CampaignTutorialStage.SHOW_LAY_IN_COURSE_HINT;
			}
			return;
		}
		
		
		if (stage == CampaignTutorialStage.SHOW_LAY_IN_COURSE_HINT) {
			String intel = Global.getSettings().getControlStringForEnumName("CORE_INTEL");
			String map = Global.getSettings().getControlStringForEnumName("CORE_MAP");
			String openMap = Global.getSettings().getControlStringForEnumName("SUBTAB_4");
			
			hints.setHint(2, "- Press %s to open the intel tab, and select the mission", false, Misc.getHighlightColor(), intel);
			hints.setHint(1, "- Press %s to open the map on the mission target", false, Misc.getHighlightColor(), openMap);
			hints.setHint(0, "- Click on " + ancyra.getName() + " and select " +
					"%s, then press %s to close the map", false, Misc.getHighlightColor(), "\"Lay in Course\"", map);
			stage = CampaignTutorialStage.WAITING_TO_LAY_IN_COURSE;
			
//			hints.setHint(1, "- Press %s to open the map", Misc.getHighlightColor(), map);
//			hints.setHint(0, "- Find " + ancyra.getName() + ", left-click on it and select " +
//					"\"Lay in Course\", then close the map", Misc.getHighlightColor(), "\"Lay in Course\"");
//			stage = CampaignTutorialStage.WAITING_TO_LAY_IN_COURSE;
			return;
		}
		
		if (stage == CampaignTutorialStage.WAITING_TO_LAY_IN_COURSE) {
			float dist = Misc.getDistance(playerFleet.getLocation(), ancyra.getLocation());
			boolean closedIn = dist < lastCheckDistToAncyra * 0.75f;
			if (closedIn || (playerFleet.getInteractionTarget() != null &&
					playerFleet.getInteractionTarget().getMarket() == ancyra.getMarket())) {
				lastCheckDistToAncyra = dist;
				hints.clearHints();
				stage = CampaignTutorialStage.SHOW_SUSTAINED_BURN_DIALOG;
				elapsed = 0;
			}
			return;
		}

		
		if (stage == CampaignTutorialStage.SHOW_SUSTAINED_BURN_DIALOG && elapsed > 5f) {
			if (Global.getSector().getCampaignUI().showInteractionDialog(new TutorialSustainedBurnDialogPluginImpl(ancyra.getMarket()), null)) {
				stage = CampaignTutorialStage.SHOW_SUSTAINED_BURN_HINT;
			}
			return;
		}
		
		if (stage == CampaignTutorialStage.SHOW_SUSTAINED_BURN_HINT) {
			String control = Global.getSettings().getControlStringForAbilitySlot(4);
			hints.setHint(0, "- Press %s to engage sustained burn", true, Misc.getHighlightColor(), control);
			stage = CampaignTutorialStage.WAIT_SUSTAINED_BURN_USE;
			elapsed = 0;
			return;
		}
		
		if (stage == CampaignTutorialStage.WAIT_SUSTAINED_BURN_USE) {
			AbilityPlugin sb = playerFleet.getAbility(Abilities.SUSTAINED_BURN);
			float dist = Misc.getDistance(playerFleet.getLocation(), ancyra.getLocation());
			boolean closedIn = dist < lastCheckDistToAncyra * 0.75f;
			if ((sb != null && sb.isActive() && elapsed > 5f) || closedIn) {
				lastCheckDistToAncyra = dist;
				hints.clearHints();
				stage = CampaignTutorialStage.SHOW_GO_SLOW_DIALOG;
				elapsed = 0f;
			}
			return;
		}
		
		
		if (stage == CampaignTutorialStage.SHOW_GO_SLOW_DIALOG && 
				Global.getSector().getPlayerFleet().getLocation().length() < 9300) {
			if (Global.getSector().getCampaignUI().showInteractionDialog(new TutorialGoSlowDialogPluginImpl(), null)) {
				stage = CampaignTutorialStage.SHOW_GO_SLOW_HINT;
			}
			return;
		}
		
		if (stage == CampaignTutorialStage.SHOW_GO_SLOW_HINT) {
			String control = Global.getSettings().getControlStringForEnumName("GO_SLOW");
			hints.clearHints();
			hints.setHint(0, "- Press and hold %s to move slowly through the asteroid belt", true, Misc.getHighlightColor(), control);
			stage = CampaignTutorialStage.WAITING_TO_GO_SLOW;
			elapsed = 0;
			return;
		}
		
		
		if (stage == CampaignTutorialStage.WAITING_TO_GO_SLOW && 
				Global.getSector().getPlayerFleet().getLocation().length() < 7850) {
			float dist = Misc.getDistance(playerFleet.getLocation(), ancyra.getLocation());
			boolean closedIn = dist < lastCheckDistToAncyra * 0.75f;
			if (closedIn || (playerFleet.getInteractionTarget() != null &&
					playerFleet.getInteractionTarget().getMarket() == ancyra.getMarket())) {
				lastCheckDistToAncyra = dist;
				hints.clearHints();
				stage = CampaignTutorialStage.SHOW_TRANSPONDER_DIALOG;
				elapsed = 0;
			}
			return;
		}
		
		
		if (stage == CampaignTutorialStage.SHOW_TRANSPONDER_DIALOG) {
			float dist = Misc.getDistance(playerFleet.getLocation(), ancyra.getLocation());
			if (dist < 6000) {
				if (Global.getSector().getCampaignUI().showInteractionDialog(new TutorialTransponderDialogPluginImpl(ancyra.getMarket()), null)) {
					stage = CampaignTutorialStage.SHOW_TRANSPONDER_HINT;
				}
			}
			return;
		}
		
		if (stage == CampaignTutorialStage.SHOW_TRANSPONDER_HINT) {
			String control = Global.getSettings().getControlStringForAbilitySlot(0);
			hints.setHint(0, "- Press %s twice to turn on the transponder", true, Misc.getHighlightColor(), control);
			stage = CampaignTutorialStage.WAIT_TRANSPONDER_USE;
			elapsed = 0;
			return;
		}
		
		if (stage == CampaignTutorialStage.WAIT_TRANSPONDER_USE) {
			AbilityPlugin transponder = playerFleet.getAbility(Abilities.TRANSPONDER);
			if ((transponder != null && transponder.isActive())) {
				hints.clearHints();
				stage = CampaignTutorialStage.DONE;
				elapsed = 0f;
			}
			return;
		}
	}
	
	
	protected void addFleets() {
		addSecurityDetachment();
		
		SectorEntityToken inner = system.getEntityById("galatia_jump_point_alpha");
		SectorEntityToken fringe = system.getEntityById("galatia_jump_point_fringe");
		SectorEntityToken derinkuyu = system.getEntityById("derinkuyu_station");
		
		CampaignFleetAPI g1 = RogueMinerMiscFleetManager.createGuardFleet(false);
		g1.addScript(new TutorialLeashAssignmentAI(g1, system, derinkuyu));
		system.addEntity(g1);
		g1.setLocation(derinkuyu.getLocation().x, derinkuyu.getLocation().y);
		
		CampaignFleetAPI g2 = RogueMinerMiscFleetManager.createGuardFleet(Misc.isEasy());
		
		if (!Misc.isEasy()) {
			FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "venture_Outdated");
			member.setVariant(member.getVariant().clone(), false, false);
			DModManager.setDHull(member.getVariant());
			member.getVariant().addPermaMod(HullMods.COMP_ARMOR, false);
			member.getVariant().addPermaMod(HullMods.FAULTY_GRID, false);
			g2.getFleetData().addFleetMember(member);
		}
		
		g2.getFleetData().sort();
		g2.addScript(new TutorialLeashAssignmentAI(g2, system, inner));
		system.addEntity(g2);
		g2.setLocation(inner.getLocation().x, inner.getLocation().y);
		
		CampaignFleetAPI g3 = RogueMinerMiscFleetManager.createGuardFleet(true);
		g3.addScript(new TutorialLeashAssignmentAI(g3, system, inner));
		system.addEntity(g3);
		g3.setLocation(inner.getLocation().x, inner.getLocation().y);
		
		
		CampaignFleetAPI g4 = RogueMinerMiscFleetManager.createGuardFleet(true);
		g4.addScript(new TutorialLeashAssignmentAI(g4, system, fringe));
		system.addEntity(g4);
		g4.setLocation(fringe.getLocation().x, fringe.getLocation().y);
		
		CampaignFleetAPI g5 = RogueMinerMiscFleetManager.createGuardFleet(true);
		g5.addScript(new TutorialLeashAssignmentAI(g5, system, fringe));
		system.addEntity(g5);
		g5.setLocation(fringe.getLocation().x, fringe.getLocation().y);
		
	}
	
	
	protected void startTutorialMissionEvent() {
		if (intel == null) intel = new TutorialMissionIntel();
	}
	
	protected void addPirateFleet() {
		pirateFleet = RogueMinerMiscFleetManager.createEmptyRogueFleet("Rogue Miner", false);
		pirateFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
		
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "cerberus_d_pirates_Standard");
		pirateFleet.getFleetData().addFleetMember(member);
	
		system.addEntity(pirateFleet);
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		pirateFleet.setLocation(playerFleet.getLocation().x + 750f, playerFleet.getLocation().y + 750f);
		
		TransmitterTrapSpecial.makeFleetInterceptPlayer(pirateFleet, true, true, 100f);
	}
	
	protected void addSecurityDetachment() {
		detachment = FleetFactoryV3.createEmptyFleet(Factions.HEGEMONY, FleetTypes.PATROL_MEDIUM, ancyra.getMarket());
		detachment.setName("Security Detachment");
		detachment.setNoFactionInName(true);
		
		//detachment.getFleetData().addFleetMember("eagle_xiv_Elite");
		detachment.getFleetData().addFleetMember("dominator_XIV_Elite");
		detachment.getFleetData().addFleetMember("mora_Strike");
		detachment.getFleetData().addFleetMember("enforcer_Escort");
		detachment.getFleetData().addFleetMember("enforcer_Assault");
		detachment.getFleetData().addFleetMember("lasher_CS");
		detachment.getFleetData().addFleetMember("lasher_CS");
		
		detachment.clearAbilities();
		detachment.addAbility(Abilities.TRANSPONDER);
		detachment.addAbility(Abilities.GO_DARK);
		detachment.addAbility(Abilities.SENSOR_BURST);
		detachment.addAbility(Abilities.EMERGENCY_BURN);
		
		detachment.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
		detachment.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
		
		system.addEntity(detachment);
		detachment.setLocation(ancyra.getLocation().x, ancyra.getLocation().y);
		
		detachment.addScript(new TutorialLeashAssignmentAI(detachment, system, ancyra));
		
		detachment.setId("tutorial_security_detachment");
	}
	
	

	public boolean isDone() {
		return stage == CampaignTutorialStage.DONE;
	}

	public boolean runWhilePaused() {
		return stage == CampaignTutorialStage.WAIT_CHAR_TAB;
	}

}
