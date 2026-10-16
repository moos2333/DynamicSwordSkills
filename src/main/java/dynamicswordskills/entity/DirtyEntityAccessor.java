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
 * - Reflection lookups now use a layered strategy that first consults FML's
 *   ObfuscationReflectionHelper (SRG-aware), then walks the inheritance chain,
 *   and finally tries every candidate name at each level. Failures degrade
 *   silently instead of crashing, so optional optimizations like preserving
 *   the attack cooldown across a weapon switch no longer take down the game.
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
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DirtyEntityAccessor {

	private static Method applyPotionDamageCalculations;
	private static Field experienceValue;
	private static Field itemStackMainHand;
	private static Method syncCurrentPlayItem;
	private static Field ticksSinceLastSwing;

	private static Field resolveField(Class<?> owner, String... candidates) {
		for (String candidate : candidates) {
			try {
				Field field = ObfuscationReflectionHelper.findField(owner, candidate);
				if (field != null) {
					field.setAccessible(true);
					return field;
				}
			} catch (Throwable ignored) {
			}
		}
		Class<?> current = owner;
		while (current != null && current != Object.class) {
			for (String candidate : candidates) {
				try {
					Field field = current.getDeclaredField(candidate);
					field.setAccessible(true);
					return field;
				} catch (Throwable ignored) {
				}
			}
			current = current.getSuperclass();
		}
		return null;
	}

	private static Method resolveMethod(Class<?> owner, String[] candidates, Class<?> returnType, Class<?>... parameterTypes) {
		for (String candidate : candidates) {
			try {
				Method method = ObfuscationReflectionHelper.findMethod(owner, candidate, returnType, parameterTypes);
				if (method != null) {
					method.setAccessible(true);
					return method;
				}
			} catch (Throwable ignored) {
			}
		}
		Class<?> current = owner;
		while (current != null && current != Object.class) {
			for (String candidate : candidates) {
				try {
					Method method = current.getDeclaredMethod(candidate, parameterTypes);
					method.setAccessible(true);
					return method;
				} catch (Throwable ignored) {
				}
			}
			current = current.getSuperclass();
		}
		return null;
	}

	public static float getModifiedDamage(EntityLivingBase entity, DamageSource source, float amount) {
		if (applyPotionDamageCalculations == null) {
			applyPotionDamageCalculations = resolveMethod(EntityLivingBase.class,
					new String[] { "applyPotionDamageCalculations", "func_70672_c" },
					Void.TYPE,
					DamageSource.class, float.class);
		}
		if (!source.isUnblockable()) {
			int armor = 25 - entity.getTotalArmorValue();
			amount = (amount * (float) armor) / 25.0F;
		}
		if (applyPotionDamageCalculations != null) {
			try {
				applyPotionDamageCalculations.invoke(entity, source, amount);
			} catch (Throwable ignored) {
			}
		}
		return Math.max(amount - entity.getAbsorptionAmount(), 0.0F);
	}

	public static void setLivingXp(EntityLiving entity, int xp, boolean add) {
		if (experienceValue == null) {
			experienceValue = resolveField(EntityLiving.class, "experienceValue", "field_70728_aV");
		}
		if (experienceValue == null) {
			return;
		}
		try {
			int value = experienceValue.getInt(entity);
			experienceValue.set(entity, (add ? value + xp : xp));
		} catch (Throwable ignored) {
		}
	}

	public static void setItemStackMainHand(EntityPlayer player, ItemStack stack) {
		if (itemStackMainHand == null) {
			itemStackMainHand = resolveField(EntityPlayer.class,
					"itemStackMainHand", "field_184831_bT");
		}
		if (itemStackMainHand == null) {
			return;
		}
		try {
			itemStackMainHand.set(player, stack.copy());
		} catch (Throwable ignored) {
		}
	}

	public static void setTicksSinceLastSwing(EntityLivingBase entity, int ticks) {
		if (ticksSinceLastSwing == null) {
			ticksSinceLastSwing = resolveField(EntityLivingBase.class,
					"ticksSinceLastSwing", "field_184617_aD");
		}
		if (ticksSinceLastSwing == null) {
			return;
		}
		try {
			ticksSinceLastSwing.set(entity, ticks);
		} catch (Throwable ignored) {
		}
	}

	@SideOnly(Side.CLIENT)
	public static void syncCurrentPlayItem(PlayerControllerMP player) {
		if (syncCurrentPlayItem == null) {
			syncCurrentPlayItem = resolveMethod(PlayerControllerMP.class,
					new String[] { "syncCurrentPlayItem", "func_78750_j" },
					Void.TYPE);
		}
		if (syncCurrentPlayItem == null) {
			return;
		}
		try {
			syncCurrentPlayItem.invoke(player);
		} catch (Throwable ignored) {
		}
	}
}