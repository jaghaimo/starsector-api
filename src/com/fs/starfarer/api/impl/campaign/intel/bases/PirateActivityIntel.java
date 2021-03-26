package com.fs.starfarer.api.impl.campaign.intel.bases;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class PirateActivityIntel extends BaseIntelPlugin {

	//protected MarketAPI market;
	protected StarSystemAPI system;
	protected PirateBaseIntel source;
	
	public PirateActivityIntel(StarSystemAPI system, PirateBaseIntel source) {
		this.system = system;
		this.source = source;
		
		
		boolean hasPlayerMarkets = false;
		for (MarketAPI curr : Global.getSector().getEconomy().getMarkets(system)) {
			hasPlayerMarkets |= curr.isPlayerOwned();
		}
		if (!hasPlayerMarkets) {
			setPostingLocation(system.getCenter());
		}
		
		Global.getSector().addScript(this);
		
		
		//Global.getSector().getIntelManager().queueIntel(this);
		if (!Misc.getMarketsInLocation(system, Factions.PLAYER).isEmpty()) {
			Global.getSector().getIntelManager().addIntel(this);
		} else {
			Global.getSector().getIntelManager().queueIntel(this);
		}
	}
	
	@Override
	public boolean canMakeVisibleToPlayer(boolean playerInRelayRange) {
		if (DebugFlags.SEND_UPDATES_WHEN_NO_COMM && source.isPlayerVisible()) return true;
		return super.canMakeVisibleToPlayer(playerInRelayRange);
	}


	public PirateBaseIntel getSource() {
		return source;
	}

	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		Global.getSector().removeScript(this);
	}


	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		for (MarketAPI curr : source.getAffectedMarkets(system)) {
			if (curr.hasCondition(Conditions.PIRATE_ACTIVITY)) {
				curr.removeCondition(Conditions.PIRATE_ACTIVITY);
			}
		}
	}

	@Override
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);
		
		if (source.isEnding() || source.getTarget() != system) {
			endAfterDelay();
			if (DebugFlags.SEND_UPDATES_WHEN_NO_COMM || Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay()) {
				sendUpdateIfPlayerHasIntel(new Object(), false);
			}
			return;
		}
		
		for (MarketAPI curr : source.getAffectedMarkets(system)) {
			if (!curr.hasCondition(Conditions.PIRATE_ACTIVITY)) {
				curr.addCondition(Conditions.PIRATE_ACTIVITY, source);
			}
		}
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
		
//		info.addPara("Danger level: " + danger, initPad, tc, dangerColor, danger);
//		initPad = 0f;
		
		unindent(info);
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		addBulletPoints(info, mode);
	}
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		info.addImage(getFactionForUIColors().getLogo(), width, 128, opad);
		
		info.addPara("Pirates have been targeting colonies and shipping " +
				"in the " + system.getNameWithLowercaseType() + ".", opad);
		
		if (source.isPlayerVisible()) {
			//info.addPara("The pirates are based out of the " + source.getSystem().getNameWithLowercaseType() + ".", opad);
			info.addPara("The pirates are based out of " +
					source.getMarket().getName() + " in the " + source.getSystem().getNameWithLowercaseType() + ".", opad);
		} else {
			float distLY = Misc.getDistanceLY(system.getLocation(), source.getSystem().getLocation());
			if (distLY < 10) {
				info.addPara("The location of the pirate base is unknown, but it's likely to be somewhere nearby.", opad);
			} else {
				info.addPara("The location of the pirate base is unknown, but there are indications that it's quite distant.", opad);
			}
		}
		
		info.addSectionHeading("Colonies affected", getFactionForUIColors().getBaseUIColor(),
							  getFactionForUIColors().getDarkUIColor(), Alignment.MID, opad);
		
		//info.addPara("Colonies affected:", opad);
		
		MarketConditionAPI condition = null;
		float initPad = opad;
		for (MarketAPI curr : source.getAffectedMarkets(system)) {
			if (condition == null) {
				condition = curr.getCondition(Conditions.PIRATE_ACTIVITY);
			}
			
			addMarketToList(info, curr, initPad, tc);
			initPad = 0f;
		}
		
		if (condition != null) {
			MarketConditionPlugin plugin = condition.getPlugin();
//			String text = condition.getSpec().getDesc();
//			Map<String, String> tokens = plugin.getTokenReplacements();
//			if (tokens != null) {
//				for (String token : tokens.keySet()) {
//					String value = tokens.get(token);
//					text = text.replaceAll("(?s)\\" + token, value);
//				}
//			}
//			if (!text.isEmpty()) {
//				info.addPara(text, opad);
//			}
			
			((PirateActivity)plugin).createTooltipAfterDescription(info, true);
		}
		
	}
	
	
	public StarSystemAPI getSystem() {
		return system;
	}

	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "pirate_activity");
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Factions.PIRATES);
		
		if (!Misc.getMarketsInLocation(system, Factions.PLAYER).isEmpty()) {
			tags.add(Tags.INTEL_COLONIES);
		}
		
		return tags;
	}
	
	public String getSortString() {
		String base = Misc.ucFirst(getFactionForUIColors().getPersonNamePrefix());
		return base + " C"; // so it goes after "Pirate Base"
	}
	
	public String getName() {
		String base = "Pirate Activity";
		if (isEnding()) {
			return base + " - Over";
		}
		return base + " - " + system.getBaseName();
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return source.getFactionForUIColors();
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return system.getCenter();
	}

	
	@Override
	public String getCommMessageSound() {
		return getSoundMinorMessage();
	}
	
	
}







