package git.doomshade.professions.api;

/**
 * Data needed for a spawn of particle
 *
 * @author Doomshade
 * @version 1.0
 */
public interface IParticleData {

    /**
     * @return the particle ID
     */
    String getParticle();

    /**
     * @return the amount of particles spawned
     */
    int getCount();

    /**
     * @return the period in ticks of a spawn
     */
    int getPeriod();

    /**
     * @return the speed of the particle (or some extra)
     */
    double getSpeed();

    /**
     * @return the x offset to the location
     */
    double getXOffset();

    /**
     * @return the x offset to the location
     */
    double getYOffset();

    /**
     * @return the x offset to the location
     */
    double getZOffset();
}
