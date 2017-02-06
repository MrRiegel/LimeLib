package mrriegel.limelib.block;

import java.util.List;

import mrriegel.limelib.LimeLib;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.helper.WorldHelper;
import mrriegel.limelib.tile.CommonTile;
import mrriegel.limelib.tile.IDataKeeper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

import com.google.common.collect.Lists;

public abstract class CommonBlockContainer<T extends CommonTile> extends CommonBlock {

	protected boolean clearRecipe = true;

	public CommonBlockContainer(Material materialIn, String name) {
		super(materialIn, name);
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if (worldIn.getTileEntity(pos) instanceof CommonTile)
			for (ItemStack stack : ((CommonTile) worldIn.getTileEntity(pos)).getDroppingItems())
				spawnAsEntity(worldIn, pos, stack.copy());
		worldIn.removeTileEntity(pos);
		worldIn.updateComparatorOutputLevel(pos, this);
	}

	@Override
	public void registerBlock() {
		super.registerBlock();
		GameRegistry.registerTileEntity(getTile(), getUnlocalizedName());
		if (clearRecipe && IDataKeeper.class.isAssignableFrom(getTile()) && !getItemBlock().getHasSubtypes()) {
			final ItemStack result = new ItemStack(this);
			ShapelessRecipes r = new ShapelessRecipes(NBTStackHelper.setBoolean(new ItemStack(this), "ClEaR", true), Lists.newArrayList(new ItemStack(this))) {
				@Override
				public ItemStack getCraftingResult(InventoryCrafting inv) {
					return result;
				}
			};
			RecipeSorter.register(LimeLib.MODID + ":idatakeeperClear", r.getClass(), Category.SHAPELESS, "after:minecraft:shapeless");
			GameRegistry.addRecipe(r);
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public abstract TileEntity createTileEntity(World world, IBlockState state);

	protected abstract Class<? extends T> getTile();

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		} else {
			TileEntity tile = worldIn.getTileEntity(pos);
			if (tile instanceof CommonTile) {
				return ((CommonTile) tile).openGUI((EntityPlayerMP) playerIn);
			}
			return false;
		}
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> lis = super.getDrops(world, pos, state, fortune);
		ItemStack stack = ItemStack.EMPTY;
		if (WorldHelper.getTile(world, pos) instanceof IDataKeeper && lis.size() == 1 && lis.get(0).getItem() == Item.getItemFromBlock(state.getBlock())) {
			IDataKeeper tile = (IDataKeeper) WorldHelper.getTile(world, pos);
			stack = lis.get(0).copy();
			NBTStackHelper.setBoolean(stack, "idatakeeper", true);
			tile.writeToStack(stack);
		}
		return !stack.isEmpty() ? Lists.newArrayList(stack) : lis;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (worldIn.getTileEntity(pos) instanceof IDataKeeper && NBTStackHelper.getBoolean(stack, "idatakeeper")) {
			IDataKeeper tile = (IDataKeeper) worldIn.getTileEntity(pos);
			tile.readFromStack(stack);
			worldIn.getTileEntity(pos).markDirty();
		}
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		List<ItemStack> lis = getDrops(worldIn, pos, state, 0);
		if (!player.capabilities.isCreativeMode && lis.size() == 1) {
			worldIn.setBlockToAir(pos);
			spawnAsEntity(worldIn, pos, lis.get(0));
		}
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		ItemStack stack = super.getPickBlock(state, target, world, pos, player);
		if (player.isSneaking() && player.capabilities.isCreativeMode && world.getTileEntity(pos) instanceof IDataKeeper && !stack.isEmpty()) {
			IDataKeeper tile = (IDataKeeper) world.getTileEntity(pos);
			NBTStackHelper.setBoolean(stack, "idatakeeper", true);
			tile.writeToStack(stack);
		}
		return stack;
	}

	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, player, tooltip, advanced);
		if (clearRecipe && NBTStackHelper.getBoolean(stack, "ClEaR"))
			tooltip.add(TextFormatting.YELLOW + "Clear content");
	}
}
