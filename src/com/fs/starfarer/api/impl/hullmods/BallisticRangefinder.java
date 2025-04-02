package com.fs.starfarer.api.impl.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BallisticRangefinder extends BaseHullMod {

	public static float BONUS_MAX_1 = 800;
	public static float BONUS_MAX_2 = 800;
	public static float BONUS_MAX_3 = 900;
	public static float BONUS_SMALL_1 = 100;
	public static float BONUS_SMALL_2 = 100;
	public static float BONUS_SMALL_3 = 200;
	public static float BONUS_MEDIUM_3 = 100;
	
	public static float HYBRID_MULT = 2f;
	public static float HYBRID_BONUS_MIN = 100f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}

	public static WeaponSize getLargestBallisticSlot(ShipAPI ship) {
		if (ship == null) return null;
		WeaponSize largest = null;
		for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			if (slot.isDecorative() ) continue;
			if (slot.getWeaponType() == WeaponType.BALLISTIC) {
				if (largest == null || largest.ordinal() < slot.getSlotSize().ordinal()) {
					largest = slot.getSlotSize();
				}
			}
		}
		return largest;
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		WeaponSize largest = getLargestBallisticSlot(ship);
		if (largest == null) return;
		float small = 0f;
		float medium = 0f;
		float max = 0f;
		switch (largest) {
		case LARGE:
			small = BONUS_SMALL_3;
			medium = BONUS_MEDIUM_3;
			max = BONUS_MAX_3;
			break;
		case MEDIUM:
			small = BONUS_SMALL_2;
			max = BONUS_MAX_2;
			break;
		case SMALL:
			small = BONUS_SMALL_1;
			max = BONUS_MAX_1;
			break;
		}
		
		ship.addListener(new RangefinderRangeModifier(small, medium, max));
	}
	
	public static class RangefinderRangeModifier implements WeaponBaseRangeModifier {
		public float small, medium, max;
		public RangefinderRangeModifier(float small, float medium, float max) {
			this.small = small;
			this.medium = medium;
			this.max = max;
		}
		
		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
//			if (weapon.getSlot() == null || weapon.getSlot().getWeaponType() != WeaponType.BALLISTIC) {
//				return 0f;
//			}
			if (weapon.getSpec() == null) {
				return 0f;
			}
			if (weapon.getSpec().getMountType() != WeaponType.BALLISTIC && 
					weapon.getSpec().getMountType() != WeaponType.HYBRID) {
				return 0f;
			}
			if (weapon.hasAIHint(AIHints.PD)) {
				return 0f;
			}
			
			float bonus = 0;
			if (weapon.getSize() == WeaponSize.SMALL) {
				bonus = small;
			} else if (weapon.getSize() == WeaponSize.MEDIUM) {
				bonus = medium;
			}
			if (weapon.getSpec().getMountType() == WeaponType.HYBRID) {
				bonus *= HYBRID_MULT;
				if (bonus < HYBRID_BONUS_MIN) {
					bonus = HYBRID_BONUS_MIN;
				}
			}
			if (bonus == 0f) return 0f;
			
			float base = weapon.getSpec().getMaxRange();
			if (base + bonus > max) {
				bonus = max - base;
			}
			if (bonus < 0) bonus = 0;
			return bonus;
		}
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		//if (index == 0) return "" + (int)RANGE_PENALTY_PERCENT + "%";
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color t = Misc.getTextColor();
		Color g = Misc.getGrayColor();
		
		WeaponSize largest = getLargestBallisticSlot(ship);
		
//		LabelAPI label = tooltip.addPara("If the largest Ballistic slot on the ship is large:"
//				+ " increases the base range of small weapons in Ballistic slots by %s,"
//				+ " and of medium weapons by %s, up to %s maximum.", opad, h,
//				"" + (int)BONUS_SMALL_3, "" + (int)BONUS_MEDIUM_3, "" + (int)BONUS_MAX_3);
////		label.setHighlight("Ballistic", "base", "Ballistic", "" + (int)BONUS_SMALL_3, "" + (int)BONUS_MEDIUM_3, "" + (int)BONUS_MAX_3);
////		label.setHighlightColors(Misc.MOUNT_BALLISTIC, h, Misc.MOUNT_BALLISTIC, h, h, h);
//		label.setHighlight("" + (int)BONUS_SMALL_3, "" + (int)BONUS_MEDIUM_3, "" + (int)BONUS_MAX_3);
//		label.setHighlightColors(h, h, h);
//		if (largest != null && largest != WeaponSize.LARGE) {
//			label.setColor(Misc.getGrayColor());
//		}
//		
//		label = tooltip.addPara("Otherwise:"
//				+ " increases the base range of small weapons in Ballistic slots by %s,"
//				+ " up to %s maximum.", opad, h,
//				"" + (int)BONUS_SMALL_1, "" + (int)BONUS_MAX_1);
////		label.setHighlight("base", "Ballistic", "" + (int)BONUS_SMALL_1, "" + (int)BONUS_MAX_1);
////		label.setHighlightColors(h, Misc.MOUNT_BALLISTIC, h, h);
//		label.setHighlight("" + (int)BONUS_SMALL_1, "" + (int)BONUS_MAX_1);
//		label.setHighlightColors(h, h);
//		if (largest != null && largest == WeaponSize.LARGE) {
//			label.setColor(Misc.getGrayColor());
//		}	
//
////		if (ship != null) {
////			tooltip.addSectionHeading("Effect on this ship", Alignment.MID, opad);
////		}
//		
//		tooltip.addSectionHeading("Exceptions", Alignment.MID, opad);
//		label = tooltip.addPara("Does not affect point-defense weapons, "
//						+ "or Ballistic weapons in Composite, Hybrid, and Universal slots.", opad);
////		label.setHighlight("Ballistic", "Composite", "Hybrid", "Universal");
////		label.setHighlightColors(Misc.MOUNT_BALLISTIC, Misc.MOUNT_COMPOSITE, Misc.MOUNT_HYBRID, Misc.MOUNT_UNIVERSAL);
//		label.setHighlight("Composite", "Hybrid", "Universal");
//		label.setHighlightColors(Misc.MOUNT_COMPOSITE, Misc.MOUNT_HYBRID, Misc.MOUNT_UNIVERSAL);
//		
//		label = tooltip.addPara("Hybrid weapons in Ballistic slots receive %s the bonus. "
////				+ "In addition, the bonus will be at least %s for all Hybrid weapons in Ballistic slots, including large ones,"
////				+ " subject to the maximum.", opad, h,
//				+ "In addition, the range bonus for all non-PD Hybrid weapons in Ballistic slots will be at least %s, "
//				+ "regardless of size or other factors, but still subject to the maximum.", opad, h,
////				+ "In addition, non-PD Hybrid weapons in Ballistic slots, including large ones, will receive %s bonus range,"
////						+ " subject to the maximum, in cases where other weapons of the same size would receive no bonus.", opad, h,
//				"" + (int)Math.round(HYBRID_MULT) + Strings.X, "" + (int)Math.round(HYBRID_BONUS_MIN));
////		label.setHighlight("Hybrid", "Ballistic", "" + (int)Math.round(HYBRID_MULT) + Strings.X);
////		label.setHighlightColors(Misc.MOUNT_HYBRID, Misc.MOUNT_BALLISTIC, h);
//		label.setHighlight("Hybrid", "" + (int)Math.round(HYBRID_MULT) + Strings.X, "Hybrid", "" + (int)Math.round(HYBRID_BONUS_MIN));
//		label.setHighlightColors(Misc.MOUNT_HYBRID, h, Misc.MOUNT_HYBRID, h);
		
		
		
		tooltip.addPara("Utilizes targeting data from the ship's largest ballistic slot "
				+ "to benefit certain weapons, extending the base range of "
				+ "typical ballistic weapons to match similar but larger weapons. "
				+ "Greatly benefits hybrid weapons. Point-defense weapons are unaffected.",
				opad, h, "ship's largest ballistic slot", "base range", "Greatly benefits hybrid weapons");
		
		//tooltip.addPara("The maximum range is capped, based on the largest slot.", opad);
		tooltip.addPara("The range bonus is based on the size of the largest ballistic slot, "
				+ "and the increased base range is capped, but still subject to other modifiers.", opad);
		
//		tooltip.addPara("Affects small and medium ballistic weapons, and all hybrid weapons. "
//				+ "Point-defense weapons are not affected.", opad, h,
//				"");
				//"small and medium", "hybrid", "Point-defense");
		
		tooltip.addSectionHeading("Ballistic weapon range", Alignment.MID, opad);
		
		
		tooltip.addPara("Affects small and medium ballistic weapons.", opad);
		
		float col1W = 120;
		float colW = (int) ((width - col1W - 12f) / 3f);
		float lastW = colW;
		
		tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
				20f, true, true, 
				new Object [] {"Largest b. slot", col1W, "Small wpn", colW, "Medium wpn", colW, "Range cap", lastW});
		
		Color reallyG = g;
		if (Global.CODEX_TOOLTIP_MODE) {
			g = h;
		}
		
		Color c = null;
		if (largest == WeaponSize.SMALL) c = h;
		else if (largest == WeaponSize.MEDIUM) c = h;
		else c = g;
		
		
		tooltip.addRow(Alignment.MID, c, "Small / Medium",
				Alignment.MID, c, "+" + (int) BONUS_SMALL_1,
				Alignment.MID, reallyG, "---",
				Alignment.MID, c, "" + (int)BONUS_MAX_1);
		
		if (largest == WeaponSize.LARGE) c = h;
		else c = g;
		tooltip.addRow(Alignment.MID, c, "Large",
				Alignment.MID, c, "+" + (int) BONUS_SMALL_3,
				Alignment.MID, c, "+" + (int) BONUS_MEDIUM_3,
				Alignment.MID, c, "" + (int)BONUS_MAX_3);
		
		tooltip.addTable("", 0, opad);

		
		tooltip.addSectionHeading("Hybrid weapon range", Alignment.MID, opad + 7f);
		
		tooltip.addPara("Affects hybrid weapons (those that can fit into both ballistic and energy slots)"
				+ " of all sizes.", opad);
		
		col1W = 120;
		colW = (int) ((width - col1W - lastW - 15f) / 3f);
		
		tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
				20f, true, true, 
				new Object [] {"Largest b. slot", col1W, "Small", colW, "Medium", colW, "Large", colW, "Range cap", lastW});
		
		
		c = null;
		if (largest == WeaponSize.SMALL) c = h;
		else if (largest == WeaponSize.MEDIUM) c = h;
		else c = g;
		tooltip.addRow(Alignment.MID, c, "Small / Medium",
				Alignment.MID, c, "+" + (int) (BONUS_SMALL_1 * HYBRID_MULT),
				Alignment.MID, c, "+" + (int) HYBRID_BONUS_MIN,
				Alignment.MID, c, "+" + (int) HYBRID_BONUS_MIN,
				Alignment.MID, c, "" + (int)BONUS_MAX_1);
		
		if (largest == WeaponSize.LARGE) c = h;
		else c = g;
		tooltip.addRow(Alignment.MID, c, "Large",
				Alignment.MID, c, "+" + (int) (BONUS_SMALL_3 * HYBRID_MULT),
				Alignment.MID, c, "+" + (int) (BONUS_MEDIUM_3 * HYBRID_MULT),
				Alignment.MID, c, "+" + (int) HYBRID_BONUS_MIN,
				Alignment.MID, c, "" + (int)BONUS_MAX_3);
		
		tooltip.addTable("", 0, opad);
		
		
		tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad + 7f);
		tooltip.addPara("Since the base range is increased, this modifier"
				+ " - unlike most other flat modifiers - "
				+ "is increased by percentage modifiers from other hullmods and skills.", opad);
	}
	
	public float getTooltipWidth() {
		return 412f;
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		WeaponSize largest = getLargestBallisticSlot(ship);
		if (ship != null && largest == null) {
			return false;
		}
		return getUnapplicableReason(ship) == null;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		WeaponSize largest = getLargestBallisticSlot(ship);
		if (ship != null && largest == null) {
			return "Ship has no ballistic weapon slots";
		}
		if (ship != null && 
				ship.getHullSize() != HullSize.CAPITAL_SHIP && 
				ship.getHullSize() != HullSize.DESTROYER && 
				ship.getHullSize() != HullSize.CRUISER) {
			return "Can only be installed on destroyer-class hulls and larger";
		}
		return null;
	}
	
}









