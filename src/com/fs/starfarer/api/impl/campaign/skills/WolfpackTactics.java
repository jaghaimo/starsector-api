package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.AfterShipCreationSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class WolfpackTactics {
	
	public static float DAMAGE_TO_LARGER_BONUS = 20f;
	public static float DAMAGE_TO_LARGER_BONUS_DEST = 10f;
	public static float PEAK_TIME_BONUS = 50f;
	public static float PEAK_TIME_BONUS_DEST = 25f;
	
//	public static float FLAGSHIP_SPEED_BONUS = 25f;
//	public static float FLAGSHIP_CP_BONUS = 100f;
	
	public static boolean isFrigateAndOfficer(MutableShipStatsAPI stats) {
		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			if (!ship.isFrigate()) return false;
			return !ship.getCaptain().isDefault();
		} else {
			FleetMemberAPI member = stats.getFleetMember();
			if (member == null) return false;
			if (!member.isFrigate()) return false;
			return !member.getCaptain().isDefault();
		}
	}
	
	public static boolean isDestroyerAndOfficer(MutableShipStatsAPI stats) {
		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			if (!ship.isDestroyer()) return false;
			return !ship.getCaptain().isDefault();
		} else {
			FleetMemberAPI member = stats.getFleetMember();
			if (member == null) return false;
			if (!member.isDestroyer()) return false;
			return !member.getCaptain().isDefault();
		}
	}
	
//	public static boolean isFrigateAndFlagship(MutableShipStatsAPI stats) {
//		if (stats.getEntity() instanceof ShipAPI) {
//			ShipAPI ship = (ShipAPI) stats.getEntity();
//			if (!ship.isFrigate()) return false;
//			if (ship.getFleetMember() != null && 
//					ship.getFleetMember().getFleetCommander() == ship.getCaptain()) {
//				return true;
//			}
//			return ship.getCaptain().isPlayer();
//		} else {
//			FleetMemberAPI member = stats.getFleetMember();
//			if (member == null) return false;
//			if (!member.isFrigate()) return false;
//			if (member.isFlagship()) {
//				return true;
//			}
//			return member.getCaptain().isPlayer();
//		}
//	}
//	public static boolean isDestroyerAndFlagship(MutableShipStatsAPI stats) {
//		if (stats.getEntity() instanceof ShipAPI) {
//			ShipAPI ship = (ShipAPI) stats.getEntity();
//			if (!ship.isDestroyer()) return false;
//			if (ship.getFleetMember() != null && 
//					ship.getFleetMember().getFleetCommander() == ship.getCaptain()) {
//				return true;
//			}
//			return ship.getCaptain().isPlayer();
//		} else {
//			FleetMemberAPI member = stats.getFleetMember();
//			if (member == null) return false;
//			if (!member.isDestroyer()) return false;
//			if (member.isFlagship()) {
//				return true;
//			}
//			return member.getCaptain().isPlayer();
//		}
//	}
	
	public static class Level1A implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (isFrigateAndOfficer(stats)) {
				stats.getDamageToDestroyers().modifyPercent(id, DAMAGE_TO_LARGER_BONUS);
				stats.getDamageToCruisers().modifyPercent(id, DAMAGE_TO_LARGER_BONUS);
				stats.getDamageToCapital().modifyPercent(id, DAMAGE_TO_LARGER_BONUS);
				
				stats.getPeakCRDuration().modifyPercent(id, PEAK_TIME_BONUS);
				//stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 1000f);
			} else if (isDestroyerAndOfficer(stats)) {
				stats.getDamageToCruisers().modifyPercent(id, DAMAGE_TO_LARGER_BONUS_DEST);
				stats.getDamageToCapital().modifyPercent(id, DAMAGE_TO_LARGER_BONUS_DEST);
				
				stats.getPeakCRDuration().modifyPercent(id, PEAK_TIME_BONUS_DEST);
				//stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 1000f);
			}
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDamageToDestroyers().unmodifyPercent(id);
			stats.getDamageToCruisers().unmodifyPercent(id);
			stats.getDamageToCapital().unmodifyPercent(id);
			
			stats.getPeakCRDuration().unmodifyPercent(id);
			//stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			//return "+" + (int)Math.round(DAMAGE_TO_LARGER_BONUS) + "% damage to ships larger than frigates";
			
			return "+" + (int)Math.round(DAMAGE_TO_LARGER_BONUS) + "% damage to ships larger than frigates if frigate, " +
				   "+" + (int)Math.round(DAMAGE_TO_LARGER_BONUS_DEST) + "% damage to capital ships and cruisers if destroyer\n" +
				   "+" + (int)(PEAK_TIME_BONUS) + "% seconds peak operating time if frigate, " + 
				   "+" + (int)(PEAK_TIME_BONUS_DEST) + "% if destroyer";
//			return "+" + (int)Math.round(DAMAGE_TO_LARGER_BONUS) + "% damage to ships larger than frigates\n" +
//			"+" + (int)(PEAK_TIME_BONUS) + " seconds peak operating time\n" +
//			"If lost in combat, ship is almost always recoverable";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	
//	public static class Level1B extends BaseSkillEffectDescription implements ShipSkillEffect {
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			if (isFrigateAndFlagship(stats)) {
//				stats.getDynamic().getMod(Stats.COMMAND_POINT_RATE_FLAT).modifyFlat(id, FLAGSHIP_CP_BONUS * 0.01f);
//			} else if (isDestroyerAndFlagship(stats)) {
//				stats.getZeroFluxSpeedBoost().modifyFlat(id, FLAGSHIP_SPEED_BONUS);
//			}
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getDynamic().getMod(Stats.COMMAND_POINT_RATE_FLAT).unmodify(id);
//			stats.getZeroFluxSpeedBoost().unmodifyFlat(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return null;
//			//return "\n+" + (int) DESTROYER_CP_BONUS + "% to command point recovery rate if flagship is a destroyer";
//		}
//		
//		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
//				TooltipMakerAPI info, float width) {
//			init(stats, skill);
//
//			float opad = 10f;
//			Color c = Misc.getBasePlayerColor();
//			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "flagship");
//			info.addSpacer(opad);
//			info.addPara("+%s to command point recovery rate if flagship is a frigate", 0f, hc, hc,
//				     "" + (int) FLAGSHIP_CP_BONUS + "%");
//			info.addPara("+%s to 0-flux speed boost if flagship is a destroyer", 0f, hc, hc,
//					"" + (int) FLAGSHIP_SPEED_BONUS + "");
//
//			//info.addSpacer(5f);
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//		
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.PILOTED_SHIP;
//		}
//	}
	
	
	//public static class Level1C extends BaseSkillEffectDescription implements ShipSkillEffect, AfterShipCreationSkillEffect {
	public static class Level1C extends BaseSkillEffectDescription implements AfterShipCreationSkillEffect {
		public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
//			MutableShipStatsAPI stats = ship.getMutableStats();
//			Global.getCombatEngine().getListenerManager().addListener(listener);
//			if (isDmoddedAndOfficer(stats)) {
//				ship.addListener(new DCDamageTakenMod(ship));
//				
//				float dmods = DModManager.getNumDMods(ship.getVariant());
//				if (dmods <= 0) return;
//				if (dmods > MAX_DMODS) dmods = MAX_DMODS;
//				
//				if (ship.getShield() == null) {
//					stats.getMinArmorFraction().modifyFlat(id, SHIELDLESS_ARMOR_BONUS_PER_DMOD * dmods);
//				}
//			}
		}

		public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
//			ship.removeListenerOfClass(DCDamageTakenMod.class);
//			
//			if (ship.getShield() == null) {
//				MutableShipStatsAPI stats = ship.getMutableStats();
//				stats.getMinArmorFraction().unmodifyFlat(id);
//			}
		}
		
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
				TooltipMakerAPI info, float width) {
//			init(stats, skill);

//			info.addPara("%s chance per d-mod* to have incoming hull damage reduced by %s", 0f, hc, hc,
//					"" + (int)AVOID_DAMAGE_CHANCE_PER_DMOD + "%",
//					"" + (int)Math.round((1f - AVOID_DAMAGE_DAMAGE_MULT) * 100f) + "%"
//			);
//			info.addPara("%s crew lost due to hull damage in combat per d-mod*", 0f, hc, hc,
//					"-" + (int)CREW_LOSS_REDUCTION_PER_DMOD + "%"
//			);
//			info.addPara("%s maximum combat readiness per d-mod*", 0f, hc, hc,
//					"+" + (int)CR_PER_DMOD + "%"
//			);
//			
//			info.addSpacer(5f);
//			info.addPara("%s minimum armor value** for damage reduction per d-mod for unshielded ships", 0f, hc, hc,
//					"+" + (int)Math.round(SHIELDLESS_ARMOR_BONUS_PER_DMOD * 100f) + "%"
//			);
//			
		}

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
		}
	}
	
	/*
	public static class WolfpackExtraDamageMod implements DamageDealtModifier, AdvanceableListener {
		protected int owner;
		public WolfpackExtraDamageMod(int owner) {
			this.owner = owner;
		}
		
		protected boolean addDamage = false;
		protected boolean addDamageAlly = false;
		public void advance(float amount) {
			CombatFleetManagerAPI cfm = Global.getCombatEngine().getFleetManager(owner);
			for (DeployedFleetMemberAPI dfm : cfm.getDeployedCopyDFM()) {
				FleetMemberAPI member = dfm.getMember();
				if (member == null) continue;
				PersonAPI commander = member.getFleetCommanderForStats();
				if (commander == null) continue;
				
				ShipAPI ship = cfm.getShipFor(commander);

			}
		}
		
		public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
			return null;
		}

		public String modifyDamageTaken(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			MutableShipStatsAPI stats = ship.getMutableStats();
			stats.getHullDamageTakenMult().unmodifyMult(DAMAGE_MOD_ID);
			
			if (shieldHit) return null;

			float chance = stats.getDynamic().getMod(AVOID_HULL_DAMAGE_CHANCE).computeEffective(0f);
			if (Math.random() >= chance) {
				return null;
			}
			
			stats.getHullDamageTakenMult().modifyMult(DAMAGE_MOD_ID, AVOID_DAMAGE_DAMAGE_MULT);
			
			return null;
		}
	}
	*/


}
