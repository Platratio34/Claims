package com.peter.claims;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import com.peter.claims.claim.ClaimStorage;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;

public class PlayerCache {

    private static final HashMap<UUID, PlayerData> players = new HashMap<>();
    private static boolean dirty = false;

    public static void addPlayer(ServerPlayerEntity player) {
        PlayerData pData = new PlayerData(player.getUuid(), player.getName().getString());
        if (players.containsKey(pData.uuid))
            return;
        players.put(pData.uuid, pData);
        Claims.LOGGER.info("added player " + pData.username + " to cache");
        dirty = true;
    }

    public static void addPlayer(GameProfile profile) {
        PlayerData pData = new PlayerData(profile.getId(), profile.getName());
        if (players.containsKey(pData.uuid))
            return;
        players.put(pData.uuid, pData);
        Claims.LOGGER.info("added player " + pData.username + " to cache");
        dirty = true;
    }

    public static PlayerData getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public static PlayerData getPlayer(String username) {
        for (PlayerData player : players.values()) {
            if (player.username.equals(username)) {
                return player;
            }
        }
        return null;
    }

    public static void save(MinecraftServer server) {
        if (!dirty) {
            return;
        }
        Path path = DimensionType.getSaveDirectory(server.getOverworld().getRegistryKey(),
                server.getSavePath(ClaimStorage.CLAIM_SAVE_PATH));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.resolve("players.json").toFile()))) {
            JsonObject data = new JsonObject();

            for (Entry<UUID, PlayerData> entry : players.entrySet()) {
                data.add(entry.getKey().toString(), entry.getValue().serialize());
            }

            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);

            jsonWriter.setLenient(true);
            jsonWriter.setIndent("\t");
            Streams.write(data, jsonWriter);

            writer.write(stringWriter.toString());

            dirty = false;

            Claims.LOGGER.info("Saved player cache");
        } catch (IOException e) {
            Claims.LOGGER.error("Error player cache data: ", e);
        }
    }

    public static void load(MinecraftServer server) {
        Path path = DimensionType.getSaveDirectory(server.getOverworld().getRegistryKey(),
                server.getSavePath(ClaimStorage.CLAIM_SAVE_PATH));
        JsonObject json;

        players.clear();
        File f = path.resolve("players.json").toFile();
        if (!f.exists()) {
            Claims.LOGGER.info("No player cache to load");
            dirty = false;
            return;
        }
        try {
            json = JsonParser.parseReader(new FileReader(f)).getAsJsonObject();
        } catch (IllegalStateException | IOException e) {
            Claims.LOGGER.error("Error player cache data: ", e);
            return;
        }
        for (Entry<String, JsonElement> entry : json.entrySet()) {
            PlayerData player = PlayerData.fromJSON(entry.getValue().getAsJsonObject());
            players.put(player.uuid, player);
        }
        Claims.LOGGER.info("Loaded player cache");
        dirty = false;
    }

    public record PlayerData(UUID uuid, String username) {

        public JsonObject serialize() {
            JsonObject json = new JsonObject();
            json.addProperty("username", username);
            json.addProperty("uuid", uuid.toString());
            return json;
        }

        public static PlayerData fromJSON(JsonObject json) {
            UUID uuid = UUID.fromString(json.get("uuid").getAsString());
            String username = json.get("username").getAsString();
            return new PlayerData(uuid, username);
        }
    }
}
