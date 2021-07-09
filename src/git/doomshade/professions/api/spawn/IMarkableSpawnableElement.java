package git.doomshade.professions.api.spawn;

public interface IMarkableSpawnableElement<SpawnPointType extends ISpawnPoint> extends ISpawnableElement<SpawnPointType> {
    String getMarkerIcon();
}
