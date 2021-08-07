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

package git.doomshade.professions.profession.professions.jewelcrafting;

import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.utils.EffectUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class GemUtils {

    public static final Map<String, GemEffect> IDS;
    static final String ACTIVE_GEM_NBT_TAG = "activeGem";
    static final String GEM_NBT_TAG = "gemItemType";
    public static final GemEffect ADD_ATTRIBUTE_EFFECT;

    static {
        ADD_ATTRIBUTE_EFFECT = new GemEffect() {

            @Override
            public void apply(Gem gem, Player player, boolean inverted) {

                for (String gemEffect : gem.getContext()) {
                    EffectUtils.addAttributes(player, inverted, gemEffect);
                }

                final String s;
                if (!inverted) {
                    s = String.format("Applying %s to %s", gem, player.getName());
                } else {
                    s = String.format("Unapplying %s from %s", gem, player.getName());
                }
                ProfessionLogger.log(s, Level.CONFIG);
            }

            @Override
            public String toString() {
                return "ADD_ATTRIBUTE";
            }
        };


        IDS = new HashMap<>() {
            {
                put(ADD_ATTRIBUTE_EFFECT.toString(), ADD_ATTRIBUTE_EFFECT);
            }
        };
    }


}
