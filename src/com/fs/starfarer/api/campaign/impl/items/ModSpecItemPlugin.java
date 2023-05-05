package com.fs.starfarer.api.campaign.impl.items;

import java.awt.Color;
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
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ModSpecItemPlugin extends BaseSpecialItemPlugin {
	private String modId;
	private HullModSpecAPI modSpec;
	//private SpriteAPI sprite;

	@Override
	public void init(CargoStackAPI stack) {
		super.init(stack);
		modId = stack.getSpecialDataIfSpecial().getData();
		modSpec = Global.getSettings().getHullModSpec(modId);
		
		//sprite = Global.getSettings().getSprite(modSpec.getSpriteName());
	}

	@Override
	public void render(float x, float y, float w, float h, float alphaMult,
					   float glowMult, SpecialItemRendererAPI renderer) {
		float cx = x + w/2f;
		float cy = y + h/2f;
		
		cx -= 2;
		cy -= 1;
		
		x = (int) x;
		y = (int) y;
		w = (int) w;
		h = (int) h;
		
		boolean known = Global.getSector().getCharacterData().knowsHullMod(modId);
		float mult = 1f;
		if (known) {
			mult = 0.5f;
		}
		
		SpriteAPI sprite = Global.getSettings().getSprite(modSpec.getSpriteName());
		
//		sprite.setNormalBlend();
//		sprite.setAlphaMult(alphaMult * mult);
//		
//		sprite.renderAtCenter(cx, cy);
//		
//		if (glowMult > 0) {
//			sprite.setAlphaMult(alphaMult * glowMult * 0.5f * mult);
//			sprite.renderAtCenter(cx, cy);
//		}
//		
//		renderer.renderScanlines(sprite, cx, cy, alphaMult);
//		if (!known) {
//			renderer.renderSchematic(sprite, cx, cy, alphaMult * 0.67f);
//		}
//		
//		if (true) return;
		
		
		w = sprite.getWidth() * 1.5f + 5;
		h = sprite.getHeight() * 1.5f;
//		x = cx - w / 2f;
//		y = cy - h / 2f;
		
		x = cx;
		y = cy;
		
//		float blX = x - w / 2f;
//		float blY = y - h / 2f + 7;
//		float tlX = x - w / 2f + 10;
//		float tlY = y + h / 2f - 7;
//		float trX = x + w / 2f;
//		float trY = y + h / 2f;
//		float brX = x + w / 2f - 10;
//		float brY = y - h / 2f;
		
// modspec.png
//		float blX = -23f;
//		float blY = -10f;
//		float tlX = -14f;
//		float tlY = 21f;
//		float trX = 25f;
//		float trY = 20f;
//		float brX = 23f;
//		float brY = -10f;

// modspec2.png
		float blX = -23f;
		float blY = -10f;
		float tlX = -18f;
		float tlY = 24f;
		float trX = 24f;
		float trY = 24f;
		float brX = 23f;
		float brY = -10f;
		
//		float tilt = 5;
//		float b = Global.getSector().getCampaignUI().getSharedFader().getBrightness();
//		tilt = 20f * b - 10f;
//		w = 100;
//		h = 100;
//		tilt = 10;
		
		sprite.setAlphaMult(alphaMult * mult);
		sprite.setNormalBlend();
		sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);
		
		
		if (glowMult > 0) {
			sprite.setAlphaMult(alphaMult * glowMult * 0.5f * mult);
			//sprite.renderAtCenter(cx, cy);
			sprite.setAdditiveBlend();
			sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);
		}
		
		//renderer.renderScanlines(sprite, cx, cy, alphaMult);
		renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false);
		if (!known) {
			//renderer.renderSchematic(sprite, cx, cy, alphaMult * 0.67f);
			renderer.renderSchematicWithCorners(sprite, null, blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult * 0.67f);
		}
	}

	@Override
	public int getPrice(MarketAPI market, SubmarketAPI submarket) {
		if (modSpec != null) return (int) modSpec.getBaseValue();
		return super.getPrice(market, submarket);
	}
	
	@Override
	public String getName() {
		return modSpec.getDisplayName() + " - Modspec";
	}

	
	@Override
	public String getDesignType() {
		if (modSpec != null) {
			return modSpec.getManufacturer();
		}
		return null;
	}
	
	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
		//super.createTooltip(tooltip, expanded, transferHandler, stackSource);
		
		float pad = 3f;
		float opad = 10f;
		float small = 5f;
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color b = Misc.getButtonTextColor();
		b = Misc.getPositiveHighlightColor();

		tooltip.addTitle(getName());
		
		String design = getDesignType();
		Misc.addDesignTypePara(tooltip, design, 10f);
		
		if (!spec.getDesc().isEmpty()) {
			tooltip.addPara(spec.getDesc(), Misc.getGrayColor(), opad);
		}
		
		tooltip.addSectionHeading("Effect", Alignment.MID, opad * 1f);
		
		HullModEffect e = modSpec.getEffect();
		HullSize size = HullSize.CAPITAL_SHIP;
		if (e.shouldAddDescriptionToTooltip(size, null, true)) {
			final String [] params = new String [] { 
					 e.getDescriptionParam(0, size, null),
					 e.getDescriptionParam(1, size, null),
					 e.getDescriptionParam(2, size, null),
					 e.getDescriptionParam(3, size, null),
					 e.getDescriptionParam(4, size, null),
					 e.getDescriptionParam(5, size, null),
					 e.getDescriptionParam(6, size, null),
					 e.getDescriptionParam(7, size, null),
					 e.getDescriptionParam(8, size, null),
					 e.getDescriptionParam(9, size, null)
				};
			
			tooltip.addPara(modSpec.getDescription(size).replaceAll("\\%", "%%"), opad, h, params);
			//e.getDescriptionParam(0, size);
		}
		
		e.addPostDescriptionSection(tooltip, size, null, getTooltipWidth(), true);
		
		if (e.hasSModEffectSection(size, null, false)) {
			e.addSModSection(tooltip, size, null, getTooltipWidth(), true, false);
		}
		
		addCostLabel(tooltip, opad, transferHandler, stackSource);
		
		//if (CampaignEngine.getInstance().getCharacterData().getPerson().getStats().isHullModAvailable(id)) {
		//tooltip.setParaSmallOrbitron();
		
		boolean known = Global.getSector().getCharacterData().knowsHullMod(modId);
		if (known) {
			//tooltip.addPara("Already known", g, pad).setAlignment(Alignment.MID);
			tooltip.addPara("Already known", g, opad);
		} else {
			//tooltip.addPara("Right-click to learn", b, pad).setAlignment(Alignment.MID);
			tooltip.addPara("Right-click to learn", b, opad);
		}
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
		return !Global.getSector().getCharacterData().knowsHullMod(modId);
	}

	@Override
	public void performRightClickAction() {
		if (Global.getSector().getCharacterData().knowsHullMod(modId)) {
			Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
					"" + modSpec.getDisplayName() + ": already known");//,
		} else {
			Global.getSoundPlayer().playUISound("ui_acquired_hullmod", 1, 1);
			Global.getSector().getCharacterData().addHullMod(modId);
			Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
					"Acquired hull mod: " + modSpec.getDisplayName() + "");//, 

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
		
		List<HullModSpecAPI> specs = Global.getSettings().getAllHullModSpecs();
		
		Iterator<HullModSpecAPI> iter = specs.iterator();
		while (iter.hasNext()) {
			HullModSpecAPI curr = iter.next();
			boolean known = Global.getSector().getPlayerFaction().knowsHullMod(curr.getId());
			if (DebugFlags.ALLOW_KNOWN_HULLMOD_DROPS) known = false;
			if (known || curr.isHidden() || curr.isHiddenEverywhere() || curr.hasTag(Tags.HULLMOD_NO_DROP)) {
				iter.remove();
				continue;
			}
		}
		
		if (tier >= 0) {
			iter = specs.iterator();
			while (iter.hasNext()) {
				HullModSpecAPI curr = iter.next();
				if (curr.getTier() != tier) iter.remove();
			}
		}
		
		if (!tags.isEmpty()) {
			iter = specs.iterator();
			while (iter.hasNext()) {
				HullModSpecAPI curr = iter.next();
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
		
		WeightedRandomPicker<HullModSpecAPI> picker = new WeightedRandomPicker<HullModSpecAPI>(random);
		for (HullModSpecAPI spec : specs) {
			picker.add(spec, 1f * spec.getRarity());
		}
		HullModSpecAPI pick = picker.pick();
		if (pick == null) {
			return null;
		} else {
			return pick.getId(); 
		}
	}

	public String getModId() {
		return modId;
	}

	
	
}





