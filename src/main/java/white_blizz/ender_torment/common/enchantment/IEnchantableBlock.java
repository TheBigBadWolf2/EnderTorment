package white_blizz.ender_torment.common.enchantment;

import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.INBTSerializable;
import white_blizz.ender_torment.utils.IEnchantmentList;

public interface IEnchantableBlock extends INBTSerializable<ListNBT>, IEnchantmentList {}