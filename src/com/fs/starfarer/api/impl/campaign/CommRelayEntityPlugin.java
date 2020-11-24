package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.CommRelayCondition;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.misc.CommSnifferIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class CommRelayEntityPlugin extends BaseCampaignObjectivePlugin {

	public static interface CommSnifferReadableIntel {
		boolean canMakeVisibleToCommSniffer(boolean playerInRelayRange, SectorEntityToken relay);
	}
	
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		readResolve();
	}
	
	Object readResolve() {
		return this;
	}
	
	public void advance(float amount) {
		if (entity.getContainingLocation() == null || entity.isInHyperspace()) return;
		
		if (entity.getMemoryWithoutUpdate().getBoolean(MemFlags.OBJECTIVE_NON_FUNCTIONAL)) return;
		
		// everything else is handled by the relay condition - it picks what relay to use and when to remove itself
		for (MarketAPI market : Misc.getMarketsInLocation(entity.getContainingLocation())) {
			CommRelayCondition mc = CommRelayCondition.get(market);
			if (mc == null) {
				market.addCondition(Conditions.COMM_RELAY);
				mc = CommRelayCondition.get(market);
			}
			if (mc != null) {
				mc.getRelays().add(entity);
			}
		}
		
		checkIntelFromCommSniffer();
	}

	

	protected boolean isMakeshift() {
		return entity.hasTag(Tags.MAKESHIFT);
	}
	
	
	public void printNonFunctionalAndHackDescription(TextPanelAPI text) {
		if (entity.getMemoryWithoutUpdate().getBoolean(MemFlags.OBJECTIVE_NON_FUNCTIONAL)) {
			text.addPara("This one, however, is not connected to the Sector-wide network and is not emitting the hyperwave radiation typically indicative of relay operation. The cause of its lack of function is unknown.");
		}
		if (isHacked()) {
			text.addPara("You have a comm sniffer running on this relay.");
		}
	}
	
	
	public void printEffect(TooltipMakerAPI text, float pad) {
//		int bonus = Math.abs(Math.round(
//					CommRelayCondition.NO_RELAY_PENALTY - CommRelayCondition.COMM_RELAY_BONUS));
//		if (isMakeshift()) {
//			bonus = Math.abs(Math.round(
//					CommRelayCondition.NO_RELAY_PENALTY - CommRelayCondition.MAKESHIFT_COMM_RELAY_BONUS));
//		}
		int bonus = Math.abs(Math.round(
				CommRelayCondition.COMM_RELAY_BONUS));
		if (isMakeshift()) {
			bonus = Math.abs(Math.round(
					CommRelayCondition.MAKESHIFT_COMM_RELAY_BONUS));
		}
		text.addPara(BaseIntelPlugin.INDENT + "%s stability for same-faction colonies in system",
				pad, Misc.getHighlightColor(), "+" + bonus);
	}
	
	public void addHackStatusToTooltip(TooltipMakerAPI text, float pad) {
//		int bonus = Math.abs(Math.round(
//				CommRelayCondition.NO_RELAY_PENALTY - CommRelayCondition.COMM_RELAY_BONUS));
//		if (isMakeshift()) {
//			bonus = Math.abs(Math.round(
//				CommRelayCondition.NO_RELAY_PENALTY - CommRelayCondition.MAKESHIFT_COMM_RELAY_BONUS));
//		}
		int bonus = Math.abs(Math.round(
				CommRelayCondition.COMM_RELAY_BONUS));
		if (isMakeshift()) {
			bonus = Math.abs(Math.round(
					CommRelayCondition.MAKESHIFT_COMM_RELAY_BONUS));
		}
			
		if (isHacked()) {
			text.addPara("%s stability for in-system colonies",
					 pad, Misc.getHighlightColor(), "+" + bonus);
			text.addPara("Comm sniffer installed", Misc.getTextColor(), pad);
		} else {
			text.addPara("%s stability for same-faction colonies in-system",
					 pad, Misc.getHighlightColor(), "+" + bonus);

		}
	}

	@Override
	public void setHacked(boolean hacked) {
		if (hacked) {
			setHacked(hacked, -1f);
			boolean found = CommSnifferIntel.getExistingSnifferIntelForRelay(entity) != null;
			if (!found) {
				CommSnifferIntel intel = new CommSnifferIntel(entity);
				InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
				if (dialog != null) {
					Global.getSector().getIntelManager().addIntelToTextPanel(intel, dialog.getTextPanel());
				}
			}
		} else {
			setHacked(hacked, -1f);
		}
	}

	
	
	private void checkIntelFromCommSniffer() {
		if (!isHacked()) return;
		
		boolean playerInRelayRange = Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay();
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getCommQueue()) {
			if (intel instanceof CommSnifferReadableIntel) {
				CommSnifferReadableIntel csi = (CommSnifferReadableIntel) intel;
				if (csi.canMakeVisibleToCommSniffer(playerInRelayRange, entity)) {
					intel.setForceAddNextFrame(true);
				}
			}
		}
	}
	
}



