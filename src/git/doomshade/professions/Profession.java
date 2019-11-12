package git.doomshade.professions;

import com.google.common.reflect.TypeToken;
import git.doomshade.professions.data.ProfessionSettings;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.IProfessionEventable;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Interfaces extending IProfessionType
 * <ul>
 * <li>ICrafting</li>
 * <li>IEnchanting</li>
 * <li>IGathering</li>
 * <li>IHunting</li>
 * <li>IMining</li>
 * </ul>
 *
 * @param <T> Profession Type listed above, you may create your own profession
 *            type by making an interface extending IProfessionType, you must
 *            then register it by calling
 */
public abstract class Profession<T extends IProfessionType>
        implements Listener, ConfigurationSerializable, Comparable<Profession<?>>, IProfessionEventable {

    private static final String ICON = "icon";
    private static final String TYPE = "type";
    private static final String NAME = "name";
    static final HashSet<Class<? extends Profession>> INITED_PROFESSIONS = new HashSet<>();
    @SuppressWarnings("serial")
    private final TypeToken<T> typeToken = new TypeToken<T>(getClass()) {
    };
    private final Type type = typeToken.getType();
    private String name = "";
    private ProfessionType pt = ProfessionType.PRIMARNI;
    private HashSet<ItemTypeHolder<?>> items = new HashSet<>();
    private ItemStack icon = new ItemStack(Material.CHEST);
    private ProfessionSettings professionSettings = null;

    public Profession() {
        INITED_PROFESSIONS.add(getClass());
    }

    @SuppressWarnings("rawtypes")
    public static Profession<?> deserialize(Map<String, Object> map) {
        return new Profession() {

            @Override
            public String getID() {
                return null;
            }

            @Override
            public String getName() {
                return map.containsKey(NAME) ? (String) map.get(NAME) : "";
            }

            @Override
            public ProfessionType getProfessionType() {
                if (!map.containsKey(TYPE)) {
                    return null;
                }
                Object o = map.get(TYPE);
                if (o instanceof String) {
                    return ProfessionType.fromString((String) o);
                } else if (o instanceof Number) {
                    return ProfessionType.fromId((int) o);
                }
                throw new IllegalStateException("Type of " + getName() + " is not a string nor a number!");
            }

            @Override
            public ItemStack getIcon() {
                return ItemStack.deserialize(((MemorySection) map.get(ICON)).getValues(true));
            }

            @Override
            public void onEvent(ProfessionEvent e) {

            }

            @Override
            public Type getType() {
                return null;
            }

            @Override
            public ProfessionSettings getProfessionSettings() {
                return null;
            }


            // DONUT DELETE
            @Override
            public int compareTo(Object o) {
                return 0;
            }

            @Override
            public int compareTo(Profession o) {
                return 0;
            }


        };
    }

    @SuppressWarnings({"unchecked", "unused"})
    protected final static <A extends ItemType<?>> ProfessionEvent<A> getEvent(ProfessionEvent<?> event,
                                                                               Class<A> clazz) {
        return (ProfessionEvent<A>) event;
    }

    public Type getType() {
        return type;
    }

    public abstract String getID();

    public String getName() {
        return this.name;
    }

    protected final void setName(String name) {
        this.name = name;
    }

    public ItemStack getIcon() {
        return icon;
    }

    protected final void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    @SuppressWarnings("unchecked")
    protected final void addItems(Class<? extends ItemType<?>> items) {
        this.items.add(Professions.getItemTypeHolder(items));
    }

    public final Set<ItemTypeHolder<?>> getItems() {
        return items;
    }

    @Override
    public final Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(NAME, getName());
        map.put(TYPE, getProfessionType() == null ? ProfessionType.PRIMARNI.name.toLowerCase()
                : getProfessionType().name.toLowerCase());
        map.put(ICON, icon.serialize());
        return map;
    }

    public ProfessionType getProfessionType() {
        return pt;
    }

    protected final void setProfessionType(ProfessionType pt) {
        this.pt = pt;
    }

    public ProfessionSettings getProfessionSettings() {
        return professionSettings;
    }

    public void setProfessionSettings(ProfessionSettings professionSettings) {
        this.professionSettings = professionSettings;
    }

    protected final boolean addExp(double exp, User user, ItemType<?> source) {
        if (!user.isSuppressExpEvent())
            return user.addExp(exp, this, source);
        return false;
    }

    protected final UserProfessionData getUserProfessionData(User user) {
        return user.getProfessionData(this);
    }

    protected final boolean addExp(ProfessionEvent<?> e) {
        return addExp(e.getExp(), e.getPlayer(), e.getObject());
    }

    @Override
    public final String toString() {
        String type = getProfessionType() != null ? getProfessionType().toString() + ", " : "";
        return getColoredName() + ChatColor.RESET + ", " + type;
    }

    public final String getColoredName() {
        return ChatColor.translateAlternateColorCodes('&', getName());
    }

    /**
     * @param e    event to check for
     * @param item the event type
     * @return true if event has registered an object of item type
     */
    protected final <ItemTypeClass extends ItemType<?>> boolean isValidEvent(ProfessionEvent<?> e, Class<ItemTypeClass> item) {
        ItemType<?> obj = e.getObject();
        for (ItemTypeHolder<?> ith : items) {
            for (ItemType<?> it : ith) {
                if (it.getClass().equals(obj.getClass())) {
                    return playerHasProfession(e) && obj.getClass().getSimpleName().equalsIgnoreCase(item.getSimpleName());
                }
            }
        }
        return false;
    }

    /**
     * @param e
     * @return
     */
    private final <ItemTypeClass extends ItemType<?>> boolean playerHasProfession(ProfessionEvent<ItemTypeClass> e) {
        return e.getPlayer().hasProfession(this);
    }

    /**
     * @param e
     * @return
     */
    protected final <ItemTypeClass extends ItemType<?>> boolean playerMeetsRequirements(ProfessionEvent<ItemTypeClass> e) {
        if (!playerHasProfession(e)) {
            return false;
        }
        ItemTypeClass obj = e.getObject();
        UserProfessionData upd = getUserProfessionData(e.getPlayer());
        return upd != null && (obj.meetsLevelReq(upd.getLevel()) || upd.getUser().isBypass());
    }

    @Override
    public int compareTo(Profession<?> o) {
        int compare = getProfessionType().compareTo(o.getProfessionType());
        if (compare == 0) {
            return ChatColor.stripColor(getColoredName()).compareTo(ChatColor.stripColor(o.getColoredName()));
        }
        return compare;
    }

    public void onLoad() {
    }

    public void onPostLoad() {
    }

    public enum ProfessionType {
        PRIMARNI("primární"),
        SEKUNDARNI("sekundární");

        private final String name;

        ProfessionType(String name) {
            this.name = name;
        }

        public static ProfessionType fromString(String prof) {
            for (ProfessionType type : values()) {
                if (type.name.equalsIgnoreCase(prof)) {
                    return type;
                }
            }
            StringBuilder sb = new StringBuilder(prof + " is not a valid profession type! (");
            for (ProfessionType type : values()) {
                sb.append(type.ordinal() + "=" + type.toString());
            }
            sb.append(")");
            throw new IllegalArgumentException(sb.toString());
        }

        public static ProfessionType fromId(int id) {
            for (ProfessionType type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            StringBuilder sb = new StringBuilder(id + " is not a valid profession id type! (");
            for (ProfessionType type : values()) {
                sb.append(type.ordinal() + "=" + type.toString());
            }
            sb.append(")");
            throw new IllegalArgumentException(sb.toString());
        }

        @Override
        public String toString() {
            return String.valueOf(name.toCharArray()[0]).toUpperCase() + name.toLowerCase().substring(1);
        }

    }
}
