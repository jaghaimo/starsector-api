package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.PlayerFleetPersonnelTracker;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.intel.misc.CryosleeperIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.HypershuntIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.WormholeIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.world.NamelessRock;
import com.fs.starfarer.api.impl.combat.threat.DisposableThreatFleetManager;
import com.fs.starfarer.api.impl.combat.threat.DisposableThreatFleetManager.ThreatFleetCreationParams;
import com.fs.starfarer.api.impl.combat.threat.ThreatFleetBehaviorScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * 
 */
public class MiscCMD extends BaseCommandPlugin {

	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected MemoryAPI memory;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;

	
	public MiscCMD() {
	}
	
	public MiscCMD(SectorEntityToken entity) {
		init(entity);
	}
	
	protected void init(SectorEntityToken entity) {
		memory = entity.getMemoryWithoutUpdate();
		this.entity = entity;
		playerFleet = Global.getSector().getPlayerFleet();
		
		
	}

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		entity = dialog.getInteractionTarget();
		init(entity);
		
		memory = getEntityMemory(memoryMap);
		
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		
		if (command.equals("addCryosleeperIntel")) {
			if (CryosleeperIntel.getCryosleeperIntel(entity) == null) {
				new CryosleeperIntel(entity, dialog.getTextPanel());
			}
		} else if (command.equals("addHypershuntIntel")) {
			if (HypershuntIntel.getHypershuntIntel(entity) == null) {
				new HypershuntIntel(entity, dialog.getTextPanel());
			}
		} else if (command.equals("addWormholeIntel")) {
			addWormholeIntelIfNeeded(entity, dialog.getTextPanel(), true);
		} else if (command.equals("addMarineXP")) {
			float amount = params.get(1).getFloat(memoryMap);
			PlayerFleetPersonnelTracker.getInstance().update();
			PlayerFleetPersonnelTracker.getInstance().getMarineData().addXP(amount);
		} else if (command.equals("mk1_spawnThreatFleet")) {
			StarSystemAPI system = Global.getSector().getStarSystem(NamelessRock.NAMELESS_ROCK_LOCATION_ID);
			
			ThreatFleetCreationParams p = new ThreatFleetCreationParams();
			p.numHives = 1;
			p.numOverseers = 1;
			p.numDestroyers = 2;
			p.numFrigates = 4;
			p.fleetType = FleetTypes.PATROL_SMALL;
			
			CampaignFleetAPI fleet = DisposableThreatFleetManager.createThreatFleet(p, new Random());
			ThreatFleetBehaviorScript behavior = new ThreatFleetBehaviorScript(fleet, system);
			behavior.setSeenByPlayer();
			fleet.addScript(behavior);
			
			system.addEntity(fleet);
			float radius = 1000f + 500f * (float) Math.random();
			Vector2f loc = Misc.getPointAtRadius(playerFleet.getLocation(), radius);
			fleet.setLocation(loc.x, loc.y);
		}
		
		return true;
	}
	
	public static void addWormholeIntelIfNeeded(SectorEntityToken entity, TextPanelAPI textPanel, boolean deployed) {
		if (WormholeIntel.getWormholeIntel(entity) == null) {
			new WormholeIntel(entity, textPanel, deployed);
		}
	}
	
}




















