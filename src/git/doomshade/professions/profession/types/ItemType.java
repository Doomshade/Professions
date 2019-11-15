package git.doomshade.professions.profession.types;

import git.doomshade.professions.Professions;
import git.doomshade.professions.data.ExpSettings;
import git.doomshade.professions.data.ItemSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.IBackup;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static git.doomshade.professions.utils.Strings.ItemTypeEnum.*;

/**
 * <li>ProfessionEvent<T> class returns ItemType object for you to handle in a
 * class extending Profession</li>
 * <li>If you want to make your own type, make a class extend this and override
 * all constructors!</li>
 * <li> {@link #ItemType()} </li>
 * <li> {@link #ItemType(Object, int)} </li>
 *
 * @param <T> the item type to look for in events
 */
public abstract class ItemType<T> implements ConfigurationSerializable, IBackup, Comparable<ItemType<T>> {

    public static final String KEY = "items";
    private int exp, levelReq;
    private T item;
    private File itemFile;
    private String name = "";
    private List<String> description, restrictedWorlds;
    private Material guiMaterial = Material.CHEST;
    private int itemTypeId;
    private boolean hiddenWhenUnavailable, ignoreSkillupColor;

    public ItemType() {
        this(null, 100);
    }

    /**
     * @param object
     * @param exp
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
            Professions.getInstance().sendConsoleMessage("Could not deserialize " + clazz.getSimpleName()
                    + " from file as it does not override an ItemType() constructor!");
        }
        return null;
    }

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
            Professions.printError(e.getMessage(), Level.WARNING);
        }

        List<String> list = Utils.getMissingKeys(map, Strings.ItemTypeEnum.values());

        if (!list.isEmpty()) {
            throw new ProfessionInitializationException(getClass(), list, getId());
        }

    }

    public final int getId() {
        return itemTypeId;
    }

    public final void setId(int id) {
        this.itemTypeId = id;
    }

    private static <A extends ItemType<?>> File getFile(Class<A> clazz) {
        return ItemUtils.getFile(clazz);
    }

    protected abstract Map<String, Object> getSerializedObject(T object);

    protected abstract T deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException;

    public abstract Class<? extends IProfessionType> getDeclaredProfessionType();

    public final List<String> getDescription() {
        return description;
    }

    public final void setDescription(List<String> description) {
        this.description = description;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final Material getGuiMaterial() {
        return guiMaterial;
    }

    public final void setGuiMaterial(Material guiMaterial) {
        this.guiMaterial = guiMaterial;
    }

    public final List<String> getRestrictedWorlds() {
        return restrictedWorlds;
    }

    public final void setRestrictedWorlds(List<String> restrictedWorlds) {
        this.restrictedWorlds = restrictedWorlds;
    }

    public final SkillupColor getSkillupColor(UserProfessionData upd) {
        return upd.getSkillupColor(this);
    }

    public boolean isIgnoreSkillupColor() {
        return ignoreSkillupColor;
    }

    public void setIgnoreSkillupColor(boolean ignoreSkillupColor) {
        this.ignoreSkillupColor = ignoreSkillupColor;
    }

    public ItemStack getIcon(UserProfessionData upd) {
        ItemStack icon = new ItemStack(getGuiMaterial());
        ItemMeta iconMeta = icon.getItemMeta();
        iconMeta.setDisplayName(getName());
        iconMeta.setLore(ItemUtils.getDescription(this, getDescription(), upd));
        icon.setItemMeta(iconMeta);
        return icon;
    }

    @Override
    public final File[] getFiles() {
        return new File[]{itemFile};
    }

    public final T getObject() {
        return item;
    }

    public final void setObject(T item) {
        this.item = item;
        if (name.isEmpty() && item != null) {
            this.name = item.toString();
        }
    }

    public final int getExp() {
        return exp;
    }

    public final void setExp(int exp) {
        this.exp = exp;
    }

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

    public boolean isValid(T t) {
        return item.equals(t);
    }

    public final boolean meetsLevelReq(int professionLevel) {
        return professionLevel >= levelReq;
    }

    public final int getLevelReq() {
        return levelReq;
    }

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

    @Override
    public int compareTo(ItemType<T> o) {
        return Integer.compare(getLevelReq(), o.getLevelReq());
    }

    public boolean isHiddenWhenUnavailable() {
        return hiddenWhenUnavailable;
    }

    public final void setHiddenWhenUnavailable(boolean hiddenWhenUnavailable) {
        this.hiddenWhenUnavailable = hiddenWhenUnavailable;
    }


}
