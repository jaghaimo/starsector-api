package com.fs.starfarer.api.impl.campaign.rulecmd.missions;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEntityPickerListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.GateExplosionScript;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * NotifyEvent $eventHandle <params> 
 * 
 */
public class GateCMD extends BaseCommandPlugin {
	
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
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
		
		if (command.equals("selectDestination")) {
			selectDestination();
		} else if (command.equals("notifyScanned")) {
			notifyScanned(entity);
		} else if (command.equals("explode")) {
			explode(entity);
		} else if (command.equals("isPopulated")) {
			return isPopulated(entity);
		}
		return true;
	}
	
	public static boolean isPopulated(SectorEntityToken targetGate) {
		if (targetGate.getContainingLocation() == null) return false;
		
		LocationAPI loc = targetGate.getContainingLocation();
		if (loc.hasTag(Tags.THEME_CORE_POPULATED)) return true;
		
		return !Misc.getMarketsInLocation(loc).isEmpty();
	}
	
	public static int computeFuelCost(SectorEntityToken targetGate) {
		float dist = Misc.getDistanceToPlayerLY(targetGate);
		float fuelPerLY = Global.getSector().getPlayerFleet().getLogistics().getFuelCostPerLightYear();
		
		return (int) Math.ceil(dist * fuelPerLY * Misc.GATE_FUEL_COST_MULT);
	}

	protected void selectDestination() {
		final ArrayList<SectorEntityToken> gates = 
				new ArrayList<SectorEntityToken>(GateEntityPlugin.getGateData().scanned);
		gates.remove(entity);
		dialog.showCampaignEntityPicker("Select destination", "Destination:", "Initiate transit", 
				Global.getSector().getPlayerFaction(), gates, 
			new CampaignEntityPickerListener() {
				public void pickedEntity(SectorEntityToken entity) {
					int cost = computeFuelCost(entity);
					Global.getSector().getPlayerFleet().getCargo().removeFuel(cost);
					
					dialog.dismiss();
					Global.getSector().setPaused(false);
					JumpDestination dest = new JumpDestination(entity, null);
					Global.getSector().doHyperspaceTransition(playerFleet, GateCMD.this.entity, dest, 2f);
					
					float distLY = Misc.getDistanceLY(entity, GateCMD.this.entity);
					if (entity.getCustomPlugin() instanceof GateEntityPlugin) {
						GateEntityPlugin plugin = (GateEntityPlugin) entity.getCustomPlugin();
						plugin.showBeingUsed(distLY);
					}
					if (GateCMD.this.entity.getCustomPlugin() instanceof GateEntityPlugin) {
						GateEntityPlugin plugin = (GateEntityPlugin) GateCMD.this.entity.getCustomPlugin();
						plugin.showBeingUsed(distLY);
					}
					
					ListenerUtil.reportFleetTransitingGate(Global.getSector().getPlayerFleet(),
														   GateCMD.this.entity, entity);
				}
				public void cancelledEntityPicking() {
					
				}
				public String getMenuItemNameOverrideFor(SectorEntityToken entity) {
					return null;
				}
				public String getSelectedTextOverrideFor(SectorEntityToken entity) {
					return entity.getName() + " - " + entity.getContainingLocation().getNameWithTypeShort();
				}
				public void createInfoText(TooltipMakerAPI info, SectorEntityToken entity) {
					
					int cost = computeFuelCost(entity);
					int available = (int) Global.getSector().getPlayerFleet().getCargo().getFuel();
					
					Color reqColor = Misc.getHighlightColor();
					Color availableColor = Misc.getHighlightColor();
					if (cost > available) {
						reqColor = Misc.getNegativeHighlightColor();
					}
					
					info.setParaSmallInsignia();
//					LabelAPI label = info.addPara("Transit requires %s fuel. "
//							+ "You have %s units of fuel available.", 0f,
//							Misc.getTextColor(),
//							//Misc.getGrayColor(),
//							availColor, Misc.getWithDGS(cost), Misc.getWithDGS(available));
//					label.setHighlightColors(reqColor, availColor);
					
					info.beginGrid(200f, 2, Misc.getGrayColor());
					info.setGridFontSmallInsignia();
					info.addToGrid(0, 0, "    Fuel required:", Misc.getWithDGS(cost), reqColor);
					info.addToGrid(1, 0, "    Fuel available:", Misc.getWithDGS(available), availableColor);
					info.addGrid(0);;
				}
				public boolean canConfirmSelection(SectorEntityToken entity) {
					int cost = computeFuelCost(entity);
					int available = (int) Global.getSector().getPlayerFleet().getCargo().getFuel();
					return cost <= available;
				}
				public float getFuelColorAlphaMult() {
					return 0.5f;
				}
				public float getFuelRangeMult() { // just for showing it on the map when picking destination
					if (true) return 0f;
					if (Misc.GATE_FUEL_COST_MULT <= 0) return 0f;
					return 1f / Misc.GATE_FUEL_COST_MULT;
				}
			});
	}
	
	public static void notifyScanned(SectorEntityToken gate) {
		GateEntityPlugin.getGateData().scanned.add(gate);
		gate.getCustomPlugin().advance(0f); // makes gate activate if already did quest
	}
	

	
	
	public static void explode(SectorEntityToken gate) {
		gate.getContainingLocation().addScript(new GateExplosionScript(gate));
	}
	
}















