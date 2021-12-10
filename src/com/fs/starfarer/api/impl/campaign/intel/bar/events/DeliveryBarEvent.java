package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.BaseOptionStoryPointActionDelegate;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.StoryOptionParams;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class DeliveryBarEvent extends BaseGetCommodityBarEvent {
	
	public static String KEY_FAILED_RECENTLY = "$core_dmi_failedRecently";
	
	public static String KEY_SAW_DELIVERY_EVENT_RECENTLY = "$core_dmi_sawRecently";
	public static String KEY_ACCEPTED_AT_THIS_MARKET_RECENTLY = "$core_dmi_acceptedAtThisMarket";
	
	public static float PROB_HIGHER_CAPACITY = 0.25f;
	
	public static float FAILED_RECENTLY_DURATION = 365f;
	public static float SAW_RECENTLY_DURATION = 180f;
	public static float ACCEPTED_AT_THIS_MARKET_DURATION = 90f;
	
	public static float PROB_TO_SHOW = 0.5f;
	
	protected MarketAPI destination;
	protected int reward;
	protected int escrow;
	protected float duration;
	protected FactionAPI faction;
	protected int playerCargoCap = 0;
	
	protected DestinationData data;
	
	public DeliveryBarEvent() {
		super();
	}
	
	public boolean shouldShowAtMarket(MarketAPI market) {
		//if (true) return true;
		if (!super.shouldShowAtMarket(market)) return false;
		
		if (market.getFactionId().equals(Factions.PIRATES)) return false;
		if (market.getFactionId().equals(Factions.LUDDIC_PATH)) return false;
		
		// what we want:
		// 1) don't show at the same market for a while
		// 2) randomly don't show at any particular market, 
		// unless the player hasn't seen any delivery events in a while
		
		if (market.getMemoryWithoutUpdate().getBoolean(KEY_ACCEPTED_AT_THIS_MARKET_RECENTLY)) {
			return false;
		}
		
		regen(market);
		
		if (destination == null) return false;
		
		if (escrow > 0 && market.isPlayerOwned()) return false;
		
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean(KEY_SAW_DELIVERY_EVENT_RECENTLY)
				&& shownAt != market) {
			if (random.nextFloat() > PROB_TO_SHOW) return false;
		}
		
		Global.getSector().getMemoryWithoutUpdate().set(KEY_SAW_DELIVERY_EVENT_RECENTLY, true, 
														SAW_RECENTLY_DURATION);
		
		return true;
	}
	
	@Override
	protected void regen(MarketAPI market) {
		//if (this.market == market) return;
		if (this.market != market) {
			playerCargoCap = 0;
		}
		
		this.market = market;
		random = new Random(seed + market.getId().hashCode());
		//random = Misc.random;
		
		computeData(market);
		
		if (destination != null) {
			person = createPerson();
		}
	}
	
	public static class DestinationData {
		public MarketAPI dest;
		public CommodityOnMarketAPI comFrom;
		public CommodityOnMarketAPI com;
		public float distLY;
		
		public boolean fromHasPA = false;
		public boolean fromHasCells = false;
		
		public boolean hasPA = false;
		public boolean hasCells = false;
		
		public boolean illegal = false;
		
		public float score = 0;
		
		public DestinationData(MarketAPI from, MarketAPI dest, 
							   CommodityOnMarketAPI comFrom, CommodityOnMarketAPI comDest) {
			this.dest = dest;
			this.comFrom = comFrom;
			this.com = comDest;
			distLY = Misc.getDistanceLY(from.getLocationInHyperspace(), dest.getLocationInHyperspace());
			
			fromHasPA = from.hasCondition(Conditions.PIRATE_ACTIVITY);
			fromHasCells = from.hasCondition(Conditions.PATHER_CELLS);
			hasPA = dest.hasCondition(Conditions.PIRATE_ACTIVITY);
			hasCells = dest.hasCondition(Conditions.PATHER_CELLS);
			
			illegal = dest.isIllegal(com.getId());
			
			score += Math.min(distLY, 10f);
			
			if (fromHasPA) score += 10f;
			if (fromHasCells) score += 5f;
			if (hasPA) score += 10f;
			if (hasCells) score += 5f;
			
//			score += (comFrom.getAvailable() + comFrom.getMaxSupply()) * 0.5f;
//			score += comDest.getMaxDemand(); 
			
		}
	}
	
	protected void computeData(MarketAPI market) {
		
		data = null;
		destination = null;
		reward = 0;
		duration = 0;
		faction = null;
		quantity = 0;
		commodity = null;
		
		List<CommodityOnMarketAPI> commodities = new ArrayList<CommodityOnMarketAPI>();
		for (CommodityOnMarketAPI com : market.getCommoditiesCopy()) {
			if (com.isNonEcon()) continue;
			if (com.isMeta()) continue;
			if (com.isPersonnel()) continue;
			if (com.isIllegal()) continue;
			
			if (com.getAvailable() <= 0) continue;
			if (com.getMaxSupply() <= 0) continue;
			
			commodities.add(com);
		}
		
		List<DestinationData> potential = new ArrayList<DestinationData>();
		
		float maxScore = 0;
		float maxDist = 0;
		for (MarketAPI other : Global.getSector().getEconomy().getMarketsCopy()) {
			if (other == market) continue;
			if (other.isHidden()) continue;
			if (other.isInvalidMissionTarget()) continue;
			
			if (other.getEconGroup() == null && market.getEconGroup() != null) continue;
			if (other.getEconGroup() != null && !other.getEconGroup().equals(market.getEconGroup())) continue;
			
			if (other.getStarSystem() == null) continue;
			
			//WeightedRandomPicker<T>
			for (CommodityOnMarketAPI com : commodities) {
				//CommodityOnMarketAPI otherCom = other.getCommodityData(com.getId());
				CommodityOnMarketAPI otherCom = other.getCommodityData(com.getDemandClass());
				if (otherCom.getMaxDemand() <= 0) continue;
				
				DestinationData data = new DestinationData(market, other, com, otherCom);
				if (data.illegal) continue;
				if (data.score > maxScore) {
					maxScore = data.score;
				}
				if (data.distLY > maxDist) {
					maxDist = data.distLY;
				}
				potential.add(data);
			}
		}
		if (maxDist > 10) maxDist = 10;
		
		WeightedRandomPicker<DestinationData> picker = new WeightedRandomPicker<DestinationData>(random);
		for (int i = 0; i < potential.size(); i++) {
			DestinationData d = potential.get(i);
			if (d.score > maxScore * 0.5f && d.distLY > maxDist * 0.5f) {
				picker.add(d, d.score * d.score * d.score);
			}
		}
		
//		Collections.sort(potential, new Comparator<DestinationData>() {
//			public int compare(DestinationData o1, DestinationData o2) {
//				return (int) Math.signum(o2.score - o1.score);
//			}
//		});
//		
//		
//		WeightedRandomPicker<DestinationData> picker = new WeightedRandomPicker<DestinationData>(random);
//		for (int i = 0; i < potential.size() && i < 5; i++) {
//			DestinationData d = potential.get(i);
//			picker.add(d, d.score * d.score * d.score);
//		}
		
		DestinationData pick = picker.pick();
		
		if (pick == null) return;
		
		destination = pick.dest;
		duration = pick.distLY * 5 + 50;
		duration = (int)duration / 10 * 10;
		
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		quantity = (int) cargo.getMaxCapacity();
		
		if (random.nextFloat() < PROB_HIGHER_CAPACITY) {
			quantity *= 1f + random.nextFloat() * 3f;
			quantity = (int) quantity;
		}
		
		// don't want mission at market to update quantity as player changes their fleet up
		if (playerCargoCap == 0) {
			playerCargoCap = quantity;
		} else {
			quantity = playerCargoCap;
		}
		
		if (pick.com.isFuel()) quantity = (int) cargo.getMaxFuel();
		
		quantity *= 0.5f + 0.25f * random.nextFloat();
		
		// somewhat less of the more valuable stuff
		quantity *= Math.min(1f, 200f / pick.comFrom.getCommodity().getBasePrice());
		
		int limit = (int) (pick.comFrom.getAvailable() * pick.comFrom.getCommodity().getEconUnit());
		limit *= 0.75f + 0.5f * random.nextFloat();
		//if (quantity > 5000) quantity = 5000;
		if (quantity > limit) quantity = limit;
		
		
		if (quantity > 10000) quantity = quantity / 1000 * 1000;
		else if (quantity > 100) quantity = quantity / 10 * 10;
		else if (quantity > 10) quantity = quantity / 10 * 10;
		
		if (quantity < 10) quantity = 10;
		
		
		//float base = pick.com.getMarket().getSupplyPrice(pick.com.getId(), 1, true);
		float base = pick.comFrom.getMarket().getSupplyPrice(pick.comFrom.getId(), 1, true);
		
		if (quantity * base < 4000) {
			base = Math.min(100, 4000 / quantity);
		}
		
//		float minBase = 100;
//		if (quantity > 500) {
//			minBase = 50;
//		}
		float minBase = 100f - 50f * Math.min(1f, quantity / 500f);
		minBase = (base + minBase) * 0.75f;
		
		if (base < minBase) base = minBase;
		
		//float extra = 2000;
		
		float mult = pick.score / 30f;
		//if (market.isPlayerOwned() && mult > 2f) mult = 2f;
		//if (market.isPlayerOwned()) mult *= 0.75f;
		
		if (mult < 0.75f) mult = 0.75f;
		//if (mult > 2) mult = 2;
		reward = (int) (base * mult * quantity);
		
//		float minPerUnit = 50;
//		if (reward / quantity < minPerUnit) {
//			reward = (int) (minPerUnit * quantity);
//		}
		
		reward = reward / 1000 * 1000;
		if (reward < 4000) reward = 4000;
		
		
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean(KEY_FAILED_RECENTLY)) {
			escrow = (int) (quantity * pick.comFrom.getCommodity().getBasePrice());
		}
		
		
		if (market.getFaction() == pick.dest.getFaction()) {
			faction = market.getFaction();
		} else {
			faction = Global.getSector().getFaction(Factions.INDEPENDENT);
			if (faction == null) faction = market.getFaction();
		}
		
		commodity = pick.comFrom.getId();
		
		data = pick;
	}
	
	protected int getNegotiatedAmount() {
		return (int) (reward * 1.5f);
	}
	
	protected void addStoryOption() {
		String id = "negotiate_id";
		options.addOption("Negotiate a higher fee for the delivery", id);
		
		StoryOptionParams params = new StoryOptionParams(id, 1, "negotiateDeliveryFee", Sounds.STORY_POINT_SPEND_INDUSTRY,
				"Negotiated higher fee for delivery of " + data.comFrom.getCommodity().getLowerCaseName() + " to " + data.dest.getName());
		
		SetStoryOption.set(dialog, params, 
			new BaseOptionStoryPointActionDelegate(dialog, params) {

				@Override
				public void confirm() {
					super.confirm();
					reward = getNegotiatedAmount();
					dialog.getTextPanel().addPara(getNegotiatedText());
					OptionPanelAPI options = dialog.getOptionPanel();
					options.clearOptions();
					options.addOption("Continue", OPTION_CONFIRM);
					//optionSelected(null, OPTION_CONFIRM);
				}
				
				@Override
				public String getTitle() {
					//return "Negotiating delivery fee";
					return null;
				}

				@Override
				public void createDescription(TooltipMakerAPI info) {
					float opad = 10f;
					
					info.addSpacer(-opad);
					
					info.setParaInsigniaLarge();
					info.addPara("You're able to negotiate the delivery fee from %s up to " +
							"%s.", 0f, Misc.getHighlightColor(),
							Misc.getDGSCredits(reward),
							Misc.getDGSCredits(getNegotiatedAmount()));
					
					info.addSpacer(opad * 2f);
					addActionCostSection(info);
				}
			
		});
	}
	
	@Override
	protected boolean canAccept() {
		if (escrow <= 0) return true;
		float credits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
		boolean canAfford = credits >= escrow;
		return canAfford;
	}

	@Override
	protected void doStandardConfirmActions() {
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		TextPanelAPI text = dialog.getTextPanel();
		
		cargo.addCommodity(commodity, quantity);
		AddRemoveCommodity.addCommodityGainText(commodity, quantity, text);
		
		if (escrow > 0) {
			cargo.getCredits().subtract(escrow);
			AddRemoveCommodity.addCreditsLossText(escrow, text);
		}
		
		createIntel();
	}
	
	protected void createIntel() {
		DeliveryMissionIntel intel = new DeliveryMissionIntel(this, dialog);
		
		market.getMemoryWithoutUpdate().set(KEY_ACCEPTED_AT_THIS_MARKET_RECENTLY, true, 
							ACCEPTED_AT_THIS_MARKET_DURATION * (0.75f + random.nextFloat() * 0.5f));
	}
	
	@Override
	protected void adjustPerson(PersonAPI person) {
		super.adjustPerson(person);
		person.setImportanceAndVoice(pickImportance(), random);
		person.addTag(Tags.CONTACT_TRADE);
	}

	@Override
	protected String getPersonFaction() {
		return faction.getId();
	}
	
	@Override
	protected String getPersonRank() {
		return Ranks.CITIZEN;
	}
	
	@Override
	protected String getPersonPost() {
		//return Ranks.CITIZEN;
		return pickOne(Ranks.POST_TRADER, Ranks.POST_COMMODITIES_AGENT, 
		 			   Ranks.POST_MERCHANT, Ranks.POST_INVESTOR);
	}
	
	@Override
	protected float getPriceMult() {
		return 0;
	}
	
	@Override
	protected String getPrompt() {
		if (faction.getId().equals(Factions.INDEPENDENT)) {
			return "At a corner table, a concerned-looking " + getManOrWoman() + 
				   " glumly examines " + getHisOrHer() + " TriPad.";
		} else {
			return "At a corner table, a concerned-looking " + getManOrWoman() + 
				   " in " + faction.getPersonNamePrefixAOrAn() + " " + faction.getPersonNamePrefix() + " uniform " + 
				   " glumly examines " + getHisOrHer() + " TriPad.";
		}
	}
	
	@Override
	protected String getOptionText() {
		return "Nod to the concerned " + getManOrWoman() + " and walk over to " + getHisOrHer() + " table";
	}
	
	@Override
	protected String getMainText() {
		String str = "";
		if (market.isPlayerOwned()) {
			String sir = "sir";
			if (Global.getSector().getPlayerPerson().isFemale()) sir = "ma'am";
			str = "\"Oh, it's you, " + sir + "!\", " + getHeOrShe() + 
			" exclaims. Taking a moment to recover " + getHisOrHer() + " composure, " + getHeOrShe() + " says \"We've got a little logistical problem that could use " +
			"your personal touch. " +
			   "There are %s units of " + data.comFrom.getCommodity().getLowerCaseName() + " that urgently need to be delivered " +
			   " to %s" + 
			   ", in the " + data.dest.getStarSystem().getNameWithLowercaseType() + ". ";
			if (data.fromHasPA || data.hasPA) {
				str += "However, recent pirate activity has been making that difficult, and the regular trade fleets " +
						"aren't quite up to the task.\"";
			} else if (data.fromHasCells || data.hasCells) {
				str += "However, recent Pather cell activity has been making that difficult, and the regular trade fleets " +
				"aren't quite up to the task.\"";
			} else {
				str += "But, well, you know what trader captains are like. " +
						"There have been some disagreements over hazard pay, and it's left us in the lurch.\"";
			}
		} else {
			str = "After brief introductions, " + getHeOrShe() + " wastes no time in getting to the point. " +
			   "\"I've got %s units of " + data.comFrom.getCommodity().getLowerCaseName() + " that urgently need to be delivered " +
			   " to %s" + 
			   ", in the " + data.dest.getStarSystem().getNameWithLowercaseType() + ". ";
			if (data.fromHasPA || data.hasPA) {
				str += "Recent pirate activity has been making that difficult, but you look like someone that could " +
					   "get the job done.\"";
			} else if (data.fromHasCells || data.hasCells) {
				str += "Recent Pather cell activity has been making that difficult, but I'm sure you can handle " +
						"any trouble.\"";
			} else {
				str += "We've had some disputes with the regular shipping company, and it's left us in the lurch. " +
					   "Should be a milk run for someone like you, though.\"";
			}
		}
		
		//str += "\n\nYou recall that " + data.dest.getName() + " is under %s control, and %s light-years away. ";
		
		String where = "located in hyperspace,";
		if (data.dest.getStarSystem() != null) {
			//where = "located in the " + data.dest.getStarSystem().getNameWithLowercaseType() + ", which is";
			where = "located in the " + data.dest.getStarSystem().getNameWithLowercaseType() + "";
		}
		//str += "\n\nYou recall that " + data.dest.getName() + " is under %s control, and " + where + " %s light-years away. ";
		str += "\n\nYou recall that " + data.dest.getName() + " is under %s control, and " + where + ". ";
		
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		if (data.comFrom.isFuel()) {
			int cap = cargo.getFreeFuelSpace();
			if (cap > 1) {
				str += " Your fleet's fuel tanks can hold an additional %s units of fuel.";
			} else {
				str += "%sYour fleet's fuel tanks are currently full."; // %s - need to have same number of highlights
			}
		} else {
			int cap = (int) cargo.getSpaceLeft();
			if (cap > 1) {
				str += " Your fleet's holds can accommodate an additional %s units of cargo.";
			} else {
				str += "%sYour fleet's cargo holds are currently full.";
			}
		}
		
		if (market.isPlayerOwned()) {
			str += "\n\n" + Misc.ucFirst(getHeOrShe()) + " double-checks something on " + getHisOrHer() + " pad. " +
				   "\"The customer will pay %s upon delivery within %s days. Can you take this on?\"";	
		} else {
			if (escrow > 0) {
				str += "\n\n" + Misc.ucFirst(getHeOrShe()) + " double-checks something on " + getHisOrHer() + " pad. " +
						"\"The offer is %s credits, payable upon delivery within %s days. You'll also have to " +
						"transfer %s to an escrow account. This will be returned to you " +
						"when you complete the delivery - " +
						"standard insurance procedure, you understand.\"";
			} else {
				str += "\n\n" + Misc.ucFirst(getHeOrShe()) + " double-checks something on " + getHisOrHer() + " pad. " +
							"\"The offer is %s credits, payable upon delivery within %s days. You in?\"";	
			}
		}
		
		return str;
	}
	
	@Override
	protected String [] getMainTextTokens() {
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		int cap = 0;
		if (data.comFrom.isFuel()) {
			cap = cargo.getFreeFuelSpace();
		} else {
			cap = (int) cargo.getSpaceLeft();
		}
		return new String [] { Misc.getWithDGS(quantity), data.dest.getName(), 
							   data.dest.getFaction().getPersonNamePrefix(),
							   //Misc.getRoundedValueMaxOneAfterDecimal(data.distLY),
							   cap > 1 ? Misc.getWithDGS(cap) : " ",
							   Misc.getDGSCredits(reward),
							   Misc.getWithDGS(duration),
							   Misc.getDGSCredits(escrow) };
	}
	@Override
	protected Color [] getMainTextColors() {
		return new Color [] { Misc.getHighlightColor(),
							  //data.dest.getFaction().getBaseUIColor(),
							  Misc.getTextColor(),
							  data.dest.getFaction().getBaseUIColor(),
							  //Misc.getHighlightColor(),
							  Misc.getHighlightColor(),
							  Misc.getHighlightColor(), 
							  Misc.getHighlightColor(), 
							  Misc.getHighlightColor() };
	}
	
	@Override
	protected String getConfirmText() {
		if (market.isPlayerOwned()) {
			return "Agree to handle the contract";
		}
		return "Accept the delivery contract";
	}
	
	protected String getNegotiatedText() {
		return "\"You drive a hard bargain! Very well, it's a deal.\" " + Misc.ucFirst(getHeOrShe()) + 
				" does not appear too displeased. You consider that the initial offer " +
				"was probably on the low side."; 
	}
	
	@Override
	protected String getCancelText() {
		if (market.isPlayerOwned()) {
			return "Decline, explaining that you've got other urgent business to attend to";
		}
		return "Decline the offer, explaining that you've got other plans";
	}

	@Override
	protected String getAcceptText() {
		return "You receive authorization codes to access a dockside warehouse, and " +
				"contact your quartermaster with instructions to begin loading the cargo.";
	}

	
	public MarketAPI getDestination() {
		return destination;
	}

	public int getReward() {
		return reward;
	}

	public float getDuration() {
		return duration;
	}

	public FactionAPI getFaction() {
		return faction;
	}

	public DestinationData getData() {
		return data;
	}

	public int getQuantity() {
		return quantity;
	}

	public int getEscrow() {
		return escrow;
	}
	
	protected boolean showCargoCap() {
		return false;
	}

	@Override
	protected void showTotalAndOptions() {
		super.showTotalAndOptions();

		String icon = Global.getSettings().getCommoditySpec(commodity).getIconName();
		String text = null;
		Set<String> tags = new LinkedHashSet<String>();
		tags.add(Tags.INTEL_MISSIONS);
		
		dialog.getVisualPanel().showMapMarker(getDestination().getPrimaryEntity(), 
					"Destination: " + getDestination().getName(), getDestination().getFaction().getBaseUIColor(), 
					true, icon, text, tags);
	}
	
	
	
}



