package com.peter.claims.permission;

import com.peter.claims.Claims;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ClaimPermission {

    public final Identifier id;
    public final Item icon;
    public final String name;

    public final PermissionState defaultState;

    public ClaimPermission(Identifier id, Item icon, String name, PermissionState defaultState) {
        this.id = id;
        this.icon = icon;
        this.name = name;
        this.defaultState = defaultState;
    }

    public ClaimPermission(String id, Item icon, String name, PermissionState defaultState) {
        this(Claims.id(id), icon, name, defaultState);
    }

    public ClaimPermission(String id, Item icon, String name) {
        this(Claims.id(id), icon, name, PermissionState.PROHIBITED);
    }

    public ClaimPermission(String id, Item icon) {
        this(Claims.id(id), icon, id, PermissionState.PROHIBITED);
    }

    public ClaimPermission(String id) {
        this(Claims.id(id), Items.PAPER, id, PermissionState.PROHIBITED);
    }

    public ItemStack getItemStack() {
        ItemStack stack = new ItemStack(icon);
        stack.set(DataComponentTypes.ITEM_NAME, Text.of(name));
        return stack;
    }

    public ItemStack getItemStack(PermissionState state) {
        ItemStack stack = new ItemStack(icon);
        stack.set(DataComponentTypes.ITEM_NAME, Text.of(name+": ").copy().append(state.getText()));
        return stack;
    }

    @Override
    public String toString() {
        return name + " ("+id+")";
    }

}
