package com.peter.claims.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class ScreenInventory implements Inventory {

    protected int width;
    protected int height;
    protected ItemButton[] items;

    public ScreenInventory(int rows, int cols) {
        this.width = cols;
        this.height = rows;
        items = new ItemButton[width * height];
    }
    public ScreenInventory(int rows) {
        this.width = 9;
        this.height = rows;
        items = new ItemButton[width * height];
    }

    @Override
    public void clear() {
        return;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (items[slot] != null)
            return items[slot].icon;
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null)
                return false;
        }
        return true;
    }

    @Override
    public void markDirty() {
        
    }

    @Override
    public ItemStack removeStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        return;
    }

    @Override
    public int size() {
        return width * height;
    }

    public void click(int slotI, SlotActionType actionType) {
        if (slotI > items.length) {
            return;
        }
        if(items[slotI] != null)
            items[slotI].onClick(actionType);
    }

}
