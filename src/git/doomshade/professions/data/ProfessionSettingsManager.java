package git.doomshade.professions.data;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.utils.Utils;
import org.apache.commons.lang3.SerializationUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.logging.Level;

public final class ProfessionSettingsManager extends AbstractSettings {
    private static final HashSet<AbstractProfessionSpecificSettings> SETTINGS = new HashSet<>();
    private final Profession<?> profession;

    public ProfessionSettingsManager(Profession<?> profession) {
        this.profession = profession;
    }

    public <T extends AbstractProfessionSpecificSettings> T getSettings(Class<T> settingsClass) {

        T theSettings = null;
        try {
            theSettings = (T) SerializationUtils.clone(Utils.findInIterable(SETTINGS, x -> x.getClass().getName().equals(settingsClass.getName())));
        } catch (Utils.SearchNotFoundException e) {
            try {
                theSettings = settingsClass.getDeclaredConstructor(Profession.class).newInstance(profession);
                theSettings.setup();
                Professions.getInstance().registerSetup(theSettings);
                SETTINGS.add(theSettings);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                ex.printStackTrace();
            } catch (ConfigurationException ex) {
                Professions.log("Could not load " + settingsClass.getSimpleName() + " settings!", Level.WARNING);
                ex.printStackTrace();
            }

        }

        return theSettings;
    }

    @Override
    public void setup() {
        SETTINGS.add(new ProfessionSpecificDropSettings(profession));

        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
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
