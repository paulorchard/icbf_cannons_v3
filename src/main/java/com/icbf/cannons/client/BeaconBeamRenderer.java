package com.icbf.cannons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

/**
 * Renders a fake beacon beam at the targeting position
 * Only visible to the player performing targeting
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BeaconBeamRenderer {
    private static BlockPos beaconPos = null;
    private static long lastUpdateTime = 0L;
    private static final long BEACON_TIMEOUT = 100L; // ms

    private static final ResourceLocation BEAM_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/beacon_beam.png");

    public static void setBeaconPosition(@Nullable BlockPos pos) {
        beaconPos = pos;
        lastUpdateTime = System.currentTimeMillis();
    }

    public static void clearBeacon() {
        beaconPos = null;
    }

    public static void preloadTexture() {
        Minecraft mc = Minecraft.getInstance();
        try {
            mc.getTextureManager().register(BEAM_TEXTURE, new SimpleTexture(BEAM_TEXTURE));
        } catch (Exception ignored) {}
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        // Timeout stale beacons
        if (beaconPos != null && System.currentTimeMillis() - lastUpdateTime > BEACON_TIMEOUT) {
            beaconPos = null;
            return;
        }

        if (beaconPos == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        poseStack.pushPose();

        // Translate to beacon position - center on block and start slightly above
        double camX = mc.gameRenderer.getMainCamera().getPosition().x;
        double camY = mc.gameRenderer.getMainCamera().getPosition().y;
        double camZ = mc.gameRenderer.getMainCamera().getPosition().z;

        double renderX = beaconPos.getX() + 0.5 - camX;
        double renderY = beaconPos.getY() + 1.0 - camY; // start from top of block
        double renderZ = beaconPos.getZ() + 0.5 - camZ;

        poseStack.translate(renderX, renderY, renderZ);

        float partialTick = event.getPartialTick();
        renderBeaconBeam(poseStack, bufferSource, partialTick);

        poseStack.popPose();
    }

    private static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        float[] color = new float[]{1.0f, 0.0f, 0.0f, 1.0f}; // Red
        long gameTime = Minecraft.getInstance().level.getGameTime();
        int height = 256;
        int yOffset = -2; // start 2 blocks down to pass through target

        net.minecraft.client.renderer.blockentity.BeaconRenderer.renderBeaconBeam(
                poseStack,
                bufferSource,
                BEAM_TEXTURE,
                partialTick,
                1.0f,
                gameTime,
                yOffset,
                height,
                color,
                0.2f,
                1.0f
        );
    }
}
