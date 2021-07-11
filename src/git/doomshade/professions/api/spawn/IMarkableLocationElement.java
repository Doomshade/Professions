package git.doomshade.professions.api.spawn;

import org.dynmap.markers.MarkerAPI;

/**
 * A markable location element
 */
public interface IMarkableLocationElement extends ILocationElement {

    /**
     * @return the icon id of this element
     * @see MarkerAPI#getMarkerIcon(String)
     */
    String getMarkerIcon();
}
