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

package git.doomshade.professions.exceptions;

import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.io.ProfessionLogger;

import java.util.Collection;
import java.util.logging.Level;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class ProfessionInitializationException extends InitializationException {

    private static final int NO_ID = -1;

    public ProfessionInitializationException(String message) {
        super(message);
    }

    /**
     * Calls {@link #ProfessionInitializationException(Class, Collection, int)} with an ID of -1 (Magical number).
     *
     * @param clazz the item type class in which the error occurred
     * @param keys  the keys of missing keys
     */
    public ProfessionInitializationException(Class<?> clazz, Collection<String> keys) {
        this(clazz, keys, NO_ID);
    }

    /**
     * Calls {@link #ProfessionInitializationException(Class, Collection, int, String)} with an ID of -1 (Magical
     * number) and empty additional message
     *
     * @param clazz             the item type class in which the error occurred
     * @param keys              the keys of missing keys
     * @param additionalMessage the additional message to add at the end of exception
     */
    public ProfessionInitializationException(Class<?> clazz, Collection<String> keys, String additionalMessage) {
        this(clazz, keys, NO_ID, additionalMessage);
    }


    /**
     * Calls {@link #ProfessionInitializationException(Class, Collection, int, String)} with an empty additional
     * message
     *
     * @param clazz the item type class in which the error occurred
     * @param keys  the keys of missing keys
     * @param id    the ID of {@code ItemType}
     */
    public ProfessionInitializationException(Class<?> clazz, Collection<String> keys, int id) {
        this(clazz, keys, id, "");
    }


    /**
     * The main constructor of this exception. Consider calling {@link ProfessionLogger#log(String, Level)} instead of
     * {@link #printStackTrace()} for the exception message if you do not need to print the stack trace.
     *
     * @param clazz             the item type class in which the error occurred
     * @param keys              the missing keys
     * @param id                the ID of {@link ItemType}
     * @param additionalMessage the additional message to add at the end of exception
     */
    public ProfessionInitializationException(Class<?> clazz, Collection<String> keys, int id,
                                             String additionalMessage) {
        super("Could not fully deserialize " + clazz.getSimpleName().replace("itemtype", "") +
                (id != NO_ID ? " with id " + id : "") + " as some of the keys are missing! - " + keys + ". " +
                additionalMessage);
    }
}
