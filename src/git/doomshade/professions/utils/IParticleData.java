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

/**
 * Data needed for a spawn of particle
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public interface IParticleData {

    /**
     * @return the particle ID
     */
    String getParticle();

    /**
     * @return the amount of particles spawned
     */
    int getCount();

    /**
     * @return the period in ticks of a spawn
     */
    int getPeriod();

    /**
     * @return the speed of the particle (or some extra)
     */
    double getSpeed();

    /**
     * @return the x offset to the location
     */
    double getXOffset();

    /**
     * @return the x offset to the location
     */
    double getYOffset();

    /**
     * @return the x offset to the location
     */
    double getZOffset();
}