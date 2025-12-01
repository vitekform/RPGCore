package cz.vitekform.rPGCore.commands.args.classes;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import cz.vitekform.rPGCore.ItemDictionary;
import cz.vitekform.rPGCore.RPGCore;
import cz.vitekform.rPGCore.commands.args.enums.RPGCoreSubcommand;
import cz.vitekform.rPGCore.objects.RPGItem;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class RPGiveSubcommandArgument implements CustomArgumentType.Converted<String, String> {

    @Override
    public String convert(String nativeType) throws CommandSyntaxException {
        if (ItemDictionary.items.containsKey(nativeType)) {
            return nativeType;
        }
        Message message = MessageComponentSerializer.message().serialize(Component.text("Unknown subcommand %s!".formatted(nativeType), NamedTextColor.RED));
        throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> itemIDs = ItemDictionary.items.keySet().stream().toList();
        for (String s : itemIDs) {
            builder.suggest(s);
        }

        return builder.buildFuture();
    }
}
