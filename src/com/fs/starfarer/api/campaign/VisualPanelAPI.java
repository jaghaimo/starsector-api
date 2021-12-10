package com.fs.starfarer.api.campaign;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.InteractionDialogImageVisual;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;

public interface VisualPanelAPI {
	void setVisualFade(float in, float out);
	
	void showFleetInfo(String titleOne, CampaignFleetAPI one, String titleTwo, CampaignFleetAPI two);
	void showFleetInfo(String titleOne, CampaignFleetAPI one, String titleTwo, CampaignFleetAPI two, FleetEncounterContextPlugin context);
	void showPersonInfo(PersonAPI person);
	void showSecondPerson(PersonAPI person);
	void hideSecondPerson();
	void showPlanetInfo(SectorEntityToken planet);
	void showFleetMemberInfo(FleetMemberAPI member);
	
	void showImagePortion(String category, String id,
						  float x, float y, float w, float h,
						  float xOffset, float yOffset,
						  float displayWidth, float displayHeight);
	void showImagePortion(String category, String id,
	 					  float w, float h,
	 					  float xOffset, float yOffset,
	 					  float displayWidth, float displayHeight);
	void showImageVisual(InteractionDialogImageVisual visual);
	
	CustomPanelAPI showCustomPanel(float width, float height, CustomUIPanelPlugin plugin);
	
	void fadeVisualOut();
	
	void showLoot(String title, CargoAPI otherCargo, boolean generatePods, CoreInteractionListener listener);
	void showLoot(String title, CargoAPI otherCargo, boolean canLeavePersonnel, boolean revealMode, boolean generatePods, CoreInteractionListener listener);
	
	/**
	 * The noCost parameter isn't used; (other.isFreeTranser() || other.getFaction().isNeutralFaction) is used instead.
	 * Use the other showCore() method.
	 * @param tabId
	 * @param other
	 * @param noCost
	 * @param listener
	 */
	@Deprecated void showCore(CoreUITabId tabId, SectorEntityToken other, boolean noCost, CoreInteractionListener listener);
	
	void showCore(CoreUITabId tabId, SectorEntityToken other, CoreInteractionListener listener);
	void showCore(CoreUITabId tabId, SectorEntityToken other, CoreUITradeMode mode, CoreInteractionListener listener);
	void hideCore();

	void showNewGameOptionsPanel(CharacterCreationData data);

	void showPersonInfo(PersonAPI person, boolean minimalMode);

	void showPreBattleJoinInfo(String playerTitle, CampaignFleetAPI playerFleet, String titleOne, String titleTwo,
								FleetEncounterContextPlugin context);

	void showFleetMemberInfo(FleetMemberAPI member, boolean recoveryMode);

	void showFleetInfo(String titleOne, CampaignFleetAPI one, String titleTwo, CampaignFleetAPI two, FleetEncounterContextPlugin context, boolean recoveryMode);

	void finishFadeFast();

	void saveCurrentVisual();

	void restoreSavedVisual();

	void closeCoreUI();

	void showPersonInfo(PersonAPI person, boolean minimalMode, boolean withRelBar);
	void showThirdPerson(PersonAPI person);
	void hideThirdPerson();

	void showCore(CoreUITabId tabId, SectorEntityToken other, Object custom, CoreInteractionListener listener);

	void showMapMarker(SectorEntityToken marker, String title, Color titleColor, 
						boolean withIntel, String icon, String text, Set<String> intelTags);

	void removeMapMarkerFromPersonInfo();
}



