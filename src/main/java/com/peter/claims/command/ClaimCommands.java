package com.peter.claims.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.peter.claims.Claims;
import com.peter.claims.claim.Claim;
import com.peter.claims.claim.ClaimStorage;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.*;

import java.util.HashMap;
import java.util.UUID;

public class ClaimCommands {

    public static final String CLAIM_COMMAND = "claim";

    private static final HashMap<UUID, BlockPos[]> positions = new HashMap<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
            RegistrationEnvironment environment) {
        dispatcher.register(
                literal(CLAIM_COMMAND).executes(ClaimCommands::base)
                    .then(literal("pos1").executes(ClaimCommands::pos1))
                    .then(literal("pos2").executes(ClaimCommands::pos2))
                    .then(literal("create").executes(ClaimCommands::createClaim))
                    .then(literal("list").executes(ClaimCommands::list))
                    .then(literal("name")
                            .then(argument("claim", StringArgumentType.string())
                                    .then(argument("name", StringArgumentType.greedyString()).executes(ClaimCommands::setName))))
                    .then(literal("remove")
                            .then(argument("claim", StringArgumentType.string()).executes(ClaimCommands::remove)))
                    .then(argument("action", StringArgumentType.string()).executes(ClaimCommands::claim))
        );
    }
    
    private static int base(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("Claim command"), false);
            return 1;
    }

    private static int claim(CommandContext<ServerCommandSource> context) {
        String action = StringArgumentType.getString(context, "action");
        switch (action) {
            case "":

                break;

            default:
                context.getSource().sendError(Text.literal("Unknown action \"" + action + "\""));
                return -1;
        }
        return 1;
    }
    
    private static int pos1(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendError(Text.literal("Position command must be executed by a player"));
            return -1;
        }
        ServerPlayerEntity player = context.getSource().getPlayer();
        HitResult hitResult = player.raycast(player.getBlockInteractionRange(), 0, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
            UUID uuid = player.getUuid();
            if (!positions.containsKey(uuid)) {
                positions.put(uuid, new BlockPos[2]);
            }
            positions.get(uuid)[0] = pos;
            context.getSource().sendFeedback(() -> Text.literal("Position 1 selected"), false);
        } else {
            context.getSource().sendError(Text.literal("You must be looking at a block"));
            return -1;
        }

        return 1;
    }
    
    private static int pos2(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendError(Text.literal("Position command must be executed by a player"));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        HitResult hitResult = player.raycast(player.getBlockInteractionRange(), 0, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
            UUID uuid = player.getUuid();
            if (!positions.containsKey(uuid)) {
                positions.put(uuid, new BlockPos[2]);
            }
            positions.get(uuid)[1] = pos;
            context.getSource().sendFeedback(() -> Text.literal("Position 2 selected"), false);
        } else {
            context.getSource().sendError(Text.literal("You must be looking at a block"));
            return -1;
        }

        return 1;
    }

    private static int createClaim(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendError(Text.literal("Command must be executed by a player"));
            return -1;
        }
        ServerPlayerEntity player = context.getSource().getPlayer();
        UUID uuid = player.getUuid();
        if (!positions.containsKey(uuid)) {
            context.getSource().sendError(Text.literal("Select 2 positions first"));
            return -1;
        }
        BlockPos[] pos = positions.get(uuid);
        if (pos[0] == null || pos[1] == null) {
            context.getSource().sendError(Text.literal("Select 2 positions first"));
            return -1;
        }

        try {
            Claim claim = new Claim(pos[0], pos[1], player);

            ClaimStorage.registerClaim(claim);
        } catch (Exception e) {
            Claims.LOGGER.error("Error in create claim command: ", e);
            context.getSource().sendError(Text.literal("Something went wrong creating claim"));
            return -1;
        }

        context.getSource().sendFeedback(() -> Text.literal("New Claim created!"), false);

        return 1;
    }
    
    private static int list(CommandContext<ServerCommandSource> context) {

        String claims = "";
        for (UUID claimId : ClaimStorage.getClaimUUIDs()) {
            if (claims.length() > 0) {
                claims += ", ";
            }
            claims += ClaimStorage.getClaim(claimId).getName() + " ("+claimId.toString()+")";
        }

        String msg = "Claims: " + claims;

        context.getSource().sendFeedback(() -> Text.literal(msg), false);

        return 1;
    }

    private static int setName(CommandContext<ServerCommandSource> context) {
        String claimId = StringArgumentType.getString(context, "claim");
        String newName = StringArgumentType.getString(context, "name");

        Claim claim;
        if (claimId.equals("-")) {
            if (!context.getSource().isExecutedByPlayer()) {
                context.getSource().sendError(Text.literal("Command must be executed by a player"));
                return -1;
            }
            ServerPlayerEntity player = context.getSource().getPlayer();

            claim = ClaimStorage.getClaim(player.getBlockPos());
            if (claim == null) {
                context.getSource().sendError(Text.literal("Must be standing in claim to use `-` selector"));
                return -1;
            }
        } else {
            UUID claimUUID;
            try {
                claimUUID = UUID.fromString(claimId);
            } catch (IllegalArgumentException e) {
                context.getSource().sendError(Text.literal("Claim Id must be a UUID"));
                return -1;
            }
            claim = ClaimStorage.getClaim(claimUUID);
            if (claim == null) {
                context.getSource().sendError(Text.literal("No claim with UUID `" + claimId + "` exists"));
                return -1;
            }
        }
        
        claim.setName(newName);

        context.getSource().sendFeedback(() -> Text.literal("Claim name set to \""+newName+"\""), false);

        return 1;
    }

    private static int remove(CommandContext<ServerCommandSource> context) {
        String claimId = StringArgumentType.getString(context, "claim");

        Claim claim;
        if (claimId.equals("-")) {
            if (!context.getSource().isExecutedByPlayer()) {
                context.getSource().sendError(Text.literal("Command must be executed by a player"));
                return -1;
            }
            ServerPlayerEntity player = context.getSource().getPlayer();

            claim = ClaimStorage.getClaim(player.getBlockPos());
            if (claim == null) {
                context.getSource().sendError(Text.literal("Must be standing in claim to use `-` selector"));
                return -1;
            }
        } else {
            UUID claimUUID;
            try {
                claimUUID = UUID.fromString(claimId);
            } catch (IllegalArgumentException e) {
                context.getSource().sendError(Text.literal("Claim Id must be a UUID"));
                return -1;
            }
            claim = ClaimStorage.getClaim(claimUUID);
            if (claim == null) {
                context.getSource().sendError(Text.literal("No claim with UUID `" + claimId + "` exists"));
                return -1;
            }
        }
        
        ClaimStorage.remove(claim);

        context.getSource().sendFeedback(() -> Text.literal("Claim removed"), false);

        return 1;
    }
}
