package git.doomshade.professions.commands;

import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

/**
 * @author Doomshade
 * @version NOT_YET_IMPLEMENTED
 */
public class NormalizeLevelsCommand extends AbstractCommand {

    public NormalizeLevelsCommand() {
        setArg(true, "previous x", "previous y", "previous z");
        setCommand("normalize");
        setDescription(
                "(DOESN'T WORK YET) Normalizes levels based on XP this curve (use with caution, save users before using this command!)");
        setMessages(Arrays.asList("Normalized %d users."));
        setRequiresPlayer(false);
        addPermission(Permissions.ADMIN);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

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
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "normalizelevels";
    }

}
