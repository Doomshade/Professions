package git.doomshade.professions.data;

import git.doomshade.professions.utils.ExpFormula;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Exp settings of professions and SkillAPI
 *
 * @author Doomshade
 * @version 1.0
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

            if (!expSection.isDouble(MULTIPLIER)) {
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
            int x = formulaSection.getInt(X);
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
        expSection.set(MULTIPLIER, expMultiplier);
    }

    public double getSkillapiExpMultiplier() {
        return skillapiExpMultiplier;
    }

    public void setSkillapiExpMultiplier(double skillapiExpMultiplier) {
        this.skillapiExpMultiplier = skillapiExpMultiplier;
        ConfigurationSection expSection = config.getConfigurationSection(XP_CURVE);
        expSection.set(SAPI_MULTIPLIER, expMultiplier);
    }

    @Override
    public String getSetupName() {
        return "exp " + super.getSetupName();
    }
}
