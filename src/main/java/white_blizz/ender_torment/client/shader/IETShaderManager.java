package white_blizz.ender_torment.client.shader;

public interface IETShaderManager {
   int getProgram();

   void markDirty();

   ETShaderLoader getVertexShaderLoader();

   ETShaderLoader getFragmentShaderLoader();
}