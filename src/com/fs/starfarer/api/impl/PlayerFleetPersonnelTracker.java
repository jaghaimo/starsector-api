package com.fs.starfarer.api.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.listeners.CargoScreenListener;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.campaign.listeners.CommodityIconProvider;
import com.fs.starfarer.api.campaign.listeners.CommodityTooltipModifier;
import com.fs.starfarer.api.campaign.listeners.GroundRaidObjectivesListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.CargoPodsEntityPlugin;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidType;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class PlayerFleetPersonnelTracker implements ColonyInteractionListener,
													GroundRaidObjectivesListener,
													CommodityTooltipModifier,
													CommodityIconProvider,
													CargoScreenListener {

	public static float XP_PER_RAID_MULT = 0.2f;
	public static float MAX_EFFECTIVENESS_PERCENT = 100f;
	public static float MAX_LOSS_REDUCTION_PERCENT = 50f;
	
	public static boolean KEEP_XP_DURING_TRANSFERS = true;
	
	public static enum PersonnelRank {
		REGULAR("Regular", "icon_crew_green", 0.25f),
		EXPERIENCED("Experienced", "icon_crew_regular", 0.5f),
		VETERAN("Veteran", "icon_crew_veteran", 0.75f),
		ELITE("Elite", "icon_crew_elite", 1f),
		;
		public String name;
		public String iconKey;
		public float threshold;
		private PersonnelRank(String name, String iconKey, float threshold) {
			this.name = name;
			this.iconKey = iconKey;
			this.threshold = threshold;
		}
		
		public static PersonnelRank getRankForXP(float xp) {
			//float f = xp /MAX_XP_LEVEL;
			float f = xp;
			for (PersonnelRank rank : values()) {
				if (f < rank.threshold) {
					return rank;
				}
			}
			return PersonnelRank.ELITE;
		}
	}
	
	public static class CommodityIconProviderWrapper {
		public CargoStackAPI stack;
		public CommodityIconProviderWrapper(CargoStackAPI stack) {
			this.stack = stack;
		}
	}
	public static class CommodityDescriptionProviderWrapper {
		public CargoStackAPI stack;
		public CommodityDescriptionProviderWrapper(CargoStackAPI stack) {
			this.stack = stack;
		}
	}
	
	public static class PersonnelData implements Cloneable {
		public String id;
		public float xp;
		public float num;
		transient public float savedNum;
		transient public float savedXP;
		public PersonnelData(String id) {
			this.id = id;
		}
		@Override
		protected PersonnelData clone() {
			try { 
				PersonnelData copy = (PersonnelData) super.clone();
				copy.savedNum = savedNum;
				copy.savedXP = savedXP;
				return copy;
			} catch (CloneNotSupportedException e) { 
				throw new RuntimeException(e);
			}
		}
		
		public void add(int add) {
			num += add;
		}
		
		public void remove(int remove, boolean removeXP) {
			if (!KEEP_XP_DURING_TRANSFERS) removeXP = true;
			
			if (remove > num) remove = (int) num;
			if (removeXP) xp *= (num - remove) / Math.max(1f, num);
			num -= remove;
			if (removeXP) {
				float maxXP = num;
				xp = Math.min(xp, maxXP);
			}
		}
		
		public void addXP(float xp) {
			this.xp += xp;
			float maxXP = num;
			this.xp = Math.min(this.xp, maxXP);	
		}
		public void removeXP(float xp) {
			this.xp -= xp;
			if (xp < 0) xp = 0;	
		}
		
		public float clampXP() {
			float maxXP = num;
			float prevXP = xp;
			this.xp = Math.min(this.xp, maxXP);
			return Math.max(0f, prevXP - maxXP);
		}
		
		public void numMayHaveChanged(float newNum, boolean keepXP) {
			// if the number was reduced in some way (i.e. picking up a stack, or lost via code, w/e
			// then adjust XP downwards in same proportion
			if (num > newNum) {
				if (keepXP) {
					clampXP();
				} else { 
					xp *= newNum / Math.max(1f, num);
				}
			}
			num = newNum;
		}
		
		public float getXPLevel() {
			float f = xp / Math.max(1f, num);
			if (f < 0) f = 0;
			if (f > 1f) f = 1f;
			return f;
		}
		
		public PersonnelRank getRank() {
			PersonnelRank rank = PersonnelRank.getRankForXP(getXPLevel());
			return rank;
		}
		
		public void integrateWithCurrentLocation(PersonnelAtEntity atLocation) {
			//int numTaken = (int) Math.max(0, num - savedNum);
			int numTaken = (int) Math.round(num - savedNum);
			if (atLocation != null) {// && numTaken > 0) {
				num = savedNum;
				xp = savedXP;
				//PersonnelData copy = atLocation.data.clone();
				PersonnelData copy = atLocation.data;
				if (numTaken > 0) {
					transferPersonnel(copy, this, numTaken, this);
				} else if (numTaken < 0) {
					transferPersonnel(this, copy, -numTaken, this);
				}
			}
		}
	}
	
	
	public static class PersonnelAtEntity implements Cloneable {
		public PersonnelData data;
		public SectorEntityToken entity;
		public String submarketId;
		public PersonnelAtEntity(SectorEntityToken entity, String commodityId, String submarketId) {
			this.entity = entity;
			data = new PersonnelData(commodityId);
			this.submarketId = submarketId;
		}
		
		@Override
		protected PersonnelAtEntity clone() {
			try { 
				PersonnelAtEntity copy = (PersonnelAtEntity) super.clone();
				copy.data = data.clone();
				return copy;
			} catch (CloneNotSupportedException e) { 
				throw new RuntimeException(e);
			}
		}
	}
	
	
	public static final String KEY = "$core_personnelTracker";
	
	public static PlayerFleetPersonnelTracker getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		if (test == null) {// || true) {
			test = new PlayerFleetPersonnelTracker();
			Global.getSector().getMemoryWithoutUpdate().set(KEY, test);
		}
		return (PlayerFleetPersonnelTracker) test; 
	}
	
	protected PersonnelData marineData = new PersonnelData(Commodities.MARINES);
	protected List<PersonnelAtEntity> droppedOff = new ArrayList<PersonnelAtEntity>();
	
	protected transient SectorEntityToken pods = null;
	protected transient SubmarketAPI currSubmarket = null;
	
	public PlayerFleetPersonnelTracker() {
		super();
		
		GenericPluginManagerAPI plugins = Global.getSector().getGenericPlugins();
		//if (!plugins.hasPlugin(PlayerFleetPersonnelTracker.class)) {
			plugins.addPlugin(this, false);
		//}
		
		//Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		Global.getSector().getListenerManager().addListener(this);
		
		//marineData.xp = 2600 * 0.7f;
		//marineData.num = 2600;
		update();
	}

	public void reportCargoScreenOpened() {
		doCleanup(true);
		update();
		currSubmarket = null;
		
		//marineData.xp = marineData.num * 0.7f;
	}
	
	public void reportSubmarketOpened(SubmarketAPI submarket) {
		doCleanup(false);
		currSubmarket = submarket;
	}

	public void reportPlayerLeftCargoPods(SectorEntityToken entity) {
		pods = entity;
	}
	
	public void reportPlayerNonMarketTransaction(PlayerMarketTransaction transaction, InteractionDialogAPI dialog) {
		if (pods == null && dialog != null) {
			SectorEntityToken target = dialog.getInteractionTarget();
			if (target != null && target.getCustomPlugin() instanceof CargoPodsEntityPlugin) {
				pods = target;
			}
		}
		processTransaction(transaction, pods);
	}
	
	public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
		if (transaction.getMarket() == null || 
				transaction.getMarket().getPrimaryEntity() == null ||
				transaction.getSubmarket() == null) return;
		if (!transaction.getSubmarket().getSpecId().equals(Submarkets.SUBMARKET_STORAGE)) {
			doCleanup(true);
			update(false, true, null);
			return;
		}
		processTransaction(transaction, transaction.getMarket().getPrimaryEntity());
	}
	
	public void processTransaction(PlayerMarketTransaction transaction, SectorEntityToken entity) {
		if (entity == null) return;
		
		SubmarketAPI sub = transaction.getSubmarket();
		
//		// when ejecting cargo, there's a fake "storage" submarket, but when interacting with the pods, there's
//		// no submarket - so for pods to display rank correctly, set the submarket when dropping off pods to null
//		if (pods != null) {
//			sub = null;
//		}
		
		for (CargoStackAPI stack : transaction.getSold().getStacksCopy()) {
			if (!stack.isPersonnelStack()) continue;
			if (stack.isMarineStack()) {
				PersonnelAtEntity at = getDroppedOffAt(stack.getCommodityId(), entity, sub, true);
				
				int num = (int) stack.getSize();
				transferPersonnel(marineData, at.data, num, marineData);
			}
		}
		
		for (CargoStackAPI stack : transaction.getBought().getStacksCopy()) {
			if (!stack.isPersonnelStack()) continue;
			if (stack.isMarineStack()) {
				PersonnelAtEntity at = getDroppedOffAt(stack.getCommodityId(), entity, sub, true);
				
				int num = (int) stack.getSize();
				transferPersonnel(at.data, marineData, num, marineData);
			}
		}
		
		doCleanup(true);
		update();
	}
	
	public static void transferPersonnel(PersonnelData from, PersonnelData to, int num, PersonnelData keepsXP) {
		if (num > from.num) {
			num = (int) from.num;
		}
		if (num <= 0) return;
		
		if (KEEP_XP_DURING_TRANSFERS && keepsXP != null) {
			to.add(num);
			from.remove(num, false);
			
			float totalXP = to.xp + from.xp;
			if (keepsXP == from) {
				from.xp = Math.min(totalXP, from.num);
				to.xp = Math.max(0f, totalXP - from.num);
			} else if (keepsXP == to) {
				to.xp = Math.min(totalXP, to.num);
				from.xp = Math.max(0f, totalXP - to.num);
			}
		} else {
			float xp = from.xp * num / from.num;
			
			to.add(num);
			to.addXP(xp);
			
			from.remove(num, true); // also removes XP
		}
	}
	
	
	public void reportRaidObjectivesAchieved(RaidResultData data, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = fleet.getCargo();
		float marines = cargo.getMarines();
		
		marineData.remove(data.marinesLost, true);
		
		float total = marines + data.marinesLost;
		float xpGain = 1f - data.raidEffectiveness;
		xpGain *= total;
		xpGain *= XP_PER_RAID_MULT;
		if (xpGain < 0) xpGain = 0;
		marineData.addXP(xpGain);
		
		update();
	}
	
	public void update() {
		update(false, false, null);
	}
	public void update(boolean withIntegrationFromCurrentLocation, boolean keepXP, CargoStackAPI stack) {
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		if (fleet == null) return;
		CargoAPI cargo = fleet.getCargo();
		
		
		float marines = cargo.getMarines();
		marineData.numMayHaveChanged(marines, keepXP);
		
		if (withIntegrationFromCurrentLocation) {
			//getDroppedOffAt(Commodities.MARINES, getInteractionEntity(), currSubmarket, true);
			PersonnelAtEntity atLocation = getPersonnelAtLocation(Commodities.MARINES, currSubmarket);
			marineData.integrateWithCurrentLocation(atLocation);
		}
		
		
		MutableFleetStatsAPI stats = fleet.getStats();
		
		String id = "marineXP";
		PersonnelRank rank = marineData.getRank();
		float effectBonus = getMarineEffectBonus(marineData);
		float casualtyReduction = getMarineLossesReductionPercent(marineData);
		if (effectBonus > 0) {
			//stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).modifyMult(id, 1f + effectBonus * 0.01f, rank.name + " marines");
			stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).modifyPercent(id, effectBonus, rank.name + " marines");
		} else {
			//stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).unmodifyMult(id);
			stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).unmodifyPercent(id);
		}
		if (casualtyReduction > 0) {
			stats.getDynamic().getStat(Stats.PLANETARY_OPERATIONS_CASUALTIES_MULT).modifyMult(id, 1f - casualtyReduction * 0.01f, rank.name + " marines");
		} else {
			stats.getDynamic().getStat(Stats.PLANETARY_OPERATIONS_CASUALTIES_MULT).unmodifyMult(id);
		}
	}
	
	
	
	
	public static float getMarineEffectBonus(PersonnelData data) {
		float f =  data.getXPLevel();
		//if (true) return 30f;
		return Math.round(f * MAX_EFFECTIVENESS_PERCENT);
	}
	public static float getMarineLossesReductionPercent(PersonnelData data) {
		float f =  data.getXPLevel();
		//if (true) return 30f;
		return Math.round(f * MAX_LOSS_REDUCTION_PERCENT);
	}
	
	public void addSectionAfterPrice(TooltipMakerAPI info, float width, boolean expanded, CargoStackAPI stack) {
		if (Commodities.MARINES.equals(stack.getCommodityId()) && !expanded) {
			saveData();
			update(true, true, stack);

			PersonnelData data = marineData;
			boolean nonPlayer = false;
			if (!stack.isInPlayerCargo()) {
				nonPlayer = true;
				PersonnelAtEntity atLoc = getPersonnelAtLocation(stack.getCommodityId(), getSubmarketFor(stack));
				if (atLoc != null) {
					data = atLoc.data;
				} else {
					data = null;
				}
			}
			//if (stack.isInPlayerCargo()) {
			if (data != null) {
				if (data.num <= 0) {
					restoreData();
					return;
				}
				
				float opad = 10f;
				float pad = 3f;
				Color h = Misc.getHighlightColor();
	
				PersonnelRank rank = data.getRank();
				
				LabelAPI heading = info.addSectionHeading(rank.name + " marines", 
									Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, opad);
				heading.autoSizeToWidth(info.getTextWidthOverride());
				PositionAPI p = heading.getPosition();
				p.setSize(p.getWidth(), p.getHeight() + 3f);
				
				
				switch (rank) {
				case REGULAR:
					if (nonPlayer) {
						info.addPara("Regular marines - tough, competent, and disciplined.", opad);
					} else {
						info.addPara("These marines are mostly regulars and have seen some combat, " +
								"but are not, overall, accustomed to your style of command.", opad); 
					}
					break;
				case EXPERIENCED:
					if (nonPlayer) {
						info.addPara("Experienced marines with substantial training and a number of " +
									 "operations under their belts.", opad);
					} else {
						info.addPara("You've led these marines on several operations, and " +
								"the experience gained by both parties is beginning to show concrete benefits.", opad);
					}
					break;
				case VETERAN:
					if (nonPlayer) {
						info.addPara("These marines are veterans of many ground operations. " +
								"Well-motivated and highly effective.", opad);
					} else {
						info.addPara("These marines are veterans of many ground operations under your leadership; " +
								"the command structure is well established and highly effective.", opad);
					}
					break;
				case ELITE:
					if (nonPlayer) {
						info.addPara("These marines are an elite force, equipped, led, and motivated well " +
						     	    "above the standards of even the professional militaries in the Sector.", opad);
					} else {
						info.addPara("These marines are an elite force, equipped, led, and motivated well " +
							     	"above the standards of even the professional militaries in the Sector.", opad);
					}
					break;
				
				}
				
				float effectBonus = getMarineEffectBonus(data);
				float casualtyReduction = getMarineLossesReductionPercent(data);
				MutableStat fake = new MutableStat(1f);
				fake.modifyPercentAlways("1", effectBonus, "increased effectiveness of ground operations");
				fake.modifyPercentAlways("2", -casualtyReduction, "reduction to marine casualties suffered during ground operations");
				info.addStatModGrid(width, 50f, 10f, opad, fake, true, null);
				
			}
			restoreData();
		}
	}
	
	
	public void reportPlayerClosedMarket(MarketAPI market) {
		update();
	}
	public void reportPlayerOpenedMarket(MarketAPI market) {
		update();
	}
	

	public String getIconName() {
		return null;
	}


	public int getHandlingPriority(Object params) {
		if (params instanceof CommodityIconProviderWrapper) {
			CargoStackAPI stack = ((CommodityIconProviderWrapper) params).stack;
			if (Commodities.MARINES.equals(stack.getCommodityId())) {
				if (stack.isInPlayerCargo()) {
					return GenericPluginManagerAPI.CORE_GENERAL;
				}
				
				SubmarketAPI sub = getSubmarketFor(stack);
				PersonnelAtEntity atLocation = getPersonnelAtLocation(stack.getCommodityId(), sub);
				if (atLocation != null) {
					return GenericPluginManagerAPI.CORE_GENERAL;
				}
			}
		}
		return -1;
	}
	
//	public PersonnelRank getFleetMarineRank() {
//		PersonnelAtEntity atLocation = getPersonnelAtLocation(Commodities.MARINES);
//		PersonnelRank rank = marineData.getRank(atLocation);
//		return rank;
//	}
	

	public String getRankIconName(CargoStackAPI stack) {
		if (stack.isPickedUp()) return null;
		saveData();
		update(true, true, stack);
		PersonnelData data = null;
		
		if (stack.isMarineStack()) {
			data = marineData;
			if (!stack.isInPlayerCargo()) {
				SubmarketAPI sub = getSubmarketFor(stack);
				PersonnelAtEntity atLocation = getPersonnelAtLocation(stack.getCommodityId(), sub);
				if (atLocation != null) {
					data = atLocation.data;
				} else {
					restoreData();
					return null;
				}
			}
		}
		
		
		if (data == null || data.num <= 0) {
			restoreData();
			return null;
		}
		
		PersonnelRank rank = data.getRank();
		restoreData();
		return Global.getSettings().getSpriteName("ui", rank.iconKey);
	}
	
	public String getIconName(CargoStackAPI stack) {
		return null;
	}
	
	
	protected transient PersonnelData savedMarineData;
	protected transient List<PersonnelAtEntity> savedPersonnelData = new ArrayList<PersonnelAtEntity>();
	
	public void saveData() {
		savedMarineData = marineData;
		marineData = marineData.clone();
		
		savedPersonnelData = new ArrayList<PersonnelAtEntity>();
		for (PersonnelAtEntity curr : droppedOff) {
			savedPersonnelData.add(curr.clone());
		}
	}
	
	public void restoreData() {
		marineData = savedMarineData;
		savedMarineData = null;
		
		droppedOff.clear();
		droppedOff.addAll(savedPersonnelData);
		savedPersonnelData.clear();
	}

	
	public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
	}

	public void modifyRaidObjectives(MarketAPI market, SectorEntityToken entity, List<GroundRaidObjectivePlugin> objectives, RaidType type, int marineTokens, int priority) {
		
	}
	
	protected void doCleanup(boolean withDroppedOff) {
		marineData.savedNum = marineData.num;
		marineData.savedXP = marineData.xp;
		pods = null;
		
		if (withDroppedOff) {
			Iterator<PersonnelAtEntity> iter = droppedOff.iterator();
			while (iter.hasNext()) {
				PersonnelAtEntity pae = iter.next();
				if (!pae.entity.isAlive() || pae.data.num <= 0 || pae.data.xp <= 0) {
					iter.remove();
				}
			}
		}
	}
	
	public SectorEntityToken getInteractionEntity() {
		InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
		SectorEntityToken entity = null;
		if (dialog != null) {
			entity = dialog.getInteractionTarget();
			if (entity != null && entity.getMarket() != null && entity.getMarket().getPrimaryEntity() != null) {
				entity = entity.getMarket().getPrimaryEntity();
			}
		}
		return entity;
	}
	
	/**
	 * Assumes stack is not in player cargo.
	 * @param stack
	 * @return
	 */
	public SubmarketAPI getSubmarketFor(CargoStackAPI stack) {
		if (stack.getCargo() == null) return null;
		SectorEntityToken entity = getInteractionEntity();
		if (entity == null || entity.getMarket() == null || entity.getMarket().getSubmarketsCopy() == null) return currSubmarket;
		
		for (SubmarketAPI sub : entity.getMarket().getSubmarketsCopy()) {
			if (sub.getCargo() == stack.getCargo()) {
				return sub;
			}
		}
		return currSubmarket;
	}

	public PersonnelAtEntity getDroppedOffAt(String commodityId, SectorEntityToken entity, SubmarketAPI sub, boolean createIfNull) {
		String submarketId = sub == null ? "" : sub.getSpecId();
		for (PersonnelAtEntity pae : droppedOff) {
			String otherSubmarketId = pae.submarketId == null ? "" : pae.submarketId;
			if (entity == pae.entity && commodityId.equals(pae.data.id) && submarketId.equals(otherSubmarketId)) {
				return pae;
			}
		}
		if (createIfNull) {
			if (submarketId.isEmpty()) submarketId = null;
			PersonnelAtEntity pae = new PersonnelAtEntity(entity, commodityId, submarketId);
			droppedOff.add(pae);
			return pae;
		}
		return null;
	}
	
	public PersonnelAtEntity getPersonnelAtLocation(String commodityId, SubmarketAPI sub) {
		SectorEntityToken entity = getInteractionEntity();
		PersonnelAtEntity atLocation = entity == null ? null : getDroppedOffAt(commodityId, entity, sub, false);
		return atLocation;
	}

	public PersonnelData getMarineData() {
		return marineData;
	}

	public List<PersonnelAtEntity> getDroppedOff() {
		return droppedOff;
	}
	
	
}







