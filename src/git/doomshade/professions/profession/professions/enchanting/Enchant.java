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

import git.doomshade.professions.api.spawn.ext.Element;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ItemAttribute;
import git.doomshade.professions.utils.ItemAttribute.AttributeType;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The generic type of EnchantedItemType. Used for modifying (enchanting) items.
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class Enchant extends Element {
    public static final int DEFAULT_INTENSITY = 0;
    private static final List<Pattern> ATTRIBUTE_PATTERNS = new ArrayList<>();
    private static final String ITEMSTACK = "itemstack", CLASS = "class", CRAFT_EXP_YIELD = "craft-exp-yield";

    static {

        // LA
        ATTRIBUTE_PATTERNS.add(Pattern.compile("[+][0-9]+ [\\D]+"));

        // SAPI
        ATTRIBUTE_PATTERNS.add(Pattern.compile("[\\D]+: [0-9]+"));
    }

    private final EnchantMetadata metadata;
    private ItemStack item;
    private int craftExpYield;

    public Enchant(String id, String name, ItemStack item, EnchantMetadata metadata,
                   boolean registerElement) {
        super(id, name, registerElement);
        setItem(item);
        this.setCraftExpYield(0);
        this.metadata = metadata;
    }

    static Enchant deserialize(Map<String, Object> map, String name) throws ProfessionObjectInitializationException {
        try {
            Enchant e = Element.deserializeElement(map, name, Enchant.class, x -> {
                return null;
            });
            MemorySection mem = (MemorySection) map.get(ITEMSTACK);
            ItemStack item = ItemUtils.deserialize(mem.getValues(false));
            int expYield = (int) map.get(CRAFT_EXP_YIELD);

            return null;
        } catch (ConfigurationException e) {
            ProfessionLogger.logError(e, false);
        }

        throw new ProfessionObjectInitializationException("Could not deserialize enchant");
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
        for (Pattern p : ATTRIBUTE_PATTERNS) {
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

    public final void setItem(ItemStack item) {
        this.item = item;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put(ITEMSTACK, ItemUtils.serialize(item));
        map.put(CLASS, getClass().getName());
        map.put(CRAFT_EXP_YIELD, craftExpYield);
        return map;
    }

    @Override
    public String toString() {
        return "enchant name: " + getClass().getSimpleName() + "\nitem: " + item;
    }

    public int getCraftExpYield() {
        return craftExpYield;
    }

    public final void setCraftExpYield(int craftExpYield) {
        this.craftExpYield = craftExpYield;
    }

    public final ItemStack use(ItemStack item) {
        return use(item, DEFAULT_INTENSITY);
    }

    public final ItemStack use(ItemStack item, int intensity) {
        return metadata.getFunc().apply(item, intensity);
    }

}
