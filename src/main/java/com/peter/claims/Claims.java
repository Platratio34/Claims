package com.peter.claims;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.peter.claims.command.ClaimCommands;

public class Claims implements ModInitializer {
	public static final String MOD_ID = "claims";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final UUID ADMIN_UUID = new UUID(0, 0);

	@Override
    public void onInitialize() {
        LOGGER.info("Loading in Claims by Platratio34");

        CommandRegistrationCallback.EVENT.register(ClaimCommands::register);

        ClaimsEvents.register();
    }
    
    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }

    public static void sendFailMessage(ServerPlayerEntity player) {
        player.sendMessage(Text.of("You can't do that here").copy().formatted(Formatting.RED), true);
    }
}