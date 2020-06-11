package white_blizz.ender_torment.client.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ETShaderLinkHelper {
   private static final Logger LOGGER = LogManager.getLogger();

   public static void useProgram(int program) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GlStateManager.useProgram(program);
   }

   public static void deleteShader(IETShaderManager p_148077_0_) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      p_148077_0_.getFragmentShaderLoader().detachShader();
      p_148077_0_.getVertexShaderLoader().detachShader();
      GlStateManager.deleteProgram(p_148077_0_.getProgram());
   }

   public static int createProgram() throws IOException {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      int i = GlStateManager.createProgram();
      if (i <= 0) {
         throw new IOException("Could not create shader program (returned program ID " + i + ")");
      } else {
         return i;
      }
   }

   public static void linkProgram(IETShaderManager p_148075_0_) throws IOException {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      p_148075_0_.getFragmentShaderLoader().attachShader(p_148075_0_);
      p_148075_0_.getVertexShaderLoader().attachShader(p_148075_0_);
      GlStateManager.linkProgram(p_148075_0_.getProgram());
      int i = GlStateManager.getProgram(p_148075_0_.getProgram(), 35714);
      if (i == 0) {
         LOGGER.warn("Error encountered when linking program containing VS {} and FS {}. Log output:", p_148075_0_.getVertexShaderLoader().getShaderFilename(), p_148075_0_.getFragmentShaderLoader().getShaderFilename());
         LOGGER.warn(GlStateManager.getProgramInfoLog(p_148075_0_.getProgram(), 32768));
      }

   }
}