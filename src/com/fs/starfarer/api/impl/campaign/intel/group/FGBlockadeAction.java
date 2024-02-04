package com.fs.starfarer.api.impl.campaign.intel.group;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.ActionType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript.MilitaryResponseParams;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI.GenericPayloadAction;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class FGBlockadeAction extends FGDurationAction implements GenericPayloadAction {

	public static class FGBlockadeParams {
		public StarSystemAPI where;
		public MarketAPI specificMarket = null;
		public boolean doNotGetSidetracked = true;
		//public float duration = 365f;
		public float accessibilityPenalty = 0.6f;

		public String patrolText = null;
		public String targetFaction = Factions.PLAYER;
	}
	
	
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);
	
	protected FGBlockadeParams params;
	protected List<SectorEntityToken> blockadePoints = new ArrayList<SectorEntityToken>();
	protected SectorEntityToken primary;
	
	protected boolean computedInitial = false;
	protected float origDuration = 1f;
	protected List<MilitaryResponseScript> scripts = new ArrayList<MilitaryResponseScript>();

	public FGBlockadeAction(FGBlockadeParams params, float duration) {
		super(duration);
		origDuration = duration;
		this.params = params;
		
		interval.forceIntervalElapsed();
	}
	
	public Object readResolve() {
		return this;
	}
	
	
	public void computeInitial() {
		computedInitial = true;
		
		blockadePoints = new ArrayList<SectorEntityToken>(params.where.getEntities(JumpPointAPI.class));
		
		final Vector2f center = new Vector2f();
		
		Collections.sort(blockadePoints, new Comparator<SectorEntityToken>() {
			public int compare(SectorEntityToken o1, SectorEntityToken o2) {
				float d1 = Misc.getDistance(center, o1.getLocation());
				float d2 = Misc.getDistance(center, o2.getLocation());
				return (int) Math.signum(d1 - d2);
			}
		});
		
		if (blockadePoints.size() == 1) {
			primary = blockadePoints.get(0);
		} else if (blockadePoints.size() >= 3) {
			primary = blockadePoints.get(1);
		} else if (blockadePoints.size() == 2) {
			float d0 = Misc.getDistance(center, blockadePoints.get(0).getLocation());
			float d1 = Misc.getDistance(center, blockadePoints.get(1).getLocation());
			if (d0 > 3000) {
				primary = blockadePoints.get(0);
			} else {
				primary = blockadePoints.get(1);
			}
		}
		
		// otherwise, WasSimScript adds extra MilitaryResponseScripts for objectives and
		// attacking fleets go there almost to the exclusion of other targets
		for (SectorEntityToken objective : params.where.getEntitiesWithTag(Tags.OBJECTIVE)) {
			WarSimScript.setNoFightingForObjective(objective, intel.getFaction(), 1000f);
		}
		
	}
	

	@Override
	public void addRouteSegment(RouteData route) {
		RouteSegment segment = new RouteSegment(getDurDays(), params.where.getCenter());
		route.addSegment(segment);
	}

	@Override
	public void notifyFleetsSpawnedMidSegment(RouteSegment segment) {
		super.notifyFleetsSpawnedMidSegment(segment);
	}

	@Override
	public void notifySegmentFinished(RouteSegment segment) {
		super.notifySegmentFinished(segment);
		
		//autoresolve();
	}
	
	@Override
	public void setActionFinished(boolean finished) {
		if (finished && !this.finished) {
			List<CampaignFleetAPI> fleets = intel.getFleets();
			for (CampaignFleetAPI fleet : fleets) {
				Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.FLEET_BUSY, fleet.getId(), true, -1f);
			}
			
			if (scripts != null) {
				for (MilitaryResponseScript s : scripts) {
					s.forceDone();
				}
				scripts.clear();
			}
			
			for (SectorEntityToken objective : params.where.getEntitiesWithTag(Tags.OBJECTIVE)) {
				WarSimScript.removeNoFightingTimeoutForObjective(objective, intel.getFaction());
			}
		}
		super.setActionFinished(finished);
	}
	

	@Override
	public void directFleets(float amount) {
		super.directFleets(amount);
		if (isActionFinished()) return;
		
		List<CampaignFleetAPI> fleets = intel.getFleets();
		if (fleets.isEmpty()) {
			setActionFinished(true);
			return;
		}
		
		if (!computedInitial) {
			computeInitial();
			orderFleetMovements();
		}
		
		if (primary == null) {
			setActionFinished(true);
			return;
		}
		
		float days = Global.getSector().getClock().convertToDays(amount);
		
		interval.advance(days);
		if (!interval.intervalElapsed()) return;

		// doing this would cause the fleets to:
		// 1) not hassle the player
		// 2) not attack nearby enemies unless they bump into them
		
//		for (CampaignFleetAPI fleet : fleets) {		
//			if (params.doNotGetSidetracked) {
//				boolean battleNear = false;
//				for (CampaignFleetAPI other : fleets) {
//					if (other == fleet || other.getBattle() == null) continue;
//					if (other.getContainingLocation() != fleet.getContainingLocation());
//					float dist = Misc.getDistance(fleet, other);
//					if (dist < 1000) {
//						CampaignFleetAIAPI ai = fleet.getAI();
//						if (ai != null && ai.wantsToJoin(other.getBattle(), other.getBattle().isPlayerInvolved())) {
//							battleNear = true;
//							break;
//						}
//					}
//				}
//				if (!battleNear) {
//					fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED, true, 0.4f);
//				}
//			}
//		}
	}

	
	protected void orderFleetMovements() {
		if (blockadePoints.isEmpty() || primary == null) {
			return;
		}
		
		float fPrimary = 1f;
		float fNonPrimary = 1f / blockadePoints.size();
		
		for (CampaignFleetAPI fleet : intel.getFleets()) {
			fleet.getMemoryWithoutUpdate().unset(MemFlags.FLEET_BUSY);
		}
		
		
		for (SectorEntityToken target : blockadePoints) {
			float rf = fNonPrimary;
			if (target == primary) rf = fPrimary;
			MilitaryResponseParams aggroParams = new MilitaryResponseParams(ActionType.HOSTILE, 
					"blockade_" + target.getId(), 
					intel.getFaction(),
					target,
					rf,
					getDurDays());
			aggroParams.travelText = null; 
			aggroParams.actionText = params.patrolText;
			
			MilitaryResponseScript script = new MilitaryResponseScript(aggroParams);
			params.where.addScript(script);
			scripts.add(script);
		}
	}
	
	public FGBlockadeParams getParams() {
		return params;
	}
	


	public float getSuccessFraction() {
		float f = getElapsed() / Math.max(1f, origDuration);
		if (f < 0f) f = 0f;
		if (f > 1f) f = 1f;
		return f;
	}
	
	public Color getSystemNameHighlightColor() {
		return Global.getSector().getFaction(params.targetFaction).getBaseUIColor();
	}

	public StarSystemAPI getWhere() {
		return params.where;
	}

	public List<SectorEntityToken> getBlockadePoints() {
		return blockadePoints;
	}

	public SectorEntityToken getPrimary() {
		return primary;
	}
	
	
	
}








