package com.fs.starfarer.api.campaign.events;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class CampaignEventTarget {

	private Object custom;
	private LocationAPI location;
	private SectorEntityToken entity;
	private Object extra;

	/**
	 * custom *must* implement hashCode() and equals().
	 * If two event targets have custom1.equals(custom2), then
	 * the event target is considered to be equal regardless of other
	 * data member values.
	 * @param custom
	 */
	public CampaignEventTarget(Object custom) {
		this.custom = custom;
	}

	public CampaignEventTarget(MarketAPI market) {
		this(market.getPrimaryEntity());
	}
	
	public CampaignEventTarget(SectorEntityToken entity) {
		this.location = entity.getContainingLocation();
		this.entity = entity;
	}

	public CampaignEventTarget(LocationAPI location) {
		this.location = location;
	}

	public MarketAPI getMarket() {
		if (entity != null) return entity.getMarket();
		return null;
	}
	
	public Vector2f getLocationInHyperspace() {
		boolean inHyper = location.isHyperspace();
		Vector2f locInHyper = inHyper && entity != null ? entity.getLocation() : location.getLocation();
		return locInHyper;
	}


	public LocationAPI getLocation() {
		return location;
	}

	public void setLocation(LocationAPI location) {
		this.location = location;
	}

	public SectorEntityToken getEntity() {
		return entity;
	}

	public void setEntity(SectorEntityToken entity) {
		this.entity = entity;
	}
	
	public Object getCustom() {
		return custom;
	}

	public void setCustom(Object custom) {
		this.custom = custom;
	}
	

	public Object getExtra() {
		return extra;
	}

	public void setExtra(Object extra) {
		this.extra = extra;
	}

	//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
//		result = prime * result
//				+ ((location == null) ? 0 : location.hashCode());
//		return result;
//	}
//
//	
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		CampaignEventTarget other = (CampaignEventTarget) obj;
//		if (entity == null) {
//			if (other.entity != null)
//				return false;
//		} else if (entity != other.entity)
//			return false;
//		if (location == null) {
//			if (other.location != null)
//				return false;
//		} else if (location != other.location)
//			return false;
//		return true;
//	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (custom != null) {
			result = prime * result + ((custom == null) ? 0 : custom.hashCode());
		} else {
			result = prime * result + ((entity == null) ? 0 : entity.hashCode());
			result = prime * result + ((location == null) ? 0 : location.hashCode());
			result = prime * result + ((extra == null) ? 0 : extra.hashCode());			
			
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CampaignEventTarget other = (CampaignEventTarget) obj;
		if (custom != null && custom.equals(other.custom)) {
			return true;
		}
			
		if (custom == null) {
			if (other.custom != null)
				return false;
		} else if (!custom.equals(other.custom))
			return false;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (entity != other.entity)
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (location != other.location)
			return false;
		if (extra == null) {
			if (other.extra != null)
				return false;
		} else if (!extra.equals(other.extra))
			return false;
		return true;
	}
	
	
	
	public FactionAPI getFaction() {
		if (entity != null) {
			return entity.getFaction();
		}
		return null;
	}
}
