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
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ISetup;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.Serializable;
import java.util.logging.Level;

/**
 * Plugin specific settings. This class implements {@link Serializable} - all fields and inner classes MUST be {@link
 * Serializable}, too!
 *
 * @author Doomshade
 * @version 1.0
 */
public abstract class AbstractSettings implements ISetup, Serializable {
    transient static final Level LEVEL = Level.WARNING;
    protected transient static FileConfiguration config;
    private final transient static Professions plugin = Professions.getInstance();
    transient static boolean outdated = false;

    static {
        loadConfig();
    }

    AbstractSettings() {
    }

    private static void loadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    protected void printError(String section, Object value) {
        if (!outdated) {
            ProfessionLogger.log("Your configuration file is outdated!", LEVEL);
            outdated = true;
        }
        ProfessionLogger.log(String.format("Missing \"%s\" section!", section), LEVEL);
        if (value == null) {
            ProfessionLogger.log("Using default values.", LEVEL);
        } else {
            ProfessionLogger.log(String.format("Using %s as default value.", value), LEVEL);
        }
    }

    protected final boolean isSection(String section, Object value) {
        boolean isSection = getDefaultSection().isConfigurationSection(section);
        if (!isSection) {
            printError(getDefaultSection().getCurrentPath() + "." + section, value);
        }
        return isSection;
    }

    private void assertSectionExists() throws ConfigurationException {
        if (getDefaultSection() == null) {
            throw new ConfigurationException();
        }
    }

    @Override
    public void setup() throws ConfigurationException {
        loadConfig();
        assertSectionExists();
    }

    @Override
    public String getSetupName() {
        return "settings";
    }

    protected ConfigurationSection getDefaultSection() {
        return config;
    }


    protected final boolean isSection(String section) {
        return isSection(section, null);
    }


}
