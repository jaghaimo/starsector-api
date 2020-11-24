package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * NotifyEvent $eventHandle <params> 
 * 
 */
public class PrintWreckDescription extends BaseCommandPlugin {
	
	public static final String TRAPPED = "$trapped";
	public static final String LOCKED = "$locked";
	public static final String CAN_UNLOCK = "$canUnlock";
	
	
	public static final float BREAK_KEEP_FRACTION = 0.5f;
	
	
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected CargoAPI cargo;
	protected MemoryAPI memory;
	protected InteractionDialogAPI dialog;
	private DerelictShipEntityPlugin plugin;
	private Map<String, MemoryAPI> memoryMap;

	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		memory = getEntityMemory(memoryMap);
		entity = dialog.getInteractionTarget();
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		playerFleet = Global.getSector().getPlayerFleet();
		cargo = playerFleet.getCargo();
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		plugin = (DerelictShipEntityPlugin) entity.getCustomPlugin();
		
		PerShipData shipData = plugin.getData().ship;
		boolean nameKnown = ShipRecoverySpecial.isNameKnown(shipData.condition);
		//String shipName = 
		//String shipVariant
		//plugin.getData().ship.
		
		ShipVariantAPI variant = shipData.variant;
		if (variant == null && shipData.variantId != null) {
			variant = Global.getSettings().getVariant(shipData.variantId);
		}
		
		ShipCondition condition = shipData.condition;
		String conStr = "";
		switch (condition) {
		case PRISTINE:
			conStr = "nearly undamaged";
			break;
		case GOOD:
			conStr = "in good condition";
			break;
		case AVERAGE:
			conStr = "showing signs of damage";
			break;
		case BATTERED:
			conStr = "battered";
			break;
		case WRECKED:
			conStr = "heavily damaged";
			break;
		}

		if (variant == null) {
			String str = "A derelict ship, drifting through space.";
			text.addParagraph(str);
		} else {
			String hullType = "";
			ShipHullSpecAPI spec = variant.getHullSpec();
			if (spec.hasHullName()) hullType += spec.getHullNameWithDashClass();
			if (spec.hasDesignation()) {
				if (!hullType.isEmpty()) hullType += " ";
				hullType += spec.getDesignation().toLowerCase();
			}
			
			String str = "A derelict " + hullType + ", drifting through space.";
			
			text.addParagraph(str);
		}
		
		text.addParagraph("It is " + conStr + ", " +
		 "though determining whether it's recoverable or not will require closer examination.");
		
		
//		float daysLeft = plugin.getData().durationDays;
//		if (daysLeft >= 1000) {
//			text.addParagraph("The ship is in a stable orbit.");
//		} else {
//			String atLeastTime = Misc.getAtLeastStringForDays((int) daysLeft);
//			text.addParagraph("The ship is not in a stable orbit, but its location is predictable" + atLeastTime + ".");
//			//text.addParagraph("The ship is not inis unstable, but should not drift apart for " + atLeastTime + ".");
//		}
		
		
		return true;
	}
	
	
	public String getString(String format) {
		return Misc.getStringWithTokenReplacement(format, entity, memoryMap);
	}

	
}















