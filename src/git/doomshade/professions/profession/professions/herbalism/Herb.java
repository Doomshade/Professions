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
import git.doomshade.professions.api.spawn.ext.Spawnable;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.ParticleData;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static git.doomshade.professions.utils.Strings.HerbEnum.*;

/**
 * A gather item type example for {@link HerbalismProfession}
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class Herb extends Spawnable {

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

    public static Herb deserialize(final Map<String, Object> map, final String markerIcon, final String name)
            throws ProfessionObjectInitializationException {
        return deserializeSpawnable(map, markerIcon, name, Herb.class, x -> {
            final int gatherTime = (int) map.get(TIME_GATHER.s);

            final MemorySection mem = (MemorySection) map.get(GATHER_ITEM.s);
            final ItemStack gatherItem1;
            try {
                gatherItem1 = ItemUtils.deserialize(mem.getValues(false));
            } catch (ConfigurationException | InitializationException e) {
                ProfessionLogger.logError(e, false);
                return null;
            }

            String displayName = gatherItem1.getType().name();
            if (gatherItem1.hasItemMeta()) {
                ItemMeta meta = gatherItem1.getItemMeta();
                if (Objects.requireNonNull(meta).hasDisplayName()) {
                    displayName = meta.getDisplayName();
                }
            }

            // TODO: 26.01.2020 make implementation of custom marker icons
            return new Herb(x.getId(), displayName, gatherItem1, x.getMaterial(), x.getMaterialData(),
                    (boolean) map.get(ENABLE_SPAWN.s),
                    x.getParticleData(), gatherTime,
                    x.getMarkerIcon());
        }, Collections.emptyList(), Strings.HerbEnum.class);
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
    public String toString() {
        return String.format("\nHerb:\nID: %s\nName: %s\nMaterial: %s", this.getId(), this.getName(),
                this.getMaterial().name());
    }

    @Override
    public boolean canSpawn() {
        return enableSpawn;
    }

    public ItemStack getGatherItem() {
        return gatherItem;
    }

    public int getGatherTime() {
        return timeGather;
    }

}
