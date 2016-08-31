package mrriegel.testmod;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import mrriegel.limelib.gui.CommonContainer;
import mrriegel.limelib.gui.CommonContainer.InvEntry;
import mrriegel.limelib.gui.CommonContainerTile;
import mrriegel.limelib.tile.CommonTileInventory;

public class ContainerTest extends CommonContainerTile {

	public ContainerTest(InventoryPlayer invPlayer, CommonTileInventory tile) {
		super(invPlayer, tile);
	}

	@Override
	protected void initSlots() {
		initPlayerSlots(20, 0);
		setSlots(getTile(), 0, 100, 2, 5, 0);
	}

	@Override
	protected List<Area> allowedSlots(ItemStack stack, IInventory inv, int index) {
		List<Area> lis = Lists.newArrayList();
		IInventory inv2 = inv instanceof InventoryPlayer ? getTile() : invPlayer;
		Area x=getAreaforEntire(inv2);
		if(x!=null)
			lis.add(x);
//		lis.add(new Area(inv2, 0, inv2.getSizeInventory()-1));
		return lis;
	}

}
