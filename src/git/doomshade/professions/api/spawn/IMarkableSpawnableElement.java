package git.doomshade.professions.api.spawn;

/**
 *
 * @param <SpawnPointType>
 * @author Doomshade
 * @version 1.0
 */
public interface IMarkableSpawnableElement<SpawnPointType extends ISpawnPoint> extends ISpawnableElement<SpawnPointType> {

    /**
     * @return the marker icon on dynmap
     */
    String getMarkerIcon();
}
