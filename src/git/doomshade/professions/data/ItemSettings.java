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

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Item default settings for item types
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class ItemSettings extends AbstractSettings {
    private static final String SECTION = "items", LORE = "default-lore";
    private List<String> defaultLore = new ArrayList<>();

    ItemSettings() {

    }

    @Override
    public void setup() {
        if (!isSection(SECTION)) {
            return;
        }
        ConfigurationSection section = config.getConfigurationSection(SECTION);
        if (Objects.requireNonNull(section).isSet(LORE)) {
            this.defaultLore = section.getStringList(LORE);
        }
    }

    public List<String> getDefaultLore() {
        return defaultLore;
    }

    @Override
    public String getSetupName() {
        return "item " + super.getSetupName();
    }
}
