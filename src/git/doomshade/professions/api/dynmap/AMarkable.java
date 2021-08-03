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

package git.doomshade.professions.api.dynmap;

import git.doomshade.professions.utils.FileEnum;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public abstract class AMarkable implements ConfigurationSerializable {

    private String markerSetId = "";
    private boolean visible = false;

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(MarkableEnum.MARKER_SET_ID.s, getMarkerSetId());
        map.put(MarkableEnum.MARKER_VISIBLE.s, isVisible());
        return map;
    }

    /**
     * @return the dynmap marker set ID
     */
    public final String getMarkerSetId() {
        return markerSetId;
    }

    protected final void setMarkerSetId(String markerSetId) {
        this.markerSetId = markerSetId;
    }

    /**
     * Sets the dynmap marker set ID
     *
     * @param from the comparing layer of the markable
     */
    public final void setMarkerSetId(AMarkable from) {
        if (from == null) {
            return;
        }

        if (isGreaterLayer(from)) {
            this.markerSetId = from.getMarkerSetId();
        }
    }

    private boolean isGreaterLayer(AMarkable comparing) {
        return comparing != null && comparing.getLayer() >= getLayer();
    }

    /**
     * @return the layer of this markable class, used to set marker set IDs based on this layer
     */
    public abstract int getLayer();

    /**
     * @return whether the marker is visible on dynmap
     */
    public final boolean isVisible() {
        return visible;
    }

    /**
     * Sets whether the marker is visible on dynmap
     *
     * @param visible {@code true} if the marker should be visible, {@code false} otherwise
     */
    protected final void setVisible(boolean visible) {
        this.visible = visible;
    }

    public final void setVisible(AMarkable comparing) {
        if (comparing == null) {
            return;
        }

        if (isGreaterLayer(comparing)) {
            this.visible = comparing.isVisible();
        }
    }

    public enum MarkableEnum implements FileEnum {
        MARKER_SET_ID("dynmap-marker"),
        MARKER_VISIBLE("marker-visible");

        public final String s;

        MarkableEnum(String s) {
            this.s = s;
        }

        @Override
        public EnumMap<MarkableEnum, Object> getDefaultValues() {
            return new EnumMap<>(AMarkable.MarkableEnum.class) {
                {
                    put(MARKER_SET_ID, "some-marker");
                    put(MARKER_VISIBLE, false);
                }
            };
        }
    }
}
