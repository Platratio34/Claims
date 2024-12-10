package com.peter.claims.permission;

import java.util.HashMap;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.peter.claims.Claims;

import net.minecraft.util.Identifier;

public class PermissionContainer {

    @Nullable
    protected PermissionContainer parent;

    protected HashMap<Identifier, PermissionState> permissions;

    protected boolean defaultAllowed = false;

    public PermissionContainer() {
        permissions = new HashMap<>();
    }

    public PermissionContainer(PermissionContainer parent) {
        permissions = new HashMap<>();
        this.parent = parent;
    }
    
    public PermissionContainer(PermissionContainer parent, HashMap<Identifier, PermissionState> permissions) {
        this.permissions = permissions;
        this.parent = parent;
    }

    public PermissionContainer copy() {
        HashMap<Identifier, PermissionState> perm2 = new HashMap<>();
        for (Entry<Identifier, PermissionState> entry : permissions.entrySet()) {
            perm2.put(entry.getKey(), entry.getValue());
        }
        return new PermissionContainer(parent, perm2);
    }

    
    public boolean hasPerm(ClaimPermission perm) {
        return hasPerm(perm.id);
    }

    public boolean hasPerm(Identifier id) {
        if (permissions.containsKey(id)) {
            PermissionState state = permissions.get(id);
            if (state == PermissionState.DEFAULT && parent != null) {
                return parent.hasPerm(id);
            }
            return state == PermissionState.ALLOWED;
        }
        return defaultAllowed;
    }

    public PermissionContainer setPerm(Identifier perm, PermissionState state) {
        permissions.put(perm, state);
        return this;
    }
    public PermissionContainer setPerm(ClaimPermission perm, PermissionState state) {
        permissions.put(perm.id, state);
        return this;
    }

    public PermissionContainer setParent(PermissionContainer parent) {
        this.parent = parent;
        return this;
    }

    public PermissionContainer defaultAllowed() {
        defaultAllowed = true;
        return this;
    }

    public JsonObject serialize() {
        JsonObject obj = new JsonObject();

        for (Entry<Identifier, PermissionState> entry : permissions.entrySet()) {
            obj.addProperty(entry.getKey().toString(), entry.getValue().toString());
        }
        
        return obj;
    }

    public PermissionContainer(JsonObject json) {
        this();

        for (Entry<String, JsonElement> entry : json.entrySet()) {
            Identifier perm = Identifier.tryParse(entry.getKey());
            permissions.put(perm, PermissionState.valueOf(entry.getValue().getAsString()));
        }
    }

    public String debugString() {
        String str = "";
        for (Entry<Identifier, PermissionState> entry : permissions.entrySet()) {
            if (str.length() > 0)
                str += ", ";
            str += entry.getKey().toString() + ": " + entry.getValue().toString();
        }
        if (defaultAllowed)
            str = "DEF_ALLOWED " + str;
        return str;
    }

    public boolean updateMissing(PermissionContainer defaultPermissions) {
        boolean updated = false;
        for (Identifier permId : defaultPermissions.permissions.keySet()) {
            if (!permissions.containsKey(permId)) {
                PermissionState newState = defaultPermissions.permissions.get(permId);
                Claims.LOGGER.info("Updating value for {} to {}", permId, newState);
                permissions.put(permId, newState);
                updated = true;
            }
        }
        return updated;
    }

    public PermissionState get(ClaimPermission permission) {
        if (permissions.containsKey(permission.id)) {
            return permissions.get(permission.id);
        }
        return PermissionState.DEFAULT;
    }

}
