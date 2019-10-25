package git.doomshade.professions.profession.types;

import git.doomshade.professions.Professions;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.Backup;
import git.doomshade.professions.utils.ItemUtils;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <li>ProfessionEvent<T> class returns ItemType object for you to handle in a
 * class extending Profession</li>
 * <li>If you want to make your own type, make a class extend this and override
 * all constructors!</li>
 *
 * @param <T> the item type to look for in events
 */
public abstract class ItemType<T> implements ConfigurationSerializable, Backup, Comparable<ItemType<T>> {

    public static final String KEY = "items";
    private int exp, levelReq;
    private T item;
    private File itemFile;
    private String name = "";
    private List<String> description, restrictedWorlds;
    private Material guiMaterial = Material.CHEST;
    private int itemTypeId;
    private boolean hiddenWhenUnavailable;

    public ItemType(){
        this(null, 100);
    }

    public ItemType(T object, int exp){
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
        this.description = new ArrayList<>(Settings.getInstance().getItemSettings().getDefaultLore());
        this.restrictedWorlds = new ArrayList<>();
        this.setHiddenWhenUnavailable(false);
    }

    void deserialize(Map<String, Object> map, int id) {
        MemorySection mem = (MemorySection) map.get(Key.OBJECT.toString());
        if (mem != null) {
            setObject(deserializeObject(mem.getValues(true)));
        }
        setId(id);
        setExp((int) map.get(Key.EXP.toString()));
        setLevelReq((int) map.get(Key.LEVEL_REQ.toString()));
        setName((String) map.get(Key.NAME.toString()));
        if (!getName().isEmpty()) {
            setName(ChatColor.translateAlternateColorCodes('&', getName()));
        }
        setDescription(ItemUtils.getItemTypeLore(this));
        setGuiMaterial(Material.getMaterial((String) map.get(Key.MATERIAL.toString())));
        setHiddenWhenUnavailable((boolean) map.get(Key.HIDDEN.toString()));
    }

    @Nullable
    public static <A extends ItemType<?>> A deserialize(Class<A> clazz, int id) {
        Map<String, Object> map = ItemUtils.getItemTypeMap(clazz, id);
        try {
            Constructor<A> c = clazz.getDeclaredConstructor();
            c.setAccessible(true);
            A instance = c.newInstance();
            instance.deserialize(map, id);
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            Professions.getInstance().sendConsoleMessage("Could not deserialize " + clazz.getSimpleName()
                    + " from file as it does not override an ItemType() constructor!");
        }
        return null;
    }

    private static <A extends ItemType<?>> File getFile(Class<A> clazz) {
        return ItemUtils.getFile(clazz);
    }

    protected abstract Map<String, Object> getSerializedObject(T object);

    protected abstract T deserializeObject(Map<String, Object> map);

    public abstract Class<? extends IProfessionType> getDeclaredProfessionType();

    public final List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public final String getName() {
        return name;
    }

    public void setName(String name) {
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

    public final int getId() {
        return itemTypeId;
    }

    final void setId(int id) {
        this.itemTypeId = id;
    }

    public SkillupColor getSkillupColor(UserProfessionData upd) {
        return upd.getSkillupColor(this);
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
    public File[] getFiles() {
        return new File[]{itemFile};
    }

    public final T getObject() {
        return item;
    }

    public final void setObject(T item) {
        this.item = item;
        if (name.isEmpty() && item != null){
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
        map.put(Key.OBJECT.toString(), getSerializedObject(item));
        map.put(Key.EXP.toString(), exp);
        map.put(Key.LEVEL_REQ.toString(), levelReq);
        map.put(Key.PROFTYPE.toString(), getDeclaredProfessionType().getSimpleName().substring(1).toLowerCase());
        map.put(Key.NAME.toString(), name);
        map.put(Key.DESCRIPTION.toString(), description);
        map.put(Key.MATERIAL.toString(), guiMaterial.name());
        map.put(Key.RESTRICTED_WORLDS.toString(), restrictedWorlds);
        map.put(Key.HIDDEN.toString(), hiddenWhenUnavailable);
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
        int cap = Settings.getInstance().getExpSettings().getLevelCap();

        // nastaví na cap, pokud je levelReq větší než cap
        this.levelReq = Math.min(/* nastaví na 0, pokud je levelReq menší než 0*/Math.max(levelReq, 0), cap);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Key.OBJECT + ": " + getSerializedObject(item).toString() + "\n");
        sb.append(Key.EXP + ": " + exp + "\n");
        sb.append(Key.LEVEL_REQ + ": " + levelReq + "\n");
        sb.append(Key.PROFTYPE + ": " + getDeclaredProfessionType().getSimpleName().substring(1).toLowerCase() + "\n");
        sb.append(Key.NAME + ": " + name + "\n");
        sb.append(Key.DESCRIPTION + ": " + description);
        return sb.toString();
    }

    @Override
    public int compareTo(ItemType<T> o) {
        return getLevelReq() > o.getLevelReq() ? 1 : getLevelReq() < o.getLevelReq() ? -1 : 0;
    }

    public boolean isHiddenWhenUnavailable() {
        return hiddenWhenUnavailable;
    }

    public final void setHiddenWhenUnavailable(boolean hiddenWhenUnavailable) {
        this.hiddenWhenUnavailable = hiddenWhenUnavailable;
    }

    public enum Key {
        LEVEL_REQ("level-req"),
        PROFTYPE("type-unchangable"),
        EXP("exp"), OBJECT("object"),
        NAME("name"),
        DESCRIPTION("description"),
        MATERIAL("gui-material"),
        RESTRICTED_WORLDS("restricted-worlds"),
        ITEM_REQUIREMENTS("item-requirements"),
        RESULT("result"), CRAFTING_TIME("crafting-time"),
        INVENTORY_REQUIREMENTS("inventory-requirements"),
        HIDDEN("hidden-when-unavailable"),
        LEVEL_REQ_COLOR("level-req-color");

        private String s;

        Key(String s) {
            this.s = s;

        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return s;
        }
    }

}
