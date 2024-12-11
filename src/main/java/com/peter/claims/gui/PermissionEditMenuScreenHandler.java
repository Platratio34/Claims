package com.peter.claims.gui;

import com.peter.claims.claim.Claim;
import com.peter.claims.permission.ClaimPermission;
import com.peter.claims.permission.PermissionContainer;
import com.peter.claims.permission.PermissionState;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.peter.claims.permission.ClaimPermissions.*;
import static com.peter.claims.permission.PermissionState.ALLOWED;
import static com.peter.claims.permission.PermissionState.DEFAULT;
import static com.peter.claims.permission.PermissionState.PROHIBITED;

public class PermissionEditMenuScreenHandler extends ServerOnlyScreenHandler {

    protected final Claim claim;
    protected final String group;

    private static int getRowsForPerms() {
        int perms = PERMISSIONS.size();
        int rows = perms / 9;
        if (perms % 9 > 0)
            rows++;
        return rows;
    }

    public PermissionEditMenuScreenHandler(int syncId, PlayerInventory playerInventory, Claim claim, String group) {
        super(syncId, playerInventory, getRowsForPerms()+1);

        this.claim = claim;
        this.group = group;

        inventory.items[0] = new ItemButton(Items.BARRIER, "Back", (b, a) -> {
            GroupPermissionsMenuScreenHandler.openMenu(player, claim);
        });


        int i = 9;
        for (ClaimPermission perm : PERMISSIONS.values()) {
            inventory.items[i] = permButton(perm, claim.getPermission(group, perm));
            i++;
        }
    }

    public static void openMenu(ServerPlayerEntity player, Claim claim, String group) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new PermissionEditMenuScreenHandler(syncId, playerInventory, claim, group);
            }

            @Override
            public Text getDisplayName() {
                return Text.of(claim.getName() + " - " + group + " Permissions");
            }
        };
        player.openHandledScreen(fac);
    }
    
    protected void togglePermission(SlotActionType action, ClaimPermission permission, ItemButton button) {
        PermissionContainer perms = claim.getPermissions(group);
        PermissionState cState = perms.get(permission);
        if (action == SlotActionType.PICKUP || action == SlotActionType.PICKUP_ALL) {
            switch (cState) {
                case ALLOWED -> perms.setPerm(permission, PROHIBITED);
                case DEFAULT -> perms.setPerm(permission, ALLOWED);
                case PROHIBITED -> perms.setPerm(permission, ALLOWED);
            }
            claim.markDirty();
            button.setName(Text.of(permission.name + ": ").copy().append(perms.get(permission).getText()));
        } else if (action == SlotActionType.CLONE) {
            perms.setPerm(permission, DEFAULT);
            claim.markDirty();
            button.setName(Text.of(permission.name + ": ").copy().append(DEFAULT.getText()));
        }
    }

    protected ItemButton permButton(ClaimPermission permission, PermissionState state) {
        ItemButton button = new ItemButton(permission.getItemStack(state), (b, a) -> {
            togglePermission(a, permission, b);
        });
        return button;
    }
}
