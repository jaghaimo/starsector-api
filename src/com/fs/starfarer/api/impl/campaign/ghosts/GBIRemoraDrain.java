package com.fs.starfarer.api.impl.campaign.ghosts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.CampaignPingSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;


public class GBIRemoraDrain extends BaseGhostBehaviorInterrupt {
	
	//public static float REMORA_RANGE = 1000f;
	
	protected SectorEntityToken target;
	protected float drainRange;
	protected IntervalUtil tracker = new IntervalUtil(5f, 10f);
	
	public GBIRemoraDrain(SectorEntityToken target, float drainRange) {
		super(0f);
		this.target = target;
		this.drainRange = drainRange;
	}
	
	
	@Override
	public void advance(float amount, SensorGhost ghost, GhostBehavior behavior) {
		super.advance(amount, ghost, behavior);
		
		tracker.advance(amount);
		if (tracker.intervalElapsed()) {
			
			CustomCampaignEntityAPI entity = ghost.getEntity();
			
			CampaignPingSpec custom = new CampaignPingSpec();
			//custom.setUseFactionColor(true);
			custom.setWidth(15);
			custom.setRange(drainRange * 1.3f);
//			custom.setRange(drainRange * 1.3f);
//			custom.setMinRange(entity.getRadius());
//			custom.setInvert(true);
			custom.setDuration(0.5f);
			custom.setAlphaMult(1f);
			custom.setInFraction(0.1f);
			custom.setNum(1);
			custom.setColor(new Color(255, 100, 100, 255));
			Global.getSector().addPing(entity, custom);
//			Color color = custom.getColor();
//			Misc.addHitGlow(entity.getContainingLocation(), entity.getLocation(), entity.getVelocity(), 
//					entity.getRadius() * 3f + 100f, 0.5f, color);
			
			if (ghost.getEntity().isInCurrentLocation()) {
				Global.getSoundPlayer().playSound("ghost_remora_hit", 1f, 1f, 
						ghost.getEntity().getLocation(), ghost.getEntity().getVelocity());
			}
			
			List<SectorEntityToken> list = new ArrayList<SectorEntityToken>(entity.getContainingLocation().getFleets());
			list.addAll(entity.getContainingLocation().getCustomEntities());
			//list.add(target);
			for (SectorEntityToken other : list) {
				if (other.hasTag(Tags.IMMUNE_TO_REMORA_PULSE)) continue;
				float dist = Misc.getDistance(entity, other) - entity.getRadius() - other.getRadius();
				if (dist < drainRange) {
					if (other.isPlayerFleet()) {
						other.addFloatingText("Drive field drain!", Misc.getNegativeHighlightColor(), 1f, true);
					}
					float mult = 0.5f + 0.5f * (1f - dist / drainRange);
					if (other instanceof CampaignFleetAPI) {
						mult *= 2f;
					}
					other.addScript(new SpeedReduction(other, 0.5f * mult, 0.5f));
				}
			}
		}
	}
	
}













