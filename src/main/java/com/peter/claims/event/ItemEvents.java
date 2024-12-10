package com.peter.claims.event;

import com.peter.claims.Claims;
import com.peter.claims.claim.ClaimStorage;
import static com.peter.claims.permission.ClaimPermissions.*;

import com.peter.claims.permission.ClaimPermission;
import com.peter.claims.permission.PermissionContainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemEvents {

    public static ActionResult useItemEvent(PlayerEntity player, World world, Hand hand) {
        HitResult hitResult = player.raycast(player.getBlockInteractionRange(), 0, false);

        if (hitResult instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            ClaimPermission perm;
            PermissionContainer perms = ClaimStorage.getPerms(pos, player);
            ItemStack stack = player.getActiveItem();
            if (stack.getItem() instanceof BlockItem) {
                perm = PLACE_BREAK_PERM;
            } else {
                perm = USE_BLOCK_PERM;
            }

            if(perm != null) {
                Claims.LOGGER.info("Checking permission {} (Item -> Block)", perm);
                if (!perms.hasPerm(perm)) {
                    return ActionResult.FAIL;
                }
            }
        }

        return ActionResult.PASS;
    }
}
