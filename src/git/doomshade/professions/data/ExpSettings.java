package git.doomshade.professions.data;

import git.doomshade.professions.utils.ExpFormula;
import org.bukkit.configuration.ConfigurationSection;

public class ExpSettings extends Settings {

    private static final String MULTIPLIER = "multiplier";
    private static final String SAPI_MULTIPLIER = "sapi-multiplier";
    private static final String Z = "z";
    private static final String Y = "y";
    private static final String X = "x";
    private static final String FORMULA = "formula";
    private static final String LEVEL_CAP = "level-cap";
    private static final String XP_CURVE = "xp-curve";
    private static final int N_DEFAULT_LEVEL_CAP = 40;
    private static final double N_DEFAULT_MULTIPLIER = 1d;
    private ExpFormula expFormula = new ExpFormula(1, 1, 1);
    private int levelCap = 40;
    private double expMultiplier = N_DEFAULT_MULTIPLIER, skillapiExpMultiplier = N_DEFAULT_MULTIPLIER;

    ExpSettings() {
    }

    @Override
    public void setup() throws Exception {
        if (isSection(XP_CURVE)) {
            ConfigurationSection expSection = config.getConfigurationSection(XP_CURVE);

            if (!expSection.isDouble(MULTIPLIER)) {
                this.setExpMultiplier(N_DEFAULT_MULTIPLIER);
                printError(XP_CURVE + "." + MULTIPLIER, N_DEFAULT_MULTIPLIER);
            } else {
                this.setExpMultiplier(expSection.getDouble(MULTIPLIER));
            }
            if (!expSection.isDouble(SAPI_MULTIPLIER)) {
                this.setSkillapiExpMultiplier(N_DEFAULT_MULTIPLIER);
                printError(XP_CURVE + "." + SAPI_MULTIPLIER, N_DEFAULT_MULTIPLIER);
            } else {
                this.setSkillapiExpMultiplier(expSection.getDouble(SAPI_MULTIPLIER));

            }

            if (!expSection.isInt(LEVEL_CAP)) {
                this.levelCap = N_DEFAULT_LEVEL_CAP;
                printError(XP_CURVE + "." + LEVEL_CAP, N_DEFAULT_LEVEL_CAP);
            } else {
                this.levelCap = expSection.getInt(LEVEL_CAP);
            }
            ConfigurationSection formulaSection = expSection.getConfigurationSection(FORMULA);
            if (!expSection.isConfigurationSection(FORMULA)) {
                printError(XP_CURVE + "." + FORMULA, 0);
            }
            int x = formulaSection.getInt(X);
            int y = formulaSection.getInt(Y);
            int z = formulaSection.getInt(Z);
            this.expFormula = new ExpFormula(x, y, z);
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
}
