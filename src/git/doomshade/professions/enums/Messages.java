package git.doomshade.professions.enums;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Profession.ProfessionType;
import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.ISetup;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Messages implements ISetup {

    private static Messages instance;
    private static FileConfiguration loader;

    static {
        instance = new Messages();
    }

    private File languageFile;

    private Messages() {
    }

    public static Messages getInstance() {
        return instance;
    }

    @Override
    public void setup() throws Exception {
        languageFile = new File(Professions.getInstance().getDataFolder(), "messages.yml");
        if (!languageFile.exists()) {
            languageFile.createNewFile();
        }
        loader = YamlConfiguration.loadConfiguration(languageFile);
        for (Message m : Message.values()) {
            loader.addDefault(m.fileId, "");
        }
        loader.options().copyDefaults(true);
        List<String> patterns = new ArrayList<>();
        for (Pattern p : Pattern.values()) {
            patterns.add("{" + p.pattern + "}");
        }
        loader.set("patterns", patterns);
        loader.save(languageFile);
    }

    public enum Message {
        EXP_GAIN("exp-gain"),
        EXP_LOSE("exp-lose"),
        LEVEL_UP("level-up"),
        PROFESSION_DOESNT_EXIST("profession-doesnt-exist"),
        SUCCESSFULLY_PROFESSED("successfully-professed"),
        ALREADY_PROFESSED("already-professed"),
        ALREADY_PROFESSED_TYPE("already-professed-profession-type"),
        MAX_LEVEL_REACHED("max-level-reached"),
        SUCCESSFULLY_UNPROFESSED("successfully-unprofessed"),
        REQUIREMENTS_NOT_MET("requirements-not-met"),
        INVALID_REPEAT_AMOUNT("invalid-repeat-amount-input"),
        REPEAT_AMOUNT("repeat-amount-input"),
        NO_INVENTORY_SPACE("no-inventory-space"),
        SUCCESSFULLY_TRAINED("successfully-trained"),
        NOT_ENOUGH_MONEY_TO_TRAIN("not-enough-money-to-train"),
        NEW_ITEM_AVAILABLE("new-item-available");

        private final String fileId;

        Message(String fileId) {
            this.fileId = fileId;
        }

        public String getMessage() {
            return loader.getString(fileId);
        }
    }

    private enum Pattern {
        P_PROFESSION("prof"), P_PLAYER("player"), P_EXP("exp"), P_LEVEL("level"), P_PROFESSION_TYPE("proftype"), P_PROFESSION_NO_COLOR("prof_no_color"), P_ITEM("item");

        private final String pattern;

        Pattern(String pattern) {
            this.pattern = pattern;
        }
    }

    public static class MessageBuilder {
        private String message;
        private Map<Pattern, String> replacements;

        public MessageBuilder() {
            this.message = "";
            this.replacements = new HashMap<>();
        }

        public MessageBuilder(Message message) {
            this.message = message.getMessage();
            replacements = new HashMap<>();
        }

        private MessageBuilder replace(Pattern regex, String replacement) {
            replacements.put(regex, replacement);
            return this;
        }

        public MessageBuilder setMessage(Message m) {
            this.message = m.getMessage();
            return this;
        }

        public MessageBuilder setProfession(Profession<? extends IProfessionType> prof) {
            setProfessionType(prof.getProfessionType());
            replace(Pattern.P_PROFESSION_NO_COLOR, ChatColor.stripColor(prof.getColoredName()));
            return replace(Pattern.P_PROFESSION, prof.getColoredName());
        }

        public MessageBuilder setProfessionType(ProfessionType type) {
            return replace(Pattern.P_PROFESSION_TYPE, type.toString());
        }

        public MessageBuilder setPlayer(User user) {
            return setPlayer(user.getPlayer());
        }

        public MessageBuilder setPlayer(Player player) {
            return replace(Pattern.P_PLAYER, player.getDisplayName());
        }

        public MessageBuilder setExp(double exp) {
            return replace(Pattern.P_EXP, String.valueOf(exp));
        }

        public MessageBuilder setExp(int exp) {
            return replace(Pattern.P_EXP, String.valueOf(exp));
        }

        public MessageBuilder setLevel(int level) {
            return replace(Pattern.P_LEVEL, String.valueOf(level));
        }

        public MessageBuilder setItemType(ItemType<?> itemType) {
            return replace(Pattern.P_ITEM, itemType.getName());
        }

        public MessageBuilder setUserProfessionData(UserProfessionData upd) {
            setProfession(upd.getProfession());
            setPlayer(upd.getUser());
            return this;
        }

        public String build() {
            if (message.isEmpty()) {
                return message;
            }
            for (Entry<Pattern, String> e : replacements.entrySet()) {
                message = message.replaceAll("\\{" + e.getKey().pattern + "\\}", e.getValue());
            }
            return ChatColor.translateAlternateColorCodes('&', message);
        }
    }
}
