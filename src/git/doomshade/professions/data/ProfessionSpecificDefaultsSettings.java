package git.doomshade.professions.data;

import git.doomshade.professions.api.Profession;
import git.doomshade.professions.dynmap.MarkerManager;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * Class for {@link Profession} defaults
 *
 * @author Doomshade
 * @version 1.0
 */
public class ProfessionSpecificDefaultsSettings extends AbstractProfessionSpecificSettings implements Cloneable {
    private static final String
            SECTION = "defaults",
            NAME = "name",
            ICON = "icon",
            TYPE = "type",
            MARKER_SET_ID = "dynmap-label",
            SHOW_ON_DYNMAP = "show-on-dynmap";

    private String name;
    private String markerSetId = MarkerManager.EMPTY_MARKER_SET_ID;
    private boolean showOnDynmap = false;
    private ItemStack icon = ItemUtils.itemStackBuilder(Material.CHEST)
            .withLore(Arrays.asList("The", "Lore"))
            .withDisplayName("&aThe display name")
            .build();
    private Profession.ProfessionType professionType = Profession.ProfessionType.PRIMARY;

    /**
     * The default constructor of settings
     *
     * @param profession the profession
     */
    ProfessionSpecificDefaultsSettings(Profession profession) {
        super(profession);
        this.name = profession.getClass().getSimpleName().replace("profession", "");
    }

    public String getMarkerSetId() {
        return markerSetId;
    }

    public boolean isShowOnDynmap() {
        return showOnDynmap;
    }

    public String getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public Profession.ProfessionType getProfessionType() {
        return professionType;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ProfessionSpecificDefaultsSettings clone = (ProfessionSpecificDefaultsSettings) super.clone();
        try {
            clone.setup();
        } catch (ConfigurationException e) {
            ProfessionLogger.logError(e);
        }
        return clone;

    }

    @Override
    public void setup() throws ConfigurationException {
        super.setup();
        ConfigurationSection section = getDefaultSection();

        this.name = section.getString(NAME);
        try {
            this.icon = ItemUtils.deserialize(
                    Objects.requireNonNull(section.getConfigurationSection(ICON)).getValues(false), false);
        } catch (InitializationException e) {
            ProfessionLogger.logError(e, false);
        }
        this.professionType = Profession.ProfessionType.fromString(section.getString(TYPE));
        this.showOnDynmap = section.getBoolean(SHOW_ON_DYNMAP);
        this.markerSetId = section.getString(MARKER_SET_ID);
    }

    @Override
    protected ConfigurationSection getDefaultSection() {
        FileConfiguration section = (FileConfiguration) super.getDefaultSection();

        if (section == null) {
            return null;
        }

        ConfigurationSection actualSection = section.isConfigurationSection(SECTION) ?
                Objects.requireNonNull(section.getConfigurationSection(SECTION)) : section.createSection(SECTION);
        actualSection.addDefault(NAME, name);
        actualSection.addDefault(ICON, ItemUtils.serialize(icon));
        actualSection.addDefault(TYPE, professionType.name());
        actualSection.addDefault(MARKER_SET_ID, markerSetId);
        actualSection.addDefault(SHOW_ON_DYNMAP, showOnDynmap);
        section.options().copyDefaults(true);

        return actualSection;

    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s]", name, icon.toString(), professionType.toString());
    }
}
