package com.fs.starfarer.api.impl.campaign.rulecmd.missions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEntityPickerListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.misc.GateHaulerIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * NotifyEvent $eventHandle <params> 
 * 
 */
public class GateHaulerCMD extends BaseCommandPlugin {
	
	public static int ACTIVATION_COST = 1000;
	
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected SectorEntityToken stableLocation;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected CargoAPI playerCargo;
	protected MemoryAPI memory;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		memory = getEntityMemory(memoryMap);
		
		entity = dialog.getInteractionTarget();
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		stableLocation = findNearestStableLocation();
		
		if (command.equals("addIntel")) {
			Global.getSector().getIntelManager().addIntel(new GateHaulerIntel(entity), false, text);
		} else if (command.equals("printCost")) {
			printCost();
		} else if (command.equals("removeActivationCosts")) {
			removeActivationCosts();
		} else if (command.equals("canActivate")) {
			return canActivate();
		} else if (command.equals("selectDestination")) {
			selectDestination();
		} else if (command.equals("activate")) {
			activate();
		} else if (command.equals("canDeploy")) {
			return canDeploy();
		} else if (command.equals("deploy")) {
			deploy();
		} else if (command.equals("isInCurrentSystem")) {
			return isInCurrentSystem();
		} else if (command.equals("wasDeployedToCurrentSystem")) {
			return wasDeployedToCurrentSystem();
		}
		return true;
	}
	
	public boolean wasDeployedToCurrentSystem() {
		LocationAPI loc = Global.getSector().getCurrentLocation();
		if (loc == null) return false;
		return loc.getMemoryWithoutUpdate().getBoolean("$deployedGateHaulerHere");
		
	}
	public boolean isInCurrentSystem() {
		GateHaulerIntel intel = GateHaulerIntel.get(entity);
		if (intel != null) {
			if (intel.getAction() == null) {
				return intel.getGateHauler().getContainingLocation() == Global.getSector().getCurrentLocation();
			}
		}
		return false;
	}
	
	public void deploy() {
		if (stableLocation == null) return;
		
		GateHaulerIntel intel = GateHaulerIntel.get(entity);
		if (intel != null) {
			intel.initiateDeployment(stableLocation);
		}
	}
	
	public boolean canDeploy() {
		GateHaulerIntel intel = GateHaulerIntel.get(entity);
		if (intel == null) return false;
		return stableLocation != null;
	}
	
	public SectorEntityToken findNearestStableLocation() {
		if (entity.getContainingLocation() == null) return null;
		float minDist = Float.MAX_VALUE;
		SectorEntityToken nearest = null;
		for (SectorEntityToken curr : entity.getContainingLocation().getEntitiesWithTag(Tags.STABLE_LOCATION)) {
			float dist = Misc.getDistance(curr, entity);
			if (dist < minDist) {
				minDist = dist;
				nearest = curr;
			}
		}
		return nearest;
	}
	
	public void activate() {
		GateHaulerIntel intel = GateHaulerIntel.get(GateHaulerCMD.this.entity);
		if (intel != null) {
			intel.activate();
		}
	}

	public void printCost() {
		Misc.showCost(text, null, null, getResources(), getQuantities());
		
		if (canActivate()) {
			text.addPara("Proceed with reactivation?");
		} else {
			text.addPara("You do not have the necessary resources to reactivate the Gate Hauler.");
		}
	}
	
	public void removeActivationCosts() {
		CargoAPI cargo = playerCargo;
		String [] res = getResources();
		int [] quantities = getQuantities();
		for (int i = 0; i < res.length; i++) {
			String commodityId = res[i];
			int quantity = quantities[i];
			cargo.removeCommodity(commodityId, quantity);
		}
	}
	
	public boolean canActivate() {
		if (DebugFlags.OBJECTIVES_DEBUG) {
			return true;
		}
		
		CargoAPI cargo = playerCargo;
		String [] res = getResources();
		int [] quantities = getQuantities();
		
		for (int i = 0; i < res.length; i++) {
			String commodityId = res[i];
			int quantity = quantities[i];
			if (quantity > cargo.getQuantity(CargoItemType.RESOURCES, commodityId)) {
				return false;
			}
		}
		return true;
	}
	
	public String [] getResources() {
		return new String[] {Commodities.RARE_METALS};
	}

	public int [] getQuantities() {
		return new int[] {ACTIVATION_COST};
	}
	
	public int getTravelDays(SectorEntityToken entity) {
		GateHaulerIntel intel = GateHaulerIntel.get(GateHaulerCMD.this.entity);
		if (intel != null) {
			StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
			return intel.computeTransitDays(system);
		}
		return 365;
	}
	
	public void selectDestination() {
		final ArrayList<SectorEntityToken> systems = new ArrayList<SectorEntityToken>();
		for (StarSystemAPI curr : Global.getSector().getStarSystems()) {
			if (curr == entity.getContainingLocation()) continue;
			if (curr.hasTag(Tags.THEME_HIDDEN) && !"Limbo".equals(curr.getBaseName())) continue;
			if (curr.isDeepSpace()) continue;
			if (curr.getHyperspaceAnchor() == null) continue;
			if (Misc.getStarSystemForAnchor(curr.getHyperspaceAnchor()) == null) continue;
			systems.add(curr.getHyperspaceAnchor());
		}
		dialog.showCampaignEntityPicker("Select destination for gate hauler", "Destination:", "Execute", 
				Global.getSector().getPlayerFaction(), systems, 
			new BaseCampaignEntityPickerListener() {
				public void pickedEntity(SectorEntityToken entity) {
					dialog.dismiss();
					Global.getSector().setPaused(false);
					
					GateHaulerIntel intel = GateHaulerIntel.get(GateHaulerCMD.this.entity);
					if (intel != null) {
						StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
						intel.initiateDeparture(system);
					}
				}
				public void cancelledEntityPicking() {
					
				}
				public String getMenuItemNameOverrideFor(SectorEntityToken entity) {
					StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
					if (system != null) {
						return system.getNameWithLowercaseTypeShort();
					}
					return null;
				}
				public String getSelectedTextOverrideFor(SectorEntityToken entity) {
					StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
					if (system != null) {
						return system.getNameWithLowercaseType();
					}
					return null;
				}
				public void createInfoText(TooltipMakerAPI info, SectorEntityToken entity) {
					int days = getTravelDays(entity);
					info.setParaSmallInsignia();
					String daysStr = "days";
					if (days == 1) daysStr = "day";
					info.addPara("    Estimated gate hauler travel time: %s " + daysStr, 0f, Misc.getHighlightColor(), "" + days);
				}

				public boolean canConfirmSelection(SectorEntityToken entity) {
					return true;
				}
				public float getFuelColorAlphaMult() {
					return 0f;
				}
				public float getFuelRangeMult() { // just for showing it on the map when picking destination
					return 0f;
				}
			});
	}
}















