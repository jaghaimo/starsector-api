package com.fs.starfarer.api.impl.combat;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.skills.CommRelayScript;

public class CommRelayEffect extends BaseBattleObjectiveEffect {

	public static final float COMM_RELAY_FOG_LIFT_RADIUS = 999f;
	public static final int COMM_RELAY_COMMAND_POINTS = 3;
	
	private CombatEngineAPI engine;
	//private List<ShipStatusItem> items = new ArrayList<ShipStatusItem>();

	private String id;
	private BattleObjectiveAPI objective;
	
	public void init(CombatEngineAPI engine, BattleObjectiveAPI objective) {
		super.init(engine, objective);
		this.engine = engine;
		this.objective = objective;
		id = "sensor_array_boost_" + objective.toString();
		
//		ShipStatusItem item = new ShipStatusItem("Comm Relay", 
//				String.format("+%d%% weapon range",
//					(int) SENSOR_ARRAY_RANGE_BONUS), 
//				false);
//		items.add(item);
	}

	public void advance(float amount) {
//		for (ShipAPI ship : engine.getShips()) {
//			if (ship.getOwner() == objective.getOwner()) {
//			} else {
//			}
//		}
		
		//giveCommandPointsForCapturing(COMM_RELAY_COMMAND_POINTS);
		revealArea(COMM_RELAY_FOG_LIFT_RADIUS);
	}


	public String getLongDescription() {
		float min = Global.getSettings().getFloat("minFractionOfBattleSizeForSmallerSide");
		int total = Global.getSettings().getBattleSize();
		int maxPoints = (int)Math.round(total * (1f - min));
		return String.format(
				   "" + (int) CommRelayScript.RATE_BONUS_PER_COMM_RELAY + "%% faster command point recovery\n\n" +
					"+%d bonus deployment points\n" + 
					"up to a maximum of " + maxPoints + " points",
					getBonusDeploymentPoints()
				   );
	}
	
	public List<ShipStatusItem> getStatusItemsFor(ShipAPI ship) {
//		if (ship.getOwner() == objective.getOwner()) {
//			return items;
//		}
		return null;
	}
}







