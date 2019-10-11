package git.doomshade.professions.profession.types.mining.commands;

import git.doomshade.professions.commands.AbstractCommandHandler;

public class MiningCommandHandler extends AbstractCommandHandler {

    @Override
    protected String getCommandName() {
        return "prof-mining";
    }

    @Override
    public void registerCommands() {
        registerCommand(new EditCommand());
        registerCommand(new ReloadCommand());
    }
}
