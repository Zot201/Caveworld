/*
 * Caveworld
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.caveworld.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraftforge.client.MinecraftForgeClient;

import com.kegare.caveworld.client.config.SelectBiomeEntry;
import com.kegare.caveworld.client.config.SelectBlockEntry;
import com.kegare.caveworld.client.config.SelectItemEntry;
import com.kegare.caveworld.client.renderer.RenderCaveman;
import com.kegare.caveworld.client.renderer.RenderMiningPickaxe;
import com.kegare.caveworld.client.renderer.RenderPortalCaveworld;
import com.kegare.caveworld.core.CommonProxy;
import com.kegare.caveworld.core.Config;
import com.kegare.caveworld.entity.EntityCaveman;
import com.kegare.caveworld.item.CaveItems;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void initializeConfigClasses()
	{
		Config.selectBlockEntryClass = SelectBlockEntry.class;
		Config.selectItemEntryClass = SelectItemEntry.class;
		Config.selectBiomeEntryClass = SelectBiomeEntry.class;
	}

	@Override
	public void registerRenderers()
	{
		RenderingRegistry.registerBlockHandler(new RenderPortalCaveworld());
		RenderingRegistry.registerEntityRenderingHandler(EntityCaveman.class, new RenderCaveman());

		MinecraftForgeClient.registerItemRenderer(CaveItems.mining_pickaxe, new RenderMiningPickaxe());
	}

	@Override
	public int getUniqueRenderType()
	{
		return RenderingRegistry.getNextAvailableRenderId();
	}

	@Override
	public void displayClientGuiScreen(Object obj)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();

		if (obj instanceof GuiScreen && (mc.currentScreen == null || mc.currentScreen.getClass() != obj.getClass()))
		{
			mc.displayGuiScreen((GuiScreen)obj);
		}
	}

	@Override
	public void destoryClientBlock(int x, int y, int z)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();
		int sideHit = mc.objectMouseOver.sideHit;

		mc.playerController.onPlayerDestroyBlock(x, y, z, sideHit);

		mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(2, x, y, z, sideHit));
	}
}