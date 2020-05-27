package white_blizz.ender_torment.utils;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Ref {
	public static final String MOD_ID = "ender_torment";

	/*public static String locStr(String path) { return loc(path).toString(); }
	public static ResourceLocation loc(String path) { return new ResourceLocation(MOD_ID, path); }

	public static ResourceLocation loc(String path, String name) {
		if (!path.endsWith("/")) path += "/";
		return loc(path+name);
	}

	public static ResourceLocation loc(String path, String name, String type) {
		if (!path.endsWith("/")) path += "/";
		return loc(path+name+"."+type);
	}*/

	public static final Loc MOD = new Loc(MOD_ID);
	public static final Loc MC = new Loc(null);

	public static final class LocBuilder {
		private String mod_id, default_path, default_type;

		private LocBuilder() {}


		public LocBuilder copy(Loc other) {
			mod_id = other.mod_id;
			default_path = other.default_path;
			default_type = other.default_type;
			return this;
		}
		public LocBuilder mod(String mod_id) {
			this.mod_id = mod_id;
			return this;
		}
		public LocBuilder path(String path) {
			this.default_path = path;
			return this;
		}
		public LocBuilder type(String type) {
			this.default_type = type;
			return this;
		}

		public Loc build() { return new Loc(mod_id, default_path, default_type); }
	}

	public static LocBuilder makeLoc() { return new LocBuilder(); }

	public static final class Loc {
		private final String mod_id;
		@Nullable private final String default_path, default_type;

		public final ILoc<String> str = new S();
		public final ILoc<ResourceLocation> rl = new RL();

		public Loc(@Nullable String mod_id) { this(mod_id, null, null); }
		public Loc(@Nullable String mod_id, @Nullable String default_path) { this(mod_id, default_path, null); }
		public Loc(@Nullable String mod_id, @Nullable String default_path, @Nullable String default_type) {
			this.mod_id = mod_id == null ? "minecraft" : mod_id;
			this.default_path = default_path;
			this.default_type = default_type;
		}

		private ResourceLocation getLoc(String path, String name, String type) {
			if (path == null) path = default_path;
			if (path != null && !path.endsWith("/")) path += "/";
			else if (path == null) path = "";
			if (type == null) type = default_type;
			if (type != null) type = "." + type;
			else type = "";
			return new ResourceLocation(mod_id, path+name+type);
		}

		public interface ILoc<T> {
			default T loc(String name) { return loc(null, name); }
			default T loc(String path, String name) { return loc(path, name, null); }
			T loc(String path, String name, String type);
		}

		private final class S implements ILoc<String> {
			@Override
			public String loc(String path, String name, String type) {
				return rl.loc(path, name, type).toString();
			}
		}

		private final class RL implements ILoc<ResourceLocation> {

			@Override
			public ResourceLocation loc(String path, String name, String type) {
				return getLoc(path, name, type);
			}
		}
	}
}
