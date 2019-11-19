package git.doomshade.professions.profession.types;

import git.doomshade.professions.Professions;
import git.doomshade.professions.data.ExpSettings;
import git.doomshade.professions.data.ItemSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

import static git.doomshade.professions.utils.Strings.ItemTypeEnum.*;

/**
 * <li>{@link git.doomshade.professions.event.ProfessionEvent} returns an object of this to handle in a {@link git.doomshade.professions.Profession}</li>
 * <li>If you want to make your own type, make a class extend this and override all constructors!</li>
 * <li> {@link #ItemType()} </li>
 * <li> {@link #ItemType(Object, int)} </li>
 *
 * @param <T> the item type to look for in {@link git.doomshade.professions.event.ProfessionEvent}
 * @author Doomshade
 */
public abstract class ItemType<T> implements ConfigurationSerializable, Comparable<ItemType<T>> {

    public static final String KEY = "items";
    private int exp, levelReq;
    private T item;
    private File itemFile;
    private String name = "";
    private List<String> description, restrictedWorlds;
    private Material guiMaterial = Material.CHEST;
    private int itemTypeId;
    private boolean hiddenWhenUnavailable, ignoreSkillupColor;

    /**
     * Calls {@link #ItemType(Object, int)} with {@code null, 0} parameters
     */
    public ItemType() {
        this(null, 0);
    }

    /**
     * @param object the object
     * @param exp    the experience yield
     */
    public ItemType(T object, int exp) {
        this.itemFile = getFile(getClass());
        if (!itemFile.exists()) {
            try {
                itemFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.setLevelReq(1);
        this.setExp(exp);
        this.setObject(object);
        this.description = new ArrayList<>(Settings.getSettings(ItemSettings.class).getDefaultLore());
        this.restrictedWorlds = new ArrayList<>();
        this.setHiddenWhenUnavailable(false);
        this.setIgnoreSkillupColor(false);
    }

    /**
     * Deserializes an ItemType from file with given id.
     *
     * @param clazz the {@link ItemType} class
     * @param id    the id of {@link ItemType}
     * @param <A>   the {@link ItemType}
     * @return the {@link ItemType} if found deserialization was successful, null otherwise
     * @throws ProfessionInitializationException when the deserialization is unsuccessful
     */
    @Nullable
    public static <A extends ItemType<?>> A deserialize(Class<A> clazz, int id) throws ProfessionInitializationException {
        Map<String, Object> map = ItemUtils.getItemTypeMap(clazz, id);
        try {
            Constructor<A> c = clazz.getDeclaredConstructor();
            c.setAccessible(true);
            A instance = c.newInstance();
            instance.setId(id);
            instance.deserialize(map);
            return instance;
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            Professions.log("Could not deserialize " + clazz.getSimpleName()
                    + " from file as it does not override an ItemType() constructor!", Level.SEVERE);
        }
        return null;
    }

    /**
     * Deserializes the ItemType. You may override this method in order to deserialize the {@link ITrainable} and {@link ICraftable} interfaces.
     *
     * @param map the serialization map
     * @throws ProfessionInitializationException when the deserialization is unsuccessful
     */
    public void deserialize(Map<String, Object> map) throws ProfessionInitializationException {

        setExp((int) map.getOrDefault(EXP.s, 0));
        setLevelReq((int) map.getOrDefault(LEVEL_REQ.s, Integer.MAX_VALUE));
        setName((String) map.getOrDefault(NAME.s, "Unknown name"));

        if (!getName().isEmpty()) {
            setName(ChatColor.translateAlternateColorCodes('&', getName()));
        }
        setGuiMaterial(Material.getMaterial((String) map.getOrDefault(MATERIAL.s, "CHEST")));
        setHiddenWhenUnavailable((boolean) map.getOrDefault(HIDDEN.s, true));
        setIgnoreSkillupColor((boolean) map.getOrDefault(IGNORE_SKILLUP_COLOR.s, true));
        setDescription(ItemUtils.getItemTypeLore(this));
        MemorySection mem = (MemorySection) map.get(OBJECT.s);

        try {
            setObject(deserializeObject(mem.getValues(true)));
        } catch (ProfessionObjectInitializationException e) {
            Professions.log(e.getMessage(), Level.WARNING);
        }

        Set<String> list = Utils.getMissingKeys(map, Strings.ItemTypeEnum.values());

        if (!list.isEmpty()) {
            throw new ProfessionInitializationException(getClass(), list, getId());
        }

    }

    /**
     * Represents an ID in the item type file.
     *
     * @return the ID
     */
    public final int getId() {
        return itemTypeId;
    }

    /**
     * Sets the id of this item type (use with caution)
     *
     * @param id the id to set
     */
    public final void setId(int id) {
        this.itemTypeId = id;
    }

    private static <A extends ItemType<?>> File getFile(Class<A> clazz) {
        return ItemUtils.getFile(clazz);
    }

    /**
     * This is basically a {@link ConfigurationSerializable#serialize()} with an argument.
     *
     * @param object the object to serialize
     * @return the map of serialization of the object
     */
    protected abstract Map<String, Object> getSerializedObject(T object);

    /**
     * @param map the map of serialized object
     * @return the object
     * @throws ProfessionObjectInitializationException if the object deserialization was unsuccessful
     */
    protected abstract T deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException;

    /**
     * @return The {@link IProfessionType} that this item type can be assigned to.
     * @see IProfessionType
     */
    public abstract Class<? extends IProfessionType> getDeclaredProfessionType();

    /**
     * @return the description of this item type (used mainly for visual representation in an item)
     */
    public final List<String> getDescription() {
        return description;
    }

    /**
     * Sets the description of this item type
     *
     * @param description the description to set
     */
    public final void setDescription(List<String> description) {
        this.description = description;
    }

    /**
     * @return the name of this item type (used mainly for visual representation in an item)
     */
    public final String getName() {
        return name;
    }

    /**
     * Sets the name of this item type
     *
     * @param name the name to set
     */
    public final void setName(String name) {
        this.name = name;
    }

    /**
     * @return the material in a GUI (used for visual representation in an item)
     */
    public final Material getGuiMaterial() {
        return guiMaterial;
    }

    /**
     * Sets the material of this item type in a GUI
     *
     * @param guiMaterial the material to set
     */
    public final void setGuiMaterial(Material guiMaterial) {
        this.guiMaterial = guiMaterial;
    }

    /**
     * @return the restricted worlds this item type will not be handled in events
     */
    public final List<String> getRestrictedWorlds() {
        return restrictedWorlds;
    }

    /**
     * Sets the restricted worlds in which this item type will not be handled in events
     *
     * @param restrictedWorlds the restricted worlds
     */
    public final void setRestrictedWorlds(List<String> restrictedWorlds) {
        this.restrictedWorlds = restrictedWorlds;
    }

    /**
     * @param upd the user profession data
     * @return the color based on {@link git.doomshade.professions.user.User}'s {@link git.doomshade.professions.Profession} data
     */
    public final SkillupColor getSkillupColor(UserProfessionData upd) {
        return upd.getSkillupColor(this);
    }

    /**
     * @return {@code true} if this item type ignores the skillup color exp modifications
     * @see git.doomshade.professions.data.ProfessionExpSettings
     */
    public boolean isIgnoreSkillupColor() {
        return ignoreSkillupColor;
    }

    /**
     * Sets whether or not this item type should ignore the skillup color exp modifications
     *
     * @param ignoreSkillupColor whether or not to ignore skillup color
     * @see git.doomshade.professions.data.ProfessionExpSettings
     */
    public void setIgnoreSkillupColor(boolean ignoreSkillupColor) {
        this.ignoreSkillupColor = ignoreSkillupColor;
    }

    /**
     * @param upd the {@link git.doomshade.professions.user.User}'s {@link git.doomshade.professions.Profession} data to base the lore and {@link SkillupColor} around
     * @return the itemstack (icon) representation of this item type used in a GUI
     */
    public ItemStack getIcon(UserProfessionData upd) {
        ItemStack icon = new ItemStack(getGuiMaterial());
        ItemMeta iconMeta = icon.getItemMeta();
        iconMeta.setDisplayName(getName());
        iconMeta.setLore(ItemUtils.getDescription(this, getDescription(), upd));
        icon.setItemMeta(iconMeta);
        return icon;
    }

    /**
     * @return the file of this item type
     */
    public final File getFile() {
        return itemFile;
    }

    /**
     * @return the object (or objective) of this item type
     */
    public final T getObject() {
        return item;
    }

    /**
     * Sets the object (or objective) of this item type and also sets the name of this item type to {@code item.toString()}.
     *
     * @param item the object to set
     */
    public final void setObject(T item) {
        this.item = item;
        if (name.isEmpty() && item != null) {
            this.name = item.toString();
        }
    }

    /**
     * @return the exp yield of this item type
     */
    public final int getExp() {
        return exp;
    }

    /**
     * Sets the exp yield of this item type
     *
     * @param exp the exp to set
     */
    public final void setExp(int exp) {
        this.exp = exp;
    }

    /**
     * Serializes the ItemType. You may override this method in order to serialize the {@link ITrainable} and {@link ICraftable} interfaces.
     *
     * @return serialized item type
     */
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(OBJECT.s, getSerializedObject(item));
        map.put(EXP.s, exp);
        map.put(LEVEL_REQ.s, levelReq);
        map.put(PROFTYPE.s, getDeclaredProfessionType().getSimpleName().substring(1).toLowerCase());
        map.put(NAME.s, name);
        map.put(DESCRIPTION.s, description);
        map.put(MATERIAL.s, guiMaterial.name());
        map.put(RESTRICTED_WORLDS.s, restrictedWorlds);
        map.put(HIDDEN.s, hiddenWhenUnavailable);
        map.put(IGNORE_SKILLUP_COLOR.s, ignoreSkillupColor);
        return map;
    }

    /**
     * You may override this method for more complex logic.
     *
     * @param object the object
     * @return {@code true} if the object equals to this class' object
     */
    public boolean equalsObject(T object) {
        return item.equals(object);
    }

    /**
     * @param professionLevel the profession level
     * @return {@code true} if the profession level meets {@link #getLevelReq()}
     */
    public final boolean meetsLevelReq(int professionLevel) {
        return professionLevel >= levelReq;
    }

    /**
     * @return the level requirement of this item type
     */
    public final int getLevelReq() {
        return levelReq;
    }

    /**
     * Sets the level requirement of this item type
     *
     * @param levelReq the level to set
     */
    public final void setLevelReq(int levelReq) {
        int cap = Settings.getSettings(ExpSettings.class).getLevelCap();

        // nastaví na cap, pokud je levelReq větší než cap
        this.levelReq = Math.min(/* nastaví na 0, pokud je levelReq menší než 0*/Math.max(levelReq, 0), cap);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(OBJECT + ": " + getSerializedObject(item).toString())
                .append("\n")
                .append(EXP + ": " + exp)
                .append("\n")
                .append(LEVEL_REQ + ": " + levelReq)
                .append("\n")
                .append(PROFTYPE + ": " + getDeclaredProfessionType().getSimpleName().substring(1).toLowerCase())
                .append("\n")
                .append(NAME + ": " + name)
                .append("\n")
                .append(DESCRIPTION + ": " + description);
        return sb.toString();
    }

    /**
     * @param o the item type to compare to
     * @return {@link Integer#compare(int, int)} where the arguments are: {@link #getLevelReq()} and {@code o.}{@link #getLevelReq()}
     */
    @Override
    public int compareTo(ItemType<T> o) {
        return Integer.compare(getLevelReq(), o.getLevelReq());
    }

    /**
     * Whether or not to hide the item type in a GUI if it doesn't meet requirements
     *
     * @return {@code true} if it is hidden when unavailable, {@code false} otherwise
     */
    public boolean isHiddenWhenUnavailable() {
        return hiddenWhenUnavailable;
    }

    /**
     * Sets the item type to be or not to be hidden when the requirements are not met
     *
     * @param hiddenWhenUnavailable whether or not the item type should be hidden when unavailable
     */
    public final void setHiddenWhenUnavailable(boolean hiddenWhenUnavailable) {
        this.hiddenWhenUnavailable = hiddenWhenUnavailable;
    }


}
