package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class RemnantNexusIntel extends MapMarkerIntel {

	public RemnantNexusIntel(CampaignFleetAPI entity) {
		
		//String icon = Global.getSettings().getSpriteName("intel", "remnant_nexus");
		String title = entity.getNameWithFactionKeepCase();
		
		String text = null;
//		if (entity.getStarSystem() != null) {
//			text = "Located in the " + entity.getStarSystem().getNameWithLowercaseTypeShort();
//		}
		setSound("ui_discovered_entity");
		
		setWithDeleteButton(false);
		//setWithTimestamp(false);
		
		init(entity, title, text, null, true);
	}
	
	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "remnant_nexus");
	}

	@Override
	public FactionAPI getFactionForUIColors() {
		return entity.getFaction();
	}
	
	public boolean isDamaged() {
		return entity.getMemoryWithoutUpdate().getBoolean("$damagedStation");
	}
	
	@Override
	protected boolean withTextInDesc() {
		return false;
	}

	@Override
	protected boolean withCustomVisual() {
		return true;
	}

	@Override
	protected boolean withCustomDescription() {
		return true;
	}

	@Override
	protected void addCustomVisual(TooltipMakerAPI info, float width, float height) {
		info.addImage(Global.getSector().getFaction(Factions.REMNANTS).getLogo(), width, 10f);
	}
	

	@Override
	protected void addCustomDescription(TooltipMakerAPI info, float width, float height) {
		Description desc = Global.getSettings().getDescription("remnant_station2", Type.SHIP);
		if (desc != null) {
			float opad = 10f;
			info.addPara(desc.getText1FirstPara(), opad);
			if (isDamaged()) {
				info.addPara("The station's long arc gapes with empty sockets where weapons platforms "
						+ "and citadels were once present. Despite the obvious damage, "
						+ "the station is armed and operational.",  opad);
			}
		}
	}

	
	public static RemnantNexusIntel getNexusIntel(SectorEntityToken entity) {
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(RemnantNexusIntel.class)) {
			if (((RemnantNexusIntel)intel).getEntity() == entity) return (RemnantNexusIntel)intel;
		}
		return null;
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		//tags.remove(Tags.INTEL_FLEET_LOG);
		tags.add(Factions.REMNANTS);
		if (!Misc.getMarketsInLocation(entity.getStarSystem(), Factions.PLAYER).isEmpty()) {
			tags.add(Tags.INTEL_COLONIES);
		}
		return tags;
	}
	
	
}




