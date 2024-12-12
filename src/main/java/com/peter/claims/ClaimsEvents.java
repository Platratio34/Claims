package com.peter.claims;

import com.peter.claims.claim.ClaimStorage;
import com.peter.claims.command.ClaimCommands;
import com.peter.claims.event.BlockEvents;
import com.peter.claims.event.ItemEvents;
import com.peter.claims.event.PlayerEvents;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class ClaimsEvents {

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register(BlockEvents::blockBreakEvent);
        UseBlockCallback.EVENT.register(BlockEvents::useBlockEvent);
        UseItemCallback.EVENT.register(ItemEvents::useItemEvent);

        ServerLifecycleEvents.BEFORE_SAVE.register((server, flush, force) -> {
            ClaimStorage.save(server);
            PlayerCache.save(server);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ClaimStorage.load(server);
            PlayerCache.load(server);
        });

        AttackEntityCallback.EVENT.register(PlayerEvents::onAttackEvent);
        UseEntityCallback.EVENT.register(PlayerEvents::onEntityUseEvent);

        ServerPlayConnectionEvents.JOIN.register((networkHandler, packetSender, server) -> {
            PlayerCache.addPlayer(networkHandler.player);
        });

        ServerTickEvents.START_WORLD_TICK.register((serverWorld) -> {
            ClaimCommands.tickDisplays(serverWorld);
        });
    }
}
