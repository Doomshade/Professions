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

import git.doomshade.professions.api.profession.Profession;
import git.doomshade.professions.io.IOManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Serializable;

/**
 * Profession specific settings. This class implements {@link Serializable} - all fields and inner classes MUST be
 * {@link Serializable} or {@code transient}, too!
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractProfessionSpecificSettings extends AbstractSettings {

    private final transient Profession profession;
    private final transient FileConfiguration loader;

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
