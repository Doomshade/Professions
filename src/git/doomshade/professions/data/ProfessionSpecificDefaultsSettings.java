package git.doomshade.professions.data;

import git.doomshade.professions.Profession;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Class for {@link Profession} defaults
 *
 * @author Doomshade
 */
public class ProfessionSpecificDefaultsSettings extends AbstractProfessionSpecificSettings implements Cloneable {
    private static final String SECTION = "defaults", NAME = "name", ICON = "icon", TYPE = "type";
    private String name = "Profession name";
    private ItemStack icon = ItemUtils.itemStackBuilder(Material.CHEST).withLore(Arrays.asList("The", "Lore")).withDisplayName("&aThe display name").build();
    private Profession.ProfessionType professionType = Profession.ProfessionType.PRIMARY;

    /**
     * The default constructor of settings
     *
     * @param profession the profession
     */
    ProfessionSpecificDefaultsSettings(Profession<?> profession) {
        super(profession);
    }

    @Override
    protected ConfigurationSection getDefaultSection() {
        FileConfiguration section = (FileConfiguration) super.getDefaultSection();

        if (section == null) {
            return null;
        }

        if (section.isConfigurationSection(SECTION)) {
            return section.getConfigurationSection(SECTION);
        }

        ConfigurationSection actualSection = section.createSection(SECTION);
        actualSection.set(NAME, name);
        actualSection.set(ICON, icon);
        actualSection.set(TYPE, professionType.name());
        return actualSection;

    }

    @Override
    public void setup() throws ConfigurationException {
        super.setup();
        ConfigurationSection section = getDefaultSection();

        this.name = section.getString(NAME);
        this.icon = section.getItemStack(ICON);
        this.professionType = Profession.ProfessionType.fromString(section.getString(TYPE));
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
            e.printStackTrace();
        }
        return clone;

    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s]", name, icon.toString(), professionType.toString());
    }
}