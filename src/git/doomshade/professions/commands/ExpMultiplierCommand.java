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

import git.doomshade.professions.data.ExpSettings;
import git.doomshade.professions.data.Settings;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 * @deprecated since 1.0, not used
 */
@SuppressWarnings("ALL")
@Deprecated
public class ExpMultiplierCommand extends AbstractCommand {

    public ExpMultiplierCommand() {
        setArg(true, "multiplier", "skillapi/professions");
        setCommand("exp-multiplier");
        setDescription("Sets the exp multiplier. (Default 1)");
        setRequiresPlayer(false);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        double expMultiplier = 1;
        try {
            expMultiplier = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("not afucking number moron");
        }
        ExpSettings settings = Settings.getSettings(ExpSettings.class);
        switch (args[2].toLowerCase()) {
            case "skillapi":
                settings.setSkillapiExpMultiplier(expMultiplier);
                break;
            case "professions":
                settings.setExpMultiplier(expMultiplier);
                break;
        }

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "exp-multiplier";
    }

}
