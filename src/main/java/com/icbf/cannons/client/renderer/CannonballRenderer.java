package com.icbf.cannons.client.renderer;

import com.icbf.cannons.IcbfCannons;
import com.icbf.cannons.entity.CannonballEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Custom renderer for cannonballs - renders as a snowball
 */
public class CannonballRenderer extends EntityRenderer<CannonballEntity> {
    private final ItemRenderer itemRenderer;

    public CannonballRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(CannonballEntity entity, float entityYaw, float partialTicks, 
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(1.5f, 1.5f, 1.5f); // Make it bigger than normal snowball
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        
        this.itemRenderer.renderStatic(
            new ItemStack(Items.SNOWBALL),
            ItemDisplayContext.GROUND,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            poseStack,
            buffer,
            entity.level(),
            entity.getId()
        );
        
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CannonballEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/snowball.png");
    }
}
