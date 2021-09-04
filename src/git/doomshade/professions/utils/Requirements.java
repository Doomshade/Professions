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

import git.doomshade.professions.api.item.ICraftableItemType;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A class managing requirements of a player.
 *
 * @author Doomshade
 * @version 1.0
 * @see ICraftableItemType
 * @since 1.0
 */
public class Requirements implements ConfigurationSerializable, Iterable<ItemStack> {

    // keep it as a list due to indexing in file
    private final List<ItemStack> items = new ArrayList<>();

    /**
     * Calls {@link Requirements#Requirements(List)} with an empty {@link List}.
     */
    public Requirements() {
        this(new ArrayList<>());
    }

    /**
     * @param items the requirements
     */
    public Requirements(List<ItemStack> items) {
        if (items != null) {
            this.items.addAll(items);
        }
    }

    /**
     * Deserialization method
     *
     * @param map the {@link ConfigurationSerializable#serialize()}
     *
     * @return deserialized class
     *
     * @see ConfigurationSerializable
     */
    public static Requirements deserialize(Map<String, Object> map)
            throws ConfigurationException, InitializationException {
        List<ItemStack> items = new ArrayList<>();
        for (Object next : map.values()) {
            if (next instanceof MemorySection) {
                items.add(ItemUtils.deserialize(((MemorySection) next).getValues(true)));
            }
        }
        return new Requirements(items);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return IntStream.range(0, items.size())
                .boxed()
                .collect(Collectors.toMap(String::valueOf, i -> ItemUtils.serialize(items.get(i)), (a, b) -> b));
    }

    /**
     * @return the list of requirements
     */
    public List<ItemStack> getRequirements() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Adds an {@link ItemStack} requirement
     *
     * @param requirement the requirement to add
     */
    public void addRequirement(ItemStack requirement) {
        items.add(Objects.requireNonNull(requirement));
    }

    /**
     * @param player the player to check the requirements for
     *
     * @return {@code true} if player meets requirements, {@code false} otherwise
     */
    public boolean meetsRequirements(Player player) {
        Set<ItemStack> itemz = getMetRequirements(player);
        if (itemz.size() > items.size()) {
            throw new IllegalStateException("Met requirements are somehow greater than needed requirements");
        }
        return itemz.size() == items.size();
    }

    private Set<ItemStack> getMetRequirements(Player player) {
        Set<ItemStack> itemz = new HashSet<>();
        if (player == null) {
            return itemz;
        }
        for (ItemStack itemContent : player.getInventory()) {
            if (itemContent == null) {
                continue;
            }
            for (ItemStack item : items) {
                if (item.isSimilar(itemContent) && itemContent.getAmount() >= item.getAmount()) {
                    itemz.add(item);
                }
            }
        }
        return itemz;
    }

    /**
     * @param player the player to check the requirements for
     *
     * @return the missing requirements of player
     */
    public Collection<ItemStack> getMissingRequirements(Player player) {
        Collection<ItemStack> items = new HashSet<>(this.items);
        items.removeAll(getMetRequirements(player));
        return Collections.unmodifiableCollection(items);
    }

    /**
     * Removes requirements from players inventory
     *
     * @param player the player to remove the requirements from
     */
    public void consumeRequiredItems(Player player) {
        PlayerInventory inv = player.getInventory();
        items.forEach(inv::removeItem);
    }

    /**
     * Calls {@link #toString(Player, ChatColor, ChatColor)} with all null arguments.
     *
     * @return the {@link String} representation of this class
     */
    @Override
    public String toString() {
        return toString(null, null, null);
    }

    /**
     * @param player            the player to base this {@link String} representation about
     * @param requirementMet    the custom met requirement color
     * @param requirementNotMet the custom not met requirement color
     *
     * @return the {@link String} representation of this class
     */
    public String toString(Player player, ChatColor requirementMet, ChatColor requirementNotMet) {
        final Set<ItemStack> metRequirements = getMetRequirements(player);
        final String[] strings = new String[items.size()];

        final ListIterator<ItemStack> li = items.listIterator();
        while (li.hasNext()) {
            StringBuilder sb = new StringBuilder();
            ItemStack item = li.next();
            addColor(requirementMet, requirementNotMet, sb, metRequirements, item);
            addItem(sb, item);
            appendItem(strings, li, sb);
        }

        return String.join(", ", strings);
    }

    private void appendItem(String[] strings, ListIterator<ItemStack> li, StringBuilder sb) {
        strings[li.previousIndex() + 1] = sb.toString();
    }

    private void addItem(StringBuilder sb, ItemStack item) {
        sb.append(item.getAmount())
                .append("x ")
                .append(item.getItemMeta() != null && item.getItemMeta().hasDisplayName()
                        ? item.getItemMeta().getDisplayName()
                        : item.getType().toString());
    }

    private void addColor(ChatColor requirementMet, ChatColor requirementNotMet, StringBuilder sb,
                          Set<ItemStack> metRequirements, ItemStack item) {
        if (metRequirements.contains(item) && requirementMet != null) {
            sb.append(requirementMet);
        } else if (requirementNotMet != null) {
            sb.append(requirementNotMet);
        }
    }

    @Override
    public @NotNull Iterator<ItemStack> iterator() {
        return items.iterator();
    }

}
