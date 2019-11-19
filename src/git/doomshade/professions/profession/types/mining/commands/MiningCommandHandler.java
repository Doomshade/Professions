package git.doomshade.professions.profession.types.mining.commands;

import git.doomshade.professions.commands.AbstractCommandHandler;

/**
 * Custom command handler for mining
 *
 * @author Doomshade
 */
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
