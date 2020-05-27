package white_blizz.ender_torment.client.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class ETShaderUniform extends ETShaderDefault implements AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   private int uniformLocation;
   private final int uniformCount;
   private final int uniformType;
   private final IntBuffer uniformIntBuffer;
   private final FloatBuffer uniformFloatBuffer;
   private final String shaderName;
   private boolean dirty;
   private final IETShaderManager shaderManager;

   public ETShaderUniform(String name, int type, int count, IETShaderManager manager) {
      this.shaderName = name;
      this.uniformCount = count;
      this.uniformType = type;
      this.shaderManager = manager;
      if (type <= 3) {
         this.uniformIntBuffer = MemoryUtil.memAllocInt(count);
         this.uniformFloatBuffer = null;
      } else {
         this.uniformIntBuffer = null;
         this.uniformFloatBuffer = MemoryUtil.memAllocFloat(count);
      }

      this.uniformLocation = -1;
      this.markDirty();
   }

   public static int func_227806_a_(int p_227806_0_, CharSequence p_227806_1_) {
      return GlStateManager.getUniformLocation(p_227806_0_, p_227806_1_);
   }

   public static void func_227805_a_(int p_227805_0_, int p_227805_1_) {
      RenderSystem.glUniform1i(p_227805_0_, p_227805_1_);
   }

   public static int func_227807_b_(int p_227807_0_, CharSequence p_227807_1_) {
      return GlStateManager.getAttribLocation(p_227807_0_, p_227807_1_);
   }

   public void close() {
      if (this.uniformIntBuffer != null) {
         MemoryUtil.memFree(this.uniformIntBuffer);
      }

      if (this.uniformFloatBuffer != null) {
         MemoryUtil.memFree(this.uniformFloatBuffer);
      }

   }

   private void markDirty() {
      this.dirty = true;
      if (this.shaderManager != null) {
         this.shaderManager.markDirty();
      }

   }

   public static int parseType(String typeName) {
      int i = -1;
      if ("int".equals(typeName)) {
         i = 0;
      } else if ("float".equals(typeName)) {
         i = 4;
      } else if (typeName.startsWith("matrix")) {
         if (typeName.endsWith("2x2")) {
            i = 8;
         } else if (typeName.endsWith("3x3")) {
            i = 9;
         } else if (typeName.endsWith("4x4")) {
            i = 10;
         }
      }

      return i;
   }

   public void setUniformLocation(int uniformLocationIn) {
      this.uniformLocation = uniformLocationIn;
   }

   public String getShaderName() {
      return this.shaderName;
   }

   public void set(float a) {
      this.uniformFloatBuffer.position(0);
      this.uniformFloatBuffer.put(0, a);
      this.markDirty();
   }

   public void set(float a, float b) {
      this.uniformFloatBuffer.position(0);
      this.uniformFloatBuffer.put(0, a);
      this.uniformFloatBuffer.put(1, b);
      this.markDirty();
   }

   public void set(float a, float b, float c) {
      this.uniformFloatBuffer.position(0);
      this.uniformFloatBuffer.put(0, a);
      this.uniformFloatBuffer.put(1, b);
      this.uniformFloatBuffer.put(2, c);
      this.markDirty();
   }

   public void set(float a, float b, float c, float d) {
      this.uniformFloatBuffer.position(0);
      this.uniformFloatBuffer.put(a);
      this.uniformFloatBuffer.put(b);
      this.uniformFloatBuffer.put(c);
      this.uniformFloatBuffer.put(d);
      this.uniformFloatBuffer.flip();
      this.markDirty();
   }

   public void setSafe(float a, float b, float c, float d) {
      this.uniformFloatBuffer.position(0);
      if (this.uniformType >= 4) {
         this.uniformFloatBuffer.put(0, a);
      }

      if (this.uniformType >= 5) {
         this.uniformFloatBuffer.put(1, b);
      }

      if (this.uniformType >= 6) {
         this.uniformFloatBuffer.put(2, c);
      }

      if (this.uniformType >= 7) {
         this.uniformFloatBuffer.put(3, d);
      }

      this.markDirty();
   }

   public void set(int a, int b, int c, int d) {
      this.uniformIntBuffer.position(0);
      if (this.uniformType >= 0) {
         this.uniformIntBuffer.put(0, a);
      }

      if (this.uniformType >= 1) {
         this.uniformIntBuffer.put(1, b);
      }

      if (this.uniformType >= 2) {
         this.uniformIntBuffer.put(2, c);
      }

      if (this.uniformType >= 3) {
         this.uniformIntBuffer.put(3, d);
      }

      this.markDirty();
   }

   public void set(float[] fs) {
      if (fs.length < this.uniformCount) {
         LOGGER.warn("Uniform.set called with a too-small value array (expected {}, got {}). Ignoring.", this.uniformCount, fs.length);
      } else {
         this.uniformFloatBuffer.position(0);
         this.uniformFloatBuffer.put(fs);
         this.uniformFloatBuffer.position(0);
         this.markDirty();
      }
   }

   public void set(Matrix4f matrix) {
      this.uniformFloatBuffer.position(0);
      matrix.write(this.uniformFloatBuffer);
      this.markDirty();
   }

   public void upload() {
      if (!this.dirty) return;

      this.dirty = false;
      if (this.uniformType <= 3) {
         this.uploadInt();
      } else if (this.uniformType <= 7) {
         this.uploadFloat();
      } else {
         if (this.uniformType > 10) {
            LOGGER.warn("Uniform.upload called, but type value ({}) is not a valid type. Ignoring.", this.uniformType);
            return;
         }

         this.uploadFloatMatrix();
      }

   }

   private void uploadInt() {
      this.uniformFloatBuffer.clear();
      switch(this.uniformType) {
      case 0:
         RenderSystem.glUniform1(this.uniformLocation, this.uniformIntBuffer);
         break;
      case 1:
         RenderSystem.glUniform2(this.uniformLocation, this.uniformIntBuffer);
         break;
      case 2:
         RenderSystem.glUniform3(this.uniformLocation, this.uniformIntBuffer);
         break;
      case 3:
         RenderSystem.glUniform4(this.uniformLocation, this.uniformIntBuffer);
         break;
      default:
         LOGGER.warn("Uniform.upload called, but count value ({}) is  not in the range of 1 to 4. Ignoring.", (int)this.uniformCount);
      }

   }

   private void uploadFloat() {
      ((Buffer)this.uniformFloatBuffer).clear();
      switch(this.uniformType) {
      case 4:
         RenderSystem.glUniform1(this.uniformLocation, this.uniformFloatBuffer);
         break;
      case 5:
         RenderSystem.glUniform2(this.uniformLocation, this.uniformFloatBuffer);
         break;
      case 6:
         RenderSystem.glUniform3(this.uniformLocation, this.uniformFloatBuffer);
         break;
      case 7:
         RenderSystem.glUniform4(this.uniformLocation, this.uniformFloatBuffer);
         break;
      default:
         LOGGER.warn("Uniform.upload called, but count value ({}) is not in the range of 1 to 4. Ignoring.", (int)this.uniformCount);
      }

   }

   private void uploadFloatMatrix() {
      ((Buffer)this.uniformFloatBuffer).clear();
      switch(this.uniformType) {
      case 8:
         RenderSystem.glUniformMatrix2(this.uniformLocation, false, this.uniformFloatBuffer);
         break;
      case 9:
         RenderSystem.glUniformMatrix3(this.uniformLocation, false, this.uniformFloatBuffer);
         break;
      case 10:
         RenderSystem.glUniformMatrix4(this.uniformLocation, false, this.uniformFloatBuffer);
      }

   }
}