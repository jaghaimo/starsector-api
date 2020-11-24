package com.fs.starfarer.api.campaign.listeners;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.WarningBeaconIntel;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;

public class CoreDiscoverEntityPlugin implements DiscoverEntityPlugin {
	
	public void discoverEntity(SectorEntityToken entity) {

		
		entity.setDiscoverable(null);
		entity.setSensorProfile(null);
		
		if (entity.hasTag(Tags.WARNING_BEACON)) {
			WarningBeaconIntel intel = new WarningBeaconIntel(entity);
			Global.getSector().getIntelManager().addIntel(intel);
		} else {
			Color c = Global.getSector().getPlayerFaction().getBaseUIColor();
			MessageIntel intel = new MessageIntel("Discovered: " + entity.getName(),
												  c, new String[] {entity.getName()}, c);
			intel.setSound("ui_discovered_entity");
			intel.setIcon(Global.getSettings().getSpriteName("intel", "discovered_entity"));
			Global.getSector().getCampaignUI().addMessage(intel);
		}
		

		
		float xp = 0;
		if (entity.hasDiscoveryXP()) {
			xp = entity.getDiscoveryXP();
		} else if (entity.getCustomEntityType() != null) {
			SalvageEntityGenDataSpec salvageSpec = (SalvageEntityGenDataSpec) Global.getSettings().getSpec(SalvageEntityGenDataSpec.class, entity.getCustomEntityType(), true);
			if (salvageSpec != null) {
				xp = salvageSpec.getXpDiscover();
			}
		}
		if (xp > 0) {
			Global.getSector().getPlayerPerson().getStats().addXP((long) xp);
		}
		
		ListenerUtil.reportEntityDiscovered(entity);
	}
	
	

	public int getHandlingPriority(Object params) {
		return 0;
	}
}
