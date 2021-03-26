package com.fs.starfarer.api.impl.campaign.missions.cb;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;

public class CBMercUW extends CBMerc {
	@Override
	public float getBountyDays() {
		return 60f;
	}
	

	protected StarSystemAPI findSystem(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
//		mission.requireSystemTags(ReqMode.ANY, Tags.THEME_RUINS, Tags.THEME_MISC, Tags.THEME_REMNANT_SECONDARY,
//								  Tags.THEME_DERELICT, Tags.THEME_REMNANT_DESTROYED);
		// allow core systems
		mission.requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_UNSAFE);
		mission.requireSystemNotHasPulsar();
		mission.preferSystemInInnerSector();

		StarSystemAPI system = mission.pickSystem();
		return system;		
	}

	protected boolean isAggro() {
		return false;
	}
}
