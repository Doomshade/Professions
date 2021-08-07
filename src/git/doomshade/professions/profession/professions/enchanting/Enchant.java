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

package git.doomshade.professions.profession.professions.enchanting;

import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ItemAttribute;
import git.doomshade.professions.utils.ItemAttribute.AttributeType;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The generic type of EnchantedItemType. Used for modifying (enchanting) items.
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public abstract class Enchant implements ConfigurationSerializable {
    public static final int DEFAULT_INTENSITY = 0;
    static final HashMap<Class<? extends Enchant>, Enchant> ENCHANTS = new HashMap<>();
    private static final List<Pattern> attributePatterns = new ArrayList<>();
    private static final String ITEMSTACK = "itemstack", CLASS = "class", CRAFT_EXP_YIELD = "craft-exp-yield";

    static {

        // LA
        attributePatterns.add(Pattern.compile("[+][0-9]+ [\\D]+"));

        // SAPI
        attributePatterns.add(Pattern.compile("[\\D]+: [0-9]+"));
    }

    protected final Random random;
    private BiFunction<ItemStack, Integer, ItemStack> function = null;
    private ItemStack item;
    private int craftExpYield;

    protected Enchant(ItemStack item) {
        setItem(item);
        this.setCraftExpYield(0);
        this.random = new Random();
    }

    @SuppressWarnings("unchecked")
    static Enchant deserialize(Map<String, Object> map) throws InitializationException {

        try {
            MemorySection mem = (MemorySection) map.get(ITEMSTACK);
            ItemStack item = ItemUtils.deserialize(mem.getValues(false));
            Class<? extends Enchant> clazz = (Class<? extends Enchant>) Class.forName((String) map.get(CLASS));
            int expYield = (int) map.get(CRAFT_EXP_YIELD);

            Enchant ench = EnchantManager.getInstance().getEnchant(clazz);
            ench.setItem(item);
            ench.setCraftExpYield(expYield);
            return ench;
        } catch (ClassNotFoundException e) {
            ProfessionLogger.logError(e);
        } catch (ConfigurationException e) {
            ProfessionLogger.logError(e, false);
        }

        return null;
    }

    protected static boolean isEnchantable(ItemStack item) {
        return isEnchantable(item, true, true, true);
    }

    protected static boolean isEnchantable(ItemStack item, boolean hasDisplay, boolean hasLore,
                                           boolean hasAttributes) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if ((hasDisplay && !Objects.requireNonNull(meta).hasDisplayName()) ||
                (hasLore && !Objects.requireNonNull(meta).hasLore())) {
            return false;
        }
        if (hasAttributes) {
            return !getAttributes(item).isEmpty();
        }
        return true;
    }

    protected static List<ItemAttribute> getAttributes(ItemStack item) {
        List<ItemAttribute> attributes = new ArrayList<>();
        if (item == null || !item.hasItemMeta()) {
            return attributes;
        }

        ItemMeta meta = item.getItemMeta();

        if (!Objects.requireNonNull(meta).hasLore()) {
            return attributes;
        }

        List<String> lore = new ArrayList<>(Objects.requireNonNull(meta.getLore()));
        attributes = lore.stream()
                .filter(s -> !s.isEmpty())
                .map(ChatColor::stripColor)
                .takeWhile(copy -> !copy.startsWith("-----------------"))
                .map(Enchant::getAttribute)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return attributes;
    }

    protected static ItemAttribute getAttribute(String loreLine) {
        if (loreLine.isEmpty()) {
            return null;
        }
        String copy = ChatColor.stripColor(loreLine);
        for (Pattern p : attributePatterns) {
            if (p.matcher(copy).find()) {
                try {
                    return new ItemAttribute(loreLine.replaceAll("[\\d]", "").replaceAll("[+]", ""),
                            Integer.parseInt(copy.replaceAll("[\\D]", "")),
                            (p.pattern().startsWith("[+]") ? AttributeType.LOREATTRIBUTES : AttributeType.SKILLAPI), p);
                } catch (NumberFormatException e) {
                    return null;
                }

            }
        }
        return null;
    }

    protected static void replaceAttributeValues(List<ItemAttribute> attrs, ItemStack on) {
        if (on == null || !on.hasItemMeta()) {
            return;
        }
        ItemMeta meta = on.getItemMeta();
        if (!Objects.requireNonNull(meta).hasLore()) {
            return;
        }

        List<String> lore = new ArrayList<>(Objects.requireNonNull(meta.getLore()));
        List<String> noColorLore = new ArrayList<>(meta.getLore());
        for (int i = 0; i < noColorLore.size(); i++) {
            String s = noColorLore.get(i);
            if (s.isEmpty()) {
                continue;
            }
            noColorLore.set(i, ChatColor.stripColor(s));
        }

        for (ItemAttribute itemAttribute : attrs) {
            String attr = itemAttribute.getAttribute();
            if (attr.isEmpty()) {
                System.out.println("(Enchant) Attribute is empty! (" + itemAttribute + ")");
                continue;
            }
            for (int i = 0; i < noColorLore.size(); i++) {
                String s = noColorLore.get(i);

                // Firstly: if it contains attribute name -> assure it's a valid attribute
                if (s.contains(ChatColor.stripColor(attr)) && itemAttribute.getPattern().matcher(s).find()) {
                    lore.set(i, itemAttribute.toString());
                }
            }
        }
        meta.setLore(lore);
        on.setItemMeta(meta);
    }

    public final ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(ITEMSTACK, ItemUtils.serialize(item));
        map.put(CLASS, getClass().getName());
        map.put(CRAFT_EXP_YIELD, craftExpYield);
        return map;
    }

    @Override
    public String toString() {
        return "enchant name: " + getClass().getSimpleName() + "\nitem: " + item;
    }

    // might not be used
    public abstract List<Integer> getIntensities();

    public int getCraftExpYield() {
        return craftExpYield;
    }

    public void setCraftExpYield(int craftExpYield) {
        this.craftExpYield = craftExpYield;
    }

    public void setEnchantFunction(BiFunction<ItemStack, Integer, ItemStack> func) {
        this.function = func;
    }

    public final ItemStack use(ItemStack item) {
        return use(item, DEFAULT_INTENSITY);
    }

    public final ItemStack use(ItemStack item, int intensity) {
        return function.apply(item, intensity);
    }

    protected abstract void init();
}
