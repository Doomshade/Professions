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

package git.doomshade.professions.api.item;

import git.doomshade.professions.utils.ILoadable;
import git.doomshade.professions.dynmap.ext.Markable;
import git.doomshade.professions.api.user.IUserProfessionData;
import git.doomshade.professions.data.ItemSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Requirements;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static git.doomshade.professions.utils.Strings.ItemTypeEnum.*;

/**
 * Implementation of {@link IItemType}
 *
 * @param <T> {@inheritDoc}
 *
 * @author Doomshade
 * @version 1.0
 * @see IItemType
 * @since 1.0
 */
public abstract class ItemType<T extends ConfigurationSerializable> extends Markable
        implements IItemType<T>, ConfigurationSerializable, Comparable<ItemType<T>>, ILoadable {

    public static final String KEY_ITEMS = "items";
    private static final int MARKER_LAYER = 1;
    private int exp, levelReq;
    private T item;
    private String name = "";
    private String configName = "";
    private List<String> description, restrictedWorlds;
    private ItemStack guiMaterial = new ItemStack(Material.CHEST);
    private int fileId = -1;
    private boolean ignoreSkillupColor;
    private int cost = -1;
    private boolean trainable = false;
    private Requirements inventoryRequirements = new Requirements();

    /**
     * Constructor for creation of the item type object
     *
     * @param object the object
     */
    public ItemType(T object) {
        this.setLevelReq(1);
        this.setExp(0);
        this.setObject(object);
        this.setMarkerSetId("");
        this.setVisible(false);
        this.description = new ArrayList<>(Settings.getSettings(ItemSettings.class).getDefaultLore());
        this.restrictedWorlds = new ArrayList<>();
        this.setIgnoreSkillupColor(false);
    }

    /**
     * @param clazz  the ItemType class
     * @param object the ItemType generic argument
     * @param <T>    the item
     * @param <Obj>  the ItemType
     *
     * @return an example instance of the ItemType
     *
     * @throws IllegalArgumentException if the ItemType class does not implement
     * {@link ItemType#ItemType(ConfigurationSerializable)}
     *                                  constructor
     */
    @SuppressWarnings("all")
    public static <T extends ConfigurationSerializable, Obj extends ItemType<T>> Obj getExampleItemType(
            Class<Obj> clazz, T object)
            throws IllegalArgumentException {
        try {
            // TODO this needs testing
            // changed getDeclaredConstructors[0] to getDeclaredConstructor(object.getClass())
            return (Obj) clazz.getDeclaredConstructor(object.getClass()).newInstance(object);
        } catch (Exception e) {
            throw new IllegalArgumentException(clazz.getSimpleName() + " does not implement ItemType(T) constructor!",
                    e);
        }
    }

    /**
     * Deserializes an ItemType from file with given id.
     *
     * @param clazz the {@link ItemType} class
     * @param id    the id of {@link ItemType}
     * @param <A>   the {@link ItemType}
     *
     * @return the {@link ItemType} if found deserialization was successful, null otherwise
     *
     * @throws ProfessionInitializationException when the deserialization is unsuccessful
     */
    @Nullable
    @SuppressWarnings("all")
    public static <A extends ItemType<?>> A deserialize(Class<A> clazz, int id) throws InitializationException {
        try {
            Map<String, Object> map = ItemUtils.getItemTypeMap(clazz, id);
            Constructor<?> c = clazz.getDeclaredConstructors()[0];
            c.setAccessible(true);

            // create a null object and pass it to the instance
            // we cannot directly pass null as it would think there are no arguments FOR SOME REASON
            final Object obj = null;
            final A instance = (A) c.newInstance(obj);
            instance.deserialize(id, map);
            return instance;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            ProfessionLogger.log("Could not deserialize " + clazz.getSimpleName()
                    + " from file as it does not override an ItemType(T) constructor!", Level.SEVERE);
            ProfessionLogger.logError(e);
        }
        return null;
    }

    /**
     * Deserializes the ItemType including its potential implementation of {@link ICraftableItemType}.
     *
     * @param id  the id of this {@link ItemType}
     * @param map the map
     *
     * @throws InitializationException if the initialization of this class is unsuccessful
     */
    protected void deserialize(int id, Map<String, Object> map) throws InitializationException {

        // setup the item type before throwing the ex as we use getOrDefault everywhere
        // and then log what's missing
        setFileId(id);
        setExp((int) map.getOrDefault(EXP.s, 0));
        setLevelReq((int) map.getOrDefault(LEVEL_REQ.s, Integer.MAX_VALUE));
        setName((String) map.getOrDefault(NAME.s, "Unknown name"));

        if (!getName().isEmpty()) {
            setName(ChatColor.translateAlternateColorCodes('&', getName()));
        }
        setGuiMaterial(ItemUtils.deserializeMaterial((String) map.get(MATERIAL.s)));
        //setHiddenWhenUnavailable((boolean) map.getOrDefault(HIDDEN.s, true));
        setIgnoreSkillupColor((boolean) map.getOrDefault(IGNORE_SKILLUP_COLOR.s, true));
        setDescription(ItemUtils.getItemTypeLore(this));
        setTrainable((boolean) map.getOrDefault(TRAINABLE.s, false));
        setTrainableCost((int) map.getOrDefault(TRAINABLE_COST.s, -1));

        // check for missing keys
        Set<String> list = Utils.getMissingKeys(map, Strings.ItemTypeEnum.values())
                .stream()
                .filter(x -> !x.equalsIgnoreCase(LEVEL_REQ_COLOR.s))
                .collect(Collectors.toSet());
        if (!list.isEmpty()) {
            throw new ProfessionInitializationException(getClass(), list, getFileId());
        }

        // requirements deserialization
        MemorySection invReqSection = (MemorySection) map.get(INVENTORY_REQUIREMENTS.s);
        try {
            setInventoryRequirements(Requirements.deserialize(invReqSection.getValues(false)));
        } catch (ConfigurationException e) {
            ProfessionLogger.logError(e, false);
        }

        // object deserialization
        MemorySection objSection = (MemorySection) map.get(OBJECT.s);
        try {
            ConfigurationSerialization.deserializeObject(objSection.getValues(true), this.getClass());
            setObject(deserializeObject(objSection.getValues(true)));
        } catch (ProfessionObjectInitializationException e) {
            ProfessionLogger.log(
                    "Failed to load object from " + ItemUtils.getItemTypeFile(getClass()).getName() + " with id " +
                            getFileId() + " (" + getConfigName() + ")", Level.WARNING);
            ProfessionLogger.logError(e, false);
        }
    }

    @Override
    public final int getFileId() throws UnsupportedOperationException {
        if (fileId < 0) {
            throw new UnsupportedOperationException("Cannot get the file ID of an example Item Type!");
        }
        return fileId;
    }

    /**
     * Sets the id of this item type
     *
     * @param fileId the id to set
     */
    private void setFileId(int fileId) {
        this.fileId = fileId;
        final String fileName = ItemUtils.getItemTypeFile(getClass()).getName();
        this.configName = fileName.substring(0, fileName.lastIndexOf('.')) + "." + fileId;
    }

    @Override
    public final String getConfigName() {
        if (configName.isEmpty()) {
            throw new UnsupportedOperationException("Cannot get the config name of an example Item Type!");
        }
        return configName;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final void setName(String name) {
        this.name = name;
    }

    @Override
    public final int getTrainableCost() {
        return cost;
    }

    @Override
    public final void setTrainableCost(int cost) {
        this.cost = cost;
    }

    @Override
    public final boolean isTrainable() {
        return trainable;
    }

    @Override
    public final void setTrainable(boolean trainable) {
        this.trainable = trainable;
    }

    @Override
    public final Requirements getInventoryRequirements() {
        return inventoryRequirements;
    }

    @Override
    public final void setInventoryRequirements(Requirements inventoryRequirements) {
        this.inventoryRequirements = inventoryRequirements;
    }

    @Override
    public final List<String> getRestrictedWorlds() {
        return restrictedWorlds;
    }

    @Override
    public final void setRestrictedWorlds(List<String> restrictedWorlds) {
        this.restrictedWorlds = restrictedWorlds;
    }

    @Override
    public final boolean isIgnoreSkillupColor() {
        return ignoreSkillupColor;
    }

    @Override
    public final void setIgnoreSkillupColor(boolean ignoreSkillupColor) {
        this.ignoreSkillupColor = ignoreSkillupColor;
    }

    @Override
    public ItemStack getIcon(@Nullable IUserProfessionData upd) {
        ItemStack icon = new ItemStack(getGuiMaterial());
        ItemMeta meta = icon.getItemMeta();

        if (meta == null) {
            return icon;
        }

        meta.setDisplayName(getName());
        final List<String> lore = ItemUtils.getDescription(this, getDescription(), (UserProfessionData) upd);

        if (upd != null) {
            Pattern regex = Pattern.compile("\\{" + INVENTORY_REQUIREMENTS.s + "}");
            updateLore(upd, lore, regex, getInventoryRequirements());
        }
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    protected void updateLore(@NotNull IUserProfessionData upd, List<String> lore, Pattern regex,
                              Requirements requirements) {
        lore.replaceAll(s -> {
            Matcher m = regex.matcher(s);
            if (!m.find()) {
                return s;
            }
            s = s.replaceAll(regex.pattern(),
                    requirements.toString(upd.getUser().getPlayer(), ChatColor.DARK_GREEN,
                            ChatColor.RED));
            return s;
        });
    }

    @Override
    public final List<String> getDescription() {
        return description;
    }

    @Override
    public final void setDescription(List<String> description) {
        this.description = description;
    }

    @Override
    public final ItemStack getGuiMaterial() {
        return guiMaterial;
    }

    @Override
    public final void setGuiMaterial(ItemStack guiMaterial) {
        this.guiMaterial = guiMaterial;
    }

    @Override
    public final T getObject() {
        return item;
    }

    @Override
    public final void setObject(T item) {
        this.item = item;
        if (name.isEmpty() && item != null) {
            this.name = item.toString();
        }
    }

    @Override
    public final int getExp() {
        return exp;
    }

    @Override
    public final void setExp(int exp) throws IllegalArgumentException {
        if (exp < 0) {
            throw new IllegalArgumentException("Exp cannot be < 0!");
        }
        this.exp = exp;
    }

    @Override
    public boolean equalsObject(T object) {
        return item.equals(object);
    }

    @Override
    public final boolean meetsLevelReq(int professionLevel) {
        return professionLevel >= levelReq;
    }

    @Override
    public final void addInventoryRequirement(ItemStack item) {
        inventoryRequirements.addRequirement(Objects.requireNonNull(item));
    }

    @Override
    public boolean meetsRequirements(Player player) {
        return inventoryRequirements.meetsRequirements(player);
    }

    @Override
    public final int getLevelReq() {
        return levelReq;
    }

    @Override
    public final void setLevelReq(int levelReq) {
        int cap = Settings.getExpSettings().getLevelCap();

        // sets the level req to 0 <= req <= global level cap
        this.levelReq = Math.min(Math.max(levelReq, 0), cap);
    }

    /**
     * @param map the map of serialized object
     *
     * @return the object
     *
     * @throws ProfessionObjectInitializationException if the object deserialization was unsuccessful
     */
    protected abstract T deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException;

    /**
     * Serializes the ItemType
     *
     * @return serialized item type
     */
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put(OBJECT.s, item.serialize());
        map.put(EXP.s, exp);
        map.put(LEVEL_REQ.s, levelReq);
        map.put(NAME.s, name);
        map.put(DESCRIPTION.s, description);
        map.put(MATERIAL.s, guiMaterial.getType().name() +
                (guiMaterial.getDurability() != 0 ? ":" + guiMaterial.getDurability() : ""));
        map.put(RESTRICTED_WORLDS.s, restrictedWorlds);
        map.put(IGNORE_SKILLUP_COLOR.s, ignoreSkillupColor);
        map.put(TRAINABLE.s, isTrainable());
        map.put(TRAINABLE_COST.s, getTrainableCost());
        map.put(INVENTORY_REQUIREMENTS.s, getInventoryRequirements().serialize());
        return map;
    }

    @Override
    public final int getLayer() {
        return MARKER_LAYER;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(OBJECT + ": " + item.serialize().toString())
                .append("\n")
                .append(EXP + ": " + exp)
                .append("\n")
                .append(LEVEL_REQ + ": " + levelReq)
                .append("\n")
                .append(NAME + ": " + name)
                .append("\n")
                .append(DESCRIPTION + ": " + description);
        return sb.toString();
    }

    @SuppressWarnings("all")
    public String toCompactString() {
        String name = ItemUtils.getItemTypeFile(getClass()).getName();
        StringBuilder sb = new StringBuilder("{")
                .append(name)
                .append(",")
                .append("config-id: " + configName)
                .append("}");
        return sb.toString();
    }

    /**
     * @param o the item type to compare to
     *
     * @return {@link Integer#compare(int, int)} where the arguments are: {@link #getLevelReq()} and {@code o.}{@link
     * #getLevelReq()}
     */
    @Override
    public int compareTo(ItemType<T> o) {
        return Integer.compare(getLevelReq(), o.getLevelReq());
    }
}
