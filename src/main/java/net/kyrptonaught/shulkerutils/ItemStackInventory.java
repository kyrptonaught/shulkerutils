package net.kyrptonaught.shulkerutils;


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;


public class ItemStackInventory extends SimpleInventory {
    protected final ItemStack itemStack;
    protected final int SIZE;

    public ItemStackInventory(ItemStack stack, int SIZE) {
        super(getStacks(stack, SIZE).toArray(new ItemStack[SIZE]));
        itemStack = stack;
        this.SIZE = SIZE;
    }

    public static DefaultedList<ItemStack> getStacks(ItemStack usedStack, int SIZE) {
        NbtCompound compoundTag = usedStack.getSubNbt("BlockEntityTag");
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
        if (compoundTag != null && compoundTag.contains("Items", 9)) {
            Inventories.readNbt(compoundTag, itemStacks);
        }
        return itemStacks;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        NbtCompound blockEntityTag = itemStack.getSubNbt("BlockEntityTag");
        if (blockEntityTag == null)
            blockEntityTag = itemStack.getOrCreateSubNbt("BlockEntityTag");

        if (isEmpty()) {
            if (blockEntityTag.contains("Items")) blockEntityTag.remove("Items");
        } else {
            DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
            for (int i = 0; i < size(); i++) {
                itemStacks.set(i, getStack(i));
            }
            Inventories.writeNbt(blockEntityTag, itemStacks);
        }

        if (shouldDeleteNBT(blockEntityTag)) {
            itemStack.removeSubNbt("BlockEntityTag");
        }
    }

    public boolean shouldDeleteNBT(NbtCompound blockEntityTag) {
        if (!blockEntityTag.contains("Items"))
            return blockEntityTag.getKeys().size() == 0;
        return isEmpty();
    }

    @Override
    public void onClose(PlayerEntity playerEntity_1) {
        if (itemStack.getCount() > 1) {
            int count = itemStack.getCount();
            itemStack.setCount(1);
            playerEntity_1.giveItemStack(new ItemStack(itemStack.getItem(), count - 1));
        }
        markDirty();
        // itemStack.removeSubTag(QuickShulkerMod.MOD_ID);
    }
}