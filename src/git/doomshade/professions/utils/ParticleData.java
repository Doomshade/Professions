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

import git.doomshade.professions.io.ProfessionLogger;
import org.bukkit.Particle;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class ParticleData implements ConfigurationSerializable, IParticleData {

    private final String particle;
    private final int count;
    private final int period;
    private final double xOffset, yOffset, zOffset, speed;

    public ParticleData() {
        this(Particle.EXPLOSION_NORMAL.name(), 0, 0, 0, 0, 0, 0);
    }

    public ParticleData(String particle, int count, int period, double xOffset, double yOffset, double zOffset,
                        double speed) {
        this.particle = particle;
        this.count = count;
        this.period = period;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.speed = speed;
    }

    public static ParticleData deserialize(Map<String, Object> map) {
        ParticleData data = new ParticleData();
        for (Field field : ParticleData.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                field.set(data, map.get(field.getName()));
            } catch (IllegalAccessException e) {
                ProfessionLogger.logError(e);
            }
        }
        return data;
    }

    @Override
    public String getParticle() {
        return particle;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public int getPeriod() {
        return period;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public double getXOffset() {
        return xOffset;
    }

    @Override
    public double getYOffset() {
        return yOffset;
    }

    @Override
    public double getZOffset() {
        return zOffset;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        for (Field field : getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                map.put(field.getName(), field.get(this));
            } catch (IllegalAccessException e) {
                ProfessionLogger.logError(e);
            }
        }
        return map;
    }
}
