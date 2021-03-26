package com.fs.starfarer.api.campaign.listeners;

import java.util.List;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;

public interface MarineLossesStatModifier {
	public void modifyMarineLossesStatPreRaid(MarketAPI market, List<GroundRaidObjectivePlugin> objectives, MutableStat stat);
}
