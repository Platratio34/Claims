package com.peter.claims.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ConfirmScreenHandler extends ServerOnlyScreenHandler {

    protected final Runnable confirm;
    protected final Runnable cancel;

    public ConfirmScreenHandler(int syncId, PlayerInventory playerInventory, Runnable confirm, Runnable cancel) {
        super(syncId, playerInventory, 1);
        this.cancel = cancel;
        this.confirm = confirm;

        inventory.items[3] = new ItemButton(Items.LIME_WOOL, (b, a) -> {
            confirm.run();
        }).setName(Text.of("Confirm").copy().formatted(Formatting.GREEN));
        inventory.items[5] = new ItemButton(Items.BARRIER, (b, a) -> {
            cancel.run();
        }).setName(Text.of("Cancel").copy().formatted(Formatting.RED));
    }

    public static void confirm(ServerPlayerEntity player, String name, Runnable confirm, Runnable cancel) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new ConfirmScreenHandler(syncId, playerInventory, confirm, cancel);
            }
            @Override
            public Text getDisplayName() {
                return Text.of(name);
            }
        };
        player.openHandledScreen(fac);
    }
}
