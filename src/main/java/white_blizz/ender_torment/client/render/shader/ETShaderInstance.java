package white_blizz.ender_torment.client.render.shader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.util.JSONBlendingMode;
import net.minecraft.client.util.JSONException;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ETShaderInstance implements IETShaderManager, AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ETShaderDefault DEFAULT = new ETShaderDefault();
   private static ETShaderInstance currentInstance;
   private static int currentProgram = -1;
   private final Map<String, Object> field_216549_e = Maps.newHashMap();
   private final List<String> field_216550_f = Lists.newArrayList();
   private final List<Integer> field_216551_g = Lists.newArrayList();
   private final List<ETShaderUniform> uniforms = Lists.newArrayList();
   private final List<Integer> field_216553_i = Lists.newArrayList();
   private final Map<String, ETShaderUniform> uniformMap = Maps.newHashMap();
   private final int program;
   private final String name;
   private final boolean cull;
   private boolean dirty;
   private final JSONBlendingMode field_216559_o;
   private final List<Integer> locations;
   private final List<String> attributes;
   private final ETShaderLoader vertexShader;
   private final ETShaderLoader fragmentShader;

   public ETShaderInstance(IResourceManager p_i50988_1_, String name) throws IOException {
      ResourceLocation rl = ResourceLocation.tryCreate(name);
      assert rl != null;
      ResourceLocation resourcelocation = new ResourceLocation(rl.getNamespace(), "shaders/program/" + rl.getPath() + ".json");
      this.name = name;
      IResource iresource = null;

      try {
         iresource = p_i50988_1_.getResource(resourcelocation);
         JsonObject jsonobject = JSONUtils.fromJson(new InputStreamReader(iresource.getInputStream(), StandardCharsets.UTF_8));
         String s = JSONUtils.getString(jsonobject, "vertex");
         String s1 = JSONUtils.getString(jsonobject, "fragment");
         JsonArray jsonarray = JSONUtils.getJsonArray(jsonobject, "samplers", null);
         if (jsonarray != null) {
            int i = 0;

            for(JsonElement jsonelement : jsonarray) {
               try {
                  this.func_216541_a(jsonelement);
               } catch (Exception exception2) {
                  JSONException jsonexception1 = JSONException.forException(exception2);
                  jsonexception1.prependJsonKey("samplers[" + i + "]");
                  throw jsonexception1;
               }

               ++i;
            }
         }

         JsonArray jsonarray1 = JSONUtils.getJsonArray(jsonobject, "attributes", null);
         if (jsonarray1 != null) {
            int j = 0;
            this.locations = Lists.newArrayListWithCapacity(jsonarray1.size());
            this.attributes = Lists.newArrayListWithCapacity(jsonarray1.size());

            for(JsonElement jsonelement1 : jsonarray1) {
               try {
                  this.attributes.add(JSONUtils.getString(jsonelement1, "attribute"));
               } catch (Exception exception1) {
                  JSONException jsonexception2 = JSONException.forException(exception1);
                  jsonexception2.prependJsonKey("attributes[" + j + "]");
                  throw jsonexception2;
               }

               ++j;
            }
         } else {
            this.locations = null;
            this.attributes = null;
         }

         JsonArray jsonarray2 = JSONUtils.getJsonArray(jsonobject, "uniforms", null);
         if (jsonarray2 != null) {
            int k = 0;

            for(JsonElement jsonelement2 : jsonarray2) {
               try {
                  this.func_216540_b(jsonelement2);
               } catch (Exception exception) {
                  JSONException jsonexception3 = JSONException.forException(exception);
                  jsonexception3.prependJsonKey("uniforms[" + k + "]");
                  throw jsonexception3;
               }

               ++k;
            }
         }

         //noinspection ConstantConditions
         this.field_216559_o = func_216543_a(JSONUtils.getJsonObject(jsonobject, "blend", null));
         this.cull = JSONUtils.getBoolean(jsonobject, "cull", true);
         this.vertexShader = getLoader(p_i50988_1_, ETShaderLoader.ShaderType.VERTEX, s);
         this.fragmentShader = getLoader(p_i50988_1_, ETShaderLoader.ShaderType.FRAGMENT, s1);
         this.program = ETShaderLinkHelper.createProgram();
         ETShaderLinkHelper.linkProgram(this);
         this.func_216536_h();
         if (this.attributes != null) {
            for(String s2 : this.attributes) {
               int l = ETShaderUniform.func_227807_b_(this.program, s2);
               this.locations.add(l);
            }
         }
      } catch (Exception exception3) {
         JSONException jsonexception = JSONException.forException(exception3);
         jsonexception.setFilenameAndFlush(resourcelocation.getPath());
         throw jsonexception;
      } finally {
         IOUtils.closeQuietly(iresource);
      }

      this.markDirty();
   }

   public static ETShaderLoader getLoader(IResourceManager manager, ETShaderLoader.ShaderType type, String name) throws IOException {
      ETShaderLoader shaderloader = type.getLoadedShaders().get(name);
      if (shaderloader == null) {
         ResourceLocation rl = ResourceLocation.tryCreate(name);
         assert rl != null;
         ResourceLocation resourcelocation = new ResourceLocation(rl.getNamespace(), "shaders/program/" + rl.getPath() + type.getShaderExtension());
         IResource iresource = manager.getResource(resourcelocation);

         try {
            shaderloader = ETShaderLoader.load(type, name, iresource.getInputStream());
         } finally {
            IOUtils.closeQuietly(iresource);
         }
      }

      return shaderloader;
   }

   public static JSONBlendingMode func_216543_a(JsonObject p_216543_0_) {
      if (p_216543_0_ == null) {
         return new JSONBlendingMode();
      } else {
         int i = 32774;
         int j = 1;
         int k = 0;
         int l = 1;
         int i1 = 0;
         boolean flag = true;
         boolean flag1 = false;
         if (JSONUtils.isString(p_216543_0_, "func")) {
            i = JSONBlendingMode.stringToBlendFunction(p_216543_0_.get("func").getAsString());
            if (i != 32774) {
               flag = false;
            }
         }

         if (JSONUtils.isString(p_216543_0_, "srcrgb")) {
            j = JSONBlendingMode.stringToBlendFactor(p_216543_0_.get("srcrgb").getAsString());
            if (j != 1) {
               flag = false;
            }
         }

         if (JSONUtils.isString(p_216543_0_, "dstrgb")) {
            k = JSONBlendingMode.stringToBlendFactor(p_216543_0_.get("dstrgb").getAsString());
            if (k != 0) {
               flag = false;
            }
         }

         if (JSONUtils.isString(p_216543_0_, "srcalpha")) {
            l = JSONBlendingMode.stringToBlendFactor(p_216543_0_.get("srcalpha").getAsString());
            if (l != 1) {
               flag = false;
            }

            flag1 = true;
         }

         if (JSONUtils.isString(p_216543_0_, "dstalpha")) {
            i1 = JSONBlendingMode.stringToBlendFactor(p_216543_0_.get("dstalpha").getAsString());
            if (i1 != 0) {
               flag = false;
            }

            flag1 = true;
         }

         if (flag) {
            return new JSONBlendingMode();
         } else {
            return flag1 ? new JSONBlendingMode(j, k, l, i1, i) : new JSONBlendingMode(j, k, i);
         }
      }
   }

   public String getName() { return name; }

   public void close() {
      for(ETShaderUniform shaderuniform : this.uniforms) {
         shaderuniform.close();
      }

      ETShaderLinkHelper.deleteShader(this);
   }

   public void unbind() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      ETShaderLinkHelper.useProgram(0);
      currentProgram = -1;
      currentInstance = null;

      for(int i = 0; i < this.field_216551_g.size(); ++i) {
         if (this.field_216549_e.get(this.field_216550_f.get(i)) != null) {
            GlStateManager.activeTexture('\u84c0' + i);
            GlStateManager.bindTexture(0);
         }
      }

   }

   public void bind() {
      RenderSystem.assertThread(RenderSystem::isOnGameThread);
      this.dirty = false;
      currentInstance = this;
      this.field_216559_o.apply();
      if (this.program != currentProgram) {
         ETShaderLinkHelper.useProgram(this.program);
         currentProgram = this.program;
      }

      if (this.cull) {
         RenderSystem.enableCull();
      } else {
         RenderSystem.disableCull();
      }

      for(int i = 0; i < this.field_216551_g.size(); ++i) {
         if (this.field_216549_e.get(this.field_216550_f.get(i)) != null) {
            RenderSystem.activeTexture('\u84c0' + i);
            RenderSystem.enableTexture();
            Object object = this.field_216549_e.get(this.field_216550_f.get(i));
            int j = -1;
            if (object instanceof Framebuffer) {
               j = ((Framebuffer)object).framebufferTexture;
            } else if (object instanceof Texture) {
               j = ((Texture)object).getGlTextureId();
            } else if (object instanceof Integer) {
               j = (Integer)object;
            }

            if (j != -1) {
               RenderSystem.bindTexture(j);
               ETShaderUniform.func_227805_a_(ETShaderUniform.func_227806_a_(this.program, this.field_216550_f.get(i)), i);
            }
         }
      }

      for(ETShaderUniform shaderuniform : this.uniforms) {
         shaderuniform.upload();
      }

   }

   public void markDirty() {
      this.dirty = true;
   }

   @Nullable
   public ETShaderUniform getUniform(String name) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return this.uniformMap.get(name);
   }

   public ETShaderDefault getShaderUniform(String name) {
      RenderSystem.assertThread(RenderSystem::isOnGameThread);
      ETShaderUniform shaderuniform = this.getUniform(name);
      return shaderuniform == null ? DEFAULT : shaderuniform;
   }

   private void func_216536_h() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      int i = 0;

      for(int j = 0; i < this.field_216550_f.size(); ++j) {
         String s = this.field_216550_f.get(i);
         int k = ETShaderUniform.func_227806_a_(this.program, s);
         if (k == -1) {
            LOGGER.warn("Shader {}could not find sampler named {} in the specified shader program.", this.name, s);
            this.field_216549_e.remove(s);
            this.field_216550_f.remove(j);
            --j;
         } else {
            this.field_216551_g.add(k);
         }

         ++i;
      }

      for(ETShaderUniform shaderuniform : this.uniforms) {
         String s1 = shaderuniform.getShaderName();
         int l = ETShaderUniform.func_227806_a_(this.program, s1);
         if (l == -1) {
            LOGGER.warn("Could not find uniform named {} in the specified shader program.", s1);
         } else {
            this.field_216553_i.add(l);
            shaderuniform.setUniformLocation(l);
            this.uniformMap.put(s1, shaderuniform);
         }
      }

   }

   private void func_216541_a(JsonElement p_216541_1_) {
      JsonObject jsonobject = JSONUtils.getJsonObject(p_216541_1_, "sampler");
      String s = JSONUtils.getString(jsonobject, "name");
      if (!JSONUtils.isString(jsonobject, "file")) {
         this.field_216549_e.put(s, null);
         this.field_216550_f.add(s);
      } else {
         this.field_216550_f.add(s);
      }
   }

   public void func_216537_a(String p_216537_1_, Object p_216537_2_) {
      this.field_216549_e.remove(p_216537_1_);

      this.field_216549_e.put(p_216537_1_, p_216537_2_);
      this.markDirty();
   }

   private void func_216540_b(JsonElement p_216540_1_) throws JSONException {
      JsonObject jsonobject = JSONUtils.getJsonObject(p_216540_1_, "uniform");
      String s = JSONUtils.getString(jsonobject, "name");
      int i = ETShaderUniform.parseType(JSONUtils.getString(jsonobject, "type"));
      int j = JSONUtils.getInt(jsonobject, "count");
      float[] afloat = new float[Math.max(j, 16)];
      JsonArray jsonarray = JSONUtils.getJsonArray(jsonobject, "values");
      if (jsonarray.size() != j && jsonarray.size() > 1) {
         throw new JSONException("Invalid amount of values specified (expected " + j + ", found " + jsonarray.size() + ")");
      } else {
         int k = 0;

         for(JsonElement jsonelement : jsonarray) {
            try {
               afloat[k] = JSONUtils.getFloat(jsonelement, "value");
            } catch (Exception exception) {
               JSONException jsonexception = JSONException.forException(exception);
               jsonexception.prependJsonKey("values[" + k + "]");
               throw jsonexception;
            }

            ++k;
         }

         if (j > 1 && jsonarray.size() == 1) {
            while(k < j) {
               afloat[k] = afloat[0];
               ++k;
            }
         }

         int l = j > 1 && j <= 4 && i < 8 ? j - 1 : 0;
         ETShaderUniform shaderuniform = new ETShaderUniform(s, i + l, j, this);
         if (i <= 3) {
            shaderuniform.set((int)afloat[0], (int)afloat[1], (int)afloat[2], (int)afloat[3]);
         } else if (i <= 7) {
            shaderuniform.setSafe(afloat[0], afloat[1], afloat[2], afloat[3]);
         } else {
            shaderuniform.set(afloat);
         }

         this.uniforms.add(shaderuniform);
      }
   }

   public ETShaderLoader getVertexShaderLoader() {
      return this.vertexShader;
   }

   public ETShaderLoader getFragmentShaderLoader() {
      return this.fragmentShader;
   }

   public int getProgram() {
      return this.program;
   }
}