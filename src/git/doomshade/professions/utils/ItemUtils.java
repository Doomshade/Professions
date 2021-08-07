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

import git.doomshade.diablolike.DiabloLike;
import git.doomshade.diablolike.utils.DiabloItem;
import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.commands.AbstractCommandHandler;
import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.commands.ReloadCommand;
import git.doomshade.professions.commands.SaveCommand;
import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static git.doomshade.professions.utils.Strings.ItemTypeEnum.DESCRIPTION;
import static git.doomshade.professions.utils.Strings.ItemTypeEnum.LEVEL_REQ_COLOR;

/**
 * Utilities for {@link ItemStack}
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class ItemUtils implements ISetup {

    public static final ItemUtils instance = new ItemUtils();
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
    static final String DIABLO_ITEM = "diabloitem";
    private static final String MATERIAL = "material";
    private static final String DISPLAY_NAME = "display-name";
    private static final String LORE = "lore";
    private static final String POTION_TYPE = "potion-type";
    private static final String AMOUNT = "amount";
    private static final HashSet<Map<String, Object>> ITEMS_LOGGED = new HashSet<>();
    private static final File ITEMS_LOGGED_FILE = new File(IOManager.getCacheFolder(), "itemutilscache.bin");
    private static final Pattern GENERIC_REGEX = Pattern.compile("\\{([a-zA-Z0-9.\\-_]+)}");
    private static boolean loggedDiablo = false;

    private ItemUtils() {
    }

    public static ItemStack deserialize(Map<String, Object> map)
            throws ConfigurationException, ProfessionObjectInitializationException {
        return deserialize(map, true);
    }

    /**
     * Deserializes an ItemStack from a map. <br> Do not overuse this method as it may not be the fastest in
     * deserializing Potions
     *
     * @param map the map
     *
     * @return deserialized ItemStack
     */
    @SuppressWarnings("unchecked")
    public static ItemStack deserialize(Map<String, Object> map, boolean checkForDiabloHook)
            throws ConfigurationException, ProfessionObjectInitializationException {
        if (map == null) {
            return null;
        }

        if (checkForDiabloHook && Professions.isDiabloLikeHook()) {
            Object potentialId = map.get(DIABLO_ITEM);
            if (potentialId instanceof String) {
                final String id = (String) potentialId;
                final DiabloItem diabloItem = DiabloLike.getItemFromConfigName(id);

                if (diabloItem != null) {
                    final ItemStack item = diabloItem.getItemUtils().getDropItem();
                    final Object potentialAmount = map.get(AMOUNT);
                    if (potentialAmount instanceof Integer) {
                        item.setAmount((int) potentialAmount);
                    }
                    return item;
                }
            } else {
                logDiablo(map);
            }
        }

        final Object potentialMaterial = map.get(MATERIAL);
        if (potentialMaterial == null) {
            throw new ConfigurationException(new NullPointerException("Null material for " + map));
        }

        ItemStack item = deserializeMaterial((String) potentialMaterial);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        final Object potentialDisplayName = map.get(DISPLAY_NAME);
        if (potentialDisplayName != null) {
            final String displayName = (String) potentialDisplayName;
            meta.setDisplayName(
                    displayName.isEmpty() ? displayName : ChatColor.translateAlternateColorCodes('&', displayName));
        }

        final Object potentialLore = map.get(LORE);
        if (potentialLore instanceof List) {
            final List<String> lore = ((List<String>) potentialLore).stream()
                    .map(x -> x.isEmpty() ? x : ChatColor.translateAlternateColorCodes('&', x))
                    .collect(Collectors.toList());
            meta.setLore(lore);
        }

        final Object potentialAmount = map.get(AMOUNT);
        if (potentialAmount != null) {
            item.setAmount((int) potentialAmount);
        }

        if (meta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) meta;
            final Object potentialPotionType = map.get(POTION_TYPE);
            if (potentialPotionType instanceof String) {

                //potionMeta.setBasePotionData(CraftPotionUtil.toBukkit((String) potentialPotionType));
            }
        }

        item.setItemMeta(meta);
        return item;
        //return deserializeInternal(map, item, meta);
    }

    public static ItemStack deserializeMaterial(String material) throws ProfessionObjectInitializationException {
        String[] split = material.split(":");
        final short damage;
        if (split.length == 2) {
            damage = Short.parseShort(split[1]);
        } else {
            damage = 0;
        }
        final Material mat;
        try {
            mat = Material.valueOf(split[0]);
        } catch (IllegalArgumentException e) {
            throw new ProfessionObjectInitializationException("Could not deserialize material " + split[0]);
        }
        return new ItemStack(mat, 1, damage);
    }

    private static void logDiablo(Map<String, Object> map) {
        if (!loggedDiablo) {
            ProfessionLogger.log("Found items that are not a DiabloItem.");
            ProfessionLogger.log(
                    "To use diablo item, replace display-name, lore, ..., with \"diabloitem: <config_name>\"");
            ProfessionLogger.log("To update the logs file, use command: " +
                            ChatColor.stripColor(AbstractCommandHandler.infoMessage(CommandHandler.class,
                                    SaveCommand.class)),
                    Level.INFO);
            loggedDiablo = true;
        }
        if (!ITEMS_LOGGED.contains(map)) {
            ProfessionLogger.log("Deserializing an item that is not a DiabloItem. Serialized form is found in logs.",
                    Level.WARNING);
            ProfessionLogger.log("DiabloItem serialized form:\n" + map, Level.CONFIG);
            ITEMS_LOGGED.add(map);
        }
    }

    public static Map<String, Object> serialize(final ItemStack item) {

        if (item == null) {
            return new HashMap<>();
        }


        Map<String, Object> map = new HashMap<>();

        map.put(MATERIAL, serializeMaterial(item));
        map.put(AMOUNT, item.getAmount());

        // no item meta, means only material and amount can be serialized only
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return map;
        }

        Map<String, Object> diabloLikeMap = serializeDisplayName(item, map, meta);

        if (diabloLikeMap != null) {
            return diabloLikeMap;
        }

        if (meta.hasLore()) {
            map.put(LORE, Objects.requireNonNull(meta.getLore())
                    .stream()
                    .map(x -> x.replaceAll("§", "&"))
                    .collect(Collectors.toList()));
        }

        if (meta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) meta;
            map.put(POTION_TYPE, potionMeta.getBasePotionData());
        }

        return map;
        // now we need to do some funky stuff with item meta because of potions
        //return serializePotionMeta(map, meta);
    }
    /*@NotNull
    private static ItemStack deserializeInternal(Map<String, Object> map, ItemStack item, ItemMeta meta) {
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

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
            NoSuchFieldException e) {
                Professions.logError(e);
                item.setItemMeta(meta);
                return item;
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
                Professions.logError(e);
            }
        }
        item.setItemMeta(meta);
        return item;
    }*/

    public static String serializeMaterial(ItemStack item) {
        return serializeMaterial(item.getType(), (byte) item.getDurability());
    }

    public static String serializeMaterial(Material material, byte materialData) {
        return materialData == 0 ? material.name() : material.name() + ":" + materialData;
    }

    @Nullable
    private static Map<String, Object> serializeDisplayName(ItemStack item, Map<String, Object> map, ItemMeta meta) {
        // TODO THIS COULD BE A PROBLEM WITH DIABLOITEMS (SAVING THEIR CONFIG NAME !BASED ON DISPLAY NAME!) !!!
        if (!meta.hasDisplayName()) {
            return null;
        }
        final String displayName = meta.getDisplayName();

        // we primarily look for DiabloLike
        diablolike:
        if (Professions.isDiabloLikeHook()) {
            // get items from display name
            final List<DiabloItem> itemFromDisplayName = DiabloLike.getItemFromDisplayName(displayName);

            // diablolike returns null if there are no items (conventions? Pepega)
            if (itemFromDisplayName == null) {
                break diablolike;
            }

            // there could be multiple items with the same name but not the same material, so filter out the items
            // with incorrect material
            final List<DiabloItem> items = itemFromDisplayName
                    .stream()
                    .filter(x -> x.getItem().getType() == item.getType())
                    .collect(Collectors.toList());

            // we found more than one diablo item with the same name and material (this shit should not happen btw)
            if (items.size() > 1) {
                // log that we found the diablo item but there were duplicates
                ProfessionLogger.log(
                        "Found multiple DiabloItems for a single itemstack, diablo item must both have unique display" +
                                " and config name" +
                                displayName, Level.WARNING);
                ProfessionLogger.log("Duplicates: " +
                        items.stream().map(DiabloItem::getConfigName).collect(Collectors.joining(", ")), Level.WARNING);
            }
            // there was only one of a kind diabloitem
            else {
                map.put(DIABLO_ITEM, items.get(0).getConfigName());
                return map;

            }
        }

        // DiabloLike item not found, continue in serialization
        map.put(DISPLAY_NAME, displayName.replaceAll("§", "&"));

        return null;
    }
    /*@SuppressWarnings("unchecked")
    private static Map<String, Object> serializePotionMeta(Map<String, Object> map, ItemMeta meta) {
        Class<? extends ItemMeta> clazz = meta.getClass();
        if (!clazz.getSimpleName().equalsIgnoreCase("craftmetaitem")) {
            clazz = (Class<? extends ItemMeta>) clazz.getSuperclass();
        }
        if (meta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) meta;
            map.put(POTION_TYPE, CraftPotionUtil.fromBukkit(potionMeta.getBasePotionData()));
        }

        Map<String, NBTBase> unhandledTags;
        try {
            final Field unhandledTagsField = clazz.getDeclaredField("unhandledTags");
            unhandledTagsField.setAccessible(true);
            unhandledTags = (Map<String, NBTBase>) unhandledTagsField.get(meta);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Professions.logError(e);
            return map;
        }

        Map<String, NBTBase> internalTags = new HashMap<>(unhandledTags);
        try {
            final Method method = clazz.getDeclaredMethod("serializeInternal", Map.class);
            method.setAccessible(true);
            method.invoke(meta, internalTags);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Professions.logError(e);
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
                Professions.logError(e);
            }
        }


        return map;
    }*/


    /**
     * Calls {@link #getDescription(ItemType, List)} with {@link ItemType}'s description from file.
     *
     * @param itemType the {@link ItemType}
     * @param <A>      the {@link ItemType}
     *
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
     *
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
     *
     * @return a description of {@link ItemType}
     */
    public static <A extends ItemType<?>> List<String> getDescription(A itemType, List<String> description,
                                                                      UserProfessionData upd) {
        Map<String, Object> map = getItemTypeMap(itemType.getClass(), itemType.getFileId());
        List<String> desc = new ArrayList<>(description);
        for (Strings.ItemTypeEnum itemTypeEnum : Strings.ItemTypeEnum.values()) {
            String regex = "\\{" + itemTypeEnum + "}";
            Object mapObject = map.get(itemTypeEnum.s);

            String replacement;
            if (itemTypeEnum == LEVEL_REQ_COLOR) {
                if (upd != null) {
                    replacement = String.valueOf(SkillupColor.getSkillupColor(itemType, upd).getColor());
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
        @SuppressWarnings("unchecked") final File f =
                getItemTypeFile((Class<? extends ItemType<?>>) itemType.getClass());
        FileConfiguration loader = YamlConfiguration.loadConfiguration(f);

        // gem.yml -> items.1
        final String itemSection = ItemType.KEY_ITEMS + "." + itemType.getFileId() + ".";
        for (int i = 0; i < desc.size(); i++) {
            String s = desc.get(i);
            if (s.isEmpty()) {
                continue;
            }
            Matcher m = GENERIC_REGEX.matcher(s);
            if (!m.find()) {
                continue;
            }
            String section = itemSection + m.group(1);
            if (section.equals(itemSection.concat(LEVEL_REQ_COLOR.s))) {
                continue;
            }

            final Object obj = loader.get(section);

            // TODO log some error
            if (obj == null) {
                ProfessionLogger.log("Could not replace patterns in item type lore because no section " + section +
                        " was found in " + f.getName() + " file.", Level.SEVERE);
                ProfessionLogger.log("\"" + s + "\"", Level.INFO);
                continue;
            }

            // special case for list of strings
            if (obj instanceof List) {
                @SuppressWarnings("all")
                List list = (List) obj;
                // first we must save the strings below the list
                List<String> stringsBelow =
                        IntStream.range(i + 1, desc.size())
                                .mapToObj(desc::get)
                                .collect(Collectors.toList());

                // then we set the list
                for (int j = 0; j < list.size(); j++) {
                    final String ss = (String) list.get(j);
                    final String element = ss.isEmpty() ? "" : ChatColor.translateAlternateColorCodes('&', ss);

                    // - aa
                    // - bb
                    // - {desc} (size = 2)
                    // - ff
                    // - zz
                    // - kk
                    // ->
                    // - aa
                    // - bb
                    // - desc1 (i + 0)
                    // - desc2 (i + 1)
                    // - zz
                    // - kk
                    try {
                        desc.set(i + j, element);
                    } catch (IndexOutOfBoundsException e) {
                        desc.add(element);
                    }
                }

                for (int j = 0; j < stringsBelow.size(); j++) {
                    final String element = stringsBelow.get(j);
                    try {
                        // - aa
                        // - bb
                        // - desc1 (i + 0)
                        // - desc2 (i + 1)
                        // - zz
                        // - kk
                        // ->
                        // - aa
                        // - bb
                        // - desc1 (i + 0)
                        // - desc2 (i + 1)
                        // - ff (i + list size (2) + 0) (stringsBelow size = 3)
                        // - zz (i + list size (2) + 1)
                        // - kk (i + list size (2) + 2)
                        desc.set(i + list.size() + j, element);
                    } catch (IndexOutOfBoundsException e) {
                        desc.add(element);
                    }
                }
            } else {
                s = s.replaceAll(GENERIC_REGEX.pattern(),
                        obj.toString().isEmpty() ? "" : ChatColor.translateAlternateColorCodes('&', obj.toString()));
                desc.set(i, s);
            }

        }
        return desc;
    }

    // TODO move this to IO and pass down ConfigurationSection instead of the map

    /**
     * @param clazz the {@link ItemType} class
     * @param id    the {@link ItemType#getFileId()}
     * @param <A>   the {@link ItemType}
     *
     * @return the serialization of {@link ItemType}
     */
    public static <A extends ItemType<?>> Map<String, Object> getItemTypeMap(Class<A> clazz, int id) {
        File file = getItemTypeFile(clazz);
        if (!file.exists()) {
            throw new IllegalStateException("Object not yet serialized!");
        }
        String itemId = String.valueOf(id);
        FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
        if (!loader.isConfigurationSection(ItemType.KEY_ITEMS)) {
            throw new IllegalArgumentException(
                    clazz.getSimpleName() + " with id " + id + " not found in file! (" + ItemType.KEY_ITEMS + "." + id + ")");
        }
        ConfigurationSection itemsSection = loader.getConfigurationSection(ItemType.KEY_ITEMS);
        return ((MemorySection) Objects.requireNonNull(itemsSection.get(itemId))).getValues(false);
    }

    /**
     * @param clazz the {@link ItemType} class
     *
     * @return the file of {@link ItemType}
     */
    public static <T extends ItemType<?>> File getItemTypeFile(Class<T> clazz) {
        return new File(IOManager.getItemFolder(),
                clazz.getSimpleName().toLowerCase().replace("itemtype", "").concat(Utils.YML_EXTENSION));
    }

    /**
     * Calls the ItemStackBuilder constructor constructor
     *
     * @param mat the material
     *
     * @return an ItemStackBuilder object
     */
    public static ItemStackBuilder itemStackBuilder(Material mat) {
        return new ItemStackBuilder(mat);
    }

    @Override
    public void setup() throws Exception {

        // reset logging of diablo, this will only print message if 1+ items were not a diabloitem
        loggedDiablo = false;

        // clear logged files, read them from file
        ITEMS_LOGGED.clear();
        if (!ITEMS_LOGGED_FILE.exists()) {
            ITEMS_LOGGED_FILE.createNewFile();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ITEMS_LOGGED_FILE))) {

            // if clear cache, don't read
            if (!ReloadCommand.isClearCache()) {
                Object read;
                try {
                    while ((read = ois.readObject()) != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) read;
                        ITEMS_LOGGED.add(map);
                    }
                } catch (EOFException ignored) {
                }
            }
        }
    }

    @Override
    public void cleanup() throws Exception {
        if (!ITEMS_LOGGED_FILE.exists()) {
            ITEMS_LOGGED_FILE.createNewFile();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ITEMS_LOGGED_FILE))) {
            ITEMS_LOGGED.forEach(x -> {
                try {
                    oos.writeObject(x);
                } catch (IOException e) {
                    ProfessionLogger.logError(e);
                }
            });
        }
    }

    /**
     * Builder for {@link ItemStack}
     */
    public static class ItemStackBuilder {
        private final Material mat;
        private final ItemMeta meta;
        private int amount;
        private short damage;

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
         *
         * @return {@code this}
         */
        public ItemStackBuilder withLore(List<String> lore) {
            if (meta != null) {
                meta.setLore(lore);
            }
            return this;
        }

        /**
         * @param displayName the display name to set
         *
         * @return {@code this}
         */
        public ItemStackBuilder withDisplayName(String displayName) {
            if (meta != null) {
                meta.setDisplayName(
                        displayName.isEmpty() ? "" : ChatColor.translateAlternateColorCodes('&', displayName));
            }
            return this;
        }

        /**
         * @param amount the amount to set
         *
         * @return {@code this}
         */
        public ItemStackBuilder setAmount(int amount) {
            this.amount = amount;
            return this;
        }

        /**
         * @param damage the damage to set
         *
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
