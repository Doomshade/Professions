package git.doomshade.professions.profession.types;

import git.doomshade.professions.Professions;
import git.doomshade.professions.data.ExpSettings;
import git.doomshade.professions.data.ItemSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.*;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static git.doomshade.professions.utils.Strings.ItemTypeEnum.*;

/**
 * <p>{@link ProfessionEvent} returns an object of this to handle in a {@link Profession}</p>
 * <p>If you want to make your own type, make a class extend this and override all constructors!</p>
 * <p>To make a specialized item type (e.g. making this item craft-able - yields a result in a time with
 * given prerequisites or train-able from an NPC with {@link git.doomshade.professions.trait.TrainerTrait}) trait,
 * see {@link ICustomType} extensions</p>
 *
 * @param <T> the item type to look for in {@link ProfessionEvent}
 * @author Doomshade
 */
public abstract class ItemType<T> implements ConfigurationSerializable, Comparable<ItemType<T>> {

    public static final String KEY = "items";

    private int exp, levelReq;
    private T item;
    private File itemFile;
    private String name = "";
    private String configName = "";
    private List<String> description, restrictedWorlds;
    private ItemStack guiMaterial = new ItemStack(Material.CHEST);
    private int fileId = -1;
    private boolean ignoreSkillupColor;
    private int cost = -1;
    private boolean trainable = false;


    /**
     * Constructor for creation of the item type object
     *
     * @param object
     */
    public ItemType(T object) {
        this.itemFile = getFile(getClass());
        if (!itemFile.exists()) {
            try {
                itemFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.setLevelReq(1);
        this.setExp(0);
        this.setObject(object);
        this.description = new ArrayList<>(Settings.getSettings(ItemSettings.class).getDefaultLore());
        this.restrictedWorlds = new ArrayList<>();
        //this.setHiddenWhenUnavailable(false);
        this.setIgnoreSkillupColor(false);

        /*if (getClass().isAnnotationPresent(SerializeAdditionalType.class)) {
            SerializeAdditionalType annotation = getClass().getAnnotation(SerializeAdditionalType.class);
            for (Class<? extends ICustomTypeNew<?>> ictn : annotation.value()) {
                try {
                    final Constructor<? extends ICustomTypeNew<?>> declaredConstructor = ictn.getDeclaredConstructor(ItemType.class);
                    declaredConstructor.setAccessible(true);
                    ICustomTypeNew<?> iCustomTypeNew = declaredConstructor.newInstance(this);
                    addAdditionalData(iCustomTypeNew);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }*/
    }

    @SuppressWarnings("all")
    public static <T, Obj extends ItemType<T>> Obj getExampleItemType(Class<Obj> clazz, T object) {
        try {
            return (Obj) clazz.getDeclaredConstructors()[0].newInstance(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    @SuppressWarnings("all")
    public static <A extends ItemType<?>> A deserialize(Class<A> clazz, int id) throws ProfessionInitializationException {
        Map<String, Object> map = ItemUtils.getItemTypeMap(clazz, id);
        try {
            Constructor<?> c = clazz.getDeclaredConstructors()[0];
            c.setAccessible(true);

            // create a null object and pass it to the instance
            // we cannot directly pass null as it would think there are no arguments FOR SOME REASON
            final Object obj = null;
            final A instance = (A) c.newInstance(obj);
            instance.deserialize(id, map);
            return instance;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            Professions.log("Could not deserialize " + clazz.getSimpleName()
                    + " from file as it does not override an ItemType(T) constructor!", Level.SEVERE);
        }
        return null;
    }

    /**
     * Deserializes the ItemType including its potential implementation of {@link ICraftable}.
     *
     * @param id  the id of this itemtype
     * @param map the map
     * @throws ProfessionInitializationException if the initialization of this class is unsuccessful
     */
    public void deserialize(int id, Map<String, Object> map) throws ProfessionInitializationException {
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

        Set<String> list = Utils.getMissingKeys(map, Strings.ItemTypeEnum.values()).stream().filter(x -> !x.equalsIgnoreCase(LEVEL_REQ_COLOR.s)).collect(Collectors.toSet());

        if (!list.isEmpty()) {
            throw new ProfessionInitializationException(getClass(), list, getFileId());
        }
        MemorySection mem = (MemorySection) map.get(OBJECT.s);

        try {
            setObject(deserializeObject(mem.getValues(true)));
        } catch (ProfessionObjectInitializationException e) {
            Professions.log(e.getMessage(), Level.WARNING);
        } catch (NullPointerException e1) {
            Professions.log("Failed to load object from " + getFile().getName() + " with id " + getFileId() + " (" + getConfigName() + ")", Level.WARNING);
            e1.printStackTrace();
        }
    }

    /**
     * Serializes the ItemType
     *
     * @return serialized item type
     */
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(OBJECT.s, getSerializedObject());
        map.put(EXP.s, exp);
        map.put(LEVEL_REQ.s, levelReq);
        map.put(PROFTYPE.s, getDeclaredProfessionType().getSimpleName().substring(1).toLowerCase());
        map.put(NAME.s, name);
        map.put(DESCRIPTION.s, description);
        map.put(MATERIAL.s, guiMaterial.getType().name() + (guiMaterial.getDurability() != 0 ? ":" + guiMaterial.getDurability() : ""));
        map.put(RESTRICTED_WORLDS.s, restrictedWorlds);
        map.put(IGNORE_SKILLUP_COLOR.s, ignoreSkillupColor);
        map.put(TRAINABLE.s, isTrainable());
        map.put(TRAINABLE_COST.s, getTrainableCost());
        return map;
    }

    /**
     * Represents the number ID in the item type file.
     *
     * @return the ID number in the file
     */
    public final int getFileId() {
        if (fileId == -1) throw new UnsupportedOperationException("Cannot get the file ID of an example Item Type!");
        return fileId;
    }

    /**
     * Sets the id of this item type
     *
     * @param fileId the id to set
     */
    private void setFileId(int fileId) {
        this.fileId = fileId;
        final String fileName = getFile().getName();
        this.configName = fileName.substring(0, fileName.lastIndexOf('.')) + "." + fileId;
    }

    /**
     * Represents the config name of this item type in a "filename.fileId" format (filename without the .yml extension).
     * <p>Note that this method was created for consistent ID's of item types, this is only a generated ID from the file.</p>
     *
     * @return the config name
     */
    public final String getConfigName() {
        if (configName.isEmpty())
            throw new UnsupportedOperationException("Cannot get the config name of an example Item Type!");
        return configName;
    }

    private static <A extends ItemType<?>> File getFile(Class<A> clazz) {
        return ItemUtils.getItemTypeFile(clazz);
    }

    /**
     * This is basically a {@link ConfigurationSerializable#serialize()} but for the specific object.
     *
     * @return the map of serialization of the object
     */
    public abstract Map<String, Object> getSerializedObject();

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
    public final ItemStack getGuiMaterial() {
        return guiMaterial;
    }

    /**
     * Sets the material of this item type in a GUI
     *
     * @param guiMaterial the material to set
     */
    public final void setGuiMaterial(ItemStack guiMaterial) {
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
     * @return the color based on {@link git.doomshade.professions.user.User}'s {@link Profession} data
     */
    public final SkillupColor getSkillupColor(UserProfessionData upd) {
        return upd.getSkillupColor(this);
    }

    /**
     * @return {@code true} if this item type ignores the skillup color exp modifications
     * @see git.doomshade.professions.data.ProfessionExpSettings
     */
    public final boolean isIgnoreSkillupColor() {
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
     * @param upd the {@link git.doomshade.professions.user.User}'s {@link Profession} data to base the lore and {@link SkillupColor} around
     * @return the itemstack (icon) representation of this item type used in a GUI
     */
    public ItemStack getIcon(@Nullable UserProfessionData upd) {
        ItemStack icon = new ItemStack(getGuiMaterial());
        ItemMeta iconMeta = icon.getItemMeta();
        iconMeta.setDisplayName(getName());
        iconMeta.setLore(ItemUtils.getDescription(this, getDescription(), upd));
        icon.setItemMeta(iconMeta);
        return icon;
    }

    /**
     * @return the cost for training this item
     */
    public final int getTrainableCost() {
        return cost;
    }

    /**
     * Sets the cost of training this item
     *
     * @param cost the cost
     */
    public final void setTrainableCost(int cost) {
        this.cost = cost;
    }

    /**
     * @return {@code} true, if this item needs to be trained
     */
    public final boolean isTrainable() {
        return trainable;
    }

    /**
     * Sets the training requirement of this item
     *
     * @param trainable the requirement
     */
    public final void setTrainable(boolean trainable) {
        this.trainable = trainable;
    }

    public final String getTrainableId() {
        return getConfigName();
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
    @Nullable
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

        // sets the level req to 0 <= req <= global level cap
        this.levelReq = Math.min(Math.max(levelReq, 0), cap);
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(OBJECT + ": " + getSerializedObject().toString())
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

    @SuppressWarnings("all")
    public String toCompactString() {
        String name = getFile().getName();
        StringBuilder sb = new StringBuilder("{")
                .append(name)
                .append(",")
                .append("config-id: " + configName)
                .append("}");
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
     * Called after plugin is reloaded. Useful for reassigning objects to memory. Calls {@link #onLoad()} by default.
     */
    public void onReload() {
        onLoad();
    }


    /**
     * Called before plugin is reloaded. Useful for cleanups. Calls {@link #onDisable()} by default.
     */
    public void onPreReload() {
        onDisable();
    }

    /**
     * Called once plugin is fully loaded
     */
    public void onLoad() {

    }

    /**
     * Called once plugin is being disabled
     */
    public void onDisable() {

    }
}
