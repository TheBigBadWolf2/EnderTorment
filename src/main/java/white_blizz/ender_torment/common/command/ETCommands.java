package white_blizz.ender_torment.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.TimeArgument;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Consumer;
import java.util.function.ToIntFunction;

public class ETCommands {
	private static LiteralArgumentBuilder<CommandSource> lit(String name) {
		return Commands.literal(name);
	}

	private static <T> RequiredArgumentBuilder<CommandSource, T> arg(String name, ArgumentType<T> type) {
		return Commands.argument(name, type);
	}



	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralCommandNode<CommandSource> cmd = dispatcher.register(
				lit("ender")
						.then(lit("time")
								.then(lit("get")
										.executes(ctx -> run(ctx, src -> src.sendFeedback(
												new StringTextComponent(
														String.valueOf(src.getWorld().getDayTime())
												), false
										)))
								).then(lit("set")
										.requires(src -> src.hasPermissionLevel(2))
										.then(arg("value", TimeArgument.func_218091_a())
												.executes(ctx -> run(ctx, src -> {
													src.getWorld().setDayTime(IntegerArgumentType.getInteger(ctx, "value"));
													src.sendFeedback(
															new StringTextComponent(
																	String.valueOf(src.getWorld().getDayTime())
															), false
													);
												}))
										)
								)
						)
		);
		dispatcher.register(lit("et").redirect(cmd.getChild("time")));
	}

	private static int run(CommandContext<CommandSource> ctx, Consumer<CommandSource> run) {
		run.accept(ctx.getSource());
		return 1;
	}
}
