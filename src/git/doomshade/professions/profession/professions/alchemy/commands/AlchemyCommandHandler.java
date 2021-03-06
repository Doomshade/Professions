package git.doomshade.professions.profession.professions.alchemy.commands;

import git.doomshade.professions.commands.AbstractCommandHandler;
import git.doomshade.professions.commands.CommandHandler;

public class AlchemyCommandHandler extends AbstractCommandHandler {
    @Override
    protected String getCommandExecutorName() {
        return CommandHandler.EXTENDED_COMMAND.concat("alchemy");
    }

    @Override
    public void registerCommands() {
        registerCommand(new PotionsCommand());
    }
}
