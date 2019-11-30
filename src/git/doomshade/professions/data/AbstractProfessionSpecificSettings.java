package git.doomshade.professions.data;

import git.doomshade.professions.Profession;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Serializable;

/**
 * Profession specific settings manager.
 *
 * @author Doomshade
 */
public abstract class AbstractProfessionSpecificSettings extends AbstractSettings implements Serializable {

    private final Profession<?> profession;
    private final FileConfiguration file;

    public AbstractProfessionSpecificSettings(Profession<?> profession) {
        this.profession = profession;
        this.file = YamlConfiguration.loadConfiguration(profession.getFile());
    }

    @Override
    protected ConfigurationSection getDefaultSection() {
        return file;
    }

    public final Profession<?> getProfession() {
        return profession;
    }

    @Override
    public String getSetupName() {
        return profession.getColoredName() + ChatColor.RESET + " settings";
    }
}
