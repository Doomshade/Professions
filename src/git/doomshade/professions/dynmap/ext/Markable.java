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

package git.doomshade.professions.dynmap.ext;

import git.doomshade.professions.dynmap.IMarkable;
import git.doomshade.professions.utils.Strings;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class Markable implements IMarkable {

    private String markerSetId = "";
    private boolean visible = false;

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(Strings.MarkableEnum.MARKER_SET_ID.s, getMarkerSetId());
        map.put(Strings.MarkableEnum.MARKER_VISIBLE.s, isVisible());
        return map;
    }

    @Override
    public final String getMarkerSetId() {
        return markerSetId;
    }

    @Override
    public final void setMarkerSetId(String markerSetId) {
        this.markerSetId = markerSetId;
    }

    @Override
    public final void setMarkerSetId(Markable from) {
        if (from == null) {
            return;
        }

        if (isGreaterLayer(from, markerSetId.isEmpty())) {
            this.markerSetId = from.getMarkerSetId();
        }
    }

    @Override
    public boolean isGreaterLayer(Markable comparing, boolean override) {
        return comparing != null && (override || comparing.getLayer() >= getLayer());
    }

    @Override
    public final boolean isVisible() {
        return visible;
    }

    @Override
    public final void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public final void setVisible(Markable comparing) {
        if (comparing == null) {
            return;
        }

        if (isGreaterLayer(comparing, false)) {
            this.visible = comparing.isVisible();
        }
    }

}
