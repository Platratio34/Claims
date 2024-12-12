package com.peter.claims.display;

import org.joml.Vector3i;

import com.peter.claims.claim.Claim;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ClaimDisplay {

    public static final SimpleParticleType CORNER_PARTICLE = ParticleTypes.WAX_ON;
    public static final SimpleParticleType EDGE_PARTICLE = ParticleTypes.WAX_OFF;
    public static final SimpleParticleType FACE_PARTICLE = ParticleTypes.SCULK_CHARGE_POP;

    public final Claim claim;
    public final ServerPlayerEntity player;
    public final ServerWorld world;

    protected int t = 0;

    public ClaimDisplay(Claim claim, ServerWorld world, ServerPlayerEntity player) {
        this.claim = claim;
        this.world = world;
        this.player = player;
    }

    public void displayTick(ServerWorld world) {
        if (world != this.world)
            return;
        t++;

        Vector3i min = claim.getMin();
        Vector3i max = claim.getMax();

        if (t % 8 == 0) {

            BlockPos playerPos = player.getBlockPos();
            Vector3i mid = min.add(max, new Vector3i()).div(2); // Get the center point of the claim
            int radius = (int)(max.sub(min, new Vector3i()).length() / 2); // maximum "radius" of the claim
            if (mid.distance(playerPos.getX(), playerPos.getY(), playerPos.getZ()) > radius + 32) { // If we are far enough from it, don't even bother trying to show it
                return;
            }

            int s = t / 8;

            for (int x = min.x; x <= max.x; x++) {
                for (int y = min.y; y <= max.y; y++) {
                    for (int z = min.z; z <= max.z; z++) {
                        if ((x == min.x || x == max.x) && (y == min.y || y == max.y) && (z == min.z || z == max.z)) {
                            drawParticle(CORNER_PARTICLE, x, y, z, 1);
                        } else if ((x == min.x || x == max.x) && (y == min.y || y == max.y)) {
                            drawParticle(EDGE_PARTICLE, x, y, z, 1);
                        } else if ((y == min.y || y == max.y) && (z == min.z || z == max.z)) {
                            drawParticle(EDGE_PARTICLE, x, y, z, 1);
                        } else if ((x == min.x || x == max.x) && (z == min.z || z == max.z)) {
                            drawParticle(EDGE_PARTICLE, x, y, z, 1);
                        } else if (x == min.x || x == max.x) {
                            int i = y + z + s;
                            if (i % 8 == 0) {
                                drawParticle(FACE_PARTICLE, x, y, z, 1);
                            }
                        } else if (y == min.y || y == max.y) {
                            int i = x + z + s;
                            if (i % 8 == 0) {
                                drawParticle(FACE_PARTICLE, x, y, z, 1);
                            }
                        } else if (z == min.z || z == max.z) {
                            int i = x + y + s;
                            if (i % 8 == 0) {
                                drawParticle(FACE_PARTICLE, x, y, z, 1);
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected void drawParticle(SimpleParticleType type, int x, int y, int z, int count) {
        world.spawnParticles(player, type, false, x + 0.5d, y + 0.5d, z + 0.5d, count, 0, 0, 0, 0);
    }
}
