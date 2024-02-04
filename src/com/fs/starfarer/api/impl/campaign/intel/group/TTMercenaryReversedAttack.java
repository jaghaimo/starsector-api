package com.fs.starfarer.api.impl.campaign.intel.group;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.TriTachyonHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ttcr.TTCRMercenariesBribedFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ttcr.TriTachyonCommerceRaiding;
import com.fs.starfarer.api.impl.campaign.intel.group.FGRaidAction.FGRaidType;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;



public class TTMercenaryReversedAttack extends GenericRaidFGI  {

	public static final String TTMAR_FLEET = "$TTMAR_fleet";
	public static final String TTMAR_COMMAND = "$TTMAR_command";
	
	public static String KEY = "$TTMAR_ref";
	public static TTMercenaryReversedAttack get() {
		return (TTMercenaryReversedAttack) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	
	
	
	public static void sendReversedAttack(InteractionDialogAPI dialog) {
		TTMercenaryAttack attack = TTMercenaryAttack.get();
		if (attack == null || attack.isEnding()) return;
		
		Random random = new Random();
		
		StarSystemAPI target = TriTachyonHostileActivityFactor.getPrimaryTriTachyonSystem();
		if (target == null) return;
		
		GenericRaidParams params = new GenericRaidParams(new Random(), false);
		//params.makeFleetsHostile = true; // irrelevant since fleets already spawned
		
		//params.source = attack.params.source;
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		SectorEntityToken origin = pf.getContainingLocation().createToken(pf.getLocation());
		
		MarketAPI fake = Global.getFactory().createMarket("fake", "an independent world", 5);
		fake.setPrimaryEntity(origin);
		origin.setMarket(fake);
		params.source = fake;
		
		params.prepDays = 0f;
		params.payloadDays = 27f + 7f * random.nextFloat();
		
		params.raidParams.where = target;
		params.raidParams.type = FGRaidType.SEQUENTIAL;
		
		Set<String> disrupt = new LinkedHashSet<String>();
		for (MarketAPI market : Misc.getMarketsInLocation(target, Factions.TRITACHYON)) {
			params.raidParams.allowedTargets.add(market);
			params.raidParams.allowNonHostileTargets = true;
			for (Industry ind : market.getIndustries()) {
				if (ind.getSpec().hasTag(Industries.TAG_UNRAIDABLE)) continue;
				disrupt.add(ind.getId());
			}
		}
		
		params.raidParams.disrupt.addAll(disrupt);
		params.raidParams.raidsPerColony = Math.min(disrupt.size(), 4);
		if (disrupt.isEmpty()) {
			params.raidParams.raidsPerColony = 2;
		}
		
		if (params.raidParams.allowedTargets.isEmpty()) {
			return;
		}
		
		params.factionId = Factions.INDEPENDENT;

		
		TTMercenaryReversedAttack reverse = new TTMercenaryReversedAttack(params);
		
//		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
//		SectorEntityToken origin = pf.getContainingLocation().createToken(pf.getLocation());
//		reverse.setOrigin(origin);
		
		
		reverse.setSpawnedFleets(true);
		reverse.setDoIncrementalSpawn(false);
		reverse.getFleets().addAll(attack.getFleets());
		reverse.setTotalFPSpawned(attack.getTotalFPSpawned());
		
		reverse.setApproximateNumberOfFleets(attack.getApproximateNumberOfFleets());
		
		reverse.getRoute().setExtra(attack.getRoute().getExtra());
		
		// the raid "source" may be a tritach market
		// so, make the fleets scatter/return to their individual sourceMarket instead 
		reverse.removeAction(GenericRaidFGI.RETURN_ACTION);
		
		attack.finish(false);
		attack.getFleets().clear();
		attack.endImmediately();
		
		String reasonTTMA = "TTMA";
		String reasonTTMAR = "TTMAR";
		for (CampaignFleetAPI fleet : reverse.getFleets()) {
			MemoryAPI mem = fleet.getMemoryWithoutUpdate();
			fleet.clearAssignments();
			mem.unset(Misc.FLEET_RETURNING_TO_DESPAWN);
			
			mem.set(TTMAR_FLEET, true);
			if (mem.getBoolean(TTMercenaryAttack.TTMA_COMMAND)) {
				mem.set(TTMAR_COMMAND, true);
			}
			
			mem.unset(TTMercenaryAttack.TTMA_COMMAND);
			mem.unset(TTMercenaryAttack.TTMA_FLEET);
			
			Misc.makeNonHostileToFaction(fleet, Factions.DIKTAT, false, -1);
			Misc.makeNonHostileToFaction(fleet, Factions.TRITACHYON, false, -1);
			
			
			Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_MAKE_HOSTILE,
							reasonTTMA, false, -1f);
			Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE,
							reasonTTMAR, true, -1f);
		}
		

		// not player targeted, so needs to be "important" so the player gets updates
		reverse.setImportant(true);
		
		Global.getSector().getIntelManager().addIntel(reverse, false, dialog.getTextPanel());
		
		TriTachyonCommerceRaiding.addFactorCreateIfNecessary(new TTCRMercenariesBribedFactor(), dialog);
	}
	
	
	
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);
	
	
	public TTMercenaryReversedAttack(GenericRaidParams params) {
		super(params);
		
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
	}
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
	}
	

	@Override
	public String getNoun() {
		return "mercenary attack";
	}

	@Override
	public String getForcesNoun() {
		return super.getForcesNoun();
	}


	@Override
	public String getBaseName() {
		return "Mercenary Counter-raid";
	}

	@Override
	public void abort() {
		if (!isAborted()) {
			for (CampaignFleetAPI curr : getFleets()) {
				curr.getMemoryWithoutUpdate().unset(TTMAR_FLEET);
			}
		}
		super.abort();
	}
	
	
	@Override
	public void advance(float amount) {
		super.advance(amount);

		float days = Misc.getDays(amount);
		interval.advance(days);
		
		if (interval.intervalElapsed()) {
			if (isCurrent(PAYLOAD_ACTION)) {
				for (CampaignFleetAPI curr : getFleets()) {
					Misc.makeHostileToFaction(curr, Factions.TRITACHYON, 1f);
				}
			}
		}
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.remove(Tags.INTEL_COLONIES);
		return tags;
	}

	
}




