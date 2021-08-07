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

import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class NormalizeLevelsCommand extends AbstractCommand {

    public NormalizeLevelsCommand() {
        setArg(true, "previous x", "previous y", "previous z");
        setCommand("normalize");
        setDescription(
                "(DOESN'T WORK YET) Normalizes levels based on XP this curve (use with caution, save users before " +
                        "using this command!)");
        addMessages("Normalized %d users.");
        setRequiresPlayer(false);
        addPermission(Permissions.ADMIN);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

		/*
		int[] form = new int[3];
		for (int i = 1; i < args.length; i++) {
			form[i - 1] = Integer.parseInt(args[i]);
			if (i == 3) {
				break;
			}
		}
		ExpFormula previousFormula = new ExpFormula(form[0], form[1], form[2]);
		ExpFormula currentFormula = Settings.getInstance().getExpSettings().getExpFormula();
		File[] files = Professions.getInstance().getPlayerFolder().listFiles();
		for (File file : files) {
			FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
			User user = User.getUser(Bukkit.getPlayer(loader.getString(User.KEY_NAME)));
			if (user == null) {
				continue;
			}
			for (UserProfessionData upd : user.getProfessions().values()) {
				int currentExp = 0;
				int prevExp = 0;
				int level = upd.getLevel();
				for (int i = 1; i < level; i++) {
					currentExp += currentFormula.calculate(level);
					prevExp += previousFormula.calculate(level);
				}
				upd.addExp(prevExp - currentExp, null);
			}
		}

		sender.sendMessage(String.format(getMessages().get(0), files.length));*/
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "normalizelevels";
    }

}
