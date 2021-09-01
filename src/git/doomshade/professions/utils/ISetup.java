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

package git.doomshade.professions.utils;

import git.doomshade.professions.Professions;

/**
 * This interface is only for this plugin's purposes, not the API's, the {@link #setup()} method will not be called
 * during {@link Professions#onEnable()} even if you register it!
 * <p>Note that both setup and cleanup methods could possibly return a boolean inducing the result, but perhaps it's too
 * late for that (too lazy)</p>
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public interface ISetup {
    /**
     * A method in which the implementing class should put all needed data to memory. Called during {@link
     * Professions#onEnable()}
     *
     * @throws Exception when needed
     */
    void setup() throws Exception;

    /**
     * A method in which the implementing class should clean up all the data. Called in {@link
     * git.doomshade.professions.commands.ReloadCommand}
     *
     * @throws Exception when needed
     */
    default void cleanup() throws Exception {

    }

    /**
     * The default name of the class (similar to toString()). Used in logging.
     *
     * @return the default class name
     */
    default String getSetupName() {
        return getClass().getSimpleName();
    }
}
