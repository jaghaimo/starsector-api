package com.fs.starfarer.api.impl.hullmods;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.Misc;

public class SharedFluxSink extends BaseHullMod {

	public static float FLUX_FRACTION = 0.5f;
	public static float HARD_FLUX_FRACTION = 0.2f;
	public static String SINK_DATA_KEY = "core_sink_data_key";
	
	
	public static class FluxSinkData {
		Map<ShipAPI, Float> dissipation = new LinkedHashMap<ShipAPI, Float>();
	}
	
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);

		if (!ship.isAlive()) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		FluxSinkData data = (FluxSinkData) engine.getCustomData().get(SINK_DATA_KEY);
		if (data == null) {
			data = new FluxSinkData();
			engine.getCustomData().put(SINK_DATA_KEY, data);
			
			for (ShipAPI module : ship.getChildModulesCopy()) {
				if (module.getStationSlot() == null || !module.isAlive() || !Misc.isActiveModule(module)) continue;
				float d = module.getMutableStats().getFluxDissipation().getModifiedValue();
				d *= FLUX_FRACTION;
				data.dissipation.put(module, d);
			}
		}
		
		
		List<ShipAPI> losses = new ArrayList<ShipAPI>(data.dissipation.keySet());
		List<ShipAPI> remaining = new ArrayList<ShipAPI>();
		float totalLiveDissipation = 0f;
		for (ShipAPI module : ship.getChildModulesCopy()) {
			if (module.getStationSlot() == null || !module.isAlive() || !Misc.isActiveModule(module)) continue;
			losses.remove(module);
			remaining.add(module);
			if (data.dissipation.containsKey(module)) { // always should, but failsafe
				totalLiveDissipation += data.dissipation.get(module);
			} 
		}
		
		float extraDissipation = 0f;
		for (ShipAPI lost : losses) {
			if (data.dissipation.containsKey(lost)) { // always should, but failsafe
				extraDissipation += data.dissipation.get(lost);
			}
		}
		
		for (ShipAPI module : remaining) {
			if (!data.dissipation.containsKey(module)) continue;
		
			float currBonus = 0f;
			if (totalLiveDissipation > 0) {
				currBonus = data.dissipation.get(module) / totalLiveDissipation * extraDissipation; 
			}
			
			module.getMutableStats().getFluxDissipation().modifyFlat("shared_flux_sink", currBonus);
			
			float hardFluxFraction = 0f;
			float totalDissipation = module.getMutableStats().getFluxDissipation().getModifiedValue();
			if (totalDissipation > 0) {
				hardFluxFraction = currBonus / totalDissipation * HARD_FLUX_FRACTION;
			}
			
			module.getMutableStats().getHardFluxDissipationFraction().modifyFlat("shared_flux_sink", hardFluxFraction);
		}
		
	}

	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int) Math.round(FLUX_FRACTION * 100f) + "%";
		if (index == 1) return "" + (int) Math.round(HARD_FLUX_FRACTION * 100f) + "%";
		return null;
	}
}




