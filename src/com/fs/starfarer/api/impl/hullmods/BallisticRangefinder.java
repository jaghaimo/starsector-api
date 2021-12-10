package com.fs.starfarer.api.impl.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
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

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		WeaponSize largest = null;
		for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			if (slot.isDecorative() ) continue;
			if (slot.getWeaponType() == WeaponType.BALLISTIC) {
				if (largest == null || largest.ordinal() < slot.getSlotSize().ordinal()) {
					largest = slot.getSlotSize();
				}
			}
		}
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
			if (weapon.getSlot() == null || weapon.getSlot().getWeaponType() != WeaponType.BALLISTIC) {
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
		
		LabelAPI label = tooltip.addPara("If the largest Ballistic slot on the ship is large:"
				+ " increases the base range of small weapons in Ballistic slots by %s,"
				+ " and of medium weapons by %s, up to %s maximum.", opad, h,
				"" + (int)BONUS_SMALL_3, "" + (int)BONUS_MEDIUM_3, "" + (int)BONUS_MAX_3);
//		label.setHighlight("Ballistic", "base", "Ballistic", "" + (int)BONUS_SMALL_3, "" + (int)BONUS_MEDIUM_3, "" + (int)BONUS_MAX_3);
//		label.setHighlightColors(Misc.MOUNT_BALLISTIC, h, Misc.MOUNT_BALLISTIC, h, h, h);
		label.setHighlight("" + (int)BONUS_SMALL_3, "" + (int)BONUS_MEDIUM_3, "" + (int)BONUS_MAX_3);
		label.setHighlightColors(h, h, h);
		
		label = tooltip.addPara("Otherwise:"
				+ " increases the base range of small weapons in Ballistic slots by %s,"
				+ " up to %s maximum.", opad, h,
				"" + (int)BONUS_SMALL_1, "" + (int)BONUS_MAX_1);
//		label.setHighlight("base", "Ballistic", "" + (int)BONUS_SMALL_1, "" + (int)BONUS_MAX_1);
//		label.setHighlightColors(h, Misc.MOUNT_BALLISTIC, h, h);
		label.setHighlight("" + (int)BONUS_SMALL_1, "" + (int)BONUS_MAX_1);
		label.setHighlightColors(h, h);
				

		tooltip.addSectionHeading("Exceptions", Alignment.MID, opad);
		label = tooltip.addPara("Does not affect point-defense weapons, "
						+ "or Ballistic weapons in Composite, Hybrid, and Universal slots.", opad);
//		label.setHighlight("Ballistic", "Composite", "Hybrid", "Universal");
//		label.setHighlightColors(Misc.MOUNT_BALLISTIC, Misc.MOUNT_COMPOSITE, Misc.MOUNT_HYBRID, Misc.MOUNT_UNIVERSAL);
		label.setHighlight("Composite", "Hybrid", "Universal");
		label.setHighlightColors(Misc.MOUNT_COMPOSITE, Misc.MOUNT_HYBRID, Misc.MOUNT_UNIVERSAL);
		
		label = tooltip.addPara("Hybrid weapons in Ballistic slots receive %s the bonus. "
//				+ "In addition, the bonus will be at least %s for all Hybrid weapons in Ballistic slots, including large ones,"
//				+ " subject to the maximum.", opad, h,
				+ "In addition, non-PD Hybrid weapons in Ballistic slots, including large ones, will receive %s bonus range,"
						+ " subject to the maximum, in cases where other weapons of the same size would receive no bonus.", opad, h,
				"" + (int)Math.round(HYBRID_MULT) + Strings.X, "" + (int)Math.round(HYBRID_BONUS_MIN));
//		label.setHighlight("Hybrid", "Ballistic", "" + (int)Math.round(HYBRID_MULT) + Strings.X);
//		label.setHighlightColors(Misc.MOUNT_HYBRID, Misc.MOUNT_BALLISTIC, h);
		label.setHighlight("Hybrid", "" + (int)Math.round(HYBRID_MULT) + Strings.X, "Hybrid", "" + (int)Math.round(HYBRID_BONUS_MIN));
		label.setHighlightColors(Misc.MOUNT_HYBRID, h, Misc.MOUNT_HYBRID, h);
		
		tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad);
		tooltip.addPara("Since the base range is increased, this modifier"
				+ " - unlike most other flat modifiers in the game - "
				+ "is affected by percentage modifiers from other hullmods and skills.", opad);
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return getUnapplicableReason(ship) == null;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship != null && 
				ship.getHullSize() != HullSize.CAPITAL_SHIP && 
				ship.getHullSize() != HullSize.DESTROYER && 
				ship.getHullSize() != HullSize.CRUISER) {
			return "Can only be installed on destroyer-class hulls and larger";
		}
		return null;
	}
	
}









