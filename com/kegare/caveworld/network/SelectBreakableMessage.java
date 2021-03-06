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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.kegare.caveworld.item.ItemMiningPickaxe;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class SelectBreakableMessage implements IMessage, IMessageHandler<SelectBreakableMessage, IMessage>
{
	private String selected;

	public SelectBreakableMessage() {}

	public SelectBreakableMessage(String selected)
	{
		this.selected = selected;
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		selected = ByteBufUtils.readUTF8String(buffer);
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		ByteBufUtils.writeUTF8String(buffer, selected);
	}

	@Override
	public IMessage onMessage(SelectBreakableMessage message, MessageContext ctx)
	{
		EntityPlayerMP player = ctx.getServerHandler().playerEntity;
		ItemStack current = player.getCurrentEquippedItem();

		if (current != null && current.getItem() instanceof ItemMiningPickaxe)
		{
			current.getTagCompound().setString("Blocks", message.selected);
		}

		return null;
	}
}