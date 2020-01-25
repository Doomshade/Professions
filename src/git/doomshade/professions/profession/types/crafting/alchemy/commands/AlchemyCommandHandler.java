package git.doomshade.professions.profession.types.crafting.alchemy.commands;

import git.doomshade.professions.commands.AbstractCommandHandler;
import git.doomshade.professions.commands.CommandHandler;

public class AlchemyCommandHandler extends AbstractCommandHandler {
    @Override
    protected String getCommandName() {
        return CommandHandler.EXTENDED_COMMAND.concat("alchemy");
    }

    @Override
    public void registerCommands() {
        registerCommand(new PotionsCommand());
    }
}
