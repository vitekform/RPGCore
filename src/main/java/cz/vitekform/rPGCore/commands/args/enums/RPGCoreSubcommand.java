package cz.vitekform.rPGCore.commands.args.enums;

import org.jetbrains.annotations.NotNull;

public enum RPGCoreSubcommand {

    HELP,
    UPDATE,
    VERSION,
    SUMMON,
    TEST,
    KIT_ADVENTURER;

    public @NotNull String permission(RPGCoreSubcommand subcommand) {
        return "rpgcore." + subcommand.name().toLowerCase();
    }
}

