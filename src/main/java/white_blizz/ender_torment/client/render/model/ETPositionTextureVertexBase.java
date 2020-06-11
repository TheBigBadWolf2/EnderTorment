package white_blizz.ender_torment.client.render.model;

import net.minecraft.client.renderer.Vector3f;

public abstract class ETPositionTextureVertexBase {
	abstract Vector3f getPosition();
	abstract float u();
	abstract float v();

	public abstract ETPositionTextureVertexBase setTextureUV(float texU, float texV);

}
