/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.commands;

/**
 * An implementation of command handler for command "prof"
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class CommandHandler extends AbstractCommandHandler {

    public static final String COMMAND = "prof";
    public static final String EXTENDED_COMMAND = COMMAND.concat("-");


    @Override
    protected String getCommandExecutorName() {
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
        registerCommand(new TestCommand());
        registerCommand(new GenerateDefaultConfigCommand());
        registerCommand(new EditTraitCommand());
    }
}
