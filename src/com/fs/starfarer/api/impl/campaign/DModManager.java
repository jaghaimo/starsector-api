package com.fs.starfarer.api.impl.campaign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.FleetMemberData;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.Status;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.plugins.DModAdderPlugin;
import com.fs.starfarer.api.plugins.DModAdderPlugin.DModAdderParams;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class DModManager {

//	public static final String HULLMOD_DAMAGE = "damage";
//	public static final String HULLMOD_PHASE_ALWAYS = "phaseAlways";
//	public static final String HULLMOD_DAMAGE_STRUCT = "damageStruct";
//	public static final String HULLMOD_DESTROYED_ALWAYS = "destroyedAlways";
//	public static final String HULLMOD_FIGHTER_BAY_DAMAGE = "fighterBayDamage";
//	public static final String HULLMOD_CARRIER_ALWAYS = "carrierAlways";
	
	
	public static int MAX_DMODS_FROM_COMBAT = Global.getSettings().getInt("maxDModsAddedByCombat");

	public static boolean setDHull(ShipVariantAPI variant) {
		//if (!variant.getHullSpec().isDHull()) {
		variant.setSource(VariantSource.REFIT);
		if (!variant.isDHull()) {
			String dHullId = Misc.getDHullId(variant.getHullSpec());
			ShipHullSpecAPI dHull = Global.getSettings().getHullSpec(dHullId);
			variant.setHullSpecAPI(dHull);
			return true;
		}
		return false;
	}
	
	public static int reduceNextDmodsBy = 0;
	
	public static void addDMods(FleetMemberData data, boolean own, CampaignFleetAPI recoverer, Random random) {
		addDMods(data.getMember(), data.getStatus() == Status.DESTROYED, own, recoverer, random);
	}
	
	public static void addDMods(FleetMemberAPI member, boolean destroyed, boolean own, CampaignFleetAPI recoverer, Random random) {
		ShipVariantAPI variant = member.getVariant();
		addDMods(variant, destroyed, own, recoverer, random);
	}
	public static void addDMods(ShipVariantAPI variant, boolean destroyed, boolean own, CampaignFleetAPI recoverer, Random random) {
		//int original = getNumDMods(variant);
		if (random == null) random = new Random();
		
		
		DModAdderParams params = new DModAdderParams();
		params.variant = variant;
		params.destroyed = destroyed;
		params.own = own;
		params.recoverer = recoverer;
		params.random = random;
		DModAdderPlugin plugin = Global.getSector().getGenericPlugins().pickPlugin(DModAdderPlugin.class, params);
		if (plugin != null) {
			plugin.addDMods(params);
			return;
		}
		
		
		if (destroyed) {
			addAllPermaModsWithTags(variant, Tags.HULLMOD_DESTROYED_ALWAYS);
//			if (own) {
//				int added = getNumDMods(variant) - original;
//				if (added > 0) return;
//			}
		}
		
//		if (member.getHullSpec().getHints().contains(ShipTypeHints.CIVILIAN)) {
//			addAllPermaModsWithTags(variant, Tags.HULLMOD_CIV_ALWAYS);
//		}

		List<HullModSpecAPI> potentialMods = getModsWithTags(Tags.HULLMOD_DAMAGE);
		removeUnsuitedMods(variant, potentialMods);
		
		boolean hasStructDamage = getNumDMods(variant, Tags.HULLMOD_DAMAGE_STRUCT) > 0;
		if (hasStructDamage) {
			potentialMods = getModsWithoutTags(potentialMods, Tags.HULLMOD_DAMAGE_STRUCT);
		}
		
		//if (variant.getHullSpec().getFighterBays() > 0 || variant.isCarrier()) {
		if (variant.getHullSpec().getFighterBays() > 0) {
			potentialMods.addAll(getModsWithTags(Tags.HULLMOD_FIGHTER_BAY_DAMAGE));
		}
		if (variant.getHullSpec().getDefenseType() == ShieldType.PHASE) {
			potentialMods.addAll(getModsWithTags(Tags.HULLMOD_DAMAGE_PHASE));
		}
		
//		if (variant.isCarrier()) {
//			if (own || true) { // bit too harsh to always add damaged flight decks to recovered enemy carriers
//				potentialMods.addAll(getModsWithTags(Tags.HULLMOD_CARRIER_ALWAYS));
//			} else {
//				addAllPermaModsWithTags(variant, Tags.HULLMOD_CARRIER_ALWAYS);
//			}
//		}
		
		removeModsAlreadyInVariant(variant, potentialMods);
		
		int num = 2 + random.nextInt(3);
		
		int reduction = 0;
		reduction += reduceNextDmodsBy;
		reduceNextDmodsBy = 0;
		if (recoverer != null) {
			reduction = (int) recoverer.getStats().getDynamic().getValue(Stats.SHIP_DMOD_REDUCTION, 0);
			reduction = random.nextInt(reduction + 1);
		}
		
		num -= reduction;
		if (num < 1) num = 1;
		
		int already = getNumDMods(variant);
		
		int add = num - already;
		if (own) {
			add = (1 - reduction);
		}
		
		if (add + already > MAX_DMODS_FROM_COMBAT) {
			add = MAX_DMODS_FROM_COMBAT - already;
		}
		if (add <= 0) return;

		
		WeightedRandomPicker<HullModSpecAPI> picker = new WeightedRandomPicker<HullModSpecAPI>(random);
		picker.addAll(potentialMods);
		for (int i = 0; i < add && !picker.isEmpty(); i++) {
			HullModSpecAPI pick = picker.pickAndRemove();
			if (pick != null) {
				if (pick.hasTag(Tags.HULLMOD_DAMAGE_STRUCT) && getNumDMods(variant, Tags.HULLMOD_DAMAGE_STRUCT) > 0) {
					i--;
					continue;
				}
				variant.removeSuppressedMod(pick.getId());
				variant.addPermaMod(pick.getId(), false);
			}
		}
	}
	
	
	public static void addDMods(FleetMemberAPI member, boolean canAddDestroyedMods, int num, Random random) {
		ShipVariantAPI variant = member.getVariant();
		addDMods(variant, canAddDestroyedMods, num, random);
	}
	public static void addDMods(ShipVariantAPI variant, boolean canAddDestroyedMods, int num, Random random) {
		if (random == null) random = new Random();
		
		DModAdderParams params = new DModAdderParams();
		params.variant = variant;
		params.canAddDestroyedMods = canAddDestroyedMods;
		params.num = num;
		params.random = random;
		DModAdderPlugin plugin = Global.getSector().getGenericPlugins().pickPlugin(DModAdderPlugin.class, params);
		if (plugin != null) {
			plugin.addDMods(params);
			return;
		}
		
		
//		if (member.getHullSpec().getHints().contains(ShipTypeHints.CIVILIAN)) {
//			int added = addAllPermaModsWithTags(variant, Tags.HULLMOD_CIV_ALWAYS);
//			if (added > 0) {
//				num -= added;
//				if (num <= 0) return;
//			}
//		}
		
		List<HullModSpecAPI> potentialMods = getModsWithTags(Tags.HULLMOD_DAMAGE);
		if (canAddDestroyedMods) potentialMods.addAll(getModsWithTags(Tags.HULLMOD_DESTROYED_ALWAYS));
		
		removeUnsuitedMods(variant, potentialMods);
		
		boolean hasStructDamage = getNumDMods(variant, Tags.HULLMOD_DAMAGE_STRUCT) > 0;
		if (hasStructDamage) {
			potentialMods = getModsWithoutTags(potentialMods, Tags.HULLMOD_DAMAGE_STRUCT);
		}
		
		if (variant.getHullSpec().getFighterBays() > 0) {
		//if (variant.getHullSpec().getFighterBays() > 0 || variant.isCarrier()) {			
			potentialMods.addAll(getModsWithTags(Tags.HULLMOD_FIGHTER_BAY_DAMAGE));
		}
		if (variant.getHullSpec().getDefenseType() == ShieldType.PHASE) {
			potentialMods.addAll(getModsWithTags(Tags.HULLMOD_DAMAGE_PHASE));
		}
		
		if (variant.isCarrier()) {
			potentialMods.addAll(getModsWithTags(Tags.HULLMOD_CARRIER_ALWAYS));
		}
		
		potentialMods = new ArrayList<HullModSpecAPI>(potentialMods);
		
		removeModsAlreadyInVariant(variant, potentialMods);
		
//		System.out.println("");
//		System.out.println("Adding: ");
		WeightedRandomPicker<HullModSpecAPI> picker = new WeightedRandomPicker<HullModSpecAPI>(random);
		picker.addAll(potentialMods);
		int added = 0;
		for (int i = 0; i < num && !picker.isEmpty(); i++) {
			HullModSpecAPI pick = picker.pickAndRemove();
			if (pick != null) {
				if (pick.hasTag(Tags.HULLMOD_DAMAGE_STRUCT) && getNumDMods(variant, Tags.HULLMOD_DAMAGE_STRUCT) > 0) {
					i--;
					continue;
				}
				variant.removeSuppressedMod(pick.getId());
				variant.addPermaMod(pick.getId(), false);
				//System.out.println("Mod: " + pick.getId());
				added++;
			}
		}
//		if (getNumDMods(variant) < 5) {
//			System.out.println("ewfwefew");
//		}
	}
	


	public static void removeUnsuitedMods(ShipVariantAPI variant, List<HullModSpecAPI> mods) {
		boolean auto = variant.hasHullMod(HullMods.AUTOMATED);
		boolean civ = variant.getHullSpec().getHints().contains(ShipTypeHints.CIVILIAN);
		boolean phase = variant.getHullSpec().getDefenseType() == ShieldType.PHASE;
		boolean peakTime = variant.getHullSpec().getNoCRLossTime() < 10000;
		boolean shields = variant.getHullSpec().getDefenseType() == ShieldType.FRONT || 
						  variant.getHullSpec().getDefenseType() == ShieldType.OMNI; 
				
		Iterator<HullModSpecAPI> iter = mods.iterator();
		while (iter.hasNext()) {
			HullModSpecAPI curr = iter.next();
			if (!peakTime && curr.hasTag(Tags.HULLMOD_PEAK_TIME)) {
				iter.remove();
				continue;
			}
			if (phase && curr.hasTag(Tags.HULLMOD_NOT_PHASE)) {
				iter.remove();
				continue;
			}
			if (auto && curr.hasTag(Tags.HULLMOD_NOT_AUTO)) {
				iter.remove();
				continue;
			}
			if (civ && curr.hasTag(Tags.HULLMOD_NOT_CIV)) {
				iter.remove();
				continue;
			}
			if (civ && !curr.hasTag(Tags.HULLMOD_CIV) && !curr.hasTag(Tags.HULLMOD_CIV_ONLY)) {
				iter.remove();
				continue;
			}
			if (!civ && curr.hasTag(Tags.HULLMOD_CIV_ONLY)) {
				iter.remove();
				continue;
			}
			if (!shields  && curr.hasTag(Tags.HULLMOD_REQ_SHIELDS)) {
				iter.remove();
				continue;
			}
		}
	}
	public static void removeModsAlreadyInVariant(ShipVariantAPI variant, List<HullModSpecAPI> mods) {
		Iterator<HullModSpecAPI> iter = mods.iterator();
		while (iter.hasNext()) {
			HullModSpecAPI curr = iter.next();
			if (variant.hasHullMod(curr.getId())) iter.remove();
		}
	}
	
	public static int addAllPermaModsWithTags(ShipVariantAPI variant, String ... tags) {
		int added = 0;
		for (HullModSpecAPI mod : getModsWithTags(tags)) {
			if (!variant.hasHullMod(mod.getId())) added++;
			variant.removeSuppressedMod(mod.getId());
			variant.addPermaMod(mod.getId(), false);
		}
		return added;
	}
	
	public static List<HullModSpecAPI> getModsWithoutTags(List<HullModSpecAPI> mods, String ... tags) {
		List<HullModSpecAPI> result = new ArrayList<HullModSpecAPI>();
		OUTER: for (HullModSpecAPI mod : mods) {
			for (String tag : tags) {
				if (mod.hasTag(tag)) continue OUTER;
			}
			result.add(mod);
		}
		return result;
	}
	
	public static List<HullModSpecAPI> getModsWithTags(String ... tags) {
		List<HullModSpecAPI> result = new ArrayList<HullModSpecAPI>();
		for (HullModSpecAPI mod : Global.getSettings().getAllHullModSpecs()) {
			if (mod.getTags().containsAll(Arrays.asList(tags))) {
				result.add(mod);
			}
		}
		return result;
	}
	
	public static int getNumDMods(ShipVariantAPI variant) {
		int count = 0;
		for (String id : variant.getHullMods()) {
			if (getMod(id).hasTag(Tags.HULLMOD_DMOD)) count++;
		}
		return count;
	}
	
	public static int getNumNonBuiltInDMods(ShipVariantAPI variant) {
		int count = 0;
		for (String id : variant.getHullMods()) {
			if (getMod(id).hasTag(Tags.HULLMOD_DMOD)) {
				if (variant.getHullSpec().getBuiltInMods().contains(id)) continue;
				count++;
			}
		}
		return count;
	}
	
	public static int getNumDMods(ShipVariantAPI variant, String ... tags) {
		int count = 0;
		for (String id : variant.getHullMods()) {
			HullModSpecAPI mod = getMod(id);
			if (!mod.getTags().containsAll(Arrays.asList(tags))) continue;
			if (mod.hasTag(Tags.HULLMOD_DMOD)) count++;
		}
		return count;
	}
	
	public static HullModSpecAPI getMod(String id) {
		return Global.getSettings().getHullModSpec(id);
	}

	public static void removeDMod(ShipVariantAPI v, String id) {
		ShipHullSpecAPI base = v.getHullSpec().getDParentHull();
		
		// so that a skin with dmods can be "restored" - i.e. just dmods suppressed w/o changing to
		// actual base skin
		if (!v.getHullSpec().isDefaultDHull() && !v.getHullSpec().isRestoreToBase()) {
			base = v.getHullSpec();
		}
		if (base == null && v.getHullSpec().isRestoreToBase()) {
			base = v.getHullSpec().getBaseHull();
		}
		if (base.isBuiltInMod(id)) {
			v.removePermaMod(id);
			v.addSuppressedMod(id);
		} else {
			v.removePermaMod(id);
			v.removeMod(id);
		}
	}
}





