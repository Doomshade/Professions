/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.dynmap;

import git.doomshade.professions.dynmap.ext.Markable;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * Class related to dynmap markers
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public interface IMarkable extends ConfigurationSerializable {

    /**
     * @return the dynmap marker set ID
     */
    String getMarkerSetId();

    /**
     * Sets the dynmap marker set ID
     *
     * @param markerSetId the marker set ID
     */
    void setMarkerSetId(String markerSetId);

    /**
     * Sets the dynmap marker set ID
     *
     * @param from the comparing layer of the markable
     */
    void setMarkerSetId(Markable from);

    /**
     *
     * @param comparing
     * @return
     */
    boolean isGreaterLayer(Markable comparing);

    /**
     * @return the layer of this markable class, used to set marker set IDs based on this layer
     */
    int getLayer();

    /**
     * @return whether the marker is visible on dynmap
     */
    boolean isVisible();

    /**
     * Sets whether the marker is visible on dynmap
     *
     * @param visible {@code true} if the marker should be visible, {@code false} otherwise
     */
    void setVisible(boolean visible);

    /**
     * Sets whether the marker is visible on dynmap
     *
     * @param comparing the comparing layer of the markable, if the comparing layer is greater or equal, this sets
     */
    void setVisible(Markable comparing);
}
