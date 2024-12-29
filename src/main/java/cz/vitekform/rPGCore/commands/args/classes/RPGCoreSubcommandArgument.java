package cz.vitekform.rPGCore.commands.args.classes;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import cz.vitekform.rPGCore.commands.args.enums.RPGCoreSubcommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class RPGCoreSubcommandArgument implements CustomArgumentType.Converted<RPGCoreSubcommand, String> {

    @Override
    public RPGCoreSubcommand convert(String nativeType) throws CommandSyntaxException {
        try {
            return RPGCoreSubcommand.valueOf(nativeType.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException ignored) {
            Message message = MessageComponentSerializer.message().serialize(Component.text("Unknown subcommand %s!".formatted(nativeType), NamedTextColor.RED));

            throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
        }
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (RPGCoreSubcommand subcommand : RPGCoreSubcommand.values()) {
            CommandSourceStack css = (CommandSourceStack) context.getSource();
            if (css.getSender().hasPermission(subcommand.permission(subcommand)) || css.getSender().hasPermission("rpgcore.admin")) {
                builder.suggest(subcommand.name().toLowerCase());
            }
        }

        return builder.buildFuture();
    }
}
