package git.doomshade.professions.profession;

import git.doomshade.professions.Professions;
import git.doomshade.professions.data.ProfessionSettingsManager;
import git.doomshade.professions.data.ProfessionSpecificDefaultsSettings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.profession.professions.enchanting.EnchantingProfession;
import git.doomshade.professions.profession.professions.jewelcrafting.JewelcraftingProfession;
import git.doomshade.professions.profession.professions.mining.MiningProfession;
import git.doomshade.professions.profession.professions.skinning.SkinningProfession;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.ISetup;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The class for custom profession.
 *
 * @author Doomshade
 * @version 1.0
 * @see IProfessionType
 * @see EnchantingProfession
 * @see JewelcraftingProfession
 * @see MiningProfession
 * @see SkinningProfession
 * @see git.doomshade.professions.profession.professions.alchemy.AlchemyProfession
 * @see git.doomshade.professions.profession.professions.smelting.SmeltingProfession
 */
public abstract class Profession implements Listener, Comparable<Profession> {

    static final HashSet<Class<? extends Profession>> INITED_PROFESSIONS = new HashSet<>();
    private final HashSet<String> requiredPlugins = new HashSet<>();
    private final String name;
    private final ProfessionType pt;
    private HashSet<ItemTypeHolder<?>> items = new HashSet<>();
    private final ItemStack icon;
    private final File file;
    private final ProfessionSettingsManager professionSettings;

    public Profession() {
        this(false);
    }

    private Profession(boolean ignoreInitializationError) {
        ensureNotInitialized(ignoreInitializationError);

        String fileName = getClass().getSimpleName().toLowerCase().replace("profession", "");
        this.file = new File(Professions.getInstance().getProfessionFolder(), fileName.concat(Utils.YML_EXTENSION));
        if (!file.exists() && !fileName.isEmpty()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // initialize settings AFTER initializing file as settings require the profession file!
        ProfessionSettingsManager settings = new ProfessionSettingsManager(this);
        try {
            settings.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.professionSettings = settings;

        ProfessionSpecificDefaultsSettings defaults = settings.getSettings(ProfessionSpecificDefaultsSettings.class);
        this.name = defaults.getName();
        this.icon = defaults.getIcon();
        this.pt = defaults.getProfessionType();


    }

    private void ensureNotInitialized(boolean ignoreError) {
        if (!INITED_PROFESSIONS.add(getClass())) {
            if (!ignoreError)
                try {
                    throw new IllegalAccessException("Do not access professions by their constructor, use ProfessionManager#getProfession(String) instead!");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
        }

    }

    /**
     * Gets the settings file for profession
     *
     * @return the settings file for profession
     */
    public final File getFile() {
        return file;
    }

    /**
     * Casts desired event to another one.
     *
     * @param event the profession event to cast
     * @param <A>   the generic argument of the event
     * @param clazz the generic argument class
     * @return the casted event
     */
    @SuppressWarnings({"unchecked"})
    protected static <A extends ItemType<?>> Optional<ProfessionEvent<A>> getEvent(ProfessionEvent<?> event, Class<A> clazz) throws ClassCastException {
        try {
            return Optional.of((ProfessionEvent<A>) event);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Casts desired event to another one.
     *
     * @param event the profession event to cast
     * @param <A>   the generic argument of the event
     * @param clazz the generic argument class
     * @return the casted event
     */
    protected static <A extends ItemType<?>> Optional<ProfessionEvent<A>> getEvent(ProfessionEventWrapper<?> event, Class<A> clazz) throws ClassCastException {
        return getEvent(event.event, clazz);
    }

    protected static <A extends ItemType<?>> ProfessionEvent<A> getEventUnsafe(ProfessionEvent<?> event, Class<A> clazz) throws ClassCastException {
        return getEvent(event, clazz).get();
    }

    protected static <A extends ItemType<?>> ProfessionEvent<A> getEventUnsafe(ProfessionEventWrapper<?> event, Class<A> clazz) throws ClassCastException {
        return getEvent(event, clazz).get();
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
     * @return the icon of this profession (for GUI purposes)
     */
    public ItemStack getIcon() {
        return icon;
    }

    /**
     * Adds {@link ItemType}s to this profession to handle in
     *
     * @param items the items
     */
    protected final void addItems(Class<? extends ItemType<?>> items) {
        this.items.add(Professions.getProfessionManager().getItemTypeHolder(items));
    }

    /**
     * @return the handled {@link ItemType}'s holders
     */
    public final Set<ItemTypeHolder<?>> getItems() {
        return items;
    }


    /**
     * @return the profession type (don't mistake it for {@link IProfessionType})
     */
    public ProfessionType getProfessionType() {
        return pt;
    }

    /**
     * @return the profession settings
     */
    public ProfessionSettingsManager getProfessionSettings() {
        return professionSettings;
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
        return addExp(e.getExp(), e.getPlayer(), e.getItemType());
    }

    @Override
    public final String toString() {
        String type = getProfessionType() != null ? getProfessionType().toString() + ", " : "";
        return getColoredName() + ChatColor.RESET + ", " + type;
    }

    /**
     * The colored name of this profession. Use this instead of getName() as that method does not translate the colour.
     *
     * @return the colored name of this profession
     */
    public final String getColoredName() {
        return ChatColor.translateAlternateColorCodes('&', getName());
    }

    /**
     * @param e               event to check for
     * @param <ItemTypeClass> the item type
     * @param errorMessage    whether or not to log error message
     * @return {@code true} if event has registered an object of item type, {@code false} otherwise
     */
    protected final <ItemTypeClass extends ItemType<?>> boolean isValidEvent(ProfessionEvent<ItemTypeClass> e, boolean errorMessage) {
        final boolean playerHasProf = playerHasProfession(e);
        if (!playerHasProf) {
            e.setCancelled(true);
            if (errorMessage) {
                final User player = e.getPlayer();
                player.sendMessage(new Messages.MessageBuilder(Messages.Global.PROFESSION_REQUIRED_FOR_THIS_ACTION)
                        .setPlayer(player)
                        .setProfession(this)
                        .build());
            }
        }
        return playerHasProf;
    }

    /**
     * @param e               event to check for
     * @param <ItemTypeClass> the item type
     * @return {@code true} if event has registered an object of item type, {@code false} otherwise
     */
    protected final <ItemTypeClass extends ItemType<?>> boolean isValidEvent(ProfessionEvent<ItemTypeClass> e) {
        return isValidEvent(e, true);
    }

    protected final <ItemTypeClass extends ItemType<?>> boolean playerHasProfession(ProfessionEvent<ItemTypeClass> e) {
        return e.getPlayer().hasProfession(this);
    }

    /**
     * @param e the event to check the player's requirements in
     * @return {@code true} if the player meets all requirements, {@code false} otherwise
     */
    protected final <ItemTypeClass extends ItemType<?>> boolean playerMeetsLevelRequirements(ProfessionEvent<ItemTypeClass> e) {
        if (!playerHasProfession(e)) {
            return false;
        }
        ItemTypeClass obj = e.getItemType();
        UserProfessionData upd = getUserProfessionData(e.getPlayer());
        return obj.meetsLevelReq(upd.getLevel()) || upd.getUser().isBypass();
    }

    /**
     * Compares the profession types and then the names.
     *
     * @param o the profession to compare
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(Profession o) {
        int compare = getProfessionType().compareTo(o.getProfessionType());
        if (compare == 0) {
            return ChatColor.stripColor(getColoredName()).compareTo(ChatColor.stripColor(o.getColoredName()));
        }
        return compare;
    }

    /**
     * Called after being loaded from a file
     */
    public void onLoad() {
    }

    /**
     * Called when a user level ups
     */
    public void onLevelUp(UserProfessionData upd) {

    }

    public final Set<String> getRequiredPlugins() {
        return requiredPlugins;
    }

    protected void addRequiredPlugin(String plugin) {
        requiredPlugins.add(plugin);
    }

    /**
     * Handles the called profession event from {@link git.doomshade.professions.listeners.ProfessionListener} <br>
     * Cancels the event if the player does not have this profession and is a rank lower than builder <br>
     * If the player has this profession and the profession event is correct, {@link #onEvent(ProfessionEventWrapper)} is called
     *
     * @param event   the profession event
     * @param <IType> the item type argument of the event (this prevents wildcards)
     */
    @EventHandler
    public <IType extends ItemType<?>> void handleEvent(ProfessionEvent<IType> event) {

        // the player has no profession but has a rank builder+ -> do not cancel the event
        if (!playerHasProfession(event)) {

            // cancels the event if the player is a rank lower than builder
            event.setCancelled(!Permissions.has(event.getPlayer().getPlayer(), Permissions.BUILDER));
            return;
        }
        for (ItemTypeHolder<?> ith : items) {
            for (ItemType<?> it : ith) {
                if (it.getClass().equals(event.getItemType().getClass())) {
                    onEvent(new ProfessionEventWrapper<>(event));
                    return;
                }
            }
        }
    }

    public abstract <IType extends ItemType<?>> void onEvent(ProfessionEventWrapper<IType> e);

    @Nullable
    public List<String> getProfessionInformation(UserProfessionData upd) {
        return null;
    }

    /**
     * The profession types
     */
    public enum ProfessionType implements ISetup {
        PRIMARY("Primary"),
        SECONDARY("Secondary");

        private String name;

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
                sb.append(type.ordinal()).append("=").append(type.toString());
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
                sb.append(type.ordinal()).append("=").append(type.toString());
            }
            sb.append(")");
            throw new IllegalArgumentException(sb.toString());
        }

        @Override
        public String toString() {
            return String.valueOf(name.toCharArray()[0]).toUpperCase() + name.toLowerCase().substring(1);
        }

        @Override
        public void setup() {
            PRIMARY.name = new Messages.MessageBuilder(Messages.Global.PROFTYPE_PRIMARY).build();
            SECONDARY.name = new Messages.MessageBuilder(Messages.Global.PROFTYPE_SECONDARY).build();
        }
    }
}
