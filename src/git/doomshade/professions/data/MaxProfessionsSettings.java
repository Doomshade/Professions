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

import git.doomshade.professions.api.profession.ProfessionType;
import git.doomshade.professions.exceptions.ConfigurationException;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Settings for max amount of primary/secondary professions
 * <p>Will be moved later to {@link Settings}</p>
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class MaxProfessionsSettings extends AbstractProfessionSettings {

	private int maxPrimaryProfessions = 1, maxSecondaryProfessions = 1;

	MaxProfessionsSettings() {
	}

	@Override
	public void setup() throws ConfigurationException {

		super.setup();
		final String s = "max-professions";

		ConfigurationSection section = getDefaultSection();

		ConfigurationSection maxSection = section.getConfigurationSection(s);
		if (maxSection != null) {
			this.maxPrimaryProfessions = maxSection.getInt("primary", 1);
			this.maxSecondaryProfessions = maxSection.getInt("secondary", 1);
		} else {
			printError(s, 1);
		}
	}

	public int getMaxProfessions(ProfessionType type) {
		return type == ProfessionType.PRIMARY ? maxPrimaryProfessions : maxSecondaryProfessions;
	}
}
