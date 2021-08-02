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

import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Class for {@link Profession} drop settings.
 *
 * @author Doomshade
 * @version 1.0
 */
public class ProfessionSpecificDropSettings extends AbstractProfessionSpecificSettings {
    private transient static final String SECTION = "drop", INCREMENT_BY = "increment-by", INCREMENT_SINCE =
            "increment-since";
    private final LinkedList<Drop> DROPS = new LinkedList<>();

    /**
     * The default constructor of settings
     *
     * @param profession the profession
     */
    ProfessionSpecificDropSettings(Profession profession) {
        super(profession);
    }

    /**
     * Gets the random drop amount from profession settings.
     *
     * @param upd  the user's profession data
     * @param item the item to base the formula around
     *
     * @return the drop amount
     *
     * @see Drop#getDropChance(int, int)
     */
    public int getDropAmount(UserProfessionData upd, ItemType<?> item) {

        return DROPS.stream()
                .filter(drop -> Math.random() < drop.getDropChance(upd.getLevel(), item.getLevelReq()))
                .findFirst()
                .map(drop -> drop.drop)
                .orElse(1);

    }

    @Override
    protected void printError(String section, Object value) {
        super.printError(SECTION + "." + section, value);
    }

    @Override
    protected ConfigurationSection getDefaultSection() {
        ConfigurationSection section = super.getDefaultSection();

        if (section == null) {
            return null;
        }

        if (section.isConfigurationSection(SECTION)) {
            return section.getConfigurationSection(SECTION);
        } else {
            ConfigurationSection sec = section.createSection(SECTION);
            for (int i = 2; i < 4; i++) {
                ConfigurationSection specificDropSec = sec.createSection(i + "");
                specificDropSec.set(INCREMENT_SINCE, 15);
                specificDropSec.set(INCREMENT_BY, 0.1);
            }

            return sec;
        }
    }

    @Override
    public void setup() throws ConfigurationException {
        super.setup();

        ConfigurationSection section = getDefaultSection();

        // add all drops to the list
        for (String s : section.getKeys(false)) {
            ConfigurationSection dropSection = section.getConfigurationSection(s);
            DROPS.add(new Drop(Integer.parseInt(s), Objects.requireNonNull(dropSection).getInt(INCREMENT_SINCE),
                    dropSection.getDouble(INCREMENT_BY)));
        }

        // sort the list in descending order
        DROPS.sort(Comparator.naturalOrder());
    }

    @Override
    public void cleanup() {
        DROPS.clear();
    }

    @Override
    public String getSetupName() {
        return getProfession().getColoredName() + ChatColor.RESET + " drop settings";
    }

    /**
     * Just a class to store 3 variables and a single compute method
     */
    private static class Drop implements Comparable<Drop>, Serializable {
        /**
         * The drop amount
         */
        private final int drop;

        /**
         * The level + updLevel to increment since
         */
        private final int incrementSince;

        /**
         * The % to increment by
         */
        private final double incrementBy;

        private Drop(int drop, int incrementSince, double incrementBy) {
            this.drop = drop;
            this.incrementBy = incrementBy / 100d;
            this.incrementSince = incrementSince;
        }

        /**
         * Returns the drop chance based on user's profession level, item's level requirement and the drop settings.
         * <br>The formula is:
         * <br>{@code Math.min(1d, Math.max(0d, (}{@link UserProfessionData#getLevel()} {@code -} ({@link
         * Drop#incrementSince} {@code +} {@link ItemType#getLevelReq()}{@code ))} {@code *} {@link
         * Drop#incrementBy}{@code ))}.
         *
         * @param updLevel     the user's profession level
         * @param itemLevelReq the item's level requirement
         *
         * @return the drop chance
         */
        private double getDropChance(int updLevel, int itemLevelReq) {
            return Math.min(1d, Math.max(0d, (updLevel - (incrementSince + itemLevelReq)) * incrementBy));
        }

        @Override
        public int compareTo(Drop o) {
            return Integer.compare(o.drop, drop);
        }

        @Override
        public String toString() {
            return String.format("Drop[drop=%d,incrementSince=%d,incrementBy=%f]", drop, incrementSince, incrementBy);
        }
    }


}
