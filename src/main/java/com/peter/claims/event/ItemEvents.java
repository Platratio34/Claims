package com.peter.claims.event;

import com.peter.claims.Claims;
import com.peter.claims.claim.ClaimStorage;
import static com.peter.claims.permission.ClaimPermissions.*;

import com.peter.claims.permission.ClaimPermission;
import com.peter.claims.permission.PermissionContainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BoatItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemEvents {

    public static ActionResult useItemEvent(PlayerEntity player, World world, Hand hand) {
        if(world.isClient)
            return ActionResult.PASS;
        HitResult hitResult = player.raycast(player.getBlockInteractionRange(), 0, false);

        if (hitResult instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            ClaimPermission perm = USE_BLOCK_PERM;
            PermissionContainer perms = ClaimStorage.getPerms(pos, player);
            ItemStack stack = player.getMainHandStack();
            if(hand == Hand.OFF_HAND)
                stack = player.getOffHandStack();
            Item item = stack.getItem();
            if (item instanceof BlockItem || item instanceof BucketItem) {
                perm = PLACE_BREAK_PERM;
            } else if (item instanceof BoatItem) {
                perm = PLACE_BREAK_BOAT_PERM;
            } else if (item instanceof SpawnEggItem) {
                perm = PLACE_BREAK_BOAT_PERM;
            } else {
                Claims.LOGGER.info("{}", item.getClass().getName());
            }

            if(perm != null) {
                Claims.LOGGER.info("Checking permission {} (Item -> Block)", perm);
                if (!perms.hasPerm(perm)) {
                    Claims.sendFailMessage((ServerPlayerEntity)player);
                    return ActionResult.FAIL;
                }
            }
        }

        return ActionResult.PASS;
    }
}
