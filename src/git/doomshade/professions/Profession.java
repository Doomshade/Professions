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
 * The class for custom profession.
 *
 * @param <T> the profession type
 * @author Doomshade
 * @see IProfessionType
 * @see git.doomshade.professions.profession.professions.EnchantingProfession
 * @see git.doomshade.professions.profession.professions.JewelcraftingProfession
 * @see git.doomshade.professions.profession.professions.MiningProfession
 * @see git.doomshade.professions.profession.professions.SkinningProfession
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
    private ProfessionType pt = ProfessionType.PRIMARY;
    private HashSet<ItemTypeHolder<?>> items = new HashSet<>();
    private ItemStack icon = new ItemStack(Material.CHEST);
    private ProfessionSettings professionSettings = null;

    /**
     * Calling this constructor marks this class as initialized.
     */
    public Profession() {
        INITED_PROFESSIONS.add(getClass());
    }

    /**
     * Partly deserializes the profession.
     *
     * @param map the serialized form of profession
     * @return the deserialized professions
     */
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

    /**
     * Casts desired event to another one.
     *
     * @param event the profession event to cast
     * @param clazz the class to cast to
     * @param <A>   the generic argument of the event
     * @return the casted event
     */
    @SuppressWarnings({"unchecked", "unused"})
    protected static <A extends ItemType<?>> ProfessionEvent<A> getEvent(ProfessionEvent<?> event,
                                                                         Class<A> clazz) {
        return (ProfessionEvent<A>) event;
    }

    /**
     * @return the {@link IProfessionType} Type token
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the unique ID of this profession
     */
    public abstract String getID();

    /**
     * @return the name of this profession
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this profession
     *
     * @param name the name to set
     */
    protected final void setName(String name) {
        this.name = name;
    }

    /**
     * @return the icon of this profession (for GUI purposes)
     */
    public ItemStack getIcon() {
        return icon;
    }

    /**
     * Sets the icon of this profession
     *
     * @param icon the icon to set to
     */
    protected final void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    /**
     * Adds {@link ItemType}s to this profession to handle in {@link #onEvent(ProfessionEvent)}
     *
     * @param items
     */
    @SuppressWarnings("unchecked")
    protected final void addItems(Class<? extends ItemType<?>> items) {
        this.items.add(Professions.getItemTypeHolder(items));
    }

    /**
     * @return the handled {@link ItemType}'s holders
     */
    public final Set<ItemTypeHolder<?>> getItems() {
        return items;
    }

    /**
     * @return serialized form of this class
     */
    @Override
    public final Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(NAME, getName());
        map.put(TYPE, getProfessionType() == null ? ProfessionType.PRIMARY.name.toLowerCase()
                : getProfessionType().name.toLowerCase());
        map.put(ICON, icon.serialize());
        return map;
    }

    /**
     * @return the profession type (don't mistake it for {@link IProfessionType})
     */
    public ProfessionType getProfessionType() {
        return pt;
    }

    /**
     * Sets the profession type of this profession (don't mistake it for {@link IProfessionType})
     *
     * @param pt the profession type to set
     */
    protected final void setProfessionType(ProfessionType pt) {
        this.pt = pt;
    }

    /**
     * @return the profession settings
     */
    public ProfessionSettings getProfessionSettings() {
        return professionSettings;
    }

    /**
     * Sets the profession settings.
     *
     * @param professionSettings the settings to set
     */
    void setProfessionSettings(ProfessionSettings professionSettings) {
        this.professionSettings = professionSettings;
    }

    /**
     * Adds exp to the user based on the source
     *
     * @param exp    the exp to add
     * @param user   the user to add the exp to
     * @param source the source of exp
     * @return {@code true} if the exp was added successfully, {@code false} otherwise
     * @see User#addExp(double, Profession, ItemType)
     */
    protected final boolean addExp(double exp, User user, ItemType<?> source) {
        if (!user.isSuppressExpEvent())
            return user.addExp(exp, this, source);
        return false;
    }

    /**
     * @param user the user to get the profession data from
     * @return the user profession data
     * @see User#getProfessionData(Profession)
     */
    protected final UserProfessionData getUserProfessionData(User user) {
        return user.getProfessionData(this);
    }

    /**
     * Adds the exp to the user based on the event given.
     *
     * @param e the event
     * @return {@code true} if the exp was given, {@code false} otherwise
     */
    protected final boolean addExp(ProfessionEvent<?> e) {
        return addExp(e.getExp(), e.getPlayer(), e.getObject());
    }

    @Override
    public final String toString() {
        String type = getProfessionType() != null ? getProfessionType().toString() + ", " : "";
        return getColoredName() + ChatColor.RESET + ", " + type;
    }

    /**
     * The colored name of this profession. Use this instead of {@link #getName()} as the {@link #getName()} method does not translate '&'.
     *
     * @return the colored name of this profession
     */
    public final String getColoredName() {
        return ChatColor.translateAlternateColorCodes('&', getName());
    }

    /**
     * @param e    event to check for
     * @param item the event type
     * @return {@code true} if event has registered an object of item type, {@code false} otherwise
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

    private <ItemTypeClass extends ItemType<?>> boolean playerHasProfession(ProfessionEvent<ItemTypeClass> e) {
        return e.getPlayer().hasProfession(this);
    }

    /**
     * @param e the event to check the player's requirements in
     * @return {@code true} if the player meets all requirements, {@code false} otherwise
     */
    protected final <ItemTypeClass extends ItemType<?>> boolean playerMeetsRequirements(ProfessionEvent<ItemTypeClass> e) {
        if (!playerHasProfession(e)) {
            return false;
        }
        ItemTypeClass obj = e.getObject();
        UserProfessionData upd = getUserProfessionData(e.getPlayer());
        return upd != null && (obj.meetsLevelReq(upd.getLevel()) || upd.getUser().isBypass());
    }

    /**
     * Compares the profession types and then the names.
     *
     * @param o the profession to compare
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(Profession<?> o) {
        int compare = getProfessionType().compareTo(o.getProfessionType());
        if (compare == 0) {
            return ChatColor.stripColor(getColoredName()).compareTo(ChatColor.stripColor(o.getColoredName()));
        }
        return compare;
    }

    /**
     * Called before loaded from a file
     */
    @Deprecated
    public void onLoad() {
    }

    /**
     * Called after being loaded from a file
     */
    public void onPostLoad() {
    }

    /**
     * The profession types. Translated to czech
     */
    public enum ProfessionType {
        PRIMARY("primární"),
        SECONDARY("sekundární");

        private final String name;

        ProfessionType(String name) {
            this.name = name;
        }

        /**
         * @param professionType the string
         * @return the converted profession type from a string
         */
        public static ProfessionType fromString(String professionType) {
            for (ProfessionType type : values()) {
                if (type.name.equalsIgnoreCase(professionType) || type.name().equalsIgnoreCase(professionType)) {
                    return type;
                }
            }
            StringBuilder sb = new StringBuilder(professionType + " is not a valid profession type! (");
            for (ProfessionType type : values()) {
                sb.append(type.ordinal() + "=" + type.toString());
            }
            sb.append(")");
            throw new IllegalArgumentException(sb.toString());
        }

        /**
         * @param id the id
         * @return the converted profession type from an id based on ordinal() method
         */
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
