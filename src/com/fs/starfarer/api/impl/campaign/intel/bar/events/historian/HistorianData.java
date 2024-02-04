package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.historian.HistorianBackstory.HistorianBackstoryInfo;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class HistorianData {
	
	public static interface HistorianOffer {
		void init(InteractionDialogAPI dialog);
		void addPromptAndOption(InteractionDialogAPI dialog);
		void optionSelected(String optionText, Object optionData);
		boolean shouldRemoveOffer();
		boolean isInteractionFinished();
		boolean shouldEndConversationOnReturning();
		void notifyAccepted();
		//String getOfferId();
		int getSortOrder();
		HistorianOfferCreator getCreator();
		void setCreator(HistorianOfferCreator creator);
	}
	
	public static interface HistorianOfferCreator {
		HistorianOffer createOffer(Random random, List<HistorianOffer> soFar);
		boolean ignoresLimit();
		float getFrequency();
		String getOfferId(BaseHistorianOffer offer);
		void notifyAccepted(HistorianOffer offer);
	}
	

	public static final String KEY = "$core_historianData";
	
	public static String TIER1 = "hist1t";
	public static String TIER2 = "hist2t";
	public static String TIER3 = "hist3t";
	
	public static HistorianData getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		if (test == null) {// || true) {
			test = new HistorianData();
			Global.getSector().getMemoryWithoutUpdate().set(KEY, test);
		}
		return (HistorianData) test; 
	}
	
	protected PersonAPI person;
	protected boolean introduced = false;
	protected int tier = 0;
	
//	protected transient List<HistorianOfferCreator> creators = new ArrayList<HistorianOfferCreator>();
//	protected transient List<HistorianBackstoryInfo> backstory = new ArrayList<HistorianBackstoryInfo>();
	protected transient List<HistorianOfferCreator> creators;
	protected transient List<HistorianBackstoryInfo> backstory;
	protected Set<String> shownBackstory = new LinkedHashSet<String>();
	protected Set<String> givenOffers = new LinkedHashSet<String>();
	
	public HistorianData() {
		person = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson();
		
		// Diana Nesinjo
		// Jonn Isaaneid
		
		if (person.getGender() == Gender.MALE) {
			person.setPortraitSprite(Global.getSettings().getSpriteName("intel", "historian_male"));
		} else {
			person.setPortraitSprite(Global.getSettings().getSpriteName("intel", "historian_female"));
		}
		
//		creators.add(new DonationOfferCreator());
//		creators.add(new ShipBlueprintOfferCreator(20f));
//		creators.add(new WeaponBlueprintOfferCreator(10f));
//		creators.add(new FighterBlueprintOfferCreator(10f));
//		creators.add(new SpecialItemOfferCreator(10f));
		readResolve();
		//HistorianBackstory.init(backstory);
	}
	
	protected Object readResolve() {
		if (creators == null) {
			creators = new ArrayList<HistorianOfferCreator>();
			creators.add(new DonationOfferCreator());
			creators.add(new ShipBlueprintOfferCreator(20f));
			creators.add(new WeaponBlueprintOfferCreator(10f));
			creators.add(new FighterBlueprintOfferCreator(10f));
			creators.add(new SpecialItemOfferCreator(10f));
		}
		if (backstory == null) {
			backstory = new ArrayList<HistorianBackstoryInfo>();
			//HistorianBackstory.init(backstory);
		}
		if (shownBackstory == null) {
			shownBackstory = new LinkedHashSet<String>();
		}
		if (givenOffers == null) {
			givenOffers = new LinkedHashSet<String>();
		}
		return this;
	}
	
	public HistorianBackstoryInfo pickBackstoryBit(Random random) {
		WeightedRandomPicker<HistorianBackstoryInfo> picker = new WeightedRandomPicker<HistorianBackstoryInfo>(random);
		for (HistorianBackstoryInfo info : backstory) {
			if (shownBackstory.contains(info.getId())) continue;
			picker.add(info, info.getWeight());
		}
		if (picker.isEmpty()) {
			shownBackstory.clear();
			for (HistorianBackstoryInfo info : backstory) {
				picker.add(info, info.getWeight());
			}	
		}
		return picker.pick();
	}
	
	public List<HistorianBackstoryInfo> getBackstory() {
		return backstory;
	}

	public Set<String> getShownBackstory() {
		return shownBackstory;
	}
	
	public Set<String> getGivenOffers() {
		return givenOffers;
	}

	public float getWeightForTags(Set<String> tags) {
		float w = 0f;
		if (tags.contains(TIER1)) {
			w = 1f;
		} else if (tags.contains(TIER2) && getTier() >= 1) {
			w = 5f;
		} else if (tags.contains(TIER3) && getTier() >= 2) {
			w = 10f;
		}
		return w;
	}
	
	public int getTier() {
		return tier;
	}

	public void setTier(int tier) {
		this.tier = tier;
	}

	public void incrTier() {
		tier++;
	}
	
	public boolean isMaxTier() {
		return getTier() >= 2;
	}
	
	
	public void setRecentlyDonated() {
		float dur = 60f + (float) Math.random() * 60f;
		Global.getSector().getMemoryWithoutUpdate().set("$historian_recentlyDonated", true, dur);
	}
	
	public boolean isRecentlyDonated() {
		return Global.getSector().getMemoryWithoutUpdate().getBoolean("$historian_recentlyDonated");
	}

	public List<HistorianOfferCreator> getCreators() {
		return creators;
	}

	public void setCreators(List<HistorianOfferCreator> creators) {
		this.creators = creators;
	}

	public List<HistorianOffer> getOffers(Random random, InteractionDialogAPI dialog) {
		//random = new Random();
		
		WeightedRandomPicker<HistorianOfferCreator> limited = new WeightedRandomPicker<HistorianOfferCreator>(random);
		List<HistorianOffer> always = new ArrayList<HistorianOffer>();
		
		for (HistorianOfferCreator c : creators) {
			if (c.ignoresLimit()) { 
				HistorianOffer offer = c.createOffer(random, always);
				if (offer != null) {
					offer.setCreator(c);
					always.add(offer);
				}
			} else {
				limited.add(c, c.getFrequency());
			}
		}
		
		List<HistorianOffer> result = new ArrayList<HistorianOffer>(always);
		
		int num = 1 + random.nextInt(2 + getTier()) + always.size();
		//num += 5;
		int attempts = num + 5;
		for (int i = 0; i < attempts && result.size() < num; i++) {
			HistorianOfferCreator c = limited.pick();
			if (c == null) continue;
			HistorianOffer offer = c.createOffer(random, result);
			if (offer != null) {
				offer.setCreator(c);
				result.add(offer);
			}
		}
		
		//limited.add(new ShipBlueprintOffer());
		//limited.add(new DonationOffer(25000));
		
		Collections.sort(result, new Comparator<HistorianOffer>() {
			public int compare(HistorianOffer o1, HistorianOffer o2) {
				return (int) Math.signum(o1.getSortOrder() - o2.getSortOrder());
			}
		});
		
		
		return result;
	}

	public PersonAPI getPerson() {
		return person;
	}
	
	protected String getManOrWoman() {
		String manOrWoman = "man";
		if (person.getGender() == Gender.FEMALE) manOrWoman = "woman";
		return manOrWoman;
	}
	
	protected String getUCHeOrShe() {
		String heOrShe = "he";
		if (person.getGender() == Gender.FEMALE) {
			heOrShe = "she";
		}
		return Misc.ucFirst(heOrShe);
	}
	protected String getHeOrShe() {
		String heOrShe = "he";
		if (person.getGender() == Gender.FEMALE) {
			heOrShe = "she";
		}
		return heOrShe;
	}
	
	protected String getHimOrHer() {
		String himOrHer = "him";
		if (person.getGender() == Gender.FEMALE) {
			himOrHer = "her";
		}
		return himOrHer;
	}
	
	protected String getHimOrHerself() {
		String himOrHer = "himself";
		if (person.getGender() == Gender.FEMALE) {
			himOrHer = "herself";
		}
		return himOrHer;
	}
	
	protected String getHisOrHer() {
		String hisOrHer = "his";
		if (person.getGender() == Gender.FEMALE) {
			hisOrHer = "her";
		}
		return hisOrHer;
	}
	
	public boolean isIntroduced() {
		return introduced;
	}

	public void setIntroduced(boolean introduced) {
		this.introduced = introduced;
	}
}


















