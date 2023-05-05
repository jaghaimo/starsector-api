package com.fs.starfarer.api.campaign.impl.items;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class MultiBlueprintItemPlugin extends BaseSpecialItemPlugin implements BlueprintProviderItem {

	
	protected Set<String> tags = new LinkedHashSet<String>();
	@Override
	public void init(CargoStackAPI stack) {
		super.init(stack);
		
		String param = spec.getParams();
		if (!param.isEmpty()) {
			for (String tag : param.split(",")) {
				tag = tag.trim();
				if (tag.isEmpty()) continue;
				tags.add(tag);
			}
		}
	}
	
	transient protected List<String> cachedFighters = null;
	transient protected List<String> cachedShips = null;
	transient protected List<String> cachedWeapons= null;
	
	public List<String> getProvidedFighters() {
		if (cachedFighters == null) {
			cachedFighters = getWingIds(tags);
		}
		return cachedFighters;
	}

	public List<String> getProvidedShips() {
		if (cachedShips == null) {
			cachedShips = getShipIds(tags);
		}
		return cachedShips;
	}

	public List<String> getProvidedWeapons() {
		if (cachedWeapons == null) {
			cachedWeapons = getWeaponIds(tags);
		}
		return cachedWeapons;
	}
	public List<String> getProvidedIndustries() {
		return null;
	}
	
	@Override
	public void render(float x, float y, float w, float h, float alphaMult,
					   float glowMult, SpecialItemRendererAPI renderer) {
		
		SpriteAPI sprite = Global.getSettings().getSprite("blueprint_packages", getId(), true);
		if (sprite.getTextureId() == 0) return; // no texture for a "holo", so no custom rendering
		
		
		float cx = x + w/2f;
		float cy = y + h/2f;
		
		w = 40;
		h = 40;
		
		float p = 1;
		float blX = cx - 12f - p;
		float blY = cy - 22f - p;
		float tlX = cx - 26f - p;
		float tlY = cy + 19f + p;
		float trX = cx + 20f + p;
		float trY = cy + 24f + p;
		float brX = cx + 34f + p;
		float brY = cy - 9f - p;
		
		List<String> ships = getProvidedShips();
		List<String> weapons = getProvidedWeapons();
		List<String> fighters = getProvidedFighters();
		boolean known = areAllKnown(ships, weapons, fighters);
		
		float mult = 1f;
		
		sprite.setAlphaMult(alphaMult * mult);
		sprite.setNormalBlend();
		sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);
		
		if (glowMult > 0) {
			sprite.setAlphaMult(alphaMult * glowMult * 0.5f * mult);
			sprite.setAdditiveBlend();
			sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);
		}
		
		if (known) {
			renderer.renderBGWithCorners(Color.black, blX, blY, tlX, tlY, trX, trY, brX, brY, 
					 alphaMult * 0.5f, 0f, false);
		}
		
		renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false);
	}

	@Override
	public int getPrice(MarketAPI market, SubmarketAPI submarket) {
		return super.getPrice(market, submarket);
	}
	
	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public String getDesignType() {
		return null;
	}
	
	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
		super.createTooltip(tooltip, expanded, transferHandler, stackSource);
		
		float pad = 3f;
		float opad = 10f;
		float small = 5f;
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color b = Misc.getButtonTextColor();
		b = Misc.getPositiveHighlightColor();
		
		List<String> ships = getProvidedShips();
		List<String> weapons = getProvidedWeapons();
		List<String> fighters = getProvidedFighters();
		
		float maxTotal = 21;
		int minPer = 3;
		
		float shipWeight = 0f;
		float weaponWeight = 0f;
		float fighterWeight = 0f;
		
		FactionAPI pf = Global.getSector().getPlayerFaction();
		
		float knownWeight = 0.25f;
		for (String id : ships) {
			if (pf.knowsShip(id)) {
				shipWeight += knownWeight;
			} else {
				shipWeight += 1f;
			}
		}
		for (String id : weapons) {
			if (pf.knowsWeapon(id)) {
				weaponWeight += knownWeight;
			} else {
				weaponWeight += 1f;
			}
		}
		for (String id : fighters) {
			if (pf.knowsFighter(id)) {
				fighterWeight += knownWeight;
			} else {
				fighterWeight += 1f;
			}
		}
		
		float totalWeight = shipWeight + weaponWeight + fighterWeight;
		if (totalWeight < knownWeight) totalWeight = knownWeight;
		
		int maxShips = (int) Math.max(minPer, (shipWeight / totalWeight) * maxTotal);
		int maxWeapons = (int) Math.max(minPer, (weaponWeight / totalWeight) * maxTotal);
		int maxFighters = (int) Math.max(minPer, (fighterWeight / totalWeight) * maxTotal);
		
		
		if (!ships.isEmpty()) {
			addShipList(tooltip, "Ship hulls:", ships, maxShips, opad);
		}
		if (!weapons.isEmpty()) {
			addWeaponList(tooltip, "Weapons:", weapons, maxWeapons, opad);
		}
		if (!fighters.isEmpty()) {
			addFighterList(tooltip, "Fighters:", fighters, maxFighters, opad);
		}
		
		
		addCostLabel(tooltip, opad, transferHandler, stackSource);

		boolean known = areAllKnown(ships, weapons, fighters);
		if (known) {
			tooltip.addPara("All blueprints in package already known", g, opad);
		} else {
			tooltip.addPara("Right-click to learn", b, opad);
		}
	}
	
	protected boolean areAllKnown(List<String> ships, List<String> weapons, List<String> fighters) {
		
		for (String hullId : ships) {
			if (!Global.getSector().getPlayerFaction().knowsShip(hullId)) {
				return false;
			}
		}
		for (String weaponId : weapons) {
			if (!Global.getSector().getPlayerFaction().knowsWeapon(weaponId)) {
				return false;
			}
		}
		for (String wingId : fighters) {
			if (!Global.getSector().getPlayerFaction().knowsFighter(wingId)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public float getTooltipWidth() {
		return super.getTooltipWidth();
	}
	
	@Override
	public boolean isTooltipExpandable() {
		return false;
	}
	
	@Override
	public boolean hasRightClickAction() {
		return true;
	}

	@Override
	public boolean shouldRemoveOnRightClickAction() {
		List<String> ships = getProvidedShips();
		List<String> weapons = getProvidedWeapons();
		List<String> fighters = getProvidedFighters();
		return !areAllKnown(ships, weapons, fighters);
	}

	@Override
	public void performRightClickAction() {
		List<String> ships = getProvidedShips();
		List<String> weapons = getProvidedWeapons();
		List<String> fighters = getProvidedFighters();
		
		if (areAllKnown(ships, weapons, fighters)) {
			Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
					"All blueprints in package already known");//,
		} else {
			Global.getSoundPlayer().playUISound("ui_acquired_blueprint", 1, 1);
			FactionAPI pf = Global.getSector().getPlayerFaction();
			int sCount = 0;
			int wCount = 0;
			int fCount = 0;
			for (String id : ships) {
				if (!pf.knowsShip(id)) sCount++;
				pf.addKnownShip(id, true);
			}
			for (String id : weapons) {
				if (!pf.knowsWeapon(id)) wCount++;
				pf.addKnownWeapon(id, true);
			}
			for (String id : fighters) {
				if (!pf.knowsFighter(id)) fCount++;
				pf.addKnownFighter(id, true);
			}
			
			if (sCount > 0) {
				Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
						"Acquired " + sCount + " ship blueprints", Misc.getTooltipTitleAndLightHighlightColor(),
						"" + sCount, Misc.getHighlightColor());
			}
			if (wCount > 0) {
				Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
						"Acquired " + wCount + " weapon blueprints", Misc.getTooltipTitleAndLightHighlightColor(),
						"" + wCount, Misc.getHighlightColor());
			}
			if (fCount > 0) {
				Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
						"Acquired " + fCount + " fighter blueprints", Misc.getTooltipTitleAndLightHighlightColor(),
						"" + fCount, Misc.getHighlightColor());
			}

		}
	}

	public static List<String> getWeaponIds(Set<String> tags) {
		List<WeaponSpecAPI> specs = Global.getSettings().getAllWeaponSpecs();
		
		Iterator<WeaponSpecAPI> iter = specs.iterator();
//		while (iter.hasNext()) {
//			WeaponSpecAPI curr = iter.next();
//			if (curr.getAIHints().contains(AIHints.SYSTEM)) {
//				iter.remove();
//			}
//		}
		
		if (!tags.isEmpty()) {
			iter = specs.iterator();
			while (iter.hasNext()) {
				WeaponSpecAPI curr = iter.next();
				for (String tag : tags) {
					boolean not = tag.startsWith("!");
					tag = not ? tag.substring(1) : tag;
					boolean has = curr.hasTag(tag);
					if (not == has) {
						iter.remove();
						break;
					}
				}
			}
		}
		
		Collections.sort(specs, new Comparator<WeaponSpecAPI>() {
			public int compare(WeaponSpecAPI o1, WeaponSpecAPI o2) {
				return o2.getSize().ordinal() - o1.getSize().ordinal();
			}
		});
		
		List<String> result = new ArrayList<String>();
		for (WeaponSpecAPI spec : specs) {
			result.add(spec.getWeaponId());
		}
		return result;
	}

	
	public static List<String> getShipIds(Set<String> tags) {
		List<ShipHullSpecAPI> specs = Global.getSettings().getAllShipHullSpecs();
		
		Iterator<ShipHullSpecAPI> iter = specs.iterator();
		
		if (!tags.isEmpty()) {
			iter = specs.iterator();
			while (iter.hasNext()) {
				ShipHullSpecAPI curr = iter.next();
//				if (tags.contains("pirate_bp") && curr.getHullId().equals("mule_d_pirates")) {
//					System.out.println(curr.getHullId());
//				}
//				if (!curr.isBaseHull()) {
//					iter.remove();
//					continue;
//				}
				
//				if (curr.hasTag("rare_bp")) {
//					System.out.println("\"" + curr.getHullId() + "\",");
//				}
				
				for (String tag : tags) {
					boolean not = tag.startsWith("!");
					tag = not ? tag.substring(1) : tag;
					boolean has = curr.hasTag(tag);
					if (not == has) {
						iter.remove();
						break;
					}
				}
			}
		} else {
			specs.clear();
		}
		
		Collections.sort(specs, new Comparator<ShipHullSpecAPI>() {
			public int compare(ShipHullSpecAPI o1, ShipHullSpecAPI o2) {
				return o2.getHullSize().ordinal() - o1.getHullSize().ordinal();
			}
		});
		
		List<String> result = new ArrayList<String>();
		for (ShipHullSpecAPI spec : specs) {
			result.add(spec.getHullId());
		}
		return result;
	}
	
	
	public static List<String> getWingIds(Set<String> tags) {
		List<FighterWingSpecAPI> specs = Global.getSettings().getAllFighterWingSpecs();
		
		Iterator<FighterWingSpecAPI> iter = specs.iterator();
		
		if (!tags.isEmpty()) {
			iter = specs.iterator();
			while (iter.hasNext()) {
				FighterWingSpecAPI curr = iter.next();
				for (String tag : tags) {
					boolean not = tag.startsWith("!");
					tag = not ? tag.substring(1) : tag;
					boolean has = curr.hasTag(tag);
					if (not == has) {
						iter.remove();
						break;
					}
				}
			}
		}
		
		List<String> result = new ArrayList<String>();
		for (FighterWingSpecAPI spec : specs) {
			result.add(spec.getId());
		}
		return result;
	}

}





