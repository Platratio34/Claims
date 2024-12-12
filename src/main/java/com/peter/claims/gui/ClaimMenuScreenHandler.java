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

public class ClaimMenuScreenHandler extends ServerOnlyScreenHandler {

    protected final Claim claim;

    public ClaimMenuScreenHandler(int syncId, PlayerInventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 1);

        this.claim = claim;

        inventory.items[0] = new ItemButton(new ItemStack(Items.OAK_SIGN), "Edit Claim Name", (b, a) -> {
            StringInputScreenHandler.getString(player, (name, sh) -> {
                if(name != null)
                    claim.setName(name);
                openClaimMenu(player, claim);
            }, claim.getName(), "Change Claim Name");
        });

        inventory.items[1] = new ItemButton(new ItemStack(Items.BEACON), "Edit Permissions", (b, a) -> {
            GroupPermissionsMenuScreenHandler.openMenu((ServerPlayerEntity)playerInventory.player, claim);
        });

        inventory.items[2] = new ItemButton(new ItemStack(Items.WRITABLE_BOOK), "Edit Groups", (b, a) -> {
            GroupMenuScreenHandler.openMenu((ServerPlayerEntity)playerInventory.player, claim);
        });
    }

    public static void openClaimMenu(ServerPlayerEntity player, Claim claim) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new ClaimMenuScreenHandler(syncId, playerInventory, claim);
            }
            @Override
            public Text getDisplayName() {
                return Text.of("Claim - " + claim.getName());
            }
        };
        player.openHandledScreen(fac);
    }
}
