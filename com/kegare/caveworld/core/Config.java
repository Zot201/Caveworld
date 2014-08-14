/*
 * Caveworld
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL.
 * Please check the contents of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package com.kegare.caveworld.core;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kegare.caveworld.api.BlockEntry;
import com.kegare.caveworld.api.CaveworldAPI;
import com.kegare.caveworld.client.config.VeinsEntry.VeinConfigEntry;
import com.kegare.caveworld.core.CaveBiomeManager.CaveBiome;
import com.kegare.caveworld.core.CaveVeinManager.CaveVein;
import com.kegare.caveworld.plugin.mceconomy.MCEconomyPlugin;
import com.kegare.caveworld.plugin.mceconomy.ShopEntry.ShopProductEntry;
import com.kegare.caveworld.util.CaveLog;
import com.kegare.caveworld.util.Version;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Config
{
	public static Configuration generalCfg;
	public static Configuration blocksCfg;
	public static Configuration dimensionCfg;
	public static Configuration biomesCfg;
	public static Configuration veinsCfg;

	private static final Map<Integer, Integer> biomesDefaultMap = Maps.newHashMap();

	public static boolean versionNotify;
	public static boolean deathLoseMiningCount;

	public static boolean portalCraftRecipe;
	public static boolean mossStoneCraftRecipe;

	public static boolean hardcore;

	public static boolean rope;

	public static int dimensionCaveworld;
	public static int subsurfaceHeight;
	public static boolean generateCaves;
	public static boolean generateRavine;
	public static boolean generateMineshaft;
	public static boolean generateStronghold;
	public static boolean generateLakes;
	public static boolean generateDungeons;
	public static boolean decorateVines;

	public static final int RENDER_TYPE_PORTAL = Caveworld.proxy.getUniqueRenderType();

	public static File getConfigFile(String name)
	{
		File dir = new File(Loader.instance().getConfigDir(), "caveworld");

		if (!dir.exists())
		{
			dir.mkdirs();
		}

		return new File(dir, "caveworld-" + name + ".cfg");
	}

	public static Configuration loadConfig(String name)
	{
		File file = getConfigFile(name);
		Configuration config = new Configuration(file, null, true);

		try
		{
			config.load();
		}
		catch (Exception e)
		{
			File dest = new File(file.getParentFile(), file.getName() + ".bak");

			if (dest.exists())
			{
				dest.delete();
			}

			file.renameTo(dest);

			CaveLog.log(Level.ERROR, e, "A critical error occured reading the " + file.getName() + " file, defaults will be used - the invalid file is backed up at " + dest.getName());
		}

		return config;
	}

	public static String getConfigName(Configuration config)
	{
		String name = FilenameUtils.getBaseName(config.toString());

		if (name != null && name.startsWith("caveworld-"))
		{
			return name.substring(name.lastIndexOf('-') + 1);
		}

		return null;
	}

	public static Optional<Class> VEIN_ENTRY = Optional.absent();

	@SideOnly(Side.CLIENT)
	private static void initializeConfigEntryClasses()
	{
		VEIN_ENTRY = Optional.of((Class)VeinConfigEntry.class);

		if (MCEconomyPlugin.enabled())
		{
			MCEconomyPlugin.PRODUCT_ENTRY = Optional.of((Class)ShopProductEntry.class);
		}
	}

	static
	{
		try
		{
			initializeConfigEntryClasses();
		}
		catch (NoSuchMethodError e) {}
	}

	public static void syncConfig()
	{
		syncGeneralCfg();
		syncBlocksCfg();
		syncDimensionCfg();
	}

	public static void syncPostConfig()
	{
		syncBiomesCfg();
		syncVeinsCfg();
	}

	public static void syncGeneralCfg()
	{
		String category = Configuration.CATEGORY_GENERAL;
		Property prop;
		List<String> propOrder = Lists.newArrayList();

		if (generalCfg == null)
		{
			generalCfg = loadConfig(category);
		}

		prop = generalCfg.get(category, "versionNotify", true);
		prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		prop.comment += Configuration.NEW_LINE;
		prop.comment += "Note: If multiplayer, does not have to match client-side and server-side.";
		propOrder.add(prop.getName());
		versionNotify = prop.getBoolean(versionNotify);
		prop = generalCfg.get(category, "deathLoseMiningCount", false);
		prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		prop.comment += Configuration.NEW_LINE;
		prop.comment += "Note: If multiplayer, server-side only.";
		propOrder.add(prop.getName());
		deathLoseMiningCount = prop.getBoolean(deathLoseMiningCount);

		generalCfg.setCategoryPropertyOrder(category, propOrder);

		category = "recipes";
		prop = generalCfg.get(category, "portalCraftRecipe", Version.DEV_DEBUG);
		prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName()).setRequiresMcRestart(true);
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		portalCraftRecipe = prop.getBoolean(portalCraftRecipe);
		prop = generalCfg.get(category, "mossStoneCraftRecipe", true);
		prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName()).setRequiresMcRestart(true);
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		mossStoneCraftRecipe = prop.getBoolean(mossStoneCraftRecipe);

		generalCfg.setCategoryPropertyOrder(category, propOrder);
		generalCfg.setCategoryRequiresMcRestart(category, true);

		category = "options";
		prop = generalCfg.get(category, "hardcore", false);
		prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		prop.comment += Configuration.NEW_LINE;
		prop.comment += "Note: If multiplayer, server-side only.";
		propOrder.add(prop.getName());
		hardcore = prop.getBoolean(hardcore);

		generalCfg.setCategoryPropertyOrder(category, propOrder);

		if (generalCfg.hasChanged())
		{
			generalCfg.save();
		}
	}

	public static void syncBlocksCfg()
	{
		String category = "blocks";
		Property prop;
		List<String> propOrder = Lists.newArrayList();

		if (blocksCfg == null)
		{
			blocksCfg = loadConfig(category);
		}

		blocksCfg.addCustomCategoryComment(category, "If multiplayer, values must match on client-side and server-side.");

		prop = blocksCfg.get(category, "Rope", true);
		prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName()).setRequiresMcRestart(true);
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		rope = prop.getBoolean(rope);

		blocksCfg.setCategoryPropertyOrder(category, propOrder);
		blocksCfg.setCategoryRequiresMcRestart(category, true);

		if (blocksCfg.hasChanged())
		{
			blocksCfg.save();
		}
	}

	public static void syncDimensionCfg()
	{
		String category = "caveworld";
		Property prop;
		List<String> propOrder = Lists.newArrayList();

		if (dimensionCfg == null)
		{
			dimensionCfg = loadConfig("dimension");
		}

		dimensionCfg.addCustomCategoryComment(category, "If multiplayer, server-side only.");

		prop = dimensionCfg.get(category, "dimensionCaveworld", -5);
		prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName()).setRequiresMcRestart(true);
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		dimensionCaveworld = prop.getInt(dimensionCaveworld);
		prop = dimensionCfg.get(category, "subsurfaceHeight", 127);
		prop.setMinValue(63).setMaxValue(255).setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [range: " + prop.getMinValue() + " ~ " + prop.getMaxValue() + ", default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		subsurfaceHeight = prop.getInt(subsurfaceHeight);
		prop = dimensionCfg.get(category, "generateCaves", true);
		prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		generateCaves = prop.getBoolean(generateCaves);
		prop = dimensionCfg.get(category, "generateRavine", true);
		prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		generateRavine = prop.getBoolean(generateRavine);
		prop = dimensionCfg.get(category, "generateMineshaft", true);
		prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		generateMineshaft = prop.getBoolean(generateMineshaft);
		prop = dimensionCfg.get(category, "generateStronghold", true);
		prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		generateStronghold = prop.getBoolean(generateStronghold);
		prop = dimensionCfg.get(category, "generateLakes", true);
		prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		generateLakes = prop.getBoolean(generateLakes);
		prop = dimensionCfg.get(category, "generateDungeons", true);
		prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		generateDungeons = prop.getBoolean(generateDungeons);
		prop = dimensionCfg.get(category, "decorateVines", true);
		prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName());
		prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
		prop.comment += " [default: " + prop.getDefault() + "]";
		propOrder.add(prop.getName());
		decorateVines = prop.getBoolean(decorateVines);

		dimensionCfg.setCategoryPropertyOrder(category, propOrder);

		if (dimensionCfg.hasChanged())
		{
			dimensionCfg.save();
		}
	}

	public static void syncBiomesCfg()
	{
		String category = "biomes";
		Property prop;
		List<String> propOrder = Lists.newArrayList();

		if (biomesCfg == null)
		{
			biomesCfg = loadConfig(category);
		}

		CaveworldAPI.clearCaveBiomes();

		if (biomesDefaultMap.isEmpty())
		{
			biomesDefaultMap.put(BiomeGenBase.ocean.biomeID, 15);
			biomesDefaultMap.put(BiomeGenBase.plains.biomeID, 100);
			biomesDefaultMap.put(BiomeGenBase.desert.biomeID, 70);
			biomesDefaultMap.put(BiomeGenBase.desertHills.biomeID, 10);
			biomesDefaultMap.put(BiomeGenBase.forest.biomeID, 80);
			biomesDefaultMap.put(BiomeGenBase.forestHills.biomeID, 10);
			biomesDefaultMap.put(BiomeGenBase.taiga.biomeID, 80);
			biomesDefaultMap.put(BiomeGenBase.taigaHills.biomeID, 10);
			biomesDefaultMap.put(BiomeGenBase.jungle.biomeID, 80);
			biomesDefaultMap.put(BiomeGenBase.jungleHills.biomeID, 10);
			biomesDefaultMap.put(BiomeGenBase.swampland.biomeID, 60);
			biomesDefaultMap.put(BiomeGenBase.extremeHills.biomeID, 30);
			biomesDefaultMap.put(BiomeGenBase.icePlains.biomeID, 15);
			biomesDefaultMap.put(BiomeGenBase.iceMountains.biomeID, 15);
			biomesDefaultMap.put(BiomeGenBase.mushroomIsland.biomeID, 10);
			biomesDefaultMap.put(BiomeGenBase.savanna.biomeID, 50);
			biomesDefaultMap.put(BiomeGenBase.mesa.biomeID, 50);
		}

		String name;
		int weight;
		String block;
		int metadata;

		for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray())
		{
			if (biome == null)
			{
				continue;
			}

			propOrder.clear();
			name = String.valueOf(biome.biomeID);
			prop = biomesCfg.get(name, "genWeight", biomesDefaultMap.containsKey(biome.biomeID) ? biomesDefaultMap.get(biome.biomeID) : 0);
			prop.setMinValue(0).setMaxValue(100).setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName());
			prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
			prop.comment += " [range: " + prop.getMinValue() + " ~ " + prop.getMaxValue() + ", default: " + prop.getDefault() + "]";
			propOrder.add(prop.getName());
			weight = MathHelper.clamp_int(prop.getInt(), Integer.valueOf(prop.getMinValue()), Integer.valueOf(prop.getMaxValue()));
			prop = biomesCfg.get(name, "terrainBlock", Block.blockRegistry.getNameForObject(Blocks.stone));
			prop.setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName());
			prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
			prop.comment += " [default: " + prop.getDefault() + "]";
			propOrder.add(prop.getName());
			block = prop.getString();
			prop = biomesCfg.get(name, "terrainBlockMetadata", 0);
			prop.setMinValue(0).setMaxValue(15).setLanguageKey(Caveworld.CONFIG_LANG + category + '.' + prop.getName());
			prop.comment = StatCollector.translateToLocal(prop.getLanguageKey() + ".tooltip");
			prop.comment += " [range: " + prop.getMinValue() + " ~ " + prop.getMaxValue() + ", default: " + prop.getDefault() + "]";
			propOrder.add(prop.getName());
			metadata = MathHelper.clamp_int(prop.getInt(), Integer.valueOf(prop.getMinValue()), Integer.valueOf(prop.getMaxValue()));

			if (BiomeDictionary.isBiomeRegistered(biome))
			{
				Set<String> types = Sets.newHashSet();

				for (Type type : BiomeDictionary.getTypesForBiome(biome))
				{
					types.add(type.name());
				}

				biomesCfg.addCustomCategoryComment(name, biome.biomeName + ": " + Joiner.on(", ").skipNulls().join(types));
			}
			else
			{
				biomesCfg.addCustomCategoryComment(name, biome.biomeName);
			}

			biomesCfg.setCategoryPropertyOrder(name, propOrder);

			if (weight > 0)
			{
				CaveworldAPI.addCaveBiome(new CaveBiome(biome, weight, new BlockEntry(block, metadata)));
			}
		}

		if (biomesCfg.hasChanged())
		{
			biomesCfg.save();
		}
	}

	public static void syncVeinsCfg()
	{
		if (veinsCfg == null)
		{
			veinsCfg = loadConfig("veins");
		}

		CaveworldAPI.clearCaveVeins();

		if (veinsCfg.getCategoryNames().isEmpty())
		{
			Map<String, CaveVein> veins = Maps.newHashMap();

			veins.put("Coal Ore Vein", new CaveVein(new BlockEntry(Blocks.coal_ore, 0), 16, 20, 0, 255));
			veins.put("Iron Ore Vein", new CaveVein(new BlockEntry(Blocks.iron_ore, 0), 10, 28, 0, 255));
			veins.put("Gold Ore Vein", new CaveVein(new BlockEntry(Blocks.gold_ore, 0), 8, 2, 0, 127));
			veins.put("Redstone Ore Vein", new CaveVein(new BlockEntry(Blocks.redstone_ore, 0), 7, 8, 0, 40));
			veins.put("Lapis Ore Vein", new CaveVein(new BlockEntry(Blocks.lapis_ore, 0), 5, 1, 0, 40));
			veins.put("Diamond Ore Vein", new CaveVein(new BlockEntry(Blocks.diamond_ore, 0), 8, 1, 0, 20));
			veins.put("Emerald Ore Vein", new CaveVein(new BlockEntry(Blocks.emerald_ore, 0), 5, 3, 50, 255, null, Type.MOUNTAIN, Type.HILLS));
			veins.put("Quartz Ore Vein", new CaveVein(new BlockEntry(Blocks.quartz_ore, 0), 10, 16, 0, 255, new BlockEntry(Blocks.netherrack, 0), Type.NETHER));
			veins.put("Dirt Vein", new CaveVein(new BlockEntry(Blocks.dirt, 0), 24, 18, 0, 255));
			veins.put("Gravel Vein", new CaveVein(new BlockEntry(Blocks.gravel, 0), 20, 6, 0, 255));
			veins.put("Sand Vein, 0", new CaveVein(new BlockEntry(Blocks.sand, 0), 20, 8, 0, 255, null, Type.SANDY));
			veins.put("Sand Vein, 1", new CaveVein(new BlockEntry(Blocks.sand, 0), 20, 8, 0, 20, new BlockEntry(Blocks.gravel, 0), Type.SANDY));
			veins.put("Soul Sand Vein", new CaveVein(new BlockEntry(Blocks.soul_sand, 0), 20, 10, 0, 255, new BlockEntry(Blocks.netherrack, 0), Type.NETHER));
			veins.put("Hardened Clay Vein, 0", new CaveVein(new BlockEntry(Blocks.hardened_clay, 1), 24, 20, 0, 255, new BlockEntry(Blocks.dirt, 0), Type.MESA));
			veins.put("Hardened Clay Vein, 1", new CaveVein(new BlockEntry(Blocks.hardened_clay, 12), 24, 14, 0, 255, new BlockEntry(Blocks.dirt, 0), Type.MESA));

			for (Map.Entry<String, CaveVein> entry : veins.entrySet())
			{
				CaveworldAPI.addCaveVeinWithConfig(entry.getKey(), entry.getValue());
			}
		}
		else
		{
			ConfigCategory category;

			for (String name : veinsCfg.getCategoryNames())
			{
				category = veinsCfg.getCategory(name);

				if (category.get("genWeight").getInt() <= 0)
				{
					veinsCfg.removeCategory(category);
				}
				else
				{
					CaveworldAPI.addCaveVeinFromConfig(name);
				}
			}
		}

		if (veinsCfg.hasChanged())
		{
			veinsCfg.save();
		}
	}
}