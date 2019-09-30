package git.doomshade.professions.user;

import git.doomshade.professions.Profession;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.enums.Messages.Message;
import git.doomshade.professions.enums.Messages.MessageBuilder;
import git.doomshade.professions.event.ProfessionExpGainEvent;
import git.doomshade.professions.event.ProfessionExpLoseEvent;
import git.doomshade.professions.event.ProfessionLevelUpEvent;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemType.Key;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class UserProfessionData {
    private static final String KEY_EXP = "exp", KEY_LEVEL = "level", KEY_EXTRAS = "extras";
    private User user;
    private Profession<? extends IProfessionType> profession;
    private double exp;
    private int level;
    private List<String> extras;
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
        this.builder = Messages.getInstance().MessageBuilder().setPlayer(user).setProfession(profession);
    }

    void save() {
        s.set(KEY_EXP, exp);
        s.set(KEY_LEVEL, level);
        s.set(KEY_EXTRAS, extras);
    }

    public Profession<? extends IProfessionType> getProfession() {
        return profession;
    }

    public User getUser() {
        return user;
    }

    public double getExp() {
        return exp;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (isMaxLevel()) {
            return;
        }
        this.level = Math.min(level, getLevelCap());
        if (!isMaxLevel()) {
            user.sendMessage(builder.copy().setMessage(Message.LEVEL_UP).setExp(exp).setLevel(level).build());
        } else {
            user.sendMessage(builder.copy().setMessage(Message.MAX_LEVEL_REACHED).setExp(exp).setLevel(level).build());
        }
        printNewPossibleItemTypes();
    }

    @Override
    public String toString() {
        return "\n" + ChatColor.STRIKETHROUGH + "--------" + ChatColor.RESET + profession.getColoredName()
                + ChatColor.RESET + "" + ChatColor.STRIKETHROUGH + "--------" + ChatColor.RESET + "\nLevel: " + level
                + "\n" + "Exp: " + (int) exp + "/" + getRequiredExp();
    }

    public void addExp(double exp, ItemType<?> source) {
        if (exp == 0) {
            return;
        }
        if (exp < 0) {
            loseExp(exp);
            return;
        }
        if (isMaxLevel()) {
            return;
        }
        double expGained = exp;
        if (source != null) {
            ProfessionExpGainEvent event = new ProfessionExpGainEvent(this, source, exp);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }
            expGained = event.getExp();
        }
        this.exp += expGained;
        user.sendMessage(builder.copy().setMessage(Message.EXP_GAIN).setExp(expGained).setLevel(level).build());
        checkForLevel();
    }

    public int getRequiredExp() {
        return Settings.getInstance().getExpSettings().getExpFormula().calculate(level);
    }

    public int getLevelCap() {
        return Settings.getInstance().getExpSettings().getLevelCap();
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

    public void addLevel(int level) {
        if (level == 0 || isMaxLevel()) {
            return;
        }
        int addedLevel = Math.min((this.level + level), getLevelCap());
        ProfessionLevelUpEvent event = new ProfessionLevelUpEvent(this, this.level, addedLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        setLevel(addedLevel);
    }

    public boolean isMaxLevel() {
        return level >= getLevelCap();
    }

    private void printNewPossibleItemTypes() {

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
        user.sendMessage(builder.copy().setMessage(Message.EXP_LOSE).setExp(exp).setLevel(level).build());
        if (this.exp < 0) {
            this.exp = 0;
        }
    }

    public boolean hasMetReq(Number value, Key key) {
        switch (key) {
            case EXP:
                return getExp() >= value.doubleValue();
            case LEVEL_REQ:
                return getLevel() >= value.intValue();
            default:
                throw new IllegalArgumentException(key.toString() + " is not a number key value!");
        }
    }

    public void addExtra(String extra) {
        if (!extra.isEmpty() && !hasExtra(extra))
            extras.add(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', extra).toLowerCase()));
    }

    public boolean hasExtra(String extra) {
        return !extra.isEmpty() && extras.contains(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', extra).toLowerCase()));
    }
}
