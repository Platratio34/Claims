package com.peter.claims;

import com.peter.claims.claim.ClaimStorage;
import com.peter.claims.event.BlockEvents;
import com.peter.claims.event.ItemEvents;
import com.peter.claims.event.PlayerEvents;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class ClaimsEvents {

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register(BlockEvents::blockBreakEvent);
        UseBlockCallback.EVENT.register(BlockEvents::useBlockEvent);
        UseItemCallback.EVENT.register(ItemEvents::useItemEvent);

        ServerLifecycleEvents.BEFORE_SAVE.register((server, flush, force) -> {
            ClaimStorage.save(server);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(ClaimStorage::load);

        AttackEntityCallback.EVENT.register(PlayerEvents::onAttackEvent);
    }
}
