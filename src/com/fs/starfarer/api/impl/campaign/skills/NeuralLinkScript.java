package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Mouse;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.NeuralInterface;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;

public class NeuralLinkScript extends BaseEveryFrameCombatPlugin {
	public static String TRANSFER_CONTROL = "SHIP_TOGGLE_XPAN_MODE";
	
	public static float INSTANT_TRANSFER_DP = 50;
	public static float TRANSFER_SECONDS_PER_DP = 0.125f;
	public static float TRANSFER_MAX_SECONDS = 5f;
	public static float TRANSFER_MIN_SECONDS_IF_NOT_INSTANT = 1f;
	
	// doesn't really work out - disables shields etc, and you also can't just
	// switch to see how the ship is doing without really disrupting it
	public static boolean ALLOW_ENGINE_CONTROL_DURING_TRANSFER = false;
	
	public static final Object KEY_STATUS = new Object();
	public static final Object KEY_STATUS2 = new Object();
	
	public static final String TRANSFER_COMPLETE_KEY = "neural_transfer_complete_key";
	
	
	public static class SavedShipControlState {
		public ShipAPI ship;
		public Map<WeaponGroupAPI, Boolean> autofiring = new LinkedHashMap<WeaponGroupAPI, Boolean>();
		public WeaponGroupAPI selected;
	}
	
	
	
	protected CombatEngineAPI engine;
	public void init(CombatEngineAPI engine) {
		this.engine = engine;
	}
	
	protected ShipAPI prevPlayerShip = null;
	protected int skipFrames = 0;
	
	protected List<ShipAPI> linked = new ArrayList<ShipAPI>();
	
	protected float untilTransfer;
	protected int lastShownTime = 0;
	
	protected SavedShipControlState prevState;
	protected SavedShipControlState savedState;
	
	protected PersonAPI playerPerson;

	public void saveControlState(ShipAPI ship) {
		prevState = savedState;
		savedState = new SavedShipControlState();
		savedState.ship = ship;
		for (WeaponGroupAPI group : ship.getWeaponGroupsCopy()) {
			savedState.autofiring.put(group, group.isAutofiring());
		}
		savedState.selected = ship.getSelectedGroupAPI();
	}
	public void restoreControlState(ShipAPI ship) {
		if (ship == null || prevState == null || prevState.ship != ship) {
			return;
		}
		for (WeaponGroupAPI group : ship.getWeaponGroupsCopy()) {
			Boolean auto = prevState.autofiring.get(group);
			if (auto == null) auto = false;
			if (auto) {
				group.toggleOn();
			} else {
				group.toggleOff();
			}
		}
		int index = ship.getWeaponGroupsCopy().indexOf(prevState.selected);
		if (index > 0) {
			ship.giveCommand(ShipCommand.SELECT_GROUP, null, index);
		}
	}
	
	public void advance(float amount, List<InputEventAPI> events) {
		if (engine == null) return;
		if (engine.isPaused()) return;
		
		
		ShipAPI playerShip = engine.getPlayerShip();
		if (playerShip == null) {
			return;
		}

		if (!playerShip.isAlive()) {
			untilTransfer = 0f;
			lastShownTime = 0;
		}
		
		if (untilTransfer > 0) {
			float timeMult = playerShip.getMutableStats().getTimeMult().getModifiedValue();
			untilTransfer -= amount * timeMult;
			
			Global.getSoundPlayer().applyLowPassFilter(0.75f, 0f);
//			Global.getSoundPlayer().applyLowPassFilter(1f - (1f - spec.getFilterGain()) * level * mult, 
//					1f - (1f - spec.getFilterGainHF()) * level * mult);
			//engine.getCombatUI().setShipInfoFanOutBrightness(0f);
			engine.getCombatUI().hideShipInfo();
			if (untilTransfer <= 0) {
				untilTransfer = 0;
				Global.getSoundPlayer().playUISound("ui_neural_transfer_complete", 1f, 1f);
				playerShip.setCustomData(TRANSFER_COMPLETE_KEY, true);
				showTranferFloatyIfNeeded();
				engine.getCombatUI().reFanOutShipInfo();
				boolean autopilot = engine.getCombatUI().isAutopilotOn();
				if (autopilot) {
					if (playerShip.getAI() == null) { // somehow?
						CombatFleetManagerAPI manager = engine.getFleetManager(FleetSide.PLAYER);
						DeployedFleetMemberAPI member = manager.getDeployedFleetMember(playerShip);
						playerShip.setShipAI(Global.getSettings().pickShipAIPlugin(member == null ? null : member.getMember(), playerShip));
					}
				} else {
					playerShip.setShipAI(null);
					restoreControlState(playerShip);
				}
			} else {
				suppressControlsDuringTransfer(playerShip);
				showTranferFloatyIfNeeded();
			}
		}
		
		// if the player changed flagships:
		// skip a few frames to make sure the status ends up on top of the status list
		if (playerShip != prevPlayerShip) {
			prevPlayerShip = playerShip;
			skipFrames = 30;
		}
		
		if (skipFrames > 0) {
			skipFrames--;
			return;
		}
		
		updateLinkState();
	}
	
	public void suppressControlsDuringTransfer(ShipAPI playerShip) {
		if (ALLOW_ENGINE_CONTROL_DURING_TRANSFER) {
			playerShip.blockCommandForOneFrame(ShipCommand.FIRE);
			playerShip.blockCommandForOneFrame(ShipCommand.TOGGLE_AUTOFIRE);
			playerShip.blockCommandForOneFrame(ShipCommand.PULL_BACK_FIGHTERS);
			playerShip.blockCommandForOneFrame(ShipCommand.VENT_FLUX);
			playerShip.blockCommandForOneFrame(ShipCommand.USE_SELECTED_GROUP);
			playerShip.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
			playerShip.blockCommandForOneFrame(ShipCommand.HOLD_FIRE);
		} else {
			engine.getCombatUI().setDisablePlayerShipControlOneFrame(true);
		}
	}
	
	public void showTranferFloatyIfNeeded() {
		ShipAPI playerShip = engine.getPlayerShip();
		if (playerShip == null) return;
		float timeMult = playerShip.getMutableStats().getTimeMult().getModifiedValue();
		Color color = new Color(0,121,216,255);
		
// feels kind of annoying		
//		if (untilTransfer <= 0) {
//			engine.addFloatingTextAlways(playerShip.getLocation(), "Transfer complete",
//					getFloatySize(playerShip), color, playerShip, 5f * timeMult, 2f, 1f/timeMult, 0f, 0f,
//					1f);
//			return;
//		}
		
		int show = (int) Math.ceil(untilTransfer);
		if (show != lastShownTime) {
			if (show > 0) {
				engine.addFloatingTextAlways(playerShip.getLocation(), "Neural transfer in " + show,
						getFloatySize(playerShip), color, playerShip, 4f * timeMult, 0.8f/timeMult, 1f/timeMult, 0f, 0f,
						1f);
			}
			lastShownTime = show;
			//Global.getSoundPlayer().playUISound("ui_hold_fire_on", 2f, 0.25f);
		}
	}
	
	public boolean canLink(ShipAPI ship) {
		// below line: debug, work for every ship
		//if (ship.isAlive() && !ship.isShuttlePod()) return true;
		
		ShipAPI playerShip = engine.getPlayerShip();
		// transferred command to officer'ed ship, can't link
		if (ship == playerShip && ship.getOriginalCaptain() != null && 
				!ship.getOriginalCaptain().isDefault() && !ship.getOriginalCaptain().isPlayer()) {
			return false;
		}
		
		if (engine.isInCampaign() || engine.isInCampaignSim()) {
			if (Global.getSector().getPlayerStats().getDynamic().getMod(Stats.HAS_NEURAL_LINK).computeEffective(0f) <= 0) {
				return false;
			}
		}
		return ship.isAlive() && !ship.isShuttlePod() &&
			   ship.getMutableStats().getDynamic().getMod(Stats.HAS_NEURAL_LINK).computeEffective(0f) > 0;
			   //ship.getVariant().hasHullMod(HullMods.NEURAL_INTERFACE);
	}
	
	public void updateLinkState() {
		ShipAPI playerShip = engine.getPlayerShip();
		if (playerShip == null) return;
		
		for (ShipAPI ship : new ArrayList<ShipAPI>(linked)) {
			if (!ship.isAlive()) {
				if (ship != playerShip) {
					PersonAPI orig = ship.getOriginalCaptain();
					if (orig.isPlayer()) {
						orig = Global.getFactory().createPerson();
						if (engine.isInCampaign() || engine.isInCampaignSim()) {
							orig.setPersonality(Global.getSector().getPlayerFaction().pickPersonality());
						}
					}
					ship.setCaptain(orig);
				}
				linked.remove(ship);
			}
		}
		
		ShipAPI physicalLocation = engine.getShipPlayerLastTransferredCommandTo();
		boolean physicallyPresent = linked.contains(physicalLocation);
		if (!linked.contains(playerShip) || !physicallyPresent ||
				!canLink(playerShip)) {
			for (ShipAPI ship : linked) {
				PersonAPI orig = ship.getOriginalCaptain();
				if (orig.isPlayer()) {
					orig = Global.getFactory().createPerson();
					if (engine.isInCampaign() || engine.isInCampaignSim()) {
						orig.setPersonality(Global.getSector().getPlayerFaction().pickPersonality());
					}
				}
				if (ship.getCaptain() != orig) {
					ship.setCaptain(orig);
					if (ship.getFleetMember() != null) {
						ship.getFleetMember().setCaptain(ship.getOriginalCaptain());
					}
				}
			}
			linked.clear();
			if (canLink(playerShip)) {
				linked.add(playerShip);
			}
		}
		
		if (linked.isEmpty()) return;
		
		
		CombatFleetManagerAPI manager = engine.getFleetManager(FleetSide.PLAYER);
		List<DeployedFleetMemberAPI> members = manager.getDeployedCopyDFM();
		
		if (physicallyPresent) {
			for (DeployedFleetMemberAPI dfm : members) {
				if (linked.size() >= 2) break;
				
				if (dfm.isFighterWing()) continue;
				if (dfm.isAlly()) continue;
				
				ShipAPI ship = dfm.getShip();
				if (linked.contains(ship)) continue;
				if (!ship.getCaptain().isDefault() && ship != playerShip &&
						!ship.getCaptain().isPlayer()) {
					// this last for when the player deploys a ship with NI in the simulator and then
					// deploys their actual flagship - so, the player ship doesn't *actually* have the player in it
					// but the other ship does
					continue;
				}
				
				// transferred command to an officer'ed ship, no link
				if (ship == playerShip && ship.getOriginalCaptain() != null && 
						!ship.getOriginalCaptain().isDefault() &&
						!ship.getOriginalCaptain().isPlayer()) {
					continue;
				}
				if (ship.controlsLocked()) continue;
				
				if (canLink(ship)) {
					linked.add(ship);
				}
			}
		}
		
		PersonAPI player = playerPerson; 
		if (player == null) {
			player = playerShip.getCaptain();
			if (!player.isDefault() && playerPerson == null) {
				playerPerson = player;
			}
		}
		
		for (ShipAPI ship : linked) {
			if (ship.getCaptain() != player) {
				ship.setCaptain(player);
				if (ship.getFleetMember() != null) {
					ship.getFleetMember().setCaptain(player);
				}
			}
		}
		
		if (linked.contains(playerShip)) {
			ShipAPI other = null;
			for (ShipAPI ship : linked) {
				if (ship != playerShip) {
					other = ship;
					break;
				}
			}
			
			String title = "Neural System Reset";
			//String title = "System Reset on Transfer";
			String icon = Global.getSettings().getSpriteName("ui", "icon_neural_link");
			String key = NeuralInterface.SYSTEM_RESET_TIMEOUT_KEY;
//			Float timeout = (Float) Global.getCombatEngine().getCustomData().get(key);
//			if (timeout == null) timeout = 0f;
			Float timeout = null;
			if (other != null) timeout = (Float) other.getCustomData().get(key);
			if (timeout == null) timeout = 0f;
			if (other == null) {
				engine.maintainStatusForPlayerShip(KEY_STATUS2, icon, title, "No signal", true);
			} else if (timeout <= 0) {
				engine.maintainStatusForPlayerShip(KEY_STATUS2, icon, title, "Ready on transfer", false);
			} else {
				int show = (int) Math.ceil(timeout);
				engine.maintainStatusForPlayerShip(KEY_STATUS2, icon, title, "Ready in " + show + " seconds", true);
			}
		}
		
		for (ShipAPI ship : linked) {
			if (ship != playerShip) {
				
				if (untilTransfer <= 0f) {
					String title = "Neural Link Active";
					//String data = ship.getName() + ", " + ship.getHullSpec().getHullNameWithDashClass();
					//String data = ship.getName() + ", " + ship.getHullSpec().getHullName();
					String data = "Target: " + ship.getHullSpec().getHullNameWithDashClass();
					String icon = Global.getSettings().getSpriteName("ui", "icon_neural_link");
					engine.maintainStatusForPlayerShip(KEY_STATUS, icon, title, data, false);
				} else {
					int show = (int) Math.ceil(untilTransfer);
					if (show > 0) {
						String title = "Neural Transfer";
						String data = "Link in " + show + " seconds";
						String icon = Global.getSettings().getSpriteName("ui", "icon_neural_link");
						engine.maintainStatusForPlayerShip(KEY_STATUS, icon, title, data, true);
					}
				}
				
				break;
			}
		}
		
		if (linked.size() <= 1 && linked.contains(playerShip)) {
			String title = "Neural Link Inactive";
			String data = "No signal";
			if (!physicallyPresent) {
				data = "requires physical transfer";
			}
			String icon = Global.getSettings().getSpriteName("ui", "icon_neural_link");
			engine.maintainStatusForPlayerShip(KEY_STATUS, icon, title, data, true);
		}
	}
	
	
	public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
		if (engine == null || engine.getCombatUI() == null || engine.getCombatUI().isShowingCommandUI()) return;
		
		ShipAPI playerShip = engine.getPlayerShip();
		if (playerShip == null) return;
		if (!linked.contains(playerShip) || linked.size() < 2) return;
		//if (untilTransfer > 0) return;
		
		for (InputEventAPI event : events) {
			if (event.isConsumed()) continue;
			
			if (event.isControlDownEvent(TRANSFER_CONTROL)) {
				if (untilTransfer <= 0) {
					for (ShipAPI ship : linked) {
						if (ship != playerShip) {
							untilTransfer = getTransferTime();
							lastShownTime = 0;
							doTransfer(ship);
						}
					}
				}
				event.consume();
				return;
			}
		}
	}
	
	
	public void doTransfer(ShipAPI ship) {
		if (ship == null) return;
		ShipAPI playerShip = engine.getPlayerShip();
		if (playerShip == null) return;
		if (!linked.contains(playerShip)) return;
		if (!linked.contains(ship)) return;
		
		if (untilTransfer <= 0) {
			Global.getSoundPlayer().playUISound("ui_neural_transfer_complete", 1f, 1f);
			ship.setCustomData(TRANSFER_COMPLETE_KEY, true);
			showTranferFloatyIfNeeded();
			engine.getCombatUI().reFanOutShipInfo();
		} else {
			Global.getSoundPlayer().playUISound("ui_neural_transfer_begin", 1f, 1f);
		}
		
		// I have it on good authority that this looks bad.
		// "[it looks like] some vague energy field effect, then like Troi would have a headache and some dubious writing would occur"
//		float animDur = Math.max(untilTransfer, 0.5f);
//		engine.addLayeredRenderingPlugin(new NeuralTransferVisual(playerShip, ship, animDur));
		
		Mouse.setCursorPosition((int)Global.getSettings().getScreenWidthPixels()/2,
								(int)Global.getSettings().getScreenHeightPixels()/2);
		
		saveControlState(playerShip);
		
		ShipAIPlugin playerShipAI = playerShip.getShipAI(); // non-null if autopilot is on
		ShipAIPlugin prevTargetAI = ship.getShipAI();
		engine.setPlayerShipExternal(ship);
		if (ship.getFleetMember() != null) {
			ship.getFleetMember().setCaptain(playerShip.getCaptain());
		}
		
		if (playerShipAI != null) {
			playerShip.setShipAI(playerShipAI); // not the player ship anymore, the old ship
		}
		
		boolean autopilot = engine.getCombatUI().isAutopilotOn();
		
		if (untilTransfer > 0) {
			if (!ALLOW_ENGINE_CONTROL_DURING_TRANSFER) {
				ship.setShipAI(prevTargetAI);
			}
			//engine.getCombatUI().setDisablePlayerShipControlOneFrame(true);
			suppressControlsDuringTransfer(ship);
			showTranferFloatyIfNeeded();
		} else if (autopilot) {
			CombatFleetManagerAPI manager = engine.getFleetManager(FleetSide.PLAYER);
			DeployedFleetMemberAPI member = manager.getDeployedFleetMember(ship);
			ship.setShipAI(Global.getSettings().pickShipAIPlugin(member == null ? null : member.getMember(), ship));
		}
		
		if (untilTransfer <= 0) {
			showTranferFloatyIfNeeded();
			if (!autopilot) {
				restoreControlState(ship);
			}
		}
	}

	
	public float getTransferTime() {
		float total = 0f;
		for (ShipAPI ship : linked) {
			if (ship.getFleetMember() == null) continue;
			total += ship.getFleetMember().getDeploymentPointsCost();
		}
		
		total = Math.round(total);
		
		//INSTANT_TRANSFER_DP = 0f;
		
		if (total <= INSTANT_TRANSFER_DP) return 0f;
		
		float time = (total - INSTANT_TRANSFER_DP) * TRANSFER_SECONDS_PER_DP;
		if (time < TRANSFER_MIN_SECONDS_IF_NOT_INSTANT) {
			time = TRANSFER_MIN_SECONDS_IF_NOT_INSTANT;
		} else if (time > TRANSFER_MAX_SECONDS) {
			time = TRANSFER_MAX_SECONDS;
		}
		time = (float) Math.ceil(time);

		return time;
	}
	

	public void renderInUICoords(ViewportAPI viewport) {
	}

	public void renderInWorldCoords(ViewportAPI viewport) {
	}

	public static float getFloatySize(ShipAPI ship) {
		switch (ship.getHullSize()) {
		case FIGHTER: return 15f;
		case FRIGATE: return 17f;
		case DESTROYER: return 21f;
		case CRUISER: return 24f;
		case CAPITAL_SHIP: return 27f;
		}
		return 10f;
	}
}
