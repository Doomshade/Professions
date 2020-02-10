package git.doomshade.professions.user;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.data.ExpSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.enums.Messages.Message;
import git.doomshade.professions.enums.Messages.MessageBuilder;
import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.event.ProfessionExpGainEvent;
import git.doomshade.professions.event.ProfessionExpLoseEvent;
import git.doomshade.professions.event.ProfessionLevelUpEvent;
import git.doomshade.professions.profession.ITrainable;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.utils.Utils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for storing and manipulating {@link User}'s {@link Profession} data.
 *
 * @author Doomshade
 */
public class UserProfessionData {
    private static final String KEY_EXP = "exp", KEY_LEVEL = "level", KEY_EXTRAS = "extras";
    private User user;
    private Profession<? extends IProfessionType> profession;
    private double exp;
    private int level;
    private final List<String> extras;
    private ConfigurationSection s;
    private MessageBuilder builder;

    UserProfessionData(User user, Profession<? extends IProfessionType> profession) {
        s = user.getProfessionSection(profession);
        if (s == null) {
            s = user.getProfessionsSection().createSection(profession.getID());
            this.exp = 0;
            this.level = 1;
            this.extras = new ArrayList<>();
            save();
        } else {
            this.exp = s.getDouble(KEY_EXP);
            this.level = s.getInt(KEY_LEVEL);
            this.extras = s.getStringList(KEY_EXTRAS);
        }

        this.user = user;
        this.profession = profession;

        this.builder = new Messages.MessageBuilder().setPlayer(user).setProfession(profession);
    }

    void save() {
        s.set(KEY_EXP, exp);
        s.set(KEY_LEVEL, level);
        s.set(KEY_EXTRAS, extras);
    }

    /**
     * @return the profession
     */
    public Profession<? extends IProfessionType> getProfession() {
        return profession;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @return the current exp
     */
    public double getExp() {
        return exp;
    }

    /**
     * Sets current exp to the value. Adds a level if {@code exp > } {@link #getRequiredExp()}
     *
     * @param exp the exp to set
     */
    public void setExp(double exp) {
        this.exp = exp;
        if (exp >= getRequiredExp()) {
            addLevel(1);
            this.exp = 0;
        }
    }

    /**
     * @return the current level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets current level to the value. This method also ensures you won't be able to go over the {@link #getLevelCap()}.
     *
     * @param level the level to set to
     */
    public void setLevel(int level) {
        int temp = this.level;
        this.level = Math.min(level, getLevelCap());
        if (temp == this.level) {
            return;
        }

        if (!isMaxLevel()) {
            user.sendMessage(builder.setMessage(Message.LEVEL_UP).setExp(exp).setLevel(level).build());
        } else {
            user.sendMessage(builder.setMessage(Message.MAX_LEVEL_REACHED).setExp(exp).setLevel(level).build());
        }


        profession.onLevelUp(this);
        // prints new possible items
        for (ItemTypeHolder<?> itemTypeHolder : profession.getItems()) {
            try {
                Utils.findAllInIterable(itemTypeHolder, x -> x.getLevelReq() == getLevel()).forEach(y -> user.sendMessage(builder.setItemType(y).build()));
            } catch (Utils.SearchNotFoundException ignored) {

            }
        }
    }

    @Override
    public String toString() {
        return "\n" + ChatColor.STRIKETHROUGH + "--------" + ChatColor.RESET + profession.getColoredName()
                + ChatColor.RESET + "" + ChatColor.STRIKETHROUGH + "--------" + ChatColor.RESET + "\nLevel: " + level
                + "\n" + "Exp: " + (int) exp + "/" + getRequiredExp();
    }

    /**
     * Adds exp to the user's profession
     *
     * @param exp    the amount of exp to give
     * @param source the source of exp ({@code null} for command source)
     * @return {@code true} if {@code exp > 0} and the {@link ProfessionExpGainEvent#isCancelled()} returns {@code false}.
     */
    public boolean addExp(double exp, ItemType<?> source) {
        if (exp == 0) {
            return false;
        }
        if (exp < 0) {
            loseExp(exp);
            return false;
        }

        double expGained = exp;
        if (source != null) {
            ProfessionExpGainEvent event = new ProfessionExpGainEvent(this, source, exp);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return false;
            }
            expGained = event.getExp();
        }
        if (isMaxLevel()) {
            return true;
        }
        this.exp += expGained;
        user.sendMessage(builder.setMessage(Message.EXP_GAIN).setExp(expGained).setLevel(level).build());
        checkForLevel();
        return true;
    }

    /**
     * This method calculates every time it is called, so be careful.
     *
     * @return required exp for next level
     */
    public int getRequiredExp() {
        return Settings.getSettings(ExpSettings.class).getExpFormula().calculate(level);
    }

    /**
     * @return the level cap
     */
    public int getLevelCap() {
        return Settings.getSettings(ExpSettings.class).getLevelCap();
    }


    /**
     * Adds levels to the user's profession data
     *
     * @param level the level to add
     * @return {@code true} if the level was added, {@code false} otherwise
     */
    public boolean addLevel(int level) {
        if (level == 0 || isMaxLevel()) {
            return false;
        }
        int addedLevel = Math.min((this.level + level), getLevelCap());
        ProfessionLevelUpEvent event = new ProfessionLevelUpEvent(this, this.level, addedLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        setLevel(addedLevel);
        return true;
    }

    /**
     * This method also ensures that, if in any case the {@link #getLevel()} > {@link #getLevelCap()}, the level is set to the level cap.
     *
     * @return {@code true} if current level == {@link #getLevelCap()}.
     */
    public boolean isMaxLevel() {
        if (level > getLevelCap()) {
            this.level = getLevelCap();
        }
        return level == getLevelCap();
    }

    /**
     * Trains a user something new. This is saved as {@code extras} in user data file.
     *
     * @param trainable the trainable to train
     * @return {@code true} if the user has successfully trained an {@link ITrainable} (has enough money and {@link #hasTrained(ITrainable)} returns {@code false}), false otherwise
     * @see #addExtra(String)
     */
    public boolean train(ITrainable trainable) {
        EconomyResponse response = Professions.getEconomy().withdrawPlayer(getUser().getPlayer(), trainable.getCost());
        if (!response.transactionSuccess() || hasTrained(trainable)) {
            return false;
        }
        addExtra(trainable.getTrainableId());
        return true;
    }

    /**
     * @param trainable the trainable to check for
     * @return {@code true} if the user has already trained this, {@code false} otherwise
     * @see #hasExtra(String)
     */
    public boolean hasTrained(ITrainable trainable) {
        return hasExtra(trainable.getTrainableId());
    }

    /**
     * @param itemType the {@link ItemType} to check for
     * @return the current {@link SkillupColor} of itemType
     */
    public SkillupColor getSkillupColor(ItemType<?> itemType) {
        return SkillupColor.getSkillupColor(itemType.getLevelReq(), getLevel());
    }

    /**
     * Adds something extra to the user for later usage. Used in {@link #train(ITrainable)}.
     *
     * @param extra the extra to add
     */
    public void addExtra(String extra) {
        if (!extra.isEmpty() && !hasExtra(extra))
            extras.add(translatedExtra(extra));
    }

    /**
     * Used in {@link #hasTrained(ITrainable)}.
     *
     * @param extra the extra to look for
     * @return {@code true} if the user has this extra, {@code false} otherwise
     */
    public boolean hasExtra(String extra) {
        return !extra.isEmpty() && extras.contains(translatedExtra(extra));
    }

    private String translatedExtra(String extra) {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', extra).toLowerCase());
    }

    private void checkForLevel() {
        int reqExp;
        while (exp >= (reqExp = getRequiredExp())) {
            if (isMaxLevel()) {
                this.exp = 0d;
                break;
            }
            exp -= reqExp;
            addLevel(1);
        }
    }

    private void loseExp(double exp) {
        if (exp > 0) {
            throw new IllegalArgumentException("dankšejd někde udělal chybu");
        }

        ProfessionExpLoseEvent event = new ProfessionExpLoseEvent(this, Math.abs(exp));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        this.exp -= Math.abs(event.getExp());
        user.sendMessage(builder.setMessage(Message.EXP_LOSE).setExp(exp).setLevel(level).build());
        if (this.exp < 0) {
            this.exp = 0;
        }
    }
}
