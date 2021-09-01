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

package git.doomshade.professions.data;

import git.doomshade.professions.utils.ExpFormula;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

/**
 * Exp settings of professions and SkillAPI
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 * @see com.sucy.skill.SkillAPI
 */
public class ExpSettings extends AbstractSettings {

    private static final String
            MULTIPLIER = "multiplier",
            SAPI_MULTIPLIER = "sapi-multiplier",
            Z = "z",
            Y = "y",
            X = "x",
            FORMULA = "formula",
            LEVEL_CAP = "level-cap",
            XP_CURVE = "xp-curve";
    private static final int N_DEFAULT_LEVEL_CAP = 40;
    private static final double N_DEFAULT_MULTIPLIER = 1d;
    private ExpFormula expFormula = new ExpFormula(1, 1, 1);
    private int levelCap = 40;
    private double expMultiplier = N_DEFAULT_MULTIPLIER, skillapiExpMultiplier = N_DEFAULT_MULTIPLIER;

    ExpSettings() {
    }

    @Override
    protected void printError(String section, Object value) {
        super.printError(XP_CURVE + "." + section, value);
    }

    @Override
    public void setup() {
        if (isSection(XP_CURVE)) {
            ConfigurationSection expSection = config.getConfigurationSection(XP_CURVE);

            if (!Objects.requireNonNull(expSection).isDouble(MULTIPLIER)) {
                this.setExpMultiplier(N_DEFAULT_MULTIPLIER);
                printError(MULTIPLIER, N_DEFAULT_MULTIPLIER);
            } else {
                this.setExpMultiplier(expSection.getDouble(MULTIPLIER));
            }
            if (!expSection.isDouble(SAPI_MULTIPLIER)) {
                this.setSkillapiExpMultiplier(N_DEFAULT_MULTIPLIER);
                printError(SAPI_MULTIPLIER, N_DEFAULT_MULTIPLIER);
            } else {
                this.setSkillapiExpMultiplier(expSection.getDouble(SAPI_MULTIPLIER));

            }

            if (!expSection.isInt(LEVEL_CAP)) {
                this.levelCap = N_DEFAULT_LEVEL_CAP;
                printError(LEVEL_CAP, N_DEFAULT_LEVEL_CAP);
            } else {
                this.levelCap = expSection.getInt(LEVEL_CAP);
            }
            ConfigurationSection formulaSection = expSection.getConfigurationSection(FORMULA);
            if (!expSection.isConfigurationSection(FORMULA)) {
                printError(FORMULA, 0);
            }
            int x = Objects.requireNonNull(formulaSection).getInt(X);
            int y = formulaSection.getInt(Y);
            int z = formulaSection.getInt(Z);
            this.expFormula = new ExpFormula(x, y, z);
        } else {
            super.printError(XP_CURVE, null);
        }
    }

    public ExpFormula getExpFormula() {
        return expFormula;
    }

    public int getLevelCap() {
        return levelCap;
    }

    public double getExpMultiplier() {
        return expMultiplier;
    }

    public void setExpMultiplier(double expMultiplier) {
        this.expMultiplier = expMultiplier;
        ConfigurationSection expSection = config.getConfigurationSection(XP_CURVE);
        Objects.requireNonNull(expSection).set(MULTIPLIER, expMultiplier);
    }

    public double getSkillapiExpMultiplier() {
        return skillapiExpMultiplier;
    }

    public void setSkillapiExpMultiplier(double skillapiExpMultiplier) {
        this.skillapiExpMultiplier = skillapiExpMultiplier;
        ConfigurationSection expSection = config.getConfigurationSection(XP_CURVE);
        Objects.requireNonNull(expSection).set(SAPI_MULTIPLIER, expMultiplier);
    }

    @Override
    public String getSetupName() {
        return "exp " + super.getSetupName();
    }
}
