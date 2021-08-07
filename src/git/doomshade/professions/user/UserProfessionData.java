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
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.api.user.IUserProfessionData;
import git.doomshade.professions.data.ExpSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.enums.Messages.Global;
import git.doomshade.professions.enums.Messages.MessageBuilder;
import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.event.ProfessionExpGainEvent;
import git.doomshade.professions.event.ProfessionExpLoseEvent;
import git.doomshade.professions.event.ProfessionLevelUpEvent;
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
 * @version 1.0
 * @since 1.0
 */
public class UserProfessionData implements IUserProfessionData {
    private static final String KEY_EXP = "exp", KEY_LEVEL = "level", KEY_EXTRAS = "extras";
    private final User user;
    private final Profession profession;
    private double exp;
    private int level;
    private final List<String> extras;
    private ConfigurationSection s;
    private final MessageBuilder builder;

    UserProfessionData(User user, Profession profession) {
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

        this.builder = new Messages.MessageBuilder().player(user).profession(profession);
    }

    @Override
    public void save() {
        s.set(KEY_EXP, exp);
        s.set(KEY_LEVEL, level);
        s.set(KEY_EXTRAS, extras);
    }

    @Override
    public Profession getProfession() {
        return profession;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public double getExp() {
        return exp;
    }

    @Override
    public void setExp(double exp) {
        this.exp = exp;
        if (exp >= getRequiredExp()) {
            addLevel(1);
            this.exp = 0;
        }
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        int temp = this.level;
        this.level = Math.min(level, getLevelCap());
        if (temp == this.level) {
            return;
        }

        if (!isMaxLevel()) {
            user.sendMessage(builder.message(Global.LEVEL_UP).exp(exp).level(level).build());
        } else {
            user.sendMessage(builder.message(Global.MAX_LEVEL_REACHED).exp(exp).level(level).build());
        }


        profession.onLevelUp(this);
        // prints new possible items
        for (ItemTypeHolder<?, ?> itemTypeHolder : profession.getItems()) {
            try {
                Utils.findAllInIterable(itemTypeHolder, x -> x.getLevelReq() == getLevel())
                        .forEach(y -> user.sendMessage(builder.itemType(y).build()));
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

    @Override
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
        user.sendMessage(builder.message(Global.EXP_GAIN).exp(expGained).level(level).build());
        checkForLevel();
        return true;
    }

    @Override
    public int getRequiredExp() {
        return Settings.getSettings(ExpSettings.class).getExpFormula().calculate(level);
    }

    @Override
    public int getLevelCap() {
        return Settings.getSettings(ExpSettings.class).getLevelCap();
    }


    @Override
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

    @Override
    public boolean isMaxLevel() {
        if (level > getLevelCap()) {
            this.level = getLevelCap();
        }
        return level == getLevelCap();
    }

    @Override
    public boolean train(ItemType<?> trainable) {
        EconomyResponse response =
                Professions.getEconomy().withdrawPlayer(getUser().getPlayer(), trainable.getTrainableCost());
        if (!response.transactionSuccess() || hasTrained(trainable)) {
            return false;
        }
        addExtra(trainable.getConfigName());
        return true;
    }

    @Override
    public boolean hasTrained(ItemType<?> trainable) {
        return hasExtra(trainable.getConfigName());
    }

    /**
     * @param itemType the {@link ItemType} to check for
     *
     * @return the current {@link SkillupColor} of itemType
     */
    public SkillupColor getSkillupColor(ItemType<?> itemType) {
        return SkillupColor.getSkillupColor(itemType, this);
    }

    @Override
    public void addExtra(String extra) {
        if (!extra.isEmpty() && !hasExtra(extra)) {
            extras.add(translatedExtra(extra));
        }
    }

    @Override
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
        user.sendMessage(builder.message(Global.EXP_LOSE).exp(exp).level(level).build());
        if (this.exp < 0) {
            this.exp = 0;
        }
    }
}
