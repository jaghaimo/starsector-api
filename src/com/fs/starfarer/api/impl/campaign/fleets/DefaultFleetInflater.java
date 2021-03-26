package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.plugins.AutofitPlugin.AutofitPluginDelegate;
import com.fs.starfarer.api.plugins.AutofitPlugin.AvailableFighter;
import com.fs.starfarer.api.plugins.AutofitPlugin.AvailableWeapon;
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin;
import com.fs.starfarer.api.util.ListMap;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class DefaultFleetInflater implements FleetInflater, AutofitPluginDelegate {

	public static float GOAL_VARIANT_PROBABILITY = 0.5f;
	
	
	public static class SortedWeapons {
		private Map<Integer, WeaponsForTier> tierMap = new LinkedHashMap<Integer, WeaponsForTier>();

		public WeaponsForTier getWeapons(int tier) {
			WeaponsForTier data = tierMap.get(tier);
			if (data == null) {
				data = new WeaponsForTier();
				tierMap.put(tier, data);
			}
			return data;
		}
	}
	
	public static class WeaponsForTier {
		private Map<String, List<AvailableWeapon>> catMap = new LinkedHashMap<String, List<AvailableWeapon>>();

		public List<AvailableWeapon> getWeapons(String cat) {
			List<AvailableWeapon> list = catMap.get(cat);
			if (list == null) {
				list = new ArrayList<AvailableWeapon>();
				catMap.put(cat, list);
			}
			return list;
		}
	}
	
	
	public static class AvailableFighterImpl implements AvailableFighter {
		private FighterWingSpecAPI spec;
		private int quantity = 0;
		
		public AvailableFighterImpl(FighterWingSpecAPI spec, int quantity) {
			this.spec = spec;
			this.quantity = quantity;
		}

		public AvailableFighterImpl(String wingId, int quantity) {
			spec = Global.getSettings().getFighterWingSpec(wingId);
			this.quantity = quantity;
		}
		
		public String getId() {
			return spec.getId();
		}
		public float getPrice() {
			return 0;
		}
		public int getQuantity() {
			return quantity;
		}
		public CargoAPI getSource() {
			return null;
		}
		public SubmarketAPI getSubmarket() {
			return null;
		}
		public FighterWingSpecAPI getWingSpec() {
			return spec;
		}
		public void setQuantity(int quantity) {
			this.quantity = quantity;	
		}
	}
	
	public static class AvailableWeaponImpl implements AvailableWeapon {
		private WeaponSpecAPI spec;
		private int quantity = 0;
		public AvailableWeaponImpl(WeaponSpecAPI spec, int quantity) {
			this.spec = spec;
			this.quantity = quantity;
		}
		
		public String getId() {
			return spec.getWeaponId();
		}
		public float getPrice() {
			return 0;
		}
		public int getQuantity() {
			return quantity;
		}
		public CargoAPI getSource() {
			return null;
		}
		public SubmarketAPI getSubmarket() {
			return null;
		}
		public WeaponSpecAPI getSpec() {
			return spec;
		}
		public void setQuantity(int quantity) {
			this.quantity = quantity;	
		}
		
		private MutableShipStatsAPI savedCostStats = null;
		private float cachedOPCost = -1;
		public float getOPCost(MutableCharacterStatsAPI stats, MutableShipStatsAPI shipStats) {
			if (savedCostStats == shipStats && cachedOPCost >= 0) return cachedOPCost;
			
			cachedOPCost = spec.getOrdnancePointCost(stats, shipStats);
			savedCostStats = shipStats;
			return cachedOPCost;
		}
	}
	
//	private float quality = 1f;
//	private Long seed = null;
//	private Long timestamp = null;
//	private Boolean persistent = null;
//	private ShipPickMode mode = null;
	
	protected DefaultFleetInflaterParams p;
	
	transient FleetMemberAPI currMember = null;
	transient ShipVariantAPI currVariant = null;
	transient List<AvailableFighter> fighters;
	transient List<AvailableWeapon> weapons;
	transient List<String> hullmods;
	transient CampaignFleetAPI fleet;
	transient FactionAPI faction;
	

	public DefaultFleetInflater(DefaultFleetInflaterParams p) {
		//this(p.quality, p.seed, p.persistent, p.mode, p.timestamp);
		this.p = p;
	}
//	public DefaultFleetInflater(float quality, Long seed, Boolean persistent, ShipPickMode mode, Long timestamp) {
//		this.quality = quality;
//		this.seed = seed;
//		this.persistent = persistent;
//		this.mode = mode;
//		this.timestamp = timestamp;
//	}
	
	
	public static float getTierProbability(int tier, float quality) {
		//if (true) return 1f;
		
//		if (tier == 1) return 0.5f + quality;
//		if (tier == 2) return 0.25f + quality * 0.5f;
//		if (tier == 3) return 0.125f + quality * 0.25f;
		
		//if (tier != 0) return 0f;
		
//		if (tier == 1) return Math.min(0.9f, 0.5f + quality);
//		if (tier == 2) return Math.min(0.9f, 0.25f + quality * 0.5f);
//		if (tier == 3) return Math.min(0.9f, 0.125f + quality * 0.25f);
		
		// since whether to upgrade or not is now randomized, higher probability of
		// better tier weapons being available (as they may still not end up being used)
		if (tier == 1) return Math.min(0.9f, 0.75f + quality);
		if (tier == 2) return Math.min(0.9f, 0.5f + quality * 0.5f);
		if (tier == 3) return Math.min(0.9f, 0.25f + quality * 0.25f);
		
		return 1f;
	}
	

	public void inflate(CampaignFleetAPI fleet) {
		Random random = new Random();
		//p.seed = null;
		if (p.seed != null) random = new Random(p.seed);
		
		//p.quality = 2f;
		
		//random = new Random();

		
		Random dmodRandom = new Random();
		if (p.seed != null) dmodRandom = Misc.getRandom(p.seed, 5);
		
		CoreAutofitPlugin auto = new CoreAutofitPlugin(fleet.getCommander());
		auto.setRandom(random);

		boolean upgrade = random.nextFloat() < Math.min(0.1f + p.quality * 0.5f, 0.5f);
		auto.setChecked(CoreAutofitPlugin.UPGRADE, upgrade);
		
		//auto.setChecked(CoreAutofitPlugin.RANDOMIZE, true);
		//auto.getOptions().get(4).checked = true; // upgrade
		
		this.fleet = fleet;
		this.faction = fleet.getFaction();
		if (p.factionId != null) {
			this.faction = Global.getSector().getFaction(p.factionId);
		}
		
		//this.faction = Global.getSector().getFaction(Factions.HEGEMONY);
		
		hullmods = new ArrayList<String>(faction.getKnownHullMods());
		
//		fighters = new ArrayList<AvailableFighter>();
//		for (String wingId : faction.getKnownFighters()) {
//			fighters.add(new AvailableFighterImpl(wingId, 1000));
//		}
		
		SortedWeapons nonPriorityWeapons = new SortedWeapons();
		SortedWeapons priorityWeapons = new SortedWeapons();
		
		
		Set<String> weaponCategories = new LinkedHashSet<String>();
		for (String weaponId : faction.getKnownWeapons()) {
			if (!faction.isWeaponKnownAt(weaponId, p.timestamp)) continue;
			
			WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(weaponId);
			//if (mode == ShipPickMode.IMPORTED && !spec.hasTag(Items.TAG_BASE_BP)) continue;
			
			if (spec == null) {
				throw new RuntimeException("Weapon with spec id [" + weaponId + "] not found");
			}
			
			int tier = spec.getTier();
			String cat = spec.getAutofitCategory();
			
			if (isPriority(spec)) {
				List<AvailableWeapon> list = priorityWeapons.getWeapons(tier).getWeapons(cat);
				list.add(new AvailableWeaponImpl(spec, 1000));
			} else {
				List<AvailableWeapon> list = nonPriorityWeapons.getWeapons(tier).getWeapons(cat);
				list.add(new AvailableWeaponImpl(spec, 1000));
			}
			weaponCategories.add(cat);
		}
		
		ListMap<AvailableFighter> nonPriorityFighters = new ListMap<AvailableFighter>(); 
		ListMap<AvailableFighter> priorityFighters = new ListMap<AvailableFighter>(); 
		Set<String> fighterCategories = new LinkedHashSet<String>();
		for (String wingId : faction.getKnownFighters()) {
			if (!faction.isFighterKnownAt(wingId, p.timestamp)) continue;
			
			FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(wingId);
			if (spec == null) {
				throw new RuntimeException("Fighter wing with spec id [" + wingId + "] not found");
			}
			
			//if (mode == ShipPickMode.IMPORTED && !spec.hasTag(Items.TAG_BASE_BP)) continue;
			//int tier = spec.getTier();
			String cat = spec.getAutofitCategory();
//			if (cat == null) {
//				System.out.println("wfewfwe");
//			}
			if (isPriority(spec)) {
				priorityFighters.add(cat, new AvailableFighterImpl(spec, 1000));
			} else {
				nonPriorityFighters.add(cat, new AvailableFighterImpl(spec, 1000));
			}
			fighterCategories.add(cat);
		}
		
		
		//float averageDmods = (1f - quality) / Global.getSettings().getFloat("qualityPerDMod");
		float averageDmods = getAverageDmodsForQuality(p.quality);
		
		//System.out.println("Quality: " + quality + ", Average: " + averageDmods);
		
		boolean forceAutofit = fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_FORCE_AUTOFIT_ON_NO_AUTOFIT_SHIPS);
		int memberIndex = 0;
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
			
			if (!forceAutofit && member.getHullSpec().hasTag(Items.TAG_NO_AUTOFIT)) {
				continue;
			}
			
			// need this so that when reinflating a fleet that lost members, the members reinflate consistently
			if (p.seed != null) {
				int extra = member.getShipName().hashCode();
				random = new Random(p.seed * extra);
				auto.setRandom(random);
				dmodRandom = Misc.getRandom(p.seed * extra, 5);
			}
			
			weapons = new ArrayList<AvailableWeapon>();
			for (String cat : weaponCategories) {
				for (int tier = 0; tier < 4; tier++) {
					float p = getTierProbability(tier, this.p.quality);
					if (this.p.allWeapons != null && this.p.allWeapons) {
						p = 1f;
					}
					
					if (random.nextFloat() >= p) continue;
					
					int num = 4;
					
					if (this.p.allWeapons != null && this.p.allWeapons) {
						num = 500;
					}
					
					List<AvailableWeapon> priority = priorityWeapons.getWeapons(tier).getWeapons(cat);

					Set<Integer> picks = makePicks(num, priority.size(), random);
					for (Integer index : picks) {
						AvailableWeapon w = priority.get(index);
						weapons.add(w);
					}
					
					num -= picks.size();
					if (num > 0) {
						List<AvailableWeapon> nonPriority = nonPriorityWeapons.getWeapons(tier).getWeapons(cat);
						picks = makePicks(num, nonPriority.size(), random);
						for (Integer index : picks) {
							AvailableWeapon w = nonPriority.get(index);
							weapons.add(w);
						}
					}
				}
			}
			
			fighters = new ArrayList<AvailableFighter>();
			for (String cat : fighterCategories) {
				List<AvailableFighter> priority = priorityFighters.get(cat);

				boolean madePriorityPicks = false;
				if (priority != null) {
					int num = random.nextInt(2) + 1;
					if (this.p.allWeapons != null && this.p.allWeapons) {
						num = 100;
					}
					
					Set<Integer> picks = makePicks(num, priority.size(), random);
					for (Integer index : picks) {
						AvailableFighter f = priority.get(index);
						fighters.add(f);
						madePriorityPicks = true;
					}
				}
				
				if (!madePriorityPicks) {
					int num = random.nextInt(2) + 1;
					if (this.p.allWeapons != null && this.p.allWeapons) {
						num = 100;
					}
					
					List<AvailableFighter> nonPriority = nonPriorityFighters.get(cat);
					Set<Integer> picks = makePicks(num, nonPriority.size(), random);
					for (Integer index : picks) {
						AvailableFighter f = nonPriority.get(index);
						fighters.add(f);
					}
				}
			}
			
			
			ShipVariantAPI target = member.getVariant();
			if (target.getOriginalVariant() != null) {
				// needed if inflating the same fleet repeatedly to pick up weapon availability changes etc
				target = Global.getSettings().getVariant(target.getOriginalVariant());
			}
			
			if (faction.isPlayerFaction()) {
				if (random.nextFloat() < GOAL_VARIANT_PROBABILITY) {
					List<ShipVariantAPI> targets = Global.getSector().getAutofitVariants().getTargetVariants(member.getHullId());
					WeightedRandomPicker<ShipVariantAPI> alts = new WeightedRandomPicker<ShipVariantAPI>(random);
					for (ShipVariantAPI curr : targets) {
						if (curr.getHullSpec().getHullId().equals(target.getHullSpec().getHullId())) {
							alts.add(curr);
						}
					}
					if (!alts.isEmpty()) {
						target = alts.pick();
					}
				}
			}
			
			
			currVariant = Global.getSettings().createEmptyVariant(fleet.getId() + "_" + memberIndex, target.getHullSpec());
			currMember = member;
			
			if (target.isStockVariant()) {
				currVariant.setOriginalVariant(target.getHullVariantId());
			}
			
			boolean randomize = random.nextFloat() < faction.getDoctrine().getAutofitRandomizeProbability();
			if (member.isStation()) randomize = false;
			auto.setChecked(CoreAutofitPlugin.RANDOMIZE, randomize);
			
			memberIndex++;
			
			int maxSmods = 0;
			if (p.averageSMods != null && !member.isCivilian()) {
				maxSmods = getMaxSMods(currVariant, p.averageSMods, dmodRandom) - currVariant.getSMods().size();
			}
			auto.doFit(currVariant, target, maxSmods, this);
			currVariant.setSource(VariantSource.REFIT);
			member.setVariant(currVariant, false, false);
			
			//int dmods = (int) Math.round(averageDmods + dmodRandom.nextFloat() * 2f - 1f);
//			int dmods = (int) Math.round(averageDmods + dmodRandom.nextFloat() * 3f - 2f);
//			if (dmods > 5) dmods = 5;
//			int dmodsAlready = DModManager.getNumDMods(currVariant);
//			dmods -= dmodsAlready;
//			if (dmods > 0) {
//				DModManager.setDHull(currVariant);
//				DModManager.addDMods(member, true, dmods, dmodRandom);
//			}
			
			if (!currMember.isStation()) {
				int addDmods = getNumDModsToAdd(currVariant, averageDmods, dmodRandom);
				if (addDmods > 0) {
					DModManager.setDHull(currVariant);
					DModManager.addDMods(member, true, addDmods, dmodRandom);
				}
			}
		}
		
		
		fleet.getFleetData().setSyncNeeded();
		fleet.getFleetData().syncIfNeeded();
		
	}
	
	public static int getNumDModsToAdd(ShipVariantAPI variant, float averageDMods, Random random) {
		int dmods = (int) Math.round(averageDMods + random.nextDouble() * 3f - 2f);
		if (dmods > 5) dmods = 5;
		int dmodsAlready = DModManager.getNumDMods(variant);
		dmods -= dmodsAlready;
		
		return Math.max(0, dmods);
	}
	
	public static int getMaxSMods(ShipVariantAPI variant, int averageSMods, Random random) {
		float f = random.nextFloat();
		int sMods = averageSMods;
		if (f < 0.25f) {
			sMods = averageSMods - 1;
		} else if (f < 0.5f) {
			sMods = averageSMods + 1;
		}
		if (sMods > 3) sMods = 3;
		if (sMods < 0) sMods = 0;
		return sMods;
	}
	
	public static float getAverageDmodsForQuality(float quality) {
		float averageDmods = (1f - quality) / Global.getSettings().getFloat("qualityPerDMod");
		return averageDmods;
	}

	
	public static Set<Integer> makePicks(int num, int max, Random random) {
		if (num > max) num = max;
		Set<Integer> result = new LinkedHashSet<Integer>();
		if (num == 0) return result;
		
		if (num == max) {
			for (int i = 0; i < max; i++) {
				result.add(i);
			}
			return result;
		}
		
		while (result.size() < num) {
			int add = random.nextInt(max);
			result.add(add);
		}
		
		return result;
	}
	
	
	public boolean removeAfterInflating() {
		return p.persistent == null || !p.persistent;
	}

	public void setRemoveAfterInflating(boolean removeAfterInflating) {
		p.persistent = !removeAfterInflating;
		if (!p.persistent) p.persistent = null;
	}

	public void clearFighterSlot(int index, ShipVariantAPI variant) {
		variant.setWingId(index, null);
		for (AvailableFighter curr : fighters) {
			if (curr.getId().equals(curr.getId())) {
				curr.setQuantity(curr.getQuantity() + 1);
				break;
			}
		}
	}

	public void clearWeaponSlot(WeaponSlotAPI slot, ShipVariantAPI variant) {
		variant.clearSlot(slot.getId());
		for (AvailableWeapon curr : weapons) {
			if (curr.getId().equals(curr.getId())) {
				curr.setQuantity(curr.getQuantity() + 1);
				break;
			}
		}
	}

	public void fitFighterInSlot(int index, AvailableFighter fighter, ShipVariantAPI variant) {
		fighter.setQuantity(fighter.getQuantity() - 1);
		variant.setWingId(index, fighter.getId());
	}

	public void fitWeaponInSlot(WeaponSlotAPI slot, AvailableWeapon weapon, ShipVariantAPI variant) {
		weapon.setQuantity(weapon.getQuantity() - 1);
		variant.addWeapon(slot.getId(), weapon.getId());
	}

	public List<AvailableFighter> getAvailableFighters() {
		return fighters;
	}

	public List<AvailableWeapon> getAvailableWeapons() {
		return weapons;
	}
	
	public List<String> getAvailableHullmods() {
		return hullmods;
	}

	public ShipAPI getShip() {
		return null;
	}

	
//	public void syncUIWithVariant() {
//		syncUIWithVariant(null);
//	}
	public void syncUIWithVariant(ShipVariantAPI variant) {
		
	}
	
	public boolean isPriority(WeaponSpecAPI weapon) {
		return faction.isWeaponPriority(weapon.getWeaponId());
	}

	public boolean isPriority(FighterWingSpecAPI wing) {
		return faction.isFighterPriority(wing.getId());
	}
	
	public FleetMemberAPI getMember() {
		return currMember;
	}
	
	
	public static void main(String[] args) {
		
		Random random = new Random();
	
		
		float total = 0f;
		float num = 1000f;
		int []counts = new int[10];
		for (int i = 0; i < num; i++) {
			int dmods = 1;
			total += dmods;
			counts[dmods]++;
		}
		
		System.out.println("Average dmods: " + total / num);
		for (int i = 0; i <= 5; i++) {
			System.out.println(i + ":" + counts[i]);
		}
	}
	public FactionAPI getFaction() {
		return faction;
	}
	public Long getSeed() {
		return p.seed;
	}
	public void setSeed(Long seed) {
		this.p.seed = seed;
	}
	public Boolean getPersistent() {
		return p.persistent;
	}
	public void setPersistent(Boolean persistent) {
		this.p.persistent = persistent;
	}
	public float getQuality() {
		return p.quality;
	}
	public void setQuality(float quality) {
		this.p.quality = quality;
	}
	public Long getTimestamp() {
		return p.timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.p.timestamp = timestamp;
	}
	public Object getParams() {
		return p;
	}
	
	
	public boolean canAddRemoveHullmodInPlayerCampaignRefit(String modId) {
		return true;
	}

	public boolean isPlayerCampaignRefit() {
		return false;
	}
}
