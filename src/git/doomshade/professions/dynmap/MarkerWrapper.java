package git.doomshade.professions.dynmap;

import org.bukkit.Location;
import org.dynmap.markers.Marker;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Wrapper class for {@link Marker}
 *
 * @author Doomshade
 * @version 1.0
 */
public final class MarkerWrapper {

    /**
     * the ID of marker
     */
    final String id, markerIcon;
    final Location location;
    private String label = null;

    public MarkerWrapper(String id, String markerIcon, Location location) {

        this.id = requireNonNull(id, "ID cannot be null");
        this.markerIcon = requireNonNull(markerIcon, "marker icon cannot be null");
        this.location = requireNonNull(location, "location cannot be null");
    }

    /**
     * @return the label
     */
    @Nullable
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label if label is null (prevents overriding current label as this class is implemented in multiple classes)
     *
     * @param label the label to set
     */
    public void setLabel(String label) {
        if (this.label == null) {
            this.label = label;
        }
    }
}
