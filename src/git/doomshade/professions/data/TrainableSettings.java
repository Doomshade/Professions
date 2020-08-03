package git.doomshade.professions.data;

import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.profession.Profession;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.user.User;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings for trainable item types
 *
 * @author Doomshade
 * @version 1.0
 */
public class TrainableSettings extends AbstractProfessionSettings {
    private static final String TRAINABLE_SECTION = "trainable",
            TRAINED = "trained",
            NOT_TRAINED = "not-trained",
            CANNOT_TRAIN = "cannot-train";

    private final ArrayList<String> trainedLore, notTrainedLore, unableToTrainLore;

    TrainableSettings() {
        trainedLore = new ArrayList<>();
        notTrainedLore = new ArrayList<>();
        unableToTrainLore = new ArrayList<>();
    }

    @Override
    public void setup() throws ConfigurationException {
        super.setup();
        ConfigurationSection section = getDefaultSection();
        ConfigurationSection trainableSection = section.getConfigurationSection(TRAINABLE_SECTION);
        if (trainableSection != null) {
            initLore(trainableSection, TRAINED, trainedLore);
            initLore(trainableSection, NOT_TRAINED, notTrainedLore);
            initLore(trainableSection, CANNOT_TRAIN, unableToTrainLore);
        } else {
            printError(TRAINABLE_SECTION, null);
        }
    }

    private void initLore(ConfigurationSection trainableSection, String section, ArrayList<String> lore) {
        if (trainableSection.isList(section)) {
            lore.addAll(trainableSection.getStringList(section));
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
            }
        } else {
            printError(TRAINABLE_SECTION + "." + section, null);
        }
    }

    @Override
    public void cleanup() {
        trainedLore.clear();
        notTrainedLore.clear();
    }

    public List<String> calculateAdditionalLore(ItemType<?> itemType, User user, Profession profession) {
        if (!user.hasProfession(profession)) {
            return getUnableToTrainLore(itemType);
        } else if (user.getProfessionData(profession).hasTrained(itemType)) {
            return getTrainedLore(itemType);
        } else {
            return getNotTrainedLore(itemType);
        }

    }

    private List<String> getNotTrainedLore(ItemType<?> itemType) {
        return replaceStrings(itemType, notTrainedLore);
    }

    private List<String> getTrainedLore(ItemType<?> itemType) {
        return replaceStrings(itemType, trainedLore);
    }


    private List<String> getUnableToTrainLore(ItemType<?> itemType) {
        return replaceStrings(itemType, unableToTrainLore);
    }

    @NotNull
    private List<String> replaceStrings(ItemType<?> itemType, List<String> lore) {
        return ItemUtils.getDescription(itemType, lore);
    }
}
