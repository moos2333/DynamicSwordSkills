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
 * - Reflection lookups now fail gracefully when a field or method is not present in the
 *   current mappings. Fixes a NoSuchFieldException crash in MortalDraw on production
 *   builds where EntityPlayer#itemStackMainHand may not exist.
 */

package dynamicswordskills.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DirtyEntityAccessor {

	/** Accessible reference to {@code EntityLivingBase#applyPotionDamageCalculations */
	private static Method applyPotionDamageCalculations;
	/** Accessible reference to {@code EntityLiving#experienceValue */
	private static Field experienceValue;
	/** Accessible reference to {@code EntityPlayer#itemStackMainHand */
	private static Field itemStackMainHand;
	/** Accessible reference to {@code PlayerControllerMP#syncCurrentPlayItem */
	private static Method syncCurrentPlayItem;
	/** Accessible reference to {@code EntityLivingBase#ticksSinceLastSwing */
	private static Field ticksSinceLastSwing;

	/**
	 * Attempts to find the first declared field matching any of the given names.
	 * Returns null if none of the names exist on the given class, so callers can
	 * skip optional optimizations instead of crashing the game.
	 */
	private static Field findFieldOrNull(Class<?> clazz, String... names) {
		for (String name : names) {
			try {
				Field field = clazz.getDeclaredField(name);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException ignored) {
				// Try the next candidate name
			} catch (Throwable ignored) {
				// Give up on this name
			}
		}
		return null;
	}

	/**
	 * Attempts to find the first declared method matching any of the given names.
	 * Returns null if none of the names exist on the given class.
	 */
	private static Method findMethodOrNull(Class<?> clazz, String[] names, Class<?>... parameterTypes) {
		for (String name : names) {
			try {
				Method method = clazz.getDeclaredMethod(name, parameterTypes);
				method.setAccessible(true);
				return method;
			} catch (NoSuchMethodException ignored) {
				// Try the next candidate name
			} catch (Throwable ignored) {
				// Give up on this name
			}
		}
		return null;
	}

	/**
	 * Returns the amount of damage the entity will receive after armor and potions are taken into account
	 */
	public static float getModifiedDamage(EntityLivingBase entity, DamageSource source, float amount) {
		if (applyPotionDamageCalculations == null) {
			applyPotionDamageCalculations = findMethodOrNull(EntityLivingBase.class,
					new String[] { "applyPotionDamageCalculations", "func_70672_c" },
					DamageSource.class, float.class);
		}
		// Don't want to actually damage the entity's armor at this point, so
		// reproduce parts of EntityLivingBase#applyArmorCalculations here:
		if (!source.isUnblockable()) {
			int armor = 25 - entity.getTotalArmorValue();
			amount = (amount * (float) armor) / 25.0F;
		}
		if (applyPotionDamageCalculations != null) {
			try {
				applyPotionDamageCalculations.invoke(entity, source, amount);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return Math.max(amount - entity.getAbsorptionAmount(), 0.0F);
	}

	/** Sets or adds to the amount of xp the entity will drop when killed */
	public static void setLivingXp(EntityLiving entity, int xp, boolean add) {
		if (experienceValue == null) {
			experienceValue = findFieldOrNull(EntityLiving.class, "experienceValue", "field_70728_aV");
		}
		if (experienceValue == null) {
			return;
		}
		try {
			int value = experienceValue.getInt(entity);
			experienceValue.set(entity, (add ? value + xp : xp));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Use to e.g. prevent setting the main hand stack from resetting the cooldown timer.
	 * If {@code EntityPlayer#itemStackMainHand} is not present in the current mappings,
	 * this method silently does nothing - the only side effect is that the attack
	 * cooldown may not be preserved across a weapon switch.
	 */
	public static void setItemStackMainHand(EntityPlayer player, ItemStack stack) {
		if (itemStackMainHand == null) {
			itemStackMainHand = findFieldOrNull(EntityPlayer.class, "itemStackMainHand", "field_184831_bT");
		}
		if (itemStackMainHand == null) {
			return;
		}
		try {
			itemStackMainHand.set(player, stack.copy());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Sets or adds to the amount of xp the entity will drop when killed */
	public static void setTicksSinceLastSwing(EntityLivingBase entity, int ticks) {
		if (ticksSinceLastSwing == null) {
			ticksSinceLastSwing = findFieldOrNull(EntityLivingBase.class, "ticksSinceLastSwing", "field_184617_aD");
		}
		if (ticksSinceLastSwing == null) {
			return;
		}
		try {
			ticksSinceLastSwing.set(entity, ticks);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Calls {@link PlayerControllerMP#syncCurrentPlayItem()} */
	@SideOnly(Side.CLIENT)
	public static void syncCurrentPlayItem(PlayerControllerMP player) {
		if (syncCurrentPlayItem == null) {
			syncCurrentPlayItem = findMethodOrNull(PlayerControllerMP.class,
					new String[] { "syncCurrentPlayItem", "func_78750_j" });
		}
		if (syncCurrentPlayItem == null) {
			return;
		}
		try {
			syncCurrentPlayItem.invoke(player);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}