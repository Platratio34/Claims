package com.peter.claims.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerOnlyScreenHandler extends GenericContainerScreenHandler {

    protected final ScreenInventory inventory;
    protected final ServerPlayerEntity player;

    public ServerOnlyScreenHandler(int syncId, PlayerInventory playerInventory, int rows) {
        super(fromRows(rows), syncId, playerInventory, new ScreenInventory(rows), rows);
        this.inventory = (ScreenInventory) getInventory();
        this.player = (ServerPlayerEntity) playerInventory.player;
    }
    
    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        inventory.click(slotIndex, actionType);
    }

    @Override
    protected void addPlayerSlots(Inventory playerInventory, int left, int top) {
        return;
    }

    private static ScreenHandlerType<GenericContainerScreenHandler> fromRows(int rows) {
        return switch (rows) {
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            case 6 -> ScreenHandlerType.GENERIC_9X6;
            default -> ScreenHandlerType.GENERIC_9X1;
        };
    }
}
