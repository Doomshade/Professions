package git.doomshade.professions.utils;

import git.doomshade.professions.Professions;
import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.user.UserProfessionData;
import net.minecraft.server.v1_9_R1.NBTBase;
import net.minecraft.server.v1_9_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_9_R1.potion.CraftPotionUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static git.doomshade.professions.utils.Strings.ItemTypeEnum.DESCRIPTION;
import static git.doomshade.professions.utils.Strings.ItemTypeEnum.LEVEL_REQ_COLOR;

/**
 * Utilities for {@link ItemStack}
 *
 * @author Doomshade
 */
public final class ItemUtils {

    private static final String MATERIAL = "material";
    private static final String DISPLAY_NAME = "display-name";
    private static final String LORE = "lore";
    private static final String POTION_TYPE = "potion-type";
    private static final String AMOUNT = "amount";

    public static ItemStack deserializeMaterial(String material) {
        String[] split = material.split(":");
        final short damage;
        if (split.length == 2) {
            damage = Short.parseShort(split[1]);
        } else {
            damage = 0;
        }
        return new ItemStack(Material.valueOf(split[0]), 1, damage);
    }

    /**
     * Deserializes an ItemStack from a map. <br>
     * Do not overuse this method as it may not be the fastest in deserializing Potions
     *
     * @param map the map
     * @return deserialized ItemStack
     */
    @SuppressWarnings("unchecked")
    public static ItemStack deserialize(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        final Object potentialMaterial = map.get(MATERIAL);
        if (potentialMaterial == null) {
            return null;
        }

        ItemStack item = deserializeMaterial((String) potentialMaterial);

        ItemMeta meta = item.getItemMeta();
        final Object potentialDisplayName = map.get(DISPLAY_NAME);
        if (potentialDisplayName != null) {
            final String displayName = (String) potentialDisplayName;
            meta.setDisplayName(displayName.isEmpty() ? displayName : ChatColor.translateAlternateColorCodes('&', displayName));
        }

        final Object potentialLore = map.get(LORE);
        if (potentialLore instanceof List) {
            final List<String> lore = ((List<String>) potentialLore).stream().map(x -> x.isEmpty() ? x : ChatColor.translateAlternateColorCodes('&', x)).collect(Collectors.toList());
            meta.setLore(lore);
        }

        final Object potentialAmount = map.get(AMOUNT);
        if (potentialAmount != null) {
            item.setAmount((int) potentialAmount);
        }

        if (meta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) meta;
            final Object potentialPotionType = map.get(POTION_TYPE);
            if (potentialPotionType == null) {
                return null;
            }
            potionMeta.setBasePotionData(CraftPotionUtil.toBukkit((String) potentialPotionType));
        }

        final Object potentialInternal = map.get("internal");

        if (potentialInternal != null) {
            String internal = (String) potentialInternal;

            Set<String> handledTags;
            Map<String, NBTBase> unhandledTags;

            Class<? extends ItemMeta> clazz = meta.getClass();
            if (!clazz.getSimpleName().equalsIgnoreCase("craftmetaitem")) {
                clazz = (Class<? extends ItemMeta>) clazz.getSuperclass();
            }
            try {
                final Method getHandledTags = clazz.getDeclaredMethod("getHandledTags");
                getHandledTags.setAccessible(true);
                handledTags = (Set<String>) getHandledTags.invoke(meta);

                final Field unhandledTagsField = clazz.getDeclaredField("unhandledTags");
                unhandledTagsField.setAccessible(true);
                unhandledTags = (Map<String, NBTBase>) unhandledTagsField.get(meta);

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
                e.printStackTrace();
                return null;
            }


            ByteArrayInputStream buf = new ByteArrayInputStream(new Base64().decode(internal));

            try {
                NBTTagCompound tag = NBTCompressedStreamTools.a(buf);
                final Method method = clazz.getDeclaredMethod("deserializeInternal", NBTTagCompound.class);
                method.setAccessible(true);
                method.invoke(meta, tag);
                Set<String> keys = tag.c();

                for (String key : keys) {
                    if (!handledTags.contains(key)) {
                        unhandledTags.put(key, tag.get(key));
                    }
                }
            } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        item.setItemMeta(meta);
        return item;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> serialize(ItemStack item) {

        if (item == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put(ItemUtils.MATERIAL, item.getType().name());
        map.put(AMOUNT, item.getAmount());
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName())
                map.put(ItemUtils.DISPLAY_NAME, meta.getDisplayName().replaceAll("§", "&"));
            if (meta.hasLore()) {
                map.put(ItemUtils.LORE, meta.getLore().stream().map(x -> x.replaceAll("§", "&")).collect(Collectors.toList()));
            }


            Class<? extends ItemMeta> clazz = meta.getClass();
            if (!clazz.getSimpleName().equalsIgnoreCase("craftmetaitem")) {
                clazz = (Class<? extends ItemMeta>) clazz.getSuperclass();
            }
            if (meta instanceof PotionMeta) {
                PotionMeta potionMeta = (PotionMeta) meta;
                map.put(ItemUtils.POTION_TYPE, CraftPotionUtil.fromBukkit(potionMeta.getBasePotionData()));
            }

            Map<String, NBTBase> unhandledTags;
            try {
                final Field unhandledTagsField = clazz.getDeclaredField("unhandledTags");
                unhandledTagsField.setAccessible(true);
                unhandledTags = (Map<String, NBTBase>) unhandledTagsField.get(meta);

            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
                return null;
            }

            Map<String, NBTBase> internalTags = new HashMap<>(unhandledTags);
            try {
                final Method method = clazz.getDeclaredMethod("serializeInternal", Map.class);
                method.setAccessible(true);
                method.invoke(meta, internalTags);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            if (!internalTags.isEmpty()) {
                NBTTagCompound internal = new NBTTagCompound();

                for (Map.Entry<String, NBTBase> stringNBTBaseEntry : internalTags.entrySet()) {
                    internal.set(stringNBTBaseEntry.getKey(), stringNBTBaseEntry.getValue());
                }

                try {
                    ByteArrayOutputStream buf = new ByteArrayOutputStream();
                    NBTCompressedStreamTools.a(internal, buf);
                    map.put("internal", Base64.encodeBase64String(buf.toByteArray()));
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        }
        return map;
    }

    public static final ItemStack EXAMPLE_REQUIREMENT = new ItemStackBuilder(Material.GLASS)
            .withLore(Arrays.asList(ChatColor.RED + "This", ChatColor.GREEN + "is a lore of requirement"))
            .withDisplayName(ChatColor.DARK_AQUA + "Display name")
            .setAmount(5)
            .build();

    public static final ItemStack EXAMPLE_RESULT = new ItemStackBuilder(Material.GLASS)
            .withLore(Arrays.asList(ChatColor.RED + "This", ChatColor.GREEN + "is a lore of result"))
            .withDisplayName(ChatColor.DARK_AQUA + "Display name")
            .setAmount(5)
            .build();
    public static final Location EXAMPLE_LOCATION = Bukkit.getWorlds().get(0).getSpawnLocation();

    /**
     * Calls {@link #getDescription(ItemType, List)} with {@link ItemType}'s description from file.
     *
     * @param itemType the {@link ItemType}
     * @param <A>      the {@link ItemType}
     * @return description of {@link ItemType}
     */
    @SuppressWarnings("unchecked")
    public static <A extends ItemType<?>> List<String> getItemTypeLore(A itemType) {
        Map<String, Object> map = getItemTypeMap(itemType.getClass(), itemType.getFileId());
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
        Map<String, Object> map = getItemTypeMap(itemType.getClass(), itemType.getFileId());
        List<String> desc = new ArrayList<>(description);
        for (Strings.ItemTypeEnum itemTypeEnum : Strings.ItemTypeEnum.values()) {
            String regex = "\\{" + itemTypeEnum + "}";
            Object mapObject = map.get(itemTypeEnum.s);

            String replacement;
            if (itemTypeEnum == LEVEL_REQ_COLOR) {
                if (upd != null) {
                    replacement = String.valueOf(SkillupColor.getSkillupColor(itemType.getLevelReq(), upd.getLevel()).getColor());
                } else {
                    continue;
                }
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
     * @param id    the {@link ItemType#getFileId()}
     * @param <A>   the {@link ItemType}
     * @return the serialization of {@link ItemType}
     */
    public static <A extends ItemType<?>> Map<String, Object> getItemTypeMap(Class<A> clazz, int id) {
        File file = getItemTypeFile(clazz);
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
    public static File getItemTypeFile(Class<?> clazz) {
        return new File(Professions.getInstance().getItemsFolder(), clazz.getSimpleName().toLowerCase().replace("itemtype", "").concat(Utils.YML_EXTENSION));
    }

    /**
     * Calls the ItemStackBuilder constructor constructor
     *
     * @param mat the material
     * @return an ItemStackBuilder object
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
