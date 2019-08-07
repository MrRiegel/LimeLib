package kdp.limelib.util;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;

import org.apache.commons.lang3.Validate;

public final class StackWrapper {
    private ItemStack stack;
    private int size;

    public StackWrapper(ItemStack stack, int size) {
        super();
        setSize(size);
        setStack(stack);
    }

    public StackWrapper(ItemStack stack) {
        this(stack, stack.getCount());
    }

    private StackWrapper() {
    }

    public void readFromNBT(CompoundNBT compound) {
        CompoundNBT c = compound.getCompound("stack");
        stack = ItemStack.read(c);
        size = compound.getInt("size");
    }

    public CompoundNBT writeToNBT(CompoundNBT compound) {
        CompoundNBT c = new CompoundNBT();
        stack.write(c);
        compound.put("stack", c);
        compound.putInt("size", size);
        return compound;
    }

    @Override
    public String toString() {
        return "[" + size + "x" + stack.getItem().getTranslationKey() + "]";
    }

    public ItemStack getStack() {
        return stack;
    }

    @SuppressWarnings("deprecation")
    public void setStack(ItemStack stack) {
        Validate.isTrue(!stack.isEmpty());
        this.stack = ItemHandlerHelper.copyStackWithSize(stack, Math.min(size, stack.getItem().getMaxStackSize()));
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
        setStack(stack);
    }

    public StackWrapper copy() {
        return new StackWrapper(stack, size);
    }

    public boolean canInsert(ItemStack stack) {
        return ItemHandlerHelper.canItemStacksStack(stack, this.stack);
    }

    public boolean insert(ItemStack stack) {
        if (canInsert(stack))
            size += stack.getCount();
        else
            return false;
        return true;
    }

    public ItemStack extract(int size) {
        if (size <= 0)
            return ItemStack.EMPTY;
        size = Math.min(size, this.size);
        this.size -= size;
        return ItemHandlerHelper.copyStackWithSize(stack, size);

    }

    public static StackWrapper loadStackWrapperFromNBT(CompoundNBT nbt) {
        StackWrapper wrap = new StackWrapper();
        wrap.readFromNBT(nbt);
        return !wrap.getStack().isEmpty() ? wrap : null;
    }

    public static NonNullList<ItemStack> toStackList(List<StackWrapper> list) {
        NonNullList<ItemStack> lis = NonNullList.create();
        for (StackWrapper s : list) {
            if (s == null || s.getStack().isEmpty())
                continue;
            int value = s.size;
            int max = s.stack.getMaxStackSize();
            while (value > 0) {
                int f = Math.min(max, value);
                lis.add(ItemHandlerHelper.copyStackWithSize(s.stack, f));
                value -= f;
            }
        }
        return lis;
    }

    public static NonNullList<ItemStack> toStackList(StackWrapper wrap) {
        return toStackList(Collections.singletonList(wrap));
    }

    public static List<StackWrapper> toWrapperList(List<ItemStack> list) {
        List<StackWrapper> lis = Lists.newArrayList();
        for (ItemStack s : list) {
            if (s.isEmpty())
                continue;
            boolean added = false;
            for (StackWrapper li : lis) {
                ItemStack stack = li.getStack();
                if (ItemHandlerHelper.canItemStacksStack(s, stack)) {
                    li.size += s.getCount();
                    //					lis.get(i).setSize(lis.get(i).getSize() + s.getCount());
                    added = true;
                    break;
                }
            }
            if (!added)
                lis.add(new StackWrapper(s, s.getCount()));
        }
        return lis;
    }

    public static void add(StackWrapper wrap, List<StackWrapper> lis) {
        boolean added = false;
        for (StackWrapper w : lis)
            if (ItemHandlerHelper.canItemStacksStack(wrap.stack, w.stack)) {
                w.size += wrap.size;
                added = true;
                break;
            }
        if (!added)
            lis.add(wrap);
    }

}