package white_blizz.ender_torment.client.render.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ETModel implements Consumer<ETModelRenderer> {
   protected final Function<ResourceLocation, RenderType> renderType;
   public int textureSize = 64;

   public ETModel(Function<ResourceLocation, RenderType> renderTypeIn) {
      this.renderType = renderTypeIn;
   }

   public void accept(ETModelRenderer renderer) {}

   public final RenderType getRenderType(ResourceLocation locationIn) {
      return this.renderType.apply(locationIn);
   }

   public abstract void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha);
}