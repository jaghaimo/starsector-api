package com.fs.starfarer.api.impl.campaign.terrain;

import java.awt.Color;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.loading.TerrainSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BaseTerrain implements CampaignTerrainPlugin {

	public static final float EXTRA_SOUND_RADIUS = 100f;

	protected SectorEntityToken entity;
	protected String terrainId;
	protected String name = "Unknown";

	public void init(String terrainId, SectorEntityToken entity, Object param) {
		this.terrainId = terrainId;
		this.entity = entity;
	}

	public String getIconSpriteName() {
		return null;
	}

	public SectorEntityToken getRelatedEntity() {
		return null;
	}

	public SectorEntityToken getEntity() {
		return entity;
	}

	public String getTerrainId() {
		return terrainId;
	}

	protected boolean shouldCheckFleetsToApplyEffect() {
		return true;
	}

	public void advance(float amount) {
		if (amount <= 0) {
			return; // happens during game load
		}
		List<CampaignFleetAPI> fleets = entity.getContainingLocation().getFleets();

		// if (entity.isInCurrentLocation()) return;
		// if (entity.isInCurrentLocation() &&
		// entity.getContainingLocation().isHyperspace()) {
		// System.out.println(entity.getContainingLocation().getTerrainCopy().size());
		// System.out.println(entity.getContainingLocation().getFleets().size());
		// }
		if (shouldCheckFleetsToApplyEffect()) {
			float renderRange = getRenderRange();
			// renderRange *= renderRange;
			float days = Global.getSector().getClock().convertToDays(amount);
			for (CampaignFleetAPI fleet : fleets) {
				if (fleet.isStationMode())
					continue;

				float dist = Misc.getDistance(fleet.getLocation(), entity.getLocation());
				if (dist > renderRange)
					continue;

				String cat = getEffectCategory();
				String key = "$terrain_" + cat;

				MemoryAPI mem = fleet.getMemoryWithoutUpdate();
				if (cat != null && !stacksWithSelf() && mem.contains(key)) {
					continue;
				}
				// if (entity.isInCurrentLocation()) continue;
				if (containsEntity(fleet)) {
					applyEffect(fleet, days);
					if (cat != null) {
						mem.set(key, true, 0);
					}
				}
			}
		}

		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		if (fleet != null && entity.isInCurrentLocation()) {
			if (containsPoint(fleet.getLocation(), fleet.getRadius() + getExtraSoundRadius())) {
				float prox = getProximitySoundFactor();
				// System.out.println(getTerrainId() + " prox: " + prox);
				float volumeMult = prox;
				float suppressionMult = prox;
				// suppressionMult = 1f;
				// System.out.println(suppressionMult);
				if (volumeMult > 0) {
					if (shouldPlayLoopOne()) {
						String soundId = getSpec().getLoopOne();
						if (soundId != null) {
							Global.getSector().getCampaignUI()
									.suppressMusic(spec.getMusicSuppression() * suppressionMult);
							Global.getSoundPlayer().playLoop(soundId, fleet, getLoopOnePitch(),
									getLoopOneVolume() * volumeMult, fleet.getLocation(), Misc.ZERO);
						}
					}
					if (shouldPlayLoopTwo()) {
						String soundId = getSpec().getLoopTwo();
						if (soundId != null) {
							Global.getSector().getCampaignUI()
									.suppressMusic(spec.getMusicSuppression() * suppressionMult);
							Global.getSoundPlayer().playLoop(soundId, fleet, getLoopTwoPitch(),
									getLoopTwoVolume() * volumeMult, fleet.getLocation(), Misc.ZERO);
						}
					}
					if (shouldPlayLoopThree()) {
						String soundId = getSpec().getLoopThree();
						if (soundId != null) {
							Global.getSector().getCampaignUI()
									.suppressMusic(spec.getMusicSuppression() * suppressionMult);
							Global.getSoundPlayer().playLoop(soundId, fleet, getLoopThreePitch(),
									getLoopThreeVolume() * volumeMult, fleet.getLocation(), Misc.ZERO);
						}
					}
					if (shouldPlayLoopFour()) {
						String soundId = getSpec().getLoopFour();
						if (soundId != null) {
							Global.getSector().getCampaignUI()
									.suppressMusic(spec.getMusicSuppression() * suppressionMult);
							Global.getSoundPlayer().playLoop(soundId, fleet, getLoopFourPitch(),
									getLoopFourVolume() * volumeMult, fleet.getLocation(), Misc.ZERO);
						}
					}
				}
			}
		}
	}

	protected float getExtraSoundRadius() {
		return EXTRA_SOUND_RADIUS;
	}

	public String getEffectCategory() {
		throw new RuntimeException("Override BaseTerrain.getEffectCategory()");
	}

	public boolean containsEntity(SectorEntityToken other) {
		return containsPoint(other.getLocation(), other.getRadius()) && !isPreventedFromAffecting(other);
	}

	public boolean containsPoint(Vector2f point, float radius) {
		return false;
	}

	public boolean stacksWithSelf() {
		return false;
	}

	public void applyEffect(SectorEntityToken entity, float days) {

	}

	public float getProximitySoundFactor() {
		return 1f;
	}

	public String getModId() {
		return terrainId + "_stat_mod";
	}

	public EnumSet<CampaignEngineLayers> getActiveLayers() {
		throw new RuntimeException(
				"Override BaseTerrain.getActiveLayers() to return the CampaignEngineLayers the terrain should render in.");
	}

	public float getRenderRange() {
		throw new RuntimeException(
				"Override BaseTerrain.getRenderRange() to return the maximum distance to render this terrain at (should exceed visible radius).");
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {

	}

	public void renderOnMap(float factor, float alphaMult) {

	}

	public void renderOnMapAbove(float factor, float alphaMult) {

	}

	public boolean hasTooltip() {
		return false;
	}

	// public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
	// }

	protected void createFirstSection(TooltipMakerAPI tooltip, boolean expanded) {
	}

	protected void createTravelSection(TooltipMakerAPI tooltip, boolean expanded, float firstPad) {
	}

	protected void createCombatSection(TooltipMakerAPI tooltip, boolean expanded) {
	}

	protected boolean shouldPlayLoopOne() {
		return getSpec().getLoopOne() != null;
	}

	protected boolean shouldPlayLoopTwo() {
		return getSpec().getLoopTwo() != null;
	}

	protected boolean shouldPlayLoopThree() {
		return getSpec().getLoopThree() != null;
	}

	protected boolean shouldPlayLoopFour() {
		return getSpec().getLoopFour() != null;
	}

	protected float getLoopOnePitch() {
		return 1f;
	}

	protected float getLoopOneVolume() {
		return 1f;
	}

	protected float getLoopTwoPitch() {
		return 1f;
	}

	protected float getLoopTwoVolume() {
		return 1f;
	}

	protected float getLoopThreePitch() {
		return 1f;
	}

	protected float getLoopThreeVolume() {
		return 1f;
	}

	protected float getLoopFourPitch() {
		return 1f;
	}

	protected float getLoopFourVolume() {
		return 1f;
	}

	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		float pad = 10f;
		float small = 5f;

		createFirstSection(tooltip, expanded);

		float nextPad = pad;
		if (expanded) {
			tooltip.addSectionHeading("Travel", Alignment.MID, pad);
			nextPad = small;
		}

		createTravelSection(tooltip, expanded, nextPad);

		if (expanded) {
			tooltip.addSectionHeading("Combat", Alignment.MID, pad);
			createCombatSection(tooltip, expanded);
		}
	}

	public boolean isTooltipExpandable() {
		return true;
	}

	public float getTooltipWidth() {
		return 350f;
	}

	public String getTerrainName() {
		return name;
	}

	public String getNameAOrAn() {
		return "a";
	}

	public void setTerrainName(String name) {
		this.name = name;
	}

	public Color getNameColor() {
		return Global.getSettings().getColor("buttonText");
	}

	public boolean hasAIFlag(Object flag) {
		return false;
	}

	public boolean hasAIFlag(Object flag, CampaignFleetAPI fleet) {
		return hasAIFlag(flag);
	}

	public float getMaxEffectRadius(Vector2f locFrom) {
		return 0f;
	}

	public float getMinEffectRadius(Vector2f locFrom) {
		return 0f;
	}

	public float getOptimalEffectRadius(Vector2f locFrom) {
		return 0f;
	}

	public boolean hasMapIcon() {
		return true;
	}

	private transient TerrainSpecAPI spec = null;

	public TerrainSpecAPI getSpec() {
		if (spec != null)
			return spec;
		spec = Global.getSettings().getTerrainSpec(terrainId);
		return spec;
	}

	public boolean canPlayerHoldStationIn() {
		return true;
	}

	public void renderOnRadar(Vector2f radarCenter, float factor, float alphaMult) {

	}

	public String getNameForTooltip() {
		return getTerrainName();
	}

	// protected boolean doNotLowerCaseName = false;
	// public String getTerrainNameLowerCase() {
	// if (doNotLowerCaseName) return getTerrainName();
	// return getTerrainName().toLowerCase();
	// }

	
	public static String TERRAIN_LOCK_KEY = "$terrain_mutex_key";
	
	public boolean isPreventedFromAffecting(SectorEntityToken other) {
		if (other.getMemoryWithoutUpdate() == null) return false;
		String id = entity.getId();
		String key = TERRAIN_LOCK_KEY;
		String lockId = other.getMemoryWithoutUpdate().getString(key);
		return lockId != null && !lockId.equals(id);
	}
	protected void preventOtherTerrainFromAffecting(SectorEntityToken other) {
		preventOtherTerrainFromAffecting(other, 0.1f);
	}
	protected void preventOtherTerrainFromAffecting(SectorEntityToken other, float dur) {
		String id = entity.getId();
		other.getMemoryWithoutUpdate().set(TERRAIN_LOCK_KEY, id, dur);
	}

}



