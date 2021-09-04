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

package git.doomshade.professions.data;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.profession.Profession;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.Utils;
import org.apache.commons.lang.SerializationUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A profession settings manager
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class ProfessionSettingsManager extends AbstractSettings {
    private transient final Set<AbstractProfessionSpecificSettings> settings = new HashSet<>();
    private transient final Profession profession;

    public ProfessionSettingsManager(Profession profession) {
        this.profession = profession;
    }

    public ProfessionSpecificDropSettings getDropSettings() {
        return getSettings(ProfessionSpecificDropSettings.class);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractProfessionSpecificSettings> T getSettings(Class<T> settingsClass) {

        T theSettings = null;
        try {
            AbstractProfessionSpecificSettings settings =
                    Utils.findInIterable(this.settings, x -> x.getClass().getName().equals(settingsClass.getName()));
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
                settings.add(theSettings);
            } catch (ConfigurationException ex) {
                ProfessionLogger.logError(ex, false);
                /*ProfessionLogger.log("Could not load " + settingsClass.getSimpleName() + " settings!" + "\n" +
                        Arrays.toString(ex.getStackTrace()), Level.WARNING);*/
            } catch (Exception ex) {
                ProfessionLogger.logError(ex);
            }
        } catch (Exception e) {
            ProfessionLogger.logError(e);
        }

        return theSettings;
    }

    public ProfessionSpecificDefaultsSettings getDefaultsSettings() {
        return getSettings(ProfessionSpecificDefaultsSettings.class);
    }

    @Override
    public void setup() throws ConfigurationException {
        register(new ProfessionSpecificDropSettings(profession));
        register(new ProfessionSpecificDefaultsSettings(profession));

        try {
            save();
        } catch (IOException e) {
            ProfessionLogger.logError(e);
        }
    }

    void register(AbstractProfessionSpecificSettings settings) throws ConfigurationException {
        settings.setup();
        this.settings.add(settings);
    }

    public void save() throws IOException {
        if (settings.isEmpty()) {
            throw new IOException("Settings are empty! #save method was likely called before #setup method!");
        }
        final File f = IOManager.getProfessionFile(profession);
        FileConfiguration loader = YamlConfiguration.loadConfiguration(f);
        for (AbstractProfessionSpecificSettings settings : settings) {
            loader.addDefaults(Objects.requireNonNull(settings.getDefaultSection().getRoot()));
        }
        loader.options().copyDefaults(true);
        loader.save(f);
    }

    @Override
    public String getSetupName() {
        return profession.getColoredName() + ChatColor.RESET + " settings";
    }

    @Override
    public void cleanup() {
        settings.clear();
    }
}
