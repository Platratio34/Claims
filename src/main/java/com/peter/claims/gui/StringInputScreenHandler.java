package com.peter.claims.gui;

import java.util.function.BiConsumer;

import com.peter.claims.Claims;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class StringInputScreenHandler extends AnvilScreenHandler {

    protected BiConsumer<String, StringInputScreenHandler> resConsumer;

    public StringInputScreenHandler(int syncId, PlayerInventory playerInventory, BiConsumer<String, StringInputScreenHandler> resConsumer, String cVal) {
        super(syncId, playerInventory);
        this.resConsumer = resConsumer;
        ItemStack s = new ItemStack(Items.PAPER);
        s.set(DataComponentTypes.ITEM_NAME, Text.of(cVal));
        input.setStack(0, s);
    }

    public static void getString(ServerPlayerEntity player, BiConsumer<String, StringInputScreenHandler> resConsumer, String cVal, Text name) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new StringInputScreenHandler(syncId, playerInventory, resConsumer, cVal);
            }

            @Override
            public Text getDisplayName() {
                return name;
            }
        };
        player.openHandledScreen(fac);
    }

    @Override
    protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
        String str = stack.get(DataComponentTypes.CUSTOM_NAME).toString();
        Claims.LOGGER.info("String: {}", str);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex <= 1) {
            resConsumer.accept(null, this);
            return;
        }
        if (slotIndex != OUTPUT_ID)
            return;

        String str = output.getStack(0).get(DataComponentTypes.CUSTOM_NAME).getString();
        resConsumer.accept(str, this);
    }
}
