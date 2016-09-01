package mrriegel.testmod;

import java.util.Random;

import mrriegel.limelib.block.CommonBlock;
import mrriegel.limelib.network.PacketHandler;
import mrriegel.limelib.tile.CommonTileInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.text.WordUtils;

@Mod(modid = "lalal", name = "kohle")
public class TestMod implements IGuiHandler {

	@Mod.Instance("lalal")
	public static TestMod mod;

	public static final CommonBlock block = new TestBlock();

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		block.registerBlock();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		NetworkRegistry.INSTANCE.registerGuiHandler(mod, this);
		PacketHandler.registerMessage(TestMessage.class, Side.CLIENT);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}

	@SubscribeEvent
	public void jump(LivingJumpEvent e) {
		if (e.getEntityLiving() instanceof EntityPlayer) {
			NBTTagCompound x = new NBTTagCompound();
			x.setString("l", WordUtils.capitalizeFully(RandomStringUtils.randomAlphabetic(new Random().nextInt(6) + 4)));
			if (!e.getEntityLiving().worldObj.isRemote) {
				PacketHandler.sendTo(new TestMessage(x), (EntityPlayerMP) e.getEntityLiving());
			}
			// if (e.getEntityLiving().worldObj.isRemote)
			// IN.sendToServer(new TestMessage(x));
		}
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerTest(player.inventory, (CommonTileInventory) world.getTileEntity(new BlockPos(x, y, z)));
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new TestGui(new ContainerTest(player.inventory, (CommonTileInventory) world.getTileEntity(new BlockPos(x, y, z))));
	}

}
