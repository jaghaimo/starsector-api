package com.fs.starfarer.api.impl.campaign.intel.misc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BreadcrumbIntel extends FleetLogIntel {

	protected SectorEntityToken foundAt;
	protected SectorEntityToken target;
	
	protected String title;
	protected String text;
	
	protected Boolean showSpecificEntity = null;
	
	public BreadcrumbIntel(SectorEntityToken foundAt, SectorEntityToken target) {
		if (foundAt != null) {
			this.foundAt = makeDoubleWithSameOrbit(foundAt);
		}
		this.target = makeDoubleWithSameOrbit(target);
		setRemoveTrigger(target);
		
//		String targetName = BreadcrumbSpecial.getNameWithAOrAn(target, null, true);
//		String targetNameUC = BreadcrumbSpecial.getNameWithAOrAn(target, null, false);
//		//String entityName = getNameWithAOrAn(entity, null);
//		String located = BreadcrumbSpecial.getLocatedString(target);
//		
//		String subject = "Location: " + Misc.ucFirst(targetNameUC.substring(targetNameUC.indexOf(" ") + 1)) + "");
//		String text1ForIntel = Misc.getStringWithTokenReplacement("While exploring $aOrAn $nameInText, " +
//							   "your crews found a partially accessible memory bank " + 
//							   "containing information that indicates " + targetName + " is " + located + ".", 
//							   foundAt, null);
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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
	}

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		info.addPara(text, opad);
		
		float days = getDaysSincePlayerVisible();
		if (days >= 1) {
			addDays(info, "ago.", days, tc, opad);
		}
	}

	@Override
	public String getIcon() {
		return super.getIcon();
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_EXPLORATION);
		return tags;
	}

	public String getSortString() {
		return "Location";
	}

	public String getName() {
		return title;
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
		if (target.getStarSystem() != null && showSpecificEntity == null) {
			return target.getStarSystem().getCenter();
		}
		return target;
	}

	@Override
	public boolean shouldRemoveIntel() {
		return super.shouldRemoveIntel();
	}

	@Override
	public String getCommMessageSound() {
		if (sound != null) return sound;
		return getSoundMinorMessage();
	}
	
	public Boolean getShowSpecificEntity() {
		return showSpecificEntity;
	}

	public void setShowSpecificEntity(Boolean showPlanet) {
		if (showPlanet != null && !showPlanet) showPlanet = null;
		
		this.showSpecificEntity = showPlanet;
	}

	public List<ArrowData> getArrowData(SectorMapAPI map) {
		if (foundAt == null) return null;
		
		List<ArrowData> result = new ArrayList<ArrowData>();
		
		if (foundAt.getContainingLocation() == target.getContainingLocation() &&
				foundAt.getContainingLocation() != null &&
				!foundAt.getContainingLocation().isHyperspace()) {
			return null;
		}
		
//		SectorEntityToken entityFrom = foundLocation;
//		if (map != null) {
//			SectorEntityToken iconEntity = map.getIntelIconEntity(this);
//			if (iconEntity != null) {
//				entityFrom = iconEntity;
//			}
//		}
//		
//		ArrowData arrow = new ArrowData(entityFrom, targetLocation);
		ArrowData arrow = new ArrowData(foundAt, target);
		
		arrow.color = getFactionForUIColors().getBaseUIColor();
		result.add(arrow);
		
		return result;
	}




}
