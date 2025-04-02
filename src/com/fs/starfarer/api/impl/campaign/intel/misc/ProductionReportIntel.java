package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.Misc;

public class ProductionReportIntel extends FleetLogIntel {

	public static class ProductionData {
		public LinkedHashMap<String, CargoAPI> data = new LinkedHashMap<String, CargoAPI>();
		
		public CargoAPI getCargo(String name) {
			CargoAPI cargo = data.get(name);
			if (cargo == null) {
				cargo = Global.getFactory().createCargo(true);
				cargo.initMothballedShips(Factions.PLAYER);
				data.put(name, cargo);
			}
			return cargo;
		}
		public boolean isEmpty() {
			for (CargoAPI cargo : data.values()) {
				if (!cargo.isEmpty()) return false;
				if (cargo.getMothballedShips() != null && !cargo.getMothballedShips().getMembersListCopy().isEmpty()) return false;
			}
			return true;
		}
	}
	
	protected MarketAPI gatheringPoint;
	protected ProductionData data;
	protected int totalCost;
	protected int accrued;
	protected boolean noProductionThisMonth;
	

	public ProductionReportIntel(MarketAPI gatheringPoint, ProductionData data, int totalCost, int accrued, boolean noProductionThisMonth) {
		this.gatheringPoint = gatheringPoint;
		this.data = data;
		this.totalCost = totalCost;
		this.accrued = accrued;
		this.noProductionThisMonth = noProductionThisMonth;
		setDuration(10f);
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
		
//		if (mode != ListInfoMode.IN_DESC) {
			if (!data.isEmpty()) {
				float days = getDaysSincePlayerVisible();
				if (days < 1) {
					info.addPara("Items delivered to %s", 
								initPad, tc, getFactionForUIColors().getBaseUIColor(), gatheringPoint.getName());
					initPad = 0f;
				} else {
					LabelAPI label = info.addPara("Items delivered to %s %s " + getDaysString(days) + " ago", 
							initPad, tc, getFactionForUIColors().getBaseUIColor(), gatheringPoint.getName(), 
							getDays(days));
					label.setHighlightColors(getFactionForUIColors().getBaseUIColor(), h);
					initPad = 0f;
				}
			}
			if (totalCost > 0) {
				info.addPara("Cost this month: %s", initPad, tc, h, Misc.getDGSCredits(totalCost));
			}
//			if (days >= 1) {
//				addDays(info, "ago", days, tc, initPad);
//			}
//		} else {
//			for (CargoStackAPI stack : cargo.getStacksCopy()) {
//				info.addPara("%s " + Strings.X + " " + stack.getDisplayName(), initPad, tc, h, "" + (int) stack.getSize());
//				initPad = 0f;
//			}
//			for (FleetMemberAPI member : cargo.getMothballedShips().getMembersListCopy()) {
//				info.addPara(member.getVariant().getFullDesignationWithHullName(), tc, initPad);
//				initPad = 0f;
//			}
//		}
		
		unindent(info);
	}

	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		addBulletPoints(info, mode);
	}

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float small = 3f;
		float opad = 10f;
		
		if (data == null) {
			data = new ProductionData();
		}

		info.addImage(getFactionForUIColors().getLogo(), width, 128, opad);
		
//		float days = getDaysSincePlayerVisible();
//		String daysStr = getDaysString(days);
//		if (days >= 1) {
//			info.addPara("Monthly production completed %s " + daysStr + " ago.", opad, h, getDays(days));
//		} else {
		
		if (accrued > 0) {
			info.addPara("A total of %s worth of production effort has been put into projects that have not yet been " +
					"completed.", opad, Misc.getHighlightColor(), "" + Misc.getDGSCredits(accrued));	
		}
		
		if (noProductionThisMonth) {
			info.addPara("No production work was done this month due to a lack of funds.", opad);
		}
		
		if (!data.isEmpty()) {
			info.addPara("Production and other resource and ship hull acquisition completed during the last month.", opad);
		}
		
//		}
//		info.addPara("Monthly production completed.; materiel delivered to %s.", 
//					opad, getFactionForUIColors().getBaseUIColor(), gatheringPoint.getName());
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
		
		
		List<String> keys = new ArrayList<String>(data.data.keySet());
		Collections.sort(keys, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		
		for (String key : keys) {
			CargoAPI cargo = data.data.get(key);
			if (cargo.isEmpty() && 
					((cargo.getMothballedShips() == null || 
					  cargo.getMothballedShips().getMembersListCopy().isEmpty()))) {
				continue;
			}
		
			info.addSectionHeading(key, Alignment.MID, opad);
			
			float valueWidth = 30;
			if (!cargo.getStacksCopy().isEmpty()) {
				info.addPara("Weapons, supplies, and other cargo:", opad);
				
				info.showCargo(cargo, 20, true, opad);
				
//				info.beginGridFlipped(width, 1, valueWidth, opad);
//				int j = 0;
//				for (CargoStackAPI stack : cargo.getStacksCopy()) {
//					String name = info.shortenString(stack.getDisplayName(), width - valueWidth - opad);
//					info.addToGrid(0, j++, name, "" + (int) stack.getSize());
//				}
//				info.addGrid(small);
			}
			if (!cargo.getMothballedShips().getMembersListCopy().isEmpty()) {
				CountingMap<String> counts = new CountingMap<String>();
				for (FleetMemberAPI member : cargo.getMothballedShips().getMembersListCopy()) {
					//counts.add(member.getVariant().getFullDesignationWithHullName());
					counts.add(member.getVariant().getHullSpec().getHullName() + " " + member.getVariant().getDesignation());
				}
				
				info.addPara("Ship hulls with basic armaments:", opad);
				
				info.showShips(cargo.getMothballedShips().getMembersListCopy(), 20, true, opad);
				
//				info.beginGridFlipped(width, 1, valueWidth, opad);
//				int j = 0;
//				for (String hull : counts.keySet()) {
//					String name = info.shortenString(hull, width - valueWidth - opad);
//					info.addToGrid(0, j++, name, "" + (int) counts.getCount(hull));
//				}
//				info.addGrid(small);
			}
			
			// in case some of the ships shown are in the player's fleet; the above may cause them to briefly get
			// set to zero CR
			Global.getSector().getPlayerFleet().getFleetData().setSyncNeeded();
			Global.getSector().getPlayerFleet().getFleetData().syncIfNeeded();
		}

		addLogTimestamp(info, tc, opad);
	}

	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "production_report");
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_PRODUCTION);
		return tags;
	}

	public String getSortString() {
		//return "Production";
		return super.getSortString();
	}

	public String getName() {
		return "Production Report";
	}

	@Override
	public FactionAPI getFactionForUIColors() {
		return Global.getSector().getPlayerFaction();
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return gatheringPoint.getPrimaryEntity();
	}

	@Override
	public boolean shouldRemoveIntel() {
		if (isImportant()) return false;
		if (getDaysSincePlayerVisible() < 30) return false;
		return super.shouldRemoveIntel();
	}


}


