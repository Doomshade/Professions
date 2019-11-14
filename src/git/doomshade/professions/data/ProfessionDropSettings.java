package git.doomshade.professions.data;

import git.doomshade.professions.Profession;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Comparator;
import java.util.LinkedList;

public class ProfessionDropSettings extends ProfessionSettings {
    private static final String SECTION = "drop", INCREMENT_BY = "increment-by", INCREMENT_SINCE = "increment-since";
    private final LinkedList<Drop> DROPS = new LinkedList<>();
    private UserProfessionData upd = null;
    private ItemType<?> item = null;

    ProfessionDropSettings(Profession<?> profession) {
        super(profession);
    }

    public UserProfessionData getUpd() {
        return upd;
    }

    public void setUpd(UserProfessionData upd) {
        this.upd = upd;
    }

    public ItemType<?> getItem() {
        return item;
    }

    public void setItem(ItemType<?> item) {
        this.item = item;
    }

    public int getDropAmount() {

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
            printError(SECTION, null);
            return null;
        }
    }

    @Override
    public void setup() {
        DROPS.clear();

        ConfigurationSection section = getDefaultSection();
        if (section == null) {
            return;
        }

        for (String s : section.getKeys(false)) {
            ConfigurationSection dropSection = section.getConfigurationSection(s);
            DROPS.add(new Drop(Integer.parseInt(s), dropSection.getInt(INCREMENT_SINCE), dropSection.getDouble(INCREMENT_BY)));
        }

        DROPS.sort(Comparator.naturalOrder());
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
