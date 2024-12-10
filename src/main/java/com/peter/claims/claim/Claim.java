package com.peter.claims.claim;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.joml.Vector3i;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.peter.claims.Claims;
import com.peter.claims.Cuboid.CuboidLike;
import com.peter.claims.permission.PermissionContainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class Claim implements CuboidLike {

    public static final String OWNER_GROUP = "Owner";
    public static final String DEFAULT_GROUP = "default";

    /** Minimum corner of the claim */
    protected Vector3i pos1;
    /** Maximum corner of the claim */
    protected Vector3i pos2;

    protected UUID owner;

    protected ServerWorld world;

    protected final UUID claimId;

    protected boolean dirty;

    /** Default permission for the claim */
    protected PermissionContainer permissions;
    /** Per-group permission for the claim */
    protected HashMap<String, PermissionContainer> groupPermissions = new HashMap<>();
    /** Map of player to permission group */
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
            if (maxY < minY)
                maxY = minY;
        }
        if (maxY > world.getTopYInclusive()) {
            maxY = world.getTopYInclusive();
            if (minY > maxY)
                minY = maxY;
        }

        pos1 = new Vector3i(minX, minY, minZ);
        pos2 = new Vector3i(maxX, maxY, maxZ);
    }

    /**
     * Check if the given position is within this claim
     * @param pos Position to check
     * @return
     */
    public boolean inClaim(Vector3i pos) {
        return between(pos1.x, pos2.x, pos.x) &&
                between(pos1.y, pos2.y, pos.y) &&
                between(pos1.z, pos2.z, pos.z);
    }
    /**
     * Check if the given position is within this claim
     * @param pos Position to check
     * @return
     */
    public boolean inClaim(BlockPos pos) {
        return between(pos1.x, pos2.x, pos.getX()) &&
                between(pos1.y, pos2.y, pos.getY()) &&
                between(pos1.z, pos2.z, pos.getZ());
    }
    /**
     * Check if the given position is within this claim
     * @param x X Position to check
     * @param y Y Position to check
     * @param z Z Position to check
     * @return
     */
    public boolean inClaim(int x, int y, int z) {
        return between(pos1.x, pos2.x, x) &&
                between(pos1.y, pos2.y, y) &&
                between(pos1.z, pos2.z, z);
    }

    public boolean intersectsClaim(Vector3i minPoint, Vector3i maxPoint) {

        // Check if we totally don't overlap
        if (pos2.x < minPoint.x || pos1.x > maxPoint.x)
            return false;
        if (pos2.y < minPoint.y || pos1.y > maxPoint.y)
            return false;
        if (pos2.z < minPoint.z || pos1.z > maxPoint.z)
            return false;
        
        // If the corners overlap
        if (inClaim(minPoint.x, minPoint.y, minPoint.z) || inClaim(minPoint.x, minPoint.y, maxPoint.z))
            return true;
        if (inClaim(maxPoint.x, minPoint.y, minPoint.z) || inClaim(maxPoint.x, minPoint.y, maxPoint.z))
            return true;
        if (inClaim(maxPoint.x, maxPoint.y, minPoint.z) || inClaim(maxPoint.x, maxPoint.y, maxPoint.z))
            return true;
        if (inClaim(minPoint.x, maxPoint.y, minPoint.z) || inClaim(minPoint.x, maxPoint.y, maxPoint.z))
            return true;
        
        
        
        return false;
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

    /**
     * Get the permissions for the given player
     * @param player Player to check for
     * @return Permission set for the player
     */
    public PermissionContainer getPermissions(PlayerEntity player) {
        return getPermissions(player.getUuid());
    }

    /**
     * Get the permissions for the given player
     * @param player Player to check for
     * @return Permission set for the player
     */
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

    /**
     * Get the permission for the specified group
     * @param group Name of the group
     * @return Permission set of the selected group
     */
    public PermissionContainer getGroup(String group) {
        if (group.equals(DEFAULT_GROUP))
            return permissions;
        if (!groupPermissions.containsKey(group))
            return null;
        return groupPermissions.get(group);
    }

    /**
     * Set the group of the specified player
     * @param player Player to update
     * @param group Group to add the player to
     */
    public void setGroup(UUID player, String group) {
        if (group.equals(DEFAULT_GROUP)) {
            groups.remove(player);
        } else {
            groups.put(player, group);
        }
    }

    @Override
    public boolean inside(Vector3i pos) {
        return inClaim(pos);
    }

    @Override
    public Vector3i getMin() {
        return pos1;
    }

    @Override
    public Vector3i getMax() {
        return pos2;
    }
}
