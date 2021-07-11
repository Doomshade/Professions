package git.doomshade.professions.utils;

import git.doomshade.professions.io.ProfessionLogger;
import org.bukkit.Particle;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ParticleData implements ConfigurationSerializable, git.doomshade.professions.api.IParticleData {

    private final String particle;
    private final int count;
    private final int period;
    private final double xOffset, yOffset, zOffset, speed;

    public ParticleData(String particle, int count, int period, double xOffset, double yOffset, double zOffset, double speed) {
        this.particle = particle;
        this.count = count;
        this.period = period;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.speed = speed;
    }

    public ParticleData() {
        this(Particle.EXPLOSION_NORMAL.name(), 0, 0, 0, 0, 0, 0);
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
    public double getSpeed() {
        return speed;
    }

    @Override
    public Map<String, Object> serialize() {
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
