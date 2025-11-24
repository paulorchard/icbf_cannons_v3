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
    private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");
    
    @Nullable
    private static BlockPos currentBeaconPos = null;
    
    public static void setBeaconPosition(@Nullable BlockPos pos) {
        currentBeaconPos = pos;
    }
    
    @Nullable
    public static BlockPos getBeaconPosition() {
        return currentBeaconPos;
    }
    
    public static void clearBeacon() {
        currentBeaconPos = null;
    }
    
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        
        if (currentBeaconPos == null) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        
        // Render the beacon beam
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        
        poseStack.pushPose();
        
        // Translate to beacon position relative to camera
        double camX = mc.gameRenderer.getMainCamera().getPosition().x;
        double camY = mc.gameRenderer.getMainCamera().getPosition().y;
        double camZ = mc.gameRenderer.getMainCamera().getPosition().z;
        
        poseStack.translate(
            currentBeaconPos.getX() - camX + 0.5,
            currentBeaconPos.getY() - camY,
            currentBeaconPos.getZ() - camZ + 0.5
        );
        
        // Render tall beam (256 blocks high to ensure visibility)
        long gameTime = mc.level.getGameTime();
        float time = (float) (gameTime % 40000L) + event.getPartialTick();

        // Render using our local implementation (keeps compatibility across mappings)
        renderBeaconBeam(poseStack, bufferSource, time, 256, new float[]{1.0f, 0.2f, 0.2f});
        
        poseStack.popPose();
    }
    
    /**
     * Renders a beacon beam similar to vanilla beacon rendering
     */
    private static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource bufferSource, float time, int height, float[] color) {
        int i = 15728880; // Full brightness
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, 0.0);
        
        float f = Mth.cos(time * 0.2F) * 0.05F + 0.05F;
        float f1 = color[0];
        float f2 = color[1];
        float f3 = color[2];
        
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.25F - 45.0F));
        
        float f4 = 0.0F;
        float f8 = 0.2F;
        float f9 = 0.0F;
        float f5 = f4 + f;
        float f6 = Math.min(1.0F, height * 0.01F);
        
        // Try the two-sided variant of the beacon RenderType first
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.beaconBeam(BEAM_LOCATION, true));
        
        renderBeamSegment(poseStack, vertexConsumer, f1, f2, f3, 0.2F, 0, height, 0.0F, f8, f8, 0.0F, f9, 0.2F, 0.0F, f5, 1.0F, i);
        
        poseStack.popPose();
        
        f4 = -1.0F + time * 0.1F;
        float f10 = (float)height * (0.5F / f8) + f4;
        
        renderBeamSegment(poseStack, vertexConsumer, f1, f2, f3, 0.1F, 0, height, -f, -f, f8, -f, -f, f8, 0.0F, f10, 1.0F, i);
        
        poseStack.popPose();
    }

    /**
     * Preload the beam texture to reduce first-frame hitch.
     */
    public static void preloadTexture() {
        Minecraft mc = Minecraft.getInstance();
        try {
            mc.getTextureManager().register(BEAM_LOCATION, new SimpleTexture(BEAM_LOCATION));
        } catch (Exception e) {
            // Non-fatal: if texture registration fails, ignore and fallback will still work
        }
    }
    
    private static void renderBeamSegment(PoseStack poseStack, VertexConsumer consumer, float red, float green, float blue, float alpha, int minY, int maxY, float x0, float z0, float x1, float z1, float x2, float z2, float u0, float u1, float v0, int packedLight) {
        PoseStack.Pose pose = poseStack.last();
        renderQuad(pose, consumer, red, green, blue, alpha, minY, maxY, x0, z0, x1, z1, u0, u1, v0, packedLight);
    }
    
    private static void renderQuad(PoseStack.Pose pose, VertexConsumer consumer, float red, float green, float blue, float alpha, int minY, int maxY, float x0, float z0, float x1, float z1, float u0, float u1, float v0, int packedLight) {
        // Draw quad (one winding)
        addVertex(pose, consumer, red, green, blue, alpha, maxY, x0, z0, u1, v0, packedLight);
        addVertex(pose, consumer, red, green, blue, alpha, minY, x0, z0, u1, v0, packedLight);
        addVertex(pose, consumer, red, green, blue, alpha, minY, x1, z1, u0, v0, packedLight);
        addVertex(pose, consumer, red, green, blue, alpha, maxY, x1, z1, u0, v0, packedLight);

        // Also draw the quad with reversed winding so the back face is visible (disable culling effect)
        addVertex(pose, consumer, red, green, blue, alpha, maxY, x1, z1, u0, v0, packedLight);
        addVertex(pose, consumer, red, green, blue, alpha, minY, x1, z1, u0, v0, packedLight);
        addVertex(pose, consumer, red, green, blue, alpha, minY, x0, z0, u1, v0, packedLight);
        addVertex(pose, consumer, red, green, blue, alpha, maxY, x0, z0, u1, v0, packedLight);
    }
    
    private static void addVertex(PoseStack.Pose pose, VertexConsumer consumer, float red, float green, float blue, float alpha, int y, float x, float z, float u, float v, int packedLight) {
        consumer.vertex(pose.pose(), x, (float)y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(0, 10)
                .uv2(packedLight)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
