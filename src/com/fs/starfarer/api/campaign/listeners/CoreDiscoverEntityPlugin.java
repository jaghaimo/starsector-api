package com.fs.starfarer.api.campaign.listeners;

import java.awt.Color;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.RemnantNexusIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.SalvorsTallyIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.SalvorsTallyIntel.SalvageValue;
import com.fs.starfarer.api.impl.campaign.intel.misc.SalvorsTallyIntel.SalvorsTally;
import com.fs.starfarer.api.impl.campaign.intel.misc.WarningBeaconIntel;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.util.Misc;

public class CoreDiscoverEntityPlugin implements DiscoverEntityPlugin, 
					CurrentLocationChangedListener, SurveyPlanetListener, DetectedEntityListener, ShowLootListener {
	
	/**
	 * How long since discovering or salvaging something before SalvorsTallyIntel pops up.
	 * Point is to not pop it up if it's just going to go away real soon anyway.
	 */
	public static float SALVORS_TALLY_DELAY_SECONDS = 10f;
	
	public void discoverEntity(SectorEntityToken entity) {
		
		entity.setDiscoverable(null);
		entity.setSensorProfile(null);
		
		if (entity.hasTag(Tags.WARNING_BEACON)) {
			WarningBeaconIntel intel = new WarningBeaconIntel(entity);
			Global.getSector().getIntelManager().addIntel(intel);
		} else {
			Color c = Global.getSector().getPlayerFaction().getBaseUIColor();
			MessageIntel intel = new MessageIntel("Discovered: " + entity.getName(),
												  c, new String[] {entity.getName()}, c);
			intel.setSound("ui_discovered_entity");
			intel.setIcon(Global.getSettings().getSpriteName("intel", "discovered_entity"));
			Global.getSector().getCampaignUI().addMessage(intel);
		}
		

		
		float xp = 0;
		if (entity.hasDiscoveryXP()) {
			xp = entity.getDiscoveryXP();
		} else if (entity.getCustomEntityType() != null) {
			SalvageEntityGenDataSpec salvageSpec = (SalvageEntityGenDataSpec) Global.getSettings().getSpec(SalvageEntityGenDataSpec.class, entity.getCustomEntityType(), true);
			if (salvageSpec != null) {
				xp = salvageSpec.getXpDiscover();
			}
		}
		if (xp > 0) {
			Global.getSector().getPlayerPerson().getStats().addXP((long) xp);
		}
		
		ListenerUtil.reportEntityDiscovered(entity);
		
		if (entity.hasTag(Tags.SALVAGEABLE)) {
			addSalvorsTallyIfNeeded(entity.getStarSystem());
		}
	}
	

	public int getHandlingPriority(Object params) {
		return 0;
	}


	@Override
	public void reportCurrentLocationChanged(LocationAPI prev, LocationAPI curr) {
		if (prev instanceof StarSystemAPI) {
			StarSystemAPI system = (StarSystemAPI) prev;
			removeSalvorsTallyIfNeeded(system);
		}
		if (curr instanceof StarSystemAPI) {
			StarSystemAPI system = (StarSystemAPI) curr;
			addSalvorsTallyIfNeeded(system);
		}
	}
	
	@Override
	public void reportPlayerSurveyedPlanet(PlanetAPI planet) {
		if (Misc.hasUnexploredRuins(planet.getMarket())) {
			addSalvorsTallyIfNeeded(planet.getStarSystem());
		}
	}
	
	@Override
	public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
		SectorEntityToken target = dialog.getInteractionTarget();
		if (target == null) return;
		if (!target.hasTag(Tags.SALVAGEABLE)) return;
		if (target.getStarSystem() == null) return;
		
		addSalvorsTallyIfNeeded(target.getStarSystem());
	}
	
	@Override
	public void reportDetectedEntity(SectorEntityToken entity, VisibilityLevel level) {
		//System.out.println("Detected entity: " + entity.getName() + " - " + level.name());
		if (entity instanceof CampaignFleetAPI && level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS) {
			CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
			if (fleet.isStationMode() && fleet.getFaction().getId().equals(Factions.REMNANTS)) {
				addRemnantNexusIntelIfNeeded(fleet);
			}
			
		}
	}
	
	
	public void addRemnantNexusIntelIfNeeded(CampaignFleetAPI nexus) {
		if (RemnantNexusIntel.getNexusIntel(nexus) == null) {
			new RemnantNexusIntel(nexus); // adds the intel
		}
	}
	
	
	public static class SalvorsTallyAdder implements EveryFrameScript {

		protected float delay = 0f;
		protected boolean done = false;
		protected SalvorsTallyIntel intel;
		
		public SalvorsTallyAdder(SalvorsTallyIntel intel, float delay) {
			this.delay = delay;
			this.intel = intel;
		}
		
		@Override
		public boolean isDone() {
			return done;
		}

		@Override
		public boolean runWhilePaused() {
			return false;
		}

		@Override
		public void advance(float amount) {
			delay -= amount;
			if (delay <= 0) {
				done = true;
				SalvorsTally tally = intel.computeTally();
				if (tally.value != SalvageValue.NONE) {
					Global.getSector().getIntelManager().addIntel(intel);
				}
			}
		}
		
	}
	
	public static void addSalvorsTallyIfNeeded(StarSystemAPI system) {
		if (system == null) return;
		if (system.hasTag(Tags.THEME_CORE)) return;
		if (system.getType() == StarSystemType.DEEP_SPACE) return;
		if (TutorialMissionIntel.isTutorialInProgress()) return;
		
		SalvorsTallyIntel intel = SalvorsTallyIntel.getSalvorsTallyIntel(system);
		if (intel == null) {
			// Delay adding so it doesn't necessarily pop up for every little thing you see and
			// immediately salvage
			for (EveryFrameScript curr : Global.getSector().getScripts()) {
				if (curr instanceof SalvorsTallyAdder) {
					SalvorsTallyAdder adder = (SalvorsTallyAdder) curr;
					if (adder.intel.getSystem() == system) {
						adder.delay = SALVORS_TALLY_DELAY_SECONDS;
						return;
					}
				}
			}		
			
			intel = new SalvorsTallyIntel(system);
			SalvorsTally tally = intel.computeTally();
			if (tally.value != SalvageValue.NONE) {
				SalvorsTallyAdder adder = new SalvorsTallyAdder(intel, SALVORS_TALLY_DELAY_SECONDS);
				Global.getSector().addScript(adder);
				//Global.getSector().getIntelManager().addIntel(intel);
			}
		}
	}
	
	public void removeSalvorsTallyIfNeeded(StarSystemAPI system) {
		if (system == null) return;
		SalvorsTallyIntel intel = SalvorsTallyIntel.getSalvorsTallyIntel(system);
		if (intel != null) {
			SalvorsTally tally = intel.computeTally();
			if (tally.value == SalvageValue.NONE) {
				intel.endImmediately();
			}
		}
	}


}
