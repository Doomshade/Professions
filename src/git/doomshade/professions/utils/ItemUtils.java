package git.doomshade.professions.utils;

import git.doomshade.professions.Professions;
import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static git.doomshade.professions.utils.Strings.ItemTypeEnum.DESCRIPTION;
import static git.doomshade.professions.utils.Strings.ItemTypeEnum.LEVEL_REQ_COLOR;

/**
 * Utilities for {@link ItemStack}
 *
 * @author Doomshade
 */
public final class ItemUtils {

    public static final ItemStack EXAMPLE_REQUIREMENT = new ItemStackBuilder(Material.GLASS)
            .withLore(Arrays.asList(ChatColor.RED + "This", ChatColor.GREEN + "is a lore of requirement"))
            .withDisplayName(ChatColor.DARK_AQUA + "Display name")
            .build();

    public static final ItemStack EXAMPLE_RESULT = new ItemStackBuilder(Material.GLASS)
            .withLore(Arrays.asList(ChatColor.RED + "This", ChatColor.GREEN + "is a lore of result"))
            .withDisplayName(ChatColor.DARK_AQUA + "Display name")
            .build();



    private static ItemUtils instance;

    static {
        instance = new ItemUtils();
    }

    /**
     * @return the instance of this class
     */
    public static ItemUtils getInstance() {
        return instance;
    }

    /**
     * Calls {@link #getDescription(ItemType, List)} with {@link ItemType}'s description from file.
     *
     * @param itemType the {@link ItemType}
     * @param <A>      the {@link ItemType}
     * @return description of {@link ItemType}
     */
    @SuppressWarnings("unchecked")
    public static <A extends ItemType<?>> List<String> getItemTypeLore(A itemType) {
        Map<String, Object> map = getItemTypeMap(itemType.getClass(), itemType.getId());
        List<String> desc = (List<String>) map.getOrDefault(DESCRIPTION.s, new ArrayList<String>());
        return getDescription(itemType, desc);
    }

    /**
     * Calls {@link #getDescription(ItemType, List, UserProfessionData)} with null {@link UserProfessionData} argument.
     *
     * @param itemType    the {@link ItemType}
     * @param description the description to modify
     * @param <A>         the {@link ItemType}
     * @return a description of {@link ItemType}
     */
    public static <A extends ItemType<?>> List<String> getDescription(A itemType, List<String> description) {
        return getDescription(itemType, description, null);
    }

    /**
     * @param itemType    the {@link ItemType}
     * @param description the description to modify
     * @param upd         the {@link UserProfessionData} to base the variables around
     * @param <A>         the {@link ItemType}
     * @return a description of {@link ItemType}
     */
    public static <A extends ItemType<?>> List<String> getDescription(A itemType, List<String> description, UserProfessionData upd) {
        Map<String, Object> map = getItemTypeMap(itemType.getClass(), itemType.getId());
        List<String> desc = new ArrayList<>(description);
        for (Strings.ItemTypeEnum itemTypeEnum : Strings.ItemTypeEnum.values()) {
            String regex = "\\{" + itemTypeEnum + "\\}";
            Object mapObject = map.get(itemTypeEnum.s);

            String replacement = regex;
            if (itemTypeEnum == LEVEL_REQ_COLOR) {
                if (upd != null)
                    replacement = String.valueOf(SkillupColor.getSkillupColor(itemType.getLevelReq(), upd.getLevel()).getColor());
            } else {
                replacement = String.valueOf(mapObject);
            }
            for (int i = 0; i < desc.size(); i++) {
                String s = desc.get(i);
                if (s.isEmpty()) {
                    continue;
                }
                desc.set(i, ChatColor.translateAlternateColorCodes('&', (s.replaceAll(regex, replacement))));
            }
        }
        return desc;
    }

    /**
     * @param clazz the {@link ItemType} class
     * @param id    the {@link ItemType#getId()}
     * @param <A>   the {@link ItemType}
     * @return the serialization of {@link ItemType}
     */
    public static <A extends ItemType<?>> Map<String, Object> getItemTypeMap(Class<A> clazz, int id) {
        File file = getFile(clazz);
        if (!file.exists()) {
            throw new IllegalStateException("Object not yet serialized!");
        }
        String itemId = String.valueOf(id);
        FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
        if (!loader.isConfigurationSection(ItemType.KEY)) {
            throw new IllegalArgumentException(
                    clazz.getSimpleName() + " with id " + id + " not found in file! (" + ItemType.KEY + "." + id + ")");
        }
        ConfigurationSection itemsSection = loader.getConfigurationSection(ItemType.KEY);
        return ((MemorySection) itemsSection.get(itemId)).getValues(true);
    }

    /**
     * @param clazz the {@link ItemType} class
     * @return the file of {@link ItemType}
     */
    public static File getFile(Class<?> clazz) {
        return new File(Professions.getInstance().getItemsFolder(), clazz.getSimpleName().toLowerCase().concat(Utils.YML_EXTENSION));
    }

    /**
     * Calls the {@link ItemStackBuilder#ItemStackBuilder(Material)} constructor
     *
     * @param mat the material
     * @return an {@link ItemStackBuilder} object
     */
    public static ItemStackBuilder itemStackBuilder(Material mat) {
        return new ItemStackBuilder(mat);
    }

    /**
     * Builder for {@link ItemStack}
     */
    public static class ItemStackBuilder {
        private Material mat;
        private int amount;
        private short damage;
        private ItemMeta meta;

        /**
         * @param mat the material
         */
        ItemStackBuilder(Material mat) {
            this.mat = mat;
            this.amount = 1;
            this.damage = 0;
            this.meta = Bukkit.getItemFactory().getItemMeta(mat);
        }

        /**
         * @param lore the lore to set
         * @return {@code this}
         */
        public ItemStackBuilder withLore(List<String> lore) {
            meta.setLore(lore);
            return this;
        }

        /**
         * @param displayName the display name to set
         * @return {@code this}
         */
        public ItemStackBuilder withDisplayName(String displayName) {
            meta.setDisplayName(displayName.isEmpty() ? "" : ChatColor.translateAlternateColorCodes('&', displayName));
            return this;
        }

        /**
         * @param amount the amount to set
         * @return {@code this}
         */
        public ItemStackBuilder setAmount(int amount) {
            this.amount = amount;
            return this;
        }

        /**
         * @param damage the damage to set
         * @return {@code this}
         */
        public ItemStackBuilder setDamage(short damage) {
            this.damage = damage;
            return this;
        }

        /**
         * @return the {@link ItemStack}
         */
        public ItemStack build() {
            ItemStack item = new ItemStack(mat, amount, damage);
            item.setItemMeta(meta);
            return item;
        }
    }


}
