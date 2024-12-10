package com.peter.claims;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.peter.claims.command.ClaimCommands;

public class Claims implements ModInitializer {
	public static final String MOD_ID = "claims";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final UUID ADMIN_UUID = new UUID(0, 0);

	@Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world!");

        CommandRegistrationCallback.EVENT.register(ClaimCommands::register);

        ClaimsEvents.register();
    }
    
    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }
}