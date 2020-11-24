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
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class IndustryBlueprintItemPlugin extends BaseSpecialItemPlugin implements BlueprintProviderItem {

	protected IndustrySpecAPI industry;
	
	@Override
	public void init(CargoStackAPI stack) {
		super.init(stack);
		industry = Global.getSettings().getIndustrySpec(stack.getSpecialDataIfSpecial().getData());
	}
	
	public List<String> getProvidedFighters() {
		return null;
	}

	public List<String> getProvidedShips() {
		return null;
	}

	public List<String> getProvidedWeapons() {
		return null;
	}
	public List<String> getProvidedIndustries() {
		List<String> result = new ArrayList<String>();
		result.add(industry.getId());
		return result;
	}
	

	@Override
	public void render(float x, float y, float w, float h, float alphaMult,
					   float glowMult, SpecialItemRendererAPI renderer) {
		float cx = x + w/2f;
		float cy = y + h/2f;
		
		float blX = cx -25f;
		float blY = cy -14f;
		float tlX = cx -30f;
		float tlY = cy +16f;
		float trX = cx +24f;
		float trY = cy +22f;
		float brX = cx +30f;
		float brY = cy -6f;
		
		SpriteAPI sprite = Global.getSettings().getSprite(industry.getImageName());
		
		String industryId = stack.getSpecialDataIfSpecial().getData();
		boolean known = Global.getSector().getPlayerFaction().knowsIndustry(industryId);
		
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
		if (industry != null) {
			float base = super.getPrice(market, submarket);
			return (int)(base + industry.getCost() * getItemPriceMult());
		}
		return super.getPrice(market, submarket);
	}
	
	@Override
	public String getName() {
		if (industry != null) {
			return industry.getName() + " Blueprint";
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
		
		String weaponId = stack.getSpecialDataIfSpecial().getData();
		boolean known = Global.getSector().getPlayerFaction().knowsWeapon(weaponId);
		
		List<String> weapons = new ArrayList<String>();
		weapons.add(weaponId);
		
		tooltip.addPara(industry.getDesc(), opad);
		
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
		String industryId = stack.getSpecialDataIfSpecial().getData();
		return !Global.getSector().getPlayerFaction().knowsIndustry(industryId);
	}

	@Override
	public void performRightClickAction() {
		String industryId = stack.getSpecialDataIfSpecial().getData();
		
		if (Global.getSector().getPlayerFaction().knowsIndustry(industryId)) {
			Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
					"" + industry.getName() + ": blueprint already known");//,
		} else {
			Global.getSoundPlayer().playUISound("ui_acquired_blueprint", 1, 1);
			Global.getSector().getPlayerFaction().addKnownIndustry(industryId);
			Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
					"Acquired blueprint: " + industry.getName() + "");//, 

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
		
		return pickWeapon(tier, tags, random);
	}

	
	protected String pickWeapon(int tier, Set<String> tags, Random random) {
		List<WeaponSpecAPI> specs = Global.getSettings().getAllWeaponSpecs();
		
		Iterator<WeaponSpecAPI> iter = specs.iterator();
		while (iter.hasNext()) {
			WeaponSpecAPI curr = iter.next();
			if (curr.getAIHints().contains(AIHints.SYSTEM)) {
				iter.remove();
			}
		}
		
		if (tier >= 0) {
			iter = specs.iterator();
			while (iter.hasNext()) {
				WeaponSpecAPI curr = iter.next();
				if (curr.getTier() != tier) iter.remove();
			}
		}
		
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
		
		WeightedRandomPicker<WeaponSpecAPI> picker = new WeightedRandomPicker<WeaponSpecAPI>(random);
		for (WeaponSpecAPI spec : specs) {
			picker.add(spec, 1f * spec.getRarity());
		}
		WeaponSpecAPI pick = picker.pick();
		if (pick == null) {
			return null;
		} else {
			return pick.getWeaponId(); 
		}
	}
	
}





