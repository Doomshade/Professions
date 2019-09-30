package git.doomshade.professions.data;

import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.profession.types.Trainable;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class ProfessionSettings extends Settings {
    private static final String SECTION = "profession", LEVEL_THRESHOLD = "level-threshold",
            TRAINABLE_SECTION = "trainable", TRAINED = "trained", NOT_TRAINED = "not-trained", EXP_SETTINGS = "exp-settings", COLOR = "color", CHANCE = "chance", COLOR_CHANGE_AFTER = "color-change-after";
    private int levelThreshold = 3;
    private List<String> trainedLore, notTrainedLore;

    ProfessionSettings() {

    }

    @Override
    public void setup() throws Exception {
        trainedLore = notTrainedLore = new ArrayList<>();
        if (isSection(SECTION)) {
            ConfigurationSection section = config.getConfigurationSection(SECTION);
            ConfigurationSection trainableSection = section.getConfigurationSection(TRAINABLE_SECTION);
            if (trainableSection != null) {
                levelThreshold = trainableSection.getInt(LEVEL_THRESHOLD);
                if (trainableSection.isList(TRAINED)) {
                    trainedLore = trainableSection.getStringList(TRAINED);
                    for (int i = 0; i < trainedLore.size(); i++) {
                        trainedLore.set(i, ChatColor.translateAlternateColorCodes('&', trainedLore.get(i)));
                    }
                } else {
                    printError(SECTION + "." + TRAINABLE_SECTION + "." + TRAINED, null);
                }
                if (trainableSection.isList(NOT_TRAINED)) {
                    notTrainedLore = trainableSection.getStringList(NOT_TRAINED);
                    for (int i = 0; i < notTrainedLore.size(); i++) {
                        notTrainedLore.set(i, ChatColor.translateAlternateColorCodes('&', notTrainedLore.get(i)));
                    }
                } else {
                    printError(SECTION + "." + TRAINABLE_SECTION + "." + NOT_TRAINED, null);
                }
            } else {
                printError(SECTION + "." + TRAINABLE_SECTION, null);
            }

            ConfigurationSection expSection = section.getConfigurationSection(EXP_SETTINGS);
            if (expSection != null) {
                for (String key : expSection.getKeys(false)) {
                    ConfigurationSection colorSection = expSection.getConfigurationSection(key);
                    for (SkillupColor skillupColor : SkillupColor.values()) {
                        if (skillupColor.name().equalsIgnoreCase(key)) {
                            skillupColor.setSkillupColor(ChatColor.getByChar(colorSection.getString(COLOR).charAt(0)), colorSection.getInt(COLOR_CHANGE_AFTER), colorSection.getDouble(CHANCE));
                        }
                    }
                }
            } else {
                printError(SECTION + "." + EXP_SETTINGS, null);
            }
        }
    }

    public int getLevelThreshold() {
        return levelThreshold;
    }

    public List<String> getNotTrainedLore(Trainable trainable) {
        for (int i = 0; i < notTrainedLore.size(); i++) {
            notTrainedLore.set(i, notTrainedLore.get(i).replaceAll(Trainable.VAR_TRAINABLE_COST, String.valueOf(trainable.getCost())));
        }
        return notTrainedLore;
    }

    public List<String> getTrainedLore(Trainable trainable) {
        for (int i = 0; i < trainedLore.size(); i++) {
            trainedLore.set(i, trainedLore.get(i).replaceAll(Trainable.VAR_TRAINABLE_COST, String.valueOf(trainable.getCost())));
        }
        return trainedLore;
    }
}
