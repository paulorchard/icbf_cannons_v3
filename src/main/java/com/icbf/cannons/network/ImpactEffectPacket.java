package com.icbf.cannons.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent from server to a specific player to guarantee they see an explosion/impact effect
 * regardless of normal client-side distance culling.
 */
public class ImpactEffectPacket {
    private final double x, y, z;
    private final float power;

    public ImpactEffectPacket(double x, double y, double z, float power) {
        this.x = x; this.y = y; this.z = z; this.power = power;
    }

    public ImpactEffectPacket(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.power = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(power);
    }

    public static void handle(ImpactEffectPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // Debug: log packet reception on client
            com.icbf.cannons.IcbfCannons.LOGGER.info("ImpactEffectPacket received on client at ({}, {}, {}), power={}", msg.x, msg.y, msg.z, msg.power);
            // Client-side handler: play particles and sound at the specified location
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc == null || mc.level == null) return;
            double x = msg.x, y = msg.y, z = msg.z;
            float power = msg.power;

            // Explosion emitter particle
            mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER, x, y, z, 0.0, 0.0, 0.0);
            // Explosion particles for flair
            java.util.Random rnd = new java.util.Random();
            for (int i = 0; i < 12; i++) {
                double vx = (rnd.nextDouble() - 0.5) * 0.7;
                double vy = rnd.nextDouble() * 0.5;
                double vz = (rnd.nextDouble() - 0.5) * 0.7;
                mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.EXPLOSION, x, y, z, vx, vy, vz);
            }

            // Swirling large smoke similar to TNT: spawn multiple LARGE_SMOKE particles with upward bias and radial velocity
            int smokeCount = 40;
            for (int i = 0; i < smokeCount; i++) {
                double angle = (i / (double) smokeCount) * Math.PI * 2.0 + (rnd.nextDouble() - 0.5) * 0.3;
                double radius = 0.2 + rnd.nextDouble() * (0.6 + power * 0.15);
                double vx = Math.cos(angle) * radius * 0.2 + (rnd.nextDouble() - 0.5) * 0.02;
                double vz = Math.sin(angle) * radius * 0.2 + (rnd.nextDouble() - 0.5) * 0.02;
                double vy = 0.08 + rnd.nextDouble() * 0.18 + power * 0.02;
                mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE, x + Math.cos(angle) * radius, y + 0.1 + rnd.nextDouble() * 0.6, z + Math.sin(angle) * radius, vx, vy, vz);
            }

            // Play explosion sound locally
            mc.level.playLocalSound(x, y, z, net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.PLAYERS, 4.0F, 1.0F, false);

            // TESTING HELPERS: display a short client chat message so the shooter can confirm packet arrival
            try {
                if (mc.player != null) {
                    mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(String.format("[ICBF] Impact at %.1f %.1f %.1f (power=%.2f)", x, y, z, power)), false);
                }
            } catch (Exception ignored) {}
        });
        ctx.setPacketHandled(true);
    }
}
