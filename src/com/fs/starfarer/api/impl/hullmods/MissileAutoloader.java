package com.fs.starfarer.api.impl.hullmods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

public class MissileAutoloader extends BaseHullMod {

	public static class ReloadCapacityData {
		public HullSize size;
		public int minW, maxW;
		public int capacity;
		public ReloadCapacityData(HullSize size, int minW, int maxW, int capacity) {
			this.size = size;
			this.minW = minW;
			this.maxW = maxW;
			this.capacity = capacity;
		}
		
		public String getSizeStr() {
			return Misc.getHullSizeStr(size);
		}
		
		public String getWeaponsString() {
			if (maxW < 0) return "" + minW + "+";
			if (minW != maxW) return "" + minW + "-" + maxW;
			return "" + minW;
		}
	}
	
	public static List<ReloadCapacityData> CAPACITY_DATA = new ArrayList<MissileAutoloader.ReloadCapacityData>();
	static {
		CAPACITY_DATA.add(new ReloadCapacityData(HullSize.FRIGATE, 1, 1, 6));
		CAPACITY_DATA.add(new ReloadCapacityData(HullSize.FRIGATE, 2, -1, 4));
		
		CAPACITY_DATA.add(new ReloadCapacityData(HullSize.DESTROYER, 1, 1, 9));
		CAPACITY_DATA.add(new ReloadCapacityData(HullSize.DESTROYER, 2, -1, 4));
		
		CAPACITY_DATA.add(new ReloadCapacityData(HullSize.CRUISER, 1, 2, 15));
		CAPACITY_DATA.add(new ReloadCapacityData(HullSize.CRUISER, 3, 3, 12));
		CAPACITY_DATA.add(new ReloadCapacityData(HullSize.CRUISER, 4, -1, 8));
		
		CAPACITY_DATA.add(new ReloadCapacityData(HullSize.CAPITAL_SHIP, 1, 3, 24));
		CAPACITY_DATA.add(new ReloadCapacityData(HullSize.CAPITAL_SHIP, 4, 6, 18));
		CAPACITY_DATA.add(new ReloadCapacityData(HullSize.CAPITAL_SHIP, 7, -1, 10));
	}
	
	public static float BASIC_COOLDOWN = 5f;
	public static float SMOD_COOLDOWN = 10f;
	
	public static String MA_DATA_KEY = "core_missile_autoloader_data_key";
	
	public static class MissileAutoloaderData {
		public IntervalUtil interval = new IntervalUtil(0.2f, 0.4f);
		public float opLeft = 0f;
		public float showExhaustedStatus = 5f;
		public TimeoutTracker<WeaponAPI> cooldown = new TimeoutTracker<WeaponAPI>();
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
		

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);

		if (!ship.isAlive()) return;
		
		String key = MA_DATA_KEY;
		ship.getCustomData().get(key);
		MissileAutoloaderData data = (MissileAutoloaderData) ship.getCustomData().get(key);
		if (data == null) {
			data = new MissileAutoloaderData();
			ReloadCapacityData cap = getCapacityData(ship);
			//data.opLeft = spec.getCostFor(ship.getHullSize());
			if (cap != null) {
				data.opLeft = cap.capacity;
			} else {
				data.showExhaustedStatus = 0;
			}
			ship.setCustomData(key, data);
		}
		
		if (data.opLeft <= 0.05f) {
			data.opLeft = 0f;
			data.showExhaustedStatus -= amount;
			if (data.showExhaustedStatus <= 0) {
				return;
			}
		}
		
		boolean playerShip = Global.getCurrentState() == GameState.COMBAT &&
							 Global.getCombatEngine() != null && Global.getCombatEngine().getPlayerShip() == ship;
		
		float mult = ship.getMutableStats().getMissileRoFMult().getModifiedValue();
		data.cooldown.advance(amount * mult);
		for (WeaponAPI w : data.cooldown.getItems()) {
			w.setRemainingCooldownTo(w.getCooldown());
		}
		
		data.interval.advance(amount);
		if (data.interval.intervalElapsed()) {
			boolean playSound = false;
			for (WeaponAPI w : ship.getAllWeapons()) {
				if (!isAffected(w)) continue;
				if (data.cooldown.contains(w)) continue;
				
				if (w.usesAmmo() && w.getAmmo() <= 0) {
					float reloadSize = w.getSpec().getMaxAmmo();
					float reloadCost = getReloadCost(w, ship);
					float salvoSize = w.getSpec().getBurstSize();
					if (salvoSize < 1) salvoSize = 1;
					if (reloadCost > data.opLeft) {
						float f = data.opLeft / reloadCost;
						if (f <= 0f) continue;
						
						reloadSize *= f;
						reloadSize /= salvoSize;
						reloadSize = (float) Math.ceil(reloadSize);
						reloadSize *= salvoSize;
						reloadSize = (int) Math.round(reloadSize);
					}
					
					playSound = true;
					
					w.setAmmo((int) reloadSize);
					boolean sMod = isSMod(ship);
					//if (COOLDOWN > 0) {
					if (sMod) {
						if (SMOD_COOLDOWN > 0) {
							data.cooldown.set(w, SMOD_COOLDOWN);
						}
					} else {
						if (BASIC_COOLDOWN > 0) {
							data.cooldown.set(w, BASIC_COOLDOWN);
						}
					}
					
					data.opLeft -= reloadCost;
					//data.opLeft = Math.round(data.opLeft);
					
					if (data.opLeft < 0) data.opLeft = 0;
					if (data.opLeft <= 0) break;
				}
			}
			
			playSound = false; // better without the sound I think
			if (playerShip && playSound) {
				Global.getSoundPlayer().playSound("missile_weapon_reloaded", 1f, 1f, ship.getLocation(), ship.getVelocity());
			}
		}
		
		if (playerShip) {
			String status = "" + Misc.getRoundedValueOneAfterDecimalIfNotWhole(data.opLeft) + " CAPACITY REMAINING";
			if (data.opLeft <= 0) status = "CAPACITY EXHAUSTED";
			Global.getCombatEngine().maintainStatusForPlayerShip(data,
					Global.getSettings().getSpriteName("ui", "icon_tactical_missile_autoloader"),
					spec.getDisplayName(), 
					status, data.opLeft <= 0);
			
		}
	}
	
	public static ReloadCapacityData getCapacityData(ShipAPI ship) {
		if (ship == null) return null;
		int count = 0;
//		for (WeaponAPI w : ship.getAllWeapons()) {
//			if (!isAffected(w)) continue;
//			if (w.getSlot().getSlotSize() != WeaponSize.SMALL || w.getSlot().getWeaponType() != WeaponType.MISSILE) {
//				count++;
//			}
//		}
		for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			if (slot.getSlotSize() == WeaponSize.SMALL && 
					slot.getWeaponType() == WeaponType.MISSILE) {
				count++;
			}
		}
		
		for (ReloadCapacityData data : CAPACITY_DATA) {
			if (data.size == ship.getHullSize()) {
				if (count >= data.minW && count <= data.maxW) return data; 
				if (count >= data.minW && data.maxW < 0) return data;
			}
		}
		return null;
	}
	
	public static boolean isAffected(WeaponAPI w) {
		if (w == null) return false;
		if (w.getType() != WeaponType.MISSILE) return false;
		if (w.getSize() != WeaponSize.SMALL) return false;
		
		if (w.getSlot().getWeaponType() != WeaponType.MISSILE) return false;
		if (w.getSlot().getSlotSize() != WeaponSize.SMALL) return false;
		
		if (w.getSpec().hasTag(Tags.NO_RELOAD)) return false;
		if (!w.usesAmmo() || w.getAmmoPerSecond() > 0) return false;
		if (w.isDecorative()) return false;
		if (w.getSlot() != null && w.getSlot().isSystemSlot()) return false;
		return true;
	}
	
	public static float getReloadCost(WeaponAPI w, ShipAPI ship) {
		if (w.getSpec().hasTag(Tags.RELOAD_1PT)) return 1f;
		if (w.getSpec().hasTag(Tags.RELOAD_1_AND_A_HALF_PT)) return 1.5f;
		if (w.getSpec().hasTag(Tags.RELOAD_2PT)) return 2f;
		if (w.getSpec().hasTag(Tags.RELOAD_3PT)) return 3f;
		if (w.getSpec().hasTag(Tags.RELOAD_4PT)) return 4f;
		if (w.getSpec().hasTag(Tags.RELOAD_5PT)) return 5f;
		if (w.getSpec().hasTag(Tags.RELOAD_6PT)) return 6f;
		
		int op = (int) Math.round(w.getSpec().getOrdnancePointCost(null, null));
		if (op == 1) return 1f;
		if (op == 2 || op == 3) return 2f;
		if (op == 4) return 3f;
		if (op == 5 || op == 6) return 4f;
		if (op == 7 || op == 8) return 6f;
		return 6f;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, final ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		
		tooltip.addPara("A combat-rated autoloader that provides a limited number of reloads, out of a shared reload capacity, to "
				+ "missile weapons installed in small missile mounts.", opad, h, "missile weapons installed in small missile mounts");
//		tooltip.addPara("Does not affect weapons that do not use ammo or already regenerate it, or weapons that are "
//				+ "mounted in any other type of weapon slot."
//				+ " The number of missiles reloaded is not affected by skills or hullmods that "
//				+ "increase missile weapon ammo capacity.", opad);		
		tooltip.addPara("Does not affect weapons that do not use ammo or regenerate it, or are mounted in any other type of slot."
				+ " Reload size is not affected by skills or hullmods that "
				+ "increase missile ammo capacity.", opad);
		
//		tooltip.addPara("A combat-rated autoloader capable of reloading small missile weapons "
//				+ "installed in small missile mounts"
//				+ " a limited number of times. Does not affect weapons that do not use ammo or regenerate it."
//				+ " The number of missiles reloaded is not affected by skills or hullmods that "
//				+ "increase missile weapon ammo capacity.", opad, h, "small missile weapons installed in small missile mounts");
		
		tooltip.addSectionHeading("Reload capacity", Alignment.MID, opad);
		tooltip.addPara("Determined by ship size and number of small missile "
				+ "slots, both filled and empty. "
				+ "Having fewer of these simplifies the task and "
				+ "increases the number of possible reloads.", opad);
		
		if (isForModSpec || ship == null) return;
		
		tooltip.setBgAlpha(0.9f);
		
		List<WeaponAPI> weapons = new ArrayList<WeaponAPI>();
		Set<String> seen = new LinkedHashSet<String>();
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (!isAffected(w)) continue;
			String id = w.getId();
			if (seen.contains(id)) continue;
			seen.add(id);
			weapons.add(w);
		}
		
		float numW = 130f;
		float reloadW = 130f;
		float sizeW = width - numW - reloadW - 10f;
		tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
				   20f, true, true, 
				   new Object [] {"Ship size", sizeW, "Small missiles", numW, "Reload capacity", reloadW});
		
		ReloadCapacityData cap = getCapacityData(ship);
		
		List<ReloadCapacityData> sortedCap = new ArrayList<ReloadCapacityData>(CAPACITY_DATA);
		Collections.sort(sortedCap, new Comparator<ReloadCapacityData>() {
			public int compare(ReloadCapacityData o1, ReloadCapacityData o2) {
				//return (int) Math.signum(o1.capacity - o2.capacity);
				if (o1.size != o2.size) {
					return (int) Math.signum(o1.size.ordinal() - o2.size.ordinal());
				}
				return (int) Math.signum(o1.capacity - o2.capacity);
			}
		});
		//sortedCap = new ArrayList<ReloadCapacityData>(CAPACITY_DATA);
		
		HullSize prev = HullSize.FRIGATE;
		for (ReloadCapacityData curr : sortedCap) {
			Color c = Misc.getGrayColor();
			if (cap == curr) {
				c = Misc.getHighlightColor();
			}
			if (curr.size != hullSize) continue;
//			if (prev != curr.size) {
//				tooltip.addRow("", "", "");
//			}
			tooltip.addRow(Alignment.MID, c, curr.getSizeStr(),
						   Alignment.MID, c, curr.getWeaponsString(),
						   Alignment.MID, c, "" + curr.capacity);
			prev = curr.size;
		}
		tooltip.addTable("", 0, opad);
		
		
		Collections.sort(weapons, new Comparator<WeaponAPI>() {
			public int compare(WeaponAPI o1, WeaponAPI o2) {
				float c1 = getReloadCost(o1, ship);
				float c2 = getReloadCost(o2, ship);
				return (int) Math.signum(c1 - c2);
			}
		});
		
		
		tooltip.addSectionHeading("Reload cost", Alignment.MID, opad + 5f);
		
		float costW = 100f;
		float nameW = width - costW - 5f;
		tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
						   20f, true, true, 
						   new Object [] {"Affected weapon", nameW, "Reload cost", costW});
		int max = 10;
		int count = 0;
		for (WeaponAPI w : weapons) {
			count++;
			float cost = getReloadCost(w, ship);
			String name = tooltip.shortenString(w.getDisplayName(), nameW - 20f);
			tooltip.addRow(Alignment.LMID, Misc.getTextColor(), name,
						   Alignment.MID, h, Misc.getRoundedValueOneAfterDecimalIfNotWhole(cost));
			if (count >= max) break;
		}
		tooltip.addTable("No affected weapons mounted", weapons.size() - max, opad);

		//tooltip.addPara("Weapons are reloaded, out of the shared pool of reload capacity, when they run out of ammo.", opad);
		tooltip.addPara("A partial reload is possible when running out of capacity.", opad);
//		tooltip.addPara("A partial reload is possible when running out of capacity "
//				+ "and the number of missiles loaded will be rounded up to the "
//				+ "nearest multiple of the weapon's salvo size.", opad);
		if (BASIC_COOLDOWN > 0) {
			tooltip.addPara("After a reload, the weapon requires an extra %s seconds,"
					+ " in addition to its normal cooldown, before it can fire again.", opad,
					h, "" + (int) BASIC_COOLDOWN);
		}
	}


	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return getCapacityData(ship) != null;
	}


	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		return "Ship does not have any small missile slots";
	}

	@Override
	public boolean isSModEffectAPenalty() {
		return true;
	}

	@Override
	public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int) SMOD_COOLDOWN;
		return null;
	}
	
	
}











