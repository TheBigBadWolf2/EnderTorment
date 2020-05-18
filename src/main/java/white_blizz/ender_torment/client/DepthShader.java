package white_blizz.ender_torment.client;

import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.resources.IResourceManager;

import java.io.IOException;

public class DepthShader extends Shader {
	public DepthShader(IResourceManager resourceManager, String programName, Framebuffer framebufferInIn, Framebuffer framebufferOutIn) throws IOException {
		super(resourceManager, programName, framebufferInIn, framebufferOutIn);
	}
}
