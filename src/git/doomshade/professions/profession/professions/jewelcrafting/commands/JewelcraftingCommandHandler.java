package git.doomshade.professions.profession.professions.jewelcrafting.commands;

import git.doomshade.professions.commands.AbstractCommandHandler;
import git.doomshade.professions.commands.CommandHandler;

public class JewelcraftingCommandHandler extends AbstractCommandHandler {
    @Override
    protected String getCommandExecutorName() {
        return CommandHandler.EXTENDED_COMMAND.concat("jc");
    }

    @Override
    public void registerCommands() {
        registerCommand(new GemEffectListCommand());
        registerCommand(new GiveCommand());
        registerCommand(new InsertCommand());
    }
}
