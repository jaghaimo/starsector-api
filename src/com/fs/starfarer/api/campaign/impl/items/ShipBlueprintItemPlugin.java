package com.fs.starfarer.api.campaign.impl.items;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import java.awt.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ShipBlueprintItemPlugin extends BaseSpecialItemPlugin implements BlueprintProviderItem {

	protected ShipHullSpecAPI ship;
	
	@Override
	public void init(CargoStackAPI stack) {
		super.init(stack);
		ship = Global.getSettings().getHullSpec(stack.getSpecialDataIfSpecial().getData());
	}
	
	public List<String> getProvidedFighters() {
		return null;
	}

	public List<String> getProvidedShips() {
		List<String> result = new ArrayList<String>();
		result.add(ship.getHullId());
		return result;
	}

	public List<String> getProvidedWeapons() {
		return null;
	}
	public List<String> getProvidedIndustries() {
		return null;
	}
	

	@Override
	public void render(float x, float y, float w, float h, float alphaMult,
					   float glowMult, SpecialItemRendererAPI renderer) {
		float cx = x + w/2f;
		float cy = y + h/2f;

		float blX = cx - 30f;
		float blY = cy - 15f;
		float tlX = cx - 20f;
		float tlY = cy + 26f;
		float trX = cx + 23f;
		float trY = cy + 26f;
		float brX = cx + 15f;
		float brY = cy - 18f;
		
		String hullId = stack.getSpecialDataIfSpecial().getData();
		
		boolean known = Global.getSector().getPlayerFaction().knowsShip(hullId);
		
		float mult = 1f;
		//if (known) mult = 0.5f;
		
		Color bgColor = Global.getSector().getPlayerFaction().getDarkUIColor();
		bgColor = Misc.setAlpha(bgColor, 255);
		
		//float b = Global.getSector().getCampaignUI().getSharedFader().getBrightness() * 0.25f;
		renderer.renderBGWithCorners(bgColor, blX, blY, tlX, tlY, trX, trY, brX, brY, 
				 alphaMult * mult, glowMult * 0.5f * mult, false);
		renderer.renderShipWithCorners(hullId, null, blX, blY, tlX, tlY, trX, trY, brX, brY, 
				alphaMult * mult, glowMult * 0.5f * mult, !known);
		
		
		SpriteAPI overlay = Global.getSettings().getSprite("ui", "bpOverlayShip");
		overlay.setColor(Color.green);
		overlay.setColor(Global.getSector().getPlayerFaction().getBrightUIColor());
		overlay.setAlphaMult(alphaMult);
		overlay.setNormalBlend();
		renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false);
		
		
		if (known) {
			renderer.renderBGWithCorners(Color.black, blX, blY, tlX, tlY, trX, trY, brX, brY, 
					alphaMult * 0.5f, 0f, false);
		}
		
		
		overlay.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);
	}

	@Override
	public int getPrice(MarketAPI market, SubmarketAPI submarket) {
		if (ship != null) {
			//float base = super.getPrice(market, submarket);
			float base = 0;
			switch (ship.getHullSize()) {
			case CAPITAL_SHIP:
				base = Global.getSettings().getFloat("blueprintBasePriceCapital");;
				break;
			case CRUISER:
				base = Global.getSettings().getFloat("blueprintBasePriceCruiser");;
				break;
			case DESTROYER:
				base = Global.getSettings().getFloat("blueprintBasePriceDestroyer");;
				break;
			case FRIGATE:
			case FIGHTER:
				base = Global.getSettings().getFloat("blueprintBasePriceFrigate");;
				break;
			}
			return (int)(base + ship.getBaseValue() * getItemPriceMult());
		}
		return super.getPrice(market, submarket);
	}
	
	@Override
	protected float getItemPriceMult() {
		return Global.getSettings().getFloat("blueprintPriceOriginalShipMult");
	}
	
	@Override
	public String getName() {
		if (ship != null) {
			//return ship.getHullName() + " Blueprint";
			return ship.getNameWithDesignationWithDashClass() + " Blueprint";
		}
		return super.getName();
	}
	
	@Override
	public String getDesignType() {
		if (ship != null) {
			return ship.getManufacturer();
		}
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

		
		String hullId = stack.getSpecialDataIfSpecial().getData();
		boolean known = Global.getSector().getPlayerFaction().knowsShip(hullId);
		
		List<String> hulls = new ArrayList<String>();
		hulls.add(hullId);
		addShipList(tooltip, "Ship hulls:", hulls, 1, opad);
		Description desc = Global.getSettings().getDescription(ship.getDescriptionId(), Type.SHIP);
		
		String prefix = "";
		if (ship.getDescriptionPrefix() != null) {
			prefix = ship.getDescriptionPrefix() + "\n\n";
		}
		tooltip.addPara(prefix + desc.getText1FirstPara(), opad);
		
		addCostLabel(tooltip, opad, transferHandler, stackSource);
		
		if (known) {
			tooltip.addPara("Already known", g, opad);
		} else {
			tooltip.addPara("Right-click to learn", b, opad);
		}
	}

	@Override
	public boolean hasRightClickAction() {
		return true;
	}

	@Override
	public boolean shouldRemoveOnRightClickAction() {
		String hullId = stack.getSpecialDataIfSpecial().getData();
		return !Global.getSector().getPlayerFaction().knowsShip(hullId);
	}

	@Override
	public void performRightClickAction() {
		String hullId = stack.getSpecialDataIfSpecial().getData();
		
//		ShipHullSpecAPI spec = Global.getSettings().getHullSpec(hullId);
//		if (spec != null && spec.hasTag(Tags.CODEX_UNLOCKABLE)) {
//			String baseId = CodexDataV2.getBaseHullId(spec);
//			SharedUnlockData.get().reportPlayerAwareOfShip(baseId, true);
//		}
		
		if (Global.getSector().getPlayerFaction().knowsShip(hullId)) {
			Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
					"" + ship.getNameWithDesignationWithDashClass() + ": blueprint already known");//,
		} else {
			Global.getSoundPlayer().playUISound("ui_acquired_blueprint", 1, 1);
			Global.getSector().getPlayerFaction().addKnownShip(hullId, true);
			
			Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
					"Acquired blueprint: " + ship.getNameWithDesignationWithDashClass() + "");//, 

		}
	}

	@Override
	public String resolveDropParamsToSpecificItemData(String params, Random random) throws JSONException {
		if (params == null || params.isEmpty()) return null;
		
		if (!params.startsWith("{")) {
			return params;
		}
		
		JSONObject json = new JSONObject(params);
		
		Set<String> tags = new HashSet<String>();
		if (json.has("tags")) {
			JSONArray tagsArray = json.getJSONArray("tags");
			for (int i = 0; i < tagsArray.length(); i++) {
				tags.add(tagsArray.getString(i));
			}
		}
		
		return pickShip(tags, random);
	}

	
	public static String pickShip(Set<String> tags, Random random) {
		List<ShipHullSpecAPI> specs = Global.getSettings().getAllShipHullSpecs();
		
		Iterator<ShipHullSpecAPI> iter = specs.iterator();
//		while (iter.hasNext()) {
//			ShipHullSpecAPI curr = iter.next();
//			if (curr.getHints().contains(ShipTypeHints.UNBOARDABLE) {
//				iter.remove();
//			}
//		}
		
//		if (tier >= 0) {
//			iter = specs.iterator();
//			while (iter.hasNext()) {
//				ShipHullSpecAPI curr = iter.next();
//				if (curr.getTier() != tier) iter.remove();
//			}
//		}
		
		if (!tags.isEmpty()) {
			iter = specs.iterator();
			while (iter.hasNext()) {
				ShipHullSpecAPI curr = iter.next();
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
		
		WeightedRandomPicker<ShipHullSpecAPI> picker = new WeightedRandomPicker<ShipHullSpecAPI>(random);
		for (ShipHullSpecAPI spec : specs) {
			picker.add(spec, 1f * spec.getRarity());
		}
		ShipHullSpecAPI pick = picker.pick();
		if (pick == null) {
			return null;
		} else {
			return pick.getHullId(); 
		}
	}
	
}





