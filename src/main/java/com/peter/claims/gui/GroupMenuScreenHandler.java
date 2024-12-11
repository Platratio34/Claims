package com.peter.claims.gui;

import com.peter.claims.claim.Claim;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GroupMenuScreenHandler extends ServerOnlyScreenHandler {

    protected final Claim claim;

    public GroupMenuScreenHandler(int syncId, PlayerInventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 1);

        this.claim = claim;

        inventory.items[0] = new ItemButton(new ItemStack(Items.BOOK), "Owner", (b, a) -> {
            GroupEditMenuScreenHandler.openMenu(player, claim, "Owner");
        });

        inventory.items[8] = new ItemButton(new ItemStack(Items.BARRIER), "Back", (b, a) -> {
            ClaimMenuScreenHandler.openClaimMenu(player, claim);
        });
    }

    public static void openMenu(ServerPlayerEntity player, Claim claim) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new GroupMenuScreenHandler(syncId, playerInventory, claim);
            }
            @Override
            public Text getDisplayName() {
                return Text.of(claim.getName() + " - Groups");
            }
        };
        player.openHandledScreen(fac);
    }
}
