package com.fs.starfarer.api.impl.campaign.ghosts;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamParams2;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamSegment;
import com.fs.starfarer.api.util.Misc;

public class GBIGenerateSlipstream extends BaseGhostBehaviorInterrupt implements EveryFrameScript {
	
	public static interface GhostBehaviorWithSlipstream {
		void setSlipstream(SlipstreamTerrainPlugin2 plugin);
	}

	protected boolean addedScript = false;
	protected float minWidth;
	protected float maxWidth;
	protected int burnLevel;
	protected SensorGhost ghost;
	protected GhostBehavior behavior;
	protected float widenRate = 50f;
	protected int maxSegments = 20;
	protected float duration;
	public GBIGenerateSlipstream(float minWidth, float maxWidth, int burnLevel, float widenRate, int maxSegments, float duration) {
		super(0f);
		this.minWidth = minWidth;
		this.maxWidth = maxWidth;
		this.burnLevel = burnLevel;
		this.widenRate = widenRate;
		this.maxSegments = maxSegments;
		this.duration = duration;
	}

	@Override
	public boolean shouldInterruptBehavior(SensorGhost ghost, GhostBehavior behavior) {
		return false;
	}
	
	public void advance(float amount, SensorGhost ghost, GhostBehavior behavior) {
		this.behavior = behavior;
		if (!addedScript) {
			if (ghost != null && ghost.getEntity() != null && ghost.getEntity().getContainingLocation() != null) {
				this.ghost = ghost;
				//ghost.getEntity().getContainingLocation().addScript(this);
				ghost.getEntity().addScript(this);
				ghost.getEntity().addTag(Tags.UNAFFECTED_BY_SLIPSTREAM);
			}
			addedScript = true;
		}
	}

	
	public boolean isDone() {
		return duration <= 0;
	}

	public boolean runWhilePaused() {
		return false;
	}

	protected CampaignTerrainAPI slipstream = null;
	protected SlipstreamTerrainPlugin2 plugin = null;
	protected Vector2f prev = null;
	
	
	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		duration -= days;
		if (ghost.getEntity() == null || ghost.getEntity().getContainingLocation() == null || duration <= 0f) {
			if (!plugin.isDespawning()) {
				plugin.despawn(0f, 1f + Misc.random.nextFloat(), Misc.random);
			}
			return;
		}
		Vector2f loc = ghost.getEntity().getLocation();
		boolean forceAdd = false;
		if (slipstream == null) {
			SlipstreamParams2 params = new SlipstreamParams2();
			
			params.burnLevel = burnLevel;
			params.widthForMaxSpeed = minWidth + widenRate * 5f;
			//params.widthForMaxSpeedMaxMult = 1f; // no faster than base burn, otherwise will catch up with ghost too easily
			params.minSpeed = Misc.getSpeedForBurnLevel(params.burnLevel - 5);
			params.maxSpeed = Misc.getSpeedForBurnLevel(params.burnLevel + 5);
			params.lineLengthFractionOfSpeed = 0.25f * Math.max(0.25f, Math.min(1f, 30f / (float) params.burnLevel));
			
			//slipstream = (CampaignTerrainAPI) Global.getSector().getCurrentLocation().addTerrain(Terrain.SLIPSTREAM, params);
			slipstream = (CampaignTerrainAPI) ghost.getEntity().getContainingLocation().addTerrain(Terrain.SLIPSTREAM, params);
			slipstream.setLocation(loc.x, loc.y);
			plugin = (SlipstreamTerrainPlugin2) slipstream.getPlugin();
			plugin.setDynamic(true);
			prev = new Vector2f(loc);
			forceAdd = true;
			
			if (behavior instanceof GhostBehaviorWithSlipstream) {
				GhostBehaviorWithSlipstream b = (GhostBehaviorWithSlipstream) behavior;
				b.setSlipstream(plugin);
			}
		}
		//ghost.getMovement().getLocation()
		
		//System.out.println("Location: [" + (int)ghost.getEntity().getLocation().x + "," + (int)ghost.getEntity().getLocation().y + "] (from GBIGenerateSlipstream)");
		
		float distPerSegment = 300f;
		
		float dist = Misc.getDistance(loc, prev);
		if (dist >= distPerSegment || forceAdd) {
			
//			if (plugin.getSegments().size() > 0 &&
//					plugin.getSegments().get(plugin.getSegments().size() - 1).loc.y < loc.y) {
//				System.out.println("ADDING BAD STREAM SEGMENT at [" + (int)loc.x + ", " + (int)loc.y + "] (from GBIGenerateSlipstream)");
//			} else {
//				System.out.println("ADDING STREAM SEGMENT at [" + (int)loc.x + ", " + (int)loc.y + "] (from GBIGenerateSlipstream)");				
//			}
			
			float width = minWidth + (maxWidth - minWidth) * Misc.random.nextFloat();
			plugin.addSegment(loc, width);
			prev = new Vector2f(loc);
			
			List<SlipstreamSegment> segments = plugin.getSegments();
			if (segments.size() == 1) {
				segments.get(0).bMult = 0f;
				segments.get(0).fader.forceOut();
			} else {
				segments.get(segments.size() - 1).fader.forceOut();
				//segments.get(segments.size() - 1).bMult = 0.67f;
			}
			
			List<SlipstreamSegment> remove = new ArrayList<SlipstreamSegment>();
			for (int i = 0; i < segments.size() - maxSegments - 1; i++) { 
				SlipstreamSegment curr = segments.get(i);
				SlipstreamSegment next = segments.get(i + 1);
				curr.fader.setDurationOut(3f);
				curr.fader.fadeOut();
				if (curr.fader.isFadedOut() && next.fader.isFadedOut()) {
					remove.add(curr);
				}
			}
			// don't do it - messes up texture offsets
			//segments.removeAll(remove);
			plugin.recompute();
		}
		
		
		List<SlipstreamSegment> segments = plugin.getSegments();
		float fadeInDist = Math.min(minWidth * 4f, distPerSegment * maxSegments / 4f);
		for (int i = Math.max(0, segments.size() - maxSegments); i < segments.size() - 1; i++) {
			SlipstreamSegment curr = segments.get(i);
			SlipstreamSegment next = segments.get(i + 1);
			
			if (!curr.fader.isFadedOut()) {
				dist = Misc.getDistance(ghost.getEntity().getLocation(), curr.loc);
				float b = dist / fadeInDist;
				if (b < 0) b = 0;
				if (b > 1) b = 1;
				curr.bMult = b;
			}
			
			
			if (next.fader.getBrightness() == 0 && !next.fader.isFadingOut()) {
				float durIn = distPerSegment / Math.max(ghost.getEntity().getVelocity().length(), 1f);
				if (durIn > 2f) durIn = 2f;
				durIn *= 2f;
				curr.fader.setDurationIn(durIn);
				curr.fader.fadeIn();
			}
			
			curr.width += widenRate * amount;
//			if (curr.width > 100f) {
//				curr.width -= widenRate * amount;
//			}
		}
	}

	
}




