package white_blizz.ender_torment.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;

public interface IEnderFluxBlock {
	static Block.Properties getDefaultProperties() {
		return Block.Properties.create(Material.IRON)
				.harvestTool(ToolType.PICKAXE)
				.hardnessAndResistance(1.0F);
	}
}
