package com.peter.claims.event;

import com.peter.claims.Claims;
import com.peter.claims.claim.Claim;
import com.peter.claims.claim.ClaimStorage;
import static com.peter.claims.permission.ClaimPermissions.*;

import com.peter.claims.permission.ClaimPermission;
import com.peter.claims.permission.PermissionContainer;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorStandItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFrameItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockEvents {

    public static boolean blockBreakEvent(World world, PlayerEntity player, BlockPos pos, BlockState state,
            BlockEntity tile) {
        if(world.isClient)
            return true;
        Claim claim = ClaimStorage.getClaim(pos);
        PermissionContainer permissions = ClaimStorage.globalPermissions;
        if (claim != null) {
            permissions = claim.getPermissions(player.getUuid());
        }

        logCheck(PLACE_BREAK_PERM, "Block");

        if (!permissions.hasPerm(PLACE_BREAK_PERM)) {
            Claims.sendFailMessage((ServerPlayerEntity)player);
            return false;
        }
        
        return true;
    }
    
    public static ActionResult useBlockEvent(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if(world.isClient)
            return ActionResult.PASS;
        if (!checkBlockPerm(world, hit.getBlockPos(), player, hand, USE_BLOCK_PERM)) {
            Claims.sendFailMessage((ServerPlayerEntity)player);
            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }
    
    public static boolean checkBlockPerm(World world, BlockPos pos, PlayerEntity player, Hand hand, ClaimPermission defaultPerm) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        ItemStack stack = player.getMainHandStack();
        if (hand == Hand.OFF_HAND) {
            stack = player.getOffHandStack();
        }

        Item item = stack.getItem();
        boolean isBlockItem = item instanceof BlockItem || item instanceof ArmorStandItem || item instanceof ItemFrameItem;

        Identifier blockId = Registries.BLOCK.getId(block);
        ClaimPermission perm = defaultPerm;

        if (player.isSneaking() && isBlockItem) {
            perm = PLACE_BREAK_PERM;
        } else if (item instanceof EndCrystalItem) {
            perm = PLACE_END_CRYSTAL_PERM;
        } else if (item instanceof SpawnEggItem) {
            perm = SPAWN_ENTITY_PERMISSION;
        } else if (BLOCK_PERMS.containsKey(blockId)) {
            perm = BLOCK_PERMS.get(blockId);
        } else {
            if (blockState.getBlock() instanceof DoorBlock) {
                perm = USE_DOOR_PERM;
            } else if (blockState.getBlock() instanceof TrapdoorBlock) {
                perm = USE_TRAP_DOOR_PERM;
            } else if (blockState.getBlock() instanceof FenceGateBlock) {
                perm = USE_GATE_PERM;
            } else if (blockState.getBlock() instanceof ButtonBlock) {
                perm = USE_BUTTON_PERM;
            } else if (blockState.getBlock() instanceof LeverBlock) {
                perm = USE_LEVER_PERM;
            } else if (blockState.getBlock() instanceof LecternBlock) {
                perm = USE_LECTERN_PERM;
            } else if (blockState.getBlock() instanceof AbstractSignBlock) {
                perm = USE_SIGN_PERM;
            } else if (blockState.getBlock() instanceof BedBlock) {
                perm = USE_BED_PERM;
            } else if (blockState.getBlock() instanceof BeaconBlock) {
                perm = USE_BEACON_PERM;
            } else if (world.getBlockEntity(pos) != null) {
                BlockEntity e = world.getBlockEntity(pos);
                if (e instanceof Inventory) {
                    perm = OPEN_INVENTORY_PERM;
                }
            } else if (isBlockItem) {
                perm = PLACE_BREAK_PERM;
            }
        }
        
        logCheck(perm, "Block");

        return ClaimStorage.getPerms(pos, player).hasPerm(perm);
    }
}
