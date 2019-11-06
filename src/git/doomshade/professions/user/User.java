package git.doomshade.professions.user;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Profession.ProfessionType;
import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.utils.IBackup;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class User implements IBackup {

    private static final String KEY_NAME = "name";
    private static final String KEY_PROFESSIONS = "professions";
    private static final Map<UUID, User> USERS = new HashMap<>();
    private static User noUser;
    private final Player player;
    private FileConfiguration loader;
    private File file;
    private ConfigurationSection profSection;
    // private final Map<ProfessionType, Profession<? extends IProfessionType>>
    // git.doomshade.professions;
    private Map<Class<?>, UserProfessionData> professions;
    private Map<ProfessionType, Boolean> usedProfessionTypes;
    private boolean bypass, suppressExpEvent;

    private User(Player player) {
        this.player = player;
        this.file = new File(Professions.getInstance().getPlayerFolder(), player.getUniqueId().toString() + ".yml");
        this.loader = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.profSection = loader.createSection(KEY_PROFESSIONS);
            this.loader.set(KEY_NAME, player.getName());
        } else {
            this.profSection = loader.getConfigurationSection(KEY_PROFESSIONS);
        }
        loadProfessions();
        this.setBypass(false);
        this.setSuppressExpEvent(false);
    }

    private User() {
        this.player = null;
        this.professions = new HashMap<>();
        usedProfessionTypes = new HashMap<>();
    }

    public static void removeUser(Player user) {
        getUser(user).unload();
    }

    public static User getNoUser() {
        if (noUser == null) {
            noUser = new User() {
                @Override
                public File[] getFiles() {
                    return new File[]{Professions.getInstance().getPlayerFolder()};
                }
            };
        }
        return noUser;
    }

    public static User getUser(Player hrac) {
        if (hrac == null) {
            return null;
        }
        loadUser(hrac);
        return USERS.get(hrac.getUniqueId());
    }

    public static User getUser(UUID uuid) {
        return getUser(Bukkit.getPlayer(uuid));
    }

    public static User getUser(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        String fileName = file.getName();
        String substring = fileName.substring(0, fileName.length() - 4);
        System.out.println(substring);
        return getUser(UUID.fromString(substring));
    }

    public static void loadUser(Player hrac) {
        if (!isLoaded(hrac)) {
            USERS.put(hrac.getUniqueId(), new User(hrac));
        }
    }

    private static boolean isLoaded(Player hrac) {
        return USERS.containsKey(hrac.getUniqueId());
    }

    public static void saveUsers() throws IOException {
        for (User user : USERS.values()) {
            user.save();
        }
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
            Profession<? extends IProfessionType> prof = Professions.getProfessionManager().fromName(x);
            if (prof != null)
                professions.put(prof.getClass(), new UserProfessionData(this, prof));
        });
        usedProfessionTypes = new HashMap<>();
        for (ProfessionType type : ProfessionType.values()) {
            usedProfessionTypes.put(type, false);
        }

        for (UserProfessionData upd : professions.values()) {
            usedProfessionTypes.put(upd.getProfession().getProfessionType(), true);
        }
        if (professions.size() > 2) {
            Professions.getInstance()
                    .sendConsoleMessage(player.getName() + " has more than 2 professions! This should not happen!");
        }
    }

    @Override
    public File[] getFiles() {
        return new File[]{file};
    }

    public void unload() {
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
        return usedProfessionTypes.get(type);
    }

    public boolean hasProfession(Profession<? extends IProfessionType> prof) {
        for (UserProfessionData udp : professions.values()) {
            if (udp.getProfession().getID().equalsIgnoreCase(prof.getID())) {
                return true;
            }
        }
        if (profSection.isConfigurationSection(prof.getID())) {
            throw new IllegalStateException(player.getName() + " has profession written in file but is not loaded!");
        }
        return false;
    }

    /**
     * @param prof profession to profess
     * @return true if professed successfully, false otherwise
     */
    public boolean profess(Profession<? extends IProfessionType> prof) {
        if (!canProfess(prof)) {
            return false;
        }
        professions.put(prof.getClass(), new UserProfessionData(this, prof));
        usedProfessionTypes.put(prof.getProfessionType(), true);
        return true;
    }

    public boolean unprofess(Profession<? extends IProfessionType> prof) {
        if (!hasProfession(prof)) {
            return false;
        }
        professions.remove(prof.getClass());
        usedProfessionTypes.put(prof.getProfessionType(), false);
        profSection.set(prof.getID(), null);
        return true;
    }

    public void sendMessage(String... message) {
        Arrays.asList(message).forEach(player::sendMessage);
    }

    public Collection<UserProfessionData> getProfessions() {
        return professions.values();
    }

    public final Player getPlayer() {
        return player;
    }

    public void addExp(double exp, Profession<? extends IProfessionType> prof, ItemType<?> source) {
        UserProfessionData upd = getProfessionData(prof);
        if (upd != null)
            upd.addExp(exp, source);
    }

    public void addLevel(int level, Profession<? extends IProfessionType> prof) {
        UserProfessionData upd = getProfessionData(prof);
        if (upd != null)
            upd.addLevel(level);
    }

    public void setExp(double exp, Profession<? extends IProfessionType> prof) {
        UserProfessionData upd = getProfessionData(prof);
        if (upd != null)
            upd.setExp(exp);
    }

    public void setLevel(int level, Profession<? extends IProfessionType> prof) {
        UserProfessionData upd = getProfessionData(prof);
        if (upd != null)
            upd.setLevel(level);
    }

    public UserProfessionData getProfessionData(Profession<? extends IProfessionType> prof) {
        return professions.get(prof.getClass());
    }

    public UserProfessionData getProfessionData(Class<? extends Profession<? extends IProfessionType>> profClass) {
        return professions.get(profClass);
    }

    public void save() throws IOException {
        for (UserProfessionData upd : professions.values()) {
            upd.save();
        }
        loader.save(file);
    }

    public boolean isBypass() {
        return bypass;
    }

    public void setBypass(boolean bypass) {
        this.bypass = bypass;
        if (!bypass) {
            setSuppressExpEvent(false);
        }
    }

    public boolean isSuppressExpEvent() {
        return suppressExpEvent;
    }

    public void setSuppressExpEvent(boolean suppressExpEvent) {
        this.suppressExpEvent = suppressExpEvent;
    }
}
