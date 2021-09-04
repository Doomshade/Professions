/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.user;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.profession.Profession;
import git.doomshade.professions.api.profession.Profession.ProfessionType;
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
 * @since 1.0
 */
public final class User implements IUser {

    private static final String KEY_NAME = "name";
    private static final String KEY_PROFESSIONS = "professions";
    private static final Map<UUID, User> USERS = new HashMap<>();
    private final Player player;
    private final FileConfiguration loader;
    private final File file;
    private final ConfigurationSection profSection;
    private final Map<String, PotionTask> activePotions = new HashMap<>();
    private Map<Class<?>, UserProfessionData> professions;
    private Map<ProfessionType, Integer> usedProfessionTypes;
    private boolean bypass, suppressExpEvent;

    private User(Player player) throws IOException {
        this.player = player;
        this.file = new File(IOManager.getPlayerFolder(), player.getUniqueId() + ".yml");
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

    private void loadProfessions() {
        this.professions = new HashMap<>();
        profSection.getKeys(false)
                .stream()
                .map(x -> Professions.getProfMan().getProfession(x))
                .forEach(opt -> opt.ifPresent(
                        prof -> professions.put(prof.getClass(), new UserProfessionData(this, prof))));
        usedProfessionTypes = new HashMap<>();
        for (ProfessionType type : ProfessionType.values()) {
            usedProfessionTypes.put(type, 0);
        }

        for (UserProfessionData upd : professions.values()) {
            final Profession profession = upd.getProfession();

            // don't register as used type if subprofession
            if (profession.isSubprofession()) {
                continue;
            }

            final ProfessionType professionType = profession.getProfessionType();
            updateUsedProfessionTypes(professionType, true);
        }

        final MaxProfessionsSettings settings = Settings.getSettings(MaxProfessionsSettings.class);
        final int maxProfessions = settings.getMaxProfessions(ProfessionType.PRIMARY) +
                settings.getMaxProfessions(ProfessionType.SECONDARY);

        // filter out subprofessions
        if (getProfessionAmount(false) >
                maxProfessions) {
            final String message =
                    player.getName() + " has more than " + maxProfessions + " professions! This should not happen!";
            ProfessionLogger.log(message, Level.SEVERE);
        }
    }

    private void updateUsedProfessionTypes(ProfessionType professionType, boolean add) {
        usedProfessionTypes.put(professionType, usedProfessionTypes.get(professionType) + (add ? 1 : -1));
    }

    public int getProfessionAmount(boolean ignoreSubProfessions) {
        return (int) professions.values()
                .stream()
                .filter(x -> ignoreSubProfessions || !(x.getProfession().isSubprofession()))
                .count();
    }

    /**
     * Saves and unloads user
     *
     * @param player the player
     *
     * @throws IOException ex
     */
    public static void unloadUser(Player player) throws IOException {
        unloadUser(getUser(player));
    }

    /**
     * Saves and unloads user
     *
     * @param user the user to unload
     *
     * @throws IOException ex
     */
    public static void unloadUser(User user) throws IOException {
        user.save();
        user.unloadUser();
    }

    @Override
    public void unloadUser() {
        activePotions.forEach((x, y) -> y.cancel());
        USERS.remove(player.getUniqueId());
    }

    @Override
    public boolean hasProfession(Profession prof) {
        try {
            Utils.findInIterable(professions.values(), x -> x.getProfession().getID().equals(prof.getID()));
            return true;
        } catch (Utils.SearchNotFoundException e) {
            if (profSection.isConfigurationSection(prof.getID())) {
                throw new IllegalStateException(
                        player.getName() + " has profession written in file but is not loaded!", e);
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

    /**
     * Whether this user can profess profession Calls {@link User#hasProfession(Profession)} and {@link
     * User#hasProfessionType(ProfessionType)} {@link Profession#isSubprofession()}
     *
     * @param prof the profession to check
     *
     * @return {@code true} if the user can profess into this profession, {@code false} otherwise
     */
    public boolean canProfess(Profession prof) {
        return !hasProfession(prof) && !hasProfessionType(prof.getProfessionType()) && !prof.isSubprofession();
    }

    /**
     * @param type the profession type
     *
     * @return true if this user has already a profession of that type
     */
    private boolean hasProfessionType(ProfessionType type) {
        return usedProfessionTypes.get(type) >=
                Settings.getSettings(MaxProfessionsSettings.class).getMaxProfessions(type);
    }

    private void registerSubProfessions(Profession prof) {
        final Collection<Class<? extends Profession>> subProfs = prof.getSubprofessions();
        if (subProfs != null) {
            final ProfessionManager profMan = ProfessionManager.getInstance();
            for (Class<? extends Profession> subProf : subProfs) {
                final Optional<Profession> opt = profMan.getProfession(subProf);

                if (opt.isEmpty()) {
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

    private void unregisterProfession(Profession prof) {
        professions.remove(prof.getClass());
        profSection.set(prof.getID(), null);
    }

    @Override
    public boolean addExp(double exp, Profession prof, ItemType<?> source) throws PlayerHasNoProfessionException {
        UserProfessionData upd = getProfessionData(prof);
        return upd.addExp(exp, source);
    }

    @Override
    public Collection<IUserProfessionData> getProfessions() {
        return Set.copyOf(professions.values());
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public UserProfessionData getProfessionData(Profession prof) throws PlayerHasNoProfessionException {
        return getProfessionData(prof.getClass());
    }

    @Override
    public UserProfessionData getProfessionData(Class<? extends Profession> profClass)
            throws PlayerHasNoProfessionException {
        final UserProfessionData upd = professions.get(profClass);
        if (upd == null) {
            throw new PlayerHasNoProfessionException(this, profClass.getSimpleName());
        }
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

    /**
     * If the player is loaded, this method returns the player from memory. If the player is not loaded, he is loaded
     * and then returned from memory.
     *
     * @param player the player
     *
     * @return an instance of {@code User}
     *
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
     * Used to load user when logging in.
     *
     * @param player the player
     *
     * @throws IOException ex
     */
    public static void loadUser(Player player) throws IOException {
        if (!isLoaded(player)) {
            USERS.put(player.getUniqueId(), new User(player));
        }
    }

    /**
     * @param player the player
     *
     * @return {@code true} if the player is loaded, {@code false} otherwise
     */
    public static boolean isLoaded(Player player) {
        if (player == null) {
            return false;
        }
        return USERS.containsKey(player.getUniqueId());
    }

    /**
     * Returns a player from a [uuid].yml file If the player is loaded, this method returns the player from memory. If
     * the player is not loaded, he is loaded and then returned from memory.
     *
     * @param file the file
     *
     * @return an instance of {@code User}
     */
    public static User getUser(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        String fileName = file.getName();
        String substring = fileName.substring(0, fileName.length() - Utils.YML_EXTENSION.length());
        return getUser(UUID.fromString(substring));
    }

    /**
     * If the player is loaded, this method returns the player from memory. If the player is not loaded, he is loaded
     * and then returned from memory.
     *
     * @param uuid the uuid
     *
     * @return an instance of {@code User}
     */
    public static User getUser(UUID uuid) {
        return getUser(Bukkit.getPlayer(uuid));
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
     * Sends the user a message
     *
     * @param message the message to send
     */
    public void sendMessage(String... message) {
        Arrays.asList(message).forEach(player::sendMessage);
    }

    /**
     * Adds levels to the player.
     *
     * @param level the level to add
     * @param prof  the profession to add the level to
     *
     * @return {@link UserProfessionData#addLevel(int)}
     */
    @SuppressWarnings("unused")
    public boolean addLevel(int level, Profession prof) throws PlayerHasNoProfessionException {
        UserProfessionData upd = getProfessionData(prof);
        return upd.addLevel(level);
    }

    /**
     * Sets the exp of a profession
     *
     * @param exp  the exp to set
     * @param prof the profession to set the exp for
     *
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
     *
     * @see UserProfessionData#setLevel(int)
     */
    @SuppressWarnings("unused")
    public void setLevel(int level, Profession prof) {
        UserProfessionData upd = getProfessionData(prof);
        upd.setLevel(level);
    }

    public void applyPotion(Potion potion) {
        if (isActivePotion(potion)) {
            return;
        }
        PotionTask potionTask = new PotionTask(potion, player);
        potionTask.runTask();
        activePotions.put(potion.getId(), potionTask);
    }

    public boolean isActivePotion(Potion potion) {
        return activePotions.containsKey(potion.getId());
    }

    @SuppressWarnings("unused")
    public void unApplyPotion(Potion potion) {
        if (!isActivePotion(potion)) {
            return;
        }
        activePotions.remove(potion.getId()).cancel();
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

    @Override
    public String toString() {
        return player.getDisplayName();
    }
}
