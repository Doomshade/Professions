package git.doomshade.professions.data;

import git.doomshade.professions.api.Profession;
import git.doomshade.professions.io.IOManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Serializable;

/**
 * Profession specific settings. This class implements {@link Serializable} - all fields and inner classes MUST be {@link Serializable} or {@code transient}, too!
 *
 * @author Doomshade
 * @version 1.0
 */
public abstract class AbstractProfessionSpecificSettings extends AbstractSettings {

    private transient final Profession profession;
    private transient final FileConfiguration loader;

    /**
     * The default constructor of settings
     *
     * @param profession the profession
     */
    AbstractProfessionSpecificSettings(Profession profession) {
        this.profession = profession;
        this.loader = YamlConfiguration.loadConfiguration(IOManager.getProfessionFile(profession));
    }

    @Override
    protected ConfigurationSection getDefaultSection() {
        return loader;
    }

    public final Profession getProfession() {
        return profession;
    }

    @Override
    public String getSetupName() {
        return profession.getColoredName() + ChatColor.RESET + " settings";
    }
}
