package white_blizz.ender_torment.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import white_blizz.ender_torment.utils.Ref;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Pack implements IResourcePack {
	static final Pack INSTANCE = new Pack();

	private final Map<ResourceLocation, NativeImage> images = new HashMap<>();
	private final Set<ResourceLocation> names = new HashSet<>();

	void add(ResourceLocation name, NativeImage image) {
		names.add(name);
		name = new ResourceLocation(name.getNamespace(), "textures/"+name.getPath()+".png");
		images.compute(name, (k, v) -> {
			if (v != null) v.close();
			return image;
		});
	}

	public List<ResourceLocation> getNames() { return ImmutableList.copyOf(names); }

	private void assertType(ResourcePackType type) throws IOException {
		if (type != ResourcePackType.CLIENT_RESOURCES)
			throw new IOException("Type " + type + " not supported!");
	}

	@Override
	public InputStream getRootResourceStream(String fileName) throws IOException {
		throw new IOException("Get your own file \""+fileName+"\"!");
	}

	@Override
	public InputStream getResourceStream(ResourcePackType type, ResourceLocation location) throws IOException {
		assertType(type);
		return new ByteArrayInputStream(images.get(location).getBytes());
	}

	@Override
	public Collection<ResourceLocation> getAllResourceLocations(ResourcePackType type, String namespaceIn, String pathIn, int maxDepthIn, Predicate<String> filterIn) {
		ResourceLocation location = new ResourceLocation(namespaceIn, pathIn);
		if (resourceExists(type, location) && filterIn.test(location.toString())) return ImmutableList.of(location);
		return ImmutableList.of();
	}

	@Override
	public boolean resourceExists(ResourcePackType type, ResourceLocation location) {
		return images.containsKey(location);
	}

	@Override
	public Set<String> getResourceNamespaces(ResourcePackType type) {
		return ImmutableSet.of(Ref.MOD_ID);
	}

	@Nullable
	@Override
	public <T> T getMetadata(IMetadataSectionSerializer<T> deserializer) {
		return null;
	}

	@Override
	public String getName() {
		return "ender_torment_fade";
	}

	@Override
	public void close() {
		images.forEach((k, v) -> v.close());
		images.clear();
		names.clear();
	}
}
