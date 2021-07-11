package git.doomshade.professions.user;

import com.google.common.collect.ImmutableSet;
import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.Profession.ProfessionType;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.user.IUser;
import git.doomshade.professions.api.user.IUserProfessionData;
import git.doomshade.professions.data.MaxProfessionsSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.exceptions.PlayerHasNoProfessionException;
import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.ProfessionManager;
import git.doomshade.professions.profession.professions.alchemy.Potion;
import git.doomshade.professions.profession.professions.alchemy.PotionTask;
import git.doomshade.professions.utils.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Class representing a user
 *
 * @author Doomshade
 * @version 1.0
 */
public final class User implements IUser {

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
        this.file = new File(IOManager.getPlayerFolder(), player.getUniqueId().toString() + ".yml");
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
     * @throws IllegalArgumentException if player is null
     */
    public static User getUser(Player player) throws IllegalArgumentException {
        Validate.notNull(player, "Player cannot be null");
        try {
            loadUser(player);
        } catch (IOException e) {
            ProfessionLogger.logError(e);
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


    @Override
    public void unloadUser() {
        ACTIVE_POTIONS.forEach((x, y) -> y.cancel());
        USERS.remove(player.getUniqueId());
    }

    /**
     * Whether or not this user can profess profession Calls
     * {@link User#hasProfession(Profession)} and
     * {@link User#hasProfessionType(ProfessionType)}
     * {@link Profession#isSubprofession()}
     *
     * @param prof the profession to check
     */
    public boolean canProfess(Profession prof) {
        return !hasProfession(prof) && !hasProfessionType(prof.getProfessionType()) && !prof.isSubprofession();
    }

    /**
     * @param type the profession type
     * @return true if this user has already a profession of that type
     */
    private boolean hasProfessionType(ProfessionType type) {
        return usedProfessionTypes.get(type) >= Settings.getSettings(MaxProfessionsSettings.class).getMaxProfessions(type);
    }

    @Override
    public boolean hasProfession(Profession prof) {
        try {
            Utils.findInIterable(professions.values(), x -> x.getProfession().getID().equals(prof.getID()));
            return true;
        } catch (Utils.SearchNotFoundException e) {
            if (profSection.isConfigurationSection(prof.getID())) {
                throw new IllegalStateException(player.getName() + " has profession written in file but is not loaded!");
            }
            return false;
        }
    }

    @Override
    public boolean profess(Profession prof) {
        if (!canProfess(prof)) {
            return false;
        }
        registerProfession(prof);
        updateUsedProfessionTypes(prof.getProfessionType(), true);

        registerSubProfessions(prof);
        return true;
    }

    private void registerSubProfessions(Profession prof) {
        final Collection<Class<? extends Profession>> subProfs = prof.getSubprofessions();
        if (subProfs != null) {
            final ProfessionManager profMan = ProfessionManager.getInstance();
            for (Class<? extends Profession> subProf : subProfs) {
                final Optional<Profession> opt = profMan.getProfession(subProf);

                if (!opt.isPresent()) {
                    continue;
                }
                Profession profession = opt.get();
                // making sure, not needed likely
                if (profession.isSubprofession()) {
                    registerProfession(profession);
                }
            }
        }
    }

    private void registerProfession(Profession prof) {
        professions.put(prof.getClass(), new UserProfessionData(this, prof));
    }

    @Override
    public boolean unprofess(Profession prof) {
        if (!hasProfession(prof)) {
            return false;
        }
        unregisterProfession(prof);
        updateUsedProfessionTypes(prof.getProfessionType(), false);

        final Collection<Class<? extends Profession>> subprofessions = prof.getSubprofessions();
        if (subprofessions != null) {
            final ProfessionManager profMan = ProfessionManager.getInstance();
            for (Class<? extends Profession> subProfClass : subprofessions) {
                final Optional<Profession> opt = profMan.getProfession(subProfClass);
                opt.ifPresent(this::unregisterProfession);
            }
        }
        return true;
    }

    private void updateUsedProfessionTypes(ProfessionType professionType, boolean add) {
        usedProfessionTypes.put(professionType, usedProfessionTypes.get(professionType) + (add ? 1 : -1));
    }

    private void unregisterProfession(Profession prof) {
        professions.remove(prof.getClass());
        profSection.set(prof.getID(), null);
    }

    /**
     * Sends the user a message
     *
     * @param message the message to send
     */
    public void sendMessage(String... message) {
        Arrays.asList(message).forEach(player::sendMessage);
    }

    @Override
    public Collection<IUserProfessionData> getProfessions() {
        return ImmutableSet.copyOf(professions.values());
    }

    @Override
    public final Player getPlayer() {
        return player;
    }

    @Override
    public boolean addExp(double exp, Profession prof, ItemType<?> source) throws PlayerHasNoProfessionException {
        UserProfessionData upd = getProfessionData(prof);
        return upd.addExp(exp, source);
    }

    /**
     * Adds levels to the player.
     *
     * @param level the level to add
     * @param prof  the profession to add the level to
     * @return {@link UserProfessionData#addLevel(int)}
     */
    public boolean addLevel(int level, Profession prof) throws PlayerHasNoProfessionException {
        UserProfessionData upd = getProfessionData(prof);
        return upd.addLevel(level);
    }

    /**
     * Sets the exp of a profession
     *
     * @param exp  the exp to set
     * @param prof the profession to set the exp for
     * @see UserProfessionData#setExp(double)
     */
    public void setExp(double exp, Profession prof) throws PlayerHasNoProfessionException {
        UserProfessionData upd = getProfessionData(prof);
        upd.setExp(exp);
    }

    /**
     * Sets the level of a profession
     *
     * @param level the level to set
     * @param prof  the profession to set the level for
     * @see UserProfessionData#setLevel(int)
     */
    public void setLevel(int level, Profession prof) {
        UserProfessionData upd = getProfessionData(prof);
        upd.setLevel(level);
    }

    @Override
    public UserProfessionData getProfessionData(Profession prof) throws PlayerHasNoProfessionException {
        return getProfessionData(prof.getClass());
    }

    @Override
    public UserProfessionData getProfessionData(Class<? extends Profession> profClass) throws PlayerHasNoProfessionException {
        final UserProfessionData upd = professions.get(profClass);
        if (upd == null) throw new PlayerHasNoProfessionException(this, profClass.getSimpleName());
        return upd;
    }

    @Override
    public void save() throws IOException {
        for (UserProfessionData upd : professions.values()) {
            upd.save();
        }
        loader.save(file);
    }

    @Override
    public boolean isBypass() {
        return bypass;
    }

    @Override
    public void setBypass(boolean bypass) {
        this.bypass = bypass;
        if (!bypass) {
            setSuppressExpEvent(false);
        }
    }

    @Override
    public boolean isSuppressExpEvent() {
        return suppressExpEvent;
    }

    @Override
    public void setSuppressExpEvent(boolean suppressExpEvent) {
        this.suppressExpEvent = suppressExpEvent;
    }

    @Override
    public void applyPotion(Potion potion) {
        if (isActivePotion(potion)) {
            return;
        }
        PotionTask potionTask = new PotionTask(potion, player);
        potionTask.runTask();
        ACTIVE_POTIONS.put(potion.getPotionId(), potionTask);
    }

    @Override
    public boolean isActivePotion(Potion potion) {
        return ACTIVE_POTIONS.containsKey(potion.getPotionId());
    }

    @Override
    public void unApplyPotion(Potion potion) {
        if (!isActivePotion(potion)) {
            return;
        }
        ACTIVE_POTIONS.remove(potion.getPotionId()).cancel();
    }

    ConfigurationSection getProfessionsSection() {
        return profSection;
    }

    ConfigurationSection getProfessionSection(Profession prof) {
        if (!profSection.isConfigurationSection(prof.getID())) {
            return null;
        }
        return profSection.getConfigurationSection(prof.getID());
    }

    private void loadProfessions() {
        this.professions = new HashMap<>();
        profSection.getKeys(false).forEach(x -> {
            Optional<Profession> opt = Professions.getProfMan().getProfession(x);
            opt.ifPresent(prof -> professions.put(prof.getClass(), new UserProfessionData(this, prof)));
        });
        usedProfessionTypes = new HashMap<>();
        for (ProfessionType type : ProfessionType.values()) {
            usedProfessionTypes.put(type, 0);
        }

        for (UserProfessionData upd : professions.values()) {
            final Profession profession = upd.getProfession();

            // don't register as used type if subprofession
            if (profession.isSubprofession()) continue;

            final ProfessionType professionType = profession.getProfessionType();
            updateUsedProfessionTypes(professionType, true);
        }

        final MaxProfessionsSettings settings = Settings.getSettings(MaxProfessionsSettings.class);
        final int maxProfessions = settings.getMaxProfessions(ProfessionType.PRIMARY) + settings.getMaxProfessions(ProfessionType.SECONDARY);

        // filter out subprofessions
        if (professions.values().stream().filter(x -> !(x.getProfession().isSubprofession())).count() > maxProfessions) {
            final String message = player.getName() + " has more than " + maxProfessions + " professions! This should not happen!";
            ProfessionLogger.log(message, Level.SEVERE);
        }
    }

    @Override
    public String toString() {
        return player.getDisplayName();
    }
}
