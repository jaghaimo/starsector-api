package com.fs.starfarer.api.impl.campaign.intel;

import java.awt.Color;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class FactionHostilityIntel extends BaseIntelPlugin {

	public static Logger log = Global.getLogger(FactionHostilityIntel.class);
	
	public static final float MIN_DAYS = 180f;
	public static final float CHECK_INTERVAL = 60f;
	public static final float END_PROB = 0.25f;
	
	
	protected FactionAPI one;
	protected FactionAPI two;
	protected String id;
	protected float daysUntilCheck;
	
	protected float initialRelationship;
	
	protected Random random = new Random();
	
	public FactionHostilityIntel(FactionAPI one, FactionAPI two) {
		FactionAPI temp = two;
		if (one.getDisplayName().compareTo(two.getDisplayName()) > 0) {
			two = one;
			one = temp;
		}
		
		this.one = one;
		this.two = two;
		
		
		id = FactionHostilityManager.getConflictId(one, two);
		
		log.info(String.format("Making factions hostile: %s <-> %s", one.getDisplayName(), two.getDisplayName()));
		initialRelationship = one.getRelationship(two.getId());
		one.setRelationship(two.getId(), RepLevel.HOSTILE);
		
		daysUntilCheck = MIN_DAYS;
		
		// let the player find out regardless of where they are - they could by checking
		// the relationship in the intel screen anyway
		Global.getSector().getIntelManager().addIntel(this);
	}
	
	public void endHostilties() {
		log.info(String.format("Ending hostilities: %s <-> %s", one.getDisplayName(), two.getDisplayName()));
		FactionHostilityManager.getInstance().notifyRecentlyEnded(id);
		one.setRelationship(two.getId(), initialRelationship);
		endAfterDelay();
		sendUpdateIfPlayerHasIntel(new Object(), false);
	}
	
	@Override
	protected void advanceImpl(float amount) {
		
		float days = Global.getSector().getClock().convertToDays(amount);
		daysUntilCheck -= days;
		
		if (daysUntilCheck <= 0) {
			daysUntilCheck = CHECK_INTERVAL * (0.5f + 1f * random.nextFloat());
			if (random.nextFloat() < END_PROB) {
				endHostilties();
			}
		}
	}


	public String getId() {
		return id;
	}

	public FactionAPI getOne() {
		return one;
	}

	public FactionAPI getTwo() {
		return two;
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
		//boolean isUpdate = getListInfoParam() != null;
		
//		info.addPara("Factions: ", tc, initPad);
//		indent(info);
//		LabelAPI label = info.addPara("%s and %s", 0f, tc,
//				 h, one.getDisplayName(), two.getDisplayName());
//		label.setHighlight(one.getDisplayName(), two.getDisplayName());
//		label.setHighlightColors(one.getBaseUIColor(), two.getBaseUIColor());
		
		info.addPara(one.getDisplayName(), 0f, tc,
					 one.getBaseUIColor(), one.getDisplayName());
		info.addPara(two.getDisplayName(), 0f, tc,
					 two.getBaseUIColor(), two.getDisplayName());
			
		unindent(info);
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color c = getTitleColor(mode);
		float pad = 3f;
		float opad = 10f;
		
		info.addPara(getName(), c, 0f);
		
		addBulletPoints(info, mode);
	}
	
	public String getSortString() {
		return "Hostilities";
	}
	
	public String getName() {
		String base = "Hostilities";
		if (isEnding()) {
			if (isSendingUpdate()) {
				return base + " Ended";
			}
			return base + " (Ended)";
		}
		return base;
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return null;
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}
	
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		info.addImages(width, 128, opad, opad, one.getCrest(), two.getCrest());
		
		if (isEnding()) {
			LabelAPI label = info.addPara("Open conflict between " + one.getDisplayNameWithArticle() + 
					" and " + two.getDisplayNameWithArticle() + " has recently ended.",
					opad);
			label.setHighlight(one.getDisplayNameWithArticleWithoutArticle(), two.getDisplayNameWithArticleWithoutArticle());
			label.setHighlightColors(one.getBaseUIColor(), two.getBaseUIColor());
		} else {
			LabelAPI label = info.addPara("Simmering tensions between " + one.getDisplayNameWithArticle() + 
					" and " + two.getDisplayNameWithArticle() + " have broken out into open conflict.",
					opad);
			label.setHighlight(one.getDisplayNameWithArticleWithoutArticle(), two.getDisplayNameWithArticleWithoutArticle());
			label.setHighlightColors(one.getBaseUIColor(), two.getBaseUIColor());
		}
		
		
//		info.addPara("wkefhuiwehfuweffewfewfeddewefewewefwefwewefwefweehfuwefwewefwefwehfuwefwewefwefwehfuwefwewefwefwkwejfwffhwejkfhkwejfh423wfwhel" +
//				"fewfwefwefwefweh[%s]fh2fwfwefwefwefwefwfkwghfieuwhfekhwejkuhfiohwef1wef2wef3wef4wefewfwefwe", 10f,
//				tc, h, "5%/6%/7%/8%");
//		info.addPara("wkefhuiwehfuweffewfewfe[%s]wefwefweee123eee" + "\u3002" + 
//				"ddewefewewefwefwe[%s]wefwefweehfuwdfeefwewefwefwehfuwefwe[%s]wefwefwehfuwefwewefwefwkwejfwffhwejkfhkwejfh423wfwhel" +
//				"fewfwefwef[%s]fh2fwfwefwefwefwefwfkwghfieuwhfekhwejkuhfiohwef1w[%s]ef2wef3wef4wefewfwefwe", 10f,
//				tc, h, "20", "4X", "103", "5%/6%/7%/8%", "54%");
		
//		info.addPara("wkefhuiwehfuweffewfewfe[%s]wefwefweeeeddewefewewefwefwe[%s]wefwefweehfuwdfeefwewefwefwehfuwefwe[%s]wefwefwehfuwefwewefwefwkwejfwffhwejkfhkwejfh423wfwhel" +
//				"fewfwefwefwefweh[%s]fh2fwfwefwefwefwefwfkwghfieuwhfekhwejkuhfiohwef1w[%s]ef2wef3wef4wefewfwefwe", 10f,
//				tc, h, "20", "4X", "103", "5%/6%/7%/8%", "54%");

	}
	
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "hostilities");
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_HOSTILITIES);
		tags.add(one.getId());
		tags.add(two.getId());
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return null;
	}


}



