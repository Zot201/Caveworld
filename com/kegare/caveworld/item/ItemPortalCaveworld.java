/*
 * Caveworld
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.caveworld.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.kegare.caveworld.block.CaveBlocks;
import com.kegare.caveworld.core.CaveAchievementList;

public class ItemPortalCaveworld extends ItemBlock
{
	public ItemPortalCaveworld(Block block)
	{
		super(block);
		this.setMaxStackSize(1);
	}

	@Override
	public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			if (side == 0)
			{
				--y;
			}
			else if (side == 1)
			{
				++y;
			}
			else if (side == 2)
			{
				--z;
			}
			else if (side == 3)
			{
				++z;
			}
			else if (side == 4)
			{
				--x;
			}
			else if (side == 5)
			{
				++x;
			}

			if (CaveBlocks.caveworld_portal.func_150000_e(world, x, y, z))
			{
				world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, field_150939_a.stepSound.func_150496_b(), 1.0F, 2.0F);

				if (!player.capabilities.isCreativeMode && --itemstack.stackSize <= 0)
				{
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				}

				player.triggerAchievement(CaveAchievementList.portal);

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		return false;
	}
}