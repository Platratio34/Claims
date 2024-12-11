package com.peter.claims.permission;

import java.util.HashMap;

import com.peter.claims.Claims;

import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import static com.peter.claims.permission.PermissionState.*;

public class ClaimPermissions {

    public static final HashMap<Identifier, ClaimPermission> PERMISSIONS = new HashMap<>();

    public static final ClaimPermission EDIT_CLAIM_PERM = registerPerm(
            new ClaimPermission("edit_claim", Items.GOLD_BLOCK, "Edit Claim"));

    public static final ClaimPermission PLACE_BREAK_PERM = registerPerm(
            new ClaimPermission("place_break_block", Items.IRON_PICKAXE, "Place/Break"));

    public static final ClaimPermission USE_BLOCK_PERM = registerPerm(
            new ClaimPermission("use_block", Items.STICK, "Use Block"));
    public static final ClaimPermission OPEN_INVENTORY_PERM = registerPerm(
            new ClaimPermission("open_inventory", Items.CHEST, "Open Inventories"));

    public static final ClaimPermission USE_DOOR_PERM = registerPerm(
            new ClaimPermission("use_door_block", Items.OAK_DOOR, "Open Door", ALLOWED));
    public static final ClaimPermission USE_TRAP_DOOR_PERM = registerPerm(
            new ClaimPermission("use_trap_door_block", Items.OAK_TRAPDOOR, "Open Trapdoor"));
    public static final ClaimPermission USE_GATE_PERM = registerPerm(
            new ClaimPermission("use_gate_block", Items.OAK_FENCE_GATE, "Open Gate"));

    public static final ClaimPermission USE_BUTTON_PERM = registerPerm(
            new ClaimPermission("use_button", Items.OAK_BUTTON, "Use Button"));
    public static final ClaimPermission USE_LEVER_PERM = registerPerm(
            new ClaimPermission("use_lever", Items.LEVER, "Use Lever"));

    public static final ClaimPermission USE_WORKSTATION_PERM = registerPerm(
            new ClaimPermission("use_workstation", Items.CRAFTING_TABLE, "Use Workstations", ALLOWED));
    public static final ClaimPermission USE_BEACON_PERM = registerPerm(
            new ClaimPermission("use_beacon", Items.BEACON, "Edit Beacon"));
    public static final ClaimPermission USE_LECTERN_PERM = registerPerm(
            new ClaimPermission("use_lectern", Items.LECTERN, "Use Lectern", ALLOWED));
    public static final ClaimPermission USE_SIGN_PERM = registerPerm(
            new ClaimPermission("use_sign", Items.OAK_SIGN, "Edit Sign"));
    public static final ClaimPermission USE_BED_PERM = registerPerm(
            new ClaimPermission("use_bed", Items.RED_BED, "Use Bed", ALLOWED));

    public static final ClaimPermission PLACE_BREAK_BOAT_PERM = registerPerm(
            new ClaimPermission("place_break_boat", Items.OAK_BOAT, "Place/Break Boat", ALLOWED));

    public static final ClaimPermission PLACE_END_CRYSTAL_PERM = registerPerm(
            new ClaimPermission("place_end_crystal", Items.END_CRYSTAL, "Place End Crystal", PROHIBITED));
    public static final ClaimPermission BREAK_END_CRYSTAL_PERM = registerPerm(
            new ClaimPermission("break_end_crystal", Items.END_CRYSTAL, "Break End Crystal", PROHIBITED));

    public static final ClaimPermission ATTACK_ENTITY_PERMISSION = registerPerm(
            new ClaimPermission("attack_entity", Items.IRON_SWORD, "Attack Entity", PROHIBITED));
    public static final ClaimPermission SPAWN_ENTITY_PERMISSION = registerPerm(
            new ClaimPermission("spawn_entity", Items.ZOMBIE_SPAWN_EGG, "Spawn Entity", PROHIBITED));
    public static final ClaimPermission USE_ENTITY_PERMISSION = registerPerm(
            new ClaimPermission("use_entity", Items.ITEM_FRAME, "Use Entity", ALLOWED));

    public static final HashMap<Identifier, ClaimPermission> BLOCK_PERMS = new HashMap<>();

    static {
        BLOCK_PERMS.put(Identifier.ofVanilla("iron_door"), USE_BLOCK_PERM);
        BLOCK_PERMS.put(Identifier.ofVanilla("iron_trapdoor"), USE_BLOCK_PERM);

        BLOCK_PERMS.put(Identifier.ofVanilla("crafting_table"), USE_WORKSTATION_PERM);
        BLOCK_PERMS.put(Identifier.ofVanilla("loom"), USE_WORKSTATION_PERM);
        BLOCK_PERMS.put(Identifier.ofVanilla("cartography_table"), USE_WORKSTATION_PERM);
        BLOCK_PERMS.put(Identifier.ofVanilla("smithing_table"), USE_WORKSTATION_PERM);
        BLOCK_PERMS.put(Identifier.ofVanilla("grindstone"), USE_WORKSTATION_PERM);
        BLOCK_PERMS.put(Identifier.ofVanilla("anvil"), USE_WORKSTATION_PERM);
        BLOCK_PERMS.put(Identifier.ofVanilla("stonecutter"), USE_WORKSTATION_PERM);
        BLOCK_PERMS.put(Identifier.ofVanilla("enchanting_table"), USE_WORKSTATION_PERM);
        BLOCK_PERMS.put(Identifier.ofVanilla("brewing_stand"), USE_WORKSTATION_PERM);
        BLOCK_PERMS.put(Identifier.ofVanilla("anvil"), USE_WORKSTATION_PERM);
    }

    public static ClaimPermission registerPerm(ClaimPermission perm) {
        if (PERMISSIONS.containsKey(perm.id))
            throw new IllegalStateException("Permission with id " + perm.id + " already registered");
        PERMISSIONS.put(perm.id, perm);
        return perm;
    }
    
    private static boolean logChecks = false;

    public static void logCheck(ClaimPermission permission, String source) {
        if (!logChecks)
            return;
        Claims.LOGGER.info("Checking permission {} ({})", permission, source);
    }

    public static void logCheck(Identifier permission, String source) {
        if(!logChecks)
            return;
        Claims.LOGGER.info("Checking permission {} ({})", permission, source);
    }

}
