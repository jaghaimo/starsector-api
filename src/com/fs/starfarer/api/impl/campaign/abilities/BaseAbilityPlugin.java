package com.fs.starfarer.api.impl.campaign.abilities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.loading.AbilitySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public abstract class BaseAbilityPlugin implements AbilityPlugin, EveryFrameScript{
	protected SectorEntityToken entity;
	protected String id;
	
	protected int disableFrames = 0;
	
	protected transient AbilitySpecAPI spec = null;
	
	public void init(String id, SectorEntityToken entity) {
		this.id = id;
		this.entity = entity;
		readResolve();
	}
	
	protected Object readResolve() {
		spec = Global.getSettings().getAbilitySpec(id);
		return this;
	}
	Object writeReplace() {
		return this;
	}
	
	public String getOnSoundUI() { return spec.getUIOn(); }
	public String getOnSoundWorld() { return spec.getWorldOn(); }
	public String getOffSoundUI() { return spec.getUIOff(); }
	public String getOffSoundWorld() { return spec.getWorldOff(); }

	// no support for UI loops currently; just playing a stereo sound is a workaround
	// but doesn't use the dedicated UI sound sources
	public String getLoopSoundUI() { return spec.getUILoop(); }
	public float getLoopSoundUIVolume() { return 1f; }
	public float getLoopSoundUIPitch() { return 1f; }
	
	public String getLoopSoundWorld() { return spec.getWorldLoop(); }
	public float getLoopSoundWorldVolume() { return 1f; }
	public float getLoopSoundWorldPitch() { return 1f; }
	

	protected void interruptIncompatible() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		for (AbilityPlugin curr : fleet.getAbilities().values()) {
			if (curr == this) continue;
			if (!isCompatible(curr) && curr.isActive()) {
				curr.deactivate();
			}
		}
	}
	
	protected void disableIncompatible() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		for (AbilityPlugin curr : fleet.getAbilities().values()) {
			if (curr == this) continue;
			if (!isCompatible(curr)) {
				curr.forceDisable();
			}
		}
	}
	
	protected boolean isCompatible(AbilityPlugin other) {
		for (String tag : spec.getTags()) {
			if (spec.isPositiveTag(tag) && other.getSpec().hasTag(tag)) return false;
			if (other.getSpec().hasOppositeTag(tag)) return false;
		}
		return true;
	}
	
	protected void addIncompatibleToTooltip(TooltipMakerAPI tooltip, String desc, String descShort, boolean expanded) {
		List<AbilityPlugin> list = getInterruptedList();
		if (list.isEmpty()) return;
		
		if (expanded) {
			String pre = desc;
			tooltip.addPara(pre, 10f);
			
			String str = "";
			for (AbilityPlugin curr : list) {
				str += "    " + curr.getSpec().getName() + "\n";
			}
			str = str.substring(0, str.length() - 1);
			Color c = Misc.getTooltipTitleAndLightHighlightColor();
			//c = Misc.interpolateColor(c, Color.black, 0.2f);
			//c = Misc.getGrayColor();
			tooltip.addPara(str, c, 3f);
		} else {
			Color c = Misc.getGrayColor();			
			tooltip.addPara(descShort, c, 10f);
		}
	}
	
	public List<AbilityPlugin> getInterruptedList() {
		List<AbilityPlugin> result = new ArrayList<AbilityPlugin>(); 
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return result;
		
		for (AbilityPlugin curr : fleet.getAbilities().values()) {
			if (curr == this) continue;
			
			if (this instanceof BaseToggleAbility && curr instanceof BaseDurationAbility) {
				continue;
			}
			if (!isCompatible(curr)) {
				result.add(curr);
			}
		}
		Collections.sort(result, new Comparator<AbilityPlugin>() {
			public int compare(AbilityPlugin o1, AbilityPlugin o2) {
				return o1.getSpec().getSortOrder() - o2.getSpec().getSortOrder();
			}
		});
		return result;
	}
	
	
	public String getModId() {
		return id + "_ability_mod";
	}
	
	public CampaignFleetAPI getFleet() {
		if (entity instanceof CampaignFleetAPI) {
			return (CampaignFleetAPI) entity;
		}
		return null;
	}
	
	public SectorEntityToken getEntity() {
		return entity;
	}
	
	public String getId() {
		return id;
	}
	
	public void advance(float amount) {
		disableFrames--;
		if (disableFrames < 0) disableFrames = 0;
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
	
	public boolean showActiveIndicator() {
		return isActive();
	}
	
	public boolean isUsable() {
		return !isOnCooldown() && disableFrames <= 0;
	}
	
	public void forceDisable() {
		disableFrames = 2;
	}
	
	public float getCooldownFraction() {
		return 1f;
	}
	
	public boolean hasCustomButtonPressSounds() {
		return false;
	}
	
	public boolean hasTooltip() {
		return false;
	}
	
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
	}
	
	public boolean isTooltipExpandable() {
		return true;
	}
	
	public float getTooltipWidth() {
		return 350f;
	}


	public void pressButton() {
		
	}

	public String getSpriteName() {
		return spec.getIconName();
//		if (spec.getIconName() != null) return spec.getIconName();
//		return Global.getSettings().getSpriteName("abilities", "empty_slot");
	}

	public void activate() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null || !fleet.isPlayerFleet()) return;
		Global.getSector().reportPlayerActivatedAbility(this, null);
		//interruptIncompatible();
	}

	public void deactivate() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null || !fleet.isPlayerFleet()) return;
		Global.getSector().reportPlayerDeactivatedAbility(this, null);
	}


	private static Color defaultCooldownColor = new Color(0,0,0,171);
	public Color getCooldownColor() {
		return defaultCooldownColor;
	}

	public Color getProgressColor() {
		return entity.getFaction().getBrightUIColor();
	}
	
	public Color getActiveColor() {
		return entity.getFaction().getBrightUIColor();
	}

	public float getProgressFraction() {
		return 0;
	}

	public boolean isActive() {
		return false;
	}

	public boolean isActiveOrInProgress() {
		return isActive() || isInProgress();
	}

	public boolean isInProgress() {
		return getProgressFraction() > 0;
	}

	public boolean showCooldownIndicator() {
		return getCooldownFraction() < 1;
	}

	public boolean showProgressIndicator() {
		return getProgressFraction() > 0;
	}
	
	
	public boolean isOnCooldown() {
		return getCooldownFraction() < 1f;
	}

	public void cleanup() {
		
	}

	public boolean isCooldownRenderingAdditive() {
		return false;
	}
	
	public abstract void setCooldownLeft(float days);
	public abstract float getCooldownLeft();
	
	protected String getActivationText() {
		return Misc.ucFirst(spec.getName().toLowerCase());
		//return null;
	}
	
	protected String getDeactivationText() {
		return null;
	}

	public void fleetJoinedBattle(BattleAPI battle) {
	}

	public void fleetLeftBattle(BattleAPI battle, boolean engagedInHostilities) {
	}

	public void fleetOpenedMarket(MarketAPI market) {
	}

	public AbilitySpecAPI getSpec() {
		return spec;
	}

	public EnumSet<CampaignEngineLayers> getActiveLayers() {
		return null;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
	}

	public float getLevel() {
		return 0;
	}

	
}
