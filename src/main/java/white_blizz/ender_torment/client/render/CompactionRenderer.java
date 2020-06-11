package white_blizz.ender_torment.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;
import white_blizz.ender_torment.common.tile_entity.CompactionTE;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.Random;

@ParametersAreNonnullByDefault
public class CompactionRenderer extends TileEntityRenderer<CompactionTE> {
	public CompactionRenderer(TileEntityRendererDispatcher rendererDispatcherIn) { super(rendererDispatcherIn); }

	@Override
	public void render(CompactionTE te,
					   float partialTicks,
					   MatrixStack stack,
					   IRenderTypeBuffer buffer,
					   int combinedLight, int combinedOverlay) {
		stack.push();

		AxisAlignedBB shape = te.getShape(partialTicks);
		stack.translate(shape.minX, shape.minY, shape.minZ);
		stack.scale((float)shape.getXSize(), (float)shape.getYSize(), (float)shape.getZSize());
		te.getState().ifPresent(state -> {
			Minecraft mc = Minecraft.getInstance();
			Optional<TileEntity> opt = te.getTE();
			BlockRenderType renderType = state.getRenderType();
			if (renderType == BlockRenderType.MODEL) {
				BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
				BlockModelRenderer renderer = dispatcher.getBlockModelRenderer();
				renderer.renderModel(
						stack.getLast(),
						buffer.getBuffer(RenderTypeLookup.getRenderType(state)),
						state, dispatcher.getModelForState(state),
						1F, 1F, 1F,
						combinedLight, combinedOverlay,
						EmptyModelData.INSTANCE
				);
			}
			if (opt.isPresent() && renderType != BlockRenderType.INVISIBLE) {
				TileEntityRendererDispatcher dispatcher = TileEntityRendererDispatcher.instance;
				TileEntityRenderer<TileEntity> renderer = dispatcher.getRenderer(opt.get());
				if (renderer != null)
					renderer.render(opt.get(), partialTicks, stack, buffer, combinedLight, combinedOverlay);
			}
		});
		stack.pop();
	}
}
