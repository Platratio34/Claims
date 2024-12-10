package com.peter.claims.permission;

import java.util.HashMap;

import com.peter.claims.Claims;

import net.minecraft.util.Identifier;

public class DefaultPermissions {

    public static final HashMap<Identifier, Boolean> DEFAULT_PERMISSIONS = new HashMap<>();

    public static final Identifier PLACE_BREAK_PERM = Claims.id("place_break_block");

    public static final Identifier USE_BLOCK_PERM = Claims.id("use_block");
    public static final Identifier OPEN_INVENTORY_PERM = Claims.id("open_inventory");

    public static final Identifier USE_DOOR_PERM = Claims.id("use_door_block");
    public static final Identifier USE_TRAP_DOOR_PERM = Claims.id("use_trap_door_block");
    public static final Identifier USE_GATE_PERM = Claims.id("use_gate_block");
    
    public static final Identifier USE_BUTTON_PERM = Claims.id("use_button");
    public static final Identifier USE_LEVER_PERM = Claims.id("use_lever");

    public static final Identifier USE_WORKSTATION_PERM = Claims.id("use_workstation");
    public static final Identifier USE_BEACON_PERM = Claims.id("use_beacon");
    public static final Identifier USE_LECTERN_PERM = Claims.id("use_lectern");
    public static final Identifier USE_SIGN_PERM = Claims.id("use_sign");
    public static final Identifier USE_BED_PERM = Claims.id("use_bed");

    public static final HashMap<Identifier, Identifier> BLOCK_PERMS = new HashMap<>();

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

        DEFAULT_PERMISSIONS.put(PLACE_BREAK_PERM, false);

        DEFAULT_PERMISSIONS.put(USE_BLOCK_PERM, false);

        DEFAULT_PERMISSIONS.put(OPEN_INVENTORY_PERM, false);

        DEFAULT_PERMISSIONS.put(USE_DOOR_PERM, false);
        DEFAULT_PERMISSIONS.put(USE_TRAP_DOOR_PERM, false);
        DEFAULT_PERMISSIONS.put(USE_GATE_PERM, false);

        DEFAULT_PERMISSIONS.put(USE_BUTTON_PERM, false);
        DEFAULT_PERMISSIONS.put(USE_LEVER_PERM, false);
        
        DEFAULT_PERMISSIONS.put(USE_WORKSTATION_PERM, true);
        DEFAULT_PERMISSIONS.put(USE_BEACON_PERM, false);
        DEFAULT_PERMISSIONS.put(USE_LECTERN_PERM, false);
        DEFAULT_PERMISSIONS.put(USE_SIGN_PERM, false);
        DEFAULT_PERMISSIONS.put(USE_BED_PERM, true);
    }

}
