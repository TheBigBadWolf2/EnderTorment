package white_blizz.ender_torment.client.render.shader;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import white_blizz.ender_torment.utils.Ref;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ETShaderHelper {
	private static final Ref.Loc POST = Ref.makeLoc()
			.copy(Ref.MOD)
			.path("shaders/post/")
			.type("json").build();

	public static ResourceLocation getPost(String name) {
		return POST.rl.loc(name);
	}

	private static Minecraft mc() {
		return Minecraft.getInstance();
	}

	public static ETShaderGroup newShaderGroup(Framebuffer in, String post) throws IOException {
		return newShaderGroup(in, post, new HashMap<>());
	}
	public static ETShaderGroup newShaderGroup(Framebuffer in, String post, Map<String, Framebuffer> overrides) throws IOException {
		return new ETShaderGroup(
				mc().textureManager,
				mc().getResourceManager(),
				in, getPost(post),
				overrides
		);
	}

	public static void pre(ETShaderGroup... groups) {
		int w = mc().getMainWindow().getFramebufferWidth();
		int h = mc().getMainWindow().getFramebufferHeight();
		for (ETShaderGroup group : groups) group.createBindFramebuffers(w, h);
	}

	public static void doFrame(Framebuffer frame, Runnable renderer) {
		frame.bindFramebuffer(true);
		renderer.run();
		frame.unbindFramebuffer();;
	}
}
