package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SalvorsTallyIntel extends BaseIntelPlugin {

	public static enum SalvageValue {
		NONE("none"),
		LOW("low"),
		MEDIUM("medium"),
		HIGH("high");
		private String valueString;
		private SalvageValue(String value) {
			this.valueString = value;
		}
		public String getValueString() {
			return valueString;
		}
		
	}
	
	public static class SalvorsTally {
		public int ruins;
		public int orbital;
		public int derelicts;
		public int debris;
		public int other;
		public SalvageValue value;
		public List<SectorEntityToken> all = new ArrayList<>();
	}
	
	
	protected StarSystemAPI system;
	protected long removalCheckTimestamp = 0;
	protected float daysUntilRemoveCheck = 1f;

	public SalvorsTallyIntel(StarSystemAPI system) {
		this.system = system;
	}
	
	
	@Override
	public boolean shouldRemoveIntel() {
		if (!system.isCurrentLocation()) {
			float daysSince = Global.getSector().getClock().getElapsedDaysSince(removalCheckTimestamp);
			if (daysSince > daysUntilRemoveCheck) {
				SalvorsTally tally = computeTally();
				if (tally.value == SalvageValue.NONE) {
					return true;
				}
				removalCheckTimestamp = Global.getSector().getClock().getTimestamp();
				daysUntilRemoveCheck = 3f + (float) Math.random() * 3f;
			}
		}
		return super.shouldRemoveIntel();
	}


	public StarSystemAPI getSystem() {
		return system;
	}

	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		SalvorsTally tally = computeTally();
		
		bullet(info);
		
		if (mode != ListInfoMode.IN_DESC) {
			Color highlight = h;
			if (tally.value == SalvageValue.NONE) highlight = tc;
			info.addPara("Projected value: %s", initPad, tc, highlight, tally.value.getValueString());
			initPad = 0f;
		}
		if (tally.ruins > 0) {
			String s = tally.ruins > 1 ? "s" : "";
			s = "s"; // "1 ruins" actually makes more sense here than "1 ruin"
			info.addPara("%s unexplored planetside ruin" + s, initPad, tc, h, "" + tally.ruins);
			initPad = 0f;
		}
		if (tally.orbital > 0) {
			String s = tally.orbital > 1 ? "s" : "";
			info.addPara("%s orbital installation" + s, initPad, tc, h, "" + tally.orbital);
			initPad = 0f;
		}
		if (tally.derelicts > 0) {
			String s = tally.derelicts > 1 ? "s" : "";
			info.addPara("%s derelict ship" + s, initPad, tc, h, "" + tally.derelicts);
			initPad = 0f;
		}
		if (tally.debris > 0) {
			String s = tally.debris > 1 ? "s" : "";
			info.addPara("%s unexplored debris field" + s, initPad, tc, h, "" + tally.debris);
			initPad = 0f;
		}
		if (tally.other > 0) {
			String s = tally.other > 1 ? "s" : "";
			info.addPara("%s other source" + s + " of salvage", initPad, tc, h, "" + tally.other);
			initPad = 0f;
		}
		
		
		unindent(info);
	}

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float small = 3f;
		float opad = 10f;

		SalvorsTally tally = computeTally();
		
		info.addImage(Global.getSettings().getSpriteName("illustrations", "salvor_explore_hull"), width, opad);

		if (tally.value == SalvageValue.NONE) {
			info.addPara("No salvage is known to be available in the " + system.getNameWithLowercaseTypeShort() + ".", opad);
		} else {
			info.addPara("A log of the salvage known to be available in the " + 
							system.getNameWithLowercaseTypeShort() + ". The projected total value of the known salvage is %s.",
							opad, h, tally.value.getValueString());
		}

		addBulletPoints(info, ListInfoMode.IN_DESC);
		
		info.addPara("Other, as yet undiscovered, salvage may be present in the system.", opad);
		
		addLogTimestamp(info, tc, opad);
		
		addDeleteButton(info, width);
	}

	@Override
	public String getIcon() {
		SalvorsTally tally = computeTally();
		if (tally.value == SalvageValue.NONE) {
			return Global.getSettings().getSpriteName("intel", "salvors_tally_none");
		} else if (tally.value == SalvageValue.LOW) {
			return Global.getSettings().getSpriteName("intel", "salvors_tally_low");
		} else if (tally.value == SalvageValue.MEDIUM) {
			return Global.getSettings().getSpriteName("intel", "salvors_tally_medium");
		} else {
			return Global.getSettings().getSpriteName("intel", "salvors_tally_high");
		}
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		//tags.add(Tags.INTEL_FLEET_LOG);
		SalvorsTally tally = computeTally();
		if (tally.value != SalvageValue.NONE && tally.value != SalvageValue.LOW) {
			tags.add(Tags.INTEL_EXPLORATION);
		}
		tags.add(Tags.INTEL_SALVAGE);
		return tags;
	}
	
	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		//return system.getCenter(); // shows star in hyperspace, not system map
		SalvorsTally tally = computeTally();
		if (tally.all.size() == 1) {
			return tally.all.get(0);
		}
		return system.createToken(0, 0);
	}

	public String getName() {
		return "Salvor's Tally - " + system.getBaseName();
	}

	
	@Override
	public String getCommMessageSound() {
		return super.getCommMessageSound();
		//return "ui_discovered_entity";
	}

	public String getSortString() {
		return getSortStringNewestFirst();
	}
	
	
	protected transient long tallyTimestamp;
	protected transient SalvorsTally cached;
	public SalvorsTally computeTally() {
		
		long ts = Global.getSector().getClock().getTimestamp();
		if (cached != null && ts == tallyTimestamp) {
			return cached;
		}
		
		SalvorsTally tally = new SalvorsTally();
		
		int value = 0;
		for (SectorEntityToken entity : system.getEntitiesWithTag(Tags.SALVAGEABLE)) {
			if (entity.isDiscoverable()) continue;
			if (entity.hasTag(Tags.NOT_RANDOM_MISSION_TARGET)) continue;
			if (entity.hasTag(Tags.FADING_OUT_AND_EXPIRING)) continue;
			if (entity.hasTag(Tags.EXPIRES)) continue;
			
			boolean wreck = isDerelictShip(entity);
			boolean station = isOrbitalInstallation(entity);
			
			if (wreck) {
				tally.derelicts++;
				value += 1;
			} else if (station) {
				tally.orbital++;
				value += 10;
			} else {
				tally.other++;
				value += 1;
			}
			
			if (entity.hasTag(Tags.NEUTRINO_HIGH)) {
				value += 20;
			}
			tally.all.add(entity);
		}
		
		for (CampaignTerrainAPI entity : system.getTerrainCopy()) {
			if (entity.isDiscoverable()) continue;
			if (entity.hasTag(Tags.EXPIRES)) continue;
			
			if (entity instanceof CampaignTerrainAPI &&
					((CampaignTerrainAPI)entity).getPlugin() instanceof DebrisFieldTerrainPlugin) {
				DebrisFieldTerrainPlugin plugin = (DebrisFieldTerrainPlugin) ((CampaignTerrainAPI)entity).getPlugin();
				if (plugin.isScavenged()) {
					continue;
				}
				
				tally.debris++;
				value += 1;
				tally.all.add(entity);
			}
		}
		
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isStar()) continue;
			
			MarketAPI market = planet.getMarket();
			if (market == null) continue;
			
			if (market.getSurveyLevel() != SurveyLevel.FULL) continue;
			
			if (Misc.hasUnexploredRuins(market)) {
				tally.ruins++;
				tally.all.add(planet);
				if (market.hasCondition(Conditions.RUINS_SCATTERED)) {
					value += 2;
				} else if (market.hasCondition(Conditions.RUINS_WIDESPREAD)) {
					value += 6;
				} else if (market.hasCondition(Conditions.RUINS_EXTENSIVE)) {
					value += 10;
				} else if (market.hasCondition(Conditions.RUINS_VAST)) {
					value += 20;
				}
			}
		}
		if (value <= 0) {
			tally.value = SalvageValue.NONE;
		} else if (value < 10) {
			tally.value = SalvageValue.LOW;
		} else if (value < 20) {
			tally.value = SalvageValue.MEDIUM;
		} else {
			tally.value = SalvageValue.HIGH;
		}
		
		tallyTimestamp = ts;
		cached = tally;
		
		return tally;
	}
	
	public static boolean isDerelictShip(SectorEntityToken entity) {
		boolean wreck = Entities.WRECK.equals(entity.getCustomEntityType());
		wreck |= entity.hasTag(Tags.WRECK);
		return wreck;
	}
	
	public static boolean isOrbitalInstallation(SectorEntityToken entity) {
		boolean station = false;
		if (entity.getCustomEntitySpec() != null) {
			CustomEntitySpecAPI spec = entity.getCustomEntitySpec();
			if (spec.getShortName().toLowerCase().contains("station") ||
					spec.getShortName().toLowerCase().contains("habitat") ||
					spec.getShortName().toLowerCase().contains("orbital") ||
					entity.hasTag("salvor_orbital")) {
				station = true;
			}
		}
		return station;
	}
	
	public static SalvorsTallyIntel getSalvorsTallyIntel(StarSystemAPI system) {
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(SalvorsTallyIntel.class)) {
			if (((SalvorsTallyIntel)intel).getSystem() == system) return (SalvorsTallyIntel)intel;
		}
		return null;
	}
}


