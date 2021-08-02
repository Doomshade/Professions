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

import com.google.common.collect.ImmutableList;
import git.doomshade.professions.exceptions.ConfigurationException;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings of defaults in item types
 *
 * @author Doomshade
 * @version 1.0
 */
public class DefaultsSettings extends AbstractProfessionSettings {

    private static final String DEFAULTS = "defaults", SORTED_BY = "sorted-by";
    private List<String> sortedBy = new ArrayList<>();

    DefaultsSettings() {
    }

    @Override
    public void setup() throws ConfigurationException {
        super.setup();

        ConfigurationSection section = getDefaultSection();

        ConfigurationSection defaultsSection = section.getConfigurationSection(DEFAULTS);
        if (defaultsSection != null) {
            this.sortedBy = defaultsSection.getStringList(SORTED_BY);
        } else {
            printError(DEFAULTS, null);
        }
    }

    public List<String> getSortedBy() {
        return ImmutableList.copyOf(sortedBy);
    }

    @Override
    public void cleanup() {
        sortedBy = new ArrayList<>();
    }
}
