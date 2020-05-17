package white_blizz.ender_torment.common.container;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;

import javax.annotation.Nullable;

public abstract class ETContainer extends Container {
	protected ETContainer(@Nullable ContainerType<?> type, int id) {
		super(type, id);
	}
}
