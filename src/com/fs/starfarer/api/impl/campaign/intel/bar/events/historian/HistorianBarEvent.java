package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.historian.HistorianData.HistorianOffer;
import com.fs.starfarer.api.impl.campaign.plog.PLIntel;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;

public class HistorianBarEvent extends BaseBarEvent {
	
	public static enum OptionId {
		GREETING,
		GREETNG_CONTINUE_1,
		GREETNG_ALREADY_INTRODUCED,
		GREETNG_PLOG,
		//GREETNG_PLOG_CONTINUE,
		WHAT_DO_YOU_HAVE,
		BACKSTORY_BIT,
		END_CONVERSATION,
	}
	
	public static int MIN_SNAPSHOTS_TO_SHOW_PLOG = 12 * 5;
	
	public static float PROB_TO_SHOW = 0.5f;
	
	
	protected long seed;
	protected MarketAPI market = null;
	
	protected transient Random random;
	protected transient List<HistorianOffer> offers = null;
	//protected transient HistorianBackstoryInfo backstory = null;
	
	public HistorianBarEvent() {
		super();
		seed = Misc.random.nextLong();
	}
	
	public boolean shouldShowAtMarket(MarketAPI market) {
		regen(market);
		if (random.nextFloat() > PROB_TO_SHOW) return false;
		return super.shouldShowAtMarket(market);
	}

	
	protected void regen(MarketAPI market) {
		//if (this.market == market) return;
		this.market = market;
		done = false;
		
		random = new Random(seed + market.getId().hashCode());
//		offers = HistorianData.getInstance().getOffers(random, dialog);
	}
	
	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.addPromptAndOption(dialog, memoryMap);
		
		regen(dialog.getInteractionTarget().getMarket());
		
		TextPanelAPI text = dialog.getTextPanel();
		text.addPara(getPrompt());
		
		dialog.getOptionPanel().addOption(getOptionText(), this);
		//dialog.setOptionColor(this, Misc.getStoryOptionColor());
		
	}

	protected String getOptionText() {
		HistorianData hd = HistorianData.getInstance();
		return "Go over to the " + hd.getManOrWoman() + " with the paper book and see what " + hd.getHeOrShe() + " wants";
	}

	protected String getPrompt() {
		HistorianData hd = HistorianData.getInstance();
		return "An old " + hd.getManOrWoman() + " sits alone at a table, " +
				"leafing through what looks like a genuine hardcopy paper book. As you walk by, " +
				hd.getHeOrShe() + " looks up and offers the seat across from " + hd.getHimOrHer() + ".";
	}

	@Override
	public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.init(dialog, memoryMap);
		
		HistorianData hd = HistorianData.getInstance();
		
		dialog.getVisualPanel().showPersonInfo(hd.getPerson(), true, true);
		
		// oh the hacks
		if (dialog.getInteractionTarget() != null && hd.getPerson() != null) {
			this.memoryMap = new LinkedHashMap<String, MemoryAPI>(memoryMap);
			memoryMap = this.memoryMap;
			
			dialog.getInteractionTarget().setActivePerson(hd.getPerson());
			MemoryAPI memory = hd.getPerson().getMemory();
			memoryMap.put(MemKeys.LOCAL, memory);
			memoryMap.put(MemKeys.PERSON_FACTION, hd.getPerson().getFaction().getMemory());
			memoryMap.put(MemKeys.ENTITY, dialog.getInteractionTarget().getMemory());
		}
		
		if (!hd.isIntroduced()) {
			optionSelected(null, OptionId.GREETING);
		} else {
			boolean hasLog = Global.getSector().getIntelManager().hasIntelOfClass(PLIntel.class);
			boolean shouldShow = PlaythroughLog.getInstance().getData().size() >= MIN_SNAPSHOTS_TO_SHOW_PLOG;
			//shouldShow = true;
			if (!hasLog && shouldShow) {
				optionSelected(null, OptionId.GREETNG_PLOG);
			} else {
				optionSelected(null, OptionId.GREETNG_ALREADY_INTRODUCED);
			}
		}
		
	}
	
	protected transient HistorianOffer currentOffer = null;

	@Override
	public void optionSelected(String optionText, Object optionData) {
		HistorianData hd = HistorianData.getInstance();
		
		if (optionData == OptionId.GREETNG_PLOG) {
			text.addPara("\"Welcome! I've got something here that I think you'll find very interesting. Your actions over " +
					"the past several cycles have caused quite a stir in certain corners, and I took it upon myself to " +
					"look into your history, as it were. Here, let me transfer the synopsis.\"");
			text.addPara("Your TriPad chimes softly; a new program is ready to be installed.");
			PLIntel intel = new PLIntel();
			Global.getSector().getIntelManager().addIntel(intel, false, dialog.getTextPanel());
			options.clearOptions();
			options.addOption("\"Thank you! This is fascinating and not at all alarming. Now, what do you have for me?\"", OptionId.WHAT_DO_YOU_HAVE);
			return;
		}
		
//		if (optionData == OptionId.GREETNG_PLOG_CONTINUE) {
//			PLIntel intel = new PLIntel();
//			Global.getSector().getIntelManager().addIntel(intel, false, dialog.getTextPanel());
//			options.clearOptions();
//			options.addOption("Continue", OptionId.WHAT_DO_YOU_HAVE);
//			return;
//		}
		
		if (optionData == OptionId.GREETING) {
			text.addPara("\"Welcome, welcome, please sit! I'm sure you're wondering why I called you over.\"\n\n" +
					hd.getUCHeOrShe() + " leans in as if conspiring, \"" +
						 "I think we can help each other, you and I. See, I have the information,\" " +
							hd.getHeOrShe() + " taps the book, "+"\"And you have the means.\"");
			text.addPara("\"I am what you might call a historian-adventurer; a rogue archaelogist uncovering the " +
						 "history of the Sector despite the danger of this exceptional era. In the course of my "
						 + "studies I often chance upon hints about where certain " +
						 "pieces of technology might be found. Retrieving those artifacts " +
						 "is, naturally, your role in our arrangement. ... Only if you're interested,"
						 + " of course - but I've done my research " +
						 "on your exploits and I'm sure that you will be interested.\"\n\nSpeech complete, " + hd.getHeOrShe() +
						 " leans back, looking pleased.");
			
			options.clearOptions();
			options.addOption("\"I expect you'll be wanting to get paid for this information?\"", OptionId.GREETNG_CONTINUE_1);
			return;
		}
		
		if (optionData == OptionId.GREETNG_CONTINUE_1) {
			text.addPara("A weary expression passes over the " + hd.getManOrWoman() + "'s face for just a moment before " + hd.getHeOrShe() + 
						 " resumes an energetic demeanor.");
			text.addPara("\"I understand why you would assume that; this is a mercenary age. " + 
						"But no, nothing quite so transactional. " +
						 "History is an uncertain trade, and such an approach " +
						 "would lead to expectations, disappointment, and recrimination. All of which I'm eager to avoid, " +
						 "especially the last.\" An additional wrinkle forms on " + hd.getHisOrHer()+ " face at this.");
			text.addPara("\"However, if you did find my information valuable, I would gladly accept... donations. Think of it as patronage to " +
						 "fuel my research, which in turn could produce valuable leads. Or it " +
						 "might help to think of it as an investment, but with no obligations incurred " +
						 "by either party. Consider it- you could enrich your own enterprises while contributing to" + 
						 " the sum of human knowledge!\"");
			hd.setIntroduced(true);
			hd.setRecentlyDonated(); // don't show the donate option first time around
			
			options.clearOptions();
			options.addOption("\"Do you have anything for me now?\"", OptionId.WHAT_DO_YOU_HAVE);
			return;
		}
		
		if (optionData == OptionId.GREETNG_ALREADY_INTRODUCED) {
			text.addPara("\"So good to see you again, " + Global.getSector().getPlayerPerson().getNameString() + "!\"");
			text.addPara("You spend a couple minutes sharing drinks and conversing about " +
					"recent goings-on in the Sector. Before long you get down to business.");
			optionSelected(null, OptionId.WHAT_DO_YOU_HAVE);
			return;
		}
		
		if (optionData == OptionId.WHAT_DO_YOU_HAVE) {
			text.addPara("The historian tells you about...");
			
			if (offers == null) {
				if (random == null) {
					random = new Random(seed + market.getId().hashCode());
				}
				offers = HistorianData.getInstance().getOffers(random, dialog);
			}
			
//			if (backstory == null) {
//				backstory = HistorianData.getInstance().pickBackstoryBit(random);
//			}
			
			options.clearOptions();
			if (offers != null) {
				for (HistorianOffer offer : offers) {
					offer.addPromptAndOption(dialog);
				}
			}
			//options.addOption("... an interesting bit of Sector history, but nothing that's immediately actionable", OptionId.END_CONVERSATION);
			options.addOption("... something interesting, but not of immediate import", OptionId.BACKSTORY_BIT);
			return;
		}
		
		boolean forceEnd = false;
		if (optionData == OptionId.BACKSTORY_BIT) {
//			if (backstory == null) {
//				forceEnd = true;
//			} else {
				Random currRandom = Misc.getRandom(seed, 11);
				Global.getSector().getRules().setRandomForNextRulePick(currRandom);
				boolean shown = FireBest.fire(null, dialog, memoryMap, "HistorianBackstoryBlurb");
				if (!shown) {
					SharedData.getData().getUniqueEncounterData().historianBlurbsShown.clear();
					currRandom = Misc.getRandom(seed, 11);
					Global.getSector().getRules().setRandomForNextRulePick(currRandom);
					shown = FireBest.fire(null, dialog, memoryMap, "HistorianBackstoryBlurb");
				}
//				if (!shown) {
//					HistorianData.getInstance().getShownBackstory().add(backstory.getId());
//					text.addPara(backstory.getText());
//				}
				options.clearOptions();
				options.addOption("End the conversation", OptionId.END_CONVERSATION);
//			}
		}
		
		
		if (optionData == OptionId.END_CONVERSATION || forceEnd) {
			text.addPara("You thank " + hd.getHimOrHer() + " for " + hd.getHisOrHer() + " the information and get up to leave.");
			text.addPara("\"See you again soon, somewhere!\" " + hd.getHeOrShe() + " says. \"In my line of work, even I don't " +
						 "know for sure where I'll end up in a couple of months.\"");
			done = true;
			
			if (dialog.getInteractionTarget() != null) {
				dialog.getInteractionTarget().setActivePerson(null);
			}
			
			return;
		}

		boolean initedOffer = false;
		if (optionData instanceof HistorianOffer) {
			currentOffer = (HistorianOffer) optionData;
			currentOffer.init(dialog);
			initedOffer = true;
			//return;
		}
		
		if (currentOffer != null) {
			if (!initedOffer) {
				currentOffer.optionSelected(optionText, optionData);
			}
			if (currentOffer.isInteractionFinished()) {
				if (currentOffer.shouldRemoveOffer()) {
					offers.remove(currentOffer);
					BarEventManager.getInstance().notifyWasInteractedWith(this);
				}
				
				currentOffer.notifyAccepted();
				
				if (currentOffer.shouldEndConversationOnReturning()) {
					currentOffer = null;
					optionSelected(null, OptionId.END_CONVERSATION);
				} else {
					currentOffer = null;
					optionSelected(null, OptionId.WHAT_DO_YOU_HAVE);
				}
			}
		}
	}
	
	
//	protected String getBacktoryBit() {
//		
//	}
	
}







