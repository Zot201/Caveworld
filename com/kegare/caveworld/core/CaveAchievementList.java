/*
 * Caveworld
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.caveworld.core;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

import com.kegare.caveworld.block.CaveBlocks;
import com.kegare.caveworld.item.CaveItems;
import com.kegare.caveworld.util.ArrayListExtended;

public class CaveAchievementList
{
	private static final ArrayListExtended<Achievement> achievementList = new ArrayListExtended();

	public static final Achievement portal = new CaveAchievement("portal", -3, -1, Blocks.mossy_cobblestone, null).initIndependentStat().registerStat();
	public static final Achievement caveworld = new CaveAchievement("caveworld", -1, -1, CaveBlocks.caveworld_portal, null).initIndependentStat().registerStat();
	public static final Achievement cavenium = new CaveAchievement("cavenium", -3, 2, CaveItems.cavenium, caveworld).registerStat();
	public static final Achievement theMiner = new CaveAchievement("theMiner", 1, 2, Items.iron_pickaxe, caveworld).registerStat();
	public static final Achievement caveman = new CaveAchievement("caveman", 5, 2, Blocks.stone, caveworld).registerStat();
	public static final Achievement compCaving = new CaveAchievement("compCaving", 5, 5, Items.egg, caveman).registerStat();
	public static final Achievement deepCaves = new CaveAchievement("deepCaves", -1, 10, new ItemStack(Blocks.stonebrick, 1, 1), caveworld).registerStat();
	public static final Achievement underCaves = new CaveAchievement("underCaves", -1, 15, Blocks.water, deepCaves).registerStat();
	public static final Achievement backFromDeep = new CaveAchievement("backFromDeep", -5, 6, new ItemStack(CaveItems.cavenium, 1, 1), deepCaves).setSpecial().registerStat();

	public static void registerAchievements()
	{
		AchievementPage page = new AchievementPage("Caveworld");
		page.getAchievements().addAll(achievementList);

		AchievementPage.registerAchievementPage(page);
	}

	public static int getAchievementIndex(Achievement achievement)
	{
		for (int i = 0; i < achievementList.size(); ++i)
		{
			Achievement entry = achievementList.get(i);

			if (entry.statId.equals(achievement.statId))
			{
				return i;
			}
		}

		return -1;
	}

	public static Achievement getAchievement(int index)
	{
		return achievementList.get(index, null);
	}

	private static class CaveAchievement extends Achievement
	{
		public CaveAchievement(String name, int column, int row, Block block, Achievement parent)
		{
			this(name, column, row, new ItemStack(block), parent);
		}

		public CaveAchievement(String name, int column, int row, Item item, Achievement parent)
		{
			this(name, column, row, new ItemStack(item), parent);
		}

		public CaveAchievement(String name, int column, int row, ItemStack itemstack, Achievement parent)
		{
			super("achievement.caveworld." + name, "caveworld." + name, column, row, itemstack, parent);
		}

		@Override
		public Achievement registerStat()
		{
			achievementList.addIfAbsent(this);

			return super.registerStat();
		}
	}
}