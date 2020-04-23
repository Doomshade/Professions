package git.doomshade.professions.profession.utils;

public interface MarkableLocationElement extends LocationElement {

    /**
     * @return the icon id of this element
     * @see org.dynmap.markers.MarkerAPI#getMarkerIcon(String)
     */
    String getMarkerIcon();
}
