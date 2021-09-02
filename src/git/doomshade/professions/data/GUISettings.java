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

import git.doomshade.professions.exceptions.ConfigurationException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

/**
 * GUI settings
 *
 * @author Doomshade
 * @version 1.0
 * @see git.doomshade.professions.commands.PlayerGuiCommand
 * @since 1.0
 */
public class GUISettings extends AbstractSettings {
    private static final String KEY_SECTION = "gui",
            KEY_PROFESSIONS_GUI_NAME = "professions-gui-name",
            KEY_LEVEL_THRESHOLD = "show-level-threshold",
            KEY_INFORMATION_SIGN_NAME = "information-sign-name";
    private static final int DEFAULT_LEVEL_THRESHOLD = 3;
    private String professionsGuiName = "Professions", signName = "Information";
    private int levelThreshold = DEFAULT_LEVEL_THRESHOLD;

    GUISettings() {
        super();
    }

    // TODO update gui settings on reload (have to add a method in GUIApi)
    @Override
    public void setup() throws ConfigurationException {
        super.setup();
        ConfigurationSection section = getDefaultSection();

        professionsGuiName = setupString(section, KEY_PROFESSIONS_GUI_NAME, professionsGuiName);
        signName = setupString(section, KEY_INFORMATION_SIGN_NAME, signName);

        if (!section.isInt(KEY_LEVEL_THRESHOLD)) {
            printError(KEY_LEVEL_THRESHOLD, DEFAULT_LEVEL_THRESHOLD);
        } else {
            this.levelThreshold = section.getInt(KEY_LEVEL_THRESHOLD);
        }
    }

    @Override
    protected ConfigurationSection getDefaultSection() {
        ConfigurationSection section = super.getDefaultSection();

        if (!section.isConfigurationSection(KEY_SECTION)) {
            super.printError(KEY_SECTION, null);
            return null;
        }

        return section.getConfigurationSection(KEY_SECTION);
    }

    @Override
    public String getSetupName() {
        return "GUI " + super.getSetupName();
    }

    @Override
    protected void printError(String section, Object value) {
        super.printError(KEY_SECTION + "." + section, value);
    }

    private String setupString(ConfigurationSection section, String path, String defaultName) {

        String str = "";
        if (!section.isString(path)) {
            printError(path, defaultName);
        } else {
            str = section.getString(path);
        }
        if (!Objects.requireNonNull(str).isEmpty()) {
            str = ChatColor.translateAlternateColorCodes('&', str);
        }
        return str;
    }

    public String getProfessionsGuiName() {
        return professionsGuiName;
    }

    public int getLevelThreshold() {
        return levelThreshold;
    }

    public String getSignName() {
        return signName;
    }
}
