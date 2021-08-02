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

package git.doomshade.professions.profession.professions.mining;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.spawn.Spawnable;
import git.doomshade.professions.profession.utils.YieldResult;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.ParticleData;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static git.doomshade.professions.profession.professions.mining.Ore.OreEnum.RESULT;

/**
 * Custom class for {@link ItemType}. Here I wanted to have a custom mining result, I'd have otherwise only passed
 * {@link Material} as a generic argument to {@link OreItemType}.
 *
 * @author Doomshade
 */
public class Ore extends Spawnable implements ConfigurationSerializable {

    //public static final HashMap<String, Ore> ORES = new HashMap<>();
    public static final Ore EXAMPLE_ORE =
            new Ore(Utils.EXAMPLE_ID, "Example ore name", Material.COAL_ORE,
                    new ArrayList<>(), new ParticleData());
    private final SortedSet<YieldResult> results = new TreeSet<>();

    private Ore(String id, String name, Material oreMaterial, Collection<YieldResult> results,
                ParticleData particleData) {
        super(id, name, oreMaterial, (byte) 0, particleData, "");
        this.results.addAll(results);
    }

    @NotNull
    @Override
    protected ItemTypeHolder<Ore, OreItemType> getItemTypeHolder() {
        return Professions.getProfMan().getItemTypeHolder(OreItemType.class);
    }
    /*
        String id = (String) map.get(ID.s);
        Material mat = Material.getMaterial((String) map.get(MATERIAL.s));

        SortedList<YieldResult> results = new SortedList<>(Comparator.naturalOrder());

        MemorySection dropSection;

        int i = 0;
        while ((dropSection = (MemorySection) map.get(RESULT.s.concat("-" + i))) != null) {
            results.add(YieldResult.deserialize(dropSection.getValues(false)));
            i++;
        }

        List<SpawnPoint> spawnPoints = new ArrayList<>();
        MemorySection spawnSection;
        int x = 0;
        while ((spawnSection = ((MemorySection) map.get(SPAWN_POINT.s.concat("-" + x)))) != null) {
            spawnPoints.add(SpawnPoint.deserialize(spawnSection.getValues(false)));
            x++;
        }

        MemorySection particleSection = (MemorySection) map.get(PARTICLE.s);

        return new Ore(id, name, mat, results, spawnPoints, ParticleData.deserialize(particleSection.getValues(true)));
    }*/

    @Override
    public @NotNull Map<String, Object> serialize() {

        final Map<String, Object> map = super.serialize();

        int i = 0;
        for (YieldResult result : results) {
            map.put(RESULT.s.concat("-" + i++), result.serialize());
        }

        return map;
    }

    /**
     * Required deserialize method of {@link ConfigurationSerializable}
     *
     * @param map serialized Ore
     *
     * @return deserialized Ore
     *
     * @throws ProfessionObjectInitializationException when Ore is not initialized correctly
     */
    public static Ore deserialize(Map<String, Object> map, final String name)
            throws ProfessionObjectInitializationException {

        AtomicReference<ProfessionObjectInitializationException> ex = new AtomicReference<>();
        final BiFunction<Spawnable, ProfessionObjectInitializationException, Ore> func =
                (x, y) -> {

                    ex.set(y);
                    if (x == null) {
                        return null;
                    }

                    SortedSet<YieldResult> results = new TreeSet<>();

                    MemorySection dropSection;

                    int i = 0;
                    while ((dropSection = (MemorySection) map.get(RESULT.s.concat("-" + i))) != null) {
                        try {
                            results.add(YieldResult.deserialize(dropSection.getValues(false)));
                        } catch (ConfigurationException e) {
                            e.append("Ore (" + name + ")");
                            ProfessionLogger.logError(e, false);
                        } catch (InitializationException e) {
                            ProfessionLogger.logError(e, false);
                        }
                        i++;
                    }
                    return new Ore(x.getId(), name, x.getMaterial(), results,
                            x.getParticleData());
                };

        final Ore deserialize = Spawnable.deserialize(map, Ore.class, func);
        // if there are missing keys, throw ex
        if (deserialize == null) {
            throw ex.get();
        }
        return deserialize;
    }

    /**
     * @return the mining result
     */
    @Nullable
    public ItemStack getMiningResult() {
        double random = Math.random() * 100;

        return results.stream()
                .filter(result -> random < result.chance)
                .findFirst()
                .map(result -> result.drop)
                .orElse(null);

    }

    @Override
    public String toString() {
        return String.format("Ore{ID=%s\nName=%s\nMaterial=%s\nSpawnPoints=%s}", getId(), getName(), getMaterial(),
                getSpawnPoints());
    }

    /**
     * Enum for keys in file
     */
    enum OreEnum implements FileEnum {
        RESULT("drop");

        final String s;

        OreEnum(String s) {
            this.s = s;
        }

        @Override
        public EnumMap<OreEnum, Object> getDefaultValues() {
            return new EnumMap<>(OreEnum.class) {
                {
                    put(RESULT, new YieldResult(40d, ItemUtils.EXAMPLE_RESULT));
                }
            };
        }

        @Override
        public String toString() {
            return s;
        }
    }
}
