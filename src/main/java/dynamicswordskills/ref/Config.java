/**
 Copyright (C) <2017> <coolAlias>

 This file is part of coolAlias' Dynamic Sword Skills Minecraft Mod; as such,
 you can redistribute it and/or modify it under the terms of the GNU
 General Public License as published by the Free Software Foundation,
 either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Modified by moos2333 on 2026-10-16:
 * - Added configurable Armor Break damage multiplier.
 * - Added configurable Sword Beam health allowance, base damage factor, per-level damage factor and range.
 * - Added configurable Back Slice damage bonus per level.
 * - Added configurable Leaping Blow damage bonus per level.
 * - Added configurable Leaping Blow lifespan and range per level.
 * - Added configurable Ending Blow damage bonus per level and duration formula.
 * - Added configurable Mortal Draw damage bonus per level.
 * - Added configurable Basic Sword range, combo size and combo damage bonus.
 */

package dynamicswordskills.ref;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dynamicswordskills.DynamicSwordSkills;
import dynamicswordskills.api.SkillGroup;
import dynamicswordskills.api.SkillRegistry;
import dynamicswordskills.client.gui.IGuiOverlay.HALIGN;
import dynamicswordskills.client.gui.IGuiOverlay.VALIGN;
import dynamicswordskills.entity.DSSPlayerInfo;
import dynamicswordskills.network.client.SyncConfigPacket;
import dynamicswordskills.skills.SkillBase;
import dynamicswordskills.skills.Skills;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import swordskillsapi.api.item.WeaponRegistry;

public class Config
{
	/** Config ID for GuiConfig */
	public static final String CONFIG_ID = "dss.config";
	public static Configuration config;
	/** Flag set after {@link #postInit()} has been called */
	private static boolean loaded;
	/*================== CLIENT SIDE SETTINGS =====================*/
	/* General client settings */
	private static boolean enableAdditionalControls;
	private static boolean enableAutoTarget;
	private static boolean enableTargetPassive;
	private static boolean enableTargetPlayer;
	private static boolean allowVanillaControls;
	private static boolean requireDoubleTap;
	private static boolean requireLockOn;
	/* Skill Manual GUI */
	private static boolean clickedGroupFilterSound;
	private static boolean clickedPageSound;
	private static boolean clickedSkillSound;
	private static boolean showBannedSkills;
	private static boolean showPaginationLabels;
	private static boolean showPlainTextIndex;
	private static boolean showSkillGroupTooltips;
	private static boolean showUnknownSkills;
	private static Map<String, Set<String>> skillGroupLists = Maps.<String, Set<String>>newHashMap();
	/* Combo HUD */
	public static int comboHudDisplayTime;
	private static int comboHudMaxHits;
	public static HALIGN comboHudXAlign;
	public static VALIGN comboHudYAlign;
	public static int comboHudXOffset;
	public static int comboHudYOffset;
	/* Ending Blow HUD */
	public static int endingBlowHudDisplayTime;
	public static boolean endingBlowHudResult;
	public static boolean endingBlowHudText;
	public static HALIGN endingBlowHudXAlign;
	public static VALIGN endingBlowHudYAlign;
	public static int endingBlowHudXOffset;
	public static int endingBlowHudYOffset;
	/*================== WEAPON REGISTRY =====================*/
	/** Items that are considered Swords for all intents and purposes */
	private static String[] swords = new String[0];
	/** Items that are considered Melee Weapons for all intents and purposes */
	private static String[] weapons = new String[0];
	/** Items that are forbidden from being considered as Swords */
	private static String[] forbidden_swords = new String[0];
	/** Items that are forbidden from being considered as Melee Weapons */
	private static String[] forbidden_weapons = new String[0];
	/*================== SERVER =====================*/
	/* General server settings */
	private static boolean backSliceDisarmorPlayer;
	private static Set<String> bannedSkills = Sets.<String>newHashSet();
	private static int baseSwingSpeed;
	private static float parryDisarmTimingBonus;
	private static float parryDisarmPenalty;
	private static boolean requireFullHealth;
	private static boolean risingCutHighJump;
	private static boolean skillSwordCreative;
	private static int skillSwordCreativeLevel;
	private static boolean skillSwordRandom;
	/* Skills modifiers */
	private static float armorBreakDamageMultiplier;
	private static float swordBeamHealthAllowancePerLevel;
	private static int swordBeamBaseDamageFactor;
	private static int swordBeamDamageFactorPerLevel;
	private static int swordBeamRangePerLevel;
	private static int swordBasicRangeBase;
	private static int swordBasicRangePerLevel;
	private static int swordBasicComboSizeBase;
	private static int swordBasicComboSizePerLevel;
	private static float swordBasicComboDamageBonus;
	private static float backSliceDamageBonusPerLevel;
	private static float leapingBlowDamageBonusPerLevel;
	private static int leapingBlowLifespanPerLevel;
	private static double leapingBlowRangeBase;
	private static double leapingBlowRangePerLevel;
	private static float endingBlowDamageBonusPerLevel;
	private static int endingBlowDurationBase;
	private static int endingBlowDurationPerLevel;
	private static int mortalDrawDamageBonusPerLevel;
	/* Loot / drops settings */
	private static boolean bonusOrbEnable;
	private static int orbLootWeight;
	private static Map<Integer, Float> orbDropChance;
	private static boolean orbDropEnable;
	private static float orbDropGeneralChance;
	private static float orbDropRandomChance;
	private static boolean playerDropEnable;
	private static int playerDropFactor;
	private static int skillSwordLootWeight;

	public static void init(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		refreshClient();
		refreshServer();
	}

	public static void refreshClient() {
		/* General client settings */
		enableAdditionalControls = config.get(Configuration.CATEGORY_CLIENT, "dss.config.client.enableAdditionalControls", false, "Enables additional WASD-equivalent keybindings for activating skills with e.g. a gamepad").setRequiresMcRestart(true).getBoolean(false);
		enableAutoTarget = config.get(Configuration.CATEGORY_CLIENT, "dss.config.client.enableAutoTarget", true, "Enable auto-targeting when locked on and the current target becomes invalid").getBoolean(true);
		enableTargetPassive = config.get(Configuration.CATEGORY_CLIENT, "dss.config.client.enableTargetPassive", true, "Allow targeting passive mobs with the lock-on mechanic").getBoolean(true);
		enableTargetPlayer = config.get(Configuration.CATEGORY_CLIENT, "dss.config.client.enableTargetPlayer", true, "Allow targeting players with the lock-on mechanic").getBoolean(true);
		allowVanillaControls = config.get(Configuration.CATEGORY_CLIENT, "dss.config.client.enableVanillaControls", true, "Allow vanilla movement keys to be used to activate skills; must be enabled if Additional Controls are disabled").getBoolean(true);
		if (!enableAdditionalControls && !allowVanillaControls) {
			DynamicSwordSkills.logger.warn("Both Vanilla and Additional Controls are disabled - Vanilla Controls were automatically enabled");
			allowVanillaControls = true;
		}
		requireDoubleTap = config.get(Configuration.CATEGORY_CLIENT, "dss.config.client.requireDoubleTap", true, "Require double-tap for Dodge and Parry (always required when Vanilla Controls are enabled)").getBoolean(true);
		requireLockOn = config.get(Configuration.CATEGORY_CLIENT, "dss.config.client.requireLockOn", false, "Require locking on to activate skills").getBoolean(false);
		/* Skill Manual GUI */
		clickedGroupFilterSound = config.get("skillGui", "dss.config.client.skillGui.clickedGroupFilterSound", true, "Play a sound when applying or removing a Skill Group filter").getBoolean(true);
		clickedPageSound = config.get("skillGui", "dss.config.client.skillGui.clickedPageSound", true, "Play a sound when the page index changes").getBoolean(true);
		clickedSkillSound = config.get("skillGui", "dss.config.client.skillGui.clickedSkillSound", true, "Play a sound when clicking on a Skill entry").getBoolean(true);
		showBannedSkills = config.get("skillGui", "dss.config.client.skillGui.showBannedSkills", false, "Display entries in the Skill Manual for skills disabled by the server").getBoolean(false);
		showPaginationLabels = config.get("skillGui", "dss.config.client.skillGui.showPaginationLabels", true, "Display text labels for 'Prev' and 'Next' page buttons").getBoolean(true);
		showPlainTextIndex = config.get("skillGui", "dss.config.client.skillGui.showPlainTextIndex", true, "Display table of contents without the standard button texture").getBoolean(true);
		showSkillGroupTooltips= config.get("skillGui", "dss.config.client.skillGui.showSkillGroupTooltips", true, "Display tooltips when hovering over the Table of Contents entries for Skill Groups that support them").getBoolean(true);
		showUnknownSkills = config.get("skillGui", "dss.config.client.skillGui.showUnknownSkills", true, "Display entries in the Skill Manual for skills not yet learned").getBoolean(true);
		if (Config.loaded) {
			refreshSkillGroups();
		}
		/* Combo HUD */
		String[] xalign = {"left", "center", "right"};
		String[] yalign = {"top", "center", "bottom"};
		comboHudDisplayTime = config.get("comboHud", "dss.config.client.comboHud.displayTime", 5000, "Number of milliseconds Combo HUD will remain on screen (0 to disable)", 0, 20000).getInt();
		comboHudMaxHits = config.get("comboHud", "dss.config.client.comboHud.maxHits", 3, "Maximum number of recent hits to display [0-12]", 0, 12).getInt();
		comboHudXAlign = HALIGN.fromString(config.get("comboHud", "dss.config.client.comboHud.xalign", "left", "Base HUD alignment on the X-Axis").setValidValues(xalign).getString());
		comboHudXOffset = config.get("comboHud", "dss.config.client.comboHud.xoffset", 0, "Number of pixels to offset HUD alignment on the X-Axis").getInt();
		comboHudYAlign = VALIGN.fromString(config.get("comboHud", "dss.config.client.comboHud.yalign", "top", "Base HUD alignment on the Y-Axis").setValidValues(yalign).getString());
		comboHudYOffset = config.get("comboHud", "dss.config.client.comboHud.yoffset", 0, "Number of pixels to offset HUD alignment on the Y-Axis").getInt();
		/* Ending Blow HUD */
		endingBlowHudDisplayTime = config.get("endingBlowHud", "dss.config.client.endingBlowHud.displayTime", 1000, "Number of milliseconds Ending Blow HUD will remain on screen (0 to disable)", 0, 20000).getInt();
		endingBlowHudResult = config.get("endingBlowHud", "dss.config.client.endingBlowHud.enableResultNotification", true, "Display success / failure notification when Ending Blow is used").getBoolean(true);
		endingBlowHudText = config.get("endingBlowHud", "dss.config.client.endingBlowHud.enableText", false, "Display text instead of icons for Ending Blow notifications").getBoolean(false);
		endingBlowHudXAlign = HALIGN.fromString(config.get("endingBlowHud", "dss.config.client.endingBlowHud.xalign", "center", "Base HUD alignment on the X-Axis").setValidValues(xalign).getString());
		endingBlowHudXOffset = config.get("endingBlowHud", "dss.config.client.endingBlowHud.xoffset", 0, "Number of pixels to offset HUD alignment on the X-Axis").getInt();
		endingBlowHudYAlign = VALIGN.fromString(config.get("endingBlowHud", "dss.config.client.endingBlowHud.yalign", "top", "Base HUD alignment on the Y-Axis").setValidValues(yalign).getString());
		endingBlowHudYOffset = config.get("endingBlowHud", "dss.config.client.endingBlowHud.yoffset", 30, "Number of pixels to offset HUD alignment on the Y-Axis").getInt();
		if (config.hasChanged()) {
			config.save();
		}
	}

	public static void refreshServer() {
		/*================== WEAPON REGISTRY =====================*/
		swords = config.get("Weapon Registry", "[Allowed Swords] Enter items as modid:registered_item_name, each on a separate line between the '<' and '>'", new String[0], "Register an item so that it is considered a SWORD by ZSS, i.e. it be used with skills that\nrequire swords, as well as other interactions that require swords, such as cutting grass.\nAll swords are also considered WEAPONS.").getStringList();
		Arrays.sort(swords);
		weapons = config.get("Weapon Registry", "[Allowed Weapons] Enter items as modid:registered_item_name, each on a separate line between the '<' and '>'", new String[0], "Register an item as a generic melee WEAPON. This means it can be used for all\nskills except those that specifically require a sword, as well as some other things.").getStringList();
		Arrays.sort(weapons);
		String[] forbidden = new String[0];
		forbidden_swords = config.get("Weapon Registry", "[Forbidden Swords] Enter items as modid:registered_item_name, each on a separate line between the '<' and '>'", forbidden, "Forbid one or more items from acting as SWORDs, e.g. if a mod item extends ItemSword but is not really a sword").getStringList();
		Arrays.sort(forbidden_swords);
		forbidden_weapons = config.get("Weapon Registry", "[Forbidden Weapons] Enter items as modid:registered_item_name, each on a separate line between the '<' and '>'", new String[0], "Forbid one or more items from acting as WEAPONs, e.g. if an item is added by IMC and you don't want it to be usable with skills.\nNote that this will also prevent the item from behaving as a SWORD.").getStringList();
		Arrays.sort(forbidden_weapons);
		/*================== SERVER =====================*/
		/* General server settings */
		backSliceDisarmorPlayer = config.get("general", "dss.config.server.general.backSliceDisarmorPlayer", true, "Allow Back Slice to potentially knock off player armor").getBoolean(true);
		String[] banned = config.get("general", "dss.config.server.general.bannedSkills", new String[0], "Enter the registry names for each skill disallowed on this server, each on a separate line between the '<' and '>'. Disabling a skill prevents players from using that skill, but does not change the player's known skills. Skill items previously generated as loot may be found but not used, and subsequent loot will not generate with that skill. Skill orb-like items may still drop from mobs / players unless disabled separately, but may not be used to learn the skill. This setting is save-game safe: skills may be disabled and re-enabled without affecting the saved game state.").setRequiresMcRestart(true).getStringList();
		bannedSkills.clear();
		bannedSkills.addAll(Lists.<String>newArrayList(banned));
		baseSwingSpeed = config.get("general", "dss.config.server.general.baseSwingSpeed", 0, "Base swing power (anti-left-click-spam): Sets minimal percentage of attack power required to perform an attack (0 to disable)[0-100]", 0, 100).setRequiresWorldRestart(true).getInt();
		parryDisarmPenalty = 0.01F * (float)config.get("general", "dss.config.server.general.parryDisarmPenalty", 10, "[Parry] Penalty to disarm chance: percent per Parry level of the opponent, default negates defender's skill bonus so disarm is based entirely on timing [0-20]", 0, 20).getInt();
		parryDisarmTimingBonus = 0.001F * (float)config.get("general", "dss.config.server.general.parryDisarmTimingBonus", 25, "[Parry] Bonus to disarm based on timing: tenths of a percent added per tick remaining on the timer [0-50]", 0, 50).getInt();
		requireFullHealth = config.get("general", "dss.config.server.general.requireFullHealth", false, "True to require a completely full health bar to use Super Spin Attack and Sword Beam, or false to allow a small amount to be missing per level").setRequiresWorldRestart(true).getBoolean(false);
		risingCutHighJump = config.get("general", "dss.config.server.general.risingCutHighJump", false, "Allow the player to activate Rising Cut without hitting a target, i.e. perform a High Jump").getBoolean(false);
		skillSwordCreative = config.get("general", "dss.config.server.general.skillSwordCreative", true, "Enable Skill Swords in the Creative Tab (iron only, as examples)").setRequiresMcRestart(true).getBoolean(true);
		skillSwordCreativeLevel = config.get("general", "dss.config.server.general.skillSwordCreativeLevel", 3, "Skill level provided by the Creative Tab Skill Swords [1-5]", 1, 5).setRequiresMcRestart(true).getInt();
		skillSwordRandom = config.get("general", "dss.config.server.general.skillSwordRandom", true, "Enable randomized Skill Swords to add to loot or drop lists").setRequiresMcRestart(true).getBoolean(true);
		/* Skills modifiers */
		armorBreakDamageMultiplier = (float) config.get("skills", "dss.config.server.skills.armorBreakDamageMultiplier", 1.0D,
				"Damage multiplier for Armor Break skill (1.0 = normal damage)", 0.0D, 10.0D).getDouble();
		swordBeamHealthAllowancePerLevel = (float) config.get("skills", "dss.config.server.skills.swordBeamHealthAllowancePerLevel", 0.6D,
				"Health allowance per level for Sword Beam, in half-hearts (0.0 = require full health; higher values allow more missing health per level)", 0.0D, 10.0D).getDouble();
		swordBeamBaseDamageFactor = config.get("skills", "dss.config.server.skills.swordBeamBaseDamageFactor", 30,
				"Base damage factor (percentage of sword damage) for Sword Beam", 0, 1000).getInt();
		swordBeamDamageFactorPerLevel = config.get("skills", "dss.config.server.skills.swordBeamDamageFactorPerLevel", 10,
				"Damage factor increase per level for Sword Beam", 0, 1000).getInt();
		swordBeamRangePerLevel = config.get("skills", "dss.config.server.skills.swordBeamRangePerLevel", 1,
				"Additional lifespan ticks per level for Sword Beam (1 = vanilla behavior; higher values increase range)", 0, 100).getInt();
		swordBasicRangeBase = config.get("skills", "dss.config.server.skills.swordBasicRangeBase", 6,
				"Base targeting range for Basic Sword (6 = vanilla behavior)", 0, 100).getInt();
		swordBasicRangePerLevel = config.get("skills", "dss.config.server.skills.swordBasicRangePerLevel", 1,
				"Additional targeting range per level for Basic Sword (1 = vanilla behavior)", 0, 100).getInt();
		swordBasicComboSizeBase = config.get("skills", "dss.config.server.skills.swordBasicComboSizeBase", 2,
				"Base max combo size for Basic Sword (2 = vanilla behavior)", 1, 100).getInt();
		swordBasicComboSizePerLevel = config.get("skills", "dss.config.server.skills.swordBasicComboSizePerLevel", 1,
				"Additional max combo size per level for Basic Sword (1 = vanilla behavior)", 0, 100).getInt();
		swordBasicComboDamageBonus = (float) config.get("skills", "dss.config.server.skills.swordBasicComboDamageBonus", 1.0D,
				"Flat damage bonus per combo hit for Basic Sword (1.0 = +1 damage per combo hit, vanilla behavior)", 0.0D, 100.0D).getDouble();
		backSliceDamageBonusPerLevel = (float) config.get("skills", "dss.config.server.skills.backSliceDamageBonusPerLevel", 0.1D,
				"Damage bonus per level for Back Slice (0.1 = +10% per level)", 0.0D, 10.0D).getDouble();
		leapingBlowDamageBonusPerLevel = (float) config.get("skills", "dss.config.server.skills.leapingBlowDamageBonusPerLevel", 1.0D,
				"Flat damage bonus per level for Leaping Blow (1.0 = +1 damage per level)", 0.0D, 100.0D).getDouble();
		leapingBlowLifespanPerLevel = config.get("skills", "dss.config.server.skills.leapingBlowLifespanPerLevel", 1,
				"Additional lifespan ticks per level for Leaping Blow (1 = vanilla behavior; controls how far the projectile travels)", 0, 100).getInt();
		leapingBlowRangeBase = config.get("skills", "dss.config.server.skills.leapingBlowRangeBase", 3.0D,
				"Base range for Leaping Blow damage falloff (3.0 = vanilla behavior)", 0.0D, 100.0D).getDouble();
		leapingBlowRangePerLevel = config.get("skills", "dss.config.server.skills.leapingBlowRangePerLevel", 1.0D,
				"Additional range per level for Leaping Blow damage falloff (1.0 = vanilla behavior)", 0.0D, 100.0D).getDouble();
		endingBlowDamageBonusPerLevel = (float) config.get("skills", "dss.config.server.skills.endingBlowDamageBonusPerLevel", 0.2D,
				"Damage bonus per level for Ending Blow (0.2 = +20% per level)", 0.0D, 10.0D).getDouble();
		endingBlowDurationBase = config.get("skills", "dss.config.server.skills.endingBlowDurationBase", 45,
				"Base duration (in ticks) of the defense-down effect for Ending Blow", 0, 1000).getInt();
		endingBlowDurationPerLevel = config.get("skills", "dss.config.server.skills.endingBlowDurationPerLevel", 5,
				"Duration reduction per level for Ending Blow (subtracted from base duration)", 0, 100).getInt();
		mortalDrawDamageBonusPerLevel = config.get("skills", "dss.config.server.skills.mortalDrawDamageBonusPerLevel", 10,
				"Damage bonus per level for Mortal Draw, in percent (10 = +10% per level)", 0, 1000).getInt();
		/* Loot / drops settings */
		bonusOrbEnable = config.get("drops", "dss.config.server.drops.bonusOrbEnable", false, "Whether all players should start with a Basic Skill orb").getBoolean(false);
		orbLootWeight = config.get("drops", "dss.config.server.drops.orbLootWeight", 1, "Weight for skill orbs when added to vanilla chest loot (0 to disable) [0-100]", 0, 100).setRequiresMcRestart(true).getInt();
		orbDropEnable = config.get("drops", "dss.config.server.drops.orbDropEnable", true, "Enable skill orbs to drop as loot from mobs (may still be disabled individually)").getBoolean(true);
		orbDropGeneralChance = 0.01F * (float)config.get("drops", "dss.config.server.drops.orbDropGeneralChance", 1, "Chance (as a percent) for generic mobs to drop a random skill orb [0-100]", 0, 100).getInt();
		orbDropRandomChance = 0.01F * (float)config.get("drops", "dss.config.server.drops.orbDropRandomChance", 10, "Chance (as a percent) for mobs with a specific skill orb drop to drop a random one instead [0-100]", 0, 100).getInt();
		orbDropChance = new HashMap<Integer, Float>(Skills.getSkillIdMap().size());
		for (Entry<Integer, ResourceLocation> entry : Skills.getSkillIdMap().entrySet()) {
			SkillBase skill = SkillRegistry.get(entry.getValue());
			int i = config.get("drops", "dss.config.server.drops.orbDropChance." + skill.getRegistryName().getPath(), 5, "Chance (in tenths of a percent) for Skill Orb of " + skill.getDisplayName() + " to drop when available (0 to disable) [0-1000]", 0, 1000).getInt();
			orbDropChance.put((int)skill.getId(), (0.001F * (float) i));
		}
		playerDropEnable = config.get("drops", "dss.config.server.drops.playerDropEnable", true, "Enable skill orbs to drop from players when killed in PvP").getBoolean(true);
		playerDropFactor = config.get("drops", "dss.config.server.drops.playerDropFactor", 5, "Factor by which to multiply chance for skill orb to drop by slain players [1-20]", 1, 20).getInt();
		skillSwordLootWeight = config.get("drops", "dss.config.server.drops.skillSwordLootWeight", 1, "Weight for random skill swords when added to vanilla chest loot (0 to disable) [0-100]", 0, 100).setRequiresMcRestart(true).getInt();
		if (config.hasChanged()) {
			config.save();
		}
	}

	private static void refreshSkillGroups() {
		String[] groups = SkillGroup.getAll().stream()
				.map(g -> g.label)
				.collect(Collectors.toList())
				.toArray(new String[0]);
		groups = config.get("skillGui", "dss.config.client.skillGui.skillGroups", groups, "Enter desired Skill Group labels in the order you wish them to appear, each on a separate line between the '<' and '>'").getStringList();
		int i = groups.length;
		for (String label : groups) {
			SkillGroup group = new SkillGroup(label).setDisplayName(label).register();
			if (group == null) {
				continue;
			}
			group.priority = i--;
			// Skill List for this group
			String[] groupSkills = SkillRegistry.getValues().stream()
					.filter(s -> s.displayInGroup(group))
					.map(s -> s.getRegistryName().toString())
					.collect(Collectors.toList())
					.toArray(new String[0]);
			groupSkills = config.get("skillGroupLists", label, groupSkills, "Enter skill registry names for each skill you wish to appear in this category, each on a separate line between the '<' and '>'").getStringList();
			Set<String> set = Sets.newHashSet(groupSkills);
			skillGroupLists.put(group.label, set);
		}
	}

	public static void postInit() {
		WeaponRegistry.INSTANCE.registerItems(swords, "Config", true);
		WeaponRegistry.INSTANCE.registerItems(weapons, "Config", false);
		WeaponRegistry.INSTANCE.forbidItems(forbidden_swords, "Config", true);
		WeaponRegistry.INSTANCE.forbidItems(forbidden_weapons, "Config", false);
		refreshSkillGroups();
		Config.loaded = true;
		if (config.hasChanged()) {
			config.save();
		}
	}
	/*================== CLIENT SIDE SETTINGS =====================*/
	public static int getHitsToDisplay() { return comboHudMaxHits; }
	public static boolean allowVanillaControls() { return allowVanillaControls; }
	public static boolean enableAdditionalControls() { return enableAdditionalControls; }
	public static boolean requiresDoubleTap() { return requireDoubleTap; }
	public static boolean requiresLockOn() { return requireLockOn; }
	public static boolean autoTargetEnabled() { return enableAutoTarget; }
	public static boolean canTargetPassiveMobs() { return enableTargetPassive; }
	public static boolean canTargetPlayers() { return enableTargetPlayer; }
	/* Skill GUI */
	public static boolean clickedGroupFilterSound() { return clickedGroupFilterSound; }
	public static boolean clickedPageSound() { return clickedPageSound; }
	public static boolean clickedSkillSound() { return clickedSkillSound; }
	public static boolean showBannedSkills() { return showBannedSkills; }
	public static boolean showPaginationLabels() { return showPaginationLabels; }
	public static boolean showPlainTextIndex() { return showPlainTextIndex; }
	public static boolean showSkillGroupTooltips() { return showSkillGroupTooltips; }
	public static boolean showUnknownSkills() { return showUnknownSkills; }
	public static boolean isSkillInGroup(SkillBase skill, SkillGroup group) {
		if (skill.getRegistryName() == null) { return false; }
		Set<String> set = skillGroupLists.get(group.label);
		String name = skill.getRegistryName().toString();
		String alt = skill.getRegistryName().getNamespace() + ":*";
		return set != null && (set.contains(name) || set.contains(alt));
	}
	/*================== SKILLS =====================*/
	public static boolean giveBonusOrb() { return bonusOrbEnable; }
	public static int getOrbLootWeight() { return orbLootWeight; }
	public static int getBaseSwingSpeed() { return baseSwingSpeed; }
	public static boolean areRandomSwordsEnabled() { return skillSwordRandom; }
	public static boolean areCreativeSwordsEnabled() { return skillSwordCreative; }
	public static boolean canDisarmorPlayers() { return backSliceDisarmorPlayer; }
	public static float getDisarmPenalty() { return parryDisarmPenalty; }
	public static float getDisarmTimingBonus() { return parryDisarmTimingBonus; }
	public static boolean canHighJump() { return risingCutHighJump; }
	public static int getSkillSwordLevel() { return skillSwordCreativeLevel; }
	public static int getSkillSwordLootWeight() { return skillSwordLootWeight; }
	public static float getArmorBreakDamageMultiplier() { return armorBreakDamageMultiplier; }
	public static float getSwordBeamHealthAllowancePerLevel() { return swordBeamHealthAllowancePerLevel; }
	public static int getSwordBeamBaseDamageFactor() { return swordBeamBaseDamageFactor; }
	public static int getSwordBeamDamageFactorPerLevel() { return swordBeamDamageFactorPerLevel; }
	public static int getSwordBeamRangePerLevel() { return swordBeamRangePerLevel; }
	public static int getSwordBasicRangeBase() { return swordBasicRangeBase; }
	public static int getSwordBasicRangePerLevel() { return swordBasicRangePerLevel; }
	public static int getSwordBasicComboSizeBase() { return swordBasicComboSizeBase; }
	public static int getSwordBasicComboSizePerLevel() { return swordBasicComboSizePerLevel; }
	public static float getSwordBasicComboDamageBonus() { return swordBasicComboDamageBonus; }
	public static float getBackSliceDamageBonusPerLevel() { return backSliceDamageBonusPerLevel; }
	public static float getLeapingBlowDamageBonusPerLevel() { return leapingBlowDamageBonusPerLevel; }
	public static int getLeapingBlowLifespanPerLevel() { return leapingBlowLifespanPerLevel; }
	public static double getLeapingBlowRangeBase() { return leapingBlowRangeBase; }
	public static double getLeapingBlowRangePerLevel() { return leapingBlowRangePerLevel; }
	public static float getEndingBlowDamageBonusPerLevel() { return endingBlowDamageBonusPerLevel; }
	public static int getEndingBlowDurationBase() { return endingBlowDurationBase; }
	public static int getEndingBlowDurationPerLevel() { return endingBlowDurationPerLevel; }
	public static int getMortalDrawDamageBonusPerLevel() { return mortalDrawDamageBonusPerLevel; }
	/** Returns amount of health that may be missing and still be able to activate certain skills (e.g. Super Spin Attack) */
	public static float getHealthAllowance(int level) {
		return (requireFullHealth ? 0.0F : (0.6F * level));
	}
	/** @return true if the skill has been disabled either by the server or client settings, or if it is null */
	public static final boolean isSkillDisabled(EntityPlayer player, @Nullable SkillBase skill) {
		return !Config.isSkillAllowed(skill) || DSSPlayerInfo.get(player).isSkillDisabled(skill);
	}
	/** @return true if the skill is allowed by the server, i.e. not banned */
	public static final boolean isSkillAllowed(@Nullable SkillBase skill) {
		return skill != null && skill.getRegistryName() != null && !bannedSkills.contains(skill.getRegistryName().toString());
	}
	/*================== DROPS =====================*/
	public static boolean arePlayerDropsEnabled() { return playerDropEnable; }
	public static float getPlayerDropFactor() { return playerDropFactor; }
	public static boolean areOrbDropsEnabled() { return orbDropEnable; }
	public static float getChanceForRandomDrop() { return orbDropRandomChance; }
	public static float getRandomMobDropChance() { return orbDropGeneralChance; }
	public static float getDropChance(int orbID) {
		return (orbDropChance.containsKey(orbID) ? orbDropChance.get(orbID) : 0.0F);
	}

	/**
	 * Updates client settings from server packet
	 */
	public static void syncClientSettings(SyncConfigPacket msg) {
		if (!msg.isMessageValid()) {
			DynamicSwordSkills.logger.error("Invalid SyncConfigPacket attempting to process!");
			return;
		}
		Config.baseSwingSpeed = msg.baseSwingSpeed;
		Config.requireFullHealth = msg.requireFullHealth;
		Config.bannedSkills.clear();
		for (Byte b : msg.disabledIds) {
			SkillBase skill = SkillRegistry.getSkillById(b);
			if (skill != null && skill.getRegistryName() != null) {
				Config.bannedSkills.add(skill.getRegistryName().toString());
			}
		}
	}
}