package git.doomshade.professions.commands;

import git.doomshade.professions.data.ExpSettings;
import git.doomshade.professions.data.Settings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

@Deprecated
public class ExpMultiplierCommand extends AbstractCommand {

    public ExpMultiplierCommand() {
        setArg(true, Arrays.asList("multiplier", "skillapi/professions"));
        setCommand("exp-multiplier");
        setDescription("Sets the exp multiplier. (Default 1)");
        setRequiresPlayer(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
            case "git/doomshade/professions":
                settings.setExpMultiplier(expMultiplier);
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "exp-multiplier";
    }

}
