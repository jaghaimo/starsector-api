package com.fs.starfarer.api.impl.campaign.intel.events.ht;

import java.util.LinkedHashSet;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.terrain.PulsarBeamTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

/**
 * @author Alex
 *
 * Copyright 2022 Fractal Softworks, LLC
 */
public class HTFactorTracker implements EveryFrameScript {

	public static float CHECK_DAYS = 0.05f;
	
	protected IntervalUtil interval = new IntervalUtil(CHECK_DAYS * 0.8f, CHECK_DAYS * 1.2f);
	protected float burnBasedPoints = 0f;
	protected float daysSinceAtHighBurn = 1f;
	protected boolean canCheckSB = true;
	
	protected LinkedHashSet<String> scanned = new LinkedHashSet<String>();
	
	protected Object readResolve() {
		if (scanned == null) {
			scanned = new LinkedHashSet<String>();
		}
		return this;
	}
	
	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}

	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		
		interval.advance(days);
		
		if (interval.intervalElapsed()) {
			checkHighBurn(interval.getIntervalDuration());
			checkSensorBursts();
		}
	}

	protected void checkHighBurn(float days) {
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		if (pf != null && pf.isInHyperspace()) {
			float burn = pf.getCurrBurnLevel();
			
			if (burn > 20) {
				daysSinceAtHighBurn = 0f;
			} else {
				daysSinceAtHighBurn += days;
			}

			float add = 0f;
			float min = 0;
			float max = 0;
			float f = 0;
			if (burn > 40) {
				min = HTPoints.PER_DAY_AT_BURN_40;
				max = HTPoints.PER_DAY_AT_BURN_50;
				f = (Math.min(burn, HTPoints.MAX_BURN_FOR_POINT_GAIN) - 40) / 10f;
			} else if (burn > 30) {
				min = HTPoints.PER_DAY_AT_BURN_30;
				max = HTPoints.PER_DAY_AT_BURN_40;
				f = (burn - 30) / 10f;
			} else if (burn > 20) {
				min = HTPoints.PER_DAY_AT_BURN_20;
				max = HTPoints.PER_DAY_AT_BURN_30;
				f = (burn - 20) / 10f;
			}
			
			add = min + (max - min) * f;
			add *= CHECK_DAYS;
			//add *= 100;
			
//			if (Global.getSettings().isDevMode()) {
//				add = 100;
//			}
			
			if (pf.getMemoryWithoutUpdate().getBoolean(MemFlags.NO_HIGH_BURN_TOPOGRAPHY_READINGS)) {
				add = 0;
			}
			
			if (add > 0) {
				burnBasedPoints += add;
				//System.out.println("Added: " + add + ", total: " + burnBasedPoints);
			}
			int chunk = HTPoints.BURN_POINT_CHUNK_SIZE;
			//chunk = 1;
			if (burnBasedPoints >= chunk && daysSinceAtHighBurn > 0.3f) {
				int mult = (int) burnBasedPoints / chunk;
				int points = chunk * mult;
				burnBasedPoints -= points;
				HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(new HTHighBurnFactor(points), null);
			}
		} else {
			daysSinceAtHighBurn = 1f;
		}
	}

	public void checkSensorBursts() {
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		if (pf == null) return;
		AbilityPlugin sb = pf.getAbility(Abilities.SENSOR_BURST);
		if (sb == null) return;
		
		if (sb.isUsable() || sb.getLevel() <= 0) {
			canCheckSB = true;
		}
		
		if (canCheckSB && !pf.isInHyperspace() && sb.isInProgress() && sb.getLevel() > 0.9f &&
				!pf.getContainingLocation().hasTag(Tags.NO_TOPOGRAPHY_SCANS)) {
			for (SectorEntityToken entity : pf.getContainingLocation().getAllEntities()) {
				checkBlackHole(entity);
				checkIonStorm(entity);
				checkGasGiant(entity);
				checkPulsar(entity);
			}
			for (CampaignTerrainAPI terrain : pf.getContainingLocation().getTerrainCopy()) {
				checkMagneticField(terrain);
			}
			
			checkSystemCenter();
			
			canCheckSB = false;
		}
	}
	
	protected void checkBlackHole(SectorEntityToken entity) {
		if (!(entity instanceof PlanetAPI)) return;
		
		PlanetAPI planet = (PlanetAPI) entity;
		if (!planet.getSpec().isBlackHole()) return;
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		float dist = Misc.getDistance(pf.getLocation(), entity.getLocation());
		
		StarCoronaTerrainPlugin eventHorizon = Misc.getCoronaFor(planet);
		if (eventHorizon == null) return;
		
		String id1 = planet.getId() + "_1";
		String id2 = planet.getId() + "_2";
		
		float closeRange = planet.getRadius() + 300f;
		
		if (dist < closeRange) {
			if (scanned.contains(id2)) {
				addMessage("Black hole already scanned at short range");
			} else {
				HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(
						new HTScanFactor("Black hole scanned at short range (" + planet.getName() + ")", HTPoints.SCAN_BLACK_HOLE_SHORT_RANGE), null);
				scanned.add(id2);
			}
		} else if (eventHorizon.containsEntity(pf)) {
			if (scanned.contains(id1)) {
				addMessage("Black hole already scanned at long range");
			} else {
				HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(
						new HTScanFactor("Black hole scanned at long range (" + planet.getName() + ")", HTPoints.SCAN_BLACK_HOLE_LONG_RANGE), null);
				scanned.add(id1);
			}
		}
	}
	
	protected void checkIonStorm(SectorEntityToken entity) {
		if (!(entity instanceof PlanetAPI)) return;
		
		PlanetAPI planet = (PlanetAPI) entity;
		if (!planet.isGasGiant()) return;
		
		StarCoronaTerrainPlugin ionStorm = Misc.getCoronaFor(planet);
		if (ionStorm == null) return;
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		String id = ionStorm.getEntity().getId();
		
		if (ionStorm.containsEntity(pf)) {
			if (scanned.contains(id)) {
				addMessage("Ion storm already scanned");
			} else {
				HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(
						new HTScanFactor("Ion storm scanned (" + planet.getName() + ")", HTPoints.SCAN_ION_STORM), null);
				scanned.add(id);
			}
		}
	}
	
	protected void checkMagneticField(CampaignTerrainAPI terrain) {
		if (terrain.getPlugin() == null) return;
		if (!Terrain.MAGNETIC_FIELD.equals(terrain.getType())) return;

		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		String id = terrain.getId();
		
		if (terrain.getPlugin().containsEntity(pf)) {
			if (scanned.contains(id)) {
				addMessage("Magnetic field already scanned");
			} else {
				HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(
						new HTScanFactor("Magnetic field scanned", HTPoints.SCAN_MAGNETIC_FIELD), null);
				scanned.add(id);
			}
		}
	}
	
	protected void checkGasGiant(SectorEntityToken entity) {
		if (!(entity instanceof PlanetAPI)) return;
		
		PlanetAPI planet = (PlanetAPI) entity;
		if (!planet.isGasGiant()) return;
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		String id = planet.getId();
		
		float dist = Misc.getDistance(pf.getLocation(), entity.getLocation());
		boolean inRange = dist < 500f + planet.getRadius();
		if (inRange) {
			if (scanned.contains(id)) {
				addMessage("Gas giant already scanned");
			} else {
				HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(
						new HTScanFactor("Gas giant scanned (" + planet.getName() + ")", HTPoints.SCAN_GAS_GIANT), null);
				scanned.add(id);
			}
		}
	}
	
	protected void checkSystemCenter() {
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		StarSystemAPI system = pf.getStarSystem();
		if (system == null) return;
		
		String type = null;
		int points = 0;
		switch (system.getType()) {
		case BINARY_CLOSE:
			type = "Center of binary system";
			points = HTPoints.SCAN_BINARY;
			break;
		case NEBULA:
			type = "Center of starless nebula";
			points = HTPoints.SCAN_NEBULA;
			break;
		case TRINARY_1CLOSE_1FAR:
			type = "Center of binary system";
			points = HTPoints.SCAN_BINARY;
			break;
		case TRINARY_2CLOSE:
			type = "Center of trinary system";
			points = HTPoints.SCAN_TRINARY;
			break;
		default:
			int count = 0;
			for (PlanetAPI curr : system.getPlanets()) {
				if (!curr.isStar()) continue;
				float dist = Misc.getDistance(curr.getLocation(), pf.getLocation());
				if (dist < 2000 + curr.getRadius()) {
					count++;
				}
			}
			if (count > 1) {
				type = "Stellar conflux";
				if (count == 2) {
					points = HTPoints.SCAN_BINARY;
				} else {
					points = HTPoints.SCAN_TRINARY;
				}
			}
			break;
		}
		if (type == null) return;
		
		String id = "systemtypescan_" + system.getId();
		float range = pf.getLocation().length();
		
		if (range < 2000) {
			if (scanned.contains(id)) {
				addMessage("Center of star system already scanned");
			} else {
				HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(
						new HTScanFactor(type + " scanned (" + system.getBaseName() + ")", points), null);
				scanned.add(id);
			}
		}
	}
	
	protected void checkPulsar(SectorEntityToken entity) {
		if (!(entity instanceof PlanetAPI)) return;
		
		PlanetAPI planet = (PlanetAPI) entity;
		if (!planet.getSpec().isPulsar()) return;
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		StarCoronaTerrainPlugin corona = Misc.getCoronaFor(planet);
		if (corona == null) return;
		
		PulsarBeamTerrainPlugin pulsar = Misc.getPulsarFor(planet);
		if (pulsar == null) return;
		
		String id1 = planet.getId() + "_1";
		String id2 = planet.getId() + "_2";
		
		if (corona.containsEntity(pf)) {
			if (scanned.contains(id2)) {
				addMessage("Neutron star already scanned");
			} else {
				HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(
						new HTScanFactor("Neutron star scanned (" + planet.getName() + ")", HTPoints.SCAN_NEUTRON_STAR), null);
				scanned.add(id2);
			}
		}
		
		if (pulsar.containsEntity(pf)) {
			if (scanned.contains(id1)) {
				addMessage("Pulsar beam already scanned");
			} else {
				HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(
						new HTScanFactor("Pulsar beam scanned (" + planet.getName() + ")", HTPoints.SCAN_PULSAR_BEAM), null);
				scanned.add(id1);
			}
		}
	}

	
	protected void addMessage(String text) {
		Global.getSector().getCampaignUI().getMessageDisplay().addMessage(text + ", no new topographic data acquired", Misc.getNegativeHighlightColor());
	}
}




