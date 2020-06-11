package white_blizz.ender_torment.client.render.shader;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ETShaderLoader {
   private final ShaderType shaderType;
   private final String shaderFilename;
   private final int shader;
   private int shaderAttachCount;

   private ETShaderLoader(ShaderType type, int shaderId, String filename) {
      this.shaderType = type;
      this.shader = shaderId;
      this.shaderFilename = filename;
   }

   public void attachShader(IETShaderManager manager) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      ++this.shaderAttachCount;
      GlStateManager.attachShader(manager.getProgram(), this.shader);
   }

   public void detachShader() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      --this.shaderAttachCount;
      if (this.shaderAttachCount <= 0) {
         GlStateManager.deleteShader(this.shader);
         this.shaderType.getLoadedShaders().remove(this.shaderFilename);
      }

   }

   public String getShaderFilename() {
      return this.shaderFilename;
   }

   public static ETShaderLoader load(ShaderType type, String name, InputStream stream) throws IOException {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      String s = TextureUtil.readResourceAsString(stream);
      if (s == null) {
         throw new IOException("Could not load program " + type.getShaderName());
      } else {
         int id = GlStateManager.createShader(type.getShaderMode());
         GlStateManager.shaderSource(id, s);
         GlStateManager.compileShader(id);
         if (GlStateManager.getShader(id, 35713) == 0) {
            String s1 = StringUtils.trim(GlStateManager.getShaderInfoLog(id, 32768));
            throw new IOException("Couldn't compile " + type.getShaderName() + " program: " + s1);
         } else {
            ETShaderLoader shaderloader = new ETShaderLoader(type, id, name);
            type.getLoadedShaders().put(name, shaderloader);
            return shaderloader;
         }
      }
   }

   public enum ShaderType {
      VERTEX("vertex", ".vsh", GL20.GL_VERTEX_SHADER),
      FRAGMENT("fragment", ".fsh", GL20.GL_FRAGMENT_SHADER);

      private final String shaderName;
      private final String shaderExtension;
      private final int shaderMode;
      private final Map<String, ETShaderLoader> loadedShaders = Maps.newHashMap();

      ShaderType(String shaderNameIn, String shaderExtensionIn, int shaderModeIn) {
         this.shaderName = shaderNameIn;
         this.shaderExtension = shaderExtensionIn;
         this.shaderMode = shaderModeIn;
      }

      public String getShaderName() {
         return this.shaderName;
      }

      public String getShaderExtension() {
         return this.shaderExtension;
      }

      private int getShaderMode() {
         return this.shaderMode;
      }

      /**
       * gets a map of loaded shaders for the ShaderType.
       */
      public Map<String, ETShaderLoader> getLoadedShaders() {
         return this.loadedShaders;
      }
   }
}