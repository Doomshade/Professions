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
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.item.object.spawn.Spawnable;
import git.doomshade.professions.dynmap.MarkerManager;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.utils.YieldResult;
import git.doomshade.professions.utils.ParticleData;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static git.doomshade.professions.utils.Strings.OreEnum.RESULT;

/**
 * Custom class for {@link ItemType}. Here I wanted to have a custom mining result, I'd have otherwise only passed
 * {@link Material} as a generic argument to {@link OreItemType}.
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class Ore extends Spawnable {

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

        final Ore ore = Spawnable.deserializeSpawnable(map, MarkerManager.getEmptyMarkerSetId(), name, Ore.class,
                x -> {

                    if (x == null) {
                        return null;
                    }

                    boolean thrown = false;

                    final PriorityQueue<YieldResult> results = new PriorityQueue<>();
                    for (int i = 0; i < map.size(); i++) {
                        final Object o = map.get(RESULT.s.concat("-" + i));

                        if (o instanceof MemorySection) {
                            try {
                                final MemorySection dropSection = (MemorySection) o;
                                results.add(YieldResult.deserialize(dropSection.getValues(false)));
                            } catch (ConfigurationException | InitializationException e) {
                                ProfessionLogger.logError(e, false);
                                thrown = true;
                            }
                        }
                    }
                    return thrown ? null : new Ore(x.getId(), name, x.getMaterial(), results,
                            x.getParticleData());
                }, Collections.singletonList(RESULT.s), Strings.OreEnum.class);

        return ore;
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

    @NotNull
    @Override
    protected ItemTypeHolder<Ore, OreItemType> getItemTypeHolder() {
        return Professions.getProfMan().getItemTypeHolder(OreItemType.class);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {

        final Map<String, Object> map = super.serialize();

        int i = 0;
        for (YieldResult result : results) {
            map.put(RESULT.s.concat("-" + i++), result.serialize());
        }

        return map;
    }

    @Override
    public String toString() {
        return "Ore{} " + super.toString();
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

}
