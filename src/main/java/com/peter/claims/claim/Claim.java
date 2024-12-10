package com.peter.claims.claim;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.joml.Vector3i;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.peter.claims.Claims;
import com.peter.claims.permission.PermissionContainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class Claim {

    public static final String OWNER_GROUP = "Owner";

    protected Vector3i pos1;
    protected Vector3i pos2;

    protected UUID owner;

    protected ServerWorld world;

    protected final UUID claimId;

    protected boolean dirty;

    protected PermissionContainer permissions;
    protected HashMap<String, PermissionContainer> groupPermissions = new HashMap<>();
    protected HashMap<UUID,String> groups = new HashMap<>();

    protected String name;

    public Claim(BlockPos pos1, BlockPos pos2, UUID owner, ServerWorld world) {
        this(blockPosToVector3i(pos1), blockPosToVector3i(pos2), owner, world);
    }

    public Claim(BlockPos pos1, BlockPos pos2, ServerPlayerEntity owner) {
        this(blockPosToVector3i(pos1), blockPosToVector3i(pos2), owner.getUuid(), owner.getServerWorld());
    }
    
    public Claim(Vector3i pos1, Vector3i pos2, UUID owner, ServerWorld world) {
        this.pos1 = pos1;
        this.pos2 = pos2;

        this.owner = owner;
        this.world = world;

        sortPos();

        claimId = ClaimStorage.newClaimUUID();
        permissions = ClaimStorage.copyDefaultPermissions();
        groups.put(owner, OWNER_GROUP);
        groupPermissions.put(OWNER_GROUP, ClaimStorage.copyOwnerPermissions().setParent(permissions));

        if (!owner.equals(Claims.ADMIN_UUID)) {
            PlayerEntity player = world.getPlayerByUuid(owner);
            if (player != null)
                name = player.getNameForScoreboard() + "'s claim";
            else
                name = owner.toString() + "'s claim";
        }

        markDirty();
    }

    protected void sortPos() {
        int minX = pos1.x <= pos2.x ? pos1.x : pos2.x;
        int minY = pos1.y <= pos2.y ? pos1.y : pos2.y;
        int minZ = pos1.z <= pos2.z ? pos1.z : pos2.z;
        
        int maxX = pos1.x > pos2.x ? pos1.x : pos2.x;
        int maxY = pos1.y > pos2.y ? pos1.y : pos2.y;
        int maxZ = pos1.z > pos2.z ? pos1.z : pos2.z;

        if (minY < world.getBottomY()) {
            minY = world.getBottomY();
            if(maxY < minY)
                maxY = minY;
        }
        if (maxY > world.getTopYInclusive()) {
            maxY = world.getTopYInclusive();
            if(minY > maxY)
                minY = maxY;
        }

        pos1 = new Vector3i(minX, minY, minZ);
        pos2 = new Vector3i(maxX, maxY, maxZ);
    }

    public boolean inClaim(Vector3i pos) {
        return between(pos1.x, pos2.x, pos.x) &&
                between(pos1.y, pos2.y, pos.y) &&
                between(pos1.z, pos2.z, pos.z);
    }

    public boolean inClaim(BlockPos pos) {
        return between(pos1.x, pos2.x, pos.getX()) &&
                between(pos1.y, pos2.y, pos.getY()) &&
                between(pos1.z, pos2.z, pos.getZ());
    }

    private boolean between(int a, int b, int c) {
        if (a < b) {
            return a <= c && c <= b;
        }
        return b <= c && c <= a;
    }

    public void markDirty() {
        dirty = true;
        ClaimStorage.markDirty();
    }

    public boolean isDirty() {
        return dirty;
    }

    protected void markClean() {
        dirty = false;
    }

    public PermissionContainer getPermissions(UUID player) {
        if (groups.containsKey(player)) {
            String group = groups.get(player);
            if (groupPermissions.containsKey(group))
                return groupPermissions.get(group);
            else
                Claims.LOGGER.warn("Claim {} had group \"{}\", but no permissions were defined", claimId, group);
        }
        return permissions;
    }

    public void setName(String name) {
        this.name = name;
        markDirty();
    }

    public String getName() {
        return name;
    }

    protected static final String JSON_CLAIM_ID = "uuid";
    protected static final String JSON_OWNER = "owner";
    protected static final String JSON_POS = "pos";
    protected static final String JSON_NAME = "name";

    protected static final String JSON_DEF_PERM = "defaultPerms";
    protected static final String JSON_GROUP_PERM = "groupsPerms";
    protected static final String JSON_GROUPS = "groups";

    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.addProperty(JSON_CLAIM_ID, claimId.toString());
        json.addProperty(JSON_OWNER, owner.toString());
        json.addProperty(JSON_NAME, name);

        json.add(JSON_POS, serializePos());

        json.add(JSON_DEF_PERM, permissions.serialize());

        JsonObject groupPerms = new JsonObject();
        for (Entry<String, PermissionContainer> entry : groupPermissions.entrySet()) {
            groupPerms.add(entry.getKey(), entry.getValue().serialize());
        }
        json.add(JSON_GROUP_PERM, groupPerms);

        JsonObject jsonGroups = new JsonObject();
        for (Entry<UUID, String> entry : groups.entrySet()) {
            jsonGroups.addProperty(entry.getKey().toString(), entry.getValue());
        }
        json.add(JSON_GROUPS, jsonGroups);

        return json;
    }

    private JsonArray serializePos() {
        JsonArray posArr = new JsonArray();

        JsonArray pos1Arr = new JsonArray();
        pos1Arr.add(pos1.x);
        pos1Arr.add(pos1.y);
        pos1Arr.add(pos1.z);
        posArr.add(pos1Arr);

        JsonArray pos2Arr = new JsonArray();
        pos2Arr.add(pos2.x);
        pos2Arr.add(pos2.y);
        pos2Arr.add(pos2.z);
        posArr.add(pos2Arr);

        return posArr;
    }
    
    public Claim(JsonObject json) {
        claimId = UUID.fromString(json.get(JSON_CLAIM_ID).getAsString());
        name = json.get(JSON_NAME).getAsString();
        owner = UUID.fromString(json.get(JSON_OWNER).getAsString());

        JsonArray posArr = json.get(JSON_POS).getAsJsonArray();
        JsonArray pos1Json = posArr.get(0).getAsJsonArray();
        pos1 = new Vector3i(pos1Json.get(0).getAsInt(), pos1Json.get(1).getAsInt(), pos1Json.get(2).getAsInt());
        JsonArray pos2Json = posArr.get(1).getAsJsonArray();
        pos2 = new Vector3i(pos2Json.get(0).getAsInt(), pos2Json.get(1).getAsInt(), pos2Json.get(2).getAsInt());

        permissions = new PermissionContainer(json.get(JSON_DEF_PERM).getAsJsonObject());
        if (permissions.updateMissing(ClaimStorage.defaultPermissions)) {
            markDirty();
        }

        for (Entry<String, JsonElement> entry : json.get(JSON_GROUP_PERM).getAsJsonObject().entrySet()) {
            PermissionContainer perms = new PermissionContainer(entry.getValue().getAsJsonObject())
                    .setParent(permissions);
            if (entry.getKey().equals(OWNER_GROUP)) {
                if(perms.updateMissing(ClaimStorage.ownerPermissions))
                    markDirty();
            } else {
                if(perms.updateMissing(ClaimStorage.defaultPermissions))
                    markDirty();
            }
            groupPermissions.put(entry.getKey(), perms);
        }

        for (Entry<String, JsonElement> entry : json.get(JSON_GROUPS).getAsJsonObject().entrySet()) {
            groups.put(UUID.fromString(entry.getKey()), entry.getValue().getAsString());
        }

    }

    public static Vector3i blockPosToVector3i(BlockPos pos) {
        return new Vector3i(pos.getX(), pos.getY(), pos.getZ());
    }
}