package com.peter.claims.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.peter.claims.claim.Claim;
import com.peter.claims.claim.ClaimStorage;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

@Mixin(Entity.class)
public abstract class PlayerMixin {

    private Claim claim;
    private BlockPos lPos;

    @Inject(method = "setPos(DDD)V", at = @At("RETURN"))
    private void onSetPos(double x, double y, double z, CallbackInfo info) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof ServerPlayerEntity player) {
            if(player.networkHandler == null || !player.networkHandler.isConnectionOpen())
                return;

            BlockPos newPos = player.getBlockPos();
            if (newPos.equals(lPos))
                return;
            lPos = newPos;

            Claim newClaim = ClaimStorage.getClaim(newPos);
            if (newClaim != claim) {
                claim = newClaim;
                if (claim != null) {
                    player.sendMessage(Text.of("Entered: " + claim.getName()).copy().formatted(Formatting.GREEN), true);
                }
            }
        }
    }

}
