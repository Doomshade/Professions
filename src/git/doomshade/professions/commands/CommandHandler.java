package git.doomshade.professions.commands;

/**
 * @author Doomshade
 */
public class CommandHandler extends AbstractCommandHandler {

    public static final String COMMAND = "prof";
    public static final String EXTENDED_COMMAND = COMMAND.concat("-");


    @Override
    protected String getCommandName() {
        return COMMAND;
    }

    @Override
    public void registerCommands() {
        registerCommand(new ProfessCommand());
        registerCommand(new ReloadCommand());
        registerCommand(new ProfessionListCommand());
        registerCommand(new ProfessionInfoCommand());
        registerCommand(new UnprofessCommand());
        registerCommand(new BackupCommand());
        registerCommand(new SaveCommand());
        registerCommand(new AddExpCommand());
        //registerCommand(new ExpMultiplierCommand());
        registerCommand(new PlayerGuiCommand());
        registerCommand(new BypassCommand());
        registerCommand(new NormalizeLevelsCommand());
        registerCommand(new AddExtraCommand());
        registerCommand(new LevelCommand());
        registerCommand(new CommandsCommand());
        registerCommand(new GenerateDefaultsCommand());
        registerCommand(new LogFilterCommand());
        registerCommand(new EditItemTypeCommand());
        registerCommand(new UndoEditCommand());
    }
}
