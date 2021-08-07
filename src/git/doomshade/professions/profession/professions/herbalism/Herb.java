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

package git.doomshade.professions.profession.professions.herbalism;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.api.spawn.ext.Spawnable;
import git.doomshade.professions.utils.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static git.doomshade.professions.utils.Strings.HerbEnum.*;

/**
 * A gather item type example for {@link HerbalismProfession}
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class Herb extends Spawnable implements ConfigurationSerializable {

    public static final Herb EXAMPLE_HERB = new Herb(Utils.EXAMPLE_ID, Objects.requireNonNull(
            ItemUtils.EXAMPLE_RESULT.getItemMeta()).getDisplayName(),
            ItemUtils.EXAMPLE_RESULT, Material.SUNFLOWER, (byte) 0, false, new ParticleData(), 5,
            "flower");

    private final ItemStack gatherItem;
    private final boolean enableSpawn;
    private final int timeGather;

    private Herb(String id, String name, ItemStack gatherItem, Material herbMaterial, byte materialData,
                 boolean enableSpawn, ParticleData particleData,
                 int gatherTime,
                 String markerIcon) {
        super(id, name, herbMaterial, materialData, particleData, markerIcon);
        this.gatherItem = gatherItem;
        this.enableSpawn = enableSpawn;
        this.timeGather = gatherTime;
    }

    public static Herb deserialize(Map<String, Object> map) throws ProfessionObjectInitializationException {
        final Set<String> missingKeys = Utils.getMissingKeys(map, Strings.HerbEnum.values());
        if (!missingKeys.isEmpty()) {
            throw new ProfessionObjectInitializationException(HerbItemType.class, missingKeys);
        }

        final Herb herb = Spawnable.deserializeSpawnable(map, Herb.class, x -> {
            int gatherTime = (int) map.get(TIME_GATHER.s);
            MemorySection mem = (MemorySection) map.get(GATHER_ITEM.s);
            ItemStack gatherItem;
            try {
                gatherItem = ItemUtils.deserialize(mem.getValues(false));
            } catch (ConfigurationException | InitializationException e) {
                ProfessionLogger.logError(e, false);
                return null;
            }
            String displayName = gatherItem.getType().name();
            if (gatherItem.hasItemMeta()) {
                ItemMeta meta = gatherItem.getItemMeta();
                if (Objects.requireNonNull(meta).hasDisplayName()) {
                    displayName = meta.getDisplayName();
                }
            }
            // TODO: 26.01.2020 make implementation of custom marker icons
            return new Herb(x.getId(), displayName, gatherItem, x.getMaterial(), x.getMaterialData(),
                    (boolean) map.get(ENABLE_SPAWN.s),
                    x.getParticleData(), gatherTime,
                    "flower");
        });
        if (herb == null) {
            throw new ProfessionObjectInitializationException("Could not deserialize herb");
        }
        return herb;
    }
        /*
        // gather item
        MemorySection mem = (MemorySection) map.get(GATHER_ITEM.s);
        ItemStack gatherItem = ItemUtils.deserialize(mem.getValues(false));

        // herb material
        ItemStack herbMaterial = ItemUtils.deserializeMaterial((String) map.get(HERB_MATERIAL.s));

        // herb id
        String herbId = (String) map.get(ID.s);

        // spawn points
        int i = 0;
        MemorySection spawnSection;
        ArrayList<SpawnPoint> spawnPoints = new ArrayList<>();
        while ((spawnSection = (MemorySection) map.get(SPAWN_POINT.s.concat("-" + i))) != null) {
            SpawnPoint sp = SpawnPoint.deserialize(spawnSection.getValues(false));
            if (sp.location.clone().add(0, -1, 0).getBlock().getType() == Material.AIR) {
                final String message = String.format("Spawn point %d of herb %s set to air. Make sure you have a
                block below the herb!", i, herbId);
                Professions.log(message, Level.INFO);
                Professions.log(message, Level.CONFIG);
            }
            spawnPoints.add(sp);
            i++;
        }

        // particles
        MemorySection particleSection = (MemorySection) map.get(PARTICLE.s);
        final ParticleData particleData = ParticleData.deserialize(particleSection.getValues(true));

        // gather time
        int gatherTime = (int) map.get(GATHER_TIME.s);

        String displayName = gatherItem.getType().name();
        if (gatherItem.hasItemMeta()) {
            ItemMeta meta = gatherItem.getItemMeta();
            if (meta.hasDisplayName()) {
                displayName = meta.getDisplayName();
            }
        }

        return new Herb(herbId, displayName, gatherItem, herbMaterial.getType(), (byte) herbMaterial.getDurability(),
         spawnPoints, (boolean) map.get(ENABLE_SPAWN.s), particleData, gatherTime);
    }*/

    private static Map<Herb, ? extends ISpawnPoint> getHerbsInWorld(World world) {
        HashMap<Herb, ISpawnPoint> herbs = new HashMap<>();
        for (Herb herb : getElements(Herb.class).values()) {
            for (ISpawnPoint sp : herb.getSpawnPoints()) {
                if (Objects.requireNonNull(sp.getLocation().getWorld()).equals(world)) {
                    herbs.put(herb, sp);
                }
            }
        }
        return herbs;
    }

    public static void spawnHerbs(World world) {
        for (Map.Entry<Herb, ? extends ISpawnPoint> entry : getHerbsInWorld(world).entrySet()) {
            try {
                entry.getValue().spawn();
            } catch (SpawnException e) {
                ProfessionLogger.logError(e);
            }
        }
    }

    @SuppressWarnings("unused")
    public static void despawnHerbs(World world) {
        for (Map.Entry<Herb, ? extends ISpawnPoint> entry : getHerbsInWorld(world).entrySet()) {
            entry.getValue().despawn();
        }
    }

    @NotNull
    @Override
    protected ItemTypeHolder<Herb, HerbItemType> getItemTypeHolder() {
        return Professions.getProfMan().getItemTypeHolder(HerbItemType.class);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        final Map<String, Object> map = super.serialize();

        map.put(GATHER_ITEM.s, ItemUtils.serialize(gatherItem));
        map.put(ENABLE_SPAWN.s, enableSpawn);
        map.put(TIME_GATHER.s, timeGather);
        return map;
        // adds ":[0-9]+" to the material
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatherItem, getMaterial(), getId());
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && gatherItem != null && gatherItem.equals(((Herb) o).gatherItem);
    }

    @Override
    public boolean canSpawn() {
        return enableSpawn;
    }

    public ItemStack getGatherItem() {
        return gatherItem;
    }

    @Override
    public String toString() {
        return String.format("\nHerb:\nID: %s\nName: %s\nMaterial: %s", this.getId(), this.getName(),
                this.getMaterial().name());
    }

    public int getGatherTime() {
        return timeGather;
    }

}
