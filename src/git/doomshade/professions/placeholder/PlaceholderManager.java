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

package git.doomshade.professions.placeholder;

import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ISetup;

import java.util.logging.Level;

/**
 * @author Doomshade
 * @version 1.0
 */
public class PlaceholderManager implements ISetup {
    private static final PlaceholderManager instance = new PlaceholderManager();
    private ProfessionPlaceholders placeholders = null;
    private static boolean registered = false;

    private PlaceholderManager() {
    }

    public static PlaceholderManager getInstance() {
        return instance;
    }

    public static boolean usesPlaceholders() {
        return registered;
    }

    @Override
    public void setup() {
        if (placeholders != null && registered) {
            return;
        }

        placeholders = new ProfessionPlaceholders();

        if (!(registered = placeholders.register())) {
            String msg = "Failed to register placeholders";
            ProfessionLogger.log(msg, Level.CONFIG);
            ProfessionLogger.log(msg, Level.WARNING);
        }
    }
}
