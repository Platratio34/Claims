package com.peter.claims.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
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

    public ItemButton(Item icon, BiConsumer<ItemButton, SlotActionType> func) {
        this.icon = new ItemStack(icon);
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

    public ItemButton setName(Text name) {
        icon.set(DataComponentTypes.ITEM_NAME, name);
        return this;
    }

    public ItemButton setLore(List<Text> lore) {
        icon.set(DataComponentTypes.LORE, new LoreComponent(lore));
        return this;
    }
    
    public ItemButton setLore(Text lore) {
        List<Text> lines = new ArrayList<>();
        lines.add(lore);
        return setLore(lines);
    }

    public ItemButton setLore(Text ... lore) {
        List<Text> lines = new ArrayList<>();
        for (int i = 0; i < lore.length; i++) {
            lines.add(lore[i]);
        }
        return setLore(lines);
    }
    
    public ItemButton setLore(String lore) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of(lore));
        return setLore(lines);
    }

    public ItemButton setLore(String ... lore) {
        List<Text> lines = new ArrayList<>();
        for (int i = 0; i < lore.length; i++) {
            lines.add(Text.of(lore[i]));
        }
        return setLore(lines);
    }
}
