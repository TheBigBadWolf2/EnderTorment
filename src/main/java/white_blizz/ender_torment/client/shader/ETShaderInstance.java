package white_blizz.ender_torment.client.shader;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ETShaderInstance implements IETShaderManager, AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ETShaderDefault field_216546_b = new ETShaderDefault();
   private static ETShaderInstance field_216547_c;
   private static int field_216548_d = -1;
   private final Map<String, Object> field_216549_e = Maps.newHashMap();
   private final List<String> field_216550_f = Lists.newArrayList();
   private final List<Integer> field_216551_g = Lists.newArrayList();
   private final List<ETShaderUniform> field_216552_h = Lists.newArrayList();
   private final List<Integer> field_216553_i = Lists.newArrayList();
   private final Map<String, ETShaderUniform> field_216554_j = Maps.newHashMap();
   private final int field_216555_k;
   private final String name;
   private final boolean field_216557_m;
   private boolean field_216558_n;
   private final JSONBlendingMode field_216559_o;
   private final List<Integer> field_216560_p;
   private final List<String> field_216561_q;
   private final ETShaderLoader field_216562_r;
   private final ETShaderLoader field_216563_s;

   public ETShaderInstance(IResourceManager p_i50988_1_, String name) throws IOException {
      ResourceLocation rl = ResourceLocation.tryCreate(name);
      ResourceLocation resourcelocation = new ResourceLocation(rl.getNamespace(), "shaders/program/" + rl.getPath() + ".json");
      this.name = name;
      IResource iresource = null;

      try {
         iresource = p_i50988_1_.getResource(resourcelocation);
         JsonObject jsonobject = JSONUtils.fromJson(new InputStreamReader(iresource.getInputStream(), StandardCharsets.UTF_8));
         String s = JSONUtils.getString(jsonobject, "vertex");
         String s1 = JSONUtils.getString(jsonobject, "fragment");
         JsonArray jsonarray = JSONUtils.getJsonArray(jsonobject, "samplers", (JsonArray)null);
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

         JsonArray jsonarray1 = JSONUtils.getJsonArray(jsonobject, "attributes", (JsonArray)null);
         if (jsonarray1 != null) {
            int j = 0;
            this.field_216560_p = Lists.newArrayListWithCapacity(jsonarray1.size());
            this.field_216561_q = Lists.newArrayListWithCapacity(jsonarray1.size());

            for(JsonElement jsonelement1 : jsonarray1) {
               try {
                  this.field_216561_q.add(JSONUtils.getString(jsonelement1, "attribute"));
               } catch (Exception exception1) {
                  JSONException jsonexception2 = JSONException.forException(exception1);
                  jsonexception2.prependJsonKey("attributes[" + j + "]");
                  throw jsonexception2;
               }

               ++j;
            }
         } else {
            this.field_216560_p = null;
            this.field_216561_q = null;
         }

         JsonArray jsonarray2 = JSONUtils.getJsonArray(jsonobject, "uniforms", (JsonArray)null);
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

         this.field_216559_o = func_216543_a(JSONUtils.getJsonObject(jsonobject, "blend", (JsonObject)null));
         this.field_216557_m = JSONUtils.getBoolean(jsonobject, "cull", true);
         this.field_216562_r = func_216542_a(p_i50988_1_, ETShaderLoader.ShaderType.VERTEX, s);
         this.field_216563_s = func_216542_a(p_i50988_1_, ETShaderLoader.ShaderType.FRAGMENT, s1);
         this.field_216555_k = ETShaderLinkHelper.createProgram();
         ETShaderLinkHelper.linkProgram(this);
         this.func_216536_h();
         if (this.field_216561_q != null) {
            for(String s2 : this.field_216561_q) {
               int l = ETShaderUniform.func_227807_b_(this.field_216555_k, s2);
               this.field_216560_p.add(l);
            }
         }
      } catch (Exception exception3) {
         JSONException jsonexception = JSONException.forException(exception3);
         jsonexception.setFilenameAndFlush(resourcelocation.getPath());
         throw jsonexception;
      } finally {
         IOUtils.closeQuietly((Closeable)iresource);
      }

      this.markDirty();
   }

   public static ETShaderLoader func_216542_a(IResourceManager p_216542_0_, ETShaderLoader.ShaderType p_216542_1_, String p_216542_2_) throws IOException {
      ETShaderLoader shaderloader = p_216542_1_.getLoadedShaders().get(p_216542_2_);
      if (shaderloader == null) {
         ResourceLocation rl = ResourceLocation.tryCreate(p_216542_2_);
         ResourceLocation resourcelocation = new ResourceLocation(rl.getNamespace(), "shaders/program/" + rl.getPath() + p_216542_1_.getShaderExtension());
         IResource iresource = p_216542_0_.getResource(resourcelocation);

         try {
            shaderloader = ETShaderLoader.func_216534_a(p_216542_1_, p_216542_2_, iresource.getInputStream());
         } finally {
            IOUtils.closeQuietly((Closeable)iresource);
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
      for(ETShaderUniform shaderuniform : this.field_216552_h) {
         shaderuniform.close();
      }

      ETShaderLinkHelper.deleteShader(this);
   }

   public void func_216544_e() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      ETShaderLinkHelper.func_227804_a_(0);
      field_216548_d = -1;
      field_216547_c = null;

      for(int i = 0; i < this.field_216551_g.size(); ++i) {
         if (this.field_216549_e.get(this.field_216550_f.get(i)) != null) {
            GlStateManager.activeTexture('\u84c0' + i);
            GlStateManager.bindTexture(0);
         }
      }

   }

   public void func_216535_f() {
      RenderSystem.assertThread(RenderSystem::isOnGameThread);
      this.field_216558_n = false;
      field_216547_c = this;
      this.field_216559_o.apply();
      if (this.field_216555_k != field_216548_d) {
         ETShaderLinkHelper.func_227804_a_(this.field_216555_k);
         field_216548_d = this.field_216555_k;
      }

      if (this.field_216557_m) {
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
               ETShaderUniform.func_227805_a_(ETShaderUniform.func_227806_a_(this.field_216555_k, this.field_216550_f.get(i)), i);
            }
         }
      }

      for(ETShaderUniform shaderuniform : this.field_216552_h) {
         shaderuniform.upload();
      }

   }

   public void markDirty() {
      this.field_216558_n = true;
   }

   @Nullable
   public ETShaderUniform getUniform(String name) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      return this.field_216554_j.get(name);
   }

   public ETShaderDefault getShaderUniform(String name) {
      RenderSystem.assertThread(RenderSystem::isOnGameThread);
      ETShaderUniform shaderuniform = this.getUniform(name);
      return shaderuniform == null ? field_216546_b : shaderuniform;
   }

   private void func_216536_h() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      int i = 0;

      for(int j = 0; i < this.field_216550_f.size(); ++j) {
         String s = this.field_216550_f.get(i);
         int k = ETShaderUniform.func_227806_a_(this.field_216555_k, s);
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

      for(ETShaderUniform shaderuniform : this.field_216552_h) {
         String s1 = shaderuniform.getShaderName();
         int l = ETShaderUniform.func_227806_a_(this.field_216555_k, s1);
         if (l == -1) {
            LOGGER.warn("Could not find uniform named {} in the specified shader program.", (Object)s1);
         } else {
            this.field_216553_i.add(l);
            shaderuniform.setUniformLocation(l);
            this.field_216554_j.put(s1, shaderuniform);
         }
      }

   }

   private void func_216541_a(JsonElement p_216541_1_) {
      JsonObject jsonobject = JSONUtils.getJsonObject(p_216541_1_, "sampler");
      String s = JSONUtils.getString(jsonobject, "name");
      if (!JSONUtils.isString(jsonobject, "file")) {
         this.field_216549_e.put(s, (Object)null);
         this.field_216550_f.add(s);
      } else {
         this.field_216550_f.add(s);
      }
   }

   public void func_216537_a(String p_216537_1_, Object p_216537_2_) {
      if (this.field_216549_e.containsKey(p_216537_1_)) {
         this.field_216549_e.remove(p_216537_1_);
      }

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

         this.field_216552_h.add(shaderuniform);
      }
   }

   public ETShaderLoader getVertexShaderLoader() {
      return this.field_216562_r;
   }

   public ETShaderLoader getFragmentShaderLoader() {
      return this.field_216563_s;
   }

   public int getProgram() {
      return this.field_216555_k;
   }
}