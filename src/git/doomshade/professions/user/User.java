package git.doomshade.professions.user;

import com.google.common.collect.ImmutableSet;
import git.doomshade.professions.Profession;
import git.doomshade.professions.Profession.ProfessionType;
import git.doomshade.professions.Professions;
import git.doomshade.professions.data.MaxProfessionsSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.crafting.alchemy.Potion;
import git.doomshade.professions.profession.types.crafting.alchemy.PotionTask;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Class representing a user
 *
 * @author Doomshade
 */
public final class User {

    private static final String KEY_NAME = "name";
    private static final String KEY_PROFESSIONS = "professions";
    private static final Map<UUID, User> USERS = new HashMap<>();
    private final Player player;
    private FileConfiguration loader;
    private File file;
    private ConfigurationSection profSection;
    private Map<Class<?>, UserProfessionData> professions;
    private Map<ProfessionType, Integer> usedProfessionTypes;
    private boolean bypass, suppressExpEvent;
    private final HashMap<String, PotionTask> ACTIVE_POTIONS = new HashMap<>();

    private User(Player player) throws IOException {
        this.player = player;
        this.file = new File(Professions.getInstance().getPlayerFolder(), player.getUniqueId().toString() + ".yml");
        this.loader = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            this.file.createNewFile();
            this.profSection = loader.createSection(KEY_PROFESSIONS);
            this.loader.set(KEY_NAME, player.getName());
        } else {
            this.profSection = loader.getConfigurationSection(KEY_PROFESSIONS);
        }
        loadProfessions();
        this.setBypass(false);
        this.setSuppressExpEvent(false);
    }

    /**
     * Saves and unloads user
     *
     * @param player the player
     * @throws IOException ex
     */
    public static void unloadUser(Player player) throws IOException {
        unloadUser(getUser(player));
    }

    /**
     * Saves and unloads user
     *
     * @param user the user to unload
     * @throws IOException ex
     */
    public static void unloadUser(User user) throws IOException {
        user.save();
        user.unloadUser();
    }

    /**
     * If the player is loaded, this method returns the player from memory. If the player is not loaded, he is loaded and then returned from memory.
     *
     * @param player the player
     * @return an instance of {@code User}
     */
    public static User getUser(Player player) {
        if (player == null) {
            return null;
        }
        try {
            loadUser(player);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return USERS.get(player.getUniqueId());
    }

    /**
     * If the player is loaded, this method returns the player from memory. If the player is not loaded, he is loaded and then returned from memory.
     *
     * @param uuid the uuid
     * @return an instance of {@code User}
     */
    public static User getUser(UUID uuid) {
        return getUser(Bukkit.getPlayer(uuid));
    }

    /**
     * Returns a player from a [uuid].yml file
     * If the player is loaded, this method returns the player from memory. If the player is not loaded, he is loaded and then returned from memory.
     *
     * @param file the file
     * @return an instance of {@code User}
     */
    public static User getUser(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        String fileName = file.getName();
        String substring = fileName.substring(0, fileName.length() - 4);
        System.out.println(substring);
        return getUser(UUID.fromString(substring));
    }

    /**
     * Used to load user when logging in.
     *
     * @param player the player
     * @throws IOException ex
     */
    public static void loadUser(Player player) throws IOException {
        if (!isLoaded(player)) {
            USERS.put(player.getUniqueId(), new User(player));
        }
    }

    /**
     * @param player the player
     * @return {@code true} if the player is loaded, {@code false} otherwise
     */
    public static boolean isLoaded(Player player) {
        return USERS.containsKey(player.getUniqueId());
    }

    /**
     * Saves all loaded users
     *
     * @throws IOException ex
     */
    public static void saveUsers() throws IOException {
        for (User user : USERS.values()) {
            user.save();
        }
    }


    /**
     * Unloads the player (used when player is logging off)
     */
    public void unloadUser() {
        ACTIVE_POTIONS.forEach((x, y) -> y.cancel());
        USERS.remove(player.getUniqueId());
    }

    /**
     * Whether or not this user can profess profession Calls
     * {@link #hasProfession(Profession)} and
     * {@link #hasProfessionType(ProfessionType)}
     *
     * @param prof the profession to check
     */
    public boolean canProfess(Profession<? extends IProfessionType> prof) {
        return !hasProfession(prof) && !hasProfessionType(prof.getProfessionType());
    }

    /**
     * @param type the profession type
     * @return true if this user has already a profession of that type
     */
    private boolean hasProfessionType(ProfessionType type) {
        return usedProfessionTypes.get(type) == Settings.getSettings(MaxProfessionsSettings.class).getMaxProfessions(type);
    }

    /**
     * @param prof the profession to look for
     * @return true if this user has already that profession
     */
    public boolean hasProfession(Profession<? extends IProfessionType> prof) {
        try {
            Utils.findInIterable(professions.values(), x -> x.getProfession().getID().equalsIgnoreCase(prof.getID()));
            return true;
        } catch (Utils.SearchNotFoundException e) {
            if (profSection.isConfigurationSection(prof.getID())) {
                throw new IllegalStateException(player.getName() + " has profession written in file but is not loaded!");
            }
            return false;
        }
    }

    /**
     * Professes the player
     *
     * @param prof profession to profess
     * @return true if professed successfully, false otherwise
     */
    public boolean profess(Profession<? extends IProfessionType> prof) {
        if (!canProfess(prof)) {
            return false;
        }
        professions.put(prof.getClass(), new UserProfessionData(this, prof));
        final ProfessionType professionType = prof.getProfessionType();
        usedProfessionTypes.put(professionType, usedProfessionTypes.get(professionType) + 1);
        return true;
    }

    /**
     * Unprofesses the player
     *
     * @param prof profession to profess
     * @return true if unprofessed successfully, false otherwise
     */
    public boolean unprofess(Profession<? extends IProfessionType> prof) {
        if (!hasProfession(prof)) {
            return false;
        }
        professions.remove(prof.getClass());
        final ProfessionType professionType = prof.getProfessionType();
        usedProfessionTypes.put(professionType, usedProfessionTypes.get(professionType) - 1);
        profSection.set(prof.getID(), null);
        return true;
    }

    /**
     * Sends the user a message
     *
     * @param message the message to send
     */
    public void sendMessage(String... message) {
        Arrays.asList(message).forEach(player::sendMessage);
    }

    /**
     * Returns all user's professions
     *
     * @return set of users profession data
     */
    public ImmutableSet<UserProfessionData> getProfessions() {
        return ImmutableSet.copyOf(professions.values());
    }

    /**
     * @return the player
     */
    public final Player getPlayer() {
        return player;
    }

    /**
     * Adds exp to the player.
     *
     * @param exp    the amount of exp to give
     * @param prof   the profession of this user
     * @param source the item source
     * @return {@link UserProfessionData#addExp(double, ItemType)}
     */
    public boolean addExp(double exp, Profession<? extends IProfessionType> prof, ItemType<?> source) {
        UserProfessionData upd = getProfessionData(prof);
        if (upd != null)
            return upd.addExp(exp, source);
        return false;
    }

    /**
     * Adds levels to the player.
     *
     * @param level the level to add
     * @param prof  the profession to add the level to
     * @return {@link UserProfessionData#addLevel(int)}
     */
    public boolean addLevel(int level, Profession<? extends IProfessionType> prof) {
        UserProfessionData upd = getProfessionData(prof);
        if (upd != null)
            return upd.addLevel(level);
        return false;
    }

    /**
     * Sets the exp of a profession
     *
     * @param exp  the exp to set
     * @param prof the profession to set the exp for
     * @see UserProfessionData#setExp(double)
     */
    public void setExp(double exp, Profession<? extends IProfessionType> prof) {
        UserProfessionData upd = getProfessionData(prof);
        if (upd != null)
            upd.setExp(exp);
    }

    /**
     * Sets the level of a profession
     *
     * @param level the level to set
     * @param prof  the profession to set the level for
     * @see UserProfessionData#setLevel(int)
     */
    public void setLevel(int level, Profession<? extends IProfessionType> prof) {
        UserProfessionData upd = getProfessionData(prof);
        if (upd != null)
            upd.setLevel(level);
    }

    /**
     * Gets the profession data
     *
     * @param prof the profession
     * @return the {@link User}'s {@link Profession} data if the user has the profession, null otherwise
     */
    @Nullable
    public UserProfessionData getProfessionData(Profession<? extends IProfessionType> prof) {
        return professions.get(prof.getClass());
    }

    /**
     * Gets the profession data
     *
     * @param profClass the profession's class
     * @return the {@link User}'s {@link Profession} data if the user has the profession, null otherwise
     */
    @Nullable
    public UserProfessionData getProfessionData(Class<? extends Profession<? extends IProfessionType>> profClass) {
        return professions.get(profClass);
    }

    /**
     * Saves the user's data
     *
     * @throws IOException ex
     */
    public void save() throws IOException {
        for (UserProfessionData upd : professions.values()) {
            upd.save();
        }
        loader.save(file);
    }

    /**
     * Used for bypassing level requirements
     *
     * @return {@code true} if the user bypasses level requirement, {@code false} otherwise
     */
    public boolean isBypass() {
        return bypass;
    }

    /**
     * Sets whether or not this user should bypass level requirement. Usable for admins. Sets {@link #setSuppressExpEvent(boolean)} to {@code false} if {@code bypass} is {@code false}
     *
     * @param bypass the bypass
     */
    public void setBypass(boolean bypass) {
        this.bypass = bypass;
        if (!bypass) {
            setSuppressExpEvent(false);
        }
    }

    /**
     * Used for suppressing the user from receiving experience
     *
     * @return {@code true} if the user is being suppressed from receiving exp, {@code false} otherwise
     */
    public boolean isSuppressExpEvent() {
        return suppressExpEvent;
    }

    /**
     * Sets the player to suppress the player from receiving exp
     *
     * @param suppressExpEvent the suppressExpEvent
     */
    public void setSuppressExpEvent(boolean suppressExpEvent) {
        this.suppressExpEvent = suppressExpEvent;
    }

    public void applyPotion(Potion potion) {
        if (isActivePotion(potion)) {
            return;
        }
        PotionTask potionTask = new PotionTask(potion, player);
        potionTask.runTask();
        ACTIVE_POTIONS.put(potion.getPotionId(), potionTask);
    }

    public boolean isActivePotion(Potion potion) {
        return ACTIVE_POTIONS.containsKey(potion.getPotionId());
    }

    public void unApplyPotion(Potion potion) {
        if (!isActivePotion(potion)) {
            return;
        }
        ACTIVE_POTIONS.remove(potion.getPotionId()).cancel();
    }

    ConfigurationSection getProfessionsSection() {
        return profSection;
    }

    ConfigurationSection getProfessionSection(Profession<? extends IProfessionType> prof) {
        if (!profSection.isConfigurationSection(prof.getID())) {
            return null;
        }
        return profSection.getConfigurationSection(prof.getID());
    }

    private void loadProfessions() {
        this.professions = new HashMap<>();
        profSection.getKeys(false).forEach(x -> {
            Profession<? extends IProfessionType> prof = Professions.getProfessionManager().getProfession(x);
            if (prof != null)
                professions.put(prof.getClass(), new UserProfessionData(this, prof));
        });
        usedProfessionTypes = new HashMap<>();
        for (ProfessionType type : ProfessionType.values()) {
            usedProfessionTypes.put(type, 0);
        }

        for (UserProfessionData upd : professions.values()) {
            final ProfessionType professionType = upd.getProfession().getProfessionType();
            usedProfessionTypes.put(professionType, usedProfessionTypes.get(professionType) + 1);
        }

        final MaxProfessionsSettings settings = Settings.getSettings(MaxProfessionsSettings.class);
        final int maxProfessions = settings.getMaxProfessions(ProfessionType.PRIMARY) + settings.getMaxProfessions(ProfessionType.SECONDARY);
        if (professions.size() > maxProfessions) {
            final String message = player.getName() + " has more than " + maxProfessions + " professions! This should not happen!";
            Professions.log(message, Level.SEVERE);
            Professions.log(message, Level.CONFIG);
        }
    }
}
