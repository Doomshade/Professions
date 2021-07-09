package git.doomshade.professions.data;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.utils.Utils;
import org.apache.commons.lang.SerializationUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;

/**
 * A profession settings manager
 *
 * @author Doomshade
 * @version 1.0
 */
public final class ProfessionSettingsManager extends AbstractSettings {
    private transient final HashSet<AbstractProfessionSpecificSettings> SETTINGS = new HashSet<>();
    private transient final Profession profession;

    public ProfessionSettingsManager(Profession profession) {
        this.profession = profession;
    }

    public <T extends AbstractProfessionSpecificSettings> T getSettings(Class<T> settingsClass) {

        T theSettings = null;
        try {
            AbstractProfessionSpecificSettings settings = Utils.findInIterable(SETTINGS, x -> x.getClass().getName().equals(settingsClass.getName()));
            if (settings instanceof Cloneable) {
                theSettings = (T) settings.getClass().getMethod("clone").invoke(settings);
            } else {
                theSettings = (T) SerializationUtils.clone(settings);
            }
        } catch (Utils.SearchNotFoundException e) {
            try {
                theSettings = settingsClass.getDeclaredConstructor(Profession.class).newInstance(profession);
                theSettings.setup();
                Professions.getInstance().registerSetup(theSettings);
                SETTINGS.add(theSettings);
            } catch (ConfigurationException ex) {
                Professions.log("Could not load " + settingsClass.getSimpleName() + " settings!" + "\n" + Arrays.toString(ex.getStackTrace()), Level.WARNING);
            } catch (Exception ex) {
                Professions.logError(ex);
            }
        } catch (Exception e) {
            Professions.logError(e);
        }

        return theSettings;
    }

    void register(AbstractProfessionSpecificSettings settings) throws ConfigurationException {
        settings.setup();
        SETTINGS.add(settings);
    }

    @Override
    public void setup() throws ConfigurationException {
        register(new ProfessionSpecificDropSettings(profession));
        register(new ProfessionSpecificDefaultsSettings(profession));

        try {
            save();
        } catch (IOException e) {
            Professions.logError(e);
        }
    }


    public void save() throws IOException {
        if (SETTINGS.isEmpty()) {
            throw new RuntimeException("Settings are empty! #save method was likely called before #setup method!");
        }
        FileConfiguration loader = YamlConfiguration.loadConfiguration(profession.getFile());
        for (AbstractProfessionSpecificSettings settings : SETTINGS) {
            loader.addDefaults(settings.getDefaultSection().getRoot());
        }
        loader.options().copyDefaults(true);
        loader.save(profession.getFile());
    }

    @Override
    public void cleanup() {
        SETTINGS.clear();
    }

    @Override
    public String getSetupName() {
        return profession.getColoredName() + ChatColor.RESET + " settings";
    }
}
