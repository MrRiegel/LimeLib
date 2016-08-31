package mrriegel.limelib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class AbstractMessage<T extends AbstractMessage<T>> implements IMessage, IMessageHandler<T, IMessage> {

	protected NBTTagCompound nbt;

	public AbstractMessage() {
	}

	public AbstractMessage(NBTTagCompound nbt) {
		this.nbt = nbt;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, nbt);
	}

	public abstract void handleMessage(EntityPlayer player, NBTTagCompound nbt);

	// public static class Handler<T extends AbstractMessage<T>> implements
	// IMessageHandler<T, IMessage> {
	//
	// @Override
	// public IMessage onMessage(final T message, final MessageContext ctx) {
	// Runnable run = new Runnable() {
	// @Override
	// public void run() {
	// message.handleMessage(ctx.side.isClient() ?
	// Minecraft.getMinecraft().thePlayer : ctx.getServerHandler().playerEntity,
	// message.nbt);
	// }
	// };
	// (ctx.side.isClient() ? Minecraft.getMinecraft() :
	// ctx.getServerHandler().playerEntity.getServerWorld()).addScheduledTask(run);
	// return null;
	// }
	//
	// }

	@Override
	public IMessage onMessage(final AbstractMessage message, final MessageContext ctx) {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				message.handleMessage(ctx.side.isClient() ? Minecraft.getMinecraft().thePlayer : ctx.getServerHandler().playerEntity, message.nbt);
			}
		};
		(ctx.side.isClient() ? Minecraft.getMinecraft() : ctx.getServerHandler().playerEntity.getServerWorld()).addScheduledTask(run);
		return null;
	}

}
