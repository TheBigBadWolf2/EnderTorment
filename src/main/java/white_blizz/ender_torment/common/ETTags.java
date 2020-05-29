package white_blizz.ender_torment.common;

import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public final class ETTags {
	public static final class Items {
		public static final Tag<Item> WRENCH = new ItemTags.Wrapper(new ResourceLocation("forge:tool/wrench"));
	}
}
