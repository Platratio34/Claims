package com.peter.claims.claim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.joml.Vector3i;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.peter.claims.Claims;
import com.peter.claims.Cuboid;

import static com.peter.claims.permission.ClaimPermissions.*;

import com.peter.claims.permission.ClaimPermission;
import com.peter.claims.permission.PermissionContainer;
import com.peter.claims.permission.PermissionState;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public class ClaimStorage {

    private static final HashMap<UUID, Claim> claims = new HashMap<>();

    public static boolean dirty = false;

    public static PermissionContainer defaultPermissions = new PermissionContainer();
    public static PermissionContainer ownerPermissions = new PermissionContainer();

    public static PermissionContainer globalPermissions = new PermissionContainer().defaultAllowed();

    public static final WorldSavePath CLAIM_SAVE_PATH = new WorldSavePath("claims");

    static {
        for (Entry<Identifier, ClaimPermission> entry : PERMISSIONS.entrySet()) {
            defaultPermissions.setPerm(entry.getKey(), entry.getValue().defaultState);
            ownerPermissions.setPerm(entry.getKey(), PermissionState.ALLOWED);
        }
    }

    public static UUID newClaimUUID() {
        UUID uuid = UUID.randomUUID();
        while (claims.containsKey(uuid)) {
            uuid = UUID.randomUUID();
        }
        return uuid;
    }

    public static void registerClaim(Claim claim) {
        claims.put(claim.claimId, claim);
        markDirty();
    }

    public static void markDirty() {
        dirty = true;
    }

    public static boolean isDirty() {
        return dirty;
    }

    protected static void markClean() {
        dirty = false;
    }

    public static PermissionContainer copyDefaultPermissions() {
        return defaultPermissions.copy().setParent(globalPermissions);
    }

    public static PermissionContainer copyOwnerPermissions() {
        return ownerPermissions.copy();
    }

    public static Claim getClaim(BlockPos pos) {
        return getClaim(new Vector3i(pos.getX(), pos.getY(), pos.getZ()));
    }

    public static Claim getClaim(Vector3i pos) {
        for (Claim claim : claims.values()) {
            if (claim.inClaim(pos))
                return claim;
        }
        return null;
    }

    public static Claim getClaim(UUID uuid) {
        if (claims.containsKey(uuid))
            return claims.get(uuid);
        return null;
    }

    public static Set<UUID> getClaimUUIDs() {
        return claims.keySet();
    }

    public static void remove(UUID uuid) {
        claims.remove(uuid);
        markDirty();
    }

    public static void remove(Claim claim) {
        remove(claim.claimId);
    }

    public static PermissionContainer getPerms(BlockPos pos, PlayerEntity player) {
        PermissionContainer perms = globalPermissions;

        Claim claim = getClaim(pos);
        if (claim != null) {
            perms = claim.getPermissions(player.getUuid());
        }

        return perms;
    }

    protected static JsonObject serialize() {
        JsonObject claimsJson = new JsonObject();
        for (Claim claim : claims.values()) {
            claimsJson.add(claim.claimId.toString(), claim.serialize());
        }
        return claimsJson;
    }

    public static void save(MinecraftServer server) {
        if (!isDirty()) {
            boolean needSaving = false;
            for (Claim claim : claims.values()) {
                if (claim.isDirty()) {
                    needSaving = true;
                    break;
                }
            }
            if (!needSaving)
                return;
        }

        Path path = DimensionType.getSaveDirectory(server.getOverworld().getRegistryKey(),
                server.getSavePath(CLAIM_SAVE_PATH));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.resolve("claims.json").toFile()))) {
            JsonObject data = serialize();

            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);

            jsonWriter.setLenient(true);
            jsonWriter.setIndent("\t");
            Streams.write(data, jsonWriter);

            writer.write(stringWriter.toString());

            markClean();
            for (Claim claim : claims.values()) {
                claim.markClean();
            }

            Claims.LOGGER.info("Saved claim information");
        } catch (IOException e) {
            Claims.LOGGER.error("Error saving claim data: ", e);
        }
    }

    public static void load(MinecraftServer server) {
        Path path = DimensionType.getSaveDirectory(server.getOverworld().getRegistryKey(),
                server.getSavePath(CLAIM_SAVE_PATH));
        JsonObject json;
        claims.clear();
        File f = path.resolve("claims.json").toFile();
        if (!f.exists()) {
            Claims.LOGGER.info("No claim information to load");
            markClean();
            return;
        }
        try {
            json = JsonParser.parseReader(new FileReader(f)).getAsJsonObject();
        } catch (IllegalStateException | IOException e) {
            Claims.LOGGER.error("Error loading claim data: ", e);
            return;
        }
        for (Entry<String, JsonElement> entry : json.entrySet()) {
            claims.put(UUID.fromString(entry.getKey()), new Claim(entry.getValue().getAsJsonObject()));
        }
        Claims.LOGGER.info("Loaded claim information");
        Claims.LOGGER.info("{} claim(s) loaded", claims.size());
        markClean();
    }

    /**
     * Verify if a given pair of positions is OUTSIDE of all claims
     * 
     * @param blockPos
     * @param blockPos2
     * @return If the cuboid is outside of all claims
     */
    public static boolean verifyArea(Cuboid cuboid) {
        

        for (Claim claim : claims.values()) {
            if (cuboid.overlaps(claim))
                return false;
            
            
        }
        return true;
    }
}
