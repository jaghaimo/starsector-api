package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class DropGroupRow implements Cloneable {
	public static final String NOTHING = "nothing";
	
	
	public static final String WEAPON_PREFIX = "wpn_";
	//public static final String MOD_PREFIX = "mod_";
	public static final String FIGHTER_PREFIX = "ftr_";
	public static final String ITEM_PREFIX = "item_";
	
	
	private String commodity, group;
	private float freq;
	private boolean percent = false;
	
	private boolean multiValued = false;
	private int tier = -1;
	private List<String> tags = new ArrayList<String>(); 
	//private List<String> tags2 = new ArrayList<String>(); 
	private WeaponType weaponType = null;
	private WeaponSize weaponSize = null;
	
//	private String itemId = null;
//	private String itemParams = null;
	
	Object writeReplace() {
		DropGroupRow copy = clone();
		if (tags != null && tags.isEmpty()) {
			copy.tags = null;
		}
//		if (tags2 != null && tags2.isEmpty()) {
//			copy.tags2 = null;
//		}
		return copy;
		//return this;
	}
	
	@Override
	public DropGroupRow clone() {
		try {
			DropGroupRow copy = (DropGroupRow) super.clone();
			return copy;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public DropGroupRow(JSONObject row) throws JSONException {
		commodity = row.getString("commodity");
		group = row.getString("group");
		
		String fStr = row.getString("freq");
		if (fStr.endsWith("%")) {
			percent = true;
			fStr = fStr.substring(0, fStr.length() - 1);
			freq = Float.parseFloat(fStr);
		} else {
			freq = (float) row.getDouble("freq");
		}
		parseData();
	}
	
	private void parseData() throws JSONException {
		if (commodity.contains(":")) {
			multiValued = true;
			
			if (commodity.startsWith(ITEM_PREFIX)) {
//				item_factory_core   								resolved
//				item_modspec:converted_hangar						resolved
//				item_modspec:{}										unresolved
//				item_:{}											unresolved
//				item_modspec:{tier:3, tags:[shields]}				unresolved
//				item_:{tags:[modspec], p:{tier:3, tags:[engines]}	unresolved
				
				String test = commodity.replaceFirst(ITEM_PREFIX, "");
				int index = test.indexOf(':');
				if (index < 0) {
					multiValued = false;
				} else {
					String itemId = test.substring(0, index);
					String secondPart = test.substring(index + 1);
					
					if (itemId.isEmpty() && secondPart.startsWith("{")) {
						JSONObject json = new JSONObject(secondPart);
						
						tier = json.optInt("tier", -1);
						if (json.has("tags")) {
							JSONArray tags = json.getJSONArray("tags");
							for (int i = 0; i < tags.length(); i++) {
								this.tags.add(tags.getString(i));
							}
						}
					} else if (!secondPart.startsWith("{")) {
						multiValued = false;
					}
				}
				return;
			}
			
			
			JSONObject json = new JSONObject(commodity.substring(commodity.indexOf(":") + 1));
			tier = json.optInt("tier", -1);
			if (json.has("tags")) {
				JSONArray tags = json.getJSONArray("tags");
				for (int i = 0; i < tags.length(); i++) {
					this.tags.add(tags.getString(i));
				}
			}
			if (json.has("weaponType")) {
				weaponType = Misc.mapToEnum(json, "weaponType", WeaponType.class, null);
			}
			if (json.has("weaponSize")) {
				weaponSize = Misc.mapToEnum(json, "weaponSize", WeaponSize.class, null);
			}
		}
	}
	
	public DropGroupRow(String commodity, String group, float freq) {
		this.commodity = commodity;
		this.group = group;
		this.freq = freq;
		try {
			parseData();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public CommoditySpecAPI getSpec() {
		if (isNothing() || isWeapon()) return null;
		
		CommoditySpecAPI spec = Global.getSector().getEconomy().getCommoditySpec(commodity);
		return spec;
	}
	
	public WeaponSpecAPI getWeaponSpec() {
		if (!isWeapon()) return null;
		
		WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(getWeaponId());
		return spec;
	}
	
//	public HullModSpecAPI getHullModSpec() {
//		if (!isHullMod()) return null;
//		
//		HullModSpecAPI spec = Global.getSettings().getHullModSpec(getHullModId());
//		return spec;
//	}
	
	public float getBaseUnitValue() {
		if (isMultiValued()) throw new RuntimeException("Call resolveToSpecificItem() before calling getBaseUnitValue()");
		
		if (isWeapon()) {
			return getWeaponSpec().getBaseValue();
//		} else if (isHullMod()) {
//			return getHullModSpec().getBaseValue();
		} else if (isFighterWing()) {
			return getFighterWingSpec().getBaseValue();
		} else if (isSpecialItem()) {
			CargoStackAPI stack = Global.getFactory().createCargoStack(CargoItemType.SPECIAL, 
					new SpecialItemData(getSpecialItemId(), getSpecialItemData()), null);
			return stack.getPlugin().getPrice(null, null);
		} else {
			return getSpec().getBasePrice();
		}
	}
	
	public FighterWingSpecAPI getFighterWingSpec() {
		if (!isFighterWing()) return null;
		
		FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(getFighterWingId());
		return spec;
	}
	
	public boolean isCommodity() {
		return !isNothing() && !isWeapon() && !isFighterWing() && !isSpecialItem();
	}
	
	public boolean isWeapon() {
		return commodity != null && commodity.startsWith(WEAPON_PREFIX);
	}
	
	public String getWeaponId() {
		return commodity.substring(WEAPON_PREFIX.length());
	}
	
	public String getSpecialItemId() {
		String afterPrefix = commodity.substring(ITEM_PREFIX.length());
		int index = afterPrefix.indexOf(":");
		if (index >= 0) {
			afterPrefix = afterPrefix.substring(0, index);
		}
		return afterPrefix;
	}
	public String getSpecialItemData() {
		String afterPrefix = commodity.substring(ITEM_PREFIX.length());
		int index = afterPrefix.indexOf(":");
		if (index >= 0) {
			afterPrefix = afterPrefix.substring(index + 1);
			return afterPrefix;
		}
		return null;
	}
	
	public SpecialItemSpecAPI getSpecialItemSpec() {
		if (!isSpecialItem()) return null;
		
		SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(getSpecialItemId());
		return spec;
	}
	
	public boolean isFighterWing() {
		return commodity != null && commodity.startsWith(FIGHTER_PREFIX);
	}
	public boolean isSpecialItem() {
		return commodity != null && commodity.startsWith(ITEM_PREFIX);
	}
	
	public String getFighterWingId() {
		return commodity.substring(FIGHTER_PREFIX.length());
	}
	
//	public boolean isHullMod() {
//		return commodity != null && commodity.startsWith(MOD_PREFIX);
//	}
//	
//	public String getHullModId() {
//		return commodity.substring(MOD_PREFIX.length());
//	}
	
	public boolean isMultiValued() {
		return multiValued;
	}
	
	public boolean isNothing() {
		return commodity == null || commodity.equals(NOTHING);
	}

	public String getCommodity() {
		return commodity;
	}

	public void setCommodity(String commodity) {
		this.commodity = commodity;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public float getFreq() {
		return freq;
	}

	public void setFreq(float freq) {
		this.freq = freq;
	}
	

	public static WeightedRandomPicker<DropGroupRow> getPicker(String group) {
		WeightedRandomPicker<DropGroupRow> picker = new WeightedRandomPicker<DropGroupRow>();
		Collection<DropGroupRow> specs = Global.getSettings().getAllSpecs(DropGroupRow.class);
		
		for (DropGroupRow spec : specs) {
//			if (!spec.isMultiValued() && spec.isHullMod() && spec.getHullModSpec().hasTag(Tags.HULLMOD_NO_DROP)) {
//				continue;
//			}
			if (!spec.isMultiValued() && spec.isFighterWing() && spec.getFighterWingSpec().hasTag(Tags.WING_NO_DROP)) {
				continue;
			}
			
			if (spec.getGroup().equals(group)) {
				picker.add(spec, spec.getFreq());
			}
		}
		
		for (DropGroupRow curr : picker.getItems()) {
			if (curr.isNothing() && curr.percent) {
				float totalOther = picker.getTotal() - curr.freq;
				
				float fNothing = curr.freq / 100f;
				if (fNothing < 0) fNothing = 0;
				if (fNothing > 1) fNothing = 1;
				
				float weightNothing = totalOther * fNothing / (1f - fNothing);
				
				picker.setWeight(picker.getItems().indexOf(curr), weightNothing);
				break;
			}
		}
		
		if (picker.isEmpty()) {
			throw new RuntimeException("No drop data found for drop group [" + group + "], probably an error in drop_groups.csv");
		}
		
		return picker;
	}
	
	
	public DropGroupRow resolveToSpecificItem(Random random) {
		if (random == null) random = new Random();
		
		if (!isMultiValued()) return this;
		
		DropGroupRow copy = clone();
		copy.multiValued = false;
		
		if (isSpecialItem()) {
//			item_factory_core   								resolved
//			item_modspec:converted_hangar						resolved
//			item_modspec:{}										unresolved
//			item_:{}											unresolved
//			item_modspec:{tier:3, tags:[shields]}				unresolved
//			item_:{tags:[modspec], p:{tier:3, tags:[engines]}	unresolved
			
			String test = commodity.replaceFirst(ITEM_PREFIX, "");
			int index = test.indexOf(':');
			if (index < 0) {
			} else {
				boolean getParamsFromP = false;
				String itemId = test.substring(0, index);
				String params = test.substring(index + 1);
				
				if (!itemId.isEmpty()) {
					// params are already set properly, and we have an item id - do nothing
				} else {
					List<SpecialItemSpecAPI> specs = Global.getSettings().getAllSpecialItemSpecs();
					
					Iterator<SpecialItemSpecAPI> iter = specs.iterator();
//					while (iter.hasNext()) {
//						SpecialItemSpecAPI curr = iter.next();
//						if (curr.isHidden() || curr.isHiddenEverywhere()) iter.remove();
//					}
					
					if (!tags.isEmpty()) {
						iter = specs.iterator();
						while (iter.hasNext()) {
							SpecialItemSpecAPI curr = iter.next();
							for (String tag : tags) {
								boolean not = tag.startsWith("!");
								tag = not ? tag.substring(1) : tag;
								boolean has = curr.hasTag(tag);
								if (not == has) {
									iter.remove();
									break;
								}
							}
						}
					}
					
					WeightedRandomPicker<SpecialItemSpecAPI> picker = new WeightedRandomPicker<SpecialItemSpecAPI>(random);
					for (SpecialItemSpecAPI spec : specs) {
						picker.add(spec, 1f * spec.getRarity());
					}
					SpecialItemSpecAPI pick = picker.pick();
					if (pick == null) {
						copy.commodity = NOTHING;
					} else {
						itemId = pick.getId(); 
						getParamsFromP = true;
					}
				}
				
				// we've picked an itemId to use
				if (!itemId.isEmpty()) {
//					item_:{tags:[modspec], p:{tier:3, tags:[engines]}	unresolved
					try {
						if (getParamsFromP) {
							JSONObject json = new JSONObject(params);
							if (json.has("p")) {
								params = json.getJSONObject("p").toString();
							} else {
								params = "{}";
							}
						}
						
						SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(itemId);
						SpecialItemPlugin plugin = spec.getNewPluginInstance(null);
						String itemData = plugin.resolveDropParamsToSpecificItemData(params, random);
						
						if (itemData == null) {
							copy.commodity = NOTHING;
						} else if (itemData.isEmpty()) {
							copy.commodity = ITEM_PREFIX + itemId;
						} else {
							copy.commodity = ITEM_PREFIX + itemId + ":" + itemData;
						}
//						if (copy.commodity.contains("{")) {
//							System.out.println("wefwefew");
//						}
					} catch (JSONException e) {
						throw new RuntimeException("Params: " + params, e);
					}
				} else {
					copy.commodity = NOTHING;
				}
			}
//		} else if (isHullMod()) {
//			List<HullModSpecAPI> specs = Global.getSettings().getAllHullModSpecs();
//			
//			Iterator<HullModSpecAPI> iter = specs.iterator();
//			while (iter.hasNext()) {
//				HullModSpecAPI curr = iter.next();
//				if (curr.isHidden() || curr.isHiddenEverywhere()) iter.remove();
//			}
//			
//			if (tier >= 0) {
//				iter = specs.iterator();
//				while (iter.hasNext()) {
//					HullModSpecAPI curr = iter.next();
////					if (curr.getId().contains("armor")) {
////						System.out.println("wfwefwe");
////					}
//					if (curr.getTier() != tier) iter.remove();
//				}
//			}
//			
//			if (!tags.isEmpty()) {
//				iter = specs.iterator();
//				while (iter.hasNext()) {
//					HullModSpecAPI curr = iter.next();
//					for (String tag : tags) {
//						boolean not = tag.startsWith("!");
//						tag = not ? tag.substring(1) : tag;
//						boolean has = curr.hasTag(tag);
//						if (not == has) {
//							iter.remove();
//							break;
//						}
//					}
//				}
//			}
//			
//			WeightedRandomPicker<HullModSpecAPI> picker = new WeightedRandomPicker<HullModSpecAPI>(random);
//			//picker.addAll(specs);
//			for (HullModSpecAPI spec : specs) {
//				picker.add(spec, 1f * spec.getRarity());
//			}
//			HullModSpecAPI pick = picker.pick();
//			if (pick == null) {
//				copy.commodity = NOTHING;
//			} else {
//				copy.commodity = MOD_PREFIX + pick.getId(); 
//			}
		} else if (isWeapon()) {
			List<WeaponSpecAPI> specs = Global.getSettings().getAllWeaponSpecs();
			if (tier >= 0) {
				Iterator<WeaponSpecAPI> iter = specs.iterator();
				while (iter.hasNext()) {
					WeaponSpecAPI curr = iter.next();
					if (curr.getTier() != tier) iter.remove();
				}
			}
			
			if (!tags.isEmpty()) {
				Iterator<WeaponSpecAPI> iter = specs.iterator();
				while (iter.hasNext()) {
					WeaponSpecAPI curr = iter.next();
					for (String tag : tags) {
						boolean not = tag.startsWith("!");
						tag = not ? tag.substring(1) : tag;
						boolean has = curr.hasTag(tag);
						if (not == has) {
							iter.remove();
							break;
						}
					}
				}
			}
			
			if (weaponType != null || weaponSize != null) {
				Iterator<WeaponSpecAPI> iter = specs.iterator();
				while (iter.hasNext()) {
					WeaponSpecAPI curr = iter.next();
					if ((weaponType != null && curr.getType() != weaponType) ||
							(weaponSize != null && curr.getSize() != weaponSize)) {
						iter.remove();
					}
				}
			}
			
			
			WeightedRandomPicker<WeaponSpecAPI> picker = new WeightedRandomPicker<WeaponSpecAPI>(random);
			//picker.addAll(specs);
			for (WeaponSpecAPI spec : specs) {
				picker.add(spec, 1f * spec.getRarity());
			}
			WeaponSpecAPI pick = picker.pick();
			if (pick == null) {
				copy.commodity = NOTHING;
			} else {
				copy.commodity = WEAPON_PREFIX + pick.getWeaponId(); 
			}
		} else if (isFighterWing()) {
			List<FighterWingSpecAPI> specs = Global.getSettings().getAllFighterWingSpecs();
			Iterator<FighterWingSpecAPI> iter = specs.iterator();
			while (iter.hasNext()) {
				FighterWingSpecAPI curr = iter.next();
				if (curr.hasTag(Tags.WING_NO_DROP)) iter.remove();
			}
			if (tier >= 0) {
				iter = specs.iterator();
				while (iter.hasNext()) {
					FighterWingSpecAPI curr = iter.next();
					if (curr.getTier() != tier) iter.remove();
				}
			}
			
			if (!tags.isEmpty()) {
				iter = specs.iterator();
				while (iter.hasNext()) {
					FighterWingSpecAPI curr = iter.next();
					for (String tag : tags) {
						boolean not = tag.startsWith("!");
						tag = not ? tag.substring(1) : tag;
						boolean has = curr.hasTag(tag);
						if (not == has) {
							iter.remove();
							break;
						}
					}
				}
			}
			WeightedRandomPicker<FighterWingSpecAPI> picker = new WeightedRandomPicker<FighterWingSpecAPI>(random);
			//picker.addAll(specs);
			for (FighterWingSpecAPI spec : specs) {
				picker.add(spec, 1f * spec.getRarity());
			}
			FighterWingSpecAPI pick = picker.pick();
			if (pick == null) {
				copy.commodity = NOTHING;
			} else {
				copy.commodity = FIGHTER_PREFIX + pick.getId(); 
			}
		}
		
		
		return copy;
	}

	@Override
	public String toString() {
		return super.toString() + " " + commodity;
	}

	
}








