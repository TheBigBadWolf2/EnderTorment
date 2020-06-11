package white_blizz.ender_torment.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;
import white_blizz.ender_torment.utils.Ref;

import java.util.OptionalDouble;

public class ETRenderType extends RenderType {
	public ETRenderType(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	public static final RenderType CONDUIT = makeType(
			Ref.MOD.str.loc("conduit"),
			DefaultVertexFormats.BLOCK,
			GL11.GL_QUADS,
			256,
			RenderType.State.getBuilder()
//					.layer(RenderState.POLYGON_OFFSET_LAYERING)
//					.transparency(NO_TRANSPARENCY)
					.texture(new TextureState(Ref.MOD.rl.loc("textures/block", "conduit", "png"), false, false))
//					.depthTest(DEPTH_ALWAYS)
//					.cull(CULL_DISABLED)
//					.lightmap(LIGHTMAP_DISABLED)
//					.writeMask(RenderState.COLOR_DEPTH_WRITE)
					.build(false)
	);

	private static final LineState THICK_LINES = new LineState(OptionalDouble.of(3.0D));

	public static final RenderType OVERLAY_LINES = makeType(Ref.MOD.str.loc("overlay_lines"),
			DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
			RenderType.State.getBuilder().line(THICK_LINES)
					.layer(PROJECTION_LAYERING)
					.transparency(TRANSLUCENT_TRANSPARENCY)
					.texture(NO_TEXTURE)
					.depthTest(DEPTH_ALWAYS)
					.cull(CULL_DISABLED)
					.lightmap(LIGHTMAP_DISABLED)
					.writeMask(COLOR_DEPTH_WRITE)
					.build(false));
}
