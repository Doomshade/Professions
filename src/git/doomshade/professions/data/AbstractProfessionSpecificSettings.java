package git.doomshade.professions.data;

import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.profession.Profession;
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
        this.loader = YamlConfiguration.loadConfiguration(profession.getFile());
    }

    @Override
    protected ConfigurationSection getDefaultSection() {
        return loader;
    }

    public final Profession getProfession() {
        return profession;
    }

    final void register(ProfessionSettingsManager manager) throws ConfigurationException {
        manager.register(this);
    }

    @Override
    public String getSetupName() {
        return profession.getColoredName() + ChatColor.RESET + " settings";
    }
}
