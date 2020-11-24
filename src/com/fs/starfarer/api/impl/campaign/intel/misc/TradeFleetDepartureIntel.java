package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.CargoQuantityData;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.EconomyRouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.IconRenderMode;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class TradeFleetDepartureIntel extends BaseIntelPlugin {

	protected RouteData route;
	
	transient protected List<CargoQuantityData> deliverList;
	transient protected List<CargoQuantityData> returnList;
	transient protected boolean goods;
	transient protected boolean materiel;
	transient protected boolean valuable;
	transient protected boolean large;
	transient protected FactionAPI faction;
	transient protected EconomyRouteData data;
	

	public TradeFleetDepartureIntel(RouteData route) {
		this.route = route;
		
		initTransientData();
		
		//if (deliverList.isEmpty() && returnList.isEmpty()) {
		if (deliverList.isEmpty()) {
			return;
		}

		float prob = 0.1f;
		if (valuable) prob += 0.1f;
		if (large) prob += 0.1f;
		if (!deliverList.isEmpty() && !returnList.isEmpty()) {
			prob += 0.2f;
		}
		
		boolean sameLoc = data.from.getPrimaryEntity().getContainingLocation() != null &&
						  data.from.getPrimaryEntity().getContainingLocation() == 
							  Global.getSector().getPlayerFleet().getContainingLocation() &&
						  !data.from.getPrimaryEntity().getContainingLocation().isHyperspace();
		if (sameLoc) prob = 1f;
		
		float target = Global.getSettings().getFloat("targetNumTradeFleetNotifications");
		float numAlready = Global.getSector().getIntelManager().getIntelCount(TradeFleetDepartureIntel.class, true);
		
		float probMult = Misc.getProbabilityMult(target, numAlready, 0.5f);
		if (probMult > 1) probMult = 1; // just making it less likely if there's a bunch of these already
		
		prob *= probMult;
		
		
		
		
		if (Math.random() > prob) {
			return;
		}
		
		float postingRange = Math.max(0f, data.size - 6f);
		if (valuable) {
			postingRange = Math.max(3f, postingRange);
		}
		setPostingRangeLY(postingRange, true);
		
		setPostingLocation(data.from.getPrimaryEntity());
		
		Global.getSector().getIntelManager().queueIntel(this);
	}
	
	
	protected void initTransientData() {
		data = (EconomyRouteData) route.getCustom();

		deliverList = new ArrayList<CargoQuantityData>();
		returnList = new ArrayList<CargoQuantityData>();
		goods = false;
		materiel = false;
		valuable = false;
		large = data.size >= 6;

		for (CargoQuantityData curr : data.cargoDeliver) {
			CommoditySpecAPI c = curr.getCommodity();
			if (c.getBasePrice() >= 100 && !c.isPersonnel()) {
				valuable = true;
			}

			if (c.isMeta()) {
				materiel = true;
			} else if (!c.isPersonnel()) {
				goods = true;
			}
			deliverList.add(curr);
		}
		for (CargoQuantityData curr : data.cargoReturn) {
			// don't care about return list too much, it's nowhere near as timely
//			CommoditySpecAPI c = curr.getCommodity();
//			if (c.getBasePrice() >= 100 && !c.isPersonnel()) {
//				valuable = true;
//			}
//
//			if (c.isMeta()) {
//				materiel = true;
//			} else if (!c.isPersonnel()) {
//				goods = true;
//			}
			returnList.add(curr);
		}
		
		faction = Global.getSector().getFaction(route.getFactionId());
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		
		bullet(info);
		boolean isUpdate = getListInfoParam() != null;
		
		if (mode != ListInfoMode.IN_DESC) {
			info.addPara("Faction: " + faction.getDisplayName(), initPad, tc,
						 faction.getBaseUIColor(), faction.getDisplayName());
			initPad = 0f;
		}
		
//		String what = getWhat();
//		if (valuable) {
//			info.addPara("Carrying valuable " + what, tc, initPad);
//			initPad = 0f;
//		} else if (large) {
//			info.addPara("Large volume of " + what, tc, initPad);
//			initPad = 0f;
//		}
		
		if (mode != ListInfoMode.IN_DESC) {
			LabelAPI label = info.addPara("From " + data.from.getName() + " to " + data.to.getName(), tc, initPad);
			label.setHighlight(data.from.getName(), data.to.getName());
			label.setHighlightColors(data.from.getFaction().getBaseUIColor(), data.to.getFaction().getBaseUIColor());
			initPad = 0f;
		}
		
		if (isUpdate) {
			info.addPara("Fleet launched", tc, initPad);
		} else {
			float delay = route.getDelay();
			if (delay > 0) {
				addDays(info, "until departure", delay, tc, initPad);
			} else {
				info.addPara("Recently launched", tc, initPad);
			}
		}
		
		unindent(info);
	}
	
	protected String getWhat() {
		String what = "goods and materiel";
		if (!materiel) what = "goods";
		if (!goods) what = "materiel";
		return what;
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color c = getTitleColor(mode);
		float pad = 3f;
		float opad = 10f;
		
		initTransientData();
		
		LabelAPI label = info.addPara(getName(), c, 0f);
//		label.setHighlight(Misc.ucFirst(getFactionForUIColors().getPersonNamePrefix()));
//		label.setHighlightColor(getFactionForUIColors().getBaseUIColor());
		
		addBulletPoints(info, mode);
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		initTransientData();
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		info.addImage(faction.getLogo(), width, 128, opad);
		
		float tier = data.size;
		String fleetType = getFleetTypeName().toLowerCase();
		
		
//		LabelAPI label = info.addPara(Misc.ucFirst(faction.getPersonNamePrefixAOrAn()) + " " + 
//					 faction.getPersonNamePrefix() + " " + fleetType + " is departing from " +
//					 data.from.getName() + " and heading to " + data.to.getName() + ".",
//					 opad, tc, 
//					 faction.getBaseUIColor(),
//					 faction.getPersonNamePrefix());
//		label.setHighlight(faction.getPersonNamePrefix(), data.from.getName(), data.to.getName());
//		label.setHighlightColors(data.from.getFaction().getBaseUIColor(), data.from.getFaction().getBaseUIColor(), data.to.getFaction().getBaseUIColor());
		
		LabelAPI label = info.addPara("Your contacts " + data.from.getOnOrAt() + " " + data.from.getName() + 
				 " let you know that " + 
				 faction.getPersonNamePrefixAOrAn() + " " + 
				 faction.getPersonNamePrefix() + " " + fleetType + " is preparing for a voyage and will soon depart for " + 
				 data.to.getName() + ".",
				 opad, tc, 
				 faction.getBaseUIColor(),
				 faction.getPersonNamePrefix());
		
		label.setHighlight(data.from.getName(), faction.getPersonNamePrefix(), data.to.getName());
		label.setHighlightColors(data.from.getFaction().getBaseUIColor(), faction.getBaseUIColor(), data.to.getFaction().getBaseUIColor());
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
		
		
		String what = getWhat();
		
		if (!deliverList.isEmpty()) {
			info.addPara("On the outward trip to " + data.to.getName() + " the fleet will carry " + 
					EconomyRouteData.getCargoList(deliverList) + ".", opad);
			
			info.beginIconGroup();
			info.setIconSpacingMedium();
			
			for (CargoQuantityData curr : deliverList) {
				CommodityOnMarketAPI com = data.from.getCommodityData(curr.cargo);
				info.addIcons(com, curr.units, IconRenderMode.NORMAL);
			}
			info.addIconGroup(32, 1, opad);
		} else {
			info.addPara("The fleet will carry nothing of note on the trip to " + data.to.getName() + ".", opad);
		}
		if (!returnList.isEmpty()) {
			info.addPara("On the trip back to " + data.from.getName() + " the fleet will carry " + 
					EconomyRouteData.getCargoList(returnList) + ".", opad);
			
			info.beginIconGroup();
			info.setIconSpacingMedium();
			for (CargoQuantityData curr : returnList) {
				CommodityOnMarketAPI com = data.to.getCommodityData(curr.cargo);
				info.addIcons(com, curr.units, IconRenderMode.NORMAL);
			}
			info.addIconGroup(32, 1, opad);
		} else {
			info.addPara("The fleet will carry nothing of note on the trip back to " + data.from.getName() + ".", opad);
		}
		
		if (valuable && large) {
			info.addPara("It's noteworthy because it's carrying a large quantity of valuable " + what + ".", opad);
		} else if (valuable) {
			info.addPara("It's noteworthy because it's carrying valuable " + what + ".", opad);
		} else if (large) {
			info.addPara("It's noteworthy because it's carrying a large quantity of " + what + ".", opad);
		}
		
		if (data.smuggling) {
			info.addPara("Smugglers often operate in a gray legal and moral area. " +
						 "Thus, if one comes to an unfortunate end - as so often happens in their line of work - " +
						 "it's unlikely to cause a unified response from whatever " +
						 "faction or organization they're nominally affiliated with.", g, opad);
		}
	}
	
	@Override
	public String getIcon() {
		initTransientData();
		if (data.smuggling) {
			return Global.getSettings().getSpriteName("intel", "tradeFleet_smuggling");
		} else if (valuable) {
			return Global.getSettings().getSpriteName("intel", "tradeFleet_valuable");
		} else if (large) {
			return Global.getSettings().getSpriteName("intel", "tradeFleet_large");
		} 
		return Global.getSettings().getSpriteName("intel", "tradeFleet_other");
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_FLEET_DEPARTURES);
		
		faction = Global.getSector().getFaction(route.getFactionId());
		tags.add(faction.getId());
		
		data = (EconomyRouteData) route.getCustom();
		if (data.smuggling) {
			tags.add(Tags.INTEL_SMUGGLING);
		}
		
		return tags;
	}
	
	
	public String getSortString() {
		return "Trade Fleet Departure";
	}
	
	public String getFleetTypeName() {
		float tier = data.size;
		String typeId = EconomyFleetRouteManager.getFleetTypeIdForTier(tier, data.smuggling);
		String fleetType = faction.getFleetTypeName(typeId);
		if (fleetType == null) {
			fleetType = "Trade Fleet";
		}
		return fleetType;
	}
	
	public String getName() {
		//return Misc.ucFirst(getFactionForUIColors().getPersonNamePrefix()) + " " + getFleetTypeName();
		return getFleetTypeName();
		//return "Trade Fleet Departure";
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		faction = Global.getSector().getFaction(route.getFactionId());
		return faction;
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return route.getMarket().getPrimaryEntity();
	}
	
	
	protected Float sinceLaunched = null;
	@Override
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);
		
		if (route.getDelay() > 0) return;
		
		if (sinceLaunched == null) sinceLaunched = 0f;
		
		if (sinceLaunched <= 0 && amount > 0) {
			sendUpdateIfPlayerHasIntel(new Object(), true);
		}
		
		float days = Misc.getDays(amount);
		sinceLaunched += days;
	}
	
	public float getTimeRemainingFraction() {
		float f = route.getDelay() / 30f;
		return f;
	}
	

	@Override
	public boolean shouldRemoveIntel() {
		if (route.getDelay() > 0) return false;
		if (sinceLaunched != null && sinceLaunched < getBaseDaysAfterEnd()) {
			return false;
		}
		return true;
	}
	

	@Override
	public void setImportant(Boolean important) {
		super.setImportant(important);
		if (isImportant()) {
			if (!Global.getSector().getScripts().contains(this)) {
				Global.getSector().addScript(this);
			}
		} else {
			Global.getSector().removeScript(this);
		}
	}

	@Override
	public void reportRemovedIntel() {
		super.reportRemovedIntel();
		Global.getSector().removeScript(this);
	}
	
	
	public List<ArrowData> getArrowData(SectorMapAPI map) {
		List<ArrowData> result = new ArrayList<ArrowData>();
		
		if (data.from.getContainingLocation() == data.to.getContainingLocation() &&
				data.from.getContainingLocation() != null &&
				!data.from.getContainingLocation().isHyperspace()) {
			return null;
		}
		
		SectorEntityToken entityFrom = data.from.getPrimaryEntity();
		if (map != null) {
			SectorEntityToken iconEntity = map.getIntelIconEntity(this);
			if (iconEntity != null) {
				entityFrom = iconEntity;
			}
		}
		
		data = (EconomyRouteData) route.getCustom();
		ArrowData arrow = new ArrowData(entityFrom, data.to.getPrimaryEntity());
		arrow.color = getFactionForUIColors().getBaseUIColor();
		result.add(arrow);
		
		return result;
	}
	
	
}







