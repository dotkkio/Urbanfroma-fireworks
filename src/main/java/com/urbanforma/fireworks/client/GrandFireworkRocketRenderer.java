package com.urbanforma.fireworks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.urbanforma.fireworks.entity.GrandFireworkRocketEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

/** Renders the custom entity through the item's vanilla firework-rocket parent model. */
public final class GrandFireworkRocketRenderer extends EntityRenderer<GrandFireworkRocketEntity> {
    private final ItemRenderer itemRenderer;

    public GrandFireworkRocketRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            GrandFireworkRocketEntity rocket,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        this.itemRenderer.renderStatic(
                rocket.getItem(),
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                rocket.level(),
                rocket.getId());
        poseStack.popPose();
        super.render(rocket, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GrandFireworkRocketEntity rocket) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
