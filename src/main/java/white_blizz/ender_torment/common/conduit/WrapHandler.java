package white_blizz.ender_torment.common.conduit;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface WrapHandler<Cap> {
	LazyOptional<? extends Cap> getCap(TileEntity te, @Nullable Direction dir);
}
