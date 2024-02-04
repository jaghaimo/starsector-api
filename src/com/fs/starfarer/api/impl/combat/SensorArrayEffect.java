package com.fs.starfarer.api.impl.combat;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.skills.ElectronicWarfareScript;

public class SensorArrayEffect extends BaseBattleObjectiveEffect {

//	public static final float SENSOR_ARRAY_RANGE_BONUS = 25f;
//	public static final float SENSOR_ARRAY_DAMAGE_BONUS = 10f;
	//public static final float SENSOR_ARRAY_FOG_LIFT_RADIUS = 5000f;
	public static final float SENSOR_ARRAY_FOG_LIFT_RADIUS = 999f;
//	public static final int SENSOR_ARRAY_COMMAND_POINTS = 1;
	
	private List<ShipStatusItem> items = new ArrayList<ShipStatusItem>();
	private String id;
	
	public void init(CombatEngineAPI engine, BattleObjectiveAPI objective) {
		super.init(engine, objective);
		id = "sensor_array_boost_" + objective.toString();
		
//		ShipStatusItem item = new ShipStatusItem(objective.getDisplayName(), 
//				String.format("+%d%% weapon damage",
//						(int) SENSOR_ARRAY_DAMAGE_BONUS), 
//						false);
//		items.add(item);
	}

	public void advance(float amount) {
//		for (ShipAPI ship : engine.getShips()) {
//			//if (ship.isFighter() || ship.isFrigate()) continue;
//			if (ship.getOwner() == objective.getOwner()) {
////				ship.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent(id, SENSOR_ARRAY_RANGE_BONUS);
////				ship.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent(id, SENSOR_ARRAY_RANGE_BONUS);
//				
//				ship.getMutableStats().getBallisticWeaponDamageMult().modifyPercent(id, SENSOR_ARRAY_DAMAGE_BONUS);
//				ship.getMutableStats().getEnergyWeaponDamageMult().modifyPercent(id, SENSOR_ARRAY_DAMAGE_BONUS);
//				ship.getMutableStats().getMissileWeaponDamageMult().modifyPercent(id, SENSOR_ARRAY_DAMAGE_BONUS);				
//				
//			} else {
////				ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify(id);
////				ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify(id);
//				
//				ship.getMutableStats().getBallisticWeaponDamageMult().unmodify(id);
//				ship.getMutableStats().getEnergyWeaponDamageMult().unmodify(id);
//				ship.getMutableStats().getMissileWeaponDamageMult().unmodify(id);
//			}
//		}
//		
//		giveCommandPointsForCapturing(SENSOR_ARRAY_COMMAND_POINTS);
		
		revealArea(SENSOR_ARRAY_FOG_LIFT_RADIUS);
	}


	public String getLongDescription() {
		float min = Global.getSettings().getFloat("minFractionOfBattleSizeForSmallerSide");
		int total = Global.getSettings().getBattleSize();
		int maxPoints = (int)Math.round(total * (1f - min));
		return String.format(
				"+%d%% ECM rating\n" +
				"reduces enemy weapon range\n" +
				"by half of the total ECM rating\n" +
				//"%d%% base maximum reduction\n" + 
				"%d%% maximum reduction\n\n" + 
				//"can be improved by skills\n\n" +
				"+%d bonus deployment points\n" + 
				"up to a maximum of " + maxPoints + " points",
				(int)ElectronicWarfareScript.PER_JAMMER,
				(int)ElectronicWarfareScript.BASE_MAXIMUM,
				getBonusDeploymentPoints());		
//		return String.format(
//				"+%d%% ECM rating\n" +
//				"reduces weapon range for\n" +
//				"side with lower ECM rating\n" +
//				//"%d%% base maximum reduction\n" + 
//				"%d%% maximum reduction\n\n" + 
//				//"can be improved by skills\n\n" +
//				"+%d bonus deployment points\n" + 
//				"up to a maximum of " + maxPoints + " points",
//				(int)ElectronicWarfareScript.PER_JAMMER,
//				(int)ElectronicWarfareScript.BASE_MAXIMUM,
//				getBonusDeploymentPoints());
//		   return String.format(
//				   "command points: +%s\n" +
//				   "reveal area: %d\n" +
//				   "\n" +
//				   //"ship weapon range: +%d%%\n" +
//				   "damage: +%d%%\n",
//				   //"no bonus to fighters\n" +
//				   //"no bonus to frigates",
//				   SENSOR_ARRAY_COMMAND_POINTS,
//				   (int) SENSOR_ARRAY_FOG_LIFT_RADIUS,
//				   (int) SENSOR_ARRAY_DAMAGE_BONUS);
	}
	
	public List<ShipStatusItem> getStatusItemsFor(ShipAPI ship) {
//		if (ship.getOwner() == objective.getOwner()) {
////			if (ship.isFighter()) {
////				return itemsNAFighters;
////			}
////			if (ship.isFrigate()) {
////				return itemsNAFrigates;
////			}
//			return items;
//		}
		return null;
	}
}







