package mrriegel.limelib.tile;

import mrriegel.limelib.network.PacketHandler;
import mrriegel.limelib.network.TileMessage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommonTile extends TileEntity {

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound syncData = new NBTTagCompound();
		this.writeToNBT(syncData);
		return new SPacketUpdateTileEntity(this.pos, 1, syncData);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	public void sync() {
		markDirty();
		if (hasWorldObj() && !worldObj.isRemote)
			for (EntityPlayer p : worldObj.playerEntities) {
				if (p.getPosition().getDistance(pos.getX(), pos.getY(), pos.getZ()) < 32) {
					try {
						((EntityPlayerMP) p).connection.sendPacket(getUpdatePacket());
					} catch (Error e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}
	}

	public ItemStack[] getDroppingItems() {
		return new ItemStack[0];
	}

	public boolean openGUI(EntityPlayer player) {
		return false;
	}

	public void handleMessage(NBTTagCompound nbt) {
	}

	public void sendMessge(NBTTagCompound nbt) {
		nbt.setLong("pos", pos.toLong());
		PacketHandler.sendToServer(new TileMessage<CommonTile>(nbt));
	}
}