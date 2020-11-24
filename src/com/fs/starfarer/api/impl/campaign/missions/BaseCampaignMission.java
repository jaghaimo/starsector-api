package com.fs.starfarer.api.impl.campaign.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignMissionPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.events.CampaignEventPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;

public class BaseCampaignMission implements CampaignMissionPlugin {
	private String id = Misc.genUID();
	protected long timestamp;
	protected SectorEntityToken acceptLocation = null;
	
	protected CampaignEventPlugin event;
	
	public BaseCampaignMission() {
		timestamp = Global.getSector().getClock().getTimestamp();
	}
	
	public void cleanup() {
		if (event != null) {
			Global.getSector().getEventManager().endEvent(event);
		}
	}
	
	public void advance(float amount) {
		
	}

	public SectorEntityToken getAcceptLocation() {
		return acceptLocation == null ? Global.getSector().getPlayerFleet() : acceptLocation;
	}

	public void setAcceptLocation(SectorEntityToken acceptLocation) {
		this.acceptLocation = acceptLocation;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getCreationTimestamp() {
		return timestamp;
	}

	public String getId() {
		return id;
	}

	public PersonAPI getImportantPerson() {
		return null;
	}

	public CampaignEventPlugin getPrimedEvent() {
		throw new RuntimeException("Override BaseCampaignMission.getPrimedEvent()");
	}

	public void playerAccept(SectorEntityToken entity) {
		setAcceptLocation(entity);
	}

	public String getFactionId() {
		return Factions.NEUTRAL;
	}

	public String getName() {
		return "Override BaseCampaignMission.getName()";
	}

	public String getPostingStage() {
		return "posting";
	}
	
	

	public boolean canPlayerAccept() {
		FactionAPI faction = Global.getSector().getFaction(getFactionId());
		FactionAPI playerFaction = Global.getSector().getFaction(Factions.PLAYER);

		RepLevel minStanding = RepLevel.SUSPICIOUS;
		if (faction.getCustom().optBoolean(Factions.CUSTOM_OFFER_MISSIONS_WHEN_HOSTILE)) {
			minStanding = RepLevel.HOSTILE;
		}
		
		PersonAPI person = getImportantPerson();
		if (person != null) {
			RepLevel personStanding = person.getRelToPlayer().getLevel();
			RepLevel factionStanding = playerFaction.getRelationshipLevel(faction);
			
			if (personStanding.isPositive()) {
				return personStanding.isAtWorst(minStanding);
			} else if (personStanding.isNeutral()) {
				return factionStanding.isAtWorst(minStanding);
			} else {
				return personStanding.isAtWorst(minStanding) && factionStanding.isAtWorst(minStanding);
			}
		} else {
			return playerFaction.isAtWorst(faction.getId(), minStanding);
		}
	}

	public String getAcceptTooltip() {
		if (canPlayerAccept()) return null;
		
		FactionAPI faction = Global.getSector().getFaction(getFactionId());
		FactionAPI playerFaction = Global.getSector().getFaction(Factions.PLAYER);
		RepLevel minStanding = RepLevel.SUSPICIOUS;
		if (faction.getCustom().optBoolean(Factions.CUSTOM_OFFER_MISSIONS_WHEN_HOSTILE)) {
			minStanding = RepLevel.HOSTILE;
		}
		
		PersonAPI person = getImportantPerson();
		if (person != null) {
			RepLevel personStanding = person.getRelToPlayer().getLevel();
			
			if (personStanding.isPositive()) {
				return "Requires: " + person.getName().getFullName() + " - " + minStanding.getDisplayName().toLowerCase() + " or better";
			} else if (personStanding.isNeutral()) {
				return "Requires: " + faction.getDisplayName() + " - " + minStanding.getDisplayName().toLowerCase() + " or better";
			} else {
				return "Requires: " + person.getName().getFullName() + " - " + minStanding.getDisplayName().toLowerCase() + " or better\n" +
					   "Requires: " + faction.getDisplayName() + " - " + minStanding.getDisplayName().toLowerCase() + " or better";
			}
		} else {
			return "Requires: " + faction.getDisplayName() + " - " + minStanding.getDisplayName().toLowerCase() + " or better";
		}
	}
	
	public Highlights getAcceptTooltipHighlights() {
		String tooltip = getAcceptTooltip();
		if (tooltip == null) return null;
		Highlights h = new Highlights();

		FactionAPI faction = Global.getSector().getFaction(getFactionId());
		FactionAPI playerFaction = Global.getSector().getFaction(Factions.PLAYER);
		RepLevel minStanding = RepLevel.SUSPICIOUS;
		if (faction.getCustom().optBoolean(Factions.CUSTOM_OFFER_MISSIONS_WHEN_HOSTILE)) {
			minStanding = RepLevel.HOSTILE;
		}
		
		PersonAPI person = getImportantPerson();
		if (person != null) {
			RepLevel personStanding = person.getRelToPlayer().getLevel();
			if (personStanding.isPositive()) {
				h.setText(minStanding.getDisplayName().toLowerCase());
				h.setColors(faction.getRelColor(minStanding));
			} else if (personStanding.isNeutral()) {
				h.setText(minStanding.getDisplayName().toLowerCase());
				h.setColors(faction.getRelColor(minStanding));
			} else {
				h.setText(minStanding.getDisplayName().toLowerCase(), minStanding.getDisplayName().toLowerCase());
				h.setColors(faction.getRelColor(minStanding), faction.getRelColor(minStanding));
			}
		} else {
			h.setText(minStanding.getDisplayName().toLowerCase());
			h.setColors(faction.getRelColor(minStanding));
		}
		return h;
	}

	public boolean showAcceptTooltipNextToButton() {
		return true;
	}

}
