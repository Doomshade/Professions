package git.doomshade.professions.data;

import git.doomshade.professions.Profession;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Comparator;
import java.util.LinkedList;

public class ProfessionSpecificDropSettings extends AbstractProfessionSpecificSettings {
    private static final String SECTION = "drop", INCREMENT_BY = "increment-by", INCREMENT_SINCE = "increment-since";
    private final LinkedList<Drop> DROPS = new LinkedList<>();


    ProfessionSpecificDropSettings(Profession<?> profession) {
        super(profession);
    }

    public int getDropAmount(UserProfessionData upd, ItemType<?> item) {

        for (Drop drop : DROPS) {
            if (Math.random() < drop.getDropChance(upd.getLevel(), item.getLevelReq())) {
                return drop.drop;
            }
        }

        return 1;
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

        for (String s : section.getKeys(false)) {
            ConfigurationSection dropSection = section.getConfigurationSection(s);
            DROPS.add(new Drop(Integer.parseInt(s), dropSection.getInt(INCREMENT_SINCE), dropSection.getDouble(INCREMENT_BY)));
        }

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

    private static class Drop implements Comparable<Drop> {
        private final int drop, incrementSince;
        private final double incrementBy;

        private Drop(int drop, int incrementSince, double incrementBy) {
            this.drop = drop;
            this.incrementBy = incrementBy / 100d;
            this.incrementSince = incrementSince;
        }

        // TODO Outputting wrong values
        private double getDropChance(int updLevel, int itemLevelReq) {
            return Math.max(0d, (updLevel - incrementSince - itemLevelReq) * incrementBy);
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
