/**
 * 
 */
package com.fs.starfarer.api.characters;

import java.awt.Color;
import java.util.EnumSet;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.loading.AbilitySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;


public interface AbilityPlugin {
	void init(String id, SectorEntityToken entity);
	
	/**
	 * Called from the UI when the button for this ability is pressed. Should not be called by the AI for
	 * other ability-using fleets, for example.
	 */
	void pressButton();
	
	
	/**
	 * Programmatic way to activate the ability.
	 * 
	 * Expected behavior:
	 * if (!isActiveOrInProgress()) {
	 *   <activate ability>
	 * }
	 */
	void activate();
	
	/**
	 * Toggleable or interruptable abilities should implement this method so
	 * that other abilities may turn them off or interrupt them if needed.
	 * 
	 * Expected behavior:
	 * if (isActiveOrInProgress()) {
	 *   <deactivate ability>
	 * }
	 * 
	 */
	void deactivate();
	
	
	/**
	 * After this method is called, it should be possible to remove the ability
	 * from the entity without any after-effects. 
	 */
	void cleanup();
	
	
	boolean showActiveIndicator();
	boolean showProgressIndicator();
	boolean showCooldownIndicator();
	
	/**
	 * Whether the ability can be activated / the UI button corresponding to it is enabled.
	 * @return
	 */
	boolean isUsable();
	
	/**
	 * Whether a toggle-style ability is turned on. Duration abilities will always return false here;
	 * use isInProgress() to get their status.
	 * @return
	 */
	boolean isActive();
	
	
	boolean isInProgress();
	boolean isOnCooldown();
	
	/**
	 * Should return (isActive() || getProgressFraction() > 0).
	 * @return
	 */
	boolean isActiveOrInProgress();
	
	/**
	 * 0 at start of cooldown, 1 at end.
	 * @return
	 */
	float getCooldownFraction();
	
	/**
	 * 0 at start of progress, 1 at end.
	 * @return
	 */
	float getProgressFraction();
	
	Color getProgressColor();
	Color getActiveColor();
	Color getCooldownColor();
	
	boolean isCooldownRenderingAdditive();
	
	boolean hasCustomButtonPressSounds();
	
	String getSpriteName();
	SectorEntityToken getEntity();
	String getId();
	
	
	/**
	 * Will be called every frame the tooltip is shown, so the tooltip can be dynamic.
	 * @param tooltip
	 * @param expanded 
	 */
	void createTooltip(TooltipMakerAPI tooltip, boolean expanded);
	boolean hasTooltip();
	boolean isTooltipExpandable();
	float getTooltipWidth();

	
	
	/**
	 * Make this ability unusable for 1-2 frames after this call.
	 */
	void forceDisable();
	
	
	void fleetJoinedBattle(BattleAPI battle);
	void fleetLeftBattle(BattleAPI battle, boolean engagedInHostilities);
	
	/**
	 * Only called for the player fleet.
	 * @param market
	 */
	void fleetOpenedMarket(MarketAPI market);

	AbilitySpecAPI getSpec();
	
	
	void render(CampaignEngineLayers layer, ViewportAPI viewport);
	EnumSet<CampaignEngineLayers> getActiveLayers();

	void setCooldownLeft(float days);

	float getCooldownLeft();

	float getLevel();
	
	
}


