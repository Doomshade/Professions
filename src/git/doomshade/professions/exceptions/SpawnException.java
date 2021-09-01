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

import git.doomshade.professions.api.spawn.ISpawnable;
import git.doomshade.professions.io.ProfessionLogger;

import java.util.logging.Level;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class SpawnException extends Exception {
    private SpawnExceptionReason reason = SpawnExceptionReason.UNKNOWN;
    private ISpawnable locationElement = null;

    public SpawnException(Throwable cause) {
        this(cause, SpawnExceptionReason.UNKNOWN);
    }

    public SpawnException(Throwable cause, SpawnExceptionReason reason) {
        this(cause, reason, null);
    }

    public SpawnException(Throwable cause, SpawnExceptionReason reason, ISpawnable locationElement) {
        super(cause);
        this.reason = reason;
        this.locationElement = locationElement;
    }

    protected SpawnException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public void printStackTrace() {
        String locationElement =
                this.locationElement == null ? "some element in the world" : this.locationElement.toString();
        ProfessionLogger.log("Could not spawn " + locationElement);

        String reason = "Reason: ";
        switch (this.reason) {
            case UNKNOWN:
                break;
            case INVALID_MATERIAL:
                ProfessionLogger.log(reason.concat("Invalid material given"), Level.WARNING);
                break;
            case INVALID_LOCATION:
                ProfessionLogger.log(reason.concat("Invalid location given"), Level.WARNING);
                break;
        }
        super.printStackTrace();
    }

    public enum SpawnExceptionReason {
        UNKNOWN,
        INVALID_MATERIAL,
        INVALID_LOCATION
    }
}
