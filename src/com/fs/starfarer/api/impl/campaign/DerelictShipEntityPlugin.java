package com.fs.starfarer.api.impl.campaign;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Drops;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.procgen.DropGroupRow;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class DerelictShipEntityPlugin extends BaseCustomEntityPlugin {

	public static enum DerelictType {
		SMALL,
		MEDIUM,
		LARGE,
		CIVILIAN
	}
	
	
	public static DerelictShipData createVariant(String variantId, Random random) {
		ShipCondition condition = pickDerelictCondition(random);
		PerShipData ship = new PerShipData(variantId, condition);
		return new DerelictShipData(ship, false);
	}
	
	public static DerelictShipData createRandom(String factionId, DerelictType type, Random random) {
		if (random == null) random = new Random();
		if (type == null) type = pickDerelictType(random);
		String variantId = null;
		switch (type) {
		case CIVILIAN: variantId = pickCivilianVariantId(factionId, random); break;
		case LARGE: variantId = pickLargeVariantId(factionId, random); break;
		case MEDIUM: variantId = pickMediumVariantId(factionId, random); break;
		case SMALL: variantId = pickSmallVariantId(factionId, random); break;
		}
		
		if (variantId == null) return null;
		
		ShipCondition condition = pickDerelictCondition(random);
		
		PerShipData ship = new PerShipData(variantId, condition);
		
		return new DerelictShipData(ship, true);
	}
	
	public static DerelictType pickDerelictType(Random random) {
		if (random == null) random = new Random();
		WeightedRandomPicker<DerelictType> picker = new WeightedRandomPicker<DerelictType>(random);
		
		picker.add(DerelictType.CIVILIAN, 10f);
		picker.add(DerelictType.LARGE, 5f);
		picker.add(DerelictType.MEDIUM, 10f);
		picker.add(DerelictType.SMALL, 20f);
		
		return picker.pick();
	}
	public static ShipCondition pickDerelictCondition(Random random) {
		if (random == null) random = new Random();
		WeightedRandomPicker<ShipCondition> picker = new WeightedRandomPicker<ShipCondition>(random);
		
		picker.add(ShipCondition.WRECKED, 10f);
		picker.add(ShipCondition.BATTERED, 10f);
		picker.add(ShipCondition.AVERAGE, 7f);
		picker.add(ShipCondition.GOOD, 5f);
		picker.add(ShipCondition.PRISTINE, 1f);
		
		return picker.pick();
	}
	
	public static ShipCondition pickBadCondition(Random random) {
		if (random == null) random = new Random();
		WeightedRandomPicker<ShipCondition> picker = new WeightedRandomPicker<ShipCondition>(random);
		
		picker.add(ShipCondition.WRECKED, 10f);
		picker.add(ShipCondition.BATTERED, 10f);
		picker.add(ShipCondition.AVERAGE, 3f);
		
		return picker.pick();
	}
	
	
	public static String pickCivilianVariantId(String factionId, Random random) {
		String variantId = pickVariant(factionId, random, 
				ShipRoles.CIV_RANDOM
		);
		return variantId;
	}
	public static String pickSmallVariantId(String factionId, Random random) {
		String variantId = pickVariant(factionId, random, 
				ShipRoles.COMBAT_SMALL,
				ShipRoles.COMBAT_FREIGHTER_SMALL,
				ShipRoles.LINER_SMALL,
				ShipRoles.TANKER_SMALL, 
				ShipRoles.PERSONNEL_SMALL
		);
		return variantId;
	}
	
	public static String pickMediumVariantId(String factionId, Random random) {
		String variantId = pickVariant(factionId, random, 
				ShipRoles.COMBAT_MEDIUM,
				ShipRoles.COMBAT_FREIGHTER_MEDIUM,
				ShipRoles.CARRIER_SMALL,
				ShipRoles.LINER_MEDIUM, 
				ShipRoles.TANKER_MEDIUM,
				ShipRoles.PERSONNEL_MEDIUM
		);
		return variantId;
	}
	
	public static String pickLargeVariantId(String factionId, Random random) {
		String variantId = pickVariant(factionId, random, 
				ShipRoles.COMBAT_LARGE,
				ShipRoles.COMBAT_CAPITAL,
				ShipRoles.COMBAT_FREIGHTER_LARGE,
				ShipRoles.CARRIER_MEDIUM,
				ShipRoles.CARRIER_LARGE,
				ShipRoles.LINER_LARGE,
				ShipRoles.TANKER_MEDIUM,
				ShipRoles.TANKER_LARGE,
				ShipRoles.PERSONNEL_LARGE
		);
		return variantId;
	}

	
	
	public static String pickVariant(String factionId, Random random, String ... shipRoles) {
		if (random == null) random = new Random();
		
		FactionAPI faction = Global.getSector().getFaction(factionId);
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		for (String role : shipRoles) {
			picker.add(role);
		}
		
		Set<String> variantsForRole = new HashSet<String>();
		while (variantsForRole.isEmpty() && !picker.isEmpty()) {
			String role = picker.pickAndRemove();
			if (role == null) return null;
			
			variantsForRole = faction.getVariantsForRole(role);
		}
		
		picker.clear();
		picker.addAll(variantsForRole);
		String variantId = picker.pick();
		
		return variantId;
	}
	
	
	
	public static class DerelictShipData {
		public PerShipData ship;
		public float durationDays = 10000000f;
		public boolean canHaveExtraCargo = false;
		public DerelictShipData(PerShipData ship, boolean canHaveExtraCargo) {
			this.ship = ship;
			this.canHaveExtraCargo = canHaveExtraCargo;
		}
		public DerelictShipData(String variantId, ShipCondition condition, float duration, boolean canHaveExtraCargo) {
			if (condition == null) condition = pickDerelictCondition(null);
			if (duration <= 0) duration = 1000000000f;
			durationDays = duration;
			ship = new PerShipData(variantId, condition);
			this.canHaveExtraCargo = canHaveExtraCargo;
		}
	}
	
	//private CustomCampaignEntityAPI entity;
	private DerelictShipData data;
	
	private transient GenericCampaignEntitySprite sprite;
	private transient FleetMemberAPI member;
	private transient float scale;

	private float angVel = 0f;
	
	public void init(SectorEntityToken entity, Object params) {
		super.init(entity, params);
		//this.entity = (CustomCampaignEntityAPI) entity;
		data = (DerelictShipData) params;
		
		angVel = 5f + (float) Math.random() * 10f;
		angVel *= Math.signum((float) Math.random() - 0.5f);
		
		readResolve();
		
		entity.setSensorProfile(1f);
		entity.setDiscoverable(false);
		
		float range = getDetectedAtRange(member.getHullSpec().getHullSize());
		
		// "gen" is id used when spawning salvage entity by default
		// so this overrides that value
		entity.getDetectedRangeMod().modifyFlat("gen", range);
		
		((CustomCampaignEntityAPI)entity).setRadius(getRadius(member.getHullSpec().getHullSize()));
		
		// add some default salvage
		// some uses of this will want to clear that out and add something more specific
		DropData data = new DropData();
		data.group = Drops.BASIC;
		data.value = (int) getBasicDropValue(member);
		entity.addDropValue(data);
		
//		data = new DropData();
//		data.group = Drops.ANY_HULLMOD_LOW;
//		data.chances = 1;
//		entity.addDropRandom(data);

		if (this.data.canHaveExtraCargo) {
			// why add this as extraSavlage instead of drops?
			// because needs to be based on cargo capacity not cargo value (which all drops are)
			long seed = Misc.getSalvageSeed(entity);
			Random r = Misc.getRandom(seed, 2);
			float extraProb = 0.5f;
			if (r.nextFloat() < extraProb) {
				if (member.getVariant().isFreighter()) {
					WeightedRandomPicker<DropGroupRow> picker = DropGroupRow.getPicker(Drops.FREIGHTER_CARGO);
					picker.setRandom(new Random(seed));
					CargoAPI extraSalvage = Global.getFactory().createCargo(true);
					for (int i = 0; i < 3; i++) {
						DropGroupRow pick = picker.pick();
						if (pick.isCommodity()) {
							extraSalvage.addCommodity(pick.getCommodity(), 
											 (int)Math.ceil(member.getCargoCapacity() * (0.15f + 0.15f * r.nextFloat())));
						}
					}
					BaseSalvageSpecial.setExtraSalvage(extraSalvage, entity.getMemoryWithoutUpdate(), -1);
				} else if (member.getVariant().isTanker()) {
					CargoAPI extraSalvage = Global.getFactory().createCargo(true);
					extraSalvage.addFuel((int)Math.ceil(member.getFuelCapacity() * (0.25f + 0.25f * r.nextFloat())));
					BaseSalvageSpecial.setExtraSalvage(extraSalvage, entity.getMemoryWithoutUpdate(), -1);
				}
			}
		}
		
		
		
		
		// can't be "discovered" by default, but something else could setDiscoverable(true)
		// in which case this XP value will matter
		entity.setDiscoveryXP((float) data.value * 0.05f);
		
		entity.setSalvageXP((float) data.value * 0.15f);
		
		//this.data.durationDays = 1f;

	}
	
	public static float getRadius(HullSize size) {
		switch (size) {
		case CAPITAL_SHIP: return 40f;
		case CRUISER: return 35f;
		case DESTROYER: return 30f;
		case FRIGATE: return 25f;
		}
		return 20f;
	}
	
	public static float getBaseDuration(HullSize size) {
		switch (size) {
		case CAPITAL_SHIP: return 50f;
		case CRUISER: return 40f;
		case DESTROYER: return 30f;
		case FRIGATE: return 25f;
		}
		return 25f;
	}
	
	public static float getDetectedAtRange(HullSize size) {
		switch (size) {
		case CAPITAL_SHIP: return 1700f;
		case CRUISER: return 1300f;
		case DESTROYER: return 1000f;
		case FRIGATE: return 800f;
		}
		return 800f;
	}
	
	public static float getBasicDropValue(FleetMemberAPI member) {
		float value = member.getDeploymentCostSupplies() * 200f;
		return value;
	}
	
	Object readResolve() {
		//sprite = new GenericFieldItemSprite(entity, category, key, cellSize, size, spawnRadius);
		if (data.ship.variantId != null) {
			member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, data.ship.variantId);
		} else {
			member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, data.ship.variant);
		}
		
		scale = Misc.getCampaignShipScaleMult(member.getHullSpec().getHullSize());
		
		//scale *= 5f;
		
		sprite = new GenericCampaignEntitySprite(entity, member.getHullSpec().getSpriteName(), scale);
		
		SpriteAPI base = Global.getSettings().getSprite(member.getHullSpec().getSpriteName());
		SpriteAPI overlay = Global.getSettings().getSprite("misc", "campaignDerelictOverlay");
		float w = base.getWidth();
		float h = base.getHeight();
		float size = Math.max(w, h) * 3f * scale;
		overlay.setSize(size, size);
		sprite.setOverlay(overlay);
		
//		SpriteAPI glow = Global.getSettings().getSprite("misc", "campaignDerelictOverlay");
//		glow.setSize(size, size);
//		sprite.setGlow(glow);
		
		return this;
	}
	
	protected float elapsed = 0f; 
	protected Boolean expiring = null;
	public void advance(float amount) {
		if (entity.isInCurrentLocation()) {
			float turn = amount * angVel;
			entity.setFacing(Misc.normalizeAngle(entity.getFacing() + turn));
		}
		
//		if (!entity.hasTag(Tags.NON_CLICKABLE)) {
//			Misc.fadeAndExpire(entity);
//		}
		
		float days = Global.getSector().getClock().convertToDays(amount);
		elapsed += days;
		
		if (elapsed > data.durationDays && expiring == null) {
			VisibilityLevel vis = entity.getVisibilityLevelToPlayerFleet();
			boolean playerCanSee = entity.isInCurrentLocation() && 
									(vis == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS ||
									 vis == VisibilityLevel.COMPOSITION_DETAILS);
			if (!playerCanSee) {
				Misc.fadeAndExpire(entity, 1f);
				expiring = true;
			}
		}
	}

	public float getRenderRange() {
		return entity.getRadius() + 100f;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		float alphaMult = viewport.getAlphaMult();
		alphaMult *= entity.getSensorFaderBrightness();
		alphaMult *= entity.getSensorContactFaderBrightness();
		if (alphaMult <= 0) return;
		
		sprite.render(0, 0, entity.getFacing(), alphaMult);
	}

	public DerelictShipData getData() {
		return data;
	}
	
	
}










