package com.fs.starfarer.api.impl.campaign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.awt.Color;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.impl.items.WormholeScannerPlugin;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.abilities.TransponderAbility;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.DumpMemory;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MiscCMD;
import com.fs.starfarer.api.impl.campaign.shared.WormholeManager;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.util.Misc;

public class JumpPointInteractionDialogPluginImpl implements InteractionDialogPlugin {

	public static final String UNSTABLE_KEY = "$unstable";
	public static final String CAN_STABILIZE = "$canStabilize";
	
	public static float WORMHOLE_FUEL_USE_MULT = 5f;
	
	private static enum OptionId {
		INIT,
		JUMP_1,
		JUMP_2,
		JUMP_3,
		JUMP_4,
		JUMP_5,
		JUMP_6,
		JUMP_7,
		JUMP_8,
		JUMP_9,
		JUMP_CONFIRM_TURN_TRANSPONDER_ON,
		JUMP_CONFIRM,
		STABILIZE,
		RETRIEVE_ANCHOR,
		RETRIEVE_ANCHOR_CONFIRM,
		LEAVE,
	}
	
	private InteractionDialogAPI dialog;
	private TextPanelAPI textPanel;
	private OptionPanelAPI options;
	private VisualPanelAPI visual;
	
	private CampaignFleetAPI playerFleet;
	private JumpPointAPI jumpPoint;
	
	protected boolean shownConfirm = false;
	protected boolean canAfford;
	
	protected OptionId beingConfirmed = null;
	
	private List<OptionId> jumpOptions = Arrays.asList(
							new OptionId [] {
									OptionId.JUMP_1,
									OptionId.JUMP_2,
									OptionId.JUMP_3,
									OptionId.JUMP_4,
									OptionId.JUMP_5,
									OptionId.JUMP_6,
									OptionId.JUMP_7,
									OptionId.JUMP_8,
									OptionId.JUMP_9
							});
	
	
	private static final Color HIGHLIGHT_COLOR = Global.getSettings().getColor("buttonShortcut");
	
	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		jumpPoint = (JumpPointAPI) (dialog.getInteractionTarget());
		
		fuelCost = playerFleet.getLogistics().getFuelCostPerLightYear();
		float rounded = Math.round(fuelCost);
		if (fuelCost > 0 && rounded <= 0) rounded = 1;
		fuelCost = rounded;
		
		if (isWormhole()) {
			fuelCost *= WORMHOLE_FUEL_USE_MULT;
		} else if (jumpPoint.isInHyperspace()) {
			fuelCost = 0f;
		}
		
		canAfford = fuelCost <= playerFleet.getCargo().getFuel();
		
		visual.setVisualFade(0.25f, 0.25f);
		if (jumpPoint.getCustomInteractionDialogImageVisual() != null) {
			visual.showImageVisual(jumpPoint.getCustomInteractionDialogImageVisual());
		} else {
			if (isWormhole()) {
				visual.showImagePortion("illustrations", "jump_point_wormhole", 640, 400, 0, 0, 480, 300);
			} else {
				if (playerFleet.getContainingLocation().isHyperspace()) {
					visual.showImagePortion("illustrations", "jump_point_hyper", 640, 400, 0, 0, 480, 300);
				} else {
					visual.showImagePortion("illustrations", "jump_point_normal", 640, 400, 0, 0, 480, 300);
				}
			}
		}
		
//		dialog.hideVisualPanel();
//		dialog.setTextWidth(1000);
	
		dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
		
		optionSelected(null, OptionId.INIT);
	}
	
	public Map<String, MemoryAPI> getMemoryMap() {
		return null;
	}
	
	private EngagementResultAPI lastResult = null;
	public void backFromEngagement(EngagementResultAPI result) {
		// no combat here, so this won't get called
	}
	
	public void optionSelected(String text, Object optionData) {
		if (optionData == null) return;
		
		if (DumpMemory.OPTION_ID == optionData) {
			Map<String, MemoryAPI> memoryMap = new HashMap<String, MemoryAPI>();
			MemoryAPI memory = dialog.getInteractionTarget().getMemory();
			
			memoryMap.put(MemKeys.LOCAL, memory);
			if (dialog.getInteractionTarget().getFaction() != null) {
				memoryMap.put(MemKeys.FACTION, dialog.getInteractionTarget().getFaction().getMemory());
			} else {
				memoryMap.put(MemKeys.FACTION, Global.getFactory().createMemory());
			}
			memoryMap.put(MemKeys.GLOBAL, Global.getSector().getMemory());
			memoryMap.put(MemKeys.PLAYER, Global.getSector().getCharacterData().getMemory());
			
			if (dialog.getInteractionTarget().getMarket() != null) {
				memoryMap.put(MemKeys.MARKET, dialog.getInteractionTarget().getMarket().getMemory());
			}
			new DumpMemory().execute(null, dialog, null, memoryMap);
			return;
		} else if (DevMenuOptions.isDevOption(optionData)) {
			DevMenuOptions.execute(dialog, (String) optionData);
			return;
		}
		
		OptionId option = (OptionId) optionData;
		
		if (text != null) {
			//textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
			dialog.addOptionSelectedText(option);
		}
		
		
		boolean unstable = jumpPoint.getMemoryWithoutUpdate().getBoolean(UNSTABLE_KEY);
		boolean stabilizing = jumpPoint.getMemoryWithoutUpdate().getExpire(UNSTABLE_KEY) > 0;
		boolean canStabilize = jumpPoint.getMemoryWithoutUpdate().getBoolean(CAN_STABILIZE);
		boolean canTransverseJump = Global.getSector().getPlayerFleet().hasAbility(Abilities.TRANSVERSE_JUMP);
		boolean tutorialInProgress = TutorialMissionIntel.isTutorialInProgress();
		
		switch (option) {
		case INIT:
//			dialog.showCustomDialog(600, 400, new CustomDialogDelegate() {
//				public boolean hasCancelButton() {
//					return false;
//				}
//				public CustomUIPanelPlugin getCustomPanelPlugin() {
//					return new ExampleCustomUIPanel();
//				}
//				public String getConfirmText() {
//					return null;
//				}
//				public String getCancelText() {
//					return null;
//				}
//				public void customDialogConfirm() {
//					System.out.println("CUSTOM Confirmed");
//				}
//				public void customDialogCancel() {
//					System.out.println("CUSTOM Cancelled");
//				}
//				public void createCustomDialog(CustomPanelAPI panel) {
//					TooltipMakerAPI text = panel.createUIElement(600f, 200f, true);
//					for (int i = 0; i < 10; i++) {
//						text.addPara("The large amount of kinetic energy delivered to shield systems of enemy craft at close-range typically causes emitter overload, a tactical option often overlooked by inexperienced captains.", 10f);
//					}
//					panel.addUIElement(text).inTL(0, 0);
//				}
//			});
			
			if (isWormhole()) {
				addText("Your fleet approaches the wormhole.");
			} else {
				addText(getString("approach"));
			}
			
			Description desc = Global.getSettings().getDescription(jumpPoint.getCustomDescriptionId(), Type.CUSTOM);
			if (desc != null && desc.hasText3()) {
				addText(desc.getText3());
			}
			
			String noun = "jump-point";
			if (isWormhole()) noun = "wormhole";
			
			if (unstable) {
				if (isWormhole() && stabilizing) {
					float dur = jumpPoint.getMemoryWithoutUpdate().getExpire(UNSTABLE_KEY);
					
					if (true) {
						String durStr = "" + (int) dur;
						String days = "days";
						if ((int)dur == 1) {
							days = "day";
						}
						if ((int)dur <= 0) {
							days = "day";
							durStr = "1";
						}
						
						textPanel.addPara("This wormhole is stabilizing and will become usable "
								+ "within %s " + days + ".", Misc.getHighlightColor(), durStr);
					} else {
						String time = Misc.getStringForDays((int) dur);
						LabelAPI label;
						if (time.contains("many")) {
							label = textPanel.addParagraph(
									"This wormhole is gradually stabilizing, but will not be usable for " + time + ".");
						} else {
							label = textPanel.addParagraph(
									"This wormhole is stabilizing, and should be usable within " + time + ".");
						}
						
						label.setHighlightColor(HIGHLIGHT_COLOR);
						label.setHighlight(time);
					}
					
				} else {
					if (stabilizing && !canTransverseJump) {
						if (tutorialInProgress) {
							addText("This jump-point is stabilizing and should be usable within a day at the most.");
						} else {
							addText("This jump-point is stabilizing but will not be usable for some time.");
						}
					} else {
						addText("This jump-point is unstable and can not be used.");
					}
					
					if (canTransverseJump && !tutorialInProgress ) {
						addText("Until it restabilizes, hyperspace is only accessible via Transverse Jump.");
					}	
				}
			} else {
				if (!jumpPoint.isInHyperspace()) {
					if (canAfford) {
						if (!jumpPoint.getDestinations().isEmpty()) {
							if (isWormhole()) {
								addText("The nav computer pings the wormhole terminus, establishing a data connection through drive-field fluctuations. "
										+ "In under a minute, and only a moment of unease as the agrav self-correction settles, the process is complete. "
										+ "Your primary interface unveils a list of possible destinations.");
							}
						}
						textPanel.addParagraph("Activating this " + noun + " to let your fleet pass through will cost " + (int)fuelCost + " fuel.");
						textPanel.highlightInLastPara(Misc.getHighlightColor(), "" + (int)fuelCost);
					} else {
						int fuel = (int) playerFleet.getCargo().getFuel();
						if (fuel == 0) {
							textPanel.addParagraph("Activating this " + noun + " to let your fleet pass through will cost " + (int)fuelCost + " fuel. You have no fuel.");
						} else {
							textPanel.addParagraph("Activating this " + noun + " to let your fleet pass through will cost " + (int)fuelCost + " fuel. You only have " + fuel + " fuel.");
						}
						textPanel.highlightInLastPara(Misc.getNegativeHighlightColor(), "" + (int)fuelCost, "" + fuel);
					}
				}
				
				if (canAfford) {
					showWarningIfNeeded();
				}
			}
			
			if (isWormhole()) {
				MiscCMD.addWormholeIntelIfNeeded(jumpPoint, textPanel, false);
			}
				
			createInitialOptions();
			break;
		case STABILIZE:
			jumpPoint.getMemoryWithoutUpdate().unset(CAN_STABILIZE);
			//jumpPoint.getMemoryWithoutUpdate().unset(UNSTABLE_KEY);
			jumpPoint.getMemoryWithoutUpdate().expire(UNSTABLE_KEY, 1f);

			addText("You load the stabilization algorithm into your jump program and the drive field goes through a series " +
					"of esoteric fluctuations, their resonance gradually cancelling out the instability in this jump-point.");
			
			addText("The jump-point should be stable enough to use within a day or so.");
			
			createInitialOptions();
			break;
		case JUMP_CONFIRM_TURN_TRANSPONDER_ON:
			AbilityPlugin t = Global.getSector().getPlayerFleet().getAbility(Abilities.TRANSPONDER);
			if (t != null && !t.isActive()) {
				t.activate();
			}
			optionSelected(null, beingConfirmed);
			break;
		case JUMP_CONFIRM:
			optionSelected(null, beingConfirmed);
			break;
		case RETRIEVE_ANCHOR_CONFIRM:
			dialog.getTextPanel().addPara(
					"You give the order. Before long, your ops chief confirms that the wormhole anchor has been stowed in a secure hold on your flagship.");
			WormholeManager.get().removeWormhole(jumpPoint, dialog);
			options.clearOptions();
			options.addOption("Leave", OptionId.LEAVE, null);
			options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
			break;
		case RETRIEVE_ANCHOR:
			dialog.getTextPanel().addPara(
					"This will shut down the wormhole and free up the stable point for other uses.");
			options.clearOptions();
			options.addOption("Confirm your orders", OptionId.RETRIEVE_ANCHOR_CONFIRM, null);
			
			options.addOption("Abort the operation", OptionId.LEAVE, null);
			options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
			break;
		case LEAVE:
			Global.getSector().getCampaignUI().setFollowingDirectCommand(true);
			Global.getSector().setPaused(false);
			dialog.dismiss();
			break;
		}
		
		if (jumpOptions.contains(option)) {
			JumpDestination dest = destinationMap.get(option);
			if (dest != null) {
				
				if (!shownConfirm) {
					SectorEntityToken target = dest.getDestination();
					CampaignFleetAPI player = Global.getSector().getPlayerFleet();
					if (target != null && target.getContainingLocation() != null && 
							!target.getContainingLocation().isHyperspace() && !player.isTransponderOn()) {
						List<FactionAPI> wouldBecomeHostile = TransponderAbility.getFactionsThatWouldBecomeHostile(player);
						boolean wouldMindTOff = false;
						boolean isPopulated = false;
						for (MarketAPI market : Global.getSector().getEconomy().getMarkets(target.getContainingLocation())) {
							if (market.isHidden()) continue;
							if (market.getFaction().isPlayerFaction()) continue;
							
							isPopulated = true;
							if (!market.getFaction().isHostileTo(Factions.PLAYER) && 
									!market.isFreePort() &&
									!market.getFaction().getCustomBoolean(Factions.CUSTOM_ALLOWS_TRANSPONDER_OFF_TRADE)) {
								wouldMindTOff = true;
							}
						}
						
						if (isPopulated) {
							if (wouldMindTOff) {
								textPanel.addPara("Your transponder is off, and patrols " +
										"in the " + 
										target.getContainingLocation().getNameWithLowercaseType() + 
										" are likely to give you trouble over the fact, if you're spotted.");
							} else {
								textPanel.addPara("Your transponder is off, but any patrols in the " + 
										target.getContainingLocation().getNameWithLowercaseType() + 
										" are unlikely to raise the issue.");
							}
							
							if (!wouldBecomeHostile.isEmpty()) {
								String str = "Turning the transponder on now would reveal your hostile actions to";
								boolean first = true;
								boolean last = false;
								for (FactionAPI faction : wouldBecomeHostile) {
									last = wouldBecomeHostile.indexOf(faction) == wouldBecomeHostile.size() - 1;
									if (first || !last) {
										str += " " + faction.getDisplayNameWithArticle() + ",";
									} else {
										str += " and " + faction.getDisplayNameWithArticle() + ",";
									}
								}
								str = str.substring(0, str.length() - 1) + ".";
								textPanel.addPara(str, Misc.getNegativeHighlightColor());
							}
							
							options.clearOptions();
							
							options.addOption("Turn the transponder on and then jump", OptionId.JUMP_CONFIRM_TURN_TRANSPONDER_ON, null);
							options.addOption("Jump, keeping the transponder off", OptionId.JUMP_CONFIRM, null);
							beingConfirmed = option;
							
							options.addOption("Abort the jump", OptionId.LEAVE, null);
							options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
							
							shownConfirm = true;
							return;
						}
					}
				}
				
				
				
				
//				SectorEntityToken token = dest.getDestination();
//				//System.out.println("JUMP SELECTED");
//				LocationAPI destLoc = token.getContainingLocation();
//				LocationAPI curr = playerFleet.getContainingLocation();
//				
//				Global.getSector().setCurrentLocation(destLoc);
//				curr.removeEntity(playerFleet);
//				destLoc.addEntity(playerFleet);
//				
//				Global.getSector().setPaused(false);
//				playerFleet.setLocation(token.getLocation().x, token.getLocation().y);
//				playerFleet.setMoveDestination(token.getLocation().x, token.getLocation().y);
				
				if (Global.getSector().getUIData().getCourseTarget() == dialog.getInteractionTarget()) {
					Global.getSector().getCampaignUI().clearLaidInCourse();
				}
				
				dialog.dismiss();
				
				Global.getSector().setPaused(false);
				Global.getSector().doHyperspaceTransition(playerFleet, jumpPoint, dest);
				
				playerFleet.getCargo().removeFuel(fuelCost);
				
				return;
			}
		}
	}
	
	protected void showWarningIfNeeded() {
		if (isWormhole()) return;
		
		if (jumpPoint.getDestinations().isEmpty()) return;
		JumpDestination dest = jumpPoint.getDestinations().get(0);
		SectorEntityToken target = dest.getDestination();
		if (target == null || target.getContainingLocation() == null) return;
		
		List<CampaignFleetAPI> fleets = new ArrayList<CampaignFleetAPI>();
		boolean hostile = false;
		float minDist = Float.MAX_VALUE;
		int maxDanger = 0;
		for (CampaignFleetAPI other : target.getContainingLocation().getFleets()) {
			float dist = Misc.getDistance(target, other);
			if (dist < 1000) {
				fleets.add(other);
				if (other.getAI() != null) {
					hostile |= other.getAI().isHostileTo(Global.getSector().getPlayerFleet());
				} else {
					hostile |= other.getFaction().isHostileTo(Factions.PLAYER);
				}
				if (other.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_PIRATE)) {
					hostile = true;
				}
				if (hostile) {
					maxDanger = Math.max(maxDanger, Misc.getDangerLevel(other));
				}
				if (dist < minDist) minDist = dist;
			}
		}
		TextPanelAPI text = dialog.getTextPanel();
		
		if (maxDanger >= 2) {
			String noun = "jump-point";
			if (isWormhole()) noun = "wormhole";
			text.addPara("Warning!", Misc.getNegativeHighlightColor());
			Global.getSoundPlayer().playUISound("cr_playership_malfunction", 1f, 0.25f);
			
			String where = "a short distance away from the exit";
			String whereHL = "";
			if (minDist < 300) {
				where = "extremely close to the exit";
				whereHL = where;
			}
			text.addPara("The jump-point exhibits fluctuations " +
										  "characteristic of drive field activity " + where + ".",
										  Misc.getNegativeHighlightColor(), whereHL);
			
			text.addPara("A disposable probe sends back a microburst of information: forces " +
					"near the exit are assesed likely hostile and a possible threat to your fleet.",
					 Misc.getNegativeHighlightColor(), "hostile", "threat");
		}
	}

	private Map<OptionId, JumpDestination> destinationMap = new HashMap<OptionId, JumpDestination>();
	private void createInitialOptions() {
		options.clearOptions();

		boolean dev = Global.getSettings().isDevMode();
		float navigation = Global.getSector().getPlayerFleet().getCommanderStats().getSkillLevel("navigation");
		boolean isStarAnchor = jumpPoint.isStarAnchor();
		boolean okToUseIfAnchor = isStarAnchor && navigation >= 7;
		
		okToUseIfAnchor = true;
		if (isStarAnchor && !okToUseIfAnchor && dev) {
			addText("(Can always be used in dev mode)");
		}
		okToUseIfAnchor |= dev;
		
		String noun = "jump-point";
		if (isWormhole()) noun = "wormhole";
		
		boolean unstable = jumpPoint.getMemoryWithoutUpdate().getBoolean(UNSTABLE_KEY);
		boolean canStabilize = jumpPoint.getMemoryWithoutUpdate().getBoolean(CAN_STABILIZE);
		
		if (unstable) {
			if (canStabilize) {
				options.addOption("Stabilize the jump-point", OptionId.STABILIZE, null);
			}
		} else {
			if (jumpPoint.getDestinations().isEmpty()) {
				if (isWormhole()) {
					addText("This wormhole is not connected to any other termini and is effectively unusable.");
				} else {
					addText(getString("noExits"));
				}
			} else if (playerFleet.getCargo().getFuel() <= 0 && !canAfford) {
				//addText(getString("noFuel"));
			} else if (isStarAnchor && !okToUseIfAnchor) {
				addText(getString("starAnchorUnusable"));
			} else if (canAfford) {
				int index = 0;
				for (JumpDestination dest : jumpPoint.getDestinations()) {
					if (index >= jumpOptions.size()) break;
					OptionId option = jumpOptions.get(index);
					index++;
					
					if (isWormhole()) {
						options.addOption("Initiate a transit to " + dest.getLabelInInteractionDialog(), option, null);
						
						boolean canUse = WormholeScannerPlugin.canPlayerUseWormholes();
						if (!canUse) {
							options.setEnabled(option, false);
							options.setTooltip(option, "Using a wormhole requires a wormhole scanner.");
						}
						
					} else { 
						options.addOption("Order a jump to " + dest.getLabelInInteractionDialog(), option, null);
					}
					destinationMap.put(option, dest);
				}
			}
		}
		
		if (isWormhole()) {
			options.addOption("Shut down the wormhole and retrieve the anchor", OptionId.RETRIEVE_ANCHOR, null);
		}
		
		options.addOption("Leave", OptionId.LEAVE, null);
		options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);

		if (Global.getSettings().getBoolean("oneClickJumpPoints")) {
			if (jumpPoint.getDestinations().size() == 1) {
				dialog.setOpacity(0);
				dialog.setBackgroundDimAmount(0f);
				optionSelected(null, OptionId.JUMP_1);
			}
		}
		
		if (Global.getSettings().isDevMode()) {
			DevMenuOptions.addOptions(dialog);
		}
	}
	
	
	protected OptionId lastOptionMousedOver = null;
	protected float fuelCost;
	
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public void advance(float amount) {
		
	}
	
	private void addText(String text) {
		textPanel.addParagraph(text);
	}
	
	private void appendText(String text) {
		textPanel.appendToLastParagraph(" " + text);
	}
	
	private String getString(String id) {
		String str = Global.getSettings().getString("jumpPointInteractionDialog", id);

		String fleetOrShip = "fleet";
		if (playerFleet.getFleetData().getMembersListCopy().size() == 1) {
			fleetOrShip = "ship";
			if (playerFleet.getFleetData().getMembersListCopy().get(0).isFighterWing()) {
				fleetOrShip = "fighter wing";
			}
		}
		str = str.replaceAll("\\$fleetOrShip", fleetOrShip);
		
		return str;
	}
	

	public Object getContext() {
		return null;
	}
	
	public boolean isWormhole() {
		return jumpPoint.isWormhole();
	}
}



