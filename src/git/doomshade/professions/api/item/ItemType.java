package git.doomshade.professions.api.item;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.user.IUserProfessionData;
import git.doomshade.professions.data.ExpSettings;
import git.doomshade.professions.data.ItemSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Requirements;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static git.doomshade.professions.utils.Strings.ItemTypeEnum.*;

/**
 * <p>{@link ProfessionEvent} returns an object of this to handle in a {@link Profession}</p>
 * <p>If you want to make your own type, make a class extend this and override all constructors!</p>
 * <p>To make a specialized item type (e.g. making this item craft-able - yields a result in a time with
 * given prerequisites or train-able from an NPC with {@link git.doomshade.professions.trait.TrainerTrait}) trait,
 * see extensions</p>
 *
 * @param <T> the item type to look for in {@link ProfessionEvent}
 * @author Doomshade
 */
public abstract class ItemType<T> implements ConfigurationSerializable, Comparable<ItemType<T>> {

    public static final String KEY = "items";

    private int exp, levelReq;
    private T item;
    private final File itemFile;
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
        this.itemFile = getFile(getClass());
        if (!itemFile.exists()) {
            try {
                itemFile.createNewFile();
            } catch (IOException e) {
                Professions.logError(e);
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
            throw new IllegalArgumentException(e);
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
            Professions.log("Could not deserialize " + clazz.getSimpleName()
                    + " from file as it does not override an ItemType(T) constructor!", Level.SEVERE);
            Professions.logError(e);
        } catch (ProfessionInitializationException ex) {
            Professions.logError(ex, false);
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

        // sets the config name aswell
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

        MemorySection invReqSection = (MemorySection) map.get(INVENTORY_REQUIREMENTS.s);
        try {
            setInventoryRequirements(Requirements.deserialize(invReqSection.getValues(false)));
        } catch (ConfigurationException e) {
            Professions.logError(e, false);
        }

        MemorySection mem = (MemorySection) map.get(OBJECT.s);

        try {
            setObject(deserializeObject(mem.getValues(true)));
        } catch (Exception e1) {
            Professions.log("Failed to load object from " + getFile().getName() + " with id " + getFileId() + " (" + getConfigName() + ")", Level.WARNING);
            Professions.logError(e1, false);
        }
    }

    /**
     * Serializes the ItemType
     *
     * @return serialized item type
     */
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(OBJECT.s, getSerializedObject());
        map.put(EXP.s, exp);
        map.put(LEVEL_REQ.s, levelReq);
        map.put(NAME.s, name);
        map.put(DESCRIPTION.s, description);
        map.put(MATERIAL.s, guiMaterial.getType().name() + (guiMaterial.getDurability() != 0 ? ":" + guiMaterial.getDurability() : ""));
        map.put(RESTRICTED_WORLDS.s, restrictedWorlds);
        map.put(IGNORE_SKILLUP_COLOR.s, ignoreSkillupColor);
        map.put(TRAINABLE.s, isTrainable());
        map.put(TRAINABLE_COST.s, getTrainableCost());
        map.put(INVENTORY_REQUIREMENTS.s, getInventoryRequirements().serialize());
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
     * @param upd the {@link User}'s {@link Profession} data to base the lore and {@link SkillupColor} around
     * @return the itemstack (icon) representation of this item type used in a GUI
     */
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
            for (int i = 0; i < lore.size(); i++) {
                String s = lore.get(i);
                Matcher m = regex.matcher(s);
                if (!m.find()) {
                    continue;
                }
                s = s.replaceAll(regex.pattern(),
                        getInventoryRequirements().toString(upd.getUser().getPlayer(), ChatColor.DARK_GREEN, ChatColor.RED));
                lore.set(i, s);
            }
        }
        meta.setLore(lore);
        icon.setItemMeta(meta);
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
     * This method is called during events, ensures that we got the correct item type
     * that gets further passed to profession
     *
     * @param object the object
     * @return {@code true} if the object equals to this generic argument object
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
     * Adds an additional inventory requirement for this item type
     *
     * @param item the inventory requirement to add
     */
    public final void addInventoryRequirement(ItemStack item) {
        inventoryRequirements.addRequirement(item);
    }

    /**
     * @return the inventory requirements
     */
    public final Requirements getInventoryRequirements() {
        return inventoryRequirements;
    }

    /**
     * Sets the inventory requirements, overriding existing ones
     *
     * @param inventoryRequirements the inventory requirements
     */
    public final void setInventoryRequirements(Requirements inventoryRequirements) {
        this.inventoryRequirements = inventoryRequirements;
    }

    /**
     * @param player the player to check for
     * @return {@code true} if the player meets requirements to proceed with the event, {@code false} otherwise. Does not check for level requirements!
     */
    public boolean meetsRequirements(Player player) {
        return inventoryRequirements.meetsRequirements(player);
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
