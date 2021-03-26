package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin.InstallableItemDescriptionMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.MarketInteractionMode;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.campaign.impl.items.GenericSpecialItemPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.econ.impl.ConstructionQueue.ConstructionQueueItem;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.IconRenderMode;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.StatModValueGetter;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

public abstract class BaseIndustry implements Industry, Cloneable {
	
	public static int SIZE_FOR_SMALL_IMAGE = 3;
	public static int SIZE_FOR_LARGE_IMAGE = 6;
	
	public static float UPKEEP_MULT = 0.75f;
	public static int DEMAND_REDUCTION = 1;
	public static int SUPPLY_BONUS = 1;
	
	public static int DEFAULT_IMPROVE_SUPPLY_BONUS = 1;
	
	@Override
	protected BaseIndustry clone() {
		BaseIndustry copy = null;
		try {
			copy = (BaseIndustry) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}

	//public static final float ORBITAL_HAZARD = 1f;
	
	public static final String BASE_VALUE_TEXT = "Base value for colony size";
	
	public static String getDeficitText(String commodityId) {
		if (commodityId == null) {
			return "Various shortages";
		}
		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(commodityId);
		return Misc.ucFirst(spec.getName().toLowerCase() + " shortage");
	}
	
	// want to have some ability to add random supply/demand to industries
	// e.g. market condition adding Volturnian Lobster supply to Volturn's Farming/Aquaculture
	protected Map<String, MutableCommodityQuantity> supply = new LinkedHashMap<String, MutableCommodityQuantity>(); 
	protected Map<String, MutableCommodityQuantity> demand = new LinkedHashMap<String, MutableCommodityQuantity>(); 
	
	protected MutableStat income = new MutableStat(0f);
	protected MutableStat upkeep = new MutableStat(0f);
	protected MarketAPI market;
	
	protected String id;
	
	protected float buildProgress = 0f;
	protected float buildTime = 1f;
	protected boolean building = false;
	protected Boolean improved = null;
	protected String upgradeId = null;
	
	protected transient IndustrySpecAPI spec = null;
	
	protected String aiCoreId = null;
//	protected int demandReduction = 0;
//	protected int supplyBonus = 0;
	
	protected MutableStat demandReduction = new MutableStat(0);
	protected MutableStat supplyBonus = new MutableStat(0);
	
	protected transient MutableStat demandReductionFromOther = new MutableStat(0);
	protected transient MutableStat supplyBonusFromOther = new MutableStat(0);
	
	
	public BaseIndustry() {
		//setAICoreId(Commodities.BETA_CORE);
	}
	
	public MutableStat getDemandReduction() {
		return demandReduction;
	}

	public MutableStat getSupplyBonus() {
		return supplyBonus;
	}
	
	public MutableStat getDemandReductionFromOther() {
		if (demandReductionFromOther == null) {
			demandReductionFromOther = new MutableStat(0); 
		}
		return demandReductionFromOther;
	}

	public MutableStat getSupplyBonusFromOther() {
		if (supplyBonusFromOther == null) {
			supplyBonusFromOther = new MutableStat(0);
		}
		return supplyBonusFromOther;
	}

	public void init(String id, MarketAPI market) {
		this.id = id;
		this.market = market;
		readResolve();
	}
	
	private transient String modId;
	private transient String [] modIds;
	protected Object readResolve() {
		spec = Global.getSettings().getIndustrySpec(id);
		
		if (buildTime < 1f) buildTime = 1f;
		
		modId = "ind_" + id;
		modIds = new String[10];
		for (int i = 0; i < modIds.length; i++) {
			modIds[i] = modId + "_" + i;
		}
		
		if (demandReduction == null) demandReduction = new MutableStat(0);
		if (supplyBonus == null) supplyBonus = new MutableStat(0);

		if (supply != null) {
			for (String id : new ArrayList<String>(supply.keySet())) {
				MutableCommodityQuantity stat = supply.get(id);
				stat.getQuantity().unmodifyFlat("ind_sb");
			}
		}
		if (demand != null) {
			for (String id : new ArrayList<String>(demand.keySet())) {
				MutableCommodityQuantity stat = demand.get(id);
				stat.getQuantity().unmodifyFlat("ind_dr");
			}
		}
		
		return this;
	}
	
	protected Object writeReplace() {
//		BaseIndustry copy = clone();
//		copy.supply = null;
//		copy.demand = null;
//		return copy;
		clearUnmodified();
		return this;
	}
	
	
	public void apply(boolean withIncomeUpdate) {
		updateSupplyAndDemandModifiers();
		
		if (withIncomeUpdate) {
			updateIncomeAndUpkeep();
		}
		
		applyAICoreModifiers();
		applyImproveModifiers();
		
		if (this instanceof MarketImmigrationModifier) {
			market.addTransientImmigrationModifier((MarketImmigrationModifier) this);
		}
		
		if (special != null) {
			InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(special.getId());
			if (effect != null) {
				List<String> unmet = effect.getUnmetRequirements(this);
				if (unmet == null || unmet.isEmpty()) {
					effect.apply(this);
				} else {
					effect.unapply(this);
				}
			}
		}
	}
	
	public void unapply() {
		applyNoAICoreModifiers();
		
		if (this instanceof MarketImmigrationModifier) {
			market.removeTransientImmigrationModifier((MarketImmigrationModifier) this);
		}
		
		if (special != null) {
			InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(special.getId());
			if (effect != null) {
				effect.unapply(this);
			}
		}
	}
	
	protected void applyAICoreModifiers() {
		if (aiCoreId == null) {
			applyNoAICoreModifiers();
			return;
		}
		boolean alpha = aiCoreId.equals(Commodities.ALPHA_CORE); 
		boolean beta = aiCoreId.equals(Commodities.BETA_CORE); 
		boolean gamma = aiCoreId.equals(Commodities.GAMMA_CORE);
		if (alpha) applyAlphaCoreModifiers();
		else if (beta) applyBetaCoreModifiers();
		else if (gamma) applyGammaCoreModifiers();
	}
	
	protected void applyAlphaCoreModifiers() {}
	protected void applyBetaCoreModifiers() {}
	protected void applyGammaCoreModifiers() {}
	protected void applyNoAICoreModifiers() {}
	
	
	
	protected String getModId() {
		//return "ind_" + id;
		return modId;
	}
	
	protected String getModId(int index) {
		//return "ind_" + id + "_" + index;
		return modIds[index];
	}
	
	public void demand(String commodityId, int quantity) {
		demand(0, commodityId, quantity, BASE_VALUE_TEXT);
	}
	
	public void demand(String commodityId, int quantity, String desc) {
		demand(0, commodityId, quantity, desc);
	}
	public void demand(int index, String commodityId, int quantity, String desc) {
		demand(getModId(index), commodityId, quantity, desc);
	}
	public void demand(String modId, String commodityId, int quantity, String desc) {
//		if (commodityId != null && commodityId.equals("organics") && getId().contains("military")) {
//			System.out.println("wefwefwe");
//		}
		//quantity -= demandReduction;
		// want to apply negative numbers here so they add up with anything coming in from market conditions
		if (quantity == 0) {
			getDemand(commodityId).getQuantity().unmodifyFlat(modId);
		} else {
			getDemand(commodityId).getQuantity().modifyFlat(modId, quantity, desc);
		}
		
		if (quantity > 0) {
			if (!demandReduction.isUnmodified()) {
				getDemand(commodityId).getQuantity().modifyFlat("ind_dr", -demandReduction.getModifiedInt());
			} else {
				getDemand(commodityId).getQuantity().unmodifyFlat("ind_dr");
			}
		}
	}
	
	public void supply(String commodityId, int quantity) {
		supply(0, commodityId, quantity, BASE_VALUE_TEXT);
	}
	
	public void supply(String commodityId, int quantity, String desc) {
		supply(0, commodityId, quantity, desc);
	}
	
	public void supply(int index, String commodityId, int quantity, String desc) {
		supply(getModId(index), commodityId, quantity, desc);
	}
	public void supply(String modId, String commodityId, int quantity, String desc) {
//		if (this instanceof Mining && market.getName().equals("Medea") &&
//				Commodities.VOLATILES.equals(commodityId)) {
//			System.out.println("efwefwe");
//		}
		
		//quantity += supplyBonus; doesn't work gets applied multiple times potentially
		// want to apply negative numbers here so they add up with anything coming in from market conditions
		if (quantity == 0) {
			getSupply(commodityId).getQuantity().unmodifyFlat(modId);
		} else {
			getSupply(commodityId).getQuantity().modifyFlat(modId, quantity, desc);
		}
		
		if (quantity > 0) {
		//if (!getSupply(commodityId).getQuantity().isUnmodified()) {
			if (!supplyBonus.isUnmodified()) {
				getSupply(commodityId).getQuantity().modifyFlat("ind_sb", supplyBonus.getModifiedInt());
			} else {
				getSupply(commodityId).getQuantity().unmodifyFlat("ind_sb");
			}
		}
		//getSupply(commodityId).getQuantity().unmodifyFlat("ind_sb");
	}
	
	protected void applyDeficitToProduction(int index, Pair<String, Integer> deficit, String ... commodities) {
		for (String commodity : commodities) {
//			if (this instanceof Mining && market.getName().equals("Louise")) {
//				System.out.println("efwefwe");
//			}
			if (getSupply(commodity).getQuantity().isUnmodified()) continue;
			supply(index, commodity, -deficit.two, getDeficitText(deficit.one));
		}
	}
	
//	public static float getIncomeStabilityMult(float stability) {
//		if (stability <= 5) {
//			return Math.max(0, stability / 5f);
//		}
//		return 1f + (stability - 5f) * .1f;
//	}
	
	public void updateIncomeAndUpkeep() {
		applyIncomeAndUpkeep(-1);
	}
	
	protected void applyIncomeAndUpkeep(float sizeOverride) {
		float size = market.getSize();
		if (sizeOverride >= 0) size = sizeOverride;
		float sizeMult = getSizeMult(size);
		sizeMult = Math.max(1, sizeMult - 2);
		
		float stabilityMult = market.getIncomeMult().getModifiedValue();
		float upkeepMult = market.getUpkeepMult().getModifiedValue();
//		if (hazardMultOverride >= 0) {
//			upkeepMult = hazardMultOverride;
//		}
		
		
		int income = (int) (getSpec().getIncome() * sizeMult);
		if (income != 0) {
			getIncome().modifyFlatAlways("ind_base", income, "Base value");
			getIncome().modifyMultAlways("ind_stability", stabilityMult, "Market income multiplier");
		} else {
			getIncome().unmodifyFlat("ind_base");
			getIncome().unmodifyMult("ind_stability");
		}
		
		
		int upkeep = (int) (getSpec().getUpkeep() * sizeMult);
		if (upkeep != 0) {
			getUpkeep().modifyFlatAlways("ind_base", upkeep, "Base value");
			getUpkeep().modifyMultAlways("ind_hazard", upkeepMult, "Market upkeep multiplier");
		} else {
			getUpkeep().unmodifyFlat("ind_base");
			getUpkeep().unmodifyMult("ind_hazard");
		}
		
		applyAICoreToIncomeAndUpkeep();
		
		if (!isFunctional()) {
			getIncome().unmodifyFlat("ind_base");
			getIncome().unmodifyMult("ind_stability");
		}
	}
	
//	public static float getUpkeepMult(MarketAPI market) {
//		return getUpkeepHazardMult(market.getHazardValue());
//	}
//	public static float getUpkeepHazardMult(float hazard) {
//		float hazardMult = hazard;
//		if (hazardMult < 0) hazardMult = 0;
//		return hazardMult;
//	}
	
	public float getBuildTime() {
		return getSpec().getBuildTime();
	}
	
	protected Float buildCostOverride = null;
	public Float getBuildCostOverride() {
		return buildCostOverride;
	}
	public void setBuildCostOverride(float buildCostOverride) {
		this.buildCostOverride = buildCostOverride;
	}
	public float getBuildCost() {
		if (buildCostOverride != null) return buildCostOverride; 
		return getSpec().getCost();
	}
	
	public float getBaseUpkeep() {
		float size = market.getSize();
		float sizeMult = getSizeMult(size);
		sizeMult = Math.max(1, sizeMult - 2);
		return getSpec().getUpkeep() * sizeMult;
	}
	
//	public float getActualUpkeep() {
//		return getBaseUpkeep() * market.getUpkeepMult().getModifiedValue();
//	}
	
	protected boolean wasDisrupted = false;
	public void advance(float amount) {
		boolean disrupted = isDisrupted();
		if (!disrupted && wasDisrupted) {
			disruptionFinished();
		}
		wasDisrupted = disrupted;
		
//		if (disrupted) {
//			//if (DebugFlags.COLONY_DEBUG) {
//				String key = getDisruptedKey();
//				market.getMemoryWithoutUpdate().unset(key);	
//			//}
//		}
		
		if (building && !disrupted) {
			float days = Global.getSector().getClock().convertToDays(amount);
			//DebugFlags.COLONY_DEBUG = true;
			if (DebugFlags.COLONY_DEBUG) {
				days *= 100f;
			}
			buildProgress += days;
			
			if (buildProgress >= buildTime) {
				finishBuildingOrUpgrading();
			}
		}
		
	}
	
	protected void notifyDisrupted() {
		
	}
	
	protected void disruptionFinished() {
		
	}
	
	public boolean isBuilding() {
		return building;
	}
	
	public boolean isFunctional() {
		if (isDisrupted()) return false;
		return !(isBuilding() && !isUpgrading());
	}
	
	public boolean isUpgrading() {
		return building && upgradeId != null;
	}
	
	public float getBuildOrUpgradeProgress() {
		if (isDisrupted()) {
			return 0f;
		}
		if (!isBuilding()) return 0f;
		
		return Math.min(1f, buildProgress / buildTime);
	}
	
	public String getBuildOrUpgradeDaysText() {
		if (isDisrupted()) {
			int left = (int) getDisruptedDays();
			if (left < 1) left = 1;
			String days = "days";
			if (left == 1) days = "day";
			
			return "" + left + " " + days + "";
		}
		
		int left = (int) (buildTime - buildProgress);
		if (left < 1) left = 1;
		String days = "days";
		if (left == 1) days = "day";
		
		return left + " " + days;
	}
	
	public String getBuildOrUpgradeProgressText() {
		if (isDisrupted()) {
			int left = (int) getDisruptedDays();
			if (left < 1) left = 1;
			String days = "days";
			if (left == 1) days = "day";
			
			return "Disrupted: " + left + " " + days + " left";
		}
		
		int left = (int) (buildTime - buildProgress);
		if (left < 1) left = 1;
		String days = "days";
		if (left == 1) days = "day";
		
//		if (isBuilding() && !isUpgrading()) {
//			//return left + " " + days;
//			return "building: " + (int)Math.round(buildProgress / buildTime * 100f) + "%";
//		}
		
		if (isUpgrading()) {
			return "Upgrading: " + left + " " + days + " left";
		} else {
			return "Building: " + left + " " + days + " left";
		}
	}
	
	public void startBuilding() {
		building = true;
		buildProgress = 0;
		upgradeId = null;
		
		buildTime = spec.getBuildTime();
		unapply();
	}
	
	public void finishBuildingOrUpgrading() {
		building = false;
		buildProgress = 0;
		buildTime = 1f;
		if (upgradeId != null) {
			market.removeIndustry(getId(), null, true);
			market.addIndustry(upgradeId);
			BaseIndustry industry = (BaseIndustry) market.getIndustry(upgradeId);
			industry.setAICoreId(getAICoreId());
			industry.setImproved(isImproved());
			industry.upgradeFinished(this);
			industry.reapply();
		} else {
			buildingFinished();
			reapply();
		}
	}
	
	public void startUpgrading() {
		building = true;
		buildProgress = 0;
		upgradeId = getSpec().getUpgrade();
		
		
		IndustrySpecAPI upgrade = Global.getSettings().getIndustrySpec(upgradeId);
		buildTime = upgrade.getBuildTime();
	}
	
	public void cancelUpgrade() {
		building = false;
		buildProgress = 0;
		upgradeId = null;
	}
	
	public void downgrade() {
		building = true;
		buildProgress = 0;
		upgradeId = getSpec().getDowngrade();
		finishBuildingOrUpgrading();
	}
	
	
	public void reapply() {
		unapply();
		apply();
	}
	
	protected void buildingFinished() {
		sendBuildOrUpgradeMessage();
		buildNextInQueue(market);
	}
	
	public static void buildNextInQueue(MarketAPI market) {
		ConstructionQueueItem next = null;
		Iterator<ConstructionQueueItem> iter = market.getConstructionQueue().getItems().iterator();
		while (iter.hasNext()) {
			next = iter.next();
			iter.remove();
			
			Industry ind = market.instantiateIndustry(next.id);
			int num = Misc.getNumIndustries(market);
			int max = Misc.getMaxIndustries(market);
			if (ind.isAvailableToBuild() && (num <= max || !ind.isIndustry())) { // <= because num includes what's queued
				break;
			} else {
				if (market.isPlayerOwned()) {
					MessageIntel intel = new MessageIntel(ind.getCurrentName() + " at " + market.getName(), Misc.getBasePlayerColor());
					intel.addLine(BaseIntelPlugin.BULLET + "Construction aborted");
					
					int refund = next.cost;
					Global.getSector().getPlayerFleet().getCargo().getCredits().add(refund);
					intel.addLine(BaseIntelPlugin.BULLET + "%s refunded", 
							Misc.getTextColor(),
							new String [] {Misc.getDGSCredits(refund)}, Misc.getHighlightColor());
					intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
					intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
					Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.COLONY_INFO, market);
				}
				next = null;
			}
		}

		if (next != null) {
			market.addIndustry(next.id);
			Industry ind = market.getIndustry(next.id);
			ind.startBuilding();
			if (ind instanceof BaseIndustry) {
				((BaseIndustry)ind).setBuildCostOverride(next.cost);
			}
			
			if (market.isPlayerOwned()) {
				MessageIntel intel = new MessageIntel(ind.getCurrentName() + " at " + market.getName(), Misc.getBasePlayerColor());
				intel.addLine(BaseIntelPlugin.BULLET + "Construction started");
				intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
				intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
				Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.COLONY_INFO, market);
			}
		}
	}
	
	protected void upgradeFinished(Industry previous) {
		sendBuildOrUpgradeMessage();
		
		setSpecialItem(previous.getSpecialItem());
	}
	
	protected void sendBuildOrUpgradeMessage() {
		if (market.isPlayerOwned()) {
			MessageIntel intel = new MessageIntel(getCurrentName() + " at " + market.getName(), Misc.getBasePlayerColor());
			intel.addLine(BaseIntelPlugin.BULLET + "Construction completed");
			intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
			intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
			Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.COLONY_INFO, market);
		}
	}
	
	public void notifyBeingRemoved(MarketInteractionMode mode, boolean forUpgrade) {
		if (aiCoreId != null && !forUpgrade) {
			CargoAPI cargo = getCargoForInteractionMode(mode);
			if (cargo != null) {
				cargo.addCommodity(aiCoreId, 1);
			}
		}
		
		if (special != null && !forUpgrade) {
			CargoAPI cargo = getCargoForInteractionMode(mode);
			if (cargo != null) {
				cargo.addSpecial(special, 1);
			}
		}
	}
	
	protected CargoAPI getCargoForInteractionMode(MarketInteractionMode mode) {
		CargoAPI cargo = null;
		if (mode == null) return null;
		
		if (mode == MarketInteractionMode.REMOTE) {
			cargo = Misc.getStorageCargo(market);
		} else {
			cargo = Global.getSector().getPlayerFleet().getCargo();
		}
		return cargo;
	}
	
	public String getId() {
		return id;
	}
	
	public IndustrySpecAPI getSpec() {
		if (spec == null) spec = Global.getSettings().getIndustrySpec(id);
		return spec;
	}

	public void clearUnmodified() {
		if (supply != null) {
			for (String id : new ArrayList<String>(supply.keySet())) {
				MutableCommodityQuantity stat = supply.get(id);
				if (stat != null && (stat.getQuantity().isUnmodified() || stat.getQuantity().getModifiedValue() <= 0)) {
					supply.remove(id);
				}
			}
		}
		if (demand != null) {
			for (String id : new ArrayList<String>(demand.keySet())) {
				MutableCommodityQuantity stat = demand.get(id);
				if (stat != null && (stat.getQuantity().isUnmodified() || stat.getQuantity().getModifiedValue() <= 0)) {
					demand.remove(id);
				}
			}
		}
	}

	public List<MutableCommodityQuantity> getAllDemand() {
		List<MutableCommodityQuantity> result = new ArrayList<MutableCommodityQuantity>();
		for (MutableCommodityQuantity q : demand.values()) {
			if (q.getQuantity().getModifiedValue() > 0) {
				result.add(q);
			}
		}
		return result;
	}
	
	public List<MutableCommodityQuantity> getAllSupply() {
		List<MutableCommodityQuantity> result = new ArrayList<MutableCommodityQuantity>();
		for (MutableCommodityQuantity q : supply.values()) {
			if (q.getQuantity().getModifiedValue() > 0) {
				result.add(q);
			}
		}
		return result;
	}
	
	public MutableCommodityQuantity getSupply(String id) {
		MutableCommodityQuantity stat = supply.get(id);
		if (stat == null) {
			stat = new MutableCommodityQuantity(id);
			supply.put(id, stat);
		}
		return stat;
	}
	
	public MutableCommodityQuantity getDemand(String id) {
		MutableCommodityQuantity stat = demand.get(id);
		if (stat == null) {
			stat = new MutableCommodityQuantity(id);
			demand.put(id, stat);
		}
		return stat;
	}

	public MutableStat getIncome() {
		return income;
	}

	public MutableStat getUpkeep() {
		return upkeep;
	}

	public MarketAPI getMarket() {
		return market;
	}
	
//	public String getMaxDeficitCommodity(String ... commodityIds) {
//		int max = 0;
//		String result = null;
//		for (String id : commodityIds) {
//			int demand = (int) getDemand(id).getQuantity().getModifiedValue();
//			CommodityOnMarketAPI com = market.getCommodityData(id);
//			int available = com.getAvailable();
//			
//			int deficit = Math.max(demand - available, 0);
//			if (deficit > max) {
//				max = deficit;
//				result = id;
//			}
//		}
//		return result;
//	}
//	
//	public int getMaxDeficit(String ... commodityIds) {
//		int max = 0;
//		for (String id : commodityIds) {
//			int demand = (int) getDemand(id).getQuantity().getModifiedValue();
//			CommodityOnMarketAPI com = market.getCommodityData(id);
//			int available = com.getAvailable();
//			
//			int deficit = Math.max(demand - available, 0);
//			if (deficit > max) max = deficit;
//		}
//		return max;
//	}
	
	public Pair<String, Integer> getMaxDeficit(String ... commodityIds) {
		Pair<String, Integer> result = new Pair<String, Integer>();
		result.two = 0;
		for (String id : commodityIds) {
			int demand = (int) getDemand(id).getQuantity().getModifiedValue();
			CommodityOnMarketAPI com = market.getCommodityData(id);
			int available = com.getAvailable();
			
			int deficit = Math.max(demand - available, 0);
			if (deficit > result.two) {
				result.one = id;
				result.two = deficit;
			}
		}
		return result;
	}
	
	public List<Pair<String, Integer>> getAllDeficit() {
		List<String> commodities = new ArrayList<String>();
		for (MutableCommodityQuantity curr : demand.values()) {
			commodities.add(curr.getCommodityId());
		}
		return getAllDeficit(commodities.toArray(new String[0]));
	}

	public List<Pair<String, Integer>> getAllDeficit(String ... commodityIds) {
		List<Pair<String, Integer>> result = new ArrayList<Pair<String,Integer>>();
		for (String id : commodityIds) {
			int demand = (int) getDemand(id).getQuantity().getModifiedValue();
			CommodityOnMarketAPI com = market.getCommodityData(id);
			int available = com.getAvailable();
			
			int deficit = Math.max(demand - available, 0);
			if (deficit > 0) {
				Pair<String, Integer> curr = new Pair<String, Integer>();
				curr.one = id;
				curr.two = deficit;
				result.add(curr);
			}
		}
		return result;
	}
	
	public float getSizeMult() {
		return getSizeMult(market.getSize());
	}
	public static float getCommodityEconUnitMult(float size) {
		if (size <= 0) return 0f;
//		if (size == 1) return 0.2f;
//		if (size == 2) return 0.3f;
//		if (size == 3) return 0.5f;
		return 1f;
	}
	public static float getSizeMult(float size) {
		if (size <= 0) return 0f;
//		if (size == 1) return 0.2f;
//		if (size == 2) return 0.5f;
//		return size - 2f;
		return size;
		
//		if (size <= 1) return 0.2f;
//		if (size == 2) return 0.3f;
//		if (size == 3) return 0.5f;
//		return size - 3f;
		
//		if (size == 4) return 2f;
//		if (size == 5) return 4f;
//		if (size == 6) return 8f;
//		if (size == 7) return 16f;
//		if (size == 8) return 32f;
//		if (size == 9) return 64f;
//		if (size == 10) return 128f;
//		
//		return (float) Math.pow(2, size - 3);
	}


	public void doPreSaveCleanup() {
		supply = null;
		demand = null;
		income = null;
		upkeep = null;
	}
	
	public void doPostSaveRestore() {
		supply = new LinkedHashMap<String, MutableCommodityQuantity>(); 
		demand = new LinkedHashMap<String, MutableCommodityQuantity>(); 
		
		income = new MutableStat(0f);
		upkeep = new MutableStat(0f);
	}


	public String getCurrentImage() {
		return getSpec().getImageName();
	}

	public String getCurrentName() {
		return getSpec().getName();
	}
	
	public boolean isAvailableToBuild() {
		if (market.hasTag(Tags.MARKET_NO_INDUSTRIES_ALLOWED)) return false; 
		return market.hasIndustry(Industries.POPULATION) && !getId().equals(Industries.POPULATION);
	}
	
	public boolean showWhenUnavailable() {
		if (market.hasTag(Tags.MARKET_NO_INDUSTRIES_ALLOWED)) return false;
		return true;
	}
	
	public String getUnavailableReason() {
		return "Can not be built";
	}
	
	public boolean isTooltipExpandable() {
		return false;
	}
	
	public float getTooltipWidth() {
		return 400f;
	}
	
	protected transient IndustryTooltipMode currTooltipMode = null;
	public void createTooltip(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
		currTooltipMode = mode;
		
		float pad = 3f;
		float opad = 10f;

		FactionAPI faction = market.getFaction();
		Color color = faction.getBaseUIColor();
		Color dark = faction.getDarkUIColor();
		Color grid = faction.getGridUIColor();
		Color bright = faction.getBrightUIColor();
		
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		
		MarketAPI copy = market.clone();
		MarketAPI orig = market;
		
		//int numBeforeAdd = Misc.getNumIndustries(market);
		
		market = copy;
		boolean needToAddIndustry = !market.hasIndustry(getId());
		//addDialogMode = true;
		if (needToAddIndustry) market.getIndustries().add(this);
		
		if (mode != IndustryTooltipMode.NORMAL) {
			market.clearCommodities();
			for (CommodityOnMarketAPI curr : market.getAllCommodities()) {
				curr.getAvailableStat().setBaseValue(100);
			}
		}
		
//		if (addDialogMode) {
//			market.reapplyConditions();
//			apply();
//		}
		market.reapplyConditions();
		reapply();
		
		String type = "";
		if (isIndustry()) type = " - Industry";
		if (isStructure()) type = " - Structure";
		
		tooltip.addTitle(getCurrentName() + type, color);
		
		String desc = spec.getDesc();
		String override = getDescriptionOverride();
		if (override != null) {
			desc = override;
		}
		desc = Global.getSector().getRules().performTokenReplacement(null, desc, market.getPrimaryEntity(), null);
		
		tooltip.addPara(desc, opad);
		
//		Industry inProgress = Misc.getCurrentlyBeingConstructed(market);
//		if ((mode == IndustryTooltipMode.ADD_INDUSTRY && inProgress != null) || 
//				(mode == IndustryTooltipMode.UPGRADE && inProgress != null)) {
//			//tooltip.addPara("Another project (" + inProgress.getCurrentName() + ") in progress", bad, opad);
//			//tooltip.addPara("Already building: " + inProgress.getCurrentName() + "", bad, opad);
//			tooltip.addPara("Another construction in progress: " + inProgress.getCurrentName() + "", bad, opad);
//		}
		
		//tooltip.addPara("Type: %s", opad, gray, highlight, type);
		if (isIndustry() && (mode == IndustryTooltipMode.ADD_INDUSTRY || 
				mode == IndustryTooltipMode.UPGRADE ||
				mode == IndustryTooltipMode.DOWNGRADE)
				) {
			
			int num = Misc.getNumIndustries(market);
			int max = Misc.getMaxIndustries(market);
			
			
			// during the creation of the tooltip, the market has both the current industry 
			// and the upgrade/downgrade. So if this upgrade/downgrade counts as an industry, it'd count double if
			// the current one is also an industry. Thus reduce num by 1 if that's the case.
			if (isIndustry()) {
				if (mode == IndustryTooltipMode.UPGRADE) {
					for (Industry curr : market.getIndustries()) {
						if (getSpec().getId().equals(curr.getSpec().getUpgrade())) {
							if (curr.isIndustry()) {
								num--;
							}
							break;
						}
					}
				} else if (mode == IndustryTooltipMode.DOWNGRADE) {
					for (Industry curr : market.getIndustries()) {
						if (getSpec().getId().equals(curr.getSpec().getDowngrade())) {
							if (curr.isIndustry()) {
								num--;
							}
							break;
						}
					}
				}
			}
			
			Color c = gray;
			c = Misc.getTextColor();
			Color h1 = highlight;
			Color h2 = highlight;
			if (num > max) {// || (num >= max && mode == IndustryTooltipMode.ADD_INDUSTRY)) {
				//c = bad;
				h1 = bad;
				num--;
				
				tooltip.addPara("Maximum number of industries reached", bad, opad);
			}
			//tooltip.addPara("Maximum of %s industries on a colony of this size. Currently: %s.",
//			LabelAPI label = tooltip.addPara("Maximum industries for a colony of this size: %s. Industries: %s. ",
//					opad, c, h1, "" + max, "" + num);
//			label.setHighlightColors(h2, h1);
		}
		
		
		
		addRightAfterDescriptionSection(tooltip, mode);
		
		if (isDisrupted()) {
			int left = (int) getDisruptedDays();
			if (left < 1) left = 1;
			String days = "days";
			if (left == 1) days = "day";
			
			tooltip.addPara("Operations disrupted! %s " + days + " until return to normal function.",
					opad, Misc.getNegativeHighlightColor(), highlight, "" + left);
		}
		
		if (DebugFlags.COLONY_DEBUG || market.isPlayerOwned()) {
			if (mode == IndustryTooltipMode.NORMAL) {
				if (getSpec().getUpgrade() != null && !isBuilding()) {
					tooltip.addPara("Click to manage or upgrade", Misc.getPositiveHighlightColor(), opad);
				} else {
					tooltip.addPara("Click to manage", Misc.getPositiveHighlightColor(), opad);
				}
				//tooltip.addPara("Click to manage", market.getFaction().getBrightUIColor(), opad);
			}
		}
		
		if (mode == IndustryTooltipMode.QUEUED) {
			tooltip.addPara("Click to remove or adjust position in queue", Misc.getPositiveHighlightColor(), opad);
			tooltip.addPara("Currently queued for construction. Does not have any impact on the colony.", opad);
			
			int left = (int) (getSpec().getBuildTime());
			if (left < 1) left = 1;
			String days = "days";
			if (left == 1) days = "day";
			tooltip.addPara("Requires %s " + days + " to build.", opad, highlight, "" + left);
			
			//return;
		} else if (!isFunctional() && mode == IndustryTooltipMode.NORMAL) {
			tooltip.addPara("Currently under construction and not producing anything or providing other benefits.", opad);
			
			int left = (int) (buildTime - buildProgress);
			if (left < 1) left = 1;
			String days = "days";
			if (left == 1) days = "day";
			tooltip.addPara("Requires %s more " + days + " to finish building.", opad, highlight, "" + left);
		}
		
		
		if (!isAvailableToBuild() && 
				(mode == IndustryTooltipMode.ADD_INDUSTRY ||
						mode == IndustryTooltipMode.UPGRADE ||
						mode == IndustryTooltipMode.DOWNGRADE)) {
			String reason = getUnavailableReason();
			if (reason != null) {
				tooltip.addPara(reason, bad, opad);
			}
		}
		
		boolean category = getSpec().hasTag(Industries.TAG_PARENT);
		
		if (!category) {
			int credits = (int) Global.getSector().getPlayerFleet().getCargo().getCredits().get();
			String creditsStr = Misc.getDGSCredits(credits);
			if (mode == IndustryTooltipMode.UPGRADE || mode == IndustryTooltipMode.ADD_INDUSTRY) {
				int cost = (int) getBuildCost();
				String costStr = Misc.getDGSCredits(cost);
				
				int days = (int) getBuildTime();
				String daysStr = "days";
				if (days == 1) daysStr = "day";
				
				LabelAPI label = null;
				if (mode == IndustryTooltipMode.UPGRADE) {
					label = tooltip.addPara("%s and %s " + daysStr + " to upgrade. You have %s.", opad, 
											highlight, costStr, "" + days, creditsStr);
				} else {
					label = tooltip.addPara("%s and %s " + daysStr + " to build. You have %s.", opad, 
											highlight, costStr, "" + days, creditsStr);
				}
				label.setHighlight(costStr, "" + days, creditsStr);
				if (credits >= cost) {
					label.setHighlightColors(highlight, highlight, highlight);
				} else {
					label.setHighlightColors(bad, highlight, highlight);
				}
			} else if (mode == IndustryTooltipMode.DOWNGRADE) {
				float refundFraction = Global.getSettings().getFloat("industryRefundFraction");
				int cost = (int) (getBuildCost() * refundFraction);
				String refundStr = Misc.getDGSCredits(cost);
				
				tooltip.addPara("%s refunded for downgrade.", opad, highlight, refundStr);
			}
			
			
			addPostDescriptionSection(tooltip, mode);
			
			if (!getIncome().isUnmodified()) {
				int income = getIncome().getModifiedInt();
				tooltip.addPara("Monthly income: %s", opad, highlight, Misc.getDGSCredits(income));
				tooltip.addStatModGrid(250, 65, 10, pad, getIncome(), true, new StatModValueGetter() {
					public String getPercentValue(StatMod mod) {return null;}
					public String getMultValue(StatMod mod) {return null;}
					public Color getModColor(StatMod mod) {return null;}
					public String getFlatValue(StatMod mod) {
						return Misc.getWithDGS(mod.value) + Strings.C;
					}
				});
			}
			
			if (!getUpkeep().isUnmodified()) {
				int upkeep = getUpkeep().getModifiedInt();
				tooltip.addPara("Monthly upkeep: %s", opad, highlight, Misc.getDGSCredits(upkeep));
				tooltip.addStatModGrid(250, 65, 10, pad, getUpkeep(), true, new StatModValueGetter() {
					public String getPercentValue(StatMod mod) {return null;}
					public String getMultValue(StatMod mod) {return null;}
					public Color getModColor(StatMod mod) {return null;}
					public String getFlatValue(StatMod mod) {
						return Misc.getWithDGS(mod.value) + Strings.C;
					}
				});
			}
			
			addPostUpkeepSection(tooltip, mode);
			
			boolean hasSupply = false;
			for (MutableCommodityQuantity curr : supply.values()) {
				int qty = curr.getQuantity().getModifiedInt();
				if (qty <= 0) continue;
				hasSupply = true;
				break;
			}
			boolean hasDemand = false;
			for (MutableCommodityQuantity curr : demand.values()) {
				int qty = curr.getQuantity().getModifiedInt();
				if (qty <= 0) continue;
				hasDemand = true;
				break;
			}
			
			float maxIconsPerRow = 10f;
			if (hasSupply) {
				tooltip.addSectionHeading("Production", color, dark, Alignment.MID, opad);
				tooltip.beginIconGroup();
				tooltip.setIconSpacingMedium();
				float icons = 0;
				for (MutableCommodityQuantity curr : supply.values()) {
					int qty = curr.getQuantity().getModifiedInt();
					//if (qty <= 0) continue;
					
					int normal = qty;
					if (normal > 0) {
						tooltip.addIcons(market.getCommodityData(curr.getCommodityId()), normal, IconRenderMode.NORMAL);
					}
					
					int plus = 0;
					int minus = 0;
					for (StatMod mod : curr.getQuantity().getFlatMods().values()) {
						if (mod.value > 0) {
							plus += (int) mod.value;
						} else if (mod.desc != null && mod.desc.contains("shortage")) {
							minus += (int) Math.abs(mod.value);
						}
					}
					minus = Math.min(minus, plus);
					if (minus > 0 && mode == IndustryTooltipMode.NORMAL) {
						tooltip.addIcons(market.getCommodityData(curr.getCommodityId()), minus, IconRenderMode.DIM_RED);
					}
					icons += normal + Math.max(0, minus);
				}
				int rows = (int) Math.ceil(icons / maxIconsPerRow);
				rows = 3;
				tooltip.addIconGroup(32, rows, opad);
				
				
			}
//			else if (!isFunctional() && mode == IndustryTooltipMode.NORMAL) {
//				tooltip.addPara("Currently under construction and not producing anything or providing other benefits.", opad);
//			}
			
			addPostSupplySection(tooltip, hasSupply, mode);
			
			if (hasDemand || hasPostDemandSection(hasDemand, mode)) {
				tooltip.addSectionHeading("Demand & effects", color, dark, Alignment.MID, opad);
			}
			if (hasDemand) {
				tooltip.beginIconGroup();
				tooltip.setIconSpacingMedium();
				float icons = 0;
				for (MutableCommodityQuantity curr : demand.values()) {
					int qty = curr.getQuantity().getModifiedInt();
					if (qty <= 0) continue;
					
					CommodityOnMarketAPI com = orig.getCommodityData(curr.getCommodityId());
					int available = com.getAvailable();
					
					int normal = Math.min(available, qty);
					int red = Math.max(0, qty - available);
					
					if (mode != IndustryTooltipMode.NORMAL) {
						normal = qty;
						red = 0;
					}
					if (normal > 0) {
						tooltip.addIcons(com, normal, IconRenderMode.NORMAL);
					}
					if (red > 0) {
						tooltip.addIcons(com, red, IconRenderMode.DIM_RED);
					}
					icons += normal + Math.max(0, red);
				}
				int rows = (int) Math.ceil(icons / maxIconsPerRow);
				rows = 3;
				rows = 1;
				tooltip.addIconGroup(32, rows, opad);
			}
			
			addPostDemandSection(tooltip, hasDemand, mode);
			
			if (!needToAddIndustry) {
				//addAICoreSection(tooltip, AICoreDescriptionMode.TOOLTIP);
				addInstalledItemsSection(mode, tooltip, expanded);
				addImprovedSection(mode, tooltip, expanded);
			}
			
			tooltip.addPara("*Shown production and demand values are already adjusted based on current market size and local conditions.", gray, opad);
		}
		
		if (needToAddIndustry) {
			unapply();
			market.getIndustries().remove(this);
		}
		market = orig;
		if (!needToAddIndustry) {
			reapply();
		}
	}
	
	public void addInstalledItemsSection(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
		float opad = 10f;

		FactionAPI faction = market.getFaction();
		Color color = faction.getBaseUIColor();
		Color dark = faction.getDarkUIColor();
		
		LabelAPI heading = tooltip.addSectionHeading("Items", color, dark, Alignment.MID, opad);

		boolean addedSomething = false;
		if (aiCoreId != null) {
			AICoreDescriptionMode aiCoreDescMode = AICoreDescriptionMode.INDUSTRY_TOOLTIP;
			addAICoreSection(tooltip, aiCoreId, aiCoreDescMode);
			addedSomething = true;
		}
		addedSomething |= addNonAICoreInstalledItems(mode, tooltip, expanded);
		
		if (!addedSomething) {
			heading.setText("No items installed");
			//tooltip.addPara("None.", opad);
		}
	}
	
	protected boolean addNonAICoreInstalledItems(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
		if (special == null) return false;
		
		float opad = 10f;
		SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(special.getId());
		
		TooltipMakerAPI text = tooltip.beginImageWithText(spec.getIconName(), 48);
		InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(special.getId());
		effect.addItemDescription(this, text, special, InstallableItemDescriptionMode.INDUSTRY_TOOLTIP);
		tooltip.addImageWithText(opad);
		
		return true;
	}
	
//	public List<SpecialItemData> getVisibleInstalledItems() {
//		return new ArrayList<SpecialItemData>();
//	}
	
	public List<SpecialItemData> getVisibleInstalledItems() {
		List<SpecialItemData> result = new ArrayList<SpecialItemData>();
		if (special != null) {
			result.add(special);
		}
		return result;
	}
	
	public boolean wantsToUseSpecialItem(SpecialItemData data) {
		if (special != null) return false;
		SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(data.getId());
//		String industry = spec.getParams();
//		//String industry = ItemEffectsRepo.ITEM_TO_INDUSTRY.get(data.getId());
//		return getId().equals(industry);
		String [] industries = spec.getParams().split(",");
		Set<String> all = new HashSet<String>();
		for (String ind: industries) all.add(ind.trim());
		return all.contains(getId());
	}
	
//	public boolean wantsToUseSpecialItem(SpecialItemData data) {
//		return false;
//	}
	
	
	public void addAICoreSection(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		addAICoreSection(tooltip, aiCoreId, mode);
	}
	
	public void addAICoreSection(TooltipMakerAPI tooltip, String coreId, AICoreDescriptionMode mode) {
		float opad = 10f;

		FactionAPI faction = market.getFaction();
		Color color = faction.getBaseUIColor();
		Color dark = faction.getDarkUIColor();
		
//		if (mode == AICoreDescriptionMode.TOOLTIP) {
//			tooltip.addSectionHeading("AI Core", color, dark, Alignment.MID, opad);
//		}
		
//		if (mode == AICoreDescriptionMode.TOOLTIP || mode == AICoreDescriptionMode.MANAGE_CORE_TOOLTIP) {
		if (mode == AICoreDescriptionMode.MANAGE_CORE_TOOLTIP) {
			if (coreId == null) {
				tooltip.addPara("No AI core currently assigned. Click to assign an AI core from your cargo.", opad);
				return;
			}
		}
		
		boolean alpha = coreId.equals(Commodities.ALPHA_CORE); 
		boolean beta = coreId.equals(Commodities.BETA_CORE); 
		boolean gamma = coreId.equals(Commodities.GAMMA_CORE);
		
		if (alpha) {
			addAlphaCoreDescription(tooltip, mode);
		} else if (beta) {
			addBetaCoreDescription(tooltip, mode);
		} else if (gamma) {
			addGammaCoreDescription(tooltip, mode);
		}
	}
	
	protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		String pre = "Alpha-level AI core currently assigned. ";
		if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			pre = "Alpha-level AI core. ";
		}
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP || mode == AICoreDescriptionMode.MANAGE_CORE_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
					"Increases production by %s unit.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
					"" + SUPPLY_BONUS);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
				"Increases production by %s unit.", opad, highlight,
				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
				"" + SUPPLY_BONUS);
	}
	
	protected void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		String pre = "Beta-level AI core currently assigned. ";
		if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			pre = "Beta-level AI core. ";
		}
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP || mode == AICoreDescriptionMode.MANAGE_CORE_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit.", opad, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit.", opad, highlight,
				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION);
	}
	protected void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		String pre = "Gamma-level AI core currently assigned. ";
		if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			pre = "Gamma-level AI core. ";
		}
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP || mode == AICoreDescriptionMode.MANAGE_CORE_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
//			text.addPara(pre + "Reduces upkeep cost by %s.", opad, highlight,
//					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%");
//			tooltip.addImageWithText(opad);
			text.addPara(pre + "Reduces demand by %s unit.", opad, highlight,
					"" + DEMAND_REDUCTION);
			tooltip.addImageWithText(opad);
			return;
		}
		
//		tooltip.addPara(pre + "Reduces upkeep cost by %s.", opad, highlight,
//				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%");
		tooltip.addPara(pre + "Reduces demand by %s unit.", opad, highlight,
				"" + DEMAND_REDUCTION);
	}
	
	
	protected void addPostSupplySection(TooltipMakerAPI tooltip, boolean hasSupply, IndustryTooltipMode mode) {
		
	}
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		
	}
	protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
		
	}
	protected void addPostDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
		
	}
	protected void addPostUpkeepSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
		
	}

	public String getAICoreId() {
		return aiCoreId;
	}

	public void setAICoreId(String aiCoreId) {
		this.aiCoreId = aiCoreId;
	}
	
	protected void applyAICoreToIncomeAndUpkeep() {
		if (aiCoreId == null || Commodities.GAMMA_CORE.equals(aiCoreId)) {
			getUpkeep().unmodifyMult("ind_core");
			return;
		}
		
		float mult = UPKEEP_MULT;
		String name = "AI Core assigned";
		if (aiCoreId.equals(Commodities.ALPHA_CORE)) {
			name = "Alpha Core assigned";
		} else if (aiCoreId.equals(Commodities.BETA_CORE)) {
			name = "Beta Core assigned";
		} else if (aiCoreId.equals(Commodities.GAMMA_CORE)) {
			name = "Gamma Core assigned";
		}
		
		getUpkeep().modifyMult("ind_core", mult, name);
	}
	
	protected void updateAICoreToSupplyAndDemandModifiers() {
		if (aiCoreId == null) {
			return;
		}
		
		boolean alpha = aiCoreId.equals(Commodities.ALPHA_CORE); 
		boolean beta = aiCoreId.equals(Commodities.BETA_CORE); 
		boolean gamma = aiCoreId.equals(Commodities.GAMMA_CORE);
		
		if (alpha) {
//			supplyBonus.modifyFlat(getModId(0), SUPPLY_BONUS, "Alpha core");
//			demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Alpha core");
			applyAlphaCoreSupplyAndDemandModifiers();
		} else if (beta) {
			//demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Beta core");
			applyBetaCoreSupplyAndDemandModifiers();
		} else if (gamma) {
			//demandReduction = DEMAND_REDUCTION;
			//demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Gamma core");
			applyGammaCoreSupplyAndDemandModifiers();
		}
	}
	
	protected void applyAlphaCoreSupplyAndDemandModifiers() {
		supplyBonus.modifyFlat(getModId(0), SUPPLY_BONUS, "Alpha core");
		demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Alpha core");
	}
	
	protected void applyBetaCoreSupplyAndDemandModifiers() {
		demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Beta core");
	}
	
	protected void applyGammaCoreSupplyAndDemandModifiers() {
		demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Gamma core");
	}
	
	protected void updateSupplyAndDemandModifiers() {
		
//		if (this instanceof Mining && market.getName().equals("Louise")) {
//			System.out.println("efwefwe");
//		}
//		supplyBonus = 0;
//		demandReduction = 0;
		supplyBonus.unmodify();
		demandReduction.unmodify();
		
		updateAICoreToSupplyAndDemandModifiers();
		
		updateImprovementSupplyAndDemandModifiers();
		
//		supplyBonus += market.getAdmin().getStats().getDynamic().getValue(Stats.SUPPLY_BONUS_MOD, 0);
//		demandReduction += market.getAdmin().getStats().getDynamic().getValue(Stats.DEMAND_REDUCTION_MOD, 0);
		supplyBonus.modifyFlat(getModId(1), market.getAdmin().getStats().getDynamic().getValue(Stats.SUPPLY_BONUS_MOD, 0), "Administrator");
		demandReduction.modifyFlat(getModId(1), market.getAdmin().getStats().getDynamic().getValue(Stats.DEMAND_REDUCTION_MOD, 0), "Administrator");
		
		if (supplyBonusFromOther != null) {
			supplyBonus.applyMods(supplyBonusFromOther);
		}
		if (demandReductionFromOther != null) {
			demandReduction.applyMods(demandReductionFromOther);
		}
		
//		if (supplyBonusFromOther != null) {
//			supplyBonusFromOther.unmodify();
//		}
//		if (demandReductionFromOther != null) {
//			demandReductionFromOther.unmodify();
//		}
		
	}
	
	
	public boolean showShutDown() {
		return true;
	}
	public boolean canShutDown() {
		return true;
	}
	public String getCanNotShutDownReason() {
		return null;
	}
	public boolean canUpgrade() {
		return true;
	}
	public boolean canDowngrade() {
		return true;
	}
	
	protected String getDescriptionOverride() {
		return null;
	}
	
	public String getNameForModifier() {
		return Misc.ucFirst(getCurrentName().toLowerCase());
	}

	public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return !com.isIllegal();
	}

	public boolean isSupplyLegal(CommodityOnMarketAPI com) {
		return !com.isIllegal();
	}

	protected boolean isAICoreId(String str) {
		Set<String> cores = new HashSet<String>();
		cores.add(Commodities.ALPHA_CORE);
		cores.add(Commodities.BETA_CORE);
		cores.add(Commodities.GAMMA_CORE);
		return cores.contains(str);
	}
	
	public void initWithParams(List<String> params) {
		for (String str : params) {
			if (isAICoreId(str)) {
				setAICoreId(str);
				break;
			}
		}
		
		for (String str : params) {
//			if (Items.PRISTINE_NANOFORGE.equals(str)) {
//				System.out.println("wefwefew");
//			}
			SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(str);
			if (spec == null) continue;
			
			String [] industries = spec.getParams().split(",");
			Set<String> all = new HashSet<String>();
			for (String ind : industries) all.add(ind.trim());
			if (all.contains(getId())) {
				setSpecialItem(new SpecialItemData(str, null));
			}
		}
	}

	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return false;
	}
	

	protected int getBaseStabilityMod() {
		return 0;
	}
	
	protected void modifyStabilityWithBaseMod() {
		int stabilityMod = getBaseStabilityMod();
		int stabilityPenalty = getStabilityPenalty();
		if (stabilityPenalty > stabilityMod) {
			stabilityPenalty = stabilityMod;
		}
		stabilityMod -= stabilityPenalty;
		if (stabilityMod > 0) {
			market.getStability().modifyFlat(getModId(), stabilityMod, getNameForModifier());
		}
//		else if (stabilityMod < 0) {
//			String str = getDeficitText(getStabilityAffectingDeficit().one);
//			market.getStability().modifyFlat(getModId(), stabilityMod, getNameForModifier() + " (" + str.toLowerCase() + ")");
//		}
	}
	
	protected void unmodifyStabilityWithBaseMod() {
		market.getStability().unmodifyFlat(getModId());
	}
	
	protected Pair<String, Integer> getStabilityAffectingDeficit() {
		return new Pair<String, Integer>(Commodities.SUPPLIES, 0);
	}
	protected int getStabilityPenalty() {
		float deficit = getStabilityAffectingDeficit().two;
		if (deficit < 0) deficit = 0;
		return (int) Math.round(deficit);
	}
	
	
	protected void addStabilityPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		Color h = Misc.getHighlightColor();
		float opad = 10f;
		
		MutableStat fake = new MutableStat(0);
		int stabilityMod = getBaseStabilityMod();
		int stabilityPenalty = getStabilityPenalty();

		if (stabilityPenalty > stabilityMod) {
			stabilityPenalty = stabilityMod;
		}
		
		String str = getDeficitText(getStabilityAffectingDeficit().one);
		fake.modifyFlat("1", stabilityMod, getNameForModifier());
		if (stabilityPenalty != 0) {
			fake.modifyFlat("2", -stabilityPenalty, str);
		}
		
		int total = stabilityMod - stabilityPenalty;
		String totalStr = "+" + total;
		if (total < 0) {
			totalStr = "" + total;
			h = Misc.getNegativeHighlightColor();
		}
		float pad = 3f;
		if (total >= 0) {
			tooltip.addPara("Stability bonus: %s", opad, h, totalStr);
		} else {
			tooltip.addPara("Stability penalty: %s", opad, h, totalStr);
		}
		tooltip.addStatModGrid(400, 35, opad, pad, fake, new StatModValueGetter() {
			public String getPercentValue(StatMod mod) {
				return null;
			}
			public String getMultValue(StatMod mod) {
				return null;
			}
			public Color getModColor(StatMod mod) {
				if (mod.value < 0) return Misc.getNegativeHighlightColor();
				return null;
			}
			public String getFlatValue(StatMod mod) {
				return null;
			}
		});
	}

	public void setHidden(boolean hidden) {
		if (hidden) hiddenOverride = true;
		else hiddenOverride = null;
	}
	
	protected Boolean hiddenOverride = null;
	public boolean isHidden() {
		if (hiddenOverride != null) return hiddenOverride;
		return false;
	}
	
	protected transient String dKey = null;
	public String getDisruptedKey() {
		if (dKey != null) return dKey;
		dKey = "$core_disrupted_" + getClass().getSimpleName();
		return dKey;
	}
	
	public void setDisrupted(float days) {
		setDisrupted(days, false);
	}
	public void setDisrupted(float days, boolean useMax) {
		if (!canBeDisrupted()) return;
		
		boolean was = isDisrupted();
		String key = getDisruptedKey();
		
		MemoryAPI memory = market.getMemoryWithoutUpdate();
		float dur = days;
		if (useMax) {
			dur = Math.max(memory.getExpire(key), dur);
		}
		
		if (dur <= 0) {
			memory.unset(key);
		} else {
			memory.set(key, true, dur);
		}
		
		if (!was) {
			notifyDisrupted();
		}
	}
	
	public float getDisruptedDays() {
		String key = getDisruptedKey();
		float dur = market.getMemoryWithoutUpdate().getExpire(key);
		if (dur < 0) dur = 0;
		return dur;
	}
	
	public boolean canBeDisrupted() {
		return true;
	}
	
	public boolean isDisrupted() {
		String key = getDisruptedKey();
		return market.getMemoryWithoutUpdate().is(key, true);
	}

	public float getPatherInterest() {
		float interest = 0;
		if (Commodities.ALPHA_CORE.equals(aiCoreId)) {
			interest += 4f;
		} else if (Commodities.BETA_CORE.equals(aiCoreId)) {
			interest += 2f;
		} else if (Commodities.GAMMA_CORE.equals(aiCoreId)) {
			interest += 1f;
		}
		
		if (special != null) {
			SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(special.getId());
			if (spec != null) {
				if (spec.hasTag(Items.TAG_PATHER1)) interest += 1;
				else if (spec.hasTag(Items.TAG_PATHER2)) interest += 2;
				else if (spec.hasTag(Items.TAG_PATHER4)) interest += 4;
				else if (spec.hasTag(Items.TAG_PATHER6)) interest += 6;
				else if (spec.hasTag(Items.TAG_PATHER8)) interest += 8;
				else if (spec.hasTag(Items.TAG_PATHER10)) interest += 10;
			}
		}
		
		return interest;
	}

	public CargoAPI generateCargoForGatheringPoint(Random random) {
		return null;
	}

	public String getCargoTitleForGatheringPoint() {
		return getCurrentName();
	}

	
	protected SpecialItemData special = null;
	public SpecialItemData getSpecialItem() {
		return special;
	}

	public void setSpecialItem(SpecialItemData special) {
		//if (special == null && this.special != null) {
		if (this.special != null) {
			InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(this.special.getId());
			if (effect != null) {
				effect.unapply(this);
			}
		}
		this.special = special;
	}
	
	protected float getDeficitMult(String ... commodities) {
		float deficit = getMaxDeficit(commodities).two;
		float demand = 0f;
		
		for (String id : commodities) {
			demand = Math.max(demand, getDemand(id).getQuantity().getModifiedInt());
		}
		
		if (deficit < 0) deficit = 0f;
		if (demand < 1) {
			demand = 1;
			deficit = 0f;
		}
		
		float mult = (demand - deficit) / demand;
		if (mult < 0) mult = 0;
		if (mult > 1) mult = 1;
		return mult;
	}

	
	protected void addGroundDefensesImpactSection(TooltipMakerAPI tooltip, float bonus, String ...commodities) {
		Color h = Misc.getHighlightColor();
		float opad = 10f;
		
		MutableStat fake = new MutableStat(1);

		fake.modifyFlat("1", bonus, getNameForModifier());
		
		if (commodities != null) {
			float mult = getDeficitMult(commodities);
			//mult = 0.89f;
			if (mult != 1) {
				String com = getMaxDeficit(commodities).one;
				fake.modifyFlat("2", -(1f - mult) * bonus, getDeficitText(com));
			}
		}
		
		float total = Misc.getRoundedValueFloat(fake.getModifiedValue());
		String totalStr = Strings.X + total;
		if (total < 1f) {
			h = Misc.getNegativeHighlightColor();
		}
		float pad = 3f;
		tooltip.addPara("Ground defense strength: %s", opad, h, totalStr);
		tooltip.addStatModGrid(400, 35, opad, pad, fake, new StatModValueGetter() {
			public String getPercentValue(StatMod mod) {
				return null;
			}
			public String getMultValue(StatMod mod) {
				return null;
			}
			public Color getModColor(StatMod mod) {
				if (mod.value < 0) return Misc.getNegativeHighlightColor();
				return null;
			}
			public String getFlatValue(StatMod mod) {
				String r = Misc.getRoundedValue(mod.value);
				if (mod.value >= 0) return "+" + r;
				return r;
			}
		});
	}
	
	
	public boolean isIndustry() {
		return getSpec().hasTag(Industries.TAG_INDUSTRY);
	}
	
	public boolean isStructure() {
		return getSpec().hasTag(Industries.TAG_STRUCTURE);
	}
	public boolean isOther() {
		return !isIndustry() && !isStructure();
	}
	
	public void notifyColonyRenamed() {
		
	}

	public boolean canImprove() {
		return canImproveToIncreaseProduction();
	}

	public float getImproveBonusXP() {
		return 0;
	}

	public String getImproveMenuText() {
		return "Make improvements...";
	}

	public int getImproveStoryPoints() {
		int base = Global.getSettings().getInt("industryImproveBase");
		return base * (int) Math.round(Math.pow(2, Misc.getNumImprovedIndustries(market)));
		
		//return 1 + Misc.getNumImprovedIndustries(market);
	}

//	private transient String iKey = null;
//	public String getImprovedKey() {
//		if (iKey != null) return iKey;
//		iKey = "$core_improved_" + getClass().getSimpleName();
//		return iKey;
//	}
	
	public boolean isImproved() {
		return improved != null && improved;
	}

	public void setImproved(boolean improved) {
		if (!improved) {
			this.improved = null;
		} else {
			this.improved = improved;
		}
	}
	
	protected void applyImproveModifiers() {
		
	}
	
	public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
		float initPad = 0f;
		float opad = 10f;
		boolean addedSomething = false;
		if (canImproveToIncreaseProduction()) {
			String unit = "unit";
			if (getImproveProductionBonus() != 1) {
				unit = "units";
			}
			if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
				info.addPara("Production increased by %s " + unit + ".", initPad, Misc.getHighlightColor(),
						"" + getImproveProductionBonus());
			} else {
				info.addPara("Increases production by %s " + unit + ".", initPad, Misc.getHighlightColor(),
						"" + getImproveProductionBonus());
				
			}
			initPad = opad;
			addedSomething = true;
		}
		
		if (mode != ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
	//		info.addPara("Each improved industry at a colony raises the cost to improve " +
	//				"another industry by one " + Misc.STORY + " point.", initPad, 
	//				Misc.getStoryOptionColor(), Misc.STORY + " point");
	//		info.addPara("Each improved industry at a colony doubles the number of " +
	//				"" + Misc.STORY + " points required to improve an additional industry.", initPad, 
	//				Misc.getStoryOptionColor(), Misc.STORY + " points");
	//		info.addPara("Each improved industry or structure at a colony doubles the number of " +
	//				"" + Misc.STORY + " points required to improve an additional industry or structure.", initPad, 
	//				Misc.getStoryOptionColor(), Misc.STORY + " points");
			info.addPara("Each improvement made at a colony doubles the number of " +
					"" + Misc.STORY + " points required to make an additional improvement.", initPad, 
					Misc.getStoryOptionColor(), Misc.STORY + " points");
			addedSomething = true;
		}
		if (!addedSomething) {
			info.addSpacer(-opad);
		}
	}

	public String getImproveDialogTitle() {
		return "Improving " + getSpec().getName();
	}

	public String getImproveSoundId() {
		return Sounds.STORY_POINT_SPEND_INDUSTRY;
	}

	protected boolean canImproveToIncreaseProduction() {
		return false;
	}
	
	protected int getImproveProductionBonus() {
		return DEFAULT_IMPROVE_SUPPLY_BONUS;
	}
	
	protected String getImprovementsDescForModifiers() {
		return "Improvements";
	}
	
	protected void updateImprovementSupplyAndDemandModifiers() {
		if (!canImproveToIncreaseProduction()) return;
		if (!isImproved()) return;
		
		int bonus = getImproveProductionBonus();
		if (bonus <= 0) return;
		
		supplyBonus.modifyFlat(getModId(3), bonus, getImprovementsDescForModifiers());
	}
	
	public void addImprovedSection(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {

		if (!isImproved()) return;
		
		float opad = 10f;
		
		
		tooltip.addSectionHeading("Improvements made", Misc.getStoryOptionColor(), 
								  Misc.getStoryDarkColor(), Alignment.MID, opad);
		
		tooltip.addSpacer(opad);
		addImproveDesc(tooltip, ImprovementDescriptionMode.INDUSTRY_TOOLTIP);
		
//		String noun = "industry";
//		if (isStructure()) noun = "structure";
//		tooltip.addPara("You've made improvements to this " + noun + ".", 
//						Misc.getStoryOptionColor(), opad);

	}
	
	
	public RaidDangerLevel adjustCommodityDangerLevel(String commodityId, RaidDangerLevel level) {
		return level;
	}
	
	public RaidDangerLevel adjustItemDangerLevel(String itemId, String data, RaidDangerLevel level) {
		return level;
	}
	
	public int adjustMarineTokensToRaidItem(String itemId, String data, int marineTokens) {
		return marineTokens;
	}
	
	
	public boolean canInstallAICores() {
		return true;
	}
	
//	public List<InstallableIndustryItemPlugin> getInstallableItems() {
//	return new ArrayList<InstallableIndustryItemPlugin>();
//}
	
	protected transient Boolean hasInstallableItems = null;
	public List<InstallableIndustryItemPlugin> getInstallableItems() {
		boolean found = false;
		if (hasInstallableItems != null) {
			found = hasInstallableItems;
		} else {
			OUTER: for (SpecialItemSpecAPI spec : Global.getSettings().getAllSpecialItemSpecs()) {
				if (spec.getParams() == null || spec.getParams().isEmpty()) continue;
				if (spec.getNewPluginInstance(null) instanceof GenericSpecialItemPlugin) {
					for (String id : spec.getParams().split(",")) {
						id = id.trim();
						if (id.equals(getId())) {
							found = true;
							break OUTER;
						}
					}
				}
			}
			hasInstallableItems = found;
		}
		ArrayList<InstallableIndustryItemPlugin> list = new ArrayList<InstallableIndustryItemPlugin>();
		if (found) {
			list.add(new GenericInstallableItemPlugin(this));
		}
		return list;
	}

	public float getBuildProgress() {
		return buildProgress;
	}

	public void setBuildProgress(float buildProgress) {
		this.buildProgress = buildProgress;
	}
	
	
	
	
}













