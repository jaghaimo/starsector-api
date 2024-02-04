package com.fs.starfarer.api.impl.campaign.intel;

import java.awt.Color;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.intel.events.PerseanLeagueHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.missions.DelayedFleetEncounter;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class GensHannanMachinations extends BaseIntelPlugin {
	

	// in $player memory
	public static String AGREED_TO_PAY_HOUSE_HANNAN_AGAIN = "$agreedToPayHouseHannanAgain";
	
	// in $global memory
	public static String MACHINATIONS_IN_EFFECT = "$houseHannanMachinationsInEffect";
	
	public static boolean isMachinationsInEffect() {
		return Global.getSector().getMemoryWithoutUpdate().getBoolean(MACHINATIONS_IN_EFFECT);
	}
	public static boolean isAgreedToPayHouseHannanAgain() {
		return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(AGREED_TO_PAY_HOUSE_HANNAN_AGAIN);
	}
	public static void setAgreedToPayHouseHannanAgain(boolean value) {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(AGREED_TO_PAY_HOUSE_HANNAN_AGAIN, value);
		if (!value) {
			Global.getSector().getPlayerMemoryWithoutUpdate().unset(AGREED_TO_PAY_HOUSE_HANNAN_AGAIN);
		}
	}
	
	public static boolean canRemakeDealWithHouseHannan() {
		return !isAgreedToPayHouseHannanAgain() && 
				PerseanLeagueMembership.isLeagueMember() &&
				isMachinationsInEffect() &&
				!PerseanLeagueMembership.isPayingHouseHannan();
	}
	
	public static String KEY = "$plGHMachinations_ref";
	public static GensHannanMachinations get() {
		return (GensHannanMachinations) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	public static String UPDATE_PARAM_START = "update_param_start";
	public static String UPDATE_PARAM_MACHINATION = "update_param_machination";
	
	public static float PROB_ACTION_TAKEN = 0.5f;
	public static float PROB_BOUNTY_HUNTER = 0.25f;
	
	
	protected FactionAPI faction = null;
	protected IntervalUtil interval = new IntervalUtil(10f, 110f);
	protected Random random = new Random();
	protected int repDamageRemaining = 100;
	
	protected ReputationAdjustmentResult recent = null;
	protected boolean recentIsBountyHunter = false;
	protected long recentTimestamp = 0L;
	
	public GensHannanMachinations(InteractionDialogAPI dialog) {
		if (get() != null) return;
		
		this.faction = Global.getSector().getFaction(Factions.PERSEAN);
		
		setImportant(true);
		
		TextPanelAPI text = null;
		if (dialog != null) text = dialog.getTextPanel();
		
		Global.getSector().addScript(this);
		//Global.getSector().getListenerManager().addListener(this);
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		Global.getSector().getMemoryWithoutUpdate().set(MACHINATIONS_IN_EFFECT, true);
		
		Global.getSector().getIntelManager().addIntel(this, true);
		
		sendUpdateIfPlayerHasIntel(dialog, text);
	}
	
	@Override
	protected void advanceImpl(float amount) {
		super.advanceImpl(amount);
		
		if (isEnded() || isEnding()) return;
		
		
		float days = Misc.getDays(amount);
		//days *= 100f;
		interval.advance(days);
		
		if (!interval.intervalElapsed()) return;
		
		if (PerseanLeagueHostileActivityFactor.getKazeron(false) == null) {
			setImportant(false);
			endAfterDelay();
			return;
		}
		
		if (random.nextFloat() > PROB_ACTION_TAKEN) return;
		
		
		if (isAgreedToPayHouseHannanAgain() && random.nextFloat() < PROB_BOUNTY_HUNTER) {
			sendBountyHunter();
			return;
		}
		
		int repLoss = 5 + random.nextInt(11);
		repLoss = Math.min(repLoss, repDamageRemaining);
		
		RepLevel limit = RepLevel.NEUTRAL;
		if (isAgreedToPayHouseHannanAgain()) {
			limit = RepLevel.VENGEFUL;
		}
		
		ReputationAdjustmentResult result = Misc.adjustRep(Factions.PERSEAN,
								(float)repLoss * -0.01f, limit, null, false, false);
		
		repLoss = Math.min(repLoss, (int)Math.abs(Math.round(result.delta * 100f)));
		
		if (repLoss > 0) {
			repDamageRemaining -= Math.abs(repLoss);
			recent = result;
			recentTimestamp = Global.getSector().getClock().getTimestamp();
			recentIsBountyHunter = false;
			
			sendUpdateIfPlayerHasIntel(UPDATE_PARAM_MACHINATION, false);
		}
		
		if (repDamageRemaining <= 0f) {
			setImportant(false);
			endAfterDelay();
		}
	}
	
	protected void sendBountyHunter() {
		recent = new ReputationAdjustmentResult(0);
		recentTimestamp = Global.getSector().getClock().getTimestamp();
		recentIsBountyHunter = true;
		
		DelayedFleetEncounter e = new DelayedFleetEncounter(random, "GensHannanMachinations");
		e.setDelayShort();
		//e.setDelayNone();
		//e.setLocationCoreOnly(true, market.getFactionId());
		e.setLocationInnerSector(true, Factions.INDEPENDENT);
		e.beginCreate();
		e.triggerCreateFleet(FleetSize.HUGE, FleetQuality.SMOD_1, Factions.PERSEAN, FleetTypes.MERC_BOUNTY_HUNTER, new Vector2f());
		e.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
		e.triggerSetFleetFaction(Factions.INDEPENDENT);
		e.triggerMakeNoRepImpact();
		e.triggerSetStandardAggroInterceptFlags();
		e.triggerMakeFleetIgnoreOtherFleets();
		e.triggerSetFleetGenericHailPermanent("GensHannanMachinationsHail");
		e.endCreate();
		
		sendUpdateIfPlayerHasIntel(UPDATE_PARAM_MACHINATION, false);
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
		Global.getSector().getMemoryWithoutUpdate().unset(MACHINATIONS_IN_EFFECT);
		//Global.getSector().getListenerManager().removeListener(this);
	}
	
	@Override
	protected void notifyEnded() {
		Global.getSector().removeScript(this);
		super.notifyEnded();
	}

	protected Object readResolve() {
		return this;
	}
	
	public String getBaseName() {
		return "Gens Hannan Machinations";
	}

	public String getName() {
		String postfix = "";
		return getBaseName() + postfix;
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		return faction;
	}

	public String getSmallDescriptionTitle() {
		return getName();
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

		if (recent != null) {
			int days = (int)Global.getSector().getClock().getElapsedDaysSince(recentTimestamp);
			if (days > 60f) {
				recentTimestamp = 0;
				recent = null;
				recentIsBountyHunter = false;
			} else {
				if (recentIsBountyHunter) {
					info.addPara("A bounty hunter was recently hired to eliminate you", tc, initPad);
				} else {
					CoreReputationPlugin.addAdjustmentMessage(recent.delta, faction, null, 
							null, null, info, tc, isUpdate, initPad);
					if (!isUpdate) {
						String daysStr = days == 1 ? "day" : "days";
						info.addPara("%s " + daysStr + " ago", 0f, tc, h, "" + days);
					}
				}
			}
		}

	
		unindent(info);
	}
	
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		info.addImage(getFaction().getLogo(), width, 128, opad);
		
		info.addPara("You've gone back on a deal with Gens Hannan, and they work behind the scenes"
				+ "to damage your standing with the Persean League, and worse.", opad,
				getFaction().getBaseUIColor(), "Persean League");
		
		if (isEnding() || isEnded()) {
			info.addPara("You sources indicate that the further action by Gens Hannan "
					+ "is unlikely at this time.", opad);
		} else {
			info.addPara("It's likely that at some point this work will become less of a priority, but "
					+ "for now, their agents go about ruining your reputation with commendable "
					+ "vigor and discretion.", opad);
			
			if (isAgreedToPayHouseHannanAgain()) {
				info.addPara("There is no hope of reconciliation, and the gloves are off. There is a possibility "
						+ "of your being brought into active hostility with the League, and of bounty hunters "
						+ "being hired and equipped for the job.", opad, Misc.getNegativeHighlightColor(),
						"active hostility with the League", "bounty hunters");
			} else {
				if (!canRemakeDealWithHouseHannan()) {
					info.addPara("You are not considered trustworthy enough for Gens Hannan to deal with again, and "
							+ "these actions are in the way of setting an example for others.", opad);
				} else {
					if (getMapLocation(null) != null) {
						PersonAPI person = People.getPerson(People.REYNARD_HANNAN);
						if (person != null) {
							TooltipMakerAPI sub = info.beginImageWithText(person.getPortraitSprite(), 64f);
							sub.addPara("You may be able to negotiate an end to this by going to Kazeron and speaking with "
									+ "Reynard Hannan.", 0f, h, 
									"speaking with Reynard Hannan");
							info.addImageWithText(opad);
						}
					}
				}
			}
		}
		
		if (recent != null) {
			info.addSectionHeading("Recent events", 
					getFaction().getBaseUIColor(), getFaction().getDarkUIColor(), 
					Alignment.MID, opad);
			addBulletPoints(info, ListInfoMode.IN_DESC);
		}
	}
	
	
	public String getIcon() {
		return faction.getCrest();
	}
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		//tags.add(Tags.INTEL_AGREEMENTS);
		tags.add(faction.getId());
		return tags;
	}
	
	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		MarketAPI kazeron = PerseanLeagueHostileActivityFactor.getKazeron(false);
		if (kazeron != null) {
			return kazeron.getPrimaryEntity();
		}
		return null;
	}


	public FactionAPI getFaction() {
		return faction;
	}
	
	public void endMachinations(TextPanelAPI text) {
		if (!isEnded() && !isEnding()) {
			setImportant(false);
			endImmediately();
			//sendUpdate(new Object(), text);
		}
	}
	
}






