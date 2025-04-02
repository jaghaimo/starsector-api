package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import java.util.Set;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.listeners.ExtraSalvageShownListener;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.DelayedFleetEncounter;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public abstract class BaseHistorianOfferWithLocation extends BaseHistorianOffer implements ExtraSalvageShownListener {

	private SectorEntityToken entity;
	
	public BaseHistorianOfferWithLocation(SectorEntityToken entity) {
		super();
		this.entity = entity;
		setImportant(true);
	}

	@Override
	public void init(InteractionDialogAPI dialog) {
		super.init(dialog);
		
		setDone(true);
		setRemove(true);
		setEndConversationOnReturning(true);
		
		Global.getSector().getIntelManager().addIntel(this, false, text);
		Global.getSector().getListenerManager().addListener(this);
		
		Misc.makeImportant(entity, getClass().getSimpleName());
		
		CargoAPI cargo = Global.getFactory().createCargo(true);
		addItemToCargo(cargo);
		BaseSalvageSpecial.addExtraSalvage(cargo, entity.getMemoryWithoutUpdate(), -1);
	}
	

	public void reportExtraSalvageShown(SectorEntityToken entity) {
		if (this.entity != entity) return;
		
		Misc.makeUnimportant(entity, getClass().getSimpleName());
		endIntel();
	}
	
	protected void endIntel() {
		if (isEnding() || isEnded()) return;
		
		endAfterDelay();
		Global.getSector().addScript(this);
		Global.getSector().getListenerManager().removeListener(this);
		
		if (Misc.random.nextFloat() < 0.5f) return;
		if (this instanceof WeaponBlueprintOffer) return;
		if (this instanceof FighterBlueprintOffer) return;
		
		DelayedFleetEncounter e = new DelayedFleetEncounter(null, "hist");
		e.setDelayNone();
		//e.setEncounterInHyper();
		e.setLocationAnywhere(true, Factions.LUDDIC_PATH);
		e.beginCreate();
		e.triggerCreateFleet(FleetSize.LARGE, FleetQuality.DEFAULT, Factions.LUDDIC_PATH, FleetTypes.PATROL_LARGE, new Vector2f());
		
		float q = 0f;
		if (this instanceof ShipBlueprintOffer) {
			ShipHullSpecAPI spec = Global.getSettings().getHullSpec(((ShipBlueprintOffer)this).data);
			if (spec != null) {
				q = Math.min(1f, (float)spec.getFleetPoints() / 20f);
			}
		} else if (this instanceof SpecialItemOffer) {
			q = 1f;
		}
//		else if (this instanceof WeaponBlueprintOffer) {
//			WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(((WeaponBlueprintOffer)this).data);
//			if (spec != null) {
//				q = Math.min(0.5f, (float)spec.getOrdnancePointCost(null) / 40f);
//			}
//		} else if (this instanceof FighterBlueprintOffer) {
//			FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(((FighterBlueprintOffer)this).data);
//			if (spec != null) {
//				q = Math.min(0.5f, (float)spec.getOpCost(null) / 40f);
//			}
//		}
		
		e.triggerSetAdjustStrengthBasedOnQuality(true, q);
		e.triggerSetStandardAggroInterceptFlags();
		e.triggerSetFleetGenericHailPermanent("HistorianPatherHail");
		e.triggerFleetPatherNoDefaultTithe();
		e.endCreate();
	}
	
	
	protected abstract void addItemToCargo(CargoAPI loot);
	public abstract String getName();
	

	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
	}
	
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		if (!entity.isAlive()) {
			endIntel();
		}

		Color c = getTitleColor(mode);
		info.addPara(getName(), c, 0f);
		
		addBulletPoints(info, mode);
	}
	
	
	public String getSmallDescriptionTitle() {
		return getName();
	}
	

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		//Color c = getTitleColor(mode);
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		HistorianData hd = HistorianData.getInstance();
		
		info.addPara("The historian, " + hd.getPerson().getNameString() + ", has given you information about " +
				"the location of a valuable item.", opad);
		
		info.addSectionHeading("Item", Alignment.MID, opad);
		
		CargoAPI cargo = Global.getFactory().createCargo(true);
		addItemToCargo(cargo);
		info.showCargo(cargo, 10, true, opad);
		
		info.addSectionHeading("Location", Alignment.MID, opad);
		if (isEnding()) {
			info.addPara("You have recovered this item.", opad);
		} else {
			String located = BreadcrumbSpecial.getLocatedString(entity, true);
			if (entity instanceof PlanetAPI) {
				PlanetAPI planet = (PlanetAPI) entity;
				info.addPara("The item is in the ruins found on a " + 
								planet.getTypeNameWithLowerCaseWorld().toLowerCase() + " " + located + ".", opad);
				
				info.addPara("A full survey will need to be run before the ruins can be explored.", opad);
				
				info.showFullSurveyReqs(planet, true, opad);
				
			} else if (entity.getCustomEntitySpec() != null) {
				info.addPara("The item is inside " + entity.getCustomEntitySpec().getAOrAn() + " " + 
							 entity.getCustomEntitySpec().getNameInText() + " " + located + ".", opad);
			} else {
				info.addPara("The item inside a " + entity.getName() + " " + located + ".", opad);
			}
		}
		

	}
	
	
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_EXPLORATION);
		tags.add(Tags.INTEL_ACCEPTED);
		return tags;
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		if (entity != null && entity.isDiscoverable() && entity.getStarSystem() != null) {
			return entity.getStarSystem().getCenter();
		}
		return entity;
	}

}










