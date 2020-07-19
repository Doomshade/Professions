package git.doomshade.professions.dynmap;

import javax.annotation.Nullable;

/**
 * Interface for markers on dynmap
 *
 * @author Doomshade
 * @version 1.0
 */
public interface IMarkable {

    @Nullable
    MarkerWrapper getMarker();

    String getMarkerSetId();
}
