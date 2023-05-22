package com.tac.guns.inventory.gear.armor;

import com.tac.guns.GunMod;
import com.tac.guns.item.IAmmo;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class AmmoSlot extends SlotItemHandler {

    public AmmoSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        if(index < 0) {
            GunMod.LOGGER.error("Somehow less then 0?: " + index);
        }
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof IAmmo;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.container.setChanged();
    }
}
