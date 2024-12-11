package com.peter.claims.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.peter.claims.Claims;
import com.peter.claims.Cuboid;
import com.peter.claims.claim.Claim;
import com.peter.claims.claim.ClaimStorage;
import com.peter.claims.gui.ClaimMenuScreenHandler;
import com.peter.claims.permission.ClaimPermissions;
import com.peter.claims.permission.PermissionContainer;
import com.peter.claims.permission.PermissionState;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class ClaimCommands {

    public static final String CLAIM_COMMAND = "claim";

    private static final HashMap<UUID, BlockPos[]> positions = new HashMap<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
            RegistrationEnvironment environment) {
        dispatcher.register(
                literal(CLAIM_COMMAND).executes(ClaimCommands::gui)
                        .then(literal("help").executes(ClaimCommands::help)
                                .then(argument("command", StringArgumentType.string()).executes(ClaimCommands::helpCommand)))
                        .then(literal("pos1").executes(ClaimCommands::pos1))
                        .then(literal("pos2").executes(ClaimCommands::pos2))
                        .then(literal("create").executes(ClaimCommands::createClaim))
                        .then(literal("list").executes(ClaimCommands::list))
                        .then(literal("name")
                                .then(argument("claim", StringArgumentType.string())
                                        .then(argument("name", StringArgumentType.greedyString())
                                                .executes(ClaimCommands::setName))))
                        .then(literal("remove")
                                .then(argument("claim", StringArgumentType.string()).executes(ClaimCommands::remove)))
                        .then(literal("perm")
                                .then(argument("group", StringArgumentType.string())
                                        .then(argument("permission", StringArgumentType.string())
                                                .then(argument("state", StringArgumentType.string())
                                                        .executes(ClaimCommands::setPerm)))))
                        .then(literal("group")
                                .then(argument("group", StringArgumentType.string())
                                        .then(argument("player", EntityArgumentType.players())
                                                .executes(ClaimCommands::setGroup))))
                        .then(argument("action", StringArgumentType.string()).executes(ClaimCommands::claim)));
    }
    
    private static int help(CommandContext<ServerCommandSource> context) {
        String rt = "Claims Commands:";
        rt += "\n§a|§e help §r- Show this list";
        rt += "\n§a|§e pos1 §r- Select 1st position for claiming";
        rt += "\n§a|§e pos2 §r- Select 2nd position for claiming";
        rt += "\n§a|§e create §r- Create a new claim from selected position";
        rt += "\n§a|§e remove <§bclaim§e> §r- Remove a claim";
        rt += "\n§a|§e list §r- List all current claims";
        rt += "\n§a|§e perm <§bgroup§e> <§bpermission§e> <§bstate§e> §r- Set group permission";
        rt += "\n§a|§e group <§bgroup§e> <§bplayer§e> §r- Set player group";
        rt += "\n§a|§e name <§bclaim§e> <§bname§e> §r- Rename a claim";
        Text text = Text.of(rt);
        context.getSource().sendFeedback(() -> text, false);

        return 1;
    }
    
    private static int helpCommand(CommandContext<ServerCommandSource> context) {
        String command = StringArgumentType.getString(context, "command");
        String rt = "Claims Commands: §e";
        switch (command) {
            case "help":
                rt += "help [<§bcommand§e>]§f";
                rt += "\n§a|§f Show a list of all sub-command or help for specific command";
                rt += "\n§a|§b command §f - §o(Optional)§r Sub-command to get help for";
                break;
            case "pos1":
                rt += "pos1§f";
                rt += "\n§a|§f Select 1st position for claiming";
                break;
            case "pos2":
                rt += "pos2§f";
                rt += "\n§a|§f Select 2nd position for claiming";
                break;
            case "create":
                rt += "create§f";
                rt += "\n§a|§f Create a new claim from selected position";
                break;
            case "list":
                rt += "liste§f";
                rt += "\n§a|§f List all current claims";
                break;
            case "remove":
                rt += "remove <§bclaim§e>§f";
                rt += "\n§a|§f Remove a claim";
                rt += "\n§a|§b claim §f- Claim UUID §lOR§r§f `-` for current claim";
                break;
            case "perm":
                rt += "perm <§bgroup§e> <§bpermission§e> <§bstate§e>§f";
                rt += "\n§a|§f Set group permission";
                rt += "\n§a|§b group §f- Group to modify";
                rt += "\n§a|§b permission §f- Permission (ID) to change. (ex. use_block, claims:use_block)";
                rt += "\n§a|§b state §f- Permission state: §aALLOWED§f, §cPROHIBITED§f, or §7DEFAULT";
                break;
            case "group":
                rt += "group <§bgroup§e> <§bplayer§e>§f";
                rt += "\n§a|§f Set player group";
                rt += "\n§a|§b group §f- Group to modify";
                rt += "\n§a|§b player §f- Player(s) to add to the group";
                break;
            case "name":
                rt += "name <§bclaim§e> <§bname§e>§f";
                rt += "\n§a|§f Rename a claim";
                rt += "\n§a|§b claim §f- Claim UUID §lOR§r§f `-` for current claim";
                rt += "\n§a|§b name §f- Name for the claim";
                break;
        
            default:
                context.getSource().sendError(Text.literal("Unknown sub-command for help"));
                return -1;
        }
        Text text = Text.of(rt);
        context.getSource().sendFeedback(() -> text, false);

        return 1;
    }
    
    private static int gui(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendError(Text.literal("Base command must be executed by a player"));
            return -1;
        }
        ServerPlayerEntity player = context.getSource().getPlayer();
        Claim claim = ClaimStorage.getClaim(player.getBlockPos());
        if (claim == null) {
            context.getSource().sendError(Text.literal("Must be standing in claim to use command"));
            return -1;
        }
        
        if (!claim.getPermissions(context.getSource().getPlayer().getUuid()).hasPerm(ClaimPermissions.EDIT_CLAIM_PERM)) {
            context.getSource().sendError(Text.literal("You don't have permission to edit this claim"));
            return -1;
        }

        ClaimMenuScreenHandler.openClaimMenu(player, claim);

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

        // need to do something here to prevent overlapping claims
        Cuboid cuboid = new Cuboid(pos[0], pos[1]);
        if (!ClaimStorage.verifyArea(cuboid)) {
            context.getSource().sendError(Text.literal("Claim can not intersect existing claim"));
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
                claims += "\n";
            }
            claims += "§a|§f " + ClaimStorage.getClaim(claimId).getName() + " §7("+claimId.toString()+")§f";
        }

        String msg = "Claims:\n" + claims;

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

        if (context.getSource().isExecutedByPlayer()) {
            if (!claim.getPermissions(context.getSource().getPlayer().getUuid()).hasPerm(ClaimPermissions.EDIT_CLAIM_PERM)) {
                context.getSource().sendError(Text.literal("You don't have permission to edit this claim"));
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

        if (context.getSource().isExecutedByPlayer()) {
            if (!claim.getPermissions(context.getSource().getPlayer().getUuid()).hasPerm(ClaimPermissions.EDIT_CLAIM_PERM)) {
                context.getSource().sendError(Text.literal("You don't have permission to edit this claim"));
                return -1;
            }
        }

        ClaimStorage.remove(claim);

        context.getSource().sendFeedback(() -> Text.literal("Claim removed"), false);

        return 1;
    }
    
    private static int setPerm(CommandContext<ServerCommandSource> context) {
        String group = StringArgumentType.getString(context, "group");
        String perm = StringArgumentType.getString(context, "permission");
        String state = StringArgumentType.getString(context, "state");

        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendError(Text.literal("Command must be executed by a player"));
            return -1;
        }
        ServerPlayerEntity player = context.getSource().getPlayer();

        Claim claim = ClaimStorage.getClaim(player.getBlockPos());
        if (claim == null) {
            context.getSource().sendError(Text.literal("Must be standing in claim to use this command"));
            return -1;
        }
        if (!claim.getPermissions(player.getUuid()).hasPerm(ClaimPermissions.EDIT_CLAIM_PERM)) {
            context.getSource().sendError(Text.literal("You don't have permission to edit this claim"));
            return -1;
        }

        PermissionContainer groupPerms = claim.getGroup(group);
        if (groupPerms == null) {
            context.getSource().sendError(Text.literal("Invalid group: " + group));
            return -1;
        }

        Identifier permId;
        if (!perm.contains(":")) {
            permId = Claims.id(perm);
        } else {
            permId = Identifier.tryParse(perm);
        }
        if (!ClaimPermissions.PERMISSIONS.containsKey(permId)) {
            context.getSource().sendError(Text.literal("Invalid permission: " + permId));
            return -1;
        }

        try {
            PermissionState permState = PermissionState.valueOf(state);
            groupPerms.setPerm(permId, permState);
            context.getSource().sendFeedback(() -> Text.literal("Permission " + permId + " updated to ").append(permState.getText()),
                    false);
        } catch (IllegalArgumentException e) {
            context.getSource().sendError(
                    Text.literal("Invalid state: " + state + "; Must be one of §aALLOWED§c, §4PROHIBITED§c, or §7DEFAULT§c"));
            return -1;
        }

        return 1;
    }
    
    private static int setGroup(CommandContext<ServerCommandSource> context) {
        String group = StringArgumentType.getString(context, "group");
        Collection<ServerPlayerEntity> players;
        try {
            players = EntityArgumentType.getPlayers(context, "player");
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return -1;
        }

        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendError(Text.literal("Command must be executed by a player"));
            return -1;
        }
        ServerPlayerEntity player = context.getSource().getPlayer();

        Claim claim = ClaimStorage.getClaim(player.getBlockPos());
        if (claim == null) {
            context.getSource().sendError(Text.literal("Must be standing in claim to use this command"));
            return -1;
        }
        if (!claim.getPermissions(player.getUuid()).hasPerm(ClaimPermissions.EDIT_CLAIM_PERM)) {
            context.getSource().sendError(Text.literal("You don't have permission to edit this claim"));
            return -1;
        }

        if (claim.getGroup(group) == null) {
            context.getSource().sendError(Text.literal("Invalid group: " + group));
            return -1;
        }

        String pNames = "";
        for (ServerPlayerEntity p2 : players) {
            claim.setGroup(p2.getUuid(), group);
            if (pNames.length() > 0)
                pNames += ", ";
            pNames += p2.getNameForScoreboard();
        }
        String pNames2 = pNames;
        
        context.getSource().sendFeedback(() -> Text.literal("Players "+pNames2+" added to group "+group), false);

        return 1;
    }
}

