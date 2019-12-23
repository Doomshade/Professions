package git.doomshade.professions.profession.types.gathering.herbalism.commands;

import git.doomshade.professions.commands.AbstractCommandHandler;

public class HerbalismCommandHandler extends AbstractCommandHandler {
    @Override
    protected String getCommandName() {
        return "prof-herbalism";
    }

    @Override
    public void registerCommands() {
        registerCommand(new SpawnCommand());
    }
}
