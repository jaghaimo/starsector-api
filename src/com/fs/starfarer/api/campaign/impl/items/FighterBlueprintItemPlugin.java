package com.fs.starfarer.api.campaign.impl.items;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class FighterBlueprintItemPlugin extends BaseSpecialItemPlugin implements BlueprintProviderItem {

	protected FighterWingSpecAPI wing;
	protected ShipVariantAPI fighter;
	
	@Override
	public void init(CargoStackAPI stack) {
		super.init(stack);
		wing = Global.getSettings().getFighterWingSpec(stack.getSpecialDataIfSpecial().getData());
		fighter = wing.getVariant();
	}
	
	
	public List<String> getProvidedFighters() {
		List<String> result = new ArrayList<String>();
		result.add(wing.getId());
		return result;
	}

	public List<String> getProvidedShips() {
		return null;
	}

	public List<String> getProvidedWeapons() {
		return null;
	}
	public List<String> getProvidedIndustries() {
		return null;
	}
	
	@Override
	public String getDesignType() {
		if (fighter != null) {
			return fighter.getHullSpec().getManufacturer();
		}
		return null;
	}

	@Override
	public void render(float x, float y, float w, float h, float alphaMult,
					   float glowMult, SpecialItemRendererAPI renderer) {
		float cx = x + w/2f;
		float cy = y + h/2f;

		w = 40;
		h = 40;
		
		float blX = cx - 24f;
		float blY = cy - 17f;
		float tlX = cx - 14f;
		float tlY = cy + 26f;
		float trX = cx + 28f;
		float trY = cy + 25f;
		float brX = cx + 20f;
		float brY = cy - 18f;
		
		String wingId = stack.getSpecialDataIfSpecial().getData();
		boolean known = Global.getSector().getPlayerFaction().knowsFighter(wingId);
		
		float mult = 1f;
		
		Color bgColor = Global.getSector().getPlayerFaction().getDarkUIColor();
		bgColor = Misc.setAlpha(bgColor, 255);
		renderer.renderBGWithCorners(bgColor, blX, blY, tlX, tlY, trX, trY, brX, brY, 
				 alphaMult * mult, glowMult * 0.5f * mult, false);
		renderer.renderShipWithCorners(wingId, null, blX, blY, tlX, tlY, trX, trY, brX, brY, 
										 alphaMult * mult, glowMult * 0.5f * mult, !known);
		
		SpriteAPI overlay = Global.getSettings().getSprite("ui", "bpOverlayFighter");
		overlay.setColor(Color.green);
		overlay.setColor(Global.getSector().getPlayerFaction().getBrightUIColor());
		overlay.setAlphaMult(alphaMult);
		overlay.setNormalBlend();
		renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false);
		
		if (known) {
			renderer.renderBGWithCorners(Color.black, blX, blY, tlX, tlY, trX, trY, brX, brY, 
					 alphaMult * 0.5f, 0f, false);
		}
		
		//renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false);
		
		overlay.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);
	}

	@Override
	public int getPrice(MarketAPI market, SubmarketAPI submarket) {
		if (wing != null) {
			float base = super.getPrice(market, submarket);
			return (int)(base + wing.getBaseValue() * getItemPriceMult());
		}
		return super.getPrice(market, submarket);
	}
	
	@Override
	public String getName() {
		if (wing != null) {
			return wing.getWingName() + " Blueprint";
		}
		return super.getName();
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
		
		String wingId = stack.getSpecialDataIfSpecial().getData();
		boolean known = Global.getSector().getPlayerFaction().knowsFighter(wingId);
		
		List<String> wings = new ArrayList<String>();
		wings.add(wingId);
		addFighterList(tooltip, "Fighters:", wings, 1, opad);
		
		Description desc = Global.getSettings().getDescription(fighter.getHullSpec().getDescriptionId(), Type.SHIP);
		tooltip.addPara(desc.getText1FirstPara(), opad);
		
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
		String wingId = stack.getSpecialDataIfSpecial().getData();
		return !Global.getSector().getPlayerFaction().knowsFighter(wingId);
	}

	@Override
	public void performRightClickAction() {
		String wingId = stack.getSpecialDataIfSpecial().getData();
		
		if (Global.getSector().getPlayerFaction().knowsFighter(wingId)) {
			Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
					"" + wing.getWingName() + ": blueprint already known");//,
		} else {
			Global.getSoundPlayer().playUISound("ui_acquired_blueprint", 1, 1);
			Global.getSector().getPlayerFaction().addKnownFighter(wingId, true);
			Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
					"Acquired blueprint: " + wing.getWingName() + "");//, 

		}
	}

	@Override
	public String resolveDropParamsToSpecificItemData(String params, Random random) throws JSONException {
		if (params == null || params.isEmpty()) return null;
		
		
		JSONObject json = new JSONObject(params);
		
		int tier = json.optInt("tier", -1);
		Set<String> tags = new HashSet<String>();
		if (json.has("tags")) {
			JSONArray tagsArray = json.getJSONArray("tags");
			for (int i = 0; i < tagsArray.length(); i++) {
				tags.add(tagsArray.getString(i));
			}
		}
		
		return pickFighterWing(tier, tags, random);
	}

	
	protected String pickFighterWing(int tier, Set<String> tags, Random random) {
		List<FighterWingSpecAPI> specs = Global.getSettings().getAllFighterWingSpecs();
		
		Iterator<FighterWingSpecAPI> iter = specs.iterator();
		while (iter.hasNext()) {
			FighterWingSpecAPI curr = iter.next();
			if (curr.hasTag(Tags.WING_NO_DROP)) {
				iter.remove();
			}
		}
		
		if (tier >= 0) {
			iter = specs.iterator();
			while (iter.hasNext()) {
				FighterWingSpecAPI curr = iter.next();
				if (curr.getTier() != tier) iter.remove();
			}
		}
		
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
		
		WeightedRandomPicker<FighterWingSpecAPI> picker = new WeightedRandomPicker<FighterWingSpecAPI>(random);
		for (FighterWingSpecAPI spec : specs) {
			picker.add(spec, 1f * spec.getRarity());
		}
		FighterWingSpecAPI pick = picker.pick();
		if (pick == null) {
			return null;
		} else {
			return pick.getId(); 
		}
	}
	
}





