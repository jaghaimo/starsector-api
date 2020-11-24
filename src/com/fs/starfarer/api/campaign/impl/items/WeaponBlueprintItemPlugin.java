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
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class WeaponBlueprintItemPlugin extends BaseSpecialItemPlugin implements BlueprintProviderItem {

	protected WeaponSpecAPI weapon;
	
	@Override
	public void init(CargoStackAPI stack) {
		super.init(stack);
		weapon = Global.getSettings().getWeaponSpec(stack.getSpecialDataIfSpecial().getData());
	}
	
	
	public List<String> getProvidedFighters() {
		return null;
	}

	public List<String> getProvidedShips() {
		return null;
	}

	public List<String> getProvidedWeapons() {
		List<String> result = new ArrayList<String>();
		result.add(weapon.getWeaponId());
		return result;
	}
	public List<String> getProvidedIndustries() {
		return null;
	}

	@Override
	public void render(float x, float y, float w, float h, float alphaMult,
					   float glowMult, SpecialItemRendererAPI renderer) {
		float cx = x + w/2f;
		float cy = y + h/2f;

		float blX = cx - 31f;
		float blY = cy - 16f;
		float tlX = cx - 22f;
		float tlY = cy + 27f;
		float trX = cx + 23f;
		float trY = cy + 27f;
		float brX = cx + 15f;
		float brY = cy - 19f;
		
		String weaponId = stack.getSpecialDataIfSpecial().getData();
		boolean known = Global.getSector().getPlayerFaction().knowsWeapon(weaponId);
		
		float mult = 1f;
		
		Color bgColor = Global.getSector().getPlayerFaction().getDarkUIColor();
//		if (weapon.getType() == WeaponType.BALLISTIC) {
//			bgColor = Misc.getBallisticMountColor();
//		} else if (weapon.getType() == WeaponType.ENERGY) {
//			bgColor = Misc.getEnergyMountColor();
//		} else if (weapon.getType() == WeaponType.MISSILE) {
//			bgColor = Misc.getMissileMountColor();
//		}
		//bgColor = new Color(200,0,0);
		
		bgColor = Misc.setAlpha(bgColor, 255);
		renderer.renderBGWithCorners(bgColor, blX, blY, tlX, tlY, trX, trY, brX, brY, 
				 alphaMult * mult, glowMult * 0.5f * mult, false);
		//weaponId = "hurricane";
		//weaponId = "plasma";
		//weaponId = "amblaster";
		renderer.renderWeaponWithCorners(weaponId, blX, blY, tlX, tlY, trX, trY, brX, brY, 
										 alphaMult * mult, glowMult * 0.5f * mult, !known);
		
		SpriteAPI overlay = Global.getSettings().getSprite("ui", "bpOverlayWeapon");
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
		if (weapon != null) {
			float base = super.getPrice(market, submarket);
			return (int)(base + weapon.getBaseValue() * getItemPriceMult());
		}
		return super.getPrice(market, submarket);
	}
	
	@Override
	public String getName() {
		if (weapon != null) {
			return weapon.getWeaponName() + " Blueprint";
		}
		return super.getName();
	}

	@Override
	public String getDesignType() {
		if (weapon != null) {
			return weapon.getManufacturer();
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
		
		String weaponId = stack.getSpecialDataIfSpecial().getData();
		boolean known = Global.getSector().getPlayerFaction().knowsWeapon(weaponId);
		
		List<String> weapons = new ArrayList<String>();
		weapons.add(weaponId);
		addWeaponList(tooltip, "Weapons:", weapons, 1, opad);
		
		Description desc = Global.getSettings().getDescription(weapon.getWeaponId(), Type.WEAPON);
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
		String weaponId = stack.getSpecialDataIfSpecial().getData();
		return !Global.getSector().getPlayerFaction().knowsWeapon(weaponId);
	}

	@Override
	public void performRightClickAction() {
		String weaponId = stack.getSpecialDataIfSpecial().getData();
		
		if (Global.getSector().getPlayerFaction().knowsWeapon(weaponId)) {
			Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
					"" + weapon.getWeaponName() + ": blueprint already known");//,
		} else {
			Global.getSoundPlayer().playUISound("ui_acquired_blueprint", 1, 1);
			Global.getSector().getPlayerFaction().addKnownWeapon(weaponId, true);
			Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
					"Acquired blueprint: " + weapon.getWeaponName() + "");//, 

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





