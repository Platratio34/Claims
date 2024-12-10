package com.peter.claims.gui;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class ItemButton {

    public ItemStack icon;
    private BiConsumer<ItemButton, SlotActionType> func;

    public ItemButton(ItemStack icon) {
        this.icon = icon;
    }

    public ItemButton(ItemStack icon, BiConsumer<ItemButton, SlotActionType> func) {
        this.icon = icon;
        this.func = func;
    }

    public ItemButton(Item icon, String name, BiConsumer<ItemButton, SlotActionType> func) {
        this.icon = new ItemStack(icon);
        this.icon.set(DataComponentTypes.ITEM_NAME, Text.of(name));
        this.func = func;
    }

    public ItemButton(ItemStack icon, String name, BiConsumer<ItemButton, SlotActionType> func) {
        this.icon = icon;
        this.icon.set(DataComponentTypes.ITEM_NAME, Text.of(name));
        this.func = func;
    }

    public ItemButton(ItemStack icon, Text name, BiConsumer<ItemButton, SlotActionType> func) {
        this.icon = icon;
        this.icon.set(DataComponentTypes.ITEM_NAME, name);
        this.func = func;
    }
    
    public void onClick(SlotActionType actionType) {
        func.accept(this, actionType);
    }

    public void setName(Text name) {
        icon.set(DataComponentTypes.ITEM_NAME, name);
    }
}
