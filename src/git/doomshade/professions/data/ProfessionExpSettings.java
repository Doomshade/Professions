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

import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.exceptions.ConfigurationException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

/**
 * Profession specific exp settings
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class ProfessionExpSettings extends AbstractProfessionSettings {

    private static final String
            EXP_SETTINGS = "exp-settings",
            COLOR = "color",
            CHANCE = "chance",
            COLOR_CHANGE_AFTER = "color-change-after";

    ProfessionExpSettings() {
    }

    @Override
    public void setup() throws ConfigurationException {
        super.setup();
        ConfigurationSection section = getDefaultSection();
        ConfigurationSection expSection = section.getConfigurationSection(EXP_SETTINGS);
        if (expSection != null) {
            for (String key : expSection.getKeys(false)) {
                ConfigurationSection colorSection = expSection.getConfigurationSection(key);
                for (SkillupColor skillupColor : SkillupColor.values()) {
                    if (skillupColor.name().equalsIgnoreCase(key)) {
                        skillupColor.setSkillupColor(
                                ChatColor.getByChar(Objects.requireNonNull(colorSection.getString(COLOR)).charAt(0)),
                                colorSection.getInt(COLOR_CHANGE_AFTER),
                                colorSection.getDouble(CHANCE)
                        );
                    }
                }
            }
        } else {
            printError(EXP_SETTINGS, null);
        }
    }
}
