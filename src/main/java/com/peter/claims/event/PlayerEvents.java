package com.peter.claims.event;

import static com.peter.claims.permission.ClaimPermissions.*;

import org.jetbrains.annotations.Nullable;

import com.peter.claims.Claims;
import com.peter.claims.claim.ClaimStorage;
import com.peter.claims.permission.ClaimPermission;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class PlayerEvents {

    public static ActionResult onAttackEvent(PlayerEntity player, World world, Hand hand, Entity entity,
            @Nullable EntityHitResult hitResult) {
        if (world.isClient())
            return ActionResult.PASS;

        ClaimPermission perm = ATTACK_ENTITY_PERMISSION;

        boolean isBlockEntity = entity instanceof ArmorStandEntity || entity instanceof ItemFrameEntity;

        if (entity instanceof EndCrystalEntity) {
            perm = BREAK_END_CRYSTAL_PERM;
        } else if (entity instanceof BoatEntity) {
            perm = PLACE_BREAK_BOAT_PERM;
        } else if (isBlockEntity) {
            perm = PLACE_BREAK_PERM;
        }
        
        Claims.LOGGER.info("Checking permission {} (Attack Entity)", perm);
        
        if (!ClaimStorage.getPerms(entity.getBlockPos(), player).hasPerm(perm)) {
            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }
}
