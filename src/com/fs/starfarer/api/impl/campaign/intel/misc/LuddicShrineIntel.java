package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.missions.luddic.LuddicPilgrimsPath;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class LuddicShrineIntel extends BaseIntelPlugin {

	public static String VISITED = "$visitedShrine";

	
	protected SectorEntityToken entity;
	
	public static void addShrineIntelIfNeeded(String id, TextPanelAPI text) {
		addShrineIntelIfNeeded(getEntity(id), text);
	}
	public static void addShrineIntelIfNeeded(SectorEntityToken entity, TextPanelAPI text) {
		addShrineIntelIfNeeded(entity, text, false);
	}
	public static void addShrineIntelIfNeeded(SectorEntityToken entity, TextPanelAPI text, boolean quiet) {
		if (getShrineIntel(entity) == null) {
			LuddicShrineIntel intel = new LuddicShrineIntel(entity);
			Global.getSector().getIntelManager().addIntel(intel, quiet, text);
		}
	}
	
	public static void setVisited(SectorEntityToken entity, TextPanelAPI text) {
		if (isVisited(entity)) return;
		
		entity.getMemoryWithoutUpdate().set(VISITED, true);
		if (text != null) {
			LuddicShrineIntel intel = getShrineIntel(entity);
			if (intel != null) {
				Global.getSector().getIntelManager().addIntelToTextPanel(intel, text);
			}
		}
	}
	
	public static boolean isVisited(SectorEntityToken entity) {
		return entity.getMemoryWithoutUpdate().getBoolean(VISITED) ||
				(entity.getMarket() != null && 
				entity.getMarket().getMemoryWithoutUpdate().getBoolean(VISITED));
	}
	
	
	public static LuddicShrineIntel getShrineIntel(SectorEntityToken entity) {
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(LuddicShrineIntel.class)) {
			if (((LuddicShrineIntel)intel).getEntity() == entity) return (LuddicShrineIntel)intel;
		}
		return null;
	}
	
	public static SectorEntityToken getEntity(String id) {
		MarketAPI market = Global.getSector().getEconomy().getMarket(id);
		if (market != null) {
			if (market.getPlanetEntity() != null) {
				return market.getPlanetEntity();
			}
			return market.getPrimaryEntity();
		} else {
			return Global.getSector().getEntityById(id);
		}
	}
	
	
	public LuddicShrineIntel(SectorEntityToken entity) {
		this.entity = entity;
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
		
//		if (mode == ListInfoMode.INTEL) {
//			info.addPara("Located in the " + gate.getContainingLocation().getNameWithLowercaseTypeShort(), tc, initPad);
//			initPad = 0f;
//		}
		
		boolean visited = isVisited(entity);
		
		if (visited) {
			Color c = Global.getSector().getFaction(Factions.LUDDIC_CHURCH).getBaseUIColor();
			info.addPara("Visited", c, initPad);
			initPad = 0f;
		} else {
			info.addPara("Not visited", tc, initPad);
			initPad = 0f;
		}
		
		if (mode == ListInfoMode.INTEL) {
			// not sure if this is needed
			//info.addPara("In the " + entity.getContainingLocation().getNameWithLowercaseType(), tc, 0f);
		}
		
		unindent(info);
	}
	
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		String pre = "";
		String post = "";
		if (mode == ListInfoMode.MESSAGES) {
			pre = "Discovered: ";
		}
		if (mode == ListInfoMode.INTEL) {
			//post = " - " + entity.getContainingLocation().getNameWithTypeShort();
		}
		
		Color c = getTitleColor(mode);
		info.addPara(pre + getName() + post, c, 0f);
		addBulletPoints(info, mode);
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		String id = entity.getId();
		if (entity.getMarket() != null && !entity.getMarket().isPlanetConditionMarketOnly()) {
			id = entity.getMarket().getId();
		}
		
		boolean visited = isVisited(entity);
		
		if (id.equals("jangala")) {
			if (visited) {
				info.addImage(Global.getSettings().getSpriteName("illustrations", "jangala_shrine"), width, opad);
			}
			Description desc = Global.getSettings().getDescription("shrine_jangala", Type.CUSTOM);
			info.addPara(desc.getText1(), opad);
		} else if (id.equals("volturn")) {
			if (visited) {
				info.addImage(Global.getSettings().getSpriteName("illustrations", "luddic_shrine"), width, opad);
			}
			Description desc = Global.getSettings().getDescription("shrine_volturn", Type.CUSTOM);
			info.addPara(desc.getText1(), opad);
		} else if (id.equals("hesperus")) {
			if (visited) {
				info.addImage(Global.getSettings().getSpriteName("illustrations", "hesperus_shrine"), width, opad);
			}
			Description desc = Global.getSettings().getDescription("shrine_hesperus", Type.CUSTOM);
			info.addPara(desc.getText1(), opad);
		} else if (id.equals("gilead")) {
			if (visited) {
				info.addImage(Global.getSettings().getSpriteName("illustrations", "gilead_shrine"), width, opad);
			}
			Description desc = Global.getSettings().getDescription("shrine_gilead", Type.CUSTOM);
			info.addPara(desc.getText1(), opad);
		} else if (id.equals("beholder_station")) {
			if (visited) {
				info.addImage(Global.getSettings().getSpriteName("illustrations", "luddic_shrine"), width, opad);
				//info.addImage(entity.getCustomInteractionDialogImageVisual().getSpriteName(), width, opad);
			}
			
			// this is probably fine for Beholder Station, but would just get the market description for
			// shrines that are on actual colonies
			Description desc = Global.getSettings().getDescription("shrine_beholder", Type.CUSTOM);
			info.addPara(desc.getText1(), opad);
			
		} else if (id.equals("killa")) {
			if (visited) {
				info.addImage(Global.getSettings().getSpriteName("illustrations", "killa_shrine"), width, opad);
				//info.addImage(entity.getCustomInteractionDialogImageVisual().getSpriteName(), width, opad);
			}
			Description desc = Global.getSettings().getDescription("shrine_killa", Type.CUSTOM);
			info.addPara(desc.getText1(), opad);
		}
	
		
//		String buttonText = "Show shrines";
//		if (info.getIntelUI().isShowingCustomIntelSubset()) {
//			buttonText = "Go back";
//		}
//		
//		info.addSpacer(height - info.getHeightSoFar() - 20f - 20f);
//		ButtonAPI button = addGenericButton(info, width, buttonText, BUTTON_SHOW_SHRINES);
//		button.setShortcut(Keyboard.KEY_T, true);
		addShowShrinesButton(this, width, height, info);
		
		//addBulletPoints(info, ListInfoMode.IN_DESC);
		
	}

	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "luddic_shrine");
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_SHRINES);
		return tags;
	}
	
	public String getSortString() {
		return getName();
	}
	
	public String getName() {
//		String onOrAt = "on";
//		if (entity.getMarket() != null && !entity.getMarket().isPlanetConditionMarketOnly()) {
//			onOrAt = entity.getMarket().getOnOrAt();
//		}
//		return "Luddic Shrine " + onOrAt + " " + entity.getName();
		return "Luddic Shrine - " + entity.getName();
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return Global.getSector().getFaction(Factions.LUDDIC_CHURCH);
	}

	public String getSmallDescriptionTitle() {
		//return getName() + " - " + entity.getContainingLocation().getNameWithTypeShort();
		return getName();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return entity;
	}
	
	@Override
	public boolean shouldRemoveIntel() {
		return false;
	}

	@Override
	public String getCommMessageSound() {
		return "ui_discovered_entity";
	}

	public SectorEntityToken getEntity() {
		return entity;
	}
	
	
	public static String BUTTON_SHOW_SHRINES = "button_show_shrines";
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_SHOW_SHRINES) {
			toggleShrineList(this, ui);
			return;
		}
		super.buttonPressConfirmed(buttonId, ui);
	}
	
	public static void addShowShrinesButton(IntelInfoPlugin curr, float width, float height, TooltipMakerAPI info) {
		if (!Global.getSector().getIntelManager().hasIntelOfClass(LuddicPilgrimsPath.class)) return;
		
		if (!info.getIntelUI().isShowingCustomIntelSubset() && 
				curr instanceof LuddicShrineIntel) {
			return;
		}
		
		String buttonText = "Show shrines";
		if (info.getIntelUI().isShowingCustomIntelSubset()) {
			buttonText = "Go back";
		}
		
		info.addSpacer(height - info.getHeightSoFar() - 20f - 20f);
		ButtonAPI button = ((BaseIntelPlugin)curr).addGenericButton(info, width, buttonText, BUTTON_SHOW_SHRINES);
		button.setShortcut(Keyboard.KEY_T, true);
	}
	
	public static void toggleShrineList(IntelInfoPlugin curr, IntelUIAPI ui) {
		if (ui.isShowingCustomIntelSubset()) {
			ui.updateIntelList(true);
			ui.updateUIForItem(curr);
			//for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(LuddicShrineIntel.class)) {
			for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(LuddicPilgrimsPath.class)) {
				ui.selectItem(intel);
				break;
			}
		} else {
			List<IntelInfoPlugin> show = new ArrayList<IntelInfoPlugin>();
			for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(LuddicPilgrimsPath.class)) {
				show.add(intel);
			}
			for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(LuddicShrineIntel.class)) {
				show.add(intel);
			}
			Global.getSector().getIntelManager().sortIntel(show);
			ui.updateIntelList(true, show);
			ui.updateUIForItem(curr);
		}
		return;
	}
	
}
