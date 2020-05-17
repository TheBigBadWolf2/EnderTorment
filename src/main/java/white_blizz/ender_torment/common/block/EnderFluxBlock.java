package white_blizz.ender_torment.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;

import java.util.function.UnaryOperator;

public class EnderFluxBlock extends ETBlock implements IEnderFluxBlock {
	public EnderFluxBlock(UnaryOperator<Block.Properties> operator) {
		super(operator.apply(
				IEnderFluxBlock.getDefaultProperties()
		));
	}
}
