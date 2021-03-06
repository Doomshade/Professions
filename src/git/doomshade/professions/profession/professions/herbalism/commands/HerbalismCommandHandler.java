package git.doomshade.professions.profession.professions.herbalism.commands;

import git.doomshade.professions.commands.AbstractCommandHandler;
import git.doomshade.professions.commands.CommandHandler;

public class HerbalismCommandHandler extends AbstractCommandHandler {
    @Override
    protected String getCommandExecutorName() {
        return CommandHandler.EXTENDED_COMMAND.concat("herbalism");
    }

    @Override
    public void registerCommands() {
        registerCommand(new AddCommand());
        registerCommand(new SpawnCommand());
        registerCommand(new DespawnCommand());
        registerCommand(new ScheduleSpawnCommand());
    }
}
