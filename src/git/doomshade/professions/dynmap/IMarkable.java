package git.doomshade.professions.dynmap;

import javax.annotation.Nullable;

public interface IMarkable {

    @Nullable
    MarkerWrapper getMarker();

    String getMarkerSetId();
}
