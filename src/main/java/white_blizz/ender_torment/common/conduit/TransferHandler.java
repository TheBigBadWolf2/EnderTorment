package white_blizz.ender_torment.common.conduit;

import mcp.MethodsReturnNonnullByDefault;
import white_blizz.ender_torment.common.conduit.io.IConduitBuffer;
import white_blizz.ender_torment.common.conduit.io.IConduitInput;
import white_blizz.ender_torment.common.conduit.io.IConduitOutput;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public interface TransferHandler<Cap> {
	void handle(List<IConduitInput<Cap>> ins,
				List<IConduitBuffer<Cap>> buffs,
				List<IConduitOutput<Cap>> outs);
}
