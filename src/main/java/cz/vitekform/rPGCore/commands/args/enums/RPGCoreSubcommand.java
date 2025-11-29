package cz.vitekform.rPGCore.commands.args.enums;

import org.jetbrains.annotations.NotNull;

public enum RPGCoreSubcommand {

    HELP,
    UPDATE,
    VERSION,
    SUMMON,
    TEST,
    CLASS,
    ATTRIBUTES,
    SKILLS,
    LEVEL,
    KIT_ADVENTURER;

    public @NotNull String permission(RPGCoreSubcommand subcommand) {
        return "rpgcore." + subcommand.name().toLowerCase();
    }
}

