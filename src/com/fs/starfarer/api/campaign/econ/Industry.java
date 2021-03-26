package com.fs.starfarer.api.campaign.econ;

import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI.MarketInteractionMode;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;

public interface Industry {
	
	public static enum ImprovementDescriptionMode {
		MENU_BUTTON,
		STORY_POINT_USE_DIALOG,
		INDUSTRY_TOOLTIP,
	}
	
	public static enum AICoreDescriptionMode {
		INDUSTRY_TOOLTIP,
		MANAGE_CORE_TOOLTIP,
		MANAGE_CORE_DIALOG_LIST,
		MANAGE_CORE_DIALOG_INSTALLED,
	}
	
	public static enum IndustryTooltipMode {
		NORMAL,
		ADD_INDUSTRY,
		UPGRADE,
		DOWNGRADE,
		QUEUED,
	}
	
	
	/**
	 * Used when loading market from an economy .json file. Params are the special items/AI cores/etc
	 * that this industry has installed; this method should sort out what they all are based on IDs and
	 * install as appropriate.
	 * @param params
	 */
	void initWithParams(List<String> params);
	
	public MarketAPI getMarket();
	
	void apply();
	void unapply();
	
	/**
	 * Calls unapply() and then reapply().
	 */
	void reapply();

	void advance(float amount);

	List<MutableCommodityQuantity> getAllSupply();
	List<MutableCommodityQuantity> getAllDemand();
	MutableCommodityQuantity getSupply(String id);
	MutableCommodityQuantity getDemand(String id);
	
	MutableStat getIncome();
	MutableStat getUpkeep();

	void init(String id, MarketAPI market);

	String getId();
	IndustrySpecAPI getSpec();

	Pair<String, Integer> getMaxDeficit(String ... commodityIds);
	List<Pair<String, Integer>> getAllDeficit(String ... commodityIds);
	List<Pair<String, Integer>> getAllDeficit();

	void doPreSaveCleanup();
	void doPostSaveRestore();

	String getCurrentImage();
	String getCurrentName();

	/**
	 * Building OR upgrading.
	 * @return
	 */
	boolean isBuilding();
	
	/**
	 * Upgrading, but not the initial building process.
	 * @return
	 */
	boolean isUpgrading();
	void startBuilding();
	void finishBuildingOrUpgrading();
	void startUpgrading();
	float getBuildOrUpgradeProgress();

	float getBuildTime();
	float getBuildCost();
	float getBaseUpkeep();
	//float getActualUpkeep();

	boolean isAvailableToBuild();
	boolean showWhenUnavailable();
	String getUnavailableReason();

	String getBuildOrUpgradeProgressText();

	/**
	 * Building and not upgrading.
	 * @return
	 */
	boolean isFunctional();

	boolean isTooltipExpandable();
	float getTooltipWidth();
	void createTooltip(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded);

	void updateIncomeAndUpkeep();

	String getAICoreId();
	void setAICoreId(String aiCoreId);

	void supply(String modId, String commodityId, int quantity, String desc);

	void downgrade();

	void cancelUpgrade();

	
	boolean showShutDown();
	boolean canShutDown();
	String getCanNotShutDownReason();
	
	boolean canUpgrade();
	boolean canDowngrade();

	void addAICoreSection(TooltipMakerAPI tooltip, AICoreDescriptionMode mode);
	void addAICoreSection(TooltipMakerAPI tooltip, String coreId, AICoreDescriptionMode mode);

	boolean isSupplyLegal(CommodityOnMarketAPI com);
	boolean isDemandLegal(CommodityOnMarketAPI com);

	MutableStat getDemandReduction();
	MutableStat getSupplyBonus();
	
	List<SpecialItemData> getVisibleInstalledItems();
	
	List<InstallableIndustryItemPlugin> getInstallableItems();

	void notifyBeingRemoved(MarketInteractionMode mode, boolean forUpgrade);
	
	boolean isHidden();

	void setDisrupted(float days);
	void setDisrupted(float days, boolean useMax);
	float getDisruptedDays();
	boolean isDisrupted();
	boolean canBeDisrupted();
	
	float getPatherInterest();

	String getCargoTitleForGatheringPoint();
	CargoAPI generateCargoForGatheringPoint(Random random);

	SpecialItemData getSpecialItem();
	void setSpecialItem(SpecialItemData special);

	String getNameForModifier();
	
	
	/**
	 * Return false if already using one of that type, unless the other one is better.
	 * @param data
	 * @return
	 */
	boolean wantsToUseSpecialItem(SpecialItemData data);

	boolean isIndustry();
	boolean isStructure();
	boolean isOther();

	String getBuildOrUpgradeDaysText();

	void notifyColonyRenamed();
	
	boolean canImprove();
	boolean isImproved();
	void setImproved(boolean improved);
	String getImproveMenuText();
	void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode);
	int getImproveStoryPoints();
	float getImproveBonusXP();
	String getImproveSoundId();
	String getImproveDialogTitle();

	
	RaidDangerLevel adjustCommodityDangerLevel(String commodityId, RaidDangerLevel level);
	/**
	 * Includes nonecon "commodities" such as AI cores. Rule of thumb: if it requires a set number of
	 * marine tokens to raid, then this method determines the danger level. Otherwise, it's getCommodityDangerLevel().
	 * @param itemId
	 * @return
	 */
	RaidDangerLevel adjustItemDangerLevel(String itemId, String data, RaidDangerLevel level);
	int adjustMarineTokensToRaidItem(String itemId, String data, int marineTokens);

	boolean canInstallAICores();

	MutableStat getDemandReductionFromOther();
	MutableStat getSupplyBonusFromOther();

	void setHidden(boolean hidden);
}







