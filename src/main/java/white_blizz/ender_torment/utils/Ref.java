package white_blizz.ender_torment.utils;

import net.minecraft.util.ResourceLocation;

public final class Ref {
	public static final String MOD_ID = "ender_torment";

	public static String locStr(String path) { return loc(path).toString(); }
	public static ResourceLocation loc(String path) { return new ResourceLocation(MOD_ID, path); }

	public static ResourceLocation loc(String path, String name) {
		if (!path.endsWith("/")) path += "/";
		return loc(path+name);
	}

	public static ResourceLocation loc(String path, String name, String type) {
		if (!path.endsWith("/")) path += "/";
		return loc(path+name+"."+type);
	}
}
