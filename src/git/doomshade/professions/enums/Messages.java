package git.doomshade.professions.enums;

import com.google.common.collect.Sets;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.Profession.ProfessionType;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.placeholder.PlaceholderManager;
import git.doomshade.professions.profession.professions.alchemy.AlchemyProfession;
import git.doomshade.professions.profession.professions.enchanting.EnchantingProfession;
import git.doomshade.professions.profession.professions.herbalism.HerbalismProfession;
import git.doomshade.professions.profession.professions.jewelcrafting.JewelcraftingProfession;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.ISetup;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * The messages manager that loads and retrieves messages from a lang file
 *
 * @author Doomshade
 * @version 1.0
 */
@SuppressWarnings("ALL")
public class Messages implements ISetup {

    private static final Messages instance = new Messages();
    private static FileConfiguration lang;

    private Messages() {
    }

    public static Messages getInstance() {
        return instance;
    }


    /**
     * Collection builder for adding multiple collections
     *
     * @param <T> the collection type
     */
    private static class CollectionBuilder<T> {
        private final Collection<T> collection;

        private CollectionBuilder(Collection<T> defaultCollection) {
            this.collection = defaultCollection;
        }

        private CollectionBuilder<T> add(Collection<T> another) {
            this.collection.addAll(another);
            return this;
        }

        private CollectionBuilder<T> add(T[] array) {
            return add(Arrays.asList(array));
        }


        private Collection<T> build() {
            return collection;
        }
    }

    @Override
    public void setup() {
        lang = Settings.getLang();

        // current lang
        final Set<String> propertyNames = lang.getKeys(false);

        // all lang keys
        final Collection<MessagesHolder> allKeys = new CollectionBuilder<MessagesHolder>(new HashSet<>())
                .add(HerbalismMessages.values())
                .add(EnchantingMessages.values())
                .add(AlchemyMessages.values())
                .add(JewelcraftingMessages.values())
                .add(Global.values())
                .build();

        // the disjoint set
        final Sets.SetView<String> missing = Sets.difference(
                allKeys.stream()
                        .map(MessagesHolder::getKey)
                        .collect(Collectors.toSet()),
                propertyNames
        );

        // there are some missing keys in the current lang, inform about that and add defaults
        if (!missing.isEmpty()) {
            try {
                ProfessionLogger.log("Your language file is outdated!", Level.WARNING);
                ProfessionLogger.log("Adding missing properties to your file. (" + missing + ")");
                missing.forEach(x -> lang.addDefault(x, ""));
                lang.options().copyDefaults(true);
                lang.save(Settings.getLangFile());
            } catch (IOException e) {
                ProfessionLogger.logError(e);
            }
        }
    }

    /**
     * Messages for {@link JewelcraftingProfession}
     */
    public enum JewelcraftingMessages implements MessagesHolder {
        INVALID_ITEM("invalid-item"),
        NO_GEM_SPACE("no-space-for-gem"),
        ADDED_GEM_SUCCESSFUL("added-gem-successful"),
        CLICK_ON_ITEM_WITH_GEM_SLOT("click-on-gem-with-gem-slot");

        private final String key;

        JewelcraftingMessages(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }
    }

    /**
     * Messages for {@link EnchantingProfession}
     */
    public enum EnchantingMessages implements MessagesHolder {
        LOW_ITEM_LEVEL("item-level-too-low");

        private final String key;

        EnchantingMessages(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }
    }

    /**
     * Messages for {@link AlchemyProfession}
     */
    public enum AlchemyMessages implements MessagesHolder {
        POTION_ALREADY_ACTIVE("potion-already-active");

        private final String key;

        AlchemyMessages(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

    }

    /**
     * Messages for {@link HerbalismProfession}
     */
    public enum HerbalismMessages implements MessagesHolder {
        GATHERING_CANCELLED_BY_DAMAGE("gathering-cancelled-by-damage"),
        GATHERING_CANCELLED_BY_MOVEMENT("gathering-cancelled-by-movement");

        private final String key;

        HerbalismMessages(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

    }

    /**
     * Global messages for all {@link Profession}s
     */
    public enum Global implements MessagesHolder {
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
        NOT_PROFESSED("not-professed"),
        PROFTYPE_PRIMARY("proftype-primary"),
        PROFTYPE_SECONDARY("proftype-secondary"),
        PROFESSION_REQUIRED_FOR_THIS_ACTION("profession-required-for-this-action"),
        NO_ITEM_IN_HAND("no-item-in-hand");


        private final String key;

        Global(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }
    }

    private enum Pattern {
        P_PROFESSION("prof"),
        P_PLAYER("player"),
        P_EXP("exp"),
        P_LEVEL("level"),
        P_PROFESSION_TYPE("proftype"),
        P_PROFESSION_NO_COLOR("prof_no_color"),
        P_ITEM("item");

        private final String pattern;

        Pattern(String pattern) {
            this.pattern = pattern;
        }
    }

    public static class MessageBuilder {
        private String message = "";
        private final Map<Pattern, String> replacements = new HashMap<>();
        private Player player = null;

        public MessageBuilder() {
        }

        public MessageBuilder(MessagesHolder message) {
            message(message);
        }

        private MessageBuilder replace(Pattern regex, String replacement) {
            replacements.put(regex, replacement);
            return this;
        }

        public MessageBuilder message(MessagesHolder m) {
            this.message = Messages.lang.getString(m.getKey());
            return this;
        }

        public MessageBuilder profession(Profession prof) {
            professionType(prof.getProfessionType());
            replace(Pattern.P_PROFESSION_NO_COLOR, ChatColor.stripColor(prof.getColoredName()));
            return replace(Pattern.P_PROFESSION, prof.getColoredName());
        }

        public MessageBuilder professionType(ProfessionType type) {
            return replace(Pattern.P_PROFESSION_TYPE, type.toString());
        }

        public MessageBuilder player(User user) {
            return player(user.getPlayer());
        }

        public MessageBuilder player(Player player) {
            this.player = player;
            return replace(Pattern.P_PLAYER, player.getDisplayName());
        }

        public MessageBuilder exp(double exp) {
            return replace(Pattern.P_EXP, String.valueOf(exp));
        }

        public MessageBuilder exp(int exp) {
            return exp((double) exp);
        }

        public MessageBuilder level(int level) {
            return replace(Pattern.P_LEVEL, String.valueOf(level));
        }

        public MessageBuilder itemType(ItemType<?> itemType) {
            return replace(Pattern.P_ITEM, itemType.getName());
        }

        public MessageBuilder userProfessionData(UserProfessionData upd) {
            profession(upd.getProfession());
            player(upd.getUser());
            return this;
        }

        public String build() {
            if (message.isEmpty()) {
                return "No message set";
            }

            if (PlaceholderManager.usesPlaceholders()) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }

            // don't delete this as the PlaceholderAPI extension does not have to be necessarily available
            for (Entry<Pattern, String> e : replacements.entrySet()) {
                message = message.replaceAll("\\{" + e.getKey().pattern + "}", e.getValue());
            }
            return ChatColor.translateAlternateColorCodes('&', message);
        }
    }
}
