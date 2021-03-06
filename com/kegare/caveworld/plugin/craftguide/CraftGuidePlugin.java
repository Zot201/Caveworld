/*
 * Caveworld
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.caveworld.plugin.craftguide;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional.Method;

public class CraftGuidePlugin
{
	public static final String MODID = "craftguide";

	public static boolean enabled()
	{
		return Loader.isModLoaded(MODID);
	}

	@Method(modid = MODID)
	public static void invoke()
	{
		new MiningPickaxeRecipeProvider();
	}
}