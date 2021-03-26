package com.fs.starfarer.api.impl.combat;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.skills.CoordinatedManeuversScript;

public class NavBuoyEffect extends BaseBattleObjectiveEffect {

//	public static final String NEBULA_NAVIGATION_KEY = "nebula_navigation";
	
	
//	public static final String NAV_BUOY_ID = "nav_buoy";
//	public static final float NAV_BUOY_TOP_SPEED_BONUS = 15f;
//	public static final float NAV_BUOY_MANEUVERABILITY_BONUS = 25f;
//	public static final int NAV_BUOY_COMMAND_POINTS = 1;
	
	public static final float NAV_BUOY_FOG_LIFT_RADIUS = 999f;
	
	private List<ShipStatusItem> items = new ArrayList<ShipStatusItem>();

	private String id;
	
	public void init(CombatEngineAPI engine, BattleObjectiveAPI objective) {
		super.init(engine, objective);
		
		id = "nav_buoy_boost_" + objective.toString();
		
//		ShipStatusItem item = new ShipStatusItem(objective.getDisplayName(), 
//				String.format("+%d top speed, +%d%% maneuver",
//					(int) NAV_BUOY_TOP_SPEED_BONUS,
//					(int) NAV_BUOY_MANEUVERABILITY_BONUS), 
//				false);
//		items.add(item);
		
//		item = new ShipStatusItem("Nav Buoy Deployed", 
//								"nebula navigation",
//								false);
//		item.setKey(NEBULA_NAVIGATION_KEY);
//		items.add(item);		
	}

	public void advance(float amount) {
//		boolean zeroHasNav = false;
//		boolean oneHasNav = false;
//		for (BattleObjectiveAPI obj : engine.getObjectives()) {
//			if (obj.getOwner() == 0 && NAV_BUOY_ID.equals(obj.getType())) {
//				zeroHasNav = true;
//			}
//			if (obj.getOwner() == 1 && NAV_BUOY_ID.equals(obj.getType())) {
//				oneHasNav = true;
//			}
//		}
//		
//		for (ShipAPI ship : engine.getShips()) {
//			//if (ship.isFighter() || ship.isFrigate()) continue;
//			if (ship.getOwner() == objective.getOwner()) {
//				ship.getMutableStats().getMaxSpeed().modifyFlat(id, NAV_BUOY_TOP_SPEED_BONUS);
//				ship.getMutableStats().getAcceleration().modifyPercent(id, NAV_BUOY_MANEUVERABILITY_BONUS);
//				ship.getMutableStats().getDeceleration().modifyPercent(id, NAV_BUOY_MANEUVERABILITY_BONUS);
//				ship.getMutableStats().getTurnAcceleration().modifyPercent(id,NAV_BUOY_MANEUVERABILITY_BONUS);
//			} else {
//				ship.getMutableStats().getMaxSpeed().unmodify(id);
//				ship.getMutableStats().getAcceleration().unmodify(id);
//				ship.getMutableStats().getDeceleration().unmodify(id);
//				ship.getMutableStats().getTurnAcceleration().unmodify(id);
//			}
////			if ((ship.getOwner() == 0 && zeroHasNav) || (ship.getOwner() == 1 && oneHasNav)) {
////				ship.setAffectedByNebula(false);
////			} else {
////				ship.setAffectedByNebula(true);
////			}
//		}
		
//		giveCommandPointsForCapturing(NAV_BUOY_COMMAND_POINTS);
		
		revealArea(NAV_BUOY_FOG_LIFT_RADIUS);
	}


	public String getLongDescription() {
		float min = Global.getSettings().getFloat("minFractionOfBattleSizeForSmallerSide");
		int total = Global.getSettings().getBattleSize();
		int maxPoints = (int)Math.round(total * (1f - min));
		return String.format(
				"+%d%% top speed\n" +
				"%d%% base total maximum\n" + 
				//"can be improved by skills\n\n" +
				"+%d bonus deployment points\n" + 
				"up to a maximum of " + maxPoints + " points",
				(int)CoordinatedManeuversScript.PER_BUOY,
				(int)CoordinatedManeuversScript.BASE_MAXIMUM,
				getBonusDeploymentPoints());
//		   return String.format(
//				   "command points: +%d\n" +
//				   "\n" +
//				   "ship maneuverability: +%d%%\n" +
//				   "ship top speed: +%d\n",
//				   //"ships unaffected by nebula interference\n",
//				   //"no bonus to fighters\n" +
//				   //"no bonus to frigates",
//				   NAV_BUOY_COMMAND_POINTS,
//				   (int) NAV_BUOY_TOP_SPEED_BONUS,
//				   (int) NAV_BUOY_MANEUVERABILITY_BONUS);
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







