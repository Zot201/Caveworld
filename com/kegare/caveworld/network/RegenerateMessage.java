/*
 * Caveworld
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.caveworld.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

import com.kegare.caveworld.client.gui.GuiRegeneration;
import com.kegare.caveworld.core.Caveworld;
import com.kegare.caveworld.world.WorldProviderCaveworld;
import com.kegare.caveworld.world.WorldProviderDeepCaveworld;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RegenerateMessage implements IMessage, IMessageHandler<RegenerateMessage, IMessage>
{
	private boolean backup = true;

	public RegenerateMessage() {}

	public RegenerateMessage(boolean backup)
	{
		this.backup = backup;
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		backup = buffer.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		buffer.writeBoolean(backup);
	}

	@Override
	public IMessage onMessage(RegenerateMessage message, MessageContext ctx)
	{
		if (ctx.side.isClient())
		{
			Caveworld.proxy.displayClientGuiScreen(new GuiRegeneration(message.backup));
		}
		else
		{
			WorldProviderCaveworld.regenerate(message.backup);
			WorldProviderDeepCaveworld.regenerate(message.backup);
		}

		return null;
	}

	public static class ProgressNotify implements IMessage, IMessageHandler<ProgressNotify, IMessage>
	{
		private int task = -1;

		public ProgressNotify() {}

		public ProgressNotify(int task)
		{
			this.task = task;
		}

		@Override
		public void fromBytes(ByteBuf buffer)
		{
			task = buffer.readInt();
		}

		@Override
		public void toBytes(ByteBuf buffer)
		{
			buffer.writeInt(task);
		}

		@SideOnly(Side.CLIENT)
		@Override
		public IMessage onMessage(ProgressNotify message, MessageContext ctx)
		{
			Minecraft mc = FMLClientHandler.instance().getClient();

			if (mc.currentScreen != null && mc.currentScreen instanceof GuiRegeneration)
			{
				((GuiRegeneration)mc.currentScreen).updateProgress(message.task);
			}

			return null;
		}
	}
}