package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.util.Set;

import java.awt.Color;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * @author Alex
 *
 */
public class MapMarkerIntel extends FleetLogIntel {

	public static String BUTTON_EDIT = "button_edit";
	
	protected SectorEntityToken entity;
	protected SectorEntityToken copy;
	
	protected String title;
	protected String text;
	private boolean withDesc;
	
	private boolean withDeleteButton = true;
	private boolean withTimestamp = true;

	
	public MapMarkerIntel() {
	}
	
	public MapMarkerIntel(SectorEntityToken entity, String title, String text, String icon, boolean withDesc) {
		super();
		init(entity, title, text, icon, withDesc);
	}
	
	protected void init(SectorEntityToken entity, String title, String text, String icon, boolean withDesc) {
		init(entity, title, text, icon, withDesc, null);
	}
	protected void init(SectorEntityToken entity, String title, String text, String icon, boolean withDesc, TextPanelAPI textPanel) {
		this.entity = entity;
		this.withDesc = withDesc;
		//setRemoveTrigger(this.entity);
		
		// otherwise a marker on an already-explored debris field auto-removes immediately
		if (entity instanceof CampaignTerrainAPI) {
			CampaignTerrainAPI terrain = (CampaignTerrainAPI) entity;
			if (terrain.getPlugin() instanceof DebrisFieldTerrainPlugin) {
				DebrisFieldTerrainPlugin debris = (DebrisFieldTerrainPlugin) terrain.getPlugin();
				if (debris.isScavenged()) {
					setKeepExploredDebrisField(true);
				}
			}
		}
		
		
		if (entity instanceof CampaignFleetAPI && !((CampaignFleetAPI) entity).isStationMode()) {
			copy = makeDoubleWithSameOrbit(entity);
			copy.getContainingLocation().addEntity(copy);
			setRemoveTrigger(copy);
		} else {
			setRemoveTrigger(this.entity);
		}
		
		this.title = title;
		this.text = text;
		setIcon(icon);
		
		setListInfoParam(DISCOVERED_PARAM);
		Global.getSector().getIntelManager().addIntel(this, false, textPanel);
		setListInfoParam(null);
	}
	
	@Override
	public void reportRemovedIntel() {
		super.reportRemovedIntel();
		if (copy != null && copy.getContainingLocation() != null) {
			copy.getContainingLocation().removeEntity(copy);
		}
	}

	public static SectorEntityToken makeDoubleWithSameOrbit(SectorEntityToken entity) {
		SectorEntityToken copy = entity.getContainingLocation().createToken(entity.getLocation().x, entity.getLocation().y);
		if (entity.getOrbit() != null) {
			copy.setOrbit(entity.getOrbit().makeCopy());
		}
		return copy;
	}
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public SectorEntityToken getEntity() {
		return entity;
	}

	public void setEntity(SectorEntityToken entity) {
		this.entity = entity;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isWithDesc() {
		return withDesc;
	}

	public void setWithDesc(boolean withDesc) {
		this.withDesc = withDesc;
	}

	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		addBulletPoints(info, mode);
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		//if (text == null || text.trim().isEmpty()) return;
		
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);
		boolean isUpdate = getListInfoParam() != null;
		
		bullet(info);
		if (text != null && !text.isEmpty()) {
			String str = text;
			if (endsInPunct(str, true)) {
				str = str.substring(0, str.length() - 1);
			}
			info.addPara(str, tc, initPad);
			initPad = 0f;
		}
		addExtraBulletPoints(info, tc, initPad, mode);
		unindent(info);
	}
	
	protected void addExtraBulletPoints(TooltipMakerAPI info, Color tc, float initPad, ListInfoMode mode) {
		
	}
	
	protected boolean endsInPunct(String str, boolean forBulletPoint) {
		String punct = ",.?!:;";
		if (forBulletPoint) {
			punct = ",.:;";
		}
		String end = "" + str.charAt(str.length() - 1);
		return punct.contains(end);
	}
	
	
	
	protected boolean withTextInDesc() {
		return true;
	}
	protected boolean withCustomVisual() {
		return false;
	}
	protected boolean withCustomDescription() {
		return false;
	}
	
	protected void addCustomVisual(TooltipMakerAPI info, float width, float height) {
		
	}
	protected void addCustomDescription(TooltipMakerAPI info, float width, float height) {
		
	}
	
	protected void addPostDescriptionSection(TooltipMakerAPI info, float width, float height, float opad) {
		
	}
	
	public boolean isWithDeleteButton() {
		return withDeleteButton;
	}

	public void setWithDeleteButton(boolean withDeleteButton) {
		this.withDeleteButton = withDeleteButton;
	}

	public boolean isWithTimestamp() {
		return withTimestamp;
	}

	public void setWithTimestamp(boolean withTimestamp) {
		this.withTimestamp = withTimestamp;
	}

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;


		boolean addedDesc = false;
		if (withDesc) {
			if (withCustomVisual()) {
				addCustomVisual(info, width, height);
			} else if (entity.getCustomInteractionDialogImageVisual() != null) {
				info.addImage(entity.getCustomInteractionDialogImageVisual().getSpriteName(), width, opad);
			}
			
			if (withCustomDescription()) {
				addCustomDescription(info, width, height);
				addedDesc = true;
			} else if (entity.getCustomDescriptionId() != null) {
				Description desc = Global.getSettings().getDescription(entity.getCustomDescriptionId(), Type.CUSTOM);
				info.addPara(desc.getText1(), opad);
				addedDesc = true;
			}
		}
		
		if (text != null && !text.trim().isEmpty() && withTextInDesc()) {
			if (addedDesc) {
				addBulletPoints(info, ListInfoMode.IN_DESC);
			} else {
				info.addPara(text + (endsInPunct(text, false) ? "" : "."), opad);
			}
		}
		
		addPostDescriptionSection(info, width, height, opad);
		//target.getOrbit().updateLocation();
		
		if (isWithTimestamp()) {
			addLogTimestamp(info, tc, opad);
		}
		
		
		if (getClass() == MapMarkerIntel.class) {
			ButtonAPI button = info.addButton("Edit", BUTTON_EDIT, 
					  	getFactionForUIColors().getBaseUIColor(), getFactionForUIColors().getDarkUIColor(),
					  (int)(width), 20f, opad * 2f);
			button.setShortcut(Keyboard.KEY_T, true);
			if (isWithDeleteButton()) {
				info.addSpacer(-opad);
			}
		}
		
		if (isWithDeleteButton()) {
			addDeleteButton(info, width);
		}
	}
	

	@Override
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_EDIT) {
			ui.showEditIntelMarkerDialog(this);
			return;
		}
		super.buttonPressConfirmed(buttonId, ui);
	}

	@Override
	public boolean doesButtonHaveConfirmDialog(Object buttonId) {
		return super.doesButtonHaveConfirmDialog(buttonId);
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		if (getClass() == MapMarkerIntel.class) {
			tags.add(Tags.INTEL_MARKER);
		}
		return tags;
	}

	public String getSortString() {
		return super.getSortString();
	}

	protected transient String discoveredPrefixOverride = null;
	
	public String getDiscoveredPrefixOverride() {
		return discoveredPrefixOverride;
	}

	public void setDiscoveredPrefixOverride(String discoveredPrefixOverride) {
		this.discoveredPrefixOverride = discoveredPrefixOverride;
	}

	public String getName() {
		String prefix = "";
		if (getListInfoParam() == DISCOVERED_PARAM && getClass() != MapMarkerIntel.class) {
			prefix = "Discovered: ";
			if (discoveredPrefixOverride != null) {
				prefix = discoveredPrefixOverride;
			}
		}
		return prefix + title;
	}

	@Override
	public FactionAPI getFactionForUIColors() {
		return super.getFactionForUIColors();
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
//		if (copy != null && copy.getStarSystem() != null) {
//			return copy.getStarSystem().getCenter();
//		} else if (copy != null) {
//			return copy;
//		}
		if (copy != null) return copy;
		return entity;
	}

	@Override
	public boolean shouldRemoveIntel() {
		return super.shouldRemoveIntel();
	}
	
	public boolean isHidden() {
		return hidden != null;  
	}

	
}
