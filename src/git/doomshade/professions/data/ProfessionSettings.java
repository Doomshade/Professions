package git.doomshade.professions.data;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * Class for custom profession settings
 */
public class ProfessionSettings extends AbstractProfessionSettings {
    private static final HashMap<Class<? extends Profession>, ProfessionDropSettings> DROP_SETTINGS = new HashMap<>();
    private final Profession<?> profession;
    private final String section;

    public ProfessionSettings(Profession<?> profession) {
        this.profession = profession;
        this.section = profession.getID();
    }

    @Override
    @Nullable
    protected ConfigurationSection getDefaultSection() {
        ConfigurationSection section = super.getDefaultSection();

        if (section == null) {
            return null;
        }

        if (section.isConfigurationSection(this.section)) {
            return section.getConfigurationSection(this.section);
        } else {
            printError(profession.getID(), null);
            return null;
        }
    }

    @Override
    protected void printError(String section, Object value) {
        super.printError(this.section + "." + section, value);
    }

    public final Profession<?> getProfession() {
        return profession;
    }

    public final ProfessionDropSettings getDropSettings(UserProfessionData upd, ItemType<?> itemType) {
        ProfessionDropSettings settings;

        if (!DROP_SETTINGS.containsKey(profession.getClass())) {
            settings = new ProfessionDropSettings(profession);
            settings.setup();
            Professions.getInstance().registerSetup(settings);
            DROP_SETTINGS.put(profession.getClass(), settings);
        }

        settings = DROP_SETTINGS.get(profession.getClass());
        settings.setItem(itemType);
        settings.setUpd(upd);
        return settings;
    }

    @Override
    public void setup() throws Exception {

    }
}
