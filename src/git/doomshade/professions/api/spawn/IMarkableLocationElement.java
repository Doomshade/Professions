package git.doomshade.professions.api.spawn;

/**
 * A markable location elemtn
 */
public interface IMarkableLocationElement extends ILocationElement {

    /**
     * @return the icon id of this element
     * @see org.dynmap.markers.MarkerAPI#getMarkerIcon(String)
     */
    String getMarkerIcon();
}
