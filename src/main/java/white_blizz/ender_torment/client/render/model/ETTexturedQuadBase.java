package white_blizz.ender_torment.client.render.model;

import net.minecraft.client.renderer.Vector3f;

public abstract class ETTexturedQuadBase {
	abstract ETPositionTextureVertexBase[] getVertexes();
	abstract Vector3f getNormal();
}
