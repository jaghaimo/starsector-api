package com.fs.starfarer.api.impl.campaign.intel.group;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.group.FGRaidAction.FGRaidParams;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class TestFleetGroupIntel extends FleetGroupIntel {

	public static String PREPARE_ACTION = "prepare_action";
	public static String TRAVEL_ACTION = "travel_action";
	public static String RAID_ACTION = "raid_action";
	public static String RETURN_ACTION = "return_action";
	

	protected SectorEntityToken origin;
	protected FGRaidAction raidAction;
	
	public TestFleetGroupIntel() {
		super();
		
		MarketAPI garnir = Global.getSector().getEconomy().getMarket("corvus_IIIa");
		MarketAPI jangala = Global.getSector().getEconomy().getMarket("jangala");
		MarketAPI gilead = Global.getSector().getEconomy().getMarket("gilead");
		MarketAPI asher = Global.getSector().getEconomy().getMarket("asher");
		
		//gilead = Global.getSector().getEconomy().getMarket("market_system_192c:planet_1");
		
//		gilead = jangala;
//		addAction(new FGWaitAction(jangala.getPrimaryEntity(), 1f, "preparing for expedition"));
//		addAction(new FGTravelAction(jangala.getPrimaryEntity(), gilead.getPrimaryEntity().getStarSystem().getCenter()));
//		addAction(new FGTravelAction(gilead.getPrimaryEntity(), jangala.getPrimaryEntity()));
		
		setFaction(Factions.PIRATES);
		addAction(new FGWaitAction(garnir.getPrimaryEntity(), 1f, "preparing for raid"), PREPARE_ACTION);
		addAction(new FGTravelAction(garnir.getPrimaryEntity(), 
									 gilead.getPrimaryEntity().getStarSystem().getCenter()),
				  					 TRAVEL_ACTION);
//		addAction(new FGWaitAction(gilead.getPrimaryEntity().getStarSystem().getCenter(), 5f,
//								   "preparing for raid"), POST_RAID_ACTION);
		
		
		FGRaidParams params = new FGRaidParams();
		params.where = gilead.getPrimaryEntity().getStarSystem();
		params.allowedTargets.add(gilead);
		params.allowedTargets.add(asher);
		//params.setBombardment(BombardType.SATURATION);
		params.setDisrupt(Industries.FARMING, Industries.MEGAPORT);
		
		raidAction = new FGRaidAction(params, 3f);
		addAction(raidAction, RAID_ACTION);
		
		addAction(new FGTravelAction(gilead.getPrimaryEntity().getStarSystem().getCenter(),
									garnir.getPrimaryEntity().getStarSystem().getCenter()),
					 				RETURN_ACTION);
		
		
		origin = garnir.getPrimaryEntity();
		
		createRoute(Factions.PIRATES, 30, 7, null);
	}
	
	protected void spawnFleets() {
		MarketAPI garnir = Global.getSector().getEconomy().getMarket("corvus_IIIa");
		Vector2f loc = garnir.getLocationInHyperspace();
		
		Float damage = null;
		if (route != null && route.getExtra() != null) {
			damage = route.getExtra().damage;
		}
		if (damage == null) damage = 0f;
		
		String factionId;
		factionId = Factions.LUDDIC_CHURCH;
		factionId = Factions.PIRATES;
		
		
		WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<Integer>(getRandom());
		picker.add(3);
		picker.add(4);
		picker.add(6);
		picker.add(7);
		picker.add(9);
		picker.add(10);
		picker.add(10);
		
		float total = 0;
		for (Integer i : picker.getItems()) total += i;
		
		float spawnsToSkip = total * damage * 0.5f;
		float skipped = 0f;
		
		
		while (!picker.isEmpty()) {
			Integer size = picker.pickAndRemove();
			if (skipped < spawnsToSkip && getRandom().nextFloat() < damage) {
				skipped += size;
				continue;
			}
		
			FleetCreatorMission m = new FleetCreatorMission(new Random());
			m.beginFleet();
			
			m.createQualityFleet(size, factionId, loc);
			m.setFleetSource(garnir);
			m.setFleetDamageTaken(damage);
			//m.triggerSetFleetFaction();
			//m.triggerFleetAllowLongPursuit();
			m.triggerSetPirateFleet();
			//m.triggerSetWarFleet();
			m.triggerMakeNoRepImpact();
			
			CampaignFleetAPI fleet = m.createFleet();
			if (fleet != null && route != null) {
				setLocationAndCoordinates(fleet, route.getCurrent());
				fleets.add(fleet);
			}
		}
	
	}
	
	protected void addNonUpdateBulletPoints(TooltipMakerAPI info, Color tc, Object param, ListInfoMode mode, float initPad) {
		Color h = Misc.getHighlightColor();
		FGAction curr = getCurrentAction();
		StarSystemAPI system = raidAction.getParams().where;

		float untilDeparture = getETAUntil(TRAVEL_ACTION);
		float untilRaid = getETAUntil(RAID_ACTION);
		float untilReturn = getETAUntil(RETURN_ACTION, true);
		
		if (mode == ListInfoMode.MESSAGES) { // initial notification only, not updates
			info.addPara("Targeting the " + system.getNameWithLowercaseTypeShort(), tc, initPad);
			initPad = 0f;
		}
		if (untilDeparture > 0) {
			addETABulletPoints(null, null, false, untilDeparture, ETAType.DEPARTURE, info, tc, initPad);
		}
		if (untilRaid > 0 && getSource().getContainingLocation() != system) {
			addETABulletPoints(system.getNameWithLowercaseTypeShort(), null, false, untilRaid, ETAType.ARRIVING,
					   info, tc, initPad);
			initPad = 0f;
		}
		if (untilReturn > 0 && isSucceeded() && getSource().getContainingLocation() != system) {
			StarSystemAPI from = getSource().getStarSystem();
			
			addETABulletPoints(from.getNameWithLowercaseTypeShort(), null, false, untilReturn, ETAType.RETURNING,
					   info, tc, initPad);
			initPad = 0f;
		}
		if (mode == ListInfoMode.INTEL && curr != null && curr.getId().equals(RAID_ACTION)) {
			info.addPara("Operating in the " + system.getNameWithLowercaseTypeShort(), tc, initPad);
			initPad = 0f;
		}
		if (mode != ListInfoMode.IN_DESC && isEnding()) {
			if (!isSucceeded()) {
				info.addPara("The raiding forces have been defeated and scatter", tc, initPad);
			}
		}
	}
	
	protected void addUpdateBulletPoints(TooltipMakerAPI info, Color tc, Object param, ListInfoMode mode, float initPad) {
		StarSystemAPI system = raidAction.getParams().where;
		//Color h = Misc.getHighlightColor();
		if (ABORT_UPDATE.equals(param)) {
			info.addPara("The raiding forces have been defeated and scatter", tc, initPad);
		} else if (PREPARE_ACTION.equals(param)) {
			float untilRaid = getETAUntil(RAID_ACTION);
			addETABulletPoints(system.getNameWithLowercaseTypeShort(), null, true, untilRaid, ETAType.ARRIVING,
							   info, tc, initPad);
		} else if (TRAVEL_ACTION.equals(param)) {
			addArrivedBulletPoint(system.getNameWithLowercaseTypeShort(), null, info, tc, initPad);
		} else if (RAID_ACTION.equals(param)) {
			if (isSucceeded()) {
				info.addPara("The raiding forces are withdrawing", tc, initPad);
			} else {
				info.addPara("The raiding forces have been defeated and scatter", tc, initPad);
			}
		}
	}
	
	@Override
	protected boolean shouldSendIntelUpdateWhenActionFinished(FGAction action) {
		if (RETURN_ACTION.equals(action.getId())) return false;
		if (RAID_ACTION.equals(action.getId())) {
			// if it failed, will send an update on abort() instead
			return isSucceeded();
		}
		if (TRAVEL_ACTION.equals(action.getId())) {
			// no update for "arriving" when traveling from one planet in-system to another
			return getSource().getContainingLocation() != raidAction.getParams().where;
		}
		
		return super.shouldSendIntelUpdateWhenActionFinished(action);
	}

	protected void addBasicDescription(TooltipMakerAPI info, float width, float height, float opad) {
		info.addImage(getFaction().getLogo(), width, 128, opad);
		
		StarSystemAPI system = raidAction.getParams().where;
		
		info.addPara(Misc.ucFirst(faction.getPersonNamePrefixAOrAn()) + " %s raid against "
				+ "the " + system.getNameWithLowercaseTypeShort() + ".", opad,
			faction.getBaseUIColor(), faction.getEntityNamePrefix());
	}
	
	protected void addAssessmentSection(TooltipMakerAPI info, float width, float height, float opad) {
		Color h = Misc.getHighlightColor();
		
		FactionAPI faction = getFaction();
		
		List<MarketAPI> targets = raidAction.getParams().allowedTargets;
		
		if (!isEnding() && !isSucceeded()) {
			info.addSectionHeading("Assessment", 
					   faction.getBaseUIColor(), faction.getDarkUIColor(), Alignment.MID, opad);
			if (targets.isEmpty()) {
				info.addPara("There are no colonies for the raid to target in the system.", opad);
			} else {
				StarSystemAPI system = raidAction.getParams().where;
				
				boolean potentialDanger = addStrengthDesc(info, opad, system, "raiding forces",
											"the raid is unlikely to find success",
											"the raid's outcome is uncertain",
											"the raid is likely to find success");

				if (potentialDanger) {
					showMarketsInDanger(info, opad, width, system, targets, 
								"should be safe from the raid",
								"are at risk of being raided and losing stability:",
								"losing stability");
				}
			}
		}
	}
	
	
	protected void addStatusSection(TooltipMakerAPI info, float width, float height, float opad) {
		FGAction curr = getCurrentAction();

		boolean showStatus = curr != null || isEnding() || isSucceeded();
		
		if (showStatus) {
			info.addSectionHeading("Status", 
					   faction.getBaseUIColor(), faction.getDarkUIColor(), Alignment.MID, opad);
			if (isEnding() && !isSucceeded()) {
				info.addPara("The raiding forces have been defeated and any "
						+ "remaining ships are retreating in disarray.", opad);
			} else if (isEnding() || isSucceeded()) {
				info.addPara("The raid was successful and the raiding forces are withdrawing.", opad);
			} else if (curr != null) {
				//StarSystemAPI from = getSource().getStarSystem(); 
				StarSystemAPI to = raidAction.getParams().where;
				if (PREPARE_ACTION.equals(curr.getId())) {
					BaseHubMission.addStandardMarketDesc("Making preparations in orbit around", 
													getSource().getMarket(), info, opad);
				} else if (TRAVEL_ACTION.equals(curr.getId())) {
					info.addPara("Traveling from " + getSource().getMarket().getName() + " to the " +
							to.getNameWithLowercaseTypeShort() + ".", opad);
				} else if (RAID_ACTION.equals(curr.getId())) {
					info.addPara("Conducting operations in the " +
							to.getNameWithLowercaseTypeShort() + ".", opad);
				}
			}
		}
	}
	
	
	public String getBaseName() {
		return Misc.ucFirst(getFaction().getPersonNamePrefix()) + " Raid";
	}
	
	public boolean isSucceeded() {
		return raidAction.getSuccessFraction() > 0f && raidAction.isActionFinished();
	}
	
	protected SectorEntityToken getSource() {
		return origin;
	}
	
	protected SectorEntityToken getDestination() {
		return raidAction.getParams().where.getHyperspaceAnchor();
	}

	@Override
	protected boolean isPlayerTargeted() {
		return true;
	}
}




